package org.slavicmyths.bossaudit;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import net.minecraft.core.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;
import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
/** Native arrow flights through three collider heights. Not visible-mesh limb acceptance. */
@GameTestHolder("slavicmyths_bossaudit") @PrefixGameTestTemplate(false)
public final class ProjectileAudit {
 record Probe(String id,double fraction,Mob mob,Arrow arrow,float health,Vec3 start){}
 @GameTest(template="empty",timeoutTicks=100,batch="projectiles")
 public static void actualArrowFlights(GameTestHelper t)throws Exception{
  var w=t.getLevel();var owner=AuditPlayers.create(w,"ArrowAuditOwner");owner.getAbilities().invulnerable=true;owner.setNoGravity(true);owner.moveTo(t.absoluteVec(new Vec3(0,150,0)));w.addNewPlayer(owner);
  var probes=new ArrayList<Probe>();int i=0;
  for(String id:new String[]{"gneteya","tryaseya","agneya","ledeya","lomeya","neveya","viy"})for(double fraction:new double[]{.1,.5,.9}){
   Vec3 pos=t.absoluteVec(new Vec3(32*(i++),150,32));var cp=new ChunkPos(BlockPos.containing(pos));for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++){w.setChunkForced(cp.x+dx,cp.z+dz,true);w.getChunk(cp.x+dx,cp.z+dz);}
   var mob=(Mob)BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",id)).create(w);mob.setNoAi(true);mob.setNoGravity(true);mob.setPersistenceRequired();mob.moveTo(pos);w.addFreshEntity(mob);
   var arrow=new Arrow(w,owner,new ItemStack(Items.ARROW),new ItemStack(Items.BOW));arrow.setNoGravity(true);arrow.setBaseDamage(2);arrow.setPos(pos.add(0,mob.getBbHeight()*fraction,6));arrow.setDeltaMovement(0,0,-2);w.addFreshEntity(arrow);probes.add(new Probe(id,fraction,mob,arrow,mob.getHealth(),pos));
  }
  t.runAfterDelay(10,()->{try{var rows=new JsonArray();for(var p:probes){var r=new JsonObject();r.addProperty("id",p.id);r.addProperty("collider_height_fraction",p.fraction);r.addProperty("health_loss",p.health-p.mob.getHealth());r.addProperty("entity_ticks",p.mob.tickCount);r.addProperty("mob_displacement",p.mob.position().distanceTo(p.start));r.addProperty("arrow_removed",p.arrow.isRemoved());r.addProperty("arrow_alive",p.arrow.isAlive());r.addProperty("arrow_ticks",p.arrow.tickCount);r.addProperty("arrow_position",p.arrow.position().toString());r.addProperty("arrow_velocity",p.arrow.getDeltaMovement().toString());r.addProperty("mob_aabb",p.mob.getBoundingBox().toString());r.addProperty("mob_pickable",p.mob.isPickable());r.addProperty("owner_game_mode",owner.gameMode.getGameModeForPlayer().getName());float hp=p.mob.getHealth();p.mob.invulnerableTime=0;boolean direct=p.mob.hurt(p.mob.damageSources().arrow(p.arrow,owner),4);r.addProperty("direct_arrow_source_control_accepted",direct);r.addProperty("direct_arrow_source_control_health_loss",hp-p.mob.getHealth());r.addProperty("status",p.mob.getHealth()<p.health?"NATIVE_ARROW_DAMAGE_OBSERVED":"NO_DAMAGE_REQUIRES_REPRO");r.addProperty("scope","Actual world-ticked projectile, NoAI/noGravity fixture, collider-relative heights; does NOT prove visible wings/hair/weapons/limbs are hittable or active attack resilience");rows.add(r);p.arrow.discard();p.mob.discard();}owner.discard();var out=Path.of(System.getProperty("bossaudit.output"));Files.createDirectories(out);Files.writeString(out.resolve("native-arrow-flights.json"),new GsonBuilder().setPrettyPrinting().create().toJson(rows));t.succeed();}catch(Exception ex){throw new RuntimeException(ex);}});
 }
}
