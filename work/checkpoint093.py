from pathlib import Path
header='''# 0.9.3 — промежуточный этап данных (не релиз)

YagaData и verifyYaga добавлены; данные доверия/поручений/блокировки/NBT проверены.
NPC, избушка, GUI, обмен, котёл и предметы ещё НЕ реализованы.
Build, verifyYaga, verifyHunt, verifyKurgan, verify_resources PASS, Minecraft запусков 0.
В PolyMC установлен проверенный checkpoint с прежним номером 0.9.2, а не готовая 0.9.3.
Полная точка продолжения: docs/NEXT_0.9.3.md; hash/backup: docs/verification/yaga-data-checkpoint.json.

---

'''
p=Path('docs/PROJECT_STATUS.md');p.write_text(header+p.read_text('utf8'),'utf8')
p=Path('docs/ROADMAP.md');s=p.read_text('utf8').replace('- 0.9.3: Баба-яга, избушка, задания/обмен/котёл — не начато.', '- 0.9.3: начато; YagaData/NBT/verifyYaga PASS. NPC, избушка, GUI, предметы/услуги — TODO; docs/NEXT_0.9.3.md.');p.write_text(s,'utf8')
p=Path('README.md');p.write_text(header+p.read_text('utf8'),'utf8')
p=Path('docs/ARCHITECTURE.md');p.write_text('# 0.9.3 — данные прогресса (интеграция сервиса TODO)\n\nYagaData — отдельный WorldSavedData slavicmyths_yaga. Хранит каноничные anchor/NPC UUID\nи per-player Favor/stage/active/contractReady/blockedUntil. Stage переходит только\nпри совпадении активного поручения; повторная сдача отклоняется. Уже существующие\nHuntRecords и метла/ступа не меняются. Runtime сервис пока не подключён.\n\n---\n\n'+p.read_text('utf8'),'utf8')
