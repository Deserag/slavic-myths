from pathlib import Path
import json
root=Path(__file__).resolve().parents[1]
labels={
 'title':('Метки Slavic Myths','Slavic Myths markers'),'markers':('Метки','Markers'),'filters':('Фильтры','Filters'),
 'track':('Отслеживать','Track'),'stop':('Прекратить','Stop tracking'),'hide':('Скрыть','Hide'),'show':('Показать','Show'),
 'forget':('Забыть','Forget'),'save_nearby':('Сохранить место','Save nearby location'),'empty':('Известных мест пока нет.','No discoveries yet.'),
 'yaga_hut':('Избушка Бабы-яги',"Baba Yaga's hut"),
 'thread_unresolved':('Клубок кружится, но путь пока не открылся.','The thread ball spins, but the path has not opened yet.'),
 'no_known_encounter':('Задание принято. Пока нет известных следов подходящей цели; навигация появится, когда сервер обнаружит её район.','Quest accepted. No known encounter yet; guidance will appear when a real encounter is known.'),
 'no_plausible_area':('Не удалось определить подходящую область поиска. Точная цель остаётся скрытой.','No plausible search area found. The exact target remains hidden.'),
 'target_discovered':('Цель найдена.','Target discovered.'),'tracking_ended':('Отслеживаемая цель больше не активна.','Tracked target is no longer active.'),
 'entered':('Вы вошли в область поиска.','You entered the search area.'),
 'inside':('Вы в области поиска: ищите следы цели.','Inside the search area: look for traces of the target.'),
 'target_name':('Цель: %s','Target: %s'),'search_name':('Район поиска: %s','Search region: %s'),
 'distance':('%s %s м','%s %s m'),'area_distance':('До области поиска: %s %s м','To search area: %s %s m'),
 'area_name':('Район поиска: %s — ~%s м','Search region: %s — ~%s m'),
 'other_dimension':('Цель в другом измерении: %s','Target in another dimension: %s'),
 'no_new_location':('Рядом нет нового открытого строения.','No new discovered structure nearby.'),
 'test_area':('Тестовая область поиска','Developer search area'),
 'preference.always_tracked':('Всегда показывать выбранную цель','Always show tracked marker'),
 'preference.auto_quest':('Автоотслеживание новой цели задания','Auto-track new quest target'),
 'preference.auto_thread':('Автоотслеживание цели клубка','Auto-track thread ball target'),
 'preference.fallback_hud':('Подсказка направления на экране','Fallback navigation HUD'),
 'preference.xaero':('Метки в Xaero','Xaero integration'),
 'preference.hide_completed':('Скрывать завершённые события','Hide completed events'),
 'preference.auto_discovery':('Сохранять важные открытия','Auto-save major discoveries'),
}
for key,ru,en in [('yaga','Баба-яга','Baba Yaga'),('waystone','Путевые камни','Path Stones'),('kurgan','Курганы','Kurgans'),('bandit','Разбойники','Bandits'),('boss','Боссы','Bosses'),('quest','Задания','Quests'),('special_location','Особые места','Special locations'),('event','События','Events')]:labels['category.'+key]=(ru,en)
for key,ru,en in [('assigned','Область назначена','Assigned'),('approaching','До области поиска','Approaching'),('inside_search_area','В области поиска','Inside search area'),('target_discovered','Цель обнаружена','Target discovered'),('discovered','Открыто','Discovered'),('completed','Завершено','Completed'),('expired','Неактивно','Expired')]:labels['state.'+key]=(ru,en)
structures={'kurgan_small':('Малый курган','Small kurgan'),'kurgan_warrior':('Воинский курган','Warrior kurgan'),'kurgan_great':('Великий курган','Great kurgan'),'bandit_camp_small':('Разбойничий лагерь','Bandit camp'),'bandit_camp_medium':('Разбойничья база','Bandit base'),'bandit_camp_large':('Разбойничье поселение','Bandit settlement'),'swamp_hut':('Болотная хижина','Swamp hut'),'abandoned_settlement':('Заброшенное поселение','Abandoned settlement'),'bog_causeway':('Болотная гать','Bog causeway'),'flooded_shrine':('Затопленное святилище','Flooded shrine'),'fishing_camp':('Рыбацкий лагерь','Fishing camp'),'underwater_ruins':('Подводные руины','Underwater ruins'),'swamp_remnants':('Болотные остатки','Swamp remnants')}
for key,values in structures.items():labels['structure.'+key]=values
for locale,index in [('ru_ru',0),('en_us',1)]:
 p=root/'src/main/resources/assets/slavicmyths/lang'/f'{locale}.json';lang=json.loads(p.read_text(encoding='utf-8'))
 lang.update({'navigation.slavicmyths.'+key:values[index] for key,values in labels.items()})
 lang['key.slavicmyths.navigation']=('Навигация Slavic Myths','Slavic Myths navigation')[index]
 lang['item.slavicmyths.putevodny_klubok.effect']=('Навсегда запоминает реально размещённую избушку. Перезарядка: 4 секунды.',"Permanently remembers the actually placed hut. Cooldown: 4 seconds.")[index]
 p.write_text(json.dumps(lang,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

# Make the public navigation branch usable while retaining OP gates on every pre-existing branch.
for p in (root/'src/main/java/org/slavicmyths').rglob('*Commands.java'):
 if p.parent.name=='navigation':continue
 s=p.read_text(encoding='utf-8');old='Commands.literal("slavicmyths").requires(s->s.hasPermission(2))'
 if old not in s:continue
 s=s.replace(old,'Commands.literal("slavicmyths")')
 for branch in ['dev','locate']:
  s=s.replace(f'Commands.literal("{branch}")',f'Commands.literal("{branch}").requires(source->source.hasPermission(2))')
 if p.name=='KurganCommands.java':s=s.replace('modern=Commands.literal("kurgan")','modern=Commands.literal("kurgan").requires(source->source.hasPermission(2))')
 p.write_text(s,encoding='utf-8')
print('Navigation localization and command permission boundaries updated.')
