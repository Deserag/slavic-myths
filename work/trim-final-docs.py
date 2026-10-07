from pathlib import Path
for name in ['README.md','docs/ARCHITECTURE.md','docs/PROJECT_STATUS.md','docs/ROADMAP.md','docs/port/PORT_STATUS_0.9.4.md','tools/verify_static_data_1211.py']:
 p=Path(name);p.write_text(p.read_text(encoding='utf-8').rstrip()+'\n',encoding='utf-8')
