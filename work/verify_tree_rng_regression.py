from pathlib import Path
import zipfile,subprocess,json
base=Path('work/tree-regression-094');legacy=base/'legacy';current=base/'current';legacy.mkdir(parents=True,exist_ok=True);current.mkdir(parents=True,exist_ok=True)
z=zipfile.ZipFile('.tools/port-backups/slavicmyths-0.9.3-pre-port.zip');old=legacy/'TreeShape.java';old.write_bytes(z.read('src/main/java/org/slavicmyths/wood/TreeShape.java'))
code='''import java.util.*;import java.security.*;import java.nio.charset.StandardCharsets;import org.slavicmyths.wood.TreeShape;
public class TreeSnapshot{public static void main(String[]args)throws Exception{var hash=MessageDigest.getInstance("SHA-256");for(String s:new String[]{"linden","rowan","willow","pine"})for(int seed=0;seed<256;seed++){Random r=new Random(seed*173L+81);TreeShape t=new TreeShape(s,r);StringBuilder b=new StringBuilder(s+":"+seed+":"+t.variant+";");for(var e:t.logs.entrySet())cell(b,e.getKey(),e.getValue());b.append("L;");for(var e:t.leaves.entrySet())cell(b,e.getKey(),e.getValue());b.append("H;");for(var c:t.hanging)cell(b,c,0);b.append(r.nextLong());hash.update(b.toString().getBytes(StandardCharsets.UTF_8));}System.out.println(HexFormat.of().formatHex(hash.digest()));}static void cell(StringBuilder b,TreeShape.Cell c,int v){b.append(c.x).append(',').append(c.y).append(',').append(c.z).append(',').append(v).append(';');}}
'''
snapshot=base/'TreeSnapshot.java';snapshot.write_text(code,encoding='utf-8');jdk=Path('C:/Program Files/Java/jdk-21.0.12/bin');results={}
for name,out,src in [('legacy',legacy,old),('current',current,Path('src/main/java/org/slavicmyths/wood/TreeShape.java'))]:
 subprocess.run([str(jdk/'javac.exe'),'-d',str(out),str(src),str(snapshot),'tools/checks/TreeShapeCheck.java'],check=True,capture_output=True)
 test=subprocess.run([str(jdk/'java.exe'),'-cp',str(out),'TreeShapeCheck'],check=True,capture_output=True,text=True);print(name,test.stdout.strip())
 results[name]=subprocess.run([str(jdk/'java.exe'),'-cp',str(out),'TreeSnapshot'],check=True,capture_output=True,text=True).stdout.strip()
assert results['legacy']==results['current'],results
Path('docs/port/finalize-tree-regression.json').write_text(json.dumps({'status':'PASS','shapes':1024,'checks':['ordered log coordinates/axes','leaf coordinates/distances','hanging coordinates','variant','post-generation RNG state'], 'hashes':results,'runtime_worldgen_verified':False},indent=2)+'\n',encoding='utf-8');print('PASS: 1024 tree shapes and RNG state identical to preserved 0.9.3 source',results['current'])
