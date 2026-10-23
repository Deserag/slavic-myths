"""P40: five real family-art scopes, source/JAR only. No Minecraft/PolyMC launch."""
from pathlib import Path
import json,sys,zipfile
R=Path(__file__).resolve().parents[1]
A=R/"src/main/resources/assets/slavicmyths"
def item(name):
    return json.loads((A/"models/item"/(name+".json")).read_text(encoding="utf-8"))
def cuboid(e):
    assert len(e["from"])==len(e["to"])==3
    assert all(a<b for a,b in zip(e["from"],e["to"]))
    assert set(e["faces"])=={"north","south","east","west","up","down"}
for id,num in (("bone_knife",11),("club",17),("spear",23)):
    m=item(id)
    assert len(m["elements"])==num,(id,len(m["elements"]))
    for index,e in enumerate(m["elements"]):
        if id=="spear" and index in (6,9,12,15,18):
            assert set(e['faces'])=={'north','south','east','up','down'}
            assert m['elements'][index+1]['from'][0]==e['from'][0]
        else:cuboid(e)
    assert "gui" in m["display"] and "thirdperson_righthand" in m["display"]
    assert (A/"textures/item"/(id+".png")).is_file()
knife=item("bone_knife")
assert knife["elements"][-1]["to"][1]==14
club=item("club")
assert club["elements"][-1]["to"][0]>11
spear=item("spear")
assert spear["elements"][-1]["from"][1]>=21
# All charged visual states must carry the same display; predicates stay untouched.
for id,scale in (("short_bow",.78),("heavy_bow",1.0)):
    variants=[item(id+suffix) for suffix in ("","_pulling_0","_pulling_1","_pulling_2")]
    for v in variants:
        assert v["parent"]=="minecraft:item/bow"
        assert v["display"]==variants[0]["display"],id
        assert v["display"]["thirdperson_righthand"]["scale"]==[scale]*3
    assert len(variants[0]["overrides"])==3
    assert [v["predicate"]["pull"] for v in variants[0]["overrides"]]==[0,.65,.9]
assert item("short_bow")["display"]["thirdperson_righthand"]["scale"][0]<item("heavy_bow")["display"]["thirdperson_righthand"]["scale"][0]
# New 3D projectile geometry must retain old GUI sprite and all original hand transforms.
for name, expected in (("stone_throwing_knife",5),("stone_sulitsa",6)):
    root=item(name);held=item(name+"_held");icon=item(name+"_icon")
    assert root["loader"]=="neoforge:separate_transforms"
    assert root["base"]["parent"]=="slavicmyths:item/"+name+"_held"
    assert root["perspectives"]["gui"]["parent"]=="slavicmyths:item/"+name+"_icon"
    assert icon["textures"]["layer0"]=="slavicmyths:item/"+name
    assert len(held["elements"])==expected
    assert len(held["display"])==4
    for e in held["elements"]:cuboid(e)
    assert (A/"textures/item"/(name+".png")).is_file()
    assert held["textures"]["body"]=="slavicmyths:item/weapons87_materials"
    assert held["textures"]["particle"]=="slavicmyths:item/"+name
    assert (A/"textures/item/weapons87_materials.png").is_file()
    assert any(face["uv"]==[12,0,16,4] for e in held["elements"] for face in e["faces"].values())
knife3=item("stone_throwing_knife_held");javelin=item("stone_sulitsa_held")
def reach(x):
    return max(e["to"][1] for e in x["elements"])-min(e["from"][1] for e in x["elements"])
assert reach(javelin)>reach(knife3)*1.5
assert reach(javelin)<23 # no giant thrown spear in the world
# Thrown entity renderer does not apply frame-specific FIXED rotation.
renderer=(R/"src/main/java/org/slavicmyths/client/WeaponProjectileRenderer.java").read_text()
assert "FLIGHT_CONTEXT=ItemDisplayContext.NONE" in renderer
if "--jar" in sys.argv:
    jars=list((R/"build/libs").glob("slavicmyths-*.jar"));assert len(jars)==1
    with zipfile.ZipFile(jars[0]) as z:
        names=["bone_knife","club","spear"]+[id+s for id in ("short_bow","heavy_bow") for s in ("","_pulling_0","_pulling_1","_pulling_2")]+[name+s for name in ("stone_throwing_knife","stone_sulitsa") for s in ("","_held","_icon")]
        for name in names:
            path="assets/slavicmyths/models/item/"+name+".json"
            assert z.read(path)==(R/"src/main/resources"/path).read_bytes(),path
print("PASS P40: three held geometry details, eight matching bow states, two separate 3D thrown archetypes, source/JAR verified; GAME NOT_RUN")
