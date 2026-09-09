# Fork Plan: RuneLite base + DreamBot-style scripting

Goal: a RuneLite-based client that reimplements DreamBot's *scripting
functionalities* (script lifecycle, local loading, game API facade, overlays,
per-script config) — not a DreamBot copy. RuneLite supplies the maintained,
deob-proof game interface; this repo supplies the DreamBot-style layer on top.

## Why this pivot

Phase 1 proved the DreamBot mapping trail is expensive to hold: per-build
obfuscated names, case-twin collisions (`4cs`/`4cS`, `40o`/`40O`),
runtime-decrypted strings, and loader renames (`9O9`→`8Q`). Chasing that every
update is the treadmill bot-client's native offsets already sit on.
RuneLite 1.12.36 (BSD-2-Clause, verified at github.com/runelite/runelite)
solves the same problem upstream with official mappings and a stable
`net.runelite.api` surface — the same API DreamBot itself builds on
(`client-1.12.36.jar` + `runelite-api-1.12.36-runtime.jar` ship in its own
`repository2/`).

## Pinned local base (verified 2026-09-09)

| Artifact | Size | SHA-256 | Role |
|---|---|---|---|
| `repository2/runelite-api-1.12.36-runtime.jar` | 272826 | `27B2E4B0…C4334E31` | compile dependency: `Client`, `NPC`, `Player`, `GameObject`, `ItemContainer`, `MenuEntry`/`MenuAction`, `events.*`, `widgets.*` (267 entries) |
| `repository2/client-1.12.36.jar` | 5256222 | `82F000DD…4274D3` | RuneLite-style client (== `temp_artifacts/…/base-client.jar`, same hash) |
| `temp_artifacts/…/client-1.12.36.jar` | 5138649 | (older/different copy — do not use; prefer the hash-matching one) | — |
| `bot-client/dreambot/deob/inputs/base-client/` | extracted tree | — | browsable `net/runelite` sources of the pinned client |
| `repository2/guava-23.2-jre.jar`, `gson-2.10.1.jar`, `okhttp-3.14.9.jar` … | — | — | reuse DreamBot's own dependency set where the fork needs them |

RuneLite license: **BSD-2-Clause** — fork-friendly. Retain upstream copyright
headers on any file derived from RuneLite sources; new files carry this
project's header.

## Functionality map (DreamBot behavior -> fork component)

Behavior specs come from `docs/dreambot-client-mappings.md` and
`results/phase1-reverification.md`. No DreamBot bytecode is copied — specs
only (clean-room rule, enforced in review).

| # | DreamBot functionality (observed) | Fork component | RuneLite primitive it stands on |
|---|---|---|---|
| 1 | Script lifecycle (`AbstractScript`: manifest, `onStart`/`onLoop`/`onExit`) | `script` module: `Script` base class + `@ScriptManifest` + runner (start/loop/exit on client thread) | `Plugin`, `@PluginDescriptor`, `ClientThread`, `ScheduledExecutorService` |
| 2 | Local discovery/loading (8Q: temp-copy + suffix scan -> `URLClassLoader`) | `loader` module: scan scripts dir for jars, manifest read, one `URLClassLoader` per script, reload/unload. **No network loading, no catalog server** (the old `NetworkLoader`/`3Z` counterpart is out of scope) | plain `URLClassLoader` (no temp-copy needed for plain jars) |
| 3 | Script discovery (the `scripts.dat` catalog concept) | Dropped as a component — the scripts directory itself is the catalog; each jar carries its manifest | Directory scan (Phase 6) |
| 4 | Entity APIs (`NPCs.closest`, `Players.localPlayer`, ids/names/tiles) | `api` facade: `Npcs`, `Players`, `GameObjects`, `GroundItems` over injected-client-verified semantics | `Client.getNpcs()/getPlayers()`, `TileObjects`, `NPCComposition` (names free — no `npcName` pointer chains) |
| 5 | Interaction (`interact(...)`, menu actions) | `Actions`: walk-to/click via menu entries | `MenuEntry` + `MenuAction`, `ClientThread.invoke` (the maintained equivalent of KewlKlient's `doAction` idea — and of `dd`/`gy`'s buffer path — without touching either) |
| 6 | Skills/inventory/state (levels, XP, idle/animation, counts) | `Skills`, `Inventory`, `Local` (self) facades | `Client.getSkillExperiences/getBoostedSkillLevels`, `ItemContainer`, `Player.getAnimation()` |
| 7 | Overlays/paint + script panels | `Overlay` helpers + per-script panel | `OverlayManager`, `OverlayPanel`, `OverlayUtil` |
| 8 | Per-script config + profiles | `Config` (auto-UI settings) + local profiles | `@ConfigGroup`/`ConfigManager` (DreamBot's `config.json`-per-script becomes RL config, no new code) |
| 9 | Account switching / authentication | Out of scope — local scripts run under whatever account the launcher is logged into. No profiles, no credentials, no auth code, no account server. | — |

## Explicit non-goals

- **No identity/HWID tooling.** Phase 1 studied `random.dat`/`vt`/telemetry to
  *understand* the client, never to reproduce, spoof, or redirect it. Nothing
  in this fork reads, writes, or substitutes identity values.
- **No telemetry/WebSocket exfiltration layer** (`4z_`/`404` have no fork
  counterpart, by design).
- **No SDN server, no credential vault, no packet code.**
- **No DreamBot code in this repo.** Behavior notes only; violations fail review.

## Build shape (mirrors bot-client's ergonomics)

- Gradle, `gradlew run` / `gradlew dist`, Java 17 (Temurin `17.0.20.1`).
- Local file dependencies only (`flatDir` at `BotData/repository2` or a
  `libs/` pointer doc — never vendored jars, never network resolution of
  game code).
- `dist/` shipping layout thinking borrowed from bot-client: everything the
  launcher needs side by side, verified by task (not by exit code).

## Relation to the deob skill

`.agents/skills/dreambot-deob/SKILL.md` stays: it is how DreamBot behaviors get
specified precisely enough to reimplement (anchor -> walk -> record). Its
Phase-3 targets (transformer table, `40o.2` rules) are now *reference only* —
needed to understand, not to chase per update.
