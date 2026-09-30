package org.slavicmyths.registry;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slavicmyths.SlavicMyths;
import org.slavicmyths.entity.DomovoyEntity;
import org.slavicmyths.entity.LeshyEntity;

@Mod.EventBusSubscriber(modid = SlavicMyths.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, SlavicMyths.MOD_ID);
    public static final RegistryObject<EntityType<DomovoyEntity>> DOMOVOY = ENTITIES.register("domovoy",
            () -> EntityType.Builder.of(DomovoyEntity::new, EntityClassification.CREATURE)
                    .sized(0.55F, 1.1F).clientTrackingRange(8).build("slavicmyths:domovoy"));
    public static final RegistryObject<EntityType<LeshyEntity>> LESHY = ENTITIES.register("leshy",
            () -> EntityType.Builder.of(LeshyEntity::new, EntityClassification.MONSTER)
                    .sized(0.8F, 2.8F).clientTrackingRange(8).build("slavicmyths:leshy"));

    public static final RegistryObject<EntityType<org.slavicmyths.entity.KikimoraEntity>> KIKIMORA = ENTITIES.register("kikimora", () -> EntityType.Builder.of(org.slavicmyths.entity.KikimoraEntity::new, EntityClassification.MONSTER).sized(0.55F,2.25F).clientTrackingRange(10).build("slavicmyths:kikimora"));
    public static final RegistryObject<EntityType<org.slavicmyths.entity.PoludnitsaEntity>> POLUDNITSA = ENTITIES.register("poludnitsa", () -> EntityType.Builder.of(org.slavicmyths.entity.PoludnitsaEntity::new, EntityClassification.MONSTER).sized(0.6F,1.98F).clientTrackingRange(10).build("slavicmyths:poludnitsa"));
    public static final RegistryObject<EntityType<org.slavicmyths.entity.PolevikEntity>> POLEVIK = ENTITIES.register("polevik", () -> EntityType.Builder.of(org.slavicmyths.entity.PolevikEntity::new, EntityClassification.MONSTER).sized(0.6F,2.1F).clientTrackingRange(10).build("slavicmyths:polevik"));
    public static final RegistryObject<EntityType<org.slavicmyths.entity.BannikEntity>> BANNIK = ENTITIES.register("bannik", () -> EntityType.Builder.of(org.slavicmyths.entity.BannikEntity::new, EntityClassification.MONSTER).sized(0.9F,1.45F).clientTrackingRange(10).build("slavicmyths:bannik"));
    public static final RegistryObject<EntityType<org.slavicmyths.entity.IgoshaEntity>> IGOSHA = ENTITIES.register("igosha", () -> EntityType.Builder.of(org.slavicmyths.entity.IgoshaEntity::new, EntityClassification.MONSTER).sized(0.5F,0.75F).clientTrackingRange(10).build("slavicmyths:igosha"));
    public static final RegistryObject<EntityType<org.slavicmyths.entity.OvinnikEntity>> OVINNIK = ENTITIES.register("ovinnik", () -> EntityType.Builder.of(org.slavicmyths.entity.OvinnikEntity::new, EntityClassification.MONSTER).sized(1.5F,1.6F).clientTrackingRange(10).build("slavicmyths:ovinnik"));
    public static final RegistryObject<EntityType<org.slavicmyths.entity.HotStoneEntity>> HOT_STONE = ENTITIES.register("hot_stone", () -> EntityType.Builder.<org.slavicmyths.entity.HotStoneEntity>of(org.slavicmyths.entity.HotStoneEntity::new, EntityClassification.MISC).sized(.25F,.25F).clientTrackingRange(4).updateInterval(10).build("slavicmyths:hot_stone"));
    public static final RegistryObject<EntityType<org.slavicmyths.entity.WildlifeEntity>> BROWN_BEAR = wildlife("brown_bear", org.slavicmyths.entity.WildlifeEntity.Kind.BEAR, 1.35F, 1.65F);
    public static final RegistryObject<EntityType<org.slavicmyths.entity.WildlifeEntity>> BEAR_CUB = wildlife("bear_cub", org.slavicmyths.entity.WildlifeEntity.Kind.CUB, .72F, .78F);
    public static final RegistryObject<EntityType<org.slavicmyths.entity.WildlifeEntity>> FOREST_WOLF = wildlife("forest_wolf", org.slavicmyths.entity.WildlifeEntity.Kind.WOLF, .82F, 1.05F);
    public static final RegistryObject<EntityType<org.slavicmyths.entity.WildlifeEntity>> BOAR = wildlife("boar", org.slavicmyths.entity.WildlifeEntity.Kind.BOAR, 1.05F, 1.0F);
    public static final RegistryObject<EntityType<org.slavicmyths.entity.WildlifeEntity>> STAG = wildlife("stag", org.slavicmyths.entity.WildlifeEntity.Kind.STAG, 1.0F, 2.1F);
    public static final RegistryObject<EntityType<org.slavicmyths.entity.WildlifeEntity>> DOE = wildlife("doe", org.slavicmyths.entity.WildlifeEntity.Kind.DOE, .9F, 1.8F);
    private static RegistryObject<EntityType<org.slavicmyths.entity.WildlifeEntity>> wildlife(String id, org.slavicmyths.entity.WildlifeEntity.Kind kind, float width, float height) {
        return ENTITIES.register(id, () -> EntityType.Builder.<org.slavicmyths.entity.WildlifeEntity>of((type,world) -> new org.slavicmyths.entity.WildlifeEntity(type,world,kind), EntityClassification.CREATURE).sized(width,height).clientTrackingRange(10).build("slavicmyths:"+id));
    }

    @SubscribeEvent
    public static void attributes(EntityAttributeCreationEvent event) {
        event.put(DOMOVOY.get(), DomovoyEntity.attributes().build());
        event.put(LESHY.get(), LeshyEntity.attributes().build());
        event.put(KIKIMORA.get(), org.slavicmyths.entity.KikimoraEntity.attributes().build());
        event.put(POLUDNITSA.get(), org.slavicmyths.entity.PoludnitsaEntity.attributes().build());
        event.put(POLEVIK.get(), org.slavicmyths.entity.PolevikEntity.attributes().build());
        event.put(BANNIK.get(), org.slavicmyths.entity.BannikEntity.attributes().build());
        event.put(IGOSHA.get(), org.slavicmyths.entity.IgoshaEntity.attributes().build());
        event.put(OVINNIK.get(), org.slavicmyths.entity.OvinnikEntity.attributes().build());
        event.put(BROWN_BEAR.get(), org.slavicmyths.entity.WildlifeEntity.attributes(org.slavicmyths.entity.WildlifeEntity.Kind.BEAR).build());
        event.put(BEAR_CUB.get(), org.slavicmyths.entity.WildlifeEntity.attributes(org.slavicmyths.entity.WildlifeEntity.Kind.CUB).build());
        event.put(FOREST_WOLF.get(), org.slavicmyths.entity.WildlifeEntity.attributes(org.slavicmyths.entity.WildlifeEntity.Kind.WOLF).build());
        event.put(BOAR.get(), org.slavicmyths.entity.WildlifeEntity.attributes(org.slavicmyths.entity.WildlifeEntity.Kind.BOAR).build());
        event.put(STAG.get(), org.slavicmyths.entity.WildlifeEntity.attributes(org.slavicmyths.entity.WildlifeEntity.Kind.STAG).build());
        event.put(DOE.get(), org.slavicmyths.entity.WildlifeEntity.attributes(org.slavicmyths.entity.WildlifeEntity.Kind.DOE).build());

    }
    private ModEntities() { }
    public static final RegistryObject<EntityType<org.slavicmyths.flight.FlyingVessel>> FLYING_BROOM=ENTITIES.register("flying_broom",()->EntityType.Builder.<org.slavicmyths.flight.FlyingVessel>of((t,w)->new org.slavicmyths.flight.FlyingVessel(t,w,false),EntityClassification.MISC).sized(1F,.45F).clientTrackingRange(10).updateInterval(3).build("slavicmyths:flying_broom"));
    public static final RegistryObject<EntityType<org.slavicmyths.flight.FlyingVessel>> FLYING_MORTAR=ENTITIES.register("flying_mortar",()->EntityType.Builder.<org.slavicmyths.flight.FlyingVessel>of((t,w)->new org.slavicmyths.flight.FlyingVessel(t,w,true),EntityClassification.MISC).sized(.95F,1.2F).clientTrackingRange(10).updateInterval(3).build("slavicmyths:flying_mortar"));
}
