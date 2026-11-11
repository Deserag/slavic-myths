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
            var savedV1 = new ShipBuildConfiguration(type, 0, 0,
                    List.of(1), 1, false, false, 1);
            assertRoundTrip(savedV1);
            if (!savedV1.layout().equals(
                    org.slavicmyths.ships.ShipHullVoxelLayout.generateLegacy(savedV1.structure())))
                throw new AssertionError("Persisted V1 palette changed");
            if (original.layoutVersion() != 2 || savedV1.layoutVersion() != 1)
                throw new AssertionError("Ship layout version was not retained");
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
