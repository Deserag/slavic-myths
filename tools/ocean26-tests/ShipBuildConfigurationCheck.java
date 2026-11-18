import java.util.List;
import net.minecraft.nbt.CompoundTag;
import org.slavicmyths.ships.ShipBuildConfiguration;
import org.slavicmyths.ships.ShipSectionPlan;

/** Loaded in Ocean26 GameTest classpath, not a release mod class. */
public final class ShipBuildConfigurationCheck {
    public static void verify() {
        for (ShipSectionPlan.ShipClass type : ShipSectionPlan.ShipClass.values()) {
            var original = ShipBuildConfiguration.initial(type);
            assertRoundTrip(original);
            var enlarged = original.resize(type.maxLength, type.maxWidth);
            assertRoundTrip(enlarged);
            if (type != ShipSectionPlan.ShipClass.SMALL)
                assertRoundTrip(enlarged.withSail(1, Math.min(2, type.maxSailTier)));
        }
    }
    private static void assertRoundTrip(ShipBuildConfiguration config) {
        CompoundTag saved = config.save();
        if (!ShipBuildConfiguration.load(saved).equals(config))
            throw new AssertionError("Blueprint NBT mismatch");
        if (!ShipBuildConfiguration.fromWire(config.wire()).equals(config))
            throw new AssertionError("Blueprint wire mismatch");
    }
    public static void main(String[] args) { verify(); System.out.println("PASS ShipBuildConfigurationCheck"); }
}
