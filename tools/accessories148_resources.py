"""Scoped #150/#151 presentation overlay; no new item or art registrations."""
from pathlib import Path
import json
TRANSFERS = {'amber_charm': 'offhand', 'amulet_two_winters': 'inventory/use', 'black_wing_amulet': 'necklace', 'depth_amulet': 'necklace', 'finist_quiver': 'belt', 'forest_charm': 'offhand', 'frost_mirror': 'necklace', 'gift_alyosha_belt': 'belt', 'gift_dobrynya_ring': 'ring', 'gift_finist_amulet': 'necklace', 'grave_ward': 'charm', 'grivna': 'necklace', 'hunter_belt': 'belt', 'hunter_charm': 'use', 'invisibility_cap': 'head', 'lunula': 'necklace', 'miners_goggles': 'head', 'obereg_ohotnika': 'use', 'obereg_padayuschey_zvezdy': 'necklace', 'odnoglazyy_obereg': 'necklace', 'perun_charm': 'offhand', 'perun_ring': 'ring', 'poyas_tugarina': 'belt', 'prospectors_ring': 'ring', 'resin_ring': 'ring', 'retribution_charm': 'charm', 'ring_of_grey_pack': 'ring', 'stone_path_amulet': 'necklace', 'traveler_charm': 'offhand', 'trophy_skim_necklace': 'necklace', 'underground_guard_belt': 'belt', 'veles_amulet': 'necklace', 'volchiy_poyas': 'belt', 'zolny_obereg': 'necklace'}
LABELS = {'ru_ru': {'item.slavicmyths.lunula.effect': 'Ночью на 8% меньше урона от существ.', 'item.slavicmyths.grivna.effect': 'На 6% меньше обычного урона, учитывающего броню.', 'item.slavicmyths.grave_ward.effect': 'На 12% меньше урона от нежити.', 'item.slavicmyths.hunter_belt.effect': 'Слот: пояс. +15% урона стрелами по диким животным.', 'item.slavicmyths.trophy_skim_necklace.effect': 'Прямой урон по целям с менее чем 35% здоровья увеличен на 15%.', 'item.slavicmyths.trophy_skim_necklace.desc': 'Прямой урон по целям с менее чем 35% здоровья увеличен на 15%.'}, 'en_us': {'item.slavicmyths.lunula.effect': '8% less damage from creatures at night.', 'item.slavicmyths.grivna.effect': '6% less ordinary, armor-sensitive damage.', 'item.slavicmyths.grave_ward.effect': '12% less damage from undead.', 'item.slavicmyths.hunter_belt.effect': 'Belt slot. +15% projectile damage against wildlife.', 'item.slavicmyths.trophy_skim_necklace.effect': 'While equipped, direct player damage to targets strictly below 35% max HP gains +15% after normal damage calculation. Boss eligibility allowed with no stacking beyond one necklace. Do not trigger on DoT/pets/reflection.', 'item.slavicmyths.trophy_skim_necklace.desc': 'While equipped, direct player damage to targets strictly below 35% max HP gains +15% after normal damage calculation. Boss eligibility allowed with no stacking beyond one necklace. Do not trigger on DoT/pets/reflection.'}}

def apply(root=None):
    root=Path(root) if root is not None else Path(__file__).resolve().parents[1]
    assets=root/'src/main/resources/assets/slavicmyths'
    p=assets/'creative_catalog.tsv'
    if p.exists():
        lines=p.read_text(encoding='utf8').splitlines();kept=[];moved=[]
        for line in lines:
            fields=line.split('\t')
            if len(fields)==3 and fields[0] in TRANSFERS:
                moved.append(fields[0]+'\taccessories\t'+TRANSFERS[fields[0]])
            else:kept.append(line)
        slot_order={'head':0,'necklace':1,'ring':2,'belt':3,'charm':4,'offhand':5,'use':6,'inventory/use':7}
        moved.sort(key=lambda line:(slot_order[TRANSFERS[line.split('\t')[0]]],line.split('\t')[0]))
        p.write_text('\n'.join(kept+moved)+'\n',encoding='utf8')
    for lang,changes in LABELS.items():
        p=assets/f'lang/{lang}.json'
        if not p.exists():continue
        data=json.loads(p.read_text(encoding='utf8'));data.update(changes)
        data['itemGroup.slavicmyths.accessories']='Украшения и аксессуары' if lang=='ru_ru' else 'Jewelry & Accessories'
        p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
if __name__=='__main__':apply()
