"""#262 source gate: validate exact originals from a folder or ZIP, without extraction.

Usage: python tools/check_black_cinder_references.py [archive.zip|approved_refs]
Exit 0 only when every required original passes SHA256, dimensions and PNG decode.
Never writes production resources or accepts an SVG/thumbnail as approved art.
"""
import argparse
import hashlib
import io
import json
from pathlib import Path, PurePosixPath
import zipfile
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
EXPECTED = {
    'landscape.png': ('09a92d889d1dcabc2821d872e2287e6400f00bf01baf8674d32323d3bdb3bce8', (1672, 941)),
    'blocks_REVISED.png': ('8697b9bd400c7790065bfa243db65d02d7468d19f873b9929a09ba54e47c5e0a', (1448, 1086)),
    'mobs.png': ('0210bc96b2b4ff72b8ded59a5a0a2b1ba75f0e980974ec14a86f2ad090b29a81', (1448, 1086)),
    'zharenitsa.png': ('ad805df5f2c2c3e2ee3e2ab0ff86c5103464a54c662d3c2f2e820ed96f4d874e', (1312, 1199)),
}

def validate(source):
    rows = []
    archive = zipfile.ZipFile(source) if source.is_file() else None
    try:
        for name, (expected_hash, size) in EXPECTED.items():
            row = {'name': name, 'expected_sha256': expected_hash, 'expected_size': size}
            if archive:
                matches = [entry for entry in archive.infolist()
                           if PurePosixPath(entry.filename).name == name
                           and 'approved_refs' in PurePosixPath(entry.filename).parts]
                if len(matches) != 1:
                    rows.append(dict(row, status='BLOCKED_REFERENCE', reason=f'exactly one approved_refs/{name} required'))
                    continue
                if matches[0].file_size > 32 * 1024 * 1024:
                    rows.append(dict(row, status='FAIL', reason='image exceeds 32MiB limit'))
                    continue
                data = archive.read(matches[0])
            else:
                path = source / name
                if not path.is_file():
                    rows.append(dict(row, status='BLOCKED_REFERENCE', reason='missing original'))
                    continue
                data = path.read_bytes()
            row['actual_sha256'] = hashlib.sha256(data).hexdigest()
            try:
                with Image.open(io.BytesIO(data)) as image:
                    row['actual_size'] = image.size
                    row['format'] = image.format
                    image.verify()
                row['status'] = 'PASS' if (row['actual_sha256'] == expected_hash
                    and row['actual_size'] == size and row['format'] == 'PNG') else 'FAIL'
            except (OSError, ValueError) as error:
                row.update(status='FAIL', reason=str(error))
            rows.append(row)
    finally:
        if archive:
            archive.close()
    return rows

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('source', nargs='?', type=Path,
                        default=ROOT / 'docs/concepts/black-cinder/approved_refs')
    parser.add_argument('--report', type=Path,
                        default=ROOT / 'docs/implementation/black-cinder262-20261027/reference-gate.json')
    args = parser.parse_args()
    rows = validate(args.source)
    passed = all(row['status'] == 'PASS' for row in rows)
    status = 'PASS' if passed else 'FAIL_REFERENCE' if any(row['status'] == 'FAIL' for row in rows) else 'BLOCKED_REFERENCE'
    report = {'scope': 'SOURCE IDENTITY ONLY; not ART/runtime acceptance',
              'source': str(args.source.resolve()), 'status': status,
              'references': rows, 'runtime_runs': 0}
    args.report.parent.mkdir(parents=True, exist_ok=True)
    args.report.write_text(json.dumps(report, indent=2)+'\n', encoding='utf-8')
    print(report['status'], str(args.report.resolve()))
    return 0 if passed else 1

if __name__ == '__main__':
    raise SystemExit(main())
