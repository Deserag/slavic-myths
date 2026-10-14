"""Integrate reviewed isolated fixes over the complete current snapshot; preserve art/runes."""
from pathlib import Path
import subprocess,json,hashlib
R=Path(__file__).resolve().parents[1]
if (R/'src/main/java/org/slavicmyths/gorynych/CrossbowDamage.java').exists():raise RuntimeError('Already integrated: preserve current files and original baseline')
O=R/'docs/implementation/reward-polymc-update';O.mkdir(parents=True,exist_ok=True)
before={str(p.relative_to(R)).replace('\\','/'):hashlib.sha256(p.read_bytes()).hexdigest() for p in (R/'src/main').rglob('*') if p.is_file()}
(O/'BASELINE_HASHES.json').write_text(json.dumps(before,indent=2))
def copy(ref,path):
 p=R/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(subprocess.check_output(['git','show',ref+':'+path],cwd=R))
for ref,paths in {
 '34cb7ae7':['src/main/java/org/slavicmyths/gorynych/CrossbowDamage.java','src/main/java/org/slavicmyths/equipment/mixin/GorynychArrowDamageMixin.java','tools/reward-issues.init.gradle','tools/reward-issues-resources/data/slavicmyths_rewards/structure/empty.nbt','tools/reward-issues-tests/org/slavicmyths/verify/RewardIssueTests.java'],
 'ac6cf40a':['src/main/java/org/slavicmyths/client/FrostClient.java','src/main/java/org/slavicmyths/morozko/FrostStaff.java','src/main/java/org/slavicmyths/morozko/FrostSkills.java','src/main/java/org/slavicmyths/morozko/FrostNetwork.java','tools/reward_weapon_texts.py','tools/reward-issues-tests/org/slavicmyths/verify/FrostStreamIssueTests.java'],
 'd6bb52df':['src/main/java/org/slavicmyths/gorynych/FlameVfx.java','src/main/java/org/slavicmyths/client/GorynychFlameVfx.java','tools/reward-issues-tests/org/slavicmyths/verify/GorynychFireIssueTests.java'],
 'b3807910':['src/main/java/org/slavicmyths/gorynych/StormCrossbow.java','src/main/java/org/slavicmyths/gorynych/StormVfx.java','src/main/java/org/slavicmyths/client/GorynychStormVfx.java','src/main/java/org/slavicmyths/equipment/mixin/GorynychStormArrowMixin.java','tools/reward-issues-tests/org/slavicmyths/verify/GorynychStormIssueTests.java']
}.items():
 for p in paths:copy(ref,p)
def edit(path,old,new):
 p=R/path;s=p.read_text(encoding='utf-8');assert old in s,(path,old);p.write_text(s.replace(old,new),encoding='utf-8')
