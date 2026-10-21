"""Issue #110: seven independently authored native 16px trophy sprites.

Section A of TROPHIES_92_PIXEL_CONCEPTS.svg supplies the art direction.
Explicit pixel rows keep edges crisp and make regeneration deterministic.
This module has no import-time writes; historical generators delegate here.
"""
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
TEXTURES = ROOT / 'src/main/resources/assets/slavicmyths/textures/item'

# . = transparent. Materials use at most four values, with local accents.
ART = {
    'upyr_fang': ({'o':'#493c35','b':'#baa98a','m':'#dccdaf','h':'#f3e8ce','r':'#71372f'}, [
        '................','......oooo......','.....orrmbo.....','.....ormmbo.....',
        '.....ohmmbo.....','.....ohmmbo.....','.....ohmmbo.....','......hmbo......',
        '......hmbo......','......hmbo......','......hmbo......','.......mbo......',
        '.......mo.......','.......mo.......','.......o........','................']),
    'klyk_volkolaka': ({'o':'#483e36','s':'#8b7b67','m':'#c5b18b','h':'#e7d6af'}, [
        '................','...oooooo.......','..ohhhmmmoo.....','..ohmmmmmsso....',
        '...ommmmmsso....','....ooommmsso...','.......ommso....','.......ommso....',
        '......ohmso.....','.....ohmso......','....ohmso.......','...ohmso........',
        '..omso..........','.ooo............','................','................']),
    'nochnoy_kogot': ({'o':'#202027','s':'#34323e','m':'#555064','h':'#8d7d9f'}, [
        '................','........ooo.....','.......omsoo....','.......ohmso....',
        '........ohmso...','.........omso...','.........omso...','........ohmso...',
        '.......ohmso....','.....oohmso.....','....ohmsso......','...ohsoo........',
        '..omso..........','..oso...........','..oo............','................']),
    'ovinnaya_zola': ({'s':'#50565e','m':'#7c858c','h':'#adb5ba'}, [
        '................','................','................','................',
        '................','................','................','................',
        '...m........h...','................','.......mh.......','...mh.mhhms.....',
        '..mhhmmmsmhmms..','.smmmsmmsmmmsms.','..ssss.sssssss..','................']),
    'iskra_ovinnika': ({'o':'#44302d','s':'#b54b23','m':'#ef8d2c','h':'#ffe090'}, [
        '.......m........','.......h........','................','............m...',
        '...s........h...','...m....o.......','.......omo......','.......omso.....',
        '......omhmso....','......omhmso....','.....omhhmso....','.....omhhmso....',
        '.....osmmso.....','......osso......','.......oo.......','................']),
    'pylayuschaya_cheshuya': ({'o':'#48252b','s':'#79332f','m':'#b6512e','h':'#e98d3d'}, [
        '................','.......o........','......omo.......','.....omso.......',
        '....omsmso......','...omssmsso.....','..omsssmssso....','..ossshmssso....',
        '...ossmmsso.....','...ossmmsso.....','....osmmso......','.....omso.......',
        '.....omo........','......o.........','................','................']),
    'tugarinova_kozha': ({'o':'#513629','s':'#895331','m':'#b17a43','h':'#d0a263','r':'#913e31'}, [
        '................','..........oo....','..oo.....omso...','..omoooommmsso..',
        '.ommmhmmmmsso...','..ommssohhmso...','..ommmmsmmmso...','..oshmmmmsmso...',
        '.osmhmmmsmmso...','.ommsmmsmmmsso..','..ommsmmmsmmso..','..ommmhhmrmso...',
        '.omsoommsmso....','.oo...osssoo....','.......oo.......','................']),
}
IDS = tuple(ART)

def sprite(name):
    palette, rows = ART[name]
    assert len(rows) == 16 and all(len(row) == 16 for row in rows), name
    im = Image.new('RGBA', (16, 16))
    im.putdata([(0, 0, 0, 0) if c == '.' else tuple(bytes.fromhex(palette[c][1:])) + (255,)
                for row in rows for c in row])
    return im

def write(name, directory=TEXTURES):
    directory = Path(directory)
    directory.mkdir(parents=True, exist_ok=True)
    sprite(name).save(directory / f'{name}.png')

if __name__ == '__main__':
    import argparse
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('ids', nargs='*', choices=IDS)
    args = parser.parse_args()
    for name in args.ids or IDS:
        write(name)
        print(name)
