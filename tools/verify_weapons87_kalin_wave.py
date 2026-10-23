"""#160 sabre: active right-click frontal slash, not hidden three-hit momentum."""
from pathlib import Path
R=Path(__file__).resolve().parents[1]
s=(R/"src/main/java/org/slavicmyths/rewards/RewardEvents.java").read_text(encoding="utf-8")
item=(R/"src/main/java/org/slavicmyths/rewards/RewardContent.java").read_text(encoding="utf-8")
body=s.split("public static void sabreWave(ServerPlayer p){",1)[1].split("@SubscribeEvent",1)[0]
for token in ("p.getLookAngle()","for(int ring=1;ring<=5;ring++)","i=-8;i<=8","p.getBoundingBox().inflate(5.5)","e.distanceToSqr(p)<=30.25","e instanceof net.minecraft.world.entity.monster.Enemy","p.hasLineOfSight(e)","toward.dot(forward)<.35","5F","hits>=6","ParticleTypes.SWEEP_ATTACK","PLAYER_ATTACK_SWEEP","Разящий взмах"):
    assert token in body,token
assert "sweep(p,e.getEntity()" not in s
assert "MomentumAt" not in body and "count<3" not in body
assert 'register("trophy_kalin_sabre",()->new SwordItem' in item
assert "RewardEvents.sabreWave(server)" in item and "getCooldowns().addCooldown(this,140)" in item
assert "player.swing(hand,true)" in item
assert "stack.hurtAndBreak(2,player" in item
print("PASS: Kalin right-click slash has independent visible effect and target damage, bounded 5.5m/6 targets/7s; runtime NOT_RUN")
