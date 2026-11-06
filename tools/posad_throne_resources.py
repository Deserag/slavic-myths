"""Single-block Posad throne: basalt legs, blackstone back, crimson cushion and gold caps."""
from pathlib import Path
import json
R=Path(__file__).resolve().parents[1]/"src/main/resources"
A=R/"assets/slavicmyths";D=R/"data/slavicmyths"
def write(p,data):
 p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
def cube(a,b,texture):
 return {"from":a,"to":b,"faces":{face:{"texture":"#"+texture} for face in ("north","south","east","west","up","down")}}
model={"parent":"minecraft:block/block","textures":{"stone":"minecraft:block/polished_blackstone","basalt":"minecraft:block/polished_basalt_side","gold":"minecraft:block/gold_block","cushion":"slavicmyths:block/furniture_cloth","particle":"minecraft:block/polished_blackstone"},"elements":[]}
parts=[([2,0,2],[5,7,5],"basalt"),([11,0,2],[14,7,5],"basalt"),([2,0,11],[5,7,15],"basalt"),([11,0,11],[14,7,15],"basalt"),([2,6,2],[14,8,15],"stone"),([4,8,3],[12,9,12],"cushion"),([2,8,3],[4,11,14],"stone"),([12,8,3],[14,11,14],"stone"),([2,9,12],[14,24,15],"stone"),([5,11,11.8],[11,22,12],"cushion"),([2,23,12],[5,26,15],"gold"),([11,23,12],[14,26,15],"gold"),([6,24,12],[10,26,15],"gold")]
model["elements"]=[cube(*part) for part in parts]
write(A/"models/block/posad_throne.json",model)
write(A/"models/item/posad_throne.json",{"parent":"slavicmyths:block/posad_throne"})
write(A/"blockstates/posad_throne.json",{"variants":{"facing="+f:{"model":"slavicmyths:block/posad_throne","y":y} for f,y in zip(("north","east","south","west"),(0,90,180,270))}})
write(D/"loot_table/blocks/posad_throne.json",{"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":"slavicmyths:posad_throne"}],"conditions":[{"condition":"minecraft:survives_explosion"}]}]})
write(D/"recipe/posad_throne.json",{"type":"minecraft:crafting_shaped","category":"building","pattern":["BGB","BCB","BBB"],"key":{"B":{"item":"minecraft:polished_blackstone"},"G":{"item":"minecraft:gold_ingot"},"C":{"item":"minecraft:crimson_planks"}},"result":{"id":"slavicmyths:posad_throne","count":1}})
for lang,name in (("ru_ru","Трон посадника"),("en_us","Posadnik’s throne")):
 p=A/"lang"/(lang+".json");d=json.loads(p.read_text(encoding="utf-8"));d["block.slavicmyths.posad_throne"]=name;d["item.slavicmyths.posad_throne"]=name;write(p,d)
p=R/"data/minecraft/tags/block/mineable/pickaxe.json";d=json.loads(p.read_text(encoding="utf-8"))
if "slavicmyths:posad_throne" not in d["values"]:d["values"].append("slavicmyths:posad_throne")
write(p,d)
p=A/"creative_catalog.tsv";s=p.read_text(encoding="utf-8")
if "posad_throne\t" not in s:p.write_text(s.rstrip()+"\nposad_throne\tfurniture\tfurniture_chair\n",encoding="utf-8")
