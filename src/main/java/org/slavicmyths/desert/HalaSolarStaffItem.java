package org.slavicmyths.desert;

import java.util.List;
import java.util.LinkedHashSet;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import org.slavicmyths.combat.CombatTargeting;
import org.slavicmyths.combat.IndependentWeaponCooldowns;
import org.slavicmyths.combat.MaceItem;
import org.slavicmyths.combat.WeaponInputHandler;

/** Server combat implementation; the crystal-space beam renderer is a separate client task. */
public final class HalaSolarStaffItem extends TieredItem implements WeaponInputHandler {
    public static final DesertWeaponSpec SPEC=DesertWeaponSpec.HALA_STAFF;
    public HalaSolarStaffItem(){
        super(Tiers.DIAMOND,new Item.Properties().rarity(Rarity.RARE)
            .attributes(MaceItem.attributes(SPEC.baseDamage,1.2F)));
    }
    @Override public Component getName(ItemStack stack){
        return Component.translatableWithFallback(getDescriptionId(),"Солнечный посох Халы");
    }
    @Override public boolean intercepts(Action a){return a==Action.RIGHT_CLICK||a==Action.SHIFT_RIGHT_CLICK;}
    @Override public boolean hurtEnemy(ItemStack stack,LivingEntity target,LivingEntity attacker){
        if(!attacker.level().isClientSide)stack.hurtAndBreak(1,attacker,EquipmentSlot.MAINHAND);
        return true;
    }
    @Override public void onWeaponAction(ServerPlayer p,ItemStack held,Action a){
        if(!intercepts(a)||!p.isAlive()||p.isSpectator()||p.getMainHandItem()!=held||held.getItem()!=this)return;
        boolean burst=a==Action.SHIFT_RIGHT_CLICK;
        if(p.isShiftKeyDown()!=burst||!IndependentWeaponCooldowns.ready(p,SPEC.id,burst))return;
        var ray=DesertWeaponTargeting.trace(p,24);
        IndependentWeaponCooldowns.start(p,held,SPEC.id,burst,SPEC.cooldownTicks(burst,false));
        if(burst){
            Vec3 center=ray.target()==null?ray.end().subtract(p.getLookAngle().scale(.25)):ray.target().getBoundingBox().getCenter();
            var targets=new LinkedHashSet<>(DesertWeaponTargeting.area(p,center,4));
            if(DesertWeaponTargeting.legal(p,ray.target()))targets.add(ray.target());
            for(var target:targets){
                if(target.hurt(p.damageSources().playerAttack(p),SPEC.shiftRightDamage)
                    &&CombatTargeting.canControl(p,target)&&!CombatTargeting.controlResistant(target))
                    target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,80));
            }
            p.serverLevel().sendParticles(ParticleTypes.FLASH,center.x,center.y,center.z,1,0,0,0,0);
            p.serverLevel().sendParticles(ParticleTypes.FLAME,center.x,center.y,center.z,64,1.8,1,1.8,.06);
        }else{
            if(ray.target()!=null)ray.target().hurt(p.damageSources().playerAttack(p),SPEC.rightDamage);
            Vec3 end=ray.end();
            // Impact only until a real client crystal anchor is available; no fake beam from hand or eyes.
            p.serverLevel().sendParticles(ParticleTypes.FLAME,end.x,end.y,end.z,18,.2,.2,.2,.04);
        }
        p.swing(InteractionHand.MAIN_HAND,true);
        if(!p.hasInfiniteMaterials())held.hurtAndBreak(1,p,EquipmentSlot.MAINHAND);
    }
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag){
        lines.add(Component.translatableWithFallback(getDescriptionId()+".beam","ПКМ — солнечный луч: 36 HP, 8 сек.").withStyle(ChatFormatting.GOLD));
        lines.add(Component.translatableWithFallback(getDescriptionId()+".burst","Shift + ПКМ — солнечный взрыв: 46 HP, 15 сек.").withStyle(ChatFormatting.GOLD));
    }
}
