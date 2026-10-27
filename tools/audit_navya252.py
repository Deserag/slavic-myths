"""Read-only production audit for #252; writes evidence, never game assets.

Source inventory is deliberately NOT a runtime registry dump. Rendered swatches
are existing textures, never approved originals or native Minecraft screenshots.
"""
from pathlib import Path
import collections, hashlib, json, re, subprocess
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'docs/implementation/navya252-20261027'
RES = ROOT / 'src/main/resources'
JAVA = 'src/main/java/org/slavicmyths/navya/'
BASE = ['navya_soil', 'mossy_navya_soil', 'navya_stone',
        'cracked_navya_stone', 'grove_bone_block', 'grove_bone_column',
        'grove_bone_spike', 'rooted_navya_stone']

def git(*args):
    return subprocess.check_output(['git', *args], cwd=ROOT).decode('utf-8').strip()

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def main():
    OUT.mkdir(parents=True, exist_ok=True)
    blocks = (ROOT / JAVA / 'NavyaBlocks.java').read_text(encoding='utf-8')
    ids = re.findall(r'\b(?:add|sign)\("([a-z_]+)"', blocks)
    ids += re.findall(r'"(navya_(?:dry_branches|roots|hanging_roots|shrub|growth)|ghost_fungus)"',
                      blocks.split('for(String id:List.of(')[1].split('))')[0])
    assert len(ids) == len(set(ids)), 'duplicate source declarations'
    content = (ROOT / JAVA / 'NavyaContent.java').read_text(encoding='utf-8')
    mobs = re.findall(r'Kind\.\w+,"([a-z_]+)"', content.split('static {')[0])
    entities = mobs + re.findall(r'ENTITIES.register\("([a-z_]+)"', content)
    rows = []
    for name in ids:
        paths = [f'assets/slavicmyths/blockstates/{name}.json']
        if '_wall_' not in name:
            paths += [f'assets/slavicmyths/models/item/{name}.json',
                      f'data/slavicmyths/loot_table/blocks/{name}.json']
        rows.append({'registry': 'block', 'id': 'slavicmyths:' + name,
                     'source': JAVA + 'NavyaBlocks.java',
                     'resources': {p: sha(RES / p) if (RES / p).is_file() else None for p in paths}})
    rows += [{'registry': 'entity_type', 'id': 'slavicmyths:' + name,
              'source': JAVA + 'NavyaContent.java'} for name in entities]
    rows.append({'registry': 'biome', 'id': 'slavicmyths:navya_grove',
                 'source': JAVA + 'NavyaWorldgen.java'})
    rows.append({'registry': 'structure', 'id': 'slavicmyths:navya_heart',
                 'source': 'src/main/resources/data/slavicmyths/worldgen/structure/navya_heart.json'})
    tracked = git('ls-files', 'src/main').splitlines()
    related = [p for p in tracked if any(k in p for k in
               ('/navya/', 'navya_', '/navka', '/rootling', '/bone_crawler',
                '/grove_', '/guardian_branch', '/living_root', '/bone_thorn', '/ghost_fungus'))]
    # Include shared integration resources in preservation evidence.
    related += ['src/main/resources/data/minecraft/dimension_type/the_nether.json',
                'src/main/resources/data/minecraft/worldgen/noise_settings/nether.json']
    related = sorted(set(related))
    comparison = {}
    for ref in ('main', 'hell'):
        tree = dict(line.split(' ', 1)[::-1] for line in
                    git('ls-tree', '-r', '--format=%(objectname) %(path)', ref, 'src/main').splitlines())
        comparison[ref] = {'commit': git('rev-parse', ref),
                           'files': {p: tree.get(p) for p in related}}
    evidence = {'scope': 'source declarations, not runtime registry',
                'head': git('rev-parse', 'HEAD'), 'branch': git('branch', '--show-current'),
                'registrations': rows, 'branch_comparison': comparison,
                'working_files_sha256': {p: sha(ROOT / p) for p in related},
                'runtime_runs': 0, 'approved_originals': 'BLOCKED_REFERENCE'}
    evidence['resource_catalog'] = {p: sha(ROOT / p) for p in related if '/resources/' in p}
    languages = {lang: json.loads((RES / f'assets/slavicmyths/lang/{lang}.json').read_text(encoding='utf-8'))
                 for lang in ('ru_ru', 'en_us')}
    evidence['block_localization'] = {name: {lang: values.get('block.slavicmyths.'+name)
                                           for lang, values in languages.items()} for name in ids}
    evidence['generator_sources'] = {p.as_posix(): sha(ROOT / p) for p in map(Path,
        ('tools/navya_assets.py', 'tools/navya_entity_assets.py', 'tools/navya_worldgen.py',
         'tools/navya_loot.py', 'tools/navya_audio.py'))}
    evidence['generator_warning'] = 'Inventoried only; audio generator NOT executed (quarantine).'
    (OUT / 'inventory.json').write_text(json.dumps(evidence, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
    sheet = Image.new('RGB', (1100, 80 + 8 * 150), '#252525')
    draw = ImageDraw.Draw(sheet)
    draw.text((15, 12), 'EXISTING SOURCE TEXTURES / #252 / NOT APPROVED ORIGINALS / NOT GAME RENDER', fill='white')
    palette = []
    for index, name in enumerate(BASE):
        model = json.loads((RES / f'assets/slavicmyths/models/block/{name}.json').read_text())
        refs = sorted(set(v for v in model.get('textures', {}).values() if v.startswith('slavicmyths:')))
        assert refs, name
        y = 65 + index * 150
        draw.text((15, y), name, fill='white')
        record = {'id': 'slavicmyths:'+name, 'textures': []}
        for j, ref in enumerate(refs):
            path = RES / ('assets/slavicmyths/textures/'+ref.split(':')[1]+'.png')
            im = Image.open(path).convert('RGBA')
            colors = collections.Counter(
                pixel[:3] for pixel in im.getdata() if pixel[3])
            common = ['#%02x%02x%02x' % rgb for rgb, _ in colors.most_common(5)]
            record['textures'].append({'path': path.relative_to(ROOT).as_posix(), 'size': im.size,
                                       'sha256': sha(path), 'dominant_rgb': common})
            x = 300 + j * 240
            sheet.paste(im.resize((96, 96), Image.Resampling.NEAREST), (x, y), im.resize((96,96), Image.Resampling.NEAREST))
            draw.text((x, y+100), path.stem, fill='white')
            for k, color in enumerate(common):
                draw.rectangle((x+105, y+k*19, x+122, y+k*19+16), fill=color)
                draw.text((x+128, y+k*19), color, fill='white')
        palette.append(record)
    sheet.save(OUT / 'existing-palette.png')
    (OUT / 'palette.json').write_text(json.dumps(palette, indent=2)+'\n', encoding='utf-8')
    # Three non-prescriptive cross-sections, intentionally without new Y values.
    for index, title in enumerate(('Upper dead forest', 'Middle root terraces', 'Lower bone caverns')):
        shapes = [
          '<path d="M20 205 Q180 160 310 200 T620 180 L620 250H20Z" fill="#45484b"/>',
          '<path d="M40 190V90M20 115L40 135L70 100M140 190V60M110 90L140 115L170 75M380 190V80M355 105L380 130L410 95" stroke="#ded5c3" stroke-width="9" fill="none"/>',
          '<path d="M20 210H170L220 165H350L420 120H620V250H20Z" fill="#45484b"/><path d="M90 205L190 185L245 160M320 164L395 140L450 115" stroke="#b8b0a1" stroke-width="7" fill="none"/>',
          '<path d="M20 70H620V100L520 80L470 115L420 85L300 100L260 75L150 110L90 80H20Z M20 225L150 205L250 220L350 195L450 210L620 200V250H20Z" fill="#303238"/><path d="M160 205L170 145L183 210 M460 95L470 155L480 96" fill="#ded5c3"/>'
        ]
        selected = shapes[:2] if index == 0 else [shapes[index+1]]
        svg = '<svg xmlns="http://www.w3.org/2000/svg" width="660" height="300" viewBox="0 0 660 300"><rect width="660" height="300" fill="#1c2023"/><g fill="white" font-family="sans-serif"><text x="20" y="25">#252 TECHNICAL SCHEME — '+title+'</text><text x="20" y="280">No fixed Y / Not approved art / Not generated terrain</text></g>'+''.join(selected)+'</svg>'
        (OUT / f'section-{index+1}.svg').write_text(svg, encoding='utf-8')
    print(f'Source inventory: {len(ids)} blocks, {len(entities)} entity types; {len(related)} hashed files; 8 palette rows; 3 technical sections. Runtime=0.')

if __name__ == '__main__':
    main()
