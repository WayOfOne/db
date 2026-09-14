package bot.script;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.inject.Injector;

import bot.api.Game;
import bot.script.listener.ActionListener;
import bot.script.listener.AnimationListener;
import bot.script.listener.BreakListener;
import bot.script.listener.ChatListener;
import bot.script.listener.ExperienceListener;
import bot.script.listener.GameStateListener;
import bot.script.listener.GameTickListener;
import bot.script.listener.HitSplatListener;
import bot.script.listener.ItemContainerListener;
import bot.script.listener.LoginListener;
import bot.script.listener.MenuRowListener;
import bot.script.listener.ProjectileListener;
import bot.script.listener.RSScriptEventListener;
import bot.script.listener.RegionLoadListener;
import bot.script.listener.RenderListener;
import bot.script.listener.RuneScriptListener;
import bot.script.listener.SpawnListener;
import bot.script.listener.VarListener;
import bot.script.listener.WidgetEventListener;
import bot.script.listener.WorldViewListener;
import net.runelite.api.Actor;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Projectile;
import net.runelite.api.Skill;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemQuantityChanged;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.events.WorldChanged;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

/** Bridges the RuneLite event bus to script listener interfaces. A script
 * implementing any `bot.script.listener.*` interface gets callbacks while
 * registered (the runner registers for the run lifetime). Stateful
 * derivations — XP deltas, level-ups, container diffs, first-seen
 * projectiles, login/region edges — live here so scripts stay declarative.
 * Pure derivations are package-visible static helpers, unit-tested. */
public final class Events {
    private Events() {
    }

    private static final CopyOnWriteArrayList<Object> REGISTERED = new CopyOnWriteArrayList<>();
    private static volatile EventBus bus;

    private static final Map<Skill, int[]> XP = new EnumMap<>(Skill.class);
    private static final Map<Integer, Map<Integer, int[]>> CONTAINERS = new HashMap<>();
    private static final Set<Integer> SEEN_PROJECTILES = new HashSet<>();
    private static volatile GameState lastState;

    /** Register a script (or any listener holder). Works offline too:
     * targets always list; bus subscription only when a bus exists. */
    public static synchronized void register(Object target) {
        if (REGISTERED.contains(target)) {
            return;
        }
        EventBus b = bus();
        if (b != null) {
            b.register(target);
        }
        REGISTERED.add(target);
    }

    /** Unregister (also clears per-run derivation state). */
    public static synchronized void unregister(Object target) {
        EventBus b = bus;
        if (b != null) {
            try {
                b.unregister(target);
            } catch (Exception e) {
                // already gone; list removal below still applies
            }
        }
        REGISTERED.remove(target);
        XP.clear();
        synchronized (CONTAINERS) {
            CONTAINERS.clear();
        }
        SEEN_PROJECTILES.clear();
        lastState = null;
    }

    /** All currently registered targets (for solver broadcasts). */
    public static java.util.List<Object> registered() {
        return REGISTERED;
    }

    private static EventBus bus() {
        if (bus == null) {
            try {
                Injector injector = Game.injector();
                bus = injector == null ? null : injector.getInstance(EventBus.class);
            } catch (Exception e) {
                bus = null;
            }
        }
        return bus;
    }

    /** Break broadcasts for `BreakSolver` (no bus event exists). */
    public static void fireBreakStart() {
        for (Object t : REGISTERED) {
            if (t instanceof BreakListener l) {
                l.onBreakStart();
            }
        }
    }

    /** Break broadcasts for `BreakSolver` (no bus event exists). */
    public static void fireBreakEnd() {
        for (Object t : REGISTERED) {
            if (t instanceof BreakListener l) {
                l.onBreakEnd();
            }
        }
    }

    // -- Derivation helpers (pure, unit-tested) --

    /** [gained, levelDelta] for a skill sample, updating the tracker. */
    public static int[] xpDelta(Skill skill, int xp, int level) {
        int[] last = XP.get(skill);
        int gained = 0;
        int delta = 0;
        if (last != null) {
            gained = Math.max(0, xp - last[0]);
            delta = level - last[1];
        }
        XP.put(skill, new int[] {xp, level});
        return new int[] {gained, delta};
    }

