# Writing local scripts

Scripts are plain Java against `bot.script` + `bot.api`, loaded from jars you
drop in a directory. No server, no account system, no store: the directory is
the catalog. `src/sample/java/sample/CowHitter.java` is the worked combat example,
`src/sample/java/sample/Woodcutter.java` the gathering one (chop until full),
and `src/sample/java/sample/BankRunner.java` the banking one (open booth,
deposit, stop); everything below refers to them.

## Prerequisites

- JDK 17 (`gradlew` uses `gradle.properties`' pinned Temurin automatically).
- The pinned game jars under `%USERPROFILE%\DreamBot\BotData`
  (`gradlew verifyArtifacts` proves them; re-run after any DreamBot update).

## Your first script

```java
package sample;

import bot.api.Actions;
import bot.api.Game;
import bot.api.Npcs;
import bot.script.Script;
import bot.script.ScriptManifest;
import net.runelite.api.NPC;

@ScriptManifest(name = "CowHitter", version = "1.0",
    description = "Attacks the nearest cow.", author = "you")
public final class CowHitter extends Script {

    private static final int COW = 2805;

    @Override
    public int onLoop() {
        if (!Game.ready() || !Game.me().isIdle()) {
            return 600;                       // not our turn yet
        }
        NPC cow = Npcs.nearestWithin(10, COW);
        if (cow == null) {
            return 600;                       // none nearby
        }
        try {
            Actions.npc(cow, "Attack");       // left-click first option
        } catch (Exception e) {
            return 2000;                      // click failed, back off
        }
        return 1200;
    }
}
```

Three rules, no exceptions:

1. **`onLoop` returns a sleep, never sleeps itself.** Return `<= 0` to stop.
   A `Thread.sleep` inside `onLoop` hangs the runner; keep a `lastClick`
   timestamp field for "every few seconds" logic.
2. **Check `Game.ready()` first**, then `Game.me().isIdle()` before acting —
   acting mid-animation stacks inputs.
3. **Search before you act.** `nearestWithin` returns null; acting on null
   does nothing but thinking it can't be null is how scripts click the void.

## What a script can see and do (`bot.api`)

| Area | Entry points |
|---|---|
| World state | `Game.ready()`, `Game.me()`, `Game.client()` (raw API escape hatch) |
| Finding NPCs | `Npcs.nearest(ids...)`, `Npcs.nearestWithin(range, ids...)`, `Npcs.withId(ids...)` |
| Players | `Players.nearest()` (others only), local player via `Game.me()` |
| Scenery | `GameObjects.nearest(ids...)` — walks the scene grid, so no typed-in tiles |
| Ground loot | `GroundItems.nearest/nearestWithin(ids...)` — returns a `Loot` (item + tile, since items carry no position); `Actions.take(loot, "Take", 2)` |
| Self | `Local`: `location()`, `worldX/Y()`, `animation()`, `isIdle()`, `combatLevel()` |
| Skills | `Skills.level/base/experience(Skill)` (boosted vs real, like the skill tab) |
| Inventory | `Inventory.count/contains/full/items()`, `Inventory.health()` (current HP) |
| Bank | `Bank.isOpen/count/contains`, `openBooth/openBanker(ids...)`, `depositInventory/depositEquipment()`, `close()` (escape) — widget clicks game-only |
| Exchange | `GrandExchange.isOpen/offers/active/isDone/progress`, `openClerk(ids...)`, `collectAll()` (clicks the game's own Collect actions). Creating buy/sell offers is deferred (search/qty/price flow wants a live pass) |
| Equipment | `Equipment.equipped/count` (worn items) |
| Acting | `Actions.npc(npc, option)`, `Actions.walkTo(worldPoint)` (both via real menu entries + mouse — game-only) |
| Walking | `Walking.findPath(goal)` (A* over collision) and `walkPath(goal)` (stepped, waits arrival) — game-only |
| Dialogs | `Dialogs.isOpen/choosing`, `continueDialogue()` (space), `chooseOption(n)` (number keys) — game-only input |
| State | `Vars.varbit/varp`, `runEnergy()`, `prayerActive()`, `healthPercent()` |
| Widgets/tabs | `Widgets.findByText/findByAction/first`, `Tabs` (all fixed tabs), `Camera` yaw/pitch |
| Combat/prayer/magic | `Combat.isInCombat`, `Prayers` (quick-pray + active), `Magic` (home teleports) |
| Equipment slots | `Equipment.inSlot(slot)` |
| Zones/timing | `Area` (contains/center/random), `Sleep`, `Timing.waitCondition`, `Calculations` |
| Menus (advanced) | `Actions.npcMenu/objectMenu/groundItemMenu/widgetMenu` build entries; `Actions.install` sets them |

Entity fields (`id`, `getWorldLocation`, `getAnimation`, names) come from
RuneLite's maintained mappings — no offset files, no re-deriving after updates.

## Drawing and settings

- Extend `bot.ui.ScriptOverlay`, implement `paint(Graphics2D)`, register it:
  drawing only, never game logic in `paint`.
- Declare a `bot.ui.ScriptConfig`-style `@ConfigGroup` interface for settings;
  the client renders the controls.

## Checking your script without the game

```bat
gradlew build smokeTest
```

`smokeTest` loads every sample jar, runs `Hello` against a stub client
(5-loops-then-stop, range search, menu fields — 9 checks), all offline.

## Running for real

```bat
gradlew dist
gradlew run -Pscripts=scripts -Pscript=CowHitter
```

`run` boots the pinned client and needs the live game (network + login).
`Main` waits up to 10 minutes for a logged-in player and exits without
running anything if none appears; a script's `overlay()` is registered for
the run automatically. Drop compiled script jars in `scripts/` (see its
README); `gradlew checkRunClasspath` proves the launch classpath resolves
without starting the client.
Dispatch details (menu params, click timing) want one in-game confirmation
pass each — see `bot.api.Actions` javadoc for exactly what is and isn't
proven. Nothing here touches identity, telemetry, credentials, or packets,
and nothing ever will: those are documented non-goals (see `docs/fork-plan.md`).
