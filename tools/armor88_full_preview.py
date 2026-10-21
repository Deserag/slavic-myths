"""Actual native mesh/UV/PNG A/B, two bodies/four source poses/four views. No game."""
from pathlib import Path
import sys,json,functools,io
import numpy as np
from PIL import Image,ImageDraw
import armor88_preview as paint
R=Path(sys.argv[1]).resolve() if len(sys.argv)>1 else Path(__file__).resolve().parents[1];O=R/'docs/implementation/armor88-full'
@functools.lru_cache(maxsize=256)
def tex(n,before):
 if n.startswith('minecraft:'):return np.array(Image.open(io.BytesIO(paint.Z.read('assets/minecraft/'+n.split(':')[1]))).convert('RGBA'))
 root=O/'before/src/main/resources/assets/slavicmyths' if before else R/'src/main/resources/assets/slavicmyths'
 return np.array(Image.open(root/n).convert('RGBA'))
SKIN_CACHE={}
def render(s,yaw,before):
 paint.texture=lambda n:tex(n,before)
 key=(s['body'],s['pose'],yaw)
 if key not in SKIN_CACHE:SKIN_CACHE[key]=paint.render({'layers':s['layers'][:1]},yaw,return_depth=True)
 return paint.render({'layers':s['layers'][1:]},yaw,base=SKIN_CACHE[key])
def sheet(name,ids,before,after):
 selected=lambda data,body,pose:[s for s in data if s['id'] in ids and s['body']==body and s['pose']==pose]
 out=Image.new('RGB',(2000,2340),(22,27,34));d=ImageDraw.Draw(out);d.text((10,10),name+' / ACTUAL BAKED MODEL + UV + PNG / BEFORE | AFTER',fill='white');d.text((10,28),'OFFLINE SOURCE POSES, SAME CAMERA/SCALE. Not game screenshots; weapon_hold is an arm pose; runtime pending.',fill='white')
 row=0
 for body in ['Steve','Alex']:
  for pose in ['idle','walk_t10','weapon_hold','sneak']:
   y=60+row*285;row+=1;d.text((10,y),body+' / '+pose,fill='white')
   for col,(view,yaw) in enumerate([('FRONT',0),('RIGHT',90),('BACK',180),('3/4',-35),('LEFT',-90)]):
    for side,data in enumerate([before,after]):
     matches=selected(data,body,pose);s=dict(matches[0]);s['layers']=[matches[0]['layers'][0]]+[m['layers'][1] for m in matches]
     x=(col*2+side)*200;out.paste(render(s,yaw,side==0),(x,y+15));d.text((x+5,y+18),view+(' BEFORE' if side==0 else ' AFTER'),fill='white')
 out.save(O/(name+'.png'));print('SOURCE_PREVIEW '+name,flush=True)
def main():
 before=json.loads((O/'before-native.json').read_text());after=json.loads((O/'after-native.json').read_text());ids=list(dict.fromkeys(s['id'] for s in after))
 # Every ID gets all four poses, rather than hiding defects behind a full set.
 for name in ids:sheet(name,[name],before,after)
 for name,items in [('folk_ensemble',['linen_headscarf','linen_shirt','linen_ports','bast_shoes'])]+[(f+'_ensemble',[f+'_'+part for part in ['helmet','chestplate','leggings','boots']]) for f in ['perunite','dragon','poloz']]:sheet(name,items,before,after)
 icons=Image.new('RGB',(720,((len(ids)+3)//4)*86),(41,48,57));d=ImageDraw.Draw(icons)
 for i,name in enumerate(ids):
  x=i%4*180;y=i//4*86;d.text((x+3,y+2),name,fill='white')
  for side,root in enumerate([O/'before',R]):
   p=root/'src/main/resources/assets/slavicmyths/textures/item'/f'{name}.png';im=Image.open(p).convert('RGBA').resize((16,16),Image.Resampling.NEAREST);icons.paste(im,(x+5+side*80,y+20),im);big=im.resize((48,48),Image.Resampling.NEAREST);icons.paste(big,(x+24+side*80,y+20),big)
 icons.save(O/'ICONS-1x-3x.png')
 # Compact reproducible contact sheets complement every full per-ID source sheet.
 names=['gambeson','plate_cuirass','bogatyr_chestplate','trophy_idol_chest','trophy_marinka_cloak','trophy_kartaus_mantle']
 for filename,groups in [('REDESIGN-CONTACT.png',[(n,[n]) for n in names]),('SET-POLISH-CONTACT.png',[(f,[f+'_'+p for p in ['helmet','chestplate','leggings','boots']]) for f in ['perunite','dragon','poloz']])]:
  out=Image.new('RGB',(800,len(groups)*290),(22,27,34));draw=ImageDraw.Draw(out)
  for row,(name,items) in enumerate(groups):
   draw.text((5,row*290+3),name+' / native source, NOT game',fill='white')
   for col,(data,yaw,old) in enumerate([(before,0,True),(after,0,False),(before,180,True),(after,180,False)]):
    matches=[s for s in data if s['id'] in items and s['body']=='Steve' and s['pose']=='idle'];sample=dict(matches[0]);sample['layers']=[matches[0]['layers'][0]]+[m['layers'][1] for m in matches]
    out.paste(render(sample,yaw,old),(col*200,row*290+20));draw.text((col*200+5,row*290+22),('BEFORE' if old else 'AFTER')+(' FRONT' if yaw==0 else ' BACK'),fill='white')
  out.save(O/filename)
 import hashlib
 files=[O/'before-native.json',O/'after-native.json',R/'tools/armor88_full_preview.py',R/'tools/armor88_preview.py']
 for data,base in [(before,O/'before'),(after,R)]:
  files += [base/'src/main/resources/assets/slavicmyths'/s['layers'][1]['texture'] for s in data]
  files += [base/f'src/main/resources/assets/slavicmyths/textures/item/{name}.png' for name in ids]
 inputs={str(p.relative_to(R)).replace('\\','/'):hashlib.sha256(p.read_bytes()).hexdigest() for p in files}
 (O/'preview-input-sha256.json').write_text(json.dumps(inputs,indent=2)+'\n')
 outputs={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in O.glob('*.png')}
 (O/'preview-output-sha256.json').write_text(json.dumps(outputs,indent=2)+'\n')
if __name__=='__main__':main()
