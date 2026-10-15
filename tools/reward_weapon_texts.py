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

BALANCE_TEXTS = {'ru_ru': {'tooltip.slavicmyths.reward_raw': 'Базовый урон: %s HP (без критов и модификаторов; умения отдельно).', 'tooltip.slavicmyths.nightingale_dagger': '%s урона. Ветровой взмах: 8 блоков, 7 урона, заряд 0,4 с, перезарядка 7 с.', 'frost191.staff_stats': 'Снаряд %s HP / 0,8 с; поток %s HP каждые 0,5 с до 3 с / 10 с; 5 сосулек по последней цели за 8 с / 20 с; волна 4 HP / 16 с.', 'item.slavicmyths.three_breath_crossbow.desc': 'Всегда три стрелы: каждая наносит %s базового урона до брони. Shift+ПКМ: Мор (облако и слабость), Пламя (горение 2 с), Буря (+3 урона, отбрасывание и Замедление II на 2 с). Shift+ЛКМ: веер / очередь. Без бутылочек.'}, 'en_us': {'tooltip.slavicmyths.reward_raw': 'Base damage: %s HP (before crits and modifiers; abilities separate).', 'tooltip.slavicmyths.nightingale_dagger': '%s attack damage. Wind slash: 8 blocks, 7 damage, 0.4 s charge, 7 s cooldown.', 'frost191.staff_stats': 'Bolt %s HP / 0.8s; stream %s HP per 0.5s for 3s / 10s; 5 icicles at the last target within 8s / 20s; wave 4 HP / 16s.', 'item.slavicmyths.three_breath_crossbow.desc': 'Always three arrows: each deals %s base damage before armor. Shift+right-click: Mor (cloud and weakness), Flame (2 s burning), Storm (+3 damage, knockback and Slowness II for 2 s). Shift+left-click: fan / burst. No bottles.'}}

def apply():
    root=Path(__file__).resolve().parents[1]
    for locale,texts in FROST_STREAM_TEXTS.items():
        path=root/'src/main/resources/assets/slavicmyths/lang'/f'{locale}.json'
        data=json.loads(path.read_text(encoding='utf-8'))
        data.update({'frost191.'+key:value for key,value in texts.items()})
        data['gorynych196.storm_stats']=('Буря: +%s HP; радиус %s м; Замедление II на %s с; до %s целей. Повтор по цели раз в %s с.' if locale=='ru_ru' else 'Storm: +%s HP; %sm radius; Slowness II for %ss; up to %s targets. Repeat per target every %ss.')
        data.update(BALANCE_TEXTS[locale])
        path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

if __name__=='__main__': apply()
