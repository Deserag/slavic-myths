from PIL import Image, ImageDraw, ImageFont
import os,math,zipfile,json,subprocess,hashlib
import numpy as np
from scipy import signal
R=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
P=os.path.join(R,'05_Visual_References','Technical')
A=os.path.join(R,'06_Audio')
os.makedirs(P,exist_ok=True)
font='/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf'
fontbold='/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf'
def f(sz=24,b=False): return ImageFont.truetype(fontbold if b else font,sz)
def txt(d,xy,s,sz=22,c='#302B2A',bold=False):d.text(xy,s,font=f(sz,bold),fill=c)
def card(d,box,fill='#F5EFE4',stroke='#B49F7F',radius=12):d.rounded_rectangle(box,radius=radius,fill=fill,outline=stroke,width=3)
def label(d,s,x,y,w=0):txt(d,(x,y),s,22,'#382A23',True)
# Technical sheet: clearly schematic not misleading actual model
im=Image.new('RGB',(1600,950),'#F0EADD'); d=ImageDraw.Draw(im)
d.rectangle((0,0,1600,96),fill='#51262A');txt(d,(50,26),'DOBRYNYA: IMPLEMENTATION SCHEMATIC • 2.40 BLOCKS',33,'#F6E8CD',True)
for x,title in [(44,'FRONT'),(440,'SIDE'),(837,'BACK'),(1230,'TOP (SCHEMATIC)')]:
 card(d,(x,118,x+324,860));txt(d,(x+24,140),title,29,'#612A2A',True)
scale=230; x=200;ybase=792
# hero front based on approved image with stylized pixels
for cx,depth in [(200,0),(590,0),(998,0)]:
 # legs + boots
 for lx in [-37,37]:
  d.rectangle((cx+lx-25,ybase-115,cx+lx+20,ybase),fill='#343538',outline='#27282B',width=3)
  d.rectangle((cx+lx-29,ybase-27,cx+lx+24,ybase),fill='#443328')
 # torso
 d.rectangle((cx-94,ybase-386,cx+94,ybase-132),fill='#61666B',outline='#35383C',width=5)
 # horizontal grey lames
 for i in range(6):
  yy=ybase-380+i*32;d.line((cx-86,yy,cx+85,yy),fill='#A6ACB0',width=5)
 # shoulders
 for side in [-1,1]:
  xx=cx+side*109
  d.rectangle((xx-40,ybase-392,xx+40,ybase-302),fill='#737981',outline='#3A3B3F',width=4)
 # head / white hair
 d.rectangle((cx-47,ybase-502,cx+47,ybase-388),fill='#CCA18B',outline='#30282A',width=5)
 d.rectangle((cx-51,ybase-517,cx+50,ybase-470),fill='#E6E7E1')
 for ss in [-1,1]:
  d.rectangle((cx+ss*52-11,ybase-492,cx+ss*52+8,ybase-396),fill='#CDD1CF')
 d.line((cx-29,ybase-422,cx+29,ybase-422),fill='#B6B6B4',width=8) # STUBBLE low depth
 d.rectangle((cx-28,ybase-460,cx-13,ybase-450),fill='#4A7391')
 d.rectangle((cx+12,ybase-460,cx+27,ybase-450),fill='#4A7391')
 d.rectangle((cx-72,ybase-132,cx+70,ybase-109),fill='#593126')
 # cape back
 if cx==998:
  d.polygon([(cx-83,ybase-378),(cx+83,ybase-378),(cx+112,ybase-80),(cx-111,ybase-80)],fill='#77272D')
  d.line([(cx,ybase-310),(cx-24,ybase-290),(cx,ybase-235),(cx+24,ybase-290),(cx,ybase-310)],fill='#DBB275',width=7)
 # shield at hero left in front / back
 if cx!=590:
  sx=cx+129;d.ellipse((sx-43,ybase-305,sx+44,ybase-85),fill='#9E3537',outline='#C9A063',width=9)
  d.line((sx,ybase-250,sx,ybase-135),fill='#E1C983',width=9)
 # spear front 3.6/2.4 scale (extra above 2.4 head)
 if cx==200:
  sxx=cx-143
  d.line((sxx,ybase-35,sxx,ybase-35-590),fill='#6D4A2F',width=15)
  d.polygon([(sxx,ybase-35-590-24),(sxx-18,ybase-35-590+46),(sxx+18,ybase-35-590+46)],fill='#C5C9CA')
