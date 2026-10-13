"""Review-only native pixel UI layouts and offline weapon geometry proposals.
Does not modify production sources/resources, build a mod, or launch Minecraft.
"""
from pathlib import Path
import json,zipfile,math,io
from PIL import Image,ImageDraw
import numpy as np

R=Path(__file__).resolve().parents[1];O=R/'docs/design-review/1.8.0-proposal';O.mkdir(parents=True,exist_ok=True)
glyphs={}
for path in Path('C:/Users/pavel/.gradle/caches/neoformruntime/intermediate_results').glob('*resourcesOutput.jar'):
 with zipfile.ZipFile(path) as z:
  if 'assets/minecraft/font/include/default.json' not in z.namelist():continue
  providers=json.loads(z.read('assets/minecraft/font/include/default.json'))['providers']
  for provider in providers:
   if provider.get('type')!='bitmap':continue
   name='assets/minecraft/textures/'+provider['file'].split(':')[1]
   if name not in z.namelist():continue
   sheet=Image.open(io.BytesIO(z.read(name))).convert('RGBA');rows=provider['chars'];cw=sheet.width//len(rows[0]);ch=sheet.height//len(rows);h=provider.get('height',8)
   for iy,row in enumerate(rows):
    for ix,char in enumerate(row):
     if char=='\x00' or char in glyphs:continue
     a=sheet.crop((ix*cw,iy*ch,(ix+1)*cw,(iy+1)*ch)).getchannel('A');bounds=a.getbbox()
     if bounds:
      width=max(1,round(bounds[2]*h/ch));glyphs[char]=(a.resize((round(cw*h/ch),h),Image.Resampling.NEAREST),width+1,7-provider.get('ascent',7))
  break
def measure(s):return sum(4 if c==' ' else glyphs.get(c,(None,6,0))[1] for c in s)
def text(im,s,x,y,color='#342B24'):
 for c in s:
  if c==' ':x+=4;continue
  mask,advance,offset=glyphs.get(c,(None,6,0))
  if mask is not None:im.paste(Image.new('RGB',mask.size,color),(x,y+offset),mask)
  x+=advance
def fit(s,w):
 while measure(s)>w:s=s[:-2]+'…'
 return s
def wrap(s,w):
 out=[];row=''
 for word in s.split():
  if row and measure(row+' '+word)>w:out.append(row);row=word
  else:row+=((' ' if row else '')+word)
 if row:out.append(row)
 return out
def paragraph(im,s,x,y,w,color='#342B24'):
 lines=wrap(s,w)
 for n,line in enumerate(lines):text(im,line,x,y+n*11,color)
 return y+len(lines)*11
def box(im,rect,fill,edge='#76674E'):
 d=ImageDraw.Draw(im);d.rectangle(rect,fill=fill,outline=edge)
