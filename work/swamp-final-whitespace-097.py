from pathlib import Path
p=Path('src/main/java/org/slavicmyths/swamp/SwampPiece.java');p.write_text(p.read_text(encoding='utf-8').rstrip()+'\n',encoding='utf-8')
