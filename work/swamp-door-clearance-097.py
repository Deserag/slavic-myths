from pathlib import Path
p=Path('tools/swamp_resources_097.py');s=p.read_text(encoding='utf-8').replace("b.box((x+3,2,z+6),(x+3,3,z+6),'air')","b.box((x+3,2,z+6),(x+4,3,z+6),'air')");p.write_text(s,encoding='utf-8')
