"""Reproduce only generation-v2 worldgen registration and merge its localization."""
from pathlib import Path
import json
root=Path(__file__).resolve().parents[1]/'src/main/resources'
def write(name,data):
    path=root/name;path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
write('data/slavicmyths/worldgen/structure/chud_abandoned.json',{
    'type':'slavicmyths:chud_abandoned','biomes':'#slavicmyths:has_structure/chud_abandoned',
    'step':'underground_structures','terrain_adaptation':'none','spawn_overrides':{}})
write('data/slavicmyths/worldgen/structure_set/chud_abandoned.json',{
    'structures':[{'structure':'slavicmyths:chud_abandoned','weight':1}],
    'placement':{'type':'slavicmyths:chud_region','spacing':256,'separation':160,
                 'salt':1128813892,'spread_type':'linear'}})
write('data/slavicmyths/tags/worldgen/biome/has_structure/chud_abandoned.json',{
    'replace':False,'values':['#minecraft:is_overworld']})
for lang,name in [('ru_ru','Заброшенные владения Чуди'),('en_us','Abandoned Chud Realms')]:
    path=root/f'assets/slavicmyths/lang/{lang}.json'
    data=json.loads(path.read_text(encoding='utf-8'));data['structure.slavicmyths.chud_abandoned']=name
    write(f'assets/slavicmyths/lang/{lang}.json',data)
