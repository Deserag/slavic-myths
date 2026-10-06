package org.slavicmyths.verify;

import java.nio.file.*;
import java.util.*;
import net.minecraft.core.registries.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.gametest.*;

/** Actual loaded registry inventory; client renderer links are audited separately. */
@GameTestHolder("slavicmyths") @PrefixGameTestTemplate(false)
public final class MobAuditGameTests {
 @GameTest(template="port_empty", timeoutTicks=100)
 public static void registryAudit(GameTestHelper test)throws Exception {
  var report=new com.google.gson.JsonArray();
  var biomes=test.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
  for(var id:new TreeSet<>(BuiltInRegistries.ENTITY_TYPE.keySet())) {
   if(!id.getNamespace().equals("slavicmyths"))continue;
   var type=BuiltInRegistries.ENTITY_TYPE.get(id);var entity=type.create(test.getLevel());
   test.assertTrue(entity!=null,"Cannot construct "+id);
   var row=new com.google.gson.JsonObject();row.addProperty("id",id.toString());
   row.addProperty("class",entity.getClass().getName());row.addProperty("category",type.getCategory().getName());
   if(entity instanceof LivingEntity living) {
    row.addProperty("lootTable",living.getLootTable().location().toString());
    for(var attribute:List.of(Attributes.MAX_HEALTH,Attributes.ATTACK_DAMAGE,Attributes.ARMOR,Attributes.MOVEMENT_SPEED,Attributes.FOLLOW_RANGE))
     if(living.getAttribute(attribute)!=null)row.addProperty(attribute.getRegisteredName(),living.getAttributeValue(attribute));
   }
   var eggs=new com.google.gson.JsonArray();
   for(var item:BuiltInRegistries.ITEM)if(item instanceof SpawnEggItem egg&&egg.getType(net.minecraft.world.item.ItemStack.EMPTY)==type)eggs.add(BuiltInRegistries.ITEM.getKey(item).toString());
   row.add("spawnEggs",eggs);
   var placement=SpawnPlacements.getPlacementType(type);
   row.addProperty("placement",placement==SpawnPlacementTypes.ON_GROUND?"ON_GROUND":placement==SpawnPlacementTypes.IN_WATER?"IN_WATER":placement==SpawnPlacementTypes.NO_RESTRICTIONS?"NO_RESTRICTIONS":placement.getClass().getName());
   var spawns=new com.google.gson.JsonArray();
   for(var biome:biomes.entrySet())for(var category:MobCategory.values())for(var spawn:biome.getValue().getMobSettings().getMobs(category).unwrap())if(spawn.type==type) {
    var entry=new com.google.gson.JsonObject();entry.addProperty("biome",biome.getKey().location().toString());entry.addProperty("weight",spawn.getWeight().asInt());entry.addProperty("min",spawn.minCount);entry.addProperty("max",spawn.maxCount);spawns.add(entry);
   }
   row.add("naturalSpawns",spawns);report.add(row);
  }
  var root=Path.of(System.getProperty("slavicmyths.portRoot"));var dir=root.resolve("docs/verification/mob-worldgen-0.9.9");Files.createDirectories(dir);
  Files.writeString(dir.resolve("loaded-mob-registry.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(report)+"\n");
  System.out.println("MOB_RUNTIME_REGISTRY_AUDIT_PASS entries="+report.size());test.succeed();
 }
}
