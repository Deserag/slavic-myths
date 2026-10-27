"""Version 3 presentation and explicit event contracts; PR40 save semantics are immutable."""
from pathlib import Path
import copy, json

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT/'docs/implementation/advancements-v3'
# id | parent | RU | EN | icon | frame. Each branch begins with an actual event.
ROWS = '''root||Славянские мифы|Slavic Myths|lore_book|task
first_neighbors|root|Первые соседи|First neighbors|birch_bark|task
own_harvest|first_neighbors|Свой урожай|Your own harvest|flax|goal
linen_woven|own_harvest|От стебля до полотна|From stem to cloth|linen_cloth|goal
kitchen_meal|own_harvest|У деревенского стола|At the village table|lore_book|goal
self_brewed_beer|kitchen_meal|Пенное ремесло|The brewing craft|wooden_mug|goal
full_yard|own_harvest|Полный двор|A full yard|lore_book|goal
bind_hearth|first_neighbors|Тепло родного дома|The warmth of home|hearth|goal
first_structure|root|Первый след|The first trail|birch_bark|task
find_small_camp|first_structure|Разбойничья тропа|The bandit trail|iron_spear|task
find_large_camp|find_small_camp|Большой стан|The great camp|iron_spear|goal
explore_abandoned_settlement|first_structure|Заброшенное селение|An abandoned settlement|pine_log|task
explore_gorodishche|explore_abandoned_settlement|Земля наших предков|Land of our ancestors|pine_log|goal
traces_of_past|first_structure|Тропы топей|Wetland trails|willow_log|task
bog_explorer|traces_of_past|Исследователь топей|Wetland explorer|willow_log|goal
find_deep_pool|bog_explorer|Скрытый омут|The hidden pool|lore_book|goal
all_timber|first_structure|От липы до сосны|From linden to pine|linden_log|goal
ancient_find|first_structure|Отголосок старины|An echo of the past|ancient_coin|task
explore_kurgan|ancient_find|Курганная тропа|The burial trail|ancient_coin|task
found_ancient_sanctuary|ancient_find|Древнее святилище|An ancient sanctuary|idol_fragment|task
found_two_sanctuaries|found_ancient_sanctuary|Огонь и вода|Fire and water|idol_fragment|goal
claimed_fire_fragment|found_two_sanctuaries|Очищенное пламя|Cleansed flame|mor_fire_amulet_fragment|goal
claimed_water_fragment|found_two_sanctuaries|Чистый источник|A pure spring|mor_water_amulet_fragment|goal
first_book_entry|ancient_find|Открытая книга|An open book|lore_book|task
bookkeeper|first_book_entry|Хранитель сказаний|Keeper of tales|lore_book|goal
first_ritual|first_book_entry|Ответ на дар|An answer to a gift|idol_fragment|goal
three_signs|first_ritual|Три знака|Three signs|ancient_sign|goal
rare_accessory|three_signs|Сказочный оберег|A fairy-tale charm|warding_charm|goal
explore_chud_city|first_structure|Город Чуди|The Chud city|silver_ingot|goal
chud_halls|explore_chud_city|Залы под горой|Halls beneath the mountain|silver_ingot|goal
explore_chud_abandoned|explore_chud_city|Заброшенные владения|Abandoned domains|silver_ingot|goal
chud_labyrinth|explore_chud_abandoned|Сердце лабиринта|Heart of the labyrinth|silver_ingot|challenge
meet_bandits|root|Незваные гости|Uninvited guests|iron_spear|task
clear_large_camp|meet_bandits|Стан обезврежен|The camp is cleared|iron_spear|goal
without_ataman|clear_large_camp|Без атамана|Without an Ataman|iron_spear|goal
defeat_nightingale|without_ataman|Свист умолк|The whistle falls silent|mysterious_black_feather|challenge
steppe_champion|meet_bandits|Степной богатырь|Champion of the steppe|iron_spear|challenge
name_your_misfortune|steppe_champion|Лихая доля|An ill fate|warding_charm|challenge
endured_frost|name_your_misfortune|Выдержавший стужу|Enduring the frost|lore_book|challenge
endured_last_night|endured_frost|Последняя ночь|The last night|lore_book|challenge
defeat_idolishche|steppe_champion|Рухнувший исполин|The fallen giant|iron_spear|challenge
defeat_skim|defeat_idolishche|Долина свободна|The valley is free|iron_spear|challenge
defeat_kartaus|defeat_skim|Чёрное знамя пало|The black banner falls|iron_spear|challenge
defeat_kalin|defeat_kartaus|Сломленная рать|A broken army|iron_spear|challenge
defeat_marinka|defeat_idolishche|Чары рассеяны|The spell is broken|warding_charm|challenge
defeat_gorynych|defeat_marinka|Три головы, одна воля|Three heads, one will|lore_book|challenge
own_path|meet_bandits|Свой путь|Your own path|lore_book|task
class_iv|own_path|Имя в предании|A name in legend|lore_book|challenge
defeat_leshy|meet_bandits|Лес помнит|The forest remembers|pine_log|goal
extinguish_the_barn|defeat_leshy|Загасить овин|Extinguish the barn|lore_book|goal
silver_remedy|extinguish_the_barn|Серебряное средство|A silver remedy|silver_ingot|goal
defeat_depth_master|defeat_leshy|Омут больше не зовёт|The pool calls no more|lore_book|challenge
night_watch|defeat_leshy|До рассвета|Until dawn|warding_charm|goal
kurgan_rest|meet_bandits|Покой кургана|Rest for the burial mound|ancient_coin|goal
depth_champions|kurgan_rest|Подземные стражи|Underground guardians|silver_ingot|goal
earth_heart|depth_champions|Сердце земли|Heart of the earth|silver_ingot|challenge
great_poloz_fall|earth_heart|Падение Полоза|The fall of Poloz|silver_ingot|challenge
tales_come_alive|root|Сказки оживают|Tales come alive|lore_book|task
find_the_hut|tales_come_alive|Избушка на курьих ножках|The chicken-legged hut|pine_log|task
guest_of_yaga|find_the_hut|Гость Яги|Yaga's guest|lore_book|goal
yaga_thread_earned|guest_of_yaga|Путеводный клубок|The guiding thread|lore_book|goal
trusted_by_yaga|yaga_thread_earned|Доверенный Яги|Trusted by Yaga|lore_book|challenge
cauldron_service|yaga_thread_earned|Тайны Ягиного котла|Secrets of Yaga's cauldron|lore_book|goal
first_flight|yaga_thread_earned|Над лесом|Above the forest|lore_book|goal
talk_to_bayun|tales_come_alive|Сказитель на тропе|A storyteller on the trail|lore_book|task
bayun_new_tale|talk_to_bayun|Сказки Баюна|Bayun's tales|lore_book|goal
friend_of_bayun|bayun_new_tale|Доверие Баюна|Bayun's trust|lore_book|goal
bayun_song|talk_to_bayun|Песня Баюна|Bayun's song|lore_book|goal
mor_invitation|tales_come_alive|Мор зовёт|Mor calls|lore_book|task
mor_story_complete|mor_invitation|Последняя страница расследования|The investigation's last page|lore_book|challenge
wolf_trial_passed|tales_come_alive|Испытание Серого Волка|The Grey Wolf's trial|lore_book|goal
grey_wolf_bandits|wolf_trial_passed|Волчья охота|The wolf's hunt|iron_spear|goal
grey_wolf_kurgan|grey_wolf_bandits|Курганный долг|A burial debt|ancient_coin|goal
grey_wolf_likho|grey_wolf_kurgan|По следу Лиха|On Likho's trail|warding_charm|goal
curse_resolved|grey_wolf_likho|Проклятый Волколак|The Cursed Volkolak|warding_charm|challenge'''

