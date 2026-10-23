package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import org.slavicmyths.gorynych.SerpentCrossbow;

/** Player-held 3P animation; vanilla arms keep their native movement and equipment renderer. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class ThirdPersonWeaponMotion {
    private enum Shape { BLADE, HEAVY, SPEAR, POLEAXE, SICKLE, STAFF, KNIFE, SULITSA, CROSSBOW, BOW }
    private static final Map<String,Shape> IDS=new HashMap<>();
    static {
        register(Shape.BLADE,"bone_knife","silver_dagger","ancient_carolingian_sword","restored_carolingian_sword","silver_sword","perunite_sword");
        register(Shape.HEAVY,"club","wooden_mace","stone_mace","mace","silver_mace","perunite_mace","battle_axe","chekan","ancient_chekan");
        register(Shape.SPEAR,"ancient_spear","restored_spear","spear","gold_spear","iron_spear","silver_spear","diamond_spear","perunite_spear","netherite_spear","pool_spear","thunder_spear","victor_spear");
        register(Shape.POLEAXE,"berdysh");
        register(Shape.SICKLE,"noon_sickle");
        register(Shape.STAFF,"carved_staff","veles_staff","storm_staff");
        register(Shape.KNIFE,"stone_throwing_knife","gold_throwing_knife","iron_throwing_knife","silver_throwing_knife","diamond_throwing_knife","perunite_throwing_knife","netherite_throwing_knife");
        register(Shape.SULITSA,"stone_sulitsa","gold_sulitsa","iron_sulitsa","silver_sulitsa","diamond_sulitsa","perunite_sulitsa","netherite_sulitsa");
        register(Shape.CROSSBOW,"serpent_crossbow","three_breath_crossbow");
        register(Shape.BOW,"short_bow","heavy_bow","finist_bow");
    }
    private static void register(Shape shape,String... ids){
        for(String name:ids)if(IDS.put(name,shape)!=null)throw new IllegalStateException("Duplicate 3P weapon ID: "+name);
    }
    private static ResourceLocation id(String name){return ResourceLocation.fromNamespaceAndPath("slavicmyths",name);}
    @SubscribeEvent public static void bake(ModelEvent.ModifyBakingResult event) {
        for(var entry:IDS.entrySet()){
            var item=BuiltInRegistries.ITEM.getOptional(id(entry.getKey()));
            if(item.isEmpty())continue;
            var key=ModelResourceLocation.inventory(id(entry.getKey()));
            BakedModel source=event.getModels().get(key);
            if(source!=null)event.getModels().put(key,new MotionModel(source,null,null,entry.getValue()));
        }
    }
    private static final class MotionModel extends BakedModelWrapper<BakedModel> {
        private final LivingEntity owner;
        private final ItemStack stack;
        private final Shape shape;
        MotionModel(BakedModel source,LivingEntity owner,ItemStack stack,Shape shape){
            super(source);this.owner=owner;this.stack=stack;this.shape=shape;
        }
        @Override public ItemOverrides getOverrides() {
            return new ItemOverrides(){
                @Override public BakedModel resolve(BakedModel base,ItemStack item,ClientLevel level,LivingEntity entity,int seed){
                    BakedModel resolved=originalModel.getOverrides().resolve(originalModel,item,level,entity,seed);
                    return new MotionModel(resolved==null?originalModel:resolved,entity,item,shape);
                }
            };
        }
        @Override public BakedModel applyTransform(ItemDisplayContext context,PoseStack pose,boolean left) {
            BakedModel selected=originalModel.applyTransform(context,pose,left);
            if(context!=ItemDisplayContext.THIRD_PERSON_LEFT_HAND&&context!=ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)return selected;
            if(owner==null||stack==null||owner.isRemoved()||owner.level()==null)return this;
            InteractionHand hand=(owner.getMainArm()==HumanoidArm.LEFT)==left?InteractionHand.MAIN_HAND:InteractionHand.OFF_HAND;
            if(!ItemStack.isSameItemSameComponents(owner.getItemInHand(hand),stack))return this;
            // These player archetypes now move the actual hand and attached item together.
            // Applying the old item-only kick too would slide the grip away from the palm.
            if(owner instanceof net.minecraft.client.player.AbstractClientPlayer
                    &&PlayerWeaponPose.coverage().containsKey(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath()))return this;
            float partial=Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
            float swing=owner.swinging&&owner.swingingArm==hand?owner.getAttackAnim(partial):0;
            float walking=Mth.clamp(owner.walkAnimation.speed(partial),0,1);
            float charge=0;
            if(owner.isUsingItem()&&owner.getUsedItemHand()==hand){
                if(shape==Shape.SULITSA)charge=Mth.clamp((owner.getTicksUsingItem()+partial)/20F,0,1);
                if(shape==Shape.CROSSBOW&&stack.getItem() instanceof SerpentCrossbow)
                    charge=Mth.clamp((owner.getTicksUsingItem()+partial)/(CrossbowItem.getChargeDuration(stack,owner)+10F),0,1);
                if(shape==Shape.BOW){
                    int draw=stack.getItem() instanceof org.slavicmyths.combat.FieldBowItem field?field.drawTicks():30;
                    charge=Mth.clamp((owner.getTicksUsingItem()+partial)/draw,0,1);
                }
            }
            animate(pose,shape,left,swing,walking,charge,owner.tickCount+partial);
            return this;
        }
    }
    private static float ease(float value) {
        value=Mth.clamp(value,0,1);
        return value*value*(3-2*value);
    }
    private static void animate(PoseStack pose,Shape shape,boolean left,float swing,float walking,float charge,float age){
        int sign=left?-1:1;
        float attack=Mth.clamp(swing,0,1);
        float prep=ease(attack/.3F),contact=ease((attack-.3F)/.28F);
        float recover=1-ease((attack-.58F)/.42F);
        float windup=(prep-contact)*recover,hit=contact*recover;
        float sway=Mth.sin(age*.65F)*walking;
        switch(shape){
            case BLADE -> {
                pose.mulPose(Axis.YP.rotationDegrees(sign*(-19*windup+49*hit+2*sway)));
                pose.mulPose(Axis.XP.rotationDegrees(-18*windup+17*hit));
            }
            case HEAVY -> {
                pose.mulPose(Axis.XP.rotationDegrees(-33*windup+52*hit+2*sway));
                pose.mulPose(Axis.ZP.rotationDegrees(sign*(15*windup-23*hit)));
            }
            case SPEAR -> {
                pose.translate(0,-.02*hit,-.20*hit+.05*windup);
                pose.mulPose(Axis.XP.rotationDegrees(-17*windup+10*hit));
                pose.mulPose(Axis.YP.rotationDegrees(sign*2*sway));
            }
            case POLEAXE -> {
                pose.mulPose(Axis.YP.rotationDegrees(sign*(-22*windup+45*hit)));
                pose.mulPose(Axis.XP.rotationDegrees(-14*windup+16*hit));
            }
            case SICKLE -> {
                pose.mulPose(Axis.ZP.rotationDegrees(sign*(24*windup-41*hit)));
            }
            case STAFF -> {
                pose.translate(0,-.03*hit,-.08*hit);
                pose.mulPose(Axis.XP.rotationDegrees(-13*windup+17*hit+2*sway));
            }
            case KNIFE -> {
                pose.translate(0,0,-.13*hit);
                pose.mulPose(Axis.XP.rotationDegrees(-31*hit));
            }
            case SULITSA -> {
                pose.translate(0,-.04*charge,.11*charge-.2*hit);
                pose.mulPose(Axis.XP.rotationDegrees(-51*ease(charge)+34*hit));
            }
            case CROSSBOW -> {
                float pull=ease(charge);
                pose.translate(0,-.03*pull,.04*pull-.07*hit);
                pose.mulPose(Axis.XP.rotationDegrees(-12*pull+9*hit));
                pose.mulPose(Axis.ZP.rotationDegrees(sign*5*pull));
            }
            case BOW -> {
                float draw=ease(charge);
                pose.translate(sign*.025*draw,.02*draw,-.04*draw);
                pose.mulPose(Axis.YP.rotationDegrees(sign*-10*draw));
                pose.mulPose(Axis.XP.rotationDegrees(-9*draw));
            }
        }
    }
    private ThirdPersonWeaponMotion(){}
}
