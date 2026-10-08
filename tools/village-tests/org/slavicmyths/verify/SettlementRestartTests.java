package org.slavicmyths.verify;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.village.*;

/** Actual disk/chunk/server restart gate in the disposable GameTest world, never player saves. */
@GameTestHolder("slavicmyths_village") @PrefixGameTestTemplate(false)
public final class SettlementRestartTests {
    @GameTest(template="empty",timeoutTicks=1400)
    public static void realChunkAndRestart(GameTestHelper t)throws Exception{
        String phase=System.getProperty("slavicmyths.settlementRestartPhase","none");if(phase.equals("none")){t.succeed();return;}
        var level=t.getLevel();BlockPos chestPos=new BlockPos(1024,-58,1024),vatPos=chestPos.east(3);Path marker=Path.of("settlement-restart-1.2.2.txt");level.setChunkForced(64,64,true);level.getChunk(64,64);
        if(phase.equals("save")){
            level.setBlock(chestPos.below(),Blocks.STONE.defaultBlockState(),3);level.setBlock(chestPos,SettlementStorage.BLOCK.get().defaultBlockState(),3);var chest=(SettlementChest)level.getBlockEntity(chestPos);chest.setItem(63,new ItemStack(Items.WHEAT,12));
            level.setBlock(vatPos.below(),Blocks.STONE.defaultBlockState(),3);level.setBlock(vatPos,org.slavicmyths.brewing.Brewing.BLOCKS.get("fermentation_vat").get().defaultBlockState(),3);var vat=(org.slavicmyths.brewing.BrewTile)level.getBlockEntity(vatPos);
            vat.setItem(0,new ItemStack(org.slavicmyths.kitchen.KitchenII.item("barley_grain"),4));vat.setItem(1,new ItemStack(Items.WATER_BUCKET));org.slavicmyths.brewing.BrewTile.tick(vat);for(int i=0;i<100;i++)org.slavicmyths.brewing.BrewTile.tick(vat);t.assertTrue(vat.total==1200&&vat.progress==100&&!vat.pending.isEmpty(),"No active shared process to save");
            Villager v=EntityType.VILLAGER.create(level);v.setVillagerData(new VillagerData(VillagerType.PLAINS,VillageRoles.ROLES.get("miller").get(),1));v.moveTo(chestPos.getX()+1.5,chestPos.getY(),chestPos.getZ()+.5,0,0);level.setBlock(chestPos.east().below(),Blocks.STONE.defaultBlockState(),3);v.setNoAi(true);v.setPersistenceRequired();v.getInventory().addItem(new ItemStack(org.slavicmyths.kitchen.KitchenII.item("wheat_flour"),2));VillageWork.state(v).storage=chestPos;level.addFreshEntity(v);Files.writeString(marker,v.getUUID().toString());
            level.getServer().saveAllChunks(false,true,true);level.setChunkForced(64,64,false);
            t.succeedWhen(()->{t.assertTrue(!level.hasChunk(64,64),"Target chunk not actually unloaded");System.out.println("SETTLEMENT_RESTART_SAVE_PASS actualChunkUnload=true physicalGrain=12 personalFlour=2 pendingVat=true");});
        }else if(phase.equals("load")){
            UUID uuid=UUID.fromString(Files.readString(marker).trim());
            t.runAfterDelay(100,()->{
                t.assertTrue(level.getBlockEntity(chestPos) instanceof SettlementChest,"Actual region data lost");var chest=(SettlementChest)level.getBlockEntity(chestPos);t.assertTrue(chest.getItem(63).is(Items.WHEAT)&&chest.getItem(63).getCount()==12,"Restart chest duplication/loss");
                var loaded=level.getEntity(uuid);t.assertTrue(loaded instanceof Villager,"Saved villager not reloaded");var v=(Villager)loaded;t.assertTrue(v.getInventory().countItem(org.slavicmyths.kitchen.KitchenII.item("wheat_flour"))==2&&VillageWork.state(v).storage==null,"Restart personal inventory/cached reference");
                var vat=(org.slavicmyths.brewing.BrewTile)level.getBlockEntity(vatPos);t.assertTrue(vat.total==1200&&vat.progress>=100&&vat.progress<1200&&vat.pending.getCount()==4,"Saved pending recipe duplicated/lost or fast-forwarded");
                while(vat.total>0)org.slavicmyths.brewing.BrewTile.tick(vat);t.assertTrue(vat.getItem(6).getCount()==4&&vat.getItem(4).is(Items.BUCKET),"Resumed real process output/remainder");System.out.println("SETTLEMENT_RESTART_LOAD_PASS actualNewServer=true regionData=true savedVillager=true cacheReset=true resumedVatMalt=4");
                v.discard();level.removeBlock(chestPos,false);level.removeBlock(vatPos,false);level.setChunkForced(64,64,false);t.succeed();
            });
        }else throw new IllegalArgumentException("Unknown restart phase "+phase);
    }
}
