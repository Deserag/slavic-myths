package org.slavicmyths.verify;

import java.util.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.gametest.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import org.slavicmyths.kurgan.*;
import org.slavicmyths.combat.CutEffect;
import org.slavicmyths.registry.*;
import org.slavicmyths.worldgen.ManualStructureJobs;

@GameTestHolder("slavicmyths_hotfix") @PrefixGameTestTemplate(false)
public final class KurganHotfixGameTests {
    @GameTest(template="port_empty",timeoutTicks=200)
    public static void quotasAndSurface(GameTestHelper test){
        int plans=0;
        for(int tier=0;tier<3;tier++)for(int seed=0;seed<100;seed++){
            var plan=KurganPlan.create(tier,seed);var rows=KurganRoster.assign(plan);checkQuota(test,tier,rows.values().stream().flatMap(List::stream).toList());
            test.assertTrue(rows.equals(KurganRoster.assign(plan)),"Roster changed on repeat");
            for(var room:plan.rooms){var list=rows.get(room.id);if(room.archetype==KurganPlan.Archetype.TRAP)test.assertTrue(list.isEmpty(),"Trap pile");
                if(list.contains(KurganFighter.Kind.VOLKHV))test.assertTrue(room.archetype==KurganPlan.Archetype.RITUAL,"Volkhv outside ritual room");
                if(list.contains(KurganFighter.Kind.PRINCE))test.assertTrue(room.id==plan.finalRoom&&list.size()==1,"Prince arena prefilled");}
            plans++;
        }
        Set<Object> materials=new HashSet<>();int equal=0,total=0,dirt=0;
        for(int x=-120;x<=120;x++)for(int z=-120;z<=120;z++){
            var block=KurganSurface.material(75,x,z,70,2,1,true,false,false);materials.add(block);if(block==Blocks.DIRT)dirt++;
            if(block==KurganSurface.material(75,x+1,z,70,2,1,true,false,false))equal++;total++;
        }
        test.assertTrue(materials.size()>=9&&dirt<total*.2&&equal>total*.65,"Surface lacks coherent variety");
        System.out.println("HOTFIX_QUOTAS_PASS plans="+plans+" surfaceMaterials="+materials.size()+" adjacentEqual="+(double)equal/total);test.succeed();
    }
    private static net.minecraft.server.level.ServerPlayer visitor(net.minecraft.server.level.ServerLevel world,String name){
        var player=new net.minecraft.server.level.ServerPlayer(world.getServer(),world,new com.mojang.authlib.GameProfile(UUID.randomUUID(),name),net.minecraft.server.level.ClientInformation.createDefault());
        player.connection=FakePlayerFactory.get(world,new com.mojang.authlib.GameProfile(UUID.randomUUID(),name+"Socket")).connection;player.setGameMode(GameType.SURVIVAL);return player;
    }
    private static void checkQuota(GameTestHelper t,int tier,List<KurganFighter.Kind> list){
        int[] counts=new int[6];for(var kind:list)counts[kind.ordinal()]++;
        int[][] min={{2,0,0,0,0,0},{3,2,2,1,0,0},{5,3,4,1,1,1}},max={{4,1,1,0,0,0},{5,3,4,1,0,0},{8,5,6,1,1,1}};
        for(int k=0;k<6;k++)t.assertTrue(counts[k]>=min[tier][k]&&counts[k]<=max[tier][k],"Population tier="+tier+" counts="+Arrays.toString(counts));
    }
    @GameTest(template="port_empty",timeoutTicks=500)
    public static void cutTimelineAndRules(GameTestHelper test){
        var world=test.getLevel();var a=visitor(world,"CutAlice");var b=visitor(world,"CutBob");
        var target=EntityType.COW.create(world);target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(200);target.setHealth(200);target.setNoGravity(true);target.setNoAi(true);target.moveTo(Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(2,2,2))));a.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(ModItems.NIGHTINGALE_DAGGER.get()));
        target.invulnerableTime=0;target.hurt(world.damageSources().playerAttack(a),1);
        test.assertTrue(target.hasEffect(ModEffects.CUT),"Real damage event failed to apply Cut");
        target.removeEffect(ModEffects.CUT);target.setHealth(200);
        int[] hits={0,40,80,90,100,160};int count=0;float before=target.getHealth();
        for(int tick=0;tick<260;tick++){
            if(Arrays.binarySearch(hits,tick)>=0){CutEffect.apply(count++<3?a:b,target);var effect=target.getEffect(ModEffects.CUT);test.assertTrue(effect.getDuration()==100&&effect.getAmplifier()+1==Math.min(count,5),"Refresh timeline at "+tick);test.assertTrue(target.getPersistentData().getUUID("SlavicCutOwner").equals((count<=3?a:b).getUUID()),"Last attacker attribution");}
            float health=target.getHealth();target.invulnerableTime=0;target.tick();
            float expected=target.getPersistentData().getInt("SlavicCutPulse")==20?.25F*Math.min(count,5):0;
            test.assertTrue(Math.abs(health-target.getHealth()-expected)<.001F,"Cut pulse amount at "+tick);
            if(tick<259)test.assertTrue(target.hasEffect(ModEffects.CUT),"Early expiration "+tick);
        }
        test.assertTrue(!target.hasEffect(ModEffects.CUT),"Stacks survive common expiration");test.assertTrue(target.getHealth()<before,"No periodic damage");
        for(var accessory:List.of(ModItems.HUNTER_CHARM.get(),ModItems.PERUN_RING.get())){
            a.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(accessory));target.invulnerableTime=0;float health=target.getHealth();
            var source=org.slavicmyths.combat.MythDamageSources.periodic("cut",a);
            test.assertTrue(source.getEntity()==a&&source.getDirectEntity()==null,"Periodic attribution is a melee hit");
            target.hurt(source,1.25F);test.assertTrue(Math.abs(health-target.getHealth()-1.25F)<.001F,"Accessory altered Cut damage");
        }
        var immune=ModEntities.NAV.get().create(world);CutEffect.apply(a,immune);test.assertTrue(!immune.hasEffect(ModEffects.CUT),"Immune received Cut");
        boolean pvp=world.getServer().isPvpAllowed();world.getServer().setPvpAllowed(false);CutEffect.apply(a,b);test.assertTrue(!b.hasEffect(ModEffects.CUT),"PvP disabled bypassed");world.getServer().setPvpAllowed(true);
        var board=world.getScoreboard();var team=board.addPlayerTeam("cut_"+UUID.randomUUID().toString().substring(0,8));team.setAllowFriendlyFire(false);board.addPlayerToTeam(a.getScoreboardName(),team);board.addPlayerToTeam(b.getScoreboardName(),team);
        CutEffect.apply(a,b);test.assertTrue(!b.hasEffect(ModEffects.CUT),"Team friendly fire bypassed");board.removePlayerTeam(team);CutEffect.apply(a,b);test.assertTrue(b.getEffect(ModEffects.CUT).getDuration()==100,"Allowed PvP failed");b.removeEffect(ModEffects.CUT);world.getServer().setPvpAllowed(pvp);
        var saved=new CompoundTag();CutEffect.apply(a,target);target.saveWithoutId(saved);var copy=EntityType.COW.create(world);copy.load(saved);test.assertTrue(copy.getEffect(ModEffects.CUT).getDuration()==100,"Native effect save failed");
        System.out.println("HOTFIX_CUT_PASS timeline=0,40,80,90,100,160 expires=260 cap=5 sharedPlayers=2 damage=true immunity=true pvp=true team=true nativeNbt=true");test.succeed();
    }
    @GameTest(template="port_empty",timeoutTicks=100000)
    public static void commandsAndEncounters(GameTestHelper test){
        var world=test.getLevel();world.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DOMOBSPAWNING).set(false,world.getServer());world.setDayTime(18000);
        var player=FakePlayerFactory.get(world,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"KurganVisitor"));player.setGameMode(GameType.SURVIVAL);
        int sourceBase=64000+BurialRecords.get(world).instances.size()*16384;int[] tier={0};UUID[] job={null};KurganInstance[] active={null};int[] roomIndex={0};List<KurganFighter.Kind> seen=new ArrayList<>();
        test.onEachTick(()->{
            java.util.concurrent.locks.LockSupport.parkNanos(1_000_000L);
            if(tier[0]==3){System.out.println("HOTFIX_COMMAND_ENCOUNTERS_PASS commands=3 tiers=3 naturalSpawning=false allRoomsSpawned=true clearedRevisit=true");test.succeed();return;}
            if(job[0]==null){try{var source=world.getServer().createCommandSourceStack().withLevel(world).withPosition(new Vec3(sourceBase+tier[0]*4096,0,32000));test.assertTrue(world.getServer().getCommands().getDispatcher().execute("slavicmyths generate kurgan "+new String[]{"small","warrior","great"}[tier[0]]+" 0",source)==1,"Command rejected");job[0]=ManualStructureJobs.lastJob;}catch(Exception e){throw new RuntimeException(e);}return;}
            if(active[0]==null){var result=ManualStructureJobs.result(job[0]);if(result==null)return;test.assertTrue(result.success(),"Command failed "+result.message());active[0]=BurialRecords.get(world).instances.get(job[0]);test.assertTrue(active[0]!=null,"Command lost instance");test.assertTrue(active[0].encounters.size()==active[0].plan.rooms.size(),"Missing encounter metadata");auditSurface(test,active[0]);roomIndex[0]=0;seen.clear();}
            var instance=active[0];
            if(roomIndex[0]==instance.plan.rooms.size()){checkQuota(test,tier[0],seen);System.out.println("HOTFIX_ACTUAL_POPULATION tier="+tier[0]+" mobs="+seen);tier[0]++;job[0]=null;active[0]=null;return;}
            var room=instance.plan.rooms.get(roomIndex[0]);var roster=KurganRoster.roster(instance,room);if(roster.isEmpty()){roomIndex[0]++;return;}
            var box=room.box();boolean entitiesLoaded=true;for(int x=(instance.origin.getX()+box.x0-2)>>4;x<=(instance.origin.getX()+box.x1+2)>>4;x++)for(int z=(instance.origin.getZ()+box.z0-2)>>4;z<=(instance.origin.getZ()+box.z1+2)>>4;z++){world.setChunkForced(x,z,true);world.getChunk(x,z);if(!world.areEntitiesLoaded(ChunkPos.asLong(x,z)))entitiesLoaded=false;}if(!entitiesLoaded)return;
            if(room.hall()&&!instance.sealOpened){instance.disturbance=75;test.assertTrue(KurganDungeonPiece.openSeal(world,instance.id),"Loaded seal cannot open");}
            var probe=ModEntities.UPYR.get().create(world);probe.kurgan=instance.id;probe.room=room.id;probe.home=instance.origin.offset(room.x,room.y+1,room.z);var pos=KurganEncounters.safePosition(probe,Vec3.atBottomCenterOf(probe.home),false);test.assertTrue(pos!=null,"No player floor room="+room.id);player.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);
            player.tickCount=20;if(roomIndex[0]%2==0)player.setGameMode(GameType.CREATIVE);else player.setGameMode(GameType.SURVIVAL);
            KurganEncounters.player(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
            var state=instance.encounters.get(room.id);test.assertTrue(state.alive.size()==roster.size(),"Spawn incomplete tier="+tier[0]+" room="+room.id+" art="+room.archetype+" expected="+roster+" actual="+state.alive.size());
            KurganEncounters.trigger(world,BurialRecords.get(world),instance,room,player);test.assertTrue(state.alive.size()==roster.size(),"Duplicate trigger");
            for(UUID id:new ArrayList<>(state.alive)){var mob=(KurganCreature)world.getEntity(id);test.assertTrue(mob!=null&&world.noCollision(mob),"Invalid mob placement tier="+tier[0]+" room="+room.id+" kind="+(mob==null?"null":mob.kind)+" pos="+(mob==null?"null":mob.position()));seen.add(mob.kind);mob.hurt(world.damageSources().genericKill(),10000);}
            test.assertTrue(state.cleared,"Real deaths failed to clear room");KurganEncounters.trigger(world,BurialRecords.get(world),instance,room,player);test.assertTrue(state.alive.isEmpty(),"Cleared room respawned");
            roomIndex[0]++;
        });
    }
    private static void auditSurface(GameTestHelper test,KurganInstance instance){
        var world=test.getLevel();Map<String,Integer> counts=new TreeMap<>();int radius=instance.plan.radius+12,exposed=0;var blocks=Set.of(Blocks.GRASS_BLOCK,Blocks.COARSE_DIRT,Blocks.ROOTED_DIRT,Blocks.DIRT,Blocks.GRAVEL,Blocks.STONE,Blocks.MOSS_BLOCK,Blocks.COBBLESTONE,Blocks.MOSSY_COBBLESTONE,Blocks.PODZOL);
        for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++){
            int wx=instance.origin.getX()+x,wz=instance.origin.getZ()+z;world.getChunk(wx>>4,wz>>4);int y=world.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,wx,wz)-1;
            var p=new BlockPos(wx,y,wz);while(y>world.getMinBuildHeight()&&world.getBlockState(p).canBeReplaced()){p=p.below();y--;}
            var state=world.getBlockState(p);int original=world.getChunkSource().getGenerator().getBaseHeight(wx,wz,net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR_WG,world,world.getChunkSource().randomState())-1;
            if(y<=original||!blocks.contains(state.getBlock()))continue;exposed++;String name=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();counts.merge(name,1,Integer::sum);
        }
        test.assertTrue(exposed>100&&counts.size()>=7&&counts.getOrDefault("minecraft:dirt",0)<exposed*.8,"Raised exterior palette failed: "+counts);
        System.out.println("HOTFIX_ACTUAL_SURFACE_PASS tier="+instance.plan.tier+" exposed="+exposed+" palette="+counts);
    }
    @GameTest(template="port_empty",timeoutTicks=5000)
    public static void activeUnloadRecoveryAndClearedRevisit(GameTestHelper test){
        var world=test.getLevel();var player=visitor(world,"LifecycleVisitor");var data=BurialRecords.get(world);
        var plan=KurganPlan.create(0,31);var origin=new BlockPos(400000+data.instances.size()*1024,80,400000);var instance=new KurganInstance(UUID.randomUUID(),origin,plan);data.register(instance);
        var room=plan.rooms.stream().filter(r->!instance.population().get(r.id).isEmpty()&&r.archetype!=KurganPlan.Archetype.RELIQUARY).findFirst().orElseThrow();
        var bounds=room.box();var clip=new net.minecraft.world.level.levelgen.structure.BoundingBox(origin.getX()+bounds.x0,origin.getY()+bounds.y0,origin.getZ()+bounds.z0,origin.getX()+bounds.x1,origin.getY()+bounds.y1,origin.getZ()+bounds.z1);
        Set<ChunkPos> chunks=new HashSet<>();for(int x=(clip.minX()-16)>>4;x<=(clip.maxX()+16)>>4;x++)for(int z=(clip.minZ()-16)>>4;z<=(clip.maxZ()+16)>>4;z++){var chunk=new ChunkPos(x,z);chunks.add(chunk);world.setChunkForced(x,z,true);world.getChunk(x,z);}
        new KurganDungeonPiece(plan,origin,instance.id).postProcess(world,world.structureManager(),world.getChunkSource().getGenerator(),net.minecraft.util.RandomSource.create(31),clip,new ChunkPos(origin),origin);
        var state=instance.encounters.get(room.id);int[] stage={0},wait={0};Set<UUID> originals=new HashSet<>();UUID[] missing={null},replacement={null};
        test.onEachTick(()->{
            java.util.concurrent.locks.LockSupport.parkNanos(1_000_000L);
            if(stage[0]==0){if(chunks.stream().anyMatch(c->!world.areEntitiesLoaded(c.toLong())))return;
                var probe=ModEntities.UPYR.get().create(world);probe.kurgan=instance.id;probe.room=room.id;var pos=KurganEncounters.safePosition(probe,Vec3.atBottomCenterOf(origin.offset(room.x,room.y+1,room.z)),false);test.assertTrue(pos!=null,"Lifecycle visitor floor missing");player.moveTo(Vec3.atBottomCenterOf(pos));
                KurganEncounters.trigger(world,data,instance,room,player);test.assertTrue(state.alive.size()==instance.population().get(room.id).size(),"Lifecycle population missing");
                originals.addAll(state.alive);for(var id:originals){var mob=(KurganCreature)world.getEntity(id);test.assertTrue(mob!=null,"Lifecycle UUID hidden");mob.setNoAi(true);}
                player.moveTo(0,100,0);for(var c:chunks)world.setChunkForced(c.x,c.z,false);stage[0]=1;return;
            }
            if(stage[0]==1){if(chunks.stream().anyMatch(c->world.areEntitiesLoaded(c.toLong())))return;
                test.assertTrue(originals.stream().allMatch(id->world.getEntity(id)==null),"Chunk unload did not remove visible entities");
                KurganEncounters.trigger(world,data,instance,room,player);test.assertTrue(state.alive.equals(originals)&&!state.cleared,"Unloaded room respawned or cleared");
                for(var c:chunks){world.setChunkForced(c.x,c.z,true);world.getChunk(c.x,c.z);}stage[0]=2;return;
            }
            if(stage[0]==2){if(chunks.stream().anyMatch(c->!world.areEntitiesLoaded(c.toLong())))return;
                test.assertTrue(originals.stream().allMatch(id->world.getEntity(id)!=null),"Engine reload lost original mobs");player.moveTo(Vec3.atBottomCenterOf(origin.offset(room.x,room.y+1,room.z)));
                KurganEncounters.trigger(world,data,instance,room,player);test.assertTrue(state.alive.equals(originals),"Reload duplicated live encounter");
                missing[0]=originals.iterator().next();world.getEntity(missing[0]).discard();KurganEncounters.trigger(world,data,instance,room,player);
                test.assertTrue(!state.alive.contains(missing[0])&&state.retired.contains(missing[0])&&state.alive.size()==originals.size(),"Lost slot not recovered once");
                replacement[0]=state.alive.stream().filter(id->!originals.contains(id)).findFirst().orElseThrow();KurganEncounters.trigger(world,data,instance,room,player);test.assertTrue(state.alive.size()==originals.size(),"Recovery duplicated");
                var stale=ModEntities.UPYR.get().create(world);stale.kurgan=instance.id;stale.room=room.id;stale.setUUID(missing[0]);stale.moveTo(player.position());test.assertTrue(!world.addFreshEntity(stale),"Retired UUID rejoined");
                var restored=KurganInstance.load(instance.save());test.assertTrue(restored.encounters.get(room.id).slots.stream().filter(slot->replacement[0].equals(slot.uuid)).allMatch(slot->slot.restored),"Recovery flag not saved");
                for(var id:new ArrayList<>(state.alive))((KurganCreature)world.getEntity(id)).hurt(world.damageSources().genericKill(),10000);
                test.assertTrue(state.cleared,"Recovered room did not clear");stage[0]=3;return;
            }
            if(stage[0]==3){if(++wait[0]<25)return;player.moveTo(0,100,0);for(var c:chunks)world.setChunkForced(c.x,c.z,false);stage[0]=4;return;}
            if(stage[0]==4){if(chunks.stream().anyMatch(c->world.areEntitiesLoaded(c.toLong())))return;for(var c:chunks){world.setChunkForced(c.x,c.z,true);world.getChunk(c.x,c.z);}stage[0]=5;return;}
            if(stage[0]==5){if(chunks.stream().anyMatch(c->!world.areEntitiesLoaded(c.toLong())))return;player.moveTo(Vec3.atBottomCenterOf(origin.offset(room.x,room.y+1,room.z)));KurganEncounters.trigger(world,data,instance,room,player);
                test.assertTrue(state.cleared&&state.alive.isEmpty(),"Cleared actual reload respawned");for(var c:chunks)world.setChunkForced(c.x,c.z,false);
                System.out.println("HOTFIX_ACTUAL_CHUNK_LIFECYCLE_PASS activeUnloadReload=true sameOriginalUUIDs=true missingSlotRecovery=1 staleUUIDRejected=true clearedUnloadReload=true");test.succeed();
            }
        });
    }

    @GameTest(template="port_empty",timeoutTicks=1000)
    public static void nativePlacementRegistersEncounters(GameTestHelper test){
        var world=test.getLevel();List<UUID> ids=new ArrayList<>();
        int base=600000+BurialRecords.get(world).instances.size()*2048;
        for(int tier=0;tier<3;tier++){
            var plan=KurganPlan.create(tier,83);int x=base+tier*4096,z=600000;
            int ground=world.getChunkSource().getGenerator().getBaseHeight(x,z,net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR_WG,world,world.getChunkSource().randomState())-1;
            var site=KurganEarthwork.plan(world.getChunkSource().getGenerator(),world.getChunkSource().randomState(),world,plan,new BlockPos(x,ground,z));test.assertTrue(site!=null,"Native earthwork failed");
            UUID id=UUID.randomUUID();ids.add(id);var piece=new KurganDungeonPiece(plan,site.origin(),id,site.earthwork());var box=piece.getBoundingBox();
            for(int cx=box.minX()>>4;cx<=box.maxX()>>4;cx++)for(int cz=box.minZ()>>4;cz<=box.maxZ()>>4;cz++)world.getChunk(cx,cz);
            // Production postProcess, without direct SavedData registration or the manual prepared shortcut.
            piece.postProcess(world,world.structureManager(),world.getChunkSource().getGenerator(),net.minecraft.util.RandomSource.create(83),box,new ChunkPos(site.origin()),site.origin());
        }
        test.runAfterDelay(5,()->{
            for(int tier=0;tier<3;tier++){var instance=BurialRecords.get(world).instances.get(ids.get(tier));test.assertTrue(instance!=null,"Native placement omitted metadata");test.assertTrue(instance.encounters.size()==instance.plan.rooms.size(),"Native room identifiers missing");checkQuota(test,tier,instance.encounters.values().stream().flatMap(e->e.slots.stream()).map(slot->slot.kind).toList());}
            System.out.println("HOTFIX_NATIVE_PLACEMENT_PASS tiers=3 postProcess=true metadata=true quotas=true naturalFrequencyNotMeasured=true");test.succeed();
        });
    }

}
