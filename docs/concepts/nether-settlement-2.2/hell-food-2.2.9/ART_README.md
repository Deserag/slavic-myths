# 2.2.9 — проверяемые художественные и технические источники

**ВНИМАНИЕ CODEX:** здесь действительно лежат девять растровых PNG, но это **малые технические пиксельные схемы**, а не одобренные полноразмерные постеры. Файлы должны открываться именно по ссылкам с каталогом `art/` ниже; прежние ссылки пропускали этот каталог и были ошибочными. Исходники находятся на ветке `codex/2.2.9-hell-livestock-crops-20261030`, не на `hell` до слияния.

## A. Реально опубликованные технические PNG

- [Пекельная корова — вид сбоку](./art/cow_side_pixel_reference.png) — [GitHub raw](https://raw.githubusercontent.com/Deserag/slavic-myths/codex/2.2.9-hell-livestock-crops-20261030/docs/concepts/nether-settlement-2.2/hell-food-2.2.9/art/cow_side_pixel_reference.png)
- [Пекельная корова — вид спереди](./art/cow_front_pixel_reference.png) — [GitHub raw](https://raw.githubusercontent.com/Deserag/slavic-myths/codex/2.2.9-hell-livestock-crops-20261030/docs/concepts/nether-settlement-2.2/hell-food-2.2.9/art/cow_front_pixel_reference.png)
- [Пекельная курица — вид сбоку](./art/chicken_side_pixel_reference.png) — [GitHub raw](https://raw.githubusercontent.com/Deserag/slavic-myths/codex/2.2.9-hell-livestock-crops-20261030/docs/concepts/nether-settlement-2.2/hell-food-2.2.9/art/chicken_side_pixel_reference.png)
- [Пекельная курица — вид спереди](./art/chicken_front_pixel_reference.png) — [GitHub raw](https://raw.githubusercontent.com/Deserag/slavic-myths/codex/2.2.9-hell-livestock-crops-20261030/docs/concepts/nether-settlement-2.2/hell-food-2.2.9/art/chicken_front_pixel_reference.png)
- [Жар-корень — иконка](./art/heatroot_pixel_reference.png) — [GitHub raw](https://raw.githubusercontent.com/Deserag/slavic-myths/codex/2.2.9-hell-livestock-crops-20261030/docs/concepts/nether-settlement-2.2/hell-food-2.2.9/art/heatroot_pixel_reference.png)
- [Пепельный гриб — иконка](./art/ash_mushroom_pixel_reference.png) — [GitHub raw](https://raw.githubusercontent.com/Deserag/slavic-myths/codex/2.2.9-hell-livestock-crops-20261030/docs/concepts/nether-settlement-2.2/hell-food-2.2.9/art/ash_mushroom_pixel_reference.png)
- [Багровый перец — иконка](./art/crimson_pepper_pixel_reference.png) — [GitHub raw](https://raw.githubusercontent.com/Deserag/slavic-myths/codex/2.2.9-hell-livestock-crops-20261030/docs/concepts/nether-settlement-2.2/hell-food-2.2.9/art/crimson_pepper_pixel_reference.png)
- [Пепельный хрен — иконка](./art/ash_horseradish_pixel_reference.png) — [GitHub raw](https://raw.githubusercontent.com/Deserag/slavic-myths/codex/2.2.9-hell-livestock-crops-20261030/docs/concepts/nether-settlement-2.2/hell-food-2.2.9/art/ash_horseradish_pixel_reference.png)
- [Угольный чеснок — иконка](./art/coal_garlic_pixel_reference.png) — [GitHub raw](https://raw.githubusercontent.com/Deserag/slavic-myths/codex/2.2.9-hell-livestock-crops-20261030/docs/concepts/nether-settlement-2.2/hell-food-2.2.9/art/coal_garlic_pixel_reference.png)

Исходная пиксельная палитра для технических иконок: `k #262329` (уголь), `m #645458` (пепел), `r #9b2636` (багрянец), `o #ee672a` (жар), `y #ffbb4c` (свет), `w #dbd4c6` (кости/свет).

## B. Художественные постеры (высокое разрешение)

В беседе пользователь одобрил показанные полноцветные концепт-постеры (1122×1402): **пекельная корова, пекельная курица, жар-корень, багровый перец, пепельный гриб**. Постер пирожка с жар-корнем относится к **отложенным блюдам** и не входит в код 2.2.9. В репозитории **нет проверенных полноразмерных исходников этих изображений**. Малые схемы не должны называться «утверждёнными оригиналами». Постеры двух добавленных культур (**пепельный хрен, угольный чеснок**) пока не утверждались, а текущие PNG — лишь стартовые инженерные иконки.

**Архитектура работ:** строить игровые модели/текстуры по точному словесному паспорту и проверяемым схемам. Пока не проведено сравнение с исходными постерами и actual Minecraft, статус `ORIGINAL_FULLRES_COMPARE_NOT_RUN`; отсутствующий постер **не блокирует** регистрацию, механику, рост, дроп, спавн, тесты и создание новых рабочих ассетов.

## C. Обязательная проверка

1. Сначала открыть ссылки **каждого** файла и убедиться, что ответ GitHub не 404 и PNG действительно декодируется.
2. Сопоставить точные размеры, UV-модель, front/side/back и иконки, сверить палитру и масштаб с [контрактом](./2.2.9_LIVESTOCK_CROPS_SPEC_R1.md).
3. Экспортировать реальные игровые текстуры и сохранить скриншоты **из клиента** в одинаковых ракурсах; статический SVG/PNG — не скриншот игры.
4. Для финального художественного PASS необходимо отдельно физически передать и проверить оригинальные постеры, а затем получить приёмку владельца. Прежний пример с #353/#354 — отрицательный: нельзя прятать отсутствие оригиналов за имеющимися .bbmodel или маленькими PNG.

**Scope:** ровно 2 животных, 5 культур и их корм/дроп/спавн; без сложных готовых блюд, «Перечного жара» и кулинарных рецептов.
