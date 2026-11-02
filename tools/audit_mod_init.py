#!/usr/bin/env python3
"""Non-destructive NeoForge regression checks. Run: python tools/audit_mod_init.py"""
import json
import pathlib
import re
import sys
from collections import defaultdict

ROOT = pathlib.Path(__file__).resolve().parents[1]
JAVA = ROOT / "src/main/java/org/slavicmyths"
RECIPES = ROOT / "src/main/resources/data/slavicmyths/recipe"
errors = []
calls = defaultdict(list)
pattern = re.compile(r"(?m)^\s*(?:[\w$.]+\.)?([\w$]+)\.register\(\s*(?:bus|modEventBus)\s*\)\s*;")
decl_pattern = re.compile(r"\bDeferredRegister\s*(?:<[^;\n]*>)?\s+([\w$]+)\s*=")

for file in JAVA.rglob("*.java"):
    source = file.read_text(encoding="utf-8")
    # These checks intentionally only match direct bus calls, never register("id", supplier).
    for match in pattern.finditer(source):
        owner = match.group(1)
        line = source.count("\n", 0, match.start()) + 1
        calls[owner].append(f"{file.relative_to(ROOT)}:{line}")
    if file.name == "PosadResidentEntity.java":
        for bad in ("import net.minecraft.client.", "import org.slavicmyths.client.", "BasicArmorModels"):
            if bad in source:
                errors.append(f"{file.relative_to(ROOT)}: forbidden common-side dependency: {bad}")

for owner, sites in calls.items():
    # Name-based matches are a warning rather than a failure: same field names may
    # refer to distinct DeferredRegister instances in different classes.
    if len(sites) > 1:
        print(f"REVIEW registrar {owner}: " + ", ".join(sites))

main = (JAVA / "SlavicMyths.java").read_text(encoding="utf-8")
network = (JAVA / "netherboss/NetherTrophyNetwork.java").read_text(encoding="utf-8")
payload = (JAVA / "network/MythPayloads.java").read_text(encoding="utf-8")
if "NetherTrophyNetwork::register" not in main:
    errors.append("NetherTrophyNetwork registration listener missing in SlavicMyths")
if not all(x in network for x in ("RegisterPayloadHandlersEvent", "MythPayloads.NetherTrophy.TYPE", ".playToServer(")):
    errors.append("NetherTrophy network handler not registered for server-bound payload")
if "record NetherTrophy(" not in payload:
    errors.append("NetherTrophy payload declaration missing")
boss = (JAVA / "posad/boss/PosadBoss.java").read_text(encoding="utf-8")
if "spawnAtLocation(ledger.mint(org.slavicmyths.registry.ModItems.MYTAR_SEAL.get(), getUUID(), playerId));" not in boss:
    errors.append("PosadBoss MYTAR_SEAL reward call differs from audited form")

count = 0
for file in RECIPES.rglob("*.json"):
    count += 1
    try:
        data = json.loads(file.read_text(encoding="utf-8"))
    except (ValueError, UnicodeError) as exc:
        errors.append(f"Invalid JSON: {file.relative_to(ROOT)}: {exc}")
        continue
    if not isinstance(data, dict) or not isinstance(data.get("type"), str):
        errors.append(f"Missing recipe type: {file.relative_to(ROOT)}")

print(f"Checked {count} recipe JSON files; {sum(map(len, calls.values()))} direct event-bus registration calls")
if errors:
    for error in errors:
        print("ERROR:", error, file=sys.stderr)
    sys.exit(1)
print("Static checks passed (not a replacement for runClient/runServer)")
