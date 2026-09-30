## STATUS

PARTIAL — основной контент 0.7.2 реализован. В текущем проекте отсутствует 0.7.1;
интеграция с её специальными болотными материалами/следами не выполнена.

## DONE

Старший Водяной, собственная модель/звуки, три фазы, компактная арена, пробуждение,
boss bar, временное затопление, server-side defeated state, знак/подсказки, три жемчужины
за победу, копьё/выпад, амулет Curios, бросаемая сеть, рецепты, книга и достижения.

## PARTIAL

Progression: работает через Водяного и рыбалку в vanilla реках/болотах.
Осталось подключить редкие следы из реально существующего контента 0.7.1 после его появления.
Основные файлы: water/WaterFishing.java, water/VodyanoyEntity.java, depth/WaterSignItem.java.

## NOT STARTED

Только интеграция с отсутствующей 0.7.1. Не начинать 0.7.3 или общий polish 0.7.4.

## IMPORTANT FILES

- src/main/java/org/slavicmyths/depth/ElderVodyanoy.java
- src/main/java/org/slavicmyths/depth/PoolStoneTile.java
- src/main/java/org/slavicmyths/depth/PoolIndex.java
- src/main/java/org/slavicmyths/depth/DeepPoolFeature.java
- src/main/java/org/slavicmyths/client/ElderVodyanoyModel.java
- tools/depth_072.py
- docs/PROJECT_STATUS.md

## LAST WORKING BUILD

`gradlew.bat --offline clean build` — PASS; `python tools/verify_resources.py` — PASS.
Minecraft: 0 запусков. Поведение и сохранение в игре ещё не проверены пользователем.

## PRODUCTION JAR

`D:\slavic-myths\build\libs\slavicmyths-0.7.2.jar`

## POLYMC JAR

`C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-Testing\.minecraft\mods\slavicmyths-0.7.2.jar`

## NEXT ACTION

После появления 0.7.1 добавить древний водный знак в подходящие редкие болотные находки.
