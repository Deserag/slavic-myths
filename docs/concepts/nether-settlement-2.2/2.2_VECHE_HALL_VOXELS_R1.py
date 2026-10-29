#!/usr/bin/env python3
# Slavic Myths 2.2-B / VECHE_HALL R1 block-volume source.
# Derived exactly from the owner-reviewed VOXELS_R1.json (3598 entries),
# which remains a review-stage model, not a finished NBT or MC test.
# The compact stream represents all 29x23x18 cells including AIR; nibble 0 = air.
# Order: Y major (0..17), V (-1..21), U (-1..27). Each byte stores two cells.
import base64, gzip, hashlib, json, sys
from collections import Counter

ENCODED = "H4sIAAAAAAAC/+VWPY7aQBh91hqyOBKMJRckJbnAKvQrcwOOQLV1um3DDWi3c1qq9HuFXAEJKb3FDcj82MM3Y3tgDAGTfFrJnn3485s3j+8BGDWl9fSQ57+Le36H5zwvVvzOCzyh7eQLffLr5EKgJmRQuNhWStWeUFOPcTyOZfHLGOxtlQEjDmTYsgCIgDzf8sswwCf5xH4nLyxYHbpwkIlHZGUCXIvKIC8/bFC2FSXbshjZjjfd7eQ7AxbpvgrUSwFitOcE95EENyVit+XgZ8VAUTDALX9yPgdpC3Mrc812CKYUYigUctYyjpdKTX75HgzeKCFLBCIfSm0JocpW1oiqW6m21fJFtfIZ2tryobmtYqAo1Jwn3QpzbUUphEAp5BRTCj/jWs6UNRs9VHlnSFdhW2sa8gkwpPKFQr6szkMWoaPWDCkYVuWztCWEHuOktGZyijUTLmairOn0kAmmFExrrVmUlzXTjaPtscNutqaTrbPtMp6VX97ZKdYkU9OD7fGtFHXMmpsLvbP9ebpASyFnWclrpuB1QOBhKqjIT4EnpUrLE8Ersz3DmoOBPrKVGlK9nvizR83AN9CBF14LPfsW4l+L0iZ9serrCaZAGKD2UF80arZmUwrmhwQvCPGIlyl/jjX9piad1Cldpp6B3hzLh/FmtkWrUaPPszI1PQLdb2peP9CbYzmk8tG2aDU1y/M8N9ClNRPP35pdC4ArpaA70KlCnQ903G+gv7oqop+MyFdMdP4/wY/vjvrpMuoHl9Lf/ln5XHq94+/UPZu6vcFuUR0w9S0Mdou6lKnvy2AdMPUvV3WO/B/hePawcxcAAA=="
SYMBOLS = "BCFGHKN PQRSTUVX".replace(" ", "")  # 15 source symbols
assert SYMBOLS == "BCFGHKNPQRSTUVX"
BLOCKS = {
    "B": "minecraft:polished_blackstone_bricks",
    "C": "minecraft:chiseled_polished_blackstone",
    "F": "minecraft:polished_blackstone",
    "G": "minecraft:iron_bars[north=false,east=false,south=false,west=false]",
    "H": "minecraft:crimson_slab[type=bottom]",
    "K": "minecraft:crimson_trapdoor[facing=north,half=top,open=false]",
    "N": "minecraft:nether_brick_stairs[facing=north,half=bottom,shape=straight]",
    "P": "minecraft:polished_basalt[axis=y]",
    "Q": "minecraft:bookshelf",
    "R": "minecraft:nether_bricks",
    "S": "minecraft:nether_brick_stairs[facing=south,half=bottom,shape=straight]",
    "T": "minecraft:nether_brick_slab[type=top]",
    "U": "minecraft:polished_blackstone_brick_stairs[facing=north,half=bottom,shape=straight]",
    "V": "minecraft:crimson_planks",
    "X": "minecraft:crimson_stairs[facing=south,half=bottom,shape=straight]",
}
EXPECTED_PACK_SHA256 = "8813d5c44e144ba5e6f087584f7acbf8dfa4cf25c0e31f6f9193f0b933035cda"
DIMS = (29, 23, 18)  # (u -1..27, v -1..21, Y 0..17)
CITY_ORIGIN = (43, 13)  # city X=43+u, Z=13+v
SOURCE_COUNT = 3598

def decode():
    data = gzip.decompress(base64.b64decode(ENCODED))
    assert hashlib.sha256(data).hexdigest() == EXPECTED_PACK_SHA256, "Payload checksum mismatch"
    total = DIMS[0] * DIMS[1] * DIMS[2]
    assert len(data) == (total + 1) // 2, "Incorrect packed dimensions"
    voxels = []
    for i in range(total):
        q = (data[i // 2] >> (4 if i % 2 == 0 else 0)) & 15
        if not q:
            continue
        assert 1 <= q <= len(SYMBOLS)
        symbol = SYMBOLS[q - 1]
        u = -1 + i % 29
        v = -1 + (i // 29) % 23
        y = i // (29 * 23)
        voxels.append({
            "u": u, "y": y, "v": v,
            "city_x": 43 + u, "city_z": 13 + v,
            "symbol": symbol, "block": BLOCKS[symbol]
        })
    assert len(voxels) == SOURCE_COUNT, f"Expected {SOURCE_COUNT}, got {len(voxels)}"
    assert sum(Counter(v["symbol"] for v in voxels).values()) == SOURCE_COUNT
    return voxels

if __name__ == "__main__":
    voxels = decode()
    if len(sys.argv) > 1 and sys.argv[1] == "--count":
        print(json.dumps({"total": len(voxels), "by_block": dict(Counter(v["block"] for v in voxels))}, ensure_ascii=False, indent=2))
    else:
        print(json.dumps({"status": "R1_APPROVED_VISUAL_CONCEPT_NOT_GAME_TESTED", "city_origin": CITY_ORIGIN, "size": DIMS, "voxels": voxels}, ensure_ascii=False, indent=2))