# top views schematic, height text
x=1395; y=450
for xy,shape in [((x-98,y-56,x+100,y+90),'shoulders'),((x-73,y-46,x+75,y+76),'torso'),((x-41,y-36,x+41,y+38),'head')]:d.rounded_rectangle(xy,radius=11,fill={'shoulders':'#7A8087','torso':'#505357','head':'#E0E1DD'}[shape],outline='#29292A',width=4)
d.line((x-150,y-170,x-150,y+195),fill='#70472C',width=13);d.polygon([(x-150,y-208),(x-164,y-157),(x-136,y-157)],fill='#D5D7D7')
d.ellipse((x+102,y-120,x+194,y+115),fill='#973C3D',outline='#CFAC6B',width=9)
txt(d,(1260,730),'GRAY STEEL / WHITE HAIR',19,'#423534',True);txt(d,(1260,770),'SHORT STUBBLE, NO BEARD',18,'#A02930',True)
for sy,v in [(222,'Body 2.40 b'),(260,'Spear 3.60 b'),(298,'Shield 0.96 b')]:txt(d,(47,sy),v,19,'#382C2A',True)
txt(d,(50,882),'SCHEMATIC ONLY: source PNG defines the artistic look. See model dimensions and UV requirements in 01_Dobrynya.',18,'#67463D')
im.save(os.path.join(P,'Dobrynya_orthographic_geometry.png'))
# Marinka technical schematic, size chart + color palette
im=Image.new('RGB',(1400,850),'#F1E9DF');d=ImageDraw.Draw(im)
d.rectangle((0,0,1400,94),fill='#421A25');txt(d,(35,25),'MARINKA: VOLUMES + FORMS + HEX COLORS',31,'#F4E5D2',True)
for x,title in [(35,'FRONT / 2.10b'),(390,'BACK / BRAID'),(745,'MAGPIE / 0.9b wings'),(1070,'3 SHADOW FORMS')]:card(d,(x,120,x+294 if x<1070 else 1380,780));txt(d,(x+10,135),title,22,'#642A31',True)
for cx in [190,545]:
 y=710;d.polygon([(cx-59,478),(cx+59,478),(cx+92,y),(cx-90,y)],fill='#4D202B',outline='#26151A')
 for i in range(4):d.line((cx-72+i*44,680,cx-38+i*43,535),fill='#8E3540',width=5)
 d.rectangle((cx-57,329,cx+57,494),fill='#672B35',outline='#332027',width=5)
 for side in [-1,1]:d.rectangle((cx+side*60-11,340,cx+side*60+12,539),fill='#311D26')
 d.rectangle((cx-42,239,cx+42,341),fill='#DBAF9D',outline='#2D2222',width=3)
 d.rectangle((cx-47,220,cx+47,265),fill='#2C2629')
 if cx==545:
  d.line((cx+14,267,cx+23,553),fill='#272124',width=25)
  for k in range(9):d.ellipse((cx+10,280+k*28,cx+30,300+k*28),fill='#443039')
 else:
  d.ellipse((cx-19,286,cx-11,294),fill='#A44450');d.ellipse((cx+11,286,cx+19,294),fill='#A44450')
# magpie (side, wings)
cx=884; cy=460
d.ellipse((cx-70,cy-20,cx+30,cy+62),fill='#28313A',outline='#161B20',width=4)
d.ellipse((cx+10,cy-58,cx+70,cy+0),fill='#2C333B');d.polygon([(cx+64,cy-39),(cx+100,cy-24),(cx+64,cy-11)],fill='#876D48')
d.polygon([(cx-100,cy+20),(cx-250,cy+110),(cx-230,cy+131),(cx-55,cy+62)],fill='#21252D')
d.polygon([(cx+0,cy+0),(cx-130,cy-145),(cx-172,cy-158),(cx-27,cy+29)],fill='#E5E0D2');d.line((cx-105,cy-110,cx-37,cy+15),fill='#1E2228',width=15)
# clone silhouettes
for cx in [1145,1214,1283]:
 d.ellipse((cx-15,353,cx+15,403),fill='#B38B87')
 d.polygon([(cx-20,397),(cx+20,397),(cx+32,511),(cx-32,511)],fill='#602634')
 d.ellipse((cx-34,525,cx+34,537),outline='#353238',width=3)
