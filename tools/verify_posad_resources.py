"""Scoped resource/JAR gate for #416. Does not hide the deferred global kitchen failures."""
import hashlib
import json
import zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/"src/main/resources"
ASSETS=RES/"assets/slavicmyths"
KINDS=("chair","stool","bench","table","bedside_cabinet","shelf","wardrobe","weapon_rack")
TEST_ITEMS=("posad_test_proof","posad_test_receipt")
IDS=[wood+"_"+kind for wood in ("crimson","warped") for kind in KINDS]+["posad_cot","posad_throne"]
visited=set()

def read(path):
    return json.loads(path.read_text(encoding="utf-8"))

def own(reference,directory,suffix):
    if reference.startswith("slavicmyths:"):
        path=ASSETS/directory/(reference.split(":",1)[1]+suffix)
        assert path.is_file(),(reference,path)
        return path

def model(reference):
    path=own(reference,"models",".json")
    if path is None or path in visited:
        return
    visited.add(path)
    data=read(path)
    if "parent" in data:
        model(data["parent"])
    for texture in data.get("textures",{}).values():
        if not texture.startswith("#"):
            own(texture,"textures",".png")
    for element in data.get("elements",[]):
        assert len(element["from"])==len(element["to"])==3
        assert all(a<b for a,b in zip(element["from"],element["to"])),path
        assert element.get("faces"),path

def states(value):
    if isinstance(value,dict):
        if "model" in value:
            model(value["model"])
        for child in value.values():
            states(child)
    elif isinstance(value,list):
        for child in value:
            states(child)

def main():
    catalog={}
    for row in (ASSETS/"creative_catalog.tsv").read_text(encoding="utf-8").splitlines():
        if row and not row.startswith("#"):
            fields=row.split("\t")
            assert len(fields)==3
            assert fields[0] not in catalog,("duplicate creative item",fields[0])
            catalog[fields[0]]=fields[1:]
    for identifier in IDS:
        assert identifier in catalog,identifier
        for language in ("ru_ru","en_us"):
            lang=read(ASSETS/"lang"/(language+".json"))
            assert lang.get("block.slavicmyths."+identifier) and lang.get("item.slavicmyths."+identifier),(language,identifier)
        model("slavicmyths:item/"+identifier)
        states(read(ASSETS/"blockstates"/(identifier+".json")))
        if identifier.startswith(("crimson_","warped_")):
            state=read(ASSETS/"blockstates"/(identifier+".json"))
            for variant in state["variants"].values():
                assert variant["model"].startswith("slavicmyths:block/"+identifier),(identifier,"wrong material model",variant)
            assert read(ASSETS/"models/item"/(identifier+".json"))["parent"].startswith("slavicmyths:block/"+identifier),(identifier,"wrong item material")
        recipe=read(RES/"data/slavicmyths/recipe"/(identifier+".json"))
        assert recipe["result"]["id"]=="slavicmyths:"+identifier
        assert recipe["pattern"] and recipe["key"]
        assert {c for row in recipe["pattern"] for c in row if c!=" "}==set(recipe["key"]),identifier
        loot=read(RES/"data/slavicmyths/loot_table/blocks"/(identifier+".json"))
        assert loot["type"]=="minecraft:block" and loot["pools"]
        assert any(e.get("name")=="slavicmyths:"+identifier for pool in loot["pools"] for e in pool["entries"])
    for identifier in TEST_ITEMS:
        assert identifier in catalog
        model("slavicmyths:item/"+identifier)
        for language in ("ru_ru","en_us"):
            assert read(ASSETS/"lang"/(language+".json")).get("item.slavicmyths."+identifier)
    props=dict(line.split("=",1) for line in (ROOT/"gradle.properties").read_text().splitlines() if "=" in line and not line.startswith("#"))
    jar=ROOT/"build/libs"/("slavicmyths-"+props["mod_version"]+".jar")
    with zipfile.ZipFile(jar) as archive:
        names=archive.namelist()
        assert len(names)==len(set(names))
        assert not any(n.startswith(("org/slavicmyths/verify/","org/slavicmyths/verify_native/","org/slavicmyths/verifyclient/","org/slavicmyths/bossdedicated/","tools/smoke/")) for n in names)
        for name in names:
            if name.endswith(".class"):
                assert int.from_bytes(archive.read(name)[6:8],"big")==65,name
        for path in visited:
            assert archive.read(path.relative_to(RES).as_posix())==path.read_bytes(),path
        for identifier in IDS:
            for directory in ("assets/slavicmyths/models/item","assets/slavicmyths/blockstates","data/slavicmyths/recipe","data/slavicmyths/loot_table/blocks"):
                relative=directory+"/"+identifier+".json"
                assert archive.read(relative)==(RES/relative).read_bytes(),relative
    print("PASS scoped Posad resources: items="+str(len(IDS)+len(TEST_ITEMS))+", model closure="+str(len(visited))+", Java21 production JAR without QA classes; sha256="+hashlib.sha256(jar.read_bytes()).hexdigest())
    print("Global kitchen resource gate is separate and remains BLOCKED by owner decision.")

if __name__=="__main__":
    main()
