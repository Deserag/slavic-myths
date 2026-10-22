"""Actual source JSON+UV four-stage item projections. Not screenshots of Minecraft."""
from pathlib import Path
import json,sys
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parents[1];sys.path.insert(0,str(R/'tools'))
import weapons87_geometry as g
out=R/'docs/implementation/weapons87/blueprints/p33-p35/ci-source-views';out.mkdir(parents=True,exist_ok=True)
def sm(x):
    x=max(0,min(1,x));return x*x*(3-2*x)
def phases(x):
    return (0 if x<=0 else sm(x/.18)*(1-sm((x-.18)/.22)),sm((x-.12)/.21)*(1-sm((x-.38)/.46)))
record=[]
for kind,id in (('MACE','mace'),('SPEAR','spear')):
    board=Image.new('RGB',(1240,720),'#243039');draw=ImageDraw.Draw(board)
    draw.text((12,12),f'P33/34 {id} original JSON+UV. Approximate item-only FP wrist; NOT Minecraft or Steve/Alex.',fill='white')
    faces=g.faces(g.model('slavicmyths:item/'+id,'firstperson_righthand'))
    for i,(stage,t) in enumerate((('READY',0),('PREP',.15),('CONTACT',.35),('RECOVERY',.75))):
        prep,impact=phases(t)
        angles=((-24*prep+58*impact,12*prep-8*impact,-16*prep+21*impact) if kind=='MACE' else (11*prep-12*impact,8*prep-6*impact,0))
        offset=((-0.08*prep+.04*impact,.11*prep-.15*impact,-.05*impact) if kind=='MACE' else (-.045*prep,.035*prep,-.38*impact))
        old=g.project(faces,260,35)
        new=g.project(faces,260,35,angles,translation=tuple(x*16 for x in offset))
        row,col=divmod(i,2);x=col*610+8;y=row*340+46
        board.paste(old,(x,y),old);board.paste(new,(x+285,y),new)
        draw.text((x,y-20),stage+f' {t:.2f} prep{prep:.2f} hit{impact:.2f}',fill='white')
        draw.text((x,y+266),'Before item-only',fill='#cfdae3');draw.text((x+285,y+266),'After isolated wrist',fill='#d2eabc')
        record.append({'id':id,'phase':stage,'swing':t,'preparation':prep,'contact':impact,'rotation_degrees':angles,'offset_blocks':offset})
    draw.text((12,707),'Original geometry/pixels KEEP. No player rig, hit timing or multiplayer inferred from these source projections.',fill='white')
    board.save(out/(id+'-four-phases.png'))
(out/'source-matrix.json').write_text(json.dumps(record,indent=2)+'\n')
print('PASS two 4-frame real source item art projections. Live client NOT_RUN.')
