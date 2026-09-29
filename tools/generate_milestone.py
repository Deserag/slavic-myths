"""0.4 data and original pixel patterns, extending the existing resource generator."""
import json, struct, zlib
from pathlib import Path

def generate(root):
    res = root/'src/main/resources'
    def js(path, value):
        p = res/path; p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text(json.dumps(value, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
    names = {'hearth': ('Домовой очаг','Household Hearth'), 'ancient_idol': ('Древний идол','Ancient Idol'),
             'altar': ('Жертвенник','Offering Altar'), 'ancient_coin': ('Древняя монета','Ancient Coin'),
             'idol_fragment': ('Осколок идола','Idol Fragment'), 'ancient_sign': ('Древний знак','Ancient Sign'),
             'lore_book': ('Книга сказаний','Book of Tales')}
    blocks = {'hearth','ancient_idol','altar'}
    texts = {}
    def tr(key, ru, en): texts[key] = (ru,en)
    tr('jei.slavicmyths.rituals','Ритуалы','Rituals')
    for name, pair in names.items(): texts[('block' if name in blocks else 'item')+'.slavicmyths.'+name] = pair
    tr('tooltip.slavicmyths.ancient_sign','Знак первого обряда. Пока хранит память, а не силу.','A token of the first rite. It holds memory, not yet power.')
    messages = {
      'accepted': ('Подношение принято. Дайте Домовому минуту отдыха.','Offering accepted. Let the spirit rest for a minute.'),
      'wait': ('Домовой ещё сыт. Следующее подношение — через минуту после прошлого.','Still content. Offer again one minute after the last gift.'),
      'refused': ('Домовой отказывается: слишком много обид.','The spirit refuses: too many wounds.'),
      'hostile': ('Домовой сердит и избегает вас.','The spirit is angry and avoids you.'),
      'wary': ('Домовой недоверчив.','The spirit is wary.'),
      'neutral': ('Домовой присматривается. Угостите хлебом, молоком, мёдом или печеньем.','The spirit watches. Offer bread, milk, honey or a cookie.'),
      'friendly': ('Домовой вам доверяет. Коснитесь очага рядом с ним.','The spirit trusts you. Use a nearby hearth.'),
      'devoted': ('Домовой предан вам. Дома можно попросить благословение пустой рукой.','The spirit is devoted. Ask for a blessing at home with an empty hand.'),
      'bound': ('Домовой запомнил очаг и останется у дома.','The spirit remembers this hearth and will stay nearby.'),
      'no_friend': ('В пределах 8 блоков нет свободного дружелюбного Домового. Нужно четыре подношения.','No eligible friendly spirit within 8 blocks. Earn trust with four offerings.'),
      'blessing': ('Благословение дома: короткое восстановление.','Blessing of home: brief regeneration.')}
    for key, pair in messages.items(): texts['domovoy.slavicmyths.'+key] = pair
    tr('ritual.slavicmyths.next','Нужен компонент: %s. Порядок: полынь → свиток → оберег.','Required: %s. Order: wormwood → scroll → charm.')
    tr('ritual.slavicmyths.accepted','Принято. Следующий компонент: %s.','Accepted. Next ingredient: %s.')
    tr('ritual.slavicmyths.complete','Обряд завершён. Получен Древний знак.','Rite complete. You received an Ancient Sign.')
    tr('shrine.slavicmyths.discovered','Древнее капище. В Книге сказаний открылась запись.','Ancient shrine. An entry is now available in the Book of Tales.')
    tr('book.slavicmyths.locked','Не открыто','Undiscovered')
    tr('book.slavicmyths.previous','Назад','Previous')
    tr('book.slavicmyths.next','Далее','Next')
    entries = {
    'intro': ('Первые следы','First traces',
        'Под корой привычного мира живут старые сказания. Из книги, берестяного свитка и льняной нити создайте эту книгу.\n\nДухов узнают при взаимодействии. Древняя монета или осколок открывают рассказ о капище; можно также коснуться идола. Используйте жертвенник, чтобы узнать порядок обряда. После завершения откроется его история.\n\nСвиток: две бересты и нить. Оберег: льняная нить, полынь и золотой самородок. Рецепты доступны в обычной книге рецептов и JEI.',
        'Old tales live beneath the familiar world. Craft this book from a book, birch bark scroll and linen thread.\n\nInteract with spirits to learn about them. An ancient coin or fragment reveals the shrine entry; touching an idol also works. Use an altar to learn the rite. Completing it reveals its tale.\n\nScroll: two birch bark and string. Charm: linen thread, wormwood and a gold nugget. Standard recipes appear in the recipe book and JEI.'),
    'domovoy': ('Духи: Домовой','Spirits: Domovoy',
        'Малый хранитель любит мирный дом. Хлеб, молоко, мёд и печенье укрепляют доверие; между дарами нужна минута. Четыре дара приносят дружбу. Удары оставляют обиду, повторные — более глубокую. Сильно обиженный дух откажется от еды.\n\nПоставьте очаг (костёр, четыре булыжника и четыре бересты). Когда друг рядом, нажмите на очаг: он запомнит дом. Привязать чужого хранителя нельзя. Восемь даров дают преданность: пустой рукой у очага можно попросить пять секунд восстановления, раз в десять минут.\n\nОбраз вдохновлён фольклором; правила доверия — игровая интерпретация.',
        'A small guardian values a peaceful home. Bread, milk, honey and cookies build trust, with a minute between gifts. Four gifts earn friendship. Attacks cause resentment; repeated blows hurt trust more. An angry spirit refuses food.\n\nBuild a hearth from a campfire, four cobblestone and four birch bark. Use it with a friend nearby to bind a home. Another player cannot claim your guardian. Eight gifts earn devotion: ask with an empty hand near home for five seconds of regeneration, once per ten minutes.\n\nInspired by folklore; trust rules are a game interpretation.'),
    'leshy': ('Духи: Леший','Spirits: Leshy',
        'Лес отзывается голосом прежде, чем покажет хозяина. Днём Леший сторонится путников; ночью не подходите вплотную. Не бейте его без нужды.\n\nЛесная атака иногда замедляет на три секунды. Раненый хранитель может исчезнуть неподалёку, но не чаще раза в тридцать секунд. Сердцевина — редкая добыча, не обязательная цена первого обряда.\n\nИгровые способности не являются утверждением о едином фольклорном каноне.',
        'The forest may speak before its guardian appears. By day the Leshy avoids travellers; do not approach too closely at night. Avoid needless violence.\n\nIts blows sometimes slow you for three seconds. A wounded guardian may vanish nearby, at most once in thirty seconds. Its heart is rare loot, not a required cost of the first rite.\n\nThese abilities are game interpretations, not a claim of a single folklore canon.'),
    'shrine': ('Места: капище','Places: shrine',
        'Разрушенный круг камней, старое дерево и безымянный идол. Такие места редко встречаются на ровной земле равнин, лесов и тайги Верхнего мира, только в новых чанках.\n\nСундук хранит остатки даров: травы, бересту, монеты и осколки. Жертвенник можно забрать домой. Идол пока не посвящён конкретному богу.\n\nОблик и состав этого места созданы для игры, а не как историческая реконструкция.',
        'A broken stone circle, an old tree and a nameless idol. Rarely found on flat ground in Overworld plains, forests and taiga, in newly generated chunks.\n\nA chest holds remnants of offerings: herbs, bark, coins and fragments. The altar can be taken home. The idol is not yet dedicated to a specific deity.\n\nThis place is a game design, not a historical reconstruction.'),
    'altar': ('Обряды: жертвенник','Rites: altar',
        'Жертвенник принимает по одному предмету правой кнопкой. Порядок первого обряда: полынь, берестяной свиток, оберег. Ошибочный предмет не расходуется. Пустая рука подскажет следующий шаг.\n\nШаг сохраняется при выходе из мира. Жертвенник общий: другой игрок может продолжить обряд. Получает знак тот, кто завершил его. Разрушение жертвенника отменяет незавершённый обряд; уже отданные дары не возвращаются. Тот же обряд доступен снова сразу после завершения.',
        'Right-click to offer one item at a time. The first rite requires wormwood, a birch bark scroll, then a warding charm. Wrong items are not consumed. An empty hand reveals the next step.\n\nProgress survives world reloads. Altars are shared: another player may continue the rite. The finisher receives the sign. Breaking the altar cancels progress; committed offerings are not returned. A completed rite can be repeated immediately.'),
    'ritual': ('Обряды: первый знак','Rites: first sign',
        'Горечь травы, память бересты, узел оберега. Три дара соединены в Древнем знаке. Он подтверждает первый шаг к старым знаниям и пока не даёт боевой силы.\n\nЭтот обряд — авторская игровая интерпретация. Монеты и осколки сохраняйте для будущих историй: торговли и новых обрядов пока нет.',
        'Bitter herbs, the memory of bark, the knot of a charm. Three offerings unite in an Ancient Sign. It marks a first step into old knowledge and grants no combat power yet.\n\nThis rite is an original game interpretation. Keep coins and fragments for future tales: trading and further rites are not implemented yet.'),
    'paths': ('Предания о путях','Tales of paths',
        'Говорят о пути силы — Дружиннике, пути знания — Волхве и пути духов — Ведуне. Это только намёк на будущие игровые роли. Выбор класса, мана, способности и специализации пока не существуют.\n\nСлавянские предания разнообразны: фольклор, поздние представления и игровая фантазия не равны историческим свидетельствам.',
        'Tales speak of strength: the Druzhinnik; knowledge: the Volkhv; and spirits: the Vedun. These are hints of future game roles. Class selection, mana, abilities and specializations do not exist yet.\n\nSlavic traditions are varied: folklore, later ideas and game fantasy are not equivalent to historical evidence.')}
    for key, (ru,en,rut,ent) in entries.items():
        tr('book.slavicmyths.'+key+'.title',ru,en); tr('book.slavicmyths.'+key+'.text',rut,ent)
    advances = {
      'offer_domovoy': ('meet_domovoy','minecraft:bread','Хлеб да соль','Bread and salt','Угостите Домового.','Offer food to a Domovoy.'),
      'friend_domovoy': ('offer_domovoy','slavicmyths:warding_charm','Доверие малого хранителя','A guardian’s trust','Заслужите дружбу четырьмя подношениями.','Earn friendship with four offerings.'),
      'bind_hearth': ('friend_domovoy','slavicmyths:hearth','Тепло родного дома','Warmth of home','Привяжите дружелюбного Домового к очагу.','Bind a friendly Domovoy to a hearth.'),
      'defeat_leshy': ('meet_leshy','slavicmyths:leshy_heart','Лес помнит','The forest remembers','Победите Лешего в столкновении.','Defeat a Leshy in combat.'),
      'ancient_find': ('root','slavicmyths:ancient_coin','Отголосок старины','An ancient echo','Найдите древнюю монету или осколок идола.','Find an ancient coin or idol fragment.'),
      'shrine': ('ancient_find','slavicmyths:ancient_idol','Камни помнят','Stones remember','Коснитесь идола или прочтите книгу с древней находкой.','Touch an idol or read the book after an ancient find.'),
      'altar': ('shrine','slavicmyths:altar','Место для дара','A place for offerings','Используйте жертвенник: полынь, свиток, оберег.','Use an altar: wormwood, scroll, charm.'),
      'first_ritual': ('altar','slavicmyths:ancient_sign','Первый обряд','The first rite','Завершите последовательность трёх даров.','Complete the three offerings in order.'),
      'ancient_sign': ('first_ritual','slavicmyths:ancient_sign','Память обряда','Memory of a rite','Получите Древний знак.','Obtain an Ancient Sign.'),
      'lore_book': ('birch_bark_scroll','slavicmyths:lore_book','Сказания оживают','Tales awaken','Создайте или откройте Книгу сказаний.','Craft or open the Book of Tales.'),
      'lore_keeper': ('lore_book','slavicmyths:lore_book','Хранитель сказаний','Keeper of tales','Откройте книгу, узнав не менее трёх записей.','Open the book after discovering at least three entries.')}
    for aid,(parent,icon,ru,en,rud,end) in advances.items():
        tr('advancements.slavicmyths.'+aid+'.title',ru,en); tr('advancements.slavicmyths.'+aid+'.description',rud,end)
        criteria = {'done': {'trigger':'minecraft:impossible'}}
        if aid in ('ancient_sign','lore_book'):
            criteria = {'done': {'trigger':'minecraft:inventory_changed','conditions':{'items':[{'item':'slavicmyths:'+aid}]}}}
        if aid == 'ancient_find':
            criteria = {i:{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'item':'slavicmyths:'+i}]}} for i in ('ancient_coin','idol_fragment')}
        if aid == 'defeat_leshy': criteria = {'done':{'trigger':'minecraft:player_killed_entity','conditions':{'entity':{'type':'slavicmyths:leshy'}}}}
        obj = {'parent':'slavicmyths:'+parent,'display':{'icon':{'item':icon},'title':{'translate':'advancements.slavicmyths.'+aid+'.title'},'description':{'translate':'advancements.slavicmyths.'+aid+'.description'},'frame':'task','show_toast':True,'announce_to_chat':False,'hidden':False},'criteria':criteria}
        if aid == 'ancient_find': obj['requirements'] = [list(criteria)]
        js('data/slavicmyths/advancements/'+aid+'.json',obj)
    p = res/'data/slavicmyths/advancements/leshy_heart.json'; obj = json.loads(p.read_text(encoding='utf-8')); obj['parent']='slavicmyths:defeat_leshy'; js(str(p.relative_to(res)),obj)
    for lang, index in [('ru_ru',0),('en_us',1)]:
        p = res/f'assets/slavicmyths/lang/{lang}.json'; data = json.loads(p.read_text(encoding='utf-8'))
        data.update({key: pair[index] for key,pair in texts.items()}); js(str(p.relative_to(res)),data)
    # Original 16x16 textures, matching the existing hand-pixel asset pipeline.
    palette = {'.':(0,0,0,0),'o':(48,35,27,255),'b':(109,72,45,255),'t':(170,124,63,255),'g':(93,98,83,255),'s':(58,65,59,255),'h':(146,149,121,255),'y':(229,179,66,255),'w':(235,216,168,255),'r':(156,58,30,255)}
    sprites = {
    'ancient_coin':['................','......oooo......','....oottttoo....','...otyyyyyyto...','..otyyttttyyto..','..otytwwwtyyto..','.otyytwtwttyyto.','.otyyttwtttyyto.','.otyytwtwttyyto.','.otyyttwtttyyto.','..otyyttttyyto..','..otyyyyyyyyto..','...otttttttto...','....oooooooo....','................','................'],
    'idol_fragment':['................','.......oo.......','......obbo......','.....obttbo.....','....obtttbbo....','...obttwttbo....','..obttwwttbbo...','..obttowtttbo...','...obtotttbbo...','....obttttbo....','...obtttbbo.....','....obtbbo......','.....obbo.......','......oo........','................','................'],
    'ancient_sign':['................','......oooo......','.....oyyyyo.....','....oytwwtyo....','...oytwyywtyo...','..oytwywwywtyo..','..oytwywwywtyo..','..oytwyyyywtyo..','..oytwywwywtyo..','...oytwyywtyo...','....oytwwtyo....','.....oyyyyo.....','......orro......','......orrro.....','.......orro.....','................'],
    'lore_book':['................','..ooooooooooo...','.obbbbbbbbbbro..','.obtttttttbwro..','.obtoootttbwro..','.obtowotttbwro..','.obtowotttbwro..','.obtoootttbwro..','.obtttttttbwro..','.obtttttttbwro..','.obtttttttbwro..','.obbbbbbbbbbro..','.owwwwwwwwwooo..','..ooooooooooo...','................','................']}
    for name in blocks:
        rows=[]
        for y in range(16):
            row=''
            for x in range(16):
                if name=='ancient_idol':
                    c='b' if (x*7+y*3)%11<3 else 't'
                    if y in (4,5) and x in (3,4,11,12): c='o'
                    if y in (10,11) and 4<=x<=11: c='o'
                    if x in (0,15): c='o'
                else:
                    c='s' if y%5==0 or (x+(y//5)*4)%8==0 else ('g' if (x*3+y)%7 else 'h')
                    if name=='hearth' and 4<=x<=11 and 8<=y<=13: c='y' if (x+y)%3==0 else 'r'
                    if name=='altar' and y in (2,3): c='r' if x%3 else 't'
                row+=c
            rows.append(row)
        sprites[name]=rows
    def chunk(kind,data): return struct.pack('!I',len(data))+kind+data+struct.pack('!I',zlib.crc32(kind+data)&0xffffffff)
    for name,rows in sprites.items():
        assert len(rows)==16 and all(len(r)==16 for r in rows), name
        raw=b''.join(b'\0'+bytes(c for p in row for c in palette[p]) for row in rows)
        png=b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('!IIBBBBB',16,16,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw))+chunk(b'IEND',b'')
        folder='block' if name in blocks else 'item'; p=res/f'assets/slavicmyths/textures/{folder}/{name}.png'; p.write_bytes(png)
        js(f'assets/slavicmyths/models/item/{name}.json', {'parent':'slavicmyths:block/'+name} if name in blocks else {'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+name}})
    for name in blocks:
        model={'parent':'minecraft:block/cube_all','textures':{'all':'slavicmyths:block/'+name}}
        if name=='altar':
            model={'textures':{'stone':'slavicmyths:block/altar','particle':'slavicmyths:block/altar'},'elements':[{'from':start,'to':end,'faces':{face:{'texture':'#stone'} for face in ('up','down','north','south','west','east')}} for start,end in [([3,0,3],[13,9,13]),([1,9,1],[15,12,15])]]}
        js(f'assets/slavicmyths/models/block/{name}.json',model)
        js(f'assets/slavicmyths/blockstates/{name}.json',{'variants':{'':{'model':'slavicmyths:block/'+name}}})
        js(f'data/slavicmyths/loot_tables/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
    recipes={
      'lore_book':{'type':'minecraft:crafting_shapeless','ingredients':[{'item':i} for i in ('minecraft:book','slavicmyths:birch_bark_scroll','slavicmyths:linen_thread')]},
      'hearth':{'type':'minecraft:crafting_shaped','pattern':['BCB','CFC','BCB'],'key':{'B':{'item':'slavicmyths:birch_bark'},'C':{'item':'minecraft:cobblestone'},'F':{'item':'minecraft:campfire'}}}}
    for name,recipe in recipes.items():
        recipe['result']={'item':'slavicmyths:'+name}; js(f'data/slavicmyths/recipes/{name}.json',recipe)
        js(f'data/slavicmyths/advancements/recipes/{name}.json',{'parent':'minecraft:recipes/root','criteria':{'has_bark':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'item':'slavicmyths:birch_bark'}]}},'unlocked':{'trigger':'minecraft:recipe_unlocked','conditions':{'recipe':'slavicmyths:'+name}}},'requirements':[['has_bark','unlocked']],'rewards':{'recipes':['slavicmyths:'+name]}})
    loot=[('minecraft:bread',10,1,3),('minecraft:coal',8,1,3),('minecraft:iron_nugget',8,2,6),('minecraft:bone',8,1,3),('minecraft:string',8,1,3),('slavicmyths:wormwood',8,1,2),('slavicmyths:flax',8,1,2),('slavicmyths:birch_bark',8,1,3),('slavicmyths:birch_bark_scroll',4,1,1),('slavicmyths:ancient_coin',3,1,2),('slavicmyths:idol_fragment',3,1,1),('slavicmyths:thunder_stone',1,1,1)]
    js('data/slavicmyths/loot_tables/chests/ancient_shrine.json',{'type':'minecraft:chest','pools':[{'rolls':{'min':3,'max':5},'entries':[{'type':'minecraft:item','name':name,'weight':weight,'functions':[{'function':'minecraft:set_count','count':{'min':lo,'max':hi}}]} for name,weight,lo,hi in loot]}]})

if __name__ == '__main__': generate(Path(__file__).resolve().parents[1])
