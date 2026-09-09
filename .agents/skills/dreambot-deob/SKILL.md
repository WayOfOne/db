---
name: dreambot-deob
description: Decompile and deobfuscate the DreamBot Java client jars (dreambot-client.jar, injected-client-1.12.36.jar, client-1.12.36.jar, BotData client.jar/gamepack.jar) to find or re-derive a class, method, field, transformer target, or file/payload mapping. Use when a DreamBot update moved an obfuscated name, or when adding a feature that needs data the current mapping does not expose yet.
---

# Decompiling and deobfuscating the DreamBot client

You are helping someone re-derive a mapping in obfuscated Java jars that get
rebuilt on DreamBot/game updates. This skill is the method that works, ported
from `bot-client/.agents/skills/deob/SKILL.md` (native `osclient.exe` via
Ghidra/IDA + Lua-binding anchors) to Java bytecode (`javap` + ASM +
constant-pool anchors). The jars live in the user directory, not in this repo:

```text
%USERPROFILE%\DreamBot\BotData\repository2\dreambot-client.jar
%USERPROFILE%\DreamBot\BotData\repository2\injected-client-1.12.36.jar
%USERPROFILE%\DreamBot\BotData\repository2\client-1.12.36.jar
%USERPROFILE%\DreamBot\BotData\client.jar
%USERPROFILE%\DreamBot\BotData\gamepack.jar
%USERPROFILE%\DreamBot\BotData\.cache\scripts.dat
%USERPROFILE%\DreamBot\BotData\.cache\bin\
```

## 0. Fingerprint first (updates move everything)

One-time workspace scaffold (idempotent — safe to re-run; `inputs/` holds
extracted trees, which are git-ignored scratch, while `listings/` and
`results/` are committed evidence):

```bat
mkdir listings\injected-client listings\dreambot listings\cross-layer results inputs tools\classes 2>nul
```

Then fingerprint:

```bat
:: prerequisite: JDK 17 bin on PATH
:: (this machine: C:\Users\thewa\.jdks\temurin-17.0.20.1\bin)
where jar javap javac
certutil -hashfile "%USERPROFILE%\DreamBot\BotData\repository2\dreambot-client.jar" SHA256
certutil -hashfile "%USERPROFILE%\DreamBot\BotData\repository2\injected-client-1.12.36.jar" SHA256
certutil -hashfile "%USERPROFILE%\DreamBot\BotData\repository2\client-1.12.36.jar" SHA256
certutil -hashfile "%USERPROFILE%\DreamBot\BotData\client.jar" SHA256
certutil -hashfile "%USERPROFILE%\DreamBot\BotData\gamepack.jar" SHA256
jar tf "%USERPROFILE%\DreamBot\BotData\repository2\injected-client-1.12.36.jar" | findstr "^me\.class ^jl\.class ^vt\.class ^aar\.class ^aac\.class ^client\.class"
```

Record SHA-256 + size + entry count in the result file. After any update,
re-derive the mapping you touched — never bump a version string and keep old
class names. Obfuscated names (`me`, `jl`, `vt`, `4cs`, `406`…) are per-build.

## 1. The method: anchor, then walk references

Start from literals the client names itself, never from a guessed class name.
The exact names move; the literals survive.

Injected-client anchors (string scan):

```text
random.dat  jagex_cl  java/io/RandomAccessFile
wmic csproduct get UUID  cat /etc/machine-id  system_profiler  ProcessHandle
me.aw  jl.ab  client.lx
```

DreamBot-runtime anchors (class/constant scan):

```text
ClassLoader  defineClass  JarInputStream  Cipher
org/objectweb/asm/tree/ClassNode
java/util/UUID randomUUID  SecureRandom  MessageDigest
java/net/http/WebSocket  Gson toJson
getDeclaredField  setAccessible
```

Walk pattern (example carried over from bot-client findings):

1. Byte-scan class files for `random.dat` -> hits in `cl`, `me`, `pj`.
2. `javap -c -p me` -> `new File(..., "random.dat")`, `new aar(file,"rw",25L)`
   wrapped in `aac`, stored in static field `me.aw`.
3. Constant-pool search for field `me`/`aw` -> users `bz`, `cl`, `jl`, `lw`, `me`.
4. `javap -c -p jl` -> `ab(byte)` allocates `byte[24]`, seeks `me.aw` to 0,
   reads, rejects all-zero, falls back to 24x `0xff`. Meaning assigned:
   "`jl.ab(byte)` is the 24-byte random.dat reader".
