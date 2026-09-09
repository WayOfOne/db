# scripts/

Drop compiled script jars here. Each jar holds one or more `@ScriptManifest`
scripts; the directory itself is the catalog — no registry, no server.

```bat
gradlew run -Pscripts=scripts -Pscript=CowHitter
```

Build the bundled samples into jars with `gradlew sampleJar`, then copy
`build/sample-scripts/*.jar` here to try them. Your own scripts: compile
against `build/dist/dreambot.jar` plus the pinned `runelite-api` jar (see
`AGENTS.md` for the exact paths), annotate with `@ScriptManifest`, drop in.
