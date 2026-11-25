"""Hand-authored 16px skill symbols, matching winter HUD layout and atlas contract."""
from pathlib import Path
from PIL import Image,ImageDraw
A=Path(__file__).resolve().parents[1]/'src/main/resources/assets/slavicmyths/textures/gui/weapon_skills'
G='#ffd16e';L='#fff1bc';B='#6d442c';C='#73d8ce';D='#3b2926'
atlas=Image.new('RGBA',(128,64));sheet=Image.new('RGB',(8*112,4*132),'#20242b');sd=ImageDraw.Draw(sheet)
names=['double strike','dagger throw','sling shot','glass dash','solar beam','solar burst','dune wave','dune smash','triple arrow','piercing arrow','whip pull','whip sweep','summon ants','ant swarm','swarm command','wind punch','four punches','giant fist','fire wave','sun ray','sand vortex','ground slam','quicksand','sand pillars','ice self heal','ice area heal','fire self heal','fire area heal','bow draw']
for i in range(29):
 im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
 def line(p,c=G,w=1):d.line(p,fill=c,width=w)
 def arrow(x,y,dx=0,dy=-8):
  line([(x,y),(x+dx,y+dy)],L);xx=x+dx;yy=y+dy;line([(xx-2,yy+2),(xx,yy),(xx+2,yy+2)],G)
 def sword(x=8):line([(x-4,13),(x,9)],B,2);line([(x-3,8),(x+1,12)],G);line([(x,9),(x+5,3)],L,2)
 def sun(x=8,y=7):
  d.rectangle((x-2,y-2,x+2,y+2),fill=G);d.point((x-1,y-1),fill=L)
  for dx,dy in [(0,-4),(0,4),(-4,0),(4,0),(-3,-3),(3,-3),(-3,3),(3,3)]:d.point((x+dx,y+dy),fill=G)
 def fist(x=4,y=5):
  d.rectangle((x,y,x+6,y+5),fill=B);d.rectangle((x,y,x+5,y+2),fill=L);d.rectangle((x+1,y+4,x+6,y+6),fill=G);d.point((x+2,y+1),fill=B);d.point((x+4,y+1),fill=B)
 def ant(x,y):
  for a in range(3):d.rectangle((x,y+a*2,x+1,y+a*2+1),fill=G)
  line([(x-2,y+1),(x,y+2),(x-2,y+4)],L);line([(x+3,y+1),(x+1,y+2),(x+3,y+4)],L)
 if i==0:sword(4);sword(9)
 elif i==1:sword(6);line([(1,6),(4,3)],G);line([(2,10),(4,8)],G)
 elif i==2:d.arc((2,1,12,11),10,300,fill=B,width=2);d.rectangle((7,6,10,9),fill=L);arrow(3,14,0,-8)
 elif i==3:sword(7);line([(0,12),(4,9)],C);line([(1,8),(4,6)],C)
 elif i==4:sun(4,8);line([(7,7),(14,3)],L,2);line([(8,10),(14,6)],G)
 elif i==19:line([(7,1),(7,13)],L,2);line([(3,4),(11,4)],G);sun(8,9)
 elif i==5:sun();d.arc((1,0,15,14),0,360,fill=L)
 elif i==6:line([(1,13),(4,8),(7,11),(10,7),(14,11)],G,2);line([(3,5),(5,3),(7,5)],L)
 elif i==7:line([(9,2),(6,9)],B,2);d.rectangle((6,2,12,5),fill=G);line([(1,13),(5,11),(8,14),(11,11),(14,13)],L)
 elif i==8:
  for x in [3,8,13]:arrow(x,14,0,-10)
 elif i==9:arrow(8,15,0,-14);line([(3,7),(13,7)],B,2);line([(3,11),(13,11)],B,2)
 elif i==10:d.arc((3,2,14,12),190,520,fill=G,width=2);line([(3,8),(1,8),(1,14)],B,2);arrow(8,12,-4,-4)
 elif i==11:d.arc((1,2,14,13),20,340,fill=G,width=2);line([(11,2),(14,4),(14,1)],L)
 elif i==12:ant(7,5);line([(2,2),(4,2)],C);line([(12,2),(14,2)],C)
 elif i==13:
  for x,y in [(3,2),(10,3),(6,9)]:ant(x,y)
 elif i==14:ant(7,7);arrow(8,7,0,-6)
 elif i==15:fist(7,5);line([(1,6),(5,6)],C);line([(0,9),(4,9)],L)
 elif i==16:
  for x,y in [(1,1),(8,1),(1,9),(8,9)]:d.rectangle((x,y,x+4,y+4),fill=G);line([(x,y),(x+3,y)],L)
 elif i==17:fist(4,1);line([(2,14),(6,12),(9,14),(13,12)],C)
 elif i==18:
  d.polygon([(2,14),(1,8),(5,10),(7,2),(10,8),(13,5),(14,14)],fill='#e87829');d.polygon([(5,14),(7,7),(11,14)],fill=L)
 elif i==20:
  for y in [3,6,9,12]:line([(8-(y//2),y),(8+(y//2),y)],G)
  line([(6,1),(10,1)],L)
 elif i==21:fist(5,2);line([(1,13),(4,11),(7,14),(11,11),(14,13)],L)
 elif i==22:d.ellipse((1,8,14,14),outline=G,width=2);line([(7,2),(7,10)],L);line([(4,7),(7,10),(10,7)],L)
 elif i==28:d.arc((2,1,12,14),90,270,fill=G,width=2);line([(7,1),(12,8),(7,14)],L)
 elif i==23:
  for x,h in [(2,7),(7,12),(12,9)]:d.rectangle((x,14-h,x+2,14),fill=B);line([(x,14-h),(x+2,14-h)],L)
 else:
  color=C if i<26 else '#ff9e4b';d.rectangle((6,3,9,12),fill=color);d.rectangle((3,6,12,9),fill=color)
  if i%2==1:d.arc((0,0,15,15),0,360,fill=L)
  else:line([(1,3),(3,1)],L);line([(12,14),(14,12)],L)
 atlas.alpha_composite(im,(i%8*16,i//8*16));sheet.paste(im.resize((80,80),Image.Resampling.NEAREST),(i%8*112+16,i//8*132+8),im.resize((80,80),Image.Resampling.NEAREST));sd.text((i%8*112+2,i//8*132+96),names[i],fill='white')
atlas.save(A/'desert_atlas.png');out=Path(__file__).resolve().parents[1]/'art/desert-weapons/revision-2/skills.png';sheet.save(out);print('29 distinct 16px skill icons')
