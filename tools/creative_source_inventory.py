"""Static expansion of CURRENT item registration expressions; never starts Minecraft.

Literal registrations/helpers and literal loops are read from production source.
Compound families below mirror only the existing registration expressions and
fail when their expected source markers change. Not a loaded-registry fixture.
"""
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'src/main/java/org/slavicmyths'

def matching(text, start, opening='(', closing=')'):
    depth = 0
    quoted = False
    escape = False
    for i in range(start, len(text)):
        c = text[i]
        if quoted:
            if escape: escape = False
            elif c == '\\': escape = True
            elif c == '"': quoted = False
        elif c == '"': quoted = True
        elif c == opening: depth += 1
        elif c == closing:
            depth -= 1
            if depth == 0: return i
    raise AssertionError('Unbalanced source expression')

def inventory():
    result = {}
    helpers = {
        'registry/ModItems.java': ['egg','food','stew'],
        'husbandry/Husbandry.java': ['item','meat','egg'],
        'textile/Textiles.java': ['register'],
        'kitchen/KitchenII.java': ['food'],
        'garden/Gardens.java': ['add'],
        'furniture/Furniture.java': ['add'],
        'kurgan/KurganBlocks.java': ['add'],
        'storage/Household.java': ['add'],
        'rewards/RewardContent.java': ['register'],
        'industry/Industry.java': ['item'],
        'industry/IndustryAccessories.java': ['register'],
        'equipment/UniqueEquipment.java': ['armor'],
        'brewing/Brewing.java': ['drink','batch'],
        'serpenthill/HillContent.java': ['add'],
        'navya/NavyaBlocks.java': ['add'],
        'blackcinder/CinderBlocks.java': ['add'],
        'losthalls/HallsBlocks.java': ['add'],
    }
    def put(id, file, evidence):
        result.setdefault(id, {'source': 'src/main/java/org/slavicmyths/' + file,
                               'evidence': evidence, 'scope': 'CURRENT_SOURCE_STATIC'})
    for path in JAVA.rglob('*.java'):
        file = path.relative_to(JAVA).as_posix()
        code = path.read_text(encoding='utf-8')
        # Comments are not registration evidence.
        code = re.sub(r'/\*.*?\*/|//[^\n]*', '', code, flags=re.S)
        methods = helpers.get(file, [])
        calls = r'(?:ModItems\.ITEMS\.register' + (r'|ITEMS\.register' if file == 'registry/ModItems.java' else '')
        calls += ''.join(r'|(?<![\w.])' + method for method in methods) + r')'
        for m in re.finditer(calls + r'\(\s*"([a-z0-9_]+)"\s*[,)]', code):
            # A literal followed by concatenation is handled as a compound family below.
            put(m[1], file, m[0])
        # A loop's literal collection counts only when its body registers that variable as an item.
        for m in re.finditer(r'for\s*\(\s*(?:String|var)\s+(\w+)\s*:', code):
            start = code.index('(', m.start()); end = matching(code, start)
            expression = code[m.end():end].strip()
            if not re.match(r'(?:new String\[\]\s*\{|List\.of\()', expression): continue
            tail = end + 1
            while code[tail].isspace(): tail += 1
            if code[tail] == '{': body = code[tail:matching(code, tail, '{', '}') + 1]
            elif code.startswith('for', tail): continue  # Compound families handled below.
            else: body = code[tail:code.index(';', tail) + 1]
            if re.search(calls + r'\(\s*' + m[1] + r'\s*[,)]', body):
                for id in re.findall(r'"([a-z0-9_]+)"', expression): put(id, file, 'literal item loop ' + expression)

    def family(file, markers, ids):
        code = (JAVA / file).read_text(encoding='utf-8')
        for marker in markers:
            assert marker in code, (file, 'registration expression changed', marker)
        for id in ids: put(id, file, 'expanded existing family: ' + '; '.join(markers))

    woods = ['linden','rowan','willow','pine']
    parts = ['log','wood','stripped_log','stripped_wood','planks','leaves','sapling','stairs','slab','fence','fence_gate','door','trapdoor','pressure_plate','button','sign']
    family('wood/Woodlands.java', ['"linden","rowan","willow","pine"','ModItems.ITEMS.register(n+"_sign"'], [w+'_'+p for w in woods for p in parts])
    family('furniture/Furniture.java', ['"pine","linden"','"table","chair","stool","bench","shelf","wardrobe","bedside_cabinet","weapon_rack"','add(wood+"_"+kind,kind)'], [w+'_'+p for w in ['pine','linden'] for p in ['table','chair','stool','bench','shelf','wardrobe','bedside_cabinet','weapon_rack']])
    family('kurgan/DarkenedWood.java', ['"darkened_"+n'], ['darkened_'+p for p in re.findall(r'\badd\("([a-z_]+)"',(JAVA/'kurgan/DarkenedWood.java').read_text(encoding='utf-8'))])
    stones = [prefix+f+'_kurgan_'+t for f in ['pale_blue','bog_green'] for prefix,t in [('', 'stone'),('', 'cobblestone'),('cracked_', 'cobblestone'),('mossy_', 'cobblestone')]]
    family('kurgan/KurganBlocks.java', ['"pale_blue","bog_green"','add(base+"_slab"','add(base+"_stairs"','add(base+"_wall"'], [s+suffix for s in stones for suffix in ['', '_slab','_stairs','_wall']])
    berries = ['raspberry','blueberry','blackcurrant','lingonberry','cranberry']
    family('garden/Gardens.java', ['"raspberry","blueberry","blackcurrant","lingonberry","cranberry"','ModItems.ITEMS.register(id+"_sapling"'], berries + [s+'_sapling' for s in berries])
    family('storage/Household.java', ['"rye","barley","oat"','grain+"_sheaf"'], ['rye_sheaf','barley_sheaf','oat_sheaf'])
    materials = re.findall(r'new Material\("([a-z_]+)"',(JAVA/'combat/WeaponCatalog.java').read_text(encoding='utf-8'))
    family('combat/WeaponCatalog.java', ['"spearhead","light_spearhead","flail_ball"','"spear","sulitsa","flail","throwing_knife"','String id=weaponId(m.id(),type)'], [m+'_'+p for m in materials for p in ['spearhead','light_spearhead','flail_ball','spear','sulitsa','flail','throwing_knife'] if (m,p) not in [('stone','spear'),('iron','flail')]])
    for file,prefix in [('gorynych/GorynychContent.java','dragon'),('poloz/PolozRewards.java','poloz')]:
        family(file, ['ModItems.ITEMS.register("'+prefix+'_"+type.getName()'], [prefix+'_'+p for p in ['helmet','chestplate','leggings','boots']])
    family('mor/MorBlocks.java', ['"mor_"+id','public static final String[] IDS='], ['mor_'+p for p in re.findall(r'"([a-z_]+)"',(JAVA/'mor/MorBlocks.java').read_text().split('IDS={')[1].split('};')[0])])
    family('industry/ChudPeople.java', ['name+"_spawn_egg"','egg("chud_resident"','egg("chud_artisan"','egg("chud_guard"'], [s+'_spawn_egg' for s in ['chud_resident','chud_artisan','chud_guard']])
    family('underground/UndergroundMobs.java', ['n+"_spawn_egg"','egg("podzemnik"','egg("large_podzemnik"','egg("kladovik"'], [s+'_spawn_egg' for s in ['podzemnik','large_podzemnik','kladovik']])
    family('pesigolovtsy/DogheadContent.java', ['"pesigolovets_"+role.id()+"_spawn_egg"'], ['pesigolovets_'+s+'_spawn_egg' for s in ['scout','hunter','leader']])
    family('rpg/RuneFoundation.java', ['tier<=4','"blank_rune_"+t'], ['blank_rune_'+str(t) for t in range(1,5)])
    def string_list(file, name):
        code = (JAVA/file).read_text(encoding='utf-8')
        expr = re.search(r'\b'+re.escape(name)+r'\s*=\s*List.of\((.*?)\)', code, re.S)
        assert expr, (file, name, 'literal List.of changed')
        return re.findall(r'"([a-z0-9_]+)"', expr[1])

    for file in ['blackcinder/CinderContent.java','losthalls/HallsContent.java','burningislands/IslandsContent.java']:
        code = (JAVA/file).read_text(encoding='utf-8')
        kinds = re.search(r'enum Kind\s*\{([^}]+)\}',code)[1]
        family(file,['id(k)+"_spawn_egg"','k.name().toLowerCase(Locale.ROOT)'],[k.strip().lower()+'_spawn_egg' for k in kinds.split(',')])
    code = (JAVA/'navya/NavyaContent.java').read_text(encoding='utf-8')
    names = re.search(r'NAMES=Map.of\((.*?)\);',code,re.S)[1]
    family('navya/NavyaContent.java',['String name=NAMES.get(kind)','name+"_spawn_egg"'],[n+'_spawn_egg' for n in re.findall(r'"([a-z_]+)"',names)])
    for file, bases in [('blackcinder/CinderBlocks.java',['charred_stone','cracked_charred_stone']),('losthalls/HallsBlocks.java',['lost_hall_stone','polished_lost_hall_stone'])]:
        variable = 'id' if 'CinderBlocks' in file else 'base'
        family(file,[variable+'+"_stairs"',variable+'+"_slab"',variable+'+"_wall"'],[b+s for b in bases for s in ['_stairs','_slab','_wall']])
    file='netherores/NetherOres.java'
    family(file,['new ArrayList<>(ORES)','items.addAll(ROASTED)','ModItems.ITEMS.register(id'],sum([string_list(file,n) for n in ['ORES','RAW','ROASTED']],[])+['sulfur_deposit','sulfur','alatyr_ingot'])
    file='nethersmithing/NetherSmithing.java'
    family(file,['for(String id:MATERIALS)','"zhara_"+form'],string_list(file,'MATERIALS')+['zhara_'+f for f in string_list(file,'FORMS')])
    family('nethersmithing/NetherArmor.java',['NetherArmorRules.id(king,type)','new Piece(king,type)'],string_list('nethersmithing/NetherArmorRules.java','IDS'))
    family('burningislands/IslandsContent.java',['tier<=2','String id="rune_magma_"+tier','ModItems.ITEMS.register(id'],['rune_magma_1','rune_magma_2'])
    family('rpg/RuneRework.java',['org.slavicmyths.rpg.tools.ToolRuneRules.IDS'],['rune_'+s for s in string_list('rpg/tools/ToolRuneRules.java','IDS')])
    code=(JAVA/'trophy/TrophyHeads.java').read_text(encoding='utf-8')
    family('trophy/TrophyHeads.java',['ModItems.ITEMS.register(k.id'],re.findall(r'\w+\("([a-z_]+)",\d\)',code.split('public enum Kind {')[1].split(';')[0]))
    family('rpg/RuneRework.java', ['AdvancedRuneRules.IDS','RuneBalance.DEFINITIONS','"rune_"+id'], ['rune_'+s for s in re.findall(r'"([a-z_]+)"',(JAVA/'rpg/AdvancedRuneRules.java').read_text().split('IDS=')[1].split(';')[0])] + ['rune_'+s for s in ['strength','speed','resilience','crushing','blood','fortitude']])
    rare = re.findall(r'"([a-z_]+)"',(JAVA/'rpg/RareRuneRules.java').read_text().split('IDS=')[1].split(';')[0])
    family('rpg/RareRunes.java', ['RareRuneRules.IDS','"rune_"+id','"rune_"+id+"_fragment"'], ['rune_'+s for s in rare]+['rune_'+s+'_fragment' for s in ['flight','rare_protection','death','berserker']])
    family('industry/MiningRunes.java', ['"mining_1","mining_2","fortune_1"'], ['mining_rune_1','mining_rune_2','fortune_rune_1'])
    family('rewards/RewardContent.java', ['"stomp","dispelling","pursuit","suppression","battle_line"','register("rune_"+id'], ['rune_'+s for s in ['stomp','dispelling','pursuit','suppression','battle_line']])
    beers = re.findall(r'"([a-z_]+)"',(JAVA/'brewing/Brewing.java').read_text().split('BEERS={')[1].split('};')[0])
    family('brewing/Brewing.java', ['drink(s+"_mug"','batch(s.equals("veles_dark_beer")'], [s+'_mug' for s in beers]+[('veles_dark_wort' if s=='veles_dark_beer' else 'witch_berry_wort' if s=='witch_berry_beer' else s+'_wort') for s in beers])
    family('firebird/FirebirdContent.java', ['"firebird_feather","smoldering_feather","firebird_down"','for(String name:RESOURCES)'], ['firebird_feather','smoldering_feather','firebird_down'])
    code = (JAVA/'storage/Household.java').read_text(encoding='utf-8')
    family('storage/Household.java', ['for(Kind k:Kind.values())','ModItems.ITEMS.register(k.id'], re.findall(r'\w+\("([a-z_]+)"', code.split('public enum Kind {')[1].split(';')[0]))
    code = (JAVA/'textile/Textiles.java').read_text(encoding='utf-8')
    family('textile/Textiles.java', ['BLOCKS.forEach((s,b)->register(s,'], re.findall(r'BLOCKS.put\("([a-z_]+)"', code))
    code = (JAVA/'earth/EarthContent.java').read_text(encoding='utf-8')
    expr = re.search(r'Map.of\((.*?)\).forEach\(\(key,block\)->ModItems.ITEMS.register\(key', code, re.S)
    assert expr, 'Earth block-item map registration changed'
    family('earth/EarthContent.java', ['Map.of(', ').forEach((key,block)->ModItems.ITEMS.register(key'], re.findall(r'"([a-z_]+)"', expr[1]))
    return result

if __name__ == '__main__':
    ids = inventory()
    catalog = {line.split('\t')[0] for line in (ROOT/'src/main/resources/assets/slavicmyths/creative_catalog.tsv').read_text().splitlines() if line and not line.startswith('#')}
    print('source',len(ids),'catalog',len(catalog))
    print('source only', sorted(ids.keys()-catalog))
    print('catalog only', sorted(catalog-ids.keys()))
