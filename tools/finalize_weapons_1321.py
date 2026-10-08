"""Archive local verification evidence and update the current milestone without rewriting history."""
from pathlib import Path
import json, hashlib, shutil

root=Path(__file__).resolve().parents[1]
out=root/'docs/verification/weapons-1.3.2.1'
out.mkdir(exist_ok=True)
logs=sorted((root/'work').glob('weapons-1321*.log'))
records=[]
for log in logs:
 shutil.copy2(log,out/log.name)
 text=log.read_text(encoding='utf-8',errors='replace')
 records.append({'file':str((out/log.name).relative_to(root)), 'sha256':hashlib.sha256(log.read_bytes()).hexdigest(), 'server_launches':text.count('Started game test server'), 'build_successful':'BUILD SUCCESSFUL' in text})
shutil.copy2(root/'run-weapons-1321/weapon-audit-1321.json',root/'docs/verification/weapons-1.3.2.1-catalog.json')
resources=json.loads((root/'docs/verification/weapons-1.3.2.1-resources.json').read_text())
report={'version':'1.3.2.1','date':'2026-10-08','clean_build':'PASS','resource_gate':resources,
 'final_suites':{'weapons':16,'classes_regression':15,'total_passed':31},
 'headless_server_launches':sum(r['server_launches'] for r in records),'client_launches':0,
 'knife_sample':{'attempts':512,'lost':117,'observed_percent':117/512*100,'configured_loss_probability':0.25},
 'restart_saved_weapons':30,'native_dimension_transitions':2,'optional_jei_absent_suite_passed':15,
 'intermediate_failures':['First fixture used tick instead of native doTick for equipment attributes; corrected.', 'Fixture forced pickup after ItemEntity removal; corrected to respect native removal.', 'GameTest server disables PvP by default; fixture temporarily enables and restores it for shield test.', 'Cow health was capped at 1024; fixture resets actual maximum health per collision.', 'First compile required SoundEvents.TRIDENT_THROW.value().', 'Resource counter initially included historical weapon_wrap; corrected to count new weapon_part_* recipes.'],
 'limitations':['No client, GUI, hand rendering or actual multi-client combat acceptance.', 'Player .dat was saved and loaded by separate native server processes; this is not a real menu logout/login.', 'Native dimension/death tests use isolated fixtures; actual chunk unload and multiple-player pickup remain manual.', 'FPS/MSPT were not measured.'],
 'polymc_updated':False,'commit_created':False,'push_performed':False,'logs':records}
