package org.slavicmyths.bossaudit;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.*;
import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
@GameTestHolder("slavicmyths_bossaudit") @PrefixGameTestTemplate(false)
public final class RegistryAudit {
 @GameTest(template="empty",timeoutTicks=100)
 public static void exportBossRegistry(GameTestHelper t)throws Exception {
  var entries=new JsonArray();var errors=new JsonArray();
  var explicit=new HashSet<String>(java.util.Arrays.stream(new String(RegistryAudit.class.getResourceAsStream("/boss-audit-candidates.txt").readAllBytes(),java.nio.charset.StandardCharsets.UTF_8).split("\\n")).map(String::strip).toList());
  for(var type:BuiltInRegistries.ENTITY_TYPE){var id=BuiltInRegistries.ENTITY_TYPE.getKey(type);if(!id.getNamespace().equals("slavicmyths"))continue;
   try{var e=type.create(t.getLevel());if(!(e instanceof Mob m))continue;
    boolean bar=false;for(Class<?> c=e.getClass();c!=null;c=c.getSuperclass())for(var f:c.getDeclaredFields())if(net.minecraft.world.BossEvent.class.isAssignableFrom(f.getType()))bar=true;
    if(!bar&&!explicit.contains(id.getPath()))continue;
    var r=new JsonObject();r.addProperty("entity_id",id.toString());r.addProperty("entity_class",e.getClass().getName());r.addProperty("max_health",m.getMaxHealth());r.addProperty("width",m.getBbWidth());r.addProperty("height",m.getBbHeight());r.addProperty("no_gravity_default",m.isNoGravity());r.addProperty("has_bossbar_field",bar);r.addProperty("status","REGISTRY_ONLY_NOT_GAMEPLAY");entries.add(r);
   }catch(Throwable ex){var row=new JsonObject();row.addProperty("id",id.toString());row.addProperty("error",ex.toString());errors.add(row);}
  }
  var result=new JsonObject();result.add("bosses",entries);result.add("construction_errors",errors);var out=Path.of(System.getProperty("bossaudit.output"));Files.createDirectories(out);Files.writeString(out.resolve("registry.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));
  t.assertTrue(entries.size()>=12,"Incomplete registry export "+entries.size());System.out.println("BOSS_AUDIT_REGISTRY entries="+entries.size()+" construction_errors="+errors.size()+" NO_GAMEPLAY_CLAIM");t.succeed();
 }
}
