"""Read-only NBT inspection of the user's latest test save; never writes world data."""
import gzip,struct,json
from pathlib import Path
def load(path):
 data=memoryview(gzip.decompress(path.read_bytes()));i=0
 def read(fmt):
  nonlocal i
  n=struct.calcsize(fmt);v=struct.unpack_from(fmt,data,i);i+=n;return v[0] if len(v)==1 else v
 def string():
  nonlocal i
  n=read('>H');v=bytes(data[i:i+n]).decode('utf8');i+=n;return v
 def tag(t):
  nonlocal i
  if t in range(1,7):return read({1:'>b',2:'>h',3:'>i',4:'>q',5:'>f',6:'>d'}[t])
  if t==8:return string()
  if t==9:
   k=read('>B');return [tag(k) for _ in range(read('>i'))]
  if t==10:
   v={}
   while (k:=read('>B')):name=string();v[name]=tag(k)
   return v
  if t in [7,11,12]:return [read({7:'>b',11:'>i',12:'>q'}[t]) for _ in range(read('>i'))]
  raise ValueError(t)
 t=read('>B');string();return tag(t)
root=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing/.minecraft/saves')
for p in root.glob('*/level.dat'):
 d=load(p)['Data'];player=d.get('Player',{})
 print(json.dumps({'save':p.parent.name,'difficulty':d.get('Difficulty'),'health':player.get('Health'),'effects':player.get('active_effects',player.get('ActiveEffects')),'armor':[v for v in player.get('Inventory',[]) if v.get('Slot',0)>=100]},ensure_ascii=False))
