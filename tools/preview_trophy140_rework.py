"""Same-scale actual native ModelPart/UV renders for the full six-head rework."""
from pathlib import Path
import json
from PIL import Image, ImageDraw
from armor88_preview import render

root=Path(__file__).resolve().parents[1]
out=root/'docs/implementation/trophy140-rework'
before=json.loads((out/'before.json').read_text())
after=json.loads((out/'after.json').read_text())
def sample(row):
    vertices=row['vertices']
    return {'layers':[{'texture':row['texture'].split(':')[1], 'faces':[
        [[(x-.5)*16,(1.9-y)*16-8,(z-.5)*16,u,v] for x,y,z,u,v in vertices[i:i+4]]
        for i in range(0,len(vertices),4)]}]}

for kind in ('LIKHO','MOR_LIKHO','TUGARIN','SKIM','VOLKOLAK','PODZEMNIK'):
    for mode in ('phases','views'):
        board=Image.new('RGB',(1600,320),'#161b22');draw=ImageDraw.Draw(board)
        draw.text((8,8),kind+' / actual production vertices + UV + PNG; same camera, scale and time. OFFLINE SOURCE, NOT GAMEPLAY.',fill='white')
        options=[(t,145) for t in (0,27,40,60)] if mode=='phases' else [(27,yaw) for yaw in (180,90,0,145)]
        for col,(time,yaw) in enumerate(options):
            for side,data in enumerate((before,after)):
                row=next(r for r in data if r['kind']==kind and r['time']==time)
                x=(col*2+side)*200
                board.paste(render(sample(row),yaw),(x,35))
                draw.text((x+4,38),('BEFORE' if side==0 else 'AFTER')+f' t={time} yaw={yaw}',fill='white')
        board.save(out/(kind.lower()+'-'+mode+'.png'))
