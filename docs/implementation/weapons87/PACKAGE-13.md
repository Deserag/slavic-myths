# Пакет13 — #178, посох Лихо: KEEP формы, исправление GUI

22.10.2026. #178 OPEN, owner runtime pending; #87 продолжается.

Причина: посох высотой29 model units отображался при scale.65 относительно8,8,8 без центрирования, поэтому верхняя часть выходила за canvas16px. Ракурс скрывал уже существующую переднюю грань глаза. Иконка выглядела диагональной палкой без характерного завершения.

Сохранены все5 authored cuboids, UV, shared weapon_scythe_surface.png, искривлённое древко/верх и отдельная грань глаза. Новая GUI: center8.5,11.5,8; scale.5; yaw180/roll30; front light. Bounds5x29x3.5, authored grip8,11,8. Все non-GUI display transforms, включая обе физические руки,ground/fixed/head, прежние. Не менялись модели/риг/боевые механики Лиха #93 или способности предмета.

Варианты до production: старый cropped/back-face, центрированный с обратной стороной, выбранный центрированный eye-face. Индивидуальный чертёж actual JSON/UV/PNG с front/side/3-4,GUI1x,source palette и native Steve/Alex scale — blueprints/likho-staff. Полный before archive — before-likho-gui.zip. Это source-проекции, не Minecraft screenshots.

Изменён единственный production model JSON. Геометрия KEEP: мрачный асимметричный посох уже отличается от carved fork,storm conducting frame иMorozko ice crown; не требуется перерисовывать принятую форму ради исправления камеры GUI. `weapons87_likho_gui.py` подключён последним canonical overlay,exact draft matches.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Индивидуальная диагностика/варианты/source-чертёж до production | blueprints/likho-staff, before-likho-gui.zip | PASS |
| Вся геометрия/UV/shared PNG/способности KEEP | exact elements/textures/non-GUI equality; preserved-files.json | PASS |
| В GUI полностью виден верх и существующий глаз | GUI geometry clipping True→False,eye-color pixels0→1;actual16px source projections | PASS |
| FP/TP обе руки/Steve-Alex/idle-walk/player scale |20 native source cases;unchanged hand displays;idle head triangle candidates0 | PASS |
| Java21 clean build/applicable native fixtures | package13-build.log;test NO-SOURCE | PASS |
| Resources/generator replay/actual production JAR | package13-gates.log/receipt13 | PASS |
| Реальные свет/GUI/руки/движение/каст/frame/drop/MP | запусков0,владелец | NOT_RUN / OWNER_PENDING |
| PolyMC | промежуточная сборка не установлена | NOT_RUN / POLYMC_NOT_INSTALLED |

У source-review первого прохода все geometry checks прошли, но печать unicode arrow в cp1251 console завершилась исключением; probe-log сохранён. Печать переведена на ASCII, весь source-review повторён. Это ошибка отчётного инструмента, не игровой код или «подгонка» данных.

Точный JAR/version/SHA/bytes и полный uncommitted source manifest — package13-receipt.json; source evidence frozen. Без commit/push/Project Status writes/owner-accepted/закрытия. Баланс B из12 сохраняется, C/D/190 без ответа не применены.

Owner: посмотреть посох в16px GUI, сравнить верх/глаз,обе физические руки/Steve-Alex/ходьбу/рамку/drop и прежние способности. Сравнить held с предыдущей сборкой: геометрия и хват должны остаться прежними. Дальнейшие каст-анимации165/эффекты160–161 выполняются отдельно.
