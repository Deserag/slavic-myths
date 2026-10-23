"""P42: idempotent additive crossbow art and matching Finist draw-state displays.

Only the current working branch uses these additions. Existing dragon heads, bolts,
PNG textures, server code and the accepted two-hand transforms remain unchanged.
"""
from pathlib import Path
import json

ROOT=Path(__file__).resolve().parents[1]
MODELS=ROOT/'src/main/resources/assets/slavicmyths/models/item'
STATES=('','_pulling_0','_pulling_1','_pulling_2','_arrow','_firework')

def cuboid(lo,hi,texture,uv):
    return {'from':lo,'to':hi,'faces':{
        side:{'texture':'#'+texture,'uv':uv}
        for side in ('north','south','east','west','up','down')
    }}
ADDITIONS={
'serpent_crossbow':[
    cuboid([-1.78,7.85,3.2],[-.85,8.8,5.6],'ivory',[8,4,11.5,7.5]),
    cuboid([16.85,7.85,3.2],[17.78,8.8,5.6],'ivory',[8,4,11.5,7.5]),
    cuboid([.8,7.85,2.4],[1.48,8.5,4.5],'seam',[8,0,11.5,3.5]),
    cuboid([14.52,7.85,2.4],[15.2,8.5,4.5],'seam',[8,0,11.5,3.5]),
    cuboid([7.4,9,6],[8.6,9.55,10],'ivory',[8,4,11.5,7.5]),
],
'three_breath_crossbow':[
    cuboid([2.92,11.85,3.5],[3.58,12.42,5.3],'fire',[12,0,15.5,3.5]),
    cuboid([7.65,11.85,3.5],[8.35,12.42,5.3],'mor',[12,4,15.5,7.5]),
    cuboid([12.42,11.85,3.5],[13.08,12.42,5.3],'storm',[12,8,15.5,11.5]),
]}
FINIST={
    'thirdperson_righthand':{'rotation':[-80,260,-40],'translation':[-1,-2,2.5],'scale':[.92]*3},
    'thirdperson_lefthand':{'rotation':[-80,-260,40],'translation':[1,-2,2.5],'scale':[.92]*3},
    'firstperson_righthand':{'rotation':[0,-90,25],'translation':[1.13,3.2,1.13],'scale':[.72]*3},
    'firstperson_lefthand':{'rotation':[0,90,-25],'translation':[-1.13,3.2,1.13],'scale':[.72]*3},
}
def read(id):
    return json.loads((MODELS/(id+'.json')).read_text(encoding='utf-8'))
def write(id,data):
    (MODELS/(id+'.json')).write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def check():
    for item,parts in ADDITIONS.items():
        for suffix in STATES:
            from issue217_models import original_crossbow
            data=original_crossbow(item+suffix,read(item+suffix))
            before=(8 if item=='serpent_crossbow' else 65)+(1 if item=='serpent_crossbow' and suffix in ('_arrow','_firework') else 0)+(3 if item=='three_breath_crossbow' and suffix in ('_arrow','_firework') else 0)
            assert len(data['elements'])==before+len(parts),(item,suffix)
            assert data['elements'][-len(parts):]==parts,(item,suffix)
            for p in parts:
                for face in p['faces'].values():assert face['texture'][1:] in data['textures']
    for suffix in ('','_pulling_0','_pulling_1','_pulling_2'):
        d=read('finist_bow'+suffix)
        assert d.get('display')==FINIST,suffix
        assert d.get('parent')=='minecraft:item/bow',suffix
    print('PASS P42: 12 crossbow states, 4 Finist drawing states, no source art drift. Runtime NOT_RUN')
def apply():
    for item,parts in ADDITIONS.items():
        for suffix in STATES:
            from issue217_models import original_crossbow
            key=item+suffix;data=original_crossbow(key,read(key));arr=data['elements']
            before=(8 if item=='serpent_crossbow' else 65)+(1 if item=='serpent_crossbow' and suffix in ('_arrow','_firework') else 0)+(3 if item=='three_breath_crossbow' and suffix in ('_arrow','_firework') else 0)
            if len(arr)==before:
                arr.extend(parts);write(key,data)
            else:
                assert len(arr)==before+len(parts) and arr[-len(parts):]==parts,key+' unexpected existing owner geometry'
    for suffix in ('','_pulling_0','_pulling_1','_pulling_2'):
        key='finist_bow'+suffix;d=read(key)
        if d.get('display')!=FINIST:
            assert 'display' not in d,key+' existing transforms require owner review'
            d['display']=FINIST;write(key,d)
    check()
if __name__=='__main__':
    import argparse
    a=argparse.ArgumentParser();a.add_argument('--apply',action='store_true');arg=a.parse_args()
    if arg.apply:apply()
    else:check()
