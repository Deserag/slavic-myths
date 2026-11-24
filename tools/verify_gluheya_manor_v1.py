"""Verify approved Gluheya manor V1 coordinates and walkable routes."""
from pathlib import Path
from collections import Counter, deque
import base64, zlib

root = Path(__file__).resolve().parents[1]
data = zlib.decompress(base64.b64decode((root / 'src/main/resources/data/slavicmyths/winter/gluheya_manor_v1.txt').read_bytes()))
assert len(data) == 39 * 30 * 27
assert zlib.crc32(data) == 2209565596
counts = Counter(data)
assert sum(v for k, v in counts.items() if k) == 6907
assert max(counts) == 33 and counts[19] == 40 and counts[23] == 8
assert counts[3] == 4 and counts[12] == 2 and counts[1] == counts[31] == 2

def block(x,y,z):
    if not (-19<=x<=19 and 0<=y<=26 and -3<=z<=26):
        return 0
    return data[(y*30+z+3)*39+x+19]

def free(x,y,z):
    return all(block(x,yy,z) in (0,11) for yy in (y,y+1))

def route(start,end,y):
    q=deque([start]); seen={start}
    while q:
        x,z=q.popleft()
        if (x,z)==end: return True
        for dx,dz in ((1,0),(-1,0),(0,1),(0,-1)):
            a,b=x+dx,z+dz
            if (a,b) in seen or not (-17<=a<=17 and 3<=b<=24): continue
            if block(a,y-1,b) and free(a,y,b):
                seen.add((a,b)); q.append((a,b))
    return False

for dest in ((0,16),(12,8),(12,19),(-16,9)):
    assert route((0,3),dest,2), dest
for dest in ((12,8),(12,21)):
    assert route((-16,17),dest,9), dest
for i,z in enumerate(range(10,17)):
    assert all(block(x,2+i,z)==27 and free(x,3+i,z) for x in (-17,-16,-15))
assert all(block(0,y,16)==0 for y in range(2,11))
print('Gluheya manor V1: 6907 voxels, 48 lights, approved routes and stair headroom verified')
