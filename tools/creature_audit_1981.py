"""Read-only source inventory for the analytical 1.9.8.1 patch.

Not a Java interpreter: expressions and source evidence remain unresolved.
Never edits production sources, resources or balance.
"""
from pathlib import Path
import json
import re

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'src/main/java/org/slavicmyths'
OUT = ROOT / 'docs/audit/creatures-1.9.8.1'
OUT.mkdir(parents=True, exist_ok=True)
lang = json.loads((ROOT / 'src/main/resources/assets/slavicmyths/lang/ru_ru.json').read_text(encoding='utf-8'))
entries = []
evidence = []
for path in sorted(JAVA.rglob('*.java')):
    source = path.read_text(encoding='utf-8')
    for match in re.finditer(r'(?:ModEntities\.)?ENTITIES\.register\(\s*"([a-z0-9_]+)"', source):
        end = source.find(';', match.end())
        declaration = source[match.start():end if end != -1 else len(source)]
        if 'EntityType.Builder' not in declaration:
            continue
        ident = match.group(1)
        entries.append({'id': 'slavicmyths:' + ident, 'name': lang.get('entity.slavicmyths.' + ident, ident),
                        'source': path.relative_to(ROOT).as_posix(), 'line': source.count('\n', 0, match.start()) + 1,
                        'declaration': declaration, 'status': 'SOURCE_INDEX; manual combat audit pending'})
    # Wildlife registrations use a helper, not a literal ENTITIES.register call.
    if path.name == 'ModEntities.java':
        for match in re.finditer(r'=\s*wildlife\("([a-z_]+)"\s*,\s*([^;]+)', source):
            ident = match.group(1)
            entries.append({'id': 'slavicmyths:' + ident, 'name': lang.get('entity.slavicmyths.' + ident, ident),
                            'source': path.relative_to(ROOT).as_posix(), 'line': source.count('\n', 0, match.start()) + 1,
                            'declaration': match.group(0), 'status': 'SOURCE_INDEX; manual combat audit pending'})
    if not any(token in source for token in ('EntityType<', 'LivingIncomingDamageEvent', 'LivingDamageEvent', 'enum BossKind', 'enum Kind', 'Rules {')):
        continue
    lines = []
    for number, line in enumerate(source.splitlines(), 1):
        if re.search(r'Attributes\.|\bhurt\(|\bstrike\(|\battack\(|addEffect\(|ignite|setRemainingFireTicks|cooldown|COOLDOWN|RADIUS|DAMAGE|\bHP\b|\bHEALTH\b|silver|immune|invulner|bleed|spawn|Spawn|enum |\.inflate\(', line, re.I):
            lines.append(f'{number}: {line}')
    if lines:
        evidence.append('## ' + path.relative_to(ROOT).as_posix() + '\n\n```java\n' + '\n'.join(lines) + '\n```\n')
entries.sort(key=lambda row: row['id'])
ids = [row['id'] for row in entries]
assert len(ids) == len(set(ids)), 'Duplicate source registration; inspect manually'
(OUT / 'registry-source-index.json').write_text(json.dumps(entries, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
table = ['# Исходный индекс EntityType', '', 'Это индекс деклараций, не завершённая таблица боевого баланса. Снаряды, транспорт и вспомогательные сущности включены отдельно от будущей классификации существ.', '', '| Название | Registry ID | Декларация |', '|---|---|---|']
table += [f"| {r['name']} | `{r['id']}` | `{r['source']}:{r['line']}` |" for r in entries]
(OUT / 'REGISTRY.md').write_text('\n'.join(table) + '\n', encoding='utf-8')
(OUT / 'SOURCE_EVIDENCE.md').write_text('# Исходные свидетельства\n\nАвтоматические выдержки; наличие строки не доказывает достижимость ветки или итоговый урон. Единицы времени — тики, расстояния нужно проверять на squared comparisons.\n\n' + '\n'.join(evidence), encoding='utf-8')
print(f'Indexed {len(entries)} literal/helper entity registrations; {len(evidence)} evidence files. Manual audit remains pending.')
