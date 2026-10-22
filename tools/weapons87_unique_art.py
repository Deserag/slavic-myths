"""#184/#185 source drafts -> scoped production assets; non-game source gates only."""
from pathlib import Path
import argparse,base64,hashlib,json,struct,zipfile
ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/slavicmyths'
DOCS=ROOT/'docs/implementation/weapons87/blueprints/uniques-p20-p25'
IDS=('trophy_kalin_sabre','victor_spear','pool_spear')
TEXTURE='textures/item/kalin_sabre_87.png'
PNG=base64.b64decode('iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAANklEQVR4nGOQVtb5H5SY/f/8PJf/Lw6V/J8iq/Tf01L3f2Fj138GIgATMYpGDRg1YNSAoWAAAIifD205yZqpAAAAAElFTkSuQmCC')
ASSET_PATHS=[f'models/item/{id}.json' for id in IDS]+[TEXTURE]
def load(id,kind):
    return json.loads((DOCS/f'{kind}-{id}.json').read_text(encoding='utf-8'))
def apply():
    for id in IDS:
        dest=ASSETS/'models/item'/f'{id}.json'
        dest.write_text(json.dumps(load(id,'draft'),ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    texture=ASSETS/TEXTURE
    texture.parent.mkdir(parents=True,exist_ok=True)
    texture.write_bytes(PNG)
    print('APPLIED #184/#185 two item models and isolated 16px palette only')
def check(jar=False):
    assert PNG[:8]==b'\x89PNG\r\n\x1a\n' and struct.unpack('>II',PNG[16:24])==(16,16)
    assert hashlib.sha256(PNG).hexdigest()=='c8f47ca2b0f4954b6f97f2a926d30536f2b75a0ebcba9d83a571a02ea868df22'
    assert (ASSETS/TEXTURE).read_bytes()==PNG
    for id in IDS:
        before=load(id,'before');after=load(id,'draft')
        production=json.loads((ASSETS/'models/item'/f'{id}.json').read_text(encoding='utf-8'))
        assert production==after,id+' production changed from frozen technical draft'
        assert (before['elements']==after['elements']) if id=='pool_spear' else (before['elements']!=after['elements'])
        assert {k:v for k,v in before.items() if k not in ('elements','textures','gui_light','display')}=={k:v for k,v in after.items() if k not in ('elements','textures','gui_light','display')}
        assert {k:v for k,v in before['display'].items() if k!='gui'}=={k:v for k,v in after['display'].items() if k!='gui'}
        assert after.get('gui_light')=='front'
        assert set(before['textures'])==set(after['textures'])
        for elem in after['elements']:
            assert all(-16<=lo<hi<=32 for lo,hi in zip(elem['from'],elem['to']))
            for face in elem['faces'].values():assert face['texture']=='#surface' and all(0<=v<=16 for v in face['uv'])
        if id=='trophy_kalin_sabre':
            assert len(before['elements'])==6 and len(after['elements'])==34
            assert after['textures']['surface']=='slavicmyths:item/kalin_sabre_87'
            assert max(e['to'][0] for e in after['elements'])>=12.7
        elif id=='victor_spear':
            assert len(before['elements'])==4 and len(after['elements'])==14
            assert before['textures']==after['textures']
        else:
            assert len(before['elements'])==len(after['elements'])==14
            assert before['textures']==after['textures']
            assert before['display']['gui']!=after['display']['gui']
    if jar:
        jars=list((ROOT/'build/libs').glob('slavicmyths-*.jar'))
        assert len(jars)==1
        with zipfile.ZipFile(jars[0]) as z:
            for path in ASSET_PATHS:assert z.read('assets/slavicmyths/'+path)==(ASSETS/path).read_bytes()
    print('PASS #184/#185: frozen-source art changes only, 4 held contexts each KEEP, 16px PNG, runtime NOT_RUN, jar',jar)
def render():
    import weapons87_geometry as g
    from PIL import Image,ImageDraw
    out=DOCS/'ci-source-views'
    out.mkdir(exist_ok=True)
    for id in IDS:
        board=Image.new('RGB',(1500,750),'#253039');draw=ImageDraw.Draw(board)
        draw.text((10,10),id+' BEFORE and AFTER actual source cuboids / UV / 16px GUI, no game',fill='white')
        for row,kind in enumerate(('before','draft')):
            model=load(id,kind) if kind=='before' else json.loads((ASSETS/'models/item'/f'{id}.json').read_text())
            faces=g.faces(model)
            for col,angle in enumerate([(0,0,0),(0,90,0),(15,35,0)]):
                im=g.project(faces,245,42,angle,translation=(0,-5,0))
                x=col*255;y=40+row*325
                board.paste(im,(x,y),im)
                draw.text((x+4,y+3),kind.upper()+' '+('FRONT','PROFILE','3/4')[col],fill='white')
            d=model['display']['gui']
            icon=g.project(faces,16,16,d.get('rotation',[0,0,0]),d.get('scale',[1,1,1]),d.get('translation',[0,0,0]))
            x=875;y=85+row*325
            board.paste(icon,(x,y),icon)
            board.paste(icon.resize((128,128),Image.Resampling.NEAREST),(x+45,y))
            draw.text((x,y-22),kind+' GUI1x & 8x',fill='white')
        draw.text((15,715),'Fixed span42, actual JSON/texture; same camera. Owner runtime FP/TP/Steve/Alex NOT_RUN.',fill='white')
        board.save(out/(id+'-before-after.png'))
if __name__=='__main__':
    p=argparse.ArgumentParser()
    for key in ('apply','check','jar','render'):p.add_argument('--'+key,action='store_true')
    args=p.parse_args()
    if args.apply:apply()
    if args.check or args.jar:check(args.jar)
    if args.render:render()
