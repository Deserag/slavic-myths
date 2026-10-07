from pathlib import Path
p=Path('tools/verify_static_data_1211.py');s=p.read_text(encoding='utf-8').replace('    refs+=1\n    if value.startswith',"    if isinstance(value,list):\n        require(bool(value),f'{file}: empty {kind} list')\n        for member in value:reference(kind,member,file)\n        return\n    refs+=1\n    if value.startswith");p.write_text(s,encoding='utf-8')
