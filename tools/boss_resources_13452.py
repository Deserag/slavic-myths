"""Current additive boss sound and weapon resources; original OGG files are reused, never replaced."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
def apply():
    res=ROOT/'src/main/resources/assets/slavicmyths'
    profiles=json.loads((ROOT/'tools/boss_sound_profiles_13452.json').read_text(encoding='utf-8'))
    p=res/'sounds.json';sounds=json.loads(p.read_text(encoding='utf-8'))
    for key,target in profiles.items():sounds[key]={'sounds':[{'name':target,'type':'event'}]}
    p.write_text(json.dumps(sounds,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    for locale,text in json.loads((ROOT/'tools/boss_weapon_text_13452.json').read_text(encoding='utf-8')).items():
        p=res/f'lang/{locale}.json';d=json.loads(p.read_text(encoding='utf-8'));d['tooltip.slavicmyths.nightingale_dagger']=text;p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    from boss_reimplementation_resources import apply as latest_boss_resources
    latest_boss_resources()
if __name__=='__main__':apply()
