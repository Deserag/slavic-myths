"""Scoped, reproducible resources for the 24 October test feedback."""
import json
from pathlib import Path
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parents[1]/'src/main/resources'
def put(path,data):
 p=R/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def recipe(name,data):put(Path('data/slavicmyths/recipe')/(name+'.json'),data)
def shaped(pattern,key,result,count=1,typ='minecraft:crafting_shaped'):
 return dict(type=typ,pattern=pattern,key=key,result=dict(id='slavicmyths:'+result,count=count))
def item(s):return {'item':s if ':' in s else 'slavicmyths:'+s}
for name in ['chekan','battle_axe','weapon_part_iron_spearhead']:
 p=Path('data/slavicmyths/recipe')/(name+'.json');d=json.loads((R/p).read_text());d['type']='slavicmyths:armorer_shaped';put(p,d)
recipe('chekan',shaped(['III',' S ',' S '],{'I':item('minecraft:iron_ingot'),'S':item('minecraft:stick')},'chekan',typ='slavicmyths:armorer_shaped'))
# Existing counted processes also overlapped for large input stacks. Move the same
# water bucket to the existing catalyst slot: quantities, duration and returns stay intact.
for name in ['dark_beer_wort','hopped_beer_wort']:
 p=Path('data/slavicmyths/recipe/vat')/(name+'.json');d=json.loads((R/p).read_text())
 d['ingredients']=[v for v in d['ingredients'] if v['ingredient']!=item('minecraft:water_bucket')]
 d['catalyst']=item('minecraft:water_bucket');put(p,d)
p=Path('data/slavicmyths/recipe/tool_rune_miner_iv.json');d=json.loads((R/p).read_text())
if d['inputs'][1]['ingredient']==item('perunite_dust'):d['inputs'][1],d['inputs'][2]=d['inputs'][2],d['inputs'][1]
put(p,d)
recipe('armorer_table',shaped(['III','PCP','P P'],{'I':item('minecraft:iron_ingot'),'P':{'tag':'minecraft:planks'},'C':item('minecraft:crafting_table')},'armorer_table'))
recipe('birch_bark',dict(type='minecraft:crafting_shapeless',ingredients=[item('minecraft:birch_log')]*2,result=dict(id='slavicmyths:birch_bark',count=8)))
recipe('carved_oak_block',shaped(['P  ',' P ','  P'],{'P':item('minecraft:oak_planks')},'carved_oak_block',3))
recipe('carved_birch_planks',shaped(['P  ',' P ','  P'],{'P':item('minecraft:birch_planks')},'carved_birch_planks',3))
recipe('haystack',dict(type='minecraft:crafting_shapeless',ingredients=[item('minecraft:hay_block')]*2,result=dict(id='slavicmyths:haystack',count=2)))
recipe('wall_shelf',shaped(['S S','PPP'],{'P':{'tag':'minecraft:planks'},'S':item('minecraft:stick')},'wall_shelf',2))
recipe('storage_shelf',shaped(['PSP','SPS','PSP'],{'P':{'tag':'minecraft:planks'},'S':item('minecraft:stick')},'storage_shelf'))
recipe('runic_anvil',shaped(['SSS',' I ','BBB'],{'S':item('silver_block'),'I':item('minecraft:iron_ingot'),'B':item('minecraft:iron_block')},'runic_anvil'))
recipe('silver_block',shaped(['SSS']*3,{'S':item('silver_ingot')},'silver_block'))
recipe('silver_block_unpack',dict(type='minecraft:crafting_shapeless',ingredients=[item('silver_block')],result=dict(id='slavicmyths:silver_ingot',count=9)))
put(Path('assets/slavicmyths/blockstates/silver_block.json'),{'variants':{'':{'model':'slavicmyths:block/silver_block'}}})
texture=Image.new('RGB',(16,16),'#c3d1db');pixels=texture.load()
for y in range(16):
 for x in range(16):
  noise=((x*7+y*13)%5)-2;pixels[x,y]=(195+noise,209+noise,219+noise)
