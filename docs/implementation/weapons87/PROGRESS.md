# #87 — журнал исполнения

Поручение владельца: 21.10.2026, вся программа #87/#115–121/#157–192. Один writer текущего main. Коммитов, push, изменения установленного PolyMC и запусков игры нет.

Исходное состояние: HEAD `32d1733457e7e90360ce16c23860304ece664c64`, удалённый main `20cca4ec20f824862ed63b34d97a4aebcc2f6084`. Разница — 18 коммитов документации/workflow/project migration; production source одинаков. Чужие рабочие доработки включены в начальный baseline (9563 файла), не сбрасывались.

## Порядок пакетов

1. #157: все 68 IDs, атрибуты, получение, эффекты, ресурсы и источники. Аудит выявляет дефекты, а не автоматически меняет баланс.
2. #192: индивидуальные технические листы для подтверждённых FIX, доказательные KEEP; отдельные листы семейств и деталей.
3. #166/#167/#168 → #116/#181: детали и статическая сборка кистеней. Затем #115/#117 корректный хват, #164 динамика кистеней. Числа Ataman MaceItem не переносить из FlailItem.
4. #169–174 и #175–189 адресно: KEEP исправные, FIX с исходными доказательствами. Не трогать наконечники #99/#100.
5. #162/#163/#165/#190: независимые анимационные пакеты после соответствующей геометрии. #160 механики и #161 VFX — отдельно от урона и художественных правок.
6. #158/#159: конкретные предложения по числам на согласование владельцу; независимые художественные задачи продолжаются.
7. #121: воспроизводимость и ownership генераторов, #191 интеграционный gate; #118/#119/#120 — координаторы своих детей.

## Текущее состояние

- #157 IN_PROGRESS: `audit-68.json/csv`, `native-attributes.json`, `catalogue-provenance.json`, `SOURCE-68-BEFORE.png`. У каждого ID реальные пути/хеши моделей и текстур, JSON-рецепты/лут и ссылки из исходников. Отдельно отмечены получение посоха Велеса, прочность, касты и непроверенный runtime.
- Java21 clean build + offline экспорт 68 production constructors: PASS (`audit-build.log`). Производственный JAR идентичен исходной сборке 1.9.7.53: SHA256 `5cc58c93b828617d607e06d860d40ae03bc15615360e3bf202b6c8b95ac86fc5`. Это baseline build, не новая оружейная сборка.
- `audit-gates.json`: 9563 исходных src/tools/gradle-файла не изменены, offline классов в production JAR нет. `before-source.zip` и `before-manifest.json` — исходные реальные ресурсы до правок.
- Project Status не изменён: доступного инструмента записи поля нет. В #157 опубликован комментарий о начале; рекомендуемый статус «В работе». Все Issues остаются OPEN, приёмки не заявлены.

## Доказательства и ограничения

Проекции из фактического JSON/UV/PNG являются offline source previews, не скриншотами Minecraft. Измеренные ItemAttributeModifiers не являются измеренным боевым DPS. Полный expected DPS зависит от попаданий, armor/toughness, immunity, hit cooldown, фаз босса, зарядки и специальных условий. Неясности в аудите сохраняются явно.

Для каждого пакета: `REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN`. Только owner runtime NOT_RUN допускается при технической сдаче. Исполнительские пробелы удерживают соответствующую Issue «В работе».

## Пакет 1 — технические gates пройдены

Согласованный пакет A прочности реализован через WeaponDefaults. Статические #166/#167/#116/#181: production69 assets, индивидуальные source blueprints и реальные A/B, Steve/Alex/обе руки. Java21 clean build и resource/JAR PASS; generator sandbox PASS без отката чужих ресурсов. Точный промежуточный JAR/source SHA — package1-receipt.json, отчёт PACKAGE-1.md. В GitHub опубликованы результаты и рекомендуемое «Тестирование» статических детей; реальное поле не менялось. Parent87 и остальные дети IN_PROGRESS. Следующий независимый пакет — #164 динамика цепей, затем общий хват/остальные индивидуальные задачи. PolyMC не менялся, game launches0.

## Пакет 2 — хваты

28 display-моделей и player-only ScytheGrip реализованы, геометрия/UV/PNG сохранены. Все68 IDs просмотрены в native source body/hand matrices;22 A/B листа. Clean build Java21, native ItemTransform/custom scythe pivots/contact direction, resource/JAR и sandbox generators PASS. Точные данные PACKAGE-2.md/package2-receipt.json. #115 остаётся IN_PROGRESS: продолжить полную source-проверку первых лиц и custom paths, затем #164/#165 по зависимостям. B/C/D баланс не согласован и не применён. Кандидаты fixed-pose head intersection только у двух старых принятых чеканов, оставлены KEEP и явно обозначены для owner review.

