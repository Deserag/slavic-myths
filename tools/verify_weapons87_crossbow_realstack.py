"""#163: recoil state must use physical player's held stack, not RenderHandEvent snapshot."""
from pathlib import Path
s=(Path(__file__).resolve().parents[1]/"src/main/java/org/slavicmyths/client/WeaponHands.java").read_text(encoding="utf-8")
body=s.split("static float crossbowKick(",1)[1].split("static void renderScythe(",1)[0]
for token in ("int index=hand==InteractionHand.MAIN_HAND?0:1;","ItemStack held=player.getItemInHand(hand);","if(!(held.getItem() instanceof SerpentCrossbow)){observedCrossbows[index]=null;observedCharged[index]=false;lastShotClock[index]=Double.NEGATIVE_INFINITY;return 0;}","stack=held;","CrossbowItem.isCharged(stack)","observedCrossbows[index]!=stack","observedCharged[index]&&!charged&&!player.isUsingItem()","elapsed<0||elapsed>=7"):
    assert token in body,token
assert body.index("stack=held;")<body.index("CrossbowItem.isCharged(stack)")<body.index("observedCrossbows[index]!=stack")
assert "lastShotClock[index]=now" in body
assert "motionPlayer=player;motionLevel=level;" in body
assert "player.getItemInHand(hand)" not in s.split("@SubscribeEvent public static void hand(",1)[1].split("static float crossbowKick(",1)[0]
print("PASS: recoil tracks actual player-held ItemStack and real charged transitions, preserves per-hand reset and seven-tick window; game NOT_RUN")
