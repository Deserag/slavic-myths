package org.slavicmyths.verify;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import org.slavicmyths.navigation.*;

@GameTestHolder("slavicmyths")
@PrefixGameTestTemplate(false)
public final class NavigationGameTests {
    @GameTest(template="port_empty",timeoutTicks=100)
    public static void modelAndWire(GameTestHelper test){
        NavigationHeadless.main(new String[0]);
        var state=new NavigationState();UUID id=UUID.randomUUID();var area=SearchArea.generate(12345,67890,SearchArea.Scale.WORLD,id,9343444,0,(x,z,r)->true);
        state.upsert(new SlavicMarker(id,"quest:"+id+":search",MarkerCategory.QUEST,SlavicMarker.Kind.SEARCH_AREA,SlavicMarker.Lifetime.UNTIL_QUEST_END,"entity.slavicmyths.tugarin_zmey",ResourceLocation.parse("minecraft:overworld"),(int)area.centerX(),0,(int)area.centerZ(),0,0,area));
        var bytes=new RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),test.getLevel().registryAccess());
        try{NavigationNetwork.Snapshot.CODEC.encode(bytes,new NavigationNetwork.Snapshot(state.save(),true));var copy=NavigationNetwork.Snapshot.CODEC.decode(bytes);
            test.assertTrue(copy.open()&&NavigationState.read(copy.state()).markers.get(id).area().equals(area),"Wire geometry changed");
            test.assertTrue(!copy.state().toString().contains("Anchor")&&!copy.state().toString().contains("9343444"),"Secret payload fields");
            CompoundTag mutated=copy.state();mutated.putString("Injected","value");test.assertTrue(!copy.state().contains("Injected"),"Snapshot aliases NBT");
            for(var action:NavigationNetwork.Action.values()){bytes.clear();var request=new NavigationNetwork.Request(action,id,3,true);NavigationNetwork.Request.CODEC.encode(bytes,request);test.assertTrue(NavigationNetwork.Request.CODEC.decode(bytes).equals(request),"Request codec "+action);}
        }finally{bytes.release();}
        System.out.println("NAVIGATION_RUNTIME_MODEL_AND_WIRE_PASS");test.succeed();
    }
    @GameTest(template="port_empty",timeoutTicks=100)
    public static void serverAuthority(GameTestHelper test){
        var w=test.getLevel();var alice=FakePlayerFactory.get(w,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"NavAlice"));
        var bob=FakePlayerFactory.get(w,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"NavBob"));
        var data=NavigationRecords.get(w);var a=data.player(alice.getUUID());var b=data.player(bob.getUUID());
        UUID id=UUID.randomUUID();BlockPos target=new BlockPos(12345,64,67890);
        var area=SearchArea.generate(target.getX(),target.getZ(),SearchArea.Scale.MINI,id,w.getSeed(),0,(x,z,r)->true);
        a.upsert(new SlavicMarker(id,"quest:"+id,MarkerCategory.QUEST,SlavicMarker.Kind.SEARCH_AREA,SlavicMarker.Lifetime.UNTIL_QUEST_END,"entity.slavicmyths.ovinnik",w.dimension().location(),(int)area.centerX(),0,(int)area.centerZ(),0,0,area));a.track(id);
        data.assignments.put(id,new NavigationRecords.Assignment(alice.getUUID(),id,UUID.randomUUID(),target,w.dimension().location(),"ovinnik","dev",0));
        NavigationManager.request(bob,new NavigationNetwork.Request(NavigationNetwork.Action.TRACK,id,0,true));test.assertTrue(b.tracked==null&&b.markers.isEmpty(),"Another player adopted quest");
        NavigationManager.request(alice,new NavigationNetwork.Request(NavigationNetwork.Action.FORGET,id,0,true));test.assertTrue(a.markers.containsKey(id)&&data.assignments.containsKey(id),"Client deleted quest");
        var dispatcher=w.getServer().getCommands().getDispatcher();var source=alice.createCommandSourceStack().withPermission(0);
        var root=dispatcher.getRoot().getChild("slavicmyths");test.assertTrue(root.canUse(source)&&root.getChild("navigation").canUse(source),"Navigation requires OP");
        for(String dev:List.of("dev","locate","kurgan"))test.assertTrue(!root.getChild(dev).canUse(source),"Dev permissions weakened: "+dev);
        var saved=data.save(new CompoundTag(),w.registryAccess());test.assertTrue(saved.getList("Assignments",10).size()>0,"Private assignment not persisted");
        test.assertTrue(!a.save().contains("Assignments")&&!a.save().toString().contains("Anchor"),"Private data exposed");
        data.players.remove(alice.getUUID());data.players.remove(bob.getUUID());data.assignments.remove(id);data.setDirty();
        System.out.println("NAVIGATION_RUNTIME_SERVER_AUTHORITY_PASS");test.succeed();
    }
    @GameTest(template="port_empty",timeoutTicks=100)
    public static void lifecycleAndHomes(GameTestHelper test){
        var w=test.getLevel();var p=FakePlayerFactory.get(w,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"NavLifecycle"));
        var data=NavigationRecords.get(w);var state=data.player(p.getUUID());var yaga=org.slavicmyths.yaga.YagaData.get(w);
        var oldHome=yaga.anchor;var oldNpc=yaga.npc;boolean oldPlaced=yaga.placed;
        BlockPos stone=test.absolutePos(new BlockPos(1,2,1));var oldBlock=w.getBlockState(stone);
        org.slavicmyths.hunt.HuntMob mob=org.slavicmyths.registry.ModEntities.OVINNIK.get().create(w);
        try{
            yaga.anchor=new BlockPos(23456,80,34567);yaga.placed=false;
            test.assertTrue(!NavigationManager.thread(p)&&state.markers.isEmpty(),"Thread revealed unplaced home");
            yaga.placed=true;yaga.npc=UUID.randomUUID();NavigationManager.thread(p);NavigationManager.thread(p);
            UUID home=SlavicMarker.identity("yaga_hut:world");test.assertTrue(state.markers.size()==1&&home.equals(state.tracked),"Home duplicates/tracking");
            w.setBlock(stone,org.slavicmyths.registry.ModBlocks.PATH_STONE.get().defaultBlockState(),3);
            NavigationManager.waystone(p,stone);NavigationManager.waystone(p,stone);test.assertTrue(state.markers.size()==2,"Stone duplicates");
            var yagaProgress=yaga.progress(p.getUUID());yagaProgress.stage=1;yagaProgress.active="contract:0";
            mob.owner=p.getUUID();mob.home=stone;mob.moveTo(stone.getX()+.5,stone.getY(),stone.getZ()+.5,0,0);w.addFreshEntity(mob);
            UUID marker=UUID.randomUUID();
            test.assertTrue(NavigationManager.assign(p,data.encounters.get(mob.getUUID()),"contract:0",marker,0,true),"Actual terrain/biome assignment rejected flat test world");
            data.pendingQuests.put(p.getUUID(),new NavigationRecords.PendingQuest("contract:0","ovinnik",marker,0,state.trackingChoiceRevision));
            var restored=NavigationRecords.load(data.save(new CompoundTag(),w.registryAccess()),w.registryAccess());
            test.assertTrue(restored.player(p.getUUID()).tracked.equals(marker)&&restored.assignments.get(marker).anchor().equals(stone),"Server save/load lost quest");
            var future=new CompoundTag();future.putInt("NavigationDataVersion",99);future.putString("FutureData","preserve");
            test.assertTrue(NavigationRecords.load(future,w.registryAccess()).save(new CompoundTag(),w.registryAccess()).equals(future),"Future schema destroyed");
            NavigationManager.discovered(p,data.assignments.get(marker),mob);
            test.assertTrue(state.markers.get(marker).area()==null&&state.markers.get(marker).category()==MarkerCategory.BOSS,"Discovery transition failed");
            test.assertTrue(state.markers.get(marker).x()==stone.getX(),"Revealed target not actual entity");
            state.preferences.remove(NavigationState.Preference.AUTO_QUEST);
            mob.discard();test.assertTrue(!state.markers.containsKey(marker)&&!data.assignments.containsKey(marker)&&state.tracked==null,"Despawn left orphan");
            test.assertTrue(data.pendingQuests.get(p.getUUID()).revision()==1,"Replacement did not increment revision offline");
            test.assertTrue(data.pendingQuests.get(p.getUUID()).preserveTracked(),"Manual tracking intent lost with auto-track disabled");
            w.setBlock(stone,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);p.setPos(stone.getX(),stone.getY(),stone.getZ());p.tickCount=40;
            NavigationEvents.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));
            test.assertTrue(state.markers.values().stream().noneMatch(m->m.category()==MarkerCategory.WAYSTONE),"Removed stone persisted");
            test.assertTrue(state.markers.containsKey(home),"Cleanup removed permanent home");
            System.out.println("NAVIGATION_RUNTIME_LIFECYCLE_AND_HOMES_PASS");test.succeed();
        }finally{
            mob.discard();w.setBlock(stone,oldBlock,3);yaga.anchor=oldHome;yaga.npc=oldNpc;yaga.placed=oldPlaced;yaga.players.remove(p.getUUID());yaga.setDirty();
            data.players.remove(p.getUUID());data.pendingQuests.remove(p.getUUID());data.assignments.values().removeIf(a->a.player().equals(p.getUUID()));data.encounters.remove(mob.getUUID());data.setDirty();
            NavigationEvents.logout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(p));
        }
    }
}
