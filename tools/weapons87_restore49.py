"""Reproducible P49 restoration of eight resources rolled back by older generators."""
from pathlib import Path
import hashlib,json,shutil
R=Path(__file__).resolve().parents[1]
def apply():
    source=R/'tools/weapon-art-original/p49-restored'
    manifest=json.loads((source/'manifest.json').read_text(encoding='utf-8'))
    for relative,expected in manifest['files'].items():
        original=source/relative
        assert hashlib.sha256(original.read_bytes()).hexdigest()==expected,relative
        target=R/'src/main/resources'/relative
        target.parent.mkdir(parents=True,exist_ok=True)
        shutil.copyfile(original,target)
if __name__=='__main__':apply()
