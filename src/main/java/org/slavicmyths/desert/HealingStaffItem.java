package org.slavicmyths.desert;

import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slavicmyths.combat.CombatTargeting;
import org.slavicmyths.combat.IndependentWeaponCooldowns;
import org.slavicmyths.combat.WeaponInputHandler;
import org.slavicmyths.rpg.PathData;

@EventBusSubscriber(modid="slavicmyths")
public final class HealingStaffItem extends Item implements WeaponInputHandler {
    public static final int SELF_CD=400, AREA_CD=800, AREA_TICKS=120, IMMUNITY_TICKS=40;
    public static final double RADIUS=5;
    public static final String IMMUNE="HealingStaffImmuneUntil", REGEN="HealingStaffRegenUntil",
        REGEN_NEXT="HealingStaffRegenNext", AREA="HealingStaffArea";
    private static final String SHARED=IndependentWeaponCooldowns.HEALING_STAFF;
    private final boolean fire;

    public HealingStaffItem(boolean fire) {
        super(new Item.Properties().durability(512).rarity(Rarity.RARE));
        this.fire=fire;
    }

    @Override public boolean intercepts(Action action) {
        return action==Action.RIGHT_CLICK||action==Action.SHIFT_RIGHT_CLICK;
    }

    @Override public void appendHoverText(ItemStack stack, TooltipContext context,List<Component> lines,TooltipFlag flag) {
        lines.add(Component.translatable(getDescriptionId()+".self").withStyle(ChatFormatting.AQUA));
        lines.add(Component.translatable(getDescriptionId()+".area").withStyle(ChatFormatting.AQUA));
        lines.add(Component.translatable("item.slavicmyths.healing_staff.shared").withStyle(ChatFormatting.GRAY));
    }

    private static long now(ServerPlayer p){return p.serverLevel().getGameTime();}
    private static CompoundTag state(ServerPlayer p){return PathData.data(p);}
    private static boolean activeArea(ServerPlayer p,long now){
        if(!p.isAlive()||p.isSpectator())return false;
        CompoundTag area=state(p).getCompound(AREA);
        return area.getLong("Until")>now&&area.getString("Dimension").equals(p.level().dimension().location().toString());
    }
    private static Vec3 center(CompoundTag area){return new Vec3(area.getDouble("X"),area.getDouble("Y"),area.getDouble("Z"));}
    private static boolean eligible(ServerPlayer p,LivingEntity t){
        return t.isAlive()&&!t.isRemoved()&&!t.isSpectator()&&t.level()==p.level()
            &&(p==t||!t.getType().is(CombatTargeting.BOSS)
                &&CombatTargeting.relation(p,t,CombatTargeting.Context.AREA)==CombatTargeting.Relation.ALLY);
    }
    private static boolean inArea(ServerPlayer p,LivingEntity t,CompoundTag area){
        return eligible(p,t)&&t.position().distanceToSqr(center(area))<=RADIUS*RADIUS;
    }

    @Override public void onWeaponAction(ServerPlayer p,ItemStack held,Action action){
        if(!p.isAlive()||p.isSpectator()||p.getMainHandItem()!=held||held.getItem()!=this||!intercepts(action))return;
        boolean alternate=action==Action.SHIFT_RIGHT_CLICK;
        if(p.isShiftKeyDown()!=alternate)return;
        if(!IndependentWeaponCooldowns.ready(p,SHARED,alternate)){
            long remaining=state(p).getLong(IndependentWeaponCooldowns.key(SHARED,alternate))-now(p);
            p.displayClientMessage(Component.translatable("item.slavicmyths.healing_staff.cooldown",Math.max(1,(remaining+19)/20)),true);
            return;
        }
        long now=now(p);CompoundTag data=state(p);
        IndependentWeaponCooldowns.start(p,held,SHARED,alternate,alternate?AREA_CD:SELF_CD);
        if(alternate){
            CompoundTag area=new CompoundTag();
            area.putLong("Until",now+AREA_TICKS);area.putBoolean("Fire",fire);
            area.putString("Dimension",p.level().dimension().location().toString());
            area.putDouble("X",p.getX());area.putDouble("Y",p.getY());area.putDouble("Z",p.getZ());
            data.put(AREA,area);
            particles(p.serverLevel(),center(area),fire);
        }else{
            p.heal(fire?6F:8F);
            if(fire){data.putLong(REGEN,now+120);data.putLong(REGEN_NEXT,now+20);}
            else data.putLong(IMMUNE,now+IMMUNITY_TICKS);
            p.serverLevel().sendParticles(ParticleTypes.HEART,p.getX(),p.getY()+1,p.getZ(),6,.35,.5,.35,.01);
        }
        if(!p.hasInfiniteMaterials())held.hurtAndBreak(1,p,EquipmentSlot.MAINHAND);
    }

