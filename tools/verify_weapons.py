"""Focused, static 0.4.7 weapon-resource checks; does not launch Minecraft."""
from pathlib import Path
import json, re, struct, zipfile, zlib

ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/main/resources'
ASSETS=RES/'assets/slavicmyths'
NAMES='silver_dagger silver_sword perunite_sword bone_knife ritual_knife spear silver_spear thunder_spear battle_axe thunder_axe club mace perunite_mace berdysh carved_staff storm_staff retainer_shield perunite_shield weapon_wrap silver_fitting'.split()

def read(p):return json.loads(p.read_text(encoding='utf-8'))

registered=set(re.findall(r'\.register\(\s*"([a-z_]+)"',(ROOT/'src/main/java/org/slavicmyths/registry/ModItems.java').read_text(encoding='utf-8')))
VERSION=re.search(r"version = '([^']+)'",(ROOT/'build.gradle').read_text()).group(1)
models={}
for name in NAMES:
    assert name in registered,name
    p=ASSETS/f'models/item/{name}.json';m=read(p);models[name]=m
    assert m.get('elements'),name
    for hand in ('firstperson_righthand','firstperson_lefthand','thirdperson_righthand','thirdperson_lefthand'):
        assert hand in m['display'],(name,hand)
        assert all(0 < s <= 4 for s in m['display'][hand]['scale'])
    for e in m['elements']:
        assert all(-16 <= c <= 32 for c in e['from']+e['to']),(name,e)
        assert all(a < b for a,b in zip(e['from'],e['to'])),(name,e)
        if 'rotation' in e:assert e['rotation']['angle'] in (-45,-22.5,0,22.5,45)
        for f in e['faces'].values():
            assert f['texture'][1:] in m['textures']
            assert all(0 <= c <= 16 for c in f['uv'])
    for ref in m['textures'].values():assert (ASSETS/('textures/'+ref.split(':')[1]+'.png')).is_file(),ref
    for lang in ('ru_ru','en_us'):assert f'item.slavicmyths.{name}' in read(ASSETS/f'lang/{lang}.json')
    data=(ASSETS/f'textures/item/{name}.png').read_bytes();assert data[:8]==b'\x89PNG\r\n\x1a\n'
    off=8;raw=b''
    while off<len(data):
        n=struct.unpack('!I',data[off:off+4])[0];kind=data[off+4:off+8];body=data[off+8:off+8+n]
        assert zlib.crc32(kind+body)&0xffffffff==struct.unpack('!I',data[off+8+n:off+12+n])[0]
        if kind==b'IHDR':assert struct.unpack('!IIBBBBB',body)==(32,32,8,6,0,0,0)
        if kind==b'IDAT':raw+=body
        off+=n+12
    assert len(zlib.decompress(raw))==32*(1+32*4)

def length(name):
    e=models[name]['elements']
    return (max(p['to'][1] for p in e)-min(p['from'][1] for p in e))*models[name]['display']['thirdperson_righthand']['scale'][1]

ratio=length('silver_sword')/length('silver_dagger');assert 1.6 <= ratio <= 1.9,ratio
for name in ('spear','silver_spear','thunder_spear','carved_staff','storm_staff','berdysh'):
    assert length(name)>1.65*length('silver_sword'),name
shapes=[]
for name in NAMES:
    shape=json.dumps([(e['from'],e['to'],e.get('rotation')) for e in models[name]['elements']],sort_keys=True)
    assert shape not in shapes,('identical geometry',name)
    shapes.append(shape)
for name in ('retainer_shield','perunite_shield'):
    m=models[name];override=m['overrides'][0];assert override['predicate']=={'blocking':1}
    blocking=read(ASSETS/('models/'+override['model'].split(':')[1]+'.json'))
    assert 'overrides' not in blocking
    parent=read(ASSETS/('models/'+blocking['parent'].split(':')[1]+'.json'));assert parent['elements']==m['elements']
    for hand in ('firstperson_righthand','firstperson_lefthand','thirdperson_righthand','thirdperson_lefthand'):
        assert blocking['display'][hand]!=m['display'][hand]
for name in ('spear','silver_spear','mace','berdysh','weapon_wrap','silver_fitting'):
    r=read(RES/f'data/slavicmyths/recipes/{name}.json')
    assert set(''.join(r['pattern']))-{' '}==set(r['key'])
    for ing in r['key'].values():
        if ing['item'].startswith('slavicmyths:'):assert ing['item'].split(':')[1] in registered
    assert r['result']['item']=='slavicmyths:'+name
with zipfile.ZipFile(ROOT/f'build/libs/slavicmyths-{VERSION}.jar') as jar:
    for name in NAMES:
        for prefix,suffix in (('models','.json'),('textures','.png')):
            rel=f'assets/slavicmyths/{prefix}/item/{name}{suffix}'
            assert jar.read(rel)==(RES/rel).read_bytes(),rel
    assert b'func_78016_d' in jar.read('org/slavicmyths/registry/ModItemGroup$1.class')
    shield=jar.read('org/slavicmyths/item/MythShieldItem.class')
    assert b'isShield' in shield and struct.unpack('!H',shield[6:8])[0]==52
    assert ('version="'+VERSION+'"').encode() in jar.read('META-INF/mods.toml')
print('PASS: 20 item resources, unique geometry, sword/dagger ratio %.2f, long-weapon scale, shield overrides, recipes, packaged resources and reobf Java 8 JAR.'%ratio)
print('Static validation only; Minecraft was not launched.')
