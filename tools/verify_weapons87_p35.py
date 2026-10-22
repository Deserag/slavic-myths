"""#87 P35: five-issue read-only regression coverage. No Minecraft runtime claims."""
from pathlib import Path
import ast, json
R=Path(__file__).resolve().parents[1]
def source(p):
    f=R/p
    assert f.is_file(),p
    return f.read_text(encoding="utf-8")
hands=source("src/main/java/org/slavicmyths/client/WeaponHands.java")
axe=source("src/main/java/org/slavicmyths/item/ThunderAxeItem.java")
storm=source("src/main/java/org/slavicmyths/item/StormStaffItem.java")
generator=source("tools/weapons87_generator_gate.py")
recoil=source("tools/verify_weapons87_crossbow_recoil.py")
checks={
    "162_melee_animation_audit":{
        "native_player_arm_renderer": "PlayerRenderer" in hands,
        "boss_armpose_not_reused_in_player": "WeaponGrip58" not in hands,
        "existing_scythe_wrist": "scytheWrist(pose,sign,motion,horizontal,direction)" in hands,
        "full_ordinary_melee_player_animation": False,
    },
    "163_crossbow":{
        "two_hands": "observedCrossbows=new ItemStack[2]" in hands,
        "recoil_source": "crossbowKick(" in hands and "lastShotClock" in hands,
        "regression_fixture": "elapsed<0||elapsed>=7" in recoil,
        "complete_thirdperson_reload_qa": False,
    },
    "165_polearm_animation_audit":{
        "client_scythe_wrist": "static void scytheWrist(" in hands,
        "custom_scythe_item_geometry": "ScytheGrip.firstPerson(pose,left,horizontal)" in hands,
        "boss_polearm_rig_not_in_player": "WeaponGrip58" not in hands,
        "full_polearm_thirdperson_animation": False,
    },
    "161_vfx":{
        "storm_on_success": "if(e.hurt(" in storm and "ParticleTypes.ELECTRIC_SPARK" in storm,
        "thunder_visual_only": "bolt.setVisualOnly(true)" in axe,
        "server_sync": "world.sendParticles(" in axe and "level.sendParticles(" in storm,
        "multiplayer_visual_qa": False,
    },
    "121_generator":{
        "isolated_directory": "tempfile.TemporaryDirectory(" in generator,
        "no_real_write": "shutil.copytree(SRC,workspace/" in generator,
        "protected_exact_states": "assert len(PROTECTED)==16" in generator,
        "semantic_images": "a.convert('RGBA').tobytes()==b.convert('RGBA').tobytes()" in generator,
        "idempotence": "non_idempotent" in generator,
        "historical_other_entrypoints_tested": False,
    }
}
for group,rows in checks.items():
    for key,ok in rows.items():
        if key.startswith(("full_","complete_","multiplayer_","historical_")):assert not ok
        else: assert ok,(group,key)
report={"scope":"source audit, not owner game acceptance","issues":[162,163,165,161,121],"findings":checks,"runtime":"NOT_RUN"}
out=R/"build/weapons87-p35/source-audit.json";out.parent.mkdir(parents=True,exist_ok=True)
out.write_text(json.dumps(report,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
print("PASS P35: five issue source gates; incomplete player animation and multiplayer are explicitly NOT_DONE")
