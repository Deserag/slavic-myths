from pathlib import Path
import os
import numpy as np, subprocess, wave, tempfile
from scipy.signal import butter,sosfilt
ROOT=Path(__file__).resolve().parents[2]
OUT=Path(os.environ.get('SAND_KING_RESOURCE_ROOT', ROOT/'src/main/resources'))/'assets/slavicmyths/sounds/desert/sand_king';OUT.mkdir(parents=True,exist_ok=True)
SR=24000;rng=np.random.default_rng(71125)
def noise(n,lo,hi):
    return sosfilt(butter(2,[lo,hi],btype='bandpass',fs=SR,output='sos'),rng.normal(0,1,n))
def env(t,a,d):return np.minimum(1,t/max(a,.001))*np.exp(-t/max(d,.001))
def pulse(t,start,lo=450,hi=4500,decay=.05):
    dt=np.maximum(0,t-start);return noise(len(t),lo,hi)*env(dt,.003,decay)*(t>=start)
def tone(t,f0,f1,decay):
    phase=2*np.pi*np.cumsum(np.linspace(f0,f1,len(t)))/SR
    return np.sin(phase)*env(t,.025,decay)
def synth(name,dur):
    t=np.arange(int(dur*SR))/SR;n=len(t);grain=noise(n,800,6500);low=noise(n,45,350);mid=noise(n,180,1800)
    if name=='ambient':x=(.55*mid+.28*grain)*(0.65+.35*np.sin(2*np.pi*7*t))*env(t,.16,.55)+.15*tone(t,90,64,.45)
    elif name=='alert':x=.4*low*env(t,.03,.55)+.3*mid*env(t,.04,.6)+.3*tone(t,75,140,.55)+.45*grain*env(t,.08,.4)
    elif name=='hurt':x=.6*mid*env(t,.015,.20)+.4*tone(t,210,74,.22)+.45*pulse(t,.035,900,6000,.06)
    elif name=='death':x=.6*low*env(t,.04,.85)+.25*tone(t,105,28,.8)+.45*mid*env(t,.025,.55)+sum(.2*pulse(t,s,600,4500,.08) for s in (.1,.27,.48,.8))
    elif name=='step':x=.45*low*env(t,.007,.075)+.45*pulse(t,.01,500,5700,.07)+.3*tone(t,95,46,.05)
    elif name=='sting':x=.5*grain*np.sin(np.pi*np.minimum(1,t/dur))**2+.35*tone(t,160,500,.27)+.3*pulse(t,dur*.72,1100,6500,.05)
    elif name=='poison':x=.75*grain*env(t,.08,.55)*(.65+.35*np.sin(2*np.pi*17*t))+sum(.1*pulse(t,s,300,1700,.05) for s in (.1,.25,.46,.7))
    elif name=='claw':x=.85*pulse(t,.035,450,6900,.045)+.55*low*env(t,.007,.14)+.4*tone(t,115,48,.09)
    elif name=='jump':x=.45*mid*env(t,.03,.24)+.55*grain*np.sin(np.pi*t/dur)**2+.35*tone(t,70,150,.30)
    elif name=='land':x=.85*low*env(t,.005,.4)+.62*tone(t,75,25,.25)+.52*pulse(t,.025,300,6500,.25)
    elif name=='slam_charge':x=(.6*low+.32*mid+.2*grain)*(t/dur)**1.4*(.7+.3*np.sin(2*np.pi*(3*t+2*t*t)))+.38*tone(t,43,95,4)*(t/dur)
    elif name=='slam':x=.9*low*env(t,.008,.65)+.65*tone(t,68,23,.6)+.48*pulse(t,.01,220,5000,.38)+.22*pulse(t,.19,100,1000,.55)
    else:x=.6*mid*env(t,.03,.75)+.4*low*env(t,.05,.8)+.4*tone(t,68,120,.6)+.24*grain*env(t,.15,.8)
    x=np.tanh(x*1.7);x*=np.minimum(1,t/.015)*np.minimum(1,(dur-t)/.08);x=.78*x/max(.001,np.max(np.abs(x)))
    with tempfile.TemporaryDirectory() as td:
        f=Path(td)/'sound.wav'
        with wave.open(str(f),'wb') as w:w.setnchannels(1);w.setsampwidth(2);w.setframerate(SR);w.writeframes((x*32767).astype('<i2').tobytes())
        subprocess.run(['ffmpeg','-hide_banner','-loglevel','error','-y','-i',str(f),'-c:a','libvorbis','-q:a','4',str(OUT/(name+'.ogg'))],check=True)
for name,dur in {'ambient':1.5,'alert':1.3,'hurt':.65,'death':2.4,'step':.38,'sting':.9,'poison':1.45,'claw':.55,'jump':.8,'land':1.35,'slam_charge':2.5,'slam':2.2,'rage':1.7}.items():synth(name,dur)
print('13 original mono OGG sound effects')