    private static void particles(ServerLevel w,Vec3 c,boolean fire){
        for(int i=0;i<20;i++){
            double a=i*Math.PI/10;
            w.sendParticles(fire?ParticleTypes.FLAME:ParticleTypes.SNOWFLAKE,
                c.x+Math.cos(a)*RADIUS,c.y+.15,c.z+Math.sin(a)*RADIUS,1,0,.04,0,0);
        }
    }

    @SubscribeEvent public static void personalTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer p))return;
        CompoundTag data=state(p);long now=now(p);
        if(!p.isAlive()||p.isSpectator()){clearTemporary(data);return;}
        if(data.contains(IMMUNE)&&data.getLong(IMMUNE)<=now)data.remove(IMMUNE);
        if(data.contains(REGEN)){
            long until=data.getLong(REGEN),next=data.getLong(REGEN_NEXT);
            if(now<=until&&next>0&&now>=next){p.heal(1F);data.putLong(REGEN_NEXT,now+20);}
            if(now>=until){data.remove(REGEN);data.remove(REGEN_NEXT);}
        }
        if(data.contains(AREA)){
            if(!activeArea(p,now))data.remove(AREA);
            else if(now%10==0){
                CompoundTag area=data.getCompound(AREA);Vec3 c=center(area);
                if(p.serverLevel().hasChunkAt(net.minecraft.core.BlockPos.containing(c)))
                    particles(p.serverLevel(),c,area.getBoolean("Fire"));
            }
        }
    }

    // At most one pulse per target per second, regardless of the number of overlapping fields.
    @SubscribeEvent public static void areas(ServerTickEvent.Post event){
        if(event.getServer().overworld().getGameTime()%20!=0)return;
        Map<LivingEntity,Float> strongest=new IdentityHashMap<>();
        for(ServerPlayer p:event.getServer().getPlayerList().getPlayers()){
            if(!activeArea(p,now(p)))continue;
            CompoundTag area=state(p).getCompound(AREA);Vec3 c=center(area);
            ServerLevel w=p.serverLevel();
            if(!w.hasChunkAt(net.minecraft.core.BlockPos.containing(c)))continue;
            float heal=area.getBoolean("Fire")?1F:4F/6F;
            for(LivingEntity t:w.getEntitiesOfClass(LivingEntity.class,new AABB(c,c).inflate(RADIUS),e->inArea(p,e,area)))
                strongest.merge(t,heal,Math::max);
        }
        strongest.forEach((target,heal)->{if(target.isAlive())target.heal(heal);});
    }

    @SubscribeEvent public static void immunity(LivingIncomingDamageEvent event){
        if(event.getEntity() instanceof ServerPlayer p&&!event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                &&state(p).getLong(IMMUNE)>now(p))event.setCanceled(true);
    }
    @SubscribeEvent public static void shield(LivingDamageEvent.Pre event){
        LivingEntity target=event.getEntity();
        if(!(target.level() instanceof ServerLevel w)||event.getNewDamage()<=0||event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY))return;
        for(ServerPlayer p:w.players()){
            if(!activeArea(p,now(p)))continue;
            CompoundTag area=state(p).getCompound(AREA);
            if(!area.getBoolean("Fire")&&inArea(p,target,area)){
                event.setNewDamage(event.getNewDamage()*.9F);
                break;
            }
        }
    }
    @SubscribeEvent public static void death(LivingDeathEvent event){
        if(event.getEntity() instanceof ServerPlayer p)clearTemporary(state(p));
    }
    private static void clearTemporary(CompoundTag data){
        data.remove(IMMUNE);data.remove(REGEN);data.remove(REGEN_NEXT);data.remove(AREA);
    }
}
