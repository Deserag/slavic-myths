#!/usr/bin/env python3
"""Audit actual world-block ship wiring and deterministic sealed 7x11 hull."""
import json
import re
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
    content = src(name)
    for fragment in parts:
        verify(fragment in content, name + " lacks " + fragment)

hull = src("FirstShipBlockHull.java")
render = src("client/FirstShipRenderer.java")
deck_events = src("FirstShipDeckEvents.java")
creative = (ROOT / "src/main/java/org/slavicmyths/registry/ModItemGroup.java").read_text(encoding="utf-8")
reg = src("RaftContent.java")
model = json.loads((ASSETS / "models/item/ship_wooden_7x11.json").read_text(encoding="utf-8"))

contains("FirstShipHull.java", "WIDTH = 7", "LENGTH = 11", "MAIN_DECK = 5.00D",
         "HOLD_FLOOR = 1.00D", "hatch", "toWorld", "toLocal")
contains("FirstShipBlockHull.java", "Blocks.SPRUCE_PLANKS", "Blocks.OAK_PLANKS",
         "Blocks.STRIPPED_SPRUCE_LOG", "Blocks.LADDER", "Blocks.SPRUCE_FENCE",
         "Blocks.WHITE_WOOL", "Blocks.WATER", "BlockEntity.loadStatic",
         "public static boolean move", "public static boolean place",
         "level.removeBlockEntity", "getFluidState", "canWrite", "clear(")
contains("FirstShipItem.java", "FirstShipBlockHull.place", "loadPacked", "initializeNativeHull")
contains("FirstShipEntity.java", "baseTick()", "FirstShipBlockHull.move",
         "FirstShipBlockHull.pack", "FirstShipBlockHull.remove",
         "entityData.set(NATIVE", "getFirstPassenger() != driver", "helmStep")
contains("FirstShipNetwork.java", "FORWARD", "BACKWARD", "LEFT", "RIGHT", "PACK",
         "playToServer", "helmStep")
contains("client/FirstShipSteeringClient.java", "ClientTickEvent.Post",
         "options.keyUp.isDown()", "options.keyDown.isDown()",
         "options.keyLeft.isDown()", "options.keyRight.isDown()")
contains("client/FirstShipInventoryHud.java", "FirstShipBlockHull.cargoKind",
         "InventoryScreen")
contains("client/FirstShipRenderer.java", "RaftContent.HELM_MODULE.get()", "items.renderStatic(", "if (ship.isVoyaging())", "FirstShipBlockHull.movingPalette", "renderSingleBlock")
verify("if (ship.isVoyaging())" in render
       and "if (!ship.isNativeHull())" in render,
       "Physical chunk hulls must not be double-rendered; only detached hull renders as contraption")
contains("FirstShipBlockHull.java", "layoutV5(", "migrateGeometryV4(", "blocks.remove(world(anchor, turns, x, y, -4))")
helm_model = json.loads((ASSETS / "models/item/ship_helm_module.json").read_text(encoding="utf-8"))
verify(helm_model["parent"] == "minecraft:block/block" and len(helm_model["elements"]) >= 8,
       "Existing three-dimensional helm model missing")
contains("ShipyardUpgrades.java", "canBuildFirstShip", "buildFirstShip")
contains("ShipyardMenu.java", "action == 11")
contains("client/RaftClient.java", "RaftContent.FIRST_SHIP.get()", "FirstShipRenderer::new")
verify("player.setPos(" not in deck_events and "PlayerTickEvent" not in deck_events,
       "Old player-teleport physics must not be active")
verify("blocks.renderSingleBlock(" not in render,
       "Hull must remain physical vanilla blocks; only existing helm model is rendered")
verify("output.accept(org.slavicmyths.ships.RaftContent.FIRST_SHIP_ITEM.get())" in creative,
       "Missing first ship in Creative")
verify("new FirstShipItem(new Item.Properties().stacksTo(1))" in reg,
       "Ship must be stack-to-1 item")
verify('"raft_2511_test"' in reg, "Legacy raft registry ID removed")

# This is the exact half-width per full-block row in NativeBlockHull.
match = re.search(r"WIDTHS = \{([^}]+)\}", hull)
verify(match is not None, "Block hull row widths not found")
widths = [int(v.strip()) for v in match.group(1).split(",")]
verify(widths == [1, 2, 3, 3, 3, 3, 3, 3, 2, 1, 0],
       "Row widths changed: prow must be one block, midships 7, stern 3")
footprint = {(x, z) for z in range(-5, 6) for x in range(-widths[z + 5], widths[z + 5] + 1)}
verify(len(footprint) >= 55, "Ship has no usable internal floor")
verify((0, 5) in footprint and (-1, 5) not in footprint, "Bow tip must join in one wooden block")
for z in range(-5, 6):
    for x in range(-widths[z + 5], widths[z + 5] + 1):
        verify((x, z) in footprint, "Hole in keel/floor")
        if x > -widths[z + 5]: verify((x - 1, z) in footprint, "Split wooden hull")
        if x < widths[z + 5]: verify((x + 1, z) in footprint, "Split wooden hull")
verify(all((x, z) in footprint for x in (-1, 0) for z in (1, 2)),
       "Ladder hatch has no backing wooden hull")

verify(model["parent"] == "minecraft:block/block", "Creative 3D icon missing")
verify(len(model["elements"]) >= 20, "Ship Creative icon too rudimentary")
for index, el in enumerate(model["elements"]):
    for axis in range(3):
        verify(0 <= el["from"][axis] < el["to"][axis] <= 16,
               "Creative element %d not inside icon bounding box" % index)
    for side in ("north", "south", "east", "west", "up", "down"):
        verify(side in el["faces"], "Creative element missing face")
        verify(el["faces"][side]["texture"][1:] in model["textures"],
               "Creative element references unregistered texture")
print("PASS native-block ship audit:", checks, "static checks; runtime QA still necessary")
