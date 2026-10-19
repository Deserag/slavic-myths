"""Exact audio-only repair; no gameplay random draws or action tick changes."""
ALYOSHA='src/main/java/org/slavicmyths/skim/AlyoshaEntity.java'
FINIST='src/main/java/org/slavicmyths/finist/FinistEntity.java'
PATHS={ALYOSHA,FINIST}

def edit(path,text):
 if path==ALYOSHA:
  marker=' public AlyoshaEntity(EntityType<? extends AlyoshaEntity> t,Level w){super(t,w);}'
  assert text.count(marker)==1
  return text.replace(marker,marker+'\n private int hurtVoiceVariation;\n @Override protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source){return SkimContent.sound("alyosha.hurt_"+((hurtVoiceVariation++&1)+1));}')
 if path==FINIST:
  marker='if(action()==FalconAction.SWORD&&frame(8)&&allowed(committed)&&cone(committed,2.7,95)&&hasLineOfSight(committed))committed.hurt(damageSources().mobAttack(this),5);'
  assert text.count(marker)==1
  return text.replace(marker,'if(action()==FalconAction.SWORD&&frame(8)&&allowed(committed)&&cone(committed,2.7,95)&&hasLineOfSight(committed)){if(committed.hurt(damageSources().mobAttack(this),5))playSound(FinistContent.sound("entity.finist.sword_contact"),.8F,1);}')
 raise ValueError(path)

if __name__=='__main__':
 from pathlib import Path
 root=Path(__file__).resolve().parents[1]
 for name in sorted(PATHS):
  p=root/name;p.write_text(edit(name,p.read_text(encoding='utf-8')),encoding='utf-8')
