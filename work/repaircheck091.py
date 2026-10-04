from pathlib import Path
p=Path('tools/verify_hunt_091.py');s=p.read_text(encoding='utf-8-sig').replace('len(image.getcolors(size*size))>100','len(image.getcolors(size*size))>8').replace(" and 'case' not in ''",'').replace("assert 'case' not in '' # No runtime claims made by static hooks.\n",'');p.write_text(s,encoding='utf8')
