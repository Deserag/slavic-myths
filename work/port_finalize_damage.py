from pathlib import Path
import re,json
root=Path('src/main/resources/data/slavicmyths/damage_type');root.mkdir(parents=True,exist_ok=True)
for name in ['slavic_path','slavic_retribution','fire_serpent','serpent_trail','ovinnik_trail','ovinnik_slam','ovinnik_ash']:
 (root/(name+'.json')).write_text(json.dumps({'message_id':name,'scaling':'never' if name=='ovinnik_ash' else 'when_caused_by_living_non_player','exhaustion':.1},indent=2)+'\n',encoding='utf-8')
tag=Path('src/main/resources/data/minecraft/tags/damage_type/is_fire.json');tag.parent.mkdir(parents=True,exist_ok=True);tag.write_text(json.dumps({'replace':False,'values':['slavicmyths:fire_serpent','slavicmyths:serpent_trail','slavicmyths:ovinnik_trail']},indent=2)+'\n',encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/combat/MythDamageSources.java');p.write_text('''package org.slavicmyths.combat;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** Registry-backed versions of existing named damage sources. */
public final class MythDamageSources {
    private static Holder<DamageType> type(Entity entity,String name){
        return entity.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(
            ResourceKey.create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath("slavicmyths",name)));
    }
    public static DamageSource caused(String name,Entity cause){return new DamageSource(type(cause,name),cause);}
    public static DamageSource unattributed(String name,Entity context){return new DamageSource(type(context,name));}
    // The original shielded slam explicitly had no source position.
    public static DamageSource slam(Entity cause){return new DamageSource(type(cause,"ovinnik_slam"),cause){
        @Override public Vec3 getSourcePosition(){return null;}
    };}
    private MythDamageSources(){}
}
''',encoding='utf-8')
for p in Path('src/main/java').rglob('*.java'):
 old=s=p.read_text(encoding='utf-8')
 s=re.sub(r'new (?:net\.minecraft\.util\.)?EntityDamageSource\("(slavic_path|slavic_retribution|fire_serpent|serpent_trail|ovinnik_trail)",(\w+)\)(?:\.setIsFire\(\))?',r'org.slavicmyths.combat.MythDamageSources.caused("\1",\2)',s)
 s=s.replace('new EntityDamageSource("ovinnik_slam",this){@Override public Vec3 getSourcePosition(){return null;}}','org.slavicmyths.combat.MythDamageSources.slam(this)').replace('new DamageSource("ovinnik_ash")','org.slavicmyths.combat.MythDamageSources.unattributed("ovinnik_ash",this)')
 s=re.sub(r'([\w.]+\.getSource\(\)|\bs)\s*==\s*DamageSource\.(LIGHTNING_BOLT|OUT_OF_WORLD|FALL)',lambda m:m[1]+'.is(net.minecraft.world.damagesource.DamageTypes.'+{'LIGHTNING_BOLT':'LIGHTNING_BOLT','OUT_OF_WORLD':'FELL_OUT_OF_WORLD','FALL':'FALL'}[m[2]]+')',s)
 s=s.replace('event.getSource() != DamageSource.LIGHTNING_BOLT','!event.getSource().is(net.minecraft.world.damagesource.DamageTypes.LIGHTNING_BOLT)')
 if p.name in {'HuntMob.java','ElementHuntMob.java'}:s=s.replace('DamageSource.DROWN','damageSources().drown()')
 if p.name=='NightingaleEntity.java':s=s.replace('private DamageSource fatal=DamageSource.GENERIC;','private DamageSource fatal;').replace('super(type,world);setPersistenceRequired();','super(type,world);fatal=damageSources().generic();setPersistenceRequired();')
 s=s.replace('.setPercent(','.setProgress(').replace('.isBypassArmor()','.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR)')
 if s!=old:p.write_text(s,encoding='utf-8')
# portCore compiles the real common helper without initializing custom data registries.
p=Path('build.gradle');s=p.read_text(encoding='utf-8');needle="include 'org/slavicmyths/flight/Tailwind.java'";assert needle in s;s=s.replace(needle,needle+"\n        include 'org/slavicmyths/combat/MythDamageSources.java'");p.write_text(s,encoding='utf-8')
