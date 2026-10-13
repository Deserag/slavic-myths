package org.slavicmyths.firebird;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** One Overworld encounter, bounded loaded-site search once every five seconds. */
@EventBusSubscriber(modid="slavicmyths")
public final class FirebirdEncounter extends SavedData {
    public long observed=-1,eligible=-1,processed=-1,startDay,lastSeen;
    public UUID event,bird;public BlockPos anchor=BlockPos.ZERO;
    public int planned,issued,remaining;public int[] outcomes={-1,-1,-1};
    public static FirebirdEncounter get(ServerLevel w){return w.getServer().overworld().getDataStorage().computeIfAbsent(new Factory<>(FirebirdEncounter::new,FirebirdEncounter::load),"slavicmyths_firebird");}
    public boolean owns(FirebirdEntity b){return event!=null&&event.equals(b.visit)&&b.getUUID().equals(bird);}
    public static FirebirdEncounter load(CompoundTag n,HolderLookup.Provider p){var d=new FirebirdEncounter();d.observed=Math.max(-1,n.getLong("Observed"));d.eligible=n.contains("Eligible")?n.getLong("Eligible"):-1;d.processed=n.contains("Processed")?n.getLong("Processed"):-1;
        if(n.hasUUID("Event")&&n.hasUUID("Bird")){d.event=n.getUUID("Event");d.bird=n.getUUID("Bird");d.anchor=BlockPos.of(n.getLong("Anchor"));d.startDay=n.getLong("StartDay");d.lastSeen=n.getLong("LastSeen");d.planned=Math.clamp(n.getInt("Planned"),2,3);d.issued=n.getInt("Issued")&7;d.remaining=Math.clamp(n.getInt("Remaining"),0,2000);var a=n.getIntArray("Outcomes");for(int i=0;i<Math.min(3,a.length);i++)d.outcomes[i]=a[i]>=0&&a[i]<3?a[i]:-1;}
        return d;}
    @Override public CompoundTag save(CompoundTag n,HolderLookup.Provider p){n.putInt("Version",1);n.putLong("Observed",observed);n.putLong("Eligible",eligible);n.putLong("Processed",processed);if(event!=null){n.putUUID("Event",event);n.putUUID("Bird",bird);n.putLong("Anchor",anchor.asLong());n.putLong("StartDay",startDay);n.putLong("LastSeen",lastSeen);n.putInt("Planned",planned);n.putInt("Issued",issued);n.putInt("Remaining",remaining);n.putIntArray("Outcomes",outcomes);}return n;}
    public void close(ServerLevel w){if(event==null)return;event=null;bird=null;eligible=FirebirdRules.nextNight(Math.max(observed,Math.floorDiv(w.getDayTime(),24000)),w.random);setDirty();}
    public static BlockPos surface(ServerLevel w,int x,int z){if(!w.hasChunk(x>>4,z>>4))return null;int y=w.getHeight(Heightmap.Types.MOTION_BLOCKING,x,z);var p=new BlockPos(x,y,z);if(y<w.getMinBuildHeight()+1||y>w.getMaxBuildHeight()-29||!w.canSeeSky(p)||!w.getFluidState(p.below()).isEmpty())return null;
        for(int dx=-2;dx<=1;dx++)for(int dz=-2;dz<=1;dz++){var q=p.offset(dx,0,dz);if(!w.hasChunkAt(q)||!w.getBlockState(q.below()).isSolidRender(w,q.below())&& !w.getBlockState(q.below()).is(net.minecraft.tags.BlockTags.LEAVES))return null;for(int dy=0;dy<4;dy++){var t=q.above(dy);if(!w.getBlockState(t).getCollisionShape(w,t).isEmpty()||!w.getFluidState(t).isEmpty())return null;}String id=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(w.getBlockState(q.below()).getBlock()).getPath();if(id.contains("magma")||id.contains("cactus")||id.contains("campfire")||id.contains("planks")||id.contains("slab")||id.contains("stairs"))return null;}return p;}
    @SubscribeEvent public static void tick(LevelTickEvent.Post e){if(!(e.getLevel() instanceof ServerLevel w)||w.dimension()!=Level.OVERWORLD||w.getGameTime()%100!=0)return;var d=get(w);long day=Math.floorDiv(w.getDayTime(),24000);d.observed=Math.max(d.observed,day);if(d.eligible<0)d.eligible=FirebirdRules.nextNight(d.observed,w.random);d.setDirty();
        if(d.event!=null){if(w.getEntity(d.bird) instanceof FirebirdEntity b){d.lastSeen=w.getGameTime();if(!FirebirdRules.night(w.getDayTime()))b.depart();}else if(w.getGameTime()-d.lastSeen>48000||d.observed>d.startDay+2)d.close(w);return;}
        if(!w.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)||day!=d.observed||day<d.eligible||day<=d.processed||!FirebirdRules.window(w.getDayTime()))return;
        var players=w.players().stream().filter(p->p.isAlive()&&!p.isSpectator()&&!p.isCreative()).toList();if(players.isEmpty())return;var observer=players.get(w.random.nextInt(players.size()));
        for(int i=0;i<16;i++){double a=w.random.nextDouble()*Math.PI*2,r=65+w.random.nextDouble()*47;var p=surface(w,(int)Math.floor(observer.getX()+Math.cos(a)*r),(int)Math.floor(observer.getZ()+Math.sin(a)*r));if(p==null)continue;
            var b=FirebirdContent.ENTITY.get().create(w);if(b==null)return;b.moveTo(p.getX()+.5,p.getY()+10+w.random.nextInt(15),p.getZ()+.5,w.random.nextFloat()*360,0);if(!w.noCollision(b))continue;
            d.event=UUID.randomUUID();d.bird=b.getUUID();d.anchor=p;d.startDay=day;d.lastSeen=w.getGameTime();d.planned=2+w.random.nextInt(2);d.issued=0;d.remaining=FirebirdRules.delay(true,w.random);Arrays.fill(d.outcomes,-1);b.visit=d.event;b.anchor=p;d.setDirty();
            if(w.addFreshEntity(b)){d.processed=day;d.setDirty();}else{d.event=null;d.bird=null;d.setDirty();}return;}
    }
}
