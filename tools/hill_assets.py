"""Original pixel assets and native datapack content for the unified procedural hill."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,random,math,wave,struct,subprocess,shutil
from morozko_assets import write,merge
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';D=R/'src/main/resources/data/slavicmyths';O=R/'docs/verification/serpent-hill-1.9.5'
NAMES={'serpent_masonry':('Змеиная кладка','Serpent Masonry'),'cracked_serpent_masonry':('Потрескавшаяся змеиная кладка','Cracked Serpent Masonry'),'ruined_serpent_masonry':('Разрушенная змеиная кладка','Ruined Serpent Masonry'),'serpent_bas_relief':('Змеиный барельеф','Serpent Bas-relief'),'ancient_ritual_slab':('Древняя ритуальная плита','Ancient Ritual Slab'),'defiled_ritual_slab':('Осквернённая ритуальная плита','Defiled Ritual Slab'),'serpent_torch':('Змеиный факел','Serpent Torch'),'ritual_brazier':('Ритуальная жаровня','Ritual Brazier'),'defiled_ritual_brazier':('Осквернённая ритуальная жаровня','Defiled Ritual Brazier'),'ancient_hanging_lamp':('Древний подвесной светильник','Ancient Hanging Lamp'),'mor_crystal':('Моровой кристалл','Mor Crystal')}
LIGHT=[0,0,0,2,0,4,12,15,10,12,7]
def glyph(d,red=False):
 color='#CA354B' if red else '#8D858B'
 for cx in [8,16,24]:
  d.line([(16,28),(cx,20),(cx,12)],fill=color,width=2);d.rectangle((cx-3,7,cx+2,12),fill='#47434D',outline=color);d.rectangle((cx-1,9,cx,10),fill='#FD6373' if red else '#AA9A8D')
def tex(name):
 im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im);rng=random.Random(195+list(NAMES).index(name))
 for y in range(32):
  for x in range(32):
   v=rng.choice([40,44,49,55,59]);d.point((x,y),fill=(v,v-2,v+3,255))
 for y in [0,8,16,24,31]:d.line((0,y,31,y),fill='#211E29')
 for y in [4,20]:
  for x in range(-4,36,8):d.line([(x,y),(x+4,y+4),(x+8,y)],fill='#89838A',width=1)
 if 'cracked' in name:d.line([(5,0),(10,8),(7,14),(18,20),(16,31)],fill='#18151D',width=2)
 if 'ruined' in name:
  for _ in range(25):
   x,y=rng.randrange(32),rng.randrange(32);d.rectangle((x,y,x+2,y+1),fill=rng.choice(['#657054','#1C1A23','#8A7C6E']))
 if 'bas_relief' in name:glyph(d,True)
 if 'slab' in name:
  d.rectangle((2,2,29,29),outline='#8D858B' if 'defiled' not in name else '#BB283F',width=2);glyph(d,'defiled' in name)
 if 'torch' in name:
  d.rectangle((11,1,20,27),fill='#382A20');d.rectangle((7,4,24,12),fill='#5C503A');d.rectangle((9,6,11,8),fill='#F0A439');d.rectangle((21,6,23,8),fill='#F0A439')
 if 'brazier' in name:
  d.rectangle((0,10,31,18),fill='#363038');d.line((0,10,31,10),fill='#9A8573',width=2);d.rectangle((8,2,23,9),fill='#E88424' if 'defiled' not in name else '#CE2852')
 if 'lamp' in name:d.rectangle((5,7,26,25),fill='#674C37');d.rectangle((10,11,21,21),fill='#FFD795');d.rectangle((3,4,28,7),fill='#40353A')
 if name=='mor_crystal':
  for x,y,w,h in [(4,9,8,20),(12,2,8,27),(22,14,6,15)]:d.rectangle((x,y,x+w,y+h),fill='#772342');d.line((x+1,y+1,x+1,y+h-2),fill='#F2467A',width=2)
 im.save(A/f'textures/block/{name}.png')
def elements(name,wall=False):
 if 'masonry' in name:return [{'from':[0,0,0],'to':[16,16,16]}]
 if name=='serpent_bas_relief':return [{'from':[0,0,0],'to':[16,16,16]}]+[{'from':[x,8,-1],'to':[x+3,12,1]} for x in [3,7,11]]
 if 'slab' in name:return [{'from':[0,0,0],'to':[16,8,16]}]
 if name=='serpent_torch':
  return [{'from':[6,1,11] if wall else [7,0,7],'to':[10,11,15] if wall else [9,11,9]},{'from':[4,9,9] if wall else [4,9,4],'to':[12,13,15] if wall else [12,13,12]},{'from':[6,13,10] if wall else [6,13,6],'to':[10,16,14] if wall else [10,16,10]}]
 if 'brazier' in name:return [{'from':[3,0,3],'to':[13,2,13]},{'from':[6,2,6],'to':[10,7,10]},{'from':[1,7,1],'to':[15,10,15]},{'from':[1,10,1],'to':[3,12,15]},{'from':[13,10,1],'to':[15,12,15]},{'from':[3,10,1],'to':[13,12,3]},{'from':[3,10,13],'to':[13,12,15]},{'from':[5,10,5],'to':[11,15,11]}]
 if 'lamp' in name:return [{'from':[7,10,7],'to':[9,16,9]},{'from':[3,2,3],'to':[13,10,13]},{'from':[2,1,2],'to':[14,3,14]},{'from':[2,9,2],'to':[14,11,14]}]
 return [{'from':[2,0,2],'to':[14,3,14]},{'from':[4,3,4],'to':[7,12,8]},{'from':[8,3,7],'to':[11,16,10]},{'from':[11,3,10],'to':[14,10,13]}]
def blocks():
 for name in NAMES:
  tex(name)
  for wall in [False,True]:
   e=elements(name,wall);suffix='_wall' if name=='serpent_torch' and wall else ''
   for part in e:part['faces']={face:{'texture':'#surface'} for face in ['north','south','east','west','up','down']}
   write(A/f'models/block/{name}{suffix}.json',{'parent':'minecraft:block/block','textures':{'surface':'slavicmyths:block/'+name,'particle':'slavicmyths:block/'+name},'elements':e})
  variants={f'facing={face},hanging={str(wall).lower()}':{'model':'slavicmyths:block/'+name+('_wall' if name=='serpent_torch' and wall else ''),'y':rotation} for face,rotation in [('north',0),('east',90),('south',180),('west',270)] for wall in [False,True]}
  write(A/f'blockstates/{name}.json',{'variants':variants});write(A/f'models/item/{name}.json',{'parent':'slavicmyths:block/'+name})
  write(D/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
def recipes():
 m='slavicmyths:serpent_masonry';coal='minecraft:coal';red='minecraft:redstone';iron='minecraft:iron_ingot'
 recipes={'serpent_masonry':(['minecraft:polished_blackstone_bricks']*4+['minecraft:stone_bricks'],4),'cracked_serpent_masonry':([m,'minecraft:cobblestone'],1),'ruined_serpent_masonry':(['slavicmyths:cracked_serpent_masonry','minecraft:gravel'],1),'serpent_bas_relief':([m,'minecraft:red_dye'],1),'ancient_ritual_slab':([m]*4+[red],4),'defiled_ritual_slab':(['slavicmyths:ancient_ritual_slab',red],1),'serpent_torch':(['minecraft:stick',coal,m],2),'ritual_brazier':([iron]*4+['slavicmyths:ancient_ritual_slab',coal],1),'defiled_ritual_brazier':(['slavicmyths:ritual_brazier',red],1),'ancient_hanging_lamp':(['minecraft:chain']*2+[iron]*2+['minecraft:lantern',m],1),'mor_crystal':(['minecraft:amethyst_shard']*4+[red,'minecraft:blackstone'],1)}
 for name,(ingredients,count) in recipes.items():
  write(D/f'recipe/{name}.json',{'type':'minecraft:crafting_shapeless','ingredients':[{'item':i} for i in ingredients],'result':{'id':'slavicmyths:'+name,'count':count}})
  write(D/f'advancement/recipes/serpent_hill/{name}.json',{'parent':'minecraft:recipes/root','criteria':{'ingredient':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':ingredients[0]}]}},'has_recipe':{'trigger':'minecraft:recipe_unlocked','conditions':{'recipe':'slavicmyths:'+name}}},'requirements':[['ingredient','has_recipe']],'rewards':{'recipes':['slavicmyths:'+name]}})
def data():
 write(D/'worldgen/structure/serpent_hill.json',{'type':'slavicmyths:serpent_hill','biomes':'#slavicmyths:has_structure/serpent_hill','step':'surface_structures','terrain_adaptation':'none','spawn_overrides':{}})
 write(D/'worldgen/structure_set/serpent_hill.json',{'structures':[{'structure':'slavicmyths:serpent_hill','weight':1}],'placement':{'type':'minecraft:random_spread','spacing':512,'separation':384,'salt':1951013,'spread_type':'linear','exclusion_zone':{'other_set':'minecraft:strongholds','chunk_count':12}}})
 write(D/'tags/worldgen/biome/has_structure/serpent_hill.json',{'replace':False,'values':['minecraft:dark_forest','minecraft:forest','minecraft:birch_forest','minecraft:old_growth_birch_forest','minecraft:taiga','minecraft:old_growth_pine_taiga','minecraft:windswept_hills','minecraft:windswept_forest','minecraft:stony_peaks']})
 write(D/'damage_type/serpent_hill_mor.json',{'message_id':'serpent_hill_mor','scaling':'never','exhaustion':.1})
 write(D/'loot_table/chests/serpent_fortress.json',{'type':'minecraft:chest','pools':[{'rolls':{'type':'minecraft:uniform','min':2,'max':4},'entries':[{'type':'minecraft:item','name':name,'weight':weight,'functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':low,'max':high}}]} for name,weight,low,high in [('minecraft:iron_ingot',3,2,5),('minecraft:coal',5,3,8),('minecraft:bread',4,2,4),('slavicmyths:serpent_masonry',4,4,12),('slavicmyths:cleansing_powder',1,1,2),('slavicmyths:purified_water_bottle',1,1,1)]]}]})
 write(D/'tales/serpent_hill.json',{'part':'creatures','chapter':'legends','section':'inhabitants','title':'tales.entry.serpent_hill.title','icon':'slavicmyths:serpent_bas_relief','initial':0,'subjects':['structure/slavicmyths:serpent_hill'],'advancements':[],'links':[]})
def audio():
 ff=shutil.which('ffmpeg');assert ff;work=R/'.tools/hill-audio';work.mkdir(parents=True,exist_ok=True);sounds={}
 for index,name in enumerate(['danger','crystal','brazier','deep_ambient','raised']):
  wav=work/(name+'.wav');rate=22050;duration=.8 if name!='deep_ambient' else 1.6;rng=random.Random(1950+index)
  samples=[struct.pack('<h',int((math.sin(2*math.pi*(90+index*44)*i/rate)*.12+rng.uniform(-.035,.035))*math.sin(math.pi*i/(rate*duration))**2*32767)) for i in range(int(rate*duration))]
  with wave.open(str(wav),'wb') as out:out.setnchannels(1);out.setsampwidth(2);out.setframerate(rate);out.writeframes(b''.join(samples))
  target=A/f'sounds/serpenthill/{name}.ogg';target.parent.mkdir(parents=True,exist_ok=True);subprocess.run([ff,'-y','-loglevel','error','-i',str(wav),'-c:a','libvorbis','-q:a','3','-flags:a','+bitexact','-fflags','+bitexact',str(target)],check=True)
  sounds['serpenthill.'+name]={'subtitle':'subtitles.slavicmyths.serpenthill.'+name,'sounds':[{'name':'slavicmyths:serpenthill/'+name}]}
 merge(A/'sounds.json',sounds)
def language():
 pairs={'ward_needed':('Сильный Мор! Нужен оберег от Мора: в инвентаре или действующий.','Strong corruption! Carry a Mor Ward or activate its protection.'),'rotation':('Поворот: none, clockwise_90, clockwise_180, counterclockwise_90.','Rotation: none, clockwise_90, clockwise_180, counterclockwise_90.'),'overworld_only':('Змеиная гора создаётся только в Overworld.','Serpent Hill can only be created in the Overworld.'),'busy':('Сначала дождитесь завершения текущей генерации горы.','Wait for the current hill construction to finish.'),'border':('Комплекс выходит за границу мира.','The complex extends beyond the world border.'),'missing_structure':('Данные структуры недоступны: проверьте datapack.','Structure data is unavailable; check the datapack.'),'occupied':('В исходном чанке уже есть Змеиная гора. Выберите другие координаты.','A Serpent Hill already exists in the owner chunk. Choose another location.'),'queued':('Строительство Змеиной горы началось у %s, %s, %s. Обычные блоки в области 128×144×128 заменяются; bedrock сохраняется. ID: %s','Serpent Hill construction started at %s, %s, %s. Ordinary blocks within 128×144×128 are replaced; bedrock is preserved. ID: %s'),'complete':('Змеиная гора построена. ID: %s','Serpent Hill construction complete. ID: %s'),'height':('Мир слишком низок для целой Змеиной горы.','This world is too short for a complete Serpent Hill.')}
 for locale,idx in [('ru_ru',0),('en_us',1)]:
  values={'block.slavicmyths.'+name:pair[idx] for name,pair in NAMES.items()};values.update({'serpenthill195.'+name:pair[idx] for name,pair in pairs.items()})
  for name,ru,en in [('danger','Мор ранит путника','Corruption wounds a traveller'),('crystal','Гудит моровой кристалл','Mor crystal hums'),('brazier','Тлеет ритуальная жаровня','Ritual brazier smoulders'),('deep_ambient','Стон глубин горы','Deep hill ambience'),('raised','Поднимается Змеиная гора','Serpent Hill rises')]:values['subtitles.slavicmyths.serpenthill.'+name]=ru if idx==0 else en
  values['death.attack.serpent_hill_mor']=('%1$s поглощён Мором Змеиной горы' if idx==0 else '%1$s succumbed to Serpent Hill corruption')
  values['death.attack.serpent_hill_mor.player']=('%1$s поглощён Мором, спасаясь от %2$s' if idx==0 else '%1$s succumbed to corruption while fleeing %2$s')
  values['tales.entry.serpent_hill.title']=('Змеиная гора' if idx==0 else 'Serpent Hill')
  body=('Проклятый холм несёт древнюю Серпентинскую крепость. Нижний вход скрыт в склоне; верхние ворота ведут через двор и галереи. Во внутренних глубинах нужен существующий оберег от Мора или его действующая защита. Три головы на камне отмечают пока заблокированный путь; хранителя логова ещё нет.' if idx==0 else 'An ancient serpent fortress crowns the cursed hill. A lower cave entrance and upper gate lead through connected tunnels and galleries. Carry the existing Mor Ward or activate it before entering corrupted depths. A three-headed carving marks the sealed route; the future lair guardian is absent.')
  for stage in ['rumor','known','studied','completed','legend','observed']:values['tales.entry.serpent_hill.'+stage]=body
  merge(A/f'lang/{locale}.json',values)
def main():
 O.mkdir(parents=True,exist_ok=True);blocks();recipes();data();audio();language();write(O/'block-manifest.json',{'blocks':NAMES,'light_levels':dict(zip(NAMES,LIGHT)),'recipes':11,'sounds':5});print('HILL_ASSETS_PASS 11 blocks, 11 recipes, 5 original OGG')
if __name__=='__main__':
 main()
 from gorynych_assets import data as gorynych_data
 gorynych_data()
