package sample;

import bot.script.Script;
import bot.script.ScriptManifest;

/** Minimal script used by the offline smoke test: loops five times, then stops. */
@ScriptManifest(name = "Hello", version = "1.0", description = "Smoke-test script.", author = "fork")
public final class HelloScript extends Script {
    int ticks;

    @Override
    public int onLoop() {
        ticks++;
        return ticks >= 5 ? -1 : 100;
    }
}
