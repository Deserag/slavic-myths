"""Reproducible native pixel assets/parts/bench recipes; historical weapon assets stay intact."""
from pathlib import Path
import json
from PIL import Image,ImageDraw

ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources'
MATERIALS={'stone':('Каменный','Stone','#727574','#acadaa','minecraft:cobblestone'),
 'iron':('Железный','Iron','#8f9294','#e2e3dc','minecraft:iron_ingot'),
 'gold':('Золотой','Golden','#a17b17','#f7d755','minecraft:gold_ingot'),
 'diamond':('Алмазный','Diamond','#147e89','#57e0d6','minecraft:diamond'),
 'netherite':('Незеритовый','Netherite','#393039','#77666d','minecraft:netherite_ingot'),
 'silver':('Серебряный','Silver','#8a99a6','#e7edf2','slavicmyths:silver_ingot'),
 'perunite':('Перунитовый','Perunite','#253d78','#6999d8','slavicmyths:perunite')}
TYPES={'spear':('копьё','Spear'),'sulitsa':('сулица','Sulitsa'),'flail':('кистень','Flail'),'throwing_knife':('метательный нож','Throwing Knife')}
PARTS={'spearhead':('наконечник копья','Spearhead'),'light_spearhead':('лёгкий наконечник','Light Spearhead'),'flail_ball':('шар кистеня','Flail Ball')}
COMMON={'iron_blank':('Железная заготовка','Iron Blank'),'spear_shaft':('Древко копья','Spear Shaft'),'short_shaft':('Короткое древко','Short Shaft'),
 'flail_handle':('Рукоять кистеня','Flail Handle'),'flail_chain':('Цепь кистеня','Flail Chain'),
 'short_bow':('Короткий лук','Short Bow'),'heavy_bow':('Тяжёлый лук','Heavy Bow')}
