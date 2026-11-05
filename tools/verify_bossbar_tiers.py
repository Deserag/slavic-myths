#!/usr/bin/env python3
"""Offline integrity checks for the level-color bossbar policy."""
from pathlib import Path
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
for path in SRC.rglob("*.java"):
    text=path.read_text(encoding="utf-8")
    if not BAR.search(text):
        continue
    count=len(BAR.findall(text))
    resolved=text.count("BossBarPalette.colorFor(getType(),")
    assert count==resolved,(str(path),count,resolved)
    checked.append(str(path.relative_to(SRC)))
for name in ["LomeyaBoss.java","NeveyaBoss.java","ViyBoss.java"]:
    data=(SRC/"sisters"/name).read_text(encoding="utf-8")
    assert "setCustomNameVisible(true)" not in data,name
    assert "setCustomNameVisible(false)" in data,name
assert len(checked)>=25,checked
print(f"PASS: {len(checked)} bossbar owners use central tier palette; 3 white nameplates disabled")
