from pathlib import Path
import base64
import collections
import zlib

source = Path("src/main/resources/data/slavicmyths/winter/frozen_ship_v2.b64")
data = zlib.decompress(base64.b64decode(source.read_bytes()))
assert len(data) == 83 * 46 * 38
assert zlib.crc32(data) == 0xcddb5d6e
assert sum(v != 0 for v in data) == 16022
assert max(data) == 22
counts = collections.Counter(data)
assert counts[4] == 1588 and counts[11] == 1267 and counts[14] == 6147
def at(x, y, z):
    return data[((y+1)*46+z+23)*83+x+44]
# Captain clearance: three by three, four blocks high, on a solid deck.
for x in range(6,9):
    for z in range(-1,2):
        assert at(x,10,z) == 8
        assert all(at(x,y,z) == 0 for y in range(11,15))
# Boarding ramp and lower-deck stairs must remain unobstructed.
for i in range(11):
    assert at(5,i,-23+i) == 20
    assert all(at(5,i+h,-23+i) == 0 for h in (1,2,3))
for i in range(6):
    assert at(12+i,9-i,-2) == 20
    assert all(at(12+i,9-i+h,-2) == 0 for h in (1,2,3))
print("Frozen ship V2: exact 16022-voxel geometry, ice and access checks passed")
