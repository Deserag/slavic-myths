"""Write a review bundle scoped to the task-start snapshot, including untracked task files."""
from pathlib import Path
import json,hashlib,subprocess,difflib,shutil,re
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/creature-feedback';B=R/'.tools/creature-feedback-before';O.mkdir(parents=True,exist_ok=True)
manifest=json.loads((B/'manifest.json').read_text(encoding='utf-8'));gates=json.loads((O/'ARTIFACT_GATES.json').read_text(encoding='utf-8'))
def norm(p):return p.read_bytes().replace(b'\r\n',b'\n')
changed=[p for p in manifest['paths'] if norm(R/p)!=norm(B/p)]
new=['src/main/java/org/slavicmyths/combat/CreatureSafety.java','src/main/java/org/slavicmyths/gorynych/GorynychPose.java','tools/creature-feedback-runtime.init.gradle','tools/creature-feedback-runtime/CreatureFeedbackTests.java','tools/creature-feedback-runtime-resources/data/slavicmyths_creature_feedback/structure/empty.nbt','tools/verify_creature_feedback.py','tools/install_creature_feedback.ps1','tools/creature_feedback_report.py']
paths=sorted(set(changed+new));patch=[];rows=[]
for rel in paths:
 data=(R/rel).read_bytes();before=(B/rel).read_bytes() if (B/rel).exists() else b''
 rows.append({'path':rel,'kind':'production Java' if rel.startswith('src/main/java/') else 'resource' if rel.startswith('src/main/resources/') else 'documentation' if rel.startswith('docs/') else 'test/tool','sha256':hashlib.sha256(data).hexdigest(),'new':rel in new})
 if rel.endswith('.nbt'):continue
 patch.extend(difflib.unified_diff(before.decode('utf-8-sig').replace('\r\n','\n').splitlines(keepends=True),data.decode('utf-8-sig').replace('\r\n','\n').splitlines(keepends=True),fromfile='a/'+rel if before else '/dev/null',tofile='b/'+rel))
