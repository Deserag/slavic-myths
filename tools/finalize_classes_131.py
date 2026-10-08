"""Record final gates and update documentation without altering production sources."""
from pathlib import Path
import json, hashlib, re, shutil

ROOT = Path(__file__).resolve().parents[1]
def write(path, text):
    path = ROOT / path
    temporary = path.with_name(path.name + '.classes131.tmp')
    temporary.write_text(text, encoding='utf-8')
    temporary.replace(path)

report = json.loads((ROOT/'docs/verification/classes-1.3.1-resources.json').read_text())
jar = Path(report['jar'])
assert hashlib.sha256(jar.read_bytes()).hexdigest() == report['sha256']
build = ROOT/'work/classes-131-final-clean-build.log'
assert 'BUILD SUCCESSFUL' in build.read_text(errors='replace')
finals = {
    'classes-131-runtime-19-final.log':15,
    'classes-131-regression-village-final-2.log':53,
    'classes-131-regression-city.log':8,
    'classes-131-regression-buildings.log':6,
}
for name, count in finals.items():
    assert f'All {count} required tests passed' in (ROOT/'work'/name).read_text(errors='replace'), name
launches = []
for log in sorted((ROOT/'work').glob('classes-131-*.log')):
    text = log.read_text(errors='replace')
    if 'Started game test server' in text:
        launches.append({'log':log.name, 'passed':bool(re.search(r'All \d+ required tests passed',text)),
                         'sha256':hashlib.sha256(log.read_bytes()).hexdigest()})
evidence = ROOT/'docs/verification/classes-1.3.1'
evidence.mkdir(exist_ok=True)
for launch in launches:
    shutil.copy2(ROOT/'work'/launch['log'],evidence/launch['log'])
for name in [build.name,*finals,'classes-131-runtime-03-write.log','classes-131-runtime-04-verify.log',
             'classes-131-runtime-18-final.log','classes-131-final-resources.log',
             'classes-131-final-city-resources.log','classes-131-final-buildings-resources.log']:
    shutil.copy2(ROOT/'work'/name,evidence/name)
acceptance = {
    **report, 'date':'2026-10-08', 'clean_build':'PASS', 'required_passed':sum(finals.values()),
    'final_suites':finals,'active_rank_cases':45,'passive_rank_cases':24,'native_deaths_and_respawns':6,
    'native_player_dat_restart':'write then load in a separate process',
    'with_jei_pass':True,'without_jei_pass':True,'actual_headless_launches':len(launches),
    'launches':launches,'client_launches':0,'real_client_relogin_verified':False,
    'two_real_clients_verified':False,'mspt_fps_measured':False,'commit_created':False,
    'intermediate_failures':{
        'runtime_01':'Fixture connection missing native channel; fixed fixture.',
        'runtime_07_to_12':'Full shield block stat counter stays zero in NeoForge; class trigger now confirms native DamageContainer.blockedDamage.',
        'village_first_three':'Native pickup/deposit or long linen chain timeout; moved fixture chest closer and extended chain time, physical assertions unchanged.',
        'first_clean':'Transient Gradle artifact file lock; repeated clean build passed.'
    },
    'client_manual_checklist':'docs/MANUAL_QA_CLASSES_1.3.1.md',
}
installation=ROOT/'docs/verification/polymc-1.3.1-installation.json'
if installation.is_file():
    installed=json.loads(installation.read_text())
    if installed['sha256']==report['sha256']:
        acceptance.update(polymc_updated=True,polymc_installation='docs/verification/polymc-1.3.1-installation.json')
write('docs/verification/classes-1.3.1-acceptance.json',json.dumps(acceptance,ensure_ascii=False,indent=2)+'\n')

