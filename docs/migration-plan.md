# Migration Plan: bot-client DreamBot research -> this repo

Source: `C:\Users\thewa\Development\bot-client`
Target: this repo (`C:\Users\thewa\Development\dreambot`)
Jars: `%USERPROFILE%\DreamBot\BotData\` (user directory, never vendored here)

Status: the target repo was empty before this change. This plan ports the
reusable DreamBot knowledge out of bot-client (where it sits beside a native
`osclient.exe` injector: `client/offsets.hpp`, `launcher/`, `java/kewl/`) and
re-homes it where the DreamBot Java jars are the first-class target.

**Direction change (2026-09-09): the end goal is a RuneLite-based fork that
reimplements DreamBot-style scripting — see `docs/fork-plan.md`. Phases 0–4
below built the behavior-spec foundation; Phases 5–9 build the fork.**

## What moves, what stays

MOVE (DreamBot-specific, portable as knowledge):

| # | bot-client source | Lands here as | Notes |
|---|---|---|---|
| 1 | `docs/dreambot-random-dat-hwid-findings.md` (Findings 1–12, checklists) | `docs/dreambot-client-mappings.md` + this plan | Behavior map, not raw listings; re-verify per build |
| 2 | `docs/dreambot-sdn-script-findings.md` (catalog, loaders, 5 payload hashes) | `docs/dreambot-client-mappings.md` §6 | Catalog line re-verified 2026-09-09; payload count changed 5 -> 1 |
| 3 | `docs/dreambot-client-reverse-engineering-plan.md` (Phases 1–12) | `docs/migration-plan.md` (this file, phases below) | Scope trimmed to migration + re-verification, not a new RE campaign |
| 4 | `dreambot/deob/README.md` (workspace layout, helper list, repro rules) | `AGENTS.md` + `.agents/skills/dreambot-deob/SKILL.md` §2–3 | Helper sources (`ClassByteSearch`, `CpRefSearch`, …) ported on demand into `tools/` |
| 5 | `.agents/skills/deob/SKILL.md` (anchor-then-walk method, evidence levels) | `AGENTS.md` + `.agents/skills/dreambot-deob/SKILL.md` | Method ported native->JVM; Lua anchors replaced by constant-pool anchors |
| 6 | Injected-client map (`me.aw`, `aar/aac`, `jl.ab`, `bz.ag`, `mr.gf`, `ck.gu`, `client.lx`, `dd/gy`, `vt`, `xy.ie/dl`, …) | `docs/dreambot-client-mappings.md` §1–2 | Per-build obfuscated names; values are shapes, not constants |
| 7 | Runtime-transformer map (`4cs`, `82`, `4Xi` family, `30/0u`, `406/40o.2`, `0L/4Sl/7s`, `4kY`, `4z_`, `404`, `9J`, `9O9/9OT`, loaders, `AccountManager`) | `docs/dreambot-client-mappings.md` §3–5 | Encrypted annotation/string values are NOT VERIFIED until decrypted per build |

STAYS in bot-client (native injector, not this repo's problem):

- `client/offsets.hpp` (DO_ACTION/WORLD_TO_SCREEN RVAs, ENTITY_* struct offsets, BUILD_ID, opcodes) — native `osclient.exe` only.
- `client/*.hpp/*.cpp`, `launcher/`, `CMakeLists.txt`, `build.gradle`, `java/kewl/` — the injector + overlay + plugin API.
- `tools/ghidra_headless.ps1`, `tools/ida_headless.ps1`, `tools/ghidra_scripts/FindOffsets.java` — native binary workflow.
- `docs/deobfuscation-and-client-bridge-guide.md` — native bridge explainer; link it, don't copy it.
- `dreambot/deob/inputs/*` extracted trees — stale snapshot with old `D_Jok` paths; do not bulk-copy. Re-extract from `C:\Users\thewa\DreamBot\BotData\` when needed.

## Phase 0 — Scaffold (this change)

- [x] Create `AGENTS.md` (root agent file, DreamBot-jar target).
- [x] Create `.agents/skills/dreambot-deob/SKILL.md` (javap/ASM workflows, jar paths, probes).
- [x] Create `docs/dreambot-client-mappings.md` (all portable tables).
- [x] Create `docs/migration-plan.md` (this file).
- [x] Fingerprint current jars (table in `AGENTS.md`; 2026-09-09 values).
- [x] Toolchain: Temurin JDK 17.0.20.1 (`C:\Users\thewa\.jdks\temurin-17.0.20.1`),
  `bin` appended to user `PATH`; `jar tf` + `javap` smoke-tested against the
  injected jar (all six anchor classes resolve).
- [x] `git init` (branch `main`) + `.gitignore` (extracted trees, `tools/classes/`,
  account/secret patterns ignored; `listings/`, `results/`, docs committed).
- [x] Scaffold `listings/`, `results/`, `inputs/`, `tools/` (+ `mkdir` bootstrap
  in SKILL.md §0 so commands never fail on missing dirs).

Naming note: the root file is `AGENTS.md` (uppercase), not lowercase
`agent.md` — uppercase is the filename agents autoload; a lowercase
`agent.md` would sit unread. Same for the skill: `.agents/skills/<name>/SKILL.md`.

## Phase 1 — Re-verify anchors on current jars

- [x] Done 2026-09-09 (`results/phase1-reverification.md`; listings under
  `listings/`). Drift vs bot-client docs: setup writer `me.ao` -> `cl.az`,
  `jl.ab` callers +`client`, `pj` literal-only, loaders `9O9`/`9OT` -> `8Q`/`3Z`,
  id helper `4ky` lowercase, `40P` not a `4Xi`, 43 `0u` subclasses enumerated,
  SDN bin = 1 payload (hash + magic recorded). Still [NV]: `aar`->`406`
  rewrite, `40o.2` table, `4ky` path strings, P2P payload mapping, live opcodes.

For each row in `docs/dreambot-client-mappings.md`, re-run the listed
`javap`/`jar tf`/probe command against the 2026-09-09 jar hashes and mark
Confirmed static or Unknown. Priority order: `me.aw`/`jl.ab`/`bz.ag`,
`client.lx` readers/writers, `dd`/`gy` branch + `xy.dl`, `vt` commands,
`4cs`/`82`/`30`/`406`/`40o.2` chain, `AccountManager`/`4z_`/`404`,
`LocalLoader`/`9O9`/`9OT` + `scripts.dat` catalog line. Anything that moved
gets its new obfuscated name recorded with the jar hash — old names stay as
"last seen in build X", never deleted silently.

## Phase 2 — Recreate helper tools on demand (sources were never committed)

Recreate a helper in `tools/` only when a Phase-1 task needs it — there are no
sources to copy (bot-client holds specs only, see SKILL.md §2):

- [x] `tools/ClassByteSearch.py` recreated (Python stdlib port; case-safe zip
  scan; used for the 0u/4Xi/0L enumerations and negative sweeps).
`ClassByteSearch` -> anchor sweep; `CpRefSearch` -> field/method walk;
`SuperSearch` -> `0u` module enumeration; `AnnotationScan` -> `0L` map;
`Call40o` -> path-mapper probe; `TransformProbe` -> before/after ASM dumps
(needs local `asm-8.0.1.jar`/`asm-tree-8.0.1.jar`); `FileProbe` -> SDN triage;
`LineGrep` -> listing context. Each port keeps its evidence note (command +
jar hash) and lands in `tools/` with compiled output under `tools/classes/`.

## Phase 3 — Resolve the NOT VERIFIED list (no scope creep)

Decisive experiments only, each with a stop condition
(outcomes 2026-09-18 in `results/deob-remap-2026-09-18.md`):

1. `aar -> 406` rewrite: module `0Y` (name `cache`) + `406` target confirmed
   statically; runtime before/after `ClassNode` dump still open.
2. `40o.2` rule table: DONE — decrypted suffix + replacement strings offline
   (`xtea` rule; `random.dat`/`preferences` via `40o.7`; callers `406`/`4zF`).
3. `4kY` id-file path: inputs (`user.home`, `.jid`) + fallback constants +
   build-constant `7` mapped; exact subdir/filename Unknown (runtime salt,
   reflective-only invocation) — stopped per identity-tooling boundary
   (map, don't execute).
4. `P2P Master AI` payload: unchanged — SDN dead since 2026-08-31, key
   server-side only (see sdn-payload-decrypt session).
5. Live request framing: static ceiling recorded (`dd` form fields, RSA pair,
   server markers); sanitized-trace experiment still open — never
   credentials, tokens, or full identifiers.

Mark anything blocked by encryption-without-key, credential exposure, or
server-side unobservability as Unknown and stop.

## Phase 4 — Retire bot-client references

When Phases 1–2 are green on current jars: replace "carried over from
bot-client" labels with local evidence levels, archive the `D_Jok`-era paths
and five-payload inventory as historical notes, and keep bot-client linked as
method-origin only (its native `offsets.hpp` workflow does not apply here).

---

## Part 2 — RuneLite-based fork (see `docs/fork-plan.md`)

Clean-room rule (applies to all fork phases): behavior specs only, never
DreamBot bytecode; keep RuneLite BSD-2-Clause headers on derived files;
non-goals are final — no identity/HWID tooling, no telemetry clone, no SDN
server, no credential vault, no packet code.

## Phase 5 — Pin the RuneLite base

- [x] Fingerprint jars (API `27B2E4B0…`; client == base-client `82F000DD…`).
  Gradle scaffold: Java 17, local file deps only, `verifyArtifacts` green.

## Phase 6 — Script framework + loader (local scripts only)

- [x] `Script` base + `@ScriptManifest` + `ScriptRunner` (start/loop/exit).
- [x] Scripts-directory scan -> manifest read -> per-script `URLClassLoader`,
  reload/unload (`bot.script.ScriptLoader`; side-effect-free manifest scan).
  No catalog file, no network loading. Covered by `gradlew smokeTest`.

## Phase 7 — Game-API facade

- [x] `Npcs`/`Players`/`GameObjects` (scene-grid walk, no typed-in tiles),
  `Actions` (menu-entry builders + install + Robot dispatch),
  `Skills`/`Inventory`/`Local` (`bot.api`). Entry builders covered offline;
  live dispatch params want one in-game confirmation pass.

## Phase 8 — Overlays and config (local scripts only)

- [x] `ScriptOverlay` (draw-only, above scene) + `ScriptConfig`
  (`@ConfigGroup` settings with built UI) in `bot.ui`. No profiles, no account code.

## Phase 9 — Launcher/dist + docs

- [x] `dist` layout (`build/dist/dreambot.jar` + `AGAINST.txt`) with a
  verifying task; `bot.launcher.Main` boots the pinned client and runs one
  local script by name.
- [x] User docs: `docs/writing-scripts.md` (lifecycle, API tour, offline/online
  runs) with `sample.CowHitter` as the worked example; multi-script jars
  allowed, sample dir hermetic.
- [ ] Live pass: `gradlew run -Pscripts=<dir> -Pscript=<name>` against the
  real game (needs network + login; never attempted in this environment).

## Phase 10 — Looting + gathering round (local scripts only)

- [x] `GroundItems` (`Loot` = item + tile, since `TileItem` carries no
  position), `Actions.take`/`object`/`groundItemMenu`, `Woodcutter` sample.
  Covered by `smokeTest` (14 checks).

## Phase 11 — Banking + equipment (local scripts only)

- [x] `Bank` (open state, contents, booth/banker open, deposit buttons),
  `Equipment` (worn reads), `Actions.widgetMenu`/`widget`, `BankRunner`
  sample. Covered by `smokeTest` (19 checks).

## Phase 13 — Full API fulfillment (local scripts only)
- [x] Audited `org/dreambot/api/**` (packages: wrappers, methods/*, input,
  script, utilities, data, settings, randoms) into `results/api-coverage.md`.
- [x] `bot.util` (`Sleep`/`Timing`/`Calculations`), `Area`, `Widgets`
  (recursive search), `Camera`, `Combat`, `Tabs`, `Prayers`, `Magic` (home
  teleports), `Equipment.inSlot`, `Mouse`/`Keyboard`, `PathFinder` (A* over
  RL's own movement bits) + `Walking.walkPath`.
- [x] `smokeTest` at 40 checks, all offline. Deferred/out list in the
  coverage doc (niche widgets, server-backed, auth, random solvers).

## Phase 14 — Exchange + banking depth (local scripts only)

- [x] `GrandExchange` (slot reads, done/progress, clerk open, Collect-all via
  found actions — no hardcoded button ids), `Bank.close` (escape),
  `Keyboard.pressEscape`, `GeCollector` sample.
- [x] `smokeTest` at 45 checks, all offline. Offer *creation* (search/qty/
  price widgets) deferred to a live pass.

## Phase 15 — Widget-id mining + full offer/trade/deposit flows

- [x] Extracted every interface id from DreamBot's own API bytecode
  (`tools/ExtractWidgetIds.py`, `results/widget-id-sites.txt`): GE screens,
  qty/price/search widgets, confirms, abort, collect; trade accept/decline
  both stages; deposit buttons/slots/close.
- [x] `GrandExchange.buyOffer/sellOffer` (screen, search, qty, price,
  confirm), `Trade`, `DepositBox`, `WidgetIds`, `Actions.widgetMenu` overload.
- [x] `smokeTest` at 53 checks, all offline. Dispatch semantics want one
  live pass; ids are cited, never guessed.

## Phase 16 — Combat depth + prayers + SkillTracker (local scripts only)

- [x] Mined DreamBot's Combat/Prayer bytecode (`tools/ParsePrayerEnum.py`,
  `results/javap-c-db-combat-prayer-tracker.txt`,
  `results/javap-c-db-prayer-enum.txt`): varps 300/301/172/43/102,
  varbit 2668, style table [6,10,14,18], prayer book group 541 with all
  31 per-prayer children (`results/prayer-widget-table.txt`), quick-prayer
  root [77,4], spec orb [160,36], tab spec button [593,38].
- [x] Probe-verified the pinned RuneLite ids against the mined ones
  (`results/widgetinfo-probe.txt`): styles, auto-retaliate, quick-prayer
  root, minimap orb all match both ways.
- [x] `Combat` (level, spec %/active/toggle, retaliate read/set, style
  read/set, poisoned), `Prayers.toggle/activate/bookChild`,
  `SkillTracker` (gained, xp/hr, time-to-target), all five `Magic` home
  teleports. Per-prayer setting bits and envenom threshold stay out
  (runtime-decrypted, never exposed statically).
- [x] `smokeTest` at 77 checks, all offline. Clicks still want one live pass.

## Phase 17 — Social + emotes + random-dismiss (local scripts only)

- [x] Mined DreamBot's `Emotes`/`Emote` bytecode: container [216, 2] +
  all 54 per-emote children (`results/emote-widget-table.txt`,
  `tools/ParsePrayerEnum.py` block mode); friend tab switch [432, 1];
  clan-chat cached widgets left unused (obfuscated holders, no semantics).
- [x] `Emote` enum + `Emotes.perform`, `HintArrows` (reads + clear),
  `Friends` (reads + tab), `ClanChat` (friends-chat + guild reads + tab),
  `RandomEvents.dismiss` (Continue-boxes only — solvers permanently out),
  `Tabs` friends/ignores/chat/emotes/music.
- [x] `smokeTest` at 101 checks, all offline. Clicks/chat flows want one
  live pass; friend add/delete/message and clan join/leave deferred.

## Phase 18 — Shop + smithing + fairy rings (local scripts only)

- [x] Pinned-probe ids for the groups DreamBot hides behind
  runtime-decrypted holders: shop 300, smithing 312 (`results/widgetinfo-probe.txt`).
- [x] Mined DreamBot's `FairyRings` bytecode in full: dial widgets
  (398, 19/21/23), varp-816 masks/shifts/letter tables, confirm (398, 26),
  travel log (381, 7) (`results/fairy-ring-table.txt`,
  `results/javap-c-db-fairy.txt`). Destination codes are decrypted at
  runtime, so scripts pass plain letters (DreamBot's `travel(String[])` shape).
- [x] `Shop` (reads, buy/sell 1-5-10-50, open/close), `Smithing` (reads,
  item click, anvil open), `FairyRings` (dial reads, rotate, code entry,
  travel). Bonds stay out: no static ids and no pinned constants — needs
  one live pass to identify the redeem screen, documented in api-coverage.
- [x] `smokeTest` at 124 checks, all offline. Clicks want one live pass.

## Phase 19 — Quests + diaries + minigames (local scripts only)

- [x] `Quests.questPoints` (varp 101, one-line read). Per-quest states stay
  out: DreamBot derives them from quest-tab text colors through a
  runtime-decrypted map (`Quest$State.getForID`) — needs one live pass.
- [x] Mined all 12 diary classes: 48 tier varbits, uniform
  `getBitValue(v) == 1` shape (`results/diary-varbit-table.txt`,
  `tools/ParseDiaries.py`) -> `Diaries` area/tier reads.
- [x] Mined `MinigameTeleports`: clan-interface list [76, 22], selection
  [76, 11], confirm [76, 32] -> `Minigames` (open check, selection read,
  name-matched teleport). The clan-tab opener resolves through
  runtime-decrypted holders (no pinned constant), so teleport works when
  the list is open, fails clean otherwise.
- [x] `smokeTest` at 135 checks, all offline. Clicks want one live pass.

## Phase 20 — Combat spells + quick-prayer setup + envenom (local scripts only)

- [x] Mined all four DreamBot spellbook enums: group 218 everywhere,
  180 per-spell (child, level) rows with levels matching game-known
  values (`results/spell-table.txt`, `tools/ParseSpells.py`) ->
  `Spell` enum + `Magic.cast/canCast` (level gate only; rune costs ride
  decrypted tables and stay out).
- [x] `Prayers.quickChild/selectQuick` from the mined quick-prayer
  children (root (77, 4) probe-matches the pinned `QUICK_PRAYER_PRAYERS`).
- [x] `Combat.isEnvenomed/poisonValue` (varp 102 venom band >= 1000000
  per RuneLite's documented `POISON` scale — strong inference, needs a
  live pass).
- [x] Ruinous prayers stay out (DreamBot's own table is standard-book
  only; no static source). Bonds stay out (no static ids, no pinned
  constants). Both documented in api-coverage.
- [x] `smokeTest` at 148 checks, all offline. Clicks want one live pass.

## Phase 21 — Login driver (local scripts only)

- [x] Re-scoped per owner direction: login, world hopping, and randoms
  moved from non-goals into scope (`docs/fork-plan.md`,
  `results/api-coverage.md`). Remaining hard lines: no stored
  credentials, no account manager/switching, no telemetry/HWID, no SDN,
  no packet code, no server feeds.
- [x] `Login` (state via maintained `GameState.of(loginIndex)`,
  runtime-credential login via client fields + Enter, authenticator
  fails clean, logout via tab + probe-verified button), `Tabs.logout`,
  launcher env auto-login (`DREAMBOT_USERNAME`/`DREAMBOT_PASSWORD`,
  never logged). Credential rule added to `docs/writing-scripts.md`.
- [x] `smokeTest` at 153 checks, all offline. Real login wants a live pass.

## Phase 22 — World hopping (local scripts only)

- [x] Mined DreamBot's `WorldHopper`/`Worlds` bytecode: switcher list
  (69, 18), sibling rows (69, 17), sort headers (69, 23/24)
  (`results/javap-c-db-hop.txt`); `WORLD_SWITCHER_LIST` (69, 18) and the
  switcher/logout buttons probe-match the pinned API
  (`results/widgetinfo-probe.txt`, probe 3).
- [x] `Worlds` (list reads off `getWorldList`, members/f2p/pvp/high-risk/
  activity filters, emptiest, switcher open, row-matched hop with the
  row's own switch action). No `JSocket` server comms by design.
- [x] `Actions.widget(widget, option)` overload (also un-warts `Shop`'s
  double-install).
- [x] `smokeTest` at 164 checks, all offline. Real hops want a live pass.

## Phase 23 — Randoms framework (local scripts only)

- [x] Mined DreamBot's `randoms/` package (dismiss-shaped set — no puzzle
  solvers exist to mirror): welcome close (378, 72), bank-pin group 213
  with slots at +3..+6 ("?" pending) and digit buttons at +16+2i matched
  by child(1) label (`results/randoms-table.txt`,
  `results/javap-c-db-randoms.txt`).
- [x] `RandomSolver`/`BaseSolver` + `Randoms` manager (6 defaults,
  enable/disable, `runOnce`, session-only PIN holder): Dismiss, Genie
  (dialogue only — lamp claiming is an XP choice), Welcome, BankPin
  (runtime PIN only), Login (re-login on disconnect via session
  credentials), Break (scheduled logout/rest/re-login).
- [x] `Login` session credentials (process memory only — never persisted,
  never logged; documented as not-a-vault); `Npcs.withName/nearestNameWithin`.
- [x] Roof/resizable/zoom/death/tutorial handlers stay out (settings
  widgets with no static source — needs a live pass).
- [x] `smokeTest` at 173 checks, all offline. Live events want a live pass.

## Phase 24 — Social mutations + fairy log readers (local scripts only)

- [x] Mined DreamBot's `Friends`/`Friend`/`ClanChat` flows: entry buttons
  (429, 11 message / 14 add / 16 delete), tab-swap-chatbox-type-verify
  shape for add/delete/message, chat-tab entry + typing for clan
  join/leave (`results/javap-c-db-friends.txt`,
  `results/javap-c-db-friend-one.txt`, `results/javap-c-db-clanchat.txt`).
- [x] `Friends.addFriend/deleteFriend/sendMessage/isOnline`,
  `ClanChat.join/leave` (join/leave buttons resolve through DreamBot
  decrypted holders, so the fork clicks the visible join/leave action
  found at runtime — NOT VERIFIED labels), `FairyRings.logRows/clickLogRow`
  (rows read off mined (381, 7); format matching stays with scripts).
- [x] Ignore-list mutations stay out (no DreamBot counterpart).
- [x] `smokeTest` at 184 checks, all offline. Typing flows want a live pass.

## Phase 25 — Quest journal data (local scripts only)

- [x] Mined all three DreamBot quest-book enums with per-book
  constructor mapping verified by putfield trace: 230 quests with
  journal rows (399, 7, grandchild), QP, varp/varbit ids, progression
  tables (`results/quest-table.txt`, `tools/ParseQuests.py`) ->
  `Quest` enum + `Quests.settingValue/rowWidget/rowColor`.
- [x] State verdicts deliberately NOT implemented: single-element tables
  ([10]) read like completion values but multi-element tables
  ([35,2,0,19]) are non-monotonic and [-1] tables (all free quests)
  carry nothing — inventing thresholds would break the best-known
  quests. One live calibration (row colors vs raw values) unlocks them;
  the procedure is in the table doc.
- [x] `smokeTest` at 192 checks, all offline.

## Phase 26 — Event listeners (local scripts only)

- [x] Mirrored all 22 portable DreamBot listener contracts
  (`bot.script.listener.*`, default no-op methods, RL payloads):
  chat (+typed fan-out), game/client tick, XP (gained/level-up/change),
  animation, hitsplat, varbit, spawns (NPC/player/object/loot/projectile
  first-sight), projectile moves, containers (inventory/equipment +
  added/removed diffs), menu entries/clicks, login/logout edges, game
  state, widgets, region approximation (LOADING-exit edge), world id,
  client scripts, script callbacks, render, breaks.
- [x] `bot.script.Events` fan-out on the maintained bus (XP/level,
  container-snapshot, projectile, edge trackers inside) + `ScriptRunner`
  register/unregister for the run lifetime + `BreakSolver` break broadcasts.
- [x] Deliberately omitted (no pinned source): varp updates, fake-XP
  drops, pre/server ticks, spot-animation ids, walk events, decoded
  widgets, HumanMouse (Robot input has no event stream — design
  difference), paint (covered by `ScriptOverlay`).
- [x] `smokeTest` at 199 checks, all offline. Bus delivery wants a live pass.

## Phase 27 — Script structure + favour + music (local scripts only)

- [x] `TaskNode` (priority/accept/execute + best-node dispatch) and the
  `bot.script.tree` framework (`Leaf`/`Branch`/`Root`/`TreeScript` with
  tree-link propagation) mirroring DreamBot's contracts — additive, plain
  `Script` subclasses keep working.
- [x] `bot.util.Requirements` (skill/combat/QP/favour gates + all/any;
  quest requirements stay out — verdicts uncalibrated).
- [x] `Favour` (5 Arceuus houses, mined varbits, value + percent) and
  `Music` (793-track `Song` unlock table mined; per-track playback stays
  out — rows match decrypted titles, scripts click rows themselves).
- [x] `smokeTest` at 217 checks, all offline.

## Phase 12 — Run-readiness (no live launch in this environment)

- [x] Fixed the boot failure seen 2026-09-09: `run` passed a literal
  `libs/*.jar` path (`files()` does not glob) and Guava's plugin scan died
  on it. Classpath is now `runtimeClasspath` + `fileTree`; the client got as
  far as `PluginManager.loadCorePlugins` before that, and the fork side ran
  Hello start-to-finish in-process.
- [x] `checkRunClasspath` regression guard (59 entries, no globs, all exist).
- [x] `Main` waits for login (10 min timeout, exits cleanly without running
  blind), registers `Script.overlay()` for the run lifetime, reports
  `fork.properties` version. `Game` also publishes the Guice injector.
- [x] `scripts/` drop-in directory with README.
- [ ] Live pass: `gradlew run -Pscripts=<dir> -Pscript=<name>` against the
  real game (needs network + login; boot smoke reached plugin scan only).

## Guardrails (apply to every phase)

- Jars stay in `%USERPROFILE%\DreamBot\BotData\`; this repo holds listings,
  hashes, and notes — never jar copies, never `accounts.db`, never secrets.
- `jar tf` + `javap -classpath <jar>` are authoritative; Windows-extracted
  trees are scratch (case-collision risk: `4cs` vs `4cS`).
- `javap` bytecode is evidence; CFR/FernFlower is navigation.
- `random.dat` bytes vs account hash vs `vt` data vs UUID stay separate until
  an explicit copy is observed.
- Every result file records command + jar hash + evidence level.
