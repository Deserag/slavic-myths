"""Compile/link the exact shader in a temporary hidden WGL context. Never starts Minecraft.
No window is shown; all owned GL/window handles are released in finally.
"""
from pathlib import Path
import ctypes as C,json,zipfile,hashlib,sys
from ctypes import wintypes as W
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/rune-glow-104'
U=C.WinDLL('user32');G=C.WinDLL('gdi32');GL=C.WinDLL('opengl32')
class PFD(C.Structure):
 _fields_=[('nSize',W.WORD),('nVersion',W.WORD),('dwFlags',W.DWORD)]+[(n,C.c_ubyte) for n in ['iPixelType','cColorBits','cRedBits','cRedShift','cGreenBits','cGreenShift','cBlueBits','cBlueShift','cAlphaBits','cAlphaShift','cAccumBits','cAccumRedBits','cAccumGreenBits','cAccumBlueBits','cAccumAlphaBits','cDepthBits','cStencilBits','cAuxBuffers','iLayerType','bReserved']]+[(n,W.DWORD) for n in ['dwLayerMask','dwVisibleMask','dwDamageMask']]
U.CreateWindowExW.argtypes=[W.DWORD,W.LPCWSTR,W.LPCWSTR,W.DWORD,C.c_int,C.c_int,C.c_int,C.c_int,W.HWND,W.HMENU,W.HINSTANCE,C.c_void_p];U.CreateWindowExW.restype=W.HWND
U.GetDC.argtypes=[W.HWND];U.GetDC.restype=W.HDC
U.ReleaseDC.argtypes=[W.HWND,W.HDC];U.DestroyWindow.argtypes=[W.HWND]
G.ChoosePixelFormat.argtypes=[W.HDC,C.POINTER(PFD)];G.SetPixelFormat.argtypes=[W.HDC,C.c_int,C.POINTER(PFD)]
GL.wglCreateContext.argtypes=[W.HDC];GL.wglCreateContext.restype=C.c_void_p
GL.wglMakeCurrent.argtypes=[W.HDC,C.c_void_p];GL.wglDeleteContext.argtypes=[C.c_void_p]
GL.wglGetProcAddress.argtypes=[C.c_char_p];GL.wglGetProcAddress.restype=C.c_void_p
GL.glGetString.argtypes=[C.c_uint];GL.glGetString.restype=C.c_char_p
window=dc=context=None;shaders=[];program=0;functions={};result={'status':'NOT_RUN','minecraft_launches':0,'visible_windows':0}
def proc(name,restype,*args):
 ptr=GL.wglGetProcAddress(name.encode());assert ptr not in [None,0,1,2,3,C.c_void_p(-1).value],name
 f=C.WINFUNCTYPE(restype,*args)(ptr);functions[name]=f;return f
