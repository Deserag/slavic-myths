"""Actual native armor vertices/UV, never Minecraft screenshots or original art."""
from pathlib import Path
import json
from PIL import Image,ImageDraw
from nether327_preview import view,VIEWS
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/2026-10-29-hell-remaining/native-armor'
def main():
 data=json.loads((O/'models.json').read_text(encoding='utf-8'));tex=Image.open(O/'texture.png').convert('RGBA');sheet=Image.new('RGB',(1800,800),'#18171f')
 for row,(name,model) in enumerate(data.items()):
  for col,(camera,angle) in enumerate(VIEWS):
   im=view(model['frames'][0],tex,camera,angle,6);ImageDraw.Draw(im).text((8,28),name+' source / '+camera,fill='white');sheet.paste(im,(col*360,row*400+10))
  frames=[view(f,tex,'three-quarter',-.65,6) for f in model['frames']];frames[0].save(O/(name+'-poses.gif'),save_all=True,append_images=frames[1:],duration=350,loop=0)
 sheet.save(O/'royal-native-five-views.png');print('Source-native armor previews; runtime/original comparison NOT_RUN')
if __name__=='__main__':main()