## Продолжение первых лиц #115

В TugarinPairClient обнаружен реальный пропуск отрисовки кистей после cancel RenderHandEvent: добавлен существующий WeaponHands.renderGrip для фактической руки/рукава игрока. WeaponHands wrist/model/arm transforms и TugarinPairClient wrist выделены в статeless client-only методы без изменения старой математики; они непосредственно вызываются offline source exporter. fp-native.json:592 actual native transform/arm mesh samples для68 IDs,4 custom paths, Steve/Alex и изолированных offhand путей косы/меча Тугарина. fp-native-check.log PASS; perspective source sheets ещё генерируются tools/weapons87_fp_review.py. После этого нужны source review и clean build/resource/JAR по новым клиентским изменениям. Полный #115 ещё не сдан, динамика #164 не начиналась.

## Пакет 3 — первые лица и техническая часть #165

Scythe wrist orbit70→35° исправляет выход кисти/оружия из source camera при windup, frame contact/rest не меняется. Для обычного offhand косы JSON FP matrix теперь равна actual custom mainhand idle для физической стороны. Предложения до production и старые source images сохранены в blueprints/firstperson/. Видимые кисти Тугарина добавлены в собственный отменённый RenderHandEvent path. Java21 clean build и actual native attributes/grips/FP exporters PASS (package3-build.log). 592 actual pose/hand source samples,68 IDs,4 custom renderers. Последние perspective sheets с отдельным item_pixels ещё генерируются; parallel isolated generator/JAR gates выполняются. Новые jobs: FP render session26725, verify75939 (при потере сессий читать выходные файлы/логи, не запускать тест повторно без причины).

### Пакет 3 gates

Последние generator/JAR gates PASS;19 perspective source sheets/592 samples/68 IDs, включая изолированный offhand, сохранены в package3-fp-source-evidence.zip. Коса AFTER имеет882–2483 opaque weapon pixels во всех sampled phases, отдельно от руки. PACKAGE-3.md/package3-receipt.json фиксируют точный промежуточный JAR bdcf7869debdcd3b99c825642f2b4a952d15ffd47945423017b17c621fba9bf9. Установленный PolyMC прежний. Следующий доступный пакет #164; не останавливаться после этого отчёта.


### #87 продолжение — пакет4,21.10.2026

Пакет4 #164 технически реализован:8 dynamic flails,17 split part models,actual owner binding, сохранённый server combat.15120 численных production поз,24 source A/B sheets/248 FP records;124/124 AFTER ball centers в fixed camera. Java21 clean build, resource/JAR/generator sandbox PASS. PACKAGE-4.md/package4-receipt.json содержат точный JAR/source. Owner runtime NOT_RUN, PolyMC не установлен/не менялся. #16820 KEEP source audit ждёт готовых #171/#173. Пять индивидуальных #171 draft sheets подготовлены до production; продолжается цепочка.


### #87 продолжение — пакет5,21.10.2026

Пакет5 #171 технически реализован:5 FIX held копий,4 KEEP,15 models/0 PNG edits. До production5 чертежей;72 native transform cases и180 source cases/9 A/B листов, все PNG/4 KEEP сохранены, AFTER fixed-idle head candidates0. Java21 clean build/resource/JAR/generators PASS. PACKAGE-5.md и package5-receipt.json фиксируют точный промежуточный JAR/source. #164 owner binding очищается также при reload/non-custom resource-pack replacement. Вся #87 IN_PROGRESS; далее #173 и семейные адресные KEEP/FIX. PolyMC не менялся.


### #87 продолжение — пакет6,21.10.2026

Пакет6 #173 технически проверен:14 art KEEP, flying context FIXED→NONE устраняет унаследованный Y180.420 actual native direction cases,14 A/B source sheets/84 frame cases,28 model/PNG+2 server files byte-identical. Java21 clean build/resource/JAR/generator PASS; PACKAGE-6.md. #1696 KEEP, #1723 inventory/art KEEP (bone_arrow по-прежнему vanilla Arrow в полёте), #1708 KEEP и1 подтверждённый FIX silver_mace (terracotta вместо серебра/отделённые полосы). #170 IN_PROGRESS, готовится отдельный source blueprint. #87 не завершена, PolyMC не менялся.


