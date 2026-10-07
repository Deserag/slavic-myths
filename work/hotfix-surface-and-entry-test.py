from pathlib import Path
p=Path('tools/kurgan-tests/org/slavicmyths/verify/KurganHotfixGameTests.java');s=p.read_text()
s=s.replace('roomIndex[0]=0;seen.clear();}', 'auditSurface(test,active[0]);roomIndex[0]=0;seen.clear();}')
s=s.replace('KurganEncounters.trigger(world,BurialRecords.get(world),instance,room,player);\n            var state=', '''player.tickCount=20;if(roomIndex[0]%2==0)player.setGameMode(GameType.CREATIVE);else player.setGameMode(GameType.SURVIVAL);
            KurganEncounters.player(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
            var state=''')
pos=s.index('    @GameTest(template="port_empty",timeoutTicks=5000)')
s=s[:pos]+'''    private static void auditSurface(GameTestHelper test,KurganInstance instance){
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
''' +s[pos:]
p.write_text(s)
