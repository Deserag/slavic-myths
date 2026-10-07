from pathlib import Path
p=Path('tools/kurgan-tests/org/slavicmyths/verify/KurganHotfixGameTests.java');s=p.read_text();pos=s.rfind('\n}')
s=s[:pos]+'''
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
''' +s[pos:];p.write_text(s)
