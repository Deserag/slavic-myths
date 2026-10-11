from pathlib import Path
R=Path(__file__).resolve().parents[1]
def edit(path,a,b):
 p=R/path;s=p.read_text(encoding='utf8');assert s.count(a)==1,(path,a[:70],s.count(a));p.write_text(s.replace(a,b),encoding='utf8')
p='src/main/java/org/slavicmyths/pesigolovtsy/DogheadEntity.java'
edit(p,'seenAt,replyAt,retreatUntil;','seenAt,replyAt,retreatUntil,approachUntil;')
edit(p,'role()==Role.SCOUT&&clock.clip==Clip.SCOUT_SPOT?32*32:24*24','role()==Role.SCOUT&&(tracked==null||now<approachUntil)?32*32:24*24')
edit(p,'tracked=victim.getUUID();seenAt=now;','tracked=victim.getUUID();approachUntil=now+80;seenAt=now;')
edit(p,'if(clock.clip==Clip.READY&&victim==null&&tickCount%400==0)begin(Clip.SNIFF);','if(clock.clip==Clip.READY&&victim==null&&tickCount%400==0){begin(Clip.SNIFF);Vec3 forward=new Vec3(-Math.sin(Math.toRadians(getYRot())),0,Math.cos(Math.toRadians(getYRot())));Vec3 sniff=position().add(forward.scale(2));getNavigation().moveTo(sniff.x,sniff.y,sniff.z,.35);}')
edit(p,'clock.clip!=Clip.LEADER_GUARD)getNavigation().stop();','clock.clip!=Clip.LEADER_GUARD&&clock.clip!=Clip.SNIFF)getNavigation().stop();')
edit(p,'if(!sight)','if(!sight)') if False else None
print('Scout sight approach and actual sniff navigation applied.')
