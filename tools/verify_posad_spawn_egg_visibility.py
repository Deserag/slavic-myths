"""Regression: settlement profession eggs stay registered for old saves but hidden from Creative."""
from pathlib import Path
import re
root = Path(__file__).resolve().parents[1]
items = (root / "src/main/java/org/slavicmyths/registry/ModItems.java").read_text(encoding="utf-8")
catalog = (root / "src/main/resources/assets/slavicmyths/creative_catalog.tsv").read_text(encoding="utf-8")
entries = {row.split("\t")[0]: row.split("\t")[1] for row in catalog.splitlines() if row and not row.startswith("#")}
eggs = set(re.findall(r'"(posad_[a-z_]+_spawn_egg)"', items))
assert len(eggs) == 17, f"Expected 17 legacy settlement eggs, found {len(eggs)}"
assert all(entries.get(egg) == "hidden" for egg in eggs), f"Creative exposes: {sorted(egg for egg in eggs if entries.get(egg) != 'hidden')}"
assert len(entries) == len([r for r in catalog.splitlines() if r and not r.startswith("#")]), "Duplicate catalog entries"
main = (root / "src/main/java/org/slavicmyths/SlavicMyths.java").read_text(encoding="utf-8")
item_group = (root / "src/main/java/org/slavicmyths/registry/ModItemGroup.java").read_text(encoding="utf-8")
assert "bus.addListener(org.slavicmyths.registry.ModItemGroup::hideLegacyPosadEggs);" in main
assert "event.remove(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS)" in item_group
assert "event.remove(stack, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY)" in item_group
print("PASS: 17 legacy settlement spawn eggs hidden; registrations remain available for save compatibility")
