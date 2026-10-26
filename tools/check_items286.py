"""Scope/compatibility and pure rules gates for the current package; no generators."""
from pathlib import Path
import subprocess,json,zipfile,hashlib,re
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/items286-20261026';B=R/'build/items286-rules';B.mkdir(parents=True,exist_ok=True)
BASE='79ff212c447080a6c333cb85d5f0e24956d89101'
allowed={'src/main/java/org/slavicmyths/farming/WateringCanItem.java','src/main/java/org/slavicmyths/yaga/YagaUtilityItem.java'}
def git(*args):return subprocess.check_output(['git',*args],cwd=R)
def main():
    checks=[]
    def check(ok,label):
        assert ok,label
        checks.append(label)
    check(git('branch','--show-current').decode().strip()=='hell','working branch hell')
    check(set(git('diff',BASE,'--name-only','--','src/main').decode().splitlines())==allowed,'production delta exactly two existing handlers; all resources/recipes/loot/registry unchanged')
    check(not git('ls-files','--others','--exclude-standard','--','src/main').strip(),'no generated/untracked production resources')
    catalog=json.loads((O/'summary.json').read_text(encoding='utf8'))
    check(catalog['catalog']==923 and catalog['candidate441']==441,'full catalog triage 923/441')
    check(not catalog['unresolved_dynamic_registration'],'all detailed entries have literal or dynamic family source evidence')
    for file in ['registry/ModItems.java','item/ItemState.java','kurgan/KurganCreature.java','equipment/UniqueEquipment.java','equipment/UniqueEquipmentEvents.java','equipment/UniqueEquipmentRules.java','mor/MorFlaskItem.java','quest185/Quest185Item.java','sanctuary/SanctuaryItem.java','item/PassiveCharm.java','item/StormProtection.java','industry/ChudTools.java','industry/ChudPerson.java','village/VillageTrades.java','yaga/YagaServices.java','brewing/BeverageItem.java']:
        path='src/main/java/org/slavicmyths/'+file
        check((R/path).read_bytes().replace(b'\r\n',b'\n')==git('show',BASE+':'+path).replace(b'\r\n',b'\n'),'unchanged protected source '+file)
    # Source rules compiled normally, without any Minecraft dependency or bootstrap.
    fixture=B/'Rules286.java';fixture.write_text('''import java.util.*;
import org.slavicmyths.brewing.BrewRules;import org.slavicmyths.industry.IndustryRules;
class Rules286 {static int n;static void test(boolean ok){n++;if(!ok)throw new AssertionError("rule "+n);}public static void main(String[]args){
int[] ages={0,2399,2400,8399,8400,11999,12000,99999};String[] q={"young","young","ready","ready","aged","aged","spoiled","spoiled"};for(int i=0;i<ages.length;i++)test(BrewRules.quality(ages[i]).equals(q[i]));
test(BrewRules.duration(45,"young")==22);test(BrewRules.duration(8,"aged")==10);test(BrewRules.duration(45,"aged")==54);test(BrewRules.duration(90,"spoiled")==0);
for(int p=0;p<=5;p++)for(int e:new int[]{0,1199,1200,2399,2400,6000})test(BrewRules.decay(p,100,100+e)==Math.max(0,p-e/1200));test(BrewRules.decay(5,100,99)==5);
for(long day:new long[]{0,24000,48000}){test(IndustryRules.restockPeriod(day+1999)==0);test(IndustryRules.restockPeriod(day+2000)==1);test(IndustryRules.restockPeriod(day+13999)==1);test(IndustryRules.restockPeriod(day+14000)==2);test(IndustryRules.restockPeriod(day+23999)==2);}
test(IndustryRules.fortune(3,true,false)==4);test(IndustryRules.fortune(3,true,true)==3);test(!IndustryRules.canFinish(true,true,1,0,0,64,true));test(!IndustryRules.canFinish(false,true,1,0,64,64,true));test(!IndustryRules.canFinish(false,true,1,0,0,64,false));test(IndustryRules.canFinish(true,true,1,1,63,64,true));
System.out.println("PASS actual BrewRules/IndustryRules checks="+n+"; Minecraft=0");}}
''',encoding='utf8')
    bin=Path('C:/Program Files/Java/jdk-21.0.12/bin');sources=[fixture,R/'src/main/java/org/slavicmyths/brewing/BrewRules.java',R/'src/main/java/org/slavicmyths/industry/IndustryRules.java']
    subprocess.run([str(bin/'javac.exe'),'-d',str(B),*map(str,sources)],check=True)
    result=subprocess.run([str(bin/'java.exe'),'-cp',str(B),'Rules286'],capture_output=True,text=True,check=True);print(result.stdout);(O/'tests/rules.txt').write_text(result.stdout,encoding='utf8')
    version=re.search(r'^mod_version=(.+)$',(R/'gradle.properties').read_text(),re.M)[1].strip();jar=R/f'build/libs/slavicmyths-{version}.jar'
    baseline=Path('D:/slavic-myths-baselines/2.0.1-HELL-79ff212c-20261026/slavicmyths-2.0.1-HELL.jar')
    with zipfile.ZipFile(jar) as z,zipfile.ZipFile(baseline) as old:
        names=set(z.namelist());oldnames=set(old.namelist());check(names==oldnames,'same production JAR entry IDs/count; no new systems')
        delta=[name for name in sorted(names) if z.read(name)!=old.read(name)]
        expected={'META-INF/MANIFEST.MF','META-INF/neoforge.mods.toml','org/slavicmyths/farming/WateringCanItem.class','org/slavicmyths/yaga/YagaUtilityItem.class'}
        check(set(delta)<=expected,'all #277/#278/#284/#287 classes and all resources byte-identical to baseline')
        check(expected-{'META-INF/MANIFEST.MF'}<=set(delta),'new version and both fixes packaged')
        check(not any('verify/' in name or 'Loot276Plans' in name or 'Fixture' in name or 'Rules286' in name for name in names),'offline fixtures absent from JAR')
    report=dict(source_baseline=BASE,version=version,jar=str(jar),sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),checks=checks,jar_delta=delta,minecraft_launches=0,runtime='NOT_RUN / OWNER_PENDING',rules_checks=70)
    (O/'source-jar.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf8');print('PASS scope/package checks='+str(len(checks)))
if __name__=='__main__':main()
