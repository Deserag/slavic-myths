"""Pack the approved V2 CSV (gzip) into the exact Java palette resource."""
import csv
import gzip
import hashlib
import pathlib
import sys
import zlib

PALETTE = [
    "minecraft:chain", "minecraft:cut_sandstone", "minecraft:cyan_terracotta",
    "minecraft:dark_oak_planks", "minecraft:light_blue_stained_glass",
    "minecraft:oxidized_cut_copper", "minecraft:raw_gold_block", "minecraft:red_terracotta",
    "minecraft:sandstone", "minecraft:sea_lantern", "minecraft:smooth_sandstone",
    "minecraft:smooth_sandstone_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]",
    "minecraft:stripped_dark_oak_log",
]
W,D,H=47,89,59
data=bytearray(W*D*H)
with gzip.open(sys.argv[1],"rt",newline="") as f:
    for row in csv.DictReader(f):
        x,y,z=(int(row[k]) for k in ("x","y","z"))
        assert -23<=x<=23 and 0<=y<H and -65<=z<=23
        i=(y*D+z+65)*W+x+23
        assert not data[i], "duplicate coordinate"
        data[i]=PALETTE.index(row["block"])+1
assert sum(v!=0 for v in data)==13930
assert hashlib.sha256(data).hexdigest()=="d4d42cc599f44777a1f509eb4bb4158f040876b15003ea8537e4721b2f3a631b"
pathlib.Path(sys.argv[2]).write_bytes(zlib.compress(data,9))