def button(im,label,x,y,w,h=17,selected=False,dark=False):
 box(im,(x,y,x+w,y+h),'#6A523A' if selected else '#3D3831' if dark else '#C9BD9C','#BC9A5B' if selected else '#8A7B60');text(im,fit(label,w-10),x+5,y+(h-8)//2,'#FFF1CC' if selected or dark else '#332B22')
def save(im,name):im.resize((im.width*3,im.height*3),Image.Resampling.NEAREST).save(O/(name+'.png'))
W,H=426,240
def base(title,dark=False):
 im=Image.new('RGB',(W,H),'#262925');box(im,(6,6,W-7,H-7),'#282722' if dark else '#E3D7B6','#887858');text(im,title,15,14,'#F2E7CA' if dark else '#332B22');button(im,'×',W-29,9,17,16,dark=dark);return im
def book(focus=False):
 im=base('Книга сказаний');button(im,'←',15,28,19);button(im,'Все записи ▾',38,28,117);ImageDraw.Draw(im).polygon([(143,34),(149,34),(146,37)],fill='#665B48');box(im,(160,28,395,45),'#F6ECCF');text(im,'Поиск…',166,33,'#756C57')
 if not focus:
  x=160;d=ImageDraw.Draw(im);d.line((153,51,153,214),fill='#B0A383')
  titles=['Банник','Болотник','Домовой','Илья Муромец','Алёша Попович','Добрыня Никитич','Ским-зверь','Маринка','Картаус Рыжий Ус','Калин-царь','Кабан','Медвежонок','Леший']
  for n,t in enumerate(titles):
   y=51+n*12
   if n==0:box(im,(13,y-1,147,y+10),'#B9AC83',edge='#B9AC83')
   text(im,fit(t,122),18,y+1)
  button(im,'↑',15,214,18,15);text(im,'1–13 / 217',40,218,'#71664F');button(im,'↓',126,214,18,15)
  text(im,'Банник',x,55,'#742F29');button(im,'Скрыть список',x,211,98)
 else:
  x=17;text(im,'Банник',x,55,'#742F29');button(im,'Показать список',17,211,108)
 text(im,'УСЛЫШАНО',x,74,'#6D7650')
 paragraph(im,'Сведения пока неизвестны. Подробности откроются после настоящей встречи.',x,97,395-x)
 button(im,'Иллюстрация',x,153,94)
 text(im,'Страница 1 / 1',315,217,'#71664F')
 save(im,'book-focus' if focus else 'book-catalog')
def icon(im,name,x,y,size=20):
 p=R/'src/main/resources/assets/slavicmyths/textures/gui/classes'/f'{name}.png'
 if p.exists():a=Image.open(p).convert('RGBA').resize((size,size),Image.Resampling.NEAREST);im.paste(a,(x,y),a)
def tree(assign=False):
 im=base('Дружинник · ур. 8   Очки: 2',True);button(im,'Навыки',15,29,72,selected=True,dark=True);button(im,'Направления',92,29,95,dark=True);button(im,'Клавиши',192,29,75,dark=True)
 d=ImageDraw.Draw(im);nodes={'lunge':(190,56),'flurry':(65,96),'trip':(185,96),'temper':(325,96),'aerial_strike':(30,137),'shield_ram':(160,137),'steadfast':(315,137),'riposte':(150,178),'second_wind':(280,178)}
 for a,b in [('lunge','flurry'),('lunge','trip'),('flurry','aerial_strike'),('trip','shield_ram'),('temper','steadfast'),('shield_ram','riposte'),('temper','second_wind')]:
  ax,ay=nodes[a];bx,by=nodes[b];mid=(ay+36+by)//2;d.line([(ax+10,ay+36),(ax+10,mid),(bx+10,mid),(bx+10,by)],fill='#A38C57',width=1)
 labels={'lunge':'Выпад','flurry':'Сеча','trip':'Подсечка','aerial_strike':'Удар с воздуха','shield_ram':'Таран щитом','temper':'Закалка','steadfast':'Стойкость','riposte':'Ответный удар','second_wind':'Второе дыхание'}
 for n,(x,y) in nodes.items():
  box(im,(x-2,y-2,x+22,y+22),'#504839','#C9AA66' if n=='lunge' else '#81714D');icon(im,n,x,y);text(im,labels[n],max(15,x+10-measure(labels[n])//2),y+25,'#E0D4B7')
 for i,(key,skill) in enumerate([('Num1','Сеча'),('H','Подсечка'),('J','Пусто')]):button(im,key+' · '+skill,15+i*134,216,128,dark=True)
 if not assign:
  box(im,(235,58,399,85),'#161915','#9E8A5B');text(im,'Выпад · 1/3',242,64,'#F5E9CE');text(im,'Нажмите для выбора',242,75,'#C5B99C')
 else:
  box(im,(112,58,410,205),'#181B17','#B9A067');text(im,'Выпад · 1/3',121,67,'#F5E9CE');button(im,'×',386,62,16,dark=True)
  text(im,'Рывок вперёд с усиленным ударом.',121,84,'#C9BEA7');button(im,'Улучшить · 1 очко',121,99,174,dark=True)
  text(im,'Закрепить на клавишу:',121,125,'#F5E9CE')
  for i,(key,skill) in enumerate([('Num1','Сеча'),('H','Подсечка'),('J','Пусто')]):button(im,key+' — '+skill,121+i*93,140,88,selected=i==0,dark=True)
  text(im,'Num1: заменить «Сеча» на «Выпад»',121,167,'#E6CE8E');button(im,'Назначить',121,183,104,selected=True,dark=True);button(im,'Отмена',231,183,75,dark=True)
 save(im,'tree-assign' if assign else 'tree-canvas')

def cube(center,size,color):
 x,y,z=center;w,h,l=[v/2 for v in size];p=[(x-w,y-h,z-l),(x+w,y-h,z-l),(x+w,y+h,z-l),(x-w,y+h,z-l),(x-w,y-h,z+l),(x+w,y-h,z+l),(x+w,y+h,z+l),(x-w,y+h,z+l)]
 return {'color':color,'faces':[[list(p[i])+[0,0] for i in face] for face in [(0,1,2,3),(5,4,7,6),(4,0,3,7),(1,5,6,2),(4,5,1,0),(3,2,6,7)]]}
def candidate(who):
 path=R/('work/bogatyr/meshes.json' if who=='ilya' else 'work/skim/meshes.json');old=json.loads(path.read_text())[who+'_IDLE']
 parts=[c for c in old if c['part'].endswith('/right_hand')];assert parts
 pts=np.array([v[:3] for c in parts for face in c['faces'] for v in face]);x,y,z=(pts.min(0)+pts.max(0))/2
 weapon=[]
 if who=='ilya':
  weapon=[cube([x,y+3,z],[1.4,10,1.4],'#785238'),cube([x,y+8.4,z],[4.8,3.4,4.8],'#8D9693'),cube([x,y+6.2,z],[3.2,1.2,3.2],'#A9B0AB'),cube([x,y+10.5,z],[3.2,.8,3.2],'#747D7B'),cube([x,y-1.3,z],[1.7,1,1.7],'#A29469')]
 else:
  weapon=[cube([x,y,z],[1.4,3.5,1.4],'#785238'),cube([x,y+2.15,z],[3.6,.8,1.6],'#B69B59'),cube([x,y-2.1,z],[1.7,.7,1.7],'#B69B59')]
  for i in range(7):weapon.append(cube([x+.025*i*i,y+3.1+i*1.5,z],[1.4 if i<5 else .9 if i==5 else .35,1.55,.6],'#B7C0BA'))
 body=[c for c in old if not c['part'].endswith('/mace') and not c['part'].endswith('/saber')]
 return body+weapon,weapon

def render(mesh,who,angle,close=False):
 width,height=320,420;canvas=np.full((height,width,4),[37,42,39,255],np.uint8);depth=np.full((height,width),1e9)
 tex=np.asarray(Image.open(R/'src/main/resources/assets/slavicmyths/textures/entity'/('ilya_muromets.png' if who=='ilya' else 'alyosha_popovich.png')).convert('RGBA'))
 a=math.radians(angle);c,s=math.cos(a),math.sin(a);verts=np.array([v[:3] for q in mesh for f in q['faces'] for v in f]);xx=verts[:,0]*c-verts[:,2]*s;yy=24-verts[:,1];scale=min((width-30)/(np.ptp(xx)+1e-5),(height-35)/(np.ptp(yy)+1e-5));ox=width/2-(xx.max()+xx.min())/2*scale;oy=height-17+yy.min()*scale
 for cube_ in mesh:
  for face in cube_['faces']:
   v=np.array(face);px=v[:,0]*c-v[:,2]*s;zz=v[:,2]*c+v[:,0]*s;q=np.column_stack([ox+px*scale,oy-(24-v[:,1])*scale,zz,v[:,3],v[:,4]])
   normal=np.cross(v[1,:3]-v[0,:3],v[2,:3]-v[0,:3]);normal/=np.linalg.norm(normal)+1e-9;shade=.72+.25*max(0,normal.dot(np.array([-.45,-.75,-.45])))
   for ids in [(0,1,2),(0,2,3)]:
    t=q[list(ids)];xs,ys=t[:,0],t[:,1];x0=max(0,int(xs.min()));x1=min(width-1,int(math.ceil(xs.max())));y0=max(0,int(ys.min()));y1=min(height-1,int(math.ceil(ys.max())))
    if x1<x0 or y1<y0:continue
    xx,yy=np.meshgrid(np.arange(x0,x1+1)+.5,np.arange(y0,y1+1)+.5);den=(ys[1]-ys[2])*(xs[0]-xs[2])+(xs[2]-xs[1])*(ys[0]-ys[2])
    if abs(den)<1e-7:continue
    w0=((ys[1]-ys[2])*(xx-xs[2])+(xs[2]-xs[1])*(yy-ys[2]))/den;w1=((ys[2]-ys[0])*(xx-xs[2])+(xs[0]-xs[2])*(yy-ys[2]))/den;w2=1-w0-w1;zz=w0*t[0,2]+w1*t[1,2]+w2*t[2,2];mask=(w0>=0)&(w1>=0)&(w2>=0)&(zz<depth[y0:y1+1,x0:x1+1])
    if 'color' in cube_:
     rgba=np.empty((y1-y0+1,x1-x0+1,4),np.uint8);rgb=Image.new('RGB',(1,1),cube_['color']).getpixel((0,0));rgba[:]=[*rgb,255]
    else:
     u=w0*t[0,3]+w1*t[1,3]+w2*t[2,3];vv=w0*t[0,4]+w1*t[1,4]+w2*t[2,4];rgba=tex[np.clip((vv*tex.shape[0]).astype(int),0,tex.shape[0]-1),np.clip((u*tex.shape[1]).astype(int),0,tex.shape[1]-1)].copy()
    rgba[:,:,:3]=(rgba[:,:,:3]*shade).astype(np.uint8);mask&=rgba[:,:,3]>0;canvas[y0:y1+1,x0:x1+1][mask]=rgba[mask];depth[y0:y1+1,x0:x1+1][mask]=zz[mask]
 return Image.fromarray(canvas)

book();book(True);tree();tree(True)
sheet=Image.new('RGB',(1280,500),'#252A27')
for index,who in enumerate(['ilya','alyosha']):
 mesh,weapon=candidate(who);sheet.paste(render(mesh,who,35),(index*640,40));sheet.paste(render(weapon,who,35,True),(index*640+320,40));text(sheet,'Илья — компактная палица' if who=='ilya' else 'Алёша — цельная сабля',index*640+30,17,'#F5E8C8');text(sheet,'Оружие отдельно',index*640+360,17,'#BDAE8C')
text(sheet,'OFFLINE: предлагаемая геометрия. Не игровой скриншот; позы ещё не проверены.',15,480,'#CCB990');sheet.save(O/'weapons.png')
(O/'weapon-proposals.json').write_text(json.dumps({who:candidate(who)[1] for who in ['ilya','alyosha']},indent=2)+'\n')
print('DESIGN_PREVIEWS_ONLY: book-catalog, book-focus, tree-canvas, tree-assign, weapons')