txt(d,(1085,597),'Identical shape, effects, AI.',16,'#65383E');txt(d,(1085,628),'1 real + 2 false (24 HP).',17,'#A12B3B',True)
pal=['#21191F','#52222A','#7C202C','#C49B57','#E0B49E','#2B0C1D','#B52A47','#F85B59']
for i,c in enumerate(pal):
 xx=45+i*95;d.rectangle((xx,797,xx+70,829),fill=c);txt(d,(xx+2,803),str(i+1),13,'white' if i in [0,1,2,5] else 'black')
im.save(os.path.join(P,'Marinka_orthographic_form_palette.png'))
# topdown yard plan x range +-30 maps to 60 grid.
SZ=1250;im=Image.new('RGB',(SZ,1390),'#EFE5D4');d=ImageDraw.Draw(im)
d.rectangle((0,0,SZ,91),fill='#50262B');txt(d,(44,25),'RUINED WITCH YARD — 60 × 60 BLOCK GRID',30,'#F4E7CC',True)
O=(115,140);K=16;d.rectangle((O[0],O[1],O[0]+60*K,O[1]+60*K),fill='#B7A58A',outline='#5D4C3B',width=6)
for i in range(61):
 xx=O[0]+i*K; yy=O[1]+i*K; color='#AC987C' if i%5 else '#8D7F70';d.line((xx,O[1],xx,O[1]+60*K),fill=color,width=1 if i%5 else 2);d.line((O[0],yy,O[0]+60*K,yy),fill=color,width=1 if i%5 else 2)
def pos(x,z):return (O[0]+int((x+30)*K),O[1]+int((z+30)*K))
def rectcoords(x1,z1,x2,z2,fill,title,labelxy=None):
 p1=pos(x1,z1);p2=pos(x2,z2);d.rectangle((*p1,*p2),fill=fill,outline='#372B29',width=4);p=labelxy or (p1[0]+10,p1[1]+10);txt(d,p,title,20,'#FFF1DF',True)
# outer ruined edge
d.rectangle((O[0]+5,O[1]+5,O[0]+59*K,O[1]+59*K),outline='#473630',width=10)
# central combat ring
mx,my=pos(0,0);r=20*K;d.ellipse((mx-r,my-r,mx+r,my+r),outline='#A13948',width=9)
d.ellipse((mx-6*K,my-6*K,mx+6*K,my+6*K),outline='#6F1828',width=7)
d.ellipse((mx-22,my-22,mx+22,my+22),fill='#8B4A40')
rectcoords(-29,-27,-12,-9,'#574033','RUINED HOUSE')
rectcoords(12,-29,27,-14,'#6A503B','BARN')
rectcoords(20,-10,26,-4,'#66726B','WELL',labelxy=(pos(20,-10)[0],pos(20,-10)[1]+20))
rectcoords(-19,20,-14,23,'#5B4B3D','CART',labelxy=(pos(-19,20)[0],pos(-19,20)[1]-22))
rectcoords(18,19,26,24,'#8D603A','LEAN-TO')
for x,z in [(-24,14),(22,17),(-21,-2)]:
 px,py=pos(x,z);d.ellipse((px-34,py-34,px+34,py+34),fill='#4E5342',outline='#292C2D',width=4);d.line((px-18,py-18,px+18,py+18),fill='#958B6E',width=6)
for x,z in [(-10,10),(11,9),(-5,-12)]:
 px,py=pos(x,z);d.ellipse((px-23,py-23,px+23,py+23),outline='#842638',width=5)
