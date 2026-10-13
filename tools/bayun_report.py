"""Write final evidence from gates; never upgrades an unrun client case to PASS."""
from pathlib import Path
import hashlib,json,re,subprocess
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/bayun-book-final'
gate=json.loads((O/'resource-gates.json').read_text(encoding='utf8'))
log=(R/'work/bayun/gates-final.log').read_text(encoding='utf8')
measure=re.search(r'BAYUN_SNAPSHOT_MEASURE total100Ms=([\d.]+) averageMs=([\d.]+)',log)
wire=re.search(r'BAYUN_WIRE_PASS bytes=(\d+)',log)
launch_logs=['audit1','behavior1','behavior2','behavior3','behavior4','behavior5','recipes','final-runtime','restart-write','restart-write2','restart-read','restart-read2','restart-read3','final2','final3','wire-final','release-final','acceptance-final','gates-final']
# Filenames of the earliest exploratory runs are listed separately in AUDIT;
# count reflects actual launched JVMs, not clean builds/offline bootstraps.
launches=19
def write(name,text):(O/name).write_text(text.strip()+'\n',encoding='utf8')
write('REPORT.md',f'''# Баюн и Книга сказаний — кандидат 1.6.9, 2026-10-11

Реализация и автоматические gates завершены. **Финальная игровая приёмка НЕ завершена:** пользователь запретил автоматический запуск клиента. Поэтому внешний вид в игре, прослушивание, интерфейс на реальных экранах, обычный multiplayer и MSPT/FPS не выдаются за проверенные.

- Новый постоянный Кот Баюн: персональное доверие, молоко/сырая рыба, четыре работающих ответа, сон/уход/движение, предупреждение, песня, притяжение и физический прыжок с отменой за стеной и при промахе.
- Прежний `slavicmyths:lore_book`: однократная выдача, знания по UUID мира, восемь разделов, три уровня оглавления, поиск, история, реальные рецепты, осторожные ориентиры и настоящее дерево достижений.
- 217 записей; 1666 строк покрытия; 58 отдельных русских описаний живых существ. Технические сущности явно привязаны к владельцам. Квесты Яги читаются без изменения её данных и наград.
- Сохранены все прежние registry IDs, 162 старых достижения, их criteria/requirements/rewards и текущие модели/голоса Кузи. Добавлены 4 достижения событий и 9 заголовков разделов без XP.
- Исправлены четыре прежних испорченных русских имени подземников/Кладовика. Остальные старые строки сохраняются.

`clean build` PASS; native Bayun **364871**, Kuzya **471397**, doghead **73926** checks PASS. Последний настоящий dedicated GameTest: **15/15 PASS**. Реальный серверный мир был записан на диск, сервер остановлен и снова запущен: UUID кота, доверие 25, знания, факт выдачи книги и настоящий vanilla advancement progress сохранены. Это **не клиентский перезаход**.

Всего Minecraft: **{launches} dedicated server запусков, 0 клиентов**. Шесть ранних прогонов завершились ошибкой и были исправлены; последний прогон прошёл. Offline bootstrap и сборки не включены в число запусков. В тестах используются native ServerPlayer с тестовыми соединениями; два таких игрока не считаются полноценным multiplayer двух клиентов.

Production JAR: `D:/slavic-myths/build/libs/slavicmyths-1.6.9.jar`; копия `D:/slavic-myths/release/slavicmyths-1.6.9.jar`.

SHA256: `{gate['sha256']}`. Resource/JAR checks: **{gate['checks']} PASS**; 19 OGG декодированы, mono, не пустые и без PCM clipping. Это оригинальные синтезированные прототипы из пакета задания, **не записи живого кота**. Игровой микс ещё требуется прослушать.

[Матрица приёмки](TEST_RESULTS.md), [ресурсы](ASSET_MANIFEST.md), [русский язык](RUSSIAN_QA.md), [измерения](PERFORMANCE.md), [аудит](AUDIT.md), [изменённые файлы](MODIFIED_FILES.txt).

Ручная проверка коротко: в отдельном мире получить/потерять книгу и перезайти; открыть все разделы на русском при GUI scale 2/3/4; поставить Баюна яйцом или `/slavicmyths dev bayun generate` на подходящей площадке, проверить молоко/рыбу/отход/стену/уклонение, сон и звучание. В новом обычном мире проверить естественную поляну. Затем проверить двух настоящих клиентов и старое поручение Яги. Тестовые миры находятся только под `work/bayun`; игровые миры пользователя не менялись. Commit/push не выполнялись.
''')
matrix=(R/'docs/bayun/spec/Slavic_Myths_1.6_Final_Bayun_Book_Codex_TZ/07_QA/TEST_MATRIX.md').read_text(encoding='utf8')
cases=[]
for line in matrix.splitlines():
 if re.match(r'\| (BK|BA|AD|YG|RU|RE)-\d+',line):
  a=line.split('|');cases.append((a[1].strip(),a[2].strip()))
