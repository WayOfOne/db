package bot.api;

/** Achievement-diary tier reads. Every area diary exposes
 * easy/medium/hard/eliteFinished() as `getBitValue(varbit) == 1`; all 48
 * varbits mined from the 12 area classes
 * (`results/diary-varbit-table.txt`, `tools/ParseDiaries.py`). Pure reads. */
public final class Diaries {
    private Diaries() {
    }

    /** Diary areas, in DreamBot's own area set. */
    public enum Area {
        ARDOUGNE(4458, 4459, 4460, 4461),
        DESERT(4483, 4484, 4485, 4486),
        FALADOR(4462, 4463, 4464, 4465),
        FREMENNIK(4491, 4492, 4493, 4494),
        KANDARIN(4475, 4476, 4477, 4478),
        KARAMJA(3578, 3599, 3611, 4566),
        KOUREND_KEBOS(7925, 7926, 7927, 7928),
        LUMBRIDGE_DRAYNOR(4495, 4496, 4497, 4498),
        MORYTANIA(4487, 4488, 4489, 4490),
        VARROCK(4479, 4480, 4481, 4482),
        WESTERN_PROVINCES(4471, 4472, 4473, 4474),
        WILDERNESS(4466, 4467, 4468, 4469);

        /** Varbits for easy/medium/hard/elite, in order. */
        public final int[] tiers;

        Area(int... tiers) {
            this.tiers = tiers;
        }
    }

    /** Diary tiers. */
    public enum Tier {
        EASY,
        MEDIUM,
        HARD,
        ELITE
    }

    /** True when the area's tier is complete. */
    public static boolean finished(Area area, Tier tier) {
        if (area == null || tier == null) {
            return false;
        }
        return Vars.varbit(area.tiers[tier.ordinal()]) == 1;
    }

    /** True when every tier of the area is complete. */
    public static boolean allFinished(Area area) {
        if (area == null) {
            return false;
        }
        for (int varbit : area.tiers) {
            if (Vars.varbit(varbit) != 1) {
                return false;
            }
        }
        return true;
    }
}
