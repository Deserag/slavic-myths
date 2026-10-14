"""Stage A only: continuous crescent mesh, discrete pixel materials and GUI icon.

Silhouette: a slim 34-unit irregular wooden haft, 19.3-unit descending boss-inspired blade,
wide reinforced heel tapering to a downward point. Leather grip, aged iron
ferrules, bone bands, purple seam, ice teeth and two bone pendants. Human-scaled mesh.
NeoForge's installed 21.1.255 OBJ loader owns baking; no new renderer/registry.
"""
from pathlib import Path
import json, math, random
from PIL import Image, ImageDraw
from karachun_assets import PAL as BOSS_PALETTE

R=Path(__file__).resolve().parents[1]
A=R/'src/main/resources/assets/slavicmyths'
MATERIALS={
    'wood':tuple('#'+c for c in BOSS_PALETTE['wood']),
    'leather':('#30242f','#49363d','#614951'),
    'iron':tuple('#'+c for c in BOSS_PALETTE['metal']),
    'steel':('#242331','#34313f','#464255'),
    'silver':('#7b8291','#bac4d8','#d0d3de'),
    'bone':tuple('#'+c for c in BOSS_PALETTE['bone']),
    'waning':('#49375f','#6f49bc','#8e68dc'),
    'ice':tuple('#'+c for c in BOSS_PALETTE['ice'])}


