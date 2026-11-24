package org.slavicmyths.marine;

import java.util.UUID;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slavicmyths.combat.IndependentWeaponCooldowns;
import org.slavicmyths.nethersmithing.NetherSkills;

@GameTestHolder("slavicmyths_marine_armor")
@PrefixGameTestTemplate(false)
public final class ThrowSkillIsolationGameTests {
    @GameTest(template="empty",timeoutTicks=60)
    public static void regularAndSpecialThrowsDoNotBlockEachOther(GameTestHelper t){
        for(String id:new String[]{"shadow_throwing_knife","storm_harpoon"}){
            for(boolean first:new boolean[]{false,true}){
                var p=FakePlayerFactory.get(t.getLevel(),new GameProfile(UUID.randomUUID(),"ThrowQA"));
                p.moveTo(t.absoluteVec(new Vec3(3,3,3)));
                p.setGameMode(GameType.CREATIVE);
                var item=BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",id));
                ItemStack stack=new ItemStack(item);
                p.setItemInHand(InteractionHand.MAIN_HAND,stack);
                p.getPersistentData().putInt(MarineHarpoons.HITS,4);
                p.getPersistentData().putUUID(MarineHarpoons.LAST,UUID.randomUUID());
                for(boolean alternate:new boolean[]{first,!first}){
                    p.setShiftKeyDown(alternate);
                    stack.use(t.getLevel(),p,InteractionHand.MAIN_HAND);
                    if(id.equals("storm_harpoon")){
                        item.releaseUsing(stack,t.getLevel(),p,72000-12);
                        p.stopUsingItem();
                    }
                    t.assertTrue(!IndependentWeaponCooldowns.ready(p,id,alternate),id+" did not register this throw");
                    if(alternate==first)t.assertTrue(IndependentWeaponCooldowns.ready(p,id,!first),id+" blocked the other throw");
                }
                t.assertTrue(!p.getCooldowns().isOnCooldown(item),id+" uses shared vanilla cooldown");
                if(id.equals("shadow_throwing_knife"))t.assertTrue(!NetherSkills.ready(p,stack,id),"Curse cooldown missing");
                for(var projectile:t.getLevel().getEntitiesOfClass(Projectile.class,p.getBoundingBox().inflate(12),e->e.getOwner()==p))projectile.discard();
                p.discard();
            }
        }
        t.succeed();
    }
}
