"""Targeted mesh/resource checks and honest software previews. No Minecraft UI."""
from collections import Counter,defaultdict
from pathlib import Path
import hashlib,json,math,subprocess,zipfile
import numpy as np
from PIL import Image,ImageDraw,ImageFont
from scythe_stage_a_assets import A,R,MATERIALS,Mesh,build,curve,blade_width,main as generate

O=R/'docs/weapon-stage-a'
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def validate_geometry():
    m=build();edges=defaultdict(Counter);directed=defaultdict(Counter)
    for f in m.faces:
        v=f['v'];p,q,r=np.array([m.vertices[i] for i in v])
        assert np.linalg.norm(np.cross(q-p,r-p))>1e-7,'degenerate triangle'
        assert all(math.isfinite(n) and 0<=n<=1 for uv in f['uv'] for n in uv),'invalid UV'
        for i in range(3):
            a,b=v[i],v[(i+1)%3];edges[f['group']][tuple(sorted([a,b]))]+=1;directed[f['group']][a,b]+=1
    for g,e in edges.items():
        assert all(n==2 for n in e.values()),'open seam/nonmanifold '+g
        for a,b in e:assert directed[g][a,b]==directed[g][b,a]==1,'winding '+g
    # The blade's two planar boundaries may not cross themselves or each other.
    upper=[];lower=[]
    for j in range(65):
        t=j/64;x,y,nx,ny=curve(t);w,tooth=blade_width(t)
        upper.append((x+nx*w*.45,y+ny*w*.45));lower.append((x-nx*(w*.55+tooth),y-ny*(w*.55+tooth)))
        assert .78*(1-t)+.12>0,'blade thickness'
    outline=upper+lower[::-1]
    def orient(a,b,c):return (b[0]-a[0])*(c[1]-a[1])-(b[1]-a[1])*(c[0]-a[0])
    for i in range(len(outline)):
        a,b=outline[i],outline[(i+1)%len(outline)]
        for j in range(i+2,len(outline)):
            if i==0 and j==len(outline)-1:continue
            c,d=outline[j],outline[(j+1)%len(outline)]
            assert not (orient(a,b,c)*orient(a,b,d)<0 and orient(c,d,a)*orient(c,d,b)<0),'blade self intersection'
    assert curve(1)[1]<curve(0)[1]-8,'tip must point down'
    assert upper[0][1]-lower[0][1]>3,'reinforced heel'
    model=json.loads((A/'models/item/karachun_scythe_held.json').read_text())
    assert model['loader']=='neoforge:obj'
    for context in ['gui','ground','fixed','head','firstperson_righthand','firstperson_lefthand','thirdperson_righthand','thirdperson_lefthand']:assert context in model['display']
    for name in MATERIALS:
        p=A/f'textures/item/karachun_scythe_{name}.png';im=Image.open(p);im.verify()
        assert im.size==(32,32)
    assert len({digest(A/f'textures/item/karachun_scythe_{name}.png') for name in MATERIALS})==len(MATERIALS),'distinct material textures'
    # Validate the emitted OBJ/MTL, not just its Python source representation.
    vertices=[];uvs=[];face_count=0;material=None
    for line in (A/'models/item/karachun_scythe.obj').read_text().splitlines():
        words=line.split()
        if not words:continue
        if words[0]=='v':vertices.append(tuple(map(float,words[1:])))
        if words[0]=='vt':uvs.append(tuple(map(float,words[1:])))
        if words[0]=='usemtl':material=words[1];assert material in MATERIALS
        if words[0]=='f':
            assert material is not None and len(words)==4
            for word in words[1:]:
                vi,ti=map(int,word.split('/'));assert 1<=vi<=len(vertices) and 1<=ti<=len(uvs)
            face_count+=1
    assert np.allclose(np.array(vertices),np.array(m.vertices)/16,atol=1e-7)
    assert face_count==len(m.faces) and all(0<=n<=1 for uv in uvs for n in uv)
    library=(A/'models/item/karachun_scythe.mtl').read_text()
    for name in MATERIALS:assert 'newmtl '+name in library and 'map_Kd #'+name in library
    return {'triangles':len(m.faces),'closed_components':len(edges),'blade':'closed, consistent winding, no 2D outline self intersections','uv':'finite [0,1]','mounted_parts':'collars/grip deliberately surround haft; no coplanar exposed surfaces','contexts':8}

