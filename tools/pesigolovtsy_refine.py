"""One-off exact source refinements. Fails rather than changing unrelated source."""
from pathlib import Path
R=Path(__file__).resolve().parents[1]
def edit(path,a,b):
 p=R/path;s=p.read_text(encoding='utf8');assert s.count(a)==1,(path,a[:80],s.count(a));p.write_text(s.replace(a,b),encoding='utf8')
p='src/main/java/org/slavicmyths/pesigolovtsy/DogheadPose.java'
edit(p,'c==Clip.THRUST?0:-.20F','c==Clip.THRUST?-pitch(r,c,t):-.20F')
edit(p,'-1.8F,.52F,.8F,.2F','-1.8F,-.2F,.5F,.2F')
edit(p,'0,-2,-8,-8,-5,5,6,3,0','0,-2,-8,-8,-5,0,4,3,0')
p='src/main/java/org/slavicmyths/pesigolovtsy/DogheadEntity.java'
edit(p,'setTarget(victim);if(getTarget()!=victim)return;','setTarget(victim);if(getTarget()!=victim)return;if(victim instanceof Animal)preyAfter=level().getGameTime()+DogheadConfig.huntGap;')
edit(p,'LivingEntity victim=getTarget();if(victim!=null&&(!valid(victim)||distanceToSqr(victim)>40*40))','LivingEntity victim=getTarget();if(victim!=null&&!victim.isAlive()&&tracked!=null&&victim instanceof Animal){clearTarget();begin(Clip.VICTORY);victim=null;}if(victim!=null&&(!valid(victim)||distanceToSqr(victim)>(role()==Role.SCOUT&&clock.clip==Clip.SCOUT_SPOT?32*32:24*24)))')
edit(p,'if(clock.clip==Clip.READY&&victim!=null){double d=distanceTo(victim);','if(clock.clip==Clip.READY&&victim!=null){float wanted=(float)Math.toDegrees(Math.atan2(victim.getZ()-getZ(),victim.getX()-getX()))-90;setYRot(net.minecraft.util.Mth.rotLerp(.25F,getYRot(),wanted));yBodyRot=getYRot();boolean facing=Math.abs(net.minecraft.util.Mth.wrapDegrees(wanted-getYRot()))<15;double d=distanceTo(victim);')
edit(p,'else if(!bow()&&d<=1.75&&sight&&now>=attackAfter)','else if(!bow()&&d<=1.75&&sight&&facing&&now>=attackAfter)')
edit(p,'else if(bow()&&d>=4&&d<=18&&sight&&now>=attackAfter','else if(bow()&&d>=4&&d<=18&&sight&&facing&&now>=attackAfter')
edit(p,'else if(sight&&now>=attackAfter&&d<=','else if(sight&&facing&&now>=attackAfter&&d<=')
edit(p,'if(!placed)spawnRejected=true;','if(!placed)spawnRejected=true;')
edit(p,'||!w.getFluidState(candidate).isEmpty())continue;','||!w.getFluidState(candidate).isEmpty()||w.getLevel().getNearestPlayer(x+.5,y,z+.5,24,false)!=null)continue;')
edit(p,'private void navigate(LivingEntity target){if(tickCount%25!=0)return;','private void navigate(LivingEntity target){if(tickCount%25!=0&&(slot==null||slot.distanceToSqr(target.position())<9))return;')
edit(p,'n.putString("DogheadRole",role().name());','n.putString("DogheadRole",role().name());n.putBoolean("DogheadFounder",founder);n.putInt("DogheadNaturalCheck",naturalCheck);')
edit(p,'role(r,false);pack=','role(r,false);founder=n.getBoolean("DogheadFounder");naturalCheck=Math.clamp(n.getInt("DogheadNaturalCheck"),0,23);pack=')
edit(p,'n.putInt("DogheadAttackNonce",clock.nonce);','n.putInt("DogheadAttackNonce",clock.nonce);n.putInt("DogheadSwitchCooldown",(int)Math.clamp(switchAfter-level().getGameTime(),0,20));n.putInt("DogheadDodgeCooldown",(int)Math.clamp(dodgeAfter-level().getGameTime(),0,160));')
edit(p,'tracked=null;lastSeen=null;interestUntil=0;}','switchAfter=now+Math.clamp(n.getInt("DogheadSwitchCooldown"),0,20);dodgeAfter=now+Math.clamp(n.getInt("DogheadDodgeCooldown"),0,160);tracked=null;lastSeen=null;interestUntil=0;}')
p='src/main/java/org/slavicmyths/pesigolovtsy/DogheadContent.java'
edit(p,'p.distSqr(w.getLevel().getSharedSpawnPos())<96*96','p.distToLowCornerSqr(w.getLevel().getSharedSpawnPos().getX(),p.getY(),w.getLevel().getSharedSpawnPos().getZ())<96*96')
p='docs/pesigolovtsy/DESIGN.md';edit(p,'древко 1.85 блока','древко 2.0 блока')
print('Exact refinements applied.')
