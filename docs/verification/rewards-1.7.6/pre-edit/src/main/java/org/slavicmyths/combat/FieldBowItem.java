package org.slavicmyths.combat;

import net.minecraft.world.item.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.stats.Stats;

/** Vanilla ammo/enchantment pipeline; draw speed, velocity and damage define the two roles. */
public final class FieldBowItem extends BowItem {
    public final boolean heavy;
    public FieldBowItem(boolean heavy,Properties properties){super(properties);this.heavy=heavy;}
    public int drawTicks(){return heavy?30:12;}
    public float drawPower(int ticks){float f=Math.max(0,ticks)/(float)drawTicks();return Math.min(1,(f*f+2*f)/3);}
    @Override public void releaseUsing(ItemStack stack,Level level,LivingEntity user,int remaining) {
        if(!(user instanceof Player player))return;
        ItemStack ammo=player.getProjectile(stack);
        if(ammo.isEmpty())return;
        int held=net.neoforged.neoforge.event.EventHooks.onArrowLoose(stack,level,player,getUseDuration(stack,user)-remaining,true);
        if(held<0)return;
        float power=drawPower(held);if(power<.1)return;
        var list=draw(stack,ammo,player);
        if(level instanceof ServerLevel server&&!list.isEmpty())shoot(server,player,player.getUsedItemHand(),stack,list,power*(heavy?3.6F:2.4F),heavy?.7F:1.1F,power==1,null);
        level.playSound(null,player.blockPosition(),SoundEvents.ARROW_SHOOT,SoundSource.PLAYERS,.8F,heavy?.8F:1.3F);
        player.awardStat(Stats.ITEM_USED.get(this));
    }
    @Override protected void shootProjectile(LivingEntity shooter,Projectile projectile,int index,float velocity,float inaccuracy,float angle,LivingEntity target) {
        if(projectile instanceof AbstractArrow arrow)arrow.setBaseDamage(arrow.getBaseDamage()*(heavy?1.15:.8));
        super.shootProjectile(shooter,projectile,index,velocity,inaccuracy,angle,target);
    }
    @Override public int getDefaultProjectileRange(){return heavy?24:10;}
}
