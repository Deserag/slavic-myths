#!/usr/bin/env python3
"""Static weapon input regression checks for the 102-item source catalog.

These checks complement, but do not replace, Minecraft GameTests and multiplayer QA.
"""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "src/main/java/org/slavicmyths"
CATALOG = ROOT / "docs/balance/weapon-audit-20261122/catalog.json"


def source(relative: str) -> str:
    return (SOURCE / relative).read_text(encoding="utf-8")


def require(text: str, needle: str, label: str) -> None:
    if needle not in text:
        raise AssertionError(f"{label}: missing {needle!r}")


def main() -> None:
    catalog = json.loads(CATALOG.read_text(encoding="utf-8"))
    ids = [item["id"] for item in catalog]
    assert len(ids) == 102, f"Expected 102 catalog weapon IDs, found {len(ids)}"
    assert len(ids) == len(set(ids)), "Duplicate weapon ID"
    matrix = (ROOT / "docs/requirements/WEAPON_INPUT_102_COVERAGE_20261122.md").read_text(encoding="utf-8")
    mapped = [line.split("`")[1] for line in matrix.splitlines()
              if line.startswith("| `") and "` |" in line]
    assert len(mapped) == len(set(mapped)), "Duplicate weapon in input matrix"
    assert set(mapped) == set(ids), f"Input matrix does not cover catalog: {set(ids) ^ set(mapped)}"

    if (SOURCE / "combat/WeaponInputNetwork.java").exists():
        router=source("client/WeaponInputRouter.java")
        network=source("combat/WeaponInputNetwork.java")
        for token in ["event.setCanceled(true)","mc.screen != null","InteractionHand.MAIN_HAND","attackHeld","useHeld"]:require(router,token,"unified router")
        for token in ["Action.fromId", "player.getMainHandItem()", "handler.intercepts", "weapon_input_request", "player.isAlive()", "player.isSpectator()"]:require(network,token,"server validation")
        for relative in ["marine/MarineWeapons.java","morovoy/MorovoyStaff.java","morozko/FrostStaff.java","karachun/NightScythe.java","ninemor/NineMorNamedItems.java","sisters/BossRewardWeapons.java"]:
            require(source(relative),"WeaponInputHandler",relative)
            require(source(relative),"onWeaponAction",relative)
        assert not (SOURCE / "marine/MarineWeaponControls.java").exists(),"obsolete marine payload route"
        assert "StaffAction" not in source("network/MythPayloads.java"),"obsolete Morovoy payload route"
        require(source("client/WeaponSkillInput.java"),"CrossbowVolley.Toggle", "crossbow retained route")
        print("Static weapon input checks passed: 102 catalog IDs; merged four-action router and server validation; crossbow route retained")
        return

    routed = {
        "abyss_staff": ("MarineWeapons.STAFF.get()", "MarineWeaponControls.Secondary()"),
        "morovoy_staff": ("MorovoyStaff.STAFF.get()", "MythPayloads.StaffAction(1)"),
        "morozko_staff": ("FrostContent.STAFF.get()", "FrostNetwork.Action(1)"),
        "karachun_scythe": ("NightContent.SCYTHE.get()", "NightNetwork.Action(1)"),
    }
    client = source("client/WeaponSkillInput.java")
    bindings = source("combat/WeaponSkillBindings.java")
    for item_id, (item, packet) in routed.items():
        assert item_id in ids, f"Missing catalog item {item_id}"
        require(client, item, item_id)
        require(client, packet, item_id)
        require(bindings, item, item_id + " server cancellation")

    for token in ("event.setCanceled(true)", "event.setSwingHand(false)",
                  "mc.player.isShiftKeyDown()", "mc.screen!=null"):
        require(client, token, "Shift+LMB interception")
    for token in ("AttackEntityEvent", "LeftClickBlock", "event.setCanceled(true)"):
        require(bindings, token, "vanilla attack prevention")

    for name, file in (("Morozko", "morozko/FrostSkills.java"),
                       ("Karachun", "karachun/NightCombat.java")):
        require(source(file), "a==0&&p.isShiftKeyDown()", name + " non-Shift primary packet")

    marine = source("marine/MarineWeaponControls.java")
    morovoy = source("morovoy/MorovoyStaff.java")
    for name, body in (("Abyss", marine), ("Morovoy", morovoy)):
        require(body, "containerMenu", name + " container check")
        require(body, "isShiftKeyDown()", name + " modifier check")

    frost_staff = source("morozko/FrostStaff.java")
    require(frost_staff, "h!=InteractionHand.MAIN_HAND", "Frost main hand")
    frost_events = source("morozko/FrostEvents.java")
    assert frost_events.count("e.getHand()==net.minecraft.world.InteractionHand.MAIN_HAND") >= 2

    nine = source("ninemor/NineMorRewardAbilities.java")
    assert nine.count("if(hand!=InteractionHand.MAIN_HAND)") >= 3

    for relative in (
        "artifact/VelesStaffItem.java",
        "combat/LikhoStaffItem.java",
        "bandit/NightingaleDagger.java",
        "item/StormStaffItem.java",
        "poloz/PolozWhip.java",
        "marine/MarineWeapons.java",
        "nethersmithing/NetherWeapons.java",
    ):
        require(source(relative), "InteractionHand.MAIN_HAND", relative + " main hand")

    require(source("client/MorovoyStaffKeys.java"), "Shift+ЛКМ", "Morovoy HUD")
    assert "MorovoyStaffKeys.Registration" not in source("client/ClientSetup.java"), "Stale key listener"
    require(source("posad/boss/ShackledSickleItem.java"),
            "hand!=InteractionHand.MAIN_HAND", "Mytar chained sickle")
    require(source("gorynych/SerpentCrossbow.java"),
            "hand==InteractionHand.MAIN_HAND&&p.isShiftKeyDown()", "Gorynych crossbow mode")
    for relative in ("depth/PoolSpear.java", "rewards/RewardContent.java"):
        require(source(relative), "InteractionHand.MAIN_HAND", relative + " main hand")

    print(f"Static weapon-input regression checks passed; catalog covers {len(ids)} IDs.")


if __name__ == "__main__":
    main()
