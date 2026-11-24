package org.slavicmyths.icequeen;
import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;
public final class QueenContent {
 public static final DeferredHolder<EntityType<?>,EntityType<IceQueen>> BOSS=ModEntities.ENTITIES.register("ice_queen",()->EntityType.Builder.of(IceQueen::new,MobCategory.MONSTER).sized(.95F,2.5F).clientTrackingRange(16).updateInterval(1).build("slavicmyths:ice_queen"));
 public static final DeferredHolder<Item,Item> EGG=ModItems.ITEMS.register("ice_queen_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(BOSS,0x26365e,0x9eefff,new Item.Properties()));
 public enum Kind {
  WOLF("wolf",20,8,.65F,.85F), BEAR("bear",40,16,1.35F,1.4F), OWL("owl",15,10,.55F,.75F),
  WOLVERINE("wolverine",30,12,.75F,.75F), GOLEM("golem",40,14,.75F,1.5F), LARGE_GOLEM("large_golem",100,24,1.4F,2.7F),
  MELEE_COPY("melee_copy",200,22,1.1F,3F), RANGED_COPY("ranged_copy",150,14,1.1F,3F);
  public final String id; public final float hp,damage,width,height;
  Kind(String id,float hp,float damage,float width,float height){this.id="ice_queen_"+id;this.hp=hp;this.damage=damage;this.width=width;this.height=height;}
  public boolean copy(){return ordinal()>=6;}
 }
 public static final Map<Kind,DeferredHolder<EntityType<?>,EntityType<QueenMinion>>> MINIONS=new EnumMap<>(Kind.class);
 static {for(Kind k:Kind.values())MINIONS.put(k,ModEntities.ENTITIES.register(k.id,()->EntityType.Builder.<QueenMinion>of((t,w)->new QueenMinion(t,w,k),MobCategory.MONSTER).sized(k.width,k.height).noSave().clientTrackingRange(12).updateInterval(2).build("slavicmyths:"+k.id)));}
 public static void init(IEventBus bus){bus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent e)->{e.put(BOSS.get(),IceQueen.attributes().build());for(Kind k:Kind.values())e.put(MINIONS.get(k).get(),QueenMinion.attributes(k).build());});}
 private QueenContent(){}
}
