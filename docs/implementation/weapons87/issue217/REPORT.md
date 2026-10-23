> **Последующее исправление #223/#224 по прямому поручению:** [актуальный отчёт](finist223/REPORT.md).91/91 matrix и19/19 live hits, метка над целью, отключение наведения за стеной. Ниже сохранён предыдущий диагностический отчёт с прежними промахами. Новый artifact/source manifest отдельно в finist223/, прежние receipts не переписаны.

# #217 — фактическая проверка 23.10.2026

Работа выполняется локально поверх `1192e229c4aa9166b53661f71f5ca15299f3537e` и сохранённых незакоммиченных P53/голов/N98–T99. Автоматических commit/push/merge, изменений задач Ада и новой установки PolyMC нет. Разрешение на изолированные клиент/сервер/GameTests и громкость20% получено непосредственно от владельца в этом чате. Пользовательские saves и options не использовались; `work/issue217/client-first-run/saves/VisualQA` — отдельная копия служебного QA-мира. Ввод рабочего стола не требуется.

## Исправления и доказательства

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| #218: мечи, копья, плеть внутри одного слота | Исправлен возврат выбранной GUI-модели из обоих BakedModel wrappers; `inventory-frame-bounds.json`:88 видимых моделей, zero spill. Громовой топор дополнительно уменьшен только в GUI после13/1 выступающих пикселей | PASS |
| #220: непрозрачные щит и рог, исходные иконки сохранены | Оригинальные cuboid shapes, отдельные surface atlas; actual FP/TP/main/off/use кадры `client-lash-use-after` и последующие. Native frontal shield block предотвращает урон и расходует прочность; horn hold/cancel/threshold20/CD300/wrong-arena rejection в серверном логе | PASS |
| #221: legacy heat метательного ножа без выдачи legacy proc современному предмету | Production RuneEffects исправлен по реальному FAIL;512 native collisions и отрицательный modern heat case в `issue217-runtime-finist-shots.log` | PASS |
| #222: постоянная толщина тетивы и различимый взведённый V |12 фактических состояний,32 коротких сегмента вместо широкой плиты, толщина0,3. Loaded arrow/firework сохраняют full-draw V. `crossbow-string-source.json`,56 кадров BEFORE и56 AFTER,8 реальных циклов заряжания в каждом прогоне | PASS |
| #219: плеть FP/TP, обе руки, контакт вперёд и возвращение | Последний single-pass прогон `client-lash-single-pass`:144 реальных кадра; `whip-side-phase2.png` показывает вынос вперёд у Steve/Alex в обеих руках. Неизменный grip,81 bank, максимум128 cached corrected poses без entity references. Прежние двухпроходные `client-native-lash-*` — отвергнутая итерация | PASS в указанном render/runtime scope; визуальное принятие владельцем ожидается |
| Серверная дальность копий4,25 и плети6; внутри/вне границы и попадание | Реальная экипировка/атрибуты ServerPlayer, AABB цели, canInteractWithEntity и actual whip attack; `ISSUE217_REACH_RUNTIME_PASS` | PASS |
| Регрессия голов |6 native BlockItem.useOn,6 block entities, удаление верхней половины и ровно один drop; actual world captures. Повторный native ModelPart/UV/bounds gate `issue217-heads-offline.log` | PASS в указанном scope, не повторная приёмка всех мобов/аудио |
| Посохи/рецепты/метание/арбалетные эффекты | Actual Storm/Likho/Veles use, guardian owner/cost/cooldown;37 bench и61 wire recipes;14 thrown materials, native knife/sulitsa collision/pickup;6 игровых fan/burst MOR/FIRE/STORM случаев в первом клиентском прогоне | PASS в перечисленных сценариях |
| Java21 production JAR после последней правки плети | `logs/issue217-clean-build-delivery-check.log`: clean build52s; `jar-gates.json`:18/18 PASS;7631 ресурсов,1809 Java21 classes, тестовых classes в production0 | PASS |
| Реальный отдельный multiplayer client/server, настоящий перезаход в мир | Не выполнялись. Steve/Alex RemotePlayer в renderer являются только проверкой скинов/рук; NBT storage same process не называется перезаходом | NOT_RUN |
| Установка PolyMC | Установленный P53 JAR не перезаписывался; новый testing delivery в этом прогоне не выполнялся | NOT_RUN / NOT_UPDATED |