### #87 продолжение — пакет7,21.10.2026

Пакет7 #170: silver_mace FIX реализован по pre-production чертежу;3 старых связных компонента→1, серебристый atlas и4 рёбра вместо terracotta/плавающих полос, front-lit GUI. Все hand/ground/fixed matrices прежние;20 source native cases и0 AFTER idle head candidates.8 остальных art KEEP/чеканы сохранены. Java21 clean build/native/resource/JAR/applicable generator gates PASS; PACKAGE-7.md. #168 final source role contrast после #171/#173 теперь PASS, owner runtime остаётся. #87 продолжается, next #174 (carved/storm имеют одинаковую fork geometry; нужен storm FIX).


### #87 продолжение — пакет8,21.10.2026

Пакет8 #174 технически проверен:4 KEEP, storm_staff отдельная connected14-box perunite/crystal оправа вместо duplicate11-box woodenfork. Same hand/ground/fixed,0 shared PNG edits,30 length/gripY10.5 source A/B sheets/100 native cases,head candidates0; Java21 clean build/resource/JAR/historical generator replay PASS. PACKAGE-8.md/receipt8. Далее uniques175–189; все15 fresh descriptions сверены с исходным прочитанным snapshot — изменений0. #87 IN_PROGRESS, PolyMC не менялся.

### Пакет9,22.10.2026

#182 исправлена GUI-пара Тугарина: native-aspect canvas16x16 каждого существующего32px спрайта и боковые exposed edges вместо сжатых7x16 наклонённых плоскостей.1 root JSON,0 PNG edits; original held/PNG/renderer/server pair unchanged. Чертёж/варианты до production, actual A/B GUI/ground/fixed,18 retained native FP source matrices. Java21 clean build + fixtures PASS; resource/JAR/generator replay5617 PASS. PACKAGE-9.md/receipt9 и frozen source evidence. SHA3be53c3613900d75e6e6076a7944370af706cc2dd52196a0b79099e657bf6b8d,23694549 bytes,version53,NOT_INSTALLED. Runtime OWNER_PENDING. Номера uniques сверены:182 Tugarin,183 Nightingale. Начат отдельный пакет10 #163 bow camera timing; crossbow/MP часть163 ещё продолжается. #87 IN_PROGRESS.

### Пакет10,22.10.2026

BowDrawClient исправил FP divisor20→actual12/30 без server/art/ammo edits.606 actual factory power comparisons,120 independent vanilla pose checks,240 native samples и72 source views. Clean build/native/resource/JAR/generators PASS; PACKAGE-10.md. JAR SHA24a49c19a9ba33584c1bd27e482d42d21c35a5271e8514db0b1ae068d02b3d66,23697592 bytes,version53,NOT_INSTALLED. Issue163 IN_PROGRESS с crossbow исполнительским остатком, не Testing. Issue87 IN_PROGRESS, далее180 static art и190 отдельная animation.

### Пакет11,22.10.2026

#180 static art реализован,12 JSON/0PNG. Чертежи до обеих ревизий/старые пробы сохранены. Связная taper23-unit lash, стабильный GUI,наружная левая плоскость.40 native matrices/20 pivots/100 source cases,head idle0. Java21 clean build/resources/JAR/canonical+3 historical+Poloz weapon-only PASS,7730 checks. JAR SHA98858d897f076c29484571832183828b80354410f8a1d4174b674651b6443f3a,23704385 bytes,version53,NOT_INSTALLED. #190 ещё не реализована. #87 IN_PROGRESS; владелец22.10 явно согласовал пакетB, далее применить отдельно. C/D/190 awaiting answers; не считать истечение времени согласием.

### Пакет12,22.10.2026

Owner explicitly approvedB; реализован speed.8 perunite_mace и storm magic6/CD60-54,прежниеmultipliers/targeting/FX. Только4 production files и4 tooltip values; exact source/68 native scopes/native helpers PASS. Java21 clean build/generators/resources/JAR7739 PASS. JAR SHA3ac946aac1d13312902ced82cdc0a0dee562e51a6604197cc96de1b74852385b,23704623 bytes,version53,NOT_INSTALLED. Все158/159/87 IN_PROGRESS, runtime pending. C/D/190 unanswered. Следующий178: GUI кривого посоха Лихо обрезает верх/скрывает глаз; геометрию и хват сохранить, центрировать GUI и показать существующий eye face.

### Пакет13,22.10.2026

