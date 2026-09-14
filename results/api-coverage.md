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
| `Skills` + `SkillTracker` | `bot.api.Skills` (boosted + XP-derived); `bot.api.SkillTracker` (gained, xp/hr, time-to-target, injectable clock) | Covered (smoke) |
| `Inventory`, `Equipment`, containers | `bot.api.Inventory/Bank/Equipment` (incl. `inSlot`) | Covered (smoke) |

## Acting (`api/methods/{walking,combat,magic,prayer,tabs,dialogues,widget}`, `api/input`)

| DreamBot | Fork | Status |
|---|---|---|
| Menu interaction | `Actions` builders (compiler-checked entries) + install + Robot dispatch | Covered offline; dispatch params want one live pass |
| Local + web pathfinding (98 files) | `PathFinder` (A*, bits read from RL's own `MovementFlag`: `results/movementflag-clinit.txt`) + `Walking.walkPath`; web/server queries | Pathfinding covered (smoke incl. wall/gap/unreachable); web out (no server) |
| `Magic` (4 books, costs) | `Magic` home teleports for all five books (verified widgets) + 180-spell `cast` (level-gated) | Home teleports + slots covered (dispatch game-only); rune costs deferred (decrypted) |
| `Prayers` | quick-pray orb + `isActive` + per-prayer `toggle`/`activate` via mined book table (group 541, `results/prayer-widget-table.txt`) + quick-prayer `selectQuick` | Covered (lookup offline; dispatch game-only); Ruinous book deferred |
| `Tabs` | all fixed tabs + resizable inv/prayer, layout-aware | Covered (compiles; clicks game-only) |
| `Dialogues` | `Dialogs` (widget state + space/number-key input) | Covered (smoke state reads) |
| `GrandExchange` (excl. `LivePrices`) | `GrandExchange` reads, screens, search, qty/price, confirm, abort, collect + collect-to-bank, full `buyOffer`/`sellOffer` flows | Covered (builders + lookup offline; dispatch game-only) |
| `Trade`, `DepositBox` | `Trade` (open/accept/decline/tradeWith, both stages), `DepositBox` (open/deposit-all ×3/close/slots) | Covered (lookup offline; dispatch game-only) |
| `Combat` / `CombatStyle` | `Combat` level, spec %/active/toggle, retaliate read/set, style read/set, poisoned, envenomed (varps 300/301/172/43/102, probe-verified widgets) | Covered (reads + builders offline; dispatch game-only); envenom band is strong inference |
| Widget search + Smithing/ItemProcessing helpers | `Widgets` recursive text/action/id search | Covered (structure); trade-skill helpers deferred |
| Mouse/Keyboard | `Actions.click`, `Mouse.move`, `Keyboard.type` | Covered (builders offline; dispatch game-only) |
| Camera/Minimap | `Camera` yaw/pitch targets; minimap projection in `Actions` | Covered (compiles; game-only) |
| Varbits/varps/settings | `Vars` (+ energy, prayer, health%) | Covered (smoke defaults) |
| Areas, Sleep, Timing, Calculations | `bot.api.Area`, `bot.util.*` | Covered (smoke) |
| Emotes | `Emotes.perform` + `Emote` enum (54 mined slots, group 216) | Covered (lookup offline; dispatch game-only) |
| Hint arrows | `HintArrows` reads + clear (DreamBot `HintArrow` parity) | Covered (smoke nulls) |
| Friends/ignores | `Friends` reads (list, size, have, ignores) + tab | Covered (smoke empties); add/delete/message deferred (chatbox flows) |
| Friends-chat/clan | `ClanChat` reads (inChat, name, owner, members, guild) + tab | Covered (smoke nulls); join/leave deferred |
| Random events | `RandomEvents.dismiss` (Continue-boxes only — NOT a solver) | Covered fail-clean (smoke); solvers permanently out |
| Shop | `Shop` reads, buy/sell 1-5-10-50, keeper open, escape close (group 300, probe-verified) | Covered (lookup offline; dispatch game-only) |
| Smithing | `Smithing` reads, item click, anvil open (group 312, probe-verified; no DreamBot class exists) | Covered (lookup offline; dispatch game-only) |
| Fairy rings | `FairyRings` dial reads, rotate, code entry, travel (mined varp-816 mechanics, `results/fairy-ring-table.txt`) | Covered (reads + builders offline; dispatch game-only); log matching deferred |
| Quests | `Quests.questPoints` (varp 101) + tab open | Covered (smoke); per-quest states deferred (decrypted color map) |
| Diaries | `Diaries` 12 areas x 4 tiers (mined varbits, `results/diary-varbit-table.txt`) | Covered (smoke) |
| Minigames | `Minigames` open check, selection read, name-matched teleport (group 76) | Covered (lookup offline; dispatch game-only); clan-tab opener deferred |
| Combat spells | `Spell` enum (180 mined slots, all books) + `Magic.cast/canCast` (level gate; rune costs decrypted-out) | Covered (lookup offline; dispatch game-only) |
| Quick prayers | `Prayers.quickChild/selectQuick` (mined slots, probe-matched root) | Covered (lookup offline; dispatch game-only) |
| Envenom | `Combat.isEnvenomed/poisonValue` (varp-102 venom band per RL `POISON` scale) | Strong inference (smoke); needs a live pass |
| Login | `Login` state/logged-in/login/logout (runtime credentials only) + launcher env auto-login | Covered fail-clean (smoke); real login needs a live pass |
| Worlds | `Worlds` list reads, filters, emptiest, switcher open, row-matched hop (group 69 mined + probe-matched) | Covered (lookup offline; dispatch game-only); no `JSocket` by design |

## Widget-id provenance

All interface ids (`bot.api.WidgetIds`) were read out of DreamBot's own API
bytecode, not guessed: `results/javap-c-db-widgetapis.txt` holds the
`GrandExchange`/`Trade`/`DepositBox`/`WidgetChild`/`Widgets` dumps,
`results/widget-id-sites.txt` the extracted (group, child) call sites
(`tools/ExtractWidgetIds.py`), and `results/phase1-reverification.md` §1 the
decision log. Group ids are game content ids; child semantics come from the
enclosing DreamBot method names (openBuyScreen, cancelOffer, depositAllItems…).
Every flow built on them still wants one live pass.

## Explicitly out (non-goals, unchanged)

- `LivePrices`, `AccountManager`/switching/vault, `ClientSettings` sync,
  `JSocket` server comms, telemetry, SDN deploy — server, auth-storage, or
  exfiltration scope. Login takes runtime-only credentials (never stored,
  never logged); world hopping drives the in-game switcher only. The fork
  runs local scripts and nothing else.
- Random-event *puzzle* solvers beyond dismiss/continue flows: DreamBot's
  own set is dismiss-shaped, and anything deeper stays out.
- Still to implement (in scope, recon done): the randoms framework
  (dismiss/continue/pin-at-runtime/welcome/login-retry solvers —
  DreamBot's own set is dismiss-shaped).
- Deferred niche content (needs one live pass each): per-quest states
  (decrypted color map), bonds (no static ids and no pinned constants),
  Ruinous prayers (no DreamBot-side table), social mutations (friend
  add/delete, chat join/leave/message), fairy travel-log matching,
  clan-tab opener.
  Each is a widget-data task a script author can add following the
  `Bank`/`Widgets` pattern; none is architectural.
