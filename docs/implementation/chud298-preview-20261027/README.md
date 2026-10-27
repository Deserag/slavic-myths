# #298 — preview по новому текстовому референсу

**Не внедрено в мод. Визуальная приёмка не состоялась.** [Утверждённые требования](APPROVED_REFERENCE.md). PNG в proposed — исходные увеличенные эскизы imagegen, не финальные точные 16×16 ресурсы. Временный pack использован только в отдельном QA-профиле.

| Предмет | Sprite | GUI Minecraft | В руке Minecraft | До изменений |
|---|---|---|---|---|
| `chud_master_pickaxe` | [PNG](proposed/chud_master_pickaxe.png) | [GUI](preview-chud_master_pickaxe-gui.png) | [Рука](preview-chud_master_pickaxe-hand.png) | [GUI до](before-chud_master_pickaxe-gui.png), [рука до](before-chud_master_pickaxe-hand.png) |
| `chud_multitool_pickaxe` | [PNG](proposed/chud_multitool_pickaxe.png) | [GUI](preview-chud_multitool_pickaxe-gui.png) | [Рука](preview-chud_multitool_pickaxe-hand.png) | [GUI до](before-chud_multitool_pickaxe-gui.png), [рука до](before-chud_multitool_pickaxe-hand.png) |
| `chud_multitool_axe` | [PNG](proposed/chud_multitool_axe.png) | [GUI](preview-chud_multitool_axe-gui.png) | [Рука](preview-chud_multitool_axe-hand.png) | [GUI до](before-chud_multitool_axe-gui.png), [рука до](before-chud_multitool_axe-hand.png) |
| `chud_multitool_shovel` | [PNG](proposed/chud_multitool_shovel.png) | [GUI](preview-chud_multitool_shovel-gui.png) | [Рука](preview-chud_multitool_shovel-hand.png) | [GUI до](before-chud_multitool_shovel-gui.png), [рука до](before-chud_multitool_shovel-hand.png) |

## Оценка текущих кандидатов

Кирка опирается на ванильный силуэт, графит и медь; формы мультиинструмента комбинируют кирку/топор/мотыгу/лопату, меняя доминирующую часть. Все рендерятся через прежний handheld model, без новых ModelPart и механик.

У этих эскизов есть ограничения: изображение крупнее целевой пиксельной сетки, у мультиинструментов головка выглядит насыщенной деталями. Их нельзя считать доказанным точным recolor ванильной маски или окончательным выполнением требования «простой 16×16 sprite». Перед production потребуются упрощение на целевой сетке, проверка соответствия форме и явное решение владельца по визуалу. Не утверждать их автоматически только потому, что pack загрузился.

Два последовательных запуска одного dev client: первый дал ранний захват руки во время equip-анимации, повтор сделан после 2.5 секунд и с обычным Survival inventory (предметы выданы fixture). Финальные before/preview сняты в одинаковом окне 854×480, GUI2, первой персоне, без перемещения камеры. Это проверка рендера, не добычи/характеристик инструментов. Громкость 0.3, FPS30, render2, 2CPU/1536MiB. Игровые процессы завершены.

`first-capture-receipt.json` хранит ограничение первого захвата; `runtime-receipt.json` — финальный перечень кадров. Логи обоих запусков сохранены; в них есть обычные предупреждения NeoForge/shader/vanilla sound и задержка при reload, они не скрыты и не объявлены исправленными. [asset-manifest.json](asset-manifest.json) фиксирует SHA preview и неизменённых production textures/models. [Chud298-preview.zip](Chud298-preview.zip) — воспроизводимый временный resource pack, не production JAR и не рекомендация устанавливать его в личный профиль.
