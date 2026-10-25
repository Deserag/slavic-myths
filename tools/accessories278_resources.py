"""Scoped current passive-charm overlay. No legacy audio or full resource generator."""
from pathlib import Path
import json

IDS = ('amber_charm', 'perun_charm', 'forest_charm', 'traveler_charm')
LABELS = {
    'ru_ru': dict(zip(IDS, [
        'Слот Curios: оберег, либо в левой руке (без удвоения): −15% урона от огня.',
        'Слот Curios: оберег, либо в левой руке (без удвоения): −30% урона от молнии.',
        'Слот Curios: оберег, либо в левой руке (без удвоения): −15% урона от лесных духов.',
        'Слот Curios: оберег, либо в левой руке (без удвоения): −15% урона от падения.'])),
    'en_us': dict(zip(IDS, [
        'Curios charm slot or offhand (once only): 15% less fire damage.',
        'Curios charm slot or offhand (once only): 30% less lightning damage.',
        'Curios charm slot or offhand (once only): 15% less damage from forest spirits.',
        'Curios charm slot or offhand (once only): 15% less fall damage.']))
}
TEXT = {
    'ru_ru': {
        'book.slavicmyths.amber.text': 'Янтарь — редкая находка в старых капищах. Янтарный оберег защищает от огня в слоте Curios «оберег» либо в левой руке. Две копии не удваивают защиту.',
        'book.slavicmyths.charms.text': 'Оберег Перуна смягчает урон молнии, янтарный — урон огня. Наденьте оберег в слот Curios «оберег», оставив левую руку для щита, либо держите в левой руке. Эффект одного оберега не удваивается. Это игровые свойства, не историческое утверждение.',
        'skill.slavicmyths.charm_power.desc': 'Защитные обереги в слоте Curios «оберег» либо в левой руке: снижение соответствующего урона 20% вместо 15%; оберег Перуна: 35% вместо 30%. Без удвоения.',
        'tooltip.slavicmyths.hunter_charm': 'ПКМ: обнаружение ближайшей охотничьей цели. Восстановление: 20 с. Не пассивный аксессуар.'
    },
    'en_us': {
        'book.slavicmyths.amber.text': 'Amber is a rare shrine find. Its charm protects against fire in the Curios charm slot or offhand. Two copies do not double the protection.',
        'book.slavicmyths.charms.text': 'The Perun Charm reduces lightning damage; the amber charm reduces fire damage. Equip one in the Curios charm slot to leave your offhand free for a shield, or hold it in the offhand. The same charm never applies twice. These are game rules, not historical claims.',
        'skill.slavicmyths.charm_power.desc': 'Protective charms in the Curios charm slot or offhand: corresponding damage reduction 20% instead of 15%; Perun charm: 35% instead of 30%. No doubling.',
        'tooltip.slavicmyths.hunter_charm': 'Right-click: detect a nearby hunt target. Cooldown: 20 s. Not a passive accessory.'
    }
}

def apply(root=None):
    root = Path(root) if root is not None else Path(__file__).resolve().parents[1]
    for lang, entries in LABELS.items():
        p = root / f'src/main/resources/assets/slavicmyths/lang/{lang}.json'
        data = json.loads(p.read_text(encoding='utf8'))
        data.update({'tooltip.slavicmyths.' + key: value for key, value in entries.items()})
        data.update(TEXT[lang])
        p.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n', encoding='utf8')
    p = root / 'src/main/resources/data/curios/tags/item/charm.json'
    data = json.loads(p.read_text(encoding='utf8'))
    for item in IDS:
        if 'slavicmyths:' + item not in data['values']:
            data['values'].append('slavicmyths:' + item)
    p.write_text(json.dumps(data, indent=2) + '\n', encoding='utf8')

if __name__ == '__main__':
    apply()
