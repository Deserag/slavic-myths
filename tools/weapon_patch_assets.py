"""Final weapon resource layer. Run after the original resource generators."""
from pathlib import Path
import json
from PIL import Image, ImageDraw

R = Path(__file__).resolve().parents[1]
A = R / 'src/main/resources/assets/slavicmyths'
def write(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')

def box(lo, hi, color):
    return {'from': lo, 'to': hi, 'faces': {f: {'texture': '#surface', 'uv': [color, 0, color+1, 1]} for f in ['north','south','east','west','up','down']}}

def main():
    # Scythe: 30-unit dark wooden haft, 18-unit hooked steel blade, 1-unit blade
    # thickness, two iron ferrules. No boss-size chains, teeth or emissive ribbons.
    palette = ['#49392d','#715640','#343840','#646b73','#b5b9ba','#817c94','#bda25d']
    atlas = Image.new('RGBA', (16,16))
    for i, color in enumerate(palette):
        ImageDraw.Draw(atlas).rectangle((i,0,i,15),fill=color)
    atlas.save(A/'textures/item/weapon_scythe_surface.png')
    # Distinct silhouettes specified before construction:
    # Victor: 26-unit oak shaft + narrow 6-unit steel point, brass collar.
    # Likho: crooked 24-unit dark staff + asymmetric 5-unit iron eye socket.
    # Ataman: 12-unit grip + hanging 7-unit chain + 5-unit faceted iron weight.
    # Voivode: 4-unit leather grip + 17-unit curved, single-edged steel blade.
    shapes = {
        'victor_spear': [box([7, -5,7],[8.5,21,8.5],1),box([6.5,19,6.5],[9,21,9],6),box([6.5,21,7.25],[9,25,8.25],3),box([7.25,25,7.25],[8.25,28,8.25],4)],
        'likho_staff': [box([7,-3,7],[9,15,9],0),box([8,14,7],[10,21,9],0),box([7,20,6.5],[11,25,9.5],2),box([8,21,6],[10,23,6.5],5),box([6,24,7],[9,26,9],0)],
        'ataman_flail': [box([7,0,7],[9,12,9],0),box([6.5,10,6.5],[9.5,12,9.5],6),box([8,12,7.5],[9,15,8.5],3),box([9,14,7.5],[10,17,8.5],3),box([10,16,7.5],[11,20,8.5],3),box([9,19,6],[14,24,11],2),box([8.5,20,7],[14.5,23,10],3)],
        'trophy_kalin_sabre': [box([7,0,7],[9,4,9],0),box([5,4,6.5],[11,5.5,9.5],6),box([7,5.5,7.25],[9,14,8.25],3),box([8,13,7.25],[10,19,8.25],3),box([9,18,7.25],[11,22,8.25],4),box([10,21,7.25],[11,24,8.25],4)]}
    for name, elements in shapes.items():
        placement = {'gui':{'rotation':[0,0,-30],'scale':[.65]*3},'ground':{'translation':[0,3,0],'scale':[.4]*3},'fixed':{'scale':[.6]*3},'head':{'translation':[0,7,0],'scale':[.4]*3}}
        for side,sign in [('righthand',1),('lefthand',-1)]:
            placement['firstperson_'+side]={'rotation':[0,sign*-75,sign*15],'translation':[sign*1,-1,-2],'scale':[.6]*3}
            placement['thirdperson_'+side]={'rotation':[0,sign*90,0],'translation':[0,1,0],'scale':[.75]*3}
        write(A/f'models/item/{name}.json',{'textures':{'surface':'slavicmyths:item/weapon_scythe_surface','particle':'slavicmyths:item/weapon_scythe_surface'},'elements':elements,'display':placement})
    # Only placement polish on the existing crossbow family; geometry/materials retained.
    for name in ['serpent_crossbow','three_breath_crossbow']:
        for p in (A/'models/item').glob(name+'*.json'):
            m=json.loads(p.read_text(encoding='utf-8'))
            if not m.get('elements'):continue
            for side,sign in [('righthand',1),('lefthand',-1)]:
                m['display']['firstperson_'+side]={'rotation':[0,sign*-90,sign*15],'translation':[sign*1,0,-1],'scale':[.55]*3}
            m['display']['head']={'rotation':[0,0,0],'translation':[0,6,0],'scale':[.4]*3}
            write(p,m)
    from weapon_visual_repair_assets import apply as repair
    repair()
    from weapon_feedback_assets import apply
    apply()
    for lang in ['ru_ru','en_us']:
        p=A/f'lang/{lang}.json';m=json.loads(p.read_text(encoding='utf-8'))
        m['night192.scythe_hint'] = ('ЛКМ — тяжёлый взмах по дуге 140°, дальность 3,5 блока. Полная подготовка: 1 секунда.' if lang=='ru_ru' else 'Attack: heavy 140° arc, 3.5-block reach. Full recovery: 1 second.')
        m['night192.scythe_passives'] = ('Каждый третий удар по цели: Иссушение I на 4 с. Лечение до 2 HP раз в 5 с; нежить: +25% урона, +3 HP за убийство.' if lang=='ru_ru' else 'Every third hit on a target: Wither I for 4s. Heal up to 2 HP every 5s; undead: +25% damage, +3 HP per kill.')
        write(p,m)

    from weapons87_flails import main as flails87
    flails87(True)
if __name__=='__main__':main()
