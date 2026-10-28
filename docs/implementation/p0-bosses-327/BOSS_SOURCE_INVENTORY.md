# Фактические боссы и художественные блокеры #327

Исходники проверены на hell@c884e63c + production delta этой задачи. Это source audit, не художественная приёмка.

| Босс / Entity ID | Реальная модель / renderer | Реальная texture | ORIGINAL path/SHA | Отклонение и статус |
|---|---|---|---|---|
| Сторож / slavicmyths:grove_guardian | navya/client/NavyaMesh.java, NavyaModel.java, NavyaRenderer.java | textures/entity/grove_guardian.png + _glow | Финальный тёмный реколор из #256; raw path и SHA не получены | Владелец отверг деревянный тотем. Сопоставление silhouette/color/UV BLOCKED_REFERENCE; модель не изменялась |
| Жареница / slavicmyths:zharenitsa | blackcinder/client/CinderModel.java, CinderRenderer.java; cinder_rigs/zharenitsa.json | textures/entity/zharenitsa.png + _glow | zharenitsa.png, ad805df5f2c2c3e2ee3e2ab0ff86c5103464a54c662d3c2f2e820ed96f4d874e; physical path отсутствует | Владелец отверг тонкие прямые конечности. ART BLOCKED_REFERENCE; модель не изменялась |
| Повелитель / slavicmyths:magma_lord | burningislands/client/IslandsModel.java, IslandsClient.Renderer; burning_islands_rigs/magma_lord.json | textures/entity/burning_islands/magma_lord.png + _glow | boss SHA 354c2c318c01ee67b21b60adc653a0a4747acc81786a433cb28e1c3fc26881a6; physical path отсутствует | Владелец отверг кубического гиганта. ART BLOCKED_REFERENCE; модель не изменялась |
| Постень | EntityType/Model/Renderer отсутствуют; PostenEncounters/Reconciliation — backend | Нет production boss texture | 34e30e8cde56dffa7ab8e2ff0bf917da3ebdd808675e02da5cfeb123502d65d0; physical path отсутствует | Entity/бои/модель/арена не реализованы; BLOCKED_REFERENCE и BLOCKED_DEPENDENCY |

Пути Java относительно src/main/java/org/slavicmyths, текстуры/rig относительно src/main/resources/assets/slavicmyths. SHA актуальных production-файлов: inventory/production-sha256.json. Исходные текстуры не являются эталонными PNG из ZIP.

## Головы

- Сторож: нет GROVE_GUARDIAN Kind в TrophyHeads и нет действующей головы в loot. FAIL реализации H01/H02; ART BLOCKED_REFERENCE.
- Жареница: существующий `zharenitsa_head` сохранён в CinderBlocks.Head. Это обычный HorizontalDirectionalBlock с JSON-моделью; нет трофейного Tile/BER, анимации и корректной отдельной wall placement. FAIL H04/H05; нельзя переименовывать registry ID при последующей миграции.
- Постень: нет Kind/Block/Item/BER/дропа, настоящее кольцо масок не реализовано. FAIL H01/H02/H06; ART BLOCKED_REFERENCE.
- Повелитель: MAGMA_LORD в TrophyHeads и фактические head ModelParts/иконка уже существуют. Новых изменений модели/дропа этой задачей нет. Оригинал головы SHA 287ce3f15e28b1a646cf31de1e51fab85519bb03dc935d83bb7aea920395bc93 не найден; ART BLOCKED_REFERENCE, placement/drop/relog/MP NOT_RUN.
- Старые шесть голов: production код/ресурсы не изменены этим delta; игровая регрессия NOT_RUN, неизменность исходников не заменяет проверку игры.

До/после front/side/back/top/¾ и анимационные позы этой задачи **не созданы**, так как художественных изменений не выполнялось. Старые картинки не переобозначены как новые исправления. QA-скриншоты владельца из обещанного отдельного архива также не поступили.

## Отдельный аудит обычных мобов

ordinary-mob-palette-audit.json: 11 реальных текстур Navya/Cinder/Islands, SHA, основные RGB и попарное пересечение квантованных гистограмм. Это ограниченный texture audit; площадь UV-фона влияет на результат, совпадение палитры не доказывает одинаковый силуэт. Массовая переработка бестиария не начата.