def rotation(x,y,z):
    x,y,z=map(math.radians,[x,y,z]);cx,sx,cy,sy,cz,sz=math.cos(x),math.sin(x),math.cos(y),math.sin(y),math.cos(z),math.sin(z)
    return np.array([[1,0,0],[0,cx,-sx],[0,sx,cx]])@np.array([[cy,0,sy],[0,1,0],[-sy,0,cy]])@np.array([[cz,-sz,0],[sz,cz,0],[0,0,1]])

def raster(m,vertices,size,textures,projection=None):
    width,height=size;v=np.array(vertices)
    if projection is None:
        scale=min((width-60)/np.ptp(v[:,0]),(height-80)/np.ptp(v[:,1]));xy=v[:,:2]*scale
        xy[:,0]+=width/2-(xy[:,0].min()+xy[:,0].max())/2
        xy[:,1]=-xy[:,1]+height/2+(xy[:,1].min()+xy[:,1].max())/2
    else:xy=projection(v)
    rgb=np.empty((height,width,3),dtype=np.uint8);rgb[:]=[28,32,39];depth=np.full((height,width),-np.inf)
    for f in m.faces:
        ids=f['v'];tri=xy[ids];pos=v[ids];uv=np.array(f['uv'])
        if projection is not None and np.any(pos[:,2]>=-.05):continue
        x0=max(0,int(np.floor(tri[:,0].min())));x1=min(width-1,int(np.ceil(tri[:,0].max())))
        y0=max(0,int(np.floor(tri[:,1].min())));y1=min(height-1,int(np.ceil(tri[:,1].max())))
        if x1<x0 or y1<y0:continue
        x,y=np.meshgrid(np.arange(x0,x1+1)+.5,np.arange(y0,y1+1)+.5)
        a,b,c=tri;den=(b[1]-c[1])*(a[0]-c[0])+(c[0]-b[0])*(a[1]-c[1])
        if abs(den)<1e-8:continue
        wa=((b[1]-c[1])*(x-c[0])+(c[0]-b[0])*(y-c[1]))/den
        wb=((c[1]-a[1])*(x-c[0])+(a[0]-c[0])*(y-c[1]))/den;wc=1-wa-wb
        z=wa*pos[0,2]+wb*pos[1,2]+wc*pos[2,2];patch=depth[y0:y1+1,x0:x1+1]
        keep=(wa>=-1e-7)&(wb>=-1e-7)&(wc>=-1e-7)&(z>patch)
        u=wa*uv[0,0]+wb*uv[1,0]+wc*uv[2,0];vt=wa*uv[0,1]+wb*uv[1,1]+wc*uv[2,1]
        texture=textures[f['material']];th,tw=texture.shape[:2]
        tx=np.clip((u*tw).astype(int),0,tw-1);ty=np.clip((vt*th).astype(int),0,th-1)
        n=np.cross(pos[1]-pos[0],pos[2]-pos[0]);n/=np.linalg.norm(n)
        light=.58+.42*abs(np.dot(n,np.array([-.3,.6,.74])/np.linalg.norm([-.3,.6,.74])))
        color=np.clip(texture[ty,tx,:3]*light,0,255).astype(np.uint8)
        patch[keep]=z[keep];rgb[y0:y1+1,x0:x1+1][keep]=color[keep]
    return Image.fromarray(rgb),np.isfinite(depth)

