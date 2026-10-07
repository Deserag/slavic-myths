from pathlib import Path
p=Path('tools/verify_kurgan_096.py');s=p.read_text(encoding='utf-8-sig').replace('.read_text()',".read_text(encoding='utf-8')").replace("write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\\n')", "write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\\n',encoding='utf-8')");p.write_text(s,encoding='utf-8')
