"""Review native server receipts; fail on missing runtime evidence."""
import collections,hashlib,json,subprocess,zipfile,tomllib
from pathlib import Path
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/loot276-runtime-20261026'
def main():
    old=json.loads((O/'preflight.json').read_text(encoding='utf8'));created=json.loads((O/'create-results.json').read_text());restarted=json.loads((O/'restart-results.json').read_text())
    assert created['status']==restarted['status']=='PASS'
    assert 'slavicmyths-2.0.4-HELL.jar' in created['production_code_origin']
    assert len(created['native_table_statistics'])==30 and created['native_draws']==60000
    for site,n in [('camp_1',4),('camp_2',9),('camp_3',14),('fire',5),('spring',4),('fortress',11)]:assert created[site]['container_count']==n,(site,created[site])
    for name,v in created.items():
        if not name.startswith('swamp/'):continue
        template=name.split('/')[1];n=5 if 'cluster' in template else 1 if 'boardwalk' in template or template in ['ruin_0','ruin_2'] else 2
        assert v['container_count']==n,(name,v)
    for phase,d in [('create',created),('restart',restarted)]:assert all(c['result']=='PASS' for c in d['checks']),phase
    jar=Path(old['installed_jar']);assert hashlib.sha256(jar.read_bytes()).hexdigest()==old['sha256']
    installed=[]
    for candidate in jar.parent.glob('*.jar'):
        with zipfile.ZipFile(candidate) as z:
            for name in ['META-INF/neoforge.mods.toml','META-INF/mods.toml']:
                if name in z.namelist() and any(m.get('modId')=='slavicmyths' for m in tomllib.loads(z.read(name).decode()).get('mods',[])):installed.append(str(candidate))
    assert installed==[str(jar)],installed
    instance=jar.parent.parent
    for name,digest in old['original_saves'].items():assert hashlib.sha256((instance/name).read_bytes()).hexdigest()==digest,('original world changed',name)
    errors={};warnings={}
    for phase in ['create','restart']:
        lines=(O/(phase+'-gradle.log')).read_text(errors='replace').splitlines();errors[phase]=[l for l in lines if '/ERROR]' in l or 'Exception:' in l or 'Failed to parse' in l]
        warnings[phase]=[l for l in lines if '/WARN]' in l]
        assert not errors[phase],errors[phase]
    sites={k:v for k,v in created.items() if isinstance(v,dict) and 'container_count' in v}
    result=dict(status='PASS_NATIVE_INTEGRATION_OWNER_PENDING',jar=str(jar),sha256=old['sha256'],native_draws=created['native_draws'],native_stack_assertions=created['native_stack_assertions'],creation_checks=len(created['checks']),restart_checks=len(restarted['checks']),sites=sites,depths=created['depths'],errors=errors,warnings=warnings,original_level_dat_preserved=True,client_runs=0,network_mp='NOT_RUN; two native FakePlayer actors only',natural_generation='NOT_RUN; explicit actual piece placement on disposable flat world',production_changes=subprocess.check_output(['git','diff','HEAD','--name-only','--','src/main','gradle.properties','build.gradle'],cwd=R,text=True).splitlines())
    assert not result['production_changes'];result['installed_mod_jars']=installed
    (O/'verification.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf8');print('PASS native receipts',len(sites),'sites;',result['creation_checks'],'creation checks;',result['restart_checks'],'restart checks; original world preserved')
if __name__=='__main__':main()