try:
 window=U.CreateWindowExW(0,'STATIC','Rune shader compiler (hidden)',0x80000000,0,0,960 if '--preview' in sys.argv else 8,1600 if '--preview' in sys.argv else 8,None,None,None,None);assert window,'Hidden window unavailable'
 dc=U.GetDC(window);pfd=PFD();pfd.nSize=C.sizeof(PFD);pfd.nVersion=1;pfd.dwFlags=0x25;pfd.cColorBits=24;pfd.cDepthBits=24
 pixel=G.ChoosePixelFormat(dc,C.byref(pfd));assert pixel and G.SetPixelFormat(dc,pixel,C.byref(pfd)),'Pixel format unavailable'
 context=GL.wglCreateContext(dc);assert context and GL.wglMakeCurrent(dc,context),'WGL context unavailable'
 version=GL.glGetString(0x1f02).decode();result['opengl_version']=version
 assert int(version.split('.')[0])>=3,'No GLSL150-capable context'
 create=proc('glCreateShader',C.c_uint,C.c_uint);source=proc('glShaderSource',None,C.c_uint,C.c_int,C.POINTER(C.c_char_p),C.POINTER(C.c_int));compile_=proc('glCompileShader',None,C.c_uint)
 get=proc('glGetShaderiv',None,C.c_uint,C.c_uint,C.POINTER(C.c_int));log=proc('glGetShaderInfoLog',None,C.c_uint,C.c_int,C.POINTER(C.c_int),C.c_char_p)
 deleteShader=proc('glDeleteShader',None,C.c_uint)
 with zipfile.ZipFile('C:/Users/pavel/.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar') as z:fog=z.read('assets/minecraft/shaders/include/fog.glsl').decode()
 # Minecraft's GlslPreprocessor strips imported #version directives.
 fog='\n'.join(line for line in fog.splitlines() if not line.lstrip().startswith('#version'))
 for kind,ext in [(0x8b31,'vsh'),(0x8b30,'fsh')]:
  p=R/f'src/main/resources/assets/slavicmyths/shaders/core/rune_glint.{ext}';raw=p.read_bytes();result[ext+'_sha256']=hashlib.sha256(raw).hexdigest();code=raw.decode().replace('#moj_import <fog.glsl>',fog).encode();shader=create(kind);shaders.append(shader);strings=(C.c_char_p*1)(code);source(shader,1,strings,None);compile_(shader);status=C.c_int();get(shader,0x8b81,C.byref(status))
  if not status.value:
   buf=C.create_string_buffer(8192);log(shader,8192,None,buf);raise RuntimeError('COMPILE '+ext+': '+buf.value.decode())
 createProgram=proc('glCreateProgram',C.c_uint);attach=proc('glAttachShader',None,C.c_uint,C.c_uint);bind=proc('glBindAttribLocation',None,C.c_uint,C.c_uint,C.c_char_p);link=proc('glLinkProgram',None,C.c_uint)
 getProgram=proc('glGetProgramiv',None,C.c_uint,C.c_uint,C.POINTER(C.c_int));programLog=proc('glGetProgramInfoLog',None,C.c_uint,C.c_int,C.POINTER(C.c_int),C.c_char_p);deleteProgram=proc('glDeleteProgram',None,C.c_uint)
 program=createProgram()
 for shader in shaders:attach(program,shader)
 for i,name in enumerate(['Position','UV0','Color']):bind(program,i,name.encode())
 link(program);status=C.c_int();getProgram(program,0x8b82,C.byref(status))
 if not status.value:
  buf=C.create_string_buffer(8192);programLog(program,8192,None,buf);raise RuntimeError('LINK: '+buf.value.decode())
 uniform=proc('glGetUniformLocation',C.c_int,C.c_uint,C.c_char_p)
 descriptor=json.loads((R/'src/main/resources/assets/slavicmyths/shaders/core/rune_glint.json').read_text())
 for row in descriptor['uniforms']:assert uniform(program,row['name'].encode())>=0,'Inactive/missing uniform '+row['name']
 if '--preview' in sys.argv:
  from rune104_preview_gl import render
  render(proc,functions,program,GL,result)
 result['status']='PASS_STANDALONE_SHADER_COMPILE_LINK';result['minecraft_shader_reload']='NOT_RUN';result['minecraft_visual_acceptance']='NOT_RUN'
except Exception as e:
 result['status']='FAIL' if isinstance(e,RuntimeError) else 'BLOCKED_STANDALONE_GL';result['error']=str(e)
finally:
 if program and 'glDeleteProgram' in functions:functions['glDeleteProgram'](program)
 if 'glDeleteShader' in functions:
  for shader in shaders:functions['glDeleteShader'](shader)
 if context:GL.wglMakeCurrent(None,None);GL.wglDeleteContext(context)
 if dc and window:U.ReleaseDC(window,dc)
 if window:U.DestroyWindow(window)
 (O/'shader-compile.json').write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result))
if result['status']=='FAIL':raise SystemExit(1)
