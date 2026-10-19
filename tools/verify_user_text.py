"""Read-only UTF-8/user text gate. Never rewrites files or scans historical reports."""
from pathlib import Path
import json,re,sys,zipfile

R=Path(__file__).resolve().parents[1]
STRING=re.compile(r'"(?:[^"\\]|\\.)*"')
TOKEN=re.compile(r'%(?:(\d+)\$)?([sd])')

def suspect(s):
    if '\ufffd' in s or any(ord(c)<32 and c not in '\n\r\t' for c in s): return True
    if re.search(r'(?:[РС][\u0080-\u00bf\u0400-\u045f\u2010-\u203f]){3,}|(?:Ã.|Â.|â€){2,}',s): return True
    # Also recognize reversible corruption without assuming every Cyrillic Р/С is bad.
    for enc in ('cp1251','latin1','cp1252'):
        try: repaired=s.encode(enc).decode('utf-8')
        except UnicodeError: continue
        if repaired!=s and re.search('[А-Яа-яЁё]',repaired): return True
    return False

def placeholders(s):
    found=[]
    for i,m in enumerate(TOKEN.finditer(s.replace('%%',''))):found.append((int(m[1] or i+1),m[2]))
    return sorted(found)

def main():
    errors=[];count=0;langs={}
    files=list((R/'src/main/java').rglob('*.java'))
    # Books/dialogue and other visible data are included; binary assets and docs/tools are not.
    files+=list((R/'src/main/resources/assets/slavicmyths').rglob('*.json'))
    files+=list((R/'src/main/resources/data/slavicmyths').rglob('*.json'))
    for p in files:
        try: text=p.read_text(encoding='utf-8')
        except UnicodeError as e: errors.append(f'{p}: {e}');continue
        if p.suffix=='.json':
            try: json.loads(text)
            except ValueError as e:errors.append(f'{p}: {e}')
        for m in STRING.finditer(text):
            s=m[0][1:-1]
            # Expand Java/JSON Unicode escapes for detection too (preserve normal Unicode).
            s=re.sub(r'\\u([0-9a-fA-F]{4})',lambda x:chr(int(x[1],16)),s)
            count+=1
            if suspect(s):errors.append(f'{p.relative_to(R)}:{text.count(chr(10),0,m.start())+1}: malformed text {s!r}')
    for lang in ('ru_ru','en_us'):
        langs[lang]=json.loads((R/f'src/main/resources/assets/slavicmyths/lang/{lang}.json').read_text(encoding='utf-8'))
    # These are the owned keys introduced by #73/#74. Existing intentional vanilla
    # and third-party keys are not incorrectly reported as missing mod translations.
    keys=set(k for d in langs.values() for k in d if k.startswith('command.slavicmyths.'))
    for key in sorted(keys):
        if not all(key in langs[l] for l in langs):errors.append('Missing RU/EN key: '+key);continue
        if placeholders(langs['ru_ru'][key])!=placeholders(langs['en_us'][key]):errors.append('Placeholder mismatch: '+key)
    for p in (R/'src/main/java').rglob('*.java'):
        for key in re.findall(r'Component\.translatable\("(command\.slavicmyths\.[^"]+)"(?!\s*\+)',p.read_text(encoding='utf-8')):
            if key not in keys:errors.append('Missing literal translation: '+key)
    if '--jar' in sys.argv:
        props=dict(l.split('=',1) for l in (R/'gradle.properties').read_text().splitlines() if '=' in l)
        with zipfile.ZipFile(R/f"build/libs/slavicmyths-{props['mod_version']}.jar") as z:
            assert z.testzip() is None
            for lang in langs:
                name=f'assets/slavicmyths/lang/{lang}.json'
                if z.read(name)!=(R/'src/main/resources'/name).read_bytes():errors.append('JAR language parity: '+lang)
    if errors: raise SystemExit('\n'.join(errors))
    print(f'USER_TEXT PASS: {len(files)} production text files, {count} literals, {len(keys)} owned RU/EN keys; runtime NOT_RUN')

if __name__=='__main__':main()
