"""Offline implementation evidence/diagram; does not launch any Minecraft process."""
from pathlib import Path
import json,hashlib,xml.etree.ElementTree as ET,textwrap
ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/main/resources'
DESIGN=ROOT/'docs/implementation/advancements-redesign'
OUT=ROOT/'docs/implementation/advancements-2-implementation'
read=lambda p:json.loads(p.read_text(encoding='utf-8-sig'))
source=read(DESIGN/'SOURCE.json');tree=read(DESIGN/'TREE.json');visible={n['id']:n for n in tree}
decisions={n['id']:n for n in read(DESIGN/'DECISIONS.json')}
rows=[];lines=['# Совместимость всех 206 старых ID','','Источник: SOURCE PR31, неизменный save-contract PR25. Parent/display меняются только по утверждённым TREE/DECISIONS; successor не выдаётся автоматически.','','| Старый ID | Политика | Карточка / successor | Criteria / requirements / rewards |','|---|---|---|---|']
for old in source['nodes']:
    key=old['id'].split(':',1)[1];node=read(ROOT/old['path'])
    equal=all(node.get(k)==old['resource'].get(k) for k in ['criteria','requirements','rewards'])
    assert equal,old['id']
    policy='M2' if old['recipe'] else decisions[key]['migration']
    successor=None if old['recipe'] else decisions[key]['successor']
    rows.append({'id':old['id'],'policy':policy,'successor':successor,'criteria_requirements_rewards':'UNCHANGED','visible':'display' in node})
    lines.append(f"| {old['id']} | {policy} | {successor or '—'} | UNCHANGED |")
(OUT/'COMPATIBILITY.json').write_text(json.dumps(rows,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
(OUT/'COMPATIBILITY.md').write_text('\n'.join(lines)+'\n',encoding='utf-8')

# Generate from the actual loaded JSON presentation, not a claimed Minecraft screenshot.
ns='http://www.w3.org/2000/svg';ET.register_namespace('',ns)
svg=ET.Element('{'+ns+'}svg',{'width':'1920','height':'1080','viewBox':'0 0 1920 1080','role':'img'})
def element(tag,attrs,text=None):
    e=ET.SubElement(svg,'{'+ns+'}'+tag,{k:str(v) for k,v in attrs.items()})
    if text is not None:e.text=text
    return e
element('rect',{'width':1920,'height':1080,'fill':'#f6f0e2'})
element('text',{'x':32,'y':32,'font-size':22,'font-family':'sans-serif'},'Slavic Myths 2.0 — 55 карточек, статическая схема реализации')
element('text',{'x':32,'y':58,'font-size':14,'font-family':'sans-serif'},'Не Minecraft GUI. task / goal / challenge; родители задают отображение, а не обязательный порядок побед.')
ru=read(RES/'assets/slavicmyths/lang/ru_ru.json')
groups=[n for n in tree if n['id'].startswith('group_')]
positions={}
def group_of(key):
    if key in {g['id'] for g in groups}:return key
    return group_of(visible[key]['parent'])
for col,group in enumerate(groups):
    branch=[n for n in tree if n['id']!='root' and group_of(n['id'])==group['id']]
    for row,spec in enumerate(branch):
        node=read(RES/f"data/slavicmyths/advancement/{spec['id']}.json")
        assert node['display']['icon']['id']==spec['icon']
        x=20+col*316;y=140+row*82;positions[spec['id']]=(x,y)
        color={'task':'#8e9c83','goal':'#4d8276','challenge':'#9c6b3b'}[node['display']['frame']]
        element('rect',{'x':x,'y':y,'width':292,'height':68,'rx':8,'fill':'#fffcf5','stroke':color,'stroke-width':4 if node['display']['frame']=='challenge' else 2})
        title=ru[node['display']['title']['translate']]
        for i,line in enumerate(textwrap.wrap(title,width=27)):
            element('text',{'x':x+10,'y':y+22+i*17,'font-size':15,'font-family':'sans-serif','fill':'#263a32'},line)
        element('text',{'x':x+10,'y':y+58,'font-size':10,'font-family':'monospace','fill':'#575b50'},spec['id'])
        if spec['parent'] in positions:
            px,py=positions[spec['parent']]
            element('path',{'d':f'M {px+292} {py+34} H {x+302} V {y+34} H {x+292}','fill':'none','stroke':color,'stroke-width':1})
element('text',{'x':960,'y':100,'text-anchor':'middle','font-size':20,'font-family':'sans-serif'},ru[read(RES/'data/slavicmyths/advancement/root.json')['display']['title']['translate']])
for group in groups:
    x,y=positions[group['id']];element('path',{'d':f'M 960 108 V 118 H {x+146} V {y}','fill':'none','stroke':'#7e8578','stroke-width':1})
ET.ElementTree(svg).write(OUT/'TREE.svg',encoding='utf-8',xml_declaration=True)
print('COMPATIBILITY PASS rows='+str(len(rows))+'; actual-resource SVG cards='+str(len(visible)))
