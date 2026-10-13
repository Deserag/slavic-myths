from pathlib import Path
from PIL import Image,ImageDraw
import json
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/serpent-hill-1.9.5';A=R/'src/main/resources/assets/slavicmyths'
def color(state):
 if state=='minecraft:air':return '#14151C'
 if 'defiled' in state or 'mor_' in state:return '#A6274B'
 if 'torch' in state or 'brazier' in state:return '#E4872B'
 if 'banner' in state:return '#A51F31'
 if 'chest' in state or 'barrel' in state:return '#A67D48'
 if 'slab' in state:return '#764B65'
 if 'deepslate' in state:return '#444550'
 if 'masonry' in state or 'blackstone' in state or 'bas_relief' in state:return '#47434E'
 if 'dirt' in state:return '#665343'
 if 'log' in state:return '#40343D'
 return '#777880'
def main():
 n=json.loads((O/'layout.json').read_text(encoding='utf-8'));im=Image.new('RGB',(1500,1180),'#14151C');d=ImageDraw.Draw(im);d.text((20,12),'SERPENT HILL 1.9.5 - ACTUAL PROCEDURAL VOXELS - OFFLINE, NOT MINECRAFT QA',fill='#CCC6B8')
 for x,y,z,state in n['surface']:
  if state=='minecraft:air':continue
  d.rectangle((24+x*5,55+z*5,28+x*5,59+z*5),fill=color(state))
  ix=1080+(x-z)*2.5;iy=560+(x+z)*.9-y*2.8;d.rectangle((ix-2,iy-2,ix+3,iy+2),fill=color(state))
 d.text((30,705),'TOP PLAN: lower cave at (60,114); upper gate at (97,82)',fill='#CEC1A1');d.text((850,705),'SURFACE PROJECTION / 4 TOWERS',fill='#CEC1A1')
 for x,y,state in n['section_z84']:d.rectangle((25+x*5,1060-y*2,29+x*5,1061-y*2),fill=color(state))
 d.text((30,760),'CUT AT Z=84: connected interior stairs, deep chambers and fortress',fill='#CEC1A1')
 for idx,name in enumerate(json.loads((O/'block-manifest.json').read_text(encoding='utf-8'))['blocks']):
  tile=Image.open(A/f'textures/block/{name}.png').resize((56,56),Image.Resampling.NEAREST);x=700+(idx%6)*126;y=800+(idx//6)*140;im.paste(tile,(x,y),tile);d.text((x,y+64),name.replace('_','\n'),fill='#C4BCC2')
 im.save(O/'offline-preview.png');print(O/'offline-preview.png')
if __name__=='__main__':main()
