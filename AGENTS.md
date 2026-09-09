# DreamBot Client Deobfuscation — Agent Instructions

This repo targets the **DreamBot Java client**, not the native `osclient.exe`.
It is modeled on `bot-client/.agents/skills/deob/SKILL.md`, with the method
ported from native reversing (Ghidra/IDA + Lua-binding anchors + RVAs) to
Java bytecode reversing (`javap` + ASM + constant-pool anchors + class maps).

**Project direction: RuneLite-based fork.** The end goal (see
`docs/fork-plan.md`, phases 5–9 in `docs/migration-plan.md`) is to reimplement
DreamBot-style scripting on the pinned RuneLite 1.12.36 base (BSD-2-Clause).
This deob work produces behavior specs for that fork — clean-room only: never
copy DreamBot bytecode, and never build identity/telemetry/credential tooling
(the non-goals in `docs/fork-plan.md` are final).

Source of truth for jars (all local, authorized artifacts only):

```text
%USERPROFILE%\DreamBot\BotData\repository2\dreambot-client.jar       (~9.1 MB, DreamBot runtime)
%USERPROFILE%\DreamBot\BotData\repository2\injected-client-1.12.36.jar (~4.2 MB, obfuscated game client)
%USERPROFILE%\DreamBot\BotData\repository2\client-1.12.36.jar        (~5.2 MB, RuneLite-style client)
%USERPROFILE%\DreamBot\BotData\client.jar                            (~16.8 MB, script-loader runtime)
%USERPROFILE%\DreamBot\BotData\gamepack.jar                          (~4.3 MB)
%USERPROFILE%\DreamBot\BotData\.cache\scripts.dat                    (SDN catalog, names P2P Master AI)
%USERPROFILE%\DreamBot\BotData\.cache\bin\                           (hash-named SDN payloads)
```

Fingerprinted 2026-09-09 on this machine (`C:\Users\thewa\...`):

| Jar | Size | SHA-256 |
|---|---|---|
| `repository2/dreambot-client.jar` | 9163575 | `66036CB5246A9BD8014A93DCFC7A5AE49DDE0253B1D07DA4AD747EC0C700882A` |
| `repository2/client-1.12.36.jar` | 5256222 | `82F000DDE83C87029FD40FC7B29FABB86B41ECECE96015D3571B5966204274D3` |
| `repository2/injected-client-1.12.36.jar` | 4229994 | `FD8A49D6278431A8DCC966D97A1B84F546D4CC9715C4E64B1B55F98ABF6C06F7` |
| `BotData/client.jar` | 16866282 | `E2CAAE5045A7CC3DF5A188665B7022B499A946C46928D88697CB56EBC5F3CBD2` |
| `BotData/gamepack.jar` | 4351737 | `4F121CE9AEFED6E0D523BA23D26245968649DC34290B57EFF884900FDD7F60AD` |

Re-fingerprint with `certutil -hashfile <jar> SHA256` after any DreamBot
update. Old docs referencing `C:\Users\D_Jok\...` paths and five `.cache\bin`
payloads are stale: on 2026-09-09 `.cache\bin` holds one payload
(`838b43e2b04f367b132c939dc2ae8abf`, 6702000 bytes). Never treat old hashes,
old class names, or old payload counts as live values.

## Toolchain

- JDK: Temurin 17.0.20.1 at `C:\Users\thewa\.jdks\temurin-17.0.20.1`
  (`javac`/`jar`/`javap` all report `17.0.20.1`; verified 2026-09-09).
  Its `bin` is on the user `PATH` — open a new terminal after any PATH change
  before running the commands below.
- ASM for probes (already local, do not re-download):
  `repository2/asm-8.0.1.jar`, `asm-tree-8.0.1.jar`, `guava-23.2-jre.jar`.

## The rule that matters most

**Anchor on something the client names itself, then walk to the thing you want.**

Ported from bot-client (Lua bindings) to this repo (Java constant pool):

1. **A string literal the client uses for itself** (best). `random.dat`,
   `jagex_cl`, `java/io/RandomAccessFile`, `wmic csproduct get UUID`,
   `cat /etc/machine-id`, `system_profiler`, `ProcessHandle`. Find the literal,
   find which class/method uses it, you are standing in the code you want.
