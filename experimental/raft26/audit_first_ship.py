#!/usr/bin/env python3
"""Static first-ship regression audit. Does not claim in-game physics is proven."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
S = ROOT / "src/main/java/org/slavicmyths/ships"
ASSETS = ROOT / "src/main/resources/assets/slavicmyths"
checks = 0

def verify(condition, explanation):
    global checks
    checks += 1
    if not condition:
        raise AssertionError(explanation)

def src(name):
    return (S / name).read_text(encoding="utf-8")

def contains(name, *parts):
    text = src(name)
    for part in parts:
        verify(part in text, "%s lacks %s" % (name, part))

hull = src("FirstShipHull.java")
entity = src("FirstShipEntity.java")
render = src("client/FirstShipRenderer.java")
creative = (ROOT / "src/main/java/org/slavicmyths/registry/ModItemGroup.java").read_text(encoding="utf-8")
reg = src("RaftContent.java")
model = json.loads((ASSETS / "models/item/ship_wooden_7x11.json").read_text(encoding="utf-8"))

contains("FirstShipHull.java", "WIDTH = 7", "LENGTH = 11", "holdBeam", "halfBeam", "mainDeckSupports", "holdSupports")
contains("client/FirstShipRenderer.java", "FirstShipHull.halfBeam", "FirstShipHull.holdSupports",
         "FirstShipHull.mainDeckSupports", "FirstShipHull.HELM_Z", "FirstShipHull.MAST_Z",
         "FirstShipHull.LADDER_X", "FirstShipHull.LADDER_Z", "layer < 9")
contains("FirstShipEntity.java", "FirstShipNavigation.clear", "loadPacked", "storeModules",
         "quarantinedModules", "structuralCell", "tickShipFurnaces", "tryHelm", "tryBoard")
contains("FirstShipNavigation.java", "FirstShipHull.WIDTH", "FirstShipHull.LENGTH",
         "FluidTags.WATER", "getCollisionShape", "FirstShipHull.toWorld")
contains("FirstShipNetwork.java", "playToServer", "MODULE_MAIN", "MODULE_OFF", "player.isShiftKeyDown()")
contains("FirstShipModuleEvents.java", "PacketDistributor.sendToServer", "RightClickItem")
contains("client/FirstShipInventoryHud.java", "InventoryScreen", "FirstShipHull", "moduleKind")
contains("ShipyardUpgrades.java", "canBuildFirstShip", "buildFirstShip")
contains("ShipyardMenu.java", "action == 11", "buildFirstShip")
contains("client/RaftClient.java", "RaftContent.FIRST_SHIP.get()", "FirstShipRenderer::new")
verify("output.accept(org.slavicmyths.ships.RaftContent.FIRST_SHIP_ITEM.get())" in creative,
       "Missing 7x11 ship in Creative > Shipbuilding")
verify('"ship_wooden_7x11"' in reg and '"ship_wooden_7x11_entity"' in reg,
       "First ship registry IDs missing")
verify('"raft_2511_test"' in reg, "Historical raft registry ID lost")
verify("new FirstShipItem(new Item.Properties().stacksTo(1))" in reg,
       "Ship must remain stack-to-1 item")
verify(model["parent"] == "minecraft:block/block", "Ship item model must be a 3D block model")
verify(len(model["elements"]) >= 20, "Ship item icon too rudimentary")
for idx, el in enumerate(model["elements"]):
    frm, to = el["from"], el["to"]
    for axis in range(3):
        verify(0 <= frm[axis] < to[axis] <= 16,
               "Model element %d axis %d outside inventory 16-block box" % (idx, axis))
    for side in ("north", "south", "east", "west", "up", "down"):
        verify(side in el["faces"], "Model element %d missing %s" % (idx, side))
        tex = el["faces"][side]["texture"]
        verify(tex[1:] in model["textures"], "Unknown model texture %s" % tex)
verify(all(0.05 < v <= 0.7 for v in model["display"]["gui"]["scale"]),
       "Icon must be scaled to fit a Creative slot")

print("PASS first ship static audit:", checks, "checks (not a Minecraft runtime test)")
