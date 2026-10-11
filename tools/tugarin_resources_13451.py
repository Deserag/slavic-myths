"""Latest additive Tugarin resources. Native geometry in TugarinSerpentGeometry uses the existing original atlas."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
def apply():
    res=ROOT/'src/main/resources'
    texts={'ru_ru':{'boss.slavicmyths.tugarin.transforming':'Тугарин Змей — Пробуждение','boss.slavicmyths.tugarin.true_form':'Тугарин Змей — Истинный облик'},'en_us':{'boss.slavicmyths.tugarin.transforming':'Tugarin Zmey — Awakening','boss.slavicmyths.tugarin.true_form':'Tugarin Zmey — True Form'}}
    for lang,values in texts.items():
        p=res/f'assets/slavicmyths/lang/{lang}.json';d=json.loads(p.read_text(encoding='utf-8'));d.update(values);p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    p=res/'data/slavicmyths/loot_table/entities/tugarin_zmey.json';d=json.loads(p.read_text(encoding='utf-8'));d['pools']=[x for x in d['pools'] if x.get('name')!='slavicmyths:tugarin_pair_trophy'];d['pools'].append({'name':'slavicmyths:tugarin_pair_trophy','rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:tugarin_sword'}],'conditions':[{'condition':'minecraft:random_chance','chance':.12}]});p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
if __name__=='__main__':apply()
