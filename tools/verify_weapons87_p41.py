"""P41 five unrelated weapon-art issues: real model geometry, original GUI, canonical overlay and packaged byte checks."""
from pathlib import Path
import json,sys,zipfile
from weapons87_p41_overlay import ADDITIONS,BASE_COUNTS,check as verify_overlay
ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/"src/main/resources/assets/slavicmyths"
MODELS=ASSETS/"models/item"
def read(name):
 return json.loads((MODELS/(name+".json")).read_text(encoding="utf-8"))
verify_overlay()
sickle=read("noon_sickle_held")
assert len(sickle["elements"])==12
assert {e["faces"]["north"]["texture"] for e in sickle["elements"]}=={"#wood","#iron","#gold"}
assert max(e["to"][0] for e in sickle["elements"])>=15
assert min(e["from"][1] for e in sickle["elements"])>=1
assert any(e["from"][1]>=13 and e["to"][0]>14 for e in sickle["elements"])
assert read("noon_sickle_icon")["textures"]["layer0"]=="slavicmyths:item/noon_sickle"
assert (ASSETS/"textures/item/noon_sickle.png").exists()
for name,n in BASE_COUNTS.items():
 d=read(name)
 assert len(d["elements"])==n+len(ADDITIONS[name])
 assert len(d["display"])>=6
 for e in d["elements"]:
  for a,b in zip(e["from"],e["to"]):
   assert -16<=a<b<=32,(name,a,b)
  for face in e["faces"].values():
   assert face["texture"].startswith("#")
   assert face["texture"][1:] in d["textures"],(name,face["texture"])
   if "uv" in face:assert all(0<=v<=16 for v in face["uv"])
assert len(ADDITIONS["nightingale_dagger"])==4
assert max(e["to"][1] for e in ADDITIONS["nightingale_dagger"])<9  # no elongation of dagger
assert len(ADDITIONS["victor_spear"])==4
assert max(e["to"][1] for e in ADDITIONS["victor_spear"])<28
assert len(ADDITIONS["thunder_axe"])==6
assert len(ADDITIONS["pool_spear"])==4
if "--jar" in sys.argv:
 jars=list((ROOT/"build/libs").glob("slavicmyths-*.jar"))
 assert len(jars)==1,jars
 with zipfile.ZipFile(jars[0]) as z:
  for name in list(BASE_COUNTS)+["noon_sickle","noon_sickle_held","noon_sickle_icon"]:
   path="assets/slavicmyths/models/item/"+name+".json"
   assert z.read(path)==(ROOT/"src/main/resources"/path).read_bytes(),path
print("PASS P41: noon 3D sickle, four additive unique-held weapon designs, original GUI, generator-safe overlays; Minecraft NOT_RUN")
