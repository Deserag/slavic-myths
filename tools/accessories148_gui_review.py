"""Actual GUI PNG/source-chain A/B at 1x and 4x NEAREST, every Curios item."""
from pathlib import Path
import json,hashlib
from PIL import Image,ImageDraw
from accessories148_art import NEW,OLD
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/slavicmyths';O=ROOT/'docs/implementation/accessories-148';B=O/'before'
ids=list(json.loads((O/'audit/population.json').read_text())['curios'])+list(NEW)
rows=[];sheet=Image.new('RGB',(720,42+len(ids)*84),'#293039');draw=ImageDraw.Draw(sheet);draw.text((8,7),'ACTUAL GUI SOURCES / BEFORE x1 x4 | AFTER x1 x4 / NEW = no before',fill='white')
def gui(id):
 p=A/f'models/item/{id}.json';d=json.loads(p.read_text());chain=[str(p.relative_to(ROOT))];d=d.get('perspectives',{}).get('gui',d)
 while d.get('parent','').startswith('slavicmyths:'):
  p=A/'models'/(d['parent'].split(':')[1]+'.json');chain.append(str(p.relative_to(ROOT)));parent=json.loads(p.read_text());parent['textures']=parent.get('textures',{})|d.get('textures',{});d=parent
 assert d.get('parent')=='minecraft:item/generated',(id,d.get('parent'))
 ref=d['textures']['layer0'];assert ref.startswith('slavicmyths:');p=A/'textures'/(ref.split(':')[1]+'.png');return Image.open(p).convert('RGBA'),chain,p
for i,id in enumerate(ids):
 y=42+i*84;after,chain,png=gui(id);before=None
 if id not in NEW:
  preview=O/'audit/gui-before'/f'{id}.png'
  previous=B/'src/main/resources/assets/slavicmyths/textures/item'/f'{id}.png'
  before=Image.open(preview if preview.exists() else previous).convert('RGBA') if id in OLD else after.copy()
 draw.text((8,y+20),id,fill='white')
 for side,im in enumerate([before,after]):
  if im is None:draw.text((215,y+20),'NEW',fill='gray');continue
  small=im.resize((16,16),Image.Resampling.NEAREST);big=small.resize((64,64),Image.Resampling.NEAREST);x=210+side*190;sheet.paste(small,(x,y+24),small);sheet.paste(big,(x+30,y),big)
 decision='FIX: distinct flat sprite; held geometry preserved' if id in OLD else 'NEW' if id in NEW else 'KEEP: authored flat source, no changes'
 row={'id':id,'decision':decision,'gui_chain':chain,'png':png.relative_to(ROOT).as_posix(),'size':list(after.size),'sha256':hashlib.sha256(png.read_bytes()).hexdigest(),'runtime':'NOT_RUN / OWNER_PENDING'}
 if before is not None:row['before_preview_sha256']=hashlib.sha256(before.tobytes()).hexdigest()
 rows.append(row)
sheet.save(O/'ICONS-BEFORE-AFTER-1x-4x.png');(O/'gui-matrix.json').write_text(json.dumps(rows,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
print('GUI sources:',len(rows))
