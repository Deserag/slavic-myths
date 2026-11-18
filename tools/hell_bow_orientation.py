"""Scoped, idempotent display repair; never changes mesh/UV/PNG/predicates/gameplay.

Pinned vanilla 1.21.1 pulling sprite points (-X,+Y); these authored bows point +X.
Pre-rotate their local plane by +135 degrees before vanilla's hand transform.
Left-hand ItemTransform mirrors Z, so its stored compensation is -135.
"""
from pathlib import Path
import copy,json,math
from PIL import Image

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/slavicmyths'
VANILLA={
 'thirdperson_righthand':([-80,260,-40],[-1,-2,2.5]),
 'thirdperson_lefthand':([-80,-280,40],[-1,-2,2.5]),
 'firstperson_righthand':([0,-90,25],[1.13,3.2,1.13]),
 'firstperson_lefthand':([0,90,-25],[1.13,3.2,1.13]),
}

def orient(model,name,gui_scale=None):
    display=model.setdefault('display',{})
    for context,(rotation,translation) in VANILLA.items():
        old=display.get(context,{})
        rotation=list(rotation);rotation[2]+= -135 if context.endswith('lefthand') else 135
        fallback=({'short_bow':.78,'heavy_bow':1.0}.get(name,.65) if context.startswith('thirdperson') else {'short_bow':.62,'heavy_bow':.8}.get(name,.65))
        scale=old.get('scale',[fallback]*3)
        if name=='infernal_bow' and context.startswith('firstperson'):
            # 38 model-pixel limbs and the full-draw string otherwise cross the near
            # camera plane at .65. Keep the palm anchor; reduce FP mesh only (not TP).
            scale=[.32]*3
        display[context]={'rotation':rotation,'translation':list(translation),'scale':scale}
    if name in ('short_bow','heavy_bow'):
        display['gui']={'rotation':[0,0,135],'scale':[gui_scale]*3}
    return model

def sprite_gui_scale():
    extent=0
    for name in ('short_bow','heavy_bow'):
        for suffix in ('','_pulling_0','_pulling_1','_pulling_2'):
            with Image.open(ASSETS/'textures/item'/(name+suffix+'.png')) as im:
                bounds=im.convert('RGBA').getchannel('A').getbbox();assert bounds
                for x in (bounds[0],bounds[2]):
                    for y in (bounds[1],bounds[3]):
                        px=16*(x/im.width-.5);py=16*(.5-y/im.height)
                        a=math.radians(135)
                        extent=max(extent,abs(px*math.cos(a)-py*math.sin(a)),abs(px*math.sin(a)+py*math.cos(a)))
    # One shared scale preserves short/heavy relative sprite proportions; half-pixel GUI margin.
    return min(1,7.5/extent)

def main():
    gui=sprite_gui_scale()
    for name in ('short_bow','heavy_bow','infernal_bow'):
        for suffix in ('','_pulling_0','_pulling_1','_pulling_2'):
            p=ASSETS/'models/item'/(name+suffix+'.json');before=json.loads(p.read_text(encoding='utf-8'))
            after=orient(copy.deepcopy(before),name,gui)
            assert {k:v for k,v in before.items() if k!='display'}=={k:v for k,v in after.items() if k!='display'}
            p.write_text(json.dumps(after,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print('Bow display basis repaired in12models; GUI shared scale',gui,'; mesh/UV/PNG/predicates unchanged')

if __name__=='__main__':main()
