package org.slavicmyths.verify;

import java.util.*;
import java.nio.file.*;
import com.google.gson.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.bayun.*;
import org.slavicmyths.tales.*;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.progression.Knowledge;

/** Two actual server processes reopen the same isolated on-disk world. */
@GameTestHolder("slavicmyths_bayun_restart") @PrefixGameTestTemplate(false)
public final class BayunRestart {
    private static final UUID PLAYER=UUID.fromString("bae01769-2600-4100-a006-000000000001");
    private static final BlockPos HOME=new BlockPos(8,-58,20008);
    private static final class Connection extends net.minecraft.network.Connection {private final io.netty.channel.embedded.EmbeddedChannel channel=new io.netty.channel.embedded.EmbeddedChannel();Connection(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);}@Override public io.netty.channel.Channel channel(){return channel;}}
    private static final class Listener extends net.minecraft.server.network.ServerGamePacketListenerImpl {Listener(ServerPlayer p){super(p.getServer(),new Connection(),p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));}@Override public void send(net.minecraft.network.protocol.Packet<?> packet){}@Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener){}}
    private static ServerPlayer player(GameTestHelper t){var p=new ServerPlayer(t.getLevel().getServer(),t.getLevel(),new com.mojang.authlib.GameProfile(PLAYER,"BayunRestart"),ClientInformation.createDefault());p.connection=new Listener(p);p.setGameMode(GameType.SURVIVAL);p.moveTo(HOME.getX()+.5,HOME.getY(),HOME.getZ()+1.8);return p;}
    @GameTest(template="empty",timeoutTicks=1200) public static void diskWorldReopen(GameTestHelper t)throws Exception{
        String phase=System.getProperty("slavicmyths.bayun.restartPhase","none");var w=t.getLevel();w.getChunkAt(HOME);Path checkpoint=Path.of("bayun-restart-checkpoint.json");
        if(phase.equals("write")){
            for(int x=-6;x<=6;x++)for(int z=-6;z<=6;z++)w.setBlock(HOME.offset(x,-1,z),Blocks.STONE.defaultBlockState(),2);
            var cat=BayunContent.ENTITY.get().create(w);cat.home=HOME;cat.moveTo(HOME.getX()+.5,HOME.getY(),HOME.getZ()+.5);t.assertTrue(w.addFreshEntity(cat),"Restart NPC failed to spawn");var p=player(t);TaleService.deliver(p);t.assertTrue(p.getInventory().countItem(ModItems.LORE_BOOK.get())==1,"Restart initial delivery");p.getInventory().clearContent();p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.MILK_BUCKET));cat.interact(p,InteractionHand.MAIN_HAND);TaleService.promote(p,"slavicmyths:kot_bayun",4,false);Knowledge.award(p,"defeat_nightingale");p.getAdvancements().save();JsonObject n=new JsonObject();n.addProperty("cat",cat.getUUID().toString());n.addProperty("trust",cat.trust(p));Files.writeString(checkpoint,n.toString());System.out.println("BAYUN_RESTART_WRITE cat="+cat.getUUID()+" trust="+cat.trust(p)+" delivery=DELIVERED vanillaAdvancementFixtureSaved=true");t.succeed();
        }else if(phase.equals("read")){
            // Fixture-only ticket makes the remote test chunk entity-accessible;
            // production Bayun never requests tickets or loads its home chunk.
            w.setChunkForced(HOME.getX()>>4,HOME.getZ()>>4,true);
            var n=JsonParser.parseString(Files.readString(checkpoint)).getAsJsonObject();UUID id=UUID.fromString(n.get("cat").getAsString());t.startSequence().thenWaitUntil(()->t.assertTrue(w.getEntity(id) instanceof BayunEntity,"Persisted UUID entity not reloaded")).thenExecute(()->{var cat=(BayunEntity)w.getEntity(id);var p=player(t);t.assertTrue(cat.home.equals(HOME)&&cat.distanceToSqr(HOME.getX()+.5,HOME.getY(),HOME.getZ()+.5)<25,"NPC home or bounded position changed");t.assertTrue(cat.trust(p)==n.get("trust").getAsInt()&&cat.trust(p)==25,"Personal bond lost on disk reopen");TaleService.deliver(p);t.assertTrue(p.getInventory().countItem(ModItems.LORE_BOOK.get())==0,"Restart reissued lost book");t.assertTrue(TaleData.get(w).stage(PLAYER,"slavicmyths:kot_bayun")==4,"World knowledge lost");t.assertTrue(Knowledge.knows(p,"defeat_nightingale"),"Native advancement fixture lost");TaleService.reconcile(p);t.assertTrue(!TaleData.get(w).player(PLAYER).getCompound("Entries").getCompound("slavicmyths:nightingale").getBoolean("Defeated"),"Legacy advancement invented personal kill");w.setChunkForced(HOME.getX()>>4,HOME.getZ()>>4,false);System.out.println("BAYUN_RESTART_READ PASS actualDiskWorldReopen=true sameNpcUuid=true trust25=true lostBookNotReissued=true vanillaProgressPreserved=true NOT_REAL_CLIENT_RELOG");}).thenSucceed();
        }else throw new IllegalArgumentException("Explicit write/read phase required");
    }
}
