"""Render exported production vertices/UV at identical camera, scale and time."""
from pathlib import Path
import json
from armor88_preview import render
from PIL import Image, ImageDraw

root = Path(__file__).resolve().parents[1]
out = root / 'docs/implementation/weapons87/head-clearance'
before = json.loads((out / 'before.json').read_text())
after = json.loads((out / 'after.json').read_text())
for kind in ('TUGARIN', 'VOLKOLAK', 'PODZEMNIK'):
    board = Image.new('RGB', (1600, 320), '#161b22')
    draw = ImageDraw.Draw(board)
    draw.text((10, 8), kind + ' actual ModelPart + UV + PNG; same time, scale and camera. OFFLINE, not gameplay.', fill='white')
    for phase in range(4):
        for side, data in enumerate((before, after)):
            row = [r for r in data if r['kind'] == kind][phase]
            vertices = row['vertices']
            faces = [[[(x-.5)*16, (1.9-y)*16-8, (z-.5)*16, u, v]
                      for x, y, z, u, v in vertices[i:i+4]]
                     for i in range(0, len(vertices), 4)]
            pic = render({'layers': [{'texture': row['texture'].split(':')[1], 'faces': faces}]}, 145)
            x = (phase*2+side)*200
            board.paste(pic, (x, 35))
            draw.text((x+4, 38), ('BEFORE' if side == 0 else 'AFTER')+' t='+str(row['time']), fill='white')
    board.save(out / (kind.lower()+'.png'))
