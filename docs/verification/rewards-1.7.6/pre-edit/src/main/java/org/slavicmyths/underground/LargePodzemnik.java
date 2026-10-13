package org.slavicmyths.underground;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
public final class LargePodzemnik extends RoomBoss {
 private Vec3 heading=Vec3.ZERO;private final Set<UUID> chargeHits=new HashSet<>();
 public LargePodzemnik(EntityType<? extends Monster> t,Level l){super(t,l);}
 protected int leash(){return 32;}
 public void aiStep(){super.aiStep();if(!level().isClientSide&&isAlive()&&getHealth()<=90&&!raged()&&action()==Action.IDLE){rage();getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.242);begin(Action.RAGE,30);sound("rage",.8F);}}
 public boolean auraActive(){return isAlive()&&validTarget(getTarget());}
 protected void choose(){double d=distanceTo(getTarget());if(d>=5&&d<=12&&ready(Action.CHARGE)&&clearCharge(getTarget().position().subtract(position()).normalize())){chargeHits.clear();begin(Action.CHARGE,42);sound("warning",.8F);}else if(d>=2&&d<=9&&ready(Action.TREMOR)){begin(Action.TREMOR,34);sound("warning",.7F);}else if(d<3.1&&ready(Action.HEAVY)&&!OminousAura.knocked(getTarget())){begin(Action.HEAVY,raged()?62:70);sound("warning",.6F);}else if(d<3&&ready(Action.MELEE))begin(Action.MELEE,35);}
 private boolean clearCharge(Vec3 d){Vec3 dir=new Vec3(d.x,0,d.z).normalize();for(double x=1;x<=7;x+=.5){BlockPos p=BlockPos.containing(position().add(dir.scale(x)));if(!safe(p))return false;for(int a=-1;a<=1;a++)for(int b=-1;b<=1;b++)if(!level().getBlockState(p.offset(a,-1,b)).isCollisionShapeFullBlock(level(),p.offset(a,-1,b)))return false;}return true;}
 protected void step(){switch(action()){
  case MELEE->{if(elapsed==10)melee(10,3);if(elapsed==duration)finish(Action.MELEE,4,6);}
  case HEAVY->{int impact=raged()?32:38;if(elapsed==impact-12){if(validTarget(getTarget()))aim=getTarget().position();heading=new Vec3(aim.x-getX(),0,aim.z-getZ()).normalize();}if(elapsed==impact){sound("impact",.65F);dust(position().add(heading.scale(2)),40);for(var t:victims(position(),3.1)){Vec3 d=t.position().subtract(position()).normalize();if(EncounterRules.inCone(d.dot(heading),distanceTo(t),OminousAura.knocked(t)))t.hurt(damageSources().mobAttack(this),EncounterRules.HEAVY_DAMAGE);}}if(elapsed==duration)finish(Action.HEAVY,raged()?250:320,1);}
  case TREMOR->{if(elapsed==1){ring(2,12);ring(4,12);ring(6,12);}if(elapsed==24){sound("impact",.8F);dust(position(),36);for(var t:victims(position(),6))if(t.onGround()&&!t.isPassenger()&&Math.abs(t.getY()-getY())<.6&&!(t instanceof Player p&&(p.isCreative()||p.isSpectator()))){t.hurt(damageSources().mobAttack(this),3);t.push(0,.18,0);if(t instanceof Player p)OminousAura.knock(p,this);}}if(elapsed==duration)finish(Action.TREMOR,350,18);}
  case CHARGE->{if(elapsed==18){heading=aim.subtract(position());heading=new Vec3(heading.x,0,heading.z).normalize();if(!clearCharge(heading)){finish(Action.CHARGE,280,30);return;}sound("charge",.8F);}if(elapsed>=19&&elapsed<=36){if(horizontalCollision||!safe(BlockPos.containing(position().add(heading.scale(.45))))){setDeltaMovement(0,getDeltaMovement().y,0);sound("impact",.7F);finish(Action.CHARGE,280,30);return;}setDeltaMovement(heading.x*.42,getDeltaMovement().y,heading.z*.42);for(var t:victims(position().add(heading.scale(.4)),1.7))if(chargeHits.add(t.getUUID())){t.hurt(damageSources().mobAttack(this),8);t.push(heading.x*.8,.2,heading.z*.8);}if(elapsed%5==0)dust(position(),3);}if(elapsed==duration){setDeltaMovement(0,getDeltaMovement().y,0);finish(Action.CHARGE,280,18);}}
  case RAGE->{if(elapsed==10){dust(position(),30);sound("rage",.7F);}}
  default->{}
 }}
}
