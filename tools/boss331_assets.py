"""Trophy-only UV repack using the actual four original mask textures, no concept substitution."""
from pathlib import Path
import json
from PIL import Image
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths'
def net(p):
 x,y,z=p['size'];u,v=p['uv']
 return [(u+z,v,x,z),(u+z+x,v,x,z),(u,v+z,z,y),(u+z,v+z,x,y),(u+z+x,v+z,z,y),(u+2*z+x,v+z,x,y)]
def posten_trophy():
 path=A/'lost_halls_rigs/posten_trophy.json';d=json.loads(path.read_text(encoding='utf-8'))
 old={p['name']:p for p in json.loads((A/'lost_halls_rigs/posten.json').read_text())['parts']}
 images=[Image.open(A/f'textures/entity/lost_halls/posten{s}.png').convert('RGBA') for s in ['', '_glow']]
 out=[Image.new('RGBA',(512,512)) for _ in images];u=v=row=0
 for p in d['parts']:
  x,y,z=p['size'];w,h=2*(x+z),y+z
  if u+w+2>512:u=0;v+=row+2;row=0
  assert v+h+2<=512
  p['uv']=[u,v];row=max(row,h);u+=w+2
  for target,source in zip(out,images):
   for a,b in zip(net(old[p['name']]),net(p)):
    ax,ay,aw,ah=a;bx,by,bw,bh=b
    target.paste(source.crop((ax,ay,ax+aw,ay+ah)).resize((bw,bh),Image.Resampling.NEAREST),(bx,by))
 for image,suffix in zip(out,['','_glow']):image.save(A/f'textures/entity/lost_halls/posten_trophy{suffix}.png')
 path.write_text(json.dumps(d,indent=2)+'\n')
