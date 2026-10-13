package org.slavicmyths.husbandry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;

/** Registrations only; clients are isolated in HusbandryRenderer. */
public final class Husbandry {
    public static final DeferredHolder<EntityType<?>, EntityType<YardAnimal>> GOOSE=animal("goose",0,.70F,1.15F);
    public static final DeferredHolder<EntityType<?>, EntityType<YardAnimal>> DUCK=animal("duck",1,.65F,.70F);
    public static final DeferredHolder<EntityType<?>, EntityType<YardAnimal>> GOAT=animal("domestic_goat",2,.90F,1.25F);
    public static final DeferredHolder<Block, YardStorageBlock> FEEDER=ModBlocks.BLOCKS.register("feeder",YardStorageBlock.Feeder::new);
    public static final DeferredHolder<Block, YardStorageBlock> NEST=ModBlocks.BLOCKS.register("straw_nest",YardStorageBlock.Nest::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<YardStorage>> FEEDER_TILE=ModTiles.TILES.register("feeder",()->BlockEntityType.Builder.of(YardStorage::new,FEEDER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<YardStorage>> NEST_TILE=ModTiles.TILES.register("straw_nest",()->BlockEntityType.Builder.of(YardStorage::new,NEST.get()).build(null));
    public static final DeferredHolder<Item,Item> GOOSE_EGG=item("goose_egg",()->new Item(new Item.Properties().stacksTo(16)));
    public static final DeferredHolder<Item,Item> DUCK_EGG=item("duck_egg",()->new Item(new Item.Properties().stacksTo(16)));
    public static final DeferredHolder<Item,Item> MILK=item("goat_milk_bucket",()->new MilkBucketItem(new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));
    public static final DeferredHolder<Item,Item> RAW_GOOSE=meat("raw_goose",2,.30F), COOKED_GOOSE=meat("cooked_goose",6,.70F);
    public static final DeferredHolder<Item,Item> RAW_DUCK=meat("raw_duck",2,.30F), COOKED_DUCK=meat("cooked_duck",6,.65F);
    public static final DeferredHolder<Item,Item> RAW_GOAT=meat("raw_goat",3,.30F), COOKED_GOAT=meat("cooked_goat",7,.75F);
    public static final DeferredHolder<SoundEvent,SoundEvent> GOOSE_AMBIENT=sound("goose","ambient"), GOOSE_HISS=sound("goose","hiss"), GOOSE_HURT=sound("goose","hurt"), GOOSE_DEATH=sound("goose","death");
    public static final DeferredHolder<SoundEvent,SoundEvent> DUCK_AMBIENT=sound("duck","ambient"), DUCK_HURT=sound("duck","hurt"), DUCK_DEATH=sound("duck","death");
    public static final DeferredHolder<SoundEvent,SoundEvent> GOAT_AMBIENT=sound("domestic_goat","ambient"), GOAT_HURT=sound("domestic_goat","hurt"), GOAT_DEATH=sound("domestic_goat","death");
    public static TagKey<Item> feed(String name){return TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath("slavicmyths","animal_feed/"+name));}
    private static DeferredHolder<EntityType<?>,EntityType<YardAnimal>> animal(String id,int kind,float width,float height){return ModEntities.ENTITIES.register(id,()->EntityType.Builder.<YardAnimal>of((t,l)->new YardAnimal(t,l,kind),MobCategory.CREATURE).sized(width,height).clientTrackingRange(10).build("slavicmyths:"+id));}
    private static DeferredHolder<Item,Item> item(String id,java.util.function.Supplier<Item> factory){return ModItems.ITEMS.register(id,factory);}
    private static DeferredHolder<Item,Item> meat(String id,int food,float saturation){return item(id,()->new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(food).saturationModifier(saturation).build())));}
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String animal,String action){String id="entity."+animal+"."+action;return ModSounds.SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("slavicmyths",id)));}
    public static void init(IEventBus bus){
        item("feeder",()->new BlockItem(FEEDER.get(),new Item.Properties()));
        item("straw_nest",()->new BlockItem(NEST.get(),new Item.Properties()));
        item("goose_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(GOOSE,0xd8d7cd,0xe99031,new Item.Properties()));
        item("duck_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(DUCK,0x735038,0xe58b2d,new Item.Properties()));
        item("domestic_goat_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(GOAT,0xe9ddc5,0x8c8070,new Item.Properties()));
        bus.addListener(Husbandry::attributes);bus.addListener(Husbandry::placements);
    }
    private static void attributes(net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent e){
        var types=java.util.List.of(GOOSE,DUCK,GOAT);double[] hp={12,10,18},speed={.22,.20,.23};
        for(int i=0;i<3;i++)e.put(types.get(i).get(),Animal.createMobAttributes().add(Attributes.MAX_HEALTH,hp[i]).add(Attributes.MOVEMENT_SPEED,speed[i]).add(Attributes.FOLLOW_RANGE,16).add(Attributes.ATTACK_DAMAGE,i==0?1:0).add(Attributes.ATTACK_KNOCKBACK,0).build());
    }
    private static void placements(net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent e){
        for(var type:java.util.List.of(GOOSE,DUCK,GOAT))e.register(type.get(),SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (t,l,r,p,random)->l.getLevel().dimension()==net.minecraft.world.level.Level.OVERWORLD && l.getBlockState(p.below()).is(net.minecraft.tags.BlockTags.DIRT) && l.getMaxLocalRawBrightness(p)>8 && Mob.checkMobSpawnRules(t,l,r,p,random),net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
    private Husbandry(){}
}
