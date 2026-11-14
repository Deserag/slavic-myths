import java.util.Set;
import org.slavicmyths.ships.ShipSectionPlan;

/** Run with javac against ShipSectionPlan.java; no NeoForge or game instance required. */
public final class ShipSectionPlanCheck {
    private static void check(boolean condition, String description) {
        if (!condition) throw new AssertionError(description);
    }

    public static void main(String[] args) {
        int checked = 0;
        int[][] limits = {{1,0,1,1,1,1}, {2,1,2,2,2,2},
                {3,2,3,2,2,3}, {3,2,3,4,4,3}};
        for (ShipSectionPlan.ShipClass type : ShipSectionPlan.ShipClass.values()) {
            int[] expected = limits[type.ordinal()];
            check(type.maxLength == expected[0] && type.maxWidth == expected[1]
                    && type.maxDecks == expected[2] && type.maxMasts == expected[3]
                    && type.maxSails == expected[4] && type.maxSailTier == expected[5],
                    type + " limits");
            checked++;
            for (int length = 0; length <= type.maxLength; length++) {
                for (int width = 0; width <= type.maxWidth; width++) {
                    var configuration = ShipSectionPlan.validate(type, length, width);
                    check(configuration.sections().size() == configuration.sectionCount(),
                            type + " unique logical section positions");
                    checked++;
                    if (length < type.maxLength && width < type.maxWidth) {
                        var extended = configuration.extend(1);
                        var widened = configuration.widen(1);
                        var first = extended.widen(1);
                        var second = widened.extend(1);
                        check(first.sections().equals(second.sections()),
                                type + " order-independent geometry");
                        Set<ShipSectionPlan.Section> before = configuration.sections();
                        var delta = configuration.additionsTo(first);
                        check(!delta.isEmpty() && delta.stream().noneMatch(before::contains),
                                type + " only new cells");
                        check(before.size() + delta.size() == first.sectionCount(),
                                type + " exact construction delta");
                        checked += 3;
                    }
                }
            }
            try {
                ShipSectionPlan.validate(type, type.maxLength + 1, 0);
                throw new AssertionError(type + " allowed length past the limit");
            } catch (IllegalArgumentException expectedError) { checked++; }
            try {
                ShipSectionPlan.validate(type, 0, type.maxWidth + 1);
                throw new AssertionError(type + " allowed width past the limit");
            } catch (IllegalArgumentException expectedError) { checked++; }
        }
        System.out.println("PASS ShipSectionPlanCheck assertions=" + checked);
    }
}
