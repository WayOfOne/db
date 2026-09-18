# 0u module census — 2026-09-18 (this machine, Windows)

Follows `results/deob-remap-2026-09-18.md` (same jars, same build
`66036CB5…0882A`; same method: `BootParse`/`CtorParse` + `KeyDump` +
`IndyDecrypt`). All derived from jar bytecode; only PRNG+DES `<clinit>`s
executed (every one of the 46 verified clean first). No feature methods run.

## Registry mechanics (`0u` base) — Confirmed static

- `0u implements 4Sf`; `0u(String, String)` stores arg1→field `3`,
  arg2→field `0`, flags `5=false, 2=true, 6=false`.
- Per-module state: `ClassNode 7`, `MethodNode 9`; predicates
  `2/7/0(Object[])`, matcher `6(MethodNode)`, applier `5(Object[])`,
  abstract template `4(Object[])→InsnList`.
- Shared transform context: `static 7V[] 4` with setter `3(7V[])` / getter
  `2()` — the `█`-fetched array all modules null-guard on. 7V[] is session
  context, not a rule list (data-flow verified in `40o.2`/`40o.7`: fetched,
  null-checked, never iterated).
- `4Xi` base: abstract predicates `1/3(Object[])`, global string `8`
  (setter `4(String)` / getter `0()`).

## Census: 44 direct subclasses + 1 indirect (45 classes, 44 concrete)

Direct-superclass parse over all 2891 classes + `org/dreambot/0u`
constant-pool ref scan (45 hits = 44 modules + `0u` itself; no external
caller — module methods are invoked reflectively with runtime salts).

`3G` is abstract (`(String,long)` ctor, static `g` = **`fieldsetlistener`**,
decrypted in `<clinit>`); its only child `36` inherits the field-set-listener
role with caller-provided first arg (runtime-gated registration).

## Registration table (ctor `0u.<init>` args, all decrypted offline)

`field 3` = first arg, `field 0` = second. `"null"` is the verbatim
4-char sentinel (quoted-probe verified — single-constraint modules).
`36` via `3G`: `(caller-provided, "fieldsetlistener")`.

| Module | field 3 | field 0 | Read |
|---|---|---|---|
| `01` | `Model.drawTriangles` | `drawModels` | render hook |
| `02` | `gamethread` | *(single — 1 site)* | game-thread hook |
| `0B` | `Client.setLoginResponse` | *(single)* | login-response hook |
| `0H` | `exp` | *(single)* | — |
| `0J` | `MachineInfo.getUUID` | `getuuid` | identity-adjacent hook (mapped only, never executed) |
| `0M` | `RuneLite.stack` | `experimental` | — |
| `0N` | `Model.renderAtPoint` | `processuid` | render hook |
| `0U` | `Model.animateModel` | `animate` | render hook |
| `0Y` | `""` (const) | `cache` | RAF redirector (§3 prior session) |
| `0b` | `experimental` | *(single)* | — |
| `0d` | `Client.draw` | `fps` | draw hook |
| `0e` | `Actor.hitsplats` | `widgetloop` | actor hook |
| `0f` | `"null"` | `disablesounds` | single-constraint |
| `0i` | `"null"` | `httprequest` | single-constraint (HTTP hook) |
| `0l` | `"null"` | `getproperty` | single-constraint (property hook) |
| `0n` | `browseDesktop` | *(single)* | Desktop.browse hook (awt/URI refs in body) |
| `30` | `Client.fps` | `fps` | frame hook + 4cs injection (prior session) |
| `34` | `Projectile.target` | `projectilespawnlistener` | projectile event |
| `35` | `experimental` | *(single)* | — |
| `37` | `Client.setLoginState` | `loginlistener` | login event |
| `3A` | `Animation.id` | *(single)* | animation hook |
| `3C` | `Animation.id` | *(single)* | animation hook (dup of 3A — cf. dd triplication) |
| `3F` | `Region.maxRenderTileX` | `regionvisibility` | region hook |
| `3I` | `Widget.setX` | `widgetloop` | widget hook |
| `3K` | `SettingsClass.getMusicVolume` | `sounds` | audio hook |
| `3P` | `Menu.addRow` | `menu` | menu hook |
| `3S` | `Menu.addCancelRow` | `menu` | menu hook |
| `3V` | `hitsplatslistener` | *(single)* | hitsplat event |
| `3W` | `Region.isTileVisible` | `regionvisibility` | region hook |
| `3X` | `Projectile.target` | `projectilelistener` | projectile event (dup of 34, different listener) |
| `3_` | `Mouse.immediateMoveTime` | `tracker` | mouse tracking (MouseEvent refs) |
| `3a` | `canvas` | *(single)* | canvas hook (AWTEvent refs) |
| `3c` | `ClassData.getClassData` | `reflection` | reflection hook |
| `3g` | `Client.processMinimapClick` | `region` | minimap hook |
| `3h` | `Actor.addSpotAnimation` | `animationlistener` | animation event |
| `3k` | `Model.renderWithUID` | `drawModels` | render hook |
| `3o` | `MouseTracker.length` | `tracker` | mouse tracking |
| `3p` | `MachineInfo.init` | `experimental` | identity-adjacent (mapped only) |
| `3q` | `Client.handleLoginResponse` | `loginlistener` | login event |
| `3s` | `Region.sendWalkOnScreen` | `region` | walking hook |
| `3t` | `Client.processAction` | `actionlistener` | action (doAction-path) event |
| `3x` | `Client.setLoginResponse` | `loginlistener` | login event (dup of 0B, different 2nd arg) |
| `3z` | `Region.resetWalkVariables` | `region` | walking hook |
| `36` | (caller) | `fieldsetlistener` | field-set listener (via abstract `3G`) |

