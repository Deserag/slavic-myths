"""Reproducible 1.2.1 assets: native 128px UV atlases, distinct dress motifs and accessories."""
from pathlib import Path
import json
from PIL import Image, ImageDraw, ImageColor

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources'
ASSETS = ROOT / 'assets/slavicmyths'
ROLES = ['none','nitwit','farmer','fisherman','shepherd','librarian','cartographer','leatherworker','butcher','mason','fletcher','toolsmith','weaponsmith','armorer','cleric','miller','brewer','weaver','herder','hunter','cook']
PALETTES = {
 'none':('#998477','#6c5340'), 'nitwit':('#a39175','#726853'),
 'farmer':('#966944','#684d36'), 'fisherman':('#354c56','#293c48'),
 'shepherd':('#b2ad97','#807f68'), 'librarian':('#e2d6b7','#b59674'),
 'cartographer':('#776651','#575749'), 'leatherworker':('#80502f','#573d2b'),
 'butcher':('#d0bea6','#938877'), 'mason':('#a3a194','#75756c'),
 'fletcher':('#68724a','#464e36'), 'toolsmith':('#796148','#51463e'),
 'weaponsmith':('#564c43','#383b39'), 'armorer':('#5b6569','#3b4446'),
 'cleric':('#99a37a','#717b58'), 'miller':('#e3d8bb','#b4b39e'),
 'brewer':('#8c593c','#613c2d'), 'weaver':('#ded1b5','#ac7769'),
 'herder':('#82715a','#605b46'), 'hunter':('#52624a','#38453a'),
 'cook':('#e9dec4','#bbb49f')}

def save(image, path):
 path.parent.mkdir(parents=True,exist_ok=True)
 image.save(path)

