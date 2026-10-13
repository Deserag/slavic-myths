# Карта изображение → реальный ассет / действие

## Главные изображения (в ZIP, обязательно смотреть)
1. `02_REFERENCES/approved_full/MOROZKO_BOSS_APPROVED.png` — весь образ босса и его навыки.
2. `02_REFERENCES/approved_full/MOROZKO_ITEMS_APPROVED.png` — все шесть видов наград/артефактов.

| Вырезка | Что взять с изображения | Минимальные ассеты в игровом проекте |
|---|---|---|
| `crops/boss_01_main_model.png` | силуэт 3.25 блока, шуба, борода, широкая меховая горжетка, посох | entity model/UV/texture + boss renderer |
| `crops/boss_02_front_back.png` | симметричные орнаменты спины, силуэт сзади, передний ремень/спущенный кусок ткани | front/back texture+model |
| `crops/boss_03_face_staff.png` | кубическое лицо, свечение глаз, длинная ступенчатая борода, геометрия льдинок посоха | отдельные head/beard/staff parts |
| `crops/boss_04_three_ice_clones.png` | три полупрозрачные голубые версии того же Морозко | clone renderer alpha, particle spawn, animations |
| `crops/boss_05_frost_aura.png` | видимое кольцо снега/льда и частицы вокруг босса | client frost particle emitter / aura visual + cold HUD |
| `crops/boss_06_icicle_strike.png` | **настоящие падающие большие ледяные сосульки**, не blue arrows | temporary icicle geo/particle/hitbox, staff G attack preview |
| `crops/items_01_staff.png` | полная модель посоха, кристаллическое снежное навершие, разрезной наконечник | held 3D model, inventory icon, projectile models |
| `crops/items_02_frost_mirror.png` | серебряный амулет, прозрачный кристалл, игрок + ice clone | accessory icon + player ice clone visuals |
| `crops/items_03_resistance_rune.png` | защитный угловатый рунический знак на осколке | уникальная текстура руны морозостойкости |
| `crops/items_04_frost_rune.png` | вертикальная снежинка/колючий знак | отдельная уникальная текстура руны мороза |
| `crops/items_05_eternal_cold_heart.png` | голубое гранёное сердечко с белым ядром | item icon + defeat cutscene particle |
| `crops/items_06_trial_summon_reference.png` | переносной ледяной тотем и центральная снежинка | item icon, summon projection/particle effect |

## Привязка механических событий
- Boss аура от картинки `boss_05` соответствует `05_BOSS_STATES_AND_FROST_AURA.md` (weakness + slowness + HP drain), а не только визуальному halo.
- Boss умения и G посоха ориентируются на `boss_06` для геометрии сосулек, но число 5, 0.8 c и уход в точку выбираются по `11_REWARD_STAFF_AND_CONTROLS.md`.
- Игрок-клон из `items_02` — не голубой Steve по умолчанию, а **клон скина конкретного игрока**, имеющий snapshot его брони/оружия/энчантов, hit по тому же target даже из другого положения.
- Иконки из листа должны перерисовываться под реальный pixel-art inventory (`nearest`, readable at 16–32px), а не вставляться фотографией из артборда.

## Контроль ассетов перед сдачей
Для каждого reference crop перечислить путь до сделанного игрового PNG/geo/model, а также реализовавшего логику класса и animation. Если цель не сделана — честно пометить «не готово», **не оставлять в игре предмет с пустой иконкой или sprite placeholder**.
