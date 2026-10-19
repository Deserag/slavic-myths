"""Bounded audio-only priorities; exact source edit, unchanged explicit RNG draws."""
NIGHTINGALE='src/main/java/org/slavicmyths/bandit/NightingaleEntity.java'
NOCHNITSA='src/main/java/org/slavicmyths/nochnitsa/NochnitsaEntity.java'
PATHS={NIGHTINGALE,NOCHNITSA}

def edit(path,text):
 if path==NIGHTINGALE:
  marker='@Override protected SoundEvent getAmbientSound(){return action()!=IDLE?null:'
  assert text.count(marker)==1
  return text.replace(marker,'@Override protected SoundEvent getAmbientSound(){return action()!=IDLE||getTarget()!=null?null:')
 if path==NOCHNITSA:
  marker='    private void cue(String name,float volume){playSound(NochnitsaContent.sound(name),volume,1+(random.nextFloat()-.5F)*.10F);}'
  assert text.count(marker)==1
  replacement='''    private long secondaryVoiceAfter,hurtVoiceAfter;
    private void cue(String name,float volume){
        float pitch=1+(random.nextFloat()-.5F)*.10F; // Preserve the old draw even when an audio cue is suppressed.
        long now=level().getGameTime();
        if((name.equals("ambient")||name.equals("breath"))&&now<secondaryVoiceAfter)return;
        if(name.equals("hurt")&&now<hurtVoiceAfter)return;
        int ticks=switch(name){case "ambient"->20;case "breath"->21;case "target_spotted","scream_release"->22;case "scream_inhale"->14;case "scream_interrupt"->9;case "hurt"->18;case "death"->30;default->0;};
        if(ticks>0)secondaryVoiceAfter=now+ticks;
        if(name.equals("hurt"))hurtVoiceAfter=now+18;
        playSound(NochnitsaContent.sound(name),volume,pitch);
    }'''
  return text.replace(marker,replacement)
 raise ValueError(path)
