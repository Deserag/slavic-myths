"""Reorganize display edges without changing legacy criteria or rewards."""
import csv,json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/main/resources'
AUDIT=ROOT/'docs/verification/bayun-book-final'
before=json.loads((AUDIT/'before-advancements.json').read_text(encoding='utf8'))
adv=RES/'data/slavicmyths/advancement'
locales={l:json.loads((RES/f'assets/slavicmyths/lang/{l}.json').read_text(encoding='utf8')) for l in ('ru_ru','en_us')}
groups={
 'knowledge':('Память и обряды','Memory and rituals','lore_book'),
 'home':('Дом и хозяйство','Home and household','hearth'),
 'forest':('Лес и природа','Forest and nature','pine_log'),
 'water':('Вода и промысел','Water and fishing','fishing_net'),
 'night':('Ночь и дурная доля','Night and ill fate','warding_charm'),
 'burial':('Курганы и прошлое','Burial mounds and the past','ancient_coin'),
 'depths':('Древние глубины','Ancient depths','silver_ingot'),
 'roads':('Дороги и разбойники','Roads and bandits','iron_spear'),
 'legends':('Особые встречи','Unusual encounters','birch_bark')}
def group(key):
 if any(s in key for s in ('yaga','domovoy','kuzya','bannik','bread','feast','table','kitchen','cook','meal','food','stores','farm','crop','flax','linen','drink','brew','goat','furniture','hearth')):return 'home'
 if any(s in key for s in ('water','fish','catch','net','pool','pearl','depth_master','depth_gift','rusalka','swamp','causeway','flooded')):return 'water'
 if any(s in key for s in ('kurgan','burial','prince','volkhv','nav','upyr','past')):return 'burial'
 if any(s in key for s in ('chud','earth','podzem','kladov','industry','refin','flux','depths','mine','ore','goggles')):return 'depths'
 if any(s in key for s in ('bandit','camp','ataman','nightingale','bad_people','armorer','weapon','spear','mace','shield','dagger','armor','two_rings')):return 'roads'
 if any(s in key for s in ('wolf_trial','grey_wolf','sivka','firebird','bayun','finist','tugarin','steppe_champion')):return 'legends'
 if any(s in key for s in ('likho','ill_fate','volkolak','nochnitsa','kikimora','too_much','silver_remedy')):return 'night'
 if any(s in key for s in ('forest','wood','timber','leshy','wildlife','bear','hunt','berry','herb','wormwood','nettle','fireweed')):return 'forest'
 return 'knowledge'
