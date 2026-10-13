from pathlib import Path
from PIL import Image,ImageDraw
import json,math
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/sanctuaries-1.9.3';A=R/'src/main/resources/assets/slavicmyths'
def color(name):
 name=name.split(':')[-1]
 if name=='air':return None
 if name=='structure_block':return (240,175,68)
 if name.startswith('mor_'):return (91,38,53)
 if name in ['water','blue_wall_banner']:return (73,166,194)
 if name in ['campfire','lantern']:return (235,159,69)
 if 'log' in name or 'planks' in name or 'spruce' in name or 'fire_idol' in name or 'carved_fire' in name:return (103,75,47)
 if 'banner' in name:return (153,56,49)
 if 'moss' in name or 'azalea' in name or 'lichen' in name:return (84,119,84)
 if 'sea_lantern' in name:return (181,236,235)
 if 'blackstone' in name or 'iron' in name:return (51,52,57)
 if 'chest' in name or 'barrel' in name or 'pot' in name:return (157,110,65)
 return (132,137,136)
def main():
 out=Image.new('RGB',(1600,1250),'#182026');draw=ImageDraw.Draw(out);draw.text((20,12),'SLAVIC MYTHS 1.9.3 - OFFLINE AUTHORED BLOCK PREVIEW / NOT A GAME SCREENSHOT',fill='#D6E5E7')
 for index,family in enumerate(['fire','spring']):
  data=json.loads((O/f'{family}-blocks.json').read_text());summary=json.loads((O/f'{family}-layout.json').read_text());x0=30+index*790
  draw.text((x0,45),'FIRE 80x76 - TOP + ELEVATION' if family=='fire' else 'SPRING 96x86 - CUTAWAY + SECTION',fill='#F79C42' if family=='fire' else '#65D1E8')
  scale=6;top={}
  for b in data:
   x,y,z=b['pos'];c=color(b['name'])
   if c is None:continue
   if family=='spring' and z<75 and y>=7:continue
   if (x,z) not in top or top[x,z][0]<y:top[x,z]=(y,c)
  for (x,z),(y,c) in top.items():draw.rectangle((x0+x*scale,80+z*scale,x0+(x+1)*scale-1,80+(z+1)*scale-1),fill=c)
  for marker in summary['markers']:
   x,y,z=marker['pos'];draw.rectangle((x0+x*scale-2,80+z*scale-2,x0+x*scale+scale+1,80+z*scale+scale+1),outline='#FFFFFF');draw.text((x0+x*scale+7,80+z*scale),marker['role'],fill='#FFFFFF')
  ybase=870;profile={}
  for b in data:
   x,y,z=b['pos'];c=color(b['name'])
   if c is None:continue
   axis=x if family=='fire' else z
   distance=z if family=='fire' else abs(x-48)
   if family=='spring' and abs(x-48)>4:continue
   if (axis,y) not in profile or distance>profile[axis,y][0] if family=='fire' else (axis,y) not in profile or distance<profile[axis,y][0]:profile[axis,y]=(distance,c)
  for (x,y),(_,c) in profile.items():draw.rectangle((x0+x*scale,ybase-(y+1)*scale,x0+(x+1)*scale-1,ybase-y*scale-1),fill=c)
  draw.line((x0,ybase,x0+summary['size'][0 if family=='fire' else 2]*scale,ybase),fill='#6F8793')
  draw.text((x0,910),'Native templates, markers and paths; actual terrain/fluids/lighting require manual QA.',fill='#9DAEB9')
 names=['sacred_ember','purifying_ash','flame_vial','fading_flame_charm','ancient_rune_fragment','pure_spring_drop','purified_water_bottle','spring_tear','purification_stone']
 for i,name in enumerate(names):
  icon=Image.open(A/f'textures/item/{name}.png').resize((96,96),Image.Resampling.NEAREST);out.paste(icon,(25+i*173,1000),icon);draw.text((25+i*173,1110),name.replace('_','\n'),fill='#C1D1DA')
 out.save(O/'offline-preview.png');print(O/'offline-preview.png')
if __name__=='__main__':main()