# entry
d.rectangle((mx-3*K,O[1]+56*K,mx+3*K,O[1]+60*K),fill='#E3CA9C')
txt(d,(mx-64,O[1]+58*K),'ENTRY',18,'#49342A',True)
txt(d,(mx-80,my+16),'MAIN RITUAL',21,'#661B2D',True)
txt(d,(mx-64,my+43),'HATCH BELOW',17,'#661B2D')
# right legend
card(d,(1120,140,1210,1110));d.line((1166,200,1166,950),fill='#5A343B',width=3)
for z,t in [(190,'N'),(935,'S')]:txt(d,(1153,z),t,25,'#653039',True)
for i,ln in enumerate(['scale:', '1 cell = 1 block','center = (0,0)','outside = 60 x 60','combat dia. 40-45','house NW','barn NE','well NE','entry S','secret hatch center']):txt(d,(70,1150+i*21),ln,17,'#5D4140')
txt(d,(700,1310),'Schematic coordinates; rotate and place via structure origin.',18,'#5D4140')
im.save(os.path.join(P,'Yard_top_down_60x60.png'))
# basement diagram
im=Image.new('RGB',(1400,1020),'#EDE3D7');d=ImageDraw.Draw(im);d.rectangle((0,0,1400,100),fill='#50262B');txt(d,(45,25),'HIDDEN WITCH BASEMENT — ROOM PLAN + ELEVATION',30,'#F5E8D4',True)
card(d,(35,130,945,935));card(d,(970,130,1370,935));Ox=130;Oy=235;S=30
# approx 17x23 block with border
for ix in range(18):d.line((Ox+ix*S,Oy,Ox+ix*S,Oy+23*S),fill='#BCAD9E',width=1)
for iz in range(24):d.line((Ox,Oy+iz*S,Ox+17*S,Oy+iz*S),fill='#BCAD9E',width=1)
d.rectangle((Ox,Oy,Ox+17*S,Oy+23*S),outline='#474642',width=14)
# 3 functional rooms
for rect,fill,title in [((1,7,8,17),'#7E6359','WORKSHOP'),((9,7,16,17),'#6D5554','TRANSFORMATION'),((5,1,13,6),'#76684D','SECRET STORE'),((7,18,10,22),'#81716E','ENTRY STAIR')]:
 x1,z1,x2,z2=rect;d.rectangle((Ox+x1*S,Oy+z1*S,Ox+x2*S,Oy+z2*S),fill=fill,outline='#3D3935',width=7);txt(d,(Ox+x1*S+8,Oy+z1*S+25),'STAIR' if title=='ENTRY STAIR' else title,15 if title=='ENTRY STAIR' else 18,'#FFF1D4',True)
for x,z in [(5,12),(12,12),(9,4)]:
 px,py=Ox+x*S,Oy+z*S;d.ellipse((px-14,py-14,px+14,py+14),fill='#C49953')
