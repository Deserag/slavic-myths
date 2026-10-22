"""Reproducible #164 split geometry, overlaid after the canonical static family."""
from pathlib import Path
import copy,json,hashlib
import weapons87_flails as f
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';O=R/'docs/implementation/weapons87'
def apply():
 owned=[]
 def write(name,data):
  p=A/'models/item'/(name+'.json');p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');owned.append(p)
 for material,id in f.IDS.items():
  display=copy.deepcopy(f.DISPLAY);display['gui']={'rotation':[0,0,0],'translation':[0,0,0],'scale':[1,1,1]}
  write(id,{'parent':'builtin/entity','gui_light':'front','textures':{'particle':'slavicmyths:item/weapons87_materials'},'display':display})
  write(id+'_handle87',f.model(f.handle(material),{}))
  write(id+'_ball87',f.model(f.shifted(f.ball(material),8,8,8)+f.link([8,11.5,8]),{}))
 write('flail_link87',f.model(f.link([8,8,8]),{}))
 manifest={p.relative_to(A).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in owned}
 out=O/'blueprints/flail-motion';out.mkdir(parents=True,exist_ok=True);(out/'applied-manifest.json').write_text(json.dumps(manifest,indent=2)+'\n')
 return manifest
if __name__=='__main__':apply()
