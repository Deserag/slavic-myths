# Источники звука 1.4.3

28 настоящих mono Vorbis OGG, 23 события. Лицензии и описания источников проверены на страницах авторов. Публичные preview использованы на условиях указанных авторами лицензий.

| Источник | Автор и лицензия | Использование |
|---|---|---|
| [Dog Growl](https://freesound.org/people/coldvet/sounds/404920/) | HarveyBird/Frederick Bird (`coldvet`), CC0 1.0 | Натуральная основа дыхания/рычания/укусов, excerpt/RMS selection, pitch/speed, envelopes |
| [Growl](https://freesound.org/people/egomassive/sounds/536761/) | `egomassive`, CC0 1.0; автор указывает исходный Monster Growl `Cylon8472`, также CC0 | Ограниченный грубый слой |
| [Wolf howls](https://commons.wikimedia.org/wiki/File:Wolf_howls.ogg) | U.S. Fish and Wildlife Service, public domain | Новые отрывки 11.2–12.7s для ухода и 14.2–15.7s для короткого воя; старый Волк не изменён |
| Foley | Собственная процедурная запись/синтез, deterministic seed | Воздух когтей, земля, шаг/посадка/переход, извлечение черепа, снятие проклятия |

Основные creature cues: 72% natural dog, 24% rough growl, 4% собственный harmonic. Два воя заменяют dog слоем FWS howl. Revive: 45/25/25/5% dog/rough/own noise/own harmonic. Каждый файл перекодирован mono 44100 Hz Vorbis quality5; после повторного декодирования проверен peak ≤−1dBFS. [Пофайловые длительности, hashes, лицензии и источники](resource_gates.json). Текст `assets/slavicmyths/sounds/volkolak/LICENSES.txt` включён в production JAR. Звуки других игр/модов не использованы; silent placeholders отсутствуют. Восприятие громкости/синхронизации в Minecraft остаётся ручной приёмкой.
