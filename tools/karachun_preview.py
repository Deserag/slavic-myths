from pathlib import Path
from PIL import Image,ImageDraw
import json
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/karachun-1.9.2';A=R/'src/main/resources/assets/slavicmyths'
def main():
 out=Image.new('RGB',(1500,900),'#191924');d=ImageDraw.Draw(out);d.text((20,14),'KARACHUN 1.9.2 - OFFLINE CUBOID/UV PREVIEW - NOT GAME QA',fill='#BAC4D8')
 for panel,side in enumerate(['front','side','back']):
  rig=json.loads((O/'karachun-rig.json').read_text());atlas=Image.open(A/'textures/entity/karachun.png');ox=330+panel*450;oy=600;k=8
  d.text((ox-30,40),side.upper(),fill='#A9C6EE')
  for b in sorted(rig['boxes'],key=lambda b:b['center'][0 if side=='side' else 2],reverse=side!='back'):
   x,y,z=b['center'];w,h,depth=b['size'];u,v,_,_=b['uv'];tx=u if side=='side' else u+2*depth+w if side=='back' else u+depth;ty=v+depth;wide=depth if side=='side' else w;horizontal=-z if side=='side' else -x if side=='back' else x
   face=atlas.crop((int(tx),int(ty),int(tx+wide+.99),int(ty+h+.99))).resize((max(1,round(wide*k)),max(1,round(h*k))),Image.Resampling.NEAREST);out.paste(face,(round(ox+(horizontal-wide/2)*k),round(oy-(y+h/2)*k)),face)
  d.rectangle((ox+120,oy-32*k,ox+200,oy),outline='#697282');d.text((ox+122,oy-32*k-15),'2 BLOCKS',fill='#697282')
 for i,name in enumerate(['karachun_scythe','rune_of_waning','black_wing_amulet','shard_of_last_night','karachun_trial_totem']):
  icon=Image.open(A/f'textures/item/{name}.png').resize((128,128),Image.Resampling.NEAREST);out.paste(icon,(40+i*290,700),icon);d.text((40+i*290,850),name,fill='#A9C6EE')
 out.save(O/'offline-preview.png');print(O/'offline-preview.png')
if __name__=='__main__':main()
