"""Regenerate V3 meshes and HUD without restoring obsolete pixel extrusions."""
import subprocess
from pathlib import Path
if __name__=='__main__':
    subprocess.run(['node',str(Path(__file__).with_name('desert_visual_geometry_v3.cjs'))],check=True)
