package org.slavicmyths.verify;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.gorodishche.*;

@GameTestHolder("slavicmyths_city") @PrefixGameTestTemplate(false)
public final class CityPolishGameTests {
 @GameTest(template="empty",timeoutTicks=200)
 public static void rawFurnishings(GameTestHelper t)throws Exception{
  int stocks=0,apertures=0,towers=0;var level=t.getLevel();
  for(var b:CityCatalog.buildings()){
   var n=level.getStructureManager().get(b.id()).orElseThrow().save(new CompoundTag());var palette=n.getList("palette",10);Map<BlockPos,CompoundTag>blocks=new HashMap<>();
   for(var raw:n.getList("blocks",10)){var row=(CompoundTag)raw;var a=row.getList("pos",3);blocks.put(new BlockPos(a.getInt(0),a.getInt(1),a.getInt(2)),palette.getCompound(row.getInt("state")));var tag=row.getCompound("nbt");
    for(var item:tag.getList("Items",10)){var stack=net.minecraft.world.item.ItemStack.parseOptional(level.registryAccess(),(CompoundTag)item);t.assertTrue(!stack.isEmpty(),"Unregistered interior stock "+b.id()+" "+item);stocks++;}
    if(tag.contains("Weapon"))t.assertTrue(!net.minecraft.world.item.ItemStack.parseOptional(level.registryAccess(),tag.getCompound("Weapon")).isEmpty(),"Invalid rack weapon "+b.id());
   }
   if(b.name().equals("tower_corner_01")){towers++;for(int x:new int[]{0,8})for(int z:new int[]{0,8})for(int y=0;y<=11;y++)t.assertTrue(blocks.get(new BlockPos(x,y,z)).getString("Name").equals("minecraft:dark_oak_log"),"Broken tower post "+b.id()+" "+x+","+y+","+z);t.assertTrue(blocks.values().stream().noneMatch(s->s.getString("Name").endsWith("_fence")),"Tower fence returned");}
   if(b.category().equals("residential")||b.category().equals("profession")||b.name().startsWith("trading_house")){
    int porch=b.climate().equals("warm")?3:2,z0=porch+1,z1=b.depth()-2,x1=b.width()-2;
    for(int y:b.floors()>1?new int[]{2,7}:new int[]{2})for(int z:new int[]{z0,z1})for(int x:new int[]{3,x1-2}){
     String name=blocks.get(new BlockPos(x,y,z)).getString("Name");t.assertTrue(name.equals("minecraft:glass_pane")||name.endsWith("_trapdoor"),"Empty framed window "+b.id());int radius=b.width()<13?0:1;for(int dx=-radius;dx<=radius;dx++)t.assertTrue(blocks.get(new BlockPos(x+dx,y,z)).getString("Name").equals(name),"Mixed glass/shutters "+b.id());apertures++;
    }
    int stairX=b.name().startsWith("trading_house")?3:x1-2;
    if(b.floors()>1)for(int dz=1;dz<=2;dz++)t.assertTrue(blocks.get(new BlockPos(stairX,1,z0+dz)).getString("Name").equals("minecraft:air"),"Stair landing obstructed "+b.id());
   }
  }
  for(var e:level.getServer().getResourceManager().listResources("loot_table/chests/gorodishche",id->id.getNamespace().equals("slavicmyths")&&id.getPath().endsWith(".json")).entrySet())try(var reader=new java.io.InputStreamReader(e.getValue().open(),java.nio.charset.StandardCharsets.UTF_8)){
   var json=com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();for(var pool:json.getAsJsonArray("pools"))for(var entry:pool.getAsJsonObject().getAsJsonArray("entries")){var item=ResourceLocation.parse(entry.getAsJsonObject().get("name").getAsString());t.assertTrue(BuiltInRegistries.ITEM.containsKey(item),"Missing loot item "+item);}
  }
  t.assertTrue(stocks>500&&apertures>500&&towers==3,"Incomplete furnishing catalog");System.out.println("CITY_POLISH_ASSETS_PASS stocks="+stocks+" windows="+apertures+" continuousPosts=true stairLandings=true registeredLoot=true");t.succeed();
 }

