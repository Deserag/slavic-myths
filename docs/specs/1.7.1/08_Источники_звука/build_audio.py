from pathlib import Path
import numpy as np, wave, subprocess, tempfile, json, math
root=Path(__file__).resolve().parents[1]/'05_Звуки';sr=22050
rng=np.random.default_rng(1711)

def env(n,attack=.03,decay=.3):
 t=np.linspace(0,1,n,endpoint=False);return np.clip(t/max(attack,0.002),0,1)*np.power(np.clip(1-t,0,1),decay*4)
def sine(f,n,phase=0):return np.sin(2*np.pi*f*np.arange(n)/sr+phase)
def noise(n,lo=0.0):
 a=rng.normal(0,1,n)
 if lo:
  k=max(2,round(sr/lo));a=np.convolve(a,np.ones(k)/k,mode='same')
 return a

def synth(kind,d):
 n=int(sr*d);t=np.arange(n)/sr;v=np.zeros(n)
 if kind=='heavy_step':
  v=.6*noise(n,420)*np.exp(-t*17)+.18*sine(55,n)*np.exp(-t*11)
 elif kind=='mace_swing':
  v=.4*noise(n,1400)*np.clip((t-.06)*16,0,1)*np.clip((d-t)*9,0,1)+.2*sine(130+180*t,n)*env(n)
 elif kind=='metal_hit':
  v=.35*noise(n,4600)*np.exp(-t*21)
  for f in (315,525,850,1275):v+=.15*sine(f,n)*np.exp(-t*(4+f/360))
 elif kind=='shield_raise':
  v=.4*noise(n,3300)*np.exp(-((t-.15)/.08)**2)+.33*sine(275,n)*np.exp(-np.clip(t-.15,0,None)*10)*(t>.15)
 elif kind=='shield_block':
  v=.6*noise(n,2600)*np.exp(-t*25)+.18*sine(92,n)*np.exp(-t*12)
 elif kind=='grunt':
  freq=90+12*np.sin(t*8); phase=np.cumsum(freq)/sr
  v=.26*(np.sin(2*np.pi*phase)+.35*np.sin(4*np.pi*phase))+.06*noise(n,370)
  v*=env(n,.1,.4)
 elif kind=='taunt':
  f=135-42*t/d+7*np.sin(t*22);phase=np.cumsum(f)/sr
  v=(.25*np.sin(2*np.pi*phase)+.09*np.sin(4*np.pi*phase)+.06*noise(n,680))*env(n,.09,.2)
  v+=.1*sine(85,n)*env(n,.15,.2)
 elif kind=='heal_drink':
  pulses=np.zeros(n)
  for i,sec in enumerate((.08,.19,.34,.53,.74)):
   if sec<d:pulses+=.25*noise(n,1100)*np.exp(-((t-sec)/.028)**2)
  v=pulses+.1*sine(170,n)*env(n)
 elif kind=='recover':
  v=(.18*sine(92,n)+.05*noise(n,550))*env(n,.12,.12)
 elif kind=='growl':
  freq=53+25*np.sin(t*2.3)+6*np.sin(t*11); phase=np.cumsum(freq)/sr
  v=(.3*np.sin(2*np.pi*phase)+.1*np.sin(6*np.pi*phase)+.14*noise(n,400))*env(n,.12,.18)
 elif kind=='roar':
  freq=75-35*t/d+12*np.sin(t*7);phase=np.cumsum(freq)/sr
  v=(.25*np.sin(2*np.pi*phase)+.12*np.sin(6*np.pi*phase)+.19*noise(n,900))*env(n,.2,.15)
 elif kind=='giant_punch':
  v=.66*noise(n,420)*np.exp(-((t-.2)/.07)**2)+.26*sine(40,n)*np.exp(-np.maximum(0,t-.18)*10)*(t>.18)
 elif kind=='stomp_inner':
  v=.85*noise(n,290)*np.exp(-((t-.15)/.08)**2)+.3*sine(34,n)*np.exp(-np.maximum(0,t-.16)*5)*(t>.16)
 elif kind=='stomp_mid':
  v=.53*noise(n,380)*np.exp(-((t-.16)/.09)**2)+.16*sine(45,n)*np.exp(-np.maximum(0,t-.15)*7)*(t>.15)
 elif kind=='stomp_outer':
  v=.32*noise(n,480)*np.exp(-((t-.19)/.10)**2)+.06*sine(56,n)*np.exp(-np.maximum(0,t-.19)*9)*(t>.19)
 elif kind=='grab':
  v=.2*noise(n,1300)*env(n,.08,.7)+.22*sine(78,n)*env(n,.2,.4)
 elif kind=='throw':
  v=.37*noise(n,980)*env(n,.08,.6)+.26*sine(95,n)*np.exp(-((t-.22)/.13)**2)
 elif kind=='chew':
  for sec in (.14,.42,.71,.98):v+=.26*noise(n,500)*np.exp(-((t-sec)/.068)**2)
  v+=.1*sine(100,n)*env(n)
 elif kind=='breath':
  v=.32*noise(n,2400)*env(n,.16,.15)+.12*sine(64,n)*env(n,.4,.3)
 elif kind=='rush':
  v=.22*noise(n,1300)*np.clip(t/d,0,1)**1.5+.25*sine(65,n)*env(n,.2,.06)
 elif kind=='spawn':
  v=.32*noise(n,1100)*env(n,.05,.15)+.19*sine(73,n)*env(n,.05,.3)
 elif kind=='rage':
  freq=64+50*t/d+8*np.sin(t*15);phase=np.cumsum(freq)/sr
  v=(.28*np.sin(2*np.pi*phase)+.16*np.sin(6*np.pi*phase)+.25*noise(n,1300))*env(n,.06,.12)
 elif kind=='death':
  freq=87*(1-.56*t/d);phase=np.cumsum(freq)/sr
  v=(.28*np.sin(2*np.pi*phase)+.23*noise(n,750))*env(n,.1,.08)
 # room ambience short diffuse trail
 y=v.copy()
 for delay,gain in ((.043,.12),(.087,.08),(.134,.055)):
  s=int(delay*sr)
  if s<n:y[s:]+=v[:-s]*gain
 # soft saturation and endpoint fade
 y=np.tanh(y*1.2); y*=np.clip((d-t)/.035,0,1);y*=np.clip(t/.008,0,1)
 peak=np.max(np.abs(y)) or 1
 return np.int16(np.clip(y*.88/max(peak,1.0),-1,1)*32767)