EVENTS = {
 'yaga_thread_earned':('Завершите второе основное поручение Яги и получите клубок.',"Complete Yaga's second main task and receive the guiding thread."),
 'first_neighbors':('Поговорите с жителем новой профессии мода.','Talk to a villager with a mod profession.'),
 'own_harvest':('Посадите культуру мода и соберите её зрелой.','Plant a mod crop and harvest it when mature.'),
 'linen_woven':('Изготовьте льняное полотно.','Craft linen cloth.'),
 'self_brewed_beer':('Завершите собственное брожение пива в бродильном чане.','Complete your own beer fermentation in a fermentation vat.'),
 'first_structure':('Обнаружьте любую зарегистрированную постройку мода.','Discover any registered mod structure.'),
 'explore_gorodishche':('Найдите городище.','Find a gorodishche.'),
 'explore_kurgan':('Найдите малый, воинский или великий курган.','Find a small, warrior or great burial mound.'),
 'explore_chud_city':('Обнаружьте подземный город Чуди.','Discover an underground Chud city.'),
 'explore_chud_abandoned':('Обнаружьте заброшенный комплекс Чуди.','Discover an abandoned Chud complex.'),
 'chud_halls':('Посетите все главные залы одного города Чуди.','Visit every major hall of one Chud city.'),
 'chud_labyrinth':('Войдите в настоящий проход лабиринта заброшенного комплекса Чуди.','Enter an actual maze passage of an abandoned Chud complex.'),
 'meet_bandits':('Встретьте живого разбойника в пределах видимости.','Encounter a living bandit within sight.'),
 'tales_come_alive':('Вступите в настоящее взаимодействие с Ягой, Баюном или Серым Волком.','Have a real interaction with Yaga, Bayun or the Grey Wolf.'),
 'talk_to_bayun':('Начните настоящий разговор с Котом Баюном.','Begin a real conversation with Kot Bayun.'),
 'bayun_new_tale':('Узнайте от Баюна ранее неизвестное предание.','Learn a previously unknown tale from Bayun.'),
 'bayun_song':('Переживите настоящие чары Баюна и освободитесь, оставаясь в живых.','Survive Bayun\'s actual charm and be released alive.'),
 'mor_invitation':('Получите приглашение Яги или Баюна к расследованию Мора.','Receive Yaga\'s or Bayun\'s invitation to investigate Mor.'),
 'mor_story_complete':('Завершите шестую основную главу расследования Мора.','Complete the sixth main chapter of the Mor investigation.'),
 'wolf_trial_passed':('Пройдите настоящее испытание Серого Волка и получите признание.','Pass the Grey Wolf\'s actual trial and earn recognition.'),
 'grey_wolf_bandits':('Завершите и сдайте поручение grey_wolf_bandits: 15 разбойников.','Complete and claim grey_wolf_bandits: 15 bandits.'),
 'grey_wolf_kurgan':('Завершите и сдайте поручение grey_wolf_kurgan против главного стража.','Complete and claim grey_wolf_kurgan against the main guardian.'),
 'grey_wolf_likho':('Завершите и сдайте grey_wolf_likho против связанного Лиха.','Complete and claim grey_wolf_likho against the bound Likho.'),
}