passes={
 'BK-02':'Native pendingInventoryAndLegacy: полный инвентарь → pending → ровно одна выдача; повтор без дубликата. Настоящий сетевой login отдельно NOT_RUN.',
 'BK-11':'hintAndStateRoundtrip: смещение 25–80, радиус 250, Target/Anchor не отправляются; wire codec roundtrip PASS.',
 'BK-12':'Тот же GameTest: moved target за пределами круга и истёкший срок дают Stale=true.',
 'BK-14':'CONTENT_COVERAGE.csv: все 75 actual EntityType, включая явные группы технических типов; gate проверяет владельцев.',
 'BA-03':'immunityAndSpectator: реальные native damage sources sword/fire/lava/drown/explosion/void не наносят урон; огонь/воздух восстановлены.',
 'BA-05':'milkFishAndPersonalTrust: milk → empty bucket, trust25, canTalk=true; предупреждение отдельно отменяется warningGiftCancellation.',
 'BA-07':'Неподходящий предмет сохраняется, trust0; повторный подход с рыбой после молока не расходует её и не повышает доверие.',
 'BA-09':'actual pounce chain: реальная физика и близкий удар после grace/warn/song/charm; смерть survival native игрока подтверждена.',
 'BA-10':'solidWallCancels: реальная преграда при предупреждении; игрок жив.',
 'BA-12':'Creative survival switch и отдельный spectator GameTest: без смерти/участия в опасной встрече.',
 'BA-16':'independentVisitors: A принёс молоко и жив, B не принёс и погиб от собственного encounter. Native server players; клиентский MP отдельно NOT_RUN.',
 'BA-21':'warningGiftCancellation: подарок при warning, отмена опасности, survival жив на последующей проверке.',
 'BA-22':'physicalMissDoesNotAutokill: пounce действительно начался, игрок сместился после старта, промах не превратился в удалённое убийство.',
 'AD-03':'175 JSON; ссылки существующие, граф без циклов; hidden nodes фильтруются actual vanilla progress.',
 'RU-01':'UTF-8 JSON, паритет новых RU/EN keys, точное исправление четырёх legacy mojibake имён; новые строки без U+FFFD/CP1251 corruption.',
 'RE-01':'build.log: actual clean build, Java21, production JAR; native regressions PASS.',
 'RE-02':'resource-gates.json: все текущие code/resources в production JAR, тестовые classes/namespaces отсутствуют.',
 'RE-03':'polymc-installation.json: устанавливается только после gates в прежний 1.21.1-Testing, backup вне mods; профиль 1.16.5 не затронут.'
}
partial={
 'BK-01':'Service fixture new=1/repeat=0 PASS, настоящие 2/3 сетевых login NOT_RUN.',
 'BK-03':'Удаление книги из native inventory не вызывает повтор; настоящий огонь и item destruction NOT_RUN.',
 'BK-04':'Данные SavedData независимы от inventory; настоящая смерть/rejoin без keepInventory NOT_RUN.',
 'BK-05':'Настоящая смерть/rejoin с keepInventory NOT_RUN.',
 'BK-06':'Настоящий disk restart сохранения PASS, перемещение книги в сундук/клиентский relog/смена измерения NOT_RUN.',
 'BK-07':'Архитектура UUID-world, не NBT предмета; две реальные клиентские копии NOT_RUN.',
 'BK-08':'Snapshot encode/decode PASS, 487 actual recipes; реальные экраны SP/MP и freezes NOT_RUN.',
 'BK-09':'Функциональная навигация реализована; ручной проход всех кнопок NOT_RUN.',
 'BK-10':'Реальные 1366×768, scale2/3/4 screenshots NOT_RUN.',
 'BK-13':'Monotonic stage/no invented defeated PASS; реальная ранняя boss rumor → собственный бой с boss NOT_RUN.',
 'BK-15':'Native independentVisitors PASS; два настоящих клиента NOT_RUN.',
 'BK-16':'Код fallback/icon protection и packed asset checks; реальный client resource reload/missing assets NOT_RUN.',
 'BA-01':'В GameTest площадка, опоры, отказы до записи и 2 выхода PASS; естественный spawn на обычном seed NOT_RUN.',
 'BA-02':'Настоящий dedicated restart UUID/bond/home PASS. Клиентский relog и долгое естественное выгруженное состояние NOT_RUN.',
 'BA-04':'Native timers/цепочка PASS; настоящее audible/visual first notice NOT_RUN.',
 'BA-06':'Рыба consumed1, trust23, safe PASS; настоящее изображение жевания NOT_RUN.',
 'BA-08':'Native отход30блоков и отмена PASS; реальный клиентский контроллер NOT_RUN.',
 'BA-11':'Проверки same level/online/eligible + client TTL статически; реальные teleport/dimension/disconnect mid-charm NOT_RUN.',
 'BA-13':'Cancellation paths реализованы; смерть от другого источника при реальном клиентском charm NOT_RUN.',
 'BA-14':'Actual disk restart trust25 + native cooldown PASS; настоящий быстрый client rejoin NOT_RUN.',
 'BA-15':'Empty-hand shift interaction trust+4 once PASS; настоящее rub animation NOT_RUN.',
 'BA-17':'17 actual native animation timelines/joints offline PASS; in-game clips/пять кадров/переходы NOT_RUN.',
 'BA-18':'19 actual samples mono/decode/subtitles/routing PASS; in-game attenuation/микс/прослушивание NOT_RUN.',
 'BA-19':'Измерен серверный snapshot, не stress MSPT/FPS/memory. Длительный нагрузочный опыт NOT_RUN.',
 'BA-20':'Production без chunk tickets/forced loading; многочасовое выгруженное состояние NOT_RUN. Тестовый forced ticket существует только в изолированном restart harness.',
 'AD-01':'Native vanilla advancement JSON и disk restart PASS; настоящий существующий пользовательский мир не открывался.',
 'AD-02':'Actual Knowledge.award/PlayerAdvancements bridge PASS; клиентский toast NOT_RUN.',
 'AD-04':'Реальное GUI scale4 NOT_RUN.',
 'YG-01':'Yaga classes/data/rewards побайтно сохранены; настоящий игровой проход старого поручения NOT_RUN.',
 'YG-02':'Native readonlyBridgeAndRealGraph: чтение не создаёт Yaga Progress; проверен nonce/replay Баюна. Совместный игровой проход NPC NOT_RUN.',
 'RU-02':'Настоящие скриншоты русского GUI NOT_RUN по запрету пользователя на автоматический client launch.'
}
write('TEST_RESULTS.md','''# Матрица приёмки 1.6.9

Кандидат, не финально принятый релиз. PASS означает проверку указанного условия; native ServerPlayer с тестовым соединением не заменяет настоящий клиент. Частично покрытый комбинированный сценарий помечен NOT_RUN и содержит точную границу. Ни один ручной случай не получил PASS за компиляцию.

Основное evidence: `server-final.log` (15/15), `build.log`, `resource-gates.json`, `audio-qa.json`, `content-density.json`, `regeneration.json`; реальный restart — `restart-write2.log`/`restart-read3.log` под `work/bayun` и копии здесь. Ранние FAIL исправлены, но сохранены в AUDIT. Всего 19 dedicated запусков, 0 клиентов.

| ID | Статус | Проверка | Evidence / ограничение |
|---|---|---|---|
'''+ '\n'.join(f'| {id} | {"PASS" if id in passes else "NOT_RUN"} | {case} | {passes.get(id,partial.get(id,"Ручная игровая проверка не запускалась."))} |' for id,case in cases))
write('PERFORMANCE.md',f'''# Измерения и пределы работы

Реальный dedicated GameTest server, последний прогон: **100 вызовов TaleService.snapshot**, суммарно {measure[1]} ms, среднее **{measure[2]} ms**, 217 записей и 487 реальных серверных рецептов. Это среднее время формирования серверного снимка после прогрева, **не задержка открытия клиентского экрана, не p99 и не MSPT**. До кэша рецептов среднее того же упражнения было 11.039 ms. Вариативность последних прогонов 2.025–2.523 ms не считается измеренным обещанием для любого оборудования.

Настоящий Snapshot.CODEC encode/decode: **{wire[1]} bytes**; лимит теста <900000. Данные locale не дублируются в пакете. Кэш рецептов weak-key RecipeManager, сбрасывается при reload; клиент хранит snapshot до logout. Полный снимок — при открытии (cooldown10 ticks), локальные изменения — компактными delta. Read rate4ticks, choice rate10ticks, nonce/sequence/TTL1200.

На кота максимум32 активных персональных clocks, local notice раз в10ticks в12блоках, online UUID lookup, один физический victim. Нет поиска всех сущностей мира или всех игроков на world tick. Ambient выбор раз30–90s в пределах4блоков дома, native navigation только для уже загруженного места. Song particles3 раз в5ticks; звук ограничен cooldown и одним loop на loaded NPC. Хинты не вызывают locate и не загружают anchor chunk.

Book observation: один sample на игрока раз20ticks, local12 AABB максимум64 кандидата; известные записи не повторяют LOS. Structure detection лишь при смене block position/dimension, максимум32 starts в **уже загруженных** chunks через getChunkNow. Нет getStructureWithPieceAt/forced tickets. Открытие книги не выполняет spatial scan.

Native geometry143bones254cubes3048triangles; paw grounding32 corners, не обход всего mesh. Real MSPT для 1/10 котов, p99 AI, client FPS, memory leak/hour, mixed multiplayer, GUI cold latency: **NOT_MEASURED**. Один fixture snapshot benchmark не заменяет эти измерения. Restart fixture использует временный forced chunk только в test namespace, который исключён из production JAR.
''')
audio=json.loads((O/'audio-qa.json').read_text(encoding='utf8'))
write('ASSET_MANIFEST.md','''# Ресурсы 1.6.9

Оригинальный архив проверен по 51 SHA256 entry (`archive-integrity.json`). Изображения references служат направлениями дизайна и не выданы за готовые игровые ресурсы. Источник звука — `docs/bayun/spec/Slavic_Myths_1.6_Final_Bayun_Book_Codex_TZ/09_ASSETS/audio`; права/характер источников описаны в `10_SOURCES/RIGHTS_AND_TECH.md` и `AUDIO_MANIFEST.csv` этого пакета. Все 19 samples — оригинальные синтезированные прототипы, не live cat recordings и не голос из мультфильма.

- `docs/bayun/kot_bayun.bbmodel`: редактируемые 143 bone groups/254 cuboids, embedded 256 skin, 17 imported CSV animations. Runtime IK, grounding, переходы и наследование curled sleep pose реализованы дополнительно в native renderer, не выдаются за полную автономную функциональность Blockbench playback.
- `docs/bayun/rig.json` + `tools/bayun_assets.py`: воспроизводимые native geometry/UV/Java animation curves, 225 CSV keys, 17 timelines.
- `assets/slavicmyths/textures/entity/kot_bayun/skin.png`: 256×256, body16px/block, face64px/block, два отдельных eye islands12×8 с pupils/highlights; только eyes fullbright, eyelids независимы.
- `assets/slavicmyths/textures/gui/tales/`: восемь разных 32px category icons; `docs/bayun/category-icons.png` contact sheet.
- `assets/slavicmyths/textures/item/lore_book.png`:64×64 предмет; прежний registry ID.
- `BayunMesh`/`BayunClient`: native model, отдельные jaw/ears/eyelids/claws/tail/2-joint legs; сон наследует сложенную позу, не поворачивает стоящую модель на90°.
- `native-model-preview.png`: статическая проекция реальной native geometry/UV, **не игровой скриншот**. Все 17 native poses проверены offline; реальное in-game видео/GIF NOT_RUN.
- `tools/tale_content.py`, `creature-notes.json`:217 entries, 58 отдельных русских living profiles, actual registry ownership and recipes. RU tiers45–95/45–100/60–130/80–160 words проверены. English сохраняет краткие механические описания и все новые ключи.
- 19 SoundEvents с RU/EN subtitles и clip markers; client song loop движется вместе с котом, fades/stop при уходе. Реальный игровой микс ещё не принят.

## Декодированные OGG

| Файл | секунд | RMS (PCM16) | peak | SHA256 |
|---|---:|---:|---:|---|
'''+ '\n'.join(f"| {a['file']} | {a['seconds']:.3f} | {a['rms']:.1f} | {a['peak']} | `{a['sha256']}` |" for a in audio))
write('RUSSIAN_QA.md',f'''# Русский язык

Новые строки UTF-8 JSON parse, RU/EN key parity, no U+FFFD, CP1251→UTF8 mojibake check: PASS. Новых ключей: {gate['new_locale_keys']}. Проверка production JAR, не только source. «Книга сказаний», «Кот Баюн», восемь названий разделов и четыре уровня знаний используют native Minecraft font; текст не вшит в PNG. Заголовки и описания из actual Components, перенос через font metrics; scissor/scroll для длинной страницы. EN — краткое механическое описание каждого нового ключа.

Четыре ранее повреждённых ключа исправлены ровно обратным CP1251→UTF8 преобразованием: `entity.slavicmyths.podzemnik`, `podzemnik_miner`, `kladovik`, `large_podzemnik`. Другие прежние locale значения побайтно/по ключам сохраняются. Кузя остаётся «Домовёнок Кузя», его пять оригинальных мультфильмовых файлов не изменены. Русские названия vanilla loot взяты из локального установленного asset index17; минимальный used-name snapshot в `docs/bayun/vanilla-ru-1.21.json`.

58 индивидуальных RU living profiles: density PASS (`content-density.json`). Body — игровая интерпретация; сведения об API/loot/HP взяты из actual classes/registry/recipe manager. Technical actors описаны у владельца и не названы новой мифологией.

**Реальные скриншоты русский GUI 1366×768 scale2/3/4: NOT_RUN.** Клиент автоматически не запускался по прямому отказу пользователя. Папка для ручных captures: `docs/verification/bayun-book-final/screenshots/` (пока пустая). Русский текст в коде проверен, его реальный рендер и читаемость на этих размерах не приняты. Поэтому status кандидата не объявляется окончательным release complete.
''')
(O/'screenshots').mkdir(exist_ok=True)
for name in ('restart-write2','restart-read3'):
 p=R/'work/bayun'/f'{name}.log'
 if p.exists():(O/f'{name}.log').write_bytes(p.read_bytes())
