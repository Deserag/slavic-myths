# Подключение 33 реально созданных звуков

Скопировать физические `.ogg` из папок архива в `assets/slavicmyths/sounds/entity/ilya_muromets/` и `.../idolishche_poganoe/`.

Данные из manifest.json имеют поля `id` (event), `file` (пути в данном пакете), `duration_s`, `source`, `rights`. Кодекс должен не просто прочесть manifest, а реально сделать entries в sounds.json и вызвать события по таблицам двух `04_Звуки_...` файлов. Ресурсная ссылка в `sounds.json` в формате Minecraft обычно `slavicmyths:entity/ilya_muromets/shield_raise_01` (без .ogg); сверить с текущим проектом. Для отдельных event names адаптировать `entity.ilya_muromets.shield.raise`.

Проверка технического файла: `ffprobe -v error -show_entries stream=codec_name,channels,sample_rate -of csv=p=0 path.ogg` -> expected `vorbis,22050,1` (порядок полей утилиты не обязателен). Все эти аудио пригодны для использования как оригинальные фоновые и боевые FX, **они НЕ содержат произнесённых актёром реплик**, потому что их не было законно предоставлено. Отдельные actor recorded files могут быть добавлены в будущей версии после проверки прав.
