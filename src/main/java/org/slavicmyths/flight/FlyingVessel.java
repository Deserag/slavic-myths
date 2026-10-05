package org.slavicmyths.flight;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.*;
import net.minecraft.particles.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.*;
import net.minecraftforge.fml.network.NetworkHooks;
import org.slavicmyths.registry.ModItems;
public final class FlyingVessel extends Entity {
 private static final double[] SPEED_BONUS={1,1.15,1.3,1.5};
 private static final DataParameter<Integer> WIND=EntityDataManager.defineId(FlyingVessel.class,DataSerializers.INT);
 private static final DataParameter<Float> BANK=EntityDataManager.defineId(FlyingVessel.class,DataSerializers.FLOAT),TILT=EntityDataManager.defineId(FlyingVessel.class,DataSerializers.FLOAT),DRIVE=EntityDataManager.defineId(FlyingVessel.class,DataSerializers.FLOAT);
 public final boolean mortar;public final Inventory cargo=new Inventory(9);private UUID owner;private ItemStack vessel=ItemStack.EMPTY;public int viewers;
 private float forward,strafe,lift,wantedYaw;private boolean brake;private long lastInput;private int lerpSteps;private double targetX,targetY,targetZ;private float targetYaw;
 public FlyingVessel(EntityType<? extends FlyingVessel> t,World w,boolean mortar){super(t,w);this.mortar=mortar;blocksBuilding=true;}
 @Override protected void defineSynchedData(){entityData.define(WIND,0);entityData.define(BANK,0F);entityData.define(TILT,0F);entityData.define(DRIVE,0F);}
 public float roll(){return entityData.get(BANK);}public float pitch(){return entityData.get(TILT);}public float drive(){return entityData.get(DRIVE);}
 public int tailwind(){return entityData.get(WIND);}
 @Nullable public PlayerEntity pilot(){return getPassengers().isEmpty()||!(getPassengers().get(0) instanceof PlayerEntity)?null:(PlayerEntity)getPassengers().get(0);}
 // Vanilla vehicle-move packets must not become an alternate movement authority.
 @Override @Nullable public Entity getControllingPassenger(){return null;}
 @Override protected boolean canAddPassenger(Entity e){return getPassengers().isEmpty()&&e instanceof PlayerEntity;}
 public boolean owns(PlayerEntity p){return owner==null||owner.equals(p.getUUID());}
 public void input(ServerPlayerEntity p,float f,float s,float up,float yaw,boolean stop){
  if(pilot()!=p||!owns(p)||!p.isAlive()||!Float.isFinite(f)||!Float.isFinite(s)||!Float.isFinite(up)||!Float.isFinite(yaw))return;
  forward=MathHelper.clamp(f,-1,1);strafe=MathHelper.clamp(s,-1,1);lift=MathHelper.clamp(up,-1,1);wantedYaw=MathHelper.wrapDegrees(yaw);brake=stop;lastInput=level.getGameTime();
 }
 public void fromItem(ItemStack stack,UUID id){owner=id;vessel=stack.copy();vessel.setCount(1);loadCargo(stack.getOrCreateTag().getCompound("FlightCargo"));vessel.removeTagKey("FlightCargo");entityData.set(WIND,MathHelper.clamp(net.minecraft.enchantment.EnchantmentHelper.getItemEnchantmentLevel(Tailwind.TAILWIND.get(),vessel),0,3));}
 private void loadCargo(CompoundNBT n){cargo.clearContent();if(!mortar)return;net.minecraft.util.NonNullList<ItemStack> list=net.minecraft.util.NonNullList.withSize(9,ItemStack.EMPTY);ItemStackHelper.loadAllItems(n,list);for(int i=0;i<9;i++){ItemStack s=list.get(i);if(!s.isEmpty())s.setCount(Math.min(s.getCount(),s.getMaxStackSize()));cargo.setItem(i,s);}}
 private CompoundNBT saveCargo(){net.minecraft.util.NonNullList<ItemStack> list=net.minecraft.util.NonNullList.withSize(9,ItemStack.EMPTY);for(int i=0;i<9;i++)list.set(i,cargo.getItem(i).copy());return ItemStackHelper.saveAllItems(new CompoundNBT(),list);}
 public void openCargo(ServerPlayerEntity p){if(!mortar||!owns(p)||!isAlive()||p.distanceToSqr(this)>16||viewers>0)return;if(owner==null)owner=p.getUUID();NetworkHooks.openGui(p,new SimpleNamedContainerProvider((id,inv,player)->new CargoMenu(id,inv,this),new TranslationTextComponent("flight.slavicmyths.cargo")),b->b.writeVarInt(getId()));}
 @Override public ActionResultType interact(PlayerEntity p,Hand hand){if(level.isClientSide)return ActionResultType.SUCCESS;if(!owns(p)||p.isSpectator())return ActionResultType.FAIL;
  if(mortar&&p.isSecondaryUseActive()){openCargo((ServerPlayerEntity)p);return ActionResultType.CONSUME;}
  if(!isVehicle()){if(owner==null)owner=p.getUUID();if(p.startRiding(this)){wantedYaw=p.yRot;p.displayClientMessage(new TranslationTextComponent("flight.slavicmyths.controls"),true);}}return ActionResultType.CONSUME;
 }
 @Override public boolean hurt(DamageSource src,float amount){if(level.isClientSide||!isAlive()||isVehicle()||viewers>0||!(src.getEntity() instanceof PlayerEntity))return false;PlayerEntity p=(PlayerEntity)src.getEntity();if(!owns(p)||!p.isCrouching()||p.distanceToSqr(this)>16)return false;
  ItemStack result=vessel.isEmpty()?new ItemStack(mortar?ModItems.FLYING_MORTAR.get():ModItems.FLYING_BROOM.get()):vessel.copy();if(mortar)result.getOrCreateTag().put("FlightCargo",saveCargo());cargo.clearContent();org.slavicmyths.yaga.FlightRecovery.repack(p);remove();if(!p.inventory.add(result))p.drop(result,false);return true;
 }
 @Override public boolean isPickable(){return isAlive();}
 @Override public boolean isPushable(){return false;}
 @Override public boolean canBeCollidedWith(){return isAlive();}
 @Override public boolean causeFallDamage(float d,float multiplier){return false;}
 @Override public boolean shouldRiderSit(){return false;}
 @Override public double getPassengersRidingOffset(){return mortar?.65:0;}
 @Override public void positionRider(Entity rider){if(hasPassenger(rider)){rider.setPos(getX(),getY()+getPassengersRidingOffset()+rider.getMyRidingOffset(),getZ());rider.fallDistance=0;}}
 @Override public Vector3d getDismountLocationForPassenger(LivingEntity rider){for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){if(x==0&&z==0)continue;Vector3d p=position().add(x*1.3,.2,z*1.3);if(level.noCollision(rider,rider.getBoundingBox().move(p.subtract(rider.position()))))return p;}return position().add(0,getBbHeight()+.2,0);}
 @Override public void tick(){super.tick();if(level.isClientSide){if(lerpSteps>0){setPos(getX()+(targetX-getX())/lerpSteps,getY()+(targetY-getY())/lerpSteps,getZ()+(targetZ-getZ())/lerpSteps);yRot+=MathHelper.wrapDegrees(targetYaw-yRot)/lerpSteps;lerpSteps--;}
   if(tickCount%10==0&&isVehicle()&&drive()!=0&&level.getBlockState(blockPosition().below()).getMaterial().isSolid())level.addParticle(new BlockParticleData(ParticleTypes.BLOCK,net.minecraft.block.Blocks.OAK_LEAVES.defaultBlockState()),getX(),getY()+.1,getZ(),0,.025,0);return;}
  PlayerEntity p=pilot();boolean active=p!=null&&p.isAlive();if(!active||level.getGameTime()-lastInput>15){forward=strafe=lift=0;brake=true;}
  if(active&&mortar&&p.getMainHandItem().getItem()!=ModItems.PESTLE.get()&&p.getOffhandItem().getItem()!=ModItems.PESTLE.get()){forward=strafe=lift=0;brake=true;if(tickCount%80==0)p.displayClientMessage(new TranslationTextComponent("flight.slavicmyths.need_pestle"),true);}
  float turn=active?MathHelper.clamp(MathHelper.wrapDegrees(wantedYaw-yRot),mortar?-2.5F:-6F,mortar?2.5F:6F):0;yRot+=turn;
  double bonus=SPEED_BONUS[tailwind()],max=(mortar?.28:.6)*bonus,accel=(mortar?.009:.038)*bonus;
  Vector3d v=getDeltaMovement();double drag=brake?(mortar?.86:.68):(mortar?.975:.94);if(tailwind()==3&&!brake)drag=Math.min(.987,drag+.008);
  double rad=Math.toRadians(yRot),fx=-Math.sin(rad),fz=Math.cos(rad),norm=Math.max(1,Math.sqrt(forward*forward+strafe*strafe));
  double vx=v.x*drag+(fx*forward+fz*strafe)*accel/norm,vz=v.z*drag+(fz*forward-fx*strafe)*accel/norm;double len=Math.sqrt(vx*vx+vz*vz);if(len>max){vx*=max/len;vz*=max/len;}
  double desired=active?lift*(mortar?.17:.32):(isInWater()?.025:Math.max(-.5,v.y-.04));double vy=active?v.y+(desired-v.y)*(mortar?.3:.18):desired;
  if(getY()>level.getMaxBuildHeight()-3&&vy>0)vy=0;if(getY()<2&&vy<0)vy=0;
  Vector3d motion=new Vector3d(vx,vy,vz);if(!level.hasChunkAt(new BlockPos(getX()+vx,getY(),getZ()+vz)))motion=Vector3d.ZERO;
  boolean takeoff=isOnGround()&&vy>.03;setDeltaMovement(motion);move(MoverType.SELF,motion);if(horizontalCollision)setDeltaMovement(getDeltaMovement().multiply(.2,1,.2));if(verticalCollision)setDeltaMovement(getDeltaMovement().multiply(1,0,1));hasImpulse=true;fallDistance=0;
  entityData.set(BANK,roll()+((-turn*(mortar?.6F:2.3F))-roll())*.15F);entityData.set(TILT,pitch()+((float)(-forward*(mortar?3:9)-vy*22)-pitch())*.12F);entityData.set(DRIVE,brake?-2F:forward);
  if(active){p.fallDistance=0;if(takeoff){org.slavicmyths.progression.Knowledge.award(p,"first_flight");((net.minecraft.world.server.ServerWorld)level).sendParticles(ParticleTypes.CLOUD,getX(),getY(),getZ(),3,.2,.05,.2,.01);}
   if(tickCount%(mortar?100:40)==0)playSound(mortar?SoundEvents.BEACON_AMBIENT:SoundEvents.ELYTRA_FLYING,mortar?.035F:(float)(.025+Math.min(.07,len*.1)),mortar?.65F:1.6F);
   if(tickCount%70==0)playSound(mortar?SoundEvents.BARREL_OPEN:SoundEvents.BAMBOO_STEP,.08F,mortar?.65F:.8F);}
 }
 @Override public void lerpTo(double x,double y,double z,float yaw,float pitch,int steps,boolean teleport){targetX=x;targetY=y;targetZ=z;targetYaw=yaw;lerpSteps=Math.max(1,Math.min(10,steps));}
 @Override protected void readAdditionalSaveData(CompoundNBT n){owner=n.hasUUID("Owner")?n.getUUID("Owner"):null;vessel=ItemStack.of(n.getCompound("Vessel"));loadCargo(n.getCompound("Cargo"));entityData.set(WIND,MathHelper.clamp(net.minecraft.enchantment.EnchantmentHelper.getItemEnchantmentLevel(Tailwind.TAILWIND.get(),vessel),0,3));forward=strafe=lift=0;wantedYaw=yRot;}
 @Override protected void addAdditionalSaveData(CompoundNBT n){if(owner!=null)n.putUUID("Owner",owner);n.put("Vessel",vessel.save(new CompoundNBT()));if(mortar)n.put("Cargo",saveCargo());}
 @Override public IPacket<?> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
 @Override public ItemStack getPickedResult(net.minecraft.util.math.RayTraceResult hit){return new ItemStack(mortar?ModItems.FLYING_MORTAR.get():ModItems.FLYING_BROOM.get());}
}
