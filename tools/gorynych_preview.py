"""Offline projection of authored cuboids and atlas, never a game screenshot."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,math
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/gorynych-1.9.6';A=R/'src/main/resources/assets/slavicmyths'
def main():
 rig=json.loads((O/'zmey_gorynych_corrupted-rig.json').read_text(encoding='utf-8'));out=Image.new('RGB',(1800,1250),'#161922');draw=ImageDraw.Draw(out);draw.text((20,12),'GORYNYCH 1.9.6 - ACTUAL AUTHORED CUBOIDS / ATLAS - OFFLINE, NOT A GAME SCREENSHOT',fill='#D9D7D0')
 for panel,(label,wings,clean,angle) in enumerate([('GROUND - NO WINGS',False,False,.9),('AIR - UNFOLDED WINGS',True,False,.3),('PURIFIED - SAME RIG',True,True,.65)]):
  atlas=Image.open(A/f'textures/entity/zmey_gorynych_{"purified" if clean else "corrupted"}.png');ox=300+panel*600;oy=800;scale=2.2;draw.text((panel*600+25,65),label,fill='#C4CFD1');faces=[]
  def project(p):
   x,y,z=p;xx=x*math.cos(angle)-z*math.sin(angle);zz=x*math.sin(angle)+z*math.cos(angle);return (ox+xx*scale,oy-y*scale+zz*.32*scale),zz-y*.15
  for b in rig['boxes']:
   if not wings and b['bone'].startswith('wing_'):continue
   x,y,z=b['center'];w,h,d=b['size'];u,v,_,_=b['uv'];color=atlas.getpixel((int(u+d+w/2),int(v+d+h/2)))[:3]
   corners=[(x+sx*w/2,y+sy*h/2,z+sz*d/2) for sx,sy,sz in [(-1,-1,-1),(1,-1,-1),(1,1,-1),(-1,1,-1),(-1,-1,1),(1,-1,1),(1,1,1),(-1,1,1)]]
   for face,light in [([0,1,2,3],.9),([4,5,6,7],.7),([0,4,7,3],.75),([1,5,6,2],.85),([3,2,6,7],1.1),([0,1,5,4],.65)]:
    ps=[project(corners[i]) for i in face];faces.append((sum(p[1] for p in ps)/4,[p[0] for p in ps],tuple(min(255,int(c*light)) for c in color)))
  for _,polygon,color in sorted(faces,reverse=True):draw.polygon(polygon,fill=color,outline=tuple(max(0,c-12) for c in color))
 for i,name in enumerate(['gorynych_scale','ancient_serpent_steel','serpent_seal','rune_mor_breath','rune_dragon_flame','rune_storm_strike','gorynych_flame_breath_bottle','gorynych_mor_breath_bottle','gorynych_storm_breath_bottle','dragon_helmet','dragon_chestplate','dragon_leggings','dragon_boots']):
  im=Image.open(A/f'textures/item/{name}.png').resize((96,96),Image.Resampling.NEAREST);out.paste(im,(30+i*133,1060),im)
 out.save(O/'offline-preview.png');print('GORYNYCH_PREVIEW_PASS')
if __name__=='__main__':main()
