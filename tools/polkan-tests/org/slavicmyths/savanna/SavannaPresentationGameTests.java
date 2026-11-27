package org.slavicmyths.savanna;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.registry.*;
import org.slavicmyths.combat.CreatureBossBar;
@GameTestHolder("slavicmyths_accessories278")
@PrefixGameTestTemplate(false)
public final class SavannaPresentationGameTests {
    @GameTest(template="empty")
    public static void mobEggOrdering(GameTestHelper h){
        var items=ModItemGroup.items(8);int previous=-1;
        for(var item:items){String path=BuiltInRegistries.ITEM.getKey(item).getPath();int rank=MobEggOrder.rank(path);h.assertTrue(rank>=previous,"Unsorted mob egg: "+path);previous=rank;}
        h.assertTrue(items.indexOf(ModItems.JACKAL_SPAWN_EGG.get())<items.indexOf(ModItems.STEPPE_JACKAL_PACK_SPAWN_EGG.get()),"Ordinary jackal must precede pack");
        h.assertTrue(items.indexOf(ModItems.BLACK_HYENA_PACK_SPAWN_EGG.get())<items.indexOf(ModItems.POLKAN_SPAWN_EGG.get()),"VI must precede VII");
        h.assertTrue(items.indexOf(ModItems.POLKAN_SPAWN_EGG.get())<items.indexOf(ModItems.DIV_SPAWN_EGG.get()),"VII must precede VIII");
        h.succeed();
    }
    @GameTest(template="empty")
    public static void numericBossHealthLabels(GameTestHelper h){
        h.assertTrue(CreatureBossBar.healthLabel(Component.literal("Div"),1250,1600).getString().endsWith("1250 / 1600 HP"),"Incorrect Div display");
        h.assertTrue(CreatureBossBar.healthLabel(Component.literal("Pack"),720,900).getString().endsWith("720 / 900 HP"),"Incorrect pack display");
        h.assertTrue(CreatureBossBar.healthLabel(Component.literal("Hyena"),0,400).getString().endsWith("0 / 400 HP"),"Incorrect leader display");
        h.succeed();
    }
}
