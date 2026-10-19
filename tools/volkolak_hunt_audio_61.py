"""Exact ordinary Volkolak audio dispatch; all combat logic stays identical."""
HUNT='src/main/java/org/slavicmyths/hunt/HuntMob.java'
VOLKOLAK='src/main/java/org/slavicmyths/entity/VolkolakEntity.java'
PATHS={HUNT,VOLKOLAK}
REGISTRATIONS=[('VOLKOLAK_HUNT_RAGE','volkolak_hunt_rage'),('VOLKOLAK_HUNT_HOWL','volkolak_hunt_howl')]

def registry_edit(text):
 marker='    public static final DeferredHolder<SoundEvent,SoundEvent> BANDIT_IDLE'
 assert text.count(marker)==1
 return text.replace(marker,''.join('    public static final DeferredHolder<SoundEvent,SoundEvent> '+field+' = sound("'+event+'");\n' for field,event in REGISTRATIONS)+marker,1)

def edit(path,text):
 if path==HUNT:
  pairs=[
   (' private void cue(SoundEvent sound){playSound(sound,.85F,.65F);}',
    ' private void cue(SoundEvent sound){playSound(sound,.85F,.65F);}\n protected void huntVoice(boolean howl){cue(SoundEvents.RAVAGER_ROAR);}'),
   ('if(oven())ovenVoice(true);else cue(SoundEvents.RAVAGER_ROAR);',
    'if(oven())ovenVoice(true);else huntVoice(false);'),
   ('else cue(move==Move.HOWL?SoundEvents.RAVAGER_ROAR:move==Move.ASH?SoundEvents.FIRE_EXTINGUISH:oven()?SoundEvents.BLAZE_AMBIENT:SoundEvents.WOLF_GROWL);',
    'else if(move==Move.HOWL)huntVoice(true);else cue(move==Move.ASH?SoundEvents.FIRE_EXTINGUISH:oven()?SoundEvents.BLAZE_AMBIENT:SoundEvents.WOLF_GROWL);')]
  for before,after in pairs:
   assert text.count(before)==1,before
   text=text.replace(before,after,1)
  return text
 if path==VOLKOLAK:
  marker=' @Override public boolean oven(){return false;}'
  assert text.count(marker)==1
  return text.replace(marker,marker+'\n @Override protected void huntVoice(boolean howl){playSound((howl?org.slavicmyths.registry.ModSounds.VOLKOLAK_HUNT_HOWL:org.slavicmyths.registry.ModSounds.VOLKOLAK_HUNT_RAGE).get(),.85F,1.0F);}')
 raise ValueError(path)

if __name__=='__main__':
 from pathlib import Path
 root=Path(__file__).resolve().parents[1]
 for path in PATHS:
  p=root/path;p.write_text(edit(path,p.read_text(encoding='utf-8')),encoding='utf-8')
 p=root/'src/main/java/org/slavicmyths/registry/ModSounds.java'
 p.write_text(registry_edit(p.read_text(encoding='utf-8')),encoding='utf-8')
