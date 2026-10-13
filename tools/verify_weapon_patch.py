"""Production/resource gates; deliberately does not claim gameplay acceptance."""
from pathlib import Path
import csv, hashlib, json, math, zipfile
from PIL import Image
from check_event_subscribers import check as subscribers
from weapon_patch_assets import main as generate

R=Path(__file__).resolve().parents[1]
O=R/'docs/verification/weapon-patch'
J=R/'build/libs/slavicmyths-1.9.7.jar'
checks=0
def need(ok, label):
    global checks
    checks+=1
    if not ok: raise AssertionError(label)
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
    O.mkdir(parents=True,exist_ok=True)
    log=(R/'.tools/weapon-patch-clean-build.log').read_text(encoding='utf-8',errors='replace')
    need('BUILD SUCCESSFUL' in log,'clean build')
    need('NIGHT_OFFLINE_PASS' in log,'native boss regression')
    assets=R/'src/main/resources'
    before={p.relative_to(assets).as_posix():sha(p) for p in assets.rglob('*') if p.is_file()}
    generate()
    need(before=={p.relative_to(assets).as_posix():sha(p) for p in assets.rglob('*') if p.is_file()},'patch generator reproduces exactly')
    rows=[]
    with zipfile.ZipFile(J) as z:
        names=set(z.namelist())
        need('META-INF/neoforge.mods.toml' in names,'NeoForge production metadata')
        need(not any(n.startswith(('tools/','smoke/')) or 'NightChecks' in n for n in names),'test exclusion')
        for n,h in before.items():
            if n == "META-INF/neoforge.mods.toml":continue
            need(n in names and hashlib.sha256(z.read(n)).hexdigest()==h,'exact packaged resource '+n)
        for n in names:
            if n.endswith('.json'):
                d=json.loads(z.read(n));need(isinstance(d,(dict,list)),'JSON '+n)
                if n.startswith('assets/slavicmyths/models/item/'):
                    rows.append({'model':Path(n).stem,'parent':d.get('parent','geometry'),'cuboids':len(d.get('elements',[])),'contexts':','.join(d.get('display',{})),'acceptance':'STATIC_ONLY; GAME_NOT_RUN'})
                    for e in d.get('elements',[]):
                        need(all(math.isfinite(v) and -16<=v<=32 for k in ['from','to'] for v in e[k]),'model coordinate bounds '+n)
                        need(all(a<=b for a,b in zip(e['from'],e['to'])),'model extents '+n)
            if n.endswith('.png'):
                import io
                im=Image.open(io.BytesIO(z.read(n)));im.verify();need(True,'PNG '+n)
        base='assets/slavicmyths/models/item/'
        held=json.loads(z.read(base+'karachun_scythe_held.json'))
        need(len(held['elements'])==8,'compact scythe')
        need(all(e['to'][2]>e['from'][2] for e in held['elements']),'solid blade thickness')
        for name in ['karachun_scythe_held','victor_spear','likho_staff','ataman_flail','trophy_kalin_sabre','serpent_crossbow','three_breath_crossbow']:
            m=json.loads(z.read(base+name+'.json'))
            for context in ['firstperson_righthand','firstperson_lefthand','thirdperson_righthand','thirdperson_lefthand','gui','ground','fixed','head']:
                need(context in m['display'],'complete contexts '+name+' '+context)
        for lang in ['ru_ru','en_us']:
            m=json.loads(z.read('assets/slavicmyths/lang/'+lang+'.json'))
            for name in ['karachun_scythe','victor_spear','likho_staff','ataman_flail','trophy_kalin_sabre','serpent_crossbow','three_breath_crossbow']:
                need('item.slavicmyths.'+name in m,'localized item '+name)
            need('hit' not in m['night192.scythe_passives'] if lang=='ru_ru' else True,'Russian terminology')
    count=subscribers(J)
    with (O/'ITEM_MODELS.csv').open('w',encoding='utf-8-sig',newline='') as f:
        w=csv.DictWriter(f,fieldnames=list(rows[0]));w.writeheader();w.writerows(sorted(rows,key=lambda r:r['model']))
    result={'status':'PASS','patch':'Патч оружия','version_unchanged':'1.9.7','jar':str(J),'sha256':sha(J),'checks':checks,'item_models_audited':len(rows),'subscribers':count,'client_starts':0,'server_starts':0,'gameplay':'NOT_RUN','visual_acceptance':'REQUIRES_MANUAL_QA'}
    (O/'resource-gates.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(result,ensure_ascii=False))
if __name__=='__main__':main()
