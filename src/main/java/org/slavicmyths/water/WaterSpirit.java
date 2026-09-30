package org.slavicmyths.water;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.pathfinding.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.slavicmyths.entity.LandSpiritEntity;
public abstract class WaterSpirit extends LandSpiritEntity {
 protected WaterSpirit(EntityType<? extends WaterSpirit> t,World w){super(t,w);setPathfindingMalus(PathNodeType.WATER,0);moveControl=new MovementController(this){public void tick(){if(isInWater()&&operation==Action.MOVE_TO){Vector3d d=new Vector3d(wantedX-getX(),wantedY-getY(),wantedZ-getZ());if(d.lengthSqr()>.15){setDeltaMovement(getDeltaMovement().add(d.normalize().scale(.025*speedModifier)));yRot=(float)(Math.atan2(d.z,d.x)*180/Math.PI)-90;}else operation=Action.WAIT;}else super.tick();}};}
 protected PathNavigator createNavigation(World w){GroundPathNavigator n=new GroundPathNavigator(this,w);n.setCanFloat(true);return n;}
 public boolean canBreatheUnderwater(){return true;}
 public void travel(Vector3d input){if(isEffectiveAi()&&isInWater()){moveRelative(.03F,input);move(MoverType.SELF,getDeltaMovement());setDeltaMovement(getDeltaMovement().scale(.86));}else super.travel(input);}
 protected void registerGoals(){goalSelector.addGoal(1,new net.minecraft.entity.ai.goal.MeleeAttackGoal(this,1.1,false));goalSelector.addGoal(4,new net.minecraft.entity.ai.goal.LookAtGoal(this,net.minecraft.entity.player.PlayerEntity.class,18));goalSelector.addGoal(5,new net.minecraft.entity.ai.goal.LookRandomlyGoal(this));}
}
