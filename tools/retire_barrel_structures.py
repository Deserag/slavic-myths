"""Replace only decorative legacy barrels in authored templates; never reads saves.

Requires nbtlib. The normal village generators already emit vanilla barrel states.
Existing block-entity payloads would require manual review instead of blind conversion.
"""
from pathlib import Path
import gzip
import io
import nbtlib

ROOT = Path(__file__).resolve().parents[1]


def convert(path):
    doc = nbtlib.load(path)
    indices = {i for i, state in enumerate(doc.get('palette', []))
               if str(state.get('Name', '')) == 'slavicmyths:wooden_barrel'}
    if not indices:
        return False
    for block in doc['blocks']:
        if int(block['state']) in indices:
            assert 'nbt' not in block, f'Cannot discard legacy contents: {path}'
    for i in indices:
        state = doc['palette'][i]
        facing = state.get('Properties', {}).get('facing', nbtlib.String('north'))
        state['Name'] = nbtlib.String('minecraft:barrel')
        state['Properties'] = nbtlib.Compound({'facing': facing, 'open': nbtlib.String('false')})
    raw = io.BytesIO()
    doc.write(raw)
    path.write_bytes(gzip.compress(raw.getvalue(), mtime=0))
    assert nbtlib.load(path) == doc
    return True


if __name__ == '__main__':
    paths = (ROOT / 'src/main/resources/data/slavicmyths/structure').rglob('*.nbt')
    print('Converted decorative templates:', sum(convert(p) for p in paths))
