from pathlib import Path
root=Path(__file__).resolve().parents[1]
review=Path(r'C:\Users\pavel\.codex\worktrees\classes-3-review\slavic-myths')
changes={
 'src/main/java/org/slavicmyths/rpg/classes/ClassUltimateRuntime.java': [('p.isSpectator()||p.isPassenger()||p.getAbilities().flying','p.isSpectator()||p.isPassenger()||p.isFallFlying()||p.getAbilities().flying')],
 'src/main/java/org/slavicmyths/rpg/classes/ClassTrialService.java': [('(ally instanceof Villager||ally.isAlliedTo(p))','ClassUltimateRuntime.friendly(p,ally)')],
 'src/main/java/org/slavicmyths/rpg/classes/ClassPassiveEvents.java': [('!e.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY))','!e.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)&&!e.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_EFFECTS))')],
}
for name,replacements in changes.items():
 p=root/name;s=p.read_text(encoding='utf-8')
 for old,new in replacements:
  assert old in s,(name,old);s=s.replace(old,new)
 p.write_text(s,encoding='utf-8');(review/name).write_text(s,encoding='utf-8')
