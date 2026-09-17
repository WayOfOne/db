package bot.api;

/** Arceuus favour houses. Values mined from DreamBot's own `House` enum:
 * per-house varbit (`results/javap-c-db-favour.txt`), value =
 * `getBitValue(varbit)`, percent = value / 10. Pure reads. */
public final class Favour {
    private Favour() {
    }

    /** Favoured houses. */
    public enum House {
        ARCEUUS(4896),
        HOSIDIUS(4895),
        LOVAKENGJ(4898),
        PISCARILIUS(4899),
        SHAYZIEN(4894);

        /** Favour varbit. */
        public final int varbit;

        House(int varbit) {
            this.varbit = varbit;
        }
    }

    /** Raw favour value. */
    public static int value(House house) {
        return house == null ? 0 : Vars.varbit(house.varbit);
    }

    /** Favour percent. */
    public static double percent(House house) {
        return value(house) / 10.0;
    }
}
