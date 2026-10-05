from pathlib import Path
import json,sys
sys.path.insert(0,'tools')
import world_boss_models_092 as review
review.OUT=Path('docs/verification/yaga-0.9.3');review.OUT.mkdir(exist_ok=True)
review.PAL=['#b8a785','#ae9b54','#827a70','#3e3433','#6d382f','#614835','#c1b498','#71483b','#cebd94']
parts=json.loads(Path('work/yaga_geometry.json').read_text())
nodes=[dict(name=n,parent=p,pivot=pos,box=b,mat=m,rot=rot) for n,p,pos,b,m,rot in parts]
review.review('baba_yaga',nodes)
(review.OUT/'baba-yaga-geometry.json').write_text(json.dumps(nodes,indent=2),encoding='utf-8')
# Asset review board at actual sprite proportions, not a replacement for the in-game view.
from PIL import Image,ImageDraw
im=Image.new('RGBA',(1024,512),'#26231f');d=ImageDraw.Draw(im)
items=['putevodny_klubok','svyazka_trav_yagi','otvar_ochishcheniya','letuchaya_maz','otvar_lesnoy_zorkosti','yagin_nastoy_stoykosti','otvar_bodrosti','lesnoy_nastoy_vosstanovleniya']
for i,id in enumerate(items):
 icon=Image.open('src/main/resources/assets/slavicmyths/textures/item/'+id+'.png');icon=icon.resize((200,200),Image.Resampling.NEAREST);x=(i%4)*256+28;y=(i//4)*256;im.alpha_composite(icon,(x,y));d.text((x-10,y+210),id,fill='#ead5a5')
im.save(review.OUT/'items.png')
