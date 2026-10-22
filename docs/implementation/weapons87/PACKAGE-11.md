# Пакет11 — #180, статическая плеть Полоза

22.10.2026. #180 OPEN, owner runtime ожидается. #87 IN_PROGRESS; плавная динамика190 — отдельный ещё не реализованный пакет.

Проверены все5 actual override моделей, не только root PNG. Старые модели: крупные золотые пластины, промежуточные кубики с разрывами, несужающийся тяжёлый шипастый конец. В GUI при null-owner был другой силуэт — шипастый жёсткий инструмент. Для живого владельца GUI зависел от attack/cooldown predicate и мог подменяться полноценной pose-моделью.

Новый статический art: короткая деревянная рукоять9x1.6, тёмные обмотки,20 непрерывных участков по1.15 model unit, радиус.36→.151, закрывающие стыки кубики,3 небольших золотых крепления и лёгкий конец без кистенного шара. Свободный конец idle смотрит вниз/от тела. Материалы — существующий atlas плюс сохранённая золотая/тёмная палитра Полоза; оба старых PNG, boss art/rig и серверная логика не изменены.

12 JSON: root,5 context wrappers,5 held geometries,1 separate GUI pixel profile. Все wrappers выбирают одну стабильную GUI и одну idle ground/fixed независимо от текущей pose. GUI имеет минимальную толщину.5 и пиксельный силуэт на16x16 из существующих atlas cells; PNG не генерировался. Тонкий held-шнур в GUI распадался на точки — этот probe сохранён, заменён отдельным читаемым профилем до production.

Индивидуальные source-чертежи до production: `blueprints/poloz-whip/poloz_whip.png`, player-scale PNG, plan/drafts/manifests. Первая реализация выявила на source-ракурсах inward-изгиб в левой руке. Её чертежи/drafts заморожены в revision1-static-draft.zip, actual A/B — package11-revision1-source.zip, логи build/gates сохранены. До применения revision2 подготовлен новый player-scale лист: левая плоскость развернута наружу, FP targetY3→4. Текущий authored grip8,0,8 откалиброван в обеих физических руках; прикладной размер не подменяется auto-fit картинкой. Предыдущие стадии не выдаются за окончательный результат.

`PolozClient` и `PolozWhip` побайтно сохранены. Пять старых predicate thresholds и ссылки overrides сохранены. Удар/эффект/дальность/прочность/18-секундный cooldown не менялись. Эти5 статичных форм ещё не являются плавной анимацией190.

Генератор `weapons87_whip.py` — последний canonical overlay. В исторический `poloz_full_assets.py` добавлен ранний `--weapon-only`: воспроизводит только12 JSON и выходит ДО full-generator audio и импортных summon/armor overlays. Проверен именно этот режим в изолированном sandbox с контролем всех nonowned resources. Полный старый генератор не запускался: содержит синтетическое аудио, которое запрещено регенерировать AGENTS. Никакие звуки/руны/броня этим режимом не меняются.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Индивидуальный source-план до обеих production ревизий | blueprint/player-scale/drafts; revision1 archive и original before-whip180.zip | PASS |
| Отдельный гибкий силуэт/толщина/сужение/крепление |5 actual source forms,20 joined sections,arclength23,monotone positive radii | PASS |
| GUI1x читается и не меняется при attack/cooldown | отдельный native pixel profile; все5 effective GUI models равны | PASS |
| Правильный grip обеих рук |40 native FP/TP transforms,20 calibrated pivots,revision2 outward left plane | PASS |
| FP/TP, Steve/Alex, fixed idle/walk source views |5 A/B sheets,100 cases; default idle head triangle candidates0 | PASS |
| Art/server/predicate/boss/IDs сохранены вне12 declared JSON | preserved-files.json; old overrides equality;9563-file baseline whitelist | PASS |
| Java21 clean build + native fixtures | package11-build.log; test NO-SOURCE | PASS |
| Resource/JAR/canonical+historical generator replay | package11-gates.log, scoped Poloz --weapon-only; nonowned changes0 | PASS |
| Полный legacy Poloz art/audio generator | запрещённое аудио; не нужен для воспроизведения weapon-only scope | NOT_RUN / OUT_OF_SCOPE |
| Реальные свет/GUI/FP/TP/ходьба/атака/frame/drop/MP | запусков0; owner-only | NOT_RUN / OWNER_PENDING |
| Плавные bend/lag/contact/trailing и FPS190 | отдельно, не входят в статическую180 | NOT_RUN / SEPARATE_ISSUE190 |
| PolyMC | промежуточный пакет не установлен | NOT_RUN / POLYMC_NOT_INSTALLED |

Точные промежуточные JAR/version/SHA256/bytes и uncommitted source manifest — package11-receipt.json; source evidence заморожены. Без commit/push/смены Project Status/закрытия Issue. Общая программа не завершена.

Owner: проверить16px GUI, обе руки и оба main arms, Steve/Alex, петлю наружу в покое/ходьбе, стыки и сужение, предмет на земле/в рамке; существующие ЛКМ и ПКМ работают по старым правилам. Подвижность ещё проверяется после190, статическая180 не выдаётся за её реализацию.
