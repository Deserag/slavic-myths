package org.slavicmyths.registry;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import org.slavicmyths.SlavicMyths;
import org.slavicmyths.entity.DomovoyEntity;
import org.slavicmyths.entity.LeshyEntity;

@EventBusSubscriber(modid = SlavicMyths.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, SlavicMyths.MOD_ID);
    public static final DeferredHolder<EntityType<?>, EntityType<DomovoyEntity>> DOMOVOY = ENTITIES.register("domovoy",
            () -> EntityType.Builder.of(DomovoyEntity::new, MobCategory.CREATURE)
                    .sized(0.55F, 1.1F).clientTrackingRange(8).build("slavicmyths:domovoy"));
    public static final DeferredHolder<EntityType<?>, EntityType<LeshyEntity>> LESHY = ENTITIES.register("leshy",
            () -> EntityType.Builder.of(LeshyEntity::new, MobCategory.MONSTER)
                    .sized(0.8F, 2.8F).clientTrackingRange(8).build("slavicmyths:leshy"));

    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.entity.KikimoraEntity>> KIKIMORA = ENTITIES.register("kikimora", () -> EntityType.Builder.of(org.slavicmyths.entity.KikimoraEntity::new, MobCategory.MONSTER).sized(0.55F,2.25F).clientTrackingRange(10).build("slavicmyths:kikimora"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.entity.PoludnitsaEntity>> POLUDNITSA = ENTITIES.register("poludnitsa", () -> EntityType.Builder.of(org.slavicmyths.entity.PoludnitsaEntity::new, MobCategory.MONSTER).sized(0.6F,1.98F).clientTrackingRange(10).build("slavicmyths:poludnitsa"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.entity.PolevikEntity>> POLEVIK = ENTITIES.register("polevik", () -> EntityType.Builder.of(org.slavicmyths.entity.PolevikEntity::new, MobCategory.MONSTER).sized(0.6F,2.1F).clientTrackingRange(10).build("slavicmyths:polevik"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.entity.BannikEntity>> BANNIK = ENTITIES.register("bannik", () -> EntityType.Builder.of(org.slavicmyths.entity.BannikEntity::new, MobCategory.MONSTER).sized(0.9F,1.45F).clientTrackingRange(10).build("slavicmyths:bannik"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.entity.IgoshaEntity>> IGOSHA = ENTITIES.register("igosha", () -> EntityType.Builder.of(org.slavicmyths.entity.IgoshaEntity::new, MobCategory.MONSTER).sized(0.5F,0.75F).clientTrackingRange(10).build("slavicmyths:igosha"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.entity.OvinnikEntity>> OVINNIK = ENTITIES.register("ovinnik", () -> EntityType.Builder.of(org.slavicmyths.entity.OvinnikEntity::new, MobCategory.MONSTER).sized(1.15F,2.45F).clientTrackingRange(12).fireImmune().build("slavicmyths:ovinnik"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.entity.HotStoneEntity>> HOT_STONE = ENTITIES.register("hot_stone", () -> EntityType.Builder.<org.slavicmyths.entity.HotStoneEntity>of(org.slavicmyths.entity.HotStoneEntity::new, MobCategory.MISC).sized(.25F,.25F).clientTrackingRange(4).updateInterval(10).build("slavicmyths:hot_stone"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.entity.WildlifeEntity>> BROWN_BEAR = wildlife("brown_bear", org.slavicmyths.entity.WildlifeEntity.Kind.BEAR, 1.35F, 1.65F);
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.entity.WildlifeEntity>> BEAR_CUB = wildlife("bear_cub", org.slavicmyths.entity.WildlifeEntity.Kind.CUB, .72F, .78F);
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.entity.WildlifeEntity>> FOREST_WOLF = wildlife("forest_wolf", org.slavicmyths.entity.WildlifeEntity.Kind.WOLF, .82F, 1.05F);
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.entity.WildlifeEntity>> BOAR = wildlife("boar", org.slavicmyths.entity.WildlifeEntity.Kind.BOAR, 1.05F, 1.0F);
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.entity.WildlifeEntity>> STAG = wildlife("stag", org.slavicmyths.entity.WildlifeEntity.Kind.STAG, 1.0F, 2.1F);
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.entity.WildlifeEntity>> DOE = wildlife("doe", org.slavicmyths.entity.WildlifeEntity.Kind.DOE, .9F, 1.8F);
    private static DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.entity.WildlifeEntity>> wildlife(String id, org.slavicmyths.entity.WildlifeEntity.Kind kind, float width, float height) {
        return ENTITIES.register(id, () -> EntityType.Builder.<org.slavicmyths.entity.WildlifeEntity>of((type,world) -> new org.slavicmyths.entity.WildlifeEntity(type,world,kind), MobCategory.CREATURE).sized(width,height).clientTrackingRange(10).build("slavicmyths:"+id));
    }

    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.bandit.NightingaleEntity>> NIGHTINGALE = ENTITIES.register("nightingale", () -> EntityType.Builder.of(org.slavicmyths.bandit.NightingaleEntity::new, MobCategory.MONSTER).sized(1.1F,2.35F).clientTrackingRange(12).build("slavicmyths:nightingale"));

    public static final DeferredHolder<EntityType<?>,EntityType<org.slavicmyths.swamp.BolotnikEntity>> BOLOTNIK=ENTITIES.register("bolotnik",()->EntityType.Builder.of(org.slavicmyths.swamp.BolotnikEntity::new,MobCategory.MONSTER).sized(1.15F,2.0F).clientTrackingRange(10).build("slavicmyths:bolotnik"));

    @SubscribeEvent
    public static void attributes(EntityAttributeCreationEvent event) {
        event.put(NIGHTINGALE.get(),org.slavicmyths.bandit.NightingaleEntity.attributes().build());
        event.put(BANDIT_FIGHTER.get(),org.slavicmyths.bandit.BanditEntity.attributes(0).build());
        event.put(BANDIT_ARCHER.get(),org.slavicmyths.bandit.BanditEntity.attributes(1).build());
        event.put(BANDIT_HEAVY.get(),org.slavicmyths.bandit.BanditEntity.attributes(2).build());
        event.put(BANDIT_SENIOR.get(),org.slavicmyths.bandit.BanditEntity.attributes(3).build());
        event.put(ATAMAN.get(),org.slavicmyths.bandit.BanditEntity.attributes(4).build());
        event.put(ELDER_VODYANOY.get(),org.slavicmyths.depth.ElderVodyanoy.attributes().build());
        event.put(VODYANOY.get(),org.slavicmyths.water.VodyanoyEntity.attributes().build());
        event.put(RUSALKA.get(),org.slavicmyths.water.RusalkaEntity.attributes().build());
        event.put(PIKE.get(),org.slavicmyths.water.RiverFish.createAttributes().add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH,8).add(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED,.7).build());
        event.put(CARP.get(),org.slavicmyths.water.RiverFish.createAttributes().add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH,6).add(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED,.5).build());
        event.put(CRAYFISH.get(),org.slavicmyths.water.RiverFish.createAttributes().add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH,4).add(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED,.25).build());
        event.put(UPYR.get(),org.slavicmyths.kurgan.KurganCreature.attributes(org.slavicmyths.kurgan.KurganFighter.Kind.UPYR).build());
        event.put(NAV.get(),org.slavicmyths.kurgan.KurganCreature.attributes(org.slavicmyths.kurgan.KurganFighter.Kind.NAV).build());
        event.put(DRUZHINNIK.get(),org.slavicmyths.kurgan.KurganCreature.attributes(org.slavicmyths.kurgan.KurganFighter.Kind.DRUZHINNIK).build());
        event.put(VOEVODA.get(),org.slavicmyths.kurgan.KurganCreature.attributes(org.slavicmyths.kurgan.KurganFighter.Kind.VOEVODA).build());
        event.put(VOLKHV.get(),org.slavicmyths.kurgan.KurganCreature.attributes(org.slavicmyths.kurgan.KurganFighter.Kind.VOLKHV).build());
        event.put(PRINCE.get(),org.slavicmyths.kurgan.KurganCreature.attributes(org.slavicmyths.kurgan.KurganFighter.Kind.PRINCE).build());
        event.put(BABA_YAGA.get(),org.slavicmyths.yaga.BabaYaga.attributes().build());
        event.put(DOMOVOY.get(), DomovoyEntity.attributes().build());
        event.put(LESHY.get(), LeshyEntity.attributes().build());
        event.put(BOLOTNIK.get(),org.slavicmyths.swamp.BolotnikEntity.attributes().build());
        event.put(KIKIMORA.get(), org.slavicmyths.entity.KikimoraEntity.attributes().build());
        event.put(POLUDNITSA.get(), org.slavicmyths.entity.PoludnitsaEntity.attributes().build());
        event.put(POLEVIK.get(), org.slavicmyths.entity.PolevikEntity.attributes().build());
        event.put(BANNIK.get(), org.slavicmyths.entity.BannikEntity.attributes().build());
        event.put(IGOSHA.get(), org.slavicmyths.entity.IgoshaEntity.attributes().build());
        event.put(FIRE_SERPENT.get(),org.slavicmyths.hunt.ElementHuntMob.attributes(true).build());
        event.put(LIKHO_ONE_EYED.get(),org.slavicmyths.hunt.WorldBoss.attributes(org.slavicmyths.hunt.BossKind.LIKHO).build());
        event.put(TUGARIN_ZMEY.get(),org.slavicmyths.hunt.WorldBoss.attributes(org.slavicmyths.hunt.BossKind.TUGARIN).build());
        event.put(PODVEY.get(),org.slavicmyths.hunt.ElementHuntMob.attributes(false).build());
        event.put(SERPENT_PROJECTION.get(),org.slavicmyths.hunt.SerpentProjection.attributes().build());
        event.put(VOLKOLAK.get(),org.slavicmyths.entity.VolkolakEntity.attributes().build());
        event.put(OVINNIK.get(), org.slavicmyths.entity.OvinnikEntity.attributes().build());
        event.put(BROWN_BEAR.get(), org.slavicmyths.entity.WildlifeEntity.attributes(org.slavicmyths.entity.WildlifeEntity.Kind.BEAR).build());
        event.put(BEAR_CUB.get(), org.slavicmyths.entity.WildlifeEntity.attributes(org.slavicmyths.entity.WildlifeEntity.Kind.CUB).build());
        event.put(FOREST_WOLF.get(), org.slavicmyths.entity.WildlifeEntity.attributes(org.slavicmyths.entity.WildlifeEntity.Kind.WOLF).build());
        event.put(BOAR.get(), org.slavicmyths.entity.WildlifeEntity.attributes(org.slavicmyths.entity.WildlifeEntity.Kind.BOAR).build());
        event.put(STAG.get(), org.slavicmyths.entity.WildlifeEntity.attributes(org.slavicmyths.entity.WildlifeEntity.Kind.STAG).build());
        event.put(DOE.get(), org.slavicmyths.entity.WildlifeEntity.attributes(org.slavicmyths.entity.WildlifeEntity.Kind.DOE).build());

    }
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.water.RiverFish>> PIKE=ENTITIES.register("pike",()->EntityType.Builder.<org.slavicmyths.water.RiverFish>of((t,w)->new org.slavicmyths.water.RiverFish(t,w,0),MobCategory.WATER_AMBIENT).sized(.85F,.35F).clientTrackingRange(8).build("slavicmyths:pike"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.water.RiverFish>> CARP=ENTITIES.register("carp",()->EntityType.Builder.<org.slavicmyths.water.RiverFish>of((t,w)->new org.slavicmyths.water.RiverFish(t,w,1),MobCategory.WATER_AMBIENT).sized(.65F,.5F).clientTrackingRange(8).build("slavicmyths:carp"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.water.RiverFish>> CRAYFISH=ENTITIES.register("crayfish",()->EntityType.Builder.<org.slavicmyths.water.RiverFish>of((t,w)->new org.slavicmyths.water.RiverFish(t,w,2),MobCategory.WATER_AMBIENT).sized(.45F,.2F).clientTrackingRange(8).build("slavicmyths:crayfish"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.water.VodyanoyEntity>> VODYANOY=ENTITIES.register("vodyanoy",()->EntityType.Builder.of(org.slavicmyths.water.VodyanoyEntity::new,MobCategory.MONSTER).sized(.9F,1.7F).clientTrackingRange(10).build("slavicmyths:vodyanoy"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.water.RusalkaEntity>> RUSALKA=ENTITIES.register("rusalka",()->EntityType.Builder.of(org.slavicmyths.water.RusalkaEntity::new,MobCategory.MONSTER).sized(.55F,1.85F).clientTrackingRange(10).build("slavicmyths:rusalka"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.depth.ThrownNet>> THROWN_NET=ENTITIES.register("thrown_net",()->EntityType.Builder.<org.slavicmyths.depth.ThrownNet>of(org.slavicmyths.depth.ThrownNet::new,MobCategory.MISC).sized(.3F,.3F).clientTrackingRange(4).updateInterval(5).build("slavicmyths:thrown_net"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.depth.ElderVodyanoy>> ELDER_VODYANOY=ENTITIES.register("elder_vodyanoy",()->EntityType.Builder.of(org.slavicmyths.depth.ElderVodyanoy::new,MobCategory.MONSTER).sized(1.4F,2.35F).clientTrackingRange(10).build("slavicmyths:elder_vodyanoy"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.bandit.BanditEntity>> BANDIT_FIGHTER=ENTITIES.register("bandit_fighter",()->EntityType.Builder.<org.slavicmyths.bandit.BanditEntity>of((t,w)->new org.slavicmyths.bandit.BanditEntity(t,w,0),MobCategory.MONSTER).sized(.6F,1.95F).clientTrackingRange(10).build("slavicmyths:bandit_fighter"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.bandit.BanditEntity>> BANDIT_ARCHER=ENTITIES.register("bandit_archer",()->EntityType.Builder.<org.slavicmyths.bandit.BanditEntity>of((t,w)->new org.slavicmyths.bandit.BanditEntity(t,w,1),MobCategory.MONSTER).sized(.6F,1.95F).clientTrackingRange(10).build("slavicmyths:bandit_archer"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.bandit.BanditEntity>> BANDIT_HEAVY=ENTITIES.register("bandit_heavy",()->EntityType.Builder.<org.slavicmyths.bandit.BanditEntity>of((t,w)->new org.slavicmyths.bandit.BanditEntity(t,w,2),MobCategory.MONSTER).sized(.6F,1.95F).clientTrackingRange(10).build("slavicmyths:bandit_heavy"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.bandit.BanditEntity>> BANDIT_SENIOR=ENTITIES.register("bandit_senior",()->EntityType.Builder.<org.slavicmyths.bandit.BanditEntity>of((t,w)->new org.slavicmyths.bandit.BanditEntity(t,w,3),MobCategory.MONSTER).sized(.6F,1.95F).clientTrackingRange(10).build("slavicmyths:bandit_senior"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.bandit.BanditEntity>> ATAMAN=ENTITIES.register("ataman",()->EntityType.Builder.<org.slavicmyths.bandit.BanditEntity>of((t,w)->new org.slavicmyths.bandit.BanditEntity(t,w,4),MobCategory.MONSTER).sized(.6F,1.95F).clientTrackingRange(10).build("slavicmyths:ataman"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.kurgan.KurganCreature>> UPYR=ENTITIES.register("upyr",()->EntityType.Builder.<org.slavicmyths.kurgan.KurganCreature>of((t,world)->new org.slavicmyths.kurgan.KurganCreature(t,world,org.slavicmyths.kurgan.KurganFighter.Kind.UPYR),MobCategory.MONSTER).sized(0.65F,1.9F).clientTrackingRange(12).build("slavicmyths:upyr"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.kurgan.KurganCreature>> NAV=ENTITIES.register("nav",()->EntityType.Builder.<org.slavicmyths.kurgan.KurganCreature>of((t,world)->new org.slavicmyths.kurgan.KurganCreature(t,world,org.slavicmyths.kurgan.KurganFighter.Kind.NAV),MobCategory.MONSTER).sized(0.6F,2.1F).clientTrackingRange(12).build("slavicmyths:nav"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.kurgan.KurganCreature>> DRUZHINNIK=ENTITIES.register("kurgan_druzhinnik",()->EntityType.Builder.<org.slavicmyths.kurgan.KurganCreature>of((t,world)->new org.slavicmyths.kurgan.KurganCreature(t,world,org.slavicmyths.kurgan.KurganFighter.Kind.DRUZHINNIK),MobCategory.MONSTER).sized(0.8F,2F).clientTrackingRange(12).build("slavicmyths:kurgan_druzhinnik"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.kurgan.KurganCreature>> VOEVODA=ENTITIES.register("kurgan_voevoda",()->EntityType.Builder.<org.slavicmyths.kurgan.KurganCreature>of((t,world)->new org.slavicmyths.kurgan.KurganCreature(t,world,org.slavicmyths.kurgan.KurganFighter.Kind.VOEVODA),MobCategory.MONSTER).sized(1F,2.3F).clientTrackingRange(12).build("slavicmyths:kurgan_voevoda"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.kurgan.KurganCreature>> VOLKHV=ENTITIES.register("buried_volkhv",()->EntityType.Builder.<org.slavicmyths.kurgan.KurganCreature>of((t,world)->new org.slavicmyths.kurgan.KurganCreature(t,world,org.slavicmyths.kurgan.KurganFighter.Kind.VOLKHV),MobCategory.MONSTER).sized(0.7F,2.1F).clientTrackingRange(12).build("slavicmyths:buried_volkhv"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.kurgan.KurganCreature>> PRINCE=ENTITIES.register("unresting_prince",()->EntityType.Builder.<org.slavicmyths.kurgan.KurganCreature>of((t,world)->new org.slavicmyths.kurgan.KurganCreature(t,world,org.slavicmyths.kurgan.KurganFighter.Kind.PRINCE),MobCategory.MONSTER).sized(1.1F,2.6F).clientTrackingRange(12).build("slavicmyths:unresting_prince"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.kurgan.KurganBolt>> KURGAN_BOLT=ENTITIES.register("kurgan_bolt",()->EntityType.Builder.of(org.slavicmyths.kurgan.KurganBolt::new,MobCategory.MISC).sized(.25F,.25F).clientTrackingRange(8).updateInterval(2).build("slavicmyths:kurgan_bolt"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.hunt.ElementHuntMob>> FIRE_SERPENT=ENTITIES.register("fire_serpent",()->EntityType.Builder.of(org.slavicmyths.hunt.ElementHuntMob::new,MobCategory.MONSTER).sized(1.25F,1.55F).clientTrackingRange(12).fireImmune().build("slavicmyths:fire_serpent"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.hunt.ElementHuntMob>> PODVEY=ENTITIES.register("podvey",()->EntityType.Builder.of(org.slavicmyths.hunt.ElementHuntMob::new,MobCategory.MONSTER).sized(1.1F,2.55F).clientTrackingRange(12).build("slavicmyths:podvey"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.hunt.SerpentProjection>> SERPENT_PROJECTION=ENTITIES.register("serpent_projection",()->EntityType.Builder.of(org.slavicmyths.hunt.SerpentProjection::new,MobCategory.MISC).sized(1.25F,1.55F).clientTrackingRange(12).updateInterval(2).fireImmune().build("slavicmyths:serpent_projection"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.entity.VolkolakEntity>> VOLKOLAK=ENTITIES.register("volkolak",()->EntityType.Builder.of(org.slavicmyths.entity.VolkolakEntity::new,MobCategory.MONSTER).sized(1.05F,2.25F).clientTrackingRange(12).build("slavicmyths:volkolak"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.hunt.EmberClump>> EMBER_CLUMP=ENTITIES.register("ember_clump",()->EntityType.Builder.of(org.slavicmyths.hunt.EmberClump::new,MobCategory.MISC).sized(.3F,.3F).clientTrackingRange(8).updateInterval(2).build("slavicmyths:ember_clump"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.hunt.WorldBoss>> LIKHO_ONE_EYED=ENTITIES.register("likho_one_eyed",()->EntityType.Builder.of(org.slavicmyths.hunt.WorldBoss::new,MobCategory.MONSTER).sized(.95F,3.25F).clientTrackingRange(12).build("slavicmyths:likho_one_eyed"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.hunt.WorldBoss>> TUGARIN_ZMEY=ENTITIES.register("tugarin_zmey",()->EntityType.Builder.of(org.slavicmyths.hunt.WorldBoss::new,MobCategory.MONSTER).sized(1.5F,2.9F).clientTrackingRange(12).build("slavicmyths:tugarin_zmey"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.hunt.BossStone>> TUGARIN_STONE=ENTITIES.register("tugarin_stone",()->EntityType.Builder.of(org.slavicmyths.hunt.BossStone::new,MobCategory.MISC).sized(.4F,.4F).clientTrackingRange(8).updateInterval(2).build("slavicmyths:tugarin_stone"));
    private ModEntities() { }
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.flight.FlyingVessel>> FLYING_BROOM=ENTITIES.register("flying_broom",()->EntityType.Builder.<org.slavicmyths.flight.FlyingVessel>of((t,w)->new org.slavicmyths.flight.FlyingVessel(t,w,false),MobCategory.MISC).sized(1F,.45F).clientTrackingRange(10).updateInterval(3).build("slavicmyths:flying_broom"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.flight.FlyingVessel>> FLYING_MORTAR=ENTITIES.register("flying_mortar",()->EntityType.Builder.<org.slavicmyths.flight.FlyingVessel>of((t,w)->new org.slavicmyths.flight.FlyingVessel(t,w,true),MobCategory.MISC).sized(.95F,1.2F).clientTrackingRange(10).updateInterval(3).build("slavicmyths:flying_mortar"));
 public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.yaga.BabaYaga>> BABA_YAGA=ENTITIES.register("baba_yaga",()->EntityType.Builder.of(org.slavicmyths.yaga.BabaYaga::new,MobCategory.CREATURE).sized(.6F,1.75F).clientTrackingRange(8).build("slavicmyths:baba_yaga"));
    public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.combat.WeaponProjectile>> WEAPON_PROJECTILE=ENTITIES.register("weapon_projectile",()->EntityType.Builder.<org.slavicmyths.combat.WeaponProjectile>of(org.slavicmyths.combat.WeaponProjectile::new,MobCategory.MISC).sized(.25F,.25F).clientTrackingRange(8).updateInterval(1).build("slavicmyths:weapon_projectile"));
}
