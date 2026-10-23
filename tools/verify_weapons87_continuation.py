"""Current P53 scope/preservation, actual native evidence and production JAR gate."""
from pathlib import Path
import hashlib,json,sys,zipfile
import weapons87_thrown_family as family
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/weapons87/continuation53';RES=R/'src/main/resources'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
    baseline=json.loads((O/'production-baseline.json').read_text());current={p.relative_to(R).as_posix():sha(p) for p in (R/'src/main').rglob('*') if p.is_file()};current['gradle.properties']=sha(R/'gradle.properties')
    java=['client/PlayerWeaponPose','client/ThirdPersonWeaponMotion','client/WeaponProjectileRenderer','client/WhipClient','client/WhipMotion','combat/WeaponDefaults','equipment/mixin/PlayerWeaponPoseMixin','item/StormStaffItem','combat/LikhoStaffItem','artifact/VelesStaffItem']
    allowed={'src/main/java/org/slavicmyths/'+x+'.java' for x in java}|{'src/main/resources/slavicmyths.equipment.mixins.json','src/main/resources/data/slavicmyths/recipe/weapon_veles_staff_assembly.json','gradle.properties'}
    allowed|={'src/main/resources/assets/slavicmyths/'+p for p in family.models()}
    allowed.add('src/main/resources/assets/slavicmyths/models/item/spear.json')
    changed={p for p in set(baseline)|set(current) if baseline.get(p)!=current.get(p)}
    assert changed<=allowed,changed-allowed
    assert not set(baseline)-set(current),'production files deleted'
    assert all(current[p]==h for p,h in baseline.items() if p.endswith('.png')),'art outside held models changed'
    spear_before=json.loads((O/'spear-before.json').read_text());spear_after=json.loads((RES/'assets/slavicmyths/models/item/spear.json').read_text())
    for index in (6,9,12,15,18):spear_before['elements'][index]['faces'].pop('west')
    assert spear_after==spear_before,'spear surface fix changed geometry/display/UV'
    for cls,added in [('item/StormStaffItem','            player.swing(hand,true);\n'),('combat/LikhoStaffItem','        player.swing(hand,true);\n'),('artifact/VelesStaffItem','p.swing(h,true);')]:
        before=(O/'staff-before'/(cls.split('/')[-1]+'.java')).read_text(encoding='utf-8')
        after=(R/'src/main/java/org/slavicmyths'/(cls+'.java')).read_text(encoding='utf-8')
        assert after.count(added)==1 and after.replace(added,'')==before,cls+' changed server ability beyond success animation'
    for relative,expected in family.models().items():assert json.loads((family.A/relative).read_text())==expected,relative
    design=json.loads((O/'thrown/design.json').read_text())
    assert len(design)==14
    rendered=json.loads((O/'thrown/final-source.json').read_text())
    assert len(rendered)==14
    for row in rendered:assert row['after_resolved']==family.g.model('slavicmyths:item/'+row['id'],'thirdperson_righthand'),row['id']
    for material in family.MATERIALS:assert (O/'thrown'/(material+'-SOURCE-AB.png')).is_file()
    assert (O/'SPEAR-SURFACE-AB.png').is_file()
    for row in design:assert sha(family.A/'textures/item'/(row['id']+'.png'))==row['icon_sha256'],row['id']
    recipe=json.loads((RES/'data/slavicmyths/recipe/weapon_veles_staff_assembly.json').read_text())
    ingredients={i['item'] for i in recipe['ingredients']}
    assert ingredients=={'slavicmyths:'+x for x in ['carved_staff','leshy_heart','fern_flower','silver_fitting']}
    attrs=json.loads((O/'native-attributes.json').read_text())
    assert len(attrs)==68
    for row in attrs:
        if row['id'] in ('ancient_spear','restored_spear','thunder_spear'):assert row['reach_total']==4.25
    poses=json.loads((O/'player-source.json').read_text());assert len(poses)==360
    coverage=json.loads((O/'pose-coverage.json').read_text());assert len(coverage)==52
    catalog={line.split('\t')[0] for line in (RES/'assets/slavicmyths/creative_catalog.tsv').read_text(encoding='utf-8').splitlines() if '\tweapons\t' in line}
    assert set(coverage)<=catalog and len(catalog)==68
    mixin=json.loads((RES/'slavicmyths.equipment.mixins.json').read_text());assert 'PlayerWeaponPoseMixin' in mixin['client'] and 'PlayerWeaponPoseMixin' not in mixin.get('mixins',[])
    source=(R/'src/main/java/org/slavicmyths/client/ThirdPersonWeaponMotion.java').read_text();assert 'PlayerWeaponPose.coverage().containsKey' in source
    for name in ['BLADE','HEAVY','SPEAR','POLEAXE','SICKLE','STAFF','KNIFE','SULITSA','WHIP']:assert (O/(name+'-SOURCE-AB.png')).is_file(),name
    jar_receipt=None
    if '--jar' in sys.argv:
        jars=list((R/'build/libs').glob('slavicmyths-*.jar'));assert len(jars)==1,jars
        with zipfile.ZipFile(jars[0]) as jar:
            for p in RES.rglob('*'):
                if p.is_file():
                    rel=p.relative_to(RES).as_posix();expected=R/'build/resources/main'/rel if rel=='META-INF/neoforge.mods.toml' else p
                    assert jar.read(rel)==expected.read_bytes(),rel
            for cls in java:assert 'org/slavicmyths/'+cls+'.class' in jar.namelist(),cls
            assert not any('Weapons87Continuation' in p or 'Weapons87Native' in p or p.startswith('tools/smoke/') for p in jar.namelist())
        jar_receipt={'path':str(jars[0].resolve()),'sha256':sha(jars[0]),'resources':sum(p.is_file() for p in RES.rglob('*'))}
    report={'changed_production':{p:current[p] for p in sorted(changed)},'preserved_production':len(current)-len(changed),'native_attribute_items':68,'native_pose_items':len(coverage),'native_player_samples':len(poses),'jar':jar_receipt,'runtime_launches':0,'runtime':'OWNER_PENDING','issues_closed':[],'project_status':'NOT_CHANGED: missing Project write access','scope':'P53 executor evidence; NOT full #87 completion'}
    (O/'verification.json').write_text(json.dumps(report,indent=2)+'\n')
    print('PASS P53 production scope/preservation, 14 thrown meshes/icons, C/D native attributes/recipe, 360 native pose samples; JAR='+str(jar_receipt)+'; game NOT_RUN')
if __name__=='__main__':main()
