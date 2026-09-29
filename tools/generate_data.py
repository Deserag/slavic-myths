"""Source of truth for 0.2 data and models; called by create_resources.py."""
import json
from pathlib import Path

def generate(root):
    res = root / 'src/main/resources'
    def js(path, obj):
        p = res/path
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text(json.dumps(obj, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
    names = {
        'birch_bark_scroll': ('Берестяной свиток', 'Birch Bark Scroll'),
        'thunder_stone': ('Громовой камень', 'Thunder Stone'),
        'warding_charm': ('Оберег', 'Warding Charm'),
        'birch_bark': ('Береста', 'Birch Bark'),
        'flax': ('Лён', 'Flax'),
        'linen_thread': ('Льняная нить', 'Linen Thread'),
        'wormwood': ('Полынь', 'Wormwood'),
        'fern_flower': ('Цветок папоротника', 'Fern Flower'),
        'perunite_ore': ('Перунитовая руда', 'Perunite Ore'),
        'perunite': ('Перунит', 'Perunite'),
    }
    blocks = {'flax', 'wormwood', 'perunite_ore'}
    # id: (parent, icon, inventory requirement, title RU/EN, description RU/EN)
    advances = {
        'root': (None, 'birch_bark', 'minecraft:birch_log', ('Первые следы', 'First Traces'), ('Добудьте берёзовое бревно: из него можно изготовить бересту.', 'Obtain a birch log and craft it into birch bark.')),
        'birch_bark': ('root', 'birch_bark', 'slavicmyths:birch_bark', ('Белая память дерева', 'Memory of Birch'), ('Изготовьте 4 бересты из берёзового бревна в сетке крафта.', 'Craft a birch log into 4 birch bark.')),
        'birch_bark_scroll': ('birch_bark', 'birch_bark_scroll', 'slavicmyths:birch_bark_scroll', ('Слова на бересте', 'Words on Bark'), ('Соедините 2 бересты и нить, чтобы получить свиток.', 'Combine 2 birch bark and string to make a scroll.')),
        'flax': ('root', 'flax', 'slavicmyths:flax', ('Синие цветы полей', 'Blue Fields'), ('Соберите лён на равнинах или в лесах. Его можно пересадить.', 'Gather flax in plains or forests. It can be replanted.')),
        'linen_thread': ('flax', 'linen_thread', 'slavicmyths:linen_thread', ('Первая нить', 'First Thread'), ('Соедините 2 растения льна в сетке крафта, чтобы получить льняную нить.', 'Craft 2 flax plants into linen thread.')),
        'wormwood': ('flax', 'wormwood', 'slavicmyths:wormwood', ('Горькая трава', 'Bitter Herb'), ('Соберите полынь на равнинах, в лесу или тайге для будущего оберега.', 'Gather wormwood in plains, forests or taiga for a charm.')),
        'warding_charm': ('linen_thread', 'warding_charm', 'slavicmyths:warding_charm', ('Первый оберег', 'First Charm'), ('Соедините льняную нить, полынь и золотой самородок. Защитных эффектов пока нет.', 'Combine linen thread, wormwood and a gold nugget. No protection effects yet.')),
        'perunite': ('root', 'perunite', 'slavicmyths:perunite', ('Искра в глубине', 'Spark Below'), ('Найдите редкую перунитовую руду ниже Y=16. Нужна железная кирка или лучше.', 'Find rare perunite ore below Y=16. Use an iron pickaxe or better.')),
        'thunder_stone': ('perunite', 'thunder_stone', 'slavicmyths:thunder_stone', ('Знак грома', 'Sign of Thunder'), ('Окружите перунит 2 редстоунами, лазуритом сверху и кремнем снизу. Магии пока нет.', 'Surround perunite with 2 redstone, lapis above and flint below. No magic yet.')),
        'fern_flower': ('root', 'fern_flower', 'slavicmyths:fern_flower', ('Несбыточный цветок', 'Impossible Blossom'), ('Ломайте обычный низкий папоротник без ножниц и Шёлкового касания: шанс цветка 0,2%.', 'Break small ferns without shears or Silk Touch: 0.2% chance of a flower.')),
    }
    for lang, index in [('ru_ru', 0), ('en_us', 1)]:
        data = {'itemGroup.slavicmyths': ('Славянские мифы', 'Slavic Myths')[index]}
        for name, translations in names.items():
            data[('block' if name in blocks else 'item')+'.slavicmyths.'+name] = translations[index]
        for aid, (_, _, _, title, desc) in advances.items():
            data['advancements.slavicmyths.'+aid+'.title'] = title[index]
            data['advancements.slavicmyths.'+aid+'.description'] = desc[index]
        js(Path('assets/slavicmyths/lang')/(lang+'.json'), data)
    for name in names:
        if name in blocks:
            parent = 'minecraft:block/cube_all' if name == 'perunite_ore' else 'minecraft:block/cross'
            key = 'all' if name == 'perunite_ore' else 'cross'
            js(Path('assets/slavicmyths/models/block')/(name+'.json'), {'parent': parent, 'textures': {key: 'slavicmyths:block/'+name}})
            js(Path('assets/slavicmyths/blockstates')/(name+'.json'), {'variants': {'': {'model': 'slavicmyths:block/'+name}}})
        model = {'parent': 'slavicmyths:block/'+name} if name == 'perunite_ore' else {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'slavicmyths:'+('block/' if name in blocks else 'item/')+name}}
        js(Path('assets/slavicmyths/models/item')/(name+'.json'), model)
    for aid, (parent, icon, item, _, _) in advances.items():
        display = {'icon': {'item': 'slavicmyths:'+icon}, 'title': {'translate': 'advancements.slavicmyths.'+aid+'.title'}, 'description': {'translate': 'advancements.slavicmyths.'+aid+'.description'}, 'frame': 'challenge' if aid == 'fern_flower' else 'task', 'show_toast': aid != 'root', 'announce_to_chat': aid != 'root', 'hidden': False}
        a = {'display': display, 'criteria': {'obtained': {'trigger': 'minecraft:inventory_changed', 'conditions': {'items': [{'item': item}]}}}}
        if parent:
            a['parent'] = 'slavicmyths:'+parent
        else:
            display['background'] = 'minecraft:textures/block/birch_log.png'
        js(Path('data/slavicmyths/advancements')/(aid+'.json'), a)
    def shapeless(*ingredients):
        return {'type': 'minecraft:crafting_shapeless', 'ingredients': [{'item': i} for i in ingredients]}
    recipes = {
        'birch_bark': (shapeless('minecraft:birch_log'), 4, 'minecraft:birch_log'),
        'birch_bark_scroll': (shapeless('slavicmyths:birch_bark', 'slavicmyths:birch_bark', 'minecraft:string'), 1, 'slavicmyths:birch_bark'),
        'linen_thread': (shapeless('slavicmyths:flax', 'slavicmyths:flax'), 1, 'slavicmyths:flax'),
        'warding_charm': (shapeless('slavicmyths:linen_thread', 'slavicmyths:wormwood', 'minecraft:gold_nugget'), 1, 'slavicmyths:linen_thread'),
        'thunder_stone': ({'type': 'minecraft:crafting_shaped', 'pattern': [' L ', 'RPR', ' F '], 'key': {'L': {'item': 'minecraft:lapis_lazuli'}, 'R': {'item': 'minecraft:redstone'}, 'P': {'item': 'slavicmyths:perunite'}, 'F': {'item': 'minecraft:flint'}}}, 1, 'slavicmyths:perunite'),
    }
    for name, (recipe, count, ingredient) in recipes.items():
        recipe['result'] = {'item': 'slavicmyths:'+name, 'count': count}
        js(Path('data/slavicmyths/recipes')/(name+'.json'), recipe)
        js(Path('data/slavicmyths/advancements/recipes')/(name+'.json'), {'parent': 'minecraft:recipes/root', 'criteria': {'has_ingredient': {'trigger': 'minecraft:inventory_changed', 'conditions': {'items': [{'item': ingredient}]}}, 'has_recipe': {'trigger': 'minecraft:recipe_unlocked', 'conditions': {'recipe': 'slavicmyths:'+name}}}, 'requirements': [['has_ingredient', 'has_recipe']], 'rewards': {'recipes': ['slavicmyths:'+name]}})
    for plant in ('flax', 'wormwood'):
        js(Path('data/slavicmyths/loot_tables/blocks')/(plant+'.json'), {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': 'slavicmyths:'+plant}], 'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})
    silk = {'condition': 'minecraft:match_tool', 'predicate': {'enchantments': [{'enchantment': 'minecraft:silk_touch', 'levels': {'min': 1}}]}}
    js(Path('data/slavicmyths/loot_tables/blocks/perunite_ore.json'), {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:alternatives', 'children': [{'type': 'minecraft:item', 'name': 'slavicmyths:perunite_ore', 'conditions': [silk]}, {'type': 'minecraft:item', 'name': 'slavicmyths:perunite', 'functions': [{'function': 'minecraft:apply_bonus', 'enchantment': 'minecraft:fortune', 'formula': 'minecraft:ore_drops'}, {'function': 'minecraft:explosion_decay'}]}]}]}]})
    js(Path('data/forge/loot_modifiers/global_loot_modifiers.json'), {'replace': False, 'entries': ['slavicmyths:fern_flower']})
    js(Path('data/slavicmyths/loot_modifiers/fern_flower.json'), {'type': 'slavicmyths:fern_flower', 'conditions': [{'condition': 'forge:loot_table_id', 'loot_table_id': 'minecraft:blocks/fern'}, {'condition': 'minecraft:inverted', 'term': {'condition': 'minecraft:match_tool', 'predicate': {'item': 'minecraft:shears'}}}, {'condition': 'minecraft:inverted', 'term': silk}, {'condition': 'minecraft:random_chance', 'chance': 0.002}, {'condition': 'minecraft:survives_explosion'}]})
