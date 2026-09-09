# DreamBot API coverage — 2026-09-09

Source of the "original API": `org/dreambot/api/**` entries in
`repository2/dreambot-client.jar` (see `results/tf-dreambot-client.txt`).
Each row is one DreamBot area, its fork counterpart, and the evidence level.
"Covered" means implemented against the pinned RuneLite API with every name
compiler-checked, plus offline smoke coverage where the logic is pure.
Deferred items are niche content, server-backed features, or auth scope —
never cut for convenience on the core botting surface.

## Scripting core

| DreamBot (`api/script`, `api/settings`) | Fork | Status |
|---|---|---|
| `AbstractScript`, `ScriptManifest` (start/loop/exit) | `bot.script.Script`, `ScriptManifest`, `ScriptRunner` | Covered (smoke) |
| `ScriptManager` (discovery, state) | `bot.script.ScriptLoader` (dir scan, per-jar loaders, reload/unload) | Covered (smoke) |
| `Category`, `TaskNode`, frameworks, SDN deploy | — | Out (server scope) |
| `ScriptSettings`, `ClientSettings` | `bot.ui.ScriptConfig` (`@ConfigGroup` auto-UI) | Covered (compiles; UI is client-rendered) |

## Entities and search (`api/wrappers`, `api/methods/{skills,item,interactive,animations,container}`)

| DreamBot | Fork | Status |
|---|---|---|
| NPC/Player/GameObject/GroundItem wrappers | Used directly (RL interfaces *are* the wrappers) + `GroundItems.Loot` (item+tile, since `TileItem` carries no position — proven by compiler) | Covered (smoke) |
| `NPCs`/`Players`/`GameObjects`/`GroundItems` queries | `bot.api.Npcs/Players/GameObjects/GroundItems` (scene-grid walks, range filters) | Covered (smoke) |
| `Skills` + `SkillTracker` | `bot.api.Skills` (boosted + XP-derived); tracker (XP/hr over time) | Covered / tracker future |
| `Inventory`, `Equipment`, containers | `bot.api.Inventory/Bank/Equipment` (incl. `inSlot`) | Covered (smoke) |

## Acting (`api/methods/{walking,combat,magic,prayer,tabs,dialogues,widget}`, `api/input`)

| DreamBot | Fork | Status |
|---|---|---|
| Menu interaction | `Actions` builders (compiler-checked entries) + install + Robot dispatch | Covered offline; dispatch params want one live pass |
| Local + web pathfinding (98 files) | `PathFinder` (A*, bits read from RL's own `MovementFlag`: `results/movementflag-clinit.txt`) + `Walking.walkPath`; web/server queries | Pathfinding covered (smoke incl. wall/gap/unreachable); web out (no server) |
| `Magic` (4 books, costs) | `Magic` home teleports (verified widgets only) | Partial; full books deferred, not guessed |
| `Prayers` | quick-pray orb + `isActive` | Partial; per-prayer book clicks deferred |
| `Tabs` | all fixed tabs + resizable inv/prayer, layout-aware | Covered (compiles; clicks game-only) |
| `Dialogues` | `Dialogs` (widget state + space/number-key input) | Covered (smoke state reads) |
| `GrandExchange` (excl. `LivePrices`) | `GrandExchange` reads + clerk open + Collect-all (no hardcoded button ids); offer *creation* deferred | Covered (smoke); creation wants a live pass |
| `Combat` / `CombatStyle` | `Combat.isInCombat` (both directions) | Partial; styles deferred |
| Widget search + Smithing/ItemProcessing helpers | `Widgets` recursive text/action/id search | Covered (structure); trade-skill helpers deferred |
| Mouse/Keyboard | `Actions.click`, `Mouse.move`, `Keyboard.type` | Covered (builders offline; dispatch game-only) |
| Camera/Minimap | `Camera` yaw/pitch targets; minimap projection in `Actions` | Covered (compiles; game-only) |
| Varbits/varps/settings | `Vars` (+ energy, prayer, health%) | Covered (smoke defaults) |
| Areas, Sleep, Timing, Calculations | `bot.api.Area`, `bot.util.*` | Covered (smoke) |

## Explicitly out (non-goals, unchanged)

- `randoms/*` solvers, `LoginUtility`, `WorldHopper`/`JSocket`, `LivePrices`,
  `AccountManager`, telemetry, SDN deploy, `ClientSettings` sync — server,
  auth, or evasion-adjacent scope. The fork runs local scripts under the
  launcher's login and nothing else.
- Deferred niche content (no core loop needs them): offer *creation*,
  `Trade`, `DepositBox`, quest book data, minigames, sailing, favour, music,
  emotes, diaries, fairy rings, hint arrows, clan/friend/ignore, bonds,
  `SkillTracker`, per-prayer toggles, full spellbooks, Smithing helpers.
  Each is a widget-data task a script author can add following the
  `Bank`/`Widgets` pattern; none is architectural.
