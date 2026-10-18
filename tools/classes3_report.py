from pathlib import Path
import ast,csv,json,shutil,subprocess
R=Path(__file__).resolve().parents[1];W=Path(r'C:\Users\pavel\.codex\worktrees\classes-3-review\slavic-myths');rel=Path('docs/implementation/classes3')
for p in (W/rel).glob('isolated-*.log'):shutil.copy2(p,R/rel/p.name)
shutil.copy2(W/rel/'RESOURCE_JAR_CHECKS.json',R/rel/'ISOLATED_RESOURCE_JAR_CHECKS.json')
shutil.copy2(R/rel/'RESOURCE_JAR_CHECKS.json',W/rel/'INTEGRATED_RESOURCE_JAR_CHECKS.json')
for name in ['integrated-final.log','rune-regression.log','clean-build.log']:
 target='integrated-clean-build.log' if name=='clean-build.log' else name
 shutil.copy2(R/rel/name,W/rel/target)
tree=ast.parse((R/'tools/classes3_assets.py').read_text(encoding='utf-8'));catalog={}
for n in tree.body:
 if isinstance(n,ast.Assign) and len(n.targets)==1 and isinstance(n.targets[0],ast.Name) and n.targets[0].id in ['names','passives','trials']:catalog[n.targets[0].id]=ast.literal_eval(n.value)