#178 GUI crop/eye-facing FIX,форма5cubes/UV/PNG/все nonGUI transforms KEEP. Персональный чертёж до production,20 native source cases,clipTrue→False,eye0→1. Java21 clean build/native/resource/JAR/generators7779 PASS. SHA40398a2db4de52fe7d499a606fb9a14611ab4ec082e557813d4956a9423fa20a,23704670 bytes,version53,NOT_INSTALLED. Далее11 KEEP unique IDs175176177179183184185186187188189: нужны персональные source assessments,actual states/SHA/JAR/body/FP evidence,не новые art edits ради галочки.180/181/182/178 FIX already covered. Full87 continues; C/D/190 pending.

### Пакет14 — #115, 22.10.2026 (ветка feat/weapons87-p14-crossbow-grips)
12 арбалетных item models: только TP left/right translations [0,-2.475,-1.375] вместо [0,2,0],3D/FP/GUI/predicates/stats unchanged. До/после и проверочный источник в PACKAGE-14.md, native source boards в blueprints/crossbow-grips; Python 48 pivot cases; Java21 CI pending. Issue OPEN и owner runtime NOT_RUN. Не выдавать независимые #175/#176 или всю #115 за принятую реализацию.

### Пакеты15–19, 22.10.2026: пять source KEEP / индивидуальная оценка
Пакет15 #175 three_breath_crossbow (65–68 cuboids, крупный поздний артефакт); пакет16 #176 serpent_crossbow (8–9 cuboids, компактный оригинал); пакет17 #177 ornate karachun_scythe (60 cuboids); пакет18 #179 ледяной morozko_staff (66 cuboids); пакет19 #183 nightingale_dagger (12 cuboids). Пять личных существующих PNG-листов UV/source/profile/3⁄4/GUI и точные JSON SHA; модели не перерисованы. Арт KEEP по source, не FINAL и не owner acceptance. P14 #115 TP fix независим от арта #175/#176; CI pending. Геймплей, rig и способности untouched. Следующий #184 trophy_kalin_sabre; все Issues OPEN.

### P14–19 принятие в Git, НЕ игровая приёмка — 22.10.2026
PR #194 объединён в main `9910e5ca17e5bbf5fa14dff55fa371640fe93398`, Java21 clean build [CI 39757888300](https://github.com/Deserag/slavic-myths/actions/runs/39757888300) PASS. `slavicmyths-1.9.7.56.jar` SHA256 `73d0d04b5ef5890c8508f626022b68fc567fa30ac84a90e3d1fb208bd7d2cffc`, неизменяемый archive before + Python suite:12 models,48 TP old/new pivots,12 texture refs PASS. Реальный игровой запуск/PolyMC НЕ выполнялись. P15–19 KEEP только в source; их 5 Issues OPEN. #87/#115 OPEN. Дальше #184.

### #87 пакеты20–25 — 22.10.2026 (branch pending CI)
Пакеты20–25: #184 Kalin 6→34 элементов, отдельный 16px atlas и изгиб сабли FIX; #185 Victor 4→14 элементов и наградная втулка FIX; #186 лук Финиста с3 pull-моделями KEEP; #187 thunder_axe24 элементов KEEP; #188 thunder_spear30 элементов KEEP; #189 pool_spear сохранил14 деталей/текстуры/руки, только центрированный GUI FIX. Все before/draft для изменённого подготовлены отдельным предварительным коммитом; проверка CI в ветке, игровой запуск/PolyMC NOT_RUN. C/D/#190 не тронуты; #191 не начинали.

