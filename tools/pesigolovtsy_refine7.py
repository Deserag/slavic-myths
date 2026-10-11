from pathlib import Path
R=Path(__file__).resolve().parents[1]
def edit(path,a,b):
 p=R/path;s=p.read_text(encoding='utf8');assert s.count(a)==1,(path,a[:70],s.count(a));p.write_text(s.replace(a,b),encoding='utf8')
p='src/main/java/org/slavicmyths/pesigolovtsy/DogheadPose.java'
edit(p,'new float[]{-8,-5,-3,-3,-5,-8,-8,-8,-8}','new float[]{-8,-5,-3,-3,-5,-11,-8,-8,-8}')
p='src/main/java/org/slavicmyths/client/DogheadClient.java'
edit(p,'new float[]{-.15F,-1.2F,-1.5F,.35F,.45F,-.15F}):-.15F','new float[]{-.45F,-1.15F,-1.8F,-1.5F,-1.6F,-.45F}):-.45F')
edit(p,'n("arm_R_lower").xRot=-.3F;n("hand_R").xRot=.15F;','n("arm_R_lower").xRot=clip==Clip.KNIFE?-.05F:-.3F;n("hand_R").xRot=-n("arm_R_upper").xRot-n("arm_R_lower").xRot-n("chest").xRot;')
p='src/main/java/org/slavicmyths/pesigolovtsy/DogheadEntity.java'
edit(p,'ambientIn=240+random.nextInt(240);','ambientIn=(role()==Role.LEADER?300:240)+random.nextInt(240);')
print('Actual knife reach, axe contact and role ambience applied.')