def texture(name):
    rng=random.Random(197+list(MATERIALS).index(name))
    im=Image.new('RGB',(32,32));palette=MATERIALS[name]
    for y in range(32):
        for x in range(32):
            if name=='wood':i=0 if (x+int(2*math.sin(y*.18)))%9<2 else 2 if (x+y//11)%13==0 else 1
            elif name=='leather':i=0 if y%8==0 else 2 if x%11==0 and y%8<3 else 1
            elif name in ('steel','iron'):i=2 if y%13==0 and 3<x%14<11 else 0 if (x//7+y//9)%5==0 else 1
            elif name=='silver':i=2 if y<3 or (y+x//8)%15==0 else 0 if y>26 else 1
            elif name=='waning':i=1
            else:i=0 if (x//4+y//5)%5==0 else 1
            if name not in ('wood','leather','waning') and rng.random()<.018:i=2
            im.putpixel((x,y),tuple(bytes.fromhex(palette[i][1:])))
    if name=='waning':
        d=ImageDraw.Draw(im);d.line([(16,7),(22,15),(16,24),(10,15),(16,7)],fill=palette[2],width=2);d.line((16,5,16,26),fill=palette[2],width=1)
    if name=='steel':
        d=ImageDraw.Draw(im)
        for x,y in [(4,3),(5,4),(6,3),(20,5),(21,6)]:d.point((x,y),fill='#829aa5')
    im.save(A/f'textures/item/karachun_scythe_{name}.png')

class Mesh:
    def __init__(self):self.vertices=[];self.faces=[]
    def vertex(self,p):self.vertices.append(tuple(p));return len(self.vertices)-1
    def face(self,ids,uv,material,group,inside):
        # Orient every face away from its component interior; OBJ normal is baked
        # from winding, including the tapered blade and end caps.
        p,q,r=[self.vertices[i] for i in ids[:3]]
        ab=[q[i]-p[i] for i in range(3)];ac=[r[i]-p[i] for i in range(3)]
        normal=(ab[1]*ac[2]-ab[2]*ac[1],ab[2]*ac[0]-ab[0]*ac[2],ab[0]*ac[1]-ab[1]*ac[0])
        center=[sum(self.vertices[k][j] for k in ids)/len(ids) for j in range(3)]
        if sum(normal[j]*(center[j]-inside[j]) for j in range(3))<0:ids=ids[::-1];uv=uv[::-1]
        # Explicit triangles avoid nonplanar OBJ quads across a changing curve.
        for i in range(1,len(ids)-1):self.faces.append({'v':[ids[0],ids[i],ids[i+1]],'uv':[uv[0],uv[i],uv[i+1]],'material':material,'group':group})
    def tube(self,rings,materials,group):
        ids=[[self.vertex(p) for p in ring] for ring in rings];n=len(ids[0]);last=len(rings)-1
        for i in range(last):
            inside=tuple(sum(p[j] for p in rings[i]+rings[i+1])/(2*n) for j in range(3))
            for j in range(n):
                k=(j+1)%n;u=i/last;v=(i+1)/last
                self.face([ids[i][j],ids[i+1][j],ids[i+1][k],ids[i][k]],[(u,j/n),(v,j/n),(v,(j+1)/n),(u,(j+1)/n)],(materials(i,j) if callable(materials) else materials[j%len(materials)]),group,inside)
        for i,neighbor in [(0,1),(last,last-1)]:
            interior=tuple(sum(p[j] for p in rings[neighbor])/n for j in range(3))
            self.face(ids[i],[(.5+.45*math.cos(j*math.tau/n),.5+.45*math.sin(j*math.tau/n)) for j in range(n)],(materials(i,0) if callable(materials) else materials[0]),group,interior)
    def shaft(self,y0,y1,radius,material,group,offset=0):
        rings=[]
        for y in [y0,(y0+y1)/2,y1]:
            x=8+offset+.13*math.sin(y*.22)
            rings.append([(x+radius*math.cos(j*math.tau/8),y,8+radius*math.sin(j*math.tau/8)) for j in range(8)])
        self.tube(rings,[material],group)
    def block(self,lo,hi,mat,group):
        rings=[[(lo[0],y,lo[2]),(hi[0],y,lo[2]),(hi[0],y,hi[2]),(lo[0],y,hi[2])] for y in [lo[1],hi[1]]]
        self.tube(rings,[mat],group)

def curve(t):
    # Boss: lateral span 23.4, haft 42, descending parabola 11. Human form
    # keeps that ratio, not the tall boss transform or its bulky decorations.
    x=8-19.3*t;y=27.8-8.4*t*t-1.1*t
    dx=-19.3;dy=-16.8*t-1.1;length=math.hypot(dx,dy)
    return x,y,dy/length,-dx/length

def blade_width(t):
    base=3.6-2.2*t
    if t>.9:base=.1+(base-.1)*(1-t)/.1
    tooth=max(max(0,1-abs(t-c)/.055)*(0.65+c*.55) for c in [.15,.35,.55,.75,.9])
    return base,tooth

def build():
    m=Mesh();m.shaft(-7,27,.65,'wood','haft')
    m.shaft(-7.4,-5.6,.79,'bone','butt')
    m.shaft(3,10,.79,'leather','grip')
    for index,y in enumerate([-6.2,-.6,5.8,12.2,18.6,24.2]):m.shaft(y,y+.65,.86,'bone' if index in (0,3) else 'iron','band_'+str(index))
    m.shaft(24,28,1.02,'iron','socket')
    for y in [1,12,20]:m.block([7.82,y,7.22],[8.2,y+1.6,7.35],'waning','haft_mark_'+str(y))
    m.block([7.05,25,6.85],[8.95,27,7.12],'bone','heel_plate')
    m.block([7.45,25.3,6.72],[8.55,26.8,6.85],'waning','waning_inlay')
    m.block([7.05,25,8.9],[8.95,27,9.18],'bone','heel_plate_back')
    m.block([7.45,25.3,9.18],[8.55,26.8,9.32],'waning','waning_inlay_back')
    # Two shortened bone/ice pendants echo the boss without dangling curtains.
    for index,off in enumerate([2.5,6]):
        for j in range(3):m.block([8-off-.15,23-j*.8,7.7],[8-off+.15,23.5-j*.8,8.3],'bone',f'pendant_{index}_{j}')
        m.block([8-off-.35,20.6,7.65],[8-off+.35,21.5,8.35],'ice',f'pendant_tip_{index}')
    rings=[]
    for j in range(65):
        t=j/64;x,y,nx,ny=curve(t);w,tooth=blade_width(t);thick=.78*(1-t)+.16
        outer=(x+nx*w*.45,y+ny*w*.45)
        shoulder=(x-nx*w*.32,y-ny*w*.32)
        edge=(x-nx*(w*.55+tooth),y-ny*(w*.55+tooth))
        rings.append([(outer[0],outer[1],8-thick/2),(shoulder[0],shoulder[1],8-thick*.36),(edge[0],edge[1],8),
                      (shoulder[0],shoulder[1],8+thick*.36),(outer[0],outer[1],8+thick/2)])
    def blade_material(i,j):
        t=min(i,63)/64
        if j in [1,2] and blade_width(t)[1]>.3:return 'ice'
        return ['steel','waning' if t<.93 else 'silver','waning' if t<.93 else 'silver','steel','iron'][j]
    m.tube(rings,blade_material,'crescent')
    return m

def emit(m):
    p=A/'models/item/karachun_scythe.obj'
    lines=['# Slavic Myths Stage A: units are blocks, not pixels','mtllib karachun_scythe.mtl']
    for v in m.vertices:lines.append('v '+' '.join(f'{c/16:.7f}' for c in v))
    for face in m.faces:
        for uv in face['uv']:lines.append('vt '+' '.join(f'{u:.7f}' for u in uv))
    material=group=None
    for i,face in enumerate(m.faces):
        if face['group']!=group:group=face['group'];lines.append('g '+group)
        if face['material']!=material:material=face['material'];lines.append('usemtl '+material)
        lines.append('f '+' '.join(f'{v+1}/{3*i+j+1}' for j,v in enumerate(face['v'])))
    p.write_text('\n'.join(lines)+'\n',encoding='utf-8')
    (A/'models/item/karachun_scythe.mtl').write_text('\n'.join(f'newmtl {name}\nKd 1 1 1\nKa 0 0 0\nmap_Kd #'+name for name in MATERIALS)+'\n',encoding='utf-8')
    display={'gui':{'rotation':[0,0,-25],'scale':[.4]*3},'ground':{'rotation':[0,0,70],'translation':[0,3,0],'scale':[.35]*3},'fixed':{'rotation':[0,0,-25],'scale':[.45]*3},'head':{'translation':[0,8,0],'scale':[.3]*3}}
    for hand in ['righthand','lefthand']:
        # ItemTransform itself flips left-hand translation X and rotation Y/Z.
        # A further half turn keeps an asymmetric crescent away from the camera.
        rotation=[0,-65,12] if hand=='righthand' else [0,115,-12]
        display['firstperson_'+hand]={'rotation':rotation,'translation':[6,-3,-2],'scale':[.46]*3}
        display['thirdperson_'+hand]={'rotation':[60,180,8] if hand=='righthand' else [60,0,-8],'translation':[0,2,-1],'scale':[.68]*3}
    model={'loader':'neoforge:obj','model':'slavicmyths:models/item/karachun_scythe.obj','automatic_culling':False,'shade_quads':True,'flip_v':True,'emissive_ambient':False,
           'textures':{name:'slavicmyths:item/karachun_scythe_'+name for name in MATERIALS},'display':display}
    model['textures']['particle']='slavicmyths:item/karachun_scythe_wood'
    (A/'models/item/karachun_scythe_held.json').write_text(json.dumps(model,indent=2)+'\n',encoding='utf-8')

def icon():
    # Designed in slot space; this is pixel art, not a projected 3D render.
    im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
    d.line([(24,4),(25,14),(24,29)],fill='#241e1b',width=3)
    d.line([(24,5),(24,15),(23,28)],fill='#5a4130',width=1)
    d.line([(25,7),(25,14),(24,22)],fill='#392b23',width=1)
    d.rectangle((23,16,25,22),fill='#4c3d35')
    for y in [16,19,22]:d.line((23,y,25,y),fill='#70604f')
    for y in [9,23,29]:d.rectangle((23,y,25,y+1),fill='#626b72')
    d.polygon([(25,2),(20,3),(15,4),(10,6),(6,9),(2,13),(6,11),(10,9),(15,7),(20,6),(25,5)],fill='#242331')
    d.line([(25,2),(20,3),(15,4),(10,6),(6,9)],fill='#464255',width=1)
    d.line([(24,5),(20,6),(15,7),(10,9),(6,11),(2,13)],fill='#7956b1',width=1)
    for x,y in [(20,6),(15,7),(10,9),(6,11)]:d.polygon([(x,y),(x+1,y),(x,y+3)],fill='#a9c6ee')
    d.rectangle((23,3,25,6),fill='#34313f');d.point((24,4),fill='#8e68dc')
    for y in [10,17,24,29]:d.line((23,y,25,y),fill='#bac4d8' if y in [17,29] else '#464255')
    im.save(A/'textures/item/karachun_scythe.png')

def main():
    for name in MATERIALS:texture(name)
    emit(build());icon()
if __name__=='__main__':main()
