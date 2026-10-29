package org.slavicmyths.hellqa;

import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.*;

/** Owner regression #349: use the real Item.use/release and native ammunition path. */
@GameTestHolder("slavicmyths_hellqa") @PrefixGameTestTemplate(false)
public final class HellBowAmmoQaTests {
    private static void release(GameTestHelper t, ServerPlayer p, ItemStack bow, int ticks) {
        var result = bow.getItem().use(t.getLevel(), p, InteractionHand.MAIN_HAND);
        HellQaTests.check(result.getResult().consumesAction(), "Bow use rejected");
        try { bow.getItem().releaseUsing(bow, t.getLevel(), p, bow.getUseDuration(p) - ticks); }
        finally { p.stopUsingItem(); }
    }

    @GameTest(template="empty") public static void creativeAndSurvivalAmmo(GameTestHelper t) {
        for (String id : new String[]{"short_bow", "heavy_bow", "infernal_bow"}) {
            var p = HellQaTests.player(t);
            var online = HellProjectileQaTests.online(p);
            online.put(p.getUUID(), p);
            try {
                var bow = HellQaTests.item(id);
                HellQaTests.check(bow.getItem() instanceof org.slavicmyths.combat.FieldBowItem, "Wrong bow " + id);
                p.setItemInHand(InteractionHand.MAIN_HAND, bow);
                var area = p.getBoundingBox().inflate(8);
                var noAmmo = bow.getItem().use(t.getLevel(), p, InteractionHand.MAIN_HAND);
                HellQaTests.check(!noAmmo.getResult().consumesAction(), "Survival accepted no ammo " + id);
                bow.getItem().releaseUsing(bow, t.getLevel(), p, bow.getUseDuration(p)-30);
                HellQaTests.check(t.getLevel().getEntitiesOfClass(AbstractArrow.class, area, a->a.getOwner()==p).isEmpty(), "Free survival arrow " + id);
                p.setGameMode(GameType.CREATIVE);
                release(t,p,bow,30);
                var arrows=t.getLevel().getEntitiesOfClass(AbstractArrow.class,area,a->a.getOwner()==p);
                HellQaTests.check(arrows.size()==1, "Creative no-ammo shot " + id + ": " + arrows.size());
                HellQaTests.check(arrows.getFirst().pickup==AbstractArrow.Pickup.CREATIVE_ONLY, "Creative arrow can be farmed " + id);
                arrows.forEach(AbstractArrow::discard);
                HellQaTests.check(bow.getDamageValue()==0, "Creative durability consumed " + id);
                p.setGameMode(GameType.SURVIVAL);
                p.getInventory().setItem(1,new ItemStack(Items.ARROW,2));
                release(t,p,bow,0);
                HellQaTests.check(p.getInventory().countItem(Items.ARROW)==2 && bow.getDamageValue()==0, "Undercharge consumed ammo/wear " + id);
                release(t,p,bow,30);
                arrows=t.getLevel().getEntitiesOfClass(AbstractArrow.class,area,a->a.getOwner()==p);
                HellQaTests.check(arrows.size()==1 && p.getInventory().countItem(Items.ARROW)==1 && bow.getDamageValue()==1, "Survival shot count/ammo/wear " + id);
                arrows.forEach(AbstractArrow::discard);
                System.out.println("HELLQA_PASS #349 "+id+" survival no-ammo/undercharge/one-shot/one-ammo/one-wear; creative no-ammo/no-wear/nonfarmable");
            } finally { online.remove(p.getUUID()); }
        }
        t.succeed();
    }
}
