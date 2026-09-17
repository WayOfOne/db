# Porting DreamBot scripts to this fork

A DreamBot script cannot run unmodified: package names (`org.dreambot.api…`
vs `bot.api`/`bot.script`), wrapper types (ours are RuneLite's own), and a
few intentionally different surfaces (local jars, no SDN, runtime-only
credentials). But the port is mechanical — same structure, same method
names in most places. This guide covers the rewrites; anything not listed
here likely ports as an import swap.

## Assessment: why an adapter, not a shim

A drop-in `org.dreambot.api.*` compatibility layer (the SlugBot approach)
would need hundreds of delegation classes plus all wrapper types
(`NPC`, `Tile`, `Filter`, `WidgetChild`, …) re-created, and behavior
parity would still need the live pass. The fork instead keeps DreamBot's
*shapes* (method names, lifecycles, listener contracts, task/tree
frameworks) with RuneLite types, so ports are edits, not rewrites.
Typical script: imports + wrapper-type erasure + the table below.

## Imports

| DreamBot | Fork |
|---|---|
| `org.dreambot.api.script.AbstractScript` | `bot.script.Script` |
| `org.dreambot.api.script.ScriptManifest` | `bot.script.ScriptManifest` |
| `org.dreambot.api.script.TaskNode` | `bot.script.TaskNode` |
| `org.dreambot.api.script.frameworks.treebranch.*` | `bot.script.tree.*` (`Leaf`, `Branch`, `Root`, `TreeScript`) |
| `org.dreambot.api.script.listener.*` | `bot.script.listener.*` (same names) |
| `org.dreambot.api.methods.interactive.NPCs` etc. | `bot.api.Npcs`, `Players`, `GameObjects`, `GroundItems` |
| `org.dreambot.api.methods.container.impl.bank.Bank` | `bot.api.Bank` |
| `org.dreambot.api.methods FMisc…` | `bot.api.*` (same short names: `Combat`, `Prayers`, `Magic`, `Tabs`, `Dialogs`, `Shop`, `Friends`, …) |
| `org.dreambot.api.methods.map.Tile` | `net.runelite.api.coords.WorldPoint` |
| `org.dreambot.api.methods.map.Area` | `bot.api.Area` |
| `org.dreambot.api.methods.filter.Filter` | `java.util.function.Predicate` (our searches take ids/ranges; use `Widgets.findAll(pred)` for custom filters) |
| `org.dreambot.api.wrappers.*` | erased — use `net.runelite.api.*` directly (`NPC`, `Player`, `GameObject`, `TileItem`, `Widget`, `Item`) |
| `org.dreambot.api.methods.widget.WidgetChild` | `net.runelite.api.widgets.Widget` (+ `child()`/`getChild()`) |
| `org.dreambot.api.methods.quest.book.*` | `bot.api.Quest` (one enum, all books) |
| `org.dreambot.api.methods.magic.{Normal,…}` | `bot.api.Spell` (one enum, all books) |
| `org.dreambot.api.methods.emotes.Emote` | `bot.api.Emote` |
| `org.dreambot.api.methods.prayer.Prayer` | `net.runelite.api.Prayer` |
| `org.dreambot.api.methods.skill.Skill` | `net.runelite.api.Skill` |
| `org.dreambot.api.data.requirements.*` | `bot.util.Requirements` factories |

## Lifecycle

```java
// DreamBot                          // Fork
extends AbstractScript              extends Script  (or TreeScript)
@ScriptManifest(...)                @ScriptManifest(...)  (same fields)
onStart()/onLoop()/onExit()         identical
getRandomManager().disable(...)     Randoms.setEnabled(name, false)
addListener(...)                    implement bot.script.listener.* (auto-wired)
```

## Common calls

| DreamBot | Fork |
|---|---|
| `NPCs.closest("Cook")` / `closest(id)` | `Npcs.nearestNameWithin(range, "Cook")` / `nearestWithin(range, ids…)` |
| `npc.interact("Talk")` | `Actions.npc(npc, "Talk")` |
| `Players.localPlayer()` | `Game.me()` (`Local`: location/animation/idle/combatLevel) |
| `getInventory().count(id)` | `Inventory.count(id)` (same for contains/items) |
| `getBank().open()` / `depositAllItems()` | `Bank.openBooth/openBanker(ids…)` / `depositInventory()` |
| `getWalking().walk(tile)` | `Walking.walkPath(goal)` / `Actions.walkTo(tile)` |
| `getCombat().isInCombat()` | `Combat.isInCombat()` |
| `getPrayers().toggle(p)` | `Prayers.toggle(p)` / `activate(p)` |
| `getMagic().castSpell(s)` | `Magic.cast(spell)` |
| `getDialogues().clickContinue()` | `Dialogs.continueDialogue()` / `chooseOption(n)` |
| `getTabs().open(Tab.X)` | `Tabs.combat()/inventory()/…` or `Tabs.open(WidgetInfo…)` |
| `getWidgets().get(...)` | `Widgets.child(group, child)` / `findByText/findByAction/first` |
| `getGrandExchange()…` | `GrandExchange.…` (same shape incl. buyOffer/sellOffer) |
| `getTrade()…` / `getDepositBox()…` | `Trade.…` / `DepositBox.…` |
| `getShop()…` | `Shop.…` (buyOne/Five/Ten/Fifty, sell… ) |
| `getFriends().haveFriend(n)` | `Friends.haveFriend(n)` (+ add/delete/message now) |
| `getClanChat().join(n)` | `ClanChat.join(n)` / `leave()` |
| `getWorlds().hopWorld(id)` | `Worlds.hop(id)` |
| `getQuests().getQuestPoints()` | `Quests.questPoints()` (+ `Quest` data enum) |
| `getDiaries()` | `Diaries.finished(area, tier)` |
| `getRandomEvents()`/`getRandomManager()` | `Randoms.runOnce()` + `RandomEvents.dismiss()` |
| `getLogin().login(u, p)` | `Login.login(u, p, timeoutMs)` (runtime creds only) |
| `Sleep.sleep(a, b)` / `sleepUntil` | `bot.util.Sleep` / `Timing.waitCondition` |

## What has no counterpart (by design or pending live pass)

- SDN deploy/`Category`/server scope: local jars only.
- Stored accounts/switching/vault: runtime credentials only.
- Quest finished verdicts, bonds, Ruinous book, ignore mutations,
  clan-tab opener, roof/zoom/death handlers: live-gated (see
  `results/api-coverage.md`).
- Rune costs on spells, per-prayer setting bits, envenom damage curve:
  decrypted at runtime in DreamBot; level gates + thresholds documented.
- `HumanMouseListener`: Robot input has no event stream.
