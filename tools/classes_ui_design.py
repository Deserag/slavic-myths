"""Approval-stage PNG design only. Does not edit production GUI/resources or launch Minecraft.
Uses installed Minecraft bitmap font glyphs and existing Slavic Myths icons.
The layout and state fixture are exported so the later GUI can implement approved rectangles.
"""
from pathlib import Path
from collections import defaultdict
import csv,io,json,math,re,hashlib,zipfile
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'docs/media/classes-ui-redesign';OUT.mkdir(parents=True,exist_ok=True)
ASSETS=ROOT/'src/main/resources/assets/slavicmyths'
MC=Path('C:/Users/pavel/.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar')
C={'bg':'#17120F','tree':'#201914','panel':'#2B2119','button':'#463022','hover':'#5C4029','gold':'#D5B16D','bronze':'#8A6745','text':'#F2E8D8','muted':'#CBBBA4','disabled':'#988A7A','error':'#F09B91','available':'#DCC37D','learned':'#A8BF82'}
LANG=json.loads((ASSETS/'lang/ru_ru.json').read_text(encoding='utf-8'))
GLYPHS={}
with zipfile.ZipFile(MC) as z:
 for provider in json.loads(z.read('assets/minecraft/font/include/default.json'))['providers']:
  ns,file=provider['file'].split(':');atlas=Image.open(io.BytesIO(z.read(f'assets/{ns}/textures/{file}'))).convert('RGBA')
  chars=provider['chars'];cw=atlas.width//len(chars[0]);ch=atlas.height//len(chars);size=provider.get('height',8);factor=size/ch
  for row,sequence in enumerate(chars):
   for col,char in enumerate(sequence):
    if char=='\0' or char in GLYPHS:continue
    glyph=atlas.crop((col*cw,row*ch,(col+1)*cw,(row+1)*ch));bbox=glyph.getchannel('A').getbbox()
    advance=int((bbox[2] if bbox else 0)*factor+.5)+1
    GLYPHS[char]=(glyph.resize((round(cw*factor),size),Image.Resampling.NEAREST),advance,7-provider['ascent'])
GLYPHS[' ']=(Image.new('RGBA',(4,8)),4,0)
OPTIONS=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing/.minecraft/options.txt')
KEYS=dict(line[4:].split(':',1) for line in OPTIONS.read_text(encoding='utf-8').splitlines() if line.startswith('key_') and ':' in line)
OWN=['key.slavicmyths.path_ability','key.slavicmyths.class_slot_2','key.slavicmyths.class_slot_3']
BINDINGS=[KEYS[key] for key in OWN]
KEY_LABELS=[{'key.mouse.left':'ЛКМ','key.mouse.right':'ПКМ'}.get(value,value.rsplit('.',1)[-1].upper()) for value in BINDINGS]
CONFLICTS=[[key for key,value in KEYS.items() if key!=own and value==KEYS[own]] for own in OWN]
missing=set();checks=0;manifest=[]
def check(value,message):
 global checks
 checks+=1
 assert value,message
def glyph(char):
 if char not in GLYPHS:missing.add(char)
 return GLYPHS.get(char,GLYPHS['?'])
def text_width(text):return sum(glyph(c)[1] for c in text)
def wrap(text,width):
 lines=[]
 for paragraph in text.split('\n'):
  line=''
  for word in paragraph.split():
   candidate=(line+' '+word).strip()
   if line and text_width(candidate)>width:lines.append(line);line=word
   else:line=candidate
  lines.append(line)
 return lines
def text(im,label,x,y,color='text',center=False,scale=1):
 w=text_width(label);canvas=Image.new('RGBA',(max(1,w),12));cursor=0
 for char in label:
  mask,advance,offset=glyph(char);paint=Image.new('RGBA',mask.size,C[color] if color in C else color);paint.putalpha(mask.getchannel('A'));
  # Cursor accumulates Minecraft bitmap-provider advance, not a desktop font estimate.
  canvas.alpha_composite(paint,(cursor,max(0,offset)));cursor+=advance
 if scale!=1:canvas=canvas.resize((round(canvas.width*scale),round(canvas.height*scale)),Image.Resampling.NEAREST)
 im.alpha_composite(canvas,(round(x-canvas.width/2 if center else x),round(y)))