OLD={'spear','silver_spear','flail'}
def weapon_id(m,t):return 'spear' if (m,t)==('stone','spear') else 'flail' if (m,t)==('iron','flail') else m+'_'+t
def write(path,value):
 p=RES/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def icon(kind,dark='#8f9294',light='#e2e3dc',pull=0):
 im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
 wood='#67452c';bright='#a77843';edge='#242020'
 def line(points,color,width=1):d.line(points,fill=color,width=width)
 def blade(points):d.polygon(points,fill=edge);d.polygon([(x-1,y+1) for x,y in points[1:-1]],fill=dark)
 if kind=='spear':
  line([(3,29),(25,7)],edge,4);line([(4,28),(23,9)],wood,2);line([(4,27),(22,9)],bright)
  d.polygon([(22,12),(22,7),(29,1),(29,7),(25,12)],fill=edge);d.polygon([(23,10),(23,7),(28,3),(27,7),(25,10)],fill=dark);line([(24,8),(28,3)],light)
  line([(20,11),(22,13)],'#c6a574',2)
 elif kind=='sulitsa':
  line([(6,26),(22,10)],edge,3);line([(7,25),(22,10)],bright)
  d.polygon([(21,12),(22,7),(27,4),(26,9),(23,13)],fill=edge);line([(22,11),(26,6)],dark,2);line([(23,10),(26,5)],light)
  d.rectangle((9,22,11,24),outline='#c6a574')
 elif kind=='flail':
  line([(5,26),(13,12)],edge,5);line([(6,25),(13,13)],wood,3);line([(6,24),(12,14)],bright)
  d.rectangle((11,10,14,13),fill=dark);line([(12,10),(15,7),(19,8),(21,12),(22,19)],edge,3)
  for x,y in [(15,8),(18,8),(20,12),(21,16)]:d.rectangle((x,y,x+1,y+1),fill=light)
  d.polygon([(20,19),(25,19),(28,22),(27,27),(23,29),(19,26),(18,22)],fill=edge)
  d.rectangle((20,21,25,26),fill=dark);d.rectangle((21,21,24,23),fill=light)
 elif kind=='throwing_knife':
  line([(9,24),(13,20)],edge,4);line([(10,23),(13,20)],wood,2);line([(10,22),(12,20)],bright)
  d.polygon([(12,19),(17,12),(23,8),(20,15),(15,21)],fill=edge);d.polygon([(14,19),(18,13),(22,10),(19,15),(15,19)],fill=dark);line([(15,17),(22,10)],light)
 elif kind in ('short_bow','heavy_bow'):
  heavy=kind=='heavy_bow';top,bottom=(2,29) if heavy else (7,25);bend=5 if heavy else 9;right=21 if heavy else 19
  pts=[(right,top),(bend+3,top+5),(bend,15),(bend+3,bottom-5),(right,bottom)]
  line(pts,edge,5 if heavy else 3);line(pts,wood if heavy else bright,3 if heavy else 1)
  line([(right,top),(right+pull*2,16),(right,bottom)],'#c9c4ac')
  d.rectangle((bend-1,13,bend+3,18),fill='#47322c');d.rectangle((bend,14,bend+1,17),fill=bright)
  if heavy:
   line([(right-4,top+2),(right-2,top+4)],dark,2);line([(right-4,bottom-2),(right-2,bottom-4)],dark,2)
  if pull:line([(bend+3,16),(30,16)],'#b99863');d.polygon([(29,14),(31,16),(29,18)],fill=light)
 elif kind in ('spear_shaft','short_shaft','flail_handle'):
  start,end=((5,27),(26,6)) if kind=='spear_shaft' else ((9,24),(22,11)) if kind=='short_shaft' else ((10,23),(19,10))
  line([start,end],edge,4);line([start,end],wood,2);line([(start[0],start[1]-1),(end[0],end[1]-1)],bright)
 elif kind in ('spearhead','light_spearhead'):
  pts=[(9,24),(11,14),(24,5),(21,18),(14,24)] if kind=='spearhead' else [(12,23),(15,13),(23,7),(20,18),(15,23)]
  d.polygon(pts,fill=edge);d.polygon([(x,y-1) for x,y in pts[1:-1]],fill=dark);line([(14,19),(22,8)],light,2)
 elif kind=='flail_ball':
  d.polygon([(10,8),(21,8),(25,13),(24,22),(19,26),(10,24),(6,18),(7,12)],fill=edge)
  d.rectangle((9,11,21,22),fill=dark);d.rectangle((10,11,17,14),fill=light);d.rectangle((13,6,17,9),fill=dark)
 elif kind=='flail_chain':
  for x,y in [(8,19),(13,12),(18,5)]:d.rectangle((x,y,x+6,y+9),outline=edge,width=3);d.rectangle((x+1,y+1,x+5,y+8),outline=light)
 else:
  d.polygon([(7,14),(20,9),(26,13),(12,19)],fill=edge);line([(9,14),(20,11),(24,13),(12,17)],dark,3);line([(10,13),(20,10)],light)
 return im
