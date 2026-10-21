"""Add missing 1.21 enchantable membership without replacing vanilla/previous entries.
No assets, stats, IDs or special weapon enchantment overrides are regenerated.
"""
from pathlib import Path
import json,re
R=Path(__file__).resolve().parents[1]
D=R/'src/main/resources/data/minecraft/tags/item/enchantable'

def add(tag, values):
    p=D/(tag+'.json')
    data=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]}
    assert not data.get('replace',False)
    data['values']=list(dict.fromkeys(data['values']+values))
    p.write_text(json.dumps(data,indent=2)+'\n',encoding='utf-8')

def main():
    slots={'head_armor':[],'chest_armor':['gambeson','plate_cuirass','bogatyr_chestplate','trophy_idol_chest','trophy_marinka_cloak','trophy_kartaus_mantle'],
           'leg_armor':['indomitability_leggings'],'foot_armor':['wind_boots','seven_league_boots']}
    slots['head_armor'].append('clear_sight_helmet')
    for prefix in ['perunite','dragon','poloz']:
        for tag,suffix in [('head_armor','helmet'),('chest_armor','chestplate'),('leg_armor','leggings'),('foot_armor','boots')]:
            slots[tag].append(prefix+'_'+suffix)
    for tag,ids in slots.items():add(tag,['slavicmyths:'+s for s in ids])
    # Apply ordinary vanilla class categories to the original equipment omitted in the port.
    source=(R/'src/main/java/org/slavicmyths/registry/ModItems.java').read_text()
    groups={'sword':[],'mining':[],'mining_loot':[],'sharp_weapon':[],'weapon':[],'durability':[]}
    for ident,kind in re.findall(r'ITEMS.register\("([a-z_]+)",\s*\(\)\s*->\s*new (?:org\.slavicmyths\.item\.)?(SwordItem|AxeItem|PickaxeItem|ShovelItem|HoeItem|ThunderAxeItem|ClubItem|HeavyWeaponItem|SpearItem)\(',source):
        item='slavicmyths:'+ident
        groups['durability'].append(item)
        if kind in ['AxeItem','ThunderAxeItem','PickaxeItem','ShovelItem','HoeItem']:
            groups['mining'].append(item);groups['mining_loot'].append(item)
        if kind in ['SwordItem','AxeItem','ThunderAxeItem','ClubItem','HeavyWeaponItem','SpearItem']:
            groups['weapon'].append(item);groups['sharp_weapon'].append(item)
        if kind=='SwordItem':groups['sword'].append(item)
    # Sword subclasses retain their class's vanilla category. Existing mace/throwing overrides remain.
    for ident in ['pool_spear','nightingale_dagger']:
        for tag in ['sword','weapon','sharp_weapon','durability']:groups[tag].append('slavicmyths:'+ident)
    for tag,items in groups.items():add(tag,items)
    add('crossbow',['slavicmyths:serpent_crossbow','slavicmyths:three_breath_crossbow'])
    add('durability',['#minecraft:enchantable/armor','slavicmyths:ward_shield','slavicmyths:serpent_crossbow','slavicmyths:three_breath_crossbow'])
    print('Updated additive enchantment tags; 22 armor items across four equipment slots.')

if __name__=='__main__':main()
