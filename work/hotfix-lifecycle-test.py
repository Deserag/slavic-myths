from pathlib import Path
p=Path('tools/kurgan-tests/org/slavicmyths/verify/KurganHotfixGameTests.java');s=p.read_text();pos=s.rfind('\n}')
s=s[:pos]+'''
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
''' +s[pos:];p.write_text(s)
