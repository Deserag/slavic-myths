package org.slavicmyths.chud;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
public final class RasterChecks {
 public static void run(){
  var plan=new ChudPlan(19766,new BlockPos(-8,-4,8),0,0,76);var fills=ChudGeometry.build(plan);var decor=new ChudDecor(plan,null);var box=new BoundingBox(-135,-48,-119,119,84,135);var sections=ChudCreationJobs.raster(fills,decor,box);int compared=0;
  var chosen=new LinkedHashSet<Integer>(List.of(0,sections.size()/3,sections.size()/2,sections.size()-1));
  for(int n:chosen){var section=sections.get(n);var origin=section.origin();int min=box.minY(),max=box.maxY();var raw=new BlockState[(max-min+1)*256];var clip=new BoundingBox(origin.getX(),min,origin.getZ(),origin.getX()+15,max,origin.getZ()+15);ChudGeometry.rasterize(fills,clip,(p,s)->raw[(p.getY()-min)*256+(p.getZ()&15)*16+(p.getX()&15)]=s);
   for(int i=0;i<4096;i++){int y=origin.getY()+(i>>8),x=origin.getX()+(i&15),z=origin.getZ()+((i>>4)&15);BlockState expected=null;if(y>=min&&y<=max){int at=(y-min)*256+(i&255);var s=raw[at];if(s!=null){boolean open=y+2<=max&&raw[at+256]!=null&&raw[at+256].isAir()&&raw[at+512]!=null&&raw[at+512].isAir();expected=decor.surface(s,x,y,z,decor.zone(x,z),open);}}if(!Objects.equals(expected,section.states()[i]))throw new AssertionError("section/native geometry drift at "+x+","+y+","+z);compared++;}
  }
  System.out.println("PASS: "+compared+" destination states match full-height native raster, including section boundaries and decoration.");
 }
}
