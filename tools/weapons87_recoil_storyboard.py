"""Generate 4-phase source-projected recoil sheet from actual item JSON/UV (not client screenshots)."""
from pathlib import Path
import sys,json,math
ROOT=Path(__file__).resolve().parents[1]
sys.path.insert(0,str(ROOT/'tools'))
import weapons87_geometry as g
from PIL import Image,ImageDraw
ASSET=ROOT/'src/main/resources/assets/slavicmyths/models/item'
OUT=ROOT/'docs/implementation/weapons87/blueprints/p31-crossbow-recoil'
OUT.mkdir(parents=True,exist_ok=True)
STEPS=[('PREP / LOADED',0,True),('IMPACT / 3 TICKS',3,False),('RECOVERY / 5 TICKS',5,False),('IDLE / 7 TICKS',7,False)]
def amplitude(t):
    if t<0 or t>=7:return 0.0
    phase=t/7.0
    return math.sin(math.pi*phase)*(1-.25*phase)
record=[]
for id in ('serpent_crossbow','three_breath_crossbow'):
    board=Image.new('RGB',(1320,690),'#26323a')
    draw=ImageDraw.Draw(board)
    draw.text((20,12),f'P31 {id}: REAL JSON+UV SOURCE, approximate client wrist pose / STATIC FROZEN geometry, not Minecraft or Steve screenshot',fill='#ffffff')
    for i,(title,time,loaded) in enumerate(STEPS):
        path=ASSET/(id+('_arrow' if loaded else '')+'.json')
        data=json.loads(path.read_text(encoding='utf-8'))
        faces=g.faces(data)
        kick=amplitude(time)
        row=i//2;column=i%2
        x=column*655+16;y=row*310+44
        draw.text((x,y),title+f' recoil={kick:.3f}',fill='#f4f4f4')
        # Compare before/after using real item coordinates and a fixed angle;
        # frame-specific client wrist animation is translated from the exact Java formula.
        old=g.project(faces,250,44,(0,18,0),(.85,)*3,(0,0,0))
        new=g.project(faces,250,44,(-9*kick,18,0),(.85,)*3,(0,-.035*kick*16,.13*kick*16))
        board.paste(old,(x+8,y+34),old)
        board.paste(new,(x+315,y+34),new)
        draw.text((x+10,y+286),'OLD static wrist',fill='#cbd5df')
        draw.text((x+316,y+286),'NEW wrist + recoil',fill='#cfdfbe')
        record.append({'id':id,'phase':title,'ticks_since_fire':time,'recoil':round(kick,6),'model':path.relative_to(ROOT).as_posix(),'same_source_cuboids':len(data['elements'])})
    board.save(OUT/(id+'-four-phases.png'))
(OUT/'source-four-phase-data.json').write_text(json.dumps({'input':'real production item model JSON/UV source','camera':'orthographic projection at fixed angle/scale, not actual game camera/player hands','frames':record,'runtime':'NOT_RUN'},indent=2)+'\n',encoding='utf-8')
print('PASS real source before/after recoil boards for 2 item variants x4 phases; runtime NOT_RUN')
