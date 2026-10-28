"""Rasterize actual native ModelPart polygons/UV; no game screenshots or invented before models."""
from pathlib import Path
import json,math,copy
from PIL import Image,ImageDraw
from navya_entity_assets import render
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/burning-islands-205';A=R/'src/main/resources/assets/slavicmyths'
def main():
 rigs=json.loads((O/'native-rigs.json').read_text());sheet=Image.new('RGB',(360*4,390*4),'#18171f')
 for row,(name,rig) in enumerate(rigs.items()):
  tex=Image.open(A/f'textures/entity/burning_islands/{name}.png').convert('RGBA');idle=next(f for f in rig['frames'] if f['action']=='idle')
  for col,(label,angle) in enumerate([('front',0),('side',math.pi/2),('back',math.pi),('three-quarter',-.65)]):
   im=render(idle,tex,size=360,scale=3,angle=angle);ImageDraw.Draw(im).text((8,26),name+' / '+label,fill='white');sheet.paste(im,(360*col,390*row+15))
  selected=[f for f in rig['frames'] if (f['action'] in ['WARN_P1','CLAWS_P1'] and f['time'] in [0,11.5,23,31]) or f['action'] in ['idle','hurt','death']]
  if name=='magma_lord':selected=[f for f in rig['frames'] if f['action'].endswith('_P2') and f['action'] not in ['IDLE_P2','WARN_P2'] and f['time']>=20]
  frames=[render(f,tex,size=360,scale=3,angle=-.65) for f in selected]
  frames[0].save(O/f'{name}-native-poses.gif',save_all=True,append_images=frames[1:],duration=250,loop=0)
  if name=='magma_lord':
   head=Image.new('RGB',(360*5,390),'#18171f')
   for i,f in enumerate(rig['trophyFrames']):
    # Exact head subtree; common camera/scale for every jaw opening.
    frame=copy.deepcopy(f)
    for face in frame['faces']:
     for p in face:p[1]+=10
    im=render(frame,tex,size=360,scale=8,angle=-.65);head.paste(im,(i*360,20))
   head.save(O/'magma-lord-head-native-jaw.png')
 sheet.save(O/'native-models-four-views.png')
 before=Image.new('RGB',sheet.size,'#18171f');ImageDraw.Draw(before).text((20,20),'BEFORE 055268cf: these four entity/model IDs absent. No earlier model is fabricated.',fill='white');before.save(O/'before-models-absent.png')
 icons=Image.new('RGB',(9*144,180),'#18171f')
 for i,n in enumerate(['magma_clot','magma_core','salamander_hide','crab_claw','magma_rune_fragment','rune_magma_1','rune_magma_2','magma_lord_heart','magma_lord_head']):
  im=Image.open(A/f'textures/item/{n}.png');icons.paste(im.resize((96,96),Image.Resampling.NEAREST),(i*144+20,20),im.resize((96,96),Image.Resampling.NEAREST));ImageDraw.Draw(icons).text((i*144+2,130),n,fill='white')
 icons.save(O/'inventory-icons.png')
 print('Rendered native source: four rigs/four views, action GIFs, five trophy jaw poses, nine static GUI sprites. Original match/gameplay NOT_RUN.')
if __name__=='__main__':main()
