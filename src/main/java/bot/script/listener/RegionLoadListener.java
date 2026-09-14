package bot.script.listener;

/** Scene-load callbacks. No pinned scene event exists in this build, so
 * this fires on the LOADING-exit edge (approximation, documented):
 * login and hopping also pass through it. */
public interface RegionLoadListener extends java.util.EventListener {
    /** The scene finished loading. */
    default void onRegionLoad() {
    }
}
