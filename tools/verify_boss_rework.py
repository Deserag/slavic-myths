"""Current production/resource parity and measured neutral silhouettes; no runtime claim."""
from pathlib import Path
import itertools, json, math, re, hashlib, zipfile, argparse
from PIL import Image, ImageDraw
from boss_rework_names import boss_key
from boss_reimplementation_resources import PALETTE

ROOT=Path(__file__).resolve().parents[1]
parser=argparse.ArgumentParser()
parser.add_argument('--build-dir',default='build')
parser.add_argument('--jar')
parser.add_argument('--output-dir',default='docs/verification/boss-rework-2026-10-09')
args=parser.parse_args()
BUILD=ROOT/args.build_dir
RES=ROOT/'src/main/resources'; A=RES/'assets/slavicmyths'
OUT=ROOT/args.output_dir; OUT.mkdir(parents=True,exist_ok=True)
checks=0
def check(ok,msg):
    global checks
    checks+=1
    if not ok:raise AssertionError(msg)
def rotate(v,r):
    x,y,z=v;a,b,c=r
    y,z=y*math.cos(a)-z*math.sin(a),y*math.sin(a)+z*math.cos(a)
    x,z=x*math.cos(b)+z*math.sin(b),-x*math.sin(b)+z*math.cos(b)
    x,y=x*math.cos(c)-y*math.sin(c),x*math.sin(c)+y*math.cos(c)
    return [x,y,z]
def geometry(model):
    parts={p['name']:p for p in model['parts']}; result=[]
    def world(v,p):
        v=[a+b for a,b in zip(rotate(v,p['rotation']),p['pivot'])]
        return world(v,parts[p['parent']]) if p['parent'] else v
    for p in parts.values():
        check(not p['parent'] or p['parent'] in parts,'Missing parent')
        if p['box']:
            x,y,z,w,h,d=p['box'];check(min(w,h,d)>0,'Empty cube')
            vertices=[world(v,p) for v in itertools.product((x,x+w),(y,y+h),(z,z+d))]
            result.append((p['name'],vertices,p['material']))
    return result

for p in RES.rglob('*.json'):json.loads(p.read_text(encoding='utf-8'));check(True,'JSON')
for p in RES.rglob('*.png'):
    with Image.open(p) as im:im.verify()
    check(True,'PNG')
for p in RES.rglob('*'):
    relative=p.relative_to(RES).as_posix()
    if p.is_file() and relative.startswith(('assets/','data/')):check(relative==relative.lower(),'Invalid uppercase resource path: '+str(p))
sounds=json.loads((A/'sounds.json').read_text(encoding='utf-8'))
for event,definition in sounds.items():
    for entry in definition['sounds']:
        entry={'name':entry} if isinstance(entry,str) else entry
        namespace,_,path=entry['name'].partition(':')
        if not path:namespace,path='slavicmyths',namespace
        if namespace=='slavicmyths':
            if entry.get('type')=='event':check(path in sounds,'Missing sound alias '+path)
            else:
                ogg=A/f'sounds/{path}.ogg';check(ogg.exists(),'Missing sound '+str(ogg))
                check(ogg.read_bytes()[:4]==b'OggS','Invalid OGG '+str(ogg))
for locale in ['ru_ru','en_us']:
    data=json.loads((A/f'lang/{locale}.json').read_text(encoding='utf-8'))
    expected='Проклятый Оборотень' if locale=='ru_ru' else 'Cursed Werewolf'
    check(data['entity.slavicmyths.cursed_volkolak']==expected,'Boss name')
    check(data['entity.slavicmyths.volkolak']==('Волколак' if locale=='ru_ru' else 'Volkolak'),'Legacy mob name')
    for key,value in data.items():
        if boss_key(key):check('Волколак' not in value and 'Volkolak' not in value,'Wrong cursed boss name '+key)
    source=json.loads((ROOT/'tools/volkolak_143_text.json').read_text(encoding='utf-8'))[locale]
    for key,value in source.items():check(data[key]==value,'Generator localization parity '+key)

