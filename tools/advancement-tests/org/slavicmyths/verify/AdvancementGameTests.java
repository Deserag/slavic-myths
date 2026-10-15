package org.slavicmyths.verify;

import java.util.*;
import java.nio.file.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.progression.Knowledge;
import org.slavicmyths.tales.*;
import org.slavicmyths.kitchen.*;
import org.slavicmyths.registry.ModItems;

@GameTestHolder("slavicmyths_advancements") @PrefixGameTestTemplate(false)
public final class AdvancementGameTests {
    private static final class Connection extends net.minecraft.network.Connection {
        private final io.netty.channel.embedded.EmbeddedChannel channel=new io.netty.channel.embedded.EmbeddedChannel();
        Connection(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);}
        @Override public io.netty.channel.Channel channel(){return channel;}
    }
    private static final class Listener extends net.minecraft.server.network.ServerGamePacketListenerImpl {
        Listener(ServerPlayer p){super(p.getServer(),new Connection(),p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));}
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet){}
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener){}
    }
    private static ServerPlayer player(GameTestHelper h,UUID uuid){
        var p=new ServerPlayer(h.getLevel().getServer(),h.getLevel(),new com.mojang.authlib.GameProfile(uuid,"AdvancementQA"),ClientInformation.createDefault());
        p.connection=new Listener(p);p.setGameMode(GameType.SURVIVAL);return p;
    }
    private static ServerPlayer player(GameTestHelper h){return player(h,UUID.randomUUID());}
    private static void open(ServerPlayer p){p.getPersistentData().remove("TaleOpenReady");TaleService.open(p);}
    @GameTest(template="empty") public static void loreThreshold(GameTestHelper h){
        var p=player(h);Knowledge.award(p,"depth_gift");Knowledge.award(p,"meet_domovoy");
        p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(ModItems.LORE_BOOK.get()));
        open(p);h.assertTrue(!Knowledge.knows(p,"lore_keeper"),"Duplicate depth_gift counted twice");
        Knowledge.award(p,"meet_leshy");p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,ItemStack.EMPTY);open(p);
        h.assertTrue(!Knowledge.knows(p,"lore_keeper"),"No held book awarded");
        p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,new ItemStack(ModItems.LORE_BOOK.get()));open(p);
        h.assertTrue(Knowledge.knows(p,"lore_keeper"),"Three distinct legacy discoveries did not award");
        h.assertTrue(Knowledge.knows(p,"first_book_entry"),"Book integration lost");h.succeed();
    }
    @GameTest(template="empty") public static void reconcileReadOnly(GameTestHelper h){
        var p=player(h);Knowledge.award(p,"ancient_find");Knowledge.award(p,"leshy_heart");
        TaleService.reconcile(p);TaleService.reconcile(p);
        h.assertTrue(!Knowledge.knows(p,"shrine")&&!Knowledge.knows(p,"meet_leshy")&&!Knowledge.knows(p,"lore_keeper"),"Reconcile synthetic awards");
        for(var entry:TaleCatalog.advancement("slavicmyths:ancient_find"))h.assertTrue(TaleData.get(h.getLevel()).stage(p.getUUID(),entry.id())>=3,"Legacy book reconstruction failed");
        h.succeed();
    }
    @GameTest(template="empty") public static void kitchenServing(GameTestHelper h){
        var p=player(h);var pos=h.absolutePos(new net.minecraft.core.BlockPos(1,2,1));
        h.getLevel().setBlockAndUpdate(pos,org.slavicmyths.registry.ModBlocks.KITCHEN_TABLE.get().defaultBlockState());
        var tile=(KitchenTile)h.getLevel().getBlockEntity(pos);
        h.assertTrue(!tile.serve(p,new ItemStack(Items.BOWL))&&!Knowledge.knows(p,"kitchen_meal"),"Empty pot awarded");
        tile.dish=new ItemStack(KitchenII.item("pea_soup"));tile.servings=2;tile.maxServings=2;
        h.assertTrue(!tile.serve(p,new ItemStack(Items.STICK))&&!Knowledge.knows(p,"kitchen_meal"),"Wrong vessel awarded");
        var bowl=new ItemStack(Items.BOWL);h.assertTrue(tile.serve(p,bowl),"Serving failed");
        h.assertTrue(Knowledge.knows(p,"kitchen_meal")&&bowl.isEmpty()&&tile.servings==1,"Serving transaction/award failed");h.succeed();
    }
    @GameTest(template="empty") public static void kitchenOutput(GameTestHelper h){
        var p=player(h);var pos=h.absolutePos(new net.minecraft.core.BlockPos(2,2,2));
        h.getLevel().setBlockAndUpdate(pos,org.slavicmyths.registry.ModBlocks.KITCHEN_TABLE.get().defaultBlockState());
        p.moveTo(pos.getX(),pos.getY(),pos.getZ());var tile=(KitchenTile)h.getLevel().getBlockEntity(pos);
        var menu=new KitchenMenu(1,p.getInventory(),pos,tile.inventory,tile);
        menu.quickMoveStack(p,6);h.assertTrue(!Knowledge.knows(p,"kitchen_meal"),"Empty output awarded");
        tile.inventory.setItem(7,new ItemStack(Items.BOWL));menu.quickMoveStack(p,7);
        h.assertTrue(!Knowledge.knows(p,"kitchen_meal"),"Remainder awarded");
        tile.inventory.setItem(6,new ItemStack(KitchenII.item("bliny")));menu.quickMoveStack(p,6);
        h.assertTrue(Knowledge.knows(p,"kitchen_meal"),"Shift-click result failed");
        var normal=player(h);tile.inventory.setItem(6,new ItemStack(KitchenII.item("bliny")));
        var slot=menu.getSlot(6);var taken=slot.remove(1);slot.onTake(normal,taken);
        h.assertTrue(Knowledge.knows(normal,"kitchen_meal"),"Normal output failed");h.succeed();
    }
    @GameTest(template="empty") public static void guardianClaims(GameTestHelper h){
        var ledger=org.slavicmyths.guardians.GuardianLedger.get(h.getLevel());
        for(boolean spring:new boolean[]{true,false}){
            var p=player(h);var instance=UUID.randomUUID();var row=ledger.row(instance);row.putBoolean("Spring",spring);
            String id=spring?"claimed_water_fragment":"claimed_fire_fragment";
            row.putString("Stage","READY");
            h.assertTrue(!ledger.claim(p,instance)&&!Knowledge.knows(p,id),"Uncleared guardian awarded");
            row.putString("Stage","CLEARED");row.putBoolean("Standalone",true);
            h.assertTrue(!ledger.claim(p,instance)&&!Knowledge.knows(p,id),"Standalone guardian awarded");
            row.putBoolean("Standalone",false);
            p.setGameMode(GameType.SPECTATOR);
            h.assertTrue(!ledger.claim(p,instance)&&!Knowledge.knows(p,id),"Spectator claim awarded");
            p.setGameMode(GameType.SURVIVAL);
            for(int i=0;i<36;i++)p.getInventory().setItem(i,new ItemStack(Items.STONE,64));
            h.assertTrue(!ledger.claim(p,instance)&&!Knowledge.knows(p,id),"Full inventory claim awarded");
            p.getInventory().clearContent();
            h.assertTrue(ledger.claim(p,instance)&&Knowledge.knows(p,id),"Valid claim did not award "+id);
            h.assertTrue(ledger.claimed(p.getUUID(),spring),"Claim receipt missing");
            h.assertTrue(!ledger.claim(p,instance),"Duplicate fragment claim allowed");
        }h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=200) public static void inventoryCriteria(GameTestHelper h)throws Exception{
        var root=Path.of(System.getProperty("slavicmyths.advancementRoot"));int checked=0;
        try(var paths=Files.list(root.resolve("src/main/resources/data/slavicmyths/advancement"))){
            for(var path:paths.filter(p->p.toString().endsWith(".json")).toList()){
                var json=com.google.gson.JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                var id=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths",path.getFileName().toString().replace(".json",""));
                var advancement=h.getLevel().getServer().getAdvancements().get(id);
                for(var criterion:json.getAsJsonObject("criteria").entrySet()){
                    var c=criterion.getValue().getAsJsonObject();if(!c.get("trigger").getAsString().equals("minecraft:inventory_changed"))continue;
                    var p=player(h);var wrong=new ItemStack(Items.STICK);p.getInventory().setItem(0,wrong);
                    net.minecraft.advancements.CriteriaTriggers.INVENTORY_CHANGED.trigger(p,p.getInventory(),wrong);
                    h.assertTrue(!p.getAdvancements().getOrStartProgress(advancement).getCriterion(criterion.getKey()).isDone(),"Wrong item passed "+id+"/"+criterion.getKey());
                    p.getInventory().clearContent();int slot=0;ItemStack last=ItemStack.EMPTY;
                    for(var raw:c.getAsJsonObject("conditions").getAsJsonArray("items")){
                        var items=raw.getAsJsonObject().get("items");String name=items.isJsonArray()?items.getAsJsonArray().get(0).getAsString():items.getAsString();
                        Item item;
                        if(name.startsWith("#")){
                            var tag=net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,net.minecraft.resources.ResourceLocation.parse(name.substring(1)));
                            item=net.minecraft.core.registries.BuiltInRegistries.ITEM.getTag(tag).orElseThrow().iterator().next().value();
                        }else item=net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse(name));
                        h.assertTrue(item!=Items.AIR,"Missing inventory item "+name);last=new ItemStack(item);p.getInventory().setItem(slot++,last);
                    }
                    net.minecraft.advancements.CriteriaTriggers.INVENTORY_CHANGED.trigger(p,p.getInventory(),last);
                    h.assertTrue(p.getAdvancements().getOrStartProgress(advancement).getCriterion(criterion.getKey()).isDone(),"Expected inventory criterion failed "+id+"/"+criterion.getKey());checked++;
                }
            }
        }
        System.out.println("ADVANCEMENT_NATIVE_INVENTORY_CRITERIA_PASS positive_negative="+checked);h.succeed();
    }
    @GameTest(template="empty") public static void loadedData(GameTestHelper h)throws Exception{
        var root=Path.of(System.getProperty("slavicmyths.advancementRoot"));int count=0;
        try(var paths=Files.walk(root.resolve("src/main/resources/data/slavicmyths/advancement"))){
            for(var path:paths.filter(p->p.toString().endsWith(".json")).toList()){
                String name=root.resolve("src/main/resources/data/slavicmyths/advancement").relativize(path).toString().replace('\\','/').replaceFirst("\\.json$","");
                var a=h.getLevel().getServer().getAdvancements().get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths",name));
                h.assertTrue(a!=null,"Loader rejected "+name);count++;
            }
        }
        h.assertTrue(TaleCatalog.entries().size()==234,"Tale catalog not loaded");
        System.out.println("ADVANCEMENT_LOADED_DATA_PASS count="+count);h.succeed();
    }
    @GameTest(template="empty") public static void nativeProgressRestart(GameTestHelper h){
        var p=player(h,UUID.fromString("97b01636-1022-4000-a000-000000000022"));
        if(System.getProperty("slavicmyths.advancementPhase").equals("write")){
            Knowledge.award(p,"lore_keeper");Knowledge.award(p,"kitchen_meal");Knowledge.award(p,"meet_leshy");
            p.getAdvancements().save();System.out.println("ADVANCEMENT_NATIVE_SAVE_PASS");
        }else{
            h.assertTrue(Knowledge.knows(p,"lore_keeper")&&Knowledge.knows(p,"kitchen_meal")&&Knowledge.knows(p,"meet_leshy"),"Earned progress lost across process restart");
            h.assertTrue(!Knowledge.knows(p,"earth_heart"),"Unearned progress became awarded");
            TaleService.reconcile(p);TaleService.reconcile(p);
            h.assertTrue(Knowledge.knows(p,"meet_leshy")&&!Knowledge.knows(p,"earth_heart"),"Reconcile changed persisted progress");
            System.out.println("ADVANCEMENT_NATIVE_NEW_PROCESS_LOAD_PASS");
        }h.succeed();
    }
}
