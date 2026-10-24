"""Render captured production ModelPart vertices and real UV textures at identical scale."""
import json,sys
from pathlib import Path
from PIL import Image,ImageDraw
from native_pose_preview_56 import draw
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/feedback-20261024'
poses=json.loads((O/'native-carcass.json').read_text())
sheet=Image.new('RGB',(1000,7*300),'#202b30')
for row,(kind,scale) in enumerate([('cow','1.0'),('pig','0.9'),('sheep','0.8'),('rabbit','0.35'),('goose','0.55'),('duck','0.5'),('domestic_goat','0.8')]):
 tex=Image.open(R/f'src/main/resources/assets/slavicmyths/textures/entity/carcass/{kind}.png').convert('RGB')
 for col,label in enumerate(['before','after']):
  faces=[[[v[0],24-v[1],v[2],*v[3:]] for v in face] for face in poses[label+'_'+scale]]
  im=draw(faces,tex,35,size=(500,300),scale=5,view_pitch=18,center_y=80)
  ImageDraw.Draw(im).text((12,10),f'{kind}: {label}; same 5 px/model unit; static pose t=0',fill='white');sheet.paste(im,(col*500,row*300))
sheet.save(O/'carcass-before-after.png')
print('Native source/UV before-after: '+str(O/'carcass-before-after.png'))
