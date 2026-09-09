package bot.ui;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

/** Per-script settings that build their own UI. Declare items; the client
 * renders the controls. Mirrors the DreamBot script-config pattern on
 * RuneLite's config service. */
@ConfigGroup("script")
public interface ScriptConfig extends Config {
    @ConfigItem(
        keyName = "enabled",
        name = "Enabled",
        description = "Run this script's loop"
    )
    default boolean enabled() {
        return true;
    }

    @ConfigItem(
        keyName = "loopDelay",
        name = "Loop delay (ms)",
        description = "Sleep between loop iterations"
    )
    default int loopDelay() {
        return 600;
    }
}
