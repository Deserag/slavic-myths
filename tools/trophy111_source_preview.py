"""Offline orthographic rendering of actual #111 JSON geometry/UV and PNG.

Uses the installed 1.21.1 client JAR for inherited display transforms. No game
launch, screenshots, synthetic replacement textures or hand/player simulation.
The small renderer omits Minecraft AO and shader lighting; geometry and nearest
UV samples come from the supplied source. Generated sprites are texel prisms.
"""
from pathlib import Path
import json,zipfile,math
import numpy as np
from PIL import Image,ImageDraw

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'docs/implementation/trophy111-refinement'
CLIENT=Path('C:/Users/pavel/.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar')

def rotation(values):
    x,y,z=np.radians(values);cx,sx=math.cos(x),math.sin(x);cy,sy=math.cos(y),math.sin(y);cz,sz=math.cos(z),math.sin(z)
    return np.array([[1,0,0],[0,cx,-sx],[0,sx,cx]]) @ np.array([[cy,0,sy],[0,1,0],[-sy,0,cy]]) @ np.array([[cz,-sz,0],[sz,cz,0],[0,0,1]])

def parent_model(name):
    with zipfile.ZipFile(CLIENT) as z:return json.loads(z.read('assets/minecraft/models/'+name+'.json'))

def render(model,texture,context='gui',pixels=64):
    generated=model.get('parent')=='minecraft:item/generated'
    parent=parent_model('item/generated' if generated else 'block/block')
    pose=parent.get('display',{}).get(context,{})|model.get('display',{}).get(context,{})
    rs=rotation(pose.get('rotation',[0,0,0])) @ np.diag(pose.get('scale',[1,1,1]));translation=np.array(pose.get('translation',[0,0,0]))
    coords=(np.arange(pixels)+.5)*16/pixels-8
    xx,yy=np.meshgrid(coords,coords[::-1]);origins=np.stack([xx.ravel(),yy.ravel(),np.full(pixels*pixels,100)],axis=1)
    ray=np.array([0.,0.,-1.]);depth=np.full(pixels*pixels,np.inf);rgba=np.zeros((pixels*pixels,4),dtype=np.uint8)
    tex=np.array(texture.convert('RGBA'));height,width=tex.shape[:2]
    if generated:
        elements=[]
        for py in range(height):
            for px in range(width):
                if tex[py,px,3]:elements.append({'from':[px*16/width,(height-py-1)*16/height,7.5],'to':[(px+1)*16/width,(height-py)*16/height,8.5],'color':tex[py,px]})
    else:elements=model['elements']
    for element in elements:
        lo,hi=np.array(element['from']),np.array(element['to']);r=element.get('rotation',{});angles=[0,0,0]
        if r:angles['xyz'.index(r['axis'])]=r['angle']
        re=rotation(angles);pivot=np.array(r.get('origin',[0,0,0]));matrix=rs@re;offset=rs@(pivot-8-re@pivot)+translation;inverse=np.linalg.inv(matrix)
        origin=(origins-offset)@inverse.T;direction=inverse@ray
        parallel=np.abs(direction)<1e-10;safe=np.where(parallel,1,direction)
        t0=(lo-origin)/safe;t1=(hi-origin)/safe
        near=np.minimum(t0,t1);far=np.maximum(t0,t1)
        near[:,parallel]=-np.inf;far[:,parallel]=np.inf
        outside=np.any(((origin<lo)|(origin>hi))&parallel,axis=1)
        entry=np.max(near,axis=1);exit=np.min(far,axis=1);valid=(exit>=entry)&(entry>=0)&(entry<depth)&~outside
        indices=np.flatnonzero(valid)
        if not len(indices):continue
        if generated:colors=np.tile(element['color'],(len(indices),1))
        else:
            hit=origin[indices]+entry[indices,None]*direction;axes=np.argmax(near[indices],axis=1);colors=np.zeros((len(indices),4),dtype=np.uint8)
            x0,y0,z0=lo;x1,y1,z1=hi
            default={'west':[z0,16-y1,z1,16-y0],'east':[16-z1,16-y1,16-z0,16-y0],'down':[x0,16-z1,x1,16-z0],'up':[x0,z0,x1,z1],'north':[16-x1,16-y1,16-x0,16-y0],'south':[x0,16-y1,x1,16-y0]}
            for axis in range(3):
                selection=np.flatnonzero(axes==axis)
                if not len(selection):continue
                name=[('west','east'),('down','up'),('north','south')][axis][0 if direction[axis]>0 else 1]
                face=element.get('faces',{}).get(name,{})
                if not face:continue
                p=hit[selection];ratio=(p-lo)/(hi-lo)
                if name=='west':u,v=ratio[:,2],1-ratio[:,1]
                elif name=='east':u,v=1-ratio[:,2],1-ratio[:,1]
                elif name=='down':u,v=ratio[:,0],1-ratio[:,2]
                elif name=='up':u,v=ratio[:,0],ratio[:,2]
                elif name=='north':u,v=1-ratio[:,0],1-ratio[:,1]
                else:u,v=ratio[:,0],1-ratio[:,1]
                a,b,c,d=face.get('uv',default[name]);tx=np.clip(((a+u*(c-a))*width/16).astype(int),0,width-1);ty=np.clip(((b+v*(d-b))*height/16).astype(int),0,height-1)
                colors[selection]=tex[ty,tx]
        opaque=colors[:,3]>0;indices=indices[opaque];rgba[indices]=colors[opaque];depth[indices]=entry[indices]
    return Image.fromarray(rgba.reshape(pixels,pixels,4)),pose

def main():
    assets=ROOT/'src/main/resources/assets/slavicmyths';old=json.loads((OUT/'before/pearl_fragment.json').read_text());new=json.loads((assets/'models/item/pearl_fragment.json').read_text())
    before=Image.open(OUT/'before/pearl_fragment.png');after=Image.open(assets/'textures/item/pearl_fragment.png')
    board=Image.new('RGB',(760,500),'#2c3137');d=ImageDraw.Draw(board);d.text((12,8),'OFFLINE ACTUAL MODEL/UV renderer. Before / after; same 16-unit frame, no auto-fit.',fill='white');poses={}
    for i,context in enumerate(['gui','firstperson_righthand','thirdperson_righthand']):
        y=32+i*150;d.text((12,y),context+' (isolated item transform; no player/hand)',fill='white')
        for j,(model,texture) in enumerate([(old,before),(new,after)]):
            image,pose=render(model,texture,context,64);poses[f'{context}_{j}']=pose
            x=30+j*350;board.paste(image.resize((128,128),Image.Resampling.NEAREST),(x,y+18),image.resize((128,128),Image.Resampling.NEAREST))
            tiny,_=render(model,texture,context,16);board.paste(tiny.resize((48,48),Image.Resampling.NEAREST),(x+180,y+40),tiny.resize((48,48),Image.Resampling.NEAREST))
            if context=='gui':tiny.save(OUT/('pearl-gui-before.png' if j==0 else 'pearl-gui-after.png'))
    board.save(OUT/'pearl-model-before-after.png');(OUT/'pearl-effective-transforms.json').write_text(json.dumps(poses,indent=2)+'\n')

if __name__=='__main__':main()
