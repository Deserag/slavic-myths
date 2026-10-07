from pathlib import Path
p=Path('src/main/java/org/slavicmyths/swamp/SwampTreeShape.java');s=p.read_text(encoding='utf-8');s=s.replace('int by=height-3-r.nextInt(3),bx=x,bz=z;','int by=height-3-r.nextInt(3);final int branchY=by;Cell anchor=logs.keySet().stream().filter(q->q.y()==branchY).findFirst().orElseThrow();int bx=anchor.x(),bz=anchor.z();');s=s.replace('n<2+r.nextInt(3)','n<2+r.nextInt(2)').replace('kind.equals("willow")?2:3','2');p.write_text(s,encoding='utf-8')
# Exact current entity total, retaining old baseline parity assertions.
p=Path('tools/kurgan-tests/org/slavicmyths/verify/PortRuntimeGameTests.java');s=p.read_text(encoding='utf-8').replace('entities==47','entities==48');p.write_text(s,encoding='utf-8')
