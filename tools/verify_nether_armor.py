from pathlib import Path
import json,hashlib
from nether_armor_assets import IDS
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';D=R/'src/main/resources/data/slavicmyths'
def main():
 checks=0
 for i,name in enumerate(IDS):
  j=json.loads((D/f'recipe/{name}.json').read_text(encoding='utf-8'));base='minecraft:diamond_'+['helmet','chestplate','leggings','boots'][i%4];catalyst='slavicmyths:'+('zhara_plate' if i<4 else ['four_faces_crown_core','infernal_armor_heart','navya_armor_weave','blazing_step_seal'][i-4]);assert j['type']=='minecraft:smithing_transform' and j['template']=={'item':'slavicmyths:nether_hardening_template'} and j['base']=={'item':base} and j['addition']=={'item':catalyst} and j['result']=={'id':'slavicmyths:'+name,'count':1};checks+=1
  for wrong in ['minecraft:iron_helmet','minecraft:netherite_chestplate','minecraft:diamond_sword','minecraft:diamond_boots' if i%4!=3 else 'minecraft:diamond_helmet']:
   assert j['base']!={'item':wrong};checks+=1
  assert (A/f'models/item/{name}.json').is_file() and (A/f'textures/item/{name}.png').is_file() and (A/f'textures/armor/nether/{name}.png').is_file();checks+=1
 # Actual cuboid packed atlas rectangles have no overlap.
 rig=json.loads((A/'nether_armor_rigs/infernal_king.json').read_text(encoding='utf-8'));used=set()
 for p in rig['parts']:
  u,v=p['uv'];w,h=p['rect'];pixels={(x,y) for x in range(u,u+w) for y in range(v,v+h)};assert not pixels&used and max(x for x,y in pixels)<rig['atlas'] and max(y for x,y in pixels)<rig['atlas'];used|=pixels;checks+=1
 report={'status':'SOURCE_PASS','checks':checks,'scope':'exact recipes/components declared,32 negatives,native atlas packing and resource files; no recipe menu/equip/runtime/art acceptance'};(R/'docs/implementation/2026-10-29-hell-remaining/g1-source.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8');print(report)
if __name__=='__main__':main()