    /** Slot -> [id, qty] snapshot of a container (null-safe). */
    public static Map<Integer, int[]> snapshot(ItemContainer container) {
        Map<Integer, int[]> out = new HashMap<>();
        if (container == null || container.getItems() == null) {
            return out;
        }
        Item[] items = container.getItems();
        for (int i = 0; i < items.length; i++) {
            Item item = items[i];
            if (item != null) {
                out.put(i, new int[] {item.getId(), item.getQuantity()});
            }
        }
        return out;
    }

    /** Diff two snapshots: [addedId:qty..., removedId:qty...] as int[] pairs
     * flattened [added..., removed...] with a split index returned alongside.
     * Simpler contract: returns added map and fills removed map. */
    public static Map<Integer, Integer> diffAdded(Map<Integer, int[]> before,
            Map<Integer, int[]> after, Map<Integer, Integer> removed) {
        Map<Integer, Integer> added = new HashMap<>();
        java.util.Set<Integer> slots = new java.util.HashSet<>();
        slots.addAll(before.keySet());
        slots.addAll(after.keySet());
        for (int slot : slots) {
            int[] b = before.get(slot);
            int[] a = after.get(slot);
            int bId = b == null ? -1 : b[0];
            int bQty = b == null ? 0 : b[1];
            int aId = a == null ? -1 : a[0];
            int aQty = a == null ? 0 : a[1];
            if (aId != bId) {
                if (bId > 0) {
                    removed.merge(bId, bQty, Integer::sum);
                }
                if (aId > 0) {
                    added.merge(aId, aQty, Integer::sum);
                }
            } else if (aQty != bQty && aId > 0) {
                if (aQty > bQty) {
                    added.merge(aId, aQty - bQty, Integer::sum);
                } else {
                    removed.merge(aId, bQty - aQty, Integer::sum);
                }
            }
        }
        return added;
    }

    // -- Bus subscriptions (one instance serves all registrants) --

    /** The bus subscriber; registered once, fans out to registrants. */
    public static final class FanOut {
        /** Register the fan-out exactly once per process. */
        public static synchronized void ensure() {
            EventBus b = bus();
            if (b == null) {
                return;
            }
            for (Object o : REGISTERED) {
                if (o instanceof FanOut) {
                    return;
                }
            }
            FanOut fan = new FanOut();
            b.register(fan);
            REGISTERED.add(fan);
        }

        @Subscribe
        public void onChat(ChatMessage e) {
            for (Object t : REGISTERED) {
                if (t instanceof ChatListener l) {
                    ChatListener.dispatch(l, e);
                }
            }
        }

        @Subscribe
        public void onGameTick(GameTick e) {
            for (Object t : REGISTERED) {
                if (t instanceof GameTickListener l) {
                    l.onGameTick();
                }
            }
        }

        @Subscribe
        public void onClientTick(ClientTick e) {
            for (Object t : REGISTERED) {
                if (t instanceof GameTickListener l) {
                    l.onClientTick();
                }
            }
        }

        @Subscribe
        public void onStat(StatChanged e) {
            Skill skill = e.getSkill();
            int[] d = xpDelta(skill, e.getXp(), e.getLevel());
            for (Object t : REGISTERED) {
                if (t instanceof ExperienceListener l) {
                    if (d[0] > 0) {
                        l.onGained(skill, d[0], e.getXp());
                    }
                    int lastLevel = e.getLevel() - d[1];
                    if (d[1] > 0) {
                        l.onLevelUp(skill, e.getLevel());
                    }
                    if (d[1] != 0) {
                        l.onLevelChange(skill, lastLevel, e.getLevel());
                    }
                }
            }
        }

        @Subscribe
        public void onAnimation(AnimationChanged e) {
            Actor actor = e.getActor();
            if (actor == null) {
                return;
            }
            int id = actor.getAnimation();
            for (Object t : REGISTERED) {
                if (t instanceof AnimationListener l) {
                    if (actor instanceof Player p) {
                        l.onPlayerAnimation(p, id);
                    } else if (actor instanceof NPC n) {
                        l.onNpcAnimation(n, id);
                    }
                }
            }
        }

