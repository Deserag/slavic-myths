# Пакет4 — #164: динамика кистеней

Технический scope реализован, игровая приёмка OWNER_PENDING. Общая #87 IN_PROGRESS. Ни одна Issue не закрывается; реальное поле Status Project не изменено.

Для8 IDs добавлены FlailClient/FlailMotion: настоящий владелец из ItemOverrides.resolve передаётся в immutable per-render BakedModel. Одноразовый render-thread binding удаляется до отрисовки деталей и в finally. Remote entity не подменяется Minecraft.player. GUI/ground/fixed и owner-null оставляют статические модели. Переиспользуются прежние рукояти/грузы/атлас,17 новых standalone моделей деталей;8 root JSON переключены на builtin/entity с прежними displays и front GUI lighting. Никаких PNG изменений этого пакета.

Кривая7 закрытых чередующихся звеньев вычисляется только при отрисовке: idle, walk inertia, charge, contact, trailing и return. Шаг2.4 model units, груз закреплён на конце; индивидуальные формы7 материалов и Атамана сохранены. Заряд только у FlailItem,18 ticks. У Атамана остаётся MaceItem и LMB, новый RMB/заряд не добавляется. Серверный урон остаётся при отпускании/обычном ударе, визуальный отклик не вводит дополнительный damage timer. Истинная сетевая синхронизация проверяется владельцем в игре.

Vanilla SPEAR charge больше не переворачивает цепь: клиентский one-arm ITEM pose и собственный FP wrist. Первая source-проба выявила груз за нижней границей экрана; сохранена blueprints/flail-motion/fp-trial-clipped.png. Новый FP wrist(+/-0.42,-0.20,-1.15) сохраняет груз в кадре для подготовленных фаз. Серверный FlailItem/RareCombat byte-identical до/после. Нет polling мира, серверной физики, пакетов, новых entities/registry IDs.

Изменённые production: client/FlailClient.java, client/FlailMotion.java,8 root item JSON; добавлены16 handle/ball JSON и flail_link87.json. Генераторы: weapons87_flail_motion.py и канонический weapons87_flails.py; исторические overlays вызывают этот же канонический слой. Проверки: Weapons87FlailMotion.java, weapons87_flail_review.py, verify_weapons87.py. Базовые изменения прочности — только ранее утверждённый A.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Все8 кистеней, существующие реальные части | blueprints/flail-motion/applied-manifest.json, actual split JSON/UV | PASS |
| Соединения, единичные направления, отсутствие перевёрнутых звеньев и точный возврат | actual production FlailMotion в15120 offline численных позах | PASS |
| Груз ниже хвата в idle/charge | та же проверка, оба визуальных типа | PASS |
| Первая/третья перспектива, обе руки |24 source A/B sheets, actual native wrist/body,248 FP records;124/124 AFTER ball centers внутри fixed FOV | PASS |
| Реальные source кадры до/после без концепт-подмены | before-flail-animation.zip, package4-motion-source-evidence.zip; fixed body/wrist указан явно | PASS |
| Статические GUI/drop сохранены | каждый _icon/_held byte-identical до/после, PNG SHA исходного static manifest, front GUI flag | PASS |
| Урон/CD/заряд/Ataman механика сохранены | FlailItem/RareCombat byte equality;68 native snapshots | PASS |
| Java21 clean build | package4-build.log: BUILD SUCCESSFUL; test NO-SOURCE, не JUnit PASS | PASS |
| Воспроизводимость и сохранность чужих ресурсов | package4-gates.log, generator-checks.json, isolated5-overlay replay | PASS |
| Production JAR совпадает с source assets/classes, offline исключены | package4-receipt.json | PASS |
| Игровые GUI/FP/TP, charge/release, MP owner binding, движение, плавность, foil/reload | Minecraft/PolyMC/GameTest/server запусков0 | NOT_RUN / OWNER_PENDING |
| Установка промежуточного JAR | не поручена в оружейной программе | NOT_RUN / POLYMC_NOT_INSTALLED |

Точный локальный промежуточный JAR: `D:\slavic-myths\build\libs\slavicmyths-1.9.7.53.jar`, version1.9.7.53, SHA256 `6cdf0af7f44816d9885aa4b958ef5d4d81a1655d40da3758e50c281eeb23a7c3`,23685994 bytes. Source base HEAD `32d1733457e7e90360ce16c23860304ece664c64` + uncommitted manifest SHA `7884063d1e989eb9e754416a57f203e7f3423d40e182c25e4e7c77156d6c5bdb`. Код автоматически не коммитился/не публиковался. Установленная в PolyMC сборка не менялась. Это не финальный результат всей цепочки #87.

Owner-сценарии:8 IDs в GUI/ground/item frame; Steve/Alex/right/left, main/offhand; idle/walk/sprint;7 обычных charge<18 и>=18, отпускание по мобу и воздуху, LMB; Ataman только прежний LMB/комбо; удалённый второй игрок с иным оружием/состоянием; resource reload, glint. Проверить читаемость звеньев/шара, реальный момент попадания и возврат. Запуски выполняет владелец.

Следом #171 (чертежи пяти FIX подготовлены, production ещё не применён) и итоговая согласованность #168/#173. #168 source audit20 KEEP сохранён отдельно COMPONENTS-168.md; его окончательная сверка с готовыми семействами ещё NOT_RUN, поэтому он IN_PROGRESS.
