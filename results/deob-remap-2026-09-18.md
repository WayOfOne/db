# Deob remap pass — 2026-09-18 (this machine, Windows)

All results derived from jar bytecode (`javap -c -p` + offline `<clinit>`
probes). Nothing executed beyond static initializers; no account, identity,
or session material touched. Certainty labels per AGENTS.md.

## Inputs (re-fingerprinted this session)

| Jar | SHA-256 | Note |
|---|---|---|
| `repository2/dreambot-client.jar` | `66036CB5…0882A` | matches AGENTS.md — same build as 2026-09-09/17, no re-derivation needed |
| `repository2/injected-client-1.12.36.jar` | `FD8A49D6…F6C06F7` | matches AGENTS.md |

Per-build names re-confirmed resolving: `4cs`, `82`, `40o`/`40O` (twins!),
`6x`/`6X` (twins!), `7V`/`7v` (twins!), `4ky`/`4kY` (twins, different roles),
`40P`, `0u`, `4Xi`, `406`, `1r`, `4cx`, `6x`, `4zF`, `l`. The twin check
(`jar tf` grep) re-validates the case-collapse rule: never trust extracted
trees for these names.

## Method advance: universal layer-1 decryptor (the big unlock)

`BootParse.java` + `IndyDecrypt.java` (sources committed under
`.agents/skills/sdn-payload-decrypt/tools/`, compiled output git-ignored).
Verified mechanics, identical in every layer-1 class checked
(`40o`, `4ky`, `6x`, `0Y`, `30`):

- Static tables: `b[]` = encrypted entries (bulk-split from constant blobs in
  `<clinit>`), `c[]`/`g[]` = lazy plaintext cache, `d` = per-thread
  `Cipher` cache, key long `a`/`b` via `x.a(...).a(...)`.
- Every `w:`/`d:`/`s:`/`q:`/`n:` invokedynamic callsite computes
  `(intArg, longConst ^ l3)` at runtime, where `l3 = keyLong ^ methodConst`
  (both visible in bytecode). The bootstrap binds the decrypted string as a
  `MethodHandles.constant` — dynamic args are real, not decoy.
- Lazy entry: `idx = (intArg ^ (siteLong & 32767)) ^ MASK`
  (MASK per class: `40o` 8968, `4ky` 11544, `6x` 5641, `0Y` 7368, `30` 15690),
  `c[idx] = UTF8(DES/CBC/PKCS5Padding_{BE(siteLong), zero IV}(b[idx]))`.
- `IndyDecrypt` reflectively invokes the class's own private
  `(int,long)->String` decryptor (named `a` or `b` per class — auto-detected).
  Pure in-memory: reads `b[]`, fills `c[]`, allocates Ciphers. Only the
  target `<clinit>` runs (each verified PRNG+DES-only via `javap` first).
- `█:(JJ)` callsites route to a **different** bootstrap, `org.dreambot.l.a`
  (shared resolver; also exposes `Class b(long,long)`,
  `Method d(long,long)`, `Field c(long,long)` — Confirmed static existence,
  full semantics Unknown). `█`-fetched values are only ever null-checked
  (fail-open validity token — Strong inference: anti-tamper/integrity gate;
  a tampered key long flips every site key, and the `█` check fails first).
- Fingerprint that the model is right: decrypted site indices form exact
  permutations of the table (`40o`: 6 sites → slots 0–5; `0Y`: 9 → 0–8).

Rule for follow-ups: for any layer-1 class, (1) `javap` the method for
`(sipush int, ldc2_w const, lload l3; lxor, indy)` triples, (2) compute
`l3` from the method prologue + dumped key long, (3) `IndyDecrypt`.
Ctor-time sites are always statically computable; method-time sites need the
caller's runtime salt (see limits).

## Findings

### 1. `40P` = authenticator (TOTP) code generator [was TBD] — Confirmed static
- Plain class (no `4Xi` parent): static `1r 3` engine + `int[] 6` slot with
  setter/getter; `1(Object[]{6n account, Long salt})` → `l3 = a ^ salt` →
  `new 4cx(account)` → `1r.8(...)` with `Instant.now()` → int code;
  `InvalidKeyException` → -1.
- `1r`: `Duration` fields, ctors `(Duration,long,int[,String])`,
  `int 8(Object[]) throws InvalidKeyException` (SecretKey + Instant refs in
  pool — Strong inference: TOTP/HOTP core).
- `4cx implements javax.crypto.SecretKey`, wraps `core/6n val$account`
  (account-derived key material; `getEncoded`).
- Callers (constant-pool refs): `api/methods/login/LoginUtility` (adjacent
  to `RSLoginResponse.BAD_AUTH_CODE`) and `api/randoms/LoginSolver`.
