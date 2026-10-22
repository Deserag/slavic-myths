"""#161/#162/#165 source/JAR contract. Minecraft runtime is not tested here."""
from pathlib import Path
import json,sys,zipfile,math
R=Path(__file__).resolve().parents[1]
client=(R/'src/main/java/org/slavicmyths/client/WeaponMeleeClient.java').read_text()
staff=(R/'src/main/java/org/slavicmyths/item/StormStaffItem.java').read_text()
before=(R/'docs/implementation/weapons87/blueprints/p33-p35/before-StormStaffItem.java.txt').read_text()
maces=('WOODEN_MACE','STONE_MACE','MACE','SILVER_MACE','PERUNITE_MACE')
spears=('spear','silver_spear','gold_spear','iron_spear','diamond_spear','perunite_spear','netherite_spear')
assert '@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)' in client
assert 'RegisterClientExtensionsEvent' in client and 'applyForgeHandTransform(' in client
for id in maces:assert client.count('ModItems.'+id+'.get()')==1,id
for id in ('SPEAR','SILVER_SPEAR'):assert client.count('ModItems.'+id+'.get()')==1,id
for id in spears[2:]:assert client.count('WeaponCatalog.item("'+id+'")')==1,id
assert client.count('event.registerItem(')==2 and client.count('return true;')==1
assert 'pose.mulPose(Axis.XP.rotationDegrees(-24*prep+58*strike))' in client
assert 'pose.translate(sign*(-.045F*prep),.035F*prep,-.38F*strike)' in client
assert 'Mth.clamp(swing,0,1)' in client and 't*t*(3-2*t)' in client
assert 'pose.translate(sign*.49F,-.40F-equip*.6F,-.91F)' in client
def sm(t):
    t=max(0,min(1,t))
    return t*t*(3-2*t)
def phases(t):
    t=max(0,min(1,t))
    return (0 if t<=0 else sm(t/.18)*(1-sm((t-.18)/.22)),sm((t-.12)/.21)*(1-sm((t-.38)/.46)))
for t in (0,1,-1,2):assert phases(t)==(0,0),(t,phases(t))
assert phases(.15)[0]>.5 and phases(.35)[1]>.9 and phases(.85)[1]<.05
registrations=(R/'src/main/java/org/slavicmyths/registry/ModItems.java').read_text()
for id in maces+('SPEAR','SILVER_SPEAR'):assert ' '+id+' ' in registrations or ' '+id+'=' in registrations,id
models=R/'src/main/resources/assets/slavicmyths/models/item'
for id in ('wooden_mace','stone_mace','mace','silver_mace','perunite_mace')+spears:
    path=models/(id+'.json')
    assert path.is_file(),id
    obj=json.loads(path.read_text())
    if obj.get('loader')=='neoforge:separate_transforms':assert (models/(id+'_held.json')).is_file(),id
    else:assert obj.get('elements') or obj.get('textures')
assert staff!=before and staff.count('e.hurt(')==before.count('e.hurt(')==1
for fragment in ('boolean hit=e.hurt(', 'if(hit && world instanceof ServerLevel level)level.sendParticles(ParticleTypes.ELECTRIC_SPARK,','e.getZ(),8,.24,.28,.24,.018);','org.slavicmyths.rpg.RuneEffects.staff(player,stack,e);'):
    assert fragment in staff,fragment
assert staff.count('sendParticles(')==before.count('sendParticles(')+1
assert 'SoundEvents.TRIDENT_THUNDER.value()' in staff
for marker in ('PULSE_COOLDOWN=60','RUNE_COOLDOWN=54','BASE_PULSE_DAMAGE=6.0F'):assert marker in staff
assert 'player.getCooldowns().addCooldown(this, cooldownTicks(' in staff
assert 'setWeatherParameters' not in staff and 'new LightningBolt' not in staff
if '--jar' in sys.argv:
    jars=list((R/'build/libs').glob('slavicmyths-*.jar'));assert len(jars)==1
    with zipfile.ZipFile(jars[0]) as z:
        for p in ('org/slavicmyths/client/WeaponMeleeClient.class','org/slavicmyths/client/WeaponMeleeClient$ClientPose.class','org/slavicmyths/item/StormStaffItem.class'):
            assert p in z.namelist(),p
print('PASS #162 five maces, #165 seven spears, four swing stages, #161 successful-hit-only eight sparks. Gameplay NOT_RUN')
