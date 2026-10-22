"""Owner-reported runtime failures: source+JAR regression checks, native source-only A/B."""
from pathlib import Path
import argparse,copy,json,zipfile,hashlib
from weapons87_p42_models import ADDITIONS

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/slavicmyths'
DOCS=ROOT/'docs/implementation/weapons87/owner-qa-20261022'
SOURCE=json.loads((DOCS/'before-12-crossbow-transforms.json').read_text(encoding='utf-8'))
IDS=('serpent_crossbow','three_breath_crossbow')
STATES=('','_pulling_0','_pulling_1','_pulling_2','_arrow','_firework')
CROSSBOWS={}
for row in SOURCE['records']:CROSSBOWS[row['path']]=row
def load_model(path):
    return json.loads((ROOT/path).read_text(encoding='utf-8'))
def check(jar=False):
    assert len(SOURCE['records'])==12
    checked=0
    for id in IDS:
        for suffix in STATES:
            name=id+suffix
            path='src/main/resources/assets/slavicmyths/models/item/'+name+'.json'
            saved=CROSSBOWS[path]
            m=load_model(path)
            assert len(m['elements'])==saved['elementCount']+len(ADDITIONS[id]),name
            assert m['elements'][saved['elementCount']:] == ADDITIONS[id],name+' unexpected geometry after the P42 approved overlay'
            for side in ('right','left'):
                tag='thirdperson_'+side+'hand'
                old=saved['oldTP'][side]
                new=m['display'][tag]
                assert old['rotation']==[-90,0,0] and old['translation']==[0,-2.475,-1.375] and old['scale']==[.55]*3
                assert new['rotation']==[0,0,0] and new['translation']==[0,1.375,-2.475] and new['scale']==[.55]*3
                assert new['rotation'][0]!=old['rotation'][0],(name,tag,'vertical stock regression')
            grip=[8,5.5,12.5]
            # The source beam is aligned along +Z, not +Y. With X=0 it stays forward.
            beam=m['elements'][0]
            assert beam['to'][2]-beam['from'][2]>=14
            # Scale about blockmodel origin8; compensate offset at authored grip.
            pivot=[(grip[i]-8)*.55+m['display']['thirdperson_righthand']['translation'][i] for i in range(3)]
            assert max(abs(t) for t in pivot)<1e-6,(name,pivot)
            checked+=1
    axe=(ROOT/'src/main/java/org/slavicmyths/item/ThunderAxeItem.java').read_text(encoding='utf-8')
    for required in ('THUNDER_CHANCE = 0.2F','BONUS_DAMAGE = 1.0F','THUNDER_COOLDOWN = 60','event.setNewDamage(event.getNewDamage() + BONUS_DAMAGE)','EntityType.LIGHTNING_BOLT.create(world)','bolt.setVisualOnly(true)','world.addFreshEntity(bolt)'):
        assert required in axe,required
    assert 'target.hurt(' not in axe and 'setWeatherParameters(' not in axe and 'setVisualOnly(false)' not in axe
    sabre=(ROOT/'src/main/java/org/slavicmyths/rewards/RewardEvents.java').read_text(encoding='utf-8')
    sabre_item=(ROOT/'src/main/java/org/slavicmyths/rewards/RewardContent.java').read_text(encoding='utf-8')
    assert 'public static void sabreWave(ServerPlayer p)' in sabre
    assert 'p.getBoundingBox().inflate(5.5)' in sabre and 'hits>=6' in sabre
    assert 'PLAYER_ATTACK_SWEEP' in sabre and 'ParticleTypes.SWEEP_ATTACK' in sabre
    assert 'RewardEvents.sabreWave(server)' in sabre_item and 'player.swing(hand,true)' in sabre_item
    assert 'sweep(p,e.getEntity()' not in sabre and 'MomentumAt' not in sabre
    if jar:
        jars=list((ROOT/'build/libs').glob('slavicmyths-*.jar'))
        assert len(jars)==1
        with zipfile.ZipFile(jars[0]) as z:
            for id in IDS:
                for suffix in STATES:
                    path='assets/slavicmyths/models/item/'+id+suffix+'.json'
                    assert hashlib.sha256(z.read(path)).hexdigest()==hashlib.sha256((ROOT/'src/main/resources'/path).read_bytes()).hexdigest(),path
            for name in ('item/ThunderAxeItem.class','rewards/RewardEvents.class'):
                assert 'org/slavicmyths/'+name in z.namelist(),name
    print(f'PASS owner-feedback gates: {checked} real crossbow states, both hands all 12 Z-axis horizontal and grip-centered, +1HP visual-only lightning, active right-click sabre wave. JAR',jar,'Minecraft NOT_RUN')
def render():
    import weapons87_geometry as g
    from PIL import Image,ImageDraw
    output=DOCS/'ci-source-views'
    output.mkdir(parents=True,exist_ok=True)
    for id in IDS:
        file='src/main/resources/assets/slavicmyths/models/item/'+id+'_arrow.json'
        d=load_model(file)
        old=copy.deepcopy(d)
        for hand,side in [('right','righthand'),('left','lefthand')]:
            old['display']['thirdperson_'+side]=CROSSBOWS[file]['oldTP'][hand]
        board=Image.new('RGB',(1480,700),'#253039')
        draw=ImageDraw.Draw(board)
        draw.text((12,12),id+' loaded / actual native JSON and UV / identical geometry, same camera, NOT Minecraft',fill='white')
        for row,(label,model) in enumerate((('OWNER-REJECTED BEFORE',old),('AFTER FIX',d))):
            faces=g.faces(model)
            transform=model['display']['thirdperson_righthand']
            for col,(cam,ang) in enumerate((('FRONT',(0,0,0)),('PROFILE',(0,90,0)),('OBLIQUE',(12,35,0)))):
                angles=[transform['rotation'][i]+ang[i] for i in range(3)]
                image=g.project(faces,220,36,angles,transform['scale'],transform['translation'])
                # native item transform view vs authored orthographic view
                x=col*245+12;y=row*315+55
                board.paste(image,(x,y),image)
                draw.text((x,y-21),label+' '+cam+' / native right hand',fill='white')
            for col,side in enumerate(('righthand','lefthand')):
                td=model['display']['thirdperson_'+side]
                draw.text((810,100+row*315+col*55),side+' X tilt='+str(td['rotation'][0])+' deg, T='+str(td['translation']),fill='white')
        draw.text((12,678),'Actual models keep original cuboids/UV/PNG plus the approved P42 art details. Third-person owner client angle/arms: NOT_RUN; manually inspect Steve/Alex.',fill='white')
        board.save(output/(id+'-loaded-thirdperson-before-after.png'))
if __name__=='__main__':
    p=argparse.ArgumentParser()
    p.add_argument('--jar',action='store_true')
    p.add_argument('--render',action='store_true')
    args=p.parse_args()
    check(args.jar)
    if args.render:render()
