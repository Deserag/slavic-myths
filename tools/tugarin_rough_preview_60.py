"""Reproduce the reviewed Tugarin voice revision; never writes production assets."""
from pathlib import Path
import sys, subprocess, json, hashlib

R = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(R / '.tools/audio-libs'))
sys.path.insert(0, str(R / 'tools'))
import numpy as np
import soundfile as sf
from audio_60_foley import encode, RATE, stable_text

P = R / 'art/audio/pack-a-60/voice-rework'
source = P / 'preview/tugarin_challenge.ogg'
dest = P / 'alternatives/tugarin_challenge_rough.ogg'
# Five semitones lower, with shifted formants but unchanged duration. Compress
# the upper-mid shout, then add mild saturation rather than noise or a second voice.
filters = ('rubberband=pitch=0.7491535384:formant=shifted,'
           'highpass=f=65,lowpass=f=4200,'
           'equalizer=f=2400:t=q:w=0.8:g=-5,'
           'equalizer=f=140:t=q:w=0.7:g=4')
raw = subprocess.check_output(['ffmpeg', '-v', 'error', '-i', str(source),
    '-af', filters, '-f', 'f32le', '-ar', str(RATE), '-ac', '1', 'pipe:1'])
x = np.frombuffer(raw, dtype='<f4').astype(float)
x = np.tanh(x * 2.2) / 2.2
x -= x.mean()
x *= min(0.18 / max(np.sqrt(np.mean(x*x)), 1e-12),
         0.82 / max(np.max(np.abs(x)), 1e-12))
encode(dest, x)
y, sr = sf.read(dest)
original, original_sr = sf.read(source)
assert sr == RATE and np.isfinite(y).all() and np.max(np.abs(y)) < .95
assert abs(len(y) / sr - len(original) / original_sr) < .06
accepted = json.loads((P / 'acceptance.json').read_text(encoding='utf-8'))
selected = accepted.get('approved_sha256', {}).get('tugarin_challenge') == hashlib.sha256(dest.read_bytes()).hexdigest()
report = {'creature': 'Тугарин', 'role': 'босс', 'phase': 'человеческая, 1',
          'action': 'боевой вызов при обнаружении противника',
          'purpose': 'низкий грубый угрожающий голос вместо высокого крика',
          'processing': filters + '; mild tanh saturation; gain',
          'source_sha256': hashlib.sha256(source.read_bytes()).hexdigest(),
          'sha256': hashlib.sha256(dest.read_bytes()).hexdigest(),
          'seconds': len(y) / sr, 'peak': float(np.max(np.abs(y))),
          'owner_timbre_approved': selected, 'production_selected': selected,
          'runtime_launches': 0}
stable_text(dest.with_suffix('.json'), json.dumps(report, ensure_ascii=False, indent=2)+'\n')
print(json.dumps(report, ensure_ascii=False, indent=2))
