"""Stage A only: continuous crescent mesh, discrete pixel materials and GUI icon.

Silhouette: a slim 34-unit irregular wooden haft, 25-unit lateral crescent,
wide reinforced heel tapering to a downward point. Leather grip, aged iron
ferrules, silver bevel, one muted waning mark. Not the boss's mesh.
NeoForge's installed 21.1.255 OBJ loader owns baking; no new renderer/registry.
"""
from pathlib import Path
import json, math, random
from PIL import Image, ImageDraw

R=Path(__file__).resolve().parents[1]
A=R/'src/main/resources/assets/slavicmyths'
MATERIALS={
    'wood':('#30241d','#413027','#514032'),
    'leather':('#342b29','#493a34','#605044'),
    'iron':('#3b3f43','#51585e','#73797e'),
    'steel':('#323b46','#46515f','#5e6976'),
    'silver':('#9faeba','#bcc8cf','#dce3e5'),
    'bone':('#817f75','#a6a393','#c6c3b3'),
    'waning':('#43384e','#615070','#847295')}

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
                self.face([ids[i][j],ids[i+1][j],ids[i+1][k],ids[i][k]],[(u,j/n),(v,j/n),(v,(j+1)/n),(u,(j+1)/n)],materials[j%len(materials)],group,inside)
        for i,neighbor in [(0,1),(last,last-1)]:
            interior=tuple(sum(p[j] for p in rings[neighbor])/n for j in range(3))
            self.face(ids[i],[(.5+.45*math.cos(j*math.tau/n),.5+.45*math.sin(j*math.tau/n)) for j in range(n)],materials[0],group,interior)
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
    points=[(8,24),(-1,30),(-13,28),(-16,13)]
    x=sum(points[i][0]*math.comb(3,i)*t**i*(1-t)**(3-i) for i in range(4))
    y=sum(points[i][1]*math.comb(3,i)*t**i*(1-t)**(3-i) for i in range(4))
    dx=sum(3*(points[i+1][0]-points[i][0])*math.comb(2,i)*t**i*(1-t)**(2-i) for i in range(3))
    dy=sum(3*(points[i+1][1]-points[i][1])*math.comb(2,i)*t**i*(1-t)**(2-i) for i in range(3))
    length=math.hypot(dx,dy)
    return x,y,dy/length,-dx/length

def build():
    m=Mesh();m.shaft(-7,27,.67,'wood','haft')
    m.shaft(-7.5,-5.4,.77,'iron','butt')
    m.shaft(3,10,.82,'leather','grip')
    for index,y in enumerate([2.7,10,20.5]):m.shaft(y,y+.5,.89,'iron','band_'+str(index))
    m.shaft(22,26,1.05,'iron','socket')
    # Reinforced heel has its own bone plate and small non-emissive rune inlay.
    m.block([7.05,23,6.82],[8.95,25,7.1],'bone','heel_plate')
    m.block([7.45,23.3,6.68],[8.55,24.8,6.82],'waning','waning_inlay')
    m.block([7.05,23,9],[8.95,25,9.22],'bone','heel_plate_back')
    m.block([7.45,23.3,9.22],[8.55,24.8,9.36],'waning','waning_inlay_back')
    rings=[]
    for j in range(33):
        t=j/32;x,y,nx,ny=curve(t);w=3.9*(1-t)**.75+.10;thick=.78*(1-t)+.12
        outer=(x+nx*w*.45,y+ny*w*.45)
        shoulder=(x-nx*w*.32,y-ny*w*.32)
        edge=(x-nx*w*.55,y-ny*w*.55)
        rings.append([(outer[0],outer[1],8-thick/2),(shoulder[0],shoulder[1],8-thick*.36),(edge[0],edge[1],8),
                      (shoulder[0],shoulder[1],8+thick*.36),(outer[0],outer[1],8+thick/2)])
    m.tube(rings,['steel','silver','silver','steel','iron'],'crescent')
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
    # Broad heel -> long crescent -> fine downward tip, silver edge below spine.
    d.polygon([(25,5),(21,2),(15,1),(10,2),(6,5),(3,9),(2,14),(3,12),(5,9),(9,6),(15,5),(21,7),(24,8)],fill='#303943')
    d.line([(24,4),(20,2),(15,2),(10,3),(6,6),(3,10)],fill='#566574',width=1)
    d.line([(24,8),(20,6),(15,5),(10,6),(6,8),(3,12),(2,14)],fill='#becad2',width=1)
    d.rectangle((23,4,25,7),fill='#535a62');d.point((24,5),fill='#8b719b')
    d.point((12,3),fill='#9bb5c1');d.point((17,3),fill='#7e97a3')
    im.save(A/'textures/item/karachun_scythe.png')

def main():
    for name in MATERIALS:texture(name)
    emit(build());icon()
if __name__=='__main__':main()
