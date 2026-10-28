"""Rasterize exported production ModelPart quads with their actual PNG/UV.

Offline orthographic source renders, never labelled Minecraft screenshots.
Both stages share camera, bounds, texture filtering, scale and neutral light.
"""
from pathlib import Path
from PIL import Image, ImageDraw
import gzip, json, math, hashlib

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'docs/implementation/mob332'
BG=(32,35,43,255)

def read(stage,name):
    return json.loads(gzip.decompress((OUT/stage/(name+'.json.gz')).read_bytes()))

def projected(v,angle,elevation=.18):
    x,y,z=v[:3];d=z*math.cos(angle)-x*math.sin(angle)
    return x*math.cos(angle)+z*math.sin(angle),y*math.cos(elevation)-d*math.sin(elevation),d

def render(frame,tex,angle,scale,cx,cy,size=(240,260)):
    im=Image.new('RGBA',size,BG);quads=[]
    for face in frame['faces']:
        pts=[projected(v,angle) for v in face]
        quads.append((sum(p[2] for p in pts)/4,face,[(cx+p[0]*scale,cy+p[1]*scale) for p in pts]))
    for _,face,p in sorted(quads,key=lambda q:-q[0]):
        x0=max(0,math.floor(min(v[0] for v in p)));y0=max(0,math.floor(min(v[1] for v in p)))
        x1=min(size[0],math.ceil(max(v[0] for v in p)));y1=min(size[1],math.ceil(max(v[1] for v in p)))
        if x1<=x0 or y1<=y0:continue
        ax,ay=p[1][0]-p[0][0],p[1][1]-p[0][1];bx,by=p[3][0]-p[0][0],p[3][1]-p[0][1]
        det=ax*by-ay*bx
        if abs(det)<.02:continue
        u0,v0=face[0][3]*tex.width,face[0][4]*tex.height
        ua,va=(face[1][3]-face[0][3])*tex.width,(face[1][4]-face[0][4])*tex.height
        ub,vb=(face[3][3]-face[0][3])*tex.width,(face[3][4]-face[0][4])*tex.height
        ux=(ua*by-ub*ay)/det;uy=(-ua*bx+ub*ax)/det
        vx=(va*by-vb*ay)/det;vy=(-va*bx+vb*ax)/det
        tile=tex.transform((x1-x0,y1-y0),Image.Transform.AFFINE,(ux,uy,u0+ux*(x0-p[0][0])+uy*(y0-p[0][1]),vx,vy,v0+vx*(x0-p[0][0])+vy*(y0-p[0][1])),Image.Resampling.NEAREST)
        mask=Image.new('L',tile.size);ImageDraw.Draw(mask).polygon([(x-x0,y-y0) for x,y in p],fill=255)
        # Atlas faces are opaque; keep any transparent texels transparent.
        from PIL import ImageChops
        mask=ImageChops.multiply(mask,tile.getchannel('A'));im.paste(tile,(x0,y0),mask)
    return im

