import org.slavicmyths.yaga.YagaAccessRules;

/** Plain Java only: no Minecraft classes, game bootstrap, world or server. */
public final class YagaAccessCheck {
    public static void main(String[] args) {
        int cases = 0;
        for (int stage = 0; stage <= 4; stage++) for (int bits = 0; bits < 16; bits++) {
            boolean creative = (bits & 1) != 0, likho = (bits & 2) != 0;
            boolean tugarin = (bits & 4) != 0, nightingale = (bits & 8) != 0;
            boolean result = YagaAccessRules.allowed(creative, stage, likho, tugarin, nightingale);
            if (result != (stage > 0 || bits != 0)) throw new AssertionError(stage + ":" + bits);
            cases++;
        }
        // Creative unlock -> persisted entry -> survival without combat credit.
        if (!YagaAccessRules.allowed(false, 1, false, false, false)) throw new AssertionError("lost access");
        if (YagaAccessRules.allowed(false, 0, false, false, false)) throw new AssertionError("unearned access");
        for (String id : new String[]{"name_your_misfortune", "steppe_champion", "defeat_nightingale"})
            if (!YagaAccessRules.victory(id)) throw new AssertionError(id);
        for (String id : new String[]{"", "meet_nightingale", "guest_of_yaga", "defeat_gorynych"})
            if (YagaAccessRules.victory(id)) throw new AssertionError(id);
        System.out.println("PASS Yaga access: " + cases + " policy combinations + persistence/trigger boundaries; OFFLINE_ONLY");
    }
}
