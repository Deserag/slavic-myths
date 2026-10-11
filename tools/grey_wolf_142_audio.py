"""Original procedural cue compositions, mono Vorbis. No sampled third-party audio."""
from pathlib import Path
import json, math, random, struct, subprocess, wave
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'src/main/resources/assets/slavicmyths/sounds/wolf_story'
SPECS={
 'dialog_open':(.28,180,520), 'quest_select':(.10,650,580),
 'dialog_page':(.16,330,220), 'quest_accept':(.38,440,660),
 'quest_progress':(.16,550,640), 'quest_complete':(.62,440,880),
 'reward_claim':(.45,660,990), 'reject':(.20,210,150),
 'ring_activate':(.72,170,290), 'ring_pack_arrive':(.30,110,190),
 'ring_expire':(.36,280,120), 'finist_magic_shot':(.24,540,1250),
 'finist_target_lock':(.12,880,1100), 'finist_magic_impact':(.18,620,240),
 'finist_inverted_impact':(.26,360,110), 'quiver_equip':(.24,190,310)}

def main():
 OUT.mkdir(parents=True,exist_ok=True)
 scratch=ROOT/'work/grey-wolf-142/audio';scratch.mkdir(parents=True,exist_ok=True)
 report=[]
 for index,(name,(duration,start,end)) in enumerate(SPECS.items()):
  rng=random.Random(14200+index);rate=24000;count=round(duration*rate)
  samples=[];phase=0;noise=0
  for n in range(count):
   t=n/rate;x=n/count;phase+=2*math.pi*(start+(end-start)*x)/rate
   envelope=min(1,x*24)*((1-x)**2.1)
   noise=.72*noise+.28*rng.uniform(-1,1)
   # Gentle dry metallic/wooden taps, paper/fletching rustle, no synthetic howl.
   tonal=.46*math.sin(phase)+.15*math.sin(phase*2.73)*math.exp(-x*9)
   rustle=noise*(.65 if name in ('dialog_page','quiver_equip','ring_expire','finist_magic_shot') else .25)
   tap=math.sin(phase*4.13)*math.exp(-x*45)*.15
   samples.append((tonal+rustle+tap)*envelope*.45)
  wav=scratch/(name+'.wav')
  with wave.open(str(wav),'wb') as f:
   f.setnchannels(1);f.setsampwidth(2);f.setframerate(rate)
   f.writeframes(b''.join(struct.pack('<h',round(s*32767))for s in samples))
  target=OUT/(name+'.ogg')
  subprocess.run(['ffmpeg','-hide_banner','-loglevel','error','-y','-i',str(wav),'-ac','1','-c:a','libvorbis','-q:a','4',str(target)],check=True)
  report.append({'cue':name,'duration':duration,'channels':1,'sample_rate':rate,'peak_source':max(abs(s)for s in samples),'bytes':target.stat().st_size,'provenance':'original deterministic synthesis, seed 14200 + cue index'})
 (scratch/'manifest.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
 print('Generated',len(report),'original mono OGG cues')
if __name__=='__main__':main()
