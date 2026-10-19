# #56 — Тугарин: опоры и инерция, TEST_PREVIEW 1.9.7.18

IN_PROGRESS / OWNER_QA_PENDING. По новому указанию владельца главный приоритет —
визуал #56, затем #46, #54/#55. Массовые аудиоаудиты #61–#66 остановлены,
готовые звуки сохранены. Ни одна Issue не закрывается; Minecraft не запускается.

Изменён только TugarinPoses (клиент). Реальные ModelPart, геометрия,1024×1024 PNG/UV,
две формы, двойные клинки и их крепление сохранены. Риг уже имеет pelvis/body,
arm/fore/wrist и leg/knee/ankle: новая модель/текстуры не создавались.
TugarinCombat/WorldBoss/BossRules/server yaw/target/AI/NBT/offhand escrow не меняются.
Registry ID slavicmyths:tugarin_zmey и физические3.5×4.85 сохранены;
renderer WorldBossRenderer, генератор boss_reimplementation_resources.py сохранены.
SPIN52, pulses16/28/40, recovery24; combo10/22/34; PUNCH10 и прочие серверные
вызовы остаются исходными. Никакого клиентского урона.

SPIN: непрерывная скорость поворота таза через три контакта и торможение к52;
асимметрия плеч/локтей/кистей, контрскрутка груди/головы, низкий центр массы.
В hit16/28/40 ведущая рука R/L/R выводит клинок из лицевой защиты наружу;
вторая рука остаётся согнутой. Это проверяется и на нативной геометрии.
Двухзвенная кинематика ставит ботинки в координатах пола: опорный ботинок не
скользит за тазом; другая нога делает шаг, затем опора меняется. Подошва сохраняет
горизонтальную ориентацию. Recovery: два последовательных шага торможения,
возврат высоты/рук/корпуса, без смены позы на границе52→recovery0.
Combo/PUNCH/SWEEP получили перенос массы и отдельные шаги к контакту; charge
имеет опорный пробег со скоростью стоп19.2 model units/1.5ticks, соответствующей
прежним .8block/tick; windup10 и два тормозных шага за24 recovery сохранены.
Transition собирает обе руки и расправляет крылья с небольшим упругим возвратом;
landing начинает с наклона/раскрытия конца пикирования вместо смены корпуса;
крылья после transformation60 сходятся за действительные12 ticks recovery.
Огненная пасть по-прежнему проверяется относительно существующего server origin.

До — фактический main59a279a4 / установленная1.9.7.17, не древний bcec6355.
Нативный dump снят ДО замены Java; прежний baseline не перезаписан.
После — тот же настоящий WorldBossModel.sample, ModelPart/UV/PoseStack;
84 одинаковых тайминга на warrior/serpent/likho/mor_likho, включая плотный playback
SPIN/recovery. Ракурс, масштаб, PNG и age одинаковы. GIF100ms=2 игровых тика.
Все изображения и GIF — SOURCE PREVIEW, НЕ скриншоты/видеозапись Minecraft.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Нативное до/после: требуемые тики SPIN/recovery, клинки и ноги | before/after-19718-native.json.gz; spin-*-19718.png; motion-19718.gif | NOT_RUN |
| Combo/charge/transition/wings и fire origin | other-actions-19718.png; breath-mouth-source-19718.png; native log | NOT_RUN |
| Grounded stance/flat sole/no slide/grip; Лихо без изменений | verifyTugarin56; gates-19718.json | NOT_RUN |
| Серверные тайминги/AI/NBT, исходные art/audio | scoped source gate | NOT_RUN |
| Java21 clean build, native fixture и production JAR | TEST_PREVIEW_BUILD.json | NOT_RUN |
| Тестовый PolyMC с backup/SHA/one active JAR | TEST_PREVIEW_1.9.7.18.json + VERIFY.json | NOT_RUN |
| Пластика/естественность/игровая анимация и звук | Только ручная приёмка владельца | NOT_RUN |

Проверить владельцу: три удара вращения, опорные шаги, возврат в стойку;
combo и рывок; переход во вторую фазу, пикирование/посадку и огонь.
Принятые тембры/записанные spin1/2/3 сохранены: нового аудиоаудита нет.
Численные проверки не заменяют восприятие пластики и веса в игре.
