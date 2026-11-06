package org.slavicmyths.bossaudit;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.*;
import java.lang.reflect.*;
import java.util.*;
import java.nio.file.*;
import com.google.gson.*;
@GameTestHolder("slavicmyths_bossaudit") @PrefixGameTestTemplate(false)
public final class InterruptionAudit {
 static Field field(Class<?> c,String name)throws Exception{for(Class<?> k=c;k!=null;k=k.getSuperclass()){try{var f=k.getDeclaredField(name);f.setAccessible(true);return f;}catch(NoSuchFieldException ignored){}}throw new NoSuchFieldException(name);}
 @SuppressWarnings("unchecked") static void data(Mob m,String key,int value)throws Exception{m.getEntityData().set((EntityDataAccessor<Integer>)field(m.getClass(),key).get(null),value);}
 static void enter(Mob m,ServerPlayer p,int action,int frame)throws Exception{
  m.setTarget(p);String c=m.getClass().getSimpleName();
  if(c.equals("AgneyaBoss")){field(m.getClass(),"swingTick").setInt(m,frame);data(m,"ATTACK_STARTED",(int)m.level().getGameTime()-frame);return;}
  if(c.equals("LedeyaBoss")){data(m,"ATTACK_KIND",action);data(m,"ATTACK_STARTED",(int)m.level().getGameTime()-frame);return;}
  String method=c.equals("SisterBoss")||c.equals("LomeyaBoss")?"begin":"start";var call=m.getClass().getDeclaredMethod(method,int.class,Player.class);call.setAccessible(true);call.invoke(m,action,p);data(m,"FRAME",frame);
 }
 static int state(Mob m)throws Exception{String c=m.getClass().getSimpleName();if(c.equals("AgneyaBoss"))return field(m.getClass(),"swingTick").getInt(m)>=0?1:0;if(c.equals("LedeyaBoss"))return ((Enum<?>)m.getClass().getMethod("attack").invoke(m)).ordinal();return (int)m.getClass().getMethod("action").invoke(m);}
 @GameTest(template="empty",timeoutTicks=200,batch="interruption")
 public static void synchronousDamageAcrossStateFrames(GameTestHelper t)throws Exception{
  var w=t.getLevel();var player=AuditPlayers.create(w,"AuditDamageA");player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.setNoGravity(true);player.getAbilities().invulnerable=true;player.setPos(t.absoluteVec(new net.minecraft.world.phys.Vec3(5,200,5)));w.addNewPlayer(player);player.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_SWORD));
  String[] ids={"gneteya","tryaseya","agneya","ledeya","lomeya","neveya","viy"};var rows=new JsonArray();int failed=0,accepted=0;
  for(String id:ids){int[] actions=switch(id){case "gneteya"->new int[]{1,2,3,4,5};case "tryaseya"->new int[]{6,7,8,9,10};case "agneya"->new int[]{1};case "ledeya"->new int[]{0,1,2};case "lomeya"->new int[]{1,2,3,4,5};default->new int[]{1,2,3,4,5,6,7,8,9};};
   for(int action:actions)for(int frame:new int[]{1,8,14,20,26,32})for(String damage:new String[]{"native_sword","native_critical_candidate","direct_player","rapid_direct","knockback","mob_source","magic_source"}){
    var b=(Mob)BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",id)).create(w);b.setPos(player.position().add(0,0,2));b.setNoGravity(true);w.addFreshEntity(b);enter(b,player,action,frame);int before=state(b);float hp=b.getHealth();
    switch(damage){case "native_sword","native_critical_candidate"->{player.resetAttackStrengthTicker();player.fallDistance=damage.equals("native_sword")?0:1;player.setOnGround(damage.equals("native_sword"));player.attack(b);}case "rapid_direct"->{for(int k=0;k<5;k++)b.hurt(player.damageSources().playerAttack(player),2);}case "knockback"->{b.hurt(player.damageSources().playerAttack(player),5);b.knockback(.7,1,0);}case "mob_source"->b.hurt(b.damageSources().mobAttack(b),5);case "magic_source"->b.hurt(b.damageSources().magic(),5);default->b.hurt(player.damageSources().playerAttack(player),5);}
    int after=state(b);float loss=hp-b.getHealth();boolean pass=before==after;var row=new JsonObject();row.addProperty("entity_id","slavicmyths:"+id);row.addProperty("action",action);row.addProperty("injected_frame",frame);row.addProperty("damage",damage);row.addProperty("actual_health_loss",loss);row.addProperty("state_before",before);row.addProperty("state_after",after);row.addProperty("state_preserved",pass);row.addProperty("scope","SYNCHRONOUS_DAMAGE_ONLY; controlled private entry; phase/frame eligibility and real AI progress NOT proven; critical candidate not certified");rows.add(row);if(loss>0)accepted++;if(!pass)failed++;b.discard();
   }
  }
  player.discard();var out=Path.of(System.getProperty("bossaudit.output"));Files.createDirectories(out);Files.writeString(out.resolve("synchronous-interruptions.json"),new GsonBuilder().setPrettyPrinting().create().toJson(rows));System.out.println("BOSS_AUDIT_DAMAGE_COLLECTION rows="+rows.size()+" accepted_damage="+accepted+" state_changes="+failed+" NOT_FULL_ATTACK_QA");t.succeed();
 }
}