sheet=Image.new('RGB',(1300,1100),'#252C37');draw=ImageDraw.Draw(sheet);stats={}
faces=[(0,1,3,2),(4,5,7,6),(0,1,5,4),(2,3,7,6),(0,2,6,4),(1,3,7,5)]
for row,name in enumerate(['tugarin_phase1','tugarin_phase2','nightingale_phase1','nightingale_phase2']):
    model=json.loads((A/f'boss_geometry/{name}.json').read_text());cubes=geometry(model)
    allpoints=[v for _,vs,_ in cubes for v in vs]
    miny=min(v[1] for v in allpoints);minx=min(v[0] for v in allpoints);maxx=max(v[0] for v in allpoints)
    stats[name]={'height':(24-miny)/16,'width':(maxx-minx)/16}
    if name.startswith('nightingale'):
        hairless=[v for n,vs,_ in cubes if not n.startswith('topknot') for v in vs]
        stats[name]['height_without_hair']=(24-min(v[1] for v in hairless))/16
        check(stats[name]['height_without_hair']<1.8,'Nightingale body too tall')
        check(1.75<stats[name]['height']<2.05,'Nightingale total height')
    with Image.open(A/f'textures/entity/{name}.png') as im:check(im.size==(1024,1024),'Boss atlas')
    for col,angle in enumerate([0,math.pi/2,math.pi/4]):
        ox=col*430+215;oy=row*270+237;zoom=2.8 if name.startswith('tugarin') else 6
        polys=[]
        for _,vs,mat in cubes:
            rotated=[rotate(v,(0,angle,0)) for v in vs]
            for face in faces:
                pts=[rotated[i] for i in face];depth=sum(v[2] for v in pts)/4
                shade=.8 if face in faces[:2] else .68 if face in faces[2:4] else 1
                color=tuple(int(c*shade) for c in PALETTE[mat])
                polys.append((depth,[(ox+v[0]*zoom,oy+(v[1]-24)*zoom) for v in pts],color))
        draw.line((col*430+20,oy,col*430+410,oy),fill='#657084')
        for _,poly,color in sorted(polys,reverse=True):draw.polygon(poly,fill=color)
        # Player-height marker, same units as the model.
        draw.rectangle((col*430+30,oy-28.8*zoom,col*430+39,oy),fill='#4299AD')
        draw.text((col*430+12,row*270+8),name+[' FRONT',' SIDE',' 3/4'][col],fill='white')
draw.text((12,1080),'Offline authored geometry; cyan marker = 1.8 block player. In-game QA recorded separately.',fill='white')
sheet.save(OUT/'silhouettes.png')
check(4.7<stats['tugarin_phase1']['height']<5.2,'Tugarin height')
version=re.search(r'^mod_version=(.+)$',(ROOT/'gradle.properties').read_text(),re.M).group(1)
jar=ROOT/args.jar if args.jar else BUILD/f'libs/slavicmyths-{version}.jar'
with zipfile.ZipFile(jar) as z:
    names=set(z.namelist());check(z.testzip() is None,'JAR CRC')
    check(not any('/verify/' in n or '/smoke/' in n or 'GameTests' in n or 'BossSceneProbe' in n for n in names),'Fixture leak')
    for p in RES.rglob('*'):
        if p.is_file():
            n=p.relative_to(RES).as_posix();check(n in names,'Missing resource '+n)
            check(n=='META-INF/neoforge.mods.toml' or z.read(n)==p.read_bytes(),'Stale production resource '+n)
    for p in (BUILD/'classes/java/main').rglob('*.class'):
        n=p.relative_to(BUILD/'classes/java/main').as_posix();check(z.read(n)==p.read_bytes(),'Stale class '+n)
        check(int.from_bytes(z.read(n)[6:8],'big')==65,'Java 21')
report={'status':'PASS','checks':checks,'silhouettes':stats,'jar':str(jar),
        'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'runtime':'See REPORT.md; offline checks are not gameplay acceptance.'}
(OUT/'resources.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps(report,ensure_ascii=False))
