package org.slavicmyths.entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.nbt.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.Level;
import org.slavicmyths.registry.ModItems;
public final class PolevikEntity extends LandSpiritEntity {
    private final Map<UUID,Integer> gifts=new LinkedHashMap<>();private final Map<UUID,Long> until=new HashMap<>();private long nextGift;
    public enum Behavior { FIELD_WANDER, INSPECT, WARNING, DUST_DASH, GRASS_SNARE, RECOVERY }
    private final AttackTimeline attack=new AttackTimeline();private Behavior behavior=Behavior.FIELD_WANDER;private boolean snare,hit;private net.minecraft.world.phys.Vec3 direction=net.minecraft.world.phys.Vec3.ZERO;
    private final Map<UUID,Integer> disturbances=new LinkedHashMap<>();
    public Behavior behavior(){return behavior;}
    public PolevikEntity(EntityType<? extends PolevikEntity> t,Level w){super(t,w);}
    public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,44).add(Attributes.MOVEMENT_SPEED,.30).add(Attributes.ATTACK_DAMAGE,6).add(Attributes.ARMOR,3).add(Attributes.FOLLOW_RANGE,24);}
    public void disturb(Player player){if(friendly(player))return;if(!disturbances.containsKey(player.getUUID())&&disturbances.size()>=16)disturbances.remove(disturbances.keySet().iterator().next());int count=disturbances.merge(player.getUUID(),1,Integer::sum);voice("angry",.45F);behavior=Behavior.WARNING;if(count>=3)provoke(player);}
    @Override protected boolean meleeReady(){return false;}
    @Override protected AttackTimeline attackTimeline(){return attack;}
    @Override protected void abilityTick(){
        if(getTarget()==null){attack.cancel();return;}
        if(Math.abs(getTarget().getY()-getY())>3){chase(.8);return;}
        if(attack.ready()){
            if(level().getGameTime()>=nextPower&&attackable(getTarget(),7)){snare=true;attack.start(16,1,20,30);nextPower=level().getGameTime()+240;behavior=Behavior.GRASS_SNARE;voice("power",.5F);}
            else if(attackable(getTarget(),8)){snare=false;attack.start(12,10,20,35);behavior=Behavior.DUST_DASH;voice("angry",.5F);hit=false;}
            else{chase(1);return;}
        }
        if(attack.phase()==AttackTimeline.Phase.TELEGRAPH){faceTarget();getNavigation().stop();particles(ParticleTypes.HAPPY_VILLAGER,2);}
        boolean opened=attack.tick();
        if(opened){direction=getTarget().position().subtract(position()).multiply(1,0,1).normalize();if(snare&&attackable(getTarget(),7)){getTarget().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40));particles(ParticleTypes.HAPPY_VILLAGER,12);}}
        if(attack.phase()==AttackTimeline.Phase.ACTIVE&&!snare){if(!horizontalCollision)setDeltaMovement(direction.scale(.6).add(0,getDeltaMovement().y,0));if(!hit&&strikeTarget(6,2.3,.5F))hit=true;}
        if(attack.phase()==AttackTimeline.Phase.RECOVERY){behavior=Behavior.RECOVERY;getNavigation().stop();setDeltaMovement(getDeltaMovement().multiply(.4,1,.4));}
    }
    @Override protected void think(){
        if(getTarget()!=null){state(3);return;}
        Block b=level().getBlockState(blockPosition()).getBlock();state(b instanceof CropBlock || b==Blocks.TALL_GRASS || b==Blocks.SHORT_GRASS?1:0);
        if(level().getGameTime()>nextGift){
            for(ItemEntity drop:level().getEntitiesOfClass(ItemEntity.class,getBoundingBox().inflate(6),i->i.isAlive() && i.getOwner() instanceof Player && (i.getItem().getItem()==Items.BREAD || i.getItem().getItem()==Items.WHEAT || i.getItem().getItem()==ModItems.HONEY_BREAD.get()))){
                Player p=drop.getOwner() instanceof Player player?player:null;if(p==null || p.isSpectator())continue;UUID id=p.getUUID();
                if(distanceToSqr(drop)>2){getNavigation().moveTo(drop,.7);return;}
                drop.getItem().shrink(1);if(drop.getItem().isEmpty())drop.discard();
                if(!gifts.containsKey(id) && gifts.size()>=16){UUID oldest=gifts.keySet().iterator().next();gifts.remove(oldest);until.remove(oldest);}
                int score=(until.getOrDefault(id,0L)<level().getGameTime()?0:gifts.getOrDefault(id,0))+1;
                gifts.put(id,Math.min(5,score));until.put(id,level().getGameTime()+(org.slavicmyths.rpg.PathData.has(p,"offering")?13800:12000));nextGift=level().getGameTime()+600;state(2);particles(ParticleTypes.HAPPY_VILLAGER,8);
                if(score>=3){p.addEffect(new MobEffectInstance(MobEffects.LUCK,1200));if(random.nextInt(12)==0)spawnAtLocation(ModItems.FIELD_BUNDLE.get());}
                return;
            }
        }
        wander();
    }
    @Override public void provoke(Player p){gifts.remove(p.getUUID());until.remove(p.getUUID());super.provoke(p);}
    @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);ListTag list=new ListTag();gifts.forEach((id,score)->{CompoundTag e=new CompoundTag();e.putUUID("Player",id);e.putInt("Gifts",score);e.putLong("Until",until.getOrDefault(id,0L));list.add(e);});n.put("FieldFriends",list);n.putLong("NextGift",nextGift);}
    @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);gifts.clear();until.clear();ListTag list=n.getList("FieldFriends",10);for(int i=0;i<Math.min(16,list.size());i++){CompoundTag e=list.getCompound(i);if(e.hasUUID("Player")){UUID id=e.getUUID("Player");gifts.put(id,Math.min(5,e.getInt("Gifts")));until.put(id,e.getLong("Until"));}}nextGift=n.getLong("NextGift");}
    @Override public int getAmbientSoundInterval(){return 650;}
    @Override protected void playStepSound(net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState block){if(!level().isClientSide)voice("step",.12F);}
}