        @Subscribe
        public void onHitsplat(HitsplatApplied e) {
            int damage = e.getHitsplat() == null ? 0 : e.getHitsplat().getAmount();
            for (Object t : REGISTERED) {
                if (t instanceof HitSplatListener l) {
                    l.onHitSplat(e.getActor(), damage);
                }
            }
        }

        @Subscribe
        public void onVarbit(VarbitChanged e) {
            for (Object t : REGISTERED) {
                if (t instanceof VarListener l) {
                    l.onVarbitUpdate(e.getVarbitId(), e.getValue());
                }
            }
        }

        @Subscribe
        public void onNpcSpawn(NpcSpawned e) {
            for (Object t : REGISTERED) {
                if (t instanceof SpawnListener l) {
                    l.onNpcSpawn(e.getNpc());
                }
            }
        }

        @Subscribe
        public void onNpcDespawn(NpcDespawned e) {
            for (Object t : REGISTERED) {
                if (t instanceof SpawnListener l) {
                    l.onNpcDespawn(e.getNpc());
                }
            }
        }

        @Subscribe
        public void onPlayerSpawn(PlayerSpawned e) {
            for (Object t : REGISTERED) {
                if (t instanceof SpawnListener l) {
                    l.onPlayerSpawn(e.getPlayer());
                }
            }
        }

        @Subscribe
        public void onPlayerDespawn(PlayerDespawned e) {
            for (Object t : REGISTERED) {
                if (t instanceof SpawnListener l) {
                    l.onPlayerDespawn(e.getPlayer());
                }
            }
        }

        @Subscribe
        public void onGameObjectSpawn(GameObjectSpawned e) {
            for (Object t : REGISTERED) {
                if (t instanceof SpawnListener l) {
                    l.onGameObjectSpawn(e.getGameObject());
                }
            }
        }

        @Subscribe
        public void onGameObjectDespawn(GameObjectDespawned e) {
            for (Object t : REGISTERED) {
                if (t instanceof SpawnListener l) {
                    l.onGameObjectDespawn(e.getGameObject());
                }
            }
        }

        @Subscribe
        public void onItemSpawn(ItemSpawned e) {
            for (Object t : REGISTERED) {
                if (t instanceof SpawnListener l) {
                    l.onGroundItemSpawn(e.getItem());
                }
            }
        }

        @Subscribe
        public void onItemDespawn(ItemDespawned e) {
            for (Object t : REGISTERED) {
                if (t instanceof SpawnListener l) {
                    l.onGroundItemDespawn(e.getItem());
                }
            }
        }

        @Subscribe
        public void onItemQuantity(ItemQuantityChanged e) {
            for (Object t : REGISTERED) {
                if (t instanceof SpawnListener l) {
                    l.onGroundItemUpdate(e.getItem(), e.getOldQuantity(), e.getNewQuantity());
                }
            }
        }

        @Subscribe
        public void onProjectileMoved(ProjectileMoved e) {
            Projectile p = e.getProjectile();
            if (p != null && SEEN_PROJECTILES.add(p.getId())) {
                for (Object t : REGISTERED) {
                    if (t instanceof SpawnListener l) {
                        l.onProjectileSpawn(p);
                    }
                }
            }
            for (Object t : REGISTERED) {
                if (t instanceof ProjectileListener l) {
                    l.onProjectileMoved(p);
                }
            }
        }