- Listing: `listings/dreambot/{40P,1r,4cx}.javap`.

### 2. Module census corrected: 44 `0u` subclasses (was 43), 3 `4Xi` — Confirmed static
Direct-superclass parse over all 2891 classes (custom pool walk; long/double
two-slot handling verified against `4Xb→4Xi`). `0u` kin:
`01 02 0B 0H 0J 0M 0N 0U 0Y 0b 0d 0e 0f 0i 0l 0n 30 34 35 37 3A 3C 3F 3G
3I 3K 3P 3S 3V 3W 3X 3_ 3a 3c 3g 3h 3k 3o 3p 3q 3s 3t 3x 3z`.
`4Xi` kin: `40p 4XB 4Xb` (matches 82's constructed set below).
No class outside the families references `0u` by name → module methods are
invoked reflectively (salt from runtime annotation metadata via 4Xb-style
`Method.getAnnotation` reads) — method-time salts are runtime-gated, noted
per module. `82` references `4Xi` (static dispatch into the 3 modules).

### 3. `0Y` = the constructor/call rewrite module targeting `406` — Confirmed static
- Matches `TypeInsnNode.desc` (NEW sites) + `MethodInsnNode.owner/name`, inserts
  `new TypeInsnNode(org/dreambot/406)` + `new MethodInsnNode(owner 406, …)`
  (two plaintext `ldc class 406` constants). This is the `aar → 406` redirector
  at module level: Phase-3 item 1 advances from NOT VERIFIED to
  module+target-confirmed; end-to-end runtime ClassNode before/after still open.
- Registration: `0u.<init>("", "cache")` — module name **`cache`**
  (ctor INDY `q(10106, …)` decrypted offline → `c[8]`; Confirmed runtime).
- 8 further match comparands identified by position
  (TypeInsn desc ×2, Class.getName+replaceAll ×3, MethodInsn owner ×4+1,
  name ×3) with `(int, const)` pairs on record; plaintext needs the
  dispatcher salt → Unknown (runtime-gated).
- Listing: `listings/dreambot/0Y.javap`.

### 4. `30` = frame-hook injector registered as `Client.fps`/`fps` — Confirmed static
- Registration: `0u.<init>("Client.fps", "fps")` (both ctor INDY `n:` sites
  decrypted offline → `c[1]`, `c[11]`; Confirmed runtime).
- Body shape: MethodInsn owner/name matching + `JumpInsnNode`/`TypeInsnNode`
  insertion referencing `org/dreambot/4cs` via the `8n.4()` template —
  injects a 4cs-payload-classloader branch at the client's fps path
  (Strong inference on exact inserted semantics; `fps` = frame-counter field
  on injected `client` — classic per-frame hook point).
- Listing: `listings/dreambot/30.javap` (was already in repo from an earlier
  session; registration strings are new).

### 5. `40o` path-map rule table DECRYPTED — Confirmed static + runtime
Rule array mechanism corrected: `40o.2/7` are **plain** `(String)→String` /
`(String,String)→String` statics (no Object[] dispatch); the `█ → 7V[]`
fetch is a null-guard only (never iterated — verified by data-flow).
All six `w:` strings decrypted offline (`40o.a` = 54924725345123):

| Site | Idx | Plaintext | Role |
|---|---|---|---|
| `2: w(23174,…)` | 3 | `xtea` | endsWith suffix |
| `2: w(26679,…)` | 0 | `cache` | replacement segment |
| `2: w(15399,…)` | 4 | `xtea` | replacement segment |
| `7: w(2244,…)` | 2 | `random.dat` | equalsIgnoreCase predicate |
| `7: w(24319,…)` | 5 | `preferences` | contains predicate |
| `7: w(18814,…)` | 1 | `dreambot.path` | `System.getProperty` key |

- `40o.2(path)`: path endsWith `xtea` → return
  `<6x.7(…)> + sep + "cache" + sep + "xtea"` (creates missing parents via
  `File.mkdirs`, guarded by exists-check); else return input (fail-open).
  Only caller: `406(String,…)` (RAF opens — the XTEA cache files).
- `40o.7(base, name)`: `"random.dat".equalsIgnoreCase(name)` OR
  `name.contains("preferences")` → return
  `base.replace(getProperty("dreambot.path"), <6x.7 base>)`; else identity.
  (Direction per operand order as observed — Strong inference on intent.)
  Only caller: `4zF` (see 6). Note `4zF(String)` passes `""` → never maps.
- `406(File,…)` is identity (`5` returns arg); URI/file ctors of `4zF`
  likewise except via `4`/`9`. Correction to prior map: 406 does NOT call
  40o reflectively — direct `invokestatic 40o.2(String)`.
- Listing: `listings/dreambot/40o.javap`.

### 6. `4zF extends java.io.File` = File-side redirector — Confirmed static
- `(String parent, String child)` → `File(40o.7(parent,child), child)`;
  `(File parent, String child)` → parent if
  `40o.7(parentAbs,child).equalsIgnoreCase(parentAbs)` else `new File(mapped)`;
  `(String)` → `40o.7(path,"")` (identity by construction);
  `(URI)` → identity; `listFiles()` → **unmapped passthrough** (corrects the
  assumption it re-maps listings). `<clinit>` = PRNG only.

### 7. `6x.7` = cache-home resolver vocabulary — Confirmed static + runtime
13 `s:` strings decrypted offline (`6x.a` = 11212765722182; chain
`salt = (a40o^107970916076514) ^ 125583774779576` for the 40o.2 path):

`user.home` (property key for cached base `3N`), `UserHomeCache` (3N suffix),
`[^A-Za-z0-9]` (sanitizer), `jagexcache` / `oldschool` / `LIVE` (cache
segments), `JX_CHARACTER_ID`, `FreshStarts`, `tmpDB`, plus user-facing log
lines (`Appending user home `, `You're using a default cache location of: '`,
`…use the -userhome parameter…`, `Built user home of `).
So the 40o.2 xtea replacement roots at the Jagex-cache home with an explicit
`-userhome` override — behavior spec for the fork's cache-path handling.
Full 6x.7 return semantics (account lookup via `5I`/`core/9`) stay
Strong inference — executing it would load account-state classes; not run.
Listing: `listings/dreambot/6x.javap` (new).

### 8. `82` constructed set = `{4Xb, 40p, 4XB}` — Confirmed static
No `406` (406/4zF/0u reach the pipeline through module dispatch, not direct
construction). Listing: `listings/dreambot/82.javap` (new).

### 9. `4ky` persistent-id shape advanced, exact path still Unknown
- `<clinit>` verified PRNG+DES-only (Runtime.exec/Files/UUID calls all live
  in methods) → offline dump safe.
- Decrypted `c[]`: `wmic csproduct get UUID`, `os.name`, `user.home`,
  `.jid`, `win`, + 4 fallback UUID constants
  (`0000…`, `FFFF…`, `FEFE…`, `03000200-0400-…`) mirrored in the `Set 4`.
  The id-file inputs are `user.home`-rooted with a `.jid` name
  (Confirmed static + runtime).
- Static `7` = `8584252e-…-1046430731`, byte-identical across two fresh JVM
  loads → deterministic **build constant**, not a live id (Confirmed runtime;
  role Unknown — candidate namespace/version marker; never mistake for identity
  material). `b[]` (16 entries) stays encrypted (method-time salts).
- Exact subdirectory + filename need the dispatcher's runtime salt
  (no static referrer to `4ky` exists — reflective-only invocation) →
  Unknown (runtime-gated). NOT pursued by execution: running the path method
  would write identity material to the real FS (non-goal + side effect).
- Listing: `listings/dreambot/4ky.javap` (new).

### 10. `dd`/`gy` login-request static ceiling — Confirmed static
`dd` builders (triplicated `ab/ag/ae(long,String)` variants — the known
obfuscation duplication): RSA `BigInteger` pair (`al/aj`), form fields
`data2=`, `&dest=`, `m=accountappeal/login.ws`, `dest=passwordchoice.ws`,
server markers `OFFLINE`/`WRONG`/`RELOAD`/`Not permitted for social network
accounts.`. Wire framing (POST field order, vt-blob placement, live opcodes)
stays NOT VERIFIED — needs the Phase-3 item-5 sanitized live trace; no
credential-bearing execution is available or attempted here.
Listings: `listings/injected-client/{dd,gy}.javap` (rewrites byte-identical —
no diff).

## Status vs migration-plan Phase 3
1. `aar → 406` rewrite: module + target confirmed; runtime before/after open.
2. `40o.2` rule table: DONE (suffixes, replacements, callers, fail-open).
3. `4kY` id-file path: inputs + format constants mapped; exact subdir/file Unknown.
4. P2P payload mapping: unchanged (SDN dead; see sdn-payload-decrypt session).
5. Live request framing: static ceiling recorded; sanitized-trace experiment open.

## Follow-up recipes (mechanical from here)
- Remaining 41 `0u` modules: same recipe (ctor INDY → registration name via
  `IndyDecrypt`; method-time match strings need a runtime capture — the
  sanctioned agent pattern from the SDN session applies: dump
  `(int,long,salt)` at the `b/a(int,long)` call boundary, replay offline).
- `l.a` shared resolver + `7V[]` token semantics: one focused read.
- `6x.7` return-value flow (lines ~5330–5625 of its listing): control-flow
  read only, no execution.
