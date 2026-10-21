"""Prepare the regression renderer from the established actual-source renderer."""
from pathlib import Path
R=Path(__file__).resolve().parents[1]
p=R/'tools/rune104_preview_gl.py'
s=p.read_text().replace('import numpy as np','import sys\nimport numpy as np')
s=s.replace("O=R/'docs/implementation/rune-glow-104'","O=R/'docs/implementation/rune-glow-fix-44'")
s=s.replace("(O/'native-armor-source.json')","(R/'docs/implementation/rune-glow-104/native-armor-source.json')")
s=s.replace('[0,36,58,82]','[0,112,156,200]')
s=s.replace("for name,matrix in [('ModelViewMat',mv),('ProjMat',projection)]:", "layered=mv.copy()\n      if model['armor'] and (not overlay or '--depth-regression' not in sys.argv or count!=1): layered[:3,:]*=0.99975586\n      for name,matrix in [('ModelViewMat',layered),('ProjMat',projection)]:")
s=s.replace('assert 0<diffs[0]<diffs[1]<diffs[2],(label,diffs)', "assert (diffs[0]==0 and diffs[1]>0 and diffs[2]>diffs[1]) if '--depth-regression' in sys.argv and 'chestplate' in label else 0<diffs[0]<diffs[1]<diffs[2],(label,diffs)")
s=s.replace("(O/'source-gpu-metrics.json')","(O/('depth-regression.json' if '--depth-regression' in sys.argv else 'source-gpu-metrics.json'))")
s=s.replace("(O/'SOURCE-GPU-0-3.png')","(O/('DEPTH-REGRESSION.png' if '--depth-regression' in sys.argv else 'SOURCE-GPU-0-3.png'))")
s=s.replace("f' / runes {count}'", "(f' / runes {count}' if '--depth-regression' not in sys.argv else [' / no runes',' / OLD depth mismatch',' / FIXED depth, 2 runes',' / FIXED depth, 3 runes'][count])")
s=s.replace("result['source_gpu_preview']='SOURCE-GPU-0-3.png'", "result['source_gpu_preview']='DEPTH-REGRESSION.png' if '--depth-regression' in sys.argv else 'SOURCE-GPU-0-3.png'")
(R/'tools/rune44_preview_gl.py').write_text(s)
s=(R/'tools/rune104_shader_check.py').read_text().replace("O=R/'docs/implementation/rune-glow-104'","O=R/'docs/implementation/rune-glow-fix-44'").replace('from rune104_preview_gl import render','from rune44_preview_gl import render')
(R/'tools/rune44_shader_check.py').write_text(s)