def display(key,icon):return dict(icon={'id':'slavicmyths:'+icon},title={'translate':'advancements.slavicmyths.'+key+'.title'},description={'translate':'advancements.slavicmyths.'+key+'.description'},frame='task',show_toast=False,announce_to_chat=False,hidden=False)
nodes={Path(path.replace('\\','/')).relative_to('src/main/resources/data/slavicmyths/advancement').as_posix()[:-5]:value for path,value in before.items()}
report=[]
descriptions={'meet_nightingale':('Встретьте Соловья-разбойника.','Meet Nightingale the Robber.'),'learn_armorer':('Познакомьтесь с существующим оружейным ремеслом.','Discover the existing armorer craft.'),'find_small_camp':('Найдите малый разбойничий лагерь.','Find a small bandit camp.'),'find_medium_camp':('Найдите средний разбойничий лагерь.','Find a medium bandit camp.')}
for key,n in nodes.items():
 old=json.loads(json.dumps(n))
 if not key.startswith('recipes/') and key!='root':
  if 'display' not in n:
   n['display']=display(key,'lore_book')
   title=key.replace('_',' ').capitalize()
   # The previously invisible manual discoveries get their real existing labels.
   labels={'meet_nightingale':('Свист над станом','A whistle over the camp'),
           'learn_armorer':('Оружейное дело','Armorer’s craft'),
           'find_small_camp':('Малая шайка','Small gang'),
           'find_medium_camp':('Укреплённый лагерь','Fortified camp')}
   for i,l in enumerate(locales):
    locales[l]['advancements.slavicmyths.'+key+'.title']=labels.get(key,(title,title))[i]
    locales[l]['advancements.slavicmyths.'+key+'.description']=descriptions[key][i]
  if not n.get('parent') or n['parent']=='slavicmyths:root':n['parent']='slavicmyths:group_'+group(key)
 assert n['criteria']==old['criteria'] and n.get('requirements')==old.get('requirements') and n.get('rewards')==old.get('rewards'),key
 (adv/(key+'.json')).write_text(json.dumps(n,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
 report.append(dict(id='slavicmyths:'+key,before_parent=old.get('parent',''),after_parent=n.get('parent',''),criteria='UNCHANGED',rewards='UNCHANGED',display='ADDED' if 'display' not in old and 'display' in n else 'PRESERVED'))
for g,(ru,en,icon) in groups.items():
 key='group_'+g
 for l,title in [('ru_ru',ru),('en_us',en)]:
  locales[l]['advancements.slavicmyths.'+key+'.title']=title
  locales[l]['advancements.slavicmyths.'+key+'.description']='Раздел деяний. Отдельные узлы требуют своих настоящих условий.' if l=='ru_ru' else 'Deed section. Individual nodes require their actual conditions.'
  locales[l]['tales.group.'+g]=title
 nodes[key]=dict(parent='slavicmyths:root',display=display(key,icon),criteria={'section':{'trigger':'minecraft:tick'}})
new={
 'first_book_entry':('Открытая книга','An open book','Откройте свою книгу сказаний.','Open your tale book.','knowledge'),
 'meet_bayun':('Кот у старого пня','Cat by the old stump','Встретьте Кота Баюна.','Meet Kot Bayun.','legends'),
 'friend_of_bayun':('Доверие Баюна','Bayun’s trust','Добейтесь 85 личного доверия Баюна.','Reach 85 personal trust with Bayun.','legends'),
 'bookkeeper':('Хранитель записей','Keeper of records','Подтвердите десять записей настоящими игровыми событиями.','Verify ten records through actual gameplay events.','knowledge')}
for key,(ru,en,rd,ed,g) in new.items():
 for l,title,desc in [('ru_ru',ru,rd),('en_us',en,ed)]:
  locales[l]['advancements.slavicmyths.'+key+'.title']=title;locales[l]['advancements.slavicmyths.'+key+'.description']=desc
 d=display(key,'lore_book');d['show_toast']=True
 nodes[key]=dict(parent='slavicmyths:meet_bayun' if key=='friend_of_bayun' else 'slavicmyths:group_'+g,display=d,criteria={'confirmed':{'trigger':'minecraft:impossible'}})
for key in [*('group_'+g for g in groups),*new]:
 (adv/(key+'.json')).write_text(json.dumps(nodes[key],ensure_ascii=False,indent=2)+'\n',encoding='utf8')
 report.append(dict(id='slavicmyths:'+key,before_parent='',after_parent=nodes[key]['parent'],criteria='NEW: real event' if key in new else 'NEW: section only',rewards='NONE',display='NEW'))
for key in nodes:
 at=key;seen=set()
 while at in nodes:
  assert at not in seen,('CYCLE',key,at)
  seen.add(at);parent=nodes[at].get('parent','')
  if not parent.startswith('slavicmyths:'):break
  at=parent.split(':')[1];assert at in nodes,('MISSING PARENT',key,at)
for l,locale in locales.items():(RES/f'assets/slavicmyths/lang/{l}.json').write_text(json.dumps(locale,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
for row in report:
 key=row['id'].split(':',1)[1];n=nodes[key];d=n.get('display',{})
 row.update(existingId=row['id'] if row['criteria']=='UNCHANGED' else '',oldParent=row['before_parent'],newParent=row['after_parent'],hidden=d.get('hidden',False),existingReward=json.dumps(n.get('rewards',{}),ensure_ascii=False),bookGroup=group(key),descriptionRuKey=d.get('description',{}).get('translate',''),criteriaJson=json.dumps(n['criteria'],ensure_ascii=False),requirementsJson=json.dumps(n.get('requirements',[]),ensure_ascii=False))
with (AUDIT/'ADVANCEMENTS_BEFORE_AFTER.csv').open('w',encoding='utf8',newline='') as f:
 writer=csv.DictWriter(f,fieldnames=list(report[0]));writer.writeheader();writer.writerows(report)
print(f'{len(before)} legacy criteria/rewards preserved; {len(new)} real-event nodes and {len(groups)} section nodes; acyclic.')
