"""#173: extend the existing two stone archetypes to their six material variants.

Default produces before/design sheets without changing production. --apply installs
the exact draft models. Existing 14 inventory sprites remain authoritative.
"""
from pathlib import Path
import argparse,copy,hashlib,json
from PIL import Image,ImageDraw
import numpy as np
import weapons87_geometry as g
from weapons87_flails import TILE
from weapons87_grips import centered
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';D=R/'docs/implementation/weapons87/continuation53/thrown'
MATERIALS=('stone','gold','iron','silver','diamond','perunite','netherite')
def models():
    result={}
    for family in ('throwing_knife','sulitsa'):
        # Read the immutable P40 source rather than recursively editing generated variants.
        base=json.loads((R/'tools/weapon-art-original/thrown-p40'/('stone_'+family+'_held.json')).read_text())
        grip=[8,4.5 if family=='throwing_knife' else 3.5,8]
        for material in MATERIALS:
            name=material+'_'+family;held=copy.deepcopy(base)
            held['textures']['particle']='slavicmyths:item/'+name
            for element in held['elements']:
                for face in element['faces'].values():
                    if face['uv']==[12,0,16,4]:
                        x,y=TILE[material];face['uv']=[x,y,x+4,y+4]
            for ctx,old in held['display'].items():
                left=ctx.endswith('lefthand');target=(-1 if left else 1,-1,-2) if ctx.startswith('firstperson') else (0,0,0)
                held['display'][ctx]=centered(grip,old['rotation'],old['scale'],left,target)
            result['models/item/'+name+'_held.json']=held
            result['models/item/'+name+'_icon.json']={'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+name}}
            result['models/item/'+name+'.json']={'loader':'neoforge:separate_transforms','base':{'parent':'slavicmyths:item/'+name+'_held'},'perspectives':{'gui':{'parent':'slavicmyths:item/'+name+'_icon'},'ground':{'parent':'slavicmyths:item/'+name+'_icon'},'fixed':{'parent':'slavicmyths:item/'+name+'_icon'}}}
    return result
def apply():
    for relative,model in models().items():
        target=A/relative;target.write_text(json.dumps(model,indent=2)+'\n',encoding='utf-8')
def draft():
    D.mkdir(parents=True,exist_ok=True);proposal=models();manifest=[]
    for material in MATERIALS:
        sheet=Image.new('RGB',(1600,700),'#18212b');draw=ImageDraw.Draw(sheet)
        draw.text((8,8),'#173 / actual P40 mesh extended by material. Short blade + guard vs light shaft + small point; own existing GUI. Source design, NOT owner acceptance.',fill='white')
        for row,family in enumerate(('throwing_knife','sulitsa')):
            name=material+'_'+family;source=A/'models/item'/(name+'.json');before=g.model('slavicmyths:item/'+name,'thirdperson_righthand');after=proposal['models/item/'+name+'_held.json'];raw=source.read_bytes()
            target=D/'before'/source.relative_to(A);target.parent.mkdir(parents=True,exist_ok=True)
            if target.exists():assert target.read_bytes()==raw,'Before archive already exists with different production source'
            else:target.write_bytes(raw)
            y=45+row*325;draw.text((8,y),name+' | grip '+str([8,4.5 if row==0 else 3.5,8])+' | 1 unit=1/16 block; player32/palm4; wood + binding + '+material,fill='white')
            for ci,(label,model,angle) in enumerate([('BEFORE FRONT',before,(0,0,0)),('DRAFT FRONT',after,(0,0,0)),('BEFORE SIDE',before,(0,90,0)),('DRAFT SIDE',after,(0,90,0)),('BEFORE 3/4',before,(15,35,0)),('DRAFT 3/4',after,(15,35,0))]):
                image=g.project(g.faces(model),240,32,angle);sheet.paste(image,(ci*250,y+30),image);draw.text((ci*250+5,y+18),label,fill='white')
            icon=Image.open(A/'textures/item'/(name+'.png')).convert('RGBA');sheet.paste(icon,(1510,y+50),icon);sheet.paste(icon.resize((64,64),Image.Resampling.NEAREST),(1510,y+100))
            vertices=np.concatenate([v for _,v,_ in g.faces(after)]);manifest.append({'id':name,'before_path':str(source.relative_to(R)),'before_sha256':hashlib.sha256(raw).hexdigest(),'icon_sha256':hashlib.sha256((A/'textures/item'/(name+'.png')).read_bytes()).hexdigest(),'draft':after,'dimensions':(vertices.max(0)-vertices.min(0)).tolist(),'decision':'FIX: complete P40 material series, preserve inventory; correct authored grip and upright flight axis','owner':'PENDING'})
        sheet.save(D/(material+'-DESIGN.png'))
    (D/'design.json').write_text(json.dumps(manifest,indent=2)+'\n')
    print('14 actual before/draft records and 7 material sheets written; production unchanged')
if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('--apply',action='store_true');args=parser.parse_args()
    apply() if args.apply else draft()
