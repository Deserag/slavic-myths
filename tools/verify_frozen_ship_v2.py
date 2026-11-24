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

# Check the exact rigging delta used by the runtime builder against the original blueprint.
removed = {(-24,12,-9),(-24,12,9),(19,17,-5),(19,17,5)}
anchors = {(-20,11,-9),(-20,11,9),(-3,12,-9),(-3,11,-9),(-3,12,9),(-3,11,9),(1,12,-9),(1,12,9),(1,11,9),(13,11,-8),(13,11,8),(17,11,8),(18,12,7),(18,11,7)}
assert all(at(*p)==16 for p in removed)
assert all(at(*p)==0 for p in anchors)
assert at(-42,15,0)==0 and at(-42,14,0)==5 and at(-42,16,0)!=0
directions = ((1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1))
ropes = {(x,y,z) for x in range(-44,39) for y in range(-1,37) for z in range(-23,23) if at(x,y,z)==16}
ropes = (ropes-removed)|anchors
assert len(ropes)==442
while ropes:
    component=[ropes.pop()]
    attachment=set()
    for x,y,z in component:
        for dx,dy,dz in directions:
            p=(x+dx,y+dy,z+dz)
            if p in ropes:
                ropes.remove(p)
                component.append(p)
            elif -44<=p[0]<=38 and -1<=p[1]<=36 and -23<=p[2]<=22 and at(*p) in (4,6,7,8,11,12,14,15,17,18,19,21):
                attachment.add(p)
    assert len(component)>1 and len(attachment)>=2, (component,attachment)
print("Rigging: connected components anchored to solid structure; bow-chain gap closed")
