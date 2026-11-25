import os,gzip,struct,inspect
from pathlib import Path
import numpy as np
import soundfile as sf
import lamya_v6 as m
ROOT=Path(os.environ["LAMYA_RESOURCE_ROOT"])
out=ROOT/"assets/slavicmyths/lamya";out.mkdir(parents=True,exist_ok=True)
SCALE=.11;N=9
groups=np.array(m.GV)
names=["torso","skirt","arm-1","arm1","neck-1","neck0","neck1","head-1","head0","head1"]+[f"tail{i}" for i in range(10)]
part=np.full(len(m.V),-1,np.int32)
for i,name in enumerate(names[:10]):part[groups==name]=i
for i,s in enumerate([-1,0,1]):part[groups==f"jaw{s}"]=7+i
part[m.tailmask]=10+np.minimum(9,(10*m.ts/m.S[-1]).astype(int))
part[groups=='halo']=20 # decorative, rendered but not a damageable part
assert np.min(part)>=0
_,bone=np.unique(groups,return_inverse=True)
keys=np.column_stack((np.rint(m.V*100000).astype(np.int64),bone,part))
_,unique,inverse=np.unique(keys,axis=0,return_index=True,return_inverse=True)
inv=inverse.reshape(-1)
quads=[];colors=[];start=0
for face,color in zip(m.F,m.C):
    ids=inv[start:start+len(face)];start+=len(face)
    faces=[ids] if len(ids)==4 else [[ids[0],ids[k],ids[k+1],ids[k+1]] for k in range(1,len(ids)-1)]
    r,g,b=np.clip(np.rint(np.array(color)*255),0,255).astype(int)
    c=(255<<24)|(int(r)<<16)|(int(g)<<8)|int(b)
    for f in faces:quads.append(f);colors.append(c)
quads=np.asarray(quads,dtype=">i4");colors=np.asarray(colors,dtype=">u4")
source=inspect.getsource(m.deform).replace("p=0 if g else","p=0 if g or mode=='idle' else").replace("g or mode=='cast'","g or mode in ('cast','idle')")
exec(source,m.__dict__)
# Continue the approved raised-stinger motion into a ground-reaching thrust.
source=inspect.getsource(m.tail).replace('path[:,1]-=p*20*sm((U-.5)/.5)','path[:,1]-=p*20*sm((U-.5)/.5); strike=sm((p-.40)/.60); weight=sm((U-.53)/.47); path[:,0]+=(3-path[-1,0])*weight*strike; path[:,1]+=(-30-path[-1,1])*weight*strike; path[:,2]+=((FLOOR+2.0)-path[-1,2])*weight*strike')
exec(source,m.__dict__)
modes=['idle','glide','glide','fire','solar','wind','claw','tail_sweep','tail_sting','drought','combo_fire_wind','combo_sun_wind','combo_fire_sun','ultimate','cast','cast','idle','idle']
def coords(v):return np.column_stack((v[:,0],v[:,2],-v[:,1]))*SCALE
with gzip.open(out/"mesh.bin.gz","wb",compresslevel=4) as mesh,gzip.open(out/"bounds.bin.gz","wb",compresslevel=5) as bounds:
    mesh.write(struct.pack(">5i",0x4c414d36,len(unique),len(quads),len(modes),N))
    mesh.write(quads.tobytes());mesh.write(colors.tobytes())
    bounds.write(struct.pack(">4i",0x4c414236,len(names),len(modes),N))
    for name in names:
        b=name.encode("ascii");bounds.write(struct.pack(">H",len(b))+b)
    for action,mode in enumerate(modes):
        for step in range(N):
            t=step/(N-1);fs,tips,p,hit=m.deform(t,mode);q=np.concatenate(fs)
            if mode=='glide':q[:,1]+=14*t
            if mode=='claw':
                mask=~m.tailmask
                pivot=np.array([0,0,22-m.FLOOR])
                q[mask]=m.rot(q[mask],pivot,[1,0,0],.85*hit)
                for side in [-1,1]:
                    mask=m.GV==f'arm{side}'
                    w=m.sm((40-m.V[mask,2])/16)
                    q[mask]+=w[:,None]*hit*np.array([-side*4,-17,-13])
                for side in tips:tips[side]=m.rot(np.array(tips[side])[None,:],pivot,[1,0,0],.85*hit)[0]
            if action==14:q[:,2]-=12*(1-m.sm(t))
            if action==15:q[:,2]+=2*np.sin(np.pi*t)*m.sm(q[:,2]/40)
            if action==16:q[:,1]+=2.5*np.sin(np.pi*t)*m.sm(q[:,2]/40)
            if action==17:
                w=m.sm(t);q[:,2]*=1-.72*w;q[:,1]+=q[:,2]*.65*w
            v=coords(q);quant=np.rint(v[unique]*1024)
            assert np.max(np.abs(quant))<32767 and np.isfinite(quant).all()
            mesh.write(quant.astype(">i2").tobytes())
            for i in range(len(names)):
                a=v[part==i];assert len(a)>0,names[i]
                bounds.write(np.r_[a.min(axis=0),a.max(axis=0)].astype(">f4").tobytes())
            for s in [-1,0,1]:
                tip=np.array(tips[s],float)
                if mode=='glide':tip[1]+=14*t
                bounds.write(coords(tip[None,:]).astype(">f4").tobytes())
        print("Lamya exported",action,mode,flush=True)
