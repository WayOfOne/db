# DreamBot Client Mappings (ported from bot-client)

Carried over from `bot-client/docs/dreambot-random-dat-hwid-findings.md`,
`bot-client/docs/dreambot-sdn-script-findings.md`, and
`bot-client/dreambot/deob/README.md`, then **re-verified 2026-09-09 against
the live jars** (see `results/phase1-reverification.md` for the evidence log).
Rows marked [DRIFT] differ from the bot-client docs on this build.
Obfuscated names are per-build shapes, not constants.

> Staleness note (2026-09-09): bot-client docs reference
> `C:\Users\D_Jok\DreamBot\...` and five `.cache\bin` payloads. On
> `C:\Users\thewa\DreamBot\...` the catalog still names `P2P Master AI` but
> `.cache\bin` holds one payload (`838b43e2b04f367b132c939dc2ae8abf`,
> 6702000 bytes, SHA-256 `439bd4d5…8ecca1`, magic `41 7E 5F AB`, not `PK`).
> Quote current inventory, not the old hashes.

## 1. Injected-client map (`injected-client-1.12.36.jar`)

| Class | Members | Meaning (behavior-assigned) | Status |
|---|---|---|---|
| `me` | `ao(String,String,String,int,int)` + variants, static `aw:Laac;` (plus sibling `al`, `aj`) | Field owner; `ao` still exists but no longer touches `aw` | Confirmed static |
| `cl` | `az(String,String,String,IIB)` | [DRIFT] Cache setup now lives here: `new File(ie.ap,"random.dat")`, legacy `separatorChar+"random.dat"` loop, `RandomAccessFile` read/seek/write/seek/close fallback; 3× `putstatic me.aw`; called 6× from `client` | Confirmed static |
| `aar` | `(File,"rw",25L)` wrapper | Random-access file wrapper around `random.dat` | Confirmed static |
| `aac` | seek/read (`ay`, `ag`) | Buffered wrapper; `new aac(new aar(f,"rw",25L),24,0)` | Confirmed static |
| `jl` | `ab(byte):[B` | Reads 24 bytes via `me.aw` @0; all-zero -> `IOException` -> 24x `0xff` | Confirmed static |
| `bz` | `ag(xy,int,int)` | Copies 24 bytes from `me.aw` into existing `xy` buffer @offset | Confirmed static |
| `mr` | `gf(xy,int,int)` | Wrapper: `ck.gu(buf.aj,off,…)` then `bz.ag(buf,off,…)` | Confirmed static |
| `ck` | `gu(byte[],int,int)` | Only visible `client.lx` writer; null -> `new byte[24]`, copies 24 | Confirmed static |
| `client` | `lx:[B` | Process-global 24-byte fallback, not per-account in visible bytecode | Confirmed static |
| `dd`, `gy` | account-request builders | `client.lx != null ? write(lx) : write(jl.ab(0))`, then `xy.dl` RSA; URL `m=accountappeal/login.ws`, `dest=passwordchoice.ws` | Confirmed static (flow, not live wire bytes) |
| `xy` | `ie(...)` writers, `dl(BigInteger,BigInteger,int)` | Packet buffer + RSA encrypt | Confirmed static |
| `vt` | `re/ac` (OS cmd), `kg(int)` (process/JVM), `az(xy,int)` (serialize, marker 9) | Platform/HWID object: `wmic csproduct get UUID` / `cat /etc/machine-id` / `system_profiler …`, `ProcessHandle`, JVM args; Linux fallback `12345678-0000-0000-0000-123456789012` | Confirmed static |
| `cl`, `pj` | — | `cl` is on the setup path (`random.dat`/`RandomAccessFile`/`jagex_cl` ldc); `pj` holds the `random.dat` literal only as an unread `static final String ac` constant — no data-flow role | Confirmed static |
| `dg` / `eb.pt` / `ea` / `cp` / `pw` | `client.pn.az(…) -> vt`, `vt.kg(-1)`, field updates | `vt` init path | Confirmed static |
| `me.aw` users | constant-pool refs | `bz`, `cl`, `jl`, `lw`, `me` (ignore `bb`/`qr` `me.aw(…)` method refs — different member) | Confirmed static |
| `jl.ab(byte)` callers | constant-pool refs | `dd`, `gy`, **and `client` (3 sites, same `lx`-branch shape)** (`dj` `jl.ab(I)V` is a different overload) | Confirmed static |
| `client.lx` refs | constant-pool refs | `ck`, `dd`, `gy` only | Confirmed static |

