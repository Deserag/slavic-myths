package org.slavicmyths.water;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.slavicmyths.entity.LandSpiritEntity;
public abstract class WaterSpirit extends LandSpiritEntity {
 protected WaterSpirit(EntityType<? extends WaterSpirit> t,Level w){super(t,w);setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.WATER,0);moveControl=new MoveControl(this){public void tick(){if(isInWater()&&operation==Operation.MOVE_TO){Vec3 d=new Vec3(wantedX-getX(),wantedY-getY(),wantedZ-getZ());if(d.lengthSqr()>.15){setDeltaMovement(getDeltaMovement().add(d.normalize().scale(.025*speedModifier)));setYRot((float)(Math.atan2(d.z,d.x)*180/Math.PI)-90);}else operation=Operation.WAIT;}else super.tick();}};}
 protected PathNavigation createNavigation(Level w){GroundPathNavigation n=new GroundPathNavigation(this,w);n.setCanFloat(true);return n;}
 @Override public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor world,net.minecraft.world.DifficultyInstance difficulty,net.minecraft.world.entity.MobSpawnType reason,net.minecraft.world.entity.SpawnGroupData data){
  var result=super.finalizeSpawn(world,difficulty,reason,data);
  if(world.getBiome(blockPosition()).is(net.minecraft.world.level.biome.Biomes.SWAMP)){
   double hp=this instanceof RusalkaEntity?36:this instanceof VodyanoyEntity?72:getMaxHealth();
   getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(hp);setHealth((float)hp);
  }return result;
 }
 public void travel(Vec3 input){if(isEffectiveAi()&&isInWater()){moveRelative(.03F,input);move(MoverType.SELF,getDeltaMovement());setDeltaMovement(getDeltaMovement().scale(.86));}else super.travel(input);}
 protected void registerGoals(){goalSelector.addGoal(1,new net.minecraft.world.entity.ai.goal.MeleeAttackGoal(this,1.1,false));goalSelector.addGoal(4,new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this,net.minecraft.world.entity.player.Player.class,18));goalSelector.addGoal(5,new net.minecraft.world.entity.ai.goal.RandomLookAroundGoal(this));}
}
