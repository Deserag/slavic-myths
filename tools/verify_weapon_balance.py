"""Issue #26 current source/resource/JAR gates; never claims client acceptance."""
from pathlib import Path
import argparse, hashlib, json, re, subprocess, zipfile

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'docs/implementation/legendary-weapon-balance'
BASE='fb2994e958911169317523e1066b8a1ca7a155af'
JAVA='src/main/java/org/slavicmyths/'
EXPECTED=dict(ATAMAN=12,NIGHTINGALE=12,LIKHO=13,TUGARIN=14,VICTOR=15,FROST_BOLT=16,FROST_PULSE=4,SCYTHE=20,KALIN=18,CROSSBOW=16,WHIP=24)
ALLOWED={JAVA+p for p in ('bandit/NightingaleDagger.java','combat/LikhoStaffItem.java','combat/RareWeapons.java',
    'equipment/mixin/GorynychArrowDamageMixin.java','equipment/mixin/GorynychStormArrowMixin.java',
    'gorynych/CrossbowDamage.java','gorynych/GorynychEquipment.java','gorynych/SerpentCrossbow.java',
    'karachun/NightCombat.java','karachun/NightScythe.java','morozko/FrostSkills.java','morozko/FrostStaff.java',
    'poloz/PolozWhip.java','rewards/RewardContent.java','combat/RewardWeaponBalance.java','combat/RewardWeaponTooltips.java')}
ALLOWED.update(f'src/main/resources/assets/slavicmyths/lang/{l}.json' for l in ('ru_ru','en_us'))

def read(p):return json.loads(p.read_text(encoding='utf-8-sig'))
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()

def source_checks():
    changed=subprocess.check_output(['git','diff','--name-only',BASE,'--','src/main'],cwd=ROOT,text=True).splitlines()
    assert set(changed)<=ALLOWED,'Unrelated content changed: '+str(set(changed)-ALLOWED)
    source=(ROOT/(JAVA+'combat/RewardWeaponBalance.java')).read_text(encoding='utf8')
    constants={k:int(v) for k,v in re.findall(r'\b([A-Z_]+)=(\d+)',source)}
    assert all(constants[k]==v for k,v in EXPECTED.items()),constants
    props=dict(line.split('=',1) for line in (ROOT/'gradle.properties').read_text().splitlines() if '=' in line)
    assert props['mod_version']=='1.9.7.6' and props['minecraft_version']=='1.21.1' and props['neo_version']=='21.1.255'
    for lang in ('ru_ru','en_us'):
        text=read(ROOT/f'src/main/resources/assets/slavicmyths/lang/{lang}.json')
        for key,count in [('tooltip.slavicmyths.reward_raw',1),('tooltip.slavicmyths.nightingale_dagger',1),('frost191.staff_stats',2),('item.slavicmyths.three_breath_crossbow.desc',1)]:
            assert text[key].count('%s')==count,(lang,key)
    from reward_weapon_texts import apply
    paths=[ROOT/f'src/main/resources/assets/slavicmyths/lang/{l}.json' for l in ('ru_ru','en_us')]
    before=[digest(p) for p in paths];apply();assert before==[digest(p) for p in paths],'Text overlay not idempotent'
    return len(changed)

def production_checks():
    from PIL import Image
    from check_event_subscribers import check
    jar=ROOT/'build/libs/slavicmyths-1.9.7.6.jar';checks=0
    with zipfile.ZipFile(jar) as z:
        assert not any('/verify/' in n or '/smoke/' in n or 'BalanceGameTests' in n for n in z.namelist())
        for p in (ROOT/'src/main/resources').rglob('*'):
            if not p.is_file() or p.name=='neoforge.mods.toml':continue
            assert z.read(p.relative_to(ROOT/'src/main/resources').as_posix())==p.read_bytes(),p
            if p.suffix=='.json':read(p)
            if p.suffix=='.png':
                with Image.open(p) as image:image.verify()
            checks+=1
        for p in (ROOT/'build/classes/java/main').rglob('*.class'):
            data=z.read(p.relative_to(ROOT/'build/classes/java/main').as_posix());assert data==p.read_bytes() and int.from_bytes(data[6:8],'big')==65,p;checks+=1
        metadata=z.read('META-INF/neoforge.mods.toml').decode();assert '1.9.7.6' in metadata and '21.1.255' in metadata and '[1.21.1]' in metadata
    assert 'BUILD SUCCESSFUL' in (OUT/'FINAL_BUILD.log').read_text(encoding='utf-8-sig')
    runtime=(OUT/'ACCEPTANCE_RUNTIME.log').read_text(encoding='utf-8-sig')
    assert 'All 20 required tests passed' in runtime and 'BUILD SUCCESSFUL' in runtime
    cases=re.findall(r'BALANCE_VOLLEY_PHYSICS case=(\d+).*?direct=\[16.0, 16.0, 16.0\]',runtime)
    assert set(map(int,cases))==set(range(12)),cases
    regressions=(OUT/'REGRESSIONS.log').read_text(encoding='utf-8-sig')
    assert 'All 37 required tests passed' in regressions and 'RUNE_REWORK_OFFLINE_CHECKS=398' in regressions and 'RARE_RUNE_OFFLINE_CHECKS=20491' in regressions
    return dict(status='PASS',version='1.9.7.6',jar=str(jar),sha256=digest(jar),checks=checks,event_subscribers=check(jar))

if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('--source-only',action='store_true');args=parser.parse_args()
    count=source_checks()
    if args.source_only:print('BALANCE_SOURCE_PASS changed_production_files='+str(count))
    else:
        result=production_checks();(OUT/'RESOURCE_JAR_CHECKS.json').write_text(json.dumps(result,indent=2)+'\n',encoding='utf8');print(json.dumps(result))
