from pathlib import Path
p=Path('src/main/java/org/slavicmyths/kurgan/KurganLayout.java');s=p.read_text();s=s.replace('walk(l,-16,a.z,a.y);walk(l,-16,b.z,b.y);','walk(l,-16,a.z,-6);walk(l,-16,b.z,b.y);');p.write_text(s,encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/kurgan/KurganArchitecture.java');s=p.read_text(encoding='utf-8-sig').replace(' void place(){\n  for(var r:plan.rooms)shell(r);',' void place(){\n  buryVestibule();\n  for(var r:plan.rooms)shell(r);');needle=' private void shell(KurganPlan.Room r)';s=s.replace(needle,''' private void buryVestibule(){
  var r=plan.rooms.getFirst();int front=plan.radius;
  // Local earthen shoulders cover the entrance vault; the mound remains earth, not a stone building.
  for(int x=-r.rx-3;x<=r.rx+3;x++)for(int z=r.z-r.rz-3;z<=front;z++){
   int outside=Math.max(Math.max(0,Math.abs(x)-r.rx),Math.max(0,r.z-r.rz-z));int top=Math.max(0,r.height+2-outside*2-Math.max(0,z-r.z-r.rz));
   for(int y=0;y<=top;y++){BlockPos p=pos(x,y,z);if(clip.isInside(p)&&world.getBlockState(p).isAir())put(x,y,z,(y==top?Blocks.GRASS_BLOCK:Blocks.DIRT).defaultBlockState());}
  }
 }
'''+needle);p.write_text(s,encoding='utf-8')
