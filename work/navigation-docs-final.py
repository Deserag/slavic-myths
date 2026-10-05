from pathlib import Path
import hashlib,json,re
root=Path('.')
jar=root/'build/libs/slavicmyths-0.9.5.jar';digest=hashlib.sha256(jar.read_bytes()).hexdigest()
intro='''# Slavic Myths 0.9.5 — навигация и зоны поиска

Minecraft 1.21.1 / NeoForge 21.1.255 / Java 21. Слой навигации реализован по последнему
пакету V2. Предыдущий перенос 0.9.4 завершён; его отчёты сохранены как историческая база.

Core/server: DONE по headless проверкам. Client screen/HUD/Xaero: COMPILES, ручная QA не выполнена.
Build и финальные loader/data checks PASS. Minecraft-клиент не запускался.
Production: `build/libs/slavicmyths-0.9.5.jar`. Точный hash: `packaging/test-pack-lock.json`.

[Навигация, алгоритм и фактические ограничения](NAVIGATION_0.9.5.md),
[ручные проверки](MANUAL_QA_0.9.5_NAVIGATION.md), [evidence](verification/navigation-0.9.5/acceptance.json).

'''
for name in ('PROJECT_STATUS.md','ROADMAP.md','ARCHITECTURE.md'):
 p=root/'docs'/name;s=p.read_text(encoding='utf-8')
 if not s.startswith('# Slavic Myths 0.9.5'):
  if name=='ROADMAP.md':specific='''Следующий блок: ручной запуск тестового профиля и 28 navigation QA сценариев,
затем только воспроизведённые ошибки. Новые боссы/loot/worldgen не входят в 0.9.5.
Известный fallback: Xaero approximate center+radius, без polygon overlay. Контракт без
известного реального encounter ждёт появления цели; новых encounters навигация не генерирует.

'''
  elif name=='ARCHITECTURE.md':specific='''Core: navigation/SlavicMarker, NavigationState, SearchArea, NavigationRecords.
Серверный SavedData хранит exact assignments отдельно от public player snapshot.
Typed navigation_request проверяет ownership и права; navigation_snapshot содержит только
одного игрока. Client UI и compat/xaero изолированы; Xaero compileOnly и optional.
Реальный immutable home определяет стабильную revision зоны; discovery разрешает temporary
exact point, lifecycle очищает его. Проверки polling индексируются собственными marker IDs,
не обходят чужие quests; bounded loaded-chunk discovery не создаёт chunks.
Путевой камень по-прежнему открывает RPG menu; новые travel-механики не добавлены.

'''
  else:specific='''Восемь категорий, семь сохраняемых настроек, одна основная цель, клубок только к
размещённой избушке, активированные stones и крупные найденные структуры, private SearchArea,
quest lifecycle и optional Xaero group готовы к ручной QA. Статистика: 1000 зон,
0 outside, 0% inner25, 75.7% outer50–85, 13.3% edge85–95.
Тестовый профиль обновлён на 0.9.5 после gates; семь JAR проверены по hash.

'''
  p.write_text(intro+specific+'## Исторические записи до 0.9.5\n\n'+s,encoding='utf-8')
p=root/'README.md';s=p.read_text(encoding='utf-8')
s=s.replace('# Slavic Myths 0.9.4 — Minecraft 1.21.1 / NeoForge','# Slavic Myths 0.9.5 — Minecraft 1.21.1 / NeoForge')
s=s.replace('build/libs/slavicmyths-0.9.4.jar','build/libs/slavicmyths-0.9.5.jar')
s=re.sub(r'SHA-256: `[0-9a-f]{64}`\.',f'SHA-256: `{digest}`.',s,count=1)
nav='''## Навигация 0.9.5

`/slavicmyths navigation` работает без OP. Назначить клавишу «Навигация» в настройках управления.
Восемь категорий, список/фильтры, одна отслеживаемая цель; скрытие метки сохраняет её данные.
Без Xaero работает компактный HUD. С Xaero выбрать группу **Slavic Myths** вручную.
Зона поиска отображается приблизительным центром и радиусом; круговой overlay отсутствует.
Точные координаты босса открываются только после discovery, в виде временной метки.

Клубок сохраняет метку Яги только после фактического размещения дома. Путевой камень
сохраняет личную метку при активации и сохраняет прежнее RPG menu; телепортация не добавлена.
Крупные структуры сохраняются при обнаружении; малые — кнопкой «Сохранить место рядом».
Контракт без известного реального encounter принимается, но зона ждёт появления подходящей цели.

Диагностика (OP 2):

```text
/slavicmyths dev navigation list
/slavicmyths dev navigation marker <UUID>
/slavicmyths dev navigation searcharea create-test mini
/slavicmyths dev navigation searcharea create-test rare
/slavicmyths dev navigation searcharea create-test world
/slavicmyths dev navigation searcharea create-test unique
/slavicmyths dev navigation searcharea stats
/slavicmyths dev navigation clear-owned
```

`create-test` не создаёт encounter; `clear-owned` очищает навигацию только данного игрока.
[Подробное устройство и ограничения](docs/NAVIGATION_0.9.5.md),
[28 ручных сценариев](docs/MANUAL_QA_0.9.5_NAVIGATION.md),
[сборка/loader evidence](docs/verification/navigation-0.9.5/acceptance.json).

'''
if '## Навигация 0.9.5' not in s:s=s.replace('## Установка и готовая тестовая сборка',nav+'## Установка и готовая тестовая сборка',1)
s=s.replace('Требуются cheats/OP level 2; generate выполняется игроком в Overworld.','Для следующих dev/locate/generate команд требуются cheats/OP level 2; generate выполняется игроком в Overworld.')
s=s.replace('.\\gradlew.bat clean build kurganCheckClasses verifyPortCore', 'python tools/fetch_xaero_api_095.py\n.\\gradlew.bat clean build verifyNavigation verifyPortCore')
s=s.replace('python tools/verify_static_data_1211.py\npython tools/verify_port_1211.py','python tools/verify_static_data_1211.py --navigation\npython tools/verify_port_1211.py --navigation\npython tools/verify_polymc_094.py --navigation')
s=s.replace('выполняет четыре tests','выполняет семь tests (четыре port + три navigation)')
s=s.replace('[Текущий PORT_STATUS и доказательства]','[Исторический PORT_STATUS 0.9.4 и доказательства]')
p.write_text(s,encoding='utf-8')
p=root/'docs/port/PORT_STATUS_0.9.4.md';s=p.read_text(encoding='utf-8')
note='''> Исторический отчёт именно об артефакте 0.9.4. Текущий 0.9.5 добавляет навигацию
> по следующему пакету пользователя: [NAVIGATION_0.9.5](../NAVIGATION_0.9.5.md),
> [отдельная acceptance](../verification/navigation-0.9.5/acceptance.json).
> Старые acceptance/log hashes не перезаписаны. JAR 0.9.4 сохранён в
> `.tools/port-backups/slavicmyths-0.9.4-final.jar`; текущий build/libs содержит 0.9.5.

'''
if 'Исторический отчёт именно' not in s:s=s.replace('\n\n','\n\n'+note,1);p.write_text(s,encoding='utf-8')
print('Updated current docs/README and preserved historical port evidence')
