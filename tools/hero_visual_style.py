"""Shared vanilla-like face and material pass for the native story rigs.

Keeps bone names, pivots, animation controllers and weapon silhouettes intact.
Faces use pixel markings on one cube; hair uses a cap/back shell, not intersecting locks.
"""
HUMANS={'ilya_muromets':('hair','eye'),'dobrynya_nikitich':('white','eye'),
        'alyosha_popovich':('blond','blue'),'finist':('sand','iris'),
        'marinka':('hair','witcheye'),'kartaus':('rust','lens')}
STYLED=set(HUMANS)|{'idolishche_poganoe','skim_beast','kalin_tsar','kalin_war_horse','kalin_common_soldier','kalin_elite_soldier'}

def prepare(rig):
 if rig.name=='ilya_muromets':
  for name,bone in rig.bones.items():
   bone['pos'][0]*=.85
  for b in rig.boxes:
   old=b['center'][0];b['center'][0]*=.85
   if b['bone'] not in ('head','mace','shield','flask'):b['size'][0]*=.85
   if b['bone']=='neck':b['center'][1]-=1.2
 from hero_grip_style import apply
 apply(rig)
 if rig.name not in HUMANS:return
 hair,eye=HUMANS[rig.name]
 base=max((b for b in rig.boxes if b['bone']=='head' and b['material'] in ('skin','witchskin','pallid')),key=lambda b:b['size'][0]*b['size'][1]*b['size'][2])
 top=max(b['center'][1]+b['size'][1]/2 for b in rig.boxes if b['bone']=='head')
 # Remove facial relief and the many overlapping hair/moustache cubes. Other bones stay.
 removed=[b for b in rig.boxes if b['bone']=='head' and b is not base and b['material'] in (hair,eye,'skin','witchskin','pallid','leather','boots')]
 for b in removed:
  rig.boxes.remove(b);rig.bones['head']['boxes'].remove(b)
 # Integer eight-pixel face matches vanilla humanoid skin density.
 cx,cy,cz=base['center'];base['size']=[8,base['size'][1],8]
 rig.box('head',cx,top-.4,cz,8.4,.4,8.4,hair)
 rig.box('head',cx,cy-3,cz+4.05,8.2,6,.4,hair)

def pixel(mat,colors,x,y,w,h,d,idx,name,rw,rh):
 """Low contrast, face-aligned accents. No random camouflage on metal or skin."""
 base,shadow,light=colors[:3]
 if name not in STYLED:
  # Preserve textures of untouched birds, props and transformation forms when their
  # full original generator is run. They retain the accepted legacy material rules.
  noise=(x*17+y*31+idx*11)%29
  if mat=='steel':return colors[1 if (x+y//2)%4==0 or y%3==0 and x%3==1 else 2 if noise==4 else 0]
  if mat in ('wood','leather'):return colors[1 if (x//2+y//7)%4==0 else 2 if noise<3 else 0]
  if mat in ('red','cloth','shield'):return colors[2 if y%9==7 and x%3!=1 else 1 if x%7==0 else 0]
  if mat=='fur':return colors[1 if (x//2+y//2+idx)%4==0 else 2 if noise<8 else 0]
  if mat=='flesh':return colors[1 if (x//3+y//4+idx)%8==0 or y%11==8 else 2 if noise<3 else 0]
  if name in ('finist_falcon','kartaus_banner','kartaus_curse_anchor'):
   if mat=='chain':return colors[1 if (x+y//2)%4==0 else 2 if x%4==1 and y%3==0 else 0]
   return colors[1 if (x//4+y//5+idx)%7==0 else 2 if x%rw==0 or y%rh==0 else 0]
  return colors[1 if noise<4 else 2 if noise==10 else 0]
 blend=lambda a,b,t:tuple(round(a[k]*(1-t)+b[k]*t) for k in range(3))+(255,)
 if mat in ('steel','iron'):
  return blend(base,light,.45) if y==d or x==d else blend(base,shadow,.16) if y==d+h-1 else base
 if mat=='chain':
  return blend(base,shadow,.24) if (x%3==0 and y%2==0) else blend(base,light,.2) if (x%3==1 and y%2==0) else base
 if mat in ('skin','witchskin','pallid','flesh'):
  return blend(base,shadow,.08) if x==d or y==d+h-1 else base
 if mat in ('hair','blond','sand','white'):
  return blend(base,shadow,.16) if x%4==0 else base
 if mat in ('wood','leather','boots'):
  return blend(base,shadow,.12) if x%5==0 else blend(base,light,.12) if y==d else base
 if mat in ('black','red','crimson','cloth','cream','wine','shield','navy'):
  return blend(base,shadow,.12) if x==d or y==d+h-1 else base
 if mat in ('coat','mane','fur'):
  return blend(base,shadow,.15) if (x//3+y//4+idx)%6==0 else base
 return base

def face(rig,img,palette):
 if rig.name not in HUMANS:return
 hair,eye=HUMANS[rig.name]
 base=max((b for b in rig.boxes if b['bone']=='head' and b['material'] in ('skin','witchskin','pallid')),key=lambda b:b['size'][0]*b['size'][1]*b['size'][2])
 x,y,_,_=base['uv'];origin_x,origin_y=x,y;x+=8;y+=8
 color=lambda mat:tuple(bytes.fromhex(palette[mat][0]))+(255,)
 def put(px,py,c):img.putpixel((x+px,y+py),c)
 for py in range(8):
  for px in range(8):img.putpixel((origin_x+8+px,origin_y+py),color(hair))
 for offset in (0,16):
  for py in range(2):
   for px in range(8):img.putpixel((origin_x+offset+px,origin_y+8+py),color(hair))
 for px in range(8):put(px,0,color(hair))
 for px in (0,7):put(px,1,color(hair))
 for side in (1,5):
  put(side,3,(224,220,204,255));put(side+1,3,color(eye))
  put(side,2,color(hair));put(side+1,2,color(hair))
 put(3,4,color(base['material']));put(4,4,color(base['material']))
 chin=min(7,round(base['size'][1])-1)
 for px in (3,4):put(px,chin-1,(93,62,48,255))
 if rig.name in ('ilya_muromets','dobrynya_nikitich','kartaus'):
  for py in range(chin-(3 if rig.name=='ilya_muromets' else 2),chin+1):
   for px in range(1,7):
    if py in (4,5) and px in (3,4):continue
    put(px,py,color(hair))
  for px in (3,4):put(px,chin-1,(64,43,34,255))
