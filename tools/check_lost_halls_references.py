"""Verify 2.0.4 original PNG identity, without extracting ZIPs or changing assets.

Accept folders, PNGs and ZIPs (multiple sources allowed). Filenames are not proof:
only the approved SHA256, dimensions and successful PNG decode pass SOURCE.
This is not an artistic comparison or a Minecraft runtime test.
"""
import argparse
import hashlib
import io
import json
from pathlib import Path
import warnings
import zipfile
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
EXPECTED = {
    'blocks': ('ff3b278772fe276a464c6a5c0da7592552a1e921e016e6c075ffe20cce01e781', (1448, 1086)),
    'scenes': ('d0d95085857c930a24d6ed6fc67729ae927a5103d77b536a1387ff8db79fe4b2', (1448, 1086)),
    'mobs': ('a330f127e60c4055dd86aadb91f169e337f4e60c9c90ad8e7982b49cac4be042', (1672, 941)),
    'rewards': ('0eb92eea714205ca75219af6aa9f9f1e586dae30494b6534ad28e412daff9856', (1536, 1024)),
    'posten': ('34e30e8cde56dffa7ab8e2ff0bf917da3ebdd808675e02da5cfeb123502d65d0', (1536, 1024)),
}
MAX_IMAGE_BYTES = 32 * 1024 * 1024
MAX_ARCHIVE_BYTES = 256 * 1024 * 1024


def inspect(sources, expected=EXPECTED):
    found = {name: [] for name in expected}
    errors, scanned = [], 0

    def check(data, location):
        nonlocal scanned
        scanned += 1
        digest = hashlib.sha256(data).hexdigest()
        matches = [key for key, (sha, _) in expected.items() if sha == digest]
        if not matches:
            return
        try:
            with warnings.catch_warnings():
                warnings.simplefilter('error', Image.DecompressionBombWarning)
                with Image.open(io.BytesIO(data)) as image:
                    size, format_ = image.size, image.format
                    image.verify()
                with Image.open(io.BytesIO(data)) as image:
                    image.load()
            for key in matches:
                if size != expected[key][1] or format_ != 'PNG':
                    raise ValueError('approved hash has wrong PNG dimensions/format')
                found[key].append(location)
        except (OSError, ValueError, Image.DecompressionBombError, Image.DecompressionBombWarning) as error:
            errors.append({'path': location, 'reason': str(error)})

    visited = set()
    for source in sources:
        source = Path(source).resolve()
        if not source.exists():
            errors.append({'path': str(source), 'reason': 'source does not exist'})
            continue
        paths = sorted(source.rglob('*')) if source.is_dir() else [source]
        for path in paths:
            if not path.is_file() or path.suffix.lower() not in ('.png', '.zip'):
                continue
            if path.resolve() in visited:
                continue
            visited.add(path.resolve())
            try:
                if path.suffix.lower() == '.png':
                    if path.stat().st_size > MAX_IMAGE_BYTES:
                        raise ValueError('PNG exceeds size limit')
                    check(path.read_bytes(), str(path))
                else:
                    with zipfile.ZipFile(path) as archive:
                        entries = [e for e in archive.infolist() if e.filename.lower().endswith('.png') and not e.is_dir()]
                        if sum(e.file_size for e in entries) > MAX_ARCHIVE_BYTES:
                            raise ValueError('ZIP PNG payload exceeds size limit')
                        for entry in entries:
                            if entry.file_size > MAX_IMAGE_BYTES:
                                raise ValueError('ZIP image exceeds size limit')
                            # No extraction: traversal names cannot write outside the report.
                            check(archive.read(entry), str(path) + '!' + entry.filename)
            except (OSError, ValueError, RuntimeError, zipfile.BadZipFile) as error:
                errors.append({'path': str(path), 'reason': str(error)})
    rows = [{'reference': key, 'sha256': sha, 'size': size, 'locations': found[key],
             'status': 'PASS' if found[key] else 'BLOCKED_REFERENCE'}
            for key, (sha, size) in expected.items()]
    return {'scope': 'SOURCE_IDENTITY_ONLY; ART and runtime NOT_RUN',
            'status': 'PASS' if all(found.values()) and not errors else 'BLOCKED_REFERENCE',
            'sources': [str(Path(s).resolve()) for s in sources],
            'pngs_scanned': scanned, 'references': rows, 'scan_errors': errors,
            'runtime_launches': 0,
            'heads_note': 'Guardian/Zharenitsa original references require their own contracts; these five PNGs do not pass the complete three-head ART gate.'}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('sources', nargs='*', type=Path)
    parser.add_argument('--report', type=Path, default=ROOT / 'docs/implementation/lost-halls-source-gate/evidence/references.json')
    args = parser.parse_args()
    result = inspect(args.sources or [ROOT / 'docs/concepts/lost-halls/approved_originals'])
    args.report.parent.mkdir(parents=True, exist_ok=True)
    args.report.write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(result['status'], 'PNG candidates:', result['pngs_scanned'], args.report.resolve())
    return 0 if result['status'] == 'PASS' else 1


if __name__ == '__main__':
    raise SystemExit(main())
