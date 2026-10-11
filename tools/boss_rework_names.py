"""Rename the cursed boss's visible text; retain saved IDs and the separate hunt mob."""
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]

def boss_key(key):
    return key in ('entity.slavicmyths.cursed_volkolak',
                   'item.slavicmyths.cursed_volkolak_spawn_egg',
                   'wolfstory.slavicmyths.title.3') or key.startswith((
                       'volkolak.slavicmyths.', 'subtitles.slavicmyths.volkolak.',
                       'wolfstory.slavicmyths.dialog.3.',
                       'wolfstory.slavicmyths.dialog.handed.3',
                       'wolfstory.slavicmyths.objective.3.')) or key == 'wolfstory.slavicmyths.volkolak_escaped'

def rename(value):
    return value.replace('Волколака', 'Проклятого Оборотня').replace(
        'Волколак', 'Проклятый Оборотень').replace(
        'Проклятый Проклятый', 'Проклятый').replace(
        'Cursed Volkolak', 'Cursed Werewolf').replace('Volkolak', 'Cursed Werewolf')

def update(data):
    for key, value in data.items():
        if isinstance(value, dict):
            update(value)
        elif isinstance(value, str) and boss_key(key):
            data[key] = rename(value)

if __name__ == '__main__':
    for path in [ROOT / f'src/main/resources/assets/slavicmyths/lang/{lang}.json'
                 for lang in ('ru_ru', 'en_us')] + [
                     ROOT / 'tools/volkolak_143_text.json',
                     ROOT / 'tools/grey_wolf_142_text.json']:
        data = json.loads(path.read_text(encoding='utf-8'))
        update(data)
        path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
