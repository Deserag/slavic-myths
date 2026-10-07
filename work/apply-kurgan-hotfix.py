from pathlib import Path
p=Path('src/main/java/org/slavicmyths/kurgan/KurganEncounters.java')
s=p.read_text(encoding='utf-8-sig')
a=s.index('    private static void trigger(');b=s.index('    public static BlockPos safePosition',a)
s=s[:a]+'''    public static void trigger(ServerLevel world,BurialRecords data,KurganInstance i,KurganPlan.Room room,ServerPlayer player){
        if(room.hall()&&(!i.sealOpened||i.bossDefeated))return;
        KurganEncounterState state=i.encounters.computeIfAbsent(room.id,id->new KurganEncounterState());
        if(state.cleared)return;
        List<Kind> roster=roster(i,room);if(roster.isEmpty())return;
        // Older active rooms retain their original UUID roster, never silently reroll it.
        if(state.triggered&&state.slots.isEmpty())return;
        state.prepare(roster);boolean changed=false;
        for(var slot:state.slots){
            if(slot.defeated)continue;
            if(slot.uuid!=null){
                Entity existing=world.getEntity(slot.uuid);
                if(existing!=null){if(existing.isAlive())slot.last=existing.blockPosition();continue;}
                // FULL blocks do not imply entities have loaded. Never force-load to inspect a UUID.
                if(slot.restored||slot.last==null||!world.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(slot.last)))continue;
            }
            KurganCreature mob=type(slot.kind).create(world);if(mob==null)throw new IllegalStateException("Missing kurgan entity "+slot.kind);
            mob.kurgan=i.id;mob.room=room.id;mob.home=i.origin.offset(room.x,room.y+1,room.z);
            BlockPos spot=encounterPosition(mob,room,player);if(spot==null)continue;
            mob.moveTo(spot.getX()+.5,spot.getY(),spot.getZ()+.5,0,0);mob.setPersistenceRequired();mob.setTarget(player);
            if(!world.addFreshEntity(mob))continue;
            if(slot.uuid!=null){state.alive.remove(slot.uuid);state.retired.add(slot.uuid);slot.restored=true;}
            slot.uuid=mob.getUUID();slot.last=spot;state.alive.add(slot.uuid);state.start();changed=true;
        }
        if(changed)data.setDirty();
    }
    private static BlockPos encounterPosition(KurganCreature mob,KurganPlan.Room room,ServerPlayer player){
        for(int distance:new int[]{3,1})for(int dx=-room.rx+1;dx<room.rx;dx++)for(int dz=-room.rz+1;dz<room.rz;dz++){
            BlockPos desired=mob.home.offset(dx,0,dz);if(desired.distSqr(player.blockPosition())<distance*distance)continue;
            BlockPos p=safePosition(mob,Vec3.atBottomCenterOf(desired),false);
            if(p!=null&&p.distSqr(player.blockPosition())>=distance*distance&&player.hasLineOfSight(new Vec3(p.getX()+.5,p.getY()+1,p.getZ()+.5)))return p;
        }return null;
    }
    @SubscribeEvent public static void leaving(net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent e){
        if(!(e.getEntity() instanceof KurganCreature mob)||!(e.getLevel() instanceof ServerLevel world)||mob.kurgan==null)return;
        var data=BurialRecords.get(world);var instance=data.instances.get(mob.kurgan);if(instance==null)return;
        var state=instance.encounters.get(mob.room);if(state==null)return;
        for(var slot:state.slots)if(mob.getUUID().equals(slot.uuid)){slot.last=mob.blockPosition();data.setDirty();}
    }
    @SubscribeEvent public static void joining(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent e){
        if(!(e.getEntity() instanceof KurganCreature mob)||!(e.getLevel() instanceof ServerLevel world)||mob.kurgan==null)return;
        var instance=BurialRecords.get(world).instances.get(mob.kurgan);if(instance==null)return;
        var state=instance.encounters.get(mob.room);if(state!=null&&state.retired.contains(mob.getUUID()))e.setCanceled(true);
    }
''' + s[b:]
s=s.replace('player.hasLineOfSight(new Vec3(p.getX()+.5,p.getY()+1,p.getZ()+.5))','((ServerLevel)mob.level()).clip(new ClipContext(player.getEyePosition(),new Vec3(p.getX()+.5,p.getY()+1,p.getZ()+.5),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player)).getType()==HitResult.Type.MISS')
s=s.replace('if(!world.getBlockState(p.below()).isFaceSturdy', 'if(world.getBlockState(p).getBlock() instanceof net.minecraft.world.level.block.BasePressurePlateBlock||world.getBlockState(p.below()).is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK))continue;\n            if(!world.getBlockState(p.below()).isFaceSturdy')
s=s.replace('if(room!=null&&(!room.hall()||i.sealOpened))trigger(world,data,i,room,player);', '''if(room!=null&&(!room.hall()||i.sealOpened))trigger(world,data,i,room,player);
        else for(var nearby:i.plan.rooms)if(!nearby.hall()&&player.blockPosition().distSqr(i.origin.offset(nearby.x,nearby.y+1,nearby.z))<=64)trigger(world,data,i,nearby,player);''')
p.write_text(s,encoding='utf-8')
