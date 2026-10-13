package org.slavicmyths.earth;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import org.slavicmyths.depths.DepthsPlan;

/** Addressed encounter metadata, never scans or loads chunks. Only new explicit Depths jobs install it. */
@EventBusSubscriber(modid="slavicmyths")
public final class EarthArena extends SavedData {
    public enum State {SEALED,AWAKENING,ACTIVE,DORMANT,DEFEATED}
    public static final int[][] PILLAR_OFFSETS={{-10,-10},{10,-10},{-11,8},{11,8},{0,13}};
    public static final class Entry {
        public final UUID id; public final BlockPos center; public final int direction;
        public State state=State.SEALED; public UUID boss; public int spent; public boolean rewardClaimed,gateClosed; public long gateStart;
        public final Set<UUID> retired=new HashSet<>();
        public Entry(UUID id,BlockPos center,int direction){this.id=id;this.center=center.immutable();this.direction=direction;}
        public BlockPos at(int u,int h,int v){int fx=direction==1?1:direction==3?-1:0,fz=direction==0?-1:direction==2?1:0;return center.offset(u*fx-v*fz,h,u*fz+v*fx);}
        public BlockPos pillar(int i){return at(PILLAR_OFFSETS[i][0],0,PILLAR_OFFSETS[i][1]);}
        public AABB area(){return new AABB(center).inflate(19,9,19);}
        public AABB gateArea(){return new AABB(at(-14,0,-2).getCenter(),at(-14,7,2).getCenter()).inflate(.5,.05,.5);}
        public boolean contains(Vec3 p){
            int fx=direction==1?1:direction==3?-1:0,fz=direction==0?-1:direction==2?1:0;
            double u=(p.x-center.getX()-.5)*fx+(p.z-center.getZ()-.5)*fz;
            return u> -13.5&&area().contains(p)&&p.y>=center.getY()-.2&&p.y<=center.getY()+8;
        }
        CompoundTag save(){var n=new CompoundTag();n.putUUID("Arena",id);n.putLong("Center",center.asLong());n.putInt("Direction",direction);n.putString("State",state.name());if(boss!=null)n.putUUID("Boss",boss);n.putInt("Spent",spent);n.putBoolean("RewardClaimed",rewardClaimed);n.putBoolean("GateClosed",gateClosed);n.putLong("GateStart",gateStart);var dead=new ListTag();for(var id:retired)dead.add(NbtUtils.createUUID(id));n.put("Retired",dead);return n;}
        static Entry load(CompoundTag n){var e=new Entry(n.getUUID("Arena"),BlockPos.of(n.getLong("Center")),n.getInt("Direction"));e.state=State.valueOf(n.getString("State"));e.boss=n.hasUUID("Boss")?n.getUUID("Boss"):null;e.spent=n.getInt("Spent")&31;e.rewardClaimed=n.getBoolean("RewardClaimed");e.gateClosed=n.getBoolean("GateClosed");e.gateStart=n.getLong("GateStart");for(var id:n.getList("Retired",Tag.TAG_INT_ARRAY))e.retired.add(NbtUtils.loadUUID(id));return e;}
    }
    public final Map<UUID,Entry> entries=new LinkedHashMap<>();
    public static EarthArena get(ServerLevel w){return w.getDataStorage().computeIfAbsent(new SavedData.Factory<>(EarthArena::new,EarthArena::load),"slavicmyths_earth_encounters");}
    private static EarthArena load(CompoundTag n,HolderLookup.Provider r){var d=new EarthArena();for(var t:n.getList("Encounters",Tag.TAG_COMPOUND)){var e=Entry.load((CompoundTag)t);d.entries.put(e.id,e);}return d;}
    @Override public CompoundTag save(CompoundTag n,HolderLookup.Provider r){var list=new ListTag();entries.values().forEach(e->list.add(e.save()));n.put("Encounters",list);return n;}
    public static void install(ServerLevel w,DepthsPlan p){
        var d=get(w);if(d.entries.containsKey(p.id()))return;var c=p.bossAnchor();var e=new Entry(p.id(),new BlockPos(c.x(),c.y(),c.z()),p.direction());
        // All occupied cells must still be empty. No retrofit, terrain search or chest replacement.
        var edits=new LinkedHashMap<BlockPos,net.minecraft.world.level.block.state.BlockState>();
        for(int u=-21;u<=-15;u++)for(int v=-5;v<=5;v++)for(int h=0;h<=7;h++)if(Math.abs(v)==5||h==7)edits.put(e.at(u,h,v),Blocks.DEEPSLATE_BRICKS.defaultBlockState());
        for(int u:new int[]{-20,-16})for(int v:new int[]{-4,4}){edits.put(e.at(u,0,v),Blocks.CHISELED_DEEPSLATE.defaultBlockState());edits.put(e.at(u,1,v),Blocks.LANTERN.defaultBlockState());}
        for(int v:new int[]{-3,3})for(int h=0;h<8;h++)edits.put(e.at(-14,h,v),EarthContent.SHELTER.get().defaultBlockState());
        for(int v=-3;v<=3;v++)edits.put(e.at(-14,7,v),EarthContent.SHELTER.get().defaultBlockState());
        for(int i=0;i<5;i++)for(int x=0;x<2;x++)for(int z=0;z<2;z++)for(int y=0;y<6;y++)edits.put(e.pillar(i).offset(x,y,z),EarthContent.PILLAR.get().defaultBlockState());
        for(int[] offset:new int[][]{{-10,1},{8,11}})for(int u=0;u<3;u++)for(int v=0;v<2;v++)for(int h=0;h<4;h++)edits.put(e.at(offset[0]+u,h,offset[1]+v),EarthContent.SHELTER.get().defaultBlockState());
        for(int u=-1;u<=1;u++)for(int v=-1;v<=1;v++)edits.put(e.at(u,-1,v),Blocks.CHISELED_DEEPSLATE.defaultBlockState());
        edits.put(e.center,EarthContent.SEAL.get().defaultBlockState());
        for(var edit:edits.entrySet()){var at=edit.getKey();if(!w.hasChunkAt(at)||w.getBlockEntity(at)!=null)return;var old=w.getBlockState(at);if(at.getY()>=e.center.getY()&&!old.isAir())return;}
        for(var edit:edits.entrySet())if(!w.setBlock(edit.getKey(),edit.getValue(),3))throw new IllegalStateException("earth arena placement rejected");
        d.entries.put(e.id,e);d.setDirty();
    }
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST) public static void seal(BlockEvent.BreakEvent event){
        if(!event.getState().is(EarthContent.SEAL.get())||!(event.getLevel() instanceof ServerLevel w))return;
        activate(w,event.getPos(),event.getPlayer());event.setCanceled(true);
    }
    public static void activate(ServerLevel w,BlockPos pos,net.minecraft.world.entity.player.Player player){
        if(player.isSpectator()||!w.mayInteract(player,pos)||!w.getBlockState(pos).is(EarthContent.SEAL.get()))return;
        var d=get(w);var e=d.entries.values().stream().filter(a->a.center.equals(pos)).findFirst().orElse(null);
        if(e==null){
            var boss=EarthContent.BOSS.get().create(w);if(boss==null)return;
            boss.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,player.getYRot()+180,0);
            var box=boss.getBoundingBox();
            for(var at:BlockPos.betweenClosed(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ))){
                if(at.equals(pos))continue;
                if(!w.hasChunkAt(at)||!w.getBlockState(at).getCollisionShape(w,at).isEmpty()){
                    player.displayClientMessage(net.minecraft.network.chat.Component.translatable("earth.slavicmyths.block_needs_space"),true);return;
                }
            }
            // A player-placed stone summons outside an arena; victory still awards loot.
            boss.awaken();if(!w.addFreshEntity(boss))return;
            w.setBlock(pos,Blocks.AIR.defaultBlockState(),3);cue(w,pos,"seal_break",.9F);return;
        }
        if(e.state!=State.SEALED)return;
        e.state=State.AWAKENING;e.boss=UUID.nameUUIDFromBytes((e.id+":earth_boss").getBytes(java.nio.charset.StandardCharsets.UTF_8));d.setDirty();
        if(w.getEntity(e.boss)!=null)return;var boss=EarthContent.BOSS.get().create(w);if(boss==null){e.state=State.SEALED;d.setDirty();return;}
        boss.setUUID(e.boss);boss.moveTo(e.center.getX()+.5,e.center.getY(),e.center.getZ()+.5,0,0);boss.bind(e.id,e.center);boss.addParticipant(player);boss.awaken();
        if(!w.addFreshEntity(boss)){e.state=State.SEALED;e.boss=null;d.setDirty();return;}
        w.setBlock(e.center,EarthContent.SEAL.get().defaultBlockState().setValue(EarthSeal.CRACK,1),3);cue(w,e.center,"seal_break",.9F);
        // Address only vanilla ordinary hostiles in this encounter, preserving all mod NPCs/story mobs.
        for(var mob:w.getEntitiesOfClass(Monster.class,e.area(),m->m!=boss&&net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(m.getType()).getNamespace().equals("minecraft")&&!m.hasCustomName()&&!m.isPersistenceRequired()))mob.discard();
    }
    @SubscribeEvent public static void spawning(MobSpawnEvent.PositionCheck e){if(!(e.getLevel().getLevel() instanceof ServerLevel w)||e.getSpawnType()!=MobSpawnType.NATURAL)return;for(var a:get(w).entries.values())if((a.state==State.ACTIVE||a.state==State.AWAKENING)&&a.contains(e.getEntity().position())){e.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);return;}}
    public static void cue(ServerLevel w,BlockPos at,String cue,float volume){w.playSound(null,at,EarthContent.sound(cue),cue.equals("gate_close")||cue.equals("seal_break")?SoundSource.BLOCKS:SoundSource.HOSTILE,volume,1);}
    public static void open(ServerLevel w,Entry e){for(int v=-2;v<=2;v++)for(int h=0;h<7;h++){var p=e.at(-14,h,v);if(w.hasChunkAt(p)&&w.getBlockState(p).is(EarthContent.GATE.get()))w.setBlock(p,Blocks.AIR.defaultBlockState(),3);}e.gateClosed=false;e.gateStart=0;get(w).setDirty();}
    public static void gateTick(ServerLevel w,Entry e){if(e.gateClosed)return;if(e.gateStart==0){e.gateStart=w.getGameTime();cue(w,e.at(-14,0,0),"gate_close",.75F);EarthActor.gate(w,e);get(w).setDirty();}if(w.getGameTime()-e.gateStart<EarthRules.GATE)return;
        if(!w.getEntitiesOfClass(LivingEntity.class,e.gateArea(),l->l.isAlive()&&!l.isSpectator()).isEmpty())return;
        for(int v=-2;v<=2;v++)for(int h=0;h<7;h++){var p=e.at(-14,h,v);if(!w.hasChunkAt(p)||!w.getBlockState(p).isAir())return;}
        for(int v=-2;v<=2;v++)for(int h=0;h<7;h++)w.setBlock(e.at(-14,h,v),EarthContent.GATE.get().defaultBlockState(),3);
        e.gateClosed=true;get(w).setDirty();
    }
    public static boolean available(ServerLevel w,Entry e,int index){if(index<0||(e.spent&(1<<index))!=0)return false;for(int x=0;x<2;x++)for(int z=0;z<2;z++)for(int y=1;y<6;y++){var p=e.pillar(index).offset(x,y,z);if(!w.hasChunkAt(p)||!w.getBlockState(p).is(EarthContent.PILLAR.get()))return false;}return true;}
    public static boolean consume(ServerLevel w,Entry e,int i){if(!available(w,e,i))return false;e.spent|=1<<i;get(w).setDirty();for(int x=0;x<2;x++)for(int z=0;z<2;z++)for(int y=1;y<6;y++)w.setBlock(e.pillar(i).offset(x,y,z),Blocks.AIR.defaultBlockState(),3);return true;}
    public static void defeat(ServerLevel w,Entry e,boolean legitimate){
        if(e.state==State.DEFEATED)return;
        e.state=State.DEFEATED;open(w,e);
        if(!e.rewardClaimed){
            // A finite local search avoids dropping the one reward inside a pillar or altered floor.
            Vec3 at=e.center.getCenter().add(0,1,0);
            if(legitimate)outer:for(int h=0;h<=3;h++)for(int u=-3;u<=3;u++)for(int v=-3;v<=3;v++){
                BlockPos p=e.at(u,h,v);
                if(w.hasChunkAt(p)&&w.getBlockState(p.below()).isCollisionShapeFullBlock(w,p.below())&&w.noCollision(new AABB(p.getCenter(),p.getCenter()).inflate(.25))){at=p.getCenter();break outer;}
            }
            if(legitimate){
                var item=new ItemEntity(w,at.x,at.y,at.z,new ItemStack(EarthContent.RUNE.get()));
                item.setDefaultPickUpDelay();
                if(w.addFreshEntity(item)){e.rewardClaimed=true;cue(w,e.center,"rune_reward",.7F);}
            }else e.rewardClaimed=true;
            get(w).setDirty();
        }
    }
}
