"""ApprovedB exact production delta and preservation gates, independent of runtime."""
import hashlib,json,zipfile
from pathlib import Path
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/weapons87'

def main():
 archive=zipfile.ZipFile(O/'before-balance-b.zip');normalize=lambda b:b.decode('utf-8').replace('\r\n','\n');mod='src/main/java/org/slavicmyths/registry/ModItems.java';staff='src/main/java/org/slavicmyths/item/StormStaffItem.java'
 old=normalize(archive.read(mod));needle='new org.slavicmyths.combat.MaceItem(ModGear.PERUNITE, 9.5F, .6F, .4F, .4F, properties().rarity(Rarity.RARE))';assert old.count(needle)==1;assert (R/mod).read_text(encoding='utf-8')==old.replace(needle,needle.replace('.6F','.8F')),'Unapproved registration delta'
 old=normalize(archive.read(staff));members='''    public static final float BASE_PULSE_DAMAGE=6.0F;
    public static final int PULSE_COOLDOWN=60;
    public static final int RUNE_COOLDOWN=54;
    public static float pulseDamage(boolean staffPower,boolean thunderRune) {
        return BASE_PULSE_DAMAGE*(staffPower?1.12F:1)*(thunderRune?1.15F:1);
    }
    public static int cooldownTicks(boolean activeRune) { return activeRune?RUNE_COOLDOWN:PULSE_COOLDOWN; }
'''
 old=old.replace('public final class StormStaffItem extends Item {\n','public final class StormStaffItem extends Item {\n'+members)
 old=old.replace('3.0F*(org.slavicmyths.rpg.PathData.has(player,"staff_power")?1.12F:1)*(org.slavicmyths.rpg.Runes.has(stack,"thunder")?1.15F:1)','pulseDamage(org.slavicmyths.rpg.PathData.has(player,"staff_power"),org.slavicmyths.rpg.Runes.has(stack,"thunder"))')
 old=old.replace('((org.slavicmyths.rpg.Runes.has(stack,"midday") && org.slavicmyths.rpg.RuneEffects.day(player)) || (org.slavicmyths.rpg.Runes.has(stack,"shadow") && org.slavicmyths.rpg.RuneEffects.dark(player)))?72:80','cooldownTicks((org.slavicmyths.rpg.Runes.has(stack,"midday") && org.slavicmyths.rpg.RuneEffects.day(player)) || (org.slavicmyths.rpg.Runes.has(stack,"shadow") && org.slavicmyths.rpg.RuneEffects.dark(player)))')
 import difflib
 assert (R/staff).read_text(encoding='utf-8')==old,'Staff changed beyond approved pulse/CD/helper extraction\n'+''.join(difflib.unified_diff(old.splitlines(True),(R/staff).read_text(encoding='utf-8').splitlines(True)))
 from weapons87_balance_b import TEXT
 changes=[]
 for lang,updates in TEXT.items():
  path='src/main/resources/assets/slavicmyths/lang/'+lang+'.json';before=json.loads(archive.read(path));after=json.loads((R/path).read_text(encoding='utf-8'));actual={k for k in before if before[k]!=after.get(k)};assert actual==set(updates) and set(before)==set(after);assert all(after[k]==v for k,v in updates.items());changes.append({'path':path,'changed_keys':sorted(actual),'all_other_keys_unchanged':True})
 prior=json.loads((O/'source-manifest-package11.json').read_text());delta=[p for p,digest in prior.items() if p.startswith('src/') and hashlib.sha256((R/p).read_bytes()).hexdigest()!=digest];expected={mod,staff}|{x['path'] for x in changes};assert set(delta)==expected,'Unapproved production delta from frozen package11';assert not {p.relative_to(R).as_posix() for p in (R/'src').rglob('*') if p.is_file()}-{p for p in prior if p.startswith('src/')},'Unexpected added production file'
 result={'approval':{'package':'B','owner_exact_answer':'Согласовать пакет B','date':'2026-10-22','source':'explicit user answer in this chat; no owner-accepted label added'},'production_delta':delta,'language_changes':changes,'registration_delta':'perunite_mace speed.6→.8 only; damage9.5/pressure.4/reach/durability unchanged','staff_delta':'base magic3→6,CD80/72→60/54 only, same multipliers/target filters/range/FX/durability','remaining_C_D_190':'AWAITING_OWNER_NO_CHANGES','runtime':'NOT_RUN / OWNER_PENDING'};(O/'balance-b-source-checks.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print('PASS:exact4 production-file delta from frozen11; registration speed only, staff pulse/CD only;4 RU/EN tooltip values only; all other production/art preserved')

if __name__=='__main__':main()
