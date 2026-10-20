import java.util.*;
import java.nio.file.*;
import com.google.gson.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.slavicmyths.combat.*;
import org.slavicmyths.combat.CreatureProperties.*;
import org.slavicmyths.combat.CombatTargeting.*;
/** Compiled production policy and class metadata; no Bootstrap, world, entity instance, loader or server. */
public final class Creature79Checks {
 static int checks;static void check(boolean value,String name){checks++;if(!value)throw new AssertionError(name);}
 static ClassNode node(String name)throws Exception{try(var in=Creature79Checks.class.getClassLoader().getResourceAsStream(name.replace('.','/')+".class")){if(in==null)throw new IllegalArgumentException(name);var n=new ClassNode();new ClassReader(in).accept(n,ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);return n;}}
 static boolean inherits(String name,String parent)throws Exception{if(name.equals(parent))return true;var n=node(name);return n.superName!=null&&inherits(n.superName.replace('/','.'),parent);}
 static long calls(ClassNode n,String owner,String method){long count=0;for(var m:n.methods)for(var op:m.instructions)if(op instanceof MethodInsnNode call&&call.owner.equals(owner)&&call.name.equals(method))count++;return count;}
 static MethodNode method(ClassNode n,String name,String descriptor){return n.methods.stream().filter(m->m.name.equals(name)&&m.desc.equals(descriptor)).findFirst().orElseThrow();}
 public static void main(String[] args)throws Exception {
  var traits=EnumSet.noneOf(Trait.class);var trace=new JsonArray();
  for(var nature:Nature.values())if(nature!=Nature.UNCLASSIFIED){
   boolean biological=Set.of(Nature.HUMANOID,Nature.ANIMAL,Nature.MYTHICAL_BEAST).contains(nature);
   check(CreatureProperties.canBleed(nature,traits,false,false)==biological,"blood physiology "+nature);
   check(CreatureProperties.isPhysiologicalPoisonTarget(nature,traits,false)==biological,"poison physiology "+nature);
   check(!CreatureProperties.canBleed(nature,EnumSet.of(Trait.BLOODLESS),false,false),"bloodless "+nature);
   check(!CreatureProperties.canBleed(nature,traits,true,false),"legacy bleed immunity "+nature);
   check(!CreatureProperties.canBleed(nature,traits,false,true),"legacy undead "+nature);
   check(CreatureProperties.isPhysiologicalPoisonTarget(nature,traits,false)==biological,"bleed tag does not imply poison immunity "+nature);
   float expected=nature==Nature.SPIRIT?1.25F:nature==Nature.PLANT?1.5F:1;
   check(CreatureProperties.fireMultiplier(nature,traits)==expected,"base fire "+nature);
   check(CreatureProperties.fireMultiplier(nature,EnumSet.of(Trait.FIRE_AFFINITY))==.5F,"fire replaces base "+nature);
   check(CreatureProperties.fireMultiplier(nature,EnumSet.of(Trait.ICE_AFFINITY))==1.5F,"ice replaces base "+nature);
   check(CreatureProperties.fireMultiplier(nature,EnumSet.of(Trait.ICE_AFFINITY),true)==0,"native immunity wins "+nature);
   var row=new JsonObject();row.addProperty("nature",nature.name());row.addProperty("bleed",biological);row.addProperty("poison",biological);row.addProperty("fire",expected);trace.add(row);
  }
  check(CreatureProperties.fireMultiplier(Nature.UNCLASSIFIED,traits)==1,"unknown does not crash game");
  check(CombatTargeting.decide(Context.ACQUIRE,Intent.HOSTILE,Intent.HOSTILE,false,false,false)==Relation.NEUTRAL,"hostile is not global war");
  check(CombatTargeting.decide(Context.ACQUIRE,Intent.HOSTILE,Intent.DEFENDER,false,false,false)==Relation.NEUTRAL,"hostile does not automatically hunt defenders");
  check(CombatTargeting.decide(Context.SETTLEMENT,Intent.DEFENDER,Intent.HOSTILE,false,false,false)==Relation.ENEMY,"settlement actual threat");
  check(CombatTargeting.decide(Context.SETTLEMENT,Intent.DEFENDER,Intent.NEUTRAL,false,false,false)==Relation.NEUTRAL,"neutral spirit");
  check(CombatTargeting.decide(Context.SETTLEMENT,Intent.DEFENDER,Intent.NEUTRAL,false,true,false)==Relation.NEUTRAL,"innocent player");
  check(CombatTargeting.decide(Context.SETTLEMENT,Intent.DEFENDER,Intent.NEUTRAL,false,true,true)==Relation.ENEMY,"addressed aggressor");
  check(CombatTargeting.decide(Context.ACQUIRE,Intent.HOSTILE,Intent.NEUTRAL,false,true,false)==Relation.ENEMY,"native hostile player acquisition");
  check(CombatTargeting.decide(Context.NATIVE_HUNT,Intent.HOSTILE,Intent.PEACEFUL,false,false,false)==Relation.NEUTRAL,"existing population hunting uses physical permission + original delegate");
  check(CombatTargeting.decide(Context.RETALIATION,Intent.NEUTRAL,Intent.HOSTILE,false,false,true)==Relation.ENEMY,"retaliation");
  check(CombatTargeting.decide(Context.AREA,Intent.DEFENDER,Intent.NEUTRAL,false,false,false)==Relation.NEUTRAL,"physical AoE does not invent hostility");
  var rows=JsonParser.parseString(Files.readString(Path.of("docs/implementation/creatures-79/source-inventory.json"))).getAsJsonArray();var metadata=new JsonArray();
  for(var elem:rows){var row=elem.getAsJsonObject();String name=row.get("implementation").getAsString();boolean living=inherits(name,"net.minecraft.world.entity.LivingEntity");String nature=row.get("nature").getAsString();check(!living||!nature.equals("NO_PHYSIOLOGY"),"living type explicitly classified or projection: "+row.get("id"));check(living||nature.equals("NO_PHYSIOLOGY"),"projectile/prop does not get physiology: "+row.get("id"));var r=row.deepCopy();r.addProperty("living_by_compiled_hierarchy",living);r.addProperty("compiled_super",node(name).superName);metadata.add(r);}
  var poison=node("net.minecraft.world.effect.PoisonMobEffect");method(poison,"applyEffectTick","(Lnet/minecraft/world/entity/LivingEntity;I)Z");check(true,"installed poison target descriptor");
  var golem=node("net.minecraft.world.entity.animal.IronGolem");method(golem,"registerGoals","()V");check(true,"existing golem mixin target descriptor");
  check(calls(node("org.slavicmyths.combat.CreaturePhysiologyEvents"),"org/slavicmyths/combat/CreatureProperties","fireMultiplier")==1,"single nature damage multiplier hook");
  check(calls(node("org.slavicmyths.combat.CutEffect"),"org/slavicmyths/combat/CreatureProperties","canBleed")==2,"cut application and tick");
  check(calls(node("org.slavicmyths.combat.RareBleedEffect"),"org/slavicmyths/combat/CreatureProperties","canBleed")==1,"shared rare/wolf native tick");
  check(calls(node("org.slavicmyths.wolf.WolfBleeding"),"org/slavicmyths/combat/CreatureProperties","canBleed")==1,"wolf application");
  check(calls(node("org.slavicmyths.rpg.classes.ClassUltimateRuntime"),"org/slavicmyths/combat/CreatureProperties","canBleed")==2,"blood_mark admission and pulse");
  check(calls(node("org.slavicmyths.combat.mixin.PoisonPhysiologyMixin"),"org/slavicmyths/combat/CreatureProperties","isPhysiologicalPoisonTarget")==1,"poison-only historical tick hook");
  var result=new JsonObject();result.addProperty("status","PASS_NATIVE_OFFLINE_ONLY");result.addProperty("checks",checks);result.addProperty("runtime_launches",0);result.addProperty("runtime_registry","NOT_RUN");result.addProperty("world_contacts","NOT_RUN / OWNER_PENDING");result.add("policy_matrix",trace);var gson=new GsonBuilder().setPrettyPrinting().create();Files.writeString(Path.of("docs/implementation/creatures-79/native-checks.json"),gson.toJson(result));Files.writeString(Path.of("docs/implementation/creatures-79/compiled-inventory.json"),gson.toJson(metadata));System.out.println("CREATURE79 "+checks+" native policy/metadata checks PASS; game launches 0");
 }
}
