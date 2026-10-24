"""Apply the approved PR31 presentation overlay; never delete legacy definitions.

Only parent/display of gameplay nodes and the approved RU/EN strings change.
The immutable SOURCE snapshot supplies legacy criteria, requirements and rewards.
Run this last after historical generators, whose old layouts must not reappear.
"""
from pathlib import Path
import copy,json
ROOT=Path(__file__).resolve().parents[1]
DESIGN=ROOT/'docs/implementation/advancements-redesign'
RES=ROOT/'src/main/resources'

def apply(root=ROOT):
    design=root/'docs/implementation/advancements-redesign';res=root/'src/main/resources'
    if not design.is_dir():design=DESIGN
    source=json.loads((design/'SOURCE.json').read_text(encoding='utf-8-sig'))
    tree=json.loads((design/'TREE.json').read_text(encoding='utf-8-sig'))
    visible={n['id']:n for n in tree}
    locales={l:json.loads((res/f'assets/slavicmyths/lang/{l}.json').read_text(encoding='utf-8-sig')) for l in ['ru_ru','en_us']}
    legacy={n['id'].split(':',1)[1]:n for n in source['nodes'] if not n['recipe']}
    for key in sorted(set(legacy)|set(visible)):
        path=res/f'data/slavicmyths/advancement/{key}.json'
        old=legacy.get(key)
        node=copy.deepcopy(old['resource']) if old else {'criteria':{('mastery' if key=='class_iv' else 'victory'):{'trigger':'minecraft:impossible'}},'requirements':[[('mastery' if key=='class_iv' else 'victory')]],'rewards':{}}
        if key not in visible:
            node.pop('display',None)
        else:
            spec=visible[key]
            if spec['parent']:node['parent']='slavicmyths:'+spec['parent']
            else:node.pop('parent',None)
            display=copy.deepcopy(node.get('display',{}))
            display.update(icon={'id':spec['icon']},frame=spec['frame'],show_toast=spec['show_toast'],announce_to_chat=spec['announce_to_chat'],hidden=spec['hidden'])
            for field in ['title','description']:
                translation=display.get(field,{}).get('translate',f'advancement.slavicmyths.{key}.{field}')
                display[field]={'translate':translation}
                for locale,lang in [('ru_ru','ru'),('en_us','en')]:locales[locale][translation]=spec[field+'_'+lang]
            node['display']=display
            if key.startswith('group_'):
                for locale,language in [('ru_ru','ru'),('en_us','en')]:
                    locales[locale]['tales.group.'+key.removeprefix('group_')]=spec['title_'+language]
        path.parent.mkdir(parents=True,exist_ok=True)
        path.write_text(json.dumps(node,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    # Clarifies presentation-only Available without changing graph wire semantics.
    locales['ru_ru']['tales.available']='Ветка открыта — порядок прохождения свободный'
    locales['en_us']['tales.available']='Branch open — milestones can be earned in any order'
    for locale,value in locales.items():(res/f'assets/slavicmyths/lang/{locale}.json').write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

_apply_v2 = apply
def apply(root=ROOT):
    from advancements3_resources import apply as apply_v3
    apply_v3(root)

if __name__=='__main__':apply()
