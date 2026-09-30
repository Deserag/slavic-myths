from pathlib import Path
import sys,json,math,random,wave,struct,subprocess,shutil
ROOT=Path(__file__).resolve().parents[1];R=ROOT/'src/main/resources';A=R/'assets/slavicmyths'
from generate_046 import Art,encode
from generate_050_models import png
NAMES={'kikimora':'Кикимора','poludnitsa':'Полудница','polevik':'Полевик','bannik':'Банник','igosha':'Игоша','ovinnik':'Овинник'}
ITEMS={'kikimora_lock':('Прядь кикиморы','Kikimora Hair Lock'),'noon_ear':('Сухой колос Полудницы','Withered Noon Ear'),'field_bundle':('Полевой узелок','Field Bundle'),'bath_broom':('Банный веник','Bath Whisk'),'bath_stone':('Банный камень','Bath Stone'),'old_button':('Старая пуговица','Old Button'),'ovinnik_claw':('Коготь Овинника','Ovinnik Claw'),'ember_heart':('Угольное сердце','Ember Heart'),'noon_sickle':('Серп Полудницы','Noon Sickle')}
def js(p,o):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(o,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def items():
 for name in ITEMS:
  a=Art()
  if name=='kikimora_lock':
   for x,top,end in ((4,3,11),(6,2,14),(8,3,12),(10,4,14),(12,5,11)):
    a.line(x,top,x-2,end,'d');a.line(x+1,top+1,x-1,end,'U');a.dot(x-2,end,'s')
   a.line(3,7,10,8,'t');a.line(4,8,9,9,'e')
  elif name=='noon_ear':
   a.line(4,14,10,3,'B');a.line(5,14,11,3,'t')
   for x,y in ((7,10),(8,8),(9,6),(10,4)):
    a.line(x-2,y-2,x,y,'e');a.line(x+1,y,x+3,y-1,'y');a.dot(x-2,y-3,'W')
  elif name=='field_bundle':
   a.disc(8,10,5,4,'U');a.disc(7,9,4,3,'e');a.line(5,8,8,12,'t');a.line(8,12,10,8,'t');a.box(6,5,9,6,'B')
   a.line(7,5,4,1,'g');a.line(8,5,9,1,'t');a.dot(9,1,'e');a.dot(10,2,'y');a.dot(5,3,'L');a.dot(6,1,'p');a.dot(5,1,'W')
  elif name=='bath_broom':
   a.line(4,14,9,5,'B');a.line(5,14,10,5,'t')
   for x,y in ((5,6),(7,4),(9,2),(11,3),(12,6),(9,7)):
    a.line(7,10,x,y,'B');a.disc(x,y,2,2,'g');a.dot(x-1,y-1,'G');a.dot(x,y,'L')
   a.line(5,11,8,12,'e');a.dot(7,10,'t')
  elif name=='bath_stone':
   a.disc(8,9,6,4,'N');a.disc(7,8,5,3,'D');a.line(4,6,8,5,'S');a.line(6,8,8,9,'U');a.line(8,9,9,11,'r');a.dot(11,7,'A')
  elif name=='old_button':
   a.disc(8,8,5,4,'U');a.disc(7,7,4,3,'t');a.line(5,5,9,4,'e');a.dot(6,7,'u');a.dot(9,7,'u');a.dot(6,9,'u');a.dot(9,9,'u')
  elif name=='ovinnik_claw':
   a.box(3,2,6,5,'U');a.line(4,3,8,5,'B');a.line(4,5,8,8,'e');a.line(5,5,10,8,'W');a.line(8,8,11,11,'e');a.line(10,9,11,13,'D');a.dot(11,14,'N');a.dot(12,12,'N')
  elif name=='ember_heart':
   a.disc(8,8,5,5,'N');a.disc(7,7,4,4,'D');a.line(5,5,7,8,'r');a.line(7,8,11,7,'O');a.line(7,8,8,12,'r');a.dot(8,8,'o');a.dot(8,7,'y');a.dot(4,8,'s');a.dot(10,4,'A')
  else:
   a.line(3,14,6,9,'U');a.line(4,14,7,9,'B');a.line(5,10,7,11,'t');a.line(6,9,11,7,'D');a.line(11,7,12,4,'S');a.line(12,4,9,2,'W');a.line(9,2,5,3,'S');a.line(5,3,4,6,'D');a.dot(4,4,'W');a.dot(6,9,'y')
  (A/f'textures/item/{name}.png').write_bytes(encode(a.rows()));js(A/f'models/item/{name}.json',{'parent':'minecraft:item/handheld' if name=='noon_sickle' else 'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+name}})
 for name in NAMES:js(A/f'models/item/{name}_spawn_egg.json',{'parent':'minecraft:item/template_spawn_egg'})

def blocks():
 for name in ('bath_stove','wooden_tub'):
  a=Art();a.box(0,0,15,15,'D' if name=='bath_stove' else 'B')
  if name=='bath_stove':
   for y in (3,8,13):a.line(0,y,15,y,'N');a.line(0,y+1,15,y+1,'s')
   for x,y in ((3,0),(10,4),(5,9)):a.line(x,y,x,y+3,'N');a.dot(x+1,y+1,'S')
  else:
   for x in (0,4,8,12):a.line(x,0,x,15,'u');a.line(x+1,0,x+1,15,'t')
   for y in (3,12):a.line(0,y,15,y,'N');a.line(0,y+1,15,y+1,'s')
  (A/f'textures/block/{name}.png').write_bytes(encode(a.rows()))
 def cube(lo,hi,t):return {'from':lo,'to':hi,'faces':{s:{'texture':'#'+t} for s in ('up','down','north','south','east','west')}}
 stove=[cube([1,0,1],[15,3,15],'main'),cube([2,3,5],[14,10,14],'main'),cube([2,3,2],[5,10,5],'main'),cube([11,3,2],[14,10,5],'main'),cube([2,9,2],[14,11,14],'main')]
 for x,z,h in ((3,3,3),(8,3,4),(3,8,4),(9,9,3)):stove.append(cube([x,11,z],[x+4,11+h,z+4],'stone'))
 stove.append(cube([5,3,5],[11,7,6],'fire'))
 tub=[cube([2,0,2],[14,2,14],'main'),cube([1,2,1],[3,11,15],'main'),cube([13,2,1],[15,11,15],'main'),cube([3,2,1],[13,11,3],'main'),cube([3,2,13],[13,11,15],'main'),cube([3,5,3],[13,6,13],'water')]
 for name,els in [('bath_stove',stove),('wooden_tub',tub)]:
  js(A/f'models/block/{name}.json',{'parent':'minecraft:block/block','textures':{'main':'slavicmyths:block/'+name,'particle':'slavicmyths:block/'+name,'stone':'minecraft:block/coal_block','fire':'minecraft:block/magma','water':'minecraft:block/lapis_block'},'elements':els})
  js(A/f'models/item/{name}.json',{'parent':'slavicmyths:block/'+name});js(A/f'blockstates/{name}.json',{'variants':{'':{'model':'slavicmyths:block/'+name}}})
  js(R/f'data/slavicmyths/loot_tables/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})

def resources():
 loot={'kikimora':[('kikimora_lock',.25)],'poludnitsa':[('noon_ear',.45),('noon_sickle',.08)],'polevik':[],'bannik':[('bath_stone',.65)],'igosha':[],'ovinnik':[('ovinnik_claw',1),('ember_heart',.65)]}
 for name,entries in loot.items():js(R/f'data/slavicmyths/loot_tables/entities/{name}.json',{'type':'minecraft:entity','pools':[{'rolls':1,'conditions':[{'condition':'minecraft:killed_by_player'},{'condition':'minecraft:random_chance','chance':chance}],'entries':[{'type':'minecraft:item','name':'slavicmyths:'+item}]} for item,chance in entries]})
 for name,items_ in [('bathhouse',['minecraft:bread','minecraft:stick','slavicmyths:bath_broom']),('old_barn',['minecraft:wheat','slavicmyths:ancient_coin','slavicmyths:ritual_charcoal'])]:
  js(R/f'data/slavicmyths/loot_tables/chests/{name}.json',{'type':'minecraft:chest','pools':[{'rolls':{'min':1,'max':3},'entries':[{'type':'minecraft:item','name':i} for i in items_]}]})
 def craft(name,ingredients,result,count=1):js(R/f'data/slavicmyths/recipes/{name}.json',{'type':'minecraft:crafting_shapeless','ingredients':[{'item':i if ':' in i else 'slavicmyths:'+i} for i in ingredients],'result':{'item':result if ':' in result else 'slavicmyths:'+result,'count':count}})
 craft('bath_broom',['minecraft:stick','minecraft:string','minecraft:birch_leaves','minecraft:birch_leaves'],'bath_broom')
 craft('bath_stove',['minecraft:cobblestone','minecraft:cobblestone','minecraft:iron_ingot','bath_stone'],'bath_stove')
 craft('wooden_tub',['minecraft:spruce_planks','minecraft:spruce_planks','minecraft:iron_ingot'],'wooden_tub')
 craft('hair_charm',['kikimora_lock','linen_thread','birch_bark'],'warding_charm')
 craft('noon_stone',['noon_ear','ritual_charcoal','silver_fitting'],'thunder_stone')
 craft('field_sign',['field_bundle','silver_fitting','ritual_charcoal'],'ancient_sign')
 craft('button_charm',['old_button','linen_cloth'],'traveler_charm')
 craft('claw_knife',['ovinnik_claw','silver_dagger','weapon_wrap'],'ritual_knife')
 labels={'block.slavicmyths.bath_stove':('Печь-каменка','Bath Stove'),'block.slavicmyths.wooden_tub':('Деревянная кадка','Wooden Tub'),
  'spirit.slavicmyths.bannik_warning':('Банник предупреждает: поднесите веник у каменки или уходите.','The Bannik warns: offer a whisk at the stove, or leave.'),
  'spirit.slavicmyths.bannik_peace':('Банник принял подношение. Здесь вам рады до конца этого дня.','The Bannik accepts your offering. You are welcome for ten minutes.'),
  'spirit.slavicmyths.ovinnik_warning':('Из темноты доносится рык. Уходите, пока хозяин терпит.','A growl rises from the dark. Leave while the master tolerates you.')}
 for id,ru in NAMES.items():labels['entity.slavicmyths.'+id]=(ru,id.capitalize());labels['item.slavicmyths.'+id+'_spawn_egg']=('Яйцо призыва: '+ru,id.capitalize()+' Spawn Egg')
 for id,ns in ITEMS.items():labels['item.slavicmyths.'+id]=ns
 for id in NAMES:
  for kind in ('ambient','hurt','death','angry','power'):labels['subtitles.slavicmyths.'+id+'_'+kind]=(NAMES[id]+': '+{'ambient':'шорох','hurt':'стон','death':'затихает','angry':'предупреждение','power':'всплеск силы'}[kind],id.capitalize()+' '+kind)
 for i,lang in enumerate(('ru_ru','en_us')):
  p=A/f'lang/{lang}.json';o=json.loads(p.read_text(encoding='utf-8'));o.update({k:v[i] for k,v in labels.items()});js(p,o)

def sounds():
 s=json.loads((A/'sounds.json').read_text());tmp=ROOT/'work/audio050';tmp.mkdir(parents=True,exist_ok=True);ff=shutil.which('ffmpeg')
 if not ff:
  expected=[A/f'sounds/land/{id}_{kind}.ogg' for id in NAMES for kind in ('ambient','hurt','death','angry','power')]
  if all(p.exists() for p in expected):
   for id in NAMES:
    for kind in ('ambient','hurt','death','angry','power'):
     name=id+'_'+kind;s[name]={'subtitle':'subtitles.slavicmyths.'+name,'sounds':[{'name':'slavicmyths:land/'+name,'volume':.65}]}
   js(A/'sounds.json',s);return
  raise RuntimeError('ffmpeg is required because generated 0.5.0 OGG files are missing')
 for idx,id in enumerate(NAMES):
  for kind in ('ambient','hurt','death','angry','power'):
   name=id+'_'+kind;dur={'ambient':1.7,'hurt':.45,'death':2.2,'angry':.8,'power':1.2}[kind];rate=22050;n=int(rate*dur);rng=random.Random(name);noise=0;samples=[]
   for k in range(n):
    t=k/rate;env=min(1,t/.08)*min(1,(dur-t)/.25);noise=.75*noise+.25*rng.uniform(-1,1)
    base=[330,510,170,88,430,53][idx];base*=1.4 if kind=='angry' else .72 if kind=='death' else 1
    tone=math.sin(2*math.pi*(base*t+4*math.sin(t*8)))
    pulse=(.5+.5*math.sin(t*(17 if id=='kikimora' else 7)))**2
    if id=='polevik':value=noise*.55+math.sin(2*math.pi*(900*t+math.sin(t*3)*14))*.025
    elif id=='bannik' and kind=='power':value=noise*.8
    elif id=='ovinnik':value=tone*.15+math.sin(2*math.pi*base*.51*t)*.16+noise*.3
    elif id=='igosha':value=(tone*.07+noise*.25)*pulse
    else:value=(tone*.1+noise*.4)*pulse
    samples.append(int(max(-.9,min(.9,value*env))*32767))
   wav=tmp/(name+'.wav')
   with wave.open(str(wav),'wb') as f:f.setparams((1,2,rate,n,'NONE',''));f.writeframes(struct.pack('<'+'h'*len(samples),*samples))
   p=A/'sounds'/('land/'+name+'.ogg');p.parent.mkdir(parents=True,exist_ok=True)
   subprocess.run([ff,'-hide_banner','-loglevel','error','-y','-i',str(wav),'-c:a','libvorbis','-q:a','4',str(p)],check=True)
   s[name]={'subtitle':'subtitles.slavicmyths.'+name,'sounds':[{'name':'slavicmyths:land/'+name,'volume':.65}]}
 js(A/'sounds.json',s)
if __name__=='__main__':items();blocks();resources();sounds()
