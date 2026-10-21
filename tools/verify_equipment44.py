"""Offline resource/enchantment graph and production parity; never a gameplay test."""
from pathlib import Path
import json,zipfile,hashlib,struct,tomllib,subprocess
from PIL import Image
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/rune-glow-fix-44'
RES=R/'src/main/resources'
vanilla=zipfile.ZipFile('C:/Users/pavel/.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar')
def tag(key,chain=()):
    assert key not in chain,key
    ns,name=key.split(':');path=f'data/{ns}/tags/item/{name}.json'
    values=[]
    if path in vanilla.namelist():values=json.loads(vanilla.read(path))['values']
    p=RES/path
    if p.exists():
        local=json.loads(p.read_text());assert not local.get('replace',False),path
        values+=local['values']
    out=set()
    for value in values:
        if isinstance(value,dict):value=value['id']
        out.update(tag(value[1:],chain+(key,)) if value.startswith('#') else [value])
    return out
def eligible(item,enchantment):
    data=json.loads(vanilla.read('data/minecraft/enchantment/'+enchantment+'.json'))
    key=data['supported_items']
    return item in tag(key[1:]) if isinstance(key,str) and key.startswith('#') else item in key
cases=[]
for slot in ['head','chest','leg','foot']:
    members=tag('minecraft:enchantable/'+slot+'_armor')
    for item in sorted(members):
        if not item.startswith('slavicmyths:'):continue
        assert eligible(item,'protection') and eligible(item,'unbreaking') and eligible(item,'mending'),item
        if slot=='foot':assert eligible(item,'feather_falling') and not eligible(item,'respiration'),item
        if slot=='head':assert eligible(item,'respiration') and not eligible(item,'feather_falling'),item
        cases.append({'item':item,'slot':slot,'protection_unbreaking_mending':'PASS_DATA_GRAPH'})
assert len(cases)==22,len(cases)
for item,enchant in [('perunite_sword','sharpness'),('silver_sword','sharpness'),('perunite_pickaxe','efficiency'),('perunite_pickaxe','fortune'),('serpent_crossbow','quick_charge'),('three_breath_crossbow','piercing'),('ward_shield','unbreaking')]:
    assert eligible('slavicmyths:'+item,enchant),(item,enchant)
for p in (RES/'data/minecraft/tags/item/enchantable').glob('*.json'):
    name=p.relative_to(R).as_posix()
    old=subprocess.run(['git','show','HEAD:'+name],cwd=R,capture_output=True)
    if old.returncode==0:
        assert set(json.loads(old.stdout)['values'])<=set(json.loads(p.read_text())['values']),name
jar=R/'build/libs/slavicmyths-1.9.7.44.jar'
counts={'resources':0,'png':0,'classes':0}
with zipfile.ZipFile(jar) as z:
    assert z.testzip() is None
    meta=tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode())
    assert meta['mods'][0]['version']=='1.9.7.44'
    assert any(m['config']=='slavicmyths.rune_glow.mixins.json' for m in meta['mixins'])
    for p in RES.rglob('*'):
        if not p.is_file():continue
        name=p.relative_to(RES).as_posix()
        expected=(R/'build/resources/main'/name).read_bytes()
        if p.suffix!='.toml':assert expected==p.read_bytes(),('source drift',name)
        assert z.read(name)==expected,name
        counts['resources']+=1
        if p.suffix=='.json':json.loads(p.read_text(encoding='utf-8'))
        if p.suffix=='.png':
            with Image.open(p) as im:im.verify()
            counts['png']+=1
    for p in (R/'build/classes/java/main').rglob('*.class'):
        data=p.read_bytes();assert struct.unpack('>H',data[6:8])[0]==65
        assert z.read(p.relative_to(R/'build/classes/java/main').as_posix())==data
        counts['classes']+=1
    assert not any('RuneGlow104Checks' in n or 'Armor88LinenMeshes' in n or 'smoke/' in n for n in z.namelist())
out={'status':'PASS_OFFLINE_NOT_RUNTIME','jar':str(jar),'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'counts':counts,'armor_enchantment_cases':cases,'minecraft_launches':0,'runtime':'NOT_RUN','polymc':'NOT_INSTALLED_44'}
(O/'verification.json').write_text(json.dumps(out,indent=2)+'\n')
print(json.dumps({k:v for k,v in out.items() if k!='armor_enchantment_cases'}))
