"""Regenerate only the character meshes/textures touched by the 2026-10-12 feedback.

Each generator runs in a fresh interpreter to isolate its material palette.
Does not regenerate items, audio, translations, world data or gameplay resources.
"""
from pathlib import Path
import subprocess,sys
R=Path(__file__).resolve().parents[1]
commands=[
 "import bogatyr_assets as m; m.hero(); m.boss()",
 "import marinka_assets as m; m.hero(); m.witch()",
 "import skim_assets as m; m.hero(); m.beast()",
 "import kalin_assets as m; m.man('kalin_tsar',True); m.man('kalin_common_soldier'); m.man('kalin_elite_soldier',elite=True); m.horse()",
 "from pathlib import Path; scope={'__file__':str(Path('tools/finist_assets.py').resolve())}; exec(Path('tools/finist_assets.py').read_text(encoding='utf8').split('humanoid();humanoid(True);falcon();props();props(True)')[0],scope); scope['humanoid'](); scope['humanoid'](True)",
]
for command in commands:
 subprocess.run([sys.executable,'-c',"import sys; sys.path.insert(0,'tools'); "+command],cwd=R,check=True)
