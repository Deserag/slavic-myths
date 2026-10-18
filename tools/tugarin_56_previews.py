"""Native ModelPart/UV storyboard, never a Minecraft screenshot."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,gzip,io,subprocess,math
from grey_wolf_54_previews import draw
R=Path(__file__).resolve().parents[1];OUT=R/'docs/implementation/tugarin-56'
def main():
 before=json.loads(gzip.decompress((OUT/'before-native.json.gz').read_bytes()));after=json.loads(gzip.decompress((R/'build/tugarin-56/after-native.json.gz').read_bytes()))
 tex={}
 for old in [True,False]:
  for form,name in [('warrior','tugarin_phase1'),('serpent','tugarin_phase2')]:
   path='src/main/resources/assets/slavicmyths/textures/entity/'+name+'.png'
   tex[old,form]=Image.open(io.BytesIO(subprocess.check_output(['git','show','bcec6355:'+path],cwd=R)) if old else R/path).convert('RGB')
 keys=['spin_'+str(t) for t in [0,8,15,16,22,28,34,40,48,51]]+['recovery_spin_'+str(t) for t in [0,12,24]]
 def sheet(name,keys,top=False,old_new=True,metal=False):
  rows=(len(keys)+3)//4;canvas=Image.new('RGB',(2000,rows*445*(2 if old_new else 1)+20),'#202B30');d=ImageDraw.Draw(canvas)
  for version,(data,old) in enumerate([(before,True),(after,False)] if old_new else [(after,False)]):
   for i,key in enumerate(keys):
    form='serpent' if key.startswith(('wing','landing','BREATH','SERPENT','SMOKE','fire')) or key.startswith('transition_') and int(key.split('_')[1])>=30 else 'warrior'
    faces=data['fire_release'][key] if key.startswith('fire_') else data[form][key]
    # Explicit horizontal clipping plane: reveals lower-leg support under the torso.
    if top:faces=[f for f in faces if min(v[1] for v in f)>=8]
    if metal:faces=[f for f in faces if all(.25<=v[4]<.5 and v[3]<.5 for v in f)]
    x=i%4*500;y=(version*rows+i//4)*445;panel=draw(faces,tex[old,form],-35,scale=3.2,view_pitch=90 if top else 7)
    if key.startswith('fire_'):
     marker=ImageDraw.Draw(panel);angle=math.radians(-35);pitch=math.radians(7);mx=215-16*math.sin(angle)*3.2;my=185+((-39.2+4)*math.cos(pitch)+16*math.cos(angle)*math.sin(pitch))*3.2;marker.ellipse((mx-4,my-4,mx+4,my+4),outline='#55E3ED',width=2);marker.text((mx+7,my-10),'server fire origin',fill='#55E3ED')
    canvas.paste(panel,(x,y+25));d.text((x+12,y+7),('BEFORE ' if old else 'AFTER ')+key,fill='#DCE2DD')
  d.text((12,canvas.height-16),'Native geometry/actual UV; same camera & scale. '+('TOP: lower-body cut y>=8 model units. ' if top else 'METAL UV isolation includes cuffs/gauntlets. ' if metal else '')+'NOT IN-GAME.',fill='#91A382');canvas.save(OUT/name)
 sheet('spin-before-after.png',keys)
 sheet('spin-support-top.png',keys,top=True)
 sheet('spin-metal-uv.png',keys,metal=True)
 sheet('other-actions.png',['combo_'+str(t) for t in [10,22,34]]+['charge_'+str(t) for t in [0,10,16,22]]+['transition_'+str(t) for t in [0,20,30,40,60]]+['wing_'+str(t) for t in [0,8,20,36]]+['landing','BREATH'])
 sheet('breath-mouth-source.png',['fire_18','fire_30','fire_41'],old_new=False)
 print(OUT)
if __name__=='__main__':main()
