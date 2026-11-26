"""Export the approved V1 coordinates, without invoking rendering."""
import gzip, os, struct
from pathlib import Path
from prototype import V, walk, levels
W,D,H=105,117,47
CX,CZ=52,57
materials="air sand stone light old dark brick gold red purple teal water water2 soil leaf leaf2 wood wood2 fire flame lantern iron cloth book white stair_s stair_n stair_e stair_w chest bed_foot bed_head".split()
ids={m:i for i,m in enumerate(materials)}
v=dict(V)
# Real Minecraft blocks instead of illustrative flame voxels and unsupported lanterns.
for (x,z,y),m in list(v.items()):
    if m=="flame": del v[x,z,y]
    elif m=="lantern" and (x,z,y-1) not in v: v[x,z,y-1]="dark"
# All palace stairs retain the approved footprint; replace their treads with oriented stairs.
for base,nxt,xs in [(2,13,[-17,-16,-15,15,16,17]),(13,25,[-13,-12,-11,11,12,13])]:
    for i in range(nxt-base):
        for x in xs:
            v[x,39+i,base+i+1]="stair_s"
            for dy in range(1,4): v.pop((x,39+i,base+i+1+dy),None)
    for x in xs:
        v[x,39+nxt-base,nxt]="light"
# Real accessible containers and beds in the furnished rooms.
for x,z,y in [(x,z,3) for x in [-13,13] for z in [39,48,55]]+[(-12,38,14),(-12,47,14),(-10,57,14),(10,39,14),(12,56,14)]:
    if v.get((x,z,y))=="wood2":
        v[x,z,y]="chest"
        v.pop((x,z,y+1),None)
# Keep the large decorative bed, add two usable beds on its surface.
for x in [-17,-16]:
    v[x,54,16]="bed_foot";v[x,55,16]="bed_head"
# Ensure the palace entrance and four dry crossings are passable.
for x in range(-5,6):
    v[x,32,2]="stair_s"
    for z in range(30,36):
        for y in range(3,10):v.pop((x,z,y),None)
# Preserve a continuous one-block dry floor underneath the exterior entrance.
for x in range(-5,6):
    for z in range(-57,-52):
        v[x,z,-2]="sand";v[x,z,-1]="old";v[x,z,0]="stone";v[x,z,1]="light"
# Actual occupied coordinates determine the active footprint; no terrain edits outside it.
assert len(walk)>1500 and min(levels[p] for p in walk)>=8
assert sum(v.get((x,z,5))=="fire" for x in range(-20,21) for z in range(-22,23))==12
for x in range(-12,13):
    for z in range(-12,13):
        assert v.get((x,z,1)) in ("stone","light","red","gold")
        assert all((x,z,y) not in v for y in range(2,20))
data=bytearray(W*D*H)
for (x,z,y),m in v.items():
    assert -CX<=x<W-CX and -CZ<=z<D-CZ and -2<=y<H-2,(x,z,y)
    data[((x+CX)*D+z+CZ)*H+y+2]=ids[m]
root=Path(os.environ["OASIS_RESOURCE_ROOT"])/"assets/slavicmyths/lost_oasis"
root.mkdir(parents=True,exist_ok=True)
with gzip.open(root/"plan_v1.bin.gz","wb") as f:
    f.write(struct.pack(">5i",0x4f415331,W,D,H,len(materials)))
    f.write(data)
print("Lost Oasis V1:",sum(bool(x) for x in data),"occupied blocks;",W,D,H)
