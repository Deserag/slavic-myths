package org.slavicmyths.gorodishche;

import java.util.*;
import java.util.function.IntBinaryOperator;
import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/** Generation-only streets, frontage planes and fitted lots, shared with explicit showcase. */
public final class CityPlan {
 public record Street(String id,String roadClass,int x0,int z0,int x1,int z1,int width,int setback,int leftFront,int rightFront,int minDepth,int maxDepth){}
 public record Lot(String street,int x,int z,int front,Direction facing,int width,int depth,String category,boolean required){}
 public record Placement(CityCatalog.Building building,BlockPos origin,Rotation rotation,BoundingBox box,Lot lot){public BlockPos transform(BlockPos local){return StructureTemplate.transform(local,net.minecraft.world.level.block.Mirror.NONE,rotation,BlockPos.ZERO).offset(origin);}}
 public record Road(BlockPos pos,Direction up,String roadClass){}
 public record Plan(String layout,String climate,BlockPos origin,List<Street>streets,List<Placement>placements,List<Road>roads,BoundingBox bounds,int residential,int professions){public int beds(){return placements.stream().mapToInt(p->p.building().beds().size()).sum();}}
 private final IntBinaryOperator terrain;private boolean forced;
 private final String climate;private final int x,z,size,northY,southY,squareY;private final RandomSource random;private final List<Placement>parts=new ArrayList<>();private final List<Street>streets=new ArrayList<>();private final Map<Long,Road>roads=new LinkedHashMap<>();
 private CityPlan(String climate,long seed,int x,int z,IntBinaryOperator terrain){this.terrain=terrain;this.climate=climate;this.random=RandomSource.create(seed);this.x=x;this.z=z;size=CityCatalog.settings().size();northY=terrain.applyAsInt(x+size/2,z+58);southY=terrain.applyAsInt(x+size/2,z+118);squareY=(northY+southY)/2;}
 private CityCatalog.Building named(String name,String family){return CityCatalog.buildings().stream().filter(b->b.climate().equals(climate)&&b.name().equals(name)&&(family==null||b.family().equals(family))).findFirst().orElseThrow(()->new IllegalStateException("MISSING_TEMPLATE_DEFINITION "+climate+"/"+name+"/"+family));}
 private int grade(int localZ){if(localZ<=58)return northY;if(localZ>=118)return southY;if(localZ<70)return northY+(int)Math.round((squareY-northY)*(localZ-58)/12.0);if(localZ<=104)return squareY;return squareY+(int)Math.round((southY-squareY)*(localZ-104)/14.0);}
 private Placement add(CityCatalog.Building b,int a,int front,int floor,Direction facing,String street,String category){
  Rotation r=switch(facing){case SOUTH->Rotation.CLOCKWISE_180;case EAST->Rotation.CLOCKWISE_90;case WEST->Rotation.COUNTERCLOCKWISE_90;default->Rotation.NONE;};
  int px=facing==Direction.EAST?front-b.depth()+1:facing==Direction.WEST?front:a;
  int pz=facing==Direction.SOUTH?front-b.depth()+1:facing==Direction.NORTH?front:a;
  int w=facing.getAxis()==Direction.Axis.Z?b.width():b.depth(),d=facing.getAxis()==Direction.Axis.Z?b.depth():b.width();
  if(!forced){int low=Integer.MAX_VALUE,high=Integer.MIN_VALUE;for(int ax:new int[]{x+px,x+px+w-1})for(int az:new int[]{z+pz,z+pz+d-1}){int h=terrain.applyAsInt(ax,az);low=Math.min(low,h);high=Math.max(high,h);}int correction=CityCatalog.settings().correction();if(high-low>correction*2)throw new IllegalStateException("LOCAL_TERRAIN_TOO_STEEP "+b.id());int adjusted=Math.max(high-correction,Math.min(low+correction,floor));if(Math.abs(adjusted-floor)>(category.equals("defense")?6:3))throw new IllegalStateException("FRONTAGE_HEIGHT_TOO_STEEP "+b.id());floor=adjusted;}
  var min=new BlockPos(x+px,floor-b.surface(),z+pz);var origin=StructureTemplate.getZeroPositionWithTransform(min,net.minecraft.world.level.block.Mirror.NONE,r,b.width(),b.depth());
  var box=new BoundingBox(min.getX(),min.getY(),min.getZ(),min.getX()+w-1,min.getY()+b.height()-1,min.getZ()+d-1);
  var lot=new Lot(street,x+px,z+pz,(facing.getAxis()==Direction.Axis.Z?z:x)+front,facing,b.width(),b.depth(),category,true);var p=new Placement(b,origin,r,box,lot);parts.add(p);return p;
 }
 private void street(String id,String type,int x0,int z0,int x1,int z1,int width,int setback){boolean alongZ=x0==x1;int center=alongZ?x0:z0;streets.add(new Street(id,type,x+x0,z+z0,x+x1,z+z1,width,setback,(alongZ?x:z)+center-width/2-setback-1,(alongZ?x:z)+center+width/2+setback+1,12,24));
  int length=Math.max(x1-x0,z1-z0);for(int i=0;i<=length;i++){int a=x0+(alongZ?0:i),b=z0+(alongZ?i:0);int y=grade(b);int next=grade(b+(alongZ?1:0));Direction up=next>y?Direction.SOUTH:next<y?Direction.NORTH:null;
   for(int j=-width/2;j<=width/2;j++){var pos=new BlockPos(x+a+(alongZ?j:0),y,z+b+(alongZ?0:j));roads.put(pos.asLong(),new Road(pos,up,type));}
  }
 }
 private String family(String previous){var opts=new ArrayList<>(List.of("a","b","c","d"));opts.remove(previous);return opts.get(random.nextInt(opts.size()));}
 private CityCatalog.Building selectResidential(String priorFamily,String priorRoof,String secondRoof){
  var opts=CityCatalog.buildings().stream().filter(b->b.climate().equals(climate)&&(b.category().equals("residential")||b.name().startsWith("craftsman_"))&&b.width()<=17&&b.depth()<=22&&(parts.stream().filter(p->p.lot().category().equals("residential")).mapToInt(p->p.building().beds().size()).sum()<10||b.beds().size()<=2)&&!b.family().equals(priorFamily)&&(!priorRoof.equals(secondRoof)||!b.roof().equals(priorRoof))).toList();
  int total=opts.stream().mapToInt(CityCatalog.Building::weight).sum();if(total==0)throw new IllegalStateException("LOT_NO_FITTING_BUILDING residential");int roll=random.nextInt(total);for(var b:opts){roll-=b.weight();if(roll<0)return b;}throw new IllegalStateException("EMPTY_WEIGHTED_POOL");
 }
 private void frontageRow(List<String>names,int rowY,Direction facing,int front,String street,boolean weighted){String prior="",roof="",second="";int left=12,right=109;for(int i=0;i<names.size();i++){
  var b=weighted?selectResidential(prior,roof,second):named(names.get(i),family(prior));int a=i<3?left:right;
  if(i<3)left+=b.width()+2;else right+=b.width()+2;
  if(a+b.width()>165)throw new IllegalStateException("LOT_NO_FITTING_BUILDING row width");add(b,a,front,rowY,facing,street,"residential");prior=b.family();second=roof;roof=b.roof();
 }}
 private void walls(boolean cross){
  for(Direction side:List.of(Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.EAST)){
   boolean gate=side==(cross?Direction.EAST:Direction.SOUTH);int at=9;
   while(at<167){if(gate&&at==80){add(named("gate_main_01",null),at,side==Direction.SOUTH?175:175,grade(side==Direction.SOUTH?175:87),side,"perimeter","defense");at=95;continue;}
    int limit=gate&&at<80?80:167,len=Math.min(16,limit-at);var b=named(String.format(java.util.Locale.ROOT,"wall_straight_%02d",len),null);
    if(side==Direction.NORTH)add(b,at,0,grade(0),Direction.NORTH,"perimeter","defense");
    else if(side==Direction.SOUTH)add(b,at,175,grade(175),Direction.SOUTH,"perimeter","defense");
    else if(side==Direction.WEST)add(b,at,0,grade(at),Direction.WEST,"perimeter","defense");
    else add(b,at,175,grade(at),Direction.EAST,"perimeter","defense");at+=len;
   }
  }
  for(int tx:new int[]{0,167})for(int tz:new int[]{0,167}){var b=named("tower_corner_01",null);Direction facing=tx==0?(tz==0?Direction.NORTH:Direction.WEST):(tz==0?Direction.EAST:Direction.SOUTH);int a=facing.getAxis()==Direction.Axis.Z?tx:tz,front=facing==Direction.SOUTH?tz+b.depth()-1:facing==Direction.EAST?tx+b.depth()-1:facing==Direction.WEST?tx:tz;add(b,a,front,grade(tz),facing,"perimeter","defense");}
 }
 private Plan build(boolean showcase){forced=showcase;boolean cross=!showcase&&random.nextBoolean();
  // Roads are defined before lots. Layout B has an east gate and wider cross-axis.
  street("main_axis","MAIN",87,44,87,175,cross?5:7,1);street("north_front","SECONDARY",8,58,167,58,5,1);street("south_front","SECONDARY",8,118,167,118,5,1);street("service","ALLEY",8,132,167,132,3,1);
  if(cross)street("cross_axis","MAIN",105,87,175,87,7,1);
  street("patrol_west","ALLEY",7,7,7,168,3,1);street("patrol_east","ALLEY",168,7,168,168,3,1);street("patrol_north","ALLEY",7,7,168,7,3,1);street("patrol_south","ALLEY",7,168,168,168,3,1);
  walls(cross);add(named("square_01",null),70,70,squareY,Direction.NORTH,"square","civic");
  add(named("terem_01",null),68,45,northY,Direction.SOUTH,"princely_axis","princely");
  add(named("trading_house_0"+(1+random.nextInt(3)),null),74,68,squareY,Direction.EAST,"square_west","civic");
  add(named("sacred_yard_01",null),46,137,southY,Direction.NORTH,"service","civic");
  frontageRow(List.of("rich_izba_01","rich_izba_02","narrow_house_01","narrow_house_02","druzhinnik_house_01","craftsman_weaver_01"),northY,Direction.SOUTH,54,"north_front:north",!showcase);
  add(named("craftsman_brewer_01",family("")),12,62,northY,Direction.NORTH,"north_front:south","residential");add(named("rich_izba_02",family("a")),29,62,northY,Direction.NORTH,"north_front:south","residential");
  int extra=showcase?0:random.nextInt(4);for(int i=0;i<extra;i++){var b=selectResidential("","","none");int used=parts.stream().filter(p->p.lot().category().equals("residential")).mapToInt(p->p.building().beds().size()).sum();if(used+b.beds().size()>20)break;add(b,109+i*18,62,northY,Direction.NORTH,"north_front:south","residential");}
  List<String>prof=showcase?List.of("toolsmith_forge_01","weaponsmith_forge_01","armorer_forge_01","cook_house_01","chronicler_terem_01","herbalist_house_01"):List.of(List.of("toolsmith_forge_01","weaponsmith_forge_01","armorer_forge_01").get(random.nextInt(3)),random.nextBoolean()?"chronicler_terem_01":"herbalist_house_01","cook_house_01","craftsman_weaver_01");
  String previous="";for(int i=0;i<prof.size();i++){String f=family(previous);add(named(prof.get(i),f),i<3?12+i*18:109+(i-3)*18,114,southY,Direction.SOUTH,"south_front:north","profession");previous=f;}
  add(named("druzhinnik_barracks_01",null),61,137,southY,Direction.NORTH,"service","military");add(named("stable_01",null),139,137,southY,Direction.NORTH,"service","utility");add(named("ambar_01",null),109,137,southY,Direction.NORTH,"service","utility");add(named("klet_01",null),12,139,southY,Direction.NORTH,"service","utility");
  for(var p:parts){if(p.building().category().equals("defense"))continue;var entry=p.transform(p.building().entrance());Direction f=p.lot().facing();int n=p.lot().street().equals("service")?7:4;
   for(int i=1;i<=n;i++)for(int j=-1;j<=1;j++){var q=entry.relative(f,i).relative(f.getClockWise(),j);int floor=p.origin().getY()+p.building().surface(),streetY=grade(q.getZ()-z);int y=floor+(int)Math.round((streetY-floor)*i/(double)n),inner=floor+(int)Math.round((streetY-floor)*(i-1)/(double)n);var road=new BlockPos(q.getX(),y,q.getZ());roads.put(road.asLong(),new Road(road,inner>y?f.getOpposite():inner<y?f:null,"ALLEY"));}
  }
  for(int i=0;i<parts.size();i++)for(int j=i+1;j<parts.size();j++){var a=parts.get(i);var b=parts.get(j);if(!a.lot().category().equals("defense")&&!b.lot().category().equals("defense")&&a.box().intersects(b.box()))throw new IllegalStateException("STRUCTURE_OVERLAP "+a.building().id()+" / "+b.building().id());}
  int min=parts.stream().mapToInt(p->p.box().minY()).min().orElse(squareY),max=parts.stream().mapToInt(p->p.box().maxY()).max().orElse(squareY);var bounds=new BoundingBox(x,min,z,x+size-1,max,z+size-1);int res=(int)parts.stream().filter(p->p.lot().category().equals("residential")).count();int jobs=(int)parts.stream().filter(p->p.lot().category().equals("profession")).count();
  return new Plan(cross?"cross_axis":"axial",climate,new BlockPos(x,squareY,z),List.copyOf(streets),List.copyOf(parts),List.copyOf(roads.values()),bounds,res,jobs);
 }
 public static Plan make(String climate,long seed,int x,int z,IntBinaryOperator terrain,boolean showcase){return new CityPlan(climate,seed,x,z,terrain).build(showcase);}
 private CityPlan(){throw new UnsupportedOperationException();}
}
