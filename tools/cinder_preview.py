"""Source previews from actual baked ModelPart polygons/UV, never game captures."""
from pathlib import Path
import json,math,copy
from PIL import Image,ImageDraw
from navya_entity_assets import render
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/black-cinder-production';A=R/'src/main/resources/assets/slavicmyths'
def main():
 rigs=json.loads((O/'native-rigs.json').read_text());sheet=Image.new('RGB',(360*6,400*5),'#18171f')
 for row,(name,rig) in enumerate(rigs.items()):
  tex=Image.open(A/f'textures/entity/{name}.png').convert('RGBA');idle=rig['frames'][0]
  for col,(label,angle) in enumerate([('front',0),('left',math.pi/2),('right',-math.pi/2),('back',math.pi),('three-quarter',-.65),('top',0)]):
   frame=copy.deepcopy(idle)
   if label=='top':
    for face in frame['faces']:
     for point in face:point[1],point[2]=point[2]+6,point[1]+16
   im=render(frame,tex,size=360,scale=3.6,angle=angle);ImageDraw.Draw(im).text((10,30),name+' / '+label,fill='white');sheet.paste(im,(360*col,400*row+25))
  frames=[render(f,tex,size=360,scale=3.6,angle=-.65) for f in rig['frames']]
  frames[0].save(O/f'{name}-actual-poses.gif',save_all=True,append_images=frames[1:],duration=220,loop=0)
 sheet.save(O/'actual-models-six-views.png')
 before=Image.new('RGB',sheet.size,'#18171f');d=ImageDraw.Draw(before);d.text((25,25),'BEFORE: these five entity/model IDs were absent from hell HEAD 02765c5e. No earlier model is invented.',fill='white');before.save(O/'before-models-absent.png')
 names=['black_cinder_soil','ash_crust','smoking_cinder','charred_stone','cracked_charred_stone','blackwood_log','charred_blackwood_log','blackwood_planks']
 sw=Image.new('RGB',(640,len(names)*120),'#252525');d=ImageDraw.Draw(sw)
 for i,name in enumerate(names):
  im=Image.open(A/f'textures/block/{name}.png').convert('RGBA');d.text((10,i*120+5),name,fill='white');sw.paste(im.resize((80,80),Image.Resampling.NEAREST),(10,i*120+25))
  for y in range(5):
   for x in range(5):sw.paste(im.resize((16,16),Image.Resampling.NEAREST),(140+x*16,i*120+25+y*16))
  colors=im.convert('RGB').getcolors(256);colors=sorted(colors,reverse=True)[:5]
  for j,(_,rgb) in enumerate(colors):d.rectangle((270+j*65,i*120+30,320+j*65,i*120+70),fill=rgb);d.text((270+j*65,i*120+75),'#%02x%02x%02x'%rgb,fill='white')
 sw.save(O/'actual-palette-and-tiling.png');print('Source previews: 5 rigs x6views;5pose GIF;8palette rows. Original comparison and game capture NOT_RUN.')
if __name__=='__main__':main()