txt(d,(1020,220),'ROOM TARGETS',25,'#5E3336',True)
for i,t in enumerate(['Upper hatch', 'Y=0 courtyard','2-block safe stair','Workshop 7x9','Transformation 7x9','Hidden store ~5x6','Floor Y = -10','Clear interior H = 5','3 subareas + halls','Ordinary loot only','Unlock after win']):txt(d,(995,275+i*50),t,19,'#563E3A')
txt(d,(140,944),'Schematic placement; fitted partitions must preserve two-wide access and valid collision.',18,'#674239')
im.save(os.path.join(P,'Ritual_basement_floorplan.png'))
# audio generated original per event, no external material; controllable template-based synth
SR=44100
rng=np.random.default_rng(172)
def add_sfx(outpath,kind,seconds=1.0,pitch=1.0,seed=0):
 rr=np.random.default_rng(172+seed);n=int(SR*seconds); t=np.arange(n,dtype=np.float64)/SR
 white=rr.normal(size=n); hp=signal.lfilter([1,-.93],[1],white);lp=signal.lfilter([.08],[1,-.92],white)
 wood=signal.lfilter([.16],[1,-.84],white);art=np.zeros(n);base={'metal':470,'magic':280,'whoosh':130,'wing':220,'low':80,'creak':110,'wood':210,'liquid':420,'chime':580,'whisper':280,'foot':170,'fire':100,'crack':800}.get(kind,240)*pitch
 for harmon,dec,amt in [(1,3,.32),(2,7,.18),(3,13,.09)]:art+=amt*np.sin(2*np.pi*base*harmon*t + .07*np.sin(2*np.pi*2.7*t))*np.exp(-dec*t/max(seconds,.2))
 if kind in ('metal','chime','crack'):
  y=.65*art+.22*hp*np.exp(-t*11/seconds)+.2*lp
 elif kind in ('magic','whisper'):
  warp=np.sin(2*np.pi*(base*.5*t+50*t*t))
  y=.18*art+.28*lp*np.sin(2*np.pi*(3.7+seed%4)*t)+.27*warp+.16*white*np.exp(-t*2/seconds)
 elif kind in ('fire','whoosh','wing'):
  mod=(.48+.52*np.cos(2*np.pi*(7 if kind=='wing' else 2)*t)**2)
  y=.26*art+.36*hp*mod+.2*lp
 elif kind in ('wood','foot','creak'):
  y=.35*art+.5*lp+.12*hp*np.exp(-t*8/seconds)
 elif kind=='liquid':
  bubbles=np.sin(2*np.pi*(200*t+90*t*t))*(np.sin(2*np.pi*5*t)**16)
  y=.15*art+.18*lp+.21*bubbles
 else:y=.3*art+.35*lp
 # multiple small tactile transient impulses
 impacts=1 if kind in ('metal','foot','wood','crack') else 0
 if impacts:
  for onset in [0.02,0.08]:
   idx=int(onset*SR); dur=min(int(SR*.06),n-idx)
   if dur>0:y[idx:idx+dur] += .2*rr.normal(size=dur)*np.exp(-np.arange(dur)/SR*74)
 # smooth envelope attack/release, avoid pops
 fadeIn=np.minimum(1,t/.015)
 fadeOut=np.minimum(1,(seconds-t)/.05)
 env=fadeIn*np.maximum(0,fadeOut)*np.exp(-.75*t/seconds)
 if kind in ('fire','wing','whisper','creak'):env=fadeIn*np.maximum(0,fadeOut)*np.exp(-.2*t/seconds)
 y=np.tanh(1.8*y*env);peak=np.max(np.abs(y))
 if peak>.001:y=y/peak*.67
 pcm=(np.clip(y,-1,1)*32767).astype('<i2').tobytes()
 os.makedirs(os.path.dirname(outpath),exist_ok=True)
 p=subprocess.run(['ffmpeg','-hide_banner','-loglevel','error','-y','-f','s16le','-ar',str(SR),'-ac','1','-i','pipe:0','-acodec','libvorbis','-qscale:a','4',outpath],input=pcm,stdout=subprocess.PIPE,stderr=subprocess.PIPE)
 if p.returncode: raise RuntimeError(p.stderr.decode())
 return seconds
