"""Record a stable CURRENT checkout before an isolated clean build; preserve concurrent edits."""
from pathlib import Path
import hashlib,json

ROOT=Path(__file__).resolve().parents[1]
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
own={
    'src/main/java/org/slavicmyths/registry/ModItemGroup.java',
    'src/main/java/org/slavicmyths/registry/CreativeCatalog.java',
    'src/main/resources/assets/slavicmyths/creative_catalog.tsv',
    'src/main/resources/assets/slavicmyths/lang/ru_ru.json',
    'src/main/resources/assets/slavicmyths/lang/en_us.json',
    'tools/gardens_resources.py','tools/verify_gardens.py',
    'tools/creative_tab_names.py','tools/creative_source_inventory.py',
    'tools/creative-tests/CreativeCatalogChecks.java','tools/verify_creative_inventory.py',
    'tools/install_creative_inventory.ps1','tools/creative-build.init.gradle','tools/creative_checkpoint.py',
}
before=json.loads((ROOT/'.tools/creative-before/hashes.json').read_text(encoding='utf-8'))
current={p.relative_to(ROOT).as_posix():sha(p) for base in ['src/main','tools'] for p in (ROOT/base).rglob('*') if p.is_file() and '__pycache__' not in p.parts}
concurrent=sorted(p for p,h in current.items() if p not in own and before.get(p)!=h)
(ROOT/'.tools/creative-concurrent-files.json').write_text(json.dumps(concurrent,indent=2),encoding='utf-8')
inputs={p:h for p,h in current.items() if p.startswith('src/main/')}
inputs['gradle.properties']=sha(ROOT/'gradle.properties')
(ROOT/'.tools/creative-build-inputs.json').write_text(json.dumps(inputs,indent=2),encoding='utf-8')
locales={}
for lang in ['ru_ru','en_us']:
    old=json.loads((ROOT/f'.tools/creative-before/{lang}.json').read_text(encoding='utf-8'))
    new=json.loads((ROOT/f'src/main/resources/assets/slavicmyths/lang/{lang}.json').read_text(encoding='utf-8'))
    locales[lang]={k:{'before':old.get(k),'after':new.get(k)} for k in sorted(old.keys()|new.keys()) if old.get(k)!=new.get(k) and not k.startswith('itemGroup.slavicmyths.')}
(ROOT/'.tools/creative-parallel-locales.json').write_text(json.dumps(locales,ensure_ascii=False,indent=2),encoding='utf-8')
OUT=ROOT/'docs/implementation/creative-inventory'
(OUT/'PARALLEL_CHANGES.json').write_text(json.dumps({'files':concurrent,'localization':locales,'note':'Observed edits by the separate active weapon-fix chat, preserved in the shared checkout. Not creative-inventory changes.'},ensure_ascii=False,indent=2),encoding='utf-8')
print('CHECKPOINT production_inputs=',len(inputs),'preserved_concurrent_files=',len(concurrent))
