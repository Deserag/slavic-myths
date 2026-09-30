package org.slavicmyths.entity;
import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.*;
import net.minecraft.block.*;
import net.minecraft.world.World;
import org.slavicmyths.registry.ModItems;
public final class PolevikEntity extends LandSpiritEntity {
    private final Map<UUID,Integer> gifts=new LinkedHashMap<>();private final Map<UUID,Long> until=new HashMap<>();private long nextGift;
    public PolevikEntity(EntityType<? extends PolevikEntity> t,World w){super(t,w);}
    public static AttributeModifierMap.MutableAttribute attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,36).add(Attributes.MOVEMENT_SPEED,.28).add(Attributes.ATTACK_DAMAGE,6).add(Attributes.FOLLOW_RANGE,18);}
    @Override protected void think(){
        if(getTarget()!=null){state(3);if(level.getGameTime()>nextPower && distanceToSqr(getTarget())<36 && canSee(getTarget())){LivingEntity t=getTarget();t.knockback(.9F,getX()-t.getX(),getZ()-t.getZ());particles(ParticleTypes.HAPPY_VILLAGER,15);voice("power",.5F);nextPower=level.getGameTime()+240;}return;}
        Block b=level.getBlockState(blockPosition()).getBlock();state(b instanceof CropsBlock || b==Blocks.TALL_GRASS || b==Blocks.GRASS?1:0);
        if(level.getGameTime()>nextGift){
            for(ItemEntity drop:level.getEntitiesOfClass(ItemEntity.class,getBoundingBox().inflate(6),i->i.isAlive() && i.getThrower()!=null && (i.getItem().getItem()==Items.BREAD || i.getItem().getItem()==Items.WHEAT || i.getItem().getItem()==ModItems.HONEY_BREAD.get()))){
                UUID id=drop.getThrower();PlayerEntity p=level.getPlayerByUUID(id);if(p==null || p.isSpectator())continue;
                if(distanceToSqr(drop)>2){getNavigation().moveTo(drop,.7);return;}
                drop.getItem().shrink(1);if(drop.getItem().isEmpty())drop.remove();
                if(!gifts.containsKey(id) && gifts.size()>=16){UUID oldest=gifts.keySet().iterator().next();gifts.remove(oldest);until.remove(oldest);}
                int score=(until.getOrDefault(id,0L)<level.getGameTime()?0:gifts.getOrDefault(id,0))+1;
                gifts.put(id,Math.min(5,score));until.put(id,level.getGameTime()+(org.slavicmyths.rpg.PathData.has(p,"offering")?13800:12000));nextGift=level.getGameTime()+600;state(2);particles(ParticleTypes.HAPPY_VILLAGER,8);
                if(score>=3){p.addEffect(new EffectInstance(Effects.LUCK,1200));if(random.nextInt(12)==0)spawnAtLocation(ModItems.FIELD_BUNDLE.get());}
                return;
            }
        }
        wander();
    }
    @Override public void provoke(PlayerEntity p){gifts.remove(p.getUUID());until.remove(p.getUUID());super.provoke(p);}
    @Override public void addAdditionalSaveData(CompoundNBT n){super.addAdditionalSaveData(n);ListNBT list=new ListNBT();gifts.forEach((id,score)->{CompoundNBT e=new CompoundNBT();e.putUUID("Player",id);e.putInt("Gifts",score);e.putLong("Until",until.getOrDefault(id,0L));list.add(e);});n.put("FieldFriends",list);n.putLong("NextGift",nextGift);}
    @Override public void readAdditionalSaveData(CompoundNBT n){super.readAdditionalSaveData(n);gifts.clear();until.clear();ListNBT list=n.getList("FieldFriends",10);for(int i=0;i<Math.min(16,list.size());i++){CompoundNBT e=list.getCompound(i);if(e.hasUUID("Player")){UUID id=e.getUUID("Player");gifts.put(id,Math.min(5,e.getInt("Gifts")));until.put(id,e.getLong("Until"));}}nextGift=n.getLong("NextGift");}
    @Override public int getAmbientSoundInterval(){return 650;}
    @Override protected void playStepSound(net.minecraft.util.math.BlockPos pos,net.minecraft.block.BlockState block){if(!level.isClientSide)voice("step",.12F);}
}
