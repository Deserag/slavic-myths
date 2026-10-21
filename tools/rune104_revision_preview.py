"""Standalone GPU preview of actual source meshes/UV/textures and production glow shader.
No Minecraft rendering, GUI, hand pose, multiplayer, or in-game acceptance is claimed.
"""
from pathlib import Path
import ctypes as C,json,zipfile,math,hashlib,io
import sys
import numpy as np
from PIL import Image,ImageDraw,ImageFont
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/rune-glow-104-revision'
def render(proc,functions,glow,GL,result):
 u=C.c_uint;i=C.c_int;f=C.c_float;p=C.c_void_p
 genVAO=proc('glGenVertexArrays',None,i,C.POINTER(u));bindVAO=proc('glBindVertexArray',None,u);delVAO=proc('glDeleteVertexArrays',None,i,C.POINTER(u));genBuffer=proc('glGenBuffers',None,i,C.POINTER(u));bindBuffer=proc('glBindBuffer',None,u,u);bufferData=proc('glBufferData',None,u,C.c_ssize_t,p,u);delBuffer=proc('glDeleteBuffers',None,i,C.POINTER(u))
 attrib=proc('glVertexAttribPointer',None,u,i,u,C.c_ubyte,i,p);enableAttrib=proc('glEnableVertexAttribArray',None,u);use=proc('glUseProgram',None,u);loc=proc('glGetUniformLocation',i,u,C.c_char_p);mat=proc('glUniformMatrix4fv',None,i,i,C.c_ubyte,C.POINTER(f));uniform=proc('glUniform1f',None,i,f);uniformI=proc('glUniform1i',None,i,i);color=proc('glUniform4f',None,i,f,f,f,f)
 GL.glTexImage2D.argtypes=[u,i,i,i,i,i,u,u,p];GL.glReadPixels.argtypes=[i,i,i,i,u,u,p];GL.glClearColor.argtypes=[f,f,f,f];GL.glDepthMask.argtypes=[C.c_ubyte]
 vao=u();vbo=u();genVAO(1,C.byref(vao));bindVAO(vao);genBuffer(1,C.byref(vbo));bindBuffer(0x8892,vbo)
 textures=[];ownShaders=[];base=0
 def tex(image):
  image=image.convert('RGBA');n=u();GL.glGenTextures(1,C.byref(n));textures.append(n);GL.glBindTexture(0x0de1,n.value);GL.glTexParameteri(0x0de1,0x2801,0x2600);GL.glTexParameteri(0x0de1,0x2800,0x2600);b=image.tobytes();buf=C.create_string_buffer(b);GL.glTexImage2D(0x0de1,0,0x8058,image.width,image.height,0,0x1908,0x1401,buf);return n.value
 def simple_shader(kind,code):
  s=functions['glCreateShader'](kind);ownShaders.append(s);strings=(C.c_char_p*1)(code.encode());functions['glShaderSource'](s,1,strings,None);functions['glCompileShader'](s);ok=i();functions['glGetShaderiv'](s,0x8b81,C.byref(ok));assert ok.value,'Preview base shader compile';return s
 try:
  vs=simple_shader(0x8b31,'#version 150\nin vec3 Position;in vec2 UV0;uniform mat4 ModelViewMat;uniform mat4 ProjMat;out vec2 uv;void main(){gl_Position=ProjMat*ModelViewMat*vec4(Position,1);uv=UV0;}')
  fs=simple_shader(0x8b30,'#version 150\nuniform sampler2D Sampler0;uniform float Brightness;in vec2 uv;out vec4 fragColor;void main(){vec4 c=texture(Sampler0,uv);if(c.a<0.1)discard;fragColor=vec4(c.rgb*Brightness,c.a);}')
  base=functions['glCreateProgram']();functions['glAttachShader'](base,vs);functions['glAttachShader'](base,fs)
  for index,name in enumerate(['Position','UV0','Color']):functions['glBindAttribLocation'](base,index,name.encode())
  functions['glLinkProgram'](base);ok=i();functions['glGetProgramiv'](base,0x8b82,C.byref(ok));assert ok.value,'Preview base shader link'
  for index,size,offset in [(0,3,0),(1,2,12),(2,4,20)]:enableAttrib(index);attrib(index,size,0x1406,0,36,p(offset))
  with zipfile.ZipFile('C:/Users/pavel/.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar') as z:
   sword=Image.open(io.BytesIO(z.read('assets/minecraft/textures/item/iron_sword.png'))).convert('RGBA')
   faces=[[[0,0,.5,0,1],[1,0,.5,1,1],[1,1,.5,1,0],[0,1,.5,0,0]]]
   models=[dict(id='minecraft:iron_sword',faces=faces,texture=tex(sword),armor=False)]
   raw=json.loads((R/'src/main/resources/assets/slavicmyths/models/item/perunite_sword.json').read_text());faces=[]
   for el in raw['elements']:
    x,y,z0=el['from'];X,Y,Z=el['to']
    corners={'north':[(X,y,z0),(x,y,z0),(x,Y,z0),(X,Y,z0)],'south':[(x,y,Z),(X,y,Z),(X,Y,Z),(x,Y,Z)],'east':[(X,y,Z),(X,y,z0),(X,Y,z0),(X,Y,Z)],'west':[(x,y,z0),(x,y,Z),(x,Y,Z),(x,Y,z0)],'up':[(x,Y,z0),(x,Y,Z),(X,Y,Z),(X,Y,z0)],'down':[(x,y,Z),(x,y,z0),(X,y,z0),(X,y,Z)]}
    for name,face in el['faces'].items():
     uv=face['uv'];points=corners[name];uvs=[(uv[0]/16,uv[3]/16),(uv[2]/16,uv[3]/16),(uv[2]/16,uv[1]/16),(uv[0]/16,uv[1]/16)]
     turns=face.get('rotation',0)//90;uvs=uvs[-turns:]+uvs[:-turns] if turns else uvs
     faces.append([[px/16,py/16,pz/16,U,V] for (px,py,pz),(U,V) in zip(points,uvs)])
   models.append(dict(id='slavicmyths:perunite_sword',faces=faces,texture=tex(Image.open(R/'src/main/resources/assets/slavicmyths/textures/item/perunite_sword.png')),armor=False))
   for row in json.loads((R/'docs/implementation/rune-glow-104/native-armor-source.json').read_text()):
    texture=Image.open(io.BytesIO(z.read('assets/minecraft/'+row['texture'].split(':',1)[1]))) if row['texture'].startswith('minecraft:') else Image.open(R/'src/main/resources/assets/slavicmyths'/row['texture'])
    faces=[[[a/16,b/16,c/16,U,V] for a,b,c,U,V in face] for face in row['faces']]
    models.append(dict(id=row['id'],faces=faces,texture=tex(texture),armor=True))
  variant=next((a.split('=',1)[1] for a in sys.argv if a.startswith('--variant=')),'moderate')
  source=(O/'before' if '--before' in sys.argv else R)/'src/main/resources/assets/slavicmyths/textures/misc/rune_glint.png'
  patternImage=Image.open(source)
  if '--before' not in sys.argv:
   from PIL import ImageEnhance
   channels=list(patternImage.split());channels[0]=ImageEnhance.Brightness(channels[0]).enhance({'weak':.75,'moderate':1,'upper':1.2}[variant]);patternImage=Image.merge('RGB',channels)
  pattern=tex(patternImage)
  width,height=960,1600;GL.glViewport(0,0,width,height);GL.glClearColor(.13,.16,.19,1);GL.glClear(0x4000|0x0100);GL.glEnable(0x0b71);GL.glDisable(0x0b44);GL.glDisable(0x0be2);GL.glDepthMask(1)
  captions=[]
  for mi,model in enumerate(models):
   data=np.array([vertex for face in model['faces'] for k in [0,1,2,0,2,3] for vertex in [face[k]]],dtype='float32');pos=data[:,:3];lo=pos.min(0);hi=pos.max(0);center=(lo+hi)/2
   angle=math.radians(-25);c,s=math.cos(angle),math.sin(angle);mv=np.eye(4,dtype='float32');mv[:3,:3]=[[c,0,s],[0,-1 if model['armor'] else 1,0],[-s,0,c]];mv[:3,3]=-mv[:3,:3]@center
   transformed=pos@mv[:3,:3].T+mv[:3,3];extent=np.max(abs(transformed),0);projection=np.eye(4,dtype='float32');projection[0,0]=.8/max(extent[0],.1);projection[1,1]=.8/max(extent[1],.1);projection[2,2]=.8/max(extent[2],.1)
   # Keep the true source geometry/aspect ratio: shared XY scale for every rune count of this item.
   factor=min(projection[0,0],projection[1,1]);projection[0,0]=projection[1,1]=factor
   for shade in [0,1]:
    row=mi*2+shade;captions.append((row,model['id']+(' / source daylight' if shade==0 else ' / source shade')))
    for count in range(4):
     GL.glViewport(count*240+10,height-(row+1)*200+15,220,155)
     def draw(program,overlay):
      use(program)
      layered=mv.copy()
      if model['armor'] and (not overlay or '--depth-regression' not in sys.argv or count!=1): layered[:3,:]*=0.99975586
      for name,matrix in [('ModelViewMat',layered),('ProjMat',projection)]:mat(loc(program,name.encode()),1,0,matrix.T.copy().ctypes.data_as(C.POINTER(f)))
      uniformI(loc(program,b'Sampler0'),0)
      vertices=np.concatenate([data,np.ones((len(data),4),dtype='float32')],axis=1)
      if overlay:
       vertices[:,3]=pos[:,0]+pos[:,2]*.6;vertices[:,4]=pos[:,1]-pos[:,2]*.25;vertices[:,5:8]=[0x55/255,0xc9/255,0x78/255];vertices[:,8]=([0,112,156,200] if '--before' in sys.argv else [0,24,40,58])[count]/255
       uniform(loc(program,b'GameTime'),.125);uniform(loc(program,b'FogStart'),50);uniform(loc(program,b'FogEnd'),100);uniformI(loc(program,b'FogShape'),0);color(loc(program,b'ColorModulator'),1,1,1,1);GL.glBindTexture(0x0de1,pattern)
      else:uniform(loc(program,b'Brightness'),1 if shade==0 else .35);GL.glBindTexture(0x0de1,model['texture'])
      vertices=np.ascontiguousarray(vertices,dtype='float32');bufferData(0x8892,vertices.nbytes,vertices.ctypes.data,0x88e4);GL.glDrawArrays(0x0004,0,len(vertices))
     GL.glDisable(0x0be2);GL.glDepthFunc(0x0203);GL.glDepthMask(1);draw(base,False)
     if count:
      GL.glEnable(0x0be2);GL.glBlendFunc(0x0302,0x0303);GL.glDepthFunc(0x0202);GL.glDepthMask(0);draw(glow,True)
  GL.glFinish();pixels=C.create_string_buffer(width*height*4);GL.glReadPixels(0,0,width,height,0x1908,0x1401,pixels);image=Image.frombytes('RGBA',(width,height),pixels.raw).transpose(Image.Transpose.FLIP_TOP_BOTTOM).convert('RGB');d=ImageDraw.Draw(image);font=ImageFont.truetype('C:/Windows/Fonts/arial.ttf',13)
  for row,label in captions:
   for count in range(4):d.text((count*240+10,row*200+4),label.split(' / ')[0].replace('minecraft:','').replace('slavicmyths:',''),font=font,fill='white');d.text((count*240+10,row*200+23),label.split(' / ')[1]+(f' / runes {count}' if '--depth-regression' not in sys.argv else [' / no runes',' / OLD depth mismatch',' / FIXED depth, 2 runes',' / FIXED depth, 3 runes'][count]),font=font,fill='white')
  metrics=[]
  raw=np.asarray(image).astype('int16')
  for row,label in captions:
   baseline=raw[row*200+45:row*200+195,10:230,:]
   diffs=[]
   for count in range(1,4):
    current=raw[row*200+45:row*200+195,count*240+10:count*240+230,:]
    diffs.append(int(np.abs(current-baseline).sum()))
   assert (diffs[0]==0 and diffs[1]>0 and diffs[2]>diffs[1]) if '--depth-regression' in sys.argv and 'chestplate' in label else 0<diffs[0]<diffs[1]<diffs[2],(label,diffs)
   metrics.append({'source_case':label,'absolute_rgb_difference_1_2_3':diffs,'scope':'standalone source GPU only'})
  (O/('before-metrics.json' if '--before' in sys.argv else variant+'-metrics.json')).write_text(json.dumps(metrics,indent=2)+'\n')
  banner=Image.new('RGB',(width,height+55),'#212930');banner.paste(image,(0,55));d=ImageDraw.Draw(banner);d.text((10,8),'ACTUAL SOURCE MESH/UV/PNG + PRODUCTION GLSL: standalone OpenGL, NOT Minecraft screenshots.',font=font,fill='white');d.text((10,29),'Same geometry, camera and light in 0/1/2/3. No hands/GUI/multiplayer acceptance is claimed.',font=font,fill='white');banner.save(O/('BEFORE.png' if '--before' in sys.argv else 'CANDIDATE-'+variant+'.png'))
  result['source_gpu_preview']='BEFORE.png' if '--before' in sys.argv else 'CANDIDATE-'+variant+'.png';result['source_models']=[m['id'] for m in models];result['source_preview_is_minecraft']=False
 finally:
  if base:functions['glDeleteProgram'](base)
  for shader in ownShaders:functions['glDeleteShader'](shader)
  for texture in textures:GL.glDeleteTextures(1,C.byref(texture))
  delBuffer(1,C.byref(vbo));delVAO(1,C.byref(vao))
