"""Reproduce the source/resource inventory without loading Minecraft."""
import json,re
from pathlib import Path
R=Path(__file__).resolve().parents[2]
J=R/'src/main/java/org/slavicmyths'
A=R/'src/main/resources/assets/slavicmyths'
D=R/'src/main/resources/data/slavicmyths'

def link(p):
    rel=p.relative_to(R).as_posix()
    return f'[{p.name}](../{rel})'

def main():
    lang=json.loads((A/'lang/ru_ru.json').read_text('utf-8-sig'))
    java={p:p.read_text('utf8') for p in sorted(J.rglob('*.java'))}
    def sources(id):
        literals=('"'+id+'"','"slavicmyths:'+id+'"')
        return [p for p,t in java.items() if any(x in t for x in literals)]
    def label(key):return lang.get(key,'—').replace('|','/').replace('\n',' ')
    lines=['# Каталог текущего содержимого Slavic Myths','',
      'Срез текущих исходников 1.9.7.5. Это статический каталог production Java и ресурсов, а не runtime registry dump и не заявление об игровой приёмке. '+
      'Модель или локализация сама по себе не доказывает регистрацию; для сущностей и предметов приведены найденные literal-ссылки Java. '+
      'Динамические семейства дополнительно перечислены в разделах систем. Проверяйте фабрику регистрации и вызов init в [SlavicMyths.java](../src/main/java/org/slavicmyths/SlavicMyths.java).', '',
      'Пересоздание: `python tools/recovery-43/content_catalog.py`. Отдельная локальная V3 достижений и баланс PR #27 сюда не включены.','',
      '## Системы и регистрация','',
      '| Система | Действующие исходники |','|---|---|']
    modules={
      'Духи, водные и лесные встречи':['registry/ModEntities.java','world/SpiritSpawns.java','water/WaterSpawns.java','world/LandEncounters.java'],
      'Волк и четыре задания':['military/GreyWolfStory.java','military/WolfStoryDefinition.java'],
      'Яга и услуги':['yaga/YagaServices.java','yaga/YagaMenu.java'],
      'Кузя и Баюн':['kuzya/KuzyaContent.java','bayun/BayunContent.java','bayun/BayunFeature.java','bayun/BayunEntity.java'],
      'Богатыри, Маринка, Ским, Финист и Калин':['bogatyr/BogatyrContent.java','marinka/MarinkaContent.java','skim/SkimContent.java','finist/FinistContent.java','kalin/KalinContent.java'],
      'Мор и задания':['mor/MorContent.java','mor/MorBlocks.java','quest185/Quest185Content.java'],
      'Полоз, зимние боссы':['poloz/PolozContent.java','poloz/PolozEvents.java','morozko/FrostContent.java','karachun/NightContent.java'],
      'Святыни, хранители, Горыныч и оберег':['sanctuary/SanctuaryContent.java','guardians/GuardianContent.java','serpenthill/HillContent.java','gorynych/GorynychContent.java','winters/WintersContent.java'],
      'Классы и финальные способности':['rpg/classes/ClassDefinitions.java','rpg/classes/ClassUltimates.java','rpg/classes/ClassState.java'],
      'Оружие, броня, руны':['combat/WeaponCatalog.java','combat/RareWeapons.java','equipment/UniqueEquipment.java','rpg/Runes.java','rpg/RareRunes.java','rpg/RuneFoundation.java'],
      'Земледелие, сад, животные':['farming/Farming.java','garden/Gardens.java','husbandry/Husbandry.java'],
      'Кухня, напитки, текстиль':['kitchen/KitchenII.java','brewing/Brewing.java','textile/Textiles.java'],
      'Мебель, хранение, деревни':['furniture/Furniture.java','storage/Household.java','village/VillageRoles.java','village/VillageFoundation.java'],
      'Книга и ритуалы':['tales/TaleNetwork.java','tales/TaleData.java','item/BossRitualItem.java']}
    for title,paths in modules.items():
        found=[J/p for p in paths if (J/p).exists()]
        lines.append('|'+title+'|'+'; '.join(map(link,found))+'|')
    lines+=['','## Сущности: ID с локализацией и literal-ссылками production Java','',
      'Включены также технические сущности: снаряды, места сидения, призванные помощники. Наличие в таблице не означает естественный спавн. '+
      'Строки локализации состояний (`.enraged`, имена копий) отфильтрованы по literal ID.','',
      '| Registry ID | Имя | Java (регистрация или использование) |','|---|---|---|']
    for key in sorted(lang):
        if not key.startswith('entity.slavicmyths.'):continue
        id=key.removeprefix('entity.slavicmyths.'); files=sources(id)
        if not files:continue
        lines.append(f'|`slavicmyths:{id}`|{label(key)}|'+ '; '.join(map(link,files[:3]))+'|')
    lines+=['','## Структуры: зарегистрированные resource ID','',
      'JSON определяет type и biome tag; частоту смотрите в соответствующем structure_set, содержимое — в генераторе/шаблонах.','',
      '| Resource ID | JSON | Биомы | Тип |','|---|---|---|---|']
    for p in sorted((D/'worldgen/structure').glob('*.json')):
        obj=json.loads(p.read_text());lines.append(f'|`slavicmyths:{p.stem}`|{link(p)}|`{obj["biomes"]}`|`{obj["type"]}`|')
    lines+=['','## Блоки: текущие blockstate resource ID','',
      'Blockstate — подтверждение ресурсного определения, не самостоятельный runtime export реестра. Семейства дерева и мебели регистрируются фабриками.','',
      '| Resource ID | Имя | Blockstate |','|---|---|---|']
    for p in sorted((A/'blockstates').glob('*.json')):
        lines.append(f'|`slavicmyths:{p.stem}`|{label("block.slavicmyths."+p.stem)}|{link(p)}|')
    lines+=['','## Предметы: локализованные ID с моделями','',
      'Внутренние модели режимов/натяжения без отдельного имени предмета исключены. Третий столбец — источник ресурса; '+
      'последний — literal-ссылка Java, если она найдена. «Динамическое семейство» требует проверки соответствующей фабрики в таблице систем выше.','',
      '| ID | Имя | Модель | Java |','|---|---|---|---|']
    for p in sorted((A/'models/item').glob('*.json')):
        key='item.slavicmyths.'+p.stem
        block='block.slavicmyths.'+p.stem
        if key not in lang and block not in lang:continue
        files=sources(p.stem)
        lines.append(f'|`slavicmyths:{p.stem}`|{label(key if key in lang else block)}|{link(p)}|'+('; '.join(map(link,files[:2])) if files else 'Динамическое семейство; см. регистрацию системы')+'|')
    lines+=['','## Рецепты, сказания и задания','',
      f'- Рецепты: **{len(list((D/"recipe").rglob("*.json")))}** JSON в [data/slavicmyths/recipe](../src/main/resources/data/slavicmyths/recipe). Состав, тип станции и result — в самих JSON.',
      f'- Сказания: **{len(list((D/"tales").rglob("*.json")))}** JSON в [tales](../src/main/resources/data/slavicmyths/tales). Предметы/существа в subjects связывают статьи с открытием знаний.',
      '- История Волка: [четыре задания](../src/main/resources/data/slavicmyths/wolf_story/quests.json), серверная обработка — GreyWolfStory.',
      '- Квестовая линия Мора: production пакет [quest185](../src/main/java/org/slavicmyths/quest185).',
      '- Достижения: [advancement](../src/main/resources/data/slavicmyths/advancement). Старые vanilla ID не переименованы; отдельная V3 не интегрирована.',
      '- Динамические характеристики классов и финальных направлений задают ClassDefinitions/ClassUltimates; не ранняя CLASS_SYSTEM.md.',
      '', '## Границы проверки','',
      'Сохранены прежние mod/registry ID и реальные assets. Этот каталог воспроизводится без мира и без загрузки Minecraft. '+
      'Модели, анимации, звук, естественная встречаемость, прохождение и баланс требуют отдельной игровой проверки. '+
      'Исторические отчёты и будущие спецификации не использованы как доказательство реализации.','']
    (R/'docs/CONTENT_CATALOG.md').write_text('\n'.join(lines),'utf8')
    print('CATALOG',len(lines),'lines')

if __name__=='__main__':main()
