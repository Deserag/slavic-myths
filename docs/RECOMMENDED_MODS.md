# Recommended Mods — Minecraft 1.16.5 Forge

| Мод | Рекомендуемая версия | Назначение | Обязателен |
| --- | --- | --- | --- |
| Just Enough Items (JEI) | 7.8.0.1013 для 1.16.5 | Список предметов, рецепты и применения рядом с инвентарём | Нет |
| FallingTree | 1.16.5-2.11.7 | Рубка дерева целиком; отдельный аналог TreeCapitator | Нет |

Проверенные страницы автора/распространения:
- [JEI 7.8.0.1013 (Forge 1.16.5)](https://www.curseforge.com/minecraft/mc-mods/jei/files/5846870).
- [Maven JEI](https://maven.blamejared.com/mezz/jei/jei-1.16.5/7.8.0.1013/).
- [FallingTree 2.11.7 (1.16.5, Forge/Fabric)](https://modrinth.com/mod/fallingtree/version/2.11.7).

Это закреплённые совместимые версии, не заявление о самой новой сборке JEI.
Используйте файлы именно для 1.16.5; название JEI Integration обозначает другой мод.

Установите companion JAR отдельно в mods рядом со slavicmyths-0.4.0.jar.
В slavicmyths нет вложенных JEI/FallingTree JAR, скопированного кода этих модов или
обязательной зависимости. Все семь обычных рецептов
Slavic Myths используют стандартные crafting_shaped/crafting_shapeless и поддерживаются
ванильной интеграцией JEI. Категория «Ритуалы» показывает три последовательных дара,
Древний знак и жертвенник. Редкие выпадения и естественный spawn не являются рецептами.

Для разработки runtimeOnly fg.deobf подключает JEI 7.8.0.1013 из Maven автора.
`gradlew.bat runClient -PwithoutJei` запускает среду без него; обычная сборка никогда
не упаковывает runtimeOnly dependency внутрь мод-JAR. CompileOnly API используется
только изолированным @JeiPlugin; common setup на него не ссылается.
FallingTree в Gradle не подключён; поведение рубки в этой итерации не тестировалось.
