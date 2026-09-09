# Migration Plan: bot-client DreamBot research -> this repo

Source: `C:\Users\thewa\Development\bot-client`
Target: this repo (`C:\Users\thewa\Development\dreambot`)
Jars: `%USERPROFILE%\DreamBot\BotData\` (user directory, never vendored here)

Status: the target repo was empty before this change. This plan ports the
reusable DreamBot knowledge out of bot-client (where it sits beside a native
`osclient.exe` injector: `client/offsets.hpp`, `launcher/`, `java/kewl/`) and
re-homes it where the DreamBot Java jars are the first-class target.

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

Decisive experiments only, each with a stop condition:

1. `aar -> 406` rewrite: before/after `ClassNode` dump of runtime `aar`.
2. `40o.2` rule table: decrypted suffix + replacement strings, or observed
   path-in/path-out pairs from a disposable profile.
3. `4kY` id-file path: decrypted property/filename or observed file access.
4. `P2P Master AI` payload: before/after `.cache\bin` inventory + classloader
   code-source correlation across two disposable profiles.
5. Live request framing: sanitized local instrumentation (lengths/hashes,
   offsets, ordering) — never credentials, tokens, or full identifiers.

Mark anything blocked by encryption-without-key, credential exposure, or
server-side unobservability as Unknown and stop.

## Phase 4 — Retire bot-client references

When Phases 1–2 are green on current jars: replace "carried over from
bot-client" labels with local evidence levels, archive the `D_Jok`-era paths
and five-payload inventory as historical notes, and keep bot-client linked as
method-origin only (its native `offsets.hpp` workflow does not apply here).

## Guardrails (apply to every phase)

- Jars stay in `%USERPROFILE%\DreamBot\BotData\`; this repo holds listings,
  hashes, and notes — never jar copies, never `accounts.db`, never secrets.
- `jar tf` + `javap -classpath <jar>` are authoritative; Windows-extracted
  trees are scratch (case-collision risk: `4cs` vs `4cS`).
- `javap` bytecode is evidence; CFR/FernFlower is navigation.
- `random.dat` bytes vs account hash vs `vt` data vs UUID stay separate until
  an explicit copy is observed.
- Every result file records command + jar hash + evidence level.
