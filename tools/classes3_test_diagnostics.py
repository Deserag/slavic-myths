from pathlib import Path
p=Path('tools/class-tests/org/slavicmyths/verify/ClassGameTests.java')
s=p.read_text(encoding='utf-8')
s=s.replace('"Full class loadout lost: "+base','"Full class loadout lost: "+base+" EXPECTED="+expected+" ACTUAL="+ClassState.data(next)')
p.write_text(s,encoding='utf-8')
