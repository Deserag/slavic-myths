"""Only current reward-weapon text overlays; never regenerates art/gameplay."""
import json
from pathlib import Path

FROST_STREAM_TEXTS = {
    'ru_ru': {
        'icicles_status': 'Сосульки [%s]: %s',
        'wave_status': 'Морозная волна [%s]: %s',
        'ability_ready': 'готово',
        'ability_cooldown': 'перезарядка %s с',
        'stream': 'ПКМ — морозный поток: действует ещё %s с',
        'stream_ready': 'ПКМ — морозный поток: готов (удерживать до %s с)',
        'stream_cooldown': 'ПКМ — морозный поток: перезарядка %s с',
        'stream_released': 'Поток остановлен: ПКМ отпущена. Перезарядка 10 с.',
        'stream_finished': 'Поток завершён: 3 секунды. Перезарядка 10 с.',
        'stream_menu': 'Поток остановлен: открыто меню. Перезарядка 10 с.',
        'stream_connection': 'Поток остановлен: более секунды нет подтверждения удержания. Перезарядка 10 с.',
    },
    'en_us': {
        'icicles_status': 'Icicles [%s]: %s',
        'wave_status': 'Frost wave [%s]: %s',
        'ability_ready': 'ready',
        'ability_cooldown': '%ss cooldown',
        'stream': 'Use — frost stream: active for %ss more',
        'stream_ready': 'Use — frost stream: ready (hold up to %ss)',
        'stream_cooldown': 'Use — frost stream: %ss cooldown',
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
        data['gorynych196.storm_stats']=('Буря: +%s HP; радиус %s м; Замедление II на %s с; до %s целей. Повтор по цели раз в %s с.' if locale=='ru_ru' else 'Storm: +%s HP; %sm radius; Slowness II for %ss; up to %s targets. Repeat per target every %ss.')
        path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

if __name__=='__main__': apply()