5. Constant-pool search for method `jl`/`ab` -> callers `dd`, `gy`.
6. `javap -c -p dd` / `gy` -> `client.lx != null ? write(client.lx) :
   write(jl.ab(0))`, then `xy.dl(BigInteger,BigInteger,int)` RSA-encrypts.
   Conclusion: account-related buffer prefers `client.lx`, else `random.dat`
   bytes. The request path here builds `m=accountappeal/login.ws` with
   `dest=passwordchoice.ws` — do not relabel it as gameplay login.

## 2. Toolchain (checked in or carried over from bot-client)

Prefer `javap` listings as evidence; decompilers (CFR/FernFlower) are
navigation aids only. Recreate these helpers in `tools/` as needed — their
sources were never committed to bot-client (its `dreambot/deob/README.md`
specifies behavior only; the workspace holds just `inputs/` + README), so
there is nothing to copy, only specs to reimplement:

| Helper | Job |
|---|---|
| `ClassByteSearch.java` | UTF-8 anchor scan over class-file bytes |
| `CpRefSearch.java` | Constant-pool field/method reference search |
| `LineGrep.java` | Context around matches in listings |
| `SuperSearch.java` | Subclasses of a type (e.g. all `0u` transformer modules) |
| `AnnotationScan.java` | Runtime-visible `0L` transformer annotations without initializing classes (use `Class.forName(name,false,loader)`) |
| `Call40o.java` | Offline probe of path-mapper `40o.2(String)` |
| `TransformProbe.java` | Offline ASM before/after probe (needs `asm-8.0.1.jar` + `asm-tree-8.0.1.jar` from `repository2/`) |
| `FileProbe.java` | Payload size, magic bytes, SHA-256, printable strings — never executes cached scripts |

Repeatable commands:

```bat
:: case-safe entry list (authoritative; Windows extraction is NOT)
jar tf "%REPO%\injected-client-1.12.36.jar" | findstr "^me.class"

:: bytecode listing (evidence)
javap -classpath "%REPO%\injected-client-1.12.36.jar" -c -p me > listings\injected-client\me.javap
javap -classpath "%REPO%\dreambot-client.jar" -c -p org.dreambot.406 > listings\cross-layer\406.javap

:: constant pool (literals + owners/names when the byte scanner is unavailable)
javap -classpath "%REPO%\injected-client-1.12.36.jar" -v -p vt > listings\injected-client\vt.verbose.txt

:: annotation / hierarchy scans
java -cp tools\classes AnnotationScan <extracted-dreambot-tree>
java -cp tools\classes SuperSearch <extracted-dreambot-tree> org/dreambot/0u

:: SDN payload triage (offline, no execution)
java -cp tools\classes FileProbe "%USERPROFILE%\DreamBot\BotData\.cache\bin" results\sdn-file-probe.txt
type "%USERPROFILE%\DreamBot\BotData\.cache\scripts.dat"
```

ASM dependency for probes (already local, do not re-download blindly):

```text
%USERPROFILE%\DreamBot\BotData\repository2\asm-8.0.1.jar
%USERPROFILE%\DreamBot\BotData\repository2\asm-tree-8.0.1.jar
%USERPROFILE%\DreamBot\BotData\repository2\guava-23.2-jre.jar
```

## 3. Case-sensitivity and layout rules

1. Use original jar paths as source of truth; keep extracted trees separate
   from generated listings.
2. Prefer `jar tf` and `javap -classpath <jar>` — Windows extraction collapses
   names like `4cS`/`4cs`.
3. Treat `javap` listings as evidence, decompiler output as navigation only.
4. Record every result under `results/` with the command + source jar hash.
5. Never overwrite a listing from a different client build (hash in filename
   or metadata).
6. Never copy `accounts.db`, credentials, tokens, or session data.
7. Update `docs/dreambot-client-mappings.md` only after the result reproduces.

## 4. Runtime-transformer layer (what makes DreamBot different from osclient.exe)

The on-disk injected jar is not necessarily what runs. `bot-client` findings
confirm this chain; re-verify each link per build (all currently NOT VERIFIED
end-to-end for the `random.dat` path):

