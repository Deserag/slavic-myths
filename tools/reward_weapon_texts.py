"""Only current reward-weapon text overlays; never regenerates art/gameplay."""
import json
from pathlib import Path

FROST_STREAM_TEXTS = {
    'ru_ru': {
        'stream': 'Поток: осталось %s с',
        'stream_ready': 'ПКМ: поток %s с — готов',
        'stream_cooldown': 'Поток: перезарядка %s с',
        'stream_released': 'Поток остановлен: ПКМ отпущена. Перезарядка 10 с.',
        'stream_finished': 'Поток завершён: 3 секунды. Перезарядка 10 с.',
        'stream_menu': 'Поток остановлен: открыто меню. Перезарядка 10 с.',
        'stream_connection': 'Поток остановлен: более секунды нет подтверждения удержания. Перезарядка 10 с.',
    },
    'en_us': {
        'stream': 'Stream: %ss remaining',
        'stream_ready': 'Use: %ss stream — ready',
        'stream_cooldown': 'Stream: %ss cooldown',
        'stream_released': 'Stream stopped: use released. 10s cooldown.',
        'stream_finished': 'Stream finished: 3 seconds. 10s cooldown.',
        'stream_menu': 'Stream stopped: menu opened. 10s cooldown.',
        'stream_connection': 'Stream stopped: no hold confirmation for over one second. 10s cooldown.',
    },
}

def apply():
    root=Path(__file__).resolve().parents[1]
    for locale,texts in FROST_STREAM_TEXTS.items():
        path=root/'src/main/resources/assets/slavicmyths/lang'/f'{locale}.json'
        data=json.loads(path.read_text(encoding='utf-8'))
        data.update({'frost191.'+key:value for key,value in texts.items()})
        path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

if __name__=='__main__': apply()
