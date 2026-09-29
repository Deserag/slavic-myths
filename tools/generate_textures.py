"""Original 16x16 pixel assets; fixed palette, no external dependencies."""
from pathlib import Path
import random
import struct
import zlib

PALETTE = {
    '.': (0, 0, 0, 0), 'd': (48, 65, 39, 255), 'g': (74, 104, 48, 255),
    'l': (114, 144, 70, 255), 's': (110, 137, 105, 255), 'h': (166, 185, 146, 255),
    'b': (61, 79, 160, 255), 'c': (102, 138, 221, 255), 'w': (204, 221, 246, 255),
    'o': (74, 50, 33, 255), 't': (162, 128, 80, 255), 'e': (213, 191, 141, 255),
    'p': (240, 222, 183, 255), 'k': (64, 55, 46, 255), 'r': (157, 43, 53, 255),
    'f': (235, 79, 65, 255), 'y': (255, 189, 72, 255), 'n': (48, 61, 79, 255),
    'a': (67, 115, 145, 255), 'v': (93, 172, 194, 255), 'i': (171, 224, 223, 255),
}
SPRITES = {
    'block/flax': [
        '................', '....c.....b.....', '...cwc...bcb....', '....cg....bg....',
        '.....g...g......', '.....gl.gl......', '...l..g.g.......', '....l.g.g...l...',
        '.....lgg...l....', '......gg..l.....', '...l..gg.l......', '....l.ggl.......',
        '.....lgg........', '......gg........', '......gd........', '......dd........'],
    'block/wormwood': [
        '................', '.......h........', '....h..s...h....', '.....hs...s.....',
        '..h...ss.s...h..', '...s..ss.s..s...', '....ss.s.sss....', '..h...ssss...h..',
        '...s...ss...s...', '....ss.ss.ss....', '.....ssssss.....', '...h...ss..h....',
        '....ss.ss.s.....', '......sss.......', '.......sg.......', '.......dd.......'],
    'item/birch_bark': [
        '................', '...kkkkkkkk.....', '..kpppppppek....', '..kppkkppeek....',
        '..keppppeeek....', '..keppkkpeek....', '..keppppeeek....', '..keppppeeek....',
        '..keppkkpeek....', '..keppppeeek....', '..kepppppeek....', '..keppkkpeek....',
        '..kteepppeek....', '...kteepppek....', '....kkkkkkk.....', '................'],
    'item/linen_thread': [
        '................', '.....ooooo......', '...ootttttoo....', '..otpeeeeteto...',
        '..opptooooteto..', '..opto....oteto.', '..opto....oteto.', '..oteoo..ooteto.',
        '...otpeooeteto..', '....otppeeeto...', '.....ooooooo....', '.......to.......',
        '......to........', '.....teo........', '......oo........', '................'],
    'item/fern_flower': [
        '................', '...rr.....rr....', '...rfr...rfr....', '....rfr.rfr.....',
        '..rrrfryrfrrr...', '..rffyyyyfffr...', '...rrfyyyfrr....', '.....rrfrr......',
        '.......g........', '....l..g..l.....', '.....l.g.l......', '......lgl.......',
        '.......g........', '......gd........', '.....dd.........', '................'],
    'item/perunite': [
        '................', '.......nn.......', '......niin......', '.....niivvn.....',
        '....niivvvn.....', '...niivvvan.....', '...nivvvvan.....', '..nivvyvvaan....',
        '..nivyvvvaan....', '..nivyvvvaan....', '..nvvyyvaaan....', '...nvvyvaan.....',
        '...nvyaaan......', '....naaann......', '.....nnn........', '................'],
}

def png(path, pixels, width, height):
    def chunk(kind, data):
        return struct.pack('!I', len(data))+kind+data+struct.pack('!I', zlib.crc32(kind+data)&0xffffffff)
    raw = b''.join(b'\0'+bytes(c for pixel in row for c in pixel) for row in pixels)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR', struct.pack('!IIBBBBB', width, height, 8, 6, 0, 0, 0))+chunk(b'IDAT', zlib.compress(raw))+chunk(b'IEND', b''))

def generate(root):
    images = {}
    for name, rows in SPRITES.items():
        assert len(rows) == 16 and all(len(row) == 16 for row in rows), name
        images[name] = [[PALETTE[c] for c in row] for row in rows]
    rng = random.Random(165)
    ore = [[(lambda n: (n, n, n, 255))(rng.choice([99, 107, 115, 122, 128, 135])) for _ in range(16)] for _ in range(16)]
    for x, y in [(3, 3), (10, 2), (7, 8), (12, 12), (2, 12)]:
        for dx, dy, color in [(0, 0, 'i'), (1, 0, 'v'), (0, 1, 'v'), (1, 1, 'a'), (2, 1, 'n'), (1, 2, 'n')]:
            ore[y+dy][x+dx] = PALETTE[color]
    images['block/perunite_ore'] = ore
    for name, pixels in images.items():
        png(root/'src/main/resources/assets/slavicmyths/textures'/f'{name}.png', pixels, 16, 16)
    # Contact sheet is a development artifact; textures shipped in the JAR remain 16x16.
    sheet = [[(35, 38, 45, 255) for _ in range(len(images)*20*6)] for _ in range(20*6)]
    for i, pixels in enumerate(images.values()):
        for y, row in enumerate(pixels):
            for x, rgba in enumerate(row):
                if rgba[3]:
                    for dy in range(6):
                        for dx in range(6):
                            sheet[(y+2)*6+dy][(i*20+x+2)*6+dx] = rgba
    png(root/'docs/verification/textures-0.2.png', sheet, len(images)*20*6, 20*6)

if __name__ == '__main__':
    generate(Path(__file__).resolve().parents[1])
