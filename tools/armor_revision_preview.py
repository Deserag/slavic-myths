"""Software projections of actual baked armor; explicitly not Minecraft screenshots."""
from pathlib import Path
from io import BytesIO
import json, math, zipfile
import numpy as np
from PIL import Image, ImageDraw
from armor_revision_assets import IDS, A, ROOT

OUT=ROOT/'docs/armor-revision'

def texture(path,old=False):
    if old:
        with zipfile.ZipFile(ROOT/'work/armor-revision/baseline-production.jar') as z:
            try:return Image.open(BytesIO(z.read('assets/slavicmyths/'+path))).convert('RGBA')
            except KeyError:return None
    p=A/path
    return Image.open(p).convert('RGBA') if p.exists() else None

def project(points, center=(90,130), scale=5):
    p=np.asarray(points,float);a=math.radians(28)
    x=p[:,0]*math.cos(a)+p[:,2]*math.sin(a)
    z=-p[:,0]*math.sin(a)+p[:,2]*math.cos(a)
    return np.stack([x*scale+center[0],(p[:,1]+z*.13)*scale+center[1],z],axis=1)

def box(x,y,z,w,h,depth,color):
    v=[[x,y,z],[x+w,y,z],[x+w,y+h,z],[x,y+h,z],[x,y,z+depth],
       [x+w,y,z+depth],[x+w,y+h,z+depth],[x,y+h,z+depth]]
    return [(np.asarray([v[i] for i in indices]),color) for indices in
            ([0,1,2,3],[5,4,7,6],[4,0,3,7],[1,5,6,2],[4,5,1,0],[3,2,6,7])]

def triangle(canvas, xyz, uv, pixels):
    h,w=canvas.shape[:2];lo=np.maximum(np.floor(xyz[:,:2].min(0)).astype(int),[0,0]);hi=np.minimum(np.ceil(xyz[:,:2].max(0)).astype(int),[w-1,h-1])
    if np.any(hi<lo):return
    xx,yy=np.meshgrid(np.arange(lo[0],hi[0]+1)+.5,np.arange(lo[1],hi[1]+1)+.5)
    a,b,c=xyz[:,:2];den=(b[1]-c[1])*(a[0]-c[0])+(c[0]-b[0])*(a[1]-c[1])
    if abs(den)<1e-7:return
    aa=((b[1]-c[1])*(xx-c[0])+(c[0]-b[0])*(yy-c[1]))/den
    bb=((c[1]-a[1])*(xx-c[0])+(a[0]-c[0])*(yy-c[1]))/den;cc=1-aa-bb
    mask=(aa>=-.001)&(bb>=-.001)&(cc>=-.001)
    u=aa*uv[0,0]+bb*uv[1,0]+cc*uv[2,0];v=aa*uv[0,1]+bb*uv[1,1]+cc*uv[2,1]
    tx=np.clip((u*pixels.shape[1]).astype(int),0,pixels.shape[1]-1);ty=np.clip((v*pixels.shape[0]).astype(int),0,pixels.shape[0]-1)
    source=pixels[ty,tx];mask &=source[:,:,3]>128
    region=canvas[lo[1]:hi[1]+1,lo[0]:hi[0]+1];region[mask]=source[mask,:3]

def render(entry,old=False):
    tile=Image.new('RGB',(180,280),'#282c32');faces=[]
    for params in [(-4,-8,-4,8,8,8,'#b09a87'),(-4,0,-2,8,12,4,'#737779'),
                   (-8,0,-2,4,12,4,'#8b8177'),(4,0,-2,4,12,4,'#8b8177'),
                   (-4,12,-2,4,12,4,'#535862'),(0,12,-2,4,12,4,'#535862')]:
        for p,col in box(*params):faces.append((project(p),None,col))
    tex=texture(entry['texture'],old)
    for face in entry['faces']:
        p=np.asarray(face);faces.append((project(p[:,:3]),p[:,3:],None))
    canvas=np.array(tile);pixels=np.array(tex)
    for p,uv,color in sorted(faces,key=lambda f:f[0][:,2].mean(),reverse=True):
        if color:
            layer=Image.fromarray(canvas);ImageDraw.Draw(layer).polygon([tuple(n) for n in p[:,:2]],fill=color);canvas=np.array(layer)
        else:
            for ix in ([0,1,2],[0,2,3]):triangle(canvas,p[ix],uv[ix],pixels)
    return Image.fromarray(canvas)

def main():
    OUT.mkdir(parents=True,exist_ok=True)
    for old,label in [(True,'before'),(False,'after')]:
        mesh=json.loads((ROOT/f'work/armor-revision/meshes-{label}.json').read_text());by_id={n['id']:n for n in mesh}
        sheet=Image.new('RGB',(900,5*304+50),'#282c32');d=ImageDraw.Draw(sheet)
        d.text((16,12),f'{label.upper()} - software projection of baked models (NOT a game screenshot)',fill='white')
        for i,id in enumerate(IDS):
            x=i%5*180;y=i//5*304+45;sheet.paste(render(by_id[id],old),(x,y));d.text((x+4,y+280),id,fill='white')
        sheet.save(OUT/f'worn-{label}.png')
        icons=Image.new('RGB',(900,5*170+40),'#282c32');di=ImageDraw.Draw(icons)
        di.text((16,10),f'{label.upper()} - inventory sprites; legacy 3D entries marked explicitly',fill='white')
        for i,id in enumerate(IDS):
            x=i%5*180;y=i//5*170+40;im=texture(f'textures/item/{id}.png',old)
            if im:
                im=im.resize((112,112),Image.Resampling.NEAREST);icons.paste(im,(x+34,y+4),im)
            else:
                # The three old items have cuboid item models, not missing game assets.
                di.text((x+20,y+30),'3D ITEM MODEL',fill='#d5b96a');di.text((x+20,y+50),'no separate sprite',fill='#aaa69c')
            di.text((x+4,y+128),id,fill='white')
        icons.save(OUT/f'icons-{label}.png')
    print('ARMOR_PREVIEW: 22 native baked meshes, before/after; runtime NOT_RUN')

if __name__=='__main__':main()
