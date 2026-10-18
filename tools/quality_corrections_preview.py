"""Actual native #59/#53 pose sheets, including ground/hitbox and joint markers."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,math
from rusalka_preview import render
R=Path(__file__).resolve().parents[1]
def main():
 data=json.loads((R/'build/quality-corrections/meshes.json').read_text());anchors=json.loads((R/'build/quality-corrections/anchors.json').read_text())
 out=R/'docs/implementation/quality-59-53';out.mkdir(parents=True,exist_ok=True)
 tex=Image.open(R/'src/main/resources/assets/slavicmyths/textures/entity/rusalka.png').convert('RGB');im=Image.new('RGB',(1500,450),'#1C292D');d=ImageDraw.Draw(im)
 for i,(key,label) in enumerate([(2,'SONG INTRO'),(3,'CALM LURE'),(9,'INTENSE'),(10,'CLOSE WARNING'),(4,'BREAK')]):
  im.paste(render(data['rusalka_'+str(key)],tex,-25,(300,420)),(i*300,20));d.text((i*300+12,5),label,fill='#E8E3D6')
 d.text((12,432),'Actual production rig / source pose inputs. NOT IN-GAME.',fill='#8FA27E');im.save(out/'rusalka-stages.png')
 tex=Image.open(R/'src/main/resources/assets/slavicmyths/textures/entity/polevik.png').convert('RGB')
 frames=[('polevik_before_0',0,'BEFORE / STAND'),('polevik_before_1',0,'BEFORE / CROP'),('polevik_crop_1.0',0,'AFTER / CROP'),('polevik_crop_1.0',90,'AFTER / CROP SIDE'),('polevik_crop_0.0',0,'AFTER / STAND'),('polevik_crop_0.25',0,'CROUCH 25%'),('polevik_crop_0.5',0,'CROUCH 50%'),('polevik_crop_0.75',0,'CROUCH 75%'),('polevik_before_2',-25,'GIFT (PRESERVED)'),('polevik_phase_TELEGRAPH',-25,'TELEGRAPH'),('polevik_phase_ACTIVE',-25,'ACTIVE'),('polevik_phase_RECOVERY',-25,'RECOVERY')]
 im=Image.new('RGB',(1400,1320),'#1C292D');d=ImageDraw.Draw(im)
 for i,(key,yaw,label) in enumerate(frames):
  x=i%4*350;y=i//4*430;w,h=350,400;im.paste(render(data[key],tex,yaw,(w,h)),(x,y+25))
  angle=math.radians(yaw);pitch=math.radians(6);scale=min(w/34,h/40)
  def project(v):
   a=v[0]*math.cos(angle)+v[2]*math.sin(angle);b=v[2]*math.cos(angle)-v[0]*math.sin(angle)
   return (x+w/2+a*scale,y+25+h*.46+((v[1]-7)*math.cos(pitch)-b*math.sin(pitch))*scale)
  # Entity origin is model y=24; 0.6x2.1 block hitbox in the same native scale.
  ground=project((0,24,0))[1];d.line((x+10,ground,x+w-10,ground),fill='#8FA27E',width=2)
  corners=[(xx,yy,zz) for xx in [-4.8,4.8] for yy in [-9.6,24] for zz in [-4.8,4.8]]
  for j,one in enumerate(corners):
   for two in corners[j+1:]:
    if sum(a!=b for a,b in zip(one,two))==1:d.line((project(one),project(two)),fill='#6B8490')
  for name,pos in anchors[key].items():
   px,py=project(pos);color='#E6B66A' if 'Leg' in name else '#98BFCB' if 'Toe' in name else '#E8E3D6';d.ellipse((px-2,py-2,px+2,py+2),fill=color)
  d.text((x+10,y+6),label,fill='#E8E3D6')
 d.text((12,1295),'Native source projection, NOT IN-GAME. Green=ground; blue=0.6x2.1 hitbox; dots=body/head/hips/toes.',fill='#8FA27E');im.save(out/'polevik-before-after.png');print(out)
if __name__=='__main__':main()
