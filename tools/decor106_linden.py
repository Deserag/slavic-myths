"""#106 pilot: moderate grain correction for THREE linden source textures.

Original 16px resolution and exact material palettes retained. Board joints
remain horizontal; the grain/highlights are interrupted, never random noise.
Both end-grain textures and every other species/derived texture are KEEP.
"""
from pathlib import Path
from PIL import Image

ROOT=Path(__file__).resolve().parents[1]
TEXTURES=ROOT/'src/main/resources/assets/slavicmyths/textures/block'
ART={
 'linden_planks': (
  {'a':(217,187,125),'b':(185,155,93),'c':(222,192,130),'d':(210,180,118),'e':(193,163,101)},[
  'bbbbbbbbbbbbbbbb','ecccaaaccccaaacc','eaadddaaaaaddaaa','eaaaaadddaaaaaaa',
  'bbbbbbbbbbbbbbbb','ccaaaccccaaeccca','aaaaddaaaaaeaaaa','adaaaaaddaaeadda',
  'bbbbbbbbbbbbbbbb','acccaaecaaacccaa','aaaddaeadadddaaa','aaaaaaeddaaaaaad',
  'bbbbbbbbbbbbbbbb','ceccaaacccaaaccc','aeaaaddaaaaadaaa','aeadaaaaaddaaaaa']),
 'linden_log': (
  {'a':(113,105,86),'b':(96,88,69),'c':(118,110,91)},[
  'acaaabcaacaaaaba','acaaabcaacabaaba','aacaaabcaacbaaaa','aacaaabcaacbaaaa',
  'aaabcaacaacaaaab','aaabcaacaaacaabb','baabcaaacaaacaaa','baaacaabcaaacaaa',
  'baacaaabcaacaaaa','aaacaaacaacaaaba','aaacaabaacaaaaba','aacaaabcaacaaaca',
  'aabcaabcaacaaaca','aabcaacaacaaacaa','aaacaacaacaaabaa','acaaabcaacaaaaba']),
 'linden_stripped_log': (
  {'a':(217,187,125),'b':(221,191,129),'c':(205,175,113)},[
  'aabaaaacaabaaaca','aabaaaacaabaaaca','aaabaaacaabaaaca','aaabaaaacaaabaaa',
  'aaaabaabcaaabaaa','aaaabaabaaaacaaa','caaabaabaaabaaaa','caaabaaaaaabaaaa',
  'acaabaaaaabaaaac','acaabacaaabaaaac','aaabaabcaabaaaaa','aaabaaaacaaabaaa',
  'aaaabaaaacaaabaa','aaaabaaaacaaabaa','aabaaaacaabaaaca','aabaaaacaabaaaca'])}
FIX=tuple(ART)
KEEP=('linden_log_top','linden_stripped_log_top')

def sprite(name):
 palette,rows=ART[name]
 assert len(rows)==16 and all(len(row)==16 for row in rows),name
 im=Image.new('RGBA',(16,16));im.putdata([palette[c]+(255,) for row in rows for c in row]);return im

def write_owned(path):
 path=Path(path)
 if path.parent.name!='block' or path.suffix!='.png' or path.stem not in FIX:return False
 path.parent.mkdir(parents=True,exist_ok=True);sprite(path.stem).save(path);return True

if __name__=='__main__':
 for name in FIX:write_owned(TEXTURES/f'{name}.png');print(name)
