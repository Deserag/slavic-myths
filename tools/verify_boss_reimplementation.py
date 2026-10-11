"""Offline resource/model contracts. These do not assert in-game visual approval."""
from pathlib import Path
import hashlib,json,zipfile
from PIL import Image,ImageDraw
import math
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'docs/verification/boss-reimplementation';OUT.mkdir(parents=True,exist_ok=True)

def check(condition,message):
    if not condition:raise AssertionError(message)

def rotate(v,r):
    x,y,z=v
    a,b,c=r
    y,z=y*math.cos(a)-z*math.sin(a),y*math.sin(a)+z*math.cos(a)
    x,z=x*math.cos(b)+z*math.sin(b),-x*math.sin(b)+z*math.cos(b)
    x,y=x*math.cos(c)-y*math.sin(c),x*math.sin(c)+y*math.cos(c)
    return x,y,z

def main():
    res=ROOT/'src/main/resources';assets=res/'assets/slavicmyths'
    for p in res.rglob('*.json'):json.loads(p.read_text(encoding='utf-8'))
    for p in assets.rglob('*.png'):
        with Image.open(p) as im:im.verify()
    models=[]
    for name in ['tugarin_phase1','tugarin_phase2','nightingale_phase1','nightingale_phase2']:
        model=json.loads((assets/f'boss_geometry/{name}.json').read_text());parts={p['name']:p for p in model['parts']}
        check(len(parts)==len(model['parts']),name+' duplicate parts')
        for p in parts.values():
            check(not p['parent'] or p['parent'] in parts,name+' missing parent')
            if p['box']:check(all(x>0 for x in p['box'][3:]),name+' invalid cube')
        with Image.open(assets/f'textures/entity/{name}.png') as im:check(im.size==(512,512),name+' atlas')
        models.append(model)
    check(models[0]['parts']!=models[1]['parts'] and models[2]['parts']!=models[3]['parts'],'recolored geometry')
    fat={p['name']:p for p in models[0]['parts']};bands=[fat['bellyBand'+str(i)]['box'] for i in range(7)]
    check(max(b[3] for b in bands)==20 and max(b[5] for b in bands)==13 and min(b[2] for b in bands)<=-8,'fat belly dimensions/forward offset')
    check(bands[0][3]<bands[3][3] and bands[6][3]<bands[3][3],'belly must have rounded stepped outline')
    dragon={p['name']:p for p in models[1]['parts']}
    for n in ['jaw','snout','wingL','wingR','wingLowerJointL','wingLowerJointR','tail0','tail1','tail2','tail3','clawL0','fangR0']:check(n in dragon,'dragon required part '+n)
    comic={p['name']:p for p in models[3]['parts']}
    for n in ['topknot','moustacheL','moustacheR','bladeL','bladeR']:check(n in comic,'comic required part '+n)
    check(comic['torso']['box'][3]<fat['chest']['box'][3]/2,'silhouettes insufficiently different')
    egg=json.loads((assets/'models/item/nightingale_spawn_egg.json').read_text());check(egg['parent']=='minecraft:item/template_spawn_egg','egg model')
    for lang in ['ru_ru','en_us']:
        data=json.loads((assets/f'lang/{lang}.json').read_text(encoding='utf-8'));check(bool(data.get('item.slavicmyths.nightingale_spawn_egg')),'egg translation');check(bool(data.get('entity.slavicmyths.nightingale.enraged')),'phase translation')
    sounds=json.loads((assets/'sounds.json').read_text());profiles=json.loads((ROOT/'tools/boss_reimplementation_sounds.json').read_text())
    code='\n'.join(p.read_text(encoding='utf-8') for p in (ROOT/'src/main/java').rglob('*.java') if p.name!='ModSounds.java')
    for key,source in profiles.items():
        check(key in sounds,'missing sound '+key);check('"'+key+'"' in code,'unhooked sound '+key)
        if source.startswith('slavicmyths:'):check(source.split(':')[1] in sounds,'missing sound alias '+source)
    dagger=(ROOT/'src/main/java/org/slavicmyths/bandit/NightingaleDagger.java').read_text();check('UseAnim.NONE' in dagger and 'UseAnim.BOW' not in dagger,'dagger use animation')
    check('NIGHTINGALE_SPAWN_EGG' in (ROOT/'src/main/java/org/slavicmyths/registry/ModItems.java').read_text(),'missing egg registry')
    # Draw authored cuboids in physical proportions, using every pivot and Euler rotation.
    from boss_reimplementation_resources import PALETTE
    im=Image.new('RGB',(1100,1050),(23,27,35));draw=ImageDraw.Draw(im)
    faces=[(0,1,3,2),(4,6,7,5),(0,4,5,1),(2,3,7,6),(0,2,6,4),(1,5,7,3)]
    for index,m in enumerate(models):
        parts={p['name']:p for p in m['parts']};scale=[1,1,.84,.72][index]
        def world(v,p):
            v=rotate(v,p['rotation']);v=tuple(v[i]+p['pivot'][i] for i in range(3))
            return world(v,parts[p['parent']]) if p['parent'] else v
        for column,angle in enumerate([0,.65]):
            polygons=[];cx=column*550+275;cy=index*250+185
            for p in m['parts']:
                if not p['box'] or p['name']=='mouthGlow':continue
                x,y,z,w,h,d=p['box'];points=[]
                for dx,dy,dz in [(0,0,0),(w,0,0),(0,h,0),(w,h,0),(0,0,d),(w,0,d),(0,h,d),(w,h,d)]:
                    a,b,c=world((x+dx,y+dy,z+dz),p);a,c=a*math.cos(angle)+c*math.sin(angle),-a*math.sin(angle)+c*math.cos(angle);points.append((cx+a*3.6*scale,cy+(b-24)*3.6*scale-c*.3*scale,c))
                for face in faces:
                    vertices=[points[n] for n in face];depth=sum(v[2] for v in vertices)/4
                    polygons.append((depth,[(v[0],v[1]) for v in vertices],PALETTE[p['material']]))
            for depth,poly,color in sorted(polygons,key=lambda a:-a[0]):draw.polygon(poly,fill=color,outline=tuple(max(0,c-25) for c in color))
            draw.text((column*550+20,index*250+10),m['name']+(' FRONT' if column==0 else ' THREE QUARTER'),fill='white')
    draw.text((20,1020),'Authored geometry preview. No Minecraft/client launch; in-game appearance remains a manual check.',fill='white')
    im.save(OUT/'geometry-preview.png')
    jars=list((ROOT/'build/libs').glob('slavicmyths-*.jar'))
    if jars:
        jar=max(jars,key=lambda p:p.stat().st_mtime)
        for p in (ROOT/'src/main/java').rglob('*.java'):
            check(p.stat().st_mtime_ns<=jar.stat().st_mtime_ns,'Java source changed after JAR build: '+str(p))
        with zipfile.ZipFile(jar) as z:
            check(not any('GameTests' in n or '/verify/' in n for n in z.namelist()),'test classes in production JAR')
            for p in (ROOT/'build/classes/java/main').rglob('*.class'):
                check(z.read(p.relative_to(ROOT/'build/classes/java/main').as_posix())==p.read_bytes(),'stale production class '+str(p))
            for p in res.rglob('*'):
                if p.is_file() and p.suffix in ['.json','.png','.ogg']:check(z.read(p.relative_to(res).as_posix())==p.read_bytes(),'stale production resource '+str(p))
    report={'resource_check':'PASS','models':[{'name':m['name'],'parts':len(m['parts'])} for m in models],'required_sounds':len(profiles),'client_launches':0,'visual_approval':'MANUAL_REQUIRED'}
    (OUT/'resource-check.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report))
if __name__=='__main__':main()
