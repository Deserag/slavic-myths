package org.slavicmyths.verify;
import java.util.*;
import java.nio.file.*;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;
import org.slavicmyths.burningislands.*;
import org.slavicmyths.burningislands.client.IslandsModel;
import org.slavicmyths.burningislands.MagmaCombat.Action;

/** No client/server/world/GameTest. Actual baked cuboids, pure combat, sockets and binary NBT. */
public final class IslandsOffline {
 private static int checks;
 private static void check(boolean result,String label){checks++;if(!result)throw new AssertionError(label);}
 public static void main(String[] args)throws Exception {
  net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());
  net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
  var output=Path.of("docs/implementation/burning-islands-205");Files.createDirectories(output);
  var rigs=new JsonObject();int frames=0;
  for(var kind:IslandsContent.Kind.values()){
   String name=kind.name().toLowerCase(Locale.ROOT);
   var source=JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/slavicmyths/burning_islands_rigs/"+name+".json"))).getAsJsonObject();
   var model=new IslandsModel(source);var all=new JsonArray();
   for(boolean second:new boolean[]{false,true})for(var action:Action.values())for(float time:new float[]{0,action.release*.5F,action.release,action.release+8,action.duration}){
    model.pose(kind,action,time,second,second&&action.heavy(),2,.6F,20,15,5,0,0);all.add(frame(model.root(),action.name()+(second?"_P2":"_P1"),time));frames++;
   }
   for(String state:List.of("hurt","death","idle")){model.pose(kind,Action.IDLE,0,false,false,0,0,20,0,0,state.equals("hurt")?6:0,state.equals("death")?15:0);all.add(frame(model.root(),state,0));frames++;}
   // Animation reset must restore the exact authored neutral ModelPart transforms.
   model.pose(kind,Action.IDLE,0,false,false,0,0,0,0,0,0,0);var before=frame(model.root(),"reset",0);
   model.pose(kind,Action.RING,45,true,true,8,1,70,50,30,5,10);model.pose(kind,Action.IDLE,0,false,false,0,0,0,0,0,0,0);
   check(before.equals(frame(model.root(),"reset",0)),"model pose reset "+name);
   var item=new JsonObject();item.add("source",source);item.add("frames",all);rigs.add(name,item);
   if(kind==IslandsContent.Kind.MAGMA_LORD){
    var trophy=org.slavicmyths.client.TrophyHeadsClient.magmaHead(model.part("head"));var headFrames=new JsonArray();
    for(float open:new float[]{0,.25F,.5F,.75F,1}){
     trophy.magmaPose(40,open);var capture=new org.slavicmyths.client.TrophyHeadsClient.Capture();trophy.render(new PoseStack(),capture,0,0);var bounds=capture.bounds();
     check(bounds[0]>=.029F&&bounds[2]>=.029F&&bounds[1]>=.039F&&bounds[3]<=.971F&&bounds[5]<=.971F&&bounds[4]<=.941F,"head animation stays inside collision envelope "+Arrays.toString(bounds));
     headFrames.add(frame(trophy.part,"jaw",open));
    }
    item.add("trophyFrames",headFrames);
   }
  }
  for(int i=0;i<500;i++){
   var a=MagmaCombat.choose(i,false,3);check(a==Action.CLAWS||a==Action.FRACTURE||a==Action.PULL,"P1 only approved techniques");
   check(MagmaCombat.choose(i,false,20)!=Action.CLAWS,"long range is not a blind melee cast");
  }
  check(Action.CLAWS.damage==14&&Action.FRACTURE.damage==18&&Action.PULL.damage==0&&Action.HANDS.damage==16&&Action.ERUPTION.damage==18&&Action.RING.damage==20,"approved raw damage");
  check(Action.CLAWS.release==23&&Action.FRACTURE.release==27&&Action.HANDS.release==25&&Action.ERUPTION.release==31&&Action.RING.release==37,"cue-sheet release frames");
  for(int f=3;f<22;f++){
   check(MagmaCombat.fracture(f,0),"central ray");check(!MagmaCombat.fracture(f,12),"dodge outside rays");
   check(!MagmaCombat.fracture(-f,0),"behind caster safe");
  }
  for(int pulse=0;pulse<3;pulse++){check(MagmaCombat.eruption(5+6*pulse,0,pulse),"eruption locked center");check(!MagmaCombat.eruption(5+6*pulse,3,pulse),"eruption side escape");}
  check(MagmaRuneRules.tier(true,true)==2&&MagmaRuneRules.damage(2)==4,"tiers never stack");
  for(int tier=1;tier<=2;tier++){
   check(MagmaRuneRules.ready(tier,1,true,false,140,140),"ready at exact cooldown");check(!MagmaRuneRules.ready(tier,1,true,false,139,140),"cooldown before expiry");
   check(!MagmaRuneRules.ready(tier,0,true,false,140,140),"zero damage");check(!MagmaRuneRules.ready(tier,Float.NaN,true,false,140,140),"NaN damage");
   check(!MagmaRuneRules.ready(tier,1,false,false,140,140),"environment is no attacker");check(!MagmaRuneRules.ready(tier,1,true,true,140,140),"recursive retaliation refused");
  }
  check(MagmaRuneRules.conflict(List.of("magma_1"),"magma_2")&&MagmaRuneRules.conflict(List.of("magma_2"),"magma_1"),"symmetric family conflict");
  check(!MagmaRuneRules.conflict(List.of("heat"),"magma_1"),"existing heat rune unaffected");
  for(var item:List.of(net.minecraft.world.item.Items.IRON_CHESTPLATE,net.minecraft.world.item.Items.DIAMOND_CHESTPLATE,net.minecraft.world.item.Items.NETHERITE_CHESTPLATE))check(MagmaRunes.supports(new net.minecraft.world.item.ItemStack(item)),"native chestplate support");
  check(!MagmaRunes.supports(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_HELMET)),"helmet rejected");
  check(!org.slavicmyths.rpg.Runes.installationReason(1,2,2,List.of("magma_1"),"magma_2",true).isEmpty(),"forge conflict integration");
  var lookup=net.minecraft.data.registries.VanillaRegistries.createLookup();var journal=new MagmaEncounters();var owner=UUID.randomUUID();
  check(!journal.claim(205,owner),"unregistered arena cannot yield rewards");check(journal.register(205,owner),"first encounter");check(!journal.register(205,UUID.randomUUID()),"duplicate encounter refused");
  check(!journal.register(206,owner),"same boss cannot own another anchor");check(journal.claim(205,owner)&&!journal.claim(205,owner),"one reward claim");
  var valid=journal.save(new net.minecraft.nbt.CompoundTag(),lookup);var bytes=new java.io.ByteArrayOutputStream();net.minecraft.nbt.NbtIo.writeCompressed(valid,bytes);
  var restored=MagmaEncounters.load(net.minecraft.nbt.NbtIo.readCompressed(new java.io.ByteArrayInputStream(bytes.toByteArray()),net.minecraft.nbt.NbtAccounter.unlimitedHeap()),lookup);
  check(!restored.claim(205,owner)&&!restored.register(205,owner),"binary NBT preserves victory");
  var duplicate=valid.copy();duplicate.getList("Encounters",10).add(duplicate.getList("Encounters",10).getCompound(0).copy());
  var future=valid.copy();future.putInt("Version",99);var malformed=valid.copy();malformed.getList("Encounters",10).getCompound(0).remove("Boss");
  for(var invalid:List.of(duplicate,future,malformed)){var q=MagmaEncounters.load(invalid,lookup);check(q.isReadOnly()&&!q.register(206,UUID.randomUUID())&&!q.claim(205,owner)&&q.save(new net.minecraft.nbt.CompoundTag(),lookup).equals(invalid),"malformed/future NBT fails closed and preserves bytes");}
  Files.writeString(output.resolve("native-rigs.json"),new Gson().toJson(rigs));
  var receipt=new JsonObject();receipt.addProperty("scope","Native ModelPart/UV + pure production policy + NBT, NOT gameplay");receipt.addProperty("checks",checks);receipt.addProperty("nativeFrames",frames);receipt.addProperty("minecraftLaunches",0);receipt.addProperty("runtime","NOT_RUN");Files.writeString(output.resolve("offline-results.json"),new GsonBuilder().setPrettyPrinting().create().toJson(receipt));
  System.out.println("PASS "+checks+" offline checks; "+frames+" native frames; Minecraft/client/server/world/GameTest launches=0.");
 }
 private static JsonObject frame(ModelPart root,String action,float time){
  var result=new JsonObject();result.addProperty("action",action);result.addProperty("time",time);var faces=new JsonArray();
  root.visit(new PoseStack(),(pose,path,index,cube)->{try{var field=ModelPart.Cube.class.getDeclaredField("polygons");field.setAccessible(true);for(Object polygon:(Object[])field.get(cube)){var vertices=polygon.getClass().getDeclaredField("vertices");vertices.setAccessible(true);var face=new JsonArray();for(Object vertex:(Object[])vertices.get(polygon)){var pf=vertex.getClass().getDeclaredField("pos");var uf=vertex.getClass().getDeclaredField("u");var vf=vertex.getClass().getDeclaredField("v");pf.setAccessible(true);uf.setAccessible(true);vf.setAccessible(true);var p=(Vector3f)pf.get(vertex);var v=pose.pose().transformPosition(p.x/16,p.y/16,p.z/16,new Vector3f()).mul(16);var point=new JsonArray();for(float value:new float[]{v.x,v.y,v.z,uf.getFloat(vertex),vf.getFloat(vertex)}){if(!Float.isFinite(value))throw new AssertionError("Nonfinite "+path);point.add(value);}if(uf.getFloat(vertex)<0||uf.getFloat(vertex)>1||vf.getFloat(vertex)<0||vf.getFloat(vertex)>1)throw new AssertionError("UV "+path);face.add(point);}faces.add(face);}}catch(ReflectiveOperationException ex){throw new RuntimeException(ex);}});
  result.add("faces",faces);return result;
 }
}
