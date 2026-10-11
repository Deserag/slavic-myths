package org.slavicmyths.equipment;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Server-owned, transient run charge. Native syncable jump attribute drives client physics.
 * O(1) per wearer tick; no world/entity searches, saved charge or custom movement packets.
 */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class WindBootsMomentum {
    private static final ResourceLocation BOOST=ResourceLocation.fromNamespaceAndPath("slavicmyths","wind_boots_momentum");
    private static final Map<Player,State> STATES=new WeakHashMap<>();
    private static final class State {
        double x,y,z,extraFall,jumpY,peakRise;
        int charge,step;
        boolean jumped;
        State(Player p){x=p.getX();y=p.getY();z=p.getZ();}
    }
    public static int charge(Player p){var s=STATES.get(p);return s==null?0:s.charge;}
    public static void clear(Player p){STATES.remove(p);var a=p.getAttribute(Attributes.JUMP_STRENGTH);if(a!=null)a.removeModifier(BOOST);}
    private static boolean eligible(Player p){return p.isAlive()&&!p.isSpectator()&&!p.getAbilities().flying&&!p.isFallFlying()&&!p.isPassenger()&&!p.isInWaterOrBubble()&&!p.isInLava()&&!p.onClimbable()&&UniqueEquipmentEvents.wears(p,EquipmentSlot.FEET,UniqueEquipment.WIND_BOOTS.get());}
    private static void apply(Player p,State s){
        int step=UniqueEquipmentRules.windStep(s.charge);if(step==s.step)return;s.step=step;
        var a=p.getAttribute(Attributes.JUMP_STRENGTH);if(a==null)return;a.removeModifier(BOOST);
        if(step>0)a.addTransientModifier(new AttributeModifier(BOOST,UniqueEquipmentRules.windImpulse(step)/UniqueEquipmentRules.WIND_BASE_IMPULSE-1,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event){
        var p=event.getEntity();if(p.level().isClientSide)return;
        if(!eligible(p)){if(STATES.containsKey(p))clear(p);return;}
        var s=STATES.computeIfAbsent(p,State::new);
        double dx=p.getX()-s.x,dy=p.getY()-s.y,dz=p.getZ()-s.z,distance=dx*dx+dz*dz;
        s.x=p.getX();s.y=p.getY();s.z=p.getZ();
        // Teleports cannot supply run charge or carry landing credit between positions.
        if(distance>4 || Math.abs(dy)>4){clear(p);return;}
        if(s.jumped)s.peakRise=Math.max(s.peakRise,p.getY()-s.jumpY);
        if(p.onGround()){s.jumped=false;s.extraFall=0;}
        boolean moving=p.isSprinting()&&distance>=.035*.035&&(p.onGround()||s.jumped);
        if(p.horizontalCollision)s.charge=0;
        else s.charge=moving?Math.min(UniqueEquipmentRules.WIND_CHARGE_TICKS,s.charge+1):Math.max(0,s.charge-UniqueEquipmentRules.WIND_DECAY);
        apply(p,s);
    }
    @SubscribeEvent public static void jump(LivingEvent.LivingJumpEvent event){
        if(!(event.getEntity() instanceof Player p)||p.level().isClientSide||!eligible(p))return;
        var s=STATES.computeIfAbsent(p,State::new);s.jumped=true;s.jumpY=p.getY();s.peakRise=0;
        // Credit only the extra height of this jump, not later cliff height or a stored charge.
        s.extraFall=UniqueEquipmentRules.windApex(UniqueEquipmentRules.windImpulse(s.step))-UniqueEquipmentRules.windApex(UniqueEquipmentRules.WIND_BASE_IMPULSE);
    }
    @SubscribeEvent public static void fall(LivingFallEvent event){
        if(!(event.getEntity() instanceof Player p)||p.level().isClientSide)return;
        var s=STATES.get(p);if(s==null)return;
        double credit=Math.min(s.extraFall,Math.max(0,s.peakRise-UniqueEquipmentRules.windApex(UniqueEquipmentRules.WIND_BASE_IMPULSE)));
        if(UniqueEquipmentEvents.wears(p,EquipmentSlot.FEET,UniqueEquipment.WIND_BOOTS.get()))event.setDistance(Math.max(0,event.getDistance()-(float)credit));
        s.extraFall=0;s.jumped=false;
    }
    @SubscribeEvent public static void equipment(LivingEquipmentChangeEvent event){if(event.getEntity() instanceof Player p&&!p.level().isClientSide&&event.getSlot()==EquipmentSlot.FEET)clear(p);}
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event){clear(event.getEntity());}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event){clear(event.getEntity());}
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event){clear(event.getEntity());}
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent event){clear(event.getEntity());}
    @SubscribeEvent public static void death(LivingDeathEvent event){if(event.getEntity() instanceof Player p)clear(p);}
    private WindBootsMomentum(){}
}
