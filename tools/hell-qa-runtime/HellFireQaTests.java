package org.slavicmyths.hellqa;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.burningislands.TemporaryTerrain;

/** Real block writes and scheduled expiration in a disposable Nether fixture, not natural worldgen proof. */
@GameTestHolder("slavicmyths_hellqa") @PrefixGameTestTemplate(false)
public final class HellFireQaTests {
    @GameTest(template="empty",timeoutTicks=200) public static void realFireExpiryAndReplacement(GameTestHelper t) {
        var w=t.getLevel().getServer().getLevel(Level.NETHER);
        var center=new BlockPos(4000008,80,4000008);
        var chunk=w.getChunkAt(center);
        w.setChunkForced(chunk.getPos().x,chunk.getPos().z,true);
        var p=new ServerPlayer(w.getServer(),w,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"FireFixture"),ClientInformation.createDefault());
        p.connection=new HellQaTests.Listener(p);p.setGameMode(GameType.SURVIVAL);p.setNoGravity(true);p.moveTo(center.getX()+.5,80,center.getZ()+.5);
        var target=EntityType.COW.create(w);target.moveTo(p.position());
        var rules=w.getGameRules();
        boolean fireTick=rules.getBoolean(GameRules.RULE_DOFIRETICK),griefing=rules.getBoolean(GameRules.RULE_MOBGRIEFING);
        try {
            // GameTest disables fireTick by default. Set and restore only within this synchronous fixture.
            rules.getRule(GameRules.RULE_DOFIRETICK).set(true,w.getServer());
            rules.getRule(GameRules.RULE_MOBGRIEFING).set(true,w.getServer());
            for(var q:BlockPos.betweenClosed(center.offset(-4,-1,-4),center.offset(4,5,4)))
                w.setBlock(q,q.getY()==79?Blocks.OBSIDIAN.defaultBlockState():Blocks.AIR.defaultBlockState(),3);
            HellQaTests.check(TemporaryTerrain.fire(p,target)==0,"Player-built unmarked floor admitted fire");
            for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
                var floor=center.offset(d[0],-1,d[1]);
                TemporaryTerrain.natural(chunk,floor,w.getBlockState(floor));
            }
            rules.getRule(GameRules.RULE_DOFIRETICK).set(false,w.getServer());
            HellQaTests.check(TemporaryTerrain.fire(p,target)==0,"Fire bypassed doFireTick=false");
            rules.getRule(GameRules.RULE_DOFIRETICK).set(true,w.getServer());
            rules.getRule(GameRules.RULE_MOBGRIEFING).set(false,w.getServer());
            HellQaTests.check(TemporaryTerrain.fire(p,target)==0,"Fire bypassed mobGriefing=false");
            rules.getRule(GameRules.RULE_MOBGRIEFING).set(true,w.getServer());
            int count=TemporaryTerrain.fire(p,target);
            HellQaTests.check(count==4,"Expected four actual fire blocks on marked fixture floor: "+count);
            var replaced=center.east();
            HellQaTests.check(w.getBlockState(replaced).is(Blocks.FIRE)&&TemporaryTerrain.leased(w,replaced),"No real leased fire");
            w.setBlock(replaced,Blocks.STONE.defaultBlockState(),3);
            HellQaTests.check(!TemporaryTerrain.leased(w,replaced),"External replacement retained fire lease");
            t.runAfterDelay(70,()->{try {
                HellQaTests.check(w.getBlockState(replaced).is(Blocks.STONE),"Expiry overwrote external replacement");
                for(var q:List.of(center.west(),center.north(),center.south()))
                    HellQaTests.check(w.isEmptyBlock(q)&&!TemporaryTerrain.leased(w,q),"Real fire failed expiry at "+q);
                System.out.println("HELLQA_PASS actual four FIRE blocks; unmarked player floor rejected; external replacement preserved; three scheduled TTL expirations; artificial provenance fixture, not natural spawn/restart/claims proof");
                t.succeed();
            } finally {w.setChunkForced(chunk.getPos().x,chunk.getPos().z,false);}});
        } catch(Exception e) {w.setChunkForced(chunk.getPos().x,chunk.getPos().z,false);throw e;}
        finally {
            rules.getRule(GameRules.RULE_DOFIRETICK).set(fireTick,w.getServer());
            rules.getRule(GameRules.RULE_MOBGRIEFING).set(griefing,w.getServer());
        }
    }
}
