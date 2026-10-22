# Пакет6 — #173: корректный полёт ножей/сулиц

Технический scope #173 реализован для14 IDs. Art14 KEEP с отдельными actual source sheets, flying orientation FIX. Общая #87 IN_PROGRESS. Issue OPEN, владелец ещё не принимал, реальное поле Project не менялось.

Дефект: WeaponProjectileRenderer использовал ItemDisplayContext.FIXED. Все14 source-моделей наследуют от minecraft:item/generated поворот рамкиY180. Старый flight Z(pitch-45) рассчитан на неотражённый диагональный клинок; итоговая ось становилась перпендикулярной скорости. В полёте выбран NONE, рамки/GUI/руки не меняются. Математика yaw/pitch/scale вынесена в production flightPose и осталась прежней. Knife scale0.65, sulitsa1.25. Серверные WeaponProjectile/ThrowingWeaponItem и28 model/PNG файлов побайтно неизменны.

Два архетипа сохранены: короткое лезвие с рукоятью и длинное деревянное древко с малым остриём. Material variants внутри каждого класса — разрешённое родство формы. Генерируемый vanilla item mesh имеет толщину1 model unit, это объёмная экструзия actual sprite, не camera-facing billboard.14 source-чертежей с1x/front/profile/3-4 и proposed flight были созданы до правки renderer. Наконечники #99/#100 не редактировались.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Все14 IDs и два разных архетипа | blueprints/thrown, thrown-native-inputs.json, ручной source review | PASS |
| Реальная flying модель/UV до/после |14 source A/B sheets,84 отображённых frame cases | PASS |
| Наконечник вдоль скорости, любой проверенный yaw/pitch |420 actual native ItemTransform+production flightPose cases; BEFORE dot~0, AFTER dot~1 | PASS |
| GUI/held/item-frame/PNG и server damage/physics/save/pickup/loyalty сохранены | thrown-after/preserved-files.json,30 неизменённых файлов | PASS |
| Обе руки/held остаются прежними |14 model bytes неизменны; native #115 all68 body/FP source evidence | PASS |
| Java21 clean build + applicable native fixtures | package6-build.log, test NO-SOURCE указан явно | PASS |
| JAR/resource/generator preservation | package6-gates.log, package6-receipt.json | PASS |
| Реальные броски/попадание в блок/моба/возврат/MP и visual contact | game/server/GameTest launches0 | NOT_RUN / OWNER_PENDING |
| Установка в PolyMC | не поручена для незавершённой цепочки | NOT_RUN / POLYMC_NOT_INSTALLED |

Production изменён только client/WeaponProjectileRenderer.java: flying context + извлечение той же stateless математики. Тест/доказательства: tools/weapons87_thrown.py (guard before source), Weapons87Thrown.java, weapons87_thrown_review.py. Offline fixture не создаёт projectile/entity/world/client.

Локальный промежуточный JAR `D:\slavic-myths\build\libs\slavicmyths-1.9.7.53.jar`,version1.9.7.53,SHA256 `78c4213816a1b8dcbc954a19c9ce0430750456f7c84533b16700ac57ec841237`,23691976 bytes. Source base `32d1733457e7e90360ce16c23860304ece664c64` + uncommitted manifest SHA `0f3e1ae6b164e2c7f4859917b7756385d0fc0acdbe2c4fefddccb2d6d49397fa`. Не установлен, не commit/push, не финальная сборка87.

Owner: обе материальные линейки, правый/левый основной хват, бросить горизонтально/вверх/вниз; сравнить с GUI/рамкой; проверить остриё при полёте и застревании сулицы, нож по мобу/блоку, прежний pickup/return, второй игрок. Реальное попадание и задержки сети не объявляются проверенными source-матрицей.

Следом #170 silver_mace FIX: source review подтвердил розовато-коричневый light_gray_terracotta вместо читаемого серебра и две отделённые тёмные полосы. Остальные17 предметов семейной проверки #169/#170/#172 сохраняются KEEP с per-ID объяснениями. Отдельные timing/анимации #162/#163 не подменяются этим art QA.
