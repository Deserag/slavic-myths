"""Current NeoForge source/JAR integrity. Does not claim historical port or runtime acceptance."""
from pathlib import Path
import hashlib,json,re,sys,zipfile
from PIL import Image
R=Path(__file__).resolve().parents[1];RES=R/'src/main/resources';A=RES/'assets/slavicmyths'
def main():
    props=dict(line.split('=',1) for line in (R/'gradle.properties').read_text().splitlines() if '=' in line and not line.startswith('#'))
    assert props['minecraft_version']=='1.21.1' and props['neo_version']=='21.1.255'
    assert 'JavaLanguageVersion.of(21)' in (R/'build.gradle').read_text()
    version=props['mod_version'];jar=R/'build/libs'/('slavicmyths-'+version+'.jar')
    assert jar.is_file() and len(list((R/'build/libs').glob('slavicmyths-*.jar')))==1
    count=0;json_count=0;png_count=0;model_count=0
    for p in RES.rglob('*.json'):json.loads(p.read_text(encoding='utf-8'));json_count+=1
    for p in A.rglob('*.png'):
        with Image.open(p) as im:im.verify()
        png_count+=1
    def own_ref(reference,kind):
        if reference.startswith('slavicmyths:'):
            target=A/kind/(reference.split(':',1)[1]+('.json' if kind=='models' else '.png'))
            assert target.is_file(),(reference,str(target))
    def model_refs(data):
        if isinstance(data,dict):
            if isinstance(data.get('parent'),str):own_ref(data['parent'],'models')
            for value in data.get('textures',{}).values():
                if isinstance(value,str) and not value.startswith('#'):own_ref(value,'textures')
            for value in data.values():model_refs(value)
        elif isinstance(data,list):
            for value in data:model_refs(value)
    for p in (A/'models').rglob('*.json'):model_refs(json.loads(p.read_text(encoding='utf-8')));model_count+=1
    for p in (A/'blockstates').glob('*.json'):
        def block_refs(data):
            if isinstance(data,dict):
                if 'model' in data:own_ref(data['model'],'models')
                for value in data.values():block_refs(value)
            elif isinstance(data,list):
                for value in data:block_refs(value)
        block_refs(json.loads(p.read_text()))
    for language in ['ru_ru','en_us']:
        lang=json.loads((A/'lang'/(language+'.json')).read_text(encoding='utf-8'))
        for line in (A/'creative_catalog.tsv').read_text(encoding='utf-8').splitlines():
            if not line or line.startswith('#'):continue
            fields=line.split('\t')
            assert len(fields)==3 and fields[1] in ['slavicmyths','resources','farming','food','tools','armor','decor','magic','mobs','weapons','accessories','hidden'] and fields[2],('invalid creative catalogue row',line)
            id=fields[0]
            assert any(prefix+'slavicmyths.'+id in lang for prefix in ['item.','block.']),('translation',language,id)
            assert (A/'models/item'/(id+'.json')).is_file(),('catalogue model',id)
    assert json.loads((RES/'pack.mcmeta').read_text())['pack']['pack_format']==34
    assert not (RES/'META-INF/mods.toml').exists()
    with zipfile.ZipFile(jar) as archive:
        names=archive.namelist();assert len(names)==len(set(names)),'duplicate JAR entries'
        metadata=archive.read('META-INF/neoforge.mods.toml').decode('utf-8');assert 'modId="slavicmyths"' in metadata and 'version="'+version+'"' in metadata
        for path in RES.rglob('*'):
            if not path.is_file():continue
            rel=path.relative_to(RES).as_posix();expected=R/'build/resources/main'/rel if rel=='META-INF/neoforge.mods.toml' else path
            assert archive.read(rel)==expected.read_bytes(),('packaged source mismatch',rel);count+=1
        classes=[name for name in names if name.endswith('.class')]
        for name in classes:assert int.from_bytes(archive.read(name)[6:8],'big')==65,('not Java21',name)
        assert not any(name.startswith(('mezz/','top/theillusivec4/','org/slavicmyths/verify/','tools/smoke/')) or 'Weapons87Continuation' in name or 'Heads140Offline' in name for name in names),'test/dependency classes in production'
        assert 'data/slavicmyths/structure/port_empty.nbt' not in names
    report={'scope':'CURRENT_SOURCE_AND_PRODUCTION_JAR; historical port acceptance not asserted','version':version,'jar':str(jar.resolve()),'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'resources':count,'json':json_count,'png':png_count,'models':model_count,'java21_classes':len(classes),'runtime_launches':'NOT_TRACKED_BY_STATIC_CHECK','runtime':'NOT_ASSESSED_BY_RESOURCE_CHECK; see actual QA logs'}
    out=R/'build/current-resources.json';out.write_text(json.dumps(report,indent=2)+'\n')
    print('PASS current NeoForge resources:',json.dumps(report))
if __name__=='__main__':main()
