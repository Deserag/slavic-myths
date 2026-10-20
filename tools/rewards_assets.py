"""Import approved 1.7.6 inventory assets without replacing existing resources."""
from pathlib import Path
import json, shutil, hashlib

ROOT=Path(__file__).resolve().parents[1]
SPEC=ROOT/'docs/specs/1.7.6'
RES=ROOT/'src/main/resources'
TEMPLATE=SPEC/'10_Ready_Resource_Templates'
def merge(path, incoming):
    data=json.loads(path.read_text(encoding='utf-8')) if path.exists() else {}
    for k,v in incoming.items():
        if k in data and data[k]!=v:
            raise ValueError(f'Conflicting existing resource: {path}: {k}')
        data[k]=v
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def run():
    for lang in ('ru_ru','en_us'):
        merge(RES/f'assets/slavicmyths/lang/{lang}.json',json.loads((TEMPLATE/f'assets/slavicmyths/lang/{lang}.json').read_text(encoding='utf-8')))
    for source in (TEMPLATE/'assets/slavicmyths/models/item').glob('*.json'):
        dest=RES/'assets/slavicmyths/models/item'/source.name
        dest.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(source,dest)
    for source in (SPEC/'07_Visual_References/Item_Textures_64').glob('*.png'):
        dest=RES/'assets/slavicmyths/textures/item'/source.name
        dest.parent.mkdir(parents=True,exist_ok=True)
        from boss_runes103 import write_owned
        if not write_owned(dest):shutil.copy2(source,dest)
    for source in (TEMPLATE/'data/slavicmyths/recipe').glob('*.json'):
        data=json.loads(source.read_text(encoding='utf-8'))
        # The supplied compact ingredient strings target a newer format. 1.21.1 uses objects.
        data['key']={k:({'item':v} if isinstance(v,str) else v) for k,v in data['key'].items()}
        dest=RES/'data/slavicmyths/recipe'/source.name
        dest.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    sounds={}
    for key,entry in json.loads((SPEC/'08_Audio/SOUND_MAP.json').read_text(encoding='utf-8')).items():
        source=SPEC/'08_Audio'/entry['ogg']
        dest=RES/'assets/slavicmyths/sounds/rewards176'/source.name
        dest.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(source,dest)
        event=entry['registration'].split(':')[1]
        sounds[event]={'subtitle':f'subtitles.slavicmyths.{event}','sounds':[f'slavicmyths:rewards176/{source.stem}']}
    merge(RES/'assets/slavicmyths/sounds.json',sounds)
if __name__=='__main__':
    run()
    import runpy
    runpy.run_path(str(ROOT/'tools/rewards_models.py'))
