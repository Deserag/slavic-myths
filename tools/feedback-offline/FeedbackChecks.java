import java.util.*;
import java.nio.file.*;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.*;
import org.joml.Vector3f;
import org.slavicmyths.client.CarcassRenderer;
import org.slavicmyths.brewing.BrewRules;
import org.slavicmyths.tales.TaleData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.slavicmyths.garden.PerennialBush;

/** Native mesh/UV and durable NBT tests only. No Minecraft bootstrap, server or world. */
public final class FeedbackChecks {
 static int checks;
 static void check(boolean b,String reason){checks++;if(!b)throw new AssertionError(reason);}
 static Object field(Object o,String name)throws Exception{var f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);}
 static JsonArray capture(ModelPart model,float scale)throws Exception{
  var result=new JsonArray();var pose=new PoseStack();pose.scale(scale,scale,scale);
  for(var cube:(List<?>)field(model,"cubes"))for(var polygon:(Object[])field(cube,"polygons")){
   var face=new JsonArray();for(var vertex:(Object[])field(polygon,"vertices")){
    var at=pose.last().pose().transformPosition(new Vector3f((Vector3f)field(vertex,"pos")).div(16)).mul(16);
    float u=(float)field(vertex,"u"),v=(float)field(vertex,"v");check(u>=0&&u<=1&&v>=0&&v<=1,"UV bounds");
    var row=new JsonArray();for(float value:new float[]{at.x,at.y,at.z,u,v}){check(Float.isFinite(value),"finite native mesh");row.add(value);}face.add(row);
   }result.add(face);
  }return result;
 }
 public static void main(String[] args)throws Exception{
  var out=Path.of(System.getProperty("slavicmyths.feedbackOutput","docs/implementation/feedback-20261024"));Files.createDirectories(out);var poses=new JsonObject();
  var model=CarcassRenderer.createBundle();for(float size:new float[]{1,.9F,.8F,.55F,.5F,.35F}){
   poses.add("before_"+size,capture(model,size/16));poses.add("after_"+size,capture(model,size));
  }
  net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
  // Offline fixture only: allow constructing five unregistered blocks after vanilla bootstrap.
  var frozen=net.minecraft.core.MappedRegistry.class.getDeclaredField("frozen");frozen.setAccessible(true);frozen.setBoolean(net.minecraft.core.registries.BuiltInRegistries.BLOCK,false);
  var intrusive=net.minecraft.core.MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");intrusive.setAccessible(true);intrusive.set(net.minecraft.core.registries.BuiltInRegistries.BLOCK,new IdentityHashMap<>());
  frozen.setBoolean(net.minecraft.core.registries.BuiltInRegistries.ITEM,false);intrusive.set(net.minecraft.core.registries.BuiltInRegistries.ITEM,new IdentityHashMap<>());
  var staffs=List.of(new org.slavicmyths.item.StormStaffItem(new net.minecraft.world.item.Item.Properties().durability(240)),new org.slavicmyths.artifact.VelesStaffItem(new net.minecraft.world.item.Item.Properties()),new org.slavicmyths.morozko.FrostStaff());
  double[] expected={6,4,8};for(int i=0;i<staffs.size();i++){
   var staff=staffs.get(i);double damage=1,speed=4;for(var entry:staff.components().get(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS).modifiers()){
    check(entry.slot()==net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND,"staff mainhand only");
    if(entry.attribute().equals(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE))damage+=entry.modifier().amount();
    if(entry.attribute().equals(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED))speed+=entry.modifier().amount();
   }
   check(damage==expected[i],"native staff attack damage");check(Math.abs(speed-1.2)<.0001,"staff speed");check(staff.getEnchantmentValue()>0,"staff enchanting value");
  }
  check(org.slavicmyths.morozko.FrostRules.STAFF_STREAM_DAMAGE==3,"late staff stream pulse");
  var blocks=new HashMap<BlockPos,BlockState>();
  var fixture=(LevelAccessor)java.lang.reflect.Proxy.newProxyInstance(FeedbackChecks.class.getClassLoader(),new Class<?>[]{LevelAccessor.class},(o,m,a)->switch(m.getName()){
   case "getBlockState"->blocks.getOrDefault((BlockPos)a[0],Blocks.AIR.defaultBlockState());
   case "isEmptyBlock"->blocks.getOrDefault((BlockPos)a[0],Blocks.AIR.defaultBlockState()).isAir();
   case "setBlock"->{blocks.put(((BlockPos)a[0]).immutable(),(BlockState)a[1]);yield true;}
   case "getMinBuildHeight"->-64;case "getHeight"->384;case "getMaxBuildHeight"->320;
   case "hasChunk","hasChunkAt","hasChunksAt","isAreaLoaded"->true;
   default->throw new UnsupportedOperationException("Unexpected fixture call: "+m.getName());
  });
  var pos=new BlockPos(0,64,0);
  for(int kind=0;kind<5;kind++){
   var bush=new PerennialBush(kind);
   for(int phase=0;phase<=4;phase++){
    blocks.clear();blocks.put(pos,bush.defaultBlockState().setValue(PerennialBush.PHASE,phase));
    check(bush.isValidBonemealTarget(fixture,pos,blocks.get(pos))==(phase<4),"all berry kinds/stages bonemeal target");
    if(phase<4){check(bush.growTo(fixture,pos,phase+1),"berry growth succeeds");check(blocks.get(pos).getValue(PerennialBush.PHASE)==phase+1,"root advances");if(kind==0){check(blocks.get(pos.above()).getValue(PerennialBush.HALF)==DoubleBlockHalf.UPPER,"raspberry upper created");check(blocks.get(pos.above()).getValue(PerennialBush.PHASE)==phase+1,"halves synchronized");}}
   }
   blocks.clear();check(bush.growTo(fixture,pos,1),"worldgen may plant air");check(bush.growTo(fixture,pos,4),"ripening");check(bush.growTo(fixture,pos,1),"harvest regrowth");check(bush.isValidBonemealTarget(fixture,pos,blocks.get(pos)),"regrowth accepts bone meal");
   if(kind==0){blocks.put(pos.above(),Blocks.STONE.defaultBlockState());check(!bush.growTo(fixture,pos,2),"blocked raspberry refuses growth");check(!bush.isValidBonemealTarget(fixture,pos,blocks.get(pos)),"blocked raspberry does not consume bone meal");}
  }
  var crop=new org.slavicmyths.farming.FarmingCrop(net.minecraft.world.level.block.state.BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT),()->net.minecraft.world.item.Items.WHEAT_SEEDS,1);
  for(int age=0;age<=6;age++)check(crop.isValidBonemealTarget(fixture,pos,crop.getStateForAge(age))==(age<6),"seven-stage crop bone meal");
  var hops=new org.slavicmyths.brewing.HopsCrop(net.minecraft.world.level.block.state.BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT));
  for(int age=0;age<=5;age++){
   blocks.clear();blocks.put(pos,hops.getStateForAge(age));check(hops.isValidBonemealTarget(fixture,pos,blocks.get(pos))==(age<5),"hops stages");
   if(age<5){check(hops.growTo(fixture,pos,age+1),"hops growth");if(age>=2)check(blocks.get(pos.above()).getValue(org.slavicmyths.brewing.HopsCrop.AGE)==age+1,"hops halves synchronized");}
  }
  var apple=new org.slavicmyths.garden.AppleLeaves(net.minecraft.world.level.block.state.BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES));
  for(int age=0;age<=3;age++){
   var leaves=apple.defaultBlockState().setValue(org.slavicmyths.garden.AppleLeaves.FRUIT_STAGE,age);
   check(!apple.isValidBonemealTarget(fixture,pos,leaves),"decorative apple leaves not fertilized");check(apple.isValidBonemealTarget(fixture,pos,leaves.setValue(org.slavicmyths.garden.AppleLeaves.FRUITABLE,true))==(age<3),"fruiting apple stages");
  }
  check(BrewRules.quality(2399).equals("young"),"young boundary");check(BrewRules.quality(2400).equals("ready"),"ready boundary");check(BrewRules.quality(8399).equals("ready"),"ready end");check(BrewRules.quality(8400).equals("aged"),"aged boundary");check(BrewRules.quality(12000).equals("spoiled"),"spoiled boundary");
  check(BrewRules.pourCount("ceramic_pitcher",true,false,3)==3,"partial pitcher");check(BrewRules.pourCount("wooden_mug",false,false,8)==0,"wrong vessel");
  var data=new TaleData();UUID player=UUID.randomUUID(),other=UUID.randomUUID();var root=TaleData.compound(data.player(player),"ChudResearch");var quest=new CompoundTag();quest.putLong("Target",1234);quest.putLong("Owner",5678);quest.putString("Dimension","minecraft:overworld");quest.putBoolean("Visited",true);quest.putBoolean("Claimed",true);root.put("yaga",quest);root.putLongArray("VisitedStarts",new long[]{5678});
  var saved=data.save(new CompoundTag(),null);var loaded=TaleData.load(saved,null);var q=loaded.player(player).getCompound("ChudResearch").getCompound("yaga");check(q.getLong("Target")==1234&&q.getLong("Owner")==5678,"durable quest target");check(q.getBoolean("Visited")&&q.getBoolean("Claimed"),"durable claim state");check(!loaded.player(other).contains("ChudResearch"),"player isolation");check(loaded.player(player).getCompound("ChudResearch").getLongArray("VisitedStarts")[0]==5678,"durable explored sites");
  var fresh=q.copy();fresh.remove("Visited");fresh.remove("Claimed");
  check(!org.slavicmyths.chud.ChudResearchProgress.claim(fresh),"no reward before visit");check(!org.slavicmyths.chud.ChudResearchProgress.visit(fresh,"minecraft:the_nether",5678),"wrong dimension");check(!org.slavicmyths.chud.ChudResearchProgress.visit(fresh,"minecraft:overworld",9876),"wrong settlement");
  check(org.slavicmyths.chud.ChudResearchProgress.visit(fresh,"minecraft:overworld",5678),"actual assigned start visit");check(!org.slavicmyths.chud.ChudResearchProgress.visit(fresh,"minecraft:overworld",5678),"visit is idempotent");check(org.slavicmyths.chud.ChudResearchProgress.claim(fresh),"first reward");check(!org.slavicmyths.chud.ChudResearchProgress.claim(fresh),"duplicate reward rejected");check(!org.slavicmyths.chud.ChudResearchProgress.claim(q),"duplicate reward rejected after save/load");
  Files.writeString(out.resolve("native-carcass.json"),new Gson().toJson(poses));var report=new JsonObject();report.addProperty("checks",checks);report.addProperty("minecraft_processes",0);report.addProperty("scope","Native ModelPart/UV capture; NBT roundtrip and brewing arithmetic, not world/runtime tests");Files.writeString(out.resolve("offline-checks.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));System.out.println("Feedback offline checks: "+checks+" PASS; Minecraft processes=0");
 }
}