def tree():
    old={n['id']:n for n in json.loads((ROOT/'docs/implementation/advancements-redesign/TREE.json').read_text('utf8'))}
    result=[]
    for line in ROWS.splitlines():
        key,parent,ru,en,icon,frame=line.split('|')
        # Existing accurate descriptions are retained; restored legacy displays get their existing locale text.
        result.append(dict(id=key,parent=parent or None,title_ru=ru,title_en=en,icon='slavicmyths:'+icon,frame=frame,
                           show_toast=key!='root' and key not in {'guest_of_yaga','yaga_thread_earned','grey_wolf_bandits','grey_wolf_kurgan','grey_wolf_likho'},
                           announce_to_chat=frame=='challenge',hidden=False,
                           description_ru=EVENTS.get(key,('', ''))[0] or old.get(key,{}).get('description_ru',''),
                           description_en=EVENTS.get(key,('', ''))[1] or old.get(key,{}).get('description_en','')))
    assert len(result)==75
    return result

def location(structures):
    return {'trigger':'minecraft:location','conditions':{'player':[{'condition':'minecraft:entity_properties','entity':'this','predicate':{'location':{'structures':structures}}}]}}

def apply(root=ROOT):
    res=root/'src/main/resources'; dest=res/'data/slavicmyths/advancement'
    base=json.loads((OUT/'BASE_PR40.json').read_text('utf8')); nodes=copy.deepcopy(base)
    original={n['id'].split(':',1)[-1]:n['resource'] for n in json.loads((ROOT/'docs/implementation/advancements-redesign/SOURCE.json').read_text('utf8'))['nodes']}
    locales={l:json.loads((res/f'assets/slavicmyths/lang/{l}.json').read_text('utf8')) for l in ('ru_ru','en_us')}
    specs=tree();visible={s['id']:s for s in specs}
    for key,n in nodes.items():
        if not key.startswith('recipes/') and key not in visible:n.pop('display',None)
    for spec in specs:
        key=spec['id'];n=nodes.setdefault(key,{'criteria':{'event':{'trigger':'minecraft:impossible'}},'requirements':[['event']],'rewards':{}})
        if key not in base:
            structures={'first_structure':'#slavicmyths:advancement_discoveries','explore_gorodishche':['slavicmyths:gorodishche'],
                        'explore_kurgan':['slavicmyths:kurgan_small','slavicmyths:kurgan_warrior','slavicmyths:kurgan_great'],
                        'explore_chud_city':['slavicmyths:chud_city'],'explore_chud_abandoned':['slavicmyths:chud_abandoned']}.get(key)
            if structures:n['criteria']['event']=location(structures)
        if spec['parent']:n['parent']='slavicmyths:'+spec['parent']
        else:n.pop('parent',None)
        d=copy.deepcopy(n.get('display',{}));d.update(icon={'id':spec['icon']},frame=spec['frame'],show_toast=spec['show_toast'],announce_to_chat=spec['announce_to_chat'],hidden=False)
        for field in ('title','description'):
            translation=d.get(field,{}).get('translate',f'advancements.slavicmyths.{key}.{field}')
            for locale,lang in [('ru_ru','ru'),('en_us','en')]:
                value=spec[field+'_'+lang] or locales[locale].get(translation,'')
                if not value:
                    legacy=original.get(key,{}).get('display',{}).get(field,{}).get('translate','')
                    value=locales[locale].get(legacy,'')
                if not value:raise ValueError((key,locale,field))
                spec[field+'_'+lang]=value;locales[locale][translation]=value
            d[field]={'translate':translation}
        n['display']=d
    for key,node in nodes.items():
        path=dest/(key+'.json');path.parent.mkdir(parents=True,exist_ok=True)
        if key.startswith('recipes/'):continue # byte-for-byte recipe unlock preservation
        path.write_text(json.dumps(node,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
    tag=res/'data/slavicmyths/tags/worldgen/structure/advancement_discoveries.json';tag.parent.mkdir(parents=True,exist_ok=True)
    tag.write_text(json.dumps({'replace':False,'values':['slavicmyths:'+p.stem for p in sorted((res/'data/slavicmyths/worldgen/structure').glob('*.json'))]},indent=2)+'\n',encoding='utf8')
    for locale,labels in [('ru_ru',['Мирная жизнь','Исследование мира','Сражения','Сказочные истории']),('en_us',['Peaceful life','World exploration','Combat','Fairy-tale stories'])]:
        for key,label in zip(['peaceful','exploration','combat','stories'],labels):locales[locale]['tales.group.'+key]=label
    for locale,value in locales.items():(res/f'assets/slavicmyths/lang/{locale}.json').write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
    OUT.mkdir(parents=True,exist_ok=True)
    (OUT/'TREE.json').write_text(json.dumps(specs,ensure_ascii=False,indent=2)+'\n',encoding='utf8')

if __name__=='__main__':apply()
