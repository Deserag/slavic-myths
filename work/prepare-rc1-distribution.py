from pathlib import Path
import json,hashlib,shutil,zipfile
out=Path('work/rc1-distribution');out.mkdir(parents=True,exist_ok=True);(out/'mods').mkdir(exist_ok=True)
jar=Path('release/0.9.10-rc1/slavicmyths-0.9.10-rc1.jar');shutil.copy2(jar,out/'mods'/jar.name)
lock=json.loads(Path('packaging/test-pack-lock.json').read_text(encoding='utf-8'))
mods=[]
for m in lock['mods']:
 url=m.get('downloadUrl')
 if not url:
  fid=m['curseforgeFileId'];url='https://mediafilez.forgecdn.net/files/'+str(fid//1000)+'/'+str(fid%1000)+'/'+m['filename']
 source=Path('.tools/test-pack-0.9.4')/m['filename'];assert hashlib.sha256(source.read_bytes()).hexdigest()==m['sha256']
 mods.append({key:m[key] for key in ['id','version','filename','sha256','size','sourceUrl']}|{'downloadUrl':url,'required':m['id']=='curios'})
manifest={'minecraft':'1.21.1','neoforge':'21.1.255','java':21,'slavicmyths':{'filename':jar.name,'sha256':hashlib.sha256(jar.read_bytes()).hexdigest()},'mods':mods,'xaeroLib':'Embedded in Minimap and World Map; no separate JAR required.'}
(out/'MODS_LOCK.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
for name in ['CHANGELOG_0.9.10-RC1.md','DEPENDENCIES.md','KNOWN_ISSUES.md']:shutil.copy2(Path('release/0.9.10-rc1')/name,out/name)
(out/'README.md').write_text('''# Slavic Myths 0.9.10 RC1 — сборка для игроков

Здесь только файлы для установки: готовый мод, список зависимостей, загрузчик и документация. Исходников, Gradle, миров, логов и персональных настроек нет.

## Установка на Windows

1. Нажмите Code → Download ZIP и распакуйте архив.
2. В этой папке откройте PowerShell и выполните:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\\DOWNLOAD_MODS.ps1
```

Загрузятся Curios, JEI, Jade, Xaero Minimap, Xaero World Map и FallingTree из закреплённых официальных файлов. Каждый JAR проверяется по SHA-256. Вместе с Slavic Myths папка mods содержит 7 модов. XaeroLib уже встроен в JAR карт.

Для минимального набора Slavic Myths + Curios:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\\DOWNLOAD_MODS.ps1 -CoreOnly
```

3. Создайте отдельный профиль Minecraft 1.21.1 / NeoForge 21.1.255 / Java 21. Скопируйте содержимое mods в mods этого профиля. Другой JAR Slavic Myths в профиле нужно убрать вручную.
4. Запускайте игру сами. Скрипт не запускает игру и не меняет существующие профили, конфиги или миры.

Curios обязателен; остальные моды опциональны. На другой ОС скачайте закреплённые файлы по MODS_LOCK.json и проверьте hashes. Полный комплект можно получить без git clone через ZIP этой ветки; такой же архив прикреплён к prerelease.

Это тестовая сборка. Реальная игровая/визуальная приёмка ещё не выполнена. См. REMAINING_WORK.md и KNOWN_ISSUES.md. Issues: https://github.com/Deserag/slavic-myths/issues
''',encoding='utf-8')
(out/'REMAINING_WORK.md').write_text('''# Что осталось по запросу public playtest stabilization

## Не выполнена игровая приёмка

- Щит: не проверены в клиенте first/third person, блокирование в реальном бою, потеря прочности, анимация, cooldown после отключения щита, ремонт и enchantments. Исправлены модели/UV/переключение; совместимость функциональности сверена с установленным ShieldItem/Player API, что не заменяет игровой тест.
- Камень пути: не проверены реальное RMB/открытие экрана, выбор пути/расход подношений и XP, navigation marker, повторный вход и multiplayer. Клиентский экран зарегистрирован; серверная старая функция сохранена. Block entity не добавлялась: исходный дизайн хранит пути на игроке и markers в SavedData.
- Рунная наковальня: не пройдены реальные forge/install/remove, расход, выход в первом слоте, shift-click, закрытие, перезаход и multiplayer. Исправлены доступность экрана, валидация первого слота и перенос shift-click; новая система рун не создавалась.
- Book/Kitchen: GUI scale 2/3/4, 16:9 и wide resolutions не проверены в игре. Непрозрачные области и текущая компоновка реализованы, но это ещё не визуальная приёмка.
- Кухня: дополнительные эффекты блюда в preview не показаны отдельно; выводятся название, количество, nutrition и saturation. Некоторые длинные строки ограничены двумя строками. Нужны ручная проверка читаемости и работа с реальными рецептами.

## Оставшиеся проверки/исправления

- Общая серверная регрессия перед последним промптом была 26/27: проверка подхода обнаружила расчистку каменной кладки. Код затем исправлен; повторный runtime-тест не запускался по запрету автоматического Minecraft.
- 16 прежних ссылок на старые vanilla door-model parents не исправлены в RC1.
- Внешние скриншоты курганов, визуальная связность patches, полноценные бои/AI, реальный multiplayer Cut/перезаход не проверены.
- Полное естественное покрытие обычного мира и MSPT не приняты этим RC.

## Что сознательно отложено по заданию

- Новый overhaul разбойников/лагерей и Соловья.
- Новая система рун 0.9.11, категории/дерево рун, новые sockets/classes.

Полный Equipment & Art Overhaul из предыдущего ZIP также не завершён: его недостающие параметры не выдумывались; последний запрос переключил работу на стабилизацию RC.

## Что выполнено

Clean build, production JAR, проверка ресурсов/данных/компонентов, source branch, commit, annotated tag и публичный prerelease с проверенным скачиванием JAR. В первой публикации отсутствовала отдельная ветка для игроков; исправлено веткой release/0.9.10-distribution с готовым модом, загрузчиком зависимостей и комплектом инструкций.
''',encoding='utf-8')
(out/'SHA256SUMS.txt').write_text(manifest['slavicmyths']['sha256']+'  mods/'+jar.name+'\n')
print('Prepared distribution files; 6 pinned dependency downloads.')
