"""Reproduce the scoped Cut data, localization and native 18px status icon. Idempotent."""
from pathlib import Path
import json, struct, zlib
ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'

def write(relative, data):
    path = RES / relative
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')

write('data/slavicmyths/damage_type/cut.json', {'message_id': 'slavic_cut', 'scaling': 'never', 'exhaustion': 0.0})
write('data/slavicmyths/tags/entity_type/bleed_immune.json', {'replace': False, 'values': [
    '#minecraft:undead', 'slavicmyths:nav', 'slavicmyths:kurgan_druzhinnik',
    'slavicmyths:kurgan_voevoda', 'slavicmyths:buried_volkhv', 'slavicmyths:unresting_prince', 'slavicmyths:upyr']})
for locale in ['ru_ru', 'en_us']:
    path = RES / f'assets/slavicmyths/lang/{locale}.json'
    data = json.loads(path.read_text(encoding='utf-8'))
    russian = locale == 'ru_ru'
    data['effect.slavicmyths.cut'] = 'Порез' if russian else 'Cut'
    data['death.attack.slavic_cut'] = '%1$s погибает от порезов' if russian else '%1$s succumbed to cuts'
    data['death.attack.slavic_cut.player'] = '%1$s погибает от порезов, нанесённых %2$s' if russian else '%1$s succumbed to cuts inflicted by %2$s'
    key = 'tooltip.slavicmyths.nightingale_dagger'
    separator = ' Порез:' if russian else ' Cut:'
    base = data.get(key, '').split(separator, 1)[0]
    data[key] = base + (' Порез: до 5 зарядов; каждое попадание обновляет общий таймер до 5 сек. 0,25 урона/сек. за заряд. Нежить невосприимчива.' if russian else ' Cut: up to 5 shared stacks; each hit refreshes all stacks to 5s. 0.25 damage/sec per stack. Undead are immune.')
    write(f'assets/slavicmyths/lang/{locale}.json', data)

def chunk(kind, data):
    return struct.pack('>I', len(data)) + kind + data + struct.pack('>I', zlib.crc32(kind + data) & 0xffffffff)

pixels = bytearray()
for y in range(18):
    pixels.append(0)
    for x in range(18):
        slash = any(x == base + (15-y)//3 and 3 <= y <= 14 for base in [4, 8, 12])
        pixels.extend((137, 32, 44, 255) if slash else (0, 0, 0, 0))
path = RES / 'assets/slavicmyths/textures/mob_effect/cut.png'
path.parent.mkdir(parents=True, exist_ok=True)
path.write_bytes(b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB',18,18,8,6,0,0,0)) + chunk(b'IDAT',zlib.compress(pixels)) + chunk(b'IEND', b''))
print('CUT_RESOURCES_PASS effectIcon=18x18 languages=2 entityTag=bleed_immune damageType=cut')
