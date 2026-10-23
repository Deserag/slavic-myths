"""Owner-approved C/D, 22.10.2026: exact Veles acquisition recipe only."""
from pathlib import Path
import json
RECIPE={'type':'slavicmyths:armorer_shapeless','ingredients':[{'item':'slavicmyths:'+name} for name in ('carved_staff','leshy_heart','fern_flower','silver_fitting')],'result':{'id':'slavicmyths:veles_staff','count':1}}
def apply(root):
    path=Path(root)/'src/main/resources/data/slavicmyths/recipe/weapon_veles_staff_assembly.json'
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(RECIPE,indent=2)+'\n')
if __name__=='__main__':apply(Path(__file__).resolve().parents[1])
