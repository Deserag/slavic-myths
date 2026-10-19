"""Triage measured matches; sharing a material prop is not a voice clone verdict."""
from pathlib import Path
import json,csv,collections
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';O=R/'docs/audio-review';P=R/'art/audio/project-61'
def main():
 selected={'src/main/resources/assets/slavicmyths/sounds/'+r['file']:r for r in json.loads((P/'selection.json').read_text(encoding='utf-8'))};active=collections.defaultdict(list)
 for e,d in json.loads((A/'sounds.json').read_text(encoding='utf-8')).items():
  for v in d.get('sounds',[]):
   v={'name':v} if isinstance(v,str) else v;n=v['name']
   if v.get('type','sound')!='event' and n.startswith('slavicmyths:'):active['src/main/resources/assets/slavicmyths/sounds/'+n.split(':')[1]+'.ogg'].append(e)
 out=[]
 for row in csv.DictReader((O/'PROJECT_SIMILARITY.csv').open(encoding='utf-8-sig')):
  a,b=row['a'],row['b'];x,y=selected.get(a),selected.get(b)
  def vocal(r):return r and any(s['key'].startswith(('alba__','female_','bear__','boar__','elk__','deer__','aura__','laugh__','hael__','creature__')) for s in r['sources'])
  if not active[a] or not active[b]:decision='INACTIVE_ORIGINAL_RETAINED_NOT_CURRENT_PAIR'
  elif x and y and not vocal(x) and not vocal(y):decision='SHARED_PHYSICAL_PROP_REVIEW_ROLE_NOT_VOICE'
  elif x and y:decision='ACTIVE_RECORDED_VOICE_PAIR_REQUIRES_HEARING'
  else:decision='ACTIVE_LEGACY_OR_MIXED_PAIR_PRIORITY_REVIEW'
  row.update(a_events=';'.join(active[a]),b_events=';'.join(active[b]),triage=decision,interpretation='Measured candidate only; no auditory acceptance or semantic distinction inferred');out.append(row)
 with (O/'SIMILARITY_TRIAGE.csv').open('w',encoding='utf-8-sig',newline='') as f:
  w=csv.DictWriter(f,fieldnames=list(out[0]));w.writeheader();w.writerows(out)
 counts=collections.Counter(r['triage'] for r in out if r['result']=='HIGH_WAVEFORM_MATCH_REVIEW');(P/'similarity-triage.json').write_text(json.dumps(dict(high_match_triage=counts,hearing='NOT_LISTENED',owner_accepted=False),indent=2)+'\n');print(dict(counts))
if __name__=='__main__':main()
