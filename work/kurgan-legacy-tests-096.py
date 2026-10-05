from pathlib import Path
p=Path('tools/kurgan-tests/org/slavicmyths/verify/KurganHeadless.java');s=p.read_text();s=s.replace('KurganPlan.create(','KurganPlan.createLegacy(');p.write_text(s)
