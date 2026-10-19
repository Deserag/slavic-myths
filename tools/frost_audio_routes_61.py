"""Manually reviewed frost state routes; source evidence is not runtime proof."""
from pathlib import Path
import csv,json,hashlib
R=Path(__file__).resolve().parents[1];J='src/main/java/org/slavicmyths/'

def main():
 specs=[]
 def add(prefix,file,items):
  for cue,guard in items:specs.append((prefix+'.'+cue,J+file,cue,guard))
 add('morozko','morozko/Morozko.java',[
  ('ambient','tickCount%220==0'),('step','tickCount%12==0, moving and onGround'),
  ('blizzard_start','action3 or8 begins; release at age50/48'),
  ('blizzard_loop','action3 age50; action8 age48; stormTicks%80==0'),
  ('spike_telegraph','action4 begins'),('breath','action5 begins; cone damage ages16/30/44'),
  ('wave','action6 begins'),('clone_spawn','action7 begins'),('yield','action9 begins in yieldTrial, no normal boss death')])
 add('morozko','morozko/FrostCold.java',[('frost_damage','tier>=3, active aura, damage due;40/60tick interval')])
 add('morozko','morozko/FrostSkills.java',[('mirror_summon','mirror spawn succeeds'),('staff_wave','player action2 passes item/menu/payment gates')])
 add('morozko','client/FrostStreamSound.java',[('staff_stream','client synchronized item-use channel; stops on age>70, invalid user/item/world/menu')])
 add('morozko','morozko/TrialTotem.java',[('trial_call','boss successfully added to server world')])
 add('karachun','karachun/Karachun.java',[
  ('ambient_low_wind','tickCount%240==0'),('dark_frost','tickCount%40==0 and nightTicks>0'),
  ('long_night_cast','action7 begins'),('voice_whisper','action8 begins; intentionally muted source gap'),
  ('touch_hunger','action4 begins'),('touch_frost','action5 begins'),('touch_death','action6 begins'),
  ('last_hour_mark','action9 begins and markerAge40/80'),
  ('last_hour_impact','markerAge>=configured hourWindup'),
  ('yield','action10 begins in yieldTrial; duplicate explicit cue removed, old RNG draw preserved')])
 add('karachun','karachun/NightTotem.java',[('enter_trial','boss successfully added to server world')])
 add('karachun','karachun/NightRaven.java',[
  ('raven_call','state0, tickCount%220==slot*7'),('raven_flap','state0, tickCount%70==slot*3'),
  ('raven_dive','state0 target/visibility/nextDive gates pass; enter state1'),
  ('raven_hit','state2 swept collision/visibility; actual damage succeeds')])
 add('karachun','client/NightClient.java',[
  ('fear_phantom_spawn','client illusion created under fear cap; nextPhantom60ticks'),
  ('fear_phantom_dispel','player swing intersects phantom, phantom removed')])
 add('karachun','karachun/NightCombat.java',[
  ('last_breath','player scythe action1 passes payment, not boss respiration'),
  ('black_harvest','player scythe action2 passes payment'),
  ('life_eater','successful positive damage and LifeReady cooldown'),
  ('wing_activate','accessory equipped; both allied ravens successfully added')])
 add('karachun','karachun/NightStatus.java',[('waning_stage_up','computed tier increases; also NightEvents.notice offhand notification')])
 selection=json.loads((R/'art/audio/project-61/selection.json').read_text(encoding='utf-8'))
 maps=json.loads((R/'src/main/resources/assets/slavicmyths/sounds.json').read_text(encoding='utf-8'))
 rows=[]
 for event,path,cue,guard in specs:
  raw=(R/path).read_bytes();lines=raw.decode('utf-8').splitlines()
  found=[i for i,line in enumerate(lines,1) if '"'+cue+'"' in line];assert found,(event,path)
  recordings=[r for r in selection if r['event']==event]
  rows.append(dict(event=event,callsite=path,line=found[0],source_sha256=hashlib.sha256(raw).hexdigest(),trigger=guard,files=';'.join(r['file'] for r in recordings),seconds=';'.join(str(r['seconds']) for r in recordings),active=bool(maps[event]['sounds']),status='SOURCE_REVIEWED' if recordings else 'BLOCKED_NEEDS_AUDIO_SOURCE',runtime='NOT_RUN',hearing='NOT_LISTENED'))
 assert len(rows)==36 and len({r['event'] for r in rows})==36
 p=R/'docs/audio-review/FROST_NIGHT_ROUTES.csv'
 with p.open('w',encoding='utf-8-sig',newline='') as f:
  w=csv.DictWriter(f,fieldnames=rows[0]);w.writeheader();w.writerows(rows)
 print('36 manually reviewed event routes;35 active,1 source gap; no runtime claim')

if __name__=='__main__':main()