        @Subscribe
        public void onContainer(ItemContainerChanged e) {
            int id = e.getContainerId();
            boolean inv = id == InventoryID.INVENTORY.getId();
            boolean equip = id == InventoryID.EQUIPMENT.getId();
            if (!inv && !equip) {
                return;
            }
            Map<Integer, int[]> before;
            synchronized (CONTAINERS) {
                before = CONTAINERS.getOrDefault(id, new HashMap<>());
                Map<Integer, int[]> after = snapshot(e.getItemContainer());
                CONTAINERS.put(id, after);
                Map<Integer, Integer> removed = new HashMap<>();
                Map<Integer, Integer> added = diffAdded(before, after, removed);
                for (Object t : REGISTERED) {
                    if (t instanceof ItemContainerListener l) {
                        if (inv) {
                            l.onInventoryChanged(e.getItemContainer());
                            added.forEach(l::onInventoryItemAdded);
                            removed.forEach(l::onInventoryItemRemoved);
                        } else {
                            l.onEquipmentChanged(e.getItemContainer());
                            added.forEach(l::onEquipmentItemAdded);
                            removed.forEach(l::onEquipmentItemRemoved);
                        }
                    }
                }
            }
        }

        @Subscribe
        public void onMenuEntryAdded(MenuEntryAdded e) {
            for (Object t : REGISTERED) {
                if (t instanceof MenuRowListener l) {
                    l.onMenuEntryAdded(e.getMenuEntry());
                }
            }
        }

        @Subscribe
        public void onMenuOptionClicked(MenuOptionClicked e) {
            for (Object t : REGISTERED) {
                if (t instanceof MenuRowListener l) {
                    l.onMenuOptionClicked(e.getMenuOption(), e.getMenuTarget());
                }
                if (t instanceof ActionListener l) {
                    l.onAction(e.getMenuOption(), e.getMenuTarget());
                }
            }
        }

        @Subscribe
        public void onGameStateChanged(GameStateChanged e) {
            GameState old = lastState;
            GameState now = e.getGameState();
            lastState = now;
            for (Object t : REGISTERED) {
                if (t instanceof GameStateListener l) {
                    l.onGameStateChange(old, now);
                }
                if (t instanceof LoginListener l) {
                    if (now == GameState.LOGGED_IN && old != GameState.LOGGED_IN) {
                        l.onLogin();
                    } else if (now != GameState.LOGGED_IN && old == GameState.LOGGED_IN) {
                        l.onLogout();
                    }
                }
                if (t instanceof RegionLoadListener l) {
                    if (old == GameState.LOADING
                        && (now == GameState.LOGGED_IN || now == GameState.HOPPING)) {
                        l.onRegionLoad();
                    }
                }
            }
        }

        @Subscribe
        public void onWidgetLoaded(WidgetLoaded e) {
            for (Object t : REGISTERED) {
                if (t instanceof WidgetEventListener l) {
                    l.onWidgetLoaded(e.getGroupId());
                }
            }
        }

        @Subscribe
        public void onWidgetClosed(WidgetClosed e) {
            for (Object t : REGISTERED) {
                if (t instanceof WidgetEventListener l) {
                    l.onWidgetClosed(e.getGroupId());
                }
            }
        }

        @Subscribe
        public void onWorldChanged(WorldChanged e) {
            int world = Game.client() == null ? -1 : Game.client().getWorld();
            for (Object t : REGISTERED) {
                if (t instanceof WorldViewListener l) {
                    l.onWorldChanged(world);
                }
            }
        }

        @Subscribe
        public void onScriptPreFired(ScriptPreFired e) {
            for (Object t : REGISTERED) {
                if (t instanceof RuneScriptListener l) {
                    l.onScriptPreFired(e.getScriptId());
                }
            }
        }

        @Subscribe
        public void onScriptPostFired(ScriptPostFired e) {
            for (Object t : REGISTERED) {
                if (t instanceof RuneScriptListener l) {
                    l.onScriptPostFired(e.getScriptId());
                }
            }
        }

        @Subscribe
        public void onScriptCallback(ScriptCallbackEvent e) {
            for (Object t : REGISTERED) {
                if (t instanceof RSScriptEventListener l) {
                    l.onScriptCallback(e.getEventName());
                }
            }
        }

        @Subscribe
        public void onBeforeRender(BeforeRender e) {
            for (Object t : REGISTERED) {
                if (t instanceof RenderListener l) {
                    l.onRender();
                }
            }
        }
    }
}