`random.dat` lookup shape (`cl.az`, Confirmed static): try
`<cacheRoot>/random.dat` ([DRIFT] was `me.ao`) -> legacy bases (`c:/rscache/`, `/rscache/,
`c:/windows/`, `c:/winnt/`, `c:/`, home, `/tmp/`, …) + legacy cache dirs ->
else create/open target (`read` one byte, `seek(0)`, `write`, `seek(0)`,
`close`) and wrap. Path derives from cache roots/user-home, never from
username/account-id/token — cache-level, not per-account.

## 2. String / constant anchors

Injected: `random.dat`, `jagex_cl`, `java/io/RandomAccessFile`,
`wmic csproduct get UUID`, `cat /etc/machine-id`, `system_profiler`,
`ProcessHandle`, `me.aw`, `jl.ab`, `client.lx`.
DreamBot jar: `ClassLoader`, `defineClass`, `JarInputStream`, `Cipher`,
`org/objectweb/asm/tree/ClassNode`, `java/util/UUID randomUUID`,
`SecureRandom`, `MessageDigest`, `java/net/http/WebSocket`, `Gson toJson`,
`getDeclaredField`, `setAccessible`.
`client-1.12.36.jar` sweep found no `random.dat` anchor; `dreambot-client.jar`
sweep found no plaintext `random.dat`/`me.aw`/`jl.ab` (only unrelated
`RandomAccessFile` use) — both Confirmed static at doc time.

## 3. Runtime loader / transformer map (`dreambot-client.jar`)

| Class | Meaning | Status |
|---|---|---|
| `org.dreambot.4cs` (case-sensitive; cf. `4cS`) | `ClassLoader` ctor `(long,byte[])`; `JarInputStream` collect; Base64/cipher decrypt; `findClass` -> `defineClass` | Confirmed static |
| `org.dreambot.82` | ASM registry; Guava `ClassPath` scan for `@0L`; decrypts metadata; dispatches `ClassNode`; directly constructs `4Xb`, `40p`, `4XB` | Confirmed static |
| `org.dreambot.4Xi` + `40p`, `4XB`, `4Xb` | Transformer modules (`extends 4Xi`; [DRIFT] `40P` does **not** extend `4Xi` — different role, TBD) | Confirmed static (existence; per-target table pending) |
| `org.dreambot.0u` family (43 confirmed subclasses incl. `30`) | `ClassNode.methods`/`InsnList` iteration; `MethodInsnNode` owner/name match vs decrypted strings; `InsnList` rewrite insert | Confirmed static |
| `org.dreambot.406` | `extends RandomAccessFile`; `(String,mode)` -> `40o.2(path)` -> `super`; `(File,mode)` -> `5(file)` (identity) -> `super` | Confirmed static |
| `org.dreambot.40o` / `2(String)` | Rule array (`7V`) vs decrypted suffix; replacement from `System.getProperty` + separators + decrypted strings; mkdirs parents; no-match returns input | Confirmed static (mechanism; rule table NOT VERIFIED) |
| `org.dreambot.0L` / `4Sl` / `7s` | Transformer annotations; encrypted `value()` (+ boolean `2` / `3`) | Confirmed static |
| `aar -> 406` rewrite of `me.aw` path | The one rule that would redirect `random.dat` | NOT VERIFIED (needs before/after `aar` ClassNode + observed path pair) |

## 4. DreamBot identity / telemetry map

| Class | Meaning | Status |
|---|---|---|
| `org.dreambot.4ky` (lowercase; [DRIFT] uppercase `4kY` twin exists but has no UUID logic) | Persistent-id helper: file under `System.getProperty(<enc>)/<enc>`; exists -> `Files.readAllLines().get(0)`; else `UUID.randomUUID() + SecureRandom.getInstanceStrong().nextInt()`, `Files.write`, return. Exact path NOT VERIFIED (invokedynamic/DES strings) | Confirmed static (shape) |
| `AccountManager.getAccountHash()` | `String` account string -> `MessageDigest` -> Base64; null/empty -> `""`; no `byte[24]`, no `random.dat`/`me.aw`/`jl.ab`/`client.lx` | Confirmed static |
| `org.dreambot.4z_` | Telemetry/session object: UUID (`9J."2"`), account hash (`"8"`), legacy flag, timing, session objs, map copy | Confirmed static |
| `org.dreambot.404` | `HttpClient` + `WebSocket` + `BlockingQueue<String>` + `Gson`; `9x` envelope -> `toJson` -> `offer` -> `take` -> `sendText`; `buildAsync` with config headers | Confirmed static |
| `4z_` vs `random.dat` | Separate flows: `4z_`->`9x`->JSON->DreamBot WebSocket vs `me.aw`->`jl.ab`->`dd/gy`->RSA->client web request | Confirmed static |

## 5. Script-loader / SDN map (`BotData\client.jar` + `.cache`)

| Item | Meaning | Status |
|---|---|---|
| `scripts.dat` | Line-separated catalog; contains `P2P Master AI` (full list re-verified 2026-09-09, 46 names) | Confirmed static |
| `.cache\bin\<hash>` | Opaque payload(s), no `.jar` extension, no `PK` magic at last inspection; count varies (5 then, 1 now) | Unknown (format + mapping) |
| `LocalLoader` -> `8Q` ([DRIFT] was `9O9`; absent from all jars) | Local discovery/reload/clear; temp-file copies + suffix-matched dir scan into `URLClassLoader` (suffix decrypted) | Confirmed static |
| `NetworkLoader` -> `3Z` ([DRIFT] was `9OT`) | Free/premium catalog retrieval + mode feature gate | Confirmed static |
| `ScriptManager` | Discovered lists + current script state, calls local loader | Confirmed static |
| `P2P Master AI` payload hash | Which `.cache\bin` file (if any) is this script | Unknown (needs before/after inventory + classloader code-source correlation x2 profiles) |

## 6. Data-flow summary (do not merge branches without observed copies)

```text
me cache setup -> random.dat path -> aar/aac -> me.aw
  -> jl.ab() | bz.ag() 24 bytes -> dd/gy (+client.lx branch) -> xy.dl RSA -> account web request
DreamBot account activation -> getAccountHash() + persistent UUID -> 4z_ -> 9x/Gson -> 404 WebSocket
packed bytes -> 4cs -> 82/4Xi/0u transforms -> (406/40o.2 ?) -> same me.aw reader path
vt probes -> vt.az -> xy buffer (separate source from random.dat)
```

Fallbacks (NOT a GUID pool): `jl.ab` failure -> 24x `0xff`; `ck.gu` init ->
24x `0x00`; `vt` Linux unexpected output ->
`12345678-0000-0000-0000-123456789012`.
