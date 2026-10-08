package org.slavicmyths.verify;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.military.*;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.village.VillageRoles;
@GameTestHolder("slavicmyths_village") @PrefixGameTestTemplate(false)
public final class MilitaryRestartTests {
 @GameTest(template="empty",batch="military_restart",timeoutTicks=1400)
 public static void realPlayerAndGuardRestart(GameTestHelper t)throws Exception{String phase=System.getProperty("slavicmyths.settlementRestartPhase","none");if(phase.equals("none")){t.succeed();return;}var l=t.getLevel();var server=l.getServer();BlockPos anchor=new BlockPos(1280,-58,1280);Path receipt=Path.of("military-restart-1.2.3.txt");l.setChunkForced(80,80,true);l.getChunk(80,80);
  if(phase.equals("save")){l.setBlock(anchor.below(),Blocks.STONE.defaultBlockState(),3);l.setBlock(anchor,Military.TABLE.get().defaultBlockState(),3);var p=new ServerPlayer(server,l,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"military-save-test"),ClientInformation.createDefault());p.moveTo(anchor.getX()+2,anchor.getY(),anchor.getZ(),0,0);var offers=MilitaryQuests.available(p,anchor);var kill=offers.stream().filter(q->q.getString("Type").equals("kill")).findFirst().orElseThrow();var trophy=offers.stream().filter(q->q.getString("Type").equals("trophy")).findFirst().orElseThrow();MilitaryQuests.action(p,anchor,kill.getString("Key"),0);MilitaryQuests.accepted(p,kill.getString("Key")).putInt("Progress",3);MilitaryQuests.action(p,anchor,trophy.getString("Key"),0);p.getInventory().add(new ItemStack(Military.TOKEN.get(),6));t.assertTrue(MilitaryQuests.action(p,anchor,trophy.getString("Key"),2),"Saved claim setup");var folder=server.getWorldPath(LevelResource.PLAYER_DATA_DIR);Files.createDirectories(folder);NbtIo.writeCompressed(NbtUtils.addCurrentDataVersion(p.saveWithoutId(new CompoundTag())),folder.resolve(p.getUUID()+".dat"));
   var v=EntityType.VILLAGER.create(l);v.setNoAi(true);v.setPersistenceRequired();v.setVillagerData(v.getVillagerData().setProfession(VillageRoles.ROLES.get("druzhinnik").get()));v.setData(Military.ARCHETYPE,4);v.getPersistentData().putLong("GuardAnchor",anchor.asLong());v.moveTo(anchor.getX()+1.5,anchor.getY(),anchor.getZ()+.5,0,0);l.setBlock(anchor.east().below(),Blocks.STONE.defaultBlockState(),3);l.addFreshEntity(v);Files.writeString(receipt,p.getUUID()+"\n"+v.getUUID()+"\n"+kill.getString("Key")+"\n"+trophy.getString("Key"));server.saveAllChunks(false,true,true);l.setChunkForced(80,80,false);t.succeedWhen(()->{t.assertTrue(!l.hasChunk(80,80),"Military chunk did not really unload");System.out.println("MILITARY_RESTART_SAVE_PASS realPlayerDat=true killProgress=3 claimedReward=true guardArchetype=4 actualChunkUnload=true");});
  }else{var lines=Files.readAllLines(receipt);var p=new ServerPlayer(server,l,new com.mojang.authlib.GameProfile(UUID.fromString(lines.get(0)),"military-save-test"),ClientInformation.createDefault());t.assertTrue(server.getPlayerList().load(p).isPresent(),"Native PlayerDataStorage did not read real saved file");t.runAfterDelay(100,()->{var v=l.getEntity(UUID.fromString(lines.get(1)));t.assertTrue(v instanceof Villager&&v.getData(Military.ARCHETYPE)==4,"Guard/archetype lost in actual restart");t.assertTrue(MilitaryQuests.accepted(p,lines.get(2)).getInt("Progress")==3,"Partial quest progress lost");t.assertTrue(MilitaryQuests.accepted(p,lines.get(3)).getBoolean("Claimed"),"Claim flag lost");int coins=p.getInventory().countItem(ModItems.ANCIENT_COIN.get());t.assertTrue(coins==6&&!MilitaryQuests.action(p,anchor,lines.get(3),2)&&p.getInventory().countItem(ModItems.ANCIENT_COIN.get())==6,"Restart replay duplicated reward");v.discard();l.destroyBlock(anchor,false);l.setChunkForced(80,80,false);System.out.println("MILITARY_RESTART_LOAD_PASS actualNewServer=true nativePlayerDataLoad=true partialProgress=3 claimedReplayRejected=true coins=6 guardArchetype=4");t.succeed();});}
 }
}
