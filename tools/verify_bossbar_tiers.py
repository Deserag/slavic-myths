#!/usr/bin/env python3
"""Offline integrity checks for the level-color bossbar policy."""
from pathlib import Path
import csv
import re

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
    if count!=resolved:
        missing.append((str(path.relative_to(SRC)),count,resolved))
    checked.append(str(path.relative_to(SRC)))
for name in ["LomeyaBoss.java","NeveyaBoss.java","ViyBoss.java"]:
    data=(SRC/"sisters"/name).read_text(encoding="utf-8")
    assert "setCustomNameVisible(true)" not in data,name
    assert "setCustomNameVisible(false)" in data,name
    assert "readAdditionalSaveData(" in data,name
assert not missing, f"Unconverted bossbars: {missing}"
audit=ROOT/"docs/audit/creatures-1.9.8.1/CREATURES.csv"
roman={"IV":4,"V":5,"VI":6,"VII":7,"VIII":8}
with audit.open(encoding="utf-8-sig",newline="") as stream:
    rows=list(csv.DictReader(stream))
ranked=[entry for entry in rows if entry["group"] in {"boss","mini_boss"} and entry["danger_level"] in roman]
for entry in ranked:
    identifier=entry["entity_type"].split(":",1)[1]
    rank=roman[entry["danger_level"]]
    assert f'Map.entry("{identifier}",{rank})' in SOURCE,(identifier,rank)
assert len(ranked)==28,len(ranked)
assert len(checked)>=25,checked
print(f"PASS: {len(checked)} bossbar owners use central tier palette; 3 white nameplates disabled")