 @GameTest(template="empty",batch="city_placement",timeoutTicks=200)
 public static void placedLightsAndSupplies(GameTestHelper t)throws Exception{
  var level=t.getLevel();int x=900000,z=900000;var source=level.getServer().createCommandSourceStack().withLevel(level).withPosition(new net.minecraft.world.phys.Vec3(x-8,-56,z-8));t.assertTrue(level.getServer().getCommands().getDispatcher().execute("sm test gorodishche_showcase cold",source)>=18,"Polish showcase failed");var plan=CityPlan.make("cold",125176,x,z,(a,b)->-57,true);Set<net.minecraft.world.level.ChunkPos>held=new HashSet<>();
  for(int cx=x>>4;cx<=(x+175)>>4;cx++)for(int cz=z>>4;cz<=(z+175)>>4;cz++){held.add(new net.minecraft.world.level.ChunkPos(cx,cz));level.setChunkForced(cx,cz,true);}
  t.runAfterDelay(40,()->{try{
   int street=0,room=0,stocks=0,chests=0;
   for(var lamp:CityRoadPiece.lamps(plan)){var pos=lamp.base().relative(lamp.inward()).above(3);var state=level.getBlockState(pos);t.assertTrue(state.is(Blocks.LANTERN)&&state.canSurvive(level,pos),"Street lantern missing/unsupported "+pos);t.assertTrue(level.getBrightness(LightLayer.BLOCK,pos.above(-2))>0,"Street has no block light "+pos);street++;}
   for(var p:plan.placements()){
    var template=level.getStructureManager().get(p.building().id()).orElseThrow();var n=template.save(new CompoundTag());
    for(var raw:n.getList("blocks",10)){var row=(CompoundTag)raw;var a=row.getList("pos",3);var local=new BlockPos(a.getInt(0),a.getInt(1),a.getInt(2));var pos=p.transform(local);var state=level.getBlockState(pos);var tag=row.getCompound("nbt");
     if(n.getList("palette",10).getCompound(row.getInt("state")).getString("Name").equals("minecraft:lantern")){t.assertTrue(state.is(Blocks.LANTERN)&&state.canSurvive(level,pos),"Unsupported placed lantern "+p.building().id()+" "+local);room++;}
     if(!tag.getList("Items",10).isEmpty()){
      var be=level.getBlockEntity(pos);int actual=be instanceof net.minecraft.world.Container c?java.util.stream.IntStream.range(0,c.getContainerSize()).map(i->c.getItem(i).getCount()).sum():be instanceof org.slavicmyths.kitchen.TableTile table?table.food.stream().mapToInt(net.minecraft.world.item.ItemStack::getCount).sum():0;
      int expected=tag.getList("Items",10).stream().mapToInt(item->((CompoundTag)item).getInt("count")).sum();t.assertTrue(actual==expected,"Lost real inventory "+p.building().id()+" "+local+" expected="+expected+" actual="+actual);stocks++;
     }
     if(tag.contains("LootTable")&&level.getBlockEntity(pos)instanceof ChestBlockEntity chest){chest.unpackLootTable(null);t.assertTrue(!chest.isEmpty(),"Empty household loot "+p.building().id());chests++;}
    }
    if(p.building().name().equals("tower_corner_01")){
     int tx=p.box().minX(),tz=p.box().minZ(),sx=tx==x?tx+8:tx,sz=tz==z?tz+8:tz,innerX=tx==x?tx+1:tx+7,innerZ=tz==z?tz+1:tz+7,y=p.origin().getY()+3;
     for(var pair:List.of(List.of(new BlockPos(sx,y,innerZ),new BlockPos(sx+(tx==x?1:-1),y,innerZ)),List.of(new BlockPos(innerX,y,sz),new BlockPos(innerX,y,sz+(tz==z?1:-1)))))for(var pos:pair)t.assertTrue(level.getBlockState(pos).isSolid(),"Wall/tower gap "+pos);
    }
   }
   t.assertTrue(street>=40&&room>=60&&stocks>=40&&chests>=18,"Missing city lighting/furnishings");System.out.println("CITY_POLISH_PLACED_PASS streetLights="+street+" roomLights="+room+" realStockedInventories="+stocks+" unpackedLootChests="+chests+" actualBlockLight=true wallTowerSeams=true");t.succeed();
  }finally{for(var q:held)level.setChunkForced(q.x,q.z,false);}});
 }
}
