"""Render already-computed metrics; never read active game CSVs or claim causal equivalence."""
from pathlib import Path
import json

R=Path(__file__).resolve().parents[2]
O=R/'docs/implementation/performance-2.0'
def number(value,digits=2):
 return f'{value:.{digits}f}' if value is not None else 'NOT_MEASURED'
def mib(value):return number(value/2**20,1)

lines=['# Обычный мир и focused follow-ups: измерения',
 '', 'FPS/CPU/квантили — окно age>600, то есть после первых30s. Полные фазы, включая подготовку и начальные задержки, отдельно сохранены в JSON. Native MSPT — tally Minecraft до Post listeners, не полный CPU всех модов. TPS — по интервалам тиков. Память — used heap, не retained heap.',
 '', 'Динамические пути/камеры/сущности различаются; это наблюдательные сцены с одинаковыми настройками, не изолированная стоимость мода. Failed city locate не считается проверкой города; idle boss не считается боем. См. SCENARIO_NOTES.md и REPORT.md.', '']
data=json.loads((O/'world-metrics.json').read_text(encoding='utf-8'))
for run,record in data.items():
 lines+=['## '+run,'',f"Run COMPLETE={record['complete']}, reload/new-server+marker={record['reload_new_server_marker']}. Условия окна проверяются отдельно для каждой сцены.",'',
 '| Сцена | Конец сцены | Условий кадра, % | FPS | p95 /p99, мс | Render CPU, мс/кадр | Native MSPT avg /p99 /max | TPS | Heap min–max, MiB |',
 '|---|---|---:|---:|---:|---:|---:|---:|---:|']
 for name,s in record['scenes'].items():
  frame=s['frame'];eligible=s['eligible_for_frame_comparison'];ms=s['native_mspt']
  fps=number(s['fps'],1) if eligible else 'INVALID_CONDITIONS'
  tails=number(frame['p95'])+' / '+number(frame['p99']) if frame and eligible else 'INVALID_CONDITIONS'
  cpu=number(s['render_cpu_mean_ms'],3) if eligible else 'INVALID_CONDITIONS'
  lines.append(f"| {name} | {'COMPLETE' if s['scene_complete'] else 'PARTIAL'} | {100*s['window_valid_fraction']:.2f} | {fps} | {tails} | {cpu} | {number(ms['mean'])} / {number(ms['p99'])} / {number(ms['max'])} | {number(s['tps'])} | {mib(s['heap_min'])}–{mib(s['heap_max'])} |")
 lines+=['','| Сцена | Горизонтальный путь / смещение, блоков | Новые /старые chunk-load events | Сущностей, диапазон | Server CPU, доля одного ядра | Кадров >50 /100ms |','|---|---:|---:|---:|---:|---:|']
 for name,s in record['scenes'].items():
  lines.append(f"| {name} | {number(s['path_horizontal_blocks'],1)} / {number(s['net_horizontal_displacement'],1)} | {s['new_chunk_events']} / {s['old_chunk_events']} | {s['server_entities_range']} | {number(s['server_cpu_fraction_one_core'],3)} | {s['frames_over50']} / {s['frames_over100']} |")
 lines+=['','Начальные30s не исчезают из доказательств:', '', '| Полная фаза с подготовкой | Путь XZ /смещение | Новые chunk-load events | Native MSPT max, мс | Raw frame max, мс |','|---|---:|---:|---:|---:|']
 for name,s in record['scenes'].items():
  full=s['full_phase_including_setup']
  lines.append(f"| {name} | {number(full['path_horizontal_blocks'],1)} / {number(full['net_horizontal_displacement'],1)} | {full['new_chunk_events']} | {number(full['native_mspt']['max'])} | {number(full['raw_frame']['max'])} |")
 lines+=['','GPU3D engine utilization и RSS/private bytes процесса находятся в JSON/CSV. Это не GPU frame time и не Java heap. Числа chunk-load events не равны числу всех прошедших generation stages. Полные setup maxima включают намеренные locate/переходы/диагностические screenshots и не атрибутируются production без дополнительного доказательства.','']
(O/'METRICS.md').write_text('\n'.join(lines)+'\n',encoding='utf-8')
print('Rendered metrics for',len(data),'runs')
