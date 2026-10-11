from pathlib import Path
R=Path(__file__).resolve().parents[1]
def edit(path,a,b):
 p=R/path;s=p.read_text(encoding='utf8');assert s.count(a)==1,(path,a[:70],s.count(a));p.write_text(s.replace(a,b),encoding='utf8')
p='src/main/java/org/slavicmyths/pesigolovtsy/DogheadEntity.java'
edit(p,'new NearestAttackableTargetGoal<>(this,Player.class,10,true,false,this::valid)','new NearestAttackableTargetGoal<Player>(this,Player.class,10,true,false,this::valid){@Override public boolean canContinueToUse(){var t=DogheadEntity.this.getTarget();return valid(t)&&distanceToSqr(t)<=32*32&&(hasLineOfSight(t)||level().getGameTime()-seenAt<=160);}}')
edit(p,'if(lastSeen!=null)getNavigation().moveTo(lastSeen.x,lastSeen.y,lastSeen.z,.8);return;','if(lastSeen!=null){Vec3 search=lastSeen;if(getNavigation().isDone()&&distanceToSqr(lastSeen)<4)search=lastSeen.add(random.nextInt(9)-4,0,random.nextInt(9)-4);getNavigation().moveTo(search.x,search.y,search.z,.65);}return;')
p='src/main/java/org/slavicmyths/client/DogheadClient.java'
edit(p,'clip==Clip.READY||clip==Clip.RETREAT||clip==Clip.DODGE','clip==Clip.READY||clip==Clip.RETREAT||clip==Clip.DODGE||clip==Clip.SNIFF||clip==Clip.HUNTER_GUARD||clip==Clip.LEADER_GUARD')
print('Bounded last-seen search and prey victory applied.')
