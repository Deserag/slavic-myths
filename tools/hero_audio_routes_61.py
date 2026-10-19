"""Exact audio-only repair; no gameplay random draws or action tick changes."""
ALYOSHA='src/main/java/org/slavicmyths/skim/AlyoshaEntity.java'
FINIST='src/main/java/org/slavicmyths/finist/FinistEntity.java'
WITCH='src/main/java/org/slavicmyths/marinka/WitchActor.java'
FALCON='src/main/java/org/slavicmyths/finist/FalconActor.java'
PATHS={ALYOSHA,FINIST,WITCH,FALCON}

def edit(path,text):
 if path==ALYOSHA:
  marker=' public AlyoshaEntity(EntityType<? extends AlyoshaEntity> t,Level w){super(t,w);}'
  assert text.count(marker)==1
  return text.replace(marker,marker+'\n private int hurtVoiceVariation;\n @Override protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source){return SkimContent.sound("alyosha.hurt_"+((hurtVoiceVariation++&1)+1));}')
 if path==FINIST:
  marker='if(action()==FalconAction.SWORD&&frame(8)&&allowed(committed)&&cone(committed,2.7,95)&&hasLineOfSight(committed))committed.hurt(damageSources().mobAttack(this),5);'
  assert text.count(marker)==1
  return text.replace(marker,'if(action()==FalconAction.SWORD&&frame(8)&&allowed(committed)&&cone(committed,2.7,95)&&hasLineOfSight(committed)){if(committed.hurt(damageSources().mobAttack(this),5))playSound(FinistContent.sound("entity.finist.sword_contact"),.8F,1);}')
 if path==WITCH:
  marker='    public void cue(String name){if(!level().isClientSide)playSound(MarinkaContent.sound((this instanceof DobrynyaEntity?"dobrynya":"marinka")+"."+name),name.contains("step")?.5F:.9F,.97F+random.nextFloat()*.06F);}'
  assert text.count(marker)==1
  replacement='''    private int armorVoiceVariation,spearVoiceVariation,wingVoiceVariation;
    public void cue(String name){if(!level().isClientSide){
        if(this instanceof DobrynyaEntity){
            if(name.equals("armor_step_01"))name="armor_step_0"+((armorVoiceVariation++&1)+1);
            else if(name.equals("spear_swing_01"))name="spear_swing_0"+((spearVoiceVariation++&1)+1);
        }else if(name.equals("magpie_wings_01"))name="magpie_wings_0"+((wingVoiceVariation++&1)+1);
        playSound(MarinkaContent.sound((this instanceof DobrynyaEntity?"dobrynya":"marinka")+"."+name),name.contains("step")?.5F:.9F,.97F+random.nextFloat()*.06F);
    }}'''
  return text.replace(marker,replacement)
 if path==FALCON:
  marker=' public void cue(String k){if(!level().isClientSide)playSound(FinistContent.sound(k),.8F,.96F+random.nextFloat()*.08F);}'
  assert text.count(marker)==1
  return text.replace(marker,' private int swordVoiceVariation;\n public void cue(String k){if(!level().isClientSide){if(k.equals("entity.finist.sword_swing_1"))k="entity.finist.sword_swing_"+((swordVoiceVariation++&1)+1);playSound(FinistContent.sound(k),.8F,.96F+random.nextFloat()*.08F);}}')
 raise ValueError(path)

if __name__=='__main__':
 from pathlib import Path
 root=Path(__file__).resolve().parents[1]
 for name in sorted(PATHS):
  p=root/name;p.write_text(edit(name,p.read_text(encoding='utf-8')),encoding='utf-8')
