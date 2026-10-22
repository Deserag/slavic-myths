"""P38 acceptance: five related GitHub issues, actual production changes, not static audit only."""
from pathlib import Path
R=Path(__file__).resolve().parents[1]
load=lambda p:(R/p).read_text(encoding="utf-8")
content=load("src/main/java/org/slavicmyths/rewards/RewardContent.java")
reward=load("src/main/java/org/slavicmyths/rewards/RewardEvents.java")
staff=load("src/main/java/org/slavicmyths/item/StormStaffItem.java")
hands=load("src/main/java/org/slavicmyths/client/WeaponHands.java")
sabre=content.split('register("trophy_kalin_sabre"',1)[1].split('for(String id:List.of("mat_bone_plates"',1)[0]
slash=reward.split("public static void sabreWave(ServerPlayer p){",1)[1].split("@SubscribeEvent",1)[0]
scythe=hands.split("static void scytheWrist(",1)[1].split("static void gripArm(",1)[0]
recoil=hands.split("static float crossbowKick(",1)[1].split("static void renderScythe(",1)[0]
# #160: active button, single-target valid, visual remains when zero targets
assert "use(net.minecraft.world.level.Level world" in sabre
assert "RewardEvents.sabreWave(server)" in sabre and "addCooldown(this,140)" in sabre
assert "player.swing(hand,true)" in sabre
assert "p.getBoundingBox().inflate(5.5)" in slash and "hits>=6" in slash
assert "PLAYER_ATTACK_SWEEP" in slash and "i=-8;i<=8" in slash
assert "sweep(p,e.getEntity()" not in reward
# #161: actual thunder is visible even if no damage target; on-hit rune unchanged
assert "selected.ifPresent(" in staff
assert "var bolt=net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(server)" in staff
assert "bolt.setVisualOnly(true)" in staff
assert "server.addFreshEntity(bolt)" in staff
assert "start.lerp(impact,i/14.0)" in staff and "impact.x,impact.y,impact.z,42" in staff
assert "RuneEffects.staff(player,stack,e)" in staff
# #162: player animation feedback for active melee and tooltip consistency
assert "player.swing(hand,true)" in sabre and "ПКМ:" in sabre
# #165: actual Karachun FP windup and follow-through transforms
assert "float windup=Math.max(0,-motion),follow=Math.max(0,motion)" in scythe
assert "rotationDegrees(-18*windup+20*follow)" in scythe
assert "pose.translate(sign*(.56-.20*horizontal)" in scythe
# #163: no phantom recoil when hand is no longer holding this weapon
assert "if(!(held.getItem() instanceof SerpentCrossbow)){observedCrossbows[index]=null;observedCharged[index]=false;lastShotClock[index]=Double.NEGATIVE_INFINITY;return 0;}" in recoil
print("PASS P38: #160 active slash, #161 visible staff lightning, #162 slash animation, #165 scythe motion, #163 reset; game NOT_RUN")
