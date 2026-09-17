package bot.api;

/** Music unlock reads off the mined `Song` table (793 tracks:
 * `results/song-table.txt`). Per-track playback stays out: rows match by
 * decrypted display titles DreamBot resolves internally, so no static
 * row mapping exists — scripts click rows via `Widgets` text search
 * themselves. Pure reads unless noted. */
public final class Music {
    private Music() {
    }

    /** True when the track is unlocked (varp bit test). Pure read. */
    public static boolean isUnlocked(Song song) {
        return song != null && (Vars.varp(song.varp) & (1 << song.bit)) != 0;
    }

    /** Open the music tab. Game-only. */
    public static boolean openTab() throws Exception {
        return Tabs.music();
    }
}
