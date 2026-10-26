"""Disposable large empty native structure; no production resources modified."""
import gzip,struct
from pathlib import Path
def string(s):
 b=s.encode();return struct.pack('>H',len(b))+b
def tag(t,n,v):return bytes([t])+string(n)+v
integer=lambda n:struct.pack('>i',n)
nbt=tag(10,'',tag(3,'DataVersion',integer(3955))+tag(9,'size',bytes([3])+integer(3)+b''.join(map(integer,[80,16,80])))+tag(9,'palette',bytes([10])+integer(1)+tag(8,'Name',string('minecraft:air'))+b'\0')+tag(9,'blocks',bytes([10])+integer(0))+tag(9,'entities',bytes([10])+integer(0))+b'\0')
p=Path('tools/prince277-test-resources/data/slavicmyths_prince277/structure/arena.nbt');p.parent.mkdir(parents=True,exist_ok=True)
with p.open('wb') as f:
 with gzip.GzipFile(fileobj=f,mode='wb',mtime=0) as z:z.write(nbt)
