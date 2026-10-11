from pathlib import Path
import json,shutil
R=Path(__file__).resolve().parents[1];out=R/'docs/verification/visual-1.6.3';out.mkdir(parents=True,exist_ok=True)
g=json.loads((out/'resource-gates.json').read_text());assert g['status']=='PASS'
shutil.copy2(R/'work/visual-163/build.log',out/'build.log');shutil.copy2(R/'work/earth/native-preview.png',out/'earth-static-preview.png')
body='''# Исправления 1.6.3 — 2026-10-10

## Причина слабого урона

Read-only чтение `level.dat` последнего тестового сохранения «Новый мир (1)»: Difficulty=1 (EASY), активны Resistance I и Fire Resistance I; четыре обычных алмазных предмета брони, без enchantments/runes. Мир не изменён. Сохранение — снимок, не live телеметрия текущего боя.

Прежний обычный mobAttack / thrown дополнительно масштабировался сложностью. Для кулака raw=20: EASY raw=11 → diamond 3.41 HP → Resistance I 2.728 HP. Для камня прежний результат в этих условиях примерно 1.86 HP. Это расчёт причин снижения, не измерение нового игрового попадания.

Новые физические damage types `earth_strike`/`earth_projectile`: scaling=never, без bypass armour/effects/enchantments/shield. После полного алмаза без эффектов: кулак 8 HP, камень 5 HP, прямой столб 13 HP, splash 8 HP, ударная волна 10.5 HP, шипы 9 HP. С Resistance I обычный кулак 6.4 HP, камень 4 HP; Protection, absorption, другие настоящие защиты продолжают работать. Слуги сохраняют прежний согласованный баланс.

`/sm earth_spirit status` теперь показывает последний фактический LivingDamageEvent.Post от данного босса по игроку: тип атаки, original raw, actualHP, armour/toughness, difficulty, resistance и reductions armour/enchantments/absorption. Это диагностирует результат после всех обработчиков, а не обещание на основе raw. Без попадания выводится явный No registered hit. Диагностика не спамит чат/лог, не меняет HP.

## Темп и движение

- Кулак 24 тика (было 32), попадание на 12-м (было 18), восстановление 6 (было 12), cooldown 30 (было 50). Близкая цель имеет приоритет и без предварительного удара игрока.
- Между тяжёлыми атаками 16 тиков вместо 32. Volley cooldown 120 вместо 360, tremor 180 вместо 440, counter 180 вместо 400, pillar 280 вместо 560. Телеграфы тяжёлых атак сохранены, не сведены к мгновенному урону.
- Камень 1.65 блока/тик вместо .85; столб 1.15 вместо .65; интервал залпа 8 вместо 14. Существующий swept collision и защита от повторного попадания сохранены.
- Походка использует интерполированное реальное перемещение клиента, а не только ослабленный vanilla limb amount. Противофаза бёдер, сгиб коленей, компенсация стопы и вес тела; при неподвижном касте ноги стоят.

## Визуальные исправления

- Вся поверхность Духа использует узнаваемые пиксельные фактуры Minecraft 1.21.1: булыжник, замшелый булыжник, камень, андезит, колотый глубинный сланец. Сохранены отдельные core/root/emissive UV panels и артикулированные выступы. Корпус больше не покрыт крупным случайным камуфляжем.
- Запирающий камень — noOcclusion: неполная модель перестала скрывать верхнюю грань блока под ней. Block model получил particle texture. Коллизия/прочность/registry ID прежние.
- Хвост Сивки использует непрерывный render age вместо packet-stepped modulo gait. Три перекрывающиеся части кисточки двигаются вместе, без противоречащих поворотов; скорость/полёт/управление коня не изменены.
- Яйцо Жар-птицы уже было зарегистрировано в 1.6.2. В 1.6.3 оно первое во вкладке мода «Мобы», без дубликата. `/give @s slavicmyths:firebird_spawn_egg`. Сама одобренная модель/анимация Жар-птицы побайтно сохранена.

## Проверки

Финальный `gradlew.bat clean build` + Earth/Sivka/Firebird/Visual offline: PASS, 31 с, 46 прежних deprecation warnings. Earth 6186, Sivka 7107, Firebird 200020, Visual 15. Дополнительные проверки нового хвоста/артикуляции/условий урона. В промежуточном тестовом harness исправлены неподходящий offline bootstrap DamageType и ошибочный диапазон расчётного EASY значения; production compile проходил.

Resource/production gates: CHECKS проверок PASS; PRESERVED прежних JAR entries побайтно сохранены относительно 1.6.2. Все JSON/PNG проверены, тестовые классы не попадают в production JAR, Java 21. Общие и server классы не получили клиентских зависимостей. Геометрия: 0 перекрывающихся копланарных пар; emissive отправляется один раз. Ресурсный генератор сохраняет новый атлас, шаблоны vanilla textures локально версионированы.

**Запусков Minecraft агентом: 0. Новый игровой бой/походка/визуал NOT TESTED.** Статическое превью не включает все runtime слои и не выдаётся за игровой скриншот. Пользовательские миры и изменения не перезаписаны; commit не создавался.

Production JAR: `build/libs/slavicmyths-1.6.3.jar`; SHA256 `SHA`.

## Краткая игровая проверка

1. Перезапустить 1.21.1 test instance; подтвердить 1.6.3. Проверить яйцо в «Мобы».
2. Кулак: алмаз без enchantments/runes/effects; проверить actualHP для earth_strike в status. С Resistance I проверить снижение. Сложность EASY/NORMAL не должна дополнительно менять число.
3. В первой фазе проверить ответ на близкого игрока, темп залпа и уклонение; на дистанции увидеть шаги коленей/стоп.
4. Поставить камень на траву, осмотреть край: грань травы видна. Проверить призыв ударом и разрушением.
5. Проверить хвост Сивки в покое/галопе/полёте и с седоком; осмотреть Духа с разных сторон, включая смерть.
6. Проверить обычные награды и руну за тестовую победу вне арены.
'''.replace('CHECKS',str(g['checks'])).replace('PRESERVED',str(g['baseline_entries_preserved'])).replace('SHA',g['sha256'])
(out/'REPORT.md').write_text(body,encoding='utf8')
note='''## Исправления боя и визуала 1.6.3 — 2026-10-10

По новому игровому отзыву: в последнем сохранении подтверждены EASY + Resistance I, которые снижали старый raw удар дополнительно. Для физических атак Духа введены scaling=never damage types без обхода брони/защиты: кулак 8 HP после обычного алмаза, с Resistance I 6.4 HP. Добавлена диагностика фактического Post damage в `/sm earth_spirit status`. Ускорены кулак/залп/снаряды и cooldown первой фазы, походка следует реальному перемещению. Весь корпус получил vanilla stone/cobble/moss/andesite/deepslate atlas; noOcclusion печати исправляет скрытую траву; хвост Сивки больше не использует сетевой modulo gait. Яйцо Жар-птицы первое в «Мобы»; её принятый дизайн сохранён.

Clean build PASS (31s), Earth6186/Sivka7107/Firebird200020/Visual15 и resource/production gates PASS. **Запусков Minecraft: 0, новый игровой бой и визуал NOT TESTED**. [Отчёт и ручные проверки](verification/visual-1.6.3/REPORT.md). Мировые данные read-only, registry IDs и чужие изменения сохранены; commit не создавался.

'''
for name in ['PROJECT_STATUS.md','ROADMAP.md','ARCHITECTURE.md']:
 p=R/'docs'/name;s=p.read_text(encoding='utf8');assert not s.startswith('## Исправления боя и визуала 1.6.3');p.write_text(note+s,encoding='utf8')
print(out/'REPORT.md')
