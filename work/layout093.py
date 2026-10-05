from pathlib import Path
p=Path('src/main/java/org/slavicmyths/yaga/YagaMenu.java');s=p.read_text('utf8').replace('166+i*28,170','126+i*28,108').replace('308,170','276,108').replace('139+col*18,224+row*18','135+col*18,149+row*18').replace('139+col*18,282','135+col*18,207');p.write_text(s,'utf8')
