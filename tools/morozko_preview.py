"""Offline front/back UV contact sheet from generated cuboids, not a game screenshot."""
from pathlib import Path
from PIL import Image,ImageDraw
import json
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/morozko-1.9.1';A=R/'src/main/resources/assets/slavicmyths'
def main():
 rig=json.loads((O/'morozko-rig.json').read_text());atlas=Image.open(A/'textures/entity/morozko.png');out=Image.new('RGB',(1200,750),'#142334');d=ImageDraw.Draw(out);d.text((20,15),'MOROZKO 1.9.1 - OFFLINE UV / CUBOID PREVIEW (NOT GAME QA)',fill='#DDE7EF')
 for panel,back in enumerate([False,True]):
  ox=310+panel*570;oy=600;k=8
  d.line((ox-220,oy,ox+200,oy),fill='#41617E');d.text((ox-65,40),'BACK' if back else 'FRONT',fill='#AEE8FF')
  for b in sorted(rig['boxes'],key=lambda b:b['center'][2],reverse=not back):
   if b['bone']=='yield_heart':continue
   x,y,z=b['center'];w,h,depth=b['size'];u,v,_,_=b['uv'];tx=u+2*depth+w if back else u+depth;ty=v+depth
   face=atlas.crop((int(tx),int(ty),int(tx+w+.99),int(ty+h+.99))).resize((max(1,round(w*k)),max(1,round(h*k))),Image.Resampling.NEAREST)
   out.paste(face,(round(ox+((-x if back else x)-w/2)*k),round(oy-(y+h/2)*k)),face)
  d.rectangle((ox+160,oy-32*k,ox+160+10*k,oy),outline='#8C9AA8');d.text((ox+161,oy-32*k-15),'2 BLOCKS',fill='#8C9AA8')
 names=['morozko_staff','frost_mirror','frost_resistance_rune','frost_rune','heart_of_eternal_cold','morozko_trial_totem']
 for i,name in enumerate(names):
  icon=Image.open(A/f'textures/item/{name}.png').resize((96,96),Image.Resampling.NEAREST);out.paste(icon,(40+i*190,620),icon);d.text((40+i*190,722),name.replace('morozko_',''),fill='#AEE8FF')
 out.save(O/'offline-preview.png');print(O/'offline-preview.png')
if __name__=='__main__':main()
