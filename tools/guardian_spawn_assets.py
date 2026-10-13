"""Reproducible creative spawn egg assets for the two implemented guardians."""
import json
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/slavicmyths'
EGGS = {
    'extinguished_flame_guardian_spawn_egg': ('Яйцо призыва Стража угасшего пламени', 'Extinguished Flame Guardian Spawn Egg'),
    'defiled_spring_keeper_spawn_egg': ('Яйцо призыва Хранителя осквернённого родника', 'Defiled Spring Keeper Spawn Egg'),
}
def generate():
    for name in EGGS:
        (ROOT / f'models/item/{name}.json').write_text(json.dumps({'parent': 'minecraft:item/template_spawn_egg'}, indent=2)+'\n', encoding='utf-8')
    for index, locale in enumerate(('ru_ru', 'en_us')):
        path = ROOT / f'lang/{locale}.json'
        data = json.loads(path.read_text(encoding='utf-8-sig'))
        for name, labels in EGGS.items():
            data[f'item.slavicmyths.{name}'] = labels[index]
        path.write_text(json.dumps(data, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
if __name__ == '__main__':
    generate()