def asset(id,kind,dark,light):
 p=RES/f'assets/slavicmyths/textures/item/{id}.png';p.parent.mkdir(parents=True,exist_ok=True);icon(kind,dark,light).save(p)
 model={'parent':'minecraft:item/handheld' if kind in TYPES else 'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+id}}
 if kind in TYPES:
  scale={'spear':1.3,'sulitsa':1.05,'flail':1,'throwing_knife':.7}[kind]
  model['display']={f'{view}_{hand}':{'rotation':[0,-90 if hand=='righthand' else 90,(55 if view=='thirdperson' else 25)*(1 if hand=='righthand' else -1)],'translation':[0,4,0] if view=='thirdperson' else [1.13,3.2,1.13],'scale':[scale*(.85 if view=='thirdperson' else .7)]*3} for view in ('thirdperson','firstperson') for hand in ('righthand','lefthand')}
 if kind.endswith('_bow'):
  model['parent']='minecraft:item/bow'
  model['overrides']=[{'predicate':{'pulling':1,'pull':p},'model':f'slavicmyths:item/{id}_pulling_{i}'} for i,p in enumerate((0,.65,.9))]
  for i in range(3):
   icon(kind,dark,light,i+1).save(RES/f'assets/slavicmyths/textures/item/{id}_pulling_{i}.png')
   write(f'assets/slavicmyths/models/item/{id}_pulling_{i}.json',{'parent':'minecraft:item/bow','textures':{'layer0':f'slavicmyths:item/{id}_pulling_{i}'}})
 write(f'assets/slavicmyths/models/item/{id}.json',model)
def recipe(id,result,ingredients,copy=False):
 write(f'data/slavicmyths/recipe/weapon_{id}.json',{'type':'slavicmyths:armorer_shapeless','ingredients':[{'item':x} for x in ingredients],'result':{'id':'slavicmyths:'+result,'count':1},**({'copy_components':True} if copy else {})})
def shaped(id,result,pattern,key):
 write(f'data/slavicmyths/recipe/weapon_part_{id}.json',{'type':'minecraft:crafting_shaped','pattern':pattern,'key':{k:{'item':v} for k,v in key.items()},'result':{'id':'slavicmyths:'+result,'count':1}})
def main():
 ids=[];names={};weapons=[]
 for m,(ru,en,dark,light,material) in MATERIALS.items():
  for kind,(rname,ename) in {**TYPES,**PARTS}.items():
   id=weapon_id(m,kind) if kind in TYPES else m+'_'+kind
   if id not in OLD:asset(id,kind,dark,light);ids.append(id)
   # Correct Russian gender, without changing old localized item names.
   prefix=ru[:-2]+'ая' if kind=='sulitsa' and ru.endswith('ый') else ru[:-2]+'ая' if kind=='sulitsa' and ru.endswith('ой') else ru
   if kind=='spear':prefix=ru[:-2]+'ое'
   if id not in OLD:names[id]=(prefix+' '+rname,en+' '+ename)
   if kind in TYPES:
    weapons.append(id)
    if m=='netherite':
     recipe(id+'_upgrade',id,['slavicmyths:'+weapon_id('diamond',kind),'minecraft:netherite_ingot'],True)
     components={'spear':['spear_shaft','netherite_spearhead'],'sulitsa':['short_shaft','netherite_light_spearhead'],'flail':['flail_handle','flail_chain','netherite_flail_ball']}
     if kind in components:recipe(id+'_assembly',id,['slavicmyths:'+x for x in components[kind]])
    else:
     parts={'spear':['spear_shaft',m+'_spearhead'],'sulitsa':['short_shaft',m+'_light_spearhead'],'flail':['flail_handle','flail_chain',m+'_flail_ball'],'throwing_knife':['iron_blank' if m=='iron' else material,'minecraft:stick']}
     ingredients=[x if ':' in x else 'slavicmyths:'+x for x in parts[kind]]
     recipe(id+'_assembly',id,ingredients)
   else:
    if m=='netherite':recipe(id+'_upgrade',id,['slavicmyths:diamond_'+kind,'minecraft:netherite_ingot'],True)
    else:
     pattern=[' X ','XXX',' X '] if kind=='flail_ball' else [' X','X '] if kind=='spearhead' else ['X','X']
     shaped(id,id,pattern,{'X':material})
 for id,(ru,en) in COMMON.items():asset(id,id,'#8f9294','#e2e3dc');ids.append(id);names[id]=(ru,en)
 shaped('iron_blank','iron_blank',['NNN'],{'N':'minecraft:iron_nugget'})
 shaped('spear_shaft','spear_shaft',['S','S','S'],{'S':'minecraft:stick'})
 shaped('short_shaft','short_shaft',['S','S'],{'S':'minecraft:stick'})
 shaped('flail_handle','flail_handle',['L','S'],{'L':'minecraft:leather','S':'minecraft:stick'})
 shaped('flail_chain','flail_chain',['C','C','C'],{'C':'minecraft:chain'})
 recipe('short_bow','short_bow',['minecraft:stick']*3+['minecraft:string']*2+['minecraft:leather'])
 recipe('heavy_bow','heavy_bow',['minecraft:bow','minecraft:stick','minecraft:stick','minecraft:string','slavicmyths:iron_rings','slavicmyths:weapon_wrap'],True)
 write('data/minecraft/recipe/repair_item.json',{'type':'slavicmyths:rune_safe_repair','category':'misc'})
 for locale,n in [('ru_ru',0),('en_us',1)]:
  p=RES/f'assets/slavicmyths/lang/{locale}.json';data=json.loads(p.read_text(encoding='utf-8'))
  data.update({'item.slavicmyths.'+id:value[n] for id,value in names.items()})
  hints={'spear':('Длинное древко: +1.25 блока к дистанции атаки.','Long shaft: +1.25 blocks melee reach.'),'sulitsa':('Ближний бой: +0.5 блока. Удерживайте и отпустите для броска; подберите сулицу.','Melee reach +0.5 blocks. Hold and release to throw; recover the sulitsa.'),'flail':('Медленный удар, умеренное давление на броню, возможность сбить щит. Удерживайте для усиленного удара.','Slow strike, moderate armor pressure and shield disable. Hold for a charged strike.'),'throwing_knife':('Быстрый бросок. После попадания в существо шанс потери — 25%. Один прочный нож, не стек.','Quick throw. A creature hit has a 25% loss chance. One durable knife, not a stack.'),'short_bow':('Полный натяг: 12 тиков. Быстрее и подвижнее, но слабее обычного лука.','Full draw: 12 ticks. Faster and more mobile, but weaker than a normal bow.'),'heavy_bow':('Полный натяг: 30 тиков. Медленнее, мощнее и дальнобойнее; полный натяг сильнее замедляет.','Full draw: 30 ticks. Slower, stronger and longer ranged; full draw slows movement further.')}
  for id in ids:
   kind=next((t for t in TYPES if id.endswith('_'+t)),id)
   if kind in hints:data['tooltip.slavicmyths.weapon.'+id]=hints[kind][n]
  data['entity.slavicmyths.weapon_projectile']=('Метательное оружие','Thrown Weapon')[n]
  data['rpg.slavicmyths.rune_count']=('Руны: %s/%s','Runes: %s/%s')[n]
  for key,value in {'recipes':('Рецепты оружейного верстака','Weapon bench recipes'),'shapeless':('Порядок компонентов свободный','Ingredients in any order'),'shaped':('Повторите показанную схему','Follow the shown pattern'),'keeps_data':('Сохраняет руны и чары','Keeps runes and enchantments'),'no_recipes':('Рецепты пока не получены','Recipes not yet received')}.items():data['weapon.slavicmyths.'+key]=value[n]
  write(f'assets/slavicmyths/lang/{locale}.json',data)
 # Vanilla enchantment eligibility uses the real tag definitions, never replacement enchantments.
 bows=['slavicmyths:short_bow','slavicmyths:heavy_bow'];melee=['slavicmyths:'+x for x in weapons if not x.endswith('throwing_knife')]
 allweapons=['slavicmyths:'+x for x in weapons]+bows
 for tag,values in {'bow':bows,'durability':allweapons,'weapon':melee,'sharp_weapon':melee}.items():
  path=f'data/minecraft/tags/item/enchantable/{tag}.json';p=RES/path
  old=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]}
  old['values']=list(dict.fromkeys(old['values']+values));write(path,old)
 sheet=Image.new('RGBA',(7*120,6*100),'#d4c2a2');draw=ImageDraw.Draw(sheet)
 for x,(m,(_,en,dark,light,_)) in enumerate(MATERIALS.items()):
  draw.text((x*120+5,4),en,fill='#282321')
  for y,kind in enumerate(TYPES):sheet.alpha_composite(icon(kind,dark,light).resize((64,64),Image.Resampling.NEAREST),(x*120+20,25+y*105));draw.text((x*120+2,90+y*105),kind,fill='#282321')
 for x,kind in enumerate(('short_bow','heavy_bow')):sheet.alpha_composite(icon(kind).resize((64,64),Image.Resampling.NEAREST),(x*120+20,465));draw.text((x*120+2,535),kind,fill='#282321')
 out=ROOT/'docs/media/weapons-1321.png';out.parent.mkdir(exist_ok=True);sheet.save(out)
 print(f'Generated {len(ids)} new items, 30 weapon forms (three existing IDs retained), parts and bench recipes.')
if __name__=='__main__':
 main()
 import rare_weapon_resources_1322
 rare_weapon_resources_1322.main()
