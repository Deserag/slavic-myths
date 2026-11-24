package org.slavicmyths.winter;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slavicmyths.combat.*;
import org.slavicmyths.rpg.Abilities;
import org.slavicmyths.rpg.PathData;

@EventBusSubscriber(modid="slavicmyths")
public final class WinterPairItem extends MaceItem implements WeaponInputHandler {
    private static final String SPIN="WinterCaptainSpinUntil", NEXT="WinterCaptainSpinNext";
    private final boolean captain;
    public WinterPairItem(boolean captain){
        super(Tiers.DIAMOND,captain?12:14,captain?1.5F:1.05F,0,captain?.15F:.35F,
                new Item.Properties().rarity(captain?Rarity.EPIC:Rarity.RARE));
        this.captain=captain;
    }
    public boolean captain(){return captain;}
    @Override public boolean intercepts(Action action){
        return action==Action.RIGHT_CLICK||action==Action.SHIFT_LEFT_CLICK||captain&&action==Action.SHIFT_RIGHT_CLICK;
    }
    private static long now(ServerPlayer p){return p.serverLevel().getGameTime();}
    private boolean ready(ServerPlayer p,Action action){
        return PathData.data(p).getLong(key(action))<=p.server.overworld().getGameTime();
    }
    private String key(Action a){return "CD_winter_"+(captain?"captain_":"hammers_")+a.id();}
    private void charge(ServerPlayer p,ItemStack stack,Action action,int ticks){
        PathData.data(p).putLong(key(action),p.server.overworld().getGameTime()+ticks);
        if(!p.hasInfiniteMaterials())stack.hurtAndBreak(1,p,EquipmentSlot.MAINHAND);
        p.swing(InteractionHand.MAIN_HAND,true);
    }
    private static boolean legal(ServerPlayer p,LivingEntity t){
        return CombatTargeting.canAttack(p,t,CombatTargeting.Context.DIRECT)&&p.hasLineOfSight(t);
    }
    private static boolean enemy(ServerPlayer p,LivingEntity t){
        return CombatTargeting.isEnemy(p,t,CombatTargeting.Context.ACQUIRE)&&p.hasLineOfSight(t);
    }
    private static void strike(ServerPlayer p,LivingEntity t,float damage){
        if(legal(p,t))t.hurt(MythDamageSources.caused("winter_weapon_skill",p),damage);
    }
    private static void burst(ServerPlayer p,double radius,float damage,boolean pull){
        for(var t:p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(radius),v->enemy(p,v)&&v.distanceToSqr(p)<=radius*radius)){
            strike(p,t,damage);
            if(pull&&CombatTargeting.canControl(p,t)&&!CombatTargeting.controlResistant(t)&&!t.getType().is(CombatTargeting.BOSS)){
                Vec3 delta=p.position().subtract(t.position());
                if(delta.horizontalDistanceSqr()>.5)t.push(delta.normalize().x*.35,.05,delta.normalize().z*.35);
            }
        }
        p.serverLevel().sendParticles(ParticleTypes.SNOWFLAKE,p.getX(),p.getY()+.5,p.getZ(),40,radius*.5,.5,radius*.5,.06);
    }
    public static List<WeaponProjectile> axesInFlight(net.minecraft.world.entity.player.Player p){
        return p.level().getEntitiesOfClass(WeaponProjectile.class,p.getBoundingBox().inflate(80),v->v.isAlive()&&v.getItem().getItem() instanceof WinterPairItem item&&item.captain()&&v.getOwner()==p);
    }
    @Override public void onWeaponAction(ServerPlayer p,ItemStack held,Action action){
        if(p.getMainHandItem()!=held||held.getItem()!=this||!p.isAlive()||p.isSpectator()||!p.getOffhandItem().isEmpty())return;
        if(captain&&action==Action.RIGHT_CLICK){
            var flying=axesInFlight(p);
            if(!flying.isEmpty()){for(var axe:flying)axe.recallPair();return;}
            if(!ready(p,action))return;
            var axe=new WeaponProjectile(p.serverLevel(),p,held.copyWithCount(1),true,true);
            axe.configurePairThrow();
            axe.shootFromRotation(p,p.getXRot(),p.getYRot(),0,1.9F,0);
            if(p.serverLevel().addFreshEntity(axe))charge(p,held,action,80);
            return;
        }
        if(captain&&!axesInFlight(p).isEmpty())return;
        if(!ready(p,action))return;
        if(action==Action.SHIFT_LEFT_CLICK){
            var target=Abilities.target(p,captain?4.5:4);
            if(target==null||!legal(p,target))return;
            charge(p,held,action,captain?240:200);
            strike(p,target,captain?28:24);
            p.swing(InteractionHand.OFF_HAND,true);
        }else if(action==Action.SHIFT_RIGHT_CLICK&&captain){
            charge(p,held,action,500);
            p.getPersistentData().putLong(SPIN,now(p)+120);
            p.getPersistentData().putLong(NEXT,now(p)+20);
        }else if(action==Action.RIGHT_CLICK&&!captain){
            charge(p,held,action,120);
            burst(p,4,16,false);
        }
    }
    @SubscribeEvent public static void spin(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer p))return;
        var n=p.getPersistentData();
        if(!n.contains(SPIN))return;
        long time=now(p),until=n.getLong(SPIN);
        if(!p.isAlive()||p.isSpectator()||time>until||!(p.getMainHandItem().getItem() instanceof WinterPairItem item)||!item.captain()||!axesInFlight(p).isEmpty()){
            n.remove(SPIN);n.remove(NEXT);return;
        }
        if(time>=n.getLong(NEXT)){
            burst(p,5,5,true);
            n.putLong(NEXT,time+20);
        }
        if(time>=until){n.remove(SPIN);n.remove(NEXT);}
    }
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<net.minecraft.network.chat.Component> lines,TooltipFlag flag){
        lines.add(net.minecraft.network.chat.Component.translatable(getDescriptionId()+".skills"));
    }
}
