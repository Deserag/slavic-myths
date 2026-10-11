from pathlib import Path
R=Path(__file__).resolve().parents[1]
def edit(path,a,b):
 p=R/path;s=p.read_text(encoding='utf8');assert s.count(a)==1,(path,a[:70],s.count(a));p.write_text(s.replace(a,b),encoding='utf8')
p='src/main/java/org/slavicmyths/pesigolovtsy/DogheadEntity.java'
edit(p,'w.getLevel().noCollision(this,getBoundingBox())&&w.getBlockState','w.getLevel().noCollision(this,getBoundingBox())&&DogheadContent.clearLargestBody(w.getLevel(),candidate)&&w.getBlockState')
edit(p,'cue("shared.claw_gear",.35F);','cue(role().id()+".ambient",.35F);')
edit(p,'private float attackYaw;','private float attackYaw,stepPhase;private Vec3 stepPosition;')
edit(p,'Clip running=clock.clip;boolean marker=clock.tick();','if(stepPosition!=null&&onGround()&&(clock.clip==Clip.READY||clock.clip==Clip.RETREAT||clock.clip==Clip.DODGE)){double distance=position().subtract(stepPosition).horizontalDistance();if(distance<1){float next=stepPhase+(float)distance/DogheadPose.stride(role(),getTarget()!=null);if((int)(next*2)>(int)(stepPhase*2)){BlockPos floor=BlockPos.containing(getX(),getY()-.2,getZ());stepCue(floor,level().getBlockState(floor));}stepPhase=next%32;}}stepPosition=position();Clip running=clock.clip;boolean marker=clock.tick();')
edit(p,'@Override protected void playStepSound(BlockPos pos,net.minecraft.world.level.block.state.BlockState state){if(!level().isClientSide&&onGround()&&clock.clip==Clip.READY)cue(','@Override protected void playStepSound(BlockPos pos,net.minecraft.world.level.block.state.BlockState state){}\n private void stepCue(BlockPos pos,net.minecraft.world.level.block.state.BlockState state){if(!level().isClientSide&&onGround())cue(')
p='src/main/java/org/slavicmyths/pesigolovtsy/DogheadPose.java'
edit(p,'public static float shoulder(Role r)','public static float stride(Role r,boolean run){return (run?.85F:.65F)*(r==Role.SCOUT?.88F:r==Role.LEADER?1.12F:1);}\n public static float shoulder(Role r)')
p='src/main/java/org/slavicmyths/client/DogheadClient.java'
edit(p,'float stride=run?.85F:.65F;stride*=e.role()==Role.SCOUT?.88F:e.role()==Role.LEADER?1.12F:1;','float stride=DogheadPose.stride(e.role(),run);')
edit(p,'entry.getValue().xRot+=(float)java.lang.Math.sin(cycle-.45+name.hashCode()*.1)*.035F*w;','entry.getValue().xRot+=(float)java.lang.Math.sin(cycle-.45+name.hashCode()*.1)*.10F*w;')
print('Largest-role collision clearance and distance contacts applied.')