def histogram(im):
    h={};total=0
    for rgba,count in ((col,n) for n,col in im.getcolors(im.width*im.height)):
        if rgba==BG or rgba[3]==0:continue
        key=tuple(c//32 for c in rgba[:3]);h[key]=h.get(key,0)+count;total+=count
    return {','.join(map(str,k)):round(v/max(1,total),7) for k,v in h.items()}

def uv_histogram(frame,tex):
    # Weight each texel by its emitted 3D face area, not unused atlas padding.
    h={};total=0
    for f in frame['faces']:
        a=[f[1][i]-f[0][i] for i in range(3)];b=[f[3][i]-f[0][i] for i in range(3)]
        cross=[a[1]*b[2]-a[2]*b[1],a[2]*b[0]-a[0]*b[2],a[0]*b[1]-a[1]*b[0]];area=math.sqrt(sum(x*x for x in cross))
        u0=max(0,round(min(v[3] for v in f)*tex.width));u1=min(tex.width,round(max(v[3] for v in f)*tex.width))
        v0=max(0,round(min(v[4] for v in f)*tex.height));v1=min(tex.height,round(max(v[4] for v in f)*tex.height))
        if u1<=u0 or v1<=v0:continue
        crop=tex.crop((u0,v0,u1,v1));unit=area/((u1-u0)*(v1-v0))
        for count,c in crop.getcolors(crop.width*crop.height):
            if c[3]==0:continue
            key=','.join(str(x//32) for x in c[:3]);weight=count*unit;h[key]=h.get(key,0)+weight;total+=weight
    return {k:round(v/max(1,total),7) for k,v in h.items()}

def main():
    dest=OUT/'renders';dest.mkdir(exist_ok=True);stats={}
    for path in sorted((OUT/'before').glob('*.json.gz')):
        name=path.name.removesuffix('.json.gz');ds=[read(s,name) for s in ['before','after']];textures=[Image.open(OUT/s/(name+'.png')).convert('RGBA') for s in ['before','after']]
        angles=[0,math.pi/2,math.pi,-math.pi/2];points=[projected(v,a) for d in ds for f in d['frames'] for face in f['faces'] for v in face for a in [-.55]]
        # Stable scale across every pose and all four horizontal views.
        radius=max(math.hypot(v[0],v[2]) for d in ds for f in d['frames'] for face in f['faces'] for v in face)
        low=min(p[1] for p in points);high=max(p[1] for p in points);scale=min(210/max(1,radius*2),225/max(1,high-low));cx=120;cy=25-low*scale
        sheet=Image.new('RGBA',(960,560),BG);draw=ImageDraw.Draw(sheet)
        for row,(d,tex,stage) in enumerate(zip(ds,textures,['BEFORE','AFTER'])):
            for col,a in enumerate(angles):sheet.paste(render(d['frames'][0],tex,a,scale,cx,cy),(col*240,row*280+20))
            draw.text((8,row*280+4),name+' / '+stage+' / actual ModelPart UV PNG',fill='white')
        sheet.convert('RGB').save(dest/(name+'-four-views.png'))
        clips=[];selection=[i for state in ['walk','stop','attack','idle'] for i,f in enumerate(ds[0]['frames']) if f['state']==state]
        for idx in selection:
            panel=Image.new('RGBA',(480,280),BG);dr=ImageDraw.Draw(panel)
            for col,(d,tex) in enumerate(zip(ds,textures)):
                f=d['frames'][idx];panel.paste(render(f,tex,-.55,scale,cx,cy),(col*240,20));dr.text((col*240+8,4),('BEFORE' if col==0 else 'AFTER')+' '+f['state']+' t='+str(f['time']),fill='white')
            clips.append(panel.convert('RGB'))
        clips[0].save(dest/(name+'-motion.gif'),save_all=True,append_images=clips[1:],duration=100,loop=0,optimize=False)
        stats[name]={}
        for d,tex,stage in zip(ds,textures,['before','after']):
            f=d['frames'][0];stats[name][stage]={'visible_3quarter':histogram(render(f,tex,-.55,scale,cx,cy)),'uv_surface_area_weighted':uv_histogram(f,tex),'sha256_png':hashlib.sha256((OUT/stage/(name+'.png')).read_bytes()).hexdigest()}
        print('Rendered '+name,flush=True)
    overlap={}
    for a,b in [('heat_salamander','slag_crab'),('heat_salamander','vent_golem'),('slag_crab','vent_golem'),('coal_crawler','pekelnik')]:
        overlap[a+' / '+b]={stage:{metric:round(sum(min(w,stats[b][stage][metric].get(k,0)) for k,w in stats[a][stage][metric].items()),4) for metric in ['visible_3quarter','uv_surface_area_weighted']} for stage in ['before','after']}
    (OUT/'occupancy.json').write_text(json.dumps({'definition':'32-bin RGB histogram overlap; visible neutral orthographic vs actual emitted UV weighted by 3D face area. Not artistic acceptance. GIF samples every 2 game ticks (10fps), not a frame-rate performance measurement.','pairs':overlap,'models':stats},indent=2))

if __name__=='__main__':main()
