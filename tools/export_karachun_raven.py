"""Export the editable Karachun Raven Blockbench source to the native Minecraft ModelPart mesh."""
from pathlib import Path
import json,sys

R=Path(__file__).resolve().parents[1]
SOURCE=R/'art/models/karachun_raven.bbmodel'
TARGET=R/'src/main/java/org/slavicmyths/client/NightRavenMesh.java'

def f(n):
 n=0.0 if abs(float(n))<0.0000005 else float(n)
 return f'{n:.4f}F'

def groups(model):
 out={}
 def walk(group,parent=None):
  out[group['name']]={'parent':parent,'origin':group['origin'],'elements':[c for c in group.get('children',[]) if isinstance(c,str)]}
  for child in group.get('children',[]):
   if isinstance(child,dict):walk(child,group['name'])
 for root in model['outliner']:walk(root)
 return out

def uv_offset(element):
 west=element['faces']['west']['uv']
 depth=element['to'][2]-element['from'][2]
 return int(round(west[0])),int(round(west[1]-depth))

def render(model):
 by_id={e['uuid']:e for e in model['elements']};g=groups(model)
 lines=['package org.slavicmyths.client;','import net.minecraft.client.model.geom.*;','import net.minecraft.client.model.geom.builders.*;','// Generated from art/models/karachun_raven.bbmodel by tools/export_karachun_raven.py. Do not edit geometry here.','public final class NightRavenMesh {public static ModelPart create(){var mesh=new MeshDefinition();var root=mesh.getRoot();']
 pivots={}
 for name,bone in g.items():
  origin=bone['origin'];pivot=[origin[0],-origin[1],origin[2]];pivots[name]=pivot
  parent=bone['parent'];pp=pivots[parent] if parent else [0,0,0];builder='CubeListBuilder.create()'
  for eid in bone['elements']:
   e=by_id[eid];u,v=uv_offset(e);lo=e['from'];hi=e['to'];w=hi[0]-lo[0];h=hi[1]-lo[1];d=hi[2]-lo[2]
   local=[lo[0]-pivot[0],-hi[1]-pivot[1],lo[2]-pivot[2],w,h,d]
   builder+=f'.texOffs({u},{v}).addBox('+','.join(map(f,local))+')'
  off=[pivot[0]-pp[0],pivot[1]-pp[1],pivot[2]-pp[2]]
  lines.append(f'var {name}={(parent or "root")}.addOrReplaceChild("{name}",{builder},PartPose.offset('+','.join(map(f,off))+'));')
 lines.append(f'return LayerDefinition.create(mesh,{model["resolution"]["width"]},{model["resolution"]["height"]}).bakeRoot();}}private NightRavenMesh(){{}}}}')
 return '\n'.join(lines)+'\n'

def main():
 generated=render(json.loads(SOURCE.read_text(encoding='utf-8')))
 if sys.argv[1:]==['--check']:
  if not TARGET.exists() or TARGET.read_text(encoding='utf-8')!=generated:raise SystemExit('NightRavenMesh.java is stale; run tools/export_karachun_raven.py')
  print('KARACHUN_RAVEN_MODEL_OK elements=55 bones=7')
 elif not sys.argv[1:]:
  TARGET.write_text(generated,encoding='utf-8');print('WROTE',TARGET.relative_to(R))
 else:raise SystemExit('Supported arguments: --check')

if __name__=='__main__':main()