```text
packed client bytes
  -> org.dreambot.4cs (ClassLoader + JarInputStream + Cipher/decryptor + defineClass)
  -> org.dreambot.82 (ASM registry: scans for @0L, decrypts metadata, dispatches 4Xi modules;
     directly constructs 4Xb, 40p, 4XB)
  -> org.dreambot.30 / other 0u subclasses (MethodInsnNode owner/name match -> InsnList rewrite)
  -> new RandomAccessFile possibly rewritten to new org.dreambot.406
  -> 406(String,mode) calls 40o.2(path) for path mapping, then super(path,mode)
  -> client opens selected file through normal aar / me.aw wrapper
```

To prove a `random.dat` redirection specifically you need BOTH: (a) the `aar`
`ClassNode` before/after the full pipeline showing constructor owner change
`java/io/RandomAccessFile` -> `org/dreambot/406`, and (b) the actual path
passed to the superclass vs the `me` cache-setup path. A `Call40o` probe
returning the input unchanged proves nothing — the rule suffix/replacement
strings are runtime-decrypted and the tested names may simply miss.

Annotation types (verify with `javap -v`, values are encrypted strings):

```text
org.dreambot.0L  (targets types;  String value())
org.dreambot.4Sl (targets methods; String value() + boolean 2)
org.dreambot.7s  (targets methods; String value() + boolean 3)
```

## 5. Identity values — keep separate until a copy is observed

| Value | Type | Producer | Consumer |
|---|---|---|---|
| `random.dat` material | 24-byte `byte[]` | injected `me.aw` via `jl.ab()` / `bz.ag()` | injected `dd`/`gy` buffer, RSA via `xy.dl` |
| `client.lx` fallback | 24-byte `byte[]` | `ck.gu` (only visible writer; zero-filled init) | same `dd`/`gy` branch when non-null |
| `vt` platform data | serialized struct | `vt.re/ac/kg/az` (wmic/machine-id/system_profiler, process names, JVM args) | `xy` buffer via `vt.az` |
| DreamBot account hash | Base64 `String` | `AccountManager.getAccountHash()` (MessageDigest over account string) | `4z_` telemetry object -> `9x` envelope -> Gson -> `404` WebSocket |
| DreamBot persistent id | `String` | `4kY` (`UUID.randomUUID` + `SecureRandom`, `Files.read/write`) | local file under system-property dir; exact path NOT VERIFIED (decrypt or observe at runtime) |

"Cache-level, not per-account" is the confirmed static shape for `random.dat`:
`me` derives the path from cache roots/user-home/legacy bases, never from
username/account-id/token. Two accounts on one cache root submit the same
bytes; the server attaches account identity from the surrounding request.

## 6. SDN / script-loader layer (P2P Master AI)

Catalog: `BotData\.cache\scripts.dat` (line-separated names; verify the
`P2P Master AI` line). Payloads: `BotData\.cache\bin\<hash>` (no `.jar`
extension, no `PK` magic as of last inspection — treat format as Unknown).
Local payload count changes over time (five at bot-client doc time, one on
2026-09-09) — always re-inventory, never quote the old five hashes as live.

Loader entry points in `BotData\client.jar`:

```text
org.dreambot.api.script.loader.LocalLoader   -> delegates to obfuscated 9O9
org.dreambot.api.script.loader.NetworkLoader -> delegates to obfuscated 9OT
org.dreambot.api.script.ScriptManager        -> owns discovered lists + current state
```

`9O9.9X(File,List<String>)` copies the supplied file to a temp file, adds its
URL to a URL list, scans a system-property directory, copies suffix matches to
more temp files, returns a `URLClassLoader`. The suffix string is
runtime-decrypted — decode it before claiming which `.cache\bin` files qualify.
Do not execute unknown payloads: hash, magic-byte, and offline-parse first;
stop if the payload is encrypted without an authorized key.

## 7. Confidence and stop conditions

Label every mapping Confirmed static / Confirmed runtime / Strong inference /
Unknown. The `aar -> 406` rewrite, the `40o.2` suffix table, the `4kY` id-file
path, the `P2P Master AI` payload hash, and the live `vt`/login opcodes are
Unknown until the listed decisive evidence exists. Stop and mark Unknown when
bytes live only in an encrypted runtime blob, when the test needs credentials/
tokens, when the answer needs server-side keys, or when the test would bypass
auth/anti-abuse controls.
