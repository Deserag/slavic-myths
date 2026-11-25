"""Compatibility entry point for the accepted desert visual revision."""
import runpy
from pathlib import Path
if __name__=='__main__':
 for name in ['desert_weapon_art_v2.py','desert_bow_assets.py','desert_skill_art_v2.py']:
  runpy.run_path(str(Path(__file__).with_name(name)),run_name='__main__')
