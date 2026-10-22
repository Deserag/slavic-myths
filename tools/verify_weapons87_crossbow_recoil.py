"""P31 source/physics regression checks for the first-person recoil path; no client launch."""
from pathlib import Path
import math,re,sys,zipfile
ROOT=Path(__file__).resolve().parents[1]
code=(ROOT/'src/main/java/org/slavicmyths/client/WeaponHands.java').read_text(encoding='utf-8')
for token in ('e.getItemStack().getItem() instanceof SerpentCrossbow','CrossbowItem.isCharged(stack)',
              'observedCharged[index]&&!charged&&!player.isUsingItem()','motionPlayer','motionLevel',
              'observedCrossbows[index]!=stack','hand==InteractionHand.MAIN_HAND?0:1',
              'lastShotClock[index]=Double.NEGATIVE_INFINITY','elapsed<0||elapsed>=7',
              'Math.sin(Math.PI*phase)*(1.0-.25*phase)','renderGrip(e,sign)',
              'drawGeometry(e,mc.getItemRenderer().getModel(e.getItemStack(),mc.level,p,0),left)'):
    assert token in code,token
assert code.index('crossbowKick(p,mc.level,e.getItemStack(),e.getHand(),e.getPartialTick())')<code.index('crossbowWrist(pose,sign,swing,pull)')
assert code.index('if(kick>0){pose.translate(')<code.index('renderGrip(e,sign)')
assert code.count('crossbowKick(p,mc.level')==1
assert 'shotKick' not in code and 'CrossbowVolley.enqueue' not in code
assert not any(s in code for s in ('new LightningBolt','addFreshEntity(','getCooldowns().addCooldown','set(DataComponents.CHARGED_PROJECTILES'))
def amplitude(t):
    if t<0 or t>=7:return 0.0
    p=t/7.0
    return math.sin(math.pi*p)*(1-.25*p)
for t in [-1,0,7,10]:assert abs(amplitude(t))<1e-9
for t in [1,2,3,4,5,6]:assert 0<amplitude(t)<1
assert amplitude(3)>amplitude(1) and amplitude(6)<amplitude(3)
assert '.035* kick' in code and '.13*kick' in code and 'rotationDegrees(-9*kick)' in code
if '--jar' in sys.argv:
    jars=list((ROOT/'build/libs').glob('slavicmyths-*.jar'))
    assert len(jars)==1,jars
    with zipfile.ZipFile(jars[0]) as z:
        assert 'org/slavicmyths/client/WeaponHands.class' in z.namelist()
print('PASS P31 source recoil: two independent hands, charge->fire only on same stack, reset on world/player swap, 7 tick envelope, zero outside window, single FP pose before arm + weapon, no ammo/server changes; Minecraft NOT_RUN')
