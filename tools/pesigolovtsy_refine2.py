from pathlib import Path
R=Path(__file__).resolve().parents[1]
def edit(path,a,b):
 p=R/path;s=p.read_text(encoding='utf8');assert s.count(a)==1,(path,a[:70],s.count(a));p.write_text(s.replace(a,b),encoding='utf8')
p='src/main/java/org/slavicmyths/pesigolovtsy/DogheadEntity.java'
edit(p,'private Vec3 lastSeen,slot;','private Vec3 lastSeen,slot,formationTarget;')
edit(p,'preyAfter,interestUntil,seenAt,replyAt;','preyAfter,interestUntil,seenAt,replyAt,retreatUntil;')
edit(p,'tracked=null;lastSeen=null;slot=null;interestUntil=0;','tracked=null;lastSeen=null;slot=null;formationTarget=null;retreatUntil=0;interestUntil=0;')
edit(p,'if(tickCount%25!=0&&(slot==null||slot.distanceToSqr(target.position())<9))return;','if(tickCount%25!=0&&(formationTarget==null||formationTarget.distanceToSqr(target.position())<9))return;formationTarget=target.position();')
edit(p,'if(mates.size()>1&&wounded*2>=mates.size()','if(now<retreatUntil){getLookControl().setLookAt(victim,20,15);}else if(mates.size()>1&&wounded*2>=mates.size()')
edit(p,'begin(Clip.RETREAT);dodgeAfter=now+160;','begin(Clip.RETREAT);retreatUntil=now+80;dodgeAfter=now+160;')
edit(p,'else if(sight&&facing&&now>=attackAfter&&d<=','else if(sight&&facing&&now<attackAfter&&d<1.8&&random.nextInt(30)==0){begin(role()==Role.HUNTER?Clip.HUNTER_GUARD:Clip.LEADER_GUARD);}else if(sight&&facing&&now>=attackAfter&&d<=')
edit(p,'role(r,false);founder=','role(r,false);if(n.contains("Health",net.minecraft.nbt.Tag.TAG_ANY_NUMERIC))setHealth(Math.clamp(n.getFloat("Health"),0,r.hp));founder=')
edit(p,'n.putBoolean("DogheadFounder",founder);','n.putInt("DogheadRetreatCooldown",(int)Math.clamp(retreatUntil-level().getGameTime(),0,80));n.putBoolean("DogheadFounder",founder);')
edit(p,'switchAfter=now+Math.clamp(n.getInt("DogheadSwitchCooldown"),0,20);','retreatUntil=now+Math.clamp(n.getInt("DogheadRetreatCooldown"),0,80);switchAfter=now+Math.clamp(n.getInt("DogheadSwitchCooldown"),0,20);')
edit(p,'else navigate(victim);}\n   else if(sight','else if(bow()&&d>=4&&sight&&!clearShot(victim,DogheadPose.nock(role(),Clip.BOW,22,getYRot()).add(position()))&&now>=switchAfter){begin(Clip.KNIFE_SWAP);switchAfter=now+20;}else navigate(victim);}\n   else if(sight')
p='src/main/java/org/slavicmyths/client/DogheadClient.java'
edit(p,'n("chest").y-=.12F*breath*(1-w);','n("chest").y-=.12F*breath*(1-w&&!clip.attack()?1:0);')
edit(p,'(1-w&&!clip.attack()?1:0)','(!clip.attack()?1-w:0)')
edit(p,'s.phase+=distance/stride;','stride*=e.role()==Role.SCOUT?.88F:e.role()==Role.LEADER?1.12F:1;s.phase+=distance/stride;')
edit(p,'n("pelvis").y+=sink;','n("pelvis").y+=sink;n("chest").yRot+=(float)java.lang.Math.sin(cycle)*.035F*w;n("head").yRot-=(float)java.lang.Math.sin(cycle)*.035F*w;')
edit(p,'name.startsWith("mantle_")&&!name.contains("root")','(name.startsWith("fur_")||name.startsWith("mantle_"))&&!name.contains("root")')
edit(p,'*.035F*w;','*.10F*w;',) if False else None
print('AI/health and pose refinements applied.')