## #223 — Финист:19 реальных выпусков стрел

Первый полный прямой выстрел **реальным попаданием через FinistEffects** установил метку. Затем18 выпусков FinistBow через настоящий Curios quiver и production releaseUsing:8/18/30ticks ×0/10/45/90/135/180°. Никакой записи метки через NBT и создания готовых guided arrows вместо выпуска лука. Цель — неподвижный Zombie в8 блоках; в GameTest последовательно вызывается настоящий projectile.tick с настоящими collision/damage callbacks. Это не видео19 выстрелов человеком и не статистика вероятности попадания. `finist-19-shots.json` содержит каждую начальную скорость, hit/miss, damage, число ticks и сохранение guide.

| Натяжение | 0° | 10° | 45° | 90° | 135° | 180° |
|---|---|---|---|---|---|---|
|8ticks, слабое|попал11ticks|попал11ticks|промах|промах|промах|промах|
|18ticks, среднее|попал4ticks|попал4ticks|промах|попал83ticks|попал80ticks|промах|
|30ticks, полное|попал3ticks|попал3ticks|промах, guide потерян|промах, guide потерян|промах, guide потерян|промах, guide потерян|

Метка назначается всем18 выстрелам, расходуется ровно один заряд; скорость соответствует натяжению. **PASS теста означает, что эти сценарии выполнены и записаны, а не что все углы успешно доводятся до цели.** Полный лук при больших углах быстро уходит за48-блочную границу;6°/tick ограничивают радиус разворота. Turnrate/range/gravity и баланс Финиста в этом прогоне не изменены. #223 остаётся открытой до решения об ожидаемых пределах наведения.

## Ограничения сдачи

Родитель #217 остаётся «В работе», пока исполнительский scope не завершён. Нет `owner-accepted`, автоматического закрытия Issues и утверждения о полном #87/#191/#49–#60 PASS. Голосовые источники и финальная аудиоприёмка этим прогоном не заменяются. Исходные неудачные прогоны/отвергнутые анимации сохранены и перечисляются в `runtime-runs.json`; source/JAR guards, framebuffer и реальные механики имеют разные доказательства.

## Итоговый артефакт и запуски

Локальный production JAR `D:/slavic-myths/build/libs/slavicmyths-1.9.7.58.jar`, SHA256 `1bb4cdf7b2fd5c92956e481e538e07e8229cefa893acc95f9fe3a7b8a8756149`. В PolyMC остаётся прежний P53 SHA256 `5a4a58c61599978bf80ec517ee1642defb20dd60b6f1b1d31e01c7a5af40463e`, один mod JAR; новой установки нет. Source1192e229 + dirty snapshot: `source-manifest.json`, SHA256 `82348d139cd233d651882fdae9f846807b43a6686ee601287fca9b6437b5becd`.

Клиентских запусков 12, успешных 11; GameTest server запусков 8. Неудачные итерации сохранены. Последний сервер26/26 PASS, включая наблюдение19 стрел с явно записанными промахами. Последний клиент144 framebuffer кадра;88 GUI без spill. Старый обзор70 held предметов дал846 кадров, поздние кадры ночью: он не заменяет ручную приёмку всех68 видов. MP/настоящий перезаход не выполнялись. Пять безопасных maintained weapon generators дважды каждый: semantic/pixel/idempotence drift0. Audio bootstrap не запускался.

Project9 запись подтверждена после обновления: #218–#222 **Тестирование**; #223 **Доработка**; родитель #217 **В работе**. Все6 детей связаны реальным GitHub sub-issue relation, не только упоминанием. Issues OPEN, owner-accepted отсутствует.