2. **A constant-pool field/method reference.** `me.aw`, `jl.ab`, `client.lx`.
   `CpRefSearch` over the extracted tree beats text grep on `.class` files.
3. **Call-graph position.** "Only caller of `jl.ab(byte)`" (`dd`, `gy`);
   "only writer of `client.lx`" (`ck.gu`); "only `ClassLoader.defineClass`
   next to `JarInputStream` + `Cipher`" (`org.dreambot.4cs`).
4. **Size and shape.** A 24-byte array + seek(0) + all-zero rejection is the
   `random.dat` reader even before you know its name.

## What not to do

- **Do not scan for byte patterns.** Same rule as bot-client: compiler/optimizer
  output shifts, and a stale pattern hands you a plausible-but-wrong class.
  Anchor on literals and constant-pool refs instead.
- **Do not trust extracted trees blindly.** Obfuscated names differ only by case
  (`4cs` vs `4cS`). Windows extraction collapses them. `jar tf` is the
  authoritative entry list; `javap -classpath <jar>` is the authoritative
  reader. Keep extracted trees separate from generated listings.
- **Do not read a decompiler as evidence.** CFR/FernFlower output is a
  navigation aid. Conclusions come from `javap -c -p` bytecode; the duplicated
  obfuscated variants in `dd`/`gy` make decompiler output actively misleading.
- **Do not conflate the two identity values.** `random.dat` material is a
  24-byte `byte[]` from the injected client; `AccountManager.getAccountHash()`
  is a Base64 `String` for DreamBot telemetry. Different types, producers,
  consumers. Only a directly observed assignment or buffer copy merges them.
- **Do not copy `accounts.db`, credentials, tokens, or session cookies** into
  this repo. SDN work uses catalog + metadata + opaque payload bytes only.
- **Do not infer server behavior from client bytecode.** Keep `random.dat`
  bytes, `client.lx`, `vt` platform data, account hash, persistent UUID, and
  session/token values separate unless an explicit copy is observed.

## The workflow

```bat
:: 1. Fingerprint inputs (every session after an update)
certutil -hashfile "%USERPROFILE%\DreamBot\BotData\repository2\injected-client-1.12.36.jar" SHA256
jar tf "%USERPROFILE%\DreamBot\BotData\repository2\injected-client-1.12.36.jar" | findstr "^me\.class ^jl\.class ^vt\.class"

:: 2. Anchor, then walk (example: random.dat reader)
javap -classpath "%USERPROFILE%\DreamBot\BotData\repository2\injected-client-1.12.36.jar" -c -p me > listings\injected-client\me.javap
javap -classpath "%USERPROFILE%\DreamBot\BotData\repository2\injected-client-1.12.36.jar" -c -p jl > listings\injected-client\jl.javap
:: CpRefSearch <extracted-tree> me aw        -> bz, cl, jl, lw, me use me.aw
:: CpRefSearch <extracted-tree> jl ab        -> dd, gy call jl.ab(byte)

:: 3. Record result with evidence level; update docs/dreambot-client-mappings.md in the same change
```

The full command reference lives in `.agents/skills/dreambot-deob/SKILL.md`.
The ported class/field map lives in `docs/dreambot-client-mappings.md`.
The phased work plan lives in `docs/migration-plan.md`.

## Confidence, and saying so

Label every result (ported from
`bot-client/docs/dreambot-client-reverse-engineering-plan.md`):

| Level | Meaning |
|---|---|
| Confirmed static | Directly visible in bytecode, constant pool, annotations, class metadata |
| Confirmed runtime | Observed in a controlled local execution / sanitized trace |
| Strong inference | Multiple static facts, missing direct observation |
| Unknown | No reliable evidence yet |

Mark anything not confirmed against the running client as **NOT VERIFIED** with
what would confirm it. When sources disagree, prefer: observed running-client
behavior > live memory read > on-disk cache > anything written down elsewhere
(wiki, forum post, older copy of `bot-client` — measured on a different build).

## When you are done

Say plainly which you did: derived it from the jar bytecode, confirmed it
against a running client, or carried it over from `bot-client` docs. Those are
three different certainty levels and the next person cannot tell them apart
from a mapping table alone.