pen=ImageDraw.Draw(texture);pen.rectangle((0,0,15,15),outline='#829aa9');pen.line((1,1,14,1),fill='#f0f6f8');pen.line((1,1,1,14),fill='#e1ecf2');pen.line((2,13,13,13),fill='#a7bccb');pen.line((13,3,13,12),fill='#b0c4d2')
target=R/'assets/slavicmyths/textures/block/silver_block.png';target.parent.mkdir(parents=True,exist_ok=True);texture.save(target)
put(Path('assets/slavicmyths/models/block/silver_block.json'),{'parent':'minecraft:block/cube_all','textures':{'all':'slavicmyths:block/silver_block'}})
put(Path('assets/slavicmyths/models/item/silver_block.json'),{'parent':'slavicmyths:block/silver_block'})
put(Path('data/slavicmyths/loot_table/blocks/silver_block.json'),{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:silver_block'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
for tag in ['minecraft/mineable/pickaxe','minecraft/needs_iron_tool']:
 ns,name=tag.split('/',1);put(Path('data')/ns/'tags/block'/(name+'.json'),{'replace':False,'values':['slavicmyths:silver_block']}) if not (R/'data'/ns/'tags/block'/(name+'.json')).exists() else None
for name in ['raspberry','blueberry','blackcurrant','lingonberry','cranberry']:
 recipe(name+'_sapling_from_berries',dict(type='minecraft:crafting_shapeless',ingredients=[item(name),item('minecraft:dirt')],result=dict(id='slavicmyths:'+name+'_sapling')))
for tag in ['mineable/pickaxe','needs_iron_tool']:
 p=Path('data/minecraft/tags/block')/(tag+'.json');d=json.loads((R/p).read_text());
 if 'slavicmyths:silver_block' not in d['values']:d['values'].append('slavicmyths:silver_block')
 put(p,d)
texts={
 'guide.brewing.keg':('Малый бочонок: используйте разлитый напиток для наполнения (до 8 порций); пустую кружку/бутылку — для розлива. Это хранение, качество не меняется. Разные напитки/качество не смешиваются.','Small keg: use poured drinks to fill (up to 8 servings); use an empty mug/bottle to pour. This is storage; quality does not change. Different drinks/qualities cannot be mixed.'),
 'guide.yaga.exchange':('Обмен у Яги. Требуется: %s; материалы расходуются из инвентаря.','Trade with Yaga. Requires: %s; materials are consumed from inventory.'),
 'guide.yaga.brew':('Варка в котле Яги. Требуется: %s; положите ингредиенты в указанном порядке и освободите выход.','Brew at Yaga’s cauldron. Requires: %s; insert ingredients in the shown order and clear the output.'),
 'guide.special.title':('Особые рецепты верстака','Special crafting recipes'),
 'guide.special.winters':('Верстак 3×3, строго по схеме. Создаётся оригинал амулета «Две зимы».','Crafting table 3×3, exact pattern. Creates an original Two Winters amulet.'),
 'guide.special.winters_copy':('Строгая схема 3×3. В центре нужен зарегистрированный в этом мире оригинал амулета, а не копия. Оригинал сохраняется.','Exact 3×3 pattern. The center must contain an original amulet registered in this world, not a copy. The original is retained.'),
 'guide.special.armor':('Верстак: по 1 предмету в каждой из 6 ячеек. Сохраняются компоненты и относительный износ исходной брони.','Crafting table: one item in each of 6 slots. Retains components and relative armor wear.'),
 'guide.special.ward':('Верстак шириной 3 ячейки. Три разных компонента дают оберег.','Crafting table at least 3 slots wide. Combine the three distinct components for a ward.'),
 'research.yaga.searching':('Ищу безопасное место для дома Яги. Книга обновится после проверки.','Searching for a safe home for Yaga. Your book will update after verification.'),
 'research.yaga.found':('Дом Яги найден. В книге отмечена приблизительная область.','Yaga’s home found. The book now shows its approximate area.'),
 'guide.industry.conditions':('Нужно топливо (например, уголь), свободный выход и загруженный чанк. Горн расходует также флюс.','Requires fuel (such as coal), output space and a loaded chunk. The refining hearth also consumes flux.'),
 'research.chud.status.0':('Поговорите с поручителем о Чуди.','Ask the quest giver about the Chud.'),
 'research.chud.status.1':('Поручение принято. Исследуйте область на карте.','Assignment active. Explore the area shown on the map.'),
 'research.chud.status.2':('Поселение посещено. Вернитесь к поручителю.','Settlement visited. Return to the quest giver.'),
 'research.chud.status.3':('Награда получена. Можно взять новое поручение.','Reward claimed. A new assignment is available.'),
 'tales.entry.chud_research_yaga.title':('Чудь: поручение Яги','Chud: Yaga’s assignment'),
 'tales.entry.chud_research_bayun.title':('Чудь: поручение Баюна','Chud: Bayun’s assignment'),
 'guide.process.title':('Сушка и обработка льна','Drying and linen production'),
 'guide.process.drying':('Положите предмет на сушильную стойку и дождитесь обработки в загруженном чанке. Заберите готовый предмет.','Put the item on a drying rack and wait in a loaded chunk. Collect the finished item.'),
 'guide.process.breaker':('Вложите 4 стебля льна; трижды обработайте мялку пустой рукой. Получите 4 волокна.','Insert 4 flax stalks; work the breaker three times with an empty hand to get 4 fibers.'),
 'guide.process.textile':('Вложите материал и дождитесь обработки в загруженном чанке. Пустой рукой заберите результат; оставьте место для выхода.','Insert the material and wait in a loaded chunk. Collect the output with an empty hand; leave room for output.'),
 'guide.runes.fragments':('Осколок души: 35% с исходного упыря в кургане; призванные замены не подходят. Остальные способы преобразования смотрите в рецептах рунной наковальни.','Soul fragment: 35% drop from an original kurgan upyr; summoned replacements do not count. See the runic anvil recipes for conversions.'),
 'guide.brewing.title':('Пивоварение: полный цикл','Brewing: the full process'),
 'guide.brewing.time':('Обработка: %s с (20 тиков/с)','Processing: %s s (20 ticks/s)'),
 'guide.brewing.vat':('Чан: ингредиенты в 4 входных ячейки, катализатор справа. Освободите выход и ячейки возврата сосудов. Обрабатывается только в загруженном чанке.','Vat: use the four input slots and catalyst slot. Keep output and container return slots free. Processing requires a loaded chunk.'),
 'guide.brewing.press':('Поместите 4 яблока или ягоды в пресс. Трижды нажмите пустой рукой, затем заберите сок/мезгу и жмых пустой рукой.','Insert 4 apples or berries into the press. Use an empty hand three times; then collect juice/mash and pomace with an empty hand.'),
 'guide.brewing.young':('1 партия + пустые сосуды → 8 порций. Опустошите чан/бочку, примените сусло или муст. До 120 с — молодой напиток. Для розлива примените пустую кружку или тёмную бутылку.','1 batch + empty vessels → 8 servings. Empty the vat/barrel, then use wort or must. Before 120 s: young. Use an empty mug or dark bottle to pour.'),
 'guide.brewing.ready':('Брожение: 120–419 с в загруженном чанке. 1 партия даёт 8 порций. Розлив пустой кружкой/тёмной бутылкой. Кувшин вмещает до 4 порций кваса, медовухи или сидра.','Ferment for 120–419 s in a loaded chunk. Each batch gives 8 servings. Pour with empty mugs/dark bottles. A pitcher holds up to 4 servings of kvass, mead or cider.'),
 'guide.brewing.aged':('Выдержка: 420–599 с. С 600 с напиток испорчен! Разливайте вовремя. Малый бочонок хранит до 8 совместимых порций; качество и напитки смешивать нельзя.','Age for 420–599 s. At 600 s the brew spoils! Pour in time. A small keg stores up to 8 matching servings; different drinks and qualities cannot be mixed.'),
 'guide.brewing.overview':('Сначала солод из ячменя в чане; рецепты JEI показывают воду, катализатор и возврат сосудов. Затем сусло/муст → пустой чан или деревянная бочка → брожение → розлив. Пресс даёт сок и мезгу. Малый бочонок — хранилище уже разлитого напитка. Время идёт только в загруженных чанках.','First malt barley in a vat; JEI shows water, catalysts and returned containers. Next: wort/must → empty vat or wooden barrel → fermentation → pouring. The press produces juice and mash. A small keg stores poured drinks. Time advances only in loaded chunks.'),
 'guide.farming':('Злаки, овощи и лён сажайте на пашню при свете. Семена ищите в траве; зрелые культуры возвращают посадочный материал. Хмелю и малине оставьте свободный блок сверху. Кусты растут на земле/дёрне из тега berry_soil; для урожая дождитесь последней стадии и нажмите ПКМ. Костная мука ускоряет незрелые растения; зрелое растение сначала соберите. Черенок любой из пяти ягод можно изготовить из ягоды и земли, а также получить ножницами с куста. Яблоки собирают с плодоносящих листьев ПКМ; декоративные листья не плодоносят.','Plant grains, vegetables and flax on lit farmland. Find seeds in grass; mature crops return planting material. Leave space above hops and raspberry. Bushes grow on berry_soil dirt/grass blocks; right-click at the final stage to harvest. Bone meal advances immature plants; harvest ripe plants first. Craft any of the five berry saplings from its berry and dirt, or shear a bush. Right-click fruiting apple leaves to harvest; decorative leaves do not bear fruit.'),
 'guide.runes':('Рунная наковальня: 3 блока серебра сверху, железный слиток в центре, 3 железных блока снизу. Серебряный блок = 9 слитков (обратимый рецепт). Заготовки рун и резец показаны в JEI; резец изнашивается. Материал заготовки определяет её свойства. Серебро добывают из руды, древние знаки — при исследовании древностей; души и осколки связаны с противниками и соответствующими рецептами. В JEI откройте применение компонента и категорию наковальни. Установка рун проверяет совместимость и доступные гнёзда на сервере. Редкие обмены Яги открываются по её поручениям.','Runic anvil: 3 silver blocks on top, an iron ingot in the middle, 3 iron blocks below. A silver block packs 9 ingots reversibly. JEI lists rune blanks and chisels; chisels lose durability. Blank material affects its properties. Mine silver ore; explore ancient sites for signs. Souls and fragments come from enemies and their related recipes. Use JEI component usages and the anvil category. Installation checks compatible runes and sockets on the server. Yaga unlocks rare exchanges through her quests.'),
 'guide.workshops':('Оружейный стол из любых досок служит для чекана и оружейных рецептов 4×4. Кухонный стол показывает блюда и инструмент в JEI. Домница и рафинировочный горн имеют отдельные категории с временем обработки; сушильная стойка работает только в загруженном чанке. Жернов — декоративное рабочее место: мука изготовляется существующими рецептами. Точные ингредиенты берутся из загруженных рецептов сервера.','Use any planks for an armorer table, which crafts chekans and 4×4 equipment recipes. JEI shows kitchen dishes and tools. Bloomery and refining hearth categories show processing times; drying racks require loaded chunks. The millstone is a decorative workstation: flour uses existing crafting recipes. Exact ingredients come from loaded server recipes.'),
 'research.chud.searching':('Ищу подходящее поселение Чуди. Результат появится в книге.','Searching for a suitable Chud settlement. The result will appear in your book.'),
 'research.chud.unavailable':('Безопасная цель пока не найдена. Поручение не выдано; попробуйте из другой области.','No safe target found yet. No quest assigned; try from another region.'),
 'research.chud.assigned':('Исследуйте указанную в книге область Чуди и вернитесь к поручителю.','Explore the Chud area marked in your book, then return to the quest giver.'),
 'research.chud.visited':('Поселение Чуди исследовано. Вернитесь за наградой.','Chud settlement explored. Return for your reward.'),
 'research.chud.reward':('Исследование принято: получен древний знак. Следующий разговор даст новое поручение.','Research received: ancient sign awarded. Speak again for a new assignment.'),
 'research.chud.dialogue':('О поселениях Чуди расскажет твоя книга. Исследуй цель и вернись ко мне.','Your book will guide you to the Chud. Explore the target and return to me.'),
 'bayun.dialogue.research':('Исследовать Чудь','Research the Chud'), 'yaga.research':('О Чуди','About the Chud'),
 'block.slavicmyths.silver_block':('Серебряный блок','Block of Silver'),
 'tales.entry.yaga_search.title':('Дорога к Бабе-яге','The road to Baba Yaga'),
 'tales.entry.chud_research.title':('Исследование Чуди','Chud research'),
 'tales.entry.guide_brewing.title':('От солода до кружки','From malt to mug'),
 'tales.entry.guide_farming.title':('Посев, ягоды и саженцы','Crops, berries and saplings'),
 'tales.entry.guide_runes.title':('Материалы и руны','Materials and runes'),
 'tales.entry.guide_workshops.title':('Рабочие места','Workstations'),
}
chapters={
 'guide_brewing':('crafts','skills','guide.brewing.overview','fermentation_vat',1),
 'guide_farming':('crafts','skills','guide.farming','blackcurrant_sapling',1),
 'guide_runes':('crafts','skills','guide.runes','runic_anvil',1),
 'guide_workshops':('crafts','skills','guide.workshops','armorer_table',1),
 'yaga_search':('people','home',None,'lore_book',0),
 'chud_research':('quests','skills',None,'ancient_sign',1),
 'chud_research_yaga':('quests','skills',None,'ancient_sign',0),
 'chud_research_bayun':('quests','skills',None,'ancient_sign',0)}
for name,(part,chapter,text,icon,initial) in chapters.items():
 subjects=[]
 if name=='guide_brewing':subjects=['recipe/slavicmyths:vat/'+p.stem for p in (R/'data/slavicmyths/recipe/vat').glob('*.json')]
 if name=='guide_runes':subjects=['recipe/slavicmyths:runic_anvil','recipe/slavicmyths:silver_block']
 if name=='guide_workshops':subjects=['recipe/slavicmyths:armorer_table','recipe/slavicmyths:chekan']
 if name=='guide_farming':subjects=['recipe/slavicmyths:'+n+'_sapling_from_berries' for n in ['raspberry','blueberry','blackcurrant','lingonberry','cranberry']]
 put(Path('data/slavicmyths/tales')/(name+'.json'),dict(part=part,chapter=chapter,section='catalogue',title='tales.entry.'+name+'.title',icon='slavicmyths:'+icon,initial=initial,subjects=subjects,advancements=[],links=[]))
for index,lang in enumerate(['ru_ru','en_us']):
 p=Path('assets/slavicmyths/lang')/(lang+'.json');d=json.loads((R/p).read_text(encoding='utf-8'));d.update({k:v[index] for k,v in texts.items()})
 for name,(_,_,text,_,_) in chapters.items():
  body=texts[text][index] if text else (('Победа над Лихом, Тугарином или Соловьём открывает поиск. Откройте книгу в обычном мире: карта покажет приблизительную область реального дома Яги. Старые достижения учитываются; повторная победа не нужна. Существующий дом мира сохраняется.' if name=='yaga_search' else 'Яга и Баюн дают отдельные задания исследовать поселения Чуди. Поговорите о Чуди; область поиска появится на карте этой записи. Посетите настоящее поселение и вернитесь к тому, кто дал поручение. Награда — древний знак. Исследованные цели не назначаются повторно.') if index==0 else ('Defeat Likho, Tugarin or Nightingale to unlock this search. Open the book in the Overworld for the approximate area of Yaga’s real home. Old advancements count; no repeat kill is needed. An existing world home is preserved.' if name=='yaga_search' else 'Yaga and Bayun offer separate Chud research assignments. Ask about the Chud; this entry’s map shows the search area. Visit the actual settlement and return to your quest giver for an ancient sign. Explored targets are excluded from future assignments.'))
  if name=='guide_brewing':body+='\n\n'+texts['guide.brewing.press'][index]+'\n\n'+texts['guide.brewing.ready'][index]+'\n\n'+texts['guide.brewing.aged'][index]
  for suffix in ['rumor','discovered','studied','verified']:d['tales.entry.'+name+'.'+suffix]=body
 put(p,d)