runtime=json.loads((R/rel/'RESOURCE_JAR_CHECKS.json').read_text());isolated=json.loads((W/rel/'RESOURCE_JAR_CHECKS.json').read_text())
abilities=list(csv.DictReader((R/rel/'abilities.csv').open(encoding='utf-8')))
launches=[p.name for p in (R/rel).glob('*.log') if '[minecraft/GameTestServer]' in p.read_text(encoding='utf-8',errors='replace')]
assert len(launches)==15,launches
assert 'All 37 required tests passed' in (R/rel/'integrated-final.log').read_text(errors='replace')
assert 'CLASSES3_NATIVE_DISK_NEW_PROCESS_PASS' in (W/rel/'isolated-trial-final.log').read_text(errors='replace')
assert 'All 36 required tests passed' in (W/rel/'isolated-trial-final.log').read_text(errors='replace')
text='''# Classes 3.0 — implementation candidate, 2026-10-14

Статус: **A–D implemented; E/in-game acceptance PARTIAL**. Это не утверждение о завершённой игровой приёмке или измеренном балансе. PR #21 остаётся отдельной, не слитой дизайн-спецификацией. Реализована утверждённая система без новых базовых классов, измерений, оружия и изменения существующих registry ID.

## Основание и изоляция

Изучены задание, обязательные документы, фактические Class 2.0 исходники, PR #21 и Issues #15–#20. База review-веток: `2389b876e72d38ddcef93b7032026a74a64c40ac`. Чужие незакоммиченные исправления арбалетов/Морозка сохранены в `D:/slavic-myths`. В managed worktree перенесены только файлы Classes 3.0; изменения соседних систем в PR не включены. Версия не повышалась: review baseline 1.9.7.3, параллельная интегрированная рабочая копия 1.9.7.5.

## Реализовано

- Class XP: убийство настоящего враждебного моба 12, сильного (max HP≥40) 40; max HP≥100 — первая награда за тип 500. Повторяемый боевой источник ограничен 240 XP/мин. Истории/достижения 200 один раз, исключены root/group_*/own_path. Исследование нового **экземпляра** структуры 180: ключ типа, измерения и start chunk, без загрузки чанков и новых world scans.
- Роль: 4 XP за подтверждённый блок, успешные знак/проклятие/очищение, настоящее лечение после вражеского повреждения, дальнее попадание или выход из угрозы. Глобальный cooldown 200 тиков; максимум 48/мин, практически ≤24/мин из-за cooldown. Один target/action/owner не повторяется. Ведун получает 8 XP за первое завершение каждого реального алтарного ритуала. Vanilla XP не меняется.
- Persistent ledger ≤2048; при заполнении новые одноразовые награды отклоняются, старые не удаляются ради повторной выплаты. Creative/spectator, PvP, friendly/protected, Mor clones, spawner/trial spawner/command/egg/dispenser origins исключены. Награда не выдаётся за кнопку навыка. Серверные команды администратора остаются доверенной границей, а не клиентским XP API.
- `Class2` сохранён. v2→v3 обновляет только Version; прежние Base/First/Second/Level/Xp/Points/Ranks/Passive/Active0..2/Cooldowns не сбрасываются. Добавлены Passive2, Third, Ultimate, Trials и XpLedger. Второй слот работает при уровне ≥25 и валидной цепочке First→Second. Дубли и чужой класс запрещены; закрытый слот не даёт бонуса.
- Шесть пассивок ×3 ранга подключены к настоящим боевым/щитовым/effect событиям. Дополнительный ambush bonus ограничен общим opener cap 80%; существующие бонусы не урезаются. Неэкипированные пассивки не действуют.
- 12 IV-ветвей и 12 отдельных finite actions. Сохранённые cooldown, проверки жив/состояние/цель/щит/оружие/HP/LOS, запрет высшего урона игрокам, boss raw hit cap 6 HP, control resistance, best-only выбор защитного поля, bounded target counts. Ни один обработчик не сбрасывает vanilla i-frames. У кровотечения общий пул 8 HP, у полей общий пул предотвращения.
- Shadow Pact — направленная серверная частица-снаряд со swept segment collision и TTL, без нового EntityType; трата ровно 4 HP — ресурс, оставляет ≥4 HP, не атака с обходом брони. False Ambush использует помеченную vanilla ArmorStand: до 3 мобов, восстановление целей и cleanup logout/dimension/TTL/reload. Полный autonomous AI smoke остаётся открыт.
- Banner защищает/усиливает владельца и до 5 дружественных участников; приоритет дружинников подключён к прежнему 40-tick решению GuardBehavior, с возвратом прежнего противника. Нейтральный житель дружественен, атакующий игрока житель не получает IV-поддержку.
- K: I→IV, второе пассивное место с замком, 12 карточек, требования и прогресс, отдельный высший HUD/слот. Новая переназначаемая клавиша **U** (R уже занят рунами/полётом/освобождением); конфликты показаны. G/H/J/K сохранены. При одинаковом назначении G и ледяном посохе класс уступает Frost ICICLES. RU/EN и 30 различных прозрачных 32px эмблем/спрайтов, воспроизводимый генератор.

## 12 классов, параметры реализации

Значения — параметры работающего runtime, **не измеренные DPS/TTK и не окончательная калибровка**. CSV: [abilities.csv](abilities.csv). HP — единицы здоровья Minecraft. Бонусы/контроль IV против игроков запрещены; обычные прежние навыки не меняли PvP-политику.

| IV | Skill ID | CD, с | Срок, с | Радиус/дальность | Сила |
|---|---|---:|---:|---:|---:|
'''
for row,ability in zip(catalog['names'],abilities):text+=f"| {row[2]} | `{row[1]}` | {ability['cooldown_seconds']} | {ability['duration_seconds']} | {ability['range']} | {ability['power']} |\n"
text+='\nМеханики и ограничения каждого: RU/EN descriptions в resources, неизменяемые definitions в ClassUltimates, серверное исполнение в ClassUltimateRuntime. Радиус строки dash означает длину отдельного рывка; power полей — доля предотвращения, flurry — доля фактического урона, blood — общий HP budget. Для vulnerable_point компенсация до 20% armor reduction ограничена 2 HP; для боссов её нет, бонус 3 HP.\n\n## Пассивки\n\n| ID | Имя | Действие и ранги |\n|---|---|---|\n'
for id,ru,en,desc,_ in catalog['passives']:text+=f'| `{id}` | {ru} | {desc} |\n'
text+='\n## Испытания\n\n`Trials.<Second>` содержит числовой прогресс, encounters и Done. Сервер читает только выбранную III ветвь при уровне ≥40. Новая схватка — после >200 тиков без учитываемого боя; персональные target UUID метки предотвращают повтор. Completion даёт 400 Class XP один раз; выбор единственного IV требует отдельного подтверждения в K. Hex+slumber засчитывается только с реально присутствующим новым root, не на одной лишь попытке усыпления immune цели. Сохранённая завершённость не отзывается.\n\n| III | Условие | Проверка |\n|---|---|---|\n'
for branch,ru,en in catalog['trials']:text+=f'| `{branch}` | {ru} | Native hooks + authority/threshold tests; полный Survival-проход NOT_RUN |\n'
text+='''
Суммы щита/оберега и 15 проклятых целей требуют минимум 3 encounters. Прогресс до 40 не увеличивается. Никакое испытание не требует открытого IV, multiplayer, редкого босса или конкретного мира. Для знахаря учитывается только missing HP с недавним реальным вражеским damage credit, без overheal/самоурона. Для воеводы одиночный вариант — защитить жителя.

## Проверки и пределы доказательств

'''
text+=f'''- **{len(launches)} реальных GameTest server процессов**, включая ранние неуспешные прогоны; **0 клиентов**, 0 игровых подключений двух клиентов. Финальный integrated: **37/37 PASS**; изолированный final: **36/36 PASS**. Логи сохранены, ранние FAIL не удалены.
- Java 21 `gradlew.bat clean build` PASS в обеих копиях. В integrated также 398 RuneRework и 20491 RareRune offline assertions PASS (не игровой тест всех сочетаний).
- Integrated production JAR: `{runtime['version']}`, `{runtime['size']}` bytes, SHA256 `{runtime['sha256']}`. {runtime['checks']} JSON/PNG/localization/source/client-boundary/packaging/class-byte gates PASS.
- Изолированный review JAR: `{isolated['version']}`, SHA256 `{isolated['sha256']}`; {isolated['checks']} resource/JAR gates PASS. Test namespaces/classes/smoke не входят в production. Код и ресурсы production совпали с build outputs; новые иконки воспроизводятся побайтно.
- Все 12 эффектов проверены native fixture сценариями (для Earth Thunder ground/air transition задаёт fixture, это не клиентский прыжок). Все 6 пассивок проверены на рангах 1/2/3 и 0/1/2 equip-slot paths. Threshold/authority тесты покрывают 12 trials; это не 12 человеческих игровых прохождений.
- Native death/respawn проверяет новый Third/Passive2/Trials/XpLedger/cooldown и прежние три класса. Native `.dat` save→**новый процесс сервера**→load подтверждает новые поля/атрибуты/cooldown. Это реальный disk/server restart test; **не настоящий client relog**.
- Первые прогоны обнаружили бесплатные group_* awards; исправлены. Остальные ранние fixture FAIL: XP вместе с Monster Hunter, игрок вне world entity index, shield timer через tick вместо doTick, прицел выше spider. Эти ранние результаты не скрыты.

## Темп XP

Пороги: 10 = 2700, 25 = 16200, 40 = 40950, 50 = 63700 XP; старая кривая сохранена. [xp-pacing-model.csv](xp-pacing-model.csv): 3 базы × боевой/исследовательский профили ×4 уровня. Это расчёт с явно указанными частотами, не измеренные игровые часы. Учтено исчерпание 118 одноразовых mod-story awards; новые реальные structure sites остаются доступными до ledger cap. Модель не гарантирует, что сюжетные задания из профиля выполнятся с такой скоростью без боя.

Боевой пример (90 мобов,30 ролевых действий,3 истории,2 новых места в час): уровни 10/25/40/50 — примерно 1.25/7.5/18.97/29.5 ч. Исследовательский пример (0 мобов,6 историй,4 новых места/ч): 1.42/8.45/24.1/55.7 ч. **Не менять баланс по этим цифрам как по playtest**. Survival route trace и фактический тайминг NOT_RUN.

## Оставшиеся ограничения

1. Issue #19 не завершён: клиентская визуальная/звуковая приёмка K/HUD, GUI scales 1–4/узкое окно/F1, фактические G/Frost/U/JEI conflicts, подключённый dedicated +2 клиента, PvP/team/claims и настоящий client relog NOT_RUN. Клиент автоматически не запускался по AGENTS.md.
2. Полные DPS/TTK/armour/elite/boss matrix для 12 abilities ×54 руны/легендарное оружие, длительный autonomous decoy/guard AI, все 12 Survival trial маршрутов, измеренные MSPT/FPS/нагрузка NOT_RUN. Native effects и offline regression не заменяют эти проверки. Числа остаются initial spec, а не доказанным балансом.
3. Новые поля сохраняются, но чужие моды с нестандартным damage/heal cancellation и claim APIs отдельно не проверены. Spawn provenance учитывает известные native origins/clone flags; неизвестный чужой summon без origin metadata не объявляется доказанно отфильтрованным.
4. Старые исторические отчёты/verify скрипты 1.3.1 не заменяют `tools/verify_classes3.py` и текущий отчёт. Никакая установка в PolyMC/изменение пользовательских миров/merge в main не выполнялись.

## Краткая ручная приёмка

1. В отдельной копии мира: K, G/H/J, U, переназначение, Frost G, UI scales/F1, обе локали и 30 иконок.
2. На каждой базе: обычное Survival XP, повтор моба/события/структуры, спавнер, Creative и PvP; сравнить vanilla XP отдельно.
3. Уровни25/40: оба пассивных слота, реальные маршруты 12 испытаний, подтверждение только своей IV ветви; смерть и реальный client rejoin с cooldown/рангами.
4. Все 12 умений против обычного/бронированного/элитного/босса и группы, дружинника/жителя/тиммейта; проверить отсутствие урона союзникам, повторного DoT/поля и поведения после выхода владельца.
5. Dedicated с двумя клиентами: claims, PvP, 54 руны/легендарное оружие, замер MSPT/FPS и фактический XP route; только после этого принимать баланс/релиз и отдельно разрешать установку.

## Коммиты и PR

Отдельные зависимые ветки A→B→C/D→UI/QA; промежуточные draft PR не объявлены автономно принятыми релизами. Список изменений: [CHANGED_FILES.txt](CHANGED_FILES.txt). Ссылки после публикации: [PUBLICATION.md](PUBLICATION.md).
'''
for base in [R,W]:
 out=base/rel;out.mkdir(parents=True,exist_ok=True);(out/'REPORT.md').write_text(text,encoding='utf-8')
 summary='''## Classes 3.0 — implementation candidate (2026-10-14)

Реализованы Survival Class XP/anti-abuse, v2→v3 без сброса, второй пассивный слот и6×3 пассивки,12 trial-gated IV с отдельным U/HUD,12 server-event испытаний, RU/EN и30 эмблем. Clean builds Java21 PASS; final integrated37/37 native cases, final isolated36/36 и реальная native .dat загрузка новым процессом PASS; RuneRework398/RareRune20491 offline PASS. Всего15 GameTest servers,0 clients. Issue19 PARTIAL: полный Survival/GUI/2-client MP/claims/DPS-TTK/MSPT/FPS/настоящий client relog NOT_RUN; initial balance не назван игровым замером. Версия/registry ID/PolyMC/миры не менялись этой задачей. Изолированные dependent draft PR; main не merge-ился. [Отчёт и ограничения](implementation/classes3/REPORT.md).

'''
 for name in ['PROJECT_STATUS.md','ROADMAP.md','ARCHITECTURE.md']:
  p=base/'docs'/name;body=p.read_text(encoding='utf-8');p.write_text(summary+body,encoding='utf-8')
 arch=base/'docs/ARCHITECTURE.md';body=arch.read_text(encoding='utf-8');body=body.replace(summary,summary+'''ClassState сохраняет прежний Class2 compound и ключ Passive; Passive2 включается только валидным First→Second. ClassUltimates — отдельные immutable definitions, Third/Ultimate не занимают Active0..2. ClassTrialService слушает подтверждённые события, Trials/ledger/cooldowns остаются persisted, transient actions чистятся отдельно. ClassUltimateRuntime: один конечный action/owner, bounded local queries/target limits, логический swept shadow projectile и помеченный vanilla decoy. Старый player tick обслуживает только конкретные активные действия; GuardBehavior использует свой прежний40-tick decision cadence. Payload Classes3 отделён версией; server/Common не импортирует client. Presentation не отправляет XpLedger. Генератор classes3_assets воспроизводит30 иконок/RU-EN и bloodless tag, исторический class resource generator вызывает его последним.

''',1);arch.write_text(body,encoding='utf-8')
 changed=subprocess.check_output(['git','diff','--name-only','2389b876'],cwd=W).decode().splitlines()+subprocess.check_output(['git','diff','--name-only','2389b876','HEAD'],cwd=W).decode().splitlines()
 changed+=['docs/implementation/classes3/'+p.name for p in out.iterdir() if p.is_file()]
 (out/'CHANGED_FILES.txt').write_text('\n'.join(sorted(set(changed)))+'\n',encoding='utf-8')
 print('Reports written:',base)
