"""Run unmodified CityPlan on a flat terrain with small API test doubles.
No game classes/bootstrap. Geometry adapters are not runtime worldgen evidence.
"""
import json,subprocess,collections
from pathlib import Path
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/loot276-all-20261026';B=R/'build/rewards276-city'
def run():
    catalog=json.loads((R/'src/main/resources/data/slavicmyths/gorodishche/catalog.json').read_text())['buildings']
    sources={
      'net/minecraft/core/BlockPos.java':'''package net.minecraft.core;public record BlockPos(int x,int y,int z){public static final BlockPos ZERO=new BlockPos(0,0,0);public int getX(){return x;}public int getY(){return y;}public int getZ(){return z;}public BlockPos offset(BlockPos b){return new BlockPos(x+b.x,y+b.y,z+b.z);}public BlockPos relative(Direction d,int n){return new BlockPos(x+d.dx*n,y,z+d.dz*n);}public long asLong(){return ((long)(x&0x3ffffff)<<38)|((long)(z&0x3ffffff)<<12)|(y&4095);}}''',
      'net/minecraft/core/Direction.java':'''package net.minecraft.core;public enum Direction{NORTH(0,-1),EAST(1,0),SOUTH(0,1),WEST(-1,0),UP(0,0),DOWN(0,0);public final int dx,dz;Direction(int x,int z){dx=x;dz=z;}public enum Axis{X,Y,Z}public Axis getAxis(){return dx!=0?Axis.X:dz!=0?Axis.Z:Axis.Y;}public Direction getClockWise(){return values()[(ordinal()+1)%4];}public Direction getOpposite(){return values()[(ordinal()+2)%4];}}''',
      'net/minecraft/util/RandomSource.java':'''package net.minecraft.util;public class RandomSource extends java.util.Random{private RandomSource(long s){super(s);}public static RandomSource create(long s){return new RandomSource(s);}}''',
      'net/minecraft/world/level/block/Rotation.java':'''package net.minecraft.world.level.block;public enum Rotation{NONE,CLOCKWISE_90,CLOCKWISE_180,COUNTERCLOCKWISE_90}''',
      'net/minecraft/world/level/block/Mirror.java':'''package net.minecraft.world.level.block;public enum Mirror{NONE}''',
      'net/minecraft/world/level/levelgen/structure/BoundingBox.java':'''package net.minecraft.world.level.levelgen.structure;public record BoundingBox(int minX,int minY,int minZ,int maxX,int maxY,int maxZ){public boolean intersects(BoundingBox b){return minX<=b.maxX&&maxX>=b.minX&&minY<=b.maxY&&maxY>=b.minY&&minZ<=b.maxZ&&maxZ>=b.minZ;}}''',
      'net/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate.java':'''package net.minecraft.world.level.levelgen.structure.templatesystem;import net.minecraft.core.*;import net.minecraft.world.level.block.*;public class StructureTemplate{public static BlockPos transform(BlockPos p,Mirror m,Rotation r,BlockPos pivot){return switch(r){case NONE->p;case CLOCKWISE_90->new BlockPos(-p.getZ(),p.getY(),p.getX());case CLOCKWISE_180->new BlockPos(-p.getX(),p.getY(),-p.getZ());case COUNTERCLOCKWISE_90->new BlockPos(p.getZ(),p.getY(),-p.getX());};}public static BlockPos getZeroPositionWithTransform(BlockPos p,Mirror m,Rotation r,int w,int d){return p.offset(switch(r){case NONE->BlockPos.ZERO;case CLOCKWISE_90->new BlockPos(d-1,0,0);case CLOCKWISE_180->new BlockPos(w-1,0,d-1);case COUNTERCLOCKWISE_90->new BlockPos(0,0,w-1);});}}'''
    }
    rows=[]
    for b in catalog:
        strings=','.join(json.dumps(b[k]) for k in ['id','name','climate','category','family','roof_variant'])
        rows.append('out.add(new Building('+strings+','+','.join(map(str,[*b['size'],b['floors'],b['surface'],b['weight']]))+',new BlockPos('+','.join(map(str,b['entrance_offset']))+'),java.util.Collections.nCopies('+str(len(b['beds']))+',BlockPos.ZERO)));')
    sources['org/slavicmyths/gorodishche/CityCatalog.java']='''package org.slavicmyths.gorodishche;import java.util.*;import net.minecraft.core.BlockPos;public class CityCatalog{public record Building(String id,String name,String climate,String category,String family,String roof,int width,int height,int depth,int floors,int surface,int weight,BlockPos entrance,List<BlockPos>beds){}public record Settings(int size,int correction){}public static Settings settings(){return new Settings(176,5);}public static List<Building> buildings(){return DATA;}static List<Building> DATA=new ArrayList<>();static{'''+''.join('fill'+str(i)+'(DATA);' for i in range(5))+ '}'+''.join('static void fill'+str(i)+'(List<Building> out){'+''.join(rows[i*50:(i+1)*50])+'}' for i in range(5))+'}'
    sources['CityCorpus.java']='''import org.slavicmyths.gorodishche.*;public class CityCorpus{public static void main(String[] args){for(String climate:new String[]{"temperate","cold","warm"})for(int seed=0;seed<100;seed++){var p=CityPlan.make(climate,seed,0,0,(x,z)->70,false);System.out.println(climate+"\\t"+seed+"\\t"+p.placements().stream().map(x->x.building().id()).reduce((a,b)->a+","+b).orElse(""));}}}'''
    files=[]
    for rel,body in sources.items():
        f=B/rel;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(body,encoding='utf8');files.append(str(f))
    bin=Path('C:/Program Files/Java/jdk-21.0.12/bin')
    subprocess.run([str(bin/'javac.exe'),'-d',str(B),*files,str(R/'src/main/java/org/slavicmyths/gorodishche/CityPlan.java')],check=True)
    run=subprocess.run([str(bin/'java.exe'),'-Xmx512m','-cp',str(B),'CityCorpus'],check=True,text=True,capture_output=True)
    (O/'city-native-corpus.tsv').write_text(run.stdout,encoding='utf8')
    inventory=json.loads((O/'template-inventory.json').read_text(encoding='utf8'));hist=collections.defaultdict(collections.Counter);direct=collections.defaultdict(collections.Counter)
    for row in run.stdout.splitlines():
        climate,seed,ids=row.split('\t');manifest=collections.Counter();items=collections.Counter()
        for id in ids.split(','):
            for c in inventory.get(id.removeprefix('slavicmyths:'),{}).get('contents',[]):
                if c.get('table'):manifest[c['table'].removeprefix('slavicmyths:')]+=1
                for s in c.get('items',[]):items[s['id']]+=s.get('count',s.get('Count',1))
        hist[climate][json.dumps({'manifest':dict(manifest),'items':dict(items)},sort_keys=True)]+=1;direct[climate].update(items)
    data={c:dict(variants=[(n/100,json.loads(k)) for k,n in h.items()],direct_expected={k:n/100 for k,n in direct[c].items()},corpus_seeds=100,terrain='flat70, actual CityPlan with API test doubles, not worldgen frequency') for c,h in hist.items()}
    (O/'city-corpus.json').write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf8');print('CityPlan corpus:',{k:len(v['variants']) for k,v in data.items()})
if __name__=='__main__':run()