(root/'docs/verification/weapons-1.3.2.1-acceptance.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
assert report['headless_server_launches']==11,report['headless_server_launches']

header='## 1.3.2.1 — Оружие 2.0 (2026-10-08)\n\n'
common=('Реализованы 30 форм оружия: копья, сулицы, кистени и метательные ножи семи материалов, короткий и тяжёлый луки. '
 '53 новых предмета включают детали; три прежних оружейных ID сохранены. 36 рецептов оружейного верстака и 23 рецепта деталей; '
 'просмотр рецептов работает без JEI. Разные силуэты, ru_ru/en_us, серверные броски/подбор, сохранение данных и совместимые ремонты. '
 'Централизованы ёмкость рун и категории существующего оружия; ранее открытые слоты сохранены.\n\n'
 'Финальный `gradlew.bat clean build` PASS; 15 067 проверок ресурсов/JAR PASS, 2 566 прежних ресурсов сохранены побайтно. '
 'Итоговые headless suites: оружие 16/16, классы 15/15. 11 реальных серверных запусков, ноль клиентов. '
 '30 оружейных стеков проверены после загрузки player .dat новым процессом; проверены смерть и два нативных перехода между измерениями. '
 '512 столкновений ножа: 117 потерь при заданной вероятности 25%.\n\n'
 '[Реализация, характеристики и рецепты](WEAPONS_1.3.2.1.md), [отчёт и ограничения](verification/weapons-1.3.2.1-acceptance.json), '
 '[ручная приёмка](MANUAL_QA_WEAPONS_1.3.2.1.md). Клиентский вид, GUI, бой нескольких игроков и реальный выход/перезаход остаются ручными; '
 'FPS/MSPT не измерялись. PolyMC остаётся на проверенной 1.3.1; установка новой версии, commit и push не выполнялись.\n\n')
extras={
 'PROJECT_STATUS.md':'Пользовательские изменения и существующие миры сохранены. Production JAR: `build/libs/slavicmyths-1.3.2.1.jar`.\n\n',
 'ROADMAP.md':'Объём текущего архива завершён; броня, руны 2.0, мана, боги и измерения не добавлялись. Следующий шаг — ручная приёмка текущей версии. Прежние записи ниже относятся к своим версиям.\n\n',
 'ARCHITECTURE.md':'`WeaponCatalog` централизует материалы/регистрацию; `ThrowingWeaponItem` и `WeaponProjectile` используют нативный серверный перенос ItemStack, сохранение и однократный подбор. `FieldBowItem` сохраняет стандартные ammo/enchantment hooks. `Runes` определяет категории/ёмкость; `RuneRepair` защищает данные при ремонте. Существующий `ArmorerRecipe` поддерживает перенос компонентов при улучшении. Рендерер, bow properties и браузер рецептов изолированы на client side. Нет глобального tick polling или chunk loading ради оружия. Opt-in `weaponCheck` и его ресурсы не входят в production JAR. Версия class network 1.3.2.1 требует одинаковой версии клиента/сервера, включая новый формат рецепта.\n\n'}
for name,extra in extras.items():
 path=root/'docs'/name;old=path.read_text(encoding='utf-8')
 if not old.startswith(header):path.write_text(header+common+extra+old,encoding='utf-8')

path=root/'README.md';old=path.read_text(encoding='utf-8')
old=old.replace('**Версия 1.3.1','**Версия 1.3.2.1').replace('slavicmyths-1.3.1.jar','slavicmyths-1.3.2.1.jar')
old=old.replace('- Классы 2.0:', '- Оружие 2.0: копья, сулицы, кистени, метательные ножи семи материалов и два лука; детали и сборка на оружейном верстаке, просмотр рецептов без JEI.\n- Классы 2.0:',1)
old=old.replace('1.3.1 реализована: clean build и production-ресурсы PASS, итоговые headless suites 82/82.', '1.3.2.1 реализована: clean build и production-ресурсы PASS, итоговые headless suites 31/31; 11 серверных запусков, ноль клиентов. [Оружие, рецепты и ограничения](docs/WEAPONS_1.3.2.1.md), [ручная приёмка оружия](docs/MANUAL_QA_WEAPONS_1.3.2.1.md).')
tmp=path.with_name('README.weapons-1321.tmp');tmp.write_text(old,encoding='utf-8');tmp.replace(path)
path=root/'CHANGELOG.md';old=path.read_text(encoding='utf-8')
entry=('## 1.3.2.1\n\n- Оружие 2.0: 30 форм, семь материалов, разные силуэты; 53 новых предмета, включая детали.\n'
 '- Копья/сулицы с досягаемостью, серверные броски ножей/сулиц, кистени против щита и брони, короткий/тяжёлый луки.\n'
 '- 36 сборочных и 23 рецепта деталей; браузер оружейного верстака без обязательного JEI.\n'
 '- Ёмкость/категории рун для существующего оружия; сохранение ранее открытых слотов, данных при улучшении и совместимых ремонтах.\n'
 '- Нативное сохранение projectile/ItemStack и однократный подбор; прежние ID, рецепты, модели и текстуры сохранены.\n\n'
 'Clean build/resource/JAR gates PASS; финальные серверные suites 31/31, 11 headless запусков и ноль клиентов. [Реализация и ручные ограничения](docs/WEAPONS_1.3.2.1.md).\n\n')
if '## 1.3.2.1\n' not in old:path.write_text(old.replace('# Changelog\n\n','# Changelog\n\n'+entry,1),encoding='utf-8')
path=root/'docs/WEAPONS_1.3.2.1.md';old=path.read_text(encoding='utf-8').replace('специальный квестовый artifact/staff','специальный artifact/staff')
if '## Финальная проверка' not in old:old+='\n## Финальная проверка\n\n'+common.replace('(WEAPONS_1.3.2.1.md)','(WEAPONS_1.3.2.1.md)')
path.write_text(old,encoding='utf-8')
print(json.dumps({'headless_launches':report['headless_server_launches'],'final_passed':31,'jar_sha256':resources['sha256']}))
