package org.slavicmyths.navya;

import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;

public final class NavyaContent {
    public enum Kind { NAVKA,ROOTLING,CRAWLER,WISP,GUARDIAN }
    public static final Map<Kind,DeferredHolder<EntityType<?>,EntityType<NavyaMob>>> MOBS=new EnumMap<>(Kind.class);
    public static final Map<String,DeferredHolder<Item,Item>> ITEMS=new LinkedHashMap<>();
    public static final Map<Kind,String> NAMES=Map.of(Kind.NAVKA,"navka",Kind.ROOTLING,"rootling",Kind.CRAWLER,"bone_crawler",Kind.WISP,"navya_wisp",Kind.GUARDIAN,"grove_guardian");
    static {
        for(Kind kind:Kind.values()) {
            String name=NAMES.get(kind);
            float width=switch(kind){case NAVKA->.7F;case ROOTLING->1.2F;case CRAWLER->1.8F;case WISP->.45F;case GUARDIAN->3F;};
            float height=switch(kind){case NAVKA->2.5F;case ROOTLING->1.8F;case CRAWLER->1.1F;case WISP->.6F;case GUARDIAN->6.5F;};
            var type=ModEntities.ENTITIES.register(name,()->EntityType.Builder.<NavyaMob>of((entity,world)->new NavyaMob(entity,world,kind),kind==Kind.WISP?MobCategory.AMBIENT:MobCategory.MONSTER)
                    .sized(width,height).eyeHeight(kind==Kind.GUARDIAN?3.25F:height*.85F).fireImmune().clientTrackingRange(kind==Kind.GUARDIAN?16:10).updateInterval(3).build("slavicmyths:"+name));
            MOBS.put(kind,type);
            ModItems.ITEMS.register(name+"_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(type,
                    kind==Kind.WISP?0x719c9a:0x29262b,kind==Kind.CRAWLER?0xb9ad92:kind==Kind.WISP?0xd4e5c6:0x892e69,new Item.Properties()));
        }
        for(String name:List.of("navya_ash","living_root","bone_thorn","grove_core","guardian_branch","soul_blight"))
            ITEMS.put(name,ModItems.ITEMS.register(name,()->new Item(new Item.Properties().rarity(name.equals("grove_core")?Rarity.EPIC:Rarity.UNCOMMON))));
    }
    public static final DeferredHolder<EntityType<?>,EntityType<NavyaShot>> SHOT=ModEntities.ENTITIES.register("navya_shot",()->EntityType.Builder.<NavyaShot>of(NavyaShot::new,MobCategory.MISC)
            .sized(.5F,.5F).clientTrackingRange(12).updateInterval(2).build("slavicmyths:navya_shot"));
    public static final DeferredHolder<EntityType<?>,EntityType<NavyaRoots>> ROOTS=ModEntities.ENTITIES.register("navya_attack_roots",()->EntityType.Builder.<NavyaRoots>of(NavyaRoots::new,MobCategory.MISC)
            .sized(1.6F,2F).clientTrackingRange(12).updateInterval(10).build("slavicmyths:navya_attack_roots"));
    public static Item item(String name) { return ITEMS.get(name).get(); }
    public static void init(net.neoforged.bus.api.IEventBus bus) {
        bus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent event)->{
            for(var entry:MOBS.entrySet()) event.put(entry.getValue().get(),NavyaMob.attributes(entry.getKey()).build());
        });
        NavyaSounds.init();
    }
    private NavyaContent() { }
}
