"""0.8.4 native geometry, aged materials and archaeological data; deterministic."""
from finalize_065 import *
from water_070 import png
from woodlands_0801 import convert,merge_tag
from nightingale_082 import normalize_ogg
import math,zipfile,sys
KINDS=['log','stripped_log','planks','stairs','slab','fence','fence_gate','door','trapdoor']
NAMES={'burial_log_coffin':('Погребальная колода','Burial Log Coffin'),'ancient_carolingian_sword':('Древний каролингский меч','Ancient Carolingian Sword'),'restored_carolingian_sword':('Восстановленный каролингский меч','Restored Carolingian Sword'),'ancient_chekan':('Древний чекан','Ancient Chekan'),'chekan':('Чекан','Chekan'),'ancient_spear':('Древнее копьё','Ancient Spear'),'restored_spear':('Восстановленное копьё','Restored Spear'),'lunula':('Лунница','Lunula'),'grivna':('Гривна','Grivna'),'grave_ward':('Оберег от нежити','Undead Ward'),'ancient_fibula':('Древняя фибула','Ancient Fibula'),'ancient_comb':('Древний гребень','Ancient Comb'),'ancient_beads':('Древние бусины','Ancient Beads'),'old_buckle':('Старая пряжка','Old Buckle'),'pottery_fragment':('Фрагмент древней керамики','Ancient Pottery Fragment'),'old_arrowhead':('Старый наконечник стрелы','Old Arrowhead')}

