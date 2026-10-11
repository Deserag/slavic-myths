from pathlib import Path
import numpy as np, subprocess, json, wave, math, random, os
root=Path('/mnt/data/Slavic_Myths_1.6.4_Kuzya_Codex_TZ'); raw=root/'audio'/'raw_wav'; raw.mkdir(parents=True,exist_ok=True)
fs=22050; rng=np.random.default_rng(20261010)
entries=[]
def smooth_envelope(n, attack=.03,release=.1):
    env=np.ones(n); a=min(n//2,int(n*attack)); r=min(n//2,int(n*release));
    if a: env[:a]=np.sin(np.linspace(0,np.pi/2,a))**2
    if r: env[-r:]=np.cos(np.linspace(0,np.pi/2,r))**2
    return env
def wavwrite(path,s):
    s=np.array(s,dtype=np.float64); s=np.nan_to_num(s); s=np.tanh(s*1.25)*0.85
    path.parent.mkdir(parents=True,exist_ok=True)
    with wave.open(str(path),'wb') as f:
        f.setnchannels(1);f.setsampwidth(2);f.setframerate(fs);f.writeframes((s*32767).astype('<i2').tobytes())
def convert(stem,signal,category,event):
    src=raw/(stem+'.wav'); dest=root/'audio'/'ready'/category/(stem+'.ogg');wavwrite(src,signal)
    subprocess.run(['ffmpeg','-hide_banner','-loglevel','error','-y','-i',str(src),'-c:a','libvorbis','-q:a','5',str(dest)],check=True)
    entries.append({'file':str(dest.relative_to(root)),'event':event,'category':category,'seconds':round(len(signal)/fs,2),'status':'READY generated original'})
def noise(n,color='pink'):
    x=rng.standard_normal(n)
    if color=='pink': x=np.cumsum(x);x-=np.mean(x);x/=max(np.std(x),1e-8)
    return x
def pulses(duration,centers,base=1.0):
    n=int(fs*duration); y=np.zeros(n); t=np.arange(n)/fs
    for c in centers:
        width=.02+float(rng.random())*.035
        m=np.exp(-np.maximum(t-c,0)/(width*1.3))*(t>=c)*np.exp(-np.maximum(c-t,0)/.003)
        tex=(rng.standard_normal(n)*.32+np.sin(2*np.pi*(100+float(rng.random())*55)*t)*.25)
        y+=m*tex*base
    return y
# bodily / environmental Foley
specs=[
('step_wood_01','kuzya.step_wood',.33,[.07],.8),('step_wood_02','kuzya.step_wood',.37,[.09],.9),('step_wood_03','kuzya.step_wood',.32,[.06],.7),
('stove_scramble_01','kuzya.stove_climb',1.0,[.10,.3,.54,.79],.65),('stove_scramble_02','kuzya.stove_climb',1.2,[.06,.27,.43,.68,.95],.5),
('cloth_rustle_01','kuzya.cloth',.65,[.13,.26,.43],.3),('cloth_rustle_02','kuzya.cloth',.72,[.07,.29,.53],.25),
('sitting_down_01','kuzya.sit',.67,[.26,.43],.35),('wake_rustle_01','kuzya.wake',1.03,[.22,.5,.72],.24),
('bowl_clink_01','kuzya.bowl',.48,[.12,.22],.45),('bowl_clink_02','kuzya.bowl',.57,[.11,.35],.43),
('chew_01','kuzya.chew',.47,[.10,.24,.36],.2),('chew_02','kuzya.chew',.55,[.13,.32,.4],.22),('chew_03','kuzya.chew',.54,[.13,.29,.43],.2),
('swallow_01','kuzya.swallow',.6,[.17,.28],.13),('stove_settle_01','kuzya.stove_settle',1.0,[.2,.35,.72],.22),
('trunk_wood_01','kuzya.chest',.87,[.1,.35,.65],.45),('broom_sweep_01','kuzya.sweep',1.2,[.22,.58,.94],.2),
]
for stem,event,dur,centers,vol in specs: convert(stem,pulses(dur,centers,vol)*smooth_envelope(int(fs*dur),.01,.12),'sfx',event)
# synth non verbal breath / snores / grumbles
for name,ev,dur,kind in [
('sleep_breath_01','kuzya.sleep_breath',2.35,'breath'),('sleep_breath_02','kuzya.sleep_breath',2.9,'breath'),('snore_01','kuzya.snore',1.15,'snore'),('snore_02','kuzya.snore',1.65,'snore'),
('sleepy_sigh_01','kuzya.sigh',1.1,'sigh'),('grumble_01','kuzya.grumble_nonverbal',.95,'grumble'),('grumble_02','kuzya.grumble_nonverbal',1.2,'grumble'),
('annoyed_huff_01','kuzya.huff',.65,'huff'),('annoyed_huff_02','kuzya.huff',.84,'huff'),('happy_hum_01','kuzya.hum',1.1,'hum')]:
    n=int(dur*fs);t=np.arange(n)/fs
    slow=np.sin(np.pi*np.minimum(t/dur,1))**2
    pink=noise(n,'pink')
    if kind in ['breath','sigh','huff']:
        y= (pink*.14+np.sin(2*np.pi*120*t)*.04)*slow
    elif kind=='snore':
        pulse=(np.sin(2*np.pi*(60+15*np.sin(t*4))*t)*.18+ pink*.08)
        y=pulse*(.15+.85*np.sin(np.pi*t/dur)**2)
    else:
        freq=105+45*np.sin(t*6)+22*np.sin(t*16)
        phase=2*np.pi*np.cumsum(freq)/fs
        y=(np.sin(phase)*.17+np.sin(phase*2)*.07+pink*.055)*slow
    convert(name,y,'sfx',ev)
# vocal: entirely new, original Russian lines, NOT cartoon dialogue; eSpeak is intelligible draft, not actor voice
lines={
'voice_enter_01':('kuzya.voice_enter','Кто это у нас пожаловал?'),
'voice_enter_02':('kuzya.voice_enter','Дверь-то за собой прикрой.'),
'voice_idle_01':('kuzya.voice_idle','Порядок в доме сам собой не заведётся.'),
'voice_idle_02':('kuzya.voice_idle','Опять всё не на месте.'),
'voice_eat_01':('kuzya.voice_eat','Вот бы блинов со сметаной!'),
'voice_eat_02':('kuzya.voice_eat','Кашка-то остыла. Эх!'),
'voice_eat_03':('kuzya.voice_eat','Люблю, когда сытно да тепло.'),
'voice_sleep_01':('kuzya.voice_sleep','Не мешай дремать.'),
'voice_sleep_02':('kuzya.voice_sleep','Дай мне ещё немножко поспать.'),
'voice_notice_01':('kuzya.voice_notice','Ты надолго к нам?'),
'voice_notice_02':('kuzya.voice_notice','Чего всё оглядываешься?'),
'voice_ask_leave_01':('kuzya.voice_ask_leave','Гость-то гость, а пора бы и честь знать.'),
'voice_ask_leave_02':('kuzya.voice_ask_leave','Долго ещё гостить будешь?'),
'voice_ask_leave_03':('kuzya.voice_ask_leave','Чужому тут ночевать не положено.'),
'voice_ask_leave_04':('kuzya.voice_ask_leave','Всё ходишь и ходишь, отдыха нет.'),
'voice_ask_leave_05':('kuzya.voice_ask_leave','Ну, ступай уже, у меня забот полно.'),
'voice_yaga_01':('kuzya.voice_yaga','Бабушка, а пирожки где?'),
'voice_yaga_02':('kuzya.voice_yaga','Я тут за порядком присматриваю.'),
'voice_interact_01':('kuzya.voice_interact','Чего тебе надо?'),
'voice_interact_02':('kuzya.voice_interact','А поговорить-то можно и после обеда.'),
}
for name,(ev,phrase) in lines.items():
    tmp=raw/(name+'.wav')
    proc=subprocess.run(['espeak','-v','ru','-s','148','-p','38','-a','140','-w',str(tmp),phrase],capture_output=True,text=True)
    if proc.returncode!=0:raise Exception(proc.stderr)
    out=root/'audio'/'ready'/'voice_draft'/(name+'.ogg');subprocess.run(['ffmpeg','-hide_banner','-loglevel','error','-y','-i',str(tmp),'-af','highpass=f=115,lowpass=f=5300,acompressor=threshold=-24dB:ratio=2:attack=5:release=80,volume=1.35','-c:a','libvorbis','-q:a','5',str(out)],check=True)
    entries.append({'file':str(out.relative_to(root)),'event':ev,'category':'voice_draft','line':phrase,'status':'READY synthetic ORIGINAL non-copyrighted Russian draft; improve voice if desired'})
(root/'manifests'/'AUDIO_FILES.json').write_text(json.dumps(entries,ensure_ascii=False,indent=2),encoding='utf8')
print('Generated',len(entries),'ogg',sum((root/e['file']).stat().st_size for e in entries)//1024,'KiB')
