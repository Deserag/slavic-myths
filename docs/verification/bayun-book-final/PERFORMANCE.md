# Измерения и пределы работы

Реальный dedicated GameTest server, последний прогон: **100 вызовов TaleService.snapshot**, суммарно 214.6881 ms, среднее **2.146881 ms**, 217 записей и 487 реальных серверных рецептов. Это среднее время формирования серверного снимка после прогрева, **не задержка открытия клиентского экрана, не p99 и не MSPT**. До кэша рецептов среднее того же упражнения было 11.039 ms. Вариативность последних прогонов 2.025–2.523 ms не считается измеренным обещанием для любого оборудования.

Настоящий Snapshot.CODEC encode/decode: **229697 bytes**; лимит теста <900000. Данные locale не дублируются в пакете. Кэш рецептов weak-key RecipeManager, сбрасывается при reload; клиент хранит snapshot до logout. Полный снимок — при открытии (cooldown10 ticks), локальные изменения — компактными delta. Read rate4ticks, choice rate10ticks, nonce/sequence/TTL1200.

На кота максимум32 активных персональных clocks, local notice раз в10ticks в12блоках, online UUID lookup, один физический victim. Нет поиска всех сущностей мира или всех игроков на world tick. Ambient выбор раз30–90s в пределах4блоков дома, native navigation только для уже загруженного места. Song particles3 раз в5ticks; звук ограничен cooldown и одним loop на loaded NPC. Хинты не вызывают locate и не загружают anchor chunk.

Book observation: один sample на игрока раз20ticks, local12 AABB максимум64 кандидата; известные записи не повторяют LOS. Structure detection лишь при смене block position/dimension, максимум32 starts в **уже загруженных** chunks через getChunkNow. Нет getStructureWithPieceAt/forced tickets. Открытие книги не выполняет spatial scan.

Native geometry143bones254cubes3048triangles; paw grounding32 corners, не обход всего mesh. Real MSPT для 1/10 котов, p99 AI, client FPS, memory leak/hour, mixed multiplayer, GUI cold latency: **NOT_MEASURED**. Один fixture snapshot benchmark не заменяет эти измерения. Restart fixture использует временный forced chunk только в test namespace, который исключён из production JAR.
