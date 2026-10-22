"""Source-level regression guards for Kalin sabre's third-hit area wave."""
from pathlib import Path

src=(Path(__file__).resolve().parents[1]/"src/main/java/org/slavicmyths/rewards/RewardEvents.java").read_text(encoding="utf-8")
body=src.split("public static void sweep(ServerPlayer p,LivingEntity target,float damage){",1)[1].split("@SubscribeEvent",1)[0]
assert 'count<3' in body and 'n.putInt("Momentum",0)' in body
assert 'target.getBoundingBox().inflate(4.5)' in body
assert 'e.distanceToSqr(target)<=20.25' in body
assert 'e!=target&&ordinary(e)&&hostile(e,p)' in body
assert 'damage*.6F' in body and '++hits==3' in body
assert 'target.getX()+Math.cos(a)*2.6' in body
assert 'toward.dot(heading)' not in body
assert 'Волна воеводы!' in body
print("PASS Kalin sweep: third strike creates target-centered radial feedback, max three additional hostile living targets; runtime NOT_RUN")
