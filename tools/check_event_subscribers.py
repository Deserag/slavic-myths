"""Inspect declared method annotations in production bytecode, without loading client classes."""
import struct,zipfile,sys,json
from pathlib import Path
class Reader:
 def __init__(self,data):self.data=data;self.i=0
 def take(self,n):v=self.data[self.i:self.i+n];self.i+=n;return v
 def u1(self):return self.take(1)[0]
 def u2(self):return struct.unpack('>H',self.take(2))[0]
 def u4(self):return struct.unpack('>I',self.take(4))[0]
def inspect(data):
 r=Reader(data);assert r.u4()==0xCAFEBABE;r.take(4);count=r.u2();cp={};i=1
 while i<count:
  tag=r.u1()
  if tag==1:cp[i]=r.take(r.u2()).decode('utf8',errors='replace')
  elif tag in (3,4):r.take(4)
  elif tag in (5,6):r.take(8);i+=1
  elif tag in (7,8,16,19,20):r.take(2)
  elif tag in (9,10,11,12,17,18):r.take(4)
  elif tag==15:r.take(3)
  else:raise ValueError(tag)
  i+=1
 def value(a):
  tag=chr(a.u1())
  if tag=='e':return (cp.get(a.u2()),cp.get(a.u2()))
  if tag=='c':return cp.get(a.u2())
  if tag=='@':return annotation(a)
  if tag=='[':return [value(a) for _ in range(a.u2())]
  return cp.get(a.u2())
 def annotation(a):
  kind=cp[a.u2()];items={}
  for _ in range(a.u2()):key=cp[a.u2()];items[key]=value(a)
  return kind,items
 def attrs():
  out=[]
  for _ in range(r.u2()):
   name=cp[r.u2()];blob=r.take(r.u4())
   if name in ('RuntimeVisibleAnnotations','RuntimeInvisibleAnnotations'):
    a=Reader(blob);out.extend(annotation(a) for _ in range(a.u2()))
  return out
 r.take(6);r.take(r.u2()*2)
 for _ in range(r.u2()):r.take(6);attrs()
 handlers=0
 for _ in range(r.u2()):
  r.take(6);handlers+=sum(kind=='Lnet/neoforged/bus/api/SubscribeEvent;' for kind,_ in attrs())
 registered=any(kind=='Lnet/neoforged/fml/common/EventBusSubscriber;' for kind,_ in attrs())
 return registered,handlers
def check(jar):
 subscribers=0
 with zipfile.ZipFile(jar) as z:
  for name in z.namelist():
   if not name.endswith('.class') or not name.startswith('org/slavicmyths/'):continue
   registered,handlers=inspect(z.read(name))
   if registered:
    subscribers+=1
    assert handlers>0,'Empty event subscriber would fail client load: '+name
 return subscribers
if __name__=='__main__':print('EVENT_SUBSCRIBERS_PASS count='+str(check(Path(sys.argv[1]))))
