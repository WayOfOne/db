package bot.api;

/** Emotes by book slot. Child ids mined from DreamBot's own Emote enum
 * (results/emote-widget-table.txt); the book lives at group 216.
 * Game-content names only, no DreamBot code. */
public enum Emote {
    AIR_GUITAR(44),
    ANGRY(3),
    BECKON(8),
    BLOW_KISS(17),
    BOW(2),
    CHEER(7),
    CLAP(20),
    CLIMB_ROPE(25),
    CRAZY_DANCE(47),
    CRY(16),
    DANCE(12),
    EXPLORE(49),
    FORTIS_SALUTE(53),
    FLAP(30),
    GLASS_BOX(24),
    GLASS_WALL(27),
    GOBLIN_BOW(22),
    GOBLIN_SALUTE(23),
    HEADBANG(15),
    HYPERMOBILE_DRINKER(42),
    IDEA(28),
    JIG(13),
    JOG(39),
    JUMP_FOR_JOY(10),
    LAUGH(9),
    LEAN(26),
    NO(1),
    PANIC(18),
    PARTY(51),
    PREMIER_SHIELD(48),
    PUSH_UP(37),
    RABBIT_HOP(35),
    RASPBERRY(19),
    SALUTE(21),
    SCARED(34),
    SHRUG(6),
    SIT_DOWN(54),
    SIT_UP(36),
    SKILL_CAPE(43),
    SLAP_HEAD(31),
    SMOOTH_DANCE(46),
    SPIN(14),
    STAMP(29),
    STAR_JUMP(38),
    THINK(4),
    TRICK(52),
    URI_TRANSFORM(45),
    WAVE(5),
    YAWN(11),
    YES(0),
    ZOMBIE_DANCE(33),
    ZOMBIE_HAND(41),
    ZOMBIE_WALK(32),
    FLEX(40);

    /** Child of the emote container (group 216, child 2). */
    public final int child;

    Emote(int child) {
        this.child = child;
    }
}