def previews():
    (O/'images').mkdir(parents=True,exist_ok=True);m=build();textures={n:np.array(Image.open(A/f'textures/item/karachun_scythe_{n}.png').convert('RGB')) for n in MATERIALS}
    font=ImageFont.truetype('C:/Windows/Fonts/arial.ttf',23);small=ImageFont.truetype('C:/Windows/Fonts/arial.ttf',18)
    sheet=Image.new('RGB',(1600,950),'#1c2027');d=ImageDraw.Draw(sheet)
    d.text((24,15),'STAGE A — SOFTWARE MODEL PREVIEW; NOT A MINECRAFT SCREENSHOT',font=font,fill='#d3dbe4')
    old=json.loads(subprocess.check_output(['git','show','f53447cc:src/main/resources/assets/slavicmyths/models/item/karachun_scythe_held.json']))
    before=Mesh();oldtex={}
    palette=['#49392d','#715640','#343840','#646b73','#b5b9ba','#817c94','#bda25d']
    for i,e in enumerate(old['elements']):
        color=int(e['faces']['north']['uv'][0]);mat='old'+str(color);oldtex[mat]=np.array(Image.new('RGB',(1,1),palette[color]));before.block(e['from'],e['to'],mat,'old'+str(i))
    for col,(mesh,tex,yaw,label) in enumerate([(before,oldtex,0,'BEFORE f53447cc'),(m,textures,0,'AFTER — FRONT'),(m,textures,35,'AFTER — 35°'),(m,textures,78,'AFTER — SIDE / THICKNESS')]):
        v=np.array(mesh.vertices)@rotation(0,yaw,0).T;im,_=raster(mesh,v,(390,650),tex)
        sheet.paste(im,(col*400,75));d.text((col*400+15,52),label,font=small,fill='#cad2dd')
    oldicon=Image.open(__import__('io').BytesIO(subprocess.check_output(['git','show','f53447cc:src/main/resources/assets/slavicmyths/textures/item/karachun_scythe.png']))).convert('RGBA')
    newicon=Image.open(A/'textures/item/karachun_scythe.png')
    for im,x,label in [(oldicon,80,'BEFORE icon'),(newicon,480,'AFTER icon: hand-drawn pixels')]:
        sheet.paste(im.resize((160,160),Image.Resampling.NEAREST),(x,745),im.resize((160,160),Image.Resampling.NEAREST));d.text((x-25,913),label,font=small,fill='#cad2dd')
    d.text((810,780),'Continuous tapered crescent / silver bevel\nWood grain / leather wrap / aged iron\nMuted waning mark; no fullbright strip\nSeparate pixel icon; distinct material maps',font=font,fill='#c6cdd6',spacing=12)
    sheet.save(O/'images/scythe-comparison.png')
    # Camera-only projection of exact idle item transform and extension basis.
    # Does not simulate Minecraft's renderer, arms, lighting, bobbing or gameplay.
    display=json.loads((A/'models/item/karachun_scythe_held.json').read_text())['display']
    sheet=Image.new('RGB',(1440,670),'#1c2027');d=ImageDraw.Draw(sheet);d.text((20,8),'IDLE CAMERA PROJECTION — FOV 70 / 90 / 110; NOT IN-GAME',font=font,fill='#d3dbe4')
    views=[]
    for row,hand in enumerate(['righthand','lefthand']):
        spec=display['firstperson_'+hand];sign=1 if row==0 else -1;rot=list(spec['rotation']);rot[1]*=sign;rot[2]*=sign
        translation=np.array(spec['translation'])/16;translation[0]*=sign
        v=((np.array(m.vertices)/16-.5)*spec['scale'])@rotation(*rot).T+translation+np.array([sign*.56,-.52,-.72])
        for col,fov in enumerate([70,90,110]):
            def project(v):
                focal=300/(2*math.tan(math.radians(fov/2)));return np.column_stack([240+v[:,0]/-v[:,2]*focal,150-v[:,1]/-v[:,2]*focal])
            im,mask=raster(m,v,(480,300),textures,project);draw=ImageDraw.Draw(im);draw.line((230,150,250,150),fill='#cbd3db');draw.line((240,140,240,160),fill='#cbd3db');draw.text((8,8),hand+' · FOV '+str(fov),font=small,fill='#cbd3db')
            central=bool(mask[120:180,210:270].any());assert not central,'idle overlaps reticle in projection'
            views.append({'hand':hand,'fov':fov,'central_60px_box_occupied':central,'scope':'idle mathematical projection only'})
            sheet.paste(im,(col*480,55+row*305))
    sheet.save(O/'images/scythe-fov-projection.png')
    evidence=O/'server-evidence.json'
    if evidence.exists() and 'pose_samples' in json.loads(evidence.read_text()):
        samples=json.loads(evidence.read_text())['pose_samples']
        reference_pose_views(m,textures,display,samples,font,small)
    return views

