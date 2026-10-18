"""Restore the exact owner-approved PNGs; no drawing, conversion or resampling."""
from pathlib import Path
import hashlib
import io
import json
import zipfile
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT/'art/kurgan-16-approved'
V2_ARCHIVE_SHA256 = '98a01d34281371c40edfb5928a37f2538fbae78e140fa97bd148b348a9ae457b'


def install():
    manifest = json.loads((SOURCE/'manifest.json').read_text('utf8'))
    archive = SOURCE/'approved.zip'
    assert manifest['art_revision'] == 'V2'
    assert hashlib.sha256(archive.read_bytes()).hexdigest() == manifest['archive_sha256'] == V2_ARCHIVE_SHA256
    with zipfile.ZipFile(archive) as z:
        assert len(z.infolist()) == 12 and z.testzip() is None
        assert set(z.namelist()) == {name+'.png' for name in manifest['textures']} | {'manifest.json'}
        embedded = json.loads(z.read('manifest.json'))
        assert embedded['version'] == 'kurgan-v2-art-redesign' and embedded['resolution'] == [16,16]
        assert len(manifest['textures']) == 11
        assert set(embedded['files']) == {name+'.png' for name in manifest['textures']}
        for name, entry in manifest['textures'].items():
            data = z.read(name+'.png')
            assert hashlib.sha256(data).hexdigest() == entry['sha256'] == embedded['files'][name+'.png']['sha256']
            im = Image.open(io.BytesIO(data)); im.verify()
            assert im.size == (16,16) and im.mode == 'RGB'
        for name in manifest['textures']:
            (ROOT/f'src/main/resources/assets/slavicmyths/textures/block/{name}.png').write_bytes(z.read(name+'.png'))
    print('Installed exact owner-supplied V2 PNG bytes: 11/11')
    return frozenset(manifest['textures'])


if __name__ == '__main__':
    install()
