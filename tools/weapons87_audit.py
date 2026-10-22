"""Read-only production catalogue and provenance audit for #157 (never starts Minecraft)."""
from pathlib import Path
import hashlib,json,re
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'docs/implementation/weapons87'
SRC=ROOT/'src/main/java'
RES=ROOT/'src/main/resources'
def sha(p): return hashlib.sha256(p.read_bytes()).hexdigest()
def save(p,data): p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def main():
    catalog=[line.split('\t') for line in (RES/'assets/slavicmyths/creative_catalog.tsv').read_text(encoding='utf-8').splitlines() if line and not line.startswith('#')]
    ids=[(a,c) for a,b,c in catalog if b=='weapons'];assert len(ids)==68
    ru=json.loads((RES/'assets/slavicmyths/lang/ru_ru.json').read_text(encoding='utf-8'))
    java={p:p.read_text(encoding='utf-8') for p in SRC.rglob('*.java')}
    generators={p:p.read_text(encoding='utf-8',errors='replace') for p in (ROOT/'tools').glob('*.py') if p.name!='weapons87_audit.py'}
    jsons={p:json.loads(p.read_text(encoding='utf-8')) for folder in ['recipe','loot_table','advancement'] for p in (RES/'data/slavicmyths'/folder).rglob('*.json')}
    def refs(value,wanted):
        if isinstance(value,dict):return any(refs(v,wanted) for v in value.values())
        if isinstance(value,list):return any(refs(v,wanted) for v in value)
        return value==wanted
    rows=[]
    for id,family in ids:
        model=RES/f'assets/slavicmyths/models/item/{id}.json'
        # Standalone models selected by BEWLR/client code are not JSON overrides.
        pending=[model]+[p for suffix in ['_held','_icon','_pulling_0','_pulling_1','_pulling_2','_arrow','_firework'] if (p:=model.with_name(id+suffix+'.json')).exists()];seen=set();models=[];textures=[]
        while pending:
            p=pending.pop()
            if p in seen:continue
            seen.add(p)
            if not p.exists():raise ValueError(f'missing model {p}')
            obj=json.loads(p.read_text(encoding='utf-8'));models.append({'path':p.relative_to(ROOT).as_posix(),'sha256':sha(p),'elements':len(obj.get('elements',[])),'display':obj.get('display',{}),'parent':obj.get('parent')})
            parent=obj.get('parent','')
            if parent.startswith('slavicmyths:'):pending.append(RES/('assets/slavicmyths/models/'+parent.split(':')[1]+'.json'))
            for override in obj.get('overrides',[]):
                target=override['model']
                if target.startswith('slavicmyths:'):pending.append(RES/('assets/slavicmyths/models/'+target.split(':')[1]+'.json'))
            for tex in obj.get('textures',{}).values():
                if tex.startswith('slavicmyths:'):
                    t=RES/('assets/slavicmyths/textures/'+tex.split(':')[1]+'.png')
                    assert t.exists(),t
                    entry={'path':t.relative_to(ROOT).as_posix(),'sha256':sha(t)}
                    if entry not in textures:textures.append(entry)
        token=re.compile(r'(?<![A-Za-z0-9_])(?:'+re.escape(id)+r'|'+re.escape(id.upper())+r')(?![A-Za-z0-9_])')
        code=[{'path':p.relative_to(ROOT).as_posix(),'line':i,'source':line.strip()} for p,s in java.items() for i,line in enumerate(s.splitlines(),1) if token.search(line)]
        resources=[{'path':p.relative_to(ROOT).as_posix(),'role':'recipe_output' if '/recipe/' in p.as_posix() and refs(v.get('result'),'slavicmyths:'+id) else 'reference','data':v} for p,v in jsons.items() if refs(v,'slavicmyths:'+id)]
        rows.append({'id':id,'ru':ru.get('item.slavicmyths.'+id),'family':family,'models':models,'textures':textures,'code_references':code,'resource_references':resources,'generator_candidates':[p.relative_to(ROOT).as_posix() for p,s in generators.items() if token.search(s)],'runtime':'NOT_RUN / OWNER_PENDING','audit_state':'IN_PROGRESS: acquisition/effects and contextual DPS require review'})
    save(OUT/'catalogue-provenance.json',rows)
    print(f'CATALOGUE_PROVENANCE: {len(rows)} IDs; model/texture dependencies resolved; no production mutation')
if __name__=='__main__':main()
