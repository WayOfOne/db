# Phase 1 Re-verification — 2026-09-09

Source jars (SHA-256 re-confirmed this session, see AGENTS.md):

- `repository2/injected-client-1.12.36.jar` — 749 entries / 735 classes — `FD8A49D6…98ABF6C06F7`
- `repository2/dreambot-client.jar` — 3196 entries / 2891 classes — `66036CB5…700882A`
- `repository2/client-1.12.36.jar` — 2678 entries / 1687 classes — `82F000DD…4274D3`
- `BotData/client.jar` — 8428 entries / 7422 classes — `E2CAAE50…5F3CBD2`
- `BotData/gamepack.jar` — 1268 entries / 1265 classes — `4F121CE9…900FDD7F60AD`

Toolchain: Temurin JDK 17.0.20.1 (`javap`/`jar`), `tools/ClassByteSearch.py`
(stdlib zip scan, case-safe), PowerShell `Select-String` over dumps.
Artifacts: `results/tf-*.txt` (entry lists), `results/javap-p-injected-all.txt`
(735-class signature dump), per-class bytecode under `listings/injected-client/`
(23 classes), `listings/dreambot/` + `listings/cross-layer/` (21 runtime classes;
case-twin pairs stored jointly as `40o-40O.javap` etc.),
`listings/dreambot-client/` (`LocalLoader`, `NetworkLoader`, `8Q`, `3Z` from
`BotData/client.jar`). (The concatenated `javap -c` transcripts these were split
from were removed after verifying every section landed; 13.6MB saved.)

Legend: [CS] = Confirmed static on the jars above. [DRIFT] = bot-client docs
need correction. [NV] = still NOT VERIFIED (needs runtime evidence).

## 1. Injected client

| Claim | Verdict | Evidence |
|---|---|---|
| `me.aw:Laac` static field in `me` | [CS] | `javap -p`: `static aac aw;` in `me` (also sibling `al`, `aj`) |
| `me.ao(String,String,String,int,int)` exists | [CS] but role changed | still present; body no longer touches `me.aw` (no `cl.*` refs either) |
| Cache setup writes `me.aw` | [CS] [DRIFT] writer moved `me` -> `cl` | `cl.az(String,String,String,IIB)`: `new File(ie.ap,"random.dat")` -> `new aac(new aar…)` -> `putstatic me.aw` (2 paths: direct + legacy loop with `separatorChar+"random.dat"`); fallback `new RandomAccessFile(f,"rw")`, read/seek/write/seek/close, wrap, `putstatic me.aw` (3rd). `jagex_cl_` prefix also in `cl`. 6× `invokestatic cl.az` call sites, all in `client` (duplicated variants) |
| `jl.ab(byte):[B` 24-byte reader | [CS] | `bipush 24; newarray byte`, `me.aw.ay(0)`, `me.aw.ag(buf,…)`, all-zero scan -> `IOException`, catch fills `iconst_m1` (0xff), returns array |
| `jl.ab(B)` callers = dd, gy | [CS] [DRIFT] + `client` | `dd` (2 variants), `gy` (7 lx/ab/dl/appeal hits), plus **3 call sites in `client`** with identical `lx`-branch shape |
| `bz.ag(xy,int,int)` 24-byte copy | [CS] | null-check `me.aw`, `ay(0)`, `bipush 24`, `aac.ac([BIII)` |
| `mr.gf` = ck.gu + bz.ag | [CS] | `invokestatic ck.gu([BII)` then `invokestatic bz.ag(Lxy;II)` |
| `ck.gu` only `client.lx` writer | [CS] | sole `putstatic client.lx` in jar (in `ck`); null -> `newarray byte` init |
| `client.lx:[B` single static | [CS] | one occurrence, owner `public final class client` |
| `client.lx` readers = ck, dd, gy | [CS] | field refs exactly in those three (+`client` itself via its `jl.ab` branch sites — same three classes, no new class) |
| `dd`/`gy` branch + RSA + URL | [CS] | `client.lx` vs `jl.ab(0)` branch, `xy.dl(BigInteger,BigInteger,I)`, `m=accountappeal/login.ws`, `&dest=` (3 duplicated variants in `dd`) |
| `vt` = re/ac/kg/az platform object | [CS] | `re(vt,int):String`, `ac(int):String`, `kg(int)`, `az(xy,int)` all present; `wmic`/`machine-id`/`system_profiler`/`ProcessHandle` literals hit 10× in `vt.javap` |
| `aar(File,String,long)` + `RandomAccessFile ae` | [CS] | `public final class aar`, `java.io.RandomAccessFile ae` field, ctor `(File,String,long)` |
| `random.dat` anchors in cl, me, pj | [CS] with correction | `cl` (2× ldc, on setup path), `me` (ldc present), `pj` holds literal only as unread `static final String ac` constant (`ConstantValue: String random.dat`; zero `Field pj.ac` readers) — literal present, **no data-flow role** |
| `jagex_cl` anchors in cl, me | [CS] | ldc `jagex_cl_` in `cl` setup path; present in `me` |

