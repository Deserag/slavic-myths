from pathlib import Path
p=Path('tools/classes3-tests/org/slavicmyths/verify/Classes3GameTests.java')
s=p.read_text(encoding='utf-8')
s=s.replace('for(int n=0;n<7;n++)p.tick();','for(int n=0;n<7;n++)p.doTick();')
s=s.replace('h.getLevel().addFreshEntity(spider);p.setYRot(0);p.setXRot(0);','h.getLevel().addFreshEntity(spider);p.setYRot(0);p.setXRot(30);')
p.write_text(s,encoding='utf-8')