sounds={
'dobrynya':[
('breath_idle_01','low',1.4),('armor_step_01','foot',.38),('spear_parry_01','metal',.66),('shield_ward_01','magic',.9),('magic_deflect_01','chime',.76),('spear_swing_01','whoosh',.52),('spear_impact_01','metal',.50),('spear_sweep_01','whoosh',1.15),('unravel_hex_01','magic',1.1),('grunt_hurt_01','low',.60),('aurochs_transform_01','magic',2.15),('aurochs_charge_01','low',1.38),('elixir_drink_01','liquid',2.1),('hero_return_01','chime',.9),('spear_swing_02','whoosh',.45),('armor_step_02','foot',.33)],
'marinka':[
('witch_whisper_01','whisper',1.6),('cloth_step_01','foot',.40),('trace_hex_01','magic',1.45),('fire_charge_01','fire',.75),('fire_release_01','fire',1.05),('shadow_dissolve_01','whisper',.90),('shadow_clone_break_01','crack',.60),('barrier_raise_01','magic',.85),('barrier_break_01','crack',.73),('witch_blink_01','whoosh',.78),('magpie_change_01','wing',.82),('magpie_wings_01','wing',.48),('aurochs_hex_01','magic',2.33),('enchanted_herd_01','low',1.18),('witch_hurt_01','whisper',.56),('witch_rage_01','magic',2.1),('witch_defeat_01','magic',2.4),('witch_whisper_02','whisper',1.3),('magpie_wings_02','wing',.44)],
'arena':[('yard_wind_01','creak',3.6),('ritual_circle_pulse_01','magic',1.15),('hatch_open_01','wood',2.65),('basement_ambience_01','whisper',3.0)]
}
manifest=[]
for group,items in sounds.items():
 for k,(name,kind,duration) in enumerate(items):
  path=os.path.join(A,group,name+'.ogg');add_sfx(path,kind,duration,seed=k+100*len(group));h=hashlib.sha256(open(path,'rb').read()).hexdigest()
  manifest.append({'group':group,'file':os.path.relpath(path,R),'length_sec':duration,'codec':'Vorbis','channels':1,'rate_hz':44100,'sha256':h,'created_by':'procedural_synthesis_included_script','usage':'nonverbal gameplay SFX; original generated, no cartoon samples'})
with open(os.path.join(A,'manifest.json'),'w',encoding='utf8') as fh:json.dump(manifest,fh,ensure_ascii=False,indent=2)
print('created diagrams',len(os.listdir(P)),'ogg',len(manifest))
# Additional scale-accurate height ratio sheet (160 px per block)
im=Image.new('RGB',(1120,880),'#EEE4D7');dd=ImageDraw.Draw(im)
dd.rectangle((0,0,1120,94),fill='#50272D')
txt(dd,(40,24),'LOCKED SCALE — 2.40 b HERO / 3.60 b SPEAR',31,'#F5E7CD',True)
ground=765; pix=160
# grid 0.2 block
for j in range(21):
 yy=ground-int(j*.2*pix)
 if yy>110:dd.line((60,yy,1040,yy),fill='#CBBDAA',width=1)
for idx,(xx,h,fill,lab) in enumerate([(180,1.8,'#4C86A0','PLAYER 1.80'),(475,2.4,'#777C84','DOBRYNYA 2.40'),(820,3.6,'#754E32','SPEAR 3.60')]):
 top=ground-int(h*pix)
 if idx==0:
  dd.rectangle((xx-45,top,xx+45,ground),fill=fill,outline='#25373A',width=5)
  dd.rectangle((xx-42,top+4,xx+42,top+90),fill='#D9AD86')
 if idx==1:
  dd.rectangle((xx-65,top+95,xx+65,ground-72),fill=fill,outline='#32373A',width=5)
  dd.rectangle((xx-43,top,xx+43,top+105),fill='#D5B09B',outline='#36303A',width=4)
  dd.rectangle((xx-47,top-2,xx+47,top+28),fill='#DFE3E1')
  dd.line((xx-38,top+87,xx+38,top+87),fill='#B8BBB8',width=8)
  for lx in [-28,28]:dd.rectangle((xx+lx-26,ground-75,xx+lx+22,ground),fill='#4D4541')
  dd.ellipse((xx+66,top+155,xx+140,top+310),fill='#8D3133',outline='#C8A46A',width=8)
 if idx==2:
  dd.line((xx,ground,xx,top+80),fill=fill,width=19)
  dd.polygon([(xx,top),(xx-27,top+90),(xx+27,top+90)],fill='#B4BEC3',outline='#4B5A61')
 dd.line((xx+155,ground,xx+155,top),fill='#702C39',width=4)
 dd.line((xx+142,top,xx+168,top),fill='#702C39',width=4)
 dd.line((xx+142,ground,xx+168,ground),fill='#702C39',width=4)
 txt(dd,(xx-65,795),lab,19,'#51383A',True)
txt(dd,(40,840),'Spear = 1.5x Dobrynya height. Use this scale even when hero concept art is illustrative.',17,'#633D37')
im.save(os.path.join(P,'Dobrynya_spear_exact_height_scale.png'))
