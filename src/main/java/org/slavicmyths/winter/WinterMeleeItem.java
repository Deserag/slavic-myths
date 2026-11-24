package org.slavicmyths.winter;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slavicmyths.combat.*;
import org.slavicmyths.morozko.*;
import org.slavicmyths.rpg.PathData;

@EventBusSubscriber(modid="slavicmyths")
public final class WinterMeleeItem extends TieredItem implements WeaponInputHandler {
    public enum Kind {
        BLADES(9,2.0F,0), CLAWS(8,2.5F,0),
        SWORD(12,1.6F,0), AXE(16,1.0F,0), MACE(18,.85F,0), HALBERD(14,1.15F,1.25F), SCYTHE(13,1.25F,.75F);
        final float damage,speed,reach;
        Kind(float damage,float speed,float reach){this.damage=damage;this.speed=speed;this.reach=reach;}
    }
    private static final String TARGET="WinterClawFlurryTarget", LEFT="WinterClawFlurryLeft", NEXT="WinterClawFlurryNext";
    public final Kind kind;
    public WinterMeleeItem(Kind kind){
        super(Tiers.DIAMOND,new Item.Properties().rarity(kind==Kind.BLADES||kind==Kind.CLAWS?Rarity.RARE:Rarity.UNCOMMON).attributes(attributes(kind)));
        this.kind=kind;
    }
    private static ItemAttributeModifiers attributes(Kind k){
        var result=org.slavicmyths.combat.MaceItem.attributes(k.damage,k.speed);
        return k.reach==0?result:result.withModifierAdded(Attributes.ENTITY_INTERACTION_RANGE,
                new AttributeModifier(ResourceLocation.fromNamespaceAndPath("slavicmyths","winter_weapon_reach"),k.reach,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND);
    }
    @Override public boolean hurtEnemy(ItemStack stack,LivingEntity target,LivingEntity attacker){
        if(!attacker.level().isClientSide)stack.hurtAndBreak(1,attacker,EquipmentSlot.MAINHAND);
        return true;
    }
    @Override public boolean supportsEnchantment(ItemStack stack,Holder<Enchantment> enchantment){
        return !enchantment.is(Enchantments.SWEEPING_EDGE)&&(enchantment.value().isSupportedItem(new ItemStack(Items.IRON_SWORD))||super.supportsEnchantment(stack,enchantment));
    }
    @Override public boolean isPrimaryItemFor(ItemStack stack,Holder<Enchantment> enchantment){
        return !enchantment.is(Enchantments.SWEEPING_EDGE)&&(enchantment.value().isPrimaryItem(new ItemStack(Items.IRON_SWORD))||super.isPrimaryItemFor(stack,enchantment));
    }
    @Override public boolean intercepts(Action a){return a==Action.RIGHT_CLICK||kind==Kind.HALBERD&&a==Action.SHIFT_RIGHT_CLICK||kind==Kind.BLADES&&a==Action.SHIFT_RIGHT_CLICK||kind==Kind.CLAWS&&a==Action.SHIFT_LEFT_CLICK;}
    private static long now(ServerPlayer p){return p.server.overworld().getGameTime();}
    private String key(Action a){return "CD_winter_"+kind.name()+"_"+a.id();}
    private boolean ready(ServerPlayer p,Action a){return PathData.data(p).getLong(key(a))<=now(p);}
    private void cast(ServerPlayer p,ItemStack held,Action a,int cd){
        PathData.data(p).putLong(key(a),now(p)+cd);
        if(!p.hasInfiniteMaterials())held.hurtAndBreak(1,p,EquipmentSlot.MAINHAND);
        p.swing(InteractionHand.MAIN_HAND,true);
    }
    private static boolean legal(ServerPlayer p,LivingEntity t){
        return t!=null&&CombatTargeting.canAttack(p,t,CombatTargeting.Context.DIRECT)&&p.hasLineOfSight(t);
    }
    private static LivingEntity aim(ServerPlayer p,double range){
        Vec3 from=p.getEyePosition(),to=from.add(p.getLookAngle().scale(range));
        return p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().expandTowards(p.getLookAngle().scale(range)).inflate(1),
                t->legal(p,t)&&t.getBoundingBox().inflate(.2).clip(from,to).isPresent()).stream()
                .min(java.util.Comparator.comparingDouble(t->FrostWorld.bboxDistance(from,t.getBoundingBox()))).orElse(null);
    }
    private static void hit(ServerPlayer p,LivingEntity target,float damage,float push){
        if(!legal(p,target))return;
        if(target.hurt(MythDamageSources.caused("winter_weapon_skill",p),damage)&&push>0&&CombatTargeting.canControl(p,target)&&!CombatTargeting.controlResistant(target)){
            var delta=target.position().subtract(p.position());target.knockback(push,-delta.x,-delta.z);
        }
    }
    private static void area(ServerPlayer p,double radius,double angle,float damage,float push){
        for(var target:p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(radius),
                t->legal(p,t)&&t.distanceToSqr(p)<=radius*radius&&(angle>=180||FrostWorld.cone(p,t,radius,angle))
                &&(CombatTargeting.isEnemy(p,t,CombatTargeting.Context.ACQUIRE)||t==p.getLastHurtMob())))hit(p,target,damage,push);
        if(angle>=180)FrostVisuals.ring(p.serverLevel(),p.position(),radius,.15,28);else FrostVisuals.cone(p.serverLevel(),p,radius);
    }
    private static boolean moveTo(ServerPlayer p,Vec3 destination){
        var safe=FrostWorld.safe(p.serverLevel(),destination,.6F,1.8F,0);
        if(safe==null||p.serverLevel().clip(new net.minecraft.world.level.ClipContext(p.getEyePosition(),safe.add(0,1.62,0),
                net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,p)).getType()!=HitResult.Type.MISS)return false;
        p.teleportTo(safe.x,safe.y,safe.z);return true;
    }
    @Override public void onWeaponAction(ServerPlayer p,ItemStack held,Action a){
        if(p.getMainHandItem()!=held||held.getItem()!=this||!p.isAlive()||p.isSpectator()||
                (a==Action.SHIFT_LEFT_CLICK||a==Action.SHIFT_RIGHT_CLICK)!=p.isShiftKeyDown())return;
        if((kind==Kind.BLADES||kind==Kind.CLAWS)&&!p.getOffhandItem().isEmpty())return;
        if(kind==Kind.BLADES&&a==Action.RIGHT_CLICK){
            var flying=WeaponProjectile.pairedInFlight(p);
            if(!flying.isEmpty()){for(var blade:flying)blade.recallPair();return;}
            if(!ready(p,a))return;
            var blade=new WeaponProjectile(p.serverLevel(),p,held.copyWithCount(1),true,true);
            blade.configurePairThrow(14);blade.shootFromRotation(p,p.getXRot(),p.getYRot(),0,2.2F,0);
            if(p.serverLevel().addFreshEntity(blade))cast(p,held,a,100);return;
        }
        if(!ready(p,a))return;
        if(kind==Kind.BLADES&&a==Action.SHIFT_RIGHT_CLICK){
            if(!WeaponProjectile.pairedInFlight(p).isEmpty())return;
            var target=aim(p,8);if(target==null)return;
            var behind=target.position().subtract(target.getLookAngle().multiply(1,0,1).normalize().scale(1.5));
            if(!moveTo(p,behind)||!legal(p,target)||target.distanceToSqr(p)>16)return;
            cast(p,held,a,240);hit(p,target,24,0);return;
        }
        if(kind==Kind.CLAWS&&a==Action.SHIFT_LEFT_CLICK){
            var target=aim(p,4);if(target==null)return;
            var n=p.getPersistentData();n.putUUID(TARGET,target.getUUID());n.putInt(LEFT,3);n.putLong(NEXT,now(p));
            cast(p,held,a,200);return;
        }
        if(kind==Kind.HALBERD&&a==Action.SHIFT_RIGHT_CLICK){
            FrostShard.launch(p,p.getEyePosition().add(p.getLookAngle().scale(24)),20,0,null);
            cast(p,held,a,140);return;
        }
        if(a!=Action.RIGHT_CLICK)return;
        switch(kind){
            case CLAWS -> {
                var t=aim(p,5.5);if(t==null)return;
                if(t.distanceToSqr(p)>9){var d=t.position().subtract(p.position()).multiply(1,0,1);if(!moveTo(p,t.position().subtract(d.normalize().scale(1.5))))return;}
                cast(p,held,a,120);hit(p,t,15,.25F);
            }
            case SWORD -> {cast(p,held,a,100);area(p,6,35,14,.25F);}
            case AXE -> {var t=aim(p,4.5);if(t==null)return;cast(p,held,a,140);hit(p,t,22,.5F);}
            case MACE -> {if(!p.onGround())return;cast(p,held,a,160);area(p,4,180,16,.65F);}
            case HALBERD -> {cast(p,held,a,140);area(p,4.5,180,18,.3F);}
            case SCYTHE -> {cast(p,held,a,120);area(p,4.5,80,17,.2F);}
            default -> {}
        }
    }
    private static void clearFlurry(ServerPlayer p){var n=p.getPersistentData();n.remove(TARGET);n.remove(LEFT);n.remove(NEXT);}
    @SubscribeEvent public static void flurry(PlayerTickEvent.Post e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        var n=p.getPersistentData();if(!n.hasUUID(TARGET))return;
        if(!p.isAlive()||p.isSpectator()||!(p.getMainHandItem().getItem() instanceof WinterMeleeItem item)||item.kind!=Kind.CLAWS){clearFlurry(p);return;}
        if(!(p.serverLevel().getEntity(n.getUUID(TARGET)) instanceof LivingEntity target)||!legal(p,target)||p.distanceToSqr(target)>20.25){clearFlurry(p);return;}
        if(now(p)<n.getLong(NEXT))return;
        hit(p,target,7,0);p.swing(n.getInt(LEFT)%2==0?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND,true);
        int left=n.getInt(LEFT)-1;if(left<=0)clearFlurry(p);else{n.putInt(LEFT,left);n.putLong(NEXT,now(p)+12);}
    }
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<net.minecraft.network.chat.Component> lines,TooltipFlag flag){
        lines.add(net.minecraft.network.chat.Component.translatable(getDescriptionId()+".skills"));
        if(kind==Kind.HALBERD)lines.add(net.minecraft.network.chat.Component.literal(java.util.Locale.getDefault().getLanguage().equals("ru")?"Shift+ПКМ — ледяной снаряд: 20 урона, откат 7 с.":"Shift+RMB — frost projectile: 20 damage, 7s cooldown."));
    }
}
