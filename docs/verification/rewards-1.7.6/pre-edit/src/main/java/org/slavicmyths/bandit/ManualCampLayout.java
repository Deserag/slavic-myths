package org.slavicmyths.bandit;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
/** Existing buildings and encounter markers, at the prepared construction surface. */
public final class ManualCampLayout {
 public static List<String> templateNames(int tier){return tier==0?List.of("sleeping","storage_tent","senior","yard_small","cart"):tier==1?List.of("leader","sleeping","storage_tent","senior","warehouse","watch","canopy","yard_medium"):List.of("gate","barracks_porch","barracks_side","forge_open","forge_closed","warehouse_long","warehouse_square","kitchen","ataman_house","stable","prison","commander","utility","central_yard","nightingale_yard","tower_roof","tower_open","tower_tall","perimeter_forest","cache_0","cache_1");}
 public static List<StructurePiece> create(ServerLevel world,int tier,BlockPos anchor,UUID id,long seed){
  var random=new Random(seed);var result=new ArrayList<StructurePiece>();var templates=world.getStructureManager();
  if(tier<2){
   Object[][] layout=tier==0?new Object[][]{{"sleeping",1,1,0},{"storage_tent",15,3,2},{"senior",8,15,3},{"yard_small",0,0,-1},{"cart",0,12,-1}}:new Object[][]{{"leader",18,1,0},{"sleeping",2,5,1},{"sleeping",4,24,3},{"storage_tent",28,5,5},{"senior",26,26,6},{"warehouse",33,16,-1},{"watch",1,16,8},{"canopy",15,28,-1},{"yard_medium",0,0,-1}};
   for(Object[] item:layout){String name=(String)item[0];if(name.equals("cart")&&!random.nextBoolean())continue;require(templates,name,false);var piece=new CampPiece(templates,name,anchor.offset((int)item[1],0,(int)item[2]),id,tier==0?5:9,(int)item[3]);piece.preparedTerrain=true;result.add(piece);}
  }else{
   Object[][] layout={{"gate",34,2},{random.nextBoolean()?"barracks_porch":"barracks_side",8,16},{random.nextBoolean()?"forge_open":"forge_closed",25,16},{random.nextBoolean()?"warehouse_long":"warehouse_square",8,33},{"kitchen",25,30},{"ataman_house",40,15},{"stable",59,15},{"prison",60,29},{"commander",23,49},{"utility",9,50},{"central_yard",38,29},{"nightingale_yard",40,43},{"tower_roof",4,5},{"tower_open",69,5},{"tower_tall",8,66},{"perimeter_forest",0,0}};
   for(Object[] item:layout){String name=(String)item[0];require(templates,name,true);var piece=new LargeCampPiece(templates,name,anchor.offset((int)item[1],0,(int)item[2]),id);piece.preparedTerrain=true;result.add(piece);}
   boolean cache=random.nextFloat()<.35F;int cacheType=random.nextInt(2);if(cache){String name="cache_"+cacheType;require(templates,name,true);var piece=new LargeCampPiece(templates,name,anchor.offset(cacheType==0?43:11,-5,cacheType==0?18:36),id);piece.preparedTerrain=true;result.add(piece);}
  }
  return result;
 }
 private static void require(net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager templates,String name,boolean large){if(templates.getOrCreate(ResourceLocation.fromNamespaceAndPath("slavicmyths",(large?"stronghold/":"bandit/")+name)).getSize().equals(BlockPos.ZERO))throw new IllegalArgumentException("Отсутствует шаблон "+name);}
 private ManualCampLayout(){}
}
