package org.slavicmyths.posad;

/** Synthetic terrain contract checks, explicitly NOT seed/native-world evidence. */
public final class PosadTerrainCheck {
 private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 public static void main(String[] args) {
  for(int turns=0;turns<4;turns++) {
   var land=PosadTerrain.inspect((x,y,z)->y<64?PosadTerrain.Material.NATURAL:PosadTerrain.Material.AIR,-57,64,-57,turns);
   check(land.accepted(),"Land rejected");check(land.plan().bridgeLength()==1,"Nearest bank");
   check(land.plan().turns()==turns,"Seeded orientation tie");
   var transform=new PosadTerrain.Plan(0,64,0,turns,24,java.util.List.of());
   var shore=transform.world(56,136); final int rotation=turns;
   var lava=PosadTerrain.inspect((x,y,z)-> {
    if(y>=64)return PosadTerrain.Material.AIR;
    boolean bank=switch(rotation){case 0->z>=shore.z();case 1->x<=shore.x();case 2->z<=shore.z();default->x>=shore.x();};
    return bank||y<40?PosadTerrain.Material.NATURAL:PosadTerrain.Material.LAVA;
   },0,64,0,turns);
   check(lava.accepted(),"Lava rejected "+turns+" "+lava.reason());
   check(lava.plan().bridgeLength()==24,"Wrong shore distance");
   check(lava.plan().turns()==turns,"Wrong shoreline orientation");
   check(lava.plan().supports().size()>225,"Missing bridge supports");
  }
  var ceiling=PosadTerrain.inspect((x,y,z)->y==65?PosadTerrain.Material.NATURAL:PosadTerrain.Material.AIR,0,64,0,0);
  check(ceiling.reason().equals("HEADROOM"),"Ceiling accepted");
  var deep=PosadTerrain.inspect((x,y,z)->PosadTerrain.Material.AIR,0,64,0,0);
  check(deep.reason().equals("NO_FOOTING"),"Floating city accepted");
  var protectedBlock=PosadTerrain.inspect((x,y,z)->x==17&&z==19&&y==63?PosadTerrain.Material.PROTECTED:y<64?PosadTerrain.Material.NATURAL:PosadTerrain.Material.AIR,0,64,0,0);
  check(protectedBlock.reason().equals("PROTECTED_PLATFORM"),"Unsampled player block missed");
  System.out.println("PASS synthetic terrain: four rotations land/lava/nearest bank; ceiling, missing footing, protected block rejection. NOT native seed QA.");
 }
}
