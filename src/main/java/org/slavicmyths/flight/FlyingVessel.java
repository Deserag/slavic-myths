package org.slavicmyths.flight;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.*;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.*;
import net.minecraft.core.particles.*;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.*;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.Difficulty;
import net.minecraft.world.BossEvent;

import org.slavicmyths.registry.ModItems;
public final class FlyingVessel extends Entity {
 private static final double[] SPEED_BONUS={1,1.15,1.3,1.5};
 private static final EntityDataAccessor<Integer> WIND=SynchedEntityData.defineId(FlyingVessel.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Float> BANK=SynchedEntityData.defineId(FlyingVessel.class,EntityDataSerializers.FLOAT),TILT=SynchedEntityData.defineId(FlyingVessel.class,EntityDataSerializers.FLOAT),DRIVE=SynchedEntityData.defineId(FlyingVessel.class,EntityDataSerializers.FLOAT);
 public final boolean mortar;public final SimpleContainer cargo=new SimpleContainer(9);private UUID owner;private ItemStack vessel=ItemStack.EMPTY;public int viewers;
 private float forward,strafe,lift,wantedYaw;private boolean brake;private long lastInput;private int lerpSteps;private double targetX,targetY,targetZ;private float targetYaw;
 public FlyingVessel(EntityType<? extends FlyingVessel> t,Level w,boolean mortar){super(t,w);this.mortar=mortar;blocksBuilding=true;}
 @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder){builder.define(WIND,0);builder.define(BANK,0F);builder.define(TILT,0F);builder.define(DRIVE,0F);}
 public float roll(){return entityData.get(BANK);}public float pitch(){return entityData.get(TILT);}public float drive(){return entityData.get(DRIVE);}
 public int tailwind(){return entityData.get(WIND);}
 @Nullable public Player pilot(){return getPassengers().isEmpty()||!(getPassengers().get(0) instanceof Player)?null:(Player)getPassengers().get(0);}
 // Vanilla vehicle-move packets must not become an alternate movement authority.
 @Override @Nullable public LivingEntity getControllingPassenger(){return null;}
 @Override protected boolean canAddPassenger(Entity e){return getPassengers().isEmpty()&&e instanceof Player;}
 public boolean owns(Player p){return owner==null||owner.equals(p.getUUID());}
 public void input(ServerPlayer p,float f,float s,float up,float yaw,boolean stop){
  if(pilot()!=p||!owns(p)||!p.isAlive()||!Float.isFinite(f)||!Float.isFinite(s)||!Float.isFinite(up)||!Float.isFinite(yaw))return;
  forward=Mth.clamp(f,-1,1);strafe=Mth.clamp(s,-1,1);lift=Mth.clamp(up,-1,1);wantedYaw=Mth.wrapDegrees(yaw);brake=stop;lastInput=level().getGameTime();
 }
 public void fromItem(ItemStack stack,UUID id){owner=id;vessel=stack.copy();vessel.setCount(1);loadItemCargo(stack);org.slavicmyths.item.ItemState.removeCargo(vessel);entityData.set(WIND,Mth.clamp(Tailwind.level(registryAccess(),vessel),0,3));}
 private net.minecraft.core.NonNullList<ItemStack> itemCargo(){var items=net.minecraft.core.NonNullList.withSize(9,ItemStack.EMPTY);for(int i=0;i<9;i++)items.set(i,cargo.getItem(i).copy());return items;}
 private void loadItemCargo(ItemStack stack){cargo.clearContent();if(!mortar)return;var items=org.slavicmyths.item.ItemState.inventory(stack,true,registryAccess());for(int i=0;i<9;i++){ItemStack item=items.get(i);if(!item.isEmpty())item.setCount(Math.min(item.getCount(),item.getMaxStackSize()));cargo.setItem(i,item);}}
 private void loadCargo(CompoundTag n){cargo.clearContent();if(!mortar)return;net.minecraft.core.NonNullList<ItemStack> list=net.minecraft.core.NonNullList.withSize(9,ItemStack.EMPTY);net.minecraft.world.ContainerHelper.loadAllItems(n,list,registryAccess());for(int i=0;i<9;i++){ItemStack s=list.get(i);if(!s.isEmpty())s.setCount(Math.min(s.getCount(),s.getMaxStackSize()));cargo.setItem(i,s);}}
 private CompoundTag saveCargo(){net.minecraft.core.NonNullList<ItemStack> list=net.minecraft.core.NonNullList.withSize(9,ItemStack.EMPTY);for(int i=0;i<9;i++)list.set(i,cargo.getItem(i).copy());return net.minecraft.world.ContainerHelper.saveAllItems(new CompoundTag(),list,registryAccess());}
 public void openCargo(ServerPlayer p){if(!mortar||!owns(p)||!isAlive()||p.distanceToSqr(this)>16||viewers>0)return;if(owner==null)owner=p.getUUID();(p).openMenu(new SimpleMenuProvider((id,inv,player)->new CargoMenu(id,inv,this),Component.translatable("flight.slavicmyths.cargo")),b->b.writeVarInt(getId()));}
 @Override public InteractionResult interact(Player p,InteractionHand hand){if(level().isClientSide)return InteractionResult.SUCCESS;if(!owns(p)||p.isSpectator())return InteractionResult.FAIL;
  if(mortar&&p.isSecondaryUseActive()){openCargo((ServerPlayer)p);return InteractionResult.CONSUME;}
  if(!isVehicle()){if(owner==null)owner=p.getUUID();if(p.startRiding(this)){wantedYaw=p.getYRot();p.displayClientMessage(Component.translatable("flight.slavicmyths.controls"),true);}}return InteractionResult.CONSUME;
 }
 @Override public boolean hurt(DamageSource src,float amount){if(level().isClientSide||!isAlive()||isVehicle()||viewers>0||!(src.getEntity() instanceof Player))return false;Player p=(Player)src.getEntity();if(!owns(p)||!p.isCrouching()||p.distanceToSqr(this)>16)return false;
  ItemStack result=vessel.isEmpty()?new ItemStack(mortar?ModItems.FLYING_MORTAR.get():ModItems.FLYING_BROOM.get()):vessel.copy();if(mortar)org.slavicmyths.item.ItemState.inventory(result,true,itemCargo());cargo.clearContent();org.slavicmyths.yaga.FlightRecovery.repack(p);discard();if(!p.getInventory().add(result))p.drop(result,false);return true;
 }
 @Override public boolean isPickable(){return isAlive();}
 @Override public boolean isPushable(){return false;}
 @Override public boolean canBeCollidedWith(){return isAlive();}
 @Override public boolean causeFallDamage(float d,float multiplier,DamageSource source){return false;}
 @Override public boolean shouldRiderSit(){return false;}
 @Override public Vec3 getPassengerRidingPosition(Entity rider){return position().add(0,mortar?.65:0,0);}
 @Override protected void positionRider(Entity rider,Entity.MoveFunction move){super.positionRider(rider,move);rider.fallDistance=0;}
 @Override public Vec3 getDismountLocationForPassenger(LivingEntity rider){for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){if(x==0&&z==0)continue;Vec3 p=position().add(x*1.3,.2,z*1.3);if(level().noCollision(rider,rider.getBoundingBox().move(p.subtract(rider.position()))))return p;}return position().add(0,getBbHeight()+.2,0);}
 @Override public void tick(){super.tick();if(level().isClientSide){if(lerpSteps>0){setPos(getX()+(targetX-getX())/lerpSteps,getY()+(targetY-getY())/lerpSteps,getZ()+(targetZ-getZ())/lerpSteps);setYRot(getYRot()+Mth.wrapDegrees(targetYaw-getYRot())/lerpSteps);lerpSteps--;}
   if(tickCount%10==0&&isVehicle()&&drive()!=0&&level().getBlockState(blockPosition().below()).isSolid())level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK,net.minecraft.world.level.block.Blocks.OAK_LEAVES.defaultBlockState()),getX(),getY()+.1,getZ(),0,.025,0);return;}
  Player p=pilot();boolean active=p!=null&&p.isAlive();if(!active||level().getGameTime()-lastInput>15){forward=strafe=lift=0;brake=true;}
  if(active&&mortar&&p.getMainHandItem().getItem()!=ModItems.PESTLE.get()&&p.getOffhandItem().getItem()!=ModItems.PESTLE.get()){forward=strafe=lift=0;brake=true;if(tickCount%80==0)p.displayClientMessage(Component.translatable("flight.slavicmyths.need_pestle"),true);}
  float turn=active?Mth.clamp(Mth.wrapDegrees(wantedYaw-getYRot()),mortar?-2.5F:-6F,mortar?2.5F:6F):0;setYRot(getYRot()+turn);
  double bonus=SPEED_BONUS[tailwind()],max=(mortar?.28:.6)*bonus,accel=(mortar?.009:.038)*bonus;
  Vec3 v=getDeltaMovement();double drag=brake?(mortar?.86:.68):(mortar?.975:.94);if(tailwind()==3&&!brake)drag=Math.min(.987,drag+.008);
  double rad=Math.toRadians(getYRot()),fx=-Math.sin(rad),fz=Math.cos(rad),norm=Math.max(1,Math.sqrt(forward*forward+strafe*strafe));
  double vx=v.x*drag+(fx*forward+fz*strafe)*accel/norm,vz=v.z*drag+(fz*forward-fx*strafe)*accel/norm;double len=Math.sqrt(vx*vx+vz*vz);if(len>max){vx*=max/len;vz*=max/len;}
  double desired=active?(lift<0?lift*.32:lift*(mortar?.17:.32)):(isInWater()?.025:Math.max(-.5,v.y-.04));double vy=active?v.y+(desired-v.y)*(mortar?.3:.18):desired;
  if(getY()>level().getMaxBuildHeight()-3&&vy>0)vy=0;if(getY()<2&&vy<0)vy=0;
  Vec3 motion=new Vec3(vx,vy,vz);if(!level().hasChunkAt(BlockPos.containing(getX()+vx,getY(),getZ()+vz)))motion=Vec3.ZERO;
  boolean takeoff=onGround()&&vy>.03;setDeltaMovement(motion);move(MoverType.SELF,motion);if(horizontalCollision)setDeltaMovement(getDeltaMovement().multiply(.2,1,.2));if(verticalCollision)setDeltaMovement(getDeltaMovement().multiply(1,0,1));hasImpulse=true;fallDistance=0;
  entityData.set(BANK,roll()+((-turn*(mortar?.6F:2.3F))-roll())*.15F);entityData.set(TILT,pitch()+((float)(-forward*(mortar?3:9)-vy*22)-pitch())*.12F);entityData.set(DRIVE,brake?-2F:forward);
  if(active){p.fallDistance=0;if(takeoff){org.slavicmyths.progression.Knowledge.award(p,"first_flight");((net.minecraft.server.level.ServerLevel)level()).sendParticles(ParticleTypes.CLOUD,getX(),getY(),getZ(),3,.2,.05,.2,.01);}
   if(tickCount%(mortar?100:40)==0)playSound(mortar?SoundEvents.BEACON_AMBIENT:SoundEvents.ELYTRA_FLYING,mortar?.035F:(float)(.025+Math.min(.07,len*.1)),mortar?.65F:1.6F);
   if(tickCount%70==0)playSound(mortar?SoundEvents.BARREL_OPEN:SoundEvents.BAMBOO_STEP,.08F,mortar?.65F:.8F);}
 }
 @Override public void lerpTo(double x,double y,double z,float yaw,float pitch,int steps){targetX=x;targetY=y;targetZ=z;targetYaw=yaw;lerpSteps=Math.max(1,Math.min(10,steps));}
 @Override protected void readAdditionalSaveData(CompoundTag n){owner=n.hasUUID("Owner")?n.getUUID("Owner"):null;vessel=ItemStack.parseOptional(registryAccess(),n.getCompound("Vessel"));loadCargo(n.getCompound("Cargo"));entityData.set(WIND,Mth.clamp(Tailwind.level(registryAccess(),vessel),0,3));forward=strafe=lift=0;wantedYaw=getYRot();}
 @Override protected void addAdditionalSaveData(CompoundTag n){if(owner!=null)n.putUUID("Owner",owner);n.put("Vessel",vessel.saveOptional(registryAccess()));if(mortar)n.put("Cargo",saveCargo());}

 @Override public ItemStack getPickedResult(net.minecraft.world.phys.HitResult hit){return new ItemStack(mortar?ModItems.FLYING_MORTAR.get():ModItems.FLYING_BROOM.get());}
}