## 2. DreamBot runtime (`dreambot-client.jar` unless noted)

| Claim | Verdict | Evidence |
|---|---|---|
| `4cs` encrypted loader | [CS] | `extends ClassLoader`, `JarInputStream` iteration, `defineClass` |
| `82` ASM registry (4Xb+40p+4XB, ClassPath scan, @0L, ClassNode dispatch) | [CS] | direct `new 4Xb/40p/4XB`, Guava `ClassPath.from/getAllClasses`, `0L.value()`, `ClassNode.name` check |
| `30` extends `0u`, MethodInsnNode rewrite | [CS] | `ClassNode.methods`/`instructions` iteration, opcode/owner/name checks |
| `0u` transformer family | [CS] enumerated | byte-scan + `javap -p`: **43 confirmed `extends 0u`**: 01,02,0B,0b,0d,0e,0f,0H,0i,0J,0l,0M,0n,0N,0U,0Y,3_,30,34,35,37,3A,3a,3c,3C,3F,3g,3h,3I,3k,3K,3o,3p,3P,3q,3S,3s,3t,3V,3W,3X,3x,3z (3G merely references 0u) |
| `4Xi` impls = 40p, 4XB, 4Xb | [CS] [DRIFT] | all three `extends 4Xi`; **`40P` does not** (`public class 40P`, no extends) — different role, TBD |
| `0L`/`4Sl`/`7s` annotations | [CS] | `value()` + boolean `2` / `3`; ~30 `0L`-mentioning classes incl. `82` (per-target table = Phase 3) |
| `406` RAF shim -> `40o.2` | [CS] | `(String,mode)` -> private `8(String)` -> `40o.2(String)` -> `super`; `(File,mode)` -> `5(File)` identity -> `super` |
| `40o.2(String)` rule mapper | [CS] mechanism, [NV] table | `7V[]` rules via invokedynamic, `equalsIgnoreCase`/`contains`/`endsWith`, `getProperty` + `separator` build, `mkdirs`; match/replacement strings runtime-decrypted |
| Case twins live | [CS] | `4cs`+`4cS`, `40o`+`40O`, `40p`+`40P`, `4XB`+`4Xb`, `4kY`+`4ky` all coexist — `jar tf`/`javap -classpath` only; pair dumps stored as `40o-40O.javap` etc. (splitter collision incident documented, recovered by re-dump) |
| `4kY` persistent-id helper | [CS] [DRIFT] lowercase `4ky` | `org/dreambot/4ky`: `getProperty` -> `readAllLines`, else `randomUUID` + `SecureRandom.getInstanceStrong.nextInt` + `Files.write`; uppercase `4kY` twin exists but has no UUID logic. Exact property/filename strings [NV] |
| `AccountManager.getAccountHash()` | [CS] | `String` -> `MessageDigest` -> `Base64`; no `byte[24]`/`random.dat`/`me.aw`/`jl.ab`/`client.lx` |
| `4z_` telemetry (UUID + hash) | [CS] | fields `0:UUID`, `8:String`, ctor `(UUID,String,int,String,2s,4Sx,String,long,long,Map)`, calls `getAccountHash()`, reads `9J."2":UUID`, `9J."8":Map` |
| `404` WebSocket transport | [CS] | `Gson`, `BlockingQueue`/`ArrayBlockingQueue`, `WebSocket` + `Builder.subprotocols`, `sendText` path |
| `random.dat` in dreambot-client.jar | [CS] negative reproduces | byte-scan: zero hits |
| `9O9`/`9OT` | [DRIFT] renamed | zero hits anywhere; `LocalLoader` -> **`org.dreambot.8Q`**, `NetworkLoader` -> **`org.dreambot.3Z`** (both in `BotData/client.jar`) |
| `8Q` = temp-file URLClassLoader loader | [CS] | `URLClassLoader` mgmt, `createTempFile`, `getProperty` dir scan (documented 9O9 shape under new name) |
| `random.dat` in client-1.12.36.jar | [CS] negative reproduces | byte-scan: zero hits |

## 3. SDN / cache (live `C:\Users\thewa\…`)

- `scripts.dat`: 46 lines, contains `P2P Master AI` [CS].
- `.cache\bin`: single payload `838b43e2b04f367b132c939dc2ae8abf`, 6702000 bytes,
  SHA-256 `439bd4d5d40904c6c070ab196baee509eaa2eb2f7d33a07d7abce6830a8ecca1`,
  magic `41 7E 5F AB` (not `PK`) — format + script mapping [NV].

## 4. Still NOT VERIFIED (unchanged, needs runtime work per migration-plan Phase 3)

`aar -> 406` rewrite; `40o.2` rule table; `4ky` id-file path strings;
`P2P Master AI` payload hash; live `vt`/login opcodes; per-target transformer
table; `40P` role; `4cS` (uppercase twin) role.
