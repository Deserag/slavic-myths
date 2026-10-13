package org.slavicmyths.guardians;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.core.*;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.client.model.geom.ModelPart;
import org.slavicmyths.sanctuary.*;
import org.slavicmyths.client.*;
public final class GuardianChecks {
 static int checks;
 static void check(boolean ok,String what){checks++;if(!ok)throw new AssertionError(what);}
 static int uv(ModelPart part)throws Exception{int count=0;var cubes=ModelPart.class.getDeclaredField("cubes");cubes.setAccessible(true);var children=ModelPart.class.getDeclaredField("children");children.setAccessible(true);var polys=ModelPart.Cube.class.getDeclaredField("polygons");polys.setAccessible(true);for(var cube:(List<?>)cubes.get(part))for(var poly:(Object[])polys.get(cube)){var vertices=poly.getClass().getDeclaredField("vertices");vertices.setAccessible(true);for(var vertex:(Object[])vertices.get(poly)){var u=vertex.getClass().getDeclaredField("u");var v=vertex.getClass().getDeclaredField("v");u.setAccessible(true);v.setAccessible(true);float x=u.getFloat(vertex),y=v.getFloat(vertex);check(Float.isFinite(x)&&Float.isFinite(y)&&x>=0&&x<=1&&y>=0&&y<=1,"native UV in atlas");count++;}}for(var child:((Map<?,?>)children.get(part)).values())count+=uv((ModelPart)child);return count;}
 public static void main(String[] args)throws Exception{
  check(GuardianRules.maximum(false)==500&&GuardianRules.threshold(false)==25,"fire health/threshold");
  check(GuardianRules.maximum(true)==450&&GuardianRules.threshold(true)==22.5f,"water health/threshold");
  for(double d:new double[]{0,2.9,3,3.01,6,6.01,10,10.01})check(GuardianRules.pulse(d)==(d<=3?8:d<=6?5:d<=10?2:0),"AoE inclusive distance gradient");
  for(boolean finalPhase:new boolean[]{false,true}){int warning=GuardianRules.lockWarning(finalPhase),duration=(warning+GuardianRules.LOCK_ACTIVE)*3;check(warning>=18&&duration>=160&&duration<=200,"telegraph and shield duration");}
  for(int pattern=0;pattern<3;pattern++){int previous=-1;for(int wave=0;wave<3;wave++){int safe=GuardianRules.safeStrip(pattern,wave);check(safe>=0&&safe<=2,"one safe strip");check(previous<0||Math.abs(previous-safe)<=1,"reachable neighbouring safe strip");previous=safe;for(int cell=0;cell<140;cell++){int zone=GuardianRules.strip(cell/10.,0,14);check(zone>=0&&zone<=2,"finite fire strips");}}}
  check(GuardianRules.healAllowed(0,100,0)&&GuardianRules.healAllowed(1,100,0)&&GuardianRules.healAllowed(2,100,0)&&!GuardianRules.healAllowed(3,100,500),"single valve weakens but both stop healing");check(!GuardianRules.healAllowed(0,399,400)&&GuardianRules.healAllowed(0,400,400),"twenty second heal suppression");
  int hits=0;for(int i=0;i<5;i++)hits=GuardianRules.prisonHits(hits);check(hits==3&&GuardianRules.PRISON_MAX==80,"three actions and four second cap");check(GuardianRules.ECHO_HP==25&&GuardianRules.ECHO_LIFE==300,"bounded manifestations");
  UUID player=UUID.randomUUID();var sanct=new SanctuaryRecords();var ledger=new GuardianLedger();var ids=new ArrayList<UUID>();
  for(boolean water:new boolean[]{false,true})for(var r:Rotation.values()){UUID id=UUID.randomUUID();ids.add(id);var origin=SanctuaryGeometry.root(new BlockPos(1000,water?46:64,-800),water,r);sanct.ensure(id,origin,r,water);ledger.ensure(id,sanct.row(id));var n=ledger.row(id);check(ledger.stage(id).equals("SEALED"),"lazy instance begins sealed");check(GuardianWorld.arena(n).contains(net.minecraft.world.phys.Vec3.atBottomCenterOf(BlockPos.of(n.getLong("SpawnAnchor")))),"real spawn in actual rotated arena");check(ledger.near(BlockPos.of(n.getLong("SpawnAnchor"))).contains(id),"local chunk index");if(water){for(int role=0;role<2;role++)check(GuardianWorld.chamber(n).contains(net.minecraft.world.phys.Vec3.atCenterOf(GuardianWorld.valveAt(n,role))),"valves in old chamber");}ledger.participate(id,player);ledger.stage(id,ids.size()==1?"CLEARED":ids.size()==2?"PURIFICATION":"ACTIVE");n.putInt("Valves",1);n.putLong("ValveBlockedUntil",500);}
  var saved=ledger.save(new CompoundTag(),null);var claims=new CompoundTag();var perPlayer=new CompoundTag();perPlayer.putBoolean("Fire",true);claims.put(player.toString(),perPlayer);saved.put("Claims",claims);var loaded=GuardianLedger.load(saved,null);
  check(loaded.stage(ids.get(0)).equals("CLEARED")&&loaded.stage(ids.get(1)).equals("PURIFICATION"),"cleared and pending purification survive");check(loaded.stage(ids.get(2)).equals("READY"),"only unfinished combat resets");check(loaded.claimed(player,false)&&!loaded.claimed(player,true),"global per type claim persistence");for(var id:ids){check(loaded.row(id).getCompound("Participants").getBoolean(player.toString()),"offline participant retained");check(loaded.row(id).getInt("Valves")==0&&!loaded.row(id).contains("ValveBlockedUntil"),"temporary controls reset safely");check(loaded.near(BlockPos.of(loaded.row(id).getLong("SpawnAnchor"))).contains(id),"index rebuilt from saved metadata");}
  net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
  var fire=FireGuardianMesh.create();var water=SpringKeeperMesh.create();check(fire.getChild("pelvis").getChild("torso").getChild("core")!=null,"animated physical cavity core");for(String side:List.of("left","right")){var whip=water.getChild("pelvis").getChild("torso").getChild("arm_"+side).getChild("forearm_"+side).getChild("hand_"+side);for(int i=0;i<5;i++){whip=whip.getChild("whip_"+side+"_"+i);check(whip!=null,"linked whip segment");}}
  int vertices=uv(fire)+uv(water);check(vertices>5000,"two substantive native meshes");
  System.out.println("GUARDIAN_OFFLINE_PASS checks="+checks+" nativeUvVertices="+vertices+"; no game/server launches; NBT roundtrip is not relog");
 }
}
