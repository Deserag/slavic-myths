"""Generate runtime geometry straight from the accepted V2 authoring source.
Only NumPy and soundfile are needed (same environment as Sand King).
The original Python/Matplotlib prototype remains unchanged."""
import ast, gzip, io, math, os, struct, zlib
from pathlib import Path
import numpy as np
import soundfile as sf

ROOT=Path(os.environ["HALA_RESOURCE_ROOT"])/"assets/slavicmyths"
def rgb(c):
    if isinstance(c,str):
        return tuple(int(c[i:i+2],16)/255 for i in (1,3,5))
    return c
source=Path(__file__).with_name("prototype.py")
tree=ast.parse(source.read_text(encoding="utf-8"))
nodes=[]
for n in tree.body:
    if isinstance(n,ast.Import) and any(x.name.startswith("matplotlib") for x in n.names): continue
    if isinstance(n,ast.ImportFrom) and n.module.split(".")[0] in ("matplotlib","mpl_toolkits","PIL"): continue
    if isinstance(n,ast.Expr) or isinstance(n,ast.If): continue
    if isinstance(n,ast.FunctionDef) and n.name in ("render","static","animate"): continue
    nodes.append(n)
scope={"__file__":str(source),"to_rgb":rgb}
exec(compile(ast.Module(body=nodes,type_ignores=[]),str(source),"exec"),scope)
build,V=scope["build"],scope["V"]
ACTIONS=["idle","walk","beam","volley","orb","pillars","flash","dive","claw","ultimate","hurt","death"]
def body(action,t):
    return [b for b in build(action,t).b if b[4]!="FX"]
def group(b):
    p,s,c,r,g=b
    if g.startswith("wing_"):
        return g+str(min(2,int(max(0,abs(p[0])-12)//29)))
    return g
base=body("idle",0)
assert len(base)==2781,len(base)
groups=list(dict.fromkeys(group(b) for b in base))
tip=next(i for i,(p,s,c,r,g) in enumerate(base) if g=="staff" and c=="#ffb52d" and np.allclose(s,(6,6,5)))
folder=ROOT/"hala"
folder.mkdir(parents=True,exist_ok=True)
with (folder/"geometry.bin.gz").open("wb") as raw:
    with gzip.GzipFile(fileobj=raw,mode="wb",mtime=0,compresslevel=6) as f:
        def ints(*v): f.write(struct.pack(">"+len(v)*"i",*v))
        ints(0x48414C32,len(base),len(groups),len(ACTIONS),tip)
        for g in groups:
            s=g.encode("ascii"); f.write(struct.pack(">H",len(s))); f.write(s)
        for b in base:
            color=tuple(round(x*255) for x in rgb(b[2]))
            f.write(struct.pack(">IH",0xff000000|(color[0]<<16)|(color[1]<<8)|color[2],groups.index(group(b))))
        for a in ACTIONS:
            ts=[0.] if a=="idle" else np.linspace(0,1,13)
            ints(len(ts))
            for t in ts:
                bs=body(a,float(t))
                assert len(bs)==len(base) and all(x[4]==y[4] and rgb(x[2])==rgb(y[2]) for x,y in zip(bs,base)),a
                vertices=np.array([(V*s)@r.T+p for p,s,c,r,g in bs])/14
                assert np.isfinite(vertices).all()
                f.write(vertices.astype(">f4").tobytes())
print("Hala V2: 2781 cuboids, "+str(len(groups))+" collision groups, 12 animation tracks")
# Original, deterministic, non-vocal creature and magical sound effects.
sounddir=ROOT/"sounds/desert/hala";sounddir.mkdir(parents=True,exist_ok=True)
for i,name in enumerate(("ambient","step","hurt","death","charge","beam","volley","orb","pillars","flash","dive","claw","ultimate")):
    sr=22050;dur=(1.8 if name in ("ambient","death","ultimate","beam") else .65 if name=="step" else 1.1)
    t=np.arange(int(sr*dur))/sr; rng=np.random.default_rng(6109+i)
    envelope=np.sin(np.pi*np.clip(t/dur,0,1))**.8
    noise=rng.normal(0,1,len(t));wind=np.convolve(noise,np.ones(15)/15,mode="same")
    freq=(82+17*i)*(1+.4*np.sin(2*np.pi*t/dur));phase=2*np.pi*np.cumsum(freq)/sr
    wave=.32*np.sin(phase)+.16*np.sin(phase*2.03)+.2*wind
    if i>=4:wave+=.13*np.sin(phase*3.8)+.12*np.sin(phase*5.1)
    if name in ("step","hurt","claw"):wave*=np.exp(-4*t)
    sf.write(sounddir/(name+".ogg"),(wave*envelope*.75).astype(np.float32),sr,format="OGG",subtype="VORBIS")
# Pixel status icon; a real RGBA sprite, no external art.
def png(path,stage):
    data=bytearray()
    for y in range(18):
        data.append(0)
        for x in range(18):
            dx=x-8.5; inside=abs(dx)<(2+(.45*(y-4))) and 3<=y<=15 and (y>7 or dx<1)
            col=(240,72,15,255) if inside else (0,0,0,0)
            if inside and abs(dx)<2 and y>8:col=(255,205,55,255)
            if inside and y>13:col=(105,34,22,255)
            data.extend(col)
    def chunk(k,v):return struct.pack(">I",len(v))+k+v+struct.pack(">I",zlib.crc32(k+v)&0xffffffff)
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_bytes(b"\x89PNG\r\n\x1a\n"+chunk(b"IHDR",struct.pack(">IIBBBBB",18,18,8,6,0,0,0))+chunk(b"IDAT",zlib.compress(data))+chunk(b"IEND",b""))
png(ROOT/"textures/mob_effect/solar_burn.png",1)
