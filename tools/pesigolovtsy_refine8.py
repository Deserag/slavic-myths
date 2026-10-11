from pathlib import Path
import json,shutil
R=Path(__file__).resolve().parents[1];S=Path(json.loads((R/'work/pesigolovtsy/snapshot-location.json').read_text())['path'])
def edit(path,a,b):
 p=R/path;s=p.read_text(encoding='utf8');assert s.count(a)==1,(path,a[:70],s.count(a));p.write_text(s.replace(a,b),encoding='utf8');shutil.copy2(p,S/path)
edit('src/main/java/org/slavicmyths/pesigolovtsy/DogheadPose.java','0,-2,-8,-8,-5,0,4,3,0','0,-2,-8,-8,-5,-2,4,3,0')
edit('src/main/java/org/slavicmyths/pesigolovtsy/DogheadPose.java','new float[]{-8,-5,-3,-3,-5,-11,-8,-8,-8}','new float[]{-8,-5,-3,-3,-5,-12.2F,-8,-8,-8}')
edit('src/main/java/org/slavicmyths/client/DogheadClient.java','n("spine_upper").z-=4.3F*drive','n("spine_upper").z-=3.2F*drive')
print('Contact fixes copied only to release snapshot and owned source files.')
