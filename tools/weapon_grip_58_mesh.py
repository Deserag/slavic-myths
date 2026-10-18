"""Post-emit mesh-only grip correction. Reuses existing hand UV; never edits textures/items."""
from pathlib import Path
import re
R=Path(__file__).resolve().parents[1]
def apply_mesh(cls):
 if cls not in ['KarachunMesh','MorozkoMesh']:return
 p=R/f'src/main/java/org/slavicmyths/client/{cls}.java';s=p.read_text(encoding='utf-8');lines=s.splitlines();frost=cls=='MorozkoMesh'
 for i,line in enumerate(lines):
  if line.startswith('var scythe=') or line.startswith('var staff_root='):
   line=re.sub(r'PartPose.offset\([^)]*\)', 'PartPose.offset(0.0000F,2.0000F,0.0000F)',line)
  if line.startswith('var hand_right=') or not frost and line.startswith('var hand_left='):
   u,v=re.search(r'texOffs\((\d+),(\d+)\)',line).groups();half=3.25 if frost else 2.5;gap=2.3 if frost else 1.6;depth=3.5 if frost else 2.5;low=-1 if frost else 0;height=5
   boxes=[(-half,low,gap,2*half,height,depth-gap),(-half,low,-depth,half-gap,height,depth+gap),(gap,low,-depth,half-gap,height,depth+gap),(-gap,low,-depth,2*gap,height,depth-gap)]
   builder='CubeListBuilder.create()'+''.join(f'.texOffs({u},{v}).addBox('+','.join(f'{n:.4f}F' for n in b)+')' for b in boxes)
   line=re.sub(r'CubeListBuilder.create\(\).*?,PartPose\.',builder+',PartPose.',line)
  lines[i]=line
 s='\n'.join(lines)+'\n';p.write_text(s,encoding='utf-8')
if __name__=='__main__':
 for cls in ['KarachunMesh','MorozkoMesh']:apply_mesh(cls)
