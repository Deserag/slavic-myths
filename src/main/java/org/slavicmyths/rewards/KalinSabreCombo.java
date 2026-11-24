package org.slavicmyths.rewards;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slavicmyths.combat.*;
import org.slavicmyths.rpg.PathData;

@EventBusSubscriber(modid="slavicmyths")
public final class KalinSabreCombo {
    private static final String LAST="KalinLastTarget", LAST_TICK="KalinLastTargetTick", TARGET="KalinSummonTarget", LEFT="KalinSummonLeft", NEXT="KalinSummonNext";
    private static boolean held(net.minecraft.world.entity.player.Player p){return p.getMainHandItem().getItem() instanceof Sabre;}
    private static boolean legal(ServerPlayer p,LivingEntity t){return t!=null&&CombatTargeting.canAttack(p,t,CombatTargeting.Context.DIRECT)&&p.hasLineOfSight(t)&&p.distanceToSqr(t)<=256;}
    @SubscribeEvent public static void critical(CriticalHitEvent e){
        if(held(e.getEntity())){e.setCriticalHit(true);e.setDamageMultiplier(1.5F);}
    }
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST) public static void rapid(net.neoforged.neoforge.event.entity.player.AttackEntityEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!held(p)||!(e.getTarget() instanceof LivingEntity t)||!legal(p,t)||p.getAttackStrengthScale(.5F)<.9F)return;
        var n=p.getPersistentData();long elapsed=p.serverLevel().getGameTime()-n.getLong(LAST_TICK);
        // Only shorten this sabre's own repeat-hit window, not the first hit on a new target.
        if(n.hasUUID(LAST)&&n.getUUID(LAST).equals(t.getUUID())&&elapsed>=2&&elapsed<=10&&t.getLastDamageSource()!=null&&t.getLastDamageSource().getEntity()==p)
            t.invulnerableTime=0;
    }
    @SubscribeEvent public static void hit(LivingDamageEvent.Post e){
        if(e.getNewDamage()>0&&e.getSource().getDirectEntity() instanceof ServerPlayer p&&held(p)&&e.getSource().is(DamageTypes.PLAYER_ATTACK)&&legal(p,e.getEntity())){
            p.getPersistentData().putUUID(LAST,e.getEntity().getUUID());
            p.getPersistentData().putLong(LAST_TICK,p.serverLevel().getGameTime());
        }
    }
    public static final class Sabre extends SwordItem implements WeaponInputHandler {
        public Sabre(){super(Tiers.DIAMOND,new Item.Properties().attributes(SwordItem.createAttributes(Tiers.DIAMOND,10,6F)).rarity(Rarity.RARE));}
        @Override public boolean intercepts(Action a){return a==Action.RIGHT_CLICK||a==Action.SHIFT_RIGHT_CLICK;}
        @Override public void onWeaponAction(ServerPlayer p,ItemStack s,Action a){
            if(p.getMainHandItem()!=s||!p.isAlive()||p.isSpectator()||(a==Action.SHIFT_RIGHT_CLICK)!=p.isShiftKeyDown())return;
            String key=a==Action.RIGHT_CLICK?"KalinWaveReady":"KalinSummonReady";
            var state=PathData.data(p);long now=p.serverLevel().getGameTime();
            if(state.getLong(key)>now)return;
            if(a==Action.RIGHT_CLICK){RewardEvents.sabreWave(p);state.putLong(key,now+140);}
            else if(a==Action.SHIFT_RIGHT_CLICK){
                var n=p.getPersistentData();
                if(!n.hasUUID(LAST)||!(p.serverLevel().getEntity(n.getUUID(LAST)) instanceof LivingEntity t)||!legal(p,t))return;
                n.putUUID(TARGET,t.getUUID());n.putInt(LEFT,3);n.putLong(NEXT,now);
                state.putLong(key,now+240);
            }else return;
            CustomData.update(DataComponents.CUSTOM_DATA,s,n->n.putLong(key,state.getLong(key)));
            p.swing(InteractionHand.MAIN_HAND,true);
            if(!p.hasInfiniteMaterials())s.hurtAndBreak(1,p,EquipmentSlot.MAINHAND);
        }
        @Override public void appendHoverText(ItemStack s,Item.TooltipContext c,java.util.List<net.minecraft.network.chat.Component> l,TooltipFlag f){
            l.add(net.minecraft.network.chat.Component.translatableWithFallback("tooltip.slavicmyths.kalin.new_wave","ПКМ — Разящий взмах: 27 урона, 5.5 блока, откат 7 с."));
            l.add(net.minecraft.network.chat.Component.translatableWithFallback("tooltip.slavicmyths.kalin.new_summon","Shift+ПКМ — Призыв трёх сабель: по 18 урона, откат 12 с.; последняя цель в 16 блоках."));
            l.add(net.minecraft.network.chat.Component.translatableWithFallback("tooltip.slavicmyths.kalin.new_crit","Пассивный навык — все удары критические (×1.5). Скорость атаки: 10."));
        }
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        var n=p.getPersistentData();if(!n.hasUUID(TARGET))return;
        long now=p.serverLevel().getGameTime();
        if(!p.isAlive()||p.isSpectator()||!held(p)||!(p.serverLevel().getEntity(n.getUUID(TARGET)) instanceof LivingEntity t)||!legal(p,t)){n.remove(TARGET);n.remove(LEFT);n.remove(NEXT);return;}
        if(now<n.getLong(NEXT))return;
        int left=n.getInt(LEFT);double angle=left*2.094;
        Vec3 start=t.position().add(Math.cos(angle)*3,1.1,Math.sin(angle)*3);
        var blade=new WeaponProjectile(p.serverLevel(),p,p.getMainHandItem().copyWithCount(1),true,true);
        blade.configureSummonedSabre(t.getUUID(),18F);
        blade.setPos(start.x,start.y,start.z);
        Vec3 v=t.position().add(0,t.getBbHeight()*.5,0).subtract(start).normalize();
        blade.shoot(v.x,v.y,v.z,.75F,0);
        p.serverLevel().addFreshEntity(blade);
        p.serverLevel().sendParticles(ParticleTypes.ENCHANT,start.x,start.y,start.z,12,.2,.2,.2,.1);
        if(--left<=0){n.remove(TARGET);n.remove(LEFT);n.remove(NEXT);}else{n.putInt(LEFT,left);n.putLong(NEXT,now+14);}
    }
    private KalinSabreCombo(){}
}
