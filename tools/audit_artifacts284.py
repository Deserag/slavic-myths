"""Current artifact registry/source/acquisition index. No generators or game starts."""
from pathlib import Path
import json,re,subprocess
R=Path(__file__).resolve().parents[1]; RES=R/'src/main/resources'; OUT=R/'docs/implementation/artifacts284-20261025'
# role, current mechanic, canonical source, progression/acquisition interpretation
FACTS={
'thunder_stone':('INGREDIENT','Ритуальный материал; не активная граната/оружие','registry/ModItems.java;ritual/Rituals.java','Капища/таблицы ниже → ритуалы Перуна/посоха/оружия'),
'gusli':('ACTIVE','Удерживать ПКМ; pulses10 ticks, exposure30; r12, cap64; calm60ticks; release CD600/100; иммунны guardian/boss/mod mobs кроме wildlife','artifact/GusliItem.java;artifact/ArtifactEvents.java','Штатный рецепт → удерживать рядом с обычным мобом → прекращение target/атаки → снять; не успокаивает всех боссов'),
'skatert':('PLACED_ARTIFACT','BlockItem:6 portions по6food/.6 saturation; eat CD24; lifetime2400; pickup returns one cloth with rest6000 stored in component','artifact/SkatertItem.java;artifact/SkatertBlock.java;artifact/SkatertTile.java','Штатный рецепт → поставить на опору → поесть → забрать/дождаться возврата → отдых5мин'),
'flying_broom':('VEHICLE','Разместить→владелец садится; серверный input; одно место; упаковать crouch-hit без riders/viewers; vessel components сохраняются','flight/FlightItem.java;flight/FlyingVessel.java;flight/FlightNetwork.java','Сделка/поручения Яги → поставить → сесть → полёт → безопасно слезть → упаковать'),
'flying_mortar':('VEHICLE','Два места/явный pilot; требует pestle в руке;9 cargo; ownership/input validation; pack preserves cargo','flight/FlightItem.java;flight/FlyingVessel.java;flight/CargoMenu.java','Поручения/обмен Яги → pestle → поставить/сесть → input/багаж → посадка/упаковка'),
'pestle':('CONTROL_KEY','Plain Item; FlyingVessel требует его в любой руке для управления ступой; не отдельное оружие','registry/ModItems.java;flight/FlyingVessel.java','Штатный рецепт/Яга → держать в ступе → работает управление'),
'mor_flask':('DEBUG_CONSUMABLE','По tooltip: для испытаний. Бросок, CD10, splash r3 запускает MorController.apply(false): превращает обычного подходящего духа в моровую форму; не очищает','mor/MorFlaskItem.java;mor/MorFlaskProjectile.java;mor/MorController.java;mor/MorRules.java','Технический предмет для испытаний; survival recipe/loot/reward не найден и не добавляется; бросок → заражение обычного духа'),
'oko_likha':('QUEST_INGREDIENT','HuntItems.Role.FANG: не активный ПКМ; boss ingredient и сюжетный ключ, не искатель сам по себе','hunt/HuntItems.java;registry/ModItems.java','Победа над Лихом/штатный трофей → рецепты/сюжет; не новая способность обнаружения'),
'wolf_call':('ALLY_SUMMON','WolfUnlocked gate; один support UUID; expires2400, CD6000; refuses active/unloaded lease; combat renews expiry','quest185/Quest185Item.java;quest185/WolfSupport.java;quest185/Quest185Story.java','Цепочка очищения земли у Яги/Волка → WolfUnlocked → ПКМ → временный боевой представитель Волка'),
'tracker_sense':('ACTIVE_EFFECT','Tracker effect3600; Ready12000 overworld clock; повтор active/CD fail; player saved quest data','quest185/Quest185Item.java;quest185/Quest185Events.java;quest185/Quest185Story.java','Сюжет185 → ПКМ → временное видение/поиск предусмотренных заражённых целей'),
'sivka_whistle':('BOUND_SUMMON','Owner/bond/token/generation checks; play-time CD/recovery; safe loaded spot; retire previous only after successful spawn','sivka/SivkaWhistle.java;sivka/SivkaBonds.java;sivka/SivkaRules.java','Естественная Сивка → испытание/кормление/личная связь → свисток → вызов; чужой/истёкший запрещён'),
'ilya_call_token':('STORY_ALLY','Channel12 of22 ticks; HeroCalls.toggle; canonical UUID maps; StoryPolicy/party/lease/cooldown gates','skim/HeroCallItem.java;skim/HeroCalls.java;marinka/StoryPolicy.java','Штатная богатырская история → personal unlock → вызов/отзыв; не ранний сундук'),
'dobrynya_call_emblem':('STORY_ALLY','Channel12/22; HeroCalls.toggle; actual Dobrynya release gate, canonical owner/maps','skim/HeroCallItem.java;skim/HeroCalls.java;marinka/StoryPolicy.java','Спасти Добрыню в истории Маринки → вызов/отзыв союзника'),
'alyosha_call_whistle':('STORY_ALLY','Channel12/22; HeroCalls.toggle; actual story gate, party cap and lease','skim/HeroCallItem.java;skim/HeroCalls.java;marinka/StoryPolicy.java','История Алёши/Скима → разрешённый вызов/отзыв'),
'finist_call_feather':('STORY_ALLY','Channel15/28; HeroCalls.toggle finist; success CD40/failure8; canonical hero maps, story gate','finist/FalconCallItem.java;skim/HeroCalls.java;marinka/StoryPolicy.java','История Финиста → перо → вызов/отзыв без копий'),
'kartaus_black_seal':('BOSS_KEY','Channel8/40; KartausEncounter.start; story/encounter validation; CD40/8','finist/FalconCallItem.java;finist/KartausEncounter.java','История Финиста → печать → запуск штатной встречи Картауса'),
'kalin_battle_seal':('BOSS_KEY','KalinEncounter.start; story party gates; success CD100/fail20; not consumed','kalin/KalinSeal.java;kalin/KalinEncounter.java','После требуемых богатырских историй → печать → организованная битва Калина'),
'gorynych_mor_breath_bottle':('UPGRADE_INGREDIENT','Plain Item + glass craft remainder; component for three_breath_crossbow, not throwable; Mor weapon mode creates80tick weakness/damage cloud','gorynych/GorynychContent.java;gorynych/GorynychUpgrade.java;gorynych/BreathCloud.java;gorynych/SerpentCrossbow.java','Собрать бутылкой облако моровой головы живого Горыныча → upgrade crossbow → режим mor'),
'gorynych_flame_breath_bottle':('UPGRADE_INGREDIENT','Plain Item + glass craft remainder; upgrade ingredient; flame crossbow/rune burst damage/fire, not direct bottle use','gorynych/GorynychContent.java;gorynych/GorynychUpgrade.java;gorynych/GorynychEquipment.java','Собрать огненное облако Горыныча → upgrade → режим пламени'),
'gorynych_storm_breath_bottle':('UPGRADE_INGREDIENT','Plain Item + glass craft remainder; upgrade ingredient; storm mode uses StormCrossbow bounded targets/control/cooldown','gorynych/GorynychContent.java;gorynych/GorynychUpgrade.java;gorynych/StormCrossbow.java','Собрать штормовое облако → upgrade → режим шторма; значения не меняются'),
'warding_charm':('RITUAL_INGREDIENT','Plain Item; FirstRitual/Rituals/legacy PathData offerings, not Curios passive','registry/ModItems.java;ritual/FirstRitual.java;ritual/Rituals.java','Крафт → ритуальный компонент → янтарный оберег/ритуалы'),
'volkhv_amulet':('DESIGN_UNDECIDED','Plain Item; нет собственного equipment/use handler; отсутствие способности не объявляется дефектом без заявленной роли','registry/ModItems.java','Штатный рецепт/loot index → коллекционный/ритуальный предмет; новую способность/слот утверждает владелец'),
'fading_flame_charm':('CONSUMABLE_EFFECT','ПКМ: GUARD на WARD_SECONDS*20; active rejects; consume1 only after effect accepted; fire/mor_block damage reduced by SanctuaryConfig.GUARD','sanctuary/SanctuaryItem.java;sanctuary/SanctuaryConfig.java;sanctuary/SanctuaryEvents.java','Святилище/рецепт → ПКМ → временная защита от огня/mor_block и сокращение mor duration по SanctuaryEvents'),
'mor_ward':('CONSUMABLE_EFFECT','ПКМ WARD24000 ticks; active rejects; ordinary consumes1; GuardianWard196 reusable special recipe','quest185/Quest185Item.java;quest185/Quest185Events.java;gorynych/GorynychUpgrade.java','Сюжет/крафт → ПКМ → mor protection; особая версия из двух fragment + Око Мора сохраняется'),
'cleansing_powder':('RITUAL_TOOL','CleansingTasks.start; timed cleaning with permissions/cancel/saved task state','quest185/CleansingPowder.java;quest185/CleansingTasks.java','Сюжет/рецепт → применять на заражённой земле/голове Горыныча по этапу → очищение'),
'purified_water_bottle':('CONSUMABLE','Drink32; shorten finite slowness/weakness/poison by SanctuaryRules; preserve protected story effects; glass return','sanctuary/SanctuaryItem.java;sanctuary/SanctuaryRules.java','Святилище воды/крафт → выпить или применить на огненной голове по этапу → очищение'),
'spring_tear':('CONSUMABLE','Drink32, TEAR160ticks, heal1 on configured ticks; per-player TEAR_COOLDOWN; no bottle remainder','sanctuary/SanctuaryItem.java;sanctuary/SanctuaryRules.java;sanctuary/SanctuaryConfig.java','Чистый источник/награда → выпить при ранении → постепенное лечение'),
'flame_vial':('CONSUMABLE','Projectile spawn success required; consume1 survival; CD20','sanctuary/SanctuaryItem.java;sanctuary/FlameVial.java','Огненное святилище/рецепт → бросить → существующее воздействие огня'),
'purification_stone':('INGREDIENT','SanctuaryItem без active use; обязательный компонент fading_flame_charm, не боевой оберег','sanctuary/SanctuaryItem.java;sanctuary/SanctuaryContent.java','Лут Чистого источника → рецепт оберега угасающего пламени; нет своего ПКМ'),
'beast_challenge_horn':('ENCOUNTER_KEY','Channel20/38; SkimValley.start; story gate; CD300; not consumed','skim/HeroCallItem.java;skim/SkimValley.java','История Алёши → рог → предусмотренная встреча Скима'),
'mor_horn':('ENCOUNTER_KEY','Владение рогом разрешает60tick call; Overworld, один world event; первый вызов без dust, повтор с платой по commit; нет отдельного story gate','poloz/MorHorn.java;poloz/PolozEvent.java','Штатный крафт/источник рога → удерживать60ticks без прерывания → встреча Полоза; факт владения, не новая проверка истории'),
'obereg_ohotnika':('DETECTOR','ПКМ bounded HuntItems.detect; CD400; not Curios','hunt/HuntItems.java','Штатный рецепт → ПКМ → ближайшая подходящая охотничья цель'),
'hunter_charm':('DETECTOR','ПКМ HuntItems.detect; CD400; tooltip corrected in #278; not passive','item/SimpleCharmItem.java;hunt/HuntItems.java','Штатный рецепт → ПКМ → поиск; #278 already fixed text, no duplicate edit'),
'amulet_two_winters':('INVENTORY_ACTIVE','Существующие режимы/инвентарный passive и Nether use; не переносить в Curios','winters/WintersItem.java;winters/WintersEvents.java;winters/WintersRules.java','Морозко+Карачун → поздний амулет → выбранный режим; отдельные cooldown/условия')
}
def run():
 OUT.mkdir(parents=True,exist_ok=True)
 catalog={f[0]:f[1:] for l in (RES/'assets/slavicmyths/creative_catalog.tsv').read_text(encoding='utf8').splitlines() if l and not l.startswith('#') for f in [l.split('\t')]}
 required={id for id,cat in catalog.items() if cat in [['magic','artifacts'],['magic','charms']]}
 assert len(required)==24 and required<=FACTS.keys()
 java={p.relative_to(R/'src/main/java/org/slavicmyths').as_posix():p.read_text(encoding='utf8') for p in (R/'src/main/java/org/slavicmyths').rglob('*.java')}
 data={p.relative_to(R).as_posix():json.loads(p.read_text(encoding='utf8')) for p in (RES/'data/slavicmyths').rglob('*.json') if '/recipe/' in p.as_posix() or '/loot_table/' in p.as_posix()}
 lang=json.loads((RES/'assets/slavicmyths/lang/ru_ru.json').read_text(encoding='utf8'));entries=[]
 for id,(role,effect,source,path) in FACTS.items():
  assert id in catalog,id
  for s in source.split(';'):assert s in java,(id,s)
  needle='slavicmyths:'+id;recipes=[];loot=[];ingredients=[];refs=[]
  for file,d in data.items():
   if '/recipe/' in file:
    result=d.get('result',{})
    if isinstance(result,dict) and (result.get('id')==needle or result.get('item')==needle):recipes.append(file)
    elif needle in json.dumps(d):ingredients.append(file)
   elif re.search(r'"name":\s*"'+needle+'"',json.dumps(d)):loot.append(file)
  pattern=re.compile(r'\b'+id.upper()+r'\b')
  for file,s in java.items():
   if id.upper() not in s and '"'+id+'"' not in s:continue
   for no,line in enumerate(s.splitlines(),1):
    if pattern.search(line) or '"'+id+'"' in line:refs.append({'path':'src/main/java/org/slavicmyths/'+file,'line':no,'text':line.strip()})
  acquiring=[];survival='SOURCE_PATH_FOUND' if recipes or loot else 'DYNAMIC_SOURCE_REQUIRED'
  if id in ['gusli','skatert','kartaus_black_seal','kalin_battle_seal']:
   survival='BLOCKED_NEEDS_ACQUISITION_APPROVAL';path='Survival recipe/loot/trade/reward не найден в текущих production источниках. Механика существует; новый способ получения и стоимость утверждает владелец.'
  if id=='mor_flask':survival='INTENTIONAL_DEBUG_TOOLTIP_NO_SURVIVAL'
  if id in ['flying_broom','flying_mortar','pestle']:
   acquiring=['yaga/YagaServices.java:EXCHANGES'];survival='YAGA_EXCHANGE_STAGE_2' if id=='flying_broom' else 'YAGA_EXCHANGE_STAGE_3'
   if id=='pestle':path='Яга stage3: 4 svyazka_trav_yagi +2 uzel_podveya +8 iron_ingot → pestle → управление ступой'
  if id in ['ilya_call_token','dobrynya_call_emblem','alyosha_call_whistle','finist_call_feather']:
   acquiring=['bogatyr/HeroIntroductions.java:offer/interact/deliver'];survival='PERSONAL_INTRODUCTION_HELP_REWARD'
   path='Яга/Баюн → поручение знакомства с соответствующим героем → выполнить помощь (Helped) → получить его токен → вызов/отзыв; possession не заменяет Helped'
  if id=='wolf_call':acquiring=['quest185/Quest185Story.java:reward(8)/claim'];survival='QUEST_185_REWARD_8_WOLF_UNLOCKED'
  if id=='sivka_whistle':acquiring=['sivka/SivkaBonds.java:give/stamp'];survival='PERSONAL_SIVKA_BOND'
  if id=='amulet_two_winters':recipes=['src/main/resources/data/slavicmyths/recipe/two_winters_original.json','src/main/resources/data/slavicmyths/recipe/two_winters_copy.json'];acquiring=['winters/WintersRecipe.java'];survival='CUSTOM_PERSONAL_RECIPE'
  if id.startswith('gorynych_') and id.endswith('_breath_bottle'):acquiring=['gorynych/BreathCloud.java:interact'];ingredients.append('src/main/resources/data/slavicmyths/recipe/gorynych_upgrade.json');survival='BOSS_CLOUD_COLLECTION_3_USES'
  if id in ['mor_ward','tracker_sense']:acquiring=['quest185/Quest185Story.java:UtilityRecipes/reward','equipment/mixin/Quest185CraftingMixin.java','equipment/mixin/Quest185CrafterMixin.java'];survival='QUEST_GATED_PERSONAL_RECIPE'
  for code in acquiring:assert code.split(':')[0] in java,(id,code)
  entries.append(dict(id=id,name=lang.get('item.slavicmyths.'+id,lang.get('block.slavicmyths.'+id)),category=catalog[id],role=role,actual=effect,player_path=path,source=source.split(';'),recipes=recipes,loot=loot,ingredient_in=ingredients,acquisition_code=acquiring,survival=survival,java_refs=refs,tooltip={k:v for k,v in lang.items() if id in k and (k.endswith('.desc') or k.endswith('.effect') or k.startswith('tooltip.'))},before='server rejection consumed item/cooldown (baseline GameTest FAIL)' if id in ['mor_flask','fading_flame_charm','mor_ward','tracker_sense','spring_tear'] else 'existing implementation',change='commit consumption/cooldown only after native success' if id in ['mor_flask','fading_flame_charm','mor_ward','tracker_sense','spring_tear'] else 'KEEP / source audit',gameplay='NOT_RUN unless specific test listed in runtime.json',loot_proposal='HOLD: new rewards/rarity require #276 approval; story/unique never random chest',status=survival if survival.startswith('BLOCKED') else 'DESIGN_UNDECIDED' if role=='DESIGN_UNDECIDED' else 'SOURCE_AUDITED_OWNER_PENDING'))
 runtime=OUT/'runtime.json'
 if runtime.exists():
  coverage=json.loads(runtime.read_text(encoding='utf8'))['per_item_coverage']
  for e in entries:e['server_tests']=coverage.get(e['id'],['all34RegistryAndAcquisitionRecipes (registry/loaded recipes only)'])
 out={'base_commit':subprocess.check_output(['git','rev-parse','HEAD'],cwd=R,text=True).strip(),'required_catalog_count':24,'total_entries':len(entries),'scope':'20 artifacts +4 magic charms +10 existing functional relics; no wearable Curios duplication','items':entries,'runtime':'specific server tests in runtime.json; natural acquisition/GUI/real relog/MP NOT_RUN'}
 (OUT/'catalog.json').write_text(json.dumps(out,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
 lines=['# Аудит перед правками #284','',f'24 обязательных + {len(entries)-24} найденных функциональных/сюжетных реликвий. Source evidence не равно игровой приёмке.','', '| Item ID | Название | Назначение | Получение/PLAYER_PATH | Было | Исправлено | Проверка | Статус |','|---|---|---|---|---|---|---|---|']
 for e in entries:lines.append('| '+' | '.join([e['id'],e['name'],e['role']+': '+e['actual'],e['player_path']+'; '+e['survival']+'; '+', '.join(e['recipes']+e['loot']+e['acquisition_code']),e['before'],e['change'],'; '.join(e.get('server_tests',[]))+'; source: '+';'.join(e['source'])+'; natural acquisition/relog/MP NOT_RUN',e['status']])+' |')
 (OUT/'CATALOG.md').write_text('\n'.join(lines)+'\n',encoding='utf8');print('ARTIFACT284_SOURCE_AUDIT entries='+str(len(entries)))
if __name__=='__main__':run()
