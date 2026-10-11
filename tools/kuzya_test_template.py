"""Bounded isolated GameTest fixture; full hut lies inside the engine's ticking test volume."""
from pathlib import Path
import gzip,struct
R=Path(__file__).resolve().parents[1]
def named(t,n,v):
 b=n.encode();return bytes([t])+struct.pack('>H',len(b))+b+v
def string(s):b=s.encode();return struct.pack('>H',len(b))+b
tag=b'\x0a\x00\x00'+named(3,'DataVersion',struct.pack('>i',3955))+named(9,'size',b'\x03'+struct.pack('>iiii',3,50,18,50))+named(9,'palette',b'\x0a'+struct.pack('>i',1)+named(8,'Name',string('minecraft:air'))+b'\x00')+named(9,'blocks',b'\x0a'+struct.pack('>i',0))+named(9,'entities',b'\x0a'+struct.pack('>i',0))+b'\x00'
p=R/'tools/kuzya-runtime-resources/data/slavicmyths_kuzya/structure/empty.nbt';p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(gzip.compress(tag,mtime=0))
