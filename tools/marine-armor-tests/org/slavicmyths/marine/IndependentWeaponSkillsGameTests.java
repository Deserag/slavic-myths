package org.slavicmyths.marine;

import java.util.UUID;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slavicmyths.combat.IndependentWeaponCooldowns;
import org.slavicmyths.combat.WeaponInputHandler;
import org.slavicmyths.rpg.PathData;
import org.slavicmyths.sisters.SistersContent;

@GameTestHolder("slavicmyths_marine_armor")
@PrefixGameTestTemplate(false)
public final class IndependentWeaponSkillsGameTests {
    @GameTest(template="empty",timeoutTicks=60)
    public static void normalThenShift(GameTestHelper t){verifyAll(t,false);}
    @GameTest(template="empty",timeoutTicks=60)
    public static void shiftThenNormal(GameTestHelper t){verifyAll(t,true);}

    @GameTest(template="empty",timeoutTicks=60)
    public static void morovoyHudFollowsPlayerAcrossStacks(GameTestHelper t){
        var player=FakePlayerFactory.get(t.getLevel(),new GameProfile(UUID.randomUUID(),"MorStaffQA"));
        player.moveTo(t.absoluteVec(new Vec3(3,3,3)));
        player.setGameMode(GameType.SURVIVAL);
        var item=org.slavicmyths.morovoy.MorovoyStaff.STAFF.get();
        long now=t.getLevel().getGameTime();
        String[] keys={"MorStaff25_bolt","MorStaff25_flock","MorStaff25_area"};
        for(int i=0;i<keys.length;i++)player.getPersistentData().putLong(keys[i],now+100+i*40);
        for(int slot=0;slot<2;slot++){
            ItemStack stack=new ItemStack(item);
            player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            item.inventoryTick(stack,t.getLevel(),player,0,true);
            var tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
            for(int i=0;i<keys.length;i++)
                t.assertTrue(tag.getLong(keys[i])==now+100+i*40,"Mor staff HUD lost server deadline: "+keys[i]);
            ((WeaponInputHandler)item).onWeaponAction(player,stack,WeaponInputHandler.Action.RIGHT_CLICK);
            tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
            t.assertTrue(tag.getInt("MorovoyStaffMode")==1,"Mode switch blocked by skill cooldown");
            for(int i=0;i<keys.length;i++)
                t.assertTrue(tag.getLong(keys[i])==now+100+i*40&&player.getPersistentData().getLong(keys[i])==now+100+i*40,"Mode switch modified cooldown");
        }
        player.discard();t.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void frostBlockedBoltDoesNotThrottleWave(GameTestHelper t){
        var player=FakePlayerFactory.get(t.getLevel(),new GameProfile(UUID.randomUUID(),"FrostSkillQA"));
        player.moveTo(t.absoluteVec(new Vec3(3,3,3)));
        player.setGameMode(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(org.slavicmyths.morozko.FrostContent.STAFF.get()));
        long now=org.slavicmyths.morozko.FrostSkills.now(player);
        var data=org.slavicmyths.morozko.FrostSkills.data(player);
        data.putLong("BoltReady",now+100);
        player.setShiftKeyDown(false);
        org.slavicmyths.morozko.FrostSkills.action(player,0);
        player.setShiftKeyDown(true);
        org.slavicmyths.morozko.FrostSkills.action(player,2);
        t.assertTrue(data.getLong("WaveReady")>now,"Blocked bolt throttled independent wave");
        t.assertTrue(data.getLong("BoltReady")==now+100,"Wave modified bolt deadline");
        t.assertTrue(data.getLong("IcicleReady")==0&&data.getLong("StreamReady")==0,"Wave started unrelated cooldown");
        player.discard();t.succeed();
    }

    private static void verifyAll(GameTestHelper t,boolean first){
        String[] ids={IndependentWeaponCooldowns.NEVEYA,IndependentWeaponCooldowns.LOMEYA,
            IndependentWeaponCooldowns.SPEAR,IndependentWeaponCooldowns.AXE,
            IndependentWeaponCooldowns.MYTAR,IndependentWeaponCooldowns.POLOZ};
        int[] normal={180,190,185,175,170,360};
        int[] alternate={260,260,125,300,320,240};
        for(int i=0;i<ids.length;i++){
            String id=ids[i];
            var player=FakePlayerFactory.get(t.getLevel(),new GameProfile(UUID.randomUUID(),"SkillQA"));
            player.moveTo(t.absoluteVec(new Vec3(3,3,3)));
            player.setGameMode(GameType.SURVIVAL);
            player.setYRot(0F);player.setXRot(0F);
            var item=BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",id));
            ItemStack stack=new ItemStack(item);
            player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            if(id.equals(IndependentWeaponCooldowns.LOMEYA))
                player.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(SistersContent.LOMEYA_HAMMER_OFFHAND.get()));
            var target=EntityType.COW.create(t.getLevel());
            t.assertTrue(target!=null,id+" missing test target");
            target.setNoAi(true);target.setNoGravity(true);
            target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000D);
            target.setHealth(200F);
            target.moveTo(t.absoluteVec(new Vec3(3,3,5)));
            t.getLevel().addFreshEntity(target);
            player.getPersistentData().putUUID("SlavicNineMorLastVictim",target.getUUID());
            long now=t.getLevel().getGameTime();
            cast(player,stack,id,first);
            String firstKey=IndependentWeaponCooldowns.key(id,first);
            String secondKey=IndependentWeaponCooldowns.key(id,!first);
            long expectedFirst=now+(first?alternate[i]:normal[i]);
            t.assertTrue(PathData.data(player).getLong(firstKey)==expectedFirst,id+" first skill deadline incorrect");
            t.assertTrue(PathData.data(player).getLong(secondKey)==0,id+" other skill entered cooldown");
            t.assertTrue(IndependentWeaponCooldowns.ready(player,id,!first),id+" other action blocked");
            var tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
            t.assertTrue(IndependentWeaponCooldowns.remaining(tag,id,first,now)==expectedFirst-now,id+" HUD first timer mismatch");
            t.assertTrue(IndependentWeaponCooldowns.remaining(tag,id,!first,now)==0,id+" HUD incorrectly dims second skill");
            int damage=stack.getDamageValue();
            cast(player,stack,id,first);
            t.assertTrue(stack.getDamageValue()==damage,id+" repeated blocked skill consumed durability");
            target.invulnerableTime=0;
            cast(player,stack,id,!first);
            t.assertTrue(PathData.data(player).getLong(secondKey)==now+(first?normal[i]:alternate[i]),id+" second skill did not activate");
            t.assertTrue(PathData.data(player).getLong(firstKey)==expectedFirst,id+" second action overwrote first cooldown");
            t.assertTrue(!player.getCooldowns().isOnCooldown(item),id+" vanilla cooldown blocks both inputs");
            target.discard();player.discard();
        }
        t.succeed();
    }
    private static void cast(ServerPlayer player,ItemStack stack,String id,boolean shift){
        player.setShiftKeyDown(shift);
        if(stack.getItem() instanceof WeaponInputHandler handler&&!id.equals(IndependentWeaponCooldowns.NEVEYA)){
            handler.onWeaponAction(player,stack,shift?WeaponInputHandler.Action.SHIFT_RIGHT_CLICK:WeaponInputHandler.Action.RIGHT_CLICK);
        }else{
            stack.use(player.level(),player,InteractionHand.MAIN_HAND);
            if(id.equals(IndependentWeaponCooldowns.NEVEYA)){
                stack.getItem().releaseUsing(stack,player.level(),player,72000-10);
                player.stopUsingItem();
            }
        }
    }
}
