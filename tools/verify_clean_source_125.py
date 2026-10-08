"""Compare independently rebuilt production contents, ignoring ZIP timestamps."""
from pathlib import Path
import hashlib,json,zipfile

ROOT=Path(__file__).resolve().parents[1]
SNAPSHOT=Path('D:/slavic-myths-clean-source-125-20261008')
original=ROOT/'build/libs/slavicmyths-1.2.5.jar'
rebuilt=SNAPSHOT/'build/libs/slavicmyths-1.2.5.jar'
assert not (SNAPSHOT/'.tools').exists()
with zipfile.ZipFile(original) as a,zipfile.ZipFile(rebuilt) as b:
    assert set(a.namelist())==set(b.namelist())
    changed=[name for name in a.namelist() if a.read(name)!=b.read(name)]
    assert not changed,changed
    count=len(a.namelist())
report={'kind':'working source export, NOT remote release clone','source_directory':str(SNAPSHOT),
        'fresh_gradle_cache':'D:/slavic-myths-clean-gradle-125-20261008','local_tools_present':False,
        'build':'PASS','entries_compared':count,'entry_byte_differences':0,
        'original_sha256':hashlib.sha256(original.read_bytes()).hexdigest(),
        'rebuilt_sha256':hashlib.sha256(rebuilt.read_bytes()).hexdigest(),
        'remote_release_clone':'NOT RUN: no authorized release commit/branch yet'}
(ROOT/'docs/maintenance/clean-source-verification-1.2.5.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
print(json.dumps(report))