def pixel(base,x,y,grain=False):
 if base==(106,69,42) and (x//3+y//4)%3!=0:base=(70,74,68)
 n=(x*17+y*7+x*y)%9-4
 if grain:n+=-18 if (x+y//7)%7==0 else 4 if x%7==2 else 0
 return tuple(max(0,min(255,c+n))for c in base)+(255,)

def textures():
 colors={'wood':(66,53,42),'iron':(77,79,72),'rust':(106,69,42),'edge':(146,148,134),'leather':(72,43,29),'silver':(142,143,124),'bone':(185,171,133),'clay':(135,73,47),'red':(116,48,37),'blue':(42,61,82),'amber':(145,96,44),'patina':(64,87,74)}
 for n,c in colors.items():png(A/f'textures/item/burial_{n}.png',16,lambda x,y,c=c,n=n:pixel(c,x,y,n in ['wood','leather','bone']))
 def aged(x,y,kind):
  base=(66,55,44)if 'log'in kind else(73,63,52)
  if 'top'in kind:
   d=math.hypot(x-7.2,y-8);n=-17 if int(d)%3==0 else 4
  else:n=-22 if (x+y//7)%6==0 else 8 if (x+y//7)%6==1 else 0
  if 'planks'in kind or 'door'in kind or 'trapdoor'in kind:
   if y%4==0 or (x+5*(y//4))%16==0:n=-27
  if (x//4+y//5)%4==0:base=(78,74,64)
  if (x*5+y)%23==0:n-=22
  return tuple(max(0,min(255,c+n))for c in base)+(255,)
 for n in ['log','log_top','stripped_log','stripped_log_top','planks','door_top','door_bottom','trapdoor']:png(A/f'textures/block/darkened_{n}.png',16,lambda x,y,n=n:aged(x,y,n))
 png(A/'textures/item/darkened_door.png',16,lambda x,y:aged(x,y,'door')if 4<=x<=11 else(0,0,0,0))
 def coffin(x,y):
  if x<128:return aged(x%16,y%16,'log')
  c=(185,171,133)if y<64 else(111,87,70)if y<128 else(77,79,72)
  return pixel(c,x,y,y<64)
 png(A/'textures/entity/burial_coffin.png',256,coffin)

def wood():
 cache=Path.home()/'.gradle/caches/forge_gradle/minecraft_repo/versions/1.16.5/client-extra.jar'
 with zipfile.ZipFile(cache)as z:
  names=z.namelist()
  for k in KINDS:
   v='stripped_oak_log'if k=='stripped_log'else'oak_'+k
   for folder in ['blockstates','models/item']:
    name=f'assets/minecraft/{folder}/{v}.json'
    if name in names:write(A/f'{folder}/darkened_{k}.json',convert(json.loads(z.read(name)),'darkened'))
   write(D/f'loot_tables/blocks/darkened_{k}.json',convert(json.loads(z.read(f'data/minecraft/loot_tables/blocks/{v}.json')),'darkened'))
   for name in names:
    if name.startswith('assets/minecraft/models/block/'+v)and name.endswith('.json'):
     stem=Path(name).stem.replace('stripped_oak_','darkened_stripped_').replace('oak_','darkened_');write(A/f'models/block/{stem}.json',convert(json.loads(z.read(name)),'darkened'))
   rec=f'data/minecraft/recipes/{v}.json'
   if rec in names:
    obj=convert(json.loads(z.read(rec)),'darkened');obj=json.loads(json.dumps(obj).replace('minecraft:oak_logs','slavicmyths:darkened_logs'));write(D/f'recipes/darkened_{k}.json',obj)
 for folder in ['blocks','items']:
  merge_tag('slavicmyths',folder,'darkened_logs',['slavicmyths:darkened_log','slavicmyths:darkened_stripped_log'])
  for tag,kind in [('logs',None),('logs_that_burn',None),('planks','planks'),('wooden_stairs','stairs'),('wooden_slabs','slab'),('wooden_fences','fence'),('wooden_doors','door'),('wooden_trapdoors','trapdoor')]:merge_tag('minecraft',folder,tag,['#slavicmyths:darkened_logs'if kind is None else'slavicmyths:darkened_'+kind])
  merge_tag('forge',folder,'fence_gates/wooden',['slavicmyths:darkened_fence_gate'])
 merge_tag('minecraft','blocks','fence_gates',['slavicmyths:darkened_fence_gate'])
 write(R/'docs/verification/burial-0.8.4.json',{'blocks':['darkened_'+k for k in KINDS],'items':['darkened_'+k for k in KINDS]})

def models():
 tex={n:'slavicmyths:item/burial_'+n for n in ['wood','iron','rust','edge','leather','silver','bone','clay','red','blue','amber','patina']};tex['particle']=tex['wood']
 display={'gui':{'rotation':[15,135,-10],'scale':[.8]*3},'ground':{'scale':[.45]*3},'firstperson_righthand':{'rotation':[0,90,-10],'scale':[.8]*3},'thirdperson_righthand':{'rotation':[0,90,0],'scale':[.85]*3}}
 for ancient in [True,False]:
  steel='rust'if ancient else'iron';edge='iron'if ancient else'edge';prefix='ancient_'if ancient else'restored_'
  sword=[cube([7,-5,7],[9,1,9],'leather'),cube([5,-7,6.5],[11,-5,9.5],steel),cube([6,-8,7],[10,-7,9],steel),cube([3.5,1,7],[12.5,2.2,9],steel),cube([6,2.2,7.5],[10,20,8.5],steel),cube([6,3,7.4],[6.5,18,8.6],edge),cube([9.5,3,7.4],[10,18,8.6],edge),cube([6.7,20,7.6],[9.3,23,8.4],edge),cube([7.5,23,7.7],[8.5,24,8.3],edge),cube([7.6,3,7.3],[8.4,19,7.5],'iron')]
  sword_display=json.loads(json.dumps(display));sword_display['gui']['scale']=[.46]*3
  model('item/'+prefix+'carolingian_sword',sword,tex,sword_display)
  chekan=[cube([7.4,-8,7.4],[8.6,16,8.6],'wood'),cube([6.6,14,7],[9.4,17,9],steel),cube([2,14.8,7.4],[7,16.2,8.6],steel),cube([.5,14.3,7.6],[2.5,15.4,8.4],edge),cube([9,14.5,7],[12,16.5,9],steel),cube([11,14,6.7],[12.5,17,9.3],edge)]
  for y in range(-6,2,2):chekan.append(cube([7.2,y,7.2],[8.8,y+.5,8.8],'leather'))
  chekan_display=json.loads(json.dumps(display));chekan_display['gui']['scale']=[.55]*3
  model('item/'+('ancient_chekan'if ancient else'chekan'),chekan,tex,chekan_display)
  spear=[cube([7.5,-15,7.5],[8.5,18,8.5],'wood'),cube([7,16,7],[9,20,9],'leather'),cube([6.5,20,7.5],[9.5,26,8.5],steel),cube([7,26,7.6],[9,29,8.4],edge),cube([7.5,29,7.7],[8.5,31,8.3],edge),cube([7.8,20,7.3],[8.2,28,7.6],'iron')]
  sd=json.loads(json.dumps(display));sd['gui']['scale']=[.38]*3;sd['thirdperson_righthand']['scale']=[1.15]*3;model('item/'+prefix+'spear',spear,tex,sd)
 def save(n,els):model('item/'+n,els,tex,display)
 cord=[cube([4,8,7],[4.5,15,7.5],'leather'),cube([11.5,8,7],[12,15,7.5],'leather'),cube([4,14.5,7],[12,15,7.5],'leather')]
 save('lunula',cord+[cube([4,5,7],[12,7,8.5],'silver'),cube([3,3,7],[5,6,8.5],'silver'),cube([11,3,7],[13,6,8.5],'silver'),cube([2.5,2,7],[4,4,8.2],'silver'),cube([12,2,7],[13.5,4,8.2],'silver'),cube([7.4,7,7],[8.6,9,8],'patina')])
 ring=[]
 for i in range(21):
  a=(i/20*280+130)*math.pi/180;x=8+math.cos(a)*5;y=8+math.sin(a)*5;ring.append(cube([x-.65,y-.65,7],[x+.65,y+.65,8.4],'silver'if i%2 else'patina'))
 for i in [0,20]:a=(i/20*280+130)*math.pi/180;x=8+math.cos(a)*5;y=8+math.sin(a)*5;ring.append(cube([x-1,y-1,6.8],[x+1,y+1,8.6],'silver'))
 save('grivna',ring)
 save('grave_ward',cord+[cube([5,2,6.8],[11,8,8.2],'wood'),cube([5.5,2.5,6.5],[10.5,3.1,6.8],'iron'),cube([7.7,3,6.4],[8.3,7,6.8],'silver'),cube([6,4,6.4],[10,4.6,6.8],'silver'),cube([6,6,6.4],[10,6.6,6.8],'silver')])
 save('ancient_fibula',[cube([3,5,7],[4.5,10,8.5],'patina'),cube([4,10,7],[11,11.5,8.5],'silver'),cube([10.5,5,7],[12,10,8.5],'patina'),cube([3,5,7],[12,6,8],'silver'),cube([4,5.5,6.5],[4.7,11,7],'iron'),cube([11,4.5,6.8],[13,6.5,8.3],'patina')])
 comb=[cube([2,9,7],[14,12,8],'bone'),cube([3,10,6.7],[13,10.5,7],'wood')]
 for i in range(11):comb.append(cube([2+i,4+(i%5==0)*2,7],[2.5+i,9,8],'bone'))
 save('ancient_comb',comb)
 beads=[cube([2,8,7.5],[14,8.5,8],'leather')]
 for i in range(6):beads.append(cube([2+i*2,6.5+i%2,7],[3.5+i*2,9.5+i%2,9],['red','blue','amber'][i%3]))
 save('ancient_beads',beads)
 save('old_buckle',[cube([3,4,7],[4.5,12,8.5],'iron'),cube([11.5,4,7],[13,12,8.5],'iron'),cube([4,4,7],[12,5.5,8.5],'iron'),cube([4,10.5,7],[12,12,8.5],'iron'),cube([7.5,5,6.7],[8.2,11.5,7.5],'silver'),cube([3,8,6.7],[5,8.6,7],'patina')])
 save('pottery_fragment',[cube([4,4,7],[12,9,8.5],'clay',([8,8,8],'z',22.5)),cube([4,8,7],[10,12,8.5],'clay'),cube([4,10.5,6.8],[10,11.5,7],'wood'),cube([5,7,6.7],[9,7.5,7],'bone'),cube([6,5,6.7],[6.5,8,7],'bone'),cube([8,6,6.7],[8.5,8,7],'bone')])
 save('old_arrowhead',[cube([7,2,7],[9,6,9],'rust'),cube([5.5,6,7.5],[10.5,9,8.5],'rust'),cube([6,9,7.5],[10,11,8.5],'iron'),cube([6.8,11,7.6],[9.2,13,8.4],'rust'),cube([7.6,13,7.7],[8.4,15,8.3],'edge'),cube([7.8,6,7.3],[8.2,12,7.5],'iron')])
 coffin=[cube([1,0,-8],[15,3,24],'wood'),cube([0,3,-8],[2,10,24],'wood'),cube([14,3,-8],[16,10,24],'wood'),cube([2,3,-8],[14,10,-6],'wood'),cube([2,3,22],[14,10,24],'wood'),cube([0,10,-8],[16,11,24],'wood'),cube([1,11,-8],[15,13,24],'wood')]
 for z in [-4,18]:coffin.append(cube([-.1,4,z],[16.1,11,z+1],'iron'))
 cd=json.loads(json.dumps(display));cd['gui']['scale']=[.45]*3;model('item/burial_log_coffin',coffin,tex,cd)
 write(A/'models/block/burial_log_coffin.json',{'textures':{'particle':'slavicmyths:block/darkened_log'}})
 write(A/'blockstates/burial_log_coffin.json',{'variants':{'':{'model':'slavicmyths:block/burial_log_coffin'}}})
 drop('burial_log_coffin','burial_log_coffin',[{'condition':'minecraft:block_state_property','block':'slavicmyths:burial_log_coffin','properties':{'part':'foot'}}])

def data():
 write(D/'recipes/burial_log_coffin.json',{'type':'minecraft:crafting_shaped','pattern':['SSS','PIP','PPP'],'key':{'S':{'tag':'minecraft:wooden_slabs'},'P':{'tag':'minecraft:planks'},'I':{'item':'minecraft:iron_ingot'}},'result':{'item':'slavicmyths:burial_log_coffin'}})
 shaped('chekan',[' II',' SI',' S '],{'I':'minecraft:iron_ingot','S':'minecraft:stick'})
 for ancient,result in [('ancient_carolingian_sword','restored_carolingian_sword'),('ancient_chekan','chekan'),('ancient_spear','restored_spear')]:write(D/f'recipes/restore_{result}.json',{'type':'slavicmyths:armorer_shapeless','ingredients':[{'item':'slavicmyths:'+ancient},{'item':'minecraft:iron_ingot'},{'item':'minecraft:leather'},{'item':'minecraft:stick'}],'result':{'item':'slavicmyths:'+result}})
 for slot,ids in [('necklace',['lunula','grivna']),('charm',['grave_ward'])]:merge_tag('curios','items',slot,['slavicmyths:'+i for i in ids])
 groups={'common':['ancient_coin','ancient_comb','ancient_beads','old_buckle','old_arrowhead','pottery_fragment'],'jewelry':['ancient_fibula','grivna','lunula','grave_ward'],'weapons':['ancient_carolingian_sword','ancient_chekan','ancient_spear']}
 for group,items in groups.items():write(D/f'loot_tables/chests/burial_{group}.json',{'type':'minecraft:chest','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:'+i}for i in items]}]})
 for kind,weapon,jewelry in [('small',.05,.09),('warrior',.18,.12),('great',.15,.2)]:
  # Two independent rolls; zero, one or two finds. No guaranteed treasure.
  pools=[]
  for chance,entries in [(.55,[('common',10)]),(.28,[('common',6),('jewelry',max(1,round(jewelry*30))),('weapons',max(1,round(weapon*30)))])]:pools.append({'rolls':1,'conditions':[{'condition':'minecraft:random_chance','chance':chance}],'entries':[{'type':'minecraft:loot_table','name':'slavicmyths:chests/burial_'+g,'weight':weight}for g,weight in entries]})
  write(D/f'loot_tables/chests/burial_{kind}.json',{'type':'minecraft:chest','pools':pools})
 for lang,idx in [('ru_ru',0),('en_us',1)]:
  p=A/f'lang/{lang}.json';v=json.loads(p.read_text(encoding='utf-8'))
  for name,titles in NAMES.items():v[('block'if name=='burial_log_coffin'else'item')+'.slavicmyths.'+name]=titles[idx]
  ru=['Потемневшее бревно','Потемневшее обтёсанное бревно','Потемневшие доски','Потемневшие ступени','Потемневшая плита','Потемневший забор','Потемневшая калитка','Потемневшая дверь','Потемневший люк']
  for k,title in zip(KINDS,ru):v['block.slavicmyths.darkened_'+k]=title if idx==0 else'Darkened '+k.replace('_',' ').title()
  entries={'tooltip.slavicmyths.burial_find':('Находится в древних захоронениях','Found in ancient burials'),'tooltip.slavicmyths.burial_weapon':('Древняя находка. Восстанавливается на Столе оружейника.','Ancient find. Restore at the Weapon Smith Table.'),'item.slavicmyths.lunula.effect':('Ночью на 5% меньше урона от существ.','5% less damage from creatures at night.'),'item.slavicmyths.grivna.effect':('На 4% меньше обычного урона, учитывающего броню.','4% less ordinary, armor-sensitive damage.'),'item.slavicmyths.grave_ward.effect':('На 9% меньше урона от нежити.','9% less damage from undead.'),'kurgan.command.not_found':('Курган этого типа не найден в области поиска. Попробуйте из другого района.','No barrow of this type found in the search area. Try another region.'),'kurgan.command.found':('%s: %s, %s, %s','%s: %s, %s, %s'),'kurgan.kind.0':('Малый курган','Small Kurgan'),'kurgan.kind.1':('Воинский курган','Warrior Kurgan'),'kurgan.kind.2':('Великий курган','Great Kurgan'),'subtitles.slavicmyths.coffin_open':('Тяжёлая крышка скрипит','Heavy lid creaks'),'subtitles.slavicmyths.coffin_close':('Деревянная крышка опускается','Wooden lid closes')}
  for key,value in entries.items():v[key]=value[idx]
  write(p,v)

def sounds():
 sys.path.insert(0,str(R/'.tools/audio-libs'));import numpy as np;import soundfile as sf
 events=json.loads((A/'sounds.json').read_text(encoding='utf-8'))
 for i,name in enumerate(['coffin_open','coffin_close']):
  rate=22050;t=np.arange(int(rate*(1.1 if i==0 else .65)))/rate;noise=np.random.default_rng(8400+i).normal(0,1,len(t));phase=2*np.pi*np.cumsum(130+32*np.sin(t*9))/rate
  wave=(np.sin(phase)*.3+np.sin(phase*2.31)*.14+noise*.04)*np.exp(-t*3)*np.minimum(1,t/.03)
  if i:wave+=np.sin(2*np.pi*82*t)*np.exp(-t*15)*.4
  wave*=.5/(np.max(np.abs(wave))+1e-9);p=A/f'sounds/burial/{name}.ogg';p.parent.mkdir(parents=True,exist_ok=True);sf.write(str(p),wave,rate,format='OGG',subtype='VORBIS');normalize_ogg(p,8400+i);events[name]={'subtitle':'subtitles.slavicmyths.'+name,'sounds':[{'name':'slavicmyths:burial/'+name}]}
 write(A/'sounds.json',events)

def generate():textures();wood();models();data();sounds()
if __name__=='__main__':generate()
