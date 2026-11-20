package org.slavicmyths.marine;
import java.util.List;
import java.util.UUID;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slavicmyths.rpg.Runes;

@GameTestHolder("slavicmyths_marine_armor")
@PrefixGameTestTemplate(false)
public final class MarineRunesGameTests {
    @GameTest(template="empty", timeoutTicks=50)
    public static void registrationAndRuneInstallation(GameTestHelper t) {
        for(String id:List.of("tide","abyss","storm")) {
            ItemStack rune = new ItemStack(BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath("slavicmyths", "rune_"+id)));
            t.assertTrue(Runes.rune(rune).equals(id), "Rune item missing: "+id);
        }
        ItemStack helmet = new ItemStack(MarineArmor.HELMET.get());
        t.assertTrue(Runes.capacity(helmet)==2, "Marine armor socket count changed");
        t.assertTrue(Runes.compatible(helmet,"tide") && Runes.compatible(helmet,"abyss"),
            "Expected armor rune compatibility");
        t.assertTrue(!Runes.compatible(helmet,"storm"), "Storm illegally fits armor");
        t.assertTrue(Runes.installationReason(1,2,1,List.of(),"abyss",true).isEmpty(),
            "Runic anvil rejected the rune");
        Runes.write(helmet,1,List.of("abyss"));
        t.assertTrue(Runes.has(helmet,"abyss"), "Socket data was not saved");
        ItemStack copy=helmet.copy();
        Runes.write(helmet,1,List.of());
        t.assertTrue(!Runes.has(helmet,"abyss") && Runes.has(copy,"abyss"),
            "Rune removal corrupted the copied stack");
        // Test the actual runic anvil preview path, rather than only writing data directly.
        var inventory = new net.minecraft.world.SimpleContainer(2);
        ItemStack socketed = new ItemStack(MarineArmor.CHESTPLATE.get());
        Runes.write(socketed, 1, List.of());
        inventory.setItem(0, socketed);
        inventory.setItem(1, new ItemStack(MarineRunes.ABYSS.get()));
        ItemStack installed = Runes.preview(inventory, 1);
        t.assertTrue(!installed.isEmpty() && Runes.has(installed, "abyss"),
            "Runic anvil install preview lost abyss rune");
        var saved = (net.minecraft.nbt.CompoundTag) installed.save(t.getLevel().registryAccess());
        var loaded = ItemStack.parseOptional(t.getLevel().registryAccess(), saved);
        t.assertTrue(Runes.has(loaded, "abyss"), "ItemStack NBT roundtrip lost rune data");
        inventory.setItem(0, loaded);
        ItemStack removed = Runes.preview(inventory, 2);
        t.assertTrue(!removed.isEmpty() && !Runes.has(removed, "abyss"),
            "Runic anvil remove preview retained abyss rune");
        t.succeed();
    }
    @GameTest(template="empty", timeoutTicks=60)
    public static void buffsRequireWearingAndRespectOtherPotions(GameTestHelper t) {
        var p = FakePlayerFactory.get(t.getLevel(),new GameProfile(UUID.randomUUID(),"OceanRune524"));
        ItemStack helmet = new ItemStack(MarineArmor.HELMET.get());
        Runes.write(helmet,2,List.of("tide","abyss"));
        p.setItemSlot(EquipmentSlot.HEAD,helmet);
        p.setAirSupply(20);
        MarineRunes.apply(p,true,true);
        t.assertTrue(p.getAirSupply()==p.getMaxAirSupply(),"Abyss did not provide air");
        t.assertTrue(p.hasEffect(MobEffects.NIGHT_VISION),"Abyss did not grant night vision");
        var speed=p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        var key=ResourceLocation.fromNamespaceAndPath("slavicmyths","rune_tide_walk");
        t.assertTrue(speed!=null && speed.getModifier(key)!=null,"Tide was not applied");
        p.setItemSlot(EquipmentSlot.HEAD,ItemStack.EMPTY);
        MarineRunes.apply(p,false,false);
        t.assertTrue(speed.getModifier(key)==null,"Tide persisted after unequipping");
        t.assertTrue(!p.hasEffect(MobEffects.NIGHT_VISION),"Abyss vision persisted after unequip");
        p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,800,2));
        p.setItemSlot(EquipmentSlot.HEAD,helmet);
        MarineRunes.apply(p,true,true);
        p.setItemSlot(EquipmentSlot.HEAD,ItemStack.EMPTY);
        MarineRunes.apply(p,false,false);
        t.assertTrue(p.getEffect(MobEffects.NIGHT_VISION)!=null
            && p.getEffect(MobEffects.NIGHT_VISION).getAmplifier()==2,
            "Rune removed an unrelated stronger potion");
        p.discard();
        t.succeed();
    }
    @GameTest(template="empty", timeoutTicks=50)
    public static void stormRequiresTridentAndEnforcesCooldown(GameTestHelper t) {
        var p=FakePlayerFactory.get(t.getLevel(),new GameProfile(UUID.randomUUID(),"OceanStorm524"));
        ItemStack weapon=new ItemStack(Items.TRIDENT);
        Runes.write(weapon,1,List.of("storm"));
        p.setItemInHand(InteractionHand.MAIN_HAND,weapon);
        p.moveTo(t.absolutePos(new BlockPos(2,2,1)),0F,0F);
        t.assertTrue(Runes.compatible(weapon,"storm"),"Storm is incompatible with tridents");
        var target=EntityType.ZOMBIE.create(t.getLevel());
        t.assertTrue(target!=null,"Test target not created");
        target.moveTo(t.absolutePos(new BlockPos(2,2,2)),0F,0F);
        t.getLevel().addFreshEntity(target);
        t.assertTrue(MarineRunes.discharge(p,target),"Storm attack did not discharge");
        t.assertTrue(!MarineRunes.discharge(p,target),"Storm cooldown was bypassed");
        p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        t.assertTrue(!MarineRunes.discharge(p,target),"Storm worked without trident");
        target.discard();
        p.discard();
        t.succeed();
    }
    @GameTest(template="empty", timeoutTicks=50)
    public static void elixirAndTinctureUseOnServer(GameTestHelper t) {
        var p=FakePlayerFactory.get(t.getLevel(),new GameProfile(UUID.randomUUID(),"OceanDrink524"));
        ItemStack elixir=new ItemStack(MarineConsumables.ABYSS_ELIXIR.get(),2);
        p.setItemInHand(InteractionHand.MAIN_HAND,elixir);
        elixir.use(t.getLevel(),p,InteractionHand.MAIN_HAND);
        t.assertTrue(p.getMainHandItem().getCount()==1,"Elixir not consumed");
        elixir.use(t.getLevel(),p,InteractionHand.MAIN_HAND);
        t.assertTrue(p.getMainHandItem().getCount()==1,"Cooldown allowed a second drink");
        t.assertTrue(p.hasEffect(MobEffects.WATER_BREATHING)&&p.hasEffect(MobEffects.NIGHT_VISION),
            "Elixir does not grant both effects");
        t.assertTrue(p.getInventory().countItem(Items.GLASS_BOTTLE)==1,"Bottle not returned");
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(MarineConsumables.STORM_TINCTURE.get()));
        p.getMainHandItem().use(t.getLevel(),p,InteractionHand.MAIN_HAND);
        t.assertTrue(p.getPersistentData().getLong("SlavicMarineStormProtectionUntil")
            >t.getLevel().getGameTime(),"Tincture protection missing");
        p.discard();
        t.succeed();
    }
}
