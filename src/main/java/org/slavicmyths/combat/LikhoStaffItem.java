package org.slavicmyths.combat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.effect.MobEffectInstance;
import org.slavicmyths.registry.ModEffects;
import org.slavicmyths.rpg.*;

public final class LikhoStaffItem extends SwordItem {
    public static final int HEAL_INTERVAL = 180, COOLDOWN = 560, STUN = 25, MOROK = 120;
    public static final double RANGE = 12;
    public LikhoStaffItem() { super(Tiers.DIAMOND, new Properties().rarity(Rarity.RARE).attributes(SwordItem.createAttributes(Tiers.DIAMOND, 6, -2.8F))); }
    @Override public void inventoryTick(ItemStack stack,Level world,Entity entity,int slot,boolean selected) {
        if(entity instanceof ServerPlayer p && (selected || p.getOffhandItem()==stack) && p.getHealth()<p.getMaxHealth()
            && PathData.ready(p,"rare_likho_heal",HEAL_INTERVAL)) p.heal(1);
    }
    @Override public InteractionResultHolder<ItemStack> use(Level world,Player player,InteractionHand hand) {
        ItemStack stack=player.getItemInHand(hand);
        if(world.isClientSide) return InteractionResultHolder.success(stack);
        Vec3 eye=player.getEyePosition(),end=eye.add(player.getLookAngle().scale(RANGE));
        var wall=world.clip(new ClipContext(eye,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));
        if(wall.getType()!=HitResult.Type.MISS)end=wall.getLocation();
        LivingEntity target=null;double best=eye.distanceToSqr(end);
        for(var candidate:world.getEntitiesOfClass(LivingEntity.class,new AABB(eye,end).inflate(.3),e->e!=player&&e.isAlive()&&CombatTargeting.canAttack(player,e))) {
            var hit=candidate.getBoundingBox().inflate(.15).clip(eye,end);
            if(hit.isPresent()&&eye.distanceToSqr(hit.get())<best){best=eye.distanceToSqr(hit.get());target=candidate;}
        }
        if(target==null){player.displayClientMessage(net.minecraft.network.chat.Component.translatable("combat.slavicmyths.no_target"),true);return InteractionResultHolder.fail(stack);}
        if(!CombatTargeting.canControl(player,target)){player.displayClientMessage(net.minecraft.network.chat.Component.translatable("combat.slavicmyths.morok_immune"),true);return InteractionResultHolder.fail(stack);}
        if(PathData.data(player).getLong("CD_rare_likho")>RareCombat.now(player)){player.displayClientMessage(net.minecraft.network.chat.Component.translatable("combat.slavicmyths.cooldown",(PathData.data(player).getLong("CD_rare_likho")-RareCombat.now(player)+19)/20),true);return InteractionResultHolder.fail(stack);}
        PathData.data(player).putLong("CD_rare_likho",RareCombat.now(player)+COOLDOWN);
        var data=target.getPersistentData();data.putUUID("RareMorokOwner",player.getUUID());data.putLong("RareStunUntil",RareCombat.now(player)+(CombatTargeting.controlResistant(target)?0:STUN));
        if(CombatTargeting.controlResistant(target)){target.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS,MOROK,0,false,true,true),player);target.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,MOROK,0,false,true,true),player);}
        else target.addEffect(new MobEffectInstance(ModEffects.DISORIENTATION,STUN+MOROK,0,false,true,true),player);
        if(target instanceof Player)target.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.CONFUSION,145,0,false,true));
        RuneEffects.staff(player,stack,target);player.getCooldowns().addCooldown(this,COOLDOWN);
        RareCombat.feedback(target,net.minecraft.core.particles.ParticleTypes.WITCH);world.playSound(null,target.blockPosition(),net.minecraft.sounds.SoundEvents.EVOKER_CAST_SPELL,net.minecraft.sounds.SoundSource.PLAYERS,.35F,1.4F);player.displayClientMessage(net.minecraft.network.chat.Component.translatable(CombatTargeting.controlResistant(target)?"combat.slavicmyths.morok_resisted":"combat.slavicmyths.morok_hit"),true);
        stack.hurtAndBreak(1,player,hand==InteractionHand.MAIN_HAND?EquipmentSlot.MAINHAND:EquipmentSlot.OFFHAND);
        return InteractionResultHolder.success(stack);
    }
    @Override public void appendHoverText(ItemStack stack,Item.TooltipContext context,java.util.List<net.minecraft.network.chat.Component> lines,TooltipFlag flag){super.appendHoverText(stack,context,lines,flag);lines.add(net.minecraft.network.chat.Component.translatable("combat.slavicmyths.likho_tooltip"));}
}
