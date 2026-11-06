"""Reproduce Nether furniture using existing authored pine models and vanilla Nether materials."""
import json
from pathlib import Path
R=Path(__file__).resolve().parents[1]/"src/main/resources";A=R/"assets/slavicmyths";D=R/"data/slavicmyths"
KINDS=("chair","stool","bench","table","bedside_cabinet","shelf","wardrobe","weapon_rack")
MODELS=KINDS[:4]+("table_pair_north","table_pair_south","table_pair_east","table_pair_west","bedside_cabinet","shelf","wardrobe_lower","wardrobe_upper","weapon_rack")
def write(p,d):
 p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
def copy(source,target,wood):
 data=json.loads(source.read_text(encoding="utf-8").replace("slavicmyths:block/pine_","slavicmyths:block/"+wood+"_").replace("slavicmyths:pine_","slavicmyths:"+wood+"_"))
 write(target,data)
ru={"chair":"Стул","stool":"Табурет","bench":"Скамья","table":"Стол","bedside_cabinet":"Тумба","shelf":"Полка","wardrobe":"Шкаф","weapon_rack":"Стойка для оружия"}
for wood in ("crimson","warped"):
 for kind in KINDS:
  item=wood+"_"+kind
  for directory in ("blockstates","models/item"):
   copy(A/directory/("pine_"+kind+".json"),A/directory/(item+".json"),wood)
  copy(D/"loot_table/blocks"/("pine_"+kind+".json"),D/"loot_table/blocks"/(item+".json"),wood)
  data=json.loads((D/"recipe"/("pine_"+kind+".json")).read_text(encoding="utf-8").replace("slavicmyths:pine_","minecraft:"+wood+"_"))
  for spec in data.get("key",{}).values():
   if spec.get("item")=="minecraft:"+wood+"_log":spec["item"]="minecraft:"+wood+"_stem"
  data["result"]["id"]="slavicmyths:"+item;write(D/"recipe"/(item+".json"),data)
 for model in MODELS:
  if model=="wardrobe":continue
  tex={"wood":"minecraft:block/"+wood+"_planks","beam":"minecraft:block/stripped_"+wood+"_stem","log":"minecraft:block/"+wood+"_stem_top","particle":"minecraft:block/"+wood+"_planks"}
  write(A/"models/block"/(wood+"_"+model+".json"),{"parent":"slavicmyths:block/pine_"+model,"textures":tex})
 for lang in ("ru_ru","en_us"):
  p=A/"lang"/(lang+".json");d=json.loads(p.read_text(encoding="utf-8"))
  for kind in KINDS:
   name=(ru[kind]+(" из багрового дерева" if wood=="crimson" else " из искажённого дерева")) if lang=="ru_ru" else wood.title()+" "+kind.replace("_"," ").title()
   for prefix in ("block.","item."):d.setdefault(prefix+"slavicmyths."+wood+"_"+kind,name)
  write(p,d)
p=A/"creative_catalog.tsv";s=p.read_text(encoding="utf-8").rstrip()+"\n"
for wood in ("crimson","warped"):
 for kind in KINDS:
  item=wood+"_"+kind
  if item+"\t" not in s:s+=item+"\tfurniture\tfurniture_"+kind+"\n"
p.write_text(s,encoding="utf-8")
p=R/"data/minecraft/tags/block/mineable/axe.json";d=json.loads(p.read_text(encoding="utf-8"))
for wood in ("crimson","warped"):
 for kind in KINDS:
  key="slavicmyths:"+wood+"_"+kind
  if key not in d["values"]:d["values"].append(key)
write(p,d)
