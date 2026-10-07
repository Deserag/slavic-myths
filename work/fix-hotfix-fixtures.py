from pathlib import Path
p=Path('tools/kurgan-tests/org/slavicmyths/verify/KurganHotfixGameTests.java');s=p.read_text()
s=s.replace('var a=FakePlayerFactory.get(world,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"CutAlice"));var b=FakePlayerFactory.get(world,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"CutBob"));','var a=visitor(world,"CutAlice");var b=visitor(world,"CutBob");')
s=s.replace('int[] tier={0};UUID[] job={null};','int sourceBase=64000+BurialRecords.get(world).instances.size()*16384;int[] tier={0};UUID[] job={null};')
s=s.replace('new Vec3(32000+tier[0]*4096,0,32000)','new Vec3(sourceBase+tier[0]*4096,0,32000)')
s=s.replace('var box=room.box();for(int x=', 'var box=room.box();boolean entitiesLoaded=true;for(int x=')
s=s.replace('world.setChunkForced(x,z,true);world.getChunk(x,z);}', 'world.setChunkForced(x,z,true);world.getChunk(x,z);if(!world.areEntitiesLoaded(ChunkPos.asLong(x,z)))entitiesLoaded=false;}if(!entitiesLoaded)return;')
s=s.replace('    private static void checkQuota', '''    private static net.minecraft.server.level.ServerPlayer visitor(net.minecraft.server.level.ServerLevel world,String name){
        var player=new net.minecraft.server.level.ServerPlayer(world.getServer(),world,new com.mojang.authlib.GameProfile(UUID.randomUUID(),name),net.minecraft.server.level.ClientInformation.createDefault());
        player.connection=new net.neoforged.neoforge.common.util.FakePlayer.FakePlayerNetHandler(world.getServer(),player);player.setGameMode(GameType.SURVIVAL);return player;
    }
    private static void checkQuota''')
p.write_text(s)