intro = f'''## 1.3.1 — Классы 2.0 (2026-10-08)

Реализованы дружинник, ведун и разбойник, 18 ветвей, 28 основных навыков и шесть эволюций. Отдельные class XP/очки, серверные требования, три активных слота G/H/J и один пассивный, триггеры, экран K/камня пути, HUD и 34 разные pixel icons с ru_ru/en_us. Миграция прежних путей сохраняет данные/КД; нативные смерть, /kill и загрузка player .dat новым процессом проверены. API/debug `/smclass`; массовые источники class XP не добавлены.

Clean build PASS; ресурсы/воспроизводимость/JAR PASS. Итоговые серверные suites 15 + 53 + 8 + 6 = 82/82; {len(launches)} реальных headless запусков этой итерации, ноль клиентов. Промежуточные ошибки сохранены в [машинном отчёте](verification/classes-1.3.1-acceptance.json). Пользовательские изменения городских построек сохранены. Commit/push не выполнялись; игровые миры не перезаписывались.

[Реализация и ограничения](CLASSES_1.3.1.md). Ручная приёмка GUI scales/F1/keybindings, двух клиентов, боя и реального выхода в меню/перезахода остаётся открытой; FPS/MSPT не измерялись. [Краткий список](MANUAL_QA_CLASSES_1.3.1.md).

'''
extra = {
    'docs/PROJECT_STATUS.md':'',
    'docs/ROADMAP.md':'Объём Class 2.0 завершён в рамках задания. Оружие/броня 1.3.2, руны 2.0, мана, боги и новые измерения не реализованы. Следующий шаг — ручная приёмка текущей версии, а не автоматическое выполнение будущих milestone.\n\n',
    'docs/ARCHITECTURE.md':'Class2 хранится внутри прежнего SlavicPaths; LegacyPaths сохраняет исходный compound. ClassDefinitions/ClassBalance централизуют требования и численные параметры; ClassState выполняет серверные переходы/идемпотентные атрибуты. ClassNetwork принимает только намерения; ClassRuntime/ClassEvents используют нативный бой/столкновения и ограниченные таймеры. ClassClient/ClassScreen изолированы на client side. Нет глобального поиска по миру/игрокам или chunk loading ради способностей; test source set включается только opt-in и не входит в production JAR.\n\n',
}
for name, detail in extra.items():
    old=(ROOT/name).read_text(encoding='utf-8')
    if not old.startswith('## 1.3.1 — Классы 2.0'):
        write(name,intro+detail+old)
readme=(ROOT/'README.md').read_text(encoding='utf-8')
readme=readme.replace('Версия 1.2.5','Версия 1.3.1').replace('slavicmyths-1.2.5.jar','slavicmyths-1.3.1.jar')
needle='- Алтари, подношения, знания, репутация, ритуалы, руны, фольклорная книга и личная навигация.\n'
if 'Классы 2.0:' not in readme:
    readme=readme.replace(needle,needle+'- Классы 2.0: три базы, 18 ветвей, дерево навыков и эволюции; G/H/J — активные слоты, K — меню. Class XP пока выдаётся через API/debug `/smclass`, отдельно от vanilla XP.\n')
readme=readme.replace('1.2.5 реализована и проходит clean build и проверки production-ресурсов.',
    '1.3.1 реализована: clean build и production-ресурсы PASS, итоговые headless suites 82/82. [Классы и ограничения](docs/CLASSES_1.3.1.md), [ручной список](docs/MANUAL_QA_CLASSES_1.3.1.md).')
write('README.md',readme)
changelog=(ROOT/'CHANGELOG.md').read_text(encoding='utf-8')
if '## 1.3.1' not in changelog:
    changelog=changelog.replace('# Changelog\n','# Changelog\n\n## 1.3.1\n\n- Классы 2.0: три базы, 18 ветвей, 28 навыков и шесть эволюций; отдельные XP/очки, три активных слота и один пассивный, автоматические триггеры.\n- Серверные проверки, миграция старых путей, сохранение после смерти и загрузки без удвоения атрибутов/сброса КД.\n- Самостоятельный экран/дерево, численные параметры, переназначаемые G/H/J и K, HUD, 34 разные skill icons и ru_ru/en_us.\n- Сохранены параллельные исправления башен, интерьеров, света, лестниц и окон: [городские изменения](docs/CITY_POLISH_1.3.1.md).\n\nClean build/resource gates PASS; итоговые suites 82/82. [Отчёт и ручные ограничения](docs/CLASSES_1.3.1.md).\n')
write('CHANGELOG.md',changelog)
print(json.dumps({'sha256':report['sha256'],'launches':len(launches),'passed':sum(finals.values())}))