### P20–25 source/test/merge receipt — 22.10.2026
Пакеты20–25 в main: #184 сабля Калина FIX (6→34 cuboids, отдельная пиксельная палитра); #185 копьё победителя FIX (4→14 cuboids, наградное острие); #186 лук Финиста KEEP (3 состояния), #187 громовой топор KEEP (24 элементы), #188 громовое копьё KEEP (30 элементов); #189 копьё омута GUI FIX (14 cuboids KEEP). Production изменения ограничены 3 модельными JSON,1 новым PNG и связанными generator/test файлами, игровые классы/статистика не менялись.
### Итоговая публикация в GitHub — 22.10.2026
PR [#196](https://github.com/Deserag/slavic-myths/pull/196) слит в `main`: `22613105859a23294b366d33520edd25471ce54b`. Проверенный source-head `7809bed7cd42894e8552c7d538a1952f813dc49b`. [CI 39761931287](https://github.com/Deserag/slavic-myths/actions/runs/39761931287): Java21 `clean build` PASS, source/draft/PNG и production-JAR проверены; 12 исходных JSON/PNG для #186–188 совпадают с PINNED pre-branch `main`, три pull состояния Finist и silhouette metrics PASS. Артефакт содержит фактические source before/after PNG и JAR `slavicmyths-1.9.7.56.jar`, SHA256 `0f5faaf8fc7d968a526a192a5503ca2c8b9ebbf8c289c5ef73038bb24cd16167` (установки не было).

**Известное наследованное несоответствие:** старый `tools/verify_weapons87.py --generators --jar` FAIL на `models/item/stone_flail_held.json` против давнего `blueprints/flails/applied-manifest.json`. Git blob в базовом main и нашей ветке одинаков `94b946c68f1528ff576e2866ef49534db0cf5ec8`. Отдельно архив `unique-source-review/source-manifest.json` имеет 6 устаревших SHA для трёх KEEP источников; вместо них тест проверяет **точный исходный Git commit**, не переписывая историю. Эти наследованные gates не считать PASS и не исправлять изменением чужих моделей без адресного анализа. **Minecraft/PolyMC/живой FPS/сеть: NOT_RUN; owner acceptance: OWNER_PENDING.** Все Issues OPEN.

### Пакет26–27 / #157/#192, #160 — 22.10.2026, ветка feat/weapons87-p26-27-audit-thunderaxe
Source-only живой аудит68 IDs плюс архивный drift (109 audit68 и 51 flail, 0 исчезнувших), отчёты в build/ и CI artifact; детально P26.md. Thunder Axe одиночный post-hit magic hurt() -> +1 final damage из LivingDamageEvent.Pre при серверном подтверждённом melee, chance .2 / CD60 и аудио/VFX сохраняются; P27.md. Java21 CI pending; все Issues OPEN/OWNER_PENDING; main без изменения до CI.

### Финальные receipts пакетов26–27

### Проверенный итог и merge в main — 22.10.2026
- PR [#198](https://github.com/Deserag/slavic-myths/pull/198) merged `4fad4fa2bd231aa07d2bb0cb5c4d68fb2e395686`, gated source HEAD `4f5a3b3c880d94261f12c0a0c246903848cd5425`.
- [CI 39764351561](https://github.com/Deserag/slavic-myths/actions/runs/39764351561) **PASS**, Java21 clean NeoForge build 1m29s, 68 weapon ids source PASS, 294 source-assets byte-verified inside JAR PASS, Thunder Axe source guards PASS.
- Production output `build/libs/slavicmyths-1.9.7.56.jar` SHA256 `406a1027e06f0d6bc30c382e471bb814b7f3624ddd5320fef96cf3b00802d7b9`; downloadable **only as CI artifact**, not installed in PolyMC, client/server NOT_RUN.
- History: `audit-68.json` **109** stale SHA entries, `blueprints/flails/applied-manifest.json` **51** stale SHA entries, no missing historical paths, old full #87 manifest verifier remains **FAIL**; reports record drift rather than changing resources. Numbers reflect source lineage, not a claim of 160 combat bugs.
- Issue #87/#157/#160/#192 **OPEN**, OWNER_PENDING. Approved A/B stats preserved; C/D/#190 blocked, #191 not started.


## OCT22 OWNER SCREENSHOT / LIVE GAME BUGS — LATEST PRIORITY
User actually installed and ran earlier main build and reported **FAILED** game QA: crossbow vertical giant lever in 3P, Thunder Axe no visible lightning, Kalin sabre three slime kills no sweep. See owner-qa-20261022/PLAN.md (source baseline) and PACKAGE-28/29/30.
P28 third-person crossbow X rotation -90→0 for both hands and all 12 states; actual stock lies on Z, pivot remains physical grip, no art/FP/ammo changes. P29 visual-only LightningBolt on ThunderAxe 20%/+1HP/60tick server proc, no fire/weather/extra damage. P30 Kalin accepted melee hits1/3,2/3, third auto wave (95deg/4.5/3 additional hostile mobs/60% damage), visible 3D particles/actionbar; old hidden extra-fourth-strike removed. Latest branch feat/weapons87-owner-qa-crossbow-kalin-thunder. Java21 branch workflow runs, then cherry-pick/merge clean into main when source/JAR gates PASS. Owner **AFTER** Minecraft validation NOT_RUN; keep #115/#163/#160/#184/#87 OPEN. Subsequent #161 VFX and #162/#165 motion independent; #191 last, no C/D/#190 without approval.

## Финальная публикация и CI 22.10.2026
- Production [PR #200](https://github.com/Deserag/slavic-myths/pull/200) merged into main `ac23497c1f745817c88faa48e4fdeab961722152` (source QA head `1f418cedd782eb290775db597b81a7b2c3353ea6`).
- [Java21 CI 39770615398](https://github.com/Deserag/slavic-myths/actions/runs/39770615398) **PASS**: clean build, all12 crossbow real stages /48 hand-pivot tests, 12 referenced textures, scoped gameplay-source guards, source UV A/B actual JSON PNG; production JAR bytes match all 12 model files.
- JAR: `build/libs/slavicmyths-1.9.7.56.jar`, SHA256 `21b704ed7fddcbddac4e1bf161ae9e64b26193afc601ad48fa5224b0da87074b`. Minecraft/PolyMC CLIENT/SERVER/MP retest **NOT_RUN**; user supplied screenshot is **prior version FAIL**, not after proof. All Issues remain OPEN for owner acceptance.

## 22.10.2026 — пакет №31, #163 FIRST-PERSON CROSSBOW RECOIL, runtime not run
Новые source изменения находятся в feat/weapons87-p31-crossbow-firstperson-recoil, исходный main 6e28808da46078397bb2327b2c99b158335e1e4f. В client WeaponHands отслеживаются две независимые руки и существующий ItemStack, фиксируется реальный переход CHARGED→not charged без active item use, начинается 7-тиковый плавный kick (sin πt/7 × (1-.25t/7)), направленный назад и вверх вместе с визуализируемой рукой и оружием. Сброс при смене Stack, player или world; только client, ноль новых пакетов и server/abilities/charge/weapon stats изменений. Source guards + native existing 12 crossbow asset checks, owner screenshot fix preserved. Source four-phase storyboard from real JSON+UV, but НЕ Minecraft. GitHub Actions Java21 clean build/JAR checks в работе. Не считать весь #163 готовым: third-person reload/recoil, Steve/Alex/MP/charge variants ручная приёмка всё ещё NOT_RUN. Читай PACKAGE-31.md и tools/verify_weapons87_crossbow_recoil.py. Следом #121 generator reversion audit, #161 unique VFX and #162/#165 animations. Баланс C/D/#190 не трогать, #191 last.

## 22.10.2026 — P31/32 final verified source and CI receipts
- **P31 #163:** crossbow FP visual recoil `WeaponHands` on same ItemStack charged→empty, 7-tick smooth client-only wrist+gun kick, per-hand state reset on switch/world, not ammo/server. [PR #202](https://github.com/Deserag/slavic-myths/pull/202) merged `4247c95ff8f8b82cb875161edf960686caaaa2af`. [Java21 CI 39775007057](https://github.com/Deserag/slavic-myths/actions/runs/39775007057) **PASS**: native 12 crossbow model states /48 handed pivot cases, 294 packaged resources, actual four-phase item-source/UV storyboards. JAR `slavicmyths-1.9.7.56.jar` SHA256 `d920d52874db3d9d1d811756ff10177f0c9b94aec3db7c9edcf73eb34f86790f`.
- **P32 #121:** isolated canonical source generator run **twice** in disposable temp sandbox; no actual project asset writes, 68 active weapon-ID models, 16 protected resources (#115/#163/#184/#185/#189). Byte differences **30** (12 item JSON +18 flail PNG), but JSON **semantics** and PNG **pixels** drift **0**, double-run **idempotence** drift **0**. Legacy `audit-68.json` SHA109 and old flail applied-manifest SHA51 still archived baseline differences (not silently overwritten). [PR #203](https://github.com/Deserag/slavic-myths/pull/203) merged `f1b9756454becb2cb424d6da272a68a9ab34ff6d`; [Java21 CI 39776454779](https://github.com/Deserag/slavic-myths/actions/runs/39776454779) **PASS** (68 ID,294 JAR resources, generator source gate), same JAR SHA `d920d52874db3d9d1d811756ff10177f0c9b94aec3db7c9edcf73eb34f86790f`.
- #121 remains **partial** (only canonical `weapons87_flails --apply` chain replayed, other historical standalone entrypoints and gameplay still pending); #163 remains partial (TP player animation/MP owner test pending).
- **Issues count:** 43 child issues #115–#121 + #157–#192, 42 OPEN /1 CLOSED (#184), plus parent #87 OPEN as of audit. No Issues closed automatically. Owner gameplay/PolyMC **NOT_RUN** in assistant session. Unapproved C/D/#190 untouched, #191 last.
