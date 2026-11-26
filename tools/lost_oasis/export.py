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
# Clear the principal entrance aisle. Move the two floor lanterns to the side
# rather than leaving obstacles in the 9-block passage.
for z in range(-57,1):
    for x in range(-4,5):
        for y in range(2,9):
            if v.get((x,z,y))=="lantern":
                side=-6 if x<0 else 6
                if (side,z,2) not in v and (side,z,1) in v:
                    v[side,z,2]="lantern"
            v.pop((x,z,y),None)
# Re-audit the final exported walkable ring (rather than the preview alone).
from collections import deque
import math
def radius(x,z):return math.hypot(x,z/1.08)
def field():
    q=deque(); result={}
    for p,m in v.items():
        if m=="lantern" and 2<=p[2]<=6 and 30<=radius(*p[:2])<=52:
            q.append(p);result[p]=15
    while q:
        x,z,y=q.popleft();n=result[x,z,y]-1
        if n<=0:continue
        for dx,dz,dy in [(1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1)]:
            p=(x+dx,z+dz,y+dy)
            if not(1<=p[2]<=8 and 30<=radius(*p[:2])<=53):continue
            if p in v and v[p] not in ("lantern","water","water2"):continue
            if result.get(p,0)<n:result[p]=n;q.append(p)
    return result
final_walk=[(x,z,2) for x in range(-51,52) for z in range(-56,57) if 40<=radius(x,z)<47 and not(z>=30 and abs(x)<=24) and (x,z,1) in v and (x,z,2) not in v and (x,z,3) not in v]
final_light=field()
# Do not insert lamps in the main aisle; use supported side positions instead.
for _ in range(70):
    dark=[p for p in final_walk if p not in v and final_light.get(p,0)<8 and not(abs(p[0])<=4 and p[1]<=0)]
    if not dark:break
    p=min(dark,key=lambda p:final_light.get(p,0));v[p]="lantern";final_light=field()
final_walk=[p for p in final_walk if p not in v]
assert all(final_light.get(p,0)>=8 for p in final_walk), "Exported gallery light proxy"
print("Final exported gallery light:",len(final_walk),min(final_light[p] for p in final_walk))
for z in range(-57,1):
    for x in range(-4,5):
        assert (x,z,1) in v and all((x,z,y) not in v for y in range(2,9))
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
