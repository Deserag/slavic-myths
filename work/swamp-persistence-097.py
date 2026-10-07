from pathlib import Path
p=Path('src/main/java/org/slavicmyths/swamp/SwampPiece.java');s=p.read_text(encoding='utf-8').replace('private boolean encounterProcessed;','private boolean encounterProcessed;private final java.util.Set<Long> processedMarkers=new java.util.HashSet<>();');s=s.replace('encounterProcessed=nbt.getBoolean("EncounterProcessed");load(manager);','encounterProcessed=nbt.getBoolean("EncounterProcessed");for(long marker:nbt.getLongArray("SwampProcessedMarkers"))processedMarkers.add(marker);load(manager);');s=s.replace('nbt.putBoolean("EncounterProcessed",encounterProcessed);','nbt.putBoolean("EncounterProcessed",encounterProcessed);nbt.putLongArray("SwampProcessedMarkers",processedMarkers.stream().mapToLong(Long::longValue).toArray());');a=s.index('    @Override protected void handleDataMarker');s=s[:a]+'''    @Override protected void handleDataMarker(String marker,BlockPos pos,ServerLevelAccessor world,RandomSource random,BoundingBox box) {
        if(!box.isInside(pos))return;
        if(marker.startsWith("loot:")) {
            var existing=world.getBlockEntity(pos);
            if(existing instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity&&existing.getPersistentData().getBoolean("SlavicSwampLoot"))return;
            Random choice=new Random(seed^pos.asLong());
            if(!name.startsWith("v097_")&&((marker.endsWith("_home")&&choice.nextInt(4)==0)||(marker.endsWith("_remnants")&&choice.nextBoolean()))return;
            world.setBlock(pos,Blocks.BARREL.defaultBlockState(),2);
            if(world.getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity container){
                container.setLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,ResourceLocation.fromNamespaceAndPath("slavicmyths","chests/"+marker.substring(5))),seed^pos.asLong());
                container.getPersistentData().putBoolean("SlavicSwampLoot",true);container.setChanged();
            }
        } else if(marker.startsWith("encounter:")) {
            long key=pos.asLong();
            if(processedMarkers.contains(key)||!name.startsWith("v097_")&&encounterProcessed)return;
            processedMarkers.add(key);if(!name.startsWith("v097_"))encounterProcessed=true;
            boolean bog=marker.contains("bolotnik"),elite=marker.endsWith("_elite");
            if(world.getLevel().getDifficulty()==Difficulty.PEACEFUL||!elite&&new Random(seed^key).nextInt(bog?3:8)!=0)return;
            net.minecraft.world.entity.Mob mob=bog?org.slavicmyths.registry.ModEntities.BOLOTNIK.get().create(world.getLevel()):
                marker.endsWith("kikimora")?org.slavicmyths.registry.ModEntities.KIKIMORA.get().create(world.getLevel()):
                marker.endsWith("rusalka")?org.slavicmyths.registry.ModEntities.RUSALKA.get().create(world.getLevel()):org.slavicmyths.registry.ModEntities.VODYANOY.get().create(world.getLevel());
            if(mob==null)return;
            mob.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);
            if(!world.noCollision(mob))return;
            if(mob instanceof org.slavicmyths.entity.LandSpiritEntity spirit)spirit.home=pos;
            mob.finalizeSpawn(world,world.getCurrentDifficultyAt(pos),net.minecraft.world.entity.MobSpawnType.STRUCTURE,null);
            if(elite){mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(112);mob.setHealth(112);mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).setBaseValue(9);mob.setCustomName(net.minecraft.network.chat.Component.translatable("entity.slavicmyths.bolotnik_elite"));}
            mob.setPersistenceRequired();world.addFreshEntity(mob);
        }
    }
}
''';p.write_text(s,encoding='utf-8')
# Extend static verification to both changesets; leave historical report paths untouched.
p=Path('tools/verify_static_data_1211.py');s=p.read_text(encoding='utf-8').replace("if '--kurgan-rework' in sys.argv:","if '--kurgan-rework' in sys.argv or '--swamp-rework' in sys.argv:",1);a=s.index('sources=ROOT/');s=s[:a]+"if '--swamp-rework' in sys.argv:\n    for entry in json.loads((ROOT/'docs/swamp/registry-additions-0.9.7.json').read_text(encoding='utf-8'))['entries']:\n        known.setdefault(entry['kind'],set()).add(entry['target_id'])\n"+s[a:];s=s.replace("'docs/verification/kurgan-0.9.6/static-data-audit.json' if", "'docs/verification/swamp-0.9.7/static-data-audit.json' if '--swamp-rework' in sys.argv else 'docs/verification/kurgan-0.9.6/static-data-audit.json' if");p.write_text(s,encoding='utf-8')
