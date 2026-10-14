"""Canonical tab labels shared by resource generators; changes only these localization keys."""
from pathlib import Path
import json

TAB_NAMES = {
    'slavicmyths': ('Основное', 'Main'),
    'resources': ('Материалы и трофеи', 'Materials & Trophies'),
    'farming': ('Растения и земледелие', 'Plants & Farming'),
    'food': ('Еда и напитки', 'Food & Drinks'),
    'weapons': ('Оружие', 'Weapons'),
    'tools': ('Инструменты', 'Tools'),
    'armor': ('Броня и снаряжение', 'Armor & Equipment'),
    'decor': ('Строительство и декор', 'Building & Decoration'),
    'magic': ('Магия и ритуалы', 'Magic & Rituals'),
    'mobs': ('Существа', 'Creatures'),
}

def apply_tab_names(data, lang):
    for tab, names in TAB_NAMES.items():
        data['itemGroup.slavicmyths.' + tab] = names[0 if lang == 'ru_ru' else 1]

if __name__ == '__main__':
    root = Path(__file__).resolve().parents[1]
    for lang in ('ru_ru', 'en_us'):
        path = root / f'src/main/resources/assets/slavicmyths/lang/{lang}.json'
        data = json.loads(path.read_text(encoding='utf-8'))
        apply_tab_names(data, lang)
        path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
