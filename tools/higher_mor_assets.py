from pathlib import Path
from PIL import Image,ImageDraw
import json,random
R=Path(__file__).resolve().parents[1];out=R/'src/main/resources/assets/slavicmyths/textures/entity/mor'
def atlas(name,size,tile,eye):
 rng=random.Random(182 if name=='likho' else 183);im=Image.new('RGBA',(size,size));p=im.load()
 for y in range(size):
  for x in range(size):
   slot=x//tile+(y//tile)*(size//tile);base=([(96,83,78),(78,67,62),(112,98,90),(35,28,30),(85,66,48),(48,40,38),(18,14,17),(61,36,35)] if name=='likho' else [(38,34,35),(94,70,45),(112,103,92),(30,27,29),(62,48,37),(48,40,38),(18,14,17),(61,36,35)])[slot%8];n=rng.choice([-8,-3,0,0,4,9]);p[x,y]=tuple(max(0,c+n) for c in base)+(255,)
 # Each material island contains charred irregular grain and angular crimson fissures.
 d=ImageDraw.Draw(im)
 for by in range(0,size,tile):
  for bx in range(0,size,tile):
   for k in range(6):
    x=bx+rng.randrange(4,tile-4);y=by+rng.randrange(3,tile-10);length=rng.randrange(5,18);d.line([(x,y),(x+2,y+4),(x-1,y+8),(x+1,y+length)],fill=(114,20,29,255),width=1);d.point((x+2,y+4),fill=(192,44,42,255))
 # Dense angular seams fall inside small Minecraft UV islands as well as larger plates.
 for by in range(0,size,8):
  for bx in range(0,size,8):
   if (bx//8+by//8)%3==0:d.line([(bx+4,by+1),(bx+3,by+3),(bx+5,by+5),(bx+4,by+7)],fill=(112,24,30,255),width=1)
 # Existing emissive eyes use full material tiles. Preserve one vs two geometry eyes.
 for slot in eye:
  bx=(slot%(size//tile))*tile;by=(slot//(size//tile))*tile
  d.rectangle((bx,by,bx+tile-1,by+tile-1),fill=(139,25,32,255))
  for y in range(by,by+tile):
   for x in range(bx,bx+tile):
    if (x+y)%5==0:p[x,y]=(237,88,68,255)
 if name=='likho':d.rectangle((7*64,64,8*64-1,128-1),fill=(247,195,173,255))
 im.save(out/(name+'.png'));forming=im.copy();forming.putdata([(int(r*.8),int(g*.85),int(b*.85),a) for r,g,b,a in im.getdata()]);forming.save(out/(name+'_forming.png'))
atlas('likho',512,64,[7,8,9]);atlas('ovinnik',256,64,[3,6])
for name in ['likho','ovinnik']:
 im=Image.new('RGBA',(16,16),(22,18,23,185));d=ImageDraw.Draw(im);rng=random.Random(184 if name=='likho' else 185)
 for i in range(75):
  x,y=rng.randrange(16),rng.randrange(16);v=rng.randrange(20,46);d.point((x,y),fill=(v,v-5,v-3,205))
 d.line([(1,0),(3,4),(7,5),(6,9),(10,12),(11,15)],fill=(139,28,34,220),width=1);d.line([(6,9),(2,11),(0,10)],fill=(102,17,25,210));d.line([(10,12),(15,8)],fill=(148,42,30,220))
 # Dry grass/charred straw lie on the surface, without world vegetation changes.
 for x in [2,12]:d.line([(x,14),(x+1,9),(x+2,11)],fill=(74,54,44,210),width=1)
 im.save(out/('ground_'+name+'.png'))
for language in ['ru_ru','en_us']:
 p=R/'src/main/resources/assets/slavicmyths/lang'/ (language+'.json');data=json.loads(p.read_text(encoding='utf-8-sig'));data.update({'entity.slavicmyths.mor_likho':'Высшее моровое Лихо Одноглазое' if language=='ru_ru' else 'Greater Blighted Likho','entity.slavicmyths.mor_ovinnik':'Высший моровой Овинник' if language=='ru_ru' else 'Greater Blighted Ovinnik','entity.slavicmyths.mor_zone':'Поражённая земля' if language=='ru_ru' else 'Blighted Ground'});p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

for language in ['ru_ru','en_us']:
 p=R/'src/main/resources/assets/slavicmyths/lang'/ (language+'.json');data=json.loads(p.read_text(encoding='utf-8-sig'));data['item.slavicmyths.mor_flask.tooltip']='Для испытаний: превращает Лешего, Полудницу, Кикимору и Огненного змея в моровые формы, Лихо и Овинника — в высшие моровые.' if language=='ru_ru' else 'For testing: blights Leshy, Poludnitsa, Kikimora and Fire Serpent; grants greater blighted forms to Likho and Ovinnik.';p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

im=Image.new('RGBA',(32,32),(0,0,0,0));pix=im.load()
for y in range(32):
 for x in range(32):
  r=((x-15.5)**2+(y-15.5)**2)**.5/16
  alpha=max(0,int((1-r)*95));pix[x,y]=(65+(x+y)%7,17,25,alpha)
im.save(out/'cloud.png')