old=(O/'AUDIT.md').read_text(encoding='utf8')
old=old.replace('One actual dedicated server launched for audit, zero clients.','Initial audit: one dedicated server, zero clients. Final count: 19 dedicated servers, zero clients; six early failed runs were repaired, thirteen successful runs including the final 15/15 suite.')
addition='''

## Final candidate audit (2026-10-11)

Runtime registry:75 entity types (+kot_bayun),713 items (+spawn egg),244 blocks (unchanged),20 feature types (+bayun_nook),17 structures (unchanged),58 configured/52 placed features (+one each),487 real recipes (unchanged). All1666 registry rows represented in217 entries;58 individual living profiles, technical projection/seat/projectiles associated with owners. No Yaga schema/class/reward rewrite; no bogatyr or joint quest introduced.

Old162 advancement IDs/criteria/requirements/rewards preserved;13 new display nodes (4 real events,9 sections without rewards). Native vanilla progress persisted through an actual saved-server-world restart. Read-only Yaga lookup does not create a Progress. Four old Cyrillic name corruptions corrected; other prior keys, all Kuzya audio/models and all unrelated classes/resources checked against accepted1.6.8 JAR. No commit/push.

Early failed dedicated runs: invalid `$` catalog resource paths, two nook fixtures whose test roof spoiled the heightmap, malformed restart fixture namespace, two restart reads before native chunk/entity activation. Corrected fixture waits/preflight and resource names; no failures hidden. Final native runtime tests15/15 PASS; real restart PASS; clean build/offline/resource/JAR/audio PASS. Client visuals/audio/controller/real2-client MP and ordinary-world natural seed remain NOT_RUN, actual MSPT/FPS NOT_MEASURED. Do not call this fully accepted final release.
'''
if '## Final candidate audit' not in old:old+=addition
(O/'AUDIT.md').write_text(old,encoding='utf8')
tracked=subprocess.check_output(['git','diff','--name-only'],cwd=R,text=True).splitlines()
untracked=subprocess.check_output(['git','ls-files','--others','--exclude-standard'],cwd=R,text=True).splitlines()
write('MODIFIED_FILES.txt','\n'.join(sorted(set(tracked+untracked))))
print('Evidence reports written; manual/client cases remain NOT_RUN.')