edit('src/main/java/org/slavicmyths/morozko/FrostRules.java','public static final int STREAM_CD=200,ICICLE_CD=400,WAVE_CD=320;','public static final int STREAM_CD=200,ICICLE_CD=400,WAVE_CD=320;\n public static final int STREAM_DURATION=60,STREAM_HEARTBEAT_GRACE=20;')
edit('src/main/java/org/slavicmyths/gorynych/GorynychContent.java','GorynychUpgrade.SERIALIZERS.register(bus);','GorynychUpgrade.SERIALIZERS.register(bus);bus.addListener(FlameVfx::register);bus.addListener(StormVfx::register);')
edit('src/main/java/org/slavicmyths/gorynych/GorynychEquipment.java','shot.getPersistentData().remove("GorynychSpecial");burst(owner,e.getRayTraceResult().getLocation(),mode-1,3);','if(mode==3&&shot instanceof AbstractArrow&&e.getRayTraceResult() instanceof EntityHitResult)return;shot.getPersistentData().remove("GorynychSpecial");if(mode==3)StormCrossbow.burst(owner,e.getRayTraceResult().getLocation());else burst(owner,e.getRayTraceResult().getLocation(),mode-1,3);')
edit('src/main/java/org/slavicmyths/gorynych/GorynychEquipment.java','w.sendParticles(BreathCloud.particle(type),at.x,at.y+.3,at.z,36,radius,.4,radius,.04);','if(type==1)FlameVfx.play(w,at,radius);else w.sendParticles(BreathCloud.particle(type),at.x,at.y+.3,at.z,36,radius,.4,radius,.04);')
mix=R/'src/main/resources/slavicmyths.equipment.mixins.json';data=json.loads(mix.read_text());data['mixins']+=['GorynychArrowDamageMixin','GorynychStormArrowMixin'];mix.write_text(json.dumps(data,indent=2)+'\n')
edit('src/main/java/org/slavicmyths/gorynych/SerpentCrossbow.java','public final boolean special;','public final boolean special;private UUID regularVolley;')
edit('src/main/java/org/slavicmyths/gorynych/SerpentCrossbow.java','if(!special){super.performShooting(w,e,hand,s,speed,spread,target);return;}','if(!special){var charged=s.get(DataComponents.CHARGED_PROJECTILES);regularVolley=charged!=null&&charged.getItems().size()>1?UUID.randomUUID():null;try{super.performShooting(w,e,hand,s,speed,spread,target);}finally{regularVolley=null;}return;}')
edit('src/main/java/org/slavicmyths/gorynych/SerpentCrossbow.java','for(int i=0;i<3;i++){','UUID volley=UUID.randomUUID();\n  for(int i=0;i<3;i++){')
edit('src/main/java/org/slavicmyths/gorynych/SerpentCrossbow.java','mode,speed,i*4,i);','mode,speed,i*4,i,volley);')
edit('src/main/java/org/slavicmyths/gorynych/SerpentCrossbow.java','0,target,i);','0,target,i,volley);')
edit('src/main/java/org/slavicmyths/gorynych/SerpentCrossbow.java','LivingEntity target,int lane){','LivingEntity target,int lane,UUID volley){')
edit('src/main/java/org/slavicmyths/gorynych/SerpentCrossbow.java','shot.getPersistentData().putInt("GorynychMuzzleLane",lane);','shot.getPersistentData().putUUID(CrossbowDamage.VOLLEY,volley);\n  shot.getPersistentData().putInt("GorynychMuzzleLane",lane);')
edit('src/main/java/org/slavicmyths/gorynych/SerpentCrossbow.java','if(!owner.level().isClientSide&&shot instanceof AbstractArrow arrow)arrow.setBaseDamage(arrow.getBaseDamage()*1.18);','if(!owner.level().isClientSide){if(regularVolley!=null)shot.getPersistentData().putUUID(CrossbowDamage.VOLLEY,regularVolley);if(shot instanceof AbstractArrow arrow)arrow.setBaseDamage(arrow.getBaseDamage()*1.18);}')
edit('src/main/java/org/slavicmyths/gorynych/SerpentCrossbow.java','lines.add(GorynychContent.text(n.getBoolean("Burst")?"volley_burst":"volley_fan"));','lines.add(GorynychContent.text(n.getBoolean("Burst")?"volley_burst":"volley_fan"));if(Math.floorMod(n.getInt("Mode"),3)==2)lines.add(GorynychContent.text("storm_stats",StormCrossbow.DAMAGE,StormCrossbow.RADIUS,StormCrossbow.CONTROL_TICKS/20,StormCrossbow.MAX_TARGETS,StormCrossbow.TARGET_COOLDOWN/20.0));')
edit('src/main/java/org/slavicmyths/gorynych/CrossbowVolley.java','ServerLevel level){}','ServerLevel level,UUID volley){}')
edit('src/main/java/org/slavicmyths/gorynych/CrossbowVolley.java','int delay,int lane){','int delay,int lane,UUID volley){')
edit('src/main/java/org/slavicmyths/gorynych/CrossbowVolley.java','p.serverLevel()));','p.serverLevel(),volley));')
edit('src/main/java/org/slavicmyths/gorynych/CrossbowVolley.java','0,null,r.lane);','0,null,r.lane,r.volley);')
# The old test deliberately asserted the old timeout; new lifecycle tests cover the replacement.
edit('tools/reward-issues-tests/org/slavicmyths/verify/RewardIssueTests.java','@GameTest(template="empty") public static void frostBaseline','public static void frostBaseline')
import sys
sys.path.insert(0,str(R/'tools'))
from reward_weapon_texts import apply
apply()
for locale,value in {'ru_ru':'Буря: +%s HP; радиус %s м; Замедление II на %s с; до %s целей. Повтор по цели раз в %s с.','en_us':'Storm: +%s HP; %sm radius; Slowness II for %ss; up to %s targets. Repeat per target every %ss.'}.items():
 p=R/f'src/main/resources/assets/slavicmyths/lang/{locale}.json';d=json.loads(p.read_text(encoding="utf-8"));d['gorynych196.storm_stats']=value;p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
edit('gradle.properties','mod_version=1.9.7.3','mod_version=1.9.7.4')
print('Integrated #3/#6/#4/#5 over complete 1.9.7.3 snapshot; art/runes preserved')
