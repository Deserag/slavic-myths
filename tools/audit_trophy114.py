"""Actual catalog/model/texture provenance and offline slot sheets, no game launch."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,hashlib,zipfile,io

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'docs/implementation/trophy114'
A=ROOT/'src/main/resources/assets/slavicmyths'
SHA=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()

def main():
    import trophy111_source_preview as preview
    source=(ROOT/'tools/trophy111_source_preview.py').read_text(encoding='utf-8')
    source=source.replace('colors[selection]=tex[ty,tx]', "face_tex=np.array(MATS[face['texture'].lstrip('#')].convert('RGBA'));fh,fw=face_tex.shape[:2];fx=np.clip(((a+u*(c-a))*fw/16).astype(int),0,fw-1);fy=np.clip(((b+v*(d-b))*fh/16).astype(int),0,fh-1);colors[selection]=face_tex[fy,fx]")
    ns={'__name__':'offline_trophy_audit','__file__':str(ROOT/'tools/trophy111_source_preview.py')}
    exec(compile(source,'offline_trophy_audit','exec'),ns)
    rows=json.loads((OUT/'catalog.json').read_text());assert len(rows)==64 and len({x[0] for x in rows})==64
    ids31=json.loads((OUT/'ids.json').read_text());records=[]
    board=Image.new('RGBA',(512,512),'#30343b');label=ImageDraw.Draw(board)
    strips={}
    for index,(name,tab,group) in enumerate(rows):
        path=A/'models/item'/(name+'.json');model=json.loads(path.read_text());deps={str(path.relative_to(ROOT)):SHA(path)}
        if model.get('loader')=='neoforge:separate_transforms':model=model['perspectives']['gui']
        if model.get('parent')=='builtin/entity':
            assert name=='heart_of_eternal_cold';path=A/'models/item'/f'{name}_icon.json';model=json.loads(path.read_text());deps[str(path.relative_to(ROOT))]=SHA(path)
        if model.get('parent','').startswith('slavicmyths:'):
            path=A/'models'/(model['parent'].split(':')[1]+'.json');model=json.loads(path.read_text());deps[str(path.relative_to(ROOT))]=SHA(path)
        if model.get('parent')=='minecraft:item/generated':
            ref=model['textures']['layer0'];assert ref.startswith('slavicmyths:');path=A/'textures'/(ref.split(':')[1]+'.png');deps[str(path.relative_to(ROOT))]=SHA(path);pic=Image.open(path).convert('RGBA').resize((16,16),Image.Resampling.NEAREST)
        else:
            mats={}
            for key,ref in model['textures'].items():
                if ref.startswith('#'):continue
                namespace,relative=ref.split(':')
                if namespace=='slavicmyths':
                    path=A/'textures'/(relative+'.png');mats[key]=Image.open(path).convert('RGBA');deps[str(path.relative_to(ROOT))]=SHA(path)
                else:
                    with zipfile.ZipFile(preview.CLIENT) as z:mats[key]=Image.open(io.BytesIO(z.read('assets/minecraft/textures/'+relative+'.png'))).convert('RGBA')
            ns['MATS']=mats;pic,_=ns['render'](model,next(iter(mats.values())),'gui',16)
        assert pic.getbbox(),name
        x=index%8*64;y=index//8*64;board.paste(pic,(x,y),pic);board.paste(pic.resize((48,48),Image.Resampling.NEAREST),(x+16,y+16),pic.resize((48,48),Image.Resampling.NEAREST))
        strips[name]=pic
        records.append({'id':name,'catalog_index':index,'group':group,'dependencies_sha256':deps,'action':'FIX' if name=='firebird_down' else 'KEEP' if name in ids31 else 'PREVIOUS_PACKAGE','render':'offline actual PNG or actual JSON/UV; no runtime'})
    board.save(OUT/'catalog64-unlabelled.png')
    for start in range(0,31,8):
        names=ids31[start:start+8];sheet=Image.new('RGBA',(len(names)*64,128),'#30343b')
        for i,name in enumerate(names):
            p=strips[name];sheet.paste(p,(i*64,0),p);q=p.resize((48,48),Image.Resampling.NEAREST);sheet.paste(q,(i*64,24),q)
        sheet.save(OUT/f'QA-{start//8+1}-current-unlabelled.png')
    (OUT/'catalog64-audit.json').write_text(json.dumps(records,indent=2),encoding='utf-8')
    print('64 actual item references/slot renders PASS; 17ordinary/16elite/31legendary; source-only.')

if __name__=='__main__':main()
