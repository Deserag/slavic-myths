from pathlib import Path
import json,zipfile,io,shutil
from PIL import Image
R=Path(__file__).resolve().parents[1]
def change(name,a,b):
 p=R/name;s=p.read_text(encoding='utf8');assert a in s,(name,a);p.write_text(s.replace(a,b),encoding='utf8')
out=R/'work/visual-163';out.mkdir(parents=True,exist_ok=True)
if not (out/'baseline-1.6.2.jar').exists():shutil.copy2(R/'build/libs/slavicmyths-1.6.2.jar',out/'baseline-1.6.2.jar')
change('gradle.properties','mod_version=1.6.2','mod_version=1.6.3')
change('src/main/java/org/slavicmyths/earth/EarthSeal.java','Properties.of().strength','Properties.of().noOcclusion().strength')
# Tail waves use continuous render age, never packet-stepped wrapped gait.
p=R/'src/main/java/org/slavicmyths/client/SivkaClient.java';s=p.read_text();s=s.replace('n("tailMid").xRot=rad((float)Math.sin(u*Math.PI*2-.8)*8)','n("tailMid").xRot=rad((float)Math.sin(age*.10-.8)*4)');s=s.replace('Math.sin(u*Math.PI*2-1-i*.2)*11','Math.sin(age*.10-1-i*.2)*5');p.write_text(s)
change('src/main/java/org/slavicmyths/earth/EarthActor.java','double speed=pillar?.65:.85','double speed=pillar?1.15:1.65')
change('src/main/java/org/slavicmyths/earth/EarthRules.java','SHOT_INTERVAL=14','SHOT_INTERVAL=8')
change('src/main/java/org/slavicmyths/earth/EarthRules.java','HEAVY_GAP=32','HEAVY_GAP=16')
change('src/main/java/org/slavicmyths/earth/EarthRules.java','MELEE(32,50), PILLAR(76,560), VOLLEY(112,360), COUNTER(50,400), TREMOR(76,440)','MELEE(24,30), PILLAR(76,280), VOLLEY(82,120), COUNTER(50,180), TREMOR(76,180)')
change('src/main/java/org/slavicmyths/earth/EarthRules.java','calibrated against vanilla iron (15 armour, zero toughness), not armour bypass.','calibrated against diamond (20 armour, 8 toughness); servants retain iron calibration. No armour bypass.')
p=R/'src/main/java/org/slavicmyths/earth/EarthSpirit.java';s=p.read_text();s=s.replace('a==Action.MELEE?12:','a==Action.MELEE?6:').replace('begin(Action.MELEE,32)','begin(Action.MELEE,24)').replace('if(elapsed==18){cue("melee_impact")','if(elapsed==12){cue("melee_impact")');s=s.replace('&&tickCount-meleeLast.getOrDefault(t.getUUID(),-1000)<=50','');s=s.replace('d>=7&&d<=30','d>=4.5&&d<=30');s=s.replace('p.hurt(damageSources().mobAttack(this),EarthRules.MELEE_RAW)','p.hurt(attackSource(this),EarthRules.MELEE_RAW)')
anchor='    public UUID arenaId()'
s=s.replace(anchor,'    public net.minecraft.world.damagesource.DamageSource attackSource(Entity direct){var key=net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,EarthContent.id(direct instanceof EarthActor actor&&(actor.kind()==EarthActor.STONE||actor.kind()==EarthActor.PILLAR)?"earth_projectile":"earth_strike"));return new net.minecraft.world.damagesource.DamageSource(level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(key),direct,this);}\n'+anchor)
p.write_text(s)
change('src/main/java/org/slavicmyths/earth/EarthActor.java','kind()==STONE||kind()==PILLAR?damageSources().thrown(this,b):b.damageSources().mobAttack(b)','b.attackSource(this)')
for key in ['earth_strike','earth_projectile']:
 p=R/f'src/main/resources/data/slavicmyths/damage_type/{key}.json';p.write_text(json.dumps({'message_id':key,'scaling':'never','exhaustion':.1},indent=2)+'\n')
p=R/'src/main/resources/data/minecraft/tags/damage_type/is_projectile.json';v=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]};v['values'].append('slavicmyths:earth_projectile');p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,indent=2)+'\n')
# Vanilla limb swing is attenuated by entity scale; integrate real client displacement.
p=R/'src/main/java/org/slavicmyths/client/EarthClient.java';s=p.read_text();s=s.replace('final ModelPart root,pelvis','private static final class Stride {double x,z;float phase,weight;boolean initialized;}private final Map<EarthSpirit,Stride> strides=new WeakHashMap<>();\n        final ModelPart root,pelvis')
a='float walk=(float)Math.sin(swing*.5)*amount;'
b='var stride=strides.computeIfAbsent(e,k->new Stride());float partial=age-e.tickCount;double x=net.minecraft.util.Mth.lerp(partial,e.xo,e.getX()),z=net.minecraft.util.Mth.lerp(partial,e.zo,e.getZ());double moved=stride.initialized?Math.hypot(x-stride.x,z-stride.z):0;stride.initialized=true;stride.x=x;stride.z=z;boolean walking=e.action()==EarthRules.Action.IDLE||e.action()==EarthRules.Action.APPROACH||e.action()==EarthRules.Action.RECOVER;stride.phase+=(float)Math.min(moved,1)*2.8F;stride.weight=walking?Math.max(amount,(float)Math.min(1,e.getDeltaMovement().horizontalDistance()*12)):0;float walk=(float)Math.sin(stride.phase)*stride.weight;'
assert a in s;s=s.replace(a,b,1).replace('walk*.35F','walk*.65F').replace('walk)*.28F','walk)*.55F');s=s.replace('float[] times={0,6,15,19,25,32};arm("r"','float[] times={0,4,10,13,18,24};arm("r"').replace('ramp(t,15,19)*(1-ramp(t,19,32))','ramp(t,10,13)*(1-ramp(t,13,24))');p.write_text(s)
# Copy installed vanilla source textures once as reproducible generator inputs.
source=R/'tools/templates/earth-163';source.mkdir(parents=True,exist_ok=True)
with zipfile.ZipFile('C:/Users/pavel/.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar') as z:
 for name in ['cobblestone','mossy_cobblestone','andesite','stone','cobbled_deepslate']:
  (source/(name+'.png')).write_bytes(z.read('assets/minecraft/textures/block/'+name+'.png'))
print('1.6.3 source changes applied')
