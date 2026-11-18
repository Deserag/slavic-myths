#!/usr/bin/env python3
"""Ocean 2.6 resource/saving contract checks; not a Minecraft gameplay test."""
from pathlib import Path
import json
import re

root = Path(__file__).resolve().parents[2]
java = root / "src/main/java/org/slavicmyths/ships"
resources = root / "src/main/resources"
checks = 0

def check(condition, message):
    global checks
    checks += 1
    if not condition:
        raise AssertionError(message)

def source(name):
    return (java / name).read_text(encoding="utf-8")

hull = source("FirstShipBlockHull.java")
entity = source("FirstShipEntity.java")
network = source("FirstShipNetwork.java")
items = source("RaftContent.java")
creative = (root / "src/main/java/org/slavicmyths/registry/ModItemGroup.java").read_text(encoding="utf-8")
shipyard = source("ShipyardUpgrades.java")
client = source("client/FirstShipInteractionClient.java")
for fragment in ("positionRider(driver)", "getDismountLocationForPassenger",
                 "level().noCollision(passenger", "removePassenger", "tryHelm",
                 "migrateLegacy", "FirstShipGeometryVersion", "FirstShipRigLevel",
                 "FirstShipFabricQuality", 'pack.putInt("Version", 2)',
                 '"RigLevel"', '"FabricQuality"', "FirstShipBlockHull.move"):
    check(fragment in entity, "missing captain/save safety: " + fragment)
check("p == driver ||" in entity, "rider/walkers must remain distinct")
check("positionRider(driver)" in entity, "rider must be repositioned after movement")
check("exactCooldownTicks()" in entity and "exactCooldownTicks()" in source("ShipRigging.java"),
      "full cloth bonus cannot be lost to integer rounding")
for required in ("FirstShipDesign", 'pack.put("Design", design.save())',
                 "ShipDesign.load", 'loadedDesign.hullSize().equals("small")'):
    check(required in entity, "future design metadata not saved/validated: " + required)
design = source("ShipDesign.java")
for required in ('"HullSize"', '"HullWood"', '"MastWood"', '"SailColor"',
                 '"FlagPattern"', '"FlagSymbol"', '"CabinStyle"', "VERSION = 1"):
    check(required in design, "future customization schema missing " + required)
check("additionalSailCost = sailModule(fabricQuality)" in entity,
      "every tier upgrade must consume real extra sailcloth")
check("additionalSailRefund = sailModule(fabricQuality)" in entity,
      "every removed tier must return its sailcloth")
check("player.setPos(" not in source("FirstShipDeckEvents.java"),
      "no teleport-player-per-tick adapter")
for fragment in ("clear(level", "BlockEntity.loadStatic", "saveWithFullMetadata",
                 "level.removeBlockEntity", "restoreCargo", "canWrite",
                 "setBlock", "migrateLegacy", "private static void addSail",
                 "Rotation.CLOCKWISE_90", "Blocks.SPRUCE_FENCE"):
    check(fragment in hull, "missing native hull/container safety: " + fragment)
for level in (1, 2, 3, 4):
    check(f"case {level} ->" in hull, "missing rig tier geometry")
check('for (int y = 1; y <= 3; y++)' in hull, "dry hold/cabin not accounted for")
# The workshop now owns all player-facing rig transactions. The old native
# configureRigging function is retained for legacy save/test compatibility,
# but the client must no longer fire direct mast mounting packets.
for fragment in ("RIG_CONFIGURE", "RIG_REMOVE"):
    check(fragment in network, "legacy rig packet id missing: " + fragment)
check("configureRigging" in entity, "legacy ship migration/test API was removed")
check("outfitFirstShip" in shipyard and "SAIL_RIGGING_KIT" in shipyard,
      "workshop no longer handles ship rig transactions")
check("configureRigging(player" not in network,
      "forbidden direct in-world rigging remains reachable through network")
check("FirstShipNetwork.RIG_CONFIGURE" not in client,
      "client still sends old mast install packets")
check("action >= 20 && action <= 25" in source("ShipyardMenu.java"),
      "workshop UI buttons cannot reach server rig transactions")
check("countItem(RaftContent.SAIL_MODULE.get()) >= 1" not in shipyard.split("public static boolean canBuildFirstShip")[1],
      "bare hull cannot require automatic sail")
check("countTag(player, net.minecraft.tags.ItemTags.WOOL) >= 20" not in shipyard,
      "bare hull cannot charge for unused fabric")

ids = {
    "reinforced_sailcloth": "REINFORCED_SAILCLOTH",
    "ship_sail_linen_module": "SAIL_LINEN_MODULE",
    "ship_sail_reinforced_module": "SAIL_REINFORCED_MODULE",
    "ship_rigging_upgrade": "SAIL_RIGGING_KIT",
}
lang = json.loads((resources / "assets/slavicmyths/lang/ru_ru.json").read_text(encoding="utf-8"))
for ident, register in ids.items():
    check(register in items and f'"{ident}"' in items, "unregistered item " + ident)
    check(register in creative, "not in Creative: " + ident)
    check(f"item.slavicmyths.{ident}" in lang, "missing Russian name: " + ident)
    model = json.loads((resources / f"assets/slavicmyths/models/item/{ident}.json").read_text(encoding="utf-8"))
    check("parent" in model and "textures" in model, "missing icon model: " + ident)
    recipe = json.loads((resources / f"data/slavicmyths/recipe/{ident}.json").read_text(encoding="utf-8"))
    check(recipe["type"] == "minecraft:crafting_shaped", "not vanilla shaped recipe")
    check(recipe["result"]["id"] == "slavicmyths:" + ident, "wrong output")
    check(all(isinstance(ingredient, dict) and len(ingredient) == 1
              and ("item" in ingredient or "tag" in ingredient)
              for ingredient in recipe["key"].values()), "invalid 1.21.1 item/tag ingredient")
    check(all(all(c == ' ' or c in recipe["key"] for c in row) for row in recipe["pattern"]),
          "unknown crafting ingredient in " + ident)
check(json.loads((resources / "data/slavicmyths/recipe/linen_cloth.json").read_text(encoding="utf-8"))
      ["result"]["count"] == 2, "linen fabric craft balance")
strong = json.loads((resources / "data/slavicmyths/recipe/reinforced_sailcloth.json").read_text(encoding="utf-8"))
check(strong["pattern"] == [" L ", "LWL", " L "], "reinforced four-linen wool recipe")
check(strong["key"]["L"] == {"item": "slavicmyths:linen_cloth"}, "reinforced linen ingredient")
for recipe_path in (resources / "data/slavicmyths/recipe").glob("*.json"):
    if recipe_path.name not in {"linen_cloth.json", "reinforced_sailcloth.json"} \
       and not recipe_path.stem.startswith(("ship_", "shipyard_")):
        continue
    recipe = json.loads(recipe_path.read_text(encoding="utf-8"))
    check(all(isinstance(ingredient, dict) and len(ingredient) == 1
              and ("item" in ingredient or "tag" in ingredient)
              for ingredient in recipe["key"].values()),
          "Minecraft 1.21.1 will silently reject recipe " + recipe_path.name)
print("PASS Ocean 2.6 audit:", checks,
      "static registration, cargo, recipe, geometry and save checks; runtime NOT tested")