print("Lamya mesh:",len(unique),"vertices",len(quads),"quads")
keys=['ambient','spawn','hurt','death','crawl','charge','fire','solar','wind','claw','tail_sweep','tail_sting','drought','combo_fire_wind','combo_sun_wind','combo_fire_sun','ultimate','phase','impact','stagger']
keys+=['prepare_'+k for k in ['fire','solar','wind','claw','tail_sweep','tail_sting','drought','combo_fire_wind','combo_sun_wind','combo_fire_sun','ultimate']]
audio=ROOT/"assets/slavicmyths/sounds/desert/lamya";audio.mkdir(parents=True,exist_ok=True)
sr=22050
for i,key in enumerate(keys):
    dur=2.8 if key in ('ultimate','death','spawn') else 1.25 if key.startswith('prepare_') or key in ('ambient','charge','phase') else .7
    t=np.arange(int(sr*dur))/sr;rng=np.random.default_rng(900+i)
    noise=np.convolve(rng.normal(size=len(t)),np.ones(16)/16,mode='same')
    f=68+i*7;phase=2*np.pi*(f*t+(.5*f if key=='charge' or key.startswith('prepare_') else -.13*f)*t*t)
    env=np.minimum(1,t/.045)*np.minimum(1,(dur-t)/.14)*np.exp(-t/(dur*.72))
    wave=(.42*np.sin(phase)+.18*np.sin(phase*1.49)+.13*np.sin(phase*2.02)+.7*noise)*env
    if key in ('solar','ultimate','phase'):wave+=.19*np.sin(2*np.pi*(380*t+110*t*t))*env
    sf.write(audio/(key+'.ogg'),np.clip(wave,-.95,.95),sr,format='OGG',subtype='VORBIS')

# Pixel-art icon for the custom drought status.
from PIL import Image,ImageDraw
icon=ROOT/"assets/slavicmyths/textures/mob_effect";icon.mkdir(parents=True,exist_ok=True)
im=Image.new("RGBA",(18,18));d=ImageDraw.Draw(im)
d.polygon([(9,1),(4,8),(3,12),(5,15),(9,17),(13,15),(15,12),(14,8)],fill="#c58c42",outline="#72401c")
d.line([(9,5),(8,9),(11,10),(7,14)],fill="#4b2723",width=2)
d.line([(5,11),(8,9),(6,7)],fill="#f1c86f",width=1)
im.save(icon/"lamya_drought.png")
