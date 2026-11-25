import json, math, struct, gzip, importlib.util, os, tempfile
from pathlib import Path
import numpy as np
here=Path(__file__).resolve().parent
with tempfile.TemporaryDirectory() as work:
 os.environ['INDRIK_WORK']=work
 os.environ['INDRIK_HEADLESS']='1'
 spec=importlib.util.spec_from_file_location('rig',here/'prototype.py')
 rig=importlib.util.module_from_spec(spec);spec.loader.exec_module(rig)
 ref=json.loads((here/'approved_geometry.json').read_text())
 actual=json.loads((Path(work)/'indrik_v3_geometry.json').read_text())
 if ref!=actual: raise ValueError('Generated geometry differs from approved V3')
bones=list(ref['pivots']); cubes=ref['cuboids']
actions=['idle','walk','run','claw','tail','quake','burrow','charge','mineral','roar','enraged','death','hurt']
def pose(k,t):
 if k in ('claw','tail','quake'):return rig.pose(k,t)[:2]
 a={};s=np.zeros(3);goals={n:rig.P[n+'2'].copy() for n in rig.LEGS};pitch={n:0 for n in rig.LEGS};u=math.sin(math.pi*t)**2
 if k in ('idle','enraged'):s[1]=.18*math.sin(2*math.pi*t)**2;a['neck']=(1.4*math.sin(2*math.pi*t),0,0)
 elif k in ('walk','run','charge'):
  amp=1.8 if k=='walk' else 3.0;lift=1.3 if k=='walk' else 2.4;phase=2*math.pi*t
  if k=='charge':a['neck']=(-9*u,0,0);a['head']=(-12*u,0,0);amp*=u;lift*=u;phase*=3
  for n in rig.LEGS:
   q=phase+(math.pi if n in ('FL','HR') else 0);goals[n]+=np.array([0,lift*max(0,math.sin(q)),amp*math.cos(q)*(u if k=='charge' else 1)]);pitch[n]=8*math.sin(q)*(u if k=='charge' else 1)
  s[1]=-.35*abs(math.sin(phase));a['lumbar']=(2*math.sin(phase),0,0)
  for i in range(6):a['tail'+str(i)]=(0,2.2*math.sin(phase-i*.5),0)
 elif k=='burrow':s[1]=float(rig.keys(t,[(0,0),(.22,-20),(.78,-20),(1,0)]));a['neck']=(-10*u,0,0);a['head']=(-16*u,0,0)
 elif k=='mineral':a['neck']=(9*u,0,0);a['head']=(14*u,0,0);a['chest']=(3*u,0,0)
 elif k=='roar':a['neck']=(12*u,0,0);a['head']=(19*u,0,0);a['chest']=(4*u,0,0)
 elif k=='hurt':a['neck']=(-5*u,0,0);a['chest']=(-3*u,0,0)
 elif k=='death':s[1]=-2.5*rig.smooth(t);a['body']=(0,0,24*rig.smooth(t));a['neck']=(-10*rig.smooth(t),0,0)
 if k=='burrow':
  for n in goals:goals[n]+=s
 a['goals']={n:(goals[n],pitch[n]) for n in rig.LEGS};return a,s
out=Path(os.environ['INDRIK_RESOURCE_ROOT'])/'assets/slavicmyths/indrik/geometry.bin.gz'
out.parent.mkdir(parents=True,exist_ok=True)
with gzip.GzipFile(filename=str(out),mode='wb',mtime=0) as f:
 def write(fmt,*v):f.write(struct.pack('>'+fmt,*v))
 write('4i',0x494e4433,len(cubes),len(bones),len(actions))
 for b in bones:
  bb=b.encode();write('H',len(bb));f.write(bb)
 for c in cubes:
  p=(c['x'],c['y'],c['z']);m=c['material'];g=c['bone'];mask=0
  for i,d in enumerate(rig.DIRS):
   q=tuple(np.array(p)+d)
   if q not in rig.V or rig.G[q]!=g:mask|=1<<i
  col=int(ref['palette'][m][1:],16);flags=(1 if m in ('eye','crystal','crystal_dark') else 0)|(2 if p[1]>=20 and p[2]>=-10 and g!='head' else 0)
  write('bbbBBBBI',*p,bones.index(g),mask,flags,0,0xff000000|col)
 for k in actions:
  write('i',41)
  for t in np.linspace(0,1,41):
   a,s=pose(k,float(t));M,O,_=rig.matrices(a,s)
   for b in bones:write('12f',*np.column_stack((M[b],O[b])).flatten())
print('Exported',len(cubes),'cuboids',len(bones),'bones',len(actions),'actions',out.stat().st_size,'bytes')