def block(im,label,x,y,width,color='text',max_lines=None):
 lines=wrap(label,width)
 if max_lines is not None:check(len(lines)<=max_lines,'Text exceeds approved block: '+label)
 for i,line in enumerate(lines):text(im,line,x,y+i*10,color)
 return len(lines)*10
def panel(im,rect,fill='panel',selected=False):
 x,y,w,h=map(round,rect);d=ImageDraw.Draw(im);d.rectangle((x,y,x+w-1,y+h-1),fill=C[fill],outline=C['gold' if selected else 'bronze'])
 for px,py in [(x+2,y+2),(x+w-4,y+2),(x+2,y+h-4),(x+w-4,y+h-4)]:d.rectangle((px,py,px+1,py+1),fill=C['bronze'])
def button(im,rect,label,selected=False,disabled=False):
 panel(im,rect,'panel' if disabled else 'button',selected);x,y,w,h=rect
 check(text_width(label)<=w-4,'Button label: '+label)
 text(im,label,x+w/2,y+(h-8)//2,'disabled' if disabled else 'text',True)
def icon(im,id,x,y,size,locked=False):
 src=Image.open(ASSETS/f'textures/gui/classes/{id}.png').convert('RGBA').resize((size,size),Image.Resampling.NEAREST)
 if locked:
  channels=src.split();rgb=src.convert('RGB').point(lambda p:int(p*.58));rgb.putalpha(channels[3]);src=rgb
 im.alpha_composite(src,(round(x),round(y)))
def rect_valid(rect,W,H,name):
 x,y,w,h=rect;check(x>=0 and y>=0 and w>0 and h>0 and x+w<=W and y+h<=H,'Bounds '+name)
def intersect(a,b):return a[0]<b[0]+b[2] and b[0]<a[0]+a[2] and a[1]<b[1]+b[3] and b[1]<a[1]+a[3]
def geometry(W,H):
 viewport=(8,64,W-16,H-116);footer=(8,H-47,W-16,39)
 card_width=min(160,round(viewport[2]*.28));card=(W-14-card_width,70,card_width,viewport[3]-12)
 control_h=min(165,H-80);control=(round((W-320)/2),64+round((H-80-control_h)/2),320,control_h)
 # Modal height is measured from actual Russian glyph advances/wrapped paragraphs.
 mw=min(290,W-32);title_h=10;prompt_h=len(wrap('Вы действительно хотите выбрать путь «Гридень»?',mw-24))*10
 note_h=len(wrap('В режиме выживания обычная смена направления недоступна.',mw-24))*10
 mh=12+title_h+10+prompt_h+6+note_h+12+20+10;modal=((W-mw)//2,(H-mh)//2,mw,mh)
 mx,my,_,_=modal;by=my+mh-30;bw=(mw-32)//2
 return {'viewport':viewport,'footer':footer,'card':card,'controls':control,'modal':modal,'cancel':(mx+12,by,bw,20),'confirm':(mx+20+bw,by,bw,20),'nav':(16,H-78,142,18),'close':(W-28,12,18,18)}
SOURCE=(ROOT/'src/main/java/org/slavicmyths/rpg/classes/ClassDefinitions.java').read_text(encoding='utf-8')
LUNGE_ARRAYS=[list(map(float,values.split(','))) for _,values in re.findall(r'(ints|nums)\(([^)]*)\)',next(line for line in SOURCE.splitlines() if 'add(s,"lunge"' in line))]
skills=[]
for m in re.finditer(r'add\(s,"([^"]+)","([^"]+)",Kind\.([A-Z]+),(\d+),"([^"]*)",(\d+),"([^"]*)"',SOURCE):
 id,base,kind,level,parent,prank,branch=m.groups();skills.append(dict(id=id,base=base,kind=kind,level=int(level),parent=parent,prerequisite_rank=int(prank),branch=branch,max_rank=3))
for m in re.finditer(r'evolution\(s,"([^"]+)","([^"]+)","([^"]+)","([^"]+)",(\d+)\)',SOURCE):
 id,base,parent,branch,level=m.groups();skills.append(dict(id=id,base=base,kind='EVOLUTION',level=int(level),parent=parent,prerequisite_rank=3,branch=branch,max_rank=1))
check(len(skills)==34,'Actual skills')
graphs={}
for base in ('druzhinnik','vedun','razboinik'):
 children=defaultdict(list)
 for s in skills:
  if s['base']==base:children[s['parent'] or base].append(s['id'])
 nodes={};leaf=[0]
 def place(id,depth,parent):
  if children[id]:
   positions=[place(c,depth+1,id) for c in children[id]];x=(positions[0]+positions[-1])/2
  else:x=leaf[0]*72;leaf[0]+=1
  nodes[id]=(x,depth*65,parent);return x
 place(base,0,'');graphs[base]=nodes
 for id,(x,y,parent) in nodes.items():
  for other,(ox,oy,_) in nodes.items():
   if other!=id:check(abs(x-ox)>=34 or abs(y-oy)>=46,'Node/rank overlap')
 with (OUT/f'{base}-graph.csv').open('w',encoding='utf-8-sig',newline='') as f:
  writer=csv.writer(f);writer.writerow(['id','name','rank_fixture','kind','prerequisite','prerequisite_rank','branch','x','y'])
  for s in skills:
   if s['base']==base:
    x,y,parent=nodes[s['id']];writer.writerow([s['id'],LANG['classskill.slavicmyths.'+s['id']],f"0/{s['max_rank']}",s['kind'],s['parent'],s['prerequisite_rank'],s['branch'],x,y])
def header(im,W,H,tab=0,level=1):
 panel(im,(8,8,W-16,26),'bg');text(im,f'ДРУЖИННИК · УР. {level}',16,10,scale=1.25);text(im,f'Опыт 0/{100 if level==1 else 100+50*(level-1)} · Очки навыков: {1 if level==3 else 0}',16,24,'muted')
 button(im,(W-28,12,18,18),'×')
 for i,name in enumerate(('Дерево','Развитие','Управление')):button(im,(8+i*100,38,95,20),name,tab==i)
def footer(im,W,H,g,filled=False):
 panel(im,g['footer'],'panel');y=H-47;text(im,'АКТИВНЫЕ',16,y,'muted')
 for i in range(3):
  x=16+i*27;panel(im,(x,y+8,22,22));
  if filled and i==0:icon(im,'lunge',x+2,y+10,18)
  text(im,('I','II','III')[i],x+11,y+31,'muted',True)
 text(im,'ПАССИВНЫЙ',115,y,'muted');panel(im,(115,y+8,22,22))
 if filled:icon(im,'temper',117,y+10,18)
def tree(im,W,H,g,selected=False):
 panel(im,g['viewport'],'tree');vx,vy,vw,vh=g['viewport'];layer=Image.new('RGBA',(vw,vh));d=ImageDraw.Draw(layer)
 nodes=graphs['druzhinnik'];rx=nodes['druzhinnik'][0];cx=vw/2-rx-17;cy=23
 if vh<170:cx+=max(0,158-(cx+nodes['lunge'][0]))
 pos={id:(round(cx+x),round(cy+y)) for id,(x,y,_) in nodes.items()}
 for id,(x,y,parent) in nodes.items():
  if not parent:continue
  if pos[id][1]+46>vh-2:continue
  fx,fy=pos[parent];tx,ty=pos[id];fy+=40 if parent=='druzhinnik' else 34;fx+=17 if parent=='druzhinnik' else 31;tx+=17;mid=(fy+ty)//2
  color=C['bronze'];d.line([(fx,fy),(fx,mid),(tx,mid),(tx,ty)],fill=color,width=2)
 for id,(x,y) in pos.items():
  root=id=='druzhinnik';
  if y+46>vh-2:continue
  size=40 if root else 34;shift=-3 if root else 0;learned=selected and id in ('lunge','temper')
  panel(layer,(x+shift,y+shift,size,size),'panel',learned or root);icon(layer,id,x+3,y+3,28,not root and not learned)
  if id=='lunge' and selected:
   d.rectangle((x-2,y-2,x+35,y+35),outline='#FFF0CE',width=1)
  if not root:text(layer,f"{1 if learned else 0}/{next(s['max_rank'] for s in skills if s['id']==id)}",x+17,y+37,'muted',True)
 im.alpha_composite(layer,(vx,vy));
 if any(y+46>vh-2 for x,y in pos.values()):text(im,'Продолжение дерева ниже',vx+vw/2,vy+vh-14,'muted',True)
 x,y,w,h=g['nav'];button(im,(x,y,18,18),'−');button(im,(x+21,y,43,18),'100%');button(im,(x+67,y,18,18),'+');button(im,(x+88,y,54,18),'Центр')
 # Ensure initial root and first prerequisite branches have visible frames and rank labels.
 for id,(x,y,parent) in nodes.items():
  if id=='druzhinnik' or parent=='druzhinnik':
   px,py=pos[id];check(px>=4 and px+40<=vw-4 and py>=4 and py+46<=vh-2,'Initial branch visible'
   );check(not intersect((px+vx,py+vy,34,46),g['nav']),'Navigation obscures initial branch')
 if selected:
  x,y,w,h=g['card'];panel(im,g['card'],'panel');button(im,(x+w-18,y+4,12,12),'×');icon(im,'lunge',x+8,y+8,32);text(im,'Выпад',x+46,y+22,scale=1.2)
  body_y=y+48;bottom=y+h-34;body=Image.new('RGBA',(w-16,max(1,bottom-body_y)))
  lines=['Активный · Ранг 1/3','',LANG['classskill.slavicmyths.lunge.description'],'','Текущий ранг',f'Урон: +{LUNGE_ARRAYS[3][0]*100:g}%',f'Дальность: {LUNGE_ARRAYS[2][0]:g} блока',f'Перезарядка: {LUNGE_ARRAYS[0][0]/20:g} с','','Следующий ранг',f'Урон: +{LUNGE_ARRAYS[3][1]*100:g}%',f'Дальность: {LUNGE_ARRAYS[2][1]:g} блока',f'Перезарядка: {LUNGE_ARRAYS[0][1]/20:g} с','','Требования','Уровень 3 · 1 очко']
  yy=0
  for label in lines:
   for line in wrap(label,w-21):text(body,line,0,yy);yy+=10
  im.alpha_composite(body,(x+8,body_y));ImageDraw.Draw(im).rectangle((x+w-5,body_y,x+w-4,bottom),fill=C['bronze']);ImageDraw.Draw(im).rectangle((x+w-5,body_y,x+w-4,body_y+14),fill=C['gold'])
  button(im,(x+8,y+h-28,w-16,20),'Улучшить')
 return {'camera':[cx,cy],'zoom':1,'root':[pos['druzhinnik'][0]+vx,pos['druzhinnik'][1]+vy]}
def branches(im,W,H,g,modal=True):
 for i,(name,id,skill) in enumerate([('ВИТЯЗЬ','lunge','Выпад'),('ГРИДЕНЬ','shield_ram','Таран щитом')]):
  w=170;h=min(140,H-82);x=W//2-176+i*182;y=64+(H-72-h)//2;panel(im,(x,y,w,h),'panel',i==1);icon(im,id,x+(w-38)//2,y+7,38)
  text(im,name,x+w/2,y+50,center=True,scale=1.25);text(im,skill,x+w/2,y+68,'muted',True);text(im,'Усиление ×1.1',x+w/2,y+81,'muted',True);text(im,'Доступно · уровень 10',x+w/2,y+100,'learned',True);button(im,(x+8,y+h-26,w-16,20),'Выбрать',i==1)
 if not modal:return
 # Separate dim layer: only two modal buttons are drawn above it.
 im.alpha_composite(Image.new('RGBA',im.size,(0,0,0,180)));x,y,w,h=g['modal'];panel(im,g['modal'],'panel');text(im,'ПОДТВЕРЖДЕНИЕ ВЫБОРА',x+12,y+12);yy=y+32
 yy+=block(im,'Вы действительно хотите выбрать путь «Гридень»?',x+12,yy,w-24);yy+=6
 yy+=block(im,'В режиме выживания обычная смена направления недоступна.',x+12,yy,w-24,'muted');check(yy+12<=g['cancel'][1],'Measured modal gap')
 button(im,g['cancel'],'Отмена');button(im,g['confirm'],'Подтвердить',True);check(not intersect(g['cancel'],g['confirm']),'Modal buttons overlap')
def controls(im,W,H,g):
 x,y,w,h=g['controls'];panel(im,g['controls'],'panel');text(im,'УПРАВЛЕНИЕ НАВЫКАМИ',x+10,y+10);text(im,'Нажмите на клавишу для переназначения.',x+10,y+25,'muted')
 # 32px rows and 4px gaps; if short, the rows are inside a scissored scroll region.
 layer=Image.new('RGBA',(w-20,h-46));lx=0
 for i,(roman,key) in enumerate(zip(('I','II','III'),KEY_LABELS)):
  yy=i*36;panel(layer,(lx,yy,w-20,32),'tree');text(layer,'Активный навык '+roman,7,yy+5);button(layer,(w-20-82,yy+6,58,20),key,i==0);button(layer,(w-20-20,yy+6,20,20),'');rd=ImageDraw.Draw(layer);rx=w-20-10;ry=yy+16;rd.line([(rx+4,ry+1),(rx+4,ry-3),(rx-3,ry-3),(rx-3,ry+4),(rx+3,ry+4)],fill=C['muted'],width=1);rd.polygon([(rx-5,ry-3),(rx-1,ry-3),(rx-3,ry)],fill=C['muted'])
  check(text_width('Активный навык '+roman)<=w-20-92,'Control label collision')
  if CONFLICTS[i]:text(layer,'! Конфликт: '+str(len(CONFLICTS[i]))+' действия',7,yy+19,'error')
 im.alpha_composite(layer,(x+10,y+41))
def hud(im,W,H,g):
 # Synthetic voxel scene, intentionally not represented as a game screenshot.
 d=ImageDraw.Draw(im);d.rectangle((0,0,W,H//2),fill='#849CA8');d.rectangle((0,H//2,W,H),fill='#617448')
 for x,y,w,h in [(W//4,H//2-45,70,45),(W//2,H//2-75,90,75),(W*3//4,H//2-35,48,35)]:d.rectangle((x,y,x+w,y+h),fill='#40533A')
 text(im,'МАКЕТ HUD · НЕ ИГРОВОЙ СКРИНШОТ',12,12)
 bx=(W-182)//2;by=H-22;d.rectangle((bx,by,bx+181,by+21),fill='#242424',outline='#B0ABA0',width=1)
 for i in range(9):d.rectangle((bx+3+i*20,by+3,bx+20+i*20,by+19),outline='#827E74');
 d.rectangle((bx,by-22,bx+80,by-19),fill='#AB5148');d.rectangle((bx+102,by-22,bx+182,by-19),fill='#A7834E')
 x=W-8-60;y=H-8-29
 for i,id in enumerate(('lunge','flurry','trip')):
  sx=x+i*21;panel(im,(sx,y,18,18),'tree');icon(im,id,sx+2,y+2,14)
  if i==1:d.rectangle((sx+2,y+8,sx+15,y+15),fill='#191716');text(im,'3',sx+9,y+5,center=True)
  text(im,KEY_LABELS[i],sx+9,y+20,center=True)
 icon(im,'temper',W-22,y-16,14)
 hudrect=(x,y,60,29);rect_valid(hudrect,W,H,'HUD');check(not intersect(hudrect,(bx,by-24,182,46)),'HUD/vanilla overlap')
 return hudrect
def actual_scale(pw,ph,requested):
 scale=1
 while scale!=requested and pw//(scale+1)>=320 and ph//(scale+1)>=240:scale+=1
 return scale
def render(pw,ph,requested,kind,primary=False):
 scale=actual_scale(pw,ph,requested);W=math.ceil(pw/scale);H=math.ceil(ph/scale);g=geometry(W,H)
 for name,rect in g.items():rect_valid(rect,W,H,name)
 check(not intersect(g['viewport'],g['footer']),'Viewport/footer overlap');check(g['card'][2]<=160,'Card maximum');check(g['card'][0]>=g['viewport'][0]+g['viewport'][2]*.7,'Overlay consumes too much')
 im=Image.new('RGBA',(W,H),C['bg']);state={}
 if kind!='hud':header(im,W,H,{'tree':0,'skill':0,'branches':1,'controls':2}[kind],3 if kind=='skill' else 10 if kind=='branches' else 1)
 if kind in ('tree','skill'):state=tree(im,W,H,g,kind=='skill');footer(im,W,H,g,kind=='skill')
 if kind=='branches':
  if primary:
   base=im.copy();branches(base,W,H,g,False);base.resize((W*scale,H*scale),Image.Resampling.NEAREST).crop((0,0,pw,ph)).convert('RGB').save(OUT/'preview-branches-base.png')
  branches(im,W,H,g)
 if kind=='controls':controls(im,W,H,g)
 if kind=='hud':state={'hud':hud(im,W,H,g)}
 filename=f'{pw}x{ph}-gui{requested}-{kind}.png';im.resize((W*scale,H*scale),Image.Resampling.NEAREST).crop((0,0,pw,ph)).convert('RGB').save(OUT/filename)
 record={'file':filename,'physical':[pw,ph],'requested_gui_scale':requested,'actual_gui_scale':scale,'scaled':[W,H],'kind':kind,'rectangles':g,'state':state};manifest.append(record)
 if primary:im.resize((W*scale,H*scale),Image.Resampling.NEAREST).crop((0,0,pw,ph)).convert('RGB').save(OUT/f'preview-{kind}.png')
for resolution in [(1649,928,2),(1649,928,3),(1649,928,4),(1920,1080,2),(1920,1080,3),(1920,1080,4),(2560,1440,3),(2560,1440,4)]:
 for kind in ('tree','skill','branches','controls','hud'):render(*resolution,kind,primary=resolution==(1649,928,3))
check(not missing,'Missing Minecraft bitmap glyphs '+str(missing))
(OUT/'layout-manifest.json').write_text(json.dumps({'approval_stage':True,'runtime_screenshots':False,'font':'Minecraft 1.21.1 bitmap provider glyphs/advances, default uniform=false; rendering is an offline approximation','checks':checks,'key_bindings_read_from_options':dict(zip(OWN,BINDINGS)),'raw_key_collisions':dict(zip(OWN,CONFLICTS)),'cases':manifest},ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(f'Generated 5 primary PNGs + {len(manifest)} resolution/state snapshots; {checks} geometric/font checks. No production changes.')

# Review overview; individual PNGs preserve the full physical resolution.
sheet=Image.new('RGB',(1650,1515),C['bg']);draw=ImageDraw.Draw(sheet)
for i,(kind,label) in enumerate([('tree','1. TREE'),('skill','2. SKILL CARD'),('branches','3. CONFIRMATION'),('controls','4. KEY MAPPINGS'),('hud','5. HUD')]):
 image=Image.open(OUT/f'preview-{kind}.png').resize((825,464),Image.Resampling.NEAREST)
 x=i%2*825;y=i//2*505;draw.text((x+12,y+10),label,fill=C['text']);sheet.paste(image,(x,y+30))
sheet.save(OUT/'overview.png')
