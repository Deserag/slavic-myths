"""Comparable source renders, explicitly separate from raw Minecraft screenshots in client/."""
from pathlib import Path
import json,math
from PIL import Image,ImageDraw
from nether327_preview import view
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/boss331-rework'
def main():
 after=R/'docs/implementation/p0-bosses-327/native-331-after'
 a=json.loads((after/'models.json').read_text());b=json.loads((O/'before/models.json').read_text())
 for name,scale in [('grove_guardian',2.8),('zharenitsa',3.6),('magma_lord',3.2)]:
  sheet=Image.new('RGB',(1440,800),'#18171f')
  for row,(folder,data) in enumerate([(O/'before',b),(after,a)]):
   texture=Image.open(folder/(name+'.png')).convert('RGBA')
   for col,(camera,angle) in enumerate([('front',0),('left',math.pi/2),('back',math.pi),('three-quarter',-.65)]):
    im=view(data[name]['frames'][0],texture,camera,angle,scale)
    ImageDraw.Draw(im).text((10,45),'BEFORE' if row==0 else 'AFTER #331',fill='white');sheet.paste(im,(col*360,row*400))
  sheet.save(O/(name+'-native-comparison.png'))
 print('Comparable source renders saved; actual unedited game frames are separate in client/.')
if __name__=='__main__':main()
