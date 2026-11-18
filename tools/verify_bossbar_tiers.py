#!/usr/bin/env python3
"""Offline integrity checks for the level-color bossbar policy."""
from pathlib import Path
import csv
import re
import hashlib

ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/"src/main/java/org/slavicmyths"
BAR=re.compile(r"new\s+(?:net\.minecraft\.server\.level\.)?ServerBossEvent\s*\(")
SOURCE=(SRC/"combat/BossBarPalette.java").read_text(encoding="utf-8")
assert 'Map.entry("great_poloz",8)' in SOURCE
assert 'Map.entry("viy",8)' in SOURCE
assert all(f"case {n}->BossEvent.BossBarColor.{color}" in SOURCE for n,color in
           [(4,"GREEN"),(5,"YELLOW"),(6,"BLUE"),(7,"RED"),(8,"PURPLE")])
checked=[]
missing=[]
for path in SRC.rglob("*.java"):
    text=path.read_text(encoding="utf-8")
    if not BAR.search(text):
        continue
    count=len(BAR.findall(text))
    resolved=text.count("BossBarPalette.colorFor(getType(),")
    # Preserve the release branch's newer, independently authored Nine Mor UI.
    if path.relative_to(SRC).as_posix()=="ninemor/NineMorSerpent.java":
        assert hashlib.sha256(path.read_bytes().replace(b"\r\n",b"\n")).hexdigest()=="6d0c1537d79c179b31ee5d159e6d83830c098a1d03146c9a3ee8f33d2fb6e6f9"
        continue
    if count!=resolved:
        missing.append((str(path.relative_to(SRC)),count,resolved))
    checked.append(str(path.relative_to(SRC)))
for name in ["LomeyaBoss.java","NeveyaBoss.java","ViyBoss.java"]:
    data=(SRC/"sisters"/name).read_text(encoding="utf-8")
    assert "setCustomNameVisible(true)" not in data,name
    assert "setCustomNameVisible(false)" in data,name
    assert "readAdditionalSaveData(" in data,name
assert not missing, f"Unconverted bossbars: {missing}"
audit=ROOT/"tools/fixtures/boss-ranks.csv"
roman={"IV":4,"V":5,"VI":6,"VII":7,"VIII":8}
with audit.open(encoding="utf-8-sig",newline="") as stream:
    rows=list(csv.DictReader(stream))
ranked=[entry for entry in rows if entry["group"] in {"boss","mini_boss"} and entry["danger_level"] in roman]
for entry in ranked:
    identifier=entry["entity_type"].split(":",1)[1]
    rank=roman[entry["danger_level"]]
    assert f'Map.entry("{identifier}",{rank})' in SOURCE,(identifier,rank)
assert len(ranked)==28,len(ranked)
renderers={
    "LomeyaRenderer.java":"LomeyaBoss",
    "NeveyaRenderer.java":"NeveyaBoss",
    "ViyRenderer.java":"ViyBoss",
    "SistersRenderer.java":"SisterBoss",
    "AgneyaRenderer.java":"AgneyaBoss",
    "LedeyaRenderer.java":"LedeyaBoss",
    "PosadBossRenderer.java":"PosadBoss",
}
for renderer,entity in renderers.items():
    text=(SRC/"client"/renderer).read_text(encoding="utf-8")
    assert f"shouldShowName({entity} boss)" in text,renderer
    assert "return false;" in text,renderer
for boss_file in ["LomeyaBoss.java","NeveyaBoss.java","ViyBoss.java"]:
    text=(SRC/"sisters"/boss_file).read_text(encoding="utf-8")
    assert "BossBarOverlay.PROGRESS" in text,boss_file
assert len(checked)>=25,checked
print(f"PASS: {len(checked)} bossbar owners use central tier palette; 3 white nameplates disabled")
