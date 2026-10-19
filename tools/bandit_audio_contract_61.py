"""Exact audio-only Bandit source contract; no combat/AI state changes."""
REGISTRATIONS=[('BANDIT_SWORD_WINDUP','bandit_sword_windup'),('BANDIT_AXE_WINDUP','bandit_axe_windup'),('BANDIT_MACE_WINDUP','bandit_mace_windup'),('BANDIT_SWORD_IMPACT','bandit_sword_impact'),('BANDIT_AXE_IMPACT','bandit_axe_impact'),('BANDIT_MACE_IMPACT','bandit_mace_impact'),('BANDIT_BOW_DRAW','bandit_bow_draw'),('BANDIT_SHIELD_RAISE','bandit_shield_raise')]
def registry_edit(s):
 marker='    public static final DeferredHolder<SoundEvent,SoundEvent> BANDIT_IDLE'
 assert marker in s
 return s.replace(marker,''.join('    public static final DeferredHolder<SoundEvent,SoundEvent> '+field+' = sound("'+event+'");\n' for field,event in REGISTRATIONS)+marker,1)
REPLACEMENTS=[
 ('    private final class CombatGoal extends Goal {','    private SoundEvent weaponCue(boolean impact){\n        Item item=getMainHandItem().getItem();\n        return item instanceof SwordItem?(impact?ModSounds.BANDIT_SWORD_IMPACT.get():ModSounds.BANDIT_SWORD_WINDUP.get()):item instanceof AxeItem?(impact?ModSounds.BANDIT_AXE_IMPACT.get():ModSounds.BANDIT_AXE_WINDUP.get()):(impact?ModSounds.BANDIT_MACE_IMPACT.get():ModSounds.BANDIT_MACE_WINDUP.get());\n    }\n    private final class CombatGoal extends Goal {'),
 ('if(hit&&heavy){Vec3 d=target.position().subtract(position());target.knockback(.8F,-d.x,-d.z);}', 'if(hit&&heavy){Vec3 d=target.position().subtract(position());target.knockback(.8F,-d.x,-d.z);}\n                        if(hit)playSound(heavy?ModSounds.BANDIT_HEAVY.get():weaponCue(true),.65F,1);'),
 ('startUsingItem(InteractionHand.OFF_HAND);action(4);timer=22;return;', 'startUsingItem(InteractionHand.OFF_HAND);playSound(ModSounds.BANDIT_SHIELD_RAISE.get(),.35F,1);action(4);timer=22;return;'),
 ('if(heavy)playSound(ModSounds.BANDIT_HEAVY.get(),.8F,.9F);','playSound(weaponCue(false),heavy?.8F:.55F,1);'),
 ('action(5);shots=0;}', 'action(5);shots=0;playSound(ModSounds.BANDIT_BOW_DRAW.get(),.4F,1);}')]
def source_edit(s):
 for before,after in REPLACEMENTS:
  assert s.count(before)==1,before
  s=s.replace(before,after,1)
 return s