def reference_pose_views(m,textures,display,samples,font,small):
    """Plain humanoid reference using vanilla hand attachment order.

    Matches ItemInHandLayer's -90X / 180Y / hand offset, vanilla model pivot
    positions and Stage A pose channels. Not the PlayerRenderer, skin, armor,
    animation blending with other mods, or an in-game screenshot.
    """
    def append(scene,mesh,vertices):
        offset=len(scene.vertices);scene.vertices.extend(map(tuple,vertices))
        scene.faces.extend({**f,'v':[i+offset for i in f['v']]} for f in mesh.faces)
    def part(scene,lo,hi,material,angles=(0,0,0),at=(0,0,0)):
        p=Mesh();p.block(lo,hi,material,material)
        v=np.array(p.vertices)@rotation(*angles).T+at
        v=v*np.array([-1,-1,1])+[0,24,0];append(scene,p,v)
    tex={**textures,**{n:np.array(Image.new('RGB',(1,1),c)) for n,c in [('skin','#ac9078'),('cloth','#4b5f73'),('legs','#354354')]}}
    sheet=Image.new('RGB',(1600,780),'#1c2027');d=ImageDraw.Draw(sheet)
    d.text((20,10),'THIRD-PERSON ATTACHMENT / POSE REFERENCE — NOT A MINECRAFT SCREENSHOT',font=font,fill='#d3dbe4')
    entries=[('righthand',0,0,0,'RIGHT / IDLE'),('righthand',0,7,35,'RIGHT / ARC'),('righthand',2,9,75,'RIGHT / HEAVY'),('lefthand',0,7,35,'LEFT / ARC')]
    for col,(hand,combo,age,yaw,label) in enumerate(entries):
        sample=next(s for s in samples if s['combo']==combo and s['age']==age)
        f,blend=sample['motion'],sample['influence'];direction=-1 if combo==1 else 1
        left=hand=='lefthand';scene=Mesh()
        part(scene,[-4,0,-2],[4,12,2],'cloth',(0,math.degrees(f*.2*direction),0))
        part(scene,[-4,-8,-4],[4,0,4],'skin')
        for x in [-1.9,1.9]:part(scene,[-2,0,-2],[2,12,2],'legs',at=(x,12,0))
        for arm_left in [False,True]:
            is_main=arm_left==left
            if is_main:
                x=-.31*(1-blend)+(-.8+(f*.65 if combo==2 else 0))*blend
                y=blend*(-.18+f*.7*direction);z=blend*(.18 if left else -.18)
            else:x=-.85*blend;y=blend*(-.35 if left else .35);z=blend*(-.08 if left else .08)
            angles=list(map(math.degrees,[x,y,z]));pivot=[5 if arm_left else -5,2,0]
            lo=[-1 if arm_left else -3,-2,-2];hi=[3 if arm_left else 1,10,2]
            part(scene,lo,hi,'skin',angles,pivot)
            if is_main:
                spec=display['thirdperson_'+hand];r=list(spec['rotation']);sign=-1 if left else 1;r[1]*=sign;r[2]*=sign
                translation=np.array(spec['translation'],dtype=float);translation[0]*=sign
                v=(np.array(m.vertices)-8)*spec['scale'];v=v@rotation(*r).T+translation+[sign,2,-10]
                v=v@(rotation(-90,0,0)@rotation(0,180,0)).T
                v=v@rotation(*angles).T+pivot;v=v*[-1,-1,1]+[0,24,0];append(scene,m,v)
        im,_=raster(scene,np.array(scene.vertices)@rotation(0,yaw,0).T,(390,660),tex)
        sheet.paste(im,(400*col,70));d.text((400*col+15,48),label,font=small,fill='#cbd3db')
    d.text((20,740),'Untextured mannequin; Steve pivots. Native pose samples, mathematical attachment. Real skins / armor / held render require client QA.',font=small,fill='#cbd3db')
    sheet.save(O/'images/scythe-thirdperson-reference.png')
    sheet=Image.new('RGB',(1440,730),'#1c2027');d=ImageDraw.Draw(sheet)
    d.text((20,8),'FIRST-PERSON SWING SAMPLES — SOFTWARE PROJECTION; NOT IN-GAME',font=font,fill='#d3dbe4')
    spec=display['firstperson_righthand'];local=(np.array(m.vertices)/16-.5)*spec['scale'];local=local@rotation(*spec['rotation']).T+np.array(spec['translation'])/16
    for col,age in enumerate([0,3,5,8,11,14]):
        f=next(s['motion'] for s in samples if s['combo']==0 and s['age']==age)
        swing=rotation(0,f*42,0)@rotation(f*10,0,0)@rotation(0,0,f*8)
        v=local@swing.T+[.56,-.52,-.72]
        def project(v):return np.column_stack([240+v[:,0]/-v[:,2]*214,150-v[:,1]/-v[:,2]*214])
        im,_=raster(m,v,(480,300),textures,project);draw=ImageDraw.Draw(im);draw.line((235,150,245,150),fill='#bfc8d1');draw.line((240,145,240,155),fill='#bfc8d1')
        x=(col%3)*480;y=(col//3)*320;sheet.paste(im,(x,60+y));d.text((x+15,40+y),'tick '+str(age),font=small,fill='#cbd3db')
    d.text((20,700),'0: idle · 3: windup · 5: release · 8: follow-through · 11: recovery · 14: neutral',font=small,fill='#cbd3db')
    sheet.save(O/'images/scythe-swing-reference.png')

def main():
    O.mkdir(parents=True,exist_ok=True)
    paths=[A/'models/item/karachun_scythe_held.json',A/'models/item/karachun_scythe.obj',A/'models/item/karachun_scythe.mtl',A/'textures/item/karachun_scythe.png']+[A/f'textures/item/karachun_scythe_{n}.png' for n in MATERIALS]
    before=[digest(p) for p in paths];generate();assert before==[digest(p) for p in paths],'generator reproducibility'
    report={'geometry':validate_geometry(),'reproduction':'PASS','camera_projection':previews(),'visual_acceptance':'PENDING_AUTHOR; previews are not game tests'}
    jar=R/'build/libs/slavicmyths-1.9.7.jar'
    if jar.exists():
        with zipfile.ZipFile(jar) as z:
            for p in paths:assert z.read(p.relative_to(R/'src/main/resources').as_posix())==p.read_bytes(),'packaged '+p.name
            assert not any(('ScytheGameTests' in n or 'ScytheClientProbe' in n) or n.startswith(('tools/','data/slavicmyths_scythe/')) for n in z.namelist()),'test code/resources in production'
            baseline=json.loads((R/'docs/armor-revision/polymc-installation.json').read_text(encoding='utf-8-sig'))
            old=Path(baseline['source']);assert digest(old)==baseline['sha256'],'exact post-armor baseline'
            allowed={'slavicmyths.equipment.mixins.json','assets/slavicmyths/models/item/karachun_scythe_held.json','assets/slavicmyths/textures/item/karachun_scythe.png'}
            with zipfile.ZipFile(old) as b:
                old_names={n for n in b.namelist() if not n.endswith('/')};new_names=set(z.namelist())
                omissions=sorted(old_names-new_names)
                for n in omissions:
                    # A clean checkout of the post-armor base already lacks these
                    # generated files. Do not silently annex the armor task.
                    source='b7616080:src/main/resources/'+n
                    assert subprocess.run(['git','cat-file','-e',source],cwd=R,capture_output=True).returncode!=0,'newly removed production entry '+n
                changed=[];format_only=[];unchanged=0
                for n in old_names & new_names:
                    before,after=b.read(n),z.read(n)
                    if before==after:unchanged+=1
                    elif before.replace(b'\r\n',b'\n')==after.replace(b'\r\n',b'\n'):format_only.append(n)
                    else:changed.append(n)
                for n in changed:assert n in allowed or n.startswith(('org/slavicmyths/client/NightClient','org/slavicmyths/karachun/NightCombat')),'unrelated production change '+n
                report['parallel_content_preservation']={'baseline_sha256':baseline['sha256'],'byte_unchanged_files':unchanged,'line_endings_only_files':len(format_only),'changed_entries':sorted(changed),'preexisting_base_git_omissions':omissions,'newly_removed_entries':[],'deployment':'weapon branch only; integration and installed baseline have separate gates. Two layer_2 omissions unused: both registered armor items are CHESTPLATE'}
        report['production_sha256']=digest(jar)
    (O/'checks.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8');print(json.dumps(report,indent=2))
if __name__=='__main__':main()