(O/'OWN_PATCH.patch').write_text(''.join(patch),encoding='utf-8')
(O/'OWN_FILES.json').write_text(json.dumps(rows,ensure_ascii=False,indent=2),encoding='utf-8')
(O/'FILES.md').write_text('# Файлы текущего исправления\n\nИзменения относительно начала этого задания, включая новые untracked-файлы. Полный snapshot файлов и SHA256: OWN_FILES.json. Архивные HP/оружие/руны в общий список не включены.\n\n'+'\n'.join('- `'+row['path']+'` — '+row['kind'] for row in rows)+'\n\nДополнительно: все отчёты, логи, спецификация и квитанция в этой папке.\n',encoding='utf-8')
def logcopy(source,target):target.write_text('\n'.join(line.rstrip() for line in source.read_text(encoding='utf-8').splitlines())+'\n',encoding='utf-8')
for i in range(1,5):logcopy(R/f'.tools/creature-feedback-runtime{i}.log',O/f'RUNTIME_{i}.log')
logcopy(R/'.tools/creature-feedback-clean-build.log',O/'CLEAN_BUILD.log')
shutil.copyfile(B/'manifest.json',O/'START_SNAPSHOT.json')
spec=Path('C:/Users/pavel/.codex/attachments/73e1958d-eac4-4171-a28c-85ee8d441288/Вставленный текст.txt');shutil.copyfile(spec,O/'SPEC.md')
proc=subprocess.run(['git','diff','--check'],cwd=R,capture_output=True,text=True,encoding='utf-8');assert proc.returncode==0,proc.stdout
(O/'DIFF_CHECK.txt').write_text('git diff --check: PASS (exit 0)\n'+proc.stdout+proc.stderr,encoding='utf-8')
(O/'WORKSPACE_DIFF_STAT.txt').write_text(subprocess.check_output(['git','diff','--stat'],cwd=R,text=True,encoding='utf-8'),encoding='utf-8')
runtime=(O/'RUNTIME_4.log').read_text(encoding='utf-8');checks=len(re.findall(r'CREATURE_FEEDBACK_PASS ',runtime));hostiles=len(re.findall(r'CREATURE_FEEDBACK_PASS \d+ Peaceful rejects ',runtime))
receipt=json.loads((O/'polymc-installation.json').read_text(encoding='utf-8-sig')) if (O/'polymc-installation.json').exists() else None
status='COMPLETE_WITH_LIMITATIONS' if receipt else 'VERIFIED_PENDING_INSTALL'
report=f'''# Исправления существ после игрового тестирования

Статус: **{status}**. Minecraft 1.21.1, NeoForge 21.1.255, Java 21.0.12. Production metadata остаётся 1.9.7; это отдельный патч поверх оружия, здоровья и рун. Commit/push не выполнялись.

## Исправлено

- Мирная сложность: CreatureSafety обрабатывает смену сложности и EntityJoinLevel, а не обходит мир каждый тик. Явный перечень типов плюс mod MobCategory.MONSTER; исключения для очищенных хранителей, истощённого/очищенного Горыныча и союзных воронов. NPC, животные и богатыри сохраняются. Удаление через discard, без die/loot/victory. Морозко/Карачун получают Cancelled и доступный повтор; сюжетные финалы и прошлые награды не очищаются. Земляной дух открывает арену и возвращает её в SEALED без выдачи награды, сохраняя потраченные колонны. Картáус, Калин, Маринка и Ским используют свои процедуры неуспешного завершения. Загруженные принадлежащие им атаки очищаются; чанки ради очистки не загружаются.
- Creative/Spectator исключены общей политикой, событием смены цели, кандидатами Горыныча и обоих испытаний (включая владельца). Отложенный прямой урон блокируется. При смене режима очищаются страх/контроль, цели и захваты; проверки выполняются и в контроллерах. generic_kill оставлен для намеренного администрирования. Проверки не запрещают атаковать обычных существ из Creative и не запрещают богатырям владельца Creative сражаться в отладочных сценах.
- Полоз: 3000 HP сохранены. Root и все 24 бронированных сегмента не принимают обычный урон во всех фазах. Только три действительных scar PartEntity: контакт стрелы проверяется геометрически, ближний удар — по лучу/дальности/видимости, взрыв и косвенная splash-подмена отклоняются. rewardFissure остаётся явным отдельным исключением сюжетной способности. Удалены ложные декоративные шрамы фазы II/III; верхний шрам смещён внутрь своего hitbox. Рикошет и рана различаются звуком и частицами.
- Полоз атакует ближайших допустимых живых существ по расстоянию до multipart, включая животных и защитников, с локальным поиском раз в 20 тиков, LOS и радиусом 48 блоков от тела. Исключены собственные призванные существа, Creative/Spectator, стойки брони. Недоступные по LOS цели отбрасываются. Серый Волк получает отдельный ограниченный AI goal защиты против Полоза в IDLE_UNMET/POST_TRIAL_NPC; испытание и его переходы имеют приоритет.
- Входящий урон обычных мобов по шраму ограничен 4 raw за контакт и 12 raw суммарно за 20 тиков на босса, дополнительно к интервалу 6 тиков на атакующего. После множителя шрама максимум 18 HP/сек для всей обычной группы. Богатыри, GreyWolf, WolfSupport и распознанные сюжетные участники исключены из этого ограничения. Игроки и собственные механики союзников не получают этот cap.
- Страж Чуди: настоящий существующий железный топор при создании, setup и загрузке старого пустого mainhand; ненулевое существующее оружие сохраняется, dropChance=0. Отложенный удар использует doHurtTarget, дальность, LOS и общую допустимость цели. База стража 2 плюс реальный модификатор оружия. Роли рабочих/жителей не изменены. Уже существующий ItemInHandLayer рисует предмет; добавлена поза ITEM. Рука модели заканчивается на тех же 10 px относительно плеча, что и vanilla, поэтому произвольный hand offset не добавлялся.
- Карачун: страх накапливается от устойчивого нахождения в опасной зоне во всех боевых фазах, а не только от редкой атаки/особой ночи. Прибавка rate/5×0.6 плюс прежняя надбавка; Tier 25/50/75, понижение после ухода ниже границы на 8 и выдержки 40 тиков. Отход запускает существующий decay после 40 тиков без воздействия, успешный удар уменьшает страх на 6 с cooldown 40; source-local снятие сохраняет другие источники. Клиент получает стабилизированное отображаемое значение для 0/2/3 фантомов. Постоянная слепота не добавлена.
- Горыныч: общий GorynychPose вычисляет серверные положения голов из тех же ModelPart offsets и углов, что клиент. Исправлены знак боковых корней и направление yaw/луча. Внешние головы ограничены своими внешними секторами 0…40 / −40…0, средняя −12…12; свой корень остаётся у каждой шеи. Поворот/наклон интерполируется по четырёхтиковым снимкам; время действия экстраполируется максимум на 4 тика. Хищная голова: подготовка до 24, выпад 24–30, контакт 30–34 (серверный удар на 32), возврат до 52. Hold обозначен отдельным внутренним action=3, recovery=4. Захват запускается только после пересечения AABB головы с жертвой, LOS и принятого hurt. Сопротивление, лимит времени и cooldown освобождения сохранены.
- Локализация: 13 доказанно повреждённых значений ru_ru исправлены пятью точными обратимыми заменами; смешанные русские абзацы целиком не перекодировались. Список before/after: LOCALIZATION_FIXES.json. Подправлены порождающие подписи яиц/knockdown в underground_mob_resources.py; генераторы ресурсов не запускались. en_us сохранён побайтно. Новые записи параллельного патча рун сохранены.

## Урон Полоза

База VIII = 26. Коэффициенты A1 .65, A2 .8, A3 .7, A4 1, A5 1.15, A6 1.35, A7 1.5; blanket-множителя фаз нет. Ниже raw до брони/эффектов; фактический net зависит от защиты жертвы.

| Действие | Категория | Бюджет на жертву |
|---|---|---:|
| Таран головы | A4 | 26, один hit UUID за действие |
| Выход из земли/воды | A6 | 35.1, один hit UUID |
| Дождь | A2 | 20.8 на UUID залпа, контакт 5.2 / splash 2.6 |
| Хвост | A3 | 18.2, вторичная волна 9.1, общий dedup UUID |
| Воронка | A1 | 16.9 за три импульса по 5.6333 |
| Призыв | SUMMON | Без прямого урона |

A5/A7 в текущем наборе Полоза не назначены искусственно. Существующий интервал и поведение отдельных minions сохранены. Дождевой cap хранится у жертвы с отдельным UUID залпа, а не UUID босса.

## HP и сохранения

Предыдущая HP-реализация и её Entity NBT/SavedData migration markers сохранены. Контрольный набор снова проходит 1545 assertions, включая NBT/атрибуты/правила и ограниченный lifecycle stub Жар-птицы. Это не 1545 игровых тестов.

| Существо | Логическое HP / пороги |
|---|---|
| Проклятый Оборотень | 700; относительные фазы .625 / .25 |
| Лихо | 350; моровое состояние того же EntityType 700 |
| Морозко | default 1400; собственный IceDouble не мигрируется как босс |
| Карачун | default 1600; намеренный custom NightMax сохраняется |
| Горыныч | 2800; трансформация 1500, истощение 75; три головы очищения |
| Калин | 1800 |
| Полоз | 3000; фазы 2100 / 900 |
| Жар-птица | 3000, прежняя сюжетная неубиваемость и ответная реакция |

Старый установленный JAR: `{manifest['installed_sha256']}`. В нём не было CreatureHealthBalance.class и маркера GorynychBalance1982, поэтому HP старого игрового теста не является доказательством дефекта актуальной миграции. Реальный перезаход с переносом мира не выполнялся.

## Проверки

- Финальная clean build + creatureHealthOfflineChecks: PASS, Java 21; 1545 assertions. Gradle test штатно NO-SOURCE; специализированные проверки запущены отдельно, не скрыты под этим статусом.
- GameTest: **4 запуска изолированного сервера**, клиентских запусков **0**. Первый выявил случай стрелы внутри AABB и завершился FAIL; исправление внесено. Второй PASS (43), третий PASS (104), финальный PASS (**{checks} адресных assertions**, один последовательный GameTest-сценарий). Финальная матрица отклоняет {hostiles} зарегистрированных враждебных EntityType при spawn в Peaceful. Логи всех запусков сохранены.
- Runtime-проверки: броня root во всех фазах, реальные scar projectile contacts/cap, Creative/Spectator targeting/damage/control, реальный серверный bite до grab, отсутствие grab вне контакта и release Creative, экипировка/старый NBT стража, страх/гистерезис/снятие своего источника, отмена обоих испытаний без победы и сохранение гражданского NPC.
- 15 проверок конечности координат голов и проверки коэффициентов — математические/статические утверждения внутри GameTest; они не доказывают качество рендера. NBT roundtrip — не настоящий перезаход.
- Artifact gates PASS: {gates['gates']} проверок, {gates['packaged_json']} UTF-8 JSON; все {gates['resources']} production ресурсов и скомпилированные main-классы совпадают с JAR. Тестовых fixture в JAR нет. SHA256: `{gates['sha256']}`.
- git diff --check PASS. WORKSPACE_DIFF_STAT.txt включает параллельные изменения и не является списком этого задания. FILES.md / OWN_FILES.json / OWN_PATCH.patch выделяют собственную работу, включая untracked. Регистрации ID и production-version не изменялись.
- SHA256-файлы параллельного rune-patch сохранены; единственное осознанное отличие — 13 значений общего ru_ru, остальные ключи доказанно равны исходному состоянию. Код/модели оружия, брони, rune logic, рецепты и иконки не редактировались этим заданием.

## PolyMC

{('PASS: установлен свежий JAR, SHA256 совпадает с gates. Резервная копия старого JAR: `'+str(receipt['backup'])+'`. Остальные '+str(receipt['other_files_preserved'])+' файлов профиля сохранены побайтно. Имя профиля дополнено «Исправления существ» с сохранением «Патч оружия». Миры/config и профиль 1.16.5 не перезаписывались; клиент не запускался.') if receipt else 'Кандидат проверен; установка ещё не выполнена.'}

## Ограничения / ручная проверка

1. НЕ ПРОВЕРЕНО В КЛИЕНТЕ: визуальное совпадение шрамов/хитбоксов при интерполяции multipart, рикошет/рана на слух и по частицам; групповая битва Полоза и физическая доступность всех трёх шрамов на неровном рельефе. MSPT и производительность не измерялись.
2. НЕ ПРОВЕРЕНО: длительная самостоятельная защита Серого Волка, разные сюжетные состояния и восстановление после реальной выгрузки/перезахода; реальные сохранённые арены всех боссов и прогресс нескольких игроков. Серверный тест отмены испытаний не равен прохождению повторного ритуала игроком.
3. НЕ ПРОВЕРЕНО: плавность трёх шей во всех yaw/duck/fly/cleansing позах и при сетевой задержке; читаемость укуса и удержания. Проверены серверный контакт и запрет преждевременного grab, а не качество клиентской анимации.
4. НЕ ПРОВЕРЕНО: субъективная интенсивность страха и фантомов на клиенте, длительная игра в опасной зоне, retreat/resist с разной экипировкой; корректность видимых кириллических глифов после запуска. Encoding/source/JAR проверены отдельно.
5. BLOCKED_VISUAL: для точного исправления положения конкретного посоха/оружия у игрока нужны корректные исходные скриншоты с предметом, видом камеры и рукой. Скриншоты к заданию не приложены; произвольные player hand transforms и параллельные модели оружия не заменялись. Для Чуди использован фактический собственный renderer.
6. SEPARATE_SYSTEM_PATCH: FurnitureBlock implements EntityBlock для firewood_bundle/wooden_crate/pine_shelf/pine_stool/pine_bench, но newBlockEntity возвращает null для всех кроме weapon_rack. RackTile зарегистрирован только для двух weapon_rack; перечисленная мебель не имеет соответствующей block entity. Это объясняет конкретное предупреждение создания, но не устанавливает причину выхода -805306369. Последний лог от 14 октября заканчивается штатным сохранением всех измерений; соответствующего свежего crash-report нет. Фабрики/старый NBT мебели требуют отдельного системного исправления, миры не переписывались.
7. Параллельная разработка: полный запуск старого underground_mob_resources.py всё ещё может перезаписать исторические тексты рун. Он здесь не запускался; его rune-раздел требуется согласовать с владельцем rune-patch. Будущие балансовые/визуальные изменения оружия и следующий этап 1.9.8.2 не начинались.

Краткий ручной маршрут: новый тестовый мир/копия → Survival, шрамы/группа/Волк → Creative/Spectator посреди grab/AoE → Peaceful и повтор испытаний после возврата Normal → страх/отход/успешный удар → Чудь и кириллица. Существующий пользовательский мир ради проверки не заменять.

Ветка audit: собственные файлы перенесены в D:/slavic-myths-audit; подробности и границы состава — AUDIT_SYNC.md / AUDIT_SYNC.json. Полный staged diff --check там PASS. OWN_PATCH.patch оставлен как untracked диагностический артефакт, его содержимое включено в папку отчёта. Копии логов нормализованы по LF/концевым пробелам; исходные логи остались в .tools основного checkout. Общий установленный JAR собран из основного checkout вместе с сохранёнными параллельными правками, а не из изолированной ветки audit.
'''
(O/'REPORT.md').write_text(report,encoding='utf-8')
summary={'status':status,'runtime_servers':4,'client_starts':0,'runtime_assertions_final':checks,'peaceful_types':hostiles,'hp_assertions':1545,'jar_sha256':gates['sha256'],'owned_files':len(paths),'production_java':sum(r['kind']=='production Java' for r in rows),'install':bool(receipt)}
(O/'SUMMARY.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(summary,ensure_ascii=False))