def json_file(path,obj):
 path.parent.mkdir(parents=True,exist_ok=True)
 path.write_text(json.dumps(obj,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

def rectangle(draw,uv,color):
 x,y,w,h=uv;draw.rectangle((x*2,y*2,(x+w)*2-1,(y+h)*2-1),fill=color)

def linen(draw,uv,color):
 rectangle(draw,uv,color)
 x,y,w,h=uv
 shade=tuple(min(255,c+7)for c in ImageColor.getrgb(color))+(255,)
 # Individual 128px threads, with alternating seams rather than a scaled low-res placeholder.
 for xx in range(x*2+1,(x+w)*2,3):
  for yy in range(y*2+1,(y+h)*2,4): draw.point((xx,yy),fill=shade)

BODY=(16,20,28,18); SKIRT=(0,38,28,26); ARM=(44,22,16,12); FOLDED=(40,38,24,8); LEG=(0,22,16,20)

def embroidery(draw,x,y,width,color='#8c3033'):
 for start in range(x,x+width-5,8):
  draw.line([(start,y+2),(start+2,y),(start+4,y+2),(start+2,y+4),(start,y+2)],fill=color,width=1)
  draw.point((start+6,y+2),fill=color)

def climate(climate,variant):
 im=Image.new('RGBA',(128,128));d=ImageDraw.Draw(im)
 color={'cold':'#615c49','temperate':'#ddd0b0','warm':'#eee2c8'}[climate]
 for uv in [BODY,SKIRT,ARM,FOLDED]: linen(d,uv,color)
 linen(d,LEG,'#55483b' if variant==0 else '#6c4c42')
 # Boot bottoms in leg UV; the full long tunic follows vanilla jacket geometry.
 d.rectangle((0,72,31,83),fill='#382e27')
 for uv in [BODY,SKIRT]:
  x,y,w,h=uv;d.line((x*2+6,y*2,x*2+6,(y+h)*2-1),fill='#bba98b',width=1)
 if climate=='cold':
  for x,y,w,h in [SKIRT,ARM]:
   d.rectangle((x*2,(y+h)*2-5,(x+w)*2-1,(y+h)*2-1),fill='#b8ac90')
   for xx in range(x*2,(x+w)*2,3):d.line((xx,(y+h)*2-7,xx+1,(y+h)*2-3),fill='#9c9075')
 elif climate=='temperate':
  embroidery(d,40,54,16);embroidery(d,10,113,36);embroidery(d,89,61,25)
 else:
  d.line((40,52,53,52),fill='#9f7c57',width=2);embroidery(d,10,114,30,'#9b5843')
 return im

def profession(role,variant):
 im=Image.new('RGBA',(128,128));d=ImageDraw.Draw(im);a,b=PALETTES[role]
 if role in ['none','nitwit']:
  # Unemployed clothing is a short plain vest, never a professional apron.
  d.rectangle((12,88,16,99),fill=a);d.rectangle((23,88,27,99),fill=a)
  d.line((13,88,13,98),fill=b);d.line((26,88,26,98),fill=b)
  if role=='nitwit':
   d.rectangle((10,94,21,106),fill=b);d.line((11,95,20,105),fill='#c1ac8d');d.line((20,95,11,105),fill='#c1ac8d')
  return im
 # Front panel on the tunic: long apron / short vest / side seams differ by profession.
 vest=role in ['cartographer','fletcher','herder','hunter','shepherd']
 top=84 if vest else 91;bottom=101 if vest else 117
 width=10 if variant==0 else 14
 d.rectangle((12,top,12+width,bottom),fill=a)
 for x in range(13,12+width,4):d.line((x,top+1,x,bottom-1),fill=b)
 d.line((12,top,12+width,top),fill=b,width=2)
 d.rectangle((43,49,56,75),fill=a)
 d.line((46,49,46,60),fill=b,width=2);d.line((53,49,53,60),fill=b,width=2)
 d.line((44,74,55,74),fill=b,width=1)
 if role in ['leatherworker','toolsmith','weaponsmith','armorer','brewer']:
  d.line((13,88,13,111),fill='#402d23',width=2);d.line((22,88,22,111),fill='#402d23',width=2)
  d.rectangle((15,103,20,108),fill=b)
  for x in [14,21]:
   for y in [90,106]:d.rectangle((x,y,x+1,y+1),fill='#bba174')
 if role=='armorer':
  for x in range(13,24,3):
   for y in range(96,108,3):d.line((x,y,x+1,y+1),fill='#a0a9a4')
 if role=='fisherman':
  for x in range(12,27,4):d.line((x,96,x+3,114),fill='#74847a')
  for y in range(97,115,4):d.line((12,y,25,y),fill='#74847a')
 if role=='miller':
  for x in range(13,25,2):
   for y in range(109,117,3):d.point((x,y),fill='#f2e9d8')
 if role in ['librarian','weaver','cook']:
  embroidery(d,12,114,16);embroidery(d,43,50,16)
 if role=='shepherd':
  for x in range(13,24,3):
   for y in range(87,102,3):d.line((x,y,x+1,y+1),fill='#d8d4c0')
 if role=='butcher':
  d.rectangle((15,106,17,108),fill='#8c514b');d.point((21,111),fill='#8c514b')
 if role=='cleric':
  d.line((14,97,14,112),fill='#597351');d.line((20,96,20,114),fill='#597351')
 return im

def main():
 base=ASSETS/'textures/entity/villager'
 for c in ['cold','temperate','warm']:
  for v in range(2):save(climate(c,v),base/'climate'/f'{c}_{"ab"[v]}.png')
 for role in ROLES:
  for v in range(2):save(profession(role,v),base/'profession'/f'{role}_{"ab"[v]}.png')
 # Atlas used by explicitly separate caps, scarf sides, bag, scroll, tube, quiver, tools, pack and rolled rug.
 im=Image.new('RGBA',(128,128));d=ImageDraw.Draw(im)
 colors=['#745037','#bb9b56','#685044','#a2967a','#ded0b0','#9b353d','#303f60','#8b9493']
 for m,col in enumerate(colors):
  ox=m%2*64;oy=m//2*32
  d.rectangle((ox,oy,ox+63,oy+31),fill=col)
  shade=tuple(min(255,c+12)for c in ImageColor.getrgb(col))+(255,)
  if m in [0,2]:
   for x in range(ox+2,ox+62,7):d.line((x,oy+1,x,oy+30),fill=shade,width=1)
  elif m==1:
   for y in range(oy+2,oy+31,4):d.line((ox+1,y,ox+62,y),fill=shade,width=1)
  elif m==3:
   for x in range(ox+1,ox+62,4):
    for y in range(oy+1,oy+29,5):d.line((x,y,x+1,y+2),fill=shade,width=1)
  elif m==7:
   for x in range(ox+2,ox+62,10):d.line((x,oy+2,x+3,oy+29),fill=shade,width=1)
  else:
   for x in range(ox+1,ox+63,4):
    for y in range(oy+1,oy+31,3):d.point((x,y),fill=shade)
  if m in [4,5,6]:embroidery(d,ox+2,oy+10,58,'#ad7e41' if m==6 else '#8c3033')
 save(im,base/'accessory/materials.png')
 for lev in range(2,6):
  im=Image.new('RGBA',(128,128));d=ImageDraw.Draw(im)
  if lev>=2:d.line((12,113,26,113),fill='#ac9470',width=1)
  if lev>=3:d.rectangle((12,101,26,103),fill='#916942')
  if lev>=4:embroidery(d,12,114,16)
  if lev==5:embroidery(d,43,52,16,'#b69048')
  save(im,base/'level'/f'{lev}.png')
 # No unused transparent production placeholder for Novice.
 obsolete=base/'level/1.png'
 if obsolete.exists():obsolete.unlink()
 im=Image.new('RGBA',(128,128));d=ImageDraw.Draw(im)
 for x,y,w,h in [BODY,SKIRT,ARM,FOLDED]:
  for xx in range(x*2+2,(x+w)*2-1,9):
   for yy in range(y*2+2,(y+h)*2-1,11):d.line((xx,yy,xx+2,yy+1),fill='#414b32',width=1)
 d.line((13,115,24,117),fill='#394632',width=2);d.line((91,60,100,61),fill='#394632',width=2)
 save(im,base/'zombie/damage.png')
 im=climate('temperate',0);d=ImageDraw.Draw(im)
 for uv in [BODY,SKIRT,ARM,FOLDED]:linen(d,uv,'#303f60')
 d.rectangle((13,83,17,118),fill='#7d3544');d.rectangle((24,83,27,118),fill='#7d3544')
 embroidery(d,12,115,16,'#bb8d45');embroidery(d,43,51,16,'#bb8d45');embroidery(d,90,61,25,'#bb8d45')
 save(im,ASSETS/'textures/entity/merchant/travel_coat.png')
 # Small millstone: wooden stand, stepped octagonal stone, trough and a flour sack.
 def box(a,b,t):return {'from':a,'to':b,'faces':{f:{'texture':'#'+t,'uv':[0,0,16,16]} for f in ['north','south','east','west','up','down']}}
 elems=[box([1,0,1],[15,5,15],'wood'),box([2,5,2],[14,7,14],'wood'),box([2,7,4],[14,11,12],'stone'),box([4,7,2],[12,11,14],'stone'),box([3,11,5],[13,12,11],'stone'),box([5,11,3],[11,12,13],'stone'),box([7,12,7],[9,14,9],'wood'),box([5,4,0],[11,6,3],'wood'),box([11,5,11],[15,9,15],'sack')]
 textures={}
 for name,col in [('wood','#70533c'),('stone','#8f938a'),('sack','#cabc9b')]:
  tex=Image.new('RGBA',(128,128),col);td=ImageDraw.Draw(tex)
  if name=='stone':
   for r in range(8,64,12):td.ellipse((64-r,64-r,64+r,64+r),outline='#6d746c',width=2)
  else:
   for x in range(3,128,8):td.line((x,0,x,127),fill='#988064' if name=='wood' else '#ad9f7e',width=2)
  save(tex,ASSETS/'textures/block'/f'millstone_{name}.png');textures[name]='slavicmyths:block/millstone_'+name
 json_file(ASSETS/'models/block/millstone.json',{'textures':textures|{'particle':textures['stone']},'elements':elems})
 json_file(ASSETS/'models/item/millstone.json',{'parent':'slavicmyths:block/millstone'})
 json_file(ASSETS/'blockstates/millstone.json',{'variants':{'':{'model':'slavicmyths:block/millstone'}}})
 json_file(ROOT/'data/slavicmyths/loot_table/blocks/millstone.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:millstone'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
 json_file(ROOT/'data/slavicmyths/recipe/millstone.json',{'type':'minecraft:crafting_shaped','pattern':[' C ','PIP','PCP'],'key':{'P':{'tag':'minecraft:planks'},'C':{'item':'minecraft:cobblestone'},'I':{'item':'minecraft:iron_ingot'}},'result':{'id':'slavicmyths:millstone','count':1}})
 for tag in ['mineable/axe','mineable/pickaxe']:
  path=ROOT/'data/minecraft/tags/block'/f'{tag}.json';obj=json.loads(path.read_text(encoding='utf-8')) if path.exists() else {'replace':False,'values':[]}
  if 'slavicmyths:millstone' not in obj['values']:obj['values'].append('slavicmyths:millstone')
  json_file(path,obj)
 rus=['Земледелец','Рыбак','Пастух','Летописец','Картограф','Кожевник','Мясник','Каменщик','Стрелодел','Инструментальщик','Оружейник','Бронник','Знахарь','Бездельник']
 eng=['Farmer','Fisherman','Shepherd','Chronicler','Cartographer','Leatherworker','Butcher','Mason','Fletcher','Toolsmith','Weaponsmith','Armorer','Herbalist','Idler']
 keys=['farmer','fisherman','shepherd','librarian','cartographer','leatherworker','butcher','mason','fletcher','toolsmith','weaponsmith','armorer','cleric','nitwit']
 for lang,names,trader,newnames in [('ru_ru',rus,'Купец',['Мельник','Пивовар','Ткач','Скотовод','Охотник','Повар']),('en_us',eng,'Merchant',['Miller','Brewer','Weaver','Herder','Hunter','Cook'])]:
  overrides={f'entity.minecraft.villager.{k}':v for k,v in zip(keys,names)}|{'entity.minecraft.wandering_trader':trader}
  json_file(ROOT/'assets/minecraft/lang'/f'{lang}.json',overrides)
  p=ASSETS/'lang'/f'{lang}.json';obj=json.loads(p.read_text(encoding='utf-8'))
  obj.update({f'entity.minecraft.villager.slavicmyths.{k}':v for k,v in zip(['miller','brewer','weaver','herder','hunter','cook'],newnames)})
  obj.update({'block.slavicmyths.millstone':'Жернов' if lang=='ru_ru' else 'Millstone', 'village.slavicmyths.debug.none':'Рядом нет жителя или зомби-жителя.' if lang=='ru_ru' else 'No nearby villager or zombie villager.', 'village.slavicmyths.test.invalid':'Нужен взрослый безработный житель без сделок и свободное место для рабочего блока.' if lang=='ru_ru' else 'Requires an unemployed adult without trades and a clear workstation position.'})
  json_file(p,obj)

 from settlement_resources import main as settlement_assets
 settlement_assets()
 from village_buildings import main as buildings
 buildings()

if __name__=='__main__': main()
