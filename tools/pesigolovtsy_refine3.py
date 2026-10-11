from pathlib import Path
R=Path(__file__).resolve().parents[1]
def edit(path,a,b):
 p=R/path;s=p.read_text(encoding='utf8');assert s.count(a)==1,(path,a[:70],s.count(a));p.write_text(s.replace(a,b),encoding='utf8')
p='src/main/java/org/slavicmyths/pesigolovtsy/DogheadPose.java'
edit(p,'new float[]{-.85F,-.6F,-.25F,-.05F,0,-.25F,-.85F}):-.85F','new float[]{.85F,.6F,.25F,.05F,0,.25F,.85F}):.85F')
edit(p,'c==Clip.THRUST?-pitch(r,c,t):-.20F','c==Clip.THRUST?-pitch(r,c,t)-.55F*(1-smooth(t,5,10)+smooth(t,16,24)):c==Clip.SWEEP?-.20F:-.75F')
p='src/main/java/org/slavicmyths/pesigolovtsy/DogheadEntity.java'
edit(p,'if(running==Clip.BOW&&clock.age==12)','if(running==Clip.BOW&&clock.age==20&&victim!=null&&!clearShot(victim,DogheadPose.nock(role(),running,22,attackYaw).add(position()))){begin(Clip.READY);attackAfter=now+20;return;}if(running==Clip.BOW&&clock.age==12)')
edit(p,'state.is(net.minecraft.tags.BlockTags.DIRT)?"shared.leather_step":state.is(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD)?"shared.stone_step":"shared.mud_step"','!state.getFluidState().isEmpty()||state.is(net.minecraft.world.level.block.Blocks.MUD)?"shared.mud_step":state.is(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD)||state.is(net.minecraft.world.level.block.Blocks.COBBLESTONE)?"shared.stone_step":"shared.leather_step"')
print('Weapon ready angle and blocked shot refinements applied.')