samples={
 'ilya_muromets': [('ambient_breath_01','recover',1.1),('step_heavy_01','heavy_step',.44),('shield_raise_01','shield_raise',.53),('shield_block_01','shield_block',.60),('shield_block_02','metal_hit',.54),('mace_swing_01','mace_swing',.63),('mace_impact_01','metal_hit',.72),('taunt_nonverbal_01','taunt',1.4),('pain_nonverbal_01','grunt',.77),('pain_nonverbal_02','grunt',.94),('unyielding_nonverbal_01','taunt',1.65),('retreat_breath_01','recover',1.25),('herbal_drink_01','heal_drink',1.1),('recover_breath_01','recover',1.48),('return_nonverbal_01','taunt',1.1)],
 'idolishche_poganoe':[('step_heavy_01','heavy_step',.70),('idle_growl_01','growl',2.3),('idle_growl_02','growl',1.65),('punch_impact_01','giant_punch',.88),('stomp_near_01','stomp_inner',1.15),('stomp_mid_01','stomp_mid',1.07),('stomp_outer_01','stomp_outer',1.03),('grasp_01','grab',.91),('throw_01','throw',.96),('eat_01','chew',1.37),('poison_breath_01','breath',2.85),('rush_charge_01','rush',1.72),('minions_summon_01','spawn',1.56),('hurt_01','growl',.98),('hurt_02','roar',1.25),('rage_01','rage',2.52),('death_01','death',3.7),('roar_01','roar',2.45)]}
manifest=[]
with tempfile.TemporaryDirectory() as tmp:
 for character,entries in samples.items():
  for name,kind,dur in entries:
   out=root/character/(name+'.ogg'); tmpwav=Path(tmp)/(name+'.wav')
   a=synth(kind,dur)
   with wave.open(str(tmpwav),'wb') as w:
    w.setnchannels(1);w.setsampwidth(2);w.setframerate(sr);w.writeframes(a.tobytes())
   cp=subprocess.run(['ffmpeg','-hide_banner','-loglevel','error','-y','-i',str(tmpwav),'-c:a','libvorbis','-q:a','5',str(out)],capture_output=True,text=True)
   if cp.returncode:raise RuntimeError(cp.stderr)
   manifest.append({'file':str(out.relative_to(root)),'id':'slavicmyths:entity.'+character+'.'+name,'type':'OGG Vorbis, mono, 22050 Hz','duration_s':dur,'source':'synthetic-original-2026','rights':'original generated for this package; may be redistributed with Slavic Myths','notes':'procedural effect; nonverbal; not an excerpt from a cartoon'})
(root/'manifest.json').write_text(json.dumps({'sample_count':len(manifest),'files':manifest},ensure_ascii=False,indent=2),encoding='utf8')
print('Generated',len(manifest),'original ogg clips')
