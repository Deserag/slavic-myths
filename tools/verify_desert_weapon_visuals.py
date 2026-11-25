import json,re,sys
from pathlib import Path
from PIL import Image
A=Path('src/main/resources/assets/slavicmyths');M=A/'models/item'
ids=['desert_sword','desert_axe','desert_mace','desert_halberd','desert_scythe','scorpion_double_sting','stinger_dagger','sand_sling','desert_glass_blade','dune_mace','buried_hammer','hala_solar_staff','colony_scepter','four_winds_fists','lamya_weapon','quicksand_whip']
for id in ids:
 v=json.loads((M/f'{id}.json').read_text());base=v['base'];obj=A/base['model'].split(':')[1];assert obj.is_file(),obj
 text=obj.read_text();assert text.count('\nf ')>100,id
 for name in ['firstperson_righthand','firstperson_lefthand','thirdperson_righthand','thirdperson_lefthand']:assert name in base['display']
 for lang in ['ru_ru','en_us']:assert 'item.slavicmyths.'+id in json.loads((A/f'lang/{lang}.json').read_text(encoding='utf-8'))
 icon=Image.open(A/f'textures/item/desert_weapons/{id}.png');assert icon.size==(32,32) and icon.getbbox(),id
for s in ['', '_pulling_0','_pulling_1','_pulling_2']:assert (M/f'mirage_bow{s}.json').is_file()
atlas=Image.open(A/'textures/gui/weapon_skills/desert_atlas.png');assert atlas.size==(128,64)
tiles=[atlas.crop((i%8*16,i//8*16,i%8*16+16,i//8*16+16)).tobytes() for i in range(29)];assert len(set(tiles))==29
print('PASS: 16 OBJ resources, four hand contexts, inventory icons, 4 bow states, 29 unique skill tiles and translations')
