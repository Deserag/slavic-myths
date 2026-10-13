from pathlib import Path
from PIL import Image,ImageDraw
import json
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/guardians-1.9.4';A=R/'src/main/resources/assets/slavicmyths'
def main():
 out=Image.new('RGB',(1500,1200),'#191924');d=ImageDraw.Draw(out)
 d.text((20,12),'GUARDIANS 1.9.4 - ACTUAL UV / CUBOID PROJECTION - NOT A GAME SCREENSHOT',fill='#BBD4D9')
 for row,name in enumerate(['extinguished_flame_guardian','defiled_spring_keeper']):
  rig=json.loads((O/f'{name}_corrupted-rig.json').read_text(encoding='utf-8'))
  for col,side in enumerate(['front','side','back','cleansed']):
   ox=160+col*370;oy=510+row*500;k=6.6
   atlas=Image.open(A/f'textures/entity/{name}_{"cleansed" if side=="cleansed" else "corrupted"}.png')
   d.text((ox-35,45+row*500),side.upper(),fill='#A9C6EE')
   for b in sorted(rig['boxes'],key=lambda b:b['center'][0 if side=='side' else 2],reverse=side!='back'):
    if b['bone'].startswith('whip_'):continue
    x,y,z=b['center'];w,h,depth=b['size'];u,v,_,_=b['uv'];tx=u if side=='side' else u+2*depth+w if side=='back' else u+depth;ty=v+depth
    wide=depth if side=='side' else w;horizontal=-z if side=='side' else -x if side=='back' else x
    face=atlas.crop((int(tx),int(ty),int(tx+wide+.99),int(ty+h+.99))).resize((max(1,round(wide*k)),max(1,round(h*k))),Image.Resampling.NEAREST)
    out.paste(face,(round(ox+(horizontal-wide/2)*k),round(oy-(y+h/2)*k)),face)
 for col,name in enumerate(['mor_fire_amulet_fragment','mor_water_amulet_fragment']):
  icon=Image.open(A/f'textures/item/{name}.png').resize((128,128),Image.Resampling.NEAREST);out.paste(icon,(400+col*250,1050),icon)
 out.save(O/'offline-preview.png');print(O/'offline-preview.png')
if __name__=='__main__':main()
