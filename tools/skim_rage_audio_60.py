"""Distinct licensed rage take, selected under the owner's autonomous mandate."""
from pathlib import Path
import json, hashlib, shutil
from audio_60_foley import stable_text
R=Path(__file__).resolve().parents[1];P=R/'art/audio/pack-a-60/voice-rework';A=R/'src/main/resources/assets/slavicmyths'
def restore_selected():
 source=P/'preview/skim_rage.ogg';expected='c07b7982dcc6205ac9c4d51d7d4d089d363043b5f686ac24be40607c962a76f8'
 assert hashlib.sha256(source.read_bytes()).hexdigest()==expected
 dest=A/'sounds/voice/skim/rage.ogg'
 if not dest.exists() or dest.read_bytes()!=source.read_bytes():shutil.copyfile(source,dest)
 path=A/'sounds.json';events=json.loads(path.read_text(encoding='utf-8'));events['skim.rage']={'subtitle':'subtitles.slavicmyths.skim.rage','sounds':[{'name':'voice/skim/rage','volume':.65,'attenuation_distance':28}]};stable_text(path,json.dumps(events,ensure_ascii=False,indent=2)+'\n')
 report=dict(mob='Ским-зверь',role='Враждебный босс',action='Переход в ярость, фаза3',source='sources/skim_monster_2.flac',file='preview/skim_rage.ogg',sha256=expected,event='skim.rage',callsite='SkimBeastEntity phase3 && !rageUsed; RAGE64 ticks; cue once',seconds=3,min_runtime_pitch=.96,max_duration_at_min_pitch=3/.96,status='AGENT_SELECTED_UNDER_OWNER_DELEGATION',owner_approved=False,runtime_accepted=False,license='CC-BY-3.0, Little Robot Sound Factory; documented Monster_02.wav, not the approved roar take')
 stable_text(P/'alternatives/skim_rage_selection.json',json.dumps(report,ensure_ascii=False,indent=2)+'\n')
if __name__=='__main__':restore_selected()
