"""Source freeze and production JAR checks for #93. Never starts a game/server."""
from pathlib import Path
import hashlib,json,re,zipfile
from PIL import Image

ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'docs/implementation/likho93'
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
def body(source,signature):
 start=source.index('{',source.index(signature));depth=1;end=start+1
 while depth:
  if source[end]=='{':depth+=1
  if source[end]=='}':depth-=1
  end+=1
 return source[start+1:end-1].strip()
def main():
 checks=[]
 def check(ok,label):
  checks.append({'requirement':label,'status':'PASS' if ok else 'FAIL'});assert ok,label
 baseline=json.loads((OUT/'baseline.json').read_text())
 allowed={'gradle.properties','src/main/java/org/slavicmyths/hunt/WorldBoss.java','src/main/java/org/slavicmyths/mor/MorZone.java','src/main/java/org/slavicmyths/mor/HigherMor.java'}
 changed=[p for p,h in baseline.items() if not (ROOT/p).is_file() or sha(ROOT/p)!=h]
 check(set(changed)<=allowed,'all existing unrelated source/resources/tools unchanged from post-140/141 snapshot')
 (OUT/'changed-existing.json').write_text(json.dumps(changed,indent=2)+'\n')
 for prefix in ['src/main/resources/','src/main/java/org/slavicmyths/client/','src/main/java/org/slavicmyths/registry/','src/main/java/org/slavicmyths/trophy/']:
  paths=[p for p in baseline if p.startswith(prefix)]
  check(all(sha(ROOT/p)==baseline[p] for p in paths),'byte-exact protected scope '+prefix)
 before=(OUT/'before-WorldBoss.java').read_text();after=(ROOT/'src/main/java/org/slavicmyths/hunt/WorldBoss.java').read_text()
 for signature in ['getDefaultDimensions(','attributes(BossKind','registerGoals(','addAdditionalSaveData(','readAdditionalSaveData(','tugarinState(TugarinState','tugarinMove(Move','void die(']:
  check(body(before,signature)==body(after,signature),'unchanged WorldBoss method '+signature)
 check(before[before.index('if(tugarinState()==TugarinState.TRANSFORMING)amount'):before.index(' @Override public int getAmbientSoundInterval')]==after[after.index('if(tugarinState()==TugarinState.TRANSFORMING)amount'):after.index(' @Override public int getAmbientSoundInterval')],'unchanged Tugarin hurt/phase logic')
 check('if(kind()==BossKind.TUGARIN){tugarin().tick();return;}' in after,'Tugarin scheduler still returns before Likho changes')
 old_choice=body(before,'private Move choose(').split('}else{',1)[1];new_choice=body(after,'private Move choose(').split('}else{',1)[1]
 check(old_choice==new_choice,'Tugarin skill choice unchanged')
 for move in ['SWEEP','GRAB','LEAP','PUNCH','SLAM','STONE']:
  pattern=r'case '+move+r':(.*?)(?=\n  case |\n  default:)'
  check(re.search(pattern,before,re.S).group(1)==re.search(pattern,after,re.S).group(1),'unchanged damage/release parameters '+move)
 check('area(10,4,Math.cos(Math.toRadians(55)),.55F)' in after and 'area(8,3.5,-1,1)' in after,'CHASE and LEAP damage/range/cone unchanged')
 check('for(LivingEntity p:victims(14))' in after and 'hits.add(p.getUUID())' in after,'GROUND hits legal LivingEntity at most once per release')
 check('kind()==BossKind.LIKHO?level().getEntitiesOfClass(LivingEntity.class' in after and ':players(radius)' in after,'area victim widening guarded to Likho only')
 check('LikhoCombatRules.phaseBoundary(move())' in after and 'if(focus!=null)t=focus' in after,'phase and focus commitments wired to production scheduler')
 # Ordinary hurt neither clears action nor returns fabricated invulnerability.
 hurt=body(after,'public boolean hurt(').split('if(tugarinState()',1)[0]
 check('super.hurt(source,amount)' in hurt and 'recover(' not in hurt and 'entityData.set' not in hurt,'Likho hurt still delegates damage, without action reset')
 check('if(move()!=Move.SPIN)super.knockback(power,x,z);' in after,'existing knockback behavior retained')
 target=(ROOT/'src/main/java/org/slavicmyths/hunt/LikhoTargets.java').read_text()
 check('CombatTargeting.canAttack(self,target,CombatTargeting.Context.AREA)' in target,'shared ally/creative/spectator/protected/story gate retained')
 check('target instanceof Enemy' in target and 'mob.getTarget()==self' in target and 'pet.isTame()' in target,'enemy/direct-attacker policy and tamed exclusion wired')
 old=(OUT/'before-HigherMor.java').read_text();higher=(ROOT/'src/main/java/org/slavicmyths/mor/HigherMor.java').read_text()
 for signature in ['public static boolean enemy(','private void zone(','public void save(','public void load(']:check(body(old,signature)==body(higher,signature),'unchanged HigherMor '+signature)
 check('mob.mor.kind()!=5||mob.morSkill()==0' in higher,'phase interruption/retarget fix limited to Mor-Likho')
 zone=(ROOT/'src/main/java/org/slavicmyths/mor/MorZone.java').read_text();oldzone=(OUT/'before-MorZone.java').read_text()
 old_other=oldzone[oldzone.index('  Entity source='):oldzone.index('\n }\n @Override protected void addAdditionalSaveData')].strip()
 check(body(zone,'private void tickOtherHigherZone(').replace('otherExposure','exposure')==old_other,'Ovinnik zone body preserved verbatim')
 check('if(style()%2==1){tickOtherHigherZone();return;}' in zone,'Ovinnik uses its unchanged branch')
 for signature in ['public double radius(','public void configure(','protected void addAdditionalSaveData(','protected void readAdditionalSaveData(']:check(body(oldzone,signature)==body(zone,signature),'zone dimensions/configuration/NBT unchanged '+signature)
 check('s.isRemoved()||distanceToSqr(s)>32*32' in zone and 'exposure.clear();if((absent+=10)>=40)discard();return;' in zone,'Likho zone cannot harm with absent/dead/remote owner')
 check('e->HigherMor.enemy(s,e)' in zone and 's.clearLineForMor' in zone and 'tickCount%10!=0' in zone,'local zone policy/LOS/cadence preserved')
 check(not any(x in zone for x in ['setBlock(','setBlockAndUpdate(','getAllEntities(','getAllLevels(','addRegionTicket(']),'no world edits, global scans or chunk tickets')
 trace=(ROOT/'src/main/java/org/slavicmyths/hunt/LikhoCombatTrace.java').read_text()
 check('Boolean.getBoolean("slavicmyths.likho.trace")' in trace and 'count>=160' in trace and 'WeakHashMap' in trace,'diagnostics opt-in and bounded, no strong entity retention')
 jar=ROOT/'build/libs/slavicmyths-1.9.7.52.jar';resources=pngs=jsons=0
 with zipfile.ZipFile(jar) as z:
  for p in (ROOT/'src/main/resources').rglob('*'):
   if not p.is_file():continue
   rel=p.relative_to(ROOT/'src/main/resources').as_posix();expected=ROOT/'build/resources/main'/rel if rel=='META-INF/neoforge.mods.toml' else p
   check(z.read(rel)==expected.read_bytes(),'JAR resource parity '+rel);resources+=1
   if p.suffix=='.json':json.loads(p.read_text(encoding='utf-8-sig'));jsons+=1
   if p.suffix=='.png':
    with Image.open(p) as im:im.verify()
    pngs+=1
  for name in ['hunt/LikhoTargets','hunt/LikhoCombatRules','hunt/LikhoCombatTrace','mor/MorExposure']:
   check('org/slavicmyths/'+name+'.class' in z.namelist(),'production class packaged '+name)
  check(not any('Offline' in n or n.startswith(('tools/','org/slavicmyths/verify/')) for n in z.namelist()),'no test fixtures in production JAR')
 manifest={p.relative_to(ROOT).as_posix():sha(p) for f in ['src','tools'] for p in (ROOT/f).rglob('*') if p.is_file() and '__pycache__' not in p.parts}
 manifest['gradle.properties']=sha(ROOT/'gradle.properties');(OUT/'source-manifest.json').write_text(json.dumps(manifest,indent=2)+'\n')
 result={'status':'OWNER_PENDING','base_commit':'32d1733457e7e90360ce16c23860304ece664c64','source':'uncommitted snapshot source-manifest.json','resources':resources,'json':jsons,'png':pngs,'checks':checks,'runtime_launches':0,'HP':{'normal_base':350,'mor_multiplier':2,'default_mor_max':700,'existing_NBT_modifiers':'unchanged; not forced to a constant'},'jar':{'path':str(jar),'sha256':sha(jar),'bytes':jar.stat().st_size}}
 (OUT/'verification.json').write_text(json.dumps(result,indent=2)+'\n');print(json.dumps({k:v for k,v in result.items() if k!='checks'},indent=2))
if __name__=='__main__':main()
