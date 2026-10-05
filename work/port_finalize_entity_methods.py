from pathlib import Path
import re,zipfile
log=Path('docs/port/finalize-compile.log').read_text(encoding='utf-8',errors='replace').split('FAILURE:')[0]
# Only change method tokens on javac-confirmed lines, never arbitrary collection remove().
changes={};patterns=[('method remove in class Entity',r'\bremove\(\)','discard()'),('symbol:   method canSee(',r'\bcanSee\(','hasLineOfSight('),('symbol:   method isOnGround(',r'\bisOnGround\(','onGround('),('symbol:   method setSecondsOnFire(',r'\bsetSecondsOnFire\(','igniteForSeconds(')]
for m in re.finditer(r'([^\r\n]+\.java):(\d+): error: ([\s\S]*?)(?=\n[^\n]+\.java:\d+: (?:error|warning):|\Z)',log):
 for token,pat,repl in patterns:
  if token in m.group(3):changes.setdefault(m.group(1),{}).setdefault(int(m.group(2))-1,[]).append((pat,repl))
for name,lines in changes.items():
 p=Path(name);s=p.read_text(encoding='utf-8').splitlines(keepends=True)
 for n,pairs in lines.items():
  for pat,repl in pairs:s[n]=re.sub(pat,repl,s[n])
 p.write_text(''.join(s),encoding='utf-8')
for name in ['VolkolakEntity','OvinnikEntity']:
 p=Path('src/main/java/org/slavicmyths/entity/'+name+'.java');s=p.read_text(encoding='utf-8').replace('net.minecraft.world.entity.ai.attributes.Attribute attributes()','net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder attributes()');p.write_text(s,encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/flight/FlyingVessel.java');s=p.read_text(encoding='utf-8').replace('public final Inventory cargo=new Inventory(9)','public final SimpleContainer cargo=new SimpleContainer(9)');p.write_text(s,encoding='utf-8')
# Standard damage factories must now come from the source entity's registry-backed DamageSources.
for p in Path('src/main/java').rglob('*.java'):
 s=p.read_text(encoding='utf-8');old=s
 # these factories appear in entity subclasses only; other callers are handled explicitly later
 if re.search(r'class \w+ extends (?:\w+Entity|PathfinderMob|Monster|ThrowableItemProjectile|AbstractArrow|Entity|HuntMob)',s):
  s=re.sub(r'DamageSource\.(thrown|indirectMagic|mobAttack|playerAttack)\(',r'damageSources().\1(',s)
 if s!=old:p.write_text(s,encoding='utf-8')
print('Compiler-confirmed method changes:',sum(len(v) for v in changes.values()))
