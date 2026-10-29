#!/usr/bin/env python3
"""Strict offline structural verification for 2.2.7 Nether herbal hut.
Reads an actual gzip-compressed Minecraft NBT file (no external Python packages).
NOT a game load, GameTest, worldgen, or client QA.
"""
from __future__ import annotations

import gzip
import json
import re
import struct
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
MODEL = ROOT / "docs/concepts/nether-settlement-2.2/zeleynaya-izba-2.2.7/MODEL_R1.json"
NBT = ROOT / "src/main/resources/data/slavicmyths/structure/posad/zeleynaya_2_2_7.nbt"
LOOT = ROOT / "src/main/resources/data/slavicmyths/loot_table/chests"
ITEMS = ROOT / "src/main/java/org/slavicmyths/registry/ModItems.java"


class Reader:
    def __init__(self, raw: bytes):
        self.raw = raw
        self.index = 0

    def take(self, amount: int) -> bytes:
        if amount < 0 or self.index + amount > len(self.raw):
            raise ValueError(f"NBT truncated at {self.index}, need {amount} bytes")
        result = self.raw[self.index:self.index + amount]
        self.index += amount
        return result

    def number(self, fmt: str):
        return struct.unpack(">" + fmt, self.take(struct.calcsize(fmt)))[0]

    def text(self) -> str:
        return self.take(self.number("H")).decode("utf-8")

    def tag(self, kind: int):
        if kind == 1:
            return self.number("b")
        if kind == 2:
            return self.number("h")
        if kind == 3:
            return self.number("i")
        if kind == 4:
            return self.number("q")
        if kind == 5:
            return self.number("f")
        if kind == 6:
            return self.number("d")
        if kind == 7:
            return self.take(self.number("i"))
        if kind == 8:
            return self.text()
        if kind == 9:
            member_type = self.number("B")
            length = self.number("i")
            if length < 0:
                raise ValueError("Negative NBT list length")
            return [self.tag(member_type) for _ in range(length)]
        if kind == 10:
            obj = {}
            while True:
                child = self.number("B")
                if child == 0:
                    return obj
                name = self.text()
                if name in obj:
                    raise ValueError(f"Duplicate NBT compound tag {name}")
                obj[name] = self.tag(child)
        if kind == 11:
            length = self.number("i")
            return [self.number("i") for _ in range(length)]
        if kind == 12:
            length = self.number("i")
            return [self.number("q") for _ in range(length)]
        raise ValueError(f"Unsupported NBT type {kind}")

    def root(self):
        if self.number("B") != 10:
            raise ValueError("Root is not an NBT compound")
        self.text()
        value = self.tag(10)
        if self.index != len(self.raw):
            raise ValueError("Trailing binary data after NBT compound")
        return value


def require(ok: bool, explanation: str) -> None:
    if not ok:
        raise AssertionError(explanation)


def main() -> None:
    model = json.loads(MODEL.read_text("utf-8"))
    require(NBT.read_bytes()[:2] == b"\x1f\x8b", "NBT is not gzip")
    compound = Reader(gzip.decompress(NBT.read_bytes())).root()
    require(compound.get("DataVersion") == 3955, "DataVersion mismatch")
    require(compound.get("size") == [25, 13, 26], "Size mismatch")
    blocks = compound.get("blocks")
    palette = compound.get("palette")
    require(isinstance(blocks, list) and len(blocks) == 1599, "Block count mismatch")
    require(isinstance(palette, list) and all("Name" in p for p in palette), "Invalid palette")
    require(compound.get("entities") == [], "Unexpected entities")

    actual = {}
    for b in blocks:
        position = tuple(b["pos"])
        require(len(position) == 3 and 0 <= position[0] < 25 and 0 <= position[1] < 13 and 0 <= position[2] < 26, "Out-of-bounds NBT block")
        require(position not in actual, "Duplicate NBT block")
        require(0 <= b["state"] < len(palette), "Invalid palette index")
        actual[position] = b

    source = {tuple((b["u"], b["y"], b["v"])): b for b in model["blocks"]}
    require(len(source) == len(model["blocks"]) == len(actual), "Source duplicate or missing blocks")
    for position, original in source.items():
        require(position in actual, f"Absent source block {position}")
        entry = actual[position]
        state = palette[entry["state"]]
        require(state["Name"] == original["id"], f"Wrong block ID at {position}")
        properties = {k: str(v).lower() if isinstance(v, bool) else str(v) for k, v in original.get("state", {}).items()}
        require(state.get("Properties", {}) == properties, f"Wrong properties at {position}")

    chests = {tuple(c["local"]): c["loot_table"] for c in model["chests"].values()}
    require(len(chests) == 5, "Expected five chest markers")
    registry = set(re.findall(r'ITEMS\.register\(\s*"([^"]+)"', ITEMS.read_text("utf-8")))
    for position, entry in actual.items():
        if position in chests:
            require(palette[entry["state"]]["Name"] == "minecraft:chest", f"Chest missing at {position}")
            require(entry.get("nbt", {}).get("LootTable") == chests[position], f"Wrong chest loot at {position}")
            require(entry["nbt"].get("id") == "minecraft:chest", "Missing block entity type")
        else:
            require("nbt" not in entry, "Unexpected extra block entity metadata")
    for loot_id in chests.values():
        require(loot_id.startswith("slavicmyths:chests/"), "Unexpected loot namespace")
        name = loot_id.split("/", 1)[1]
        data = json.loads((LOOT / (name + ".json")).read_text("utf-8"))
        require(data.get("type") == "minecraft:chest" and data.get("pools"), f"Invalid loot table {loot_id}")
        ids = set(re.findall(r'"name"\s*:\s*"(slavicmyths:[^"]+)"', json.dumps(data)))
        require(all(item.split(":", 1)[1] in registry for item in ids), f"Unregistered loot item in {loot_id}")

    print(f"PASS: valid gzip NBT, {len(actual)} exact source blocks, {len(palette)} states, {len(chests)} correct chest loot references")
    print("OFFLINE ONLY: Minecraft loader, WorldGen, GameTest, save/reload and multiplayer are not verified")


if __name__ == "__main__":
    main()
