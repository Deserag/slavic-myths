package org.slavicmyths.verify;

import java.util.*;
import net.minecraft.world.item.*;
import org.slavicmyths.item.*;
import org.slavicmyths.accessories.AccessoryRules;
import top.theillusivec4.curios.api.SlotContext;

/** Existing production rules only; registry-dependent item checks are GameTests. */
public final class Accessories278Offline {
    private static int checks;
    private static void check(boolean value, String reason) {
        checks++;
        if (!value) throw new AssertionError(reason);
    }
    public static void main(String[] args) throws Exception {
        net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
        // Regression calls use the existing production rules, including thresholds and cooldowns.
        check(AccessoryRules.fall(10, 12, true) == 3, "Podvey threshold");
        check(AccessoryRules.fall(10, 11.99F, true) == 10, "Podvey below threshold");
        check(AccessoryRules.fall(10, 20, false) == 10, "Podvey cooldown");
        check(!AccessoryRules.harvest(true,true,true,true,0), "no farmer rune doubling");
        check(!AccessoryRules.spark(true,true,true,0), "fire immune target");
        check(!AccessoryRules.spark(true,false,false,0), "friendly target");
        check(!AccessoryRules.ready(999,1000) && AccessoryRules.ready(1000,1000), "cooldown expiry");
        check(AccessoryRules.bathDuration(100,true,false)==80,"bath duration");
        check(AccessoryRules.bathDuration(100,true,true)==100,"protected effect preserved");
        check(AccessoryRules.bathDuration(-1,true,false)==-1,"infinite effect preserved");
        System.out.println("ACCESSORIES278_NATIVE PASS checks=" + checks + "; world/player/game launches=0; damage/equip sync/relog/MP NOT_RUN");
    }
}