`(single)` = one INDY site in ctor (second arg is a plaintext const or absent
— positionally the name-only form; exact const noted per class in
`ctorparse.txt` approach, omitted here for brevity).
All Confirmed static + runtime (offline decrypt verified, zero failures
across 75 sites). Hook names mirror RuneLite API surface (`Client.*`,
`Actor.*`, `Model.*`, `Region.*`, `Menu.*`, `Projectile.*`, `Animation.*`,
`Widget.*`, `Mouse.*`) — these are the game-client mixin points.
`*listener` second args mark event-bus fan-out modules.

## 4Xi dispatcher roles — Confirmed static shape, Strong inference labels

- `82` constructs `{4Xb, 40p, 4XB}` (re-verified; no `406`) and Guava-scans
  for `@0L`.
- All three inspect `java.lang.reflect.Method`: `getAnnotation`,
  `getGenericReturnType/ParameterTypes`, `getName`, with StringBuilder
  assembly — annotation + erased/generic-signature match predicates for the
  registry: `40p` stringless (structural only), `4XB`/`4Xb` string-guided
  (layer-1 tables; `4Xb` resolved via `Method.getAnnotation` reads).
- `l` = runtime linker: `Class.forName` + field/method resolution by
  (name, type/return/params) with superclass/interface walk; public
  `Class b(long,long)` / `Method d(long,long)` / `Field c(long,long)` take
  decrypted longs. Full call-graph semantics Unknown (reference only).

## 6x.7 return formula (control-flow read) — Confirmed static

- Env override: `getenv("JX_CHARACTER_ID")`; else if validity-flag clear:
  `new UID().toString().split(":")[0].replace("-","")` + `FreshStarts`/`tmpDB`
  (per-run unique base — "fresh starts" mode).
- `3N` (cached base) extended, default-location warning via `Logger.warn`,
  `Built user home of …` via `Logger.log`.
- Ensures `3N` then `3N/jagexcache/oldschool/LIVE` exist (mkdirs) and
  **returns `3N`**. So the `40o.2` xtea replacement is exactly
  `3N + sep + "cache" + sep + "xtea"`. `java/rmi/server/UID` import noted
  (run-unique ids feed directory names, not exfiltrated here).

## Tooling added (committed sources, ignored output)

- `KeyDump.java` — key longs for N classes in one JVM.
- `CtorParse.java` — ASM ctor scan: `KEYM field/const` + `SITE indy/int/const`
  (fixed: LSTORE arrives via `visitVarInsn`; ICONST range handled).
- `IndyDecrypt.java` — generalized to any `(int,long)->String` decryptor
  name (`a`/`b` auto-detect); output disambiguates `<java-null>` vs `"null"`.
- Drivers/scratch stay out of the repo (`%TEMP%\opencode\regbatch.py`,
  `keydump.txt`, `ctorparse.txt`, `regbatch.txt` — reproducible via the above).

## Still open (unchanged boundaries)

- Method-time match strings for all modules (dispatcher salt = runtime).
- `36` first registration arg (caller-provided long salt + string).
- `l`/`7V` full semantics; per-module injection bodies beyond `0Y`/`30`.
