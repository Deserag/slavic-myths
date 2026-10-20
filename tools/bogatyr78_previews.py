"""Actual modified ModelPart/UV before and after at matching camera, scale and ticks."""
from pathlib import Path
import json,sys
from PIL import Image,ImageDraw
sys.path.insert(0,str(Path(__file__).resolve().parent))
from skim77_previews import render
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/bogatyr-78'
def main():
 poses=json.loads((O/'native-poses.json').read_text(encoding='utf-8'));tex=Image.open(R/'src/main/resources/assets/slavicmyths/textures/entity/alyosha_popovich.png')
 for action in ['ARRIVE','RECALL']:
  sheet=Image.new('RGB',(1500,690),'#1f2a30');d=ImageDraw.Draw(sheet)
  for row,stage in enumerate(['before','after']):
   for col,t in enumerate([0,6,12,18,24]):
    pose=poses[f'{stage}_{action}_{t}'];sheet.paste(render(pose,tex,1,-35,size=(300,310),scale=6),(col*300,row*330+20));d.text((col*300+8,row*330+3),f'{stage.upper()} / {action} tick {t}',fill='white')
  d.text((8,673),'SOURCE MODEL/UV ONLY / same scale, camera, age=20 / NOT IN GAME',fill='white');sheet.save(O/f'{action.lower()}-matched.png')
 print('Matched native ARRIVE/RECALL source previews saved')
if __name__=='__main__':main()
