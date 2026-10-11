package org.slavicmyths.industry;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.slavicmyths.chud.*;

/** Finite optional contents, after final rasterization, only for newly-created pieces. No old-chunk retrofit. */
public final class ChudContents {
    record Site(String key,int x,int y,int z,int xmin,int xmax,int zmin,int zmax,String content,int role,boolean rare){}
    private static Site site(String key,int x,int y,int z,int xmin,int xmax,int zmin,int zmax,String content){return new Site(key,x,y,z,xmin,xmax,zmin,zmax,content,-1,false);}
    private static Site npc(String key,int x,int y,int z,int xmin,int xmax,int zmin,int zmax,int role,boolean rare){return new Site(key,x,y,z,xmin,xmax,zmin,zmax,"npc",role,rare);}
    public static void city(WorldGenLevel world,ChudPlan p,AbandonedPlan b,BoundingBox clip){
        var sites=new ArrayList<Site>();var forge=p.halls.stream().filter(h->h.role()==ChudPlan.Role.FORGE).findFirst().orElse(null);var store=p.halls.stream().filter(h->h.role()==ChudPlan.Role.STORE).findFirst().orElse(null);
        if(forge!=null){int x=forge.x(),y=forge.y()+1,z=forge.z(),rx=forge.width()/2-3,rz=forge.depth()/2-3;
            sites.add(site("bloomery",x-18,y,z-17,x-rx,x+rx,z-rz,z+rz,"chud_bloomery"));sites.add(site("hearth",x+15,y,z-17,x-rx,x+rx,z-rz,z+rz,"refining_hearth"));
            sites.add(site("workshop",x+20,y,z+7,x-rx,x+rx,z-rz,z+rz,"chud_workshop"));
            sites.add(npc("miner",x-15,y,z-15,x-rx,x+rx,z-rz,z+rz,2,false));sites.add(npc("artisan",x+12,y,z-15,x-rx,x+rx,z-rz,z+rz,1,false));
        }
        if(store!=null){int x=store.x(),z=store.z(),rx=store.width()/2-3,rz=store.depth()/2-3;for(int i=0;i<2;i++)sites.add(site("store_"+i,x+(i==0?-rx+2:rx-2),store.y()+1,z+rz-4,x-rx,x+rx,z-rz,z+rz,"chud_storehouse"));}
        int residents=p.variant==2?5:4;
        for(int i=0;i<Math.min(residents,p.homes.size());i++){var h=p.homes.get(i);sites.add(npc("resident_"+i,h.x()+h.width()-3,h.y()+1,h.z()+h.depth()-4,h.x()+2,h.x()+h.width()-3,h.z()+2,h.z()+h.depth()-3,0,i==0));}
        for(int i=1;i<Math.min(8,p.homes.size());i+=2){var h=p.homes.get(i);sites.add(site("home_"+i,h.x()+2,h.y()+1,h.z()+h.depth()-4,h.x()+2,h.x()+h.width()-3,h.z()+2,h.z()+h.depth()-3,"chud_residential"));}
        for(int i=0;i<2;i++){var h=p.halls.get(i==0?0:5);int rx=h.width()/2-3,rz=h.depth()/2-3;sites.add(npc("guard_"+i,h.x()+rx-3,h.y()+1,h.z()+rz-4,h.x()-rx,h.x()+rx,h.z()-rz,h.z()+rz,3,false));}
        place(world,clip,p.seed,p.cityId(),p::world,p.blockRotation(),sites,(local)->p.connectors.stream().noneMatch(c->c.floors().stream().anyMatch(f->Math.abs(f.getY()+1-local.getY())<=2&&Math.abs(f.getX()-local.getX())<=c.width()/2+2&&Math.abs(f.getZ()-local.getZ())<=c.width()/2+2)));
        if(b!=null)abandoned(world,b,clip);
    }
    public static void abandoned(WorldGenLevel world,AbandonedPlan p,BoundingBox clip){
        var sites=new ArrayList<Site>();boolean attached=p.kind==AbandonedPlan.Kind.ATTACHED_SMALL;int living=attached?1:Math.min(3,p.ruinedHomes.size());
        for(int i=0;i<living;i++){var h=p.ruinedHomes.get(i);sites.add(site("living_"+i,h.x()+h.width()-3,h.y()+1,h.z()+h.depth()-4,h.x()+2,h.x()+h.width()-3,h.z()+2,h.z()+h.depth()-3,"abandoned_living"));}
        var gallery=p.chambers.stream().filter(c->c.role()==AbandonedPlan.Role.GALLERY).findFirst().orElse(null);
        if(gallery!=null){int rx=gallery.width()/2-3,rz=gallery.depth()/2-3;for(int i=0;i<(attached?1:p.kind==AbandonedPlan.Kind.INDEPENDENT_GREAT?3:2);i++)sites.add(site("mine_"+i,gallery.x()+rx-3,gallery.y()+1,gallery.z()-rz+4+i*9,gallery.x()-rx,gallery.x()+rx,gallery.z()-rz,gallery.z()+rz,"abandoned_mine"));}
        var industrial=p.chambers.stream().filter(c->c.module()==AbandonedPlan.Module.TOOL_ROOM||c.module()==AbandonedPlan.Module.ORE_SORTING).findFirst().orElse(null);
        var random=new Random(p.seed^0x494e445553545259L);
        if(industrial!=null){var h=industrial;int rx=h.width()/2-1,rz=h.depth()/2-1;if(random.nextDouble()<(attached?.20:.35))sites.add(site("old_bloomery",h.x()-rx+1,h.y()+1,h.z()-rz+2,h.x()-rx,h.x()+rx,h.z()-rz,h.z()+rz,"chud_bloomery"));if(random.nextDouble()<(attached?.10:.20))sites.add(site("old_hearth",h.x()+rx-1,h.y()+1,h.z()-rz+2,h.x()-rx,h.x()+rx,h.z()-rz,h.z()+rz,"refining_hearth"));}
        if(!attached){var storage=p.chambers.stream().filter(c->c.module()==AbandonedPlan.Module.ABANDONED_STORAGE).min(Comparator.comparingInt(AbandonedPlan.Chamber::y)).orElse(null);if(storage!=null){int rx=storage.width()/2-1,rz=storage.depth()/2-1;sites.add(site("treasury",storage.x()-rx+1,storage.y()+1,storage.z()-rz+2,storage.x()-rx,storage.x()+rx,storage.z()-rz,storage.z()+rz,"abandoned_treasury"));}}
        place(world,clip,p.seed,p.rootId+":"+p.kind,p::world,p.blockRotation(),sites,local->p.routes.stream().noneMatch(c->c.floors().stream().anyMatch(f->Math.abs(f.getY()+1-local.getY())<=2&&Math.abs(f.getX()-local.getX())<=c.width()/2+2&&Math.abs(f.getZ()-local.getZ())<=c.width()/2+2)));
    }
    private static void place(WorldGenLevel world,BoundingBox clip,long seed,String root,Function<BlockPos,BlockPos> transform,Rotation rotation,List<Site> sites,java.util.function.Predicate<BlockPos> awayFromPath){
        for(var s:sites){BlockPos base=transform.apply(new BlockPos(s.x,s.y,s.z));if(!clip.isInside(base))continue;boolean done=false;
            for(int radius=0;radius<=3&&!done;radius++)for(int dx=-radius;dx<=radius&&!done;dx++)for(int dz=-radius;dz<=radius&&!done;dz++){
                if(Math.max(Math.abs(dx),Math.abs(dz))!=radius)continue;int x=s.x+dx,z=s.z+dz;if(x<s.xmin||x>s.xmax||z<s.zmin||z>s.zmax)continue;
                var local=new BlockPos(x,s.y,z);if(!awayFromPath.test(local))continue;var at=transform.apply(local);
                if(!clip.isInside(at)||!clip.isInside(at.offset(-1,0,-1))||!clip.isInside(at.offset(1,0,1)))continue;
                if(world.getBlockEntity(at)!=null||!world.getBlockState(at).isAir()||!world.getBlockState(at.above()).isAir()||!world.getBlockState(at.above(2)).isAir()||!world.getBlockState(at.below()).isCollisionShapeFullBlock(world,at.below()))continue;
                Direction facing=null;for(var d:Direction.Plane.HORIZONTAL)if(world.getBlockState(at.relative(d)).isAir()&&world.getBlockState(at.relative(d).above()).isAir()&&world.getBlockState(at.relative(d).below()).isCollisionShapeFullBlock(world,at.relative(d).below())){facing=d;break;}if(facing==null)continue;
                if(s.role>=0){
                    boolean floorSafe=true;for(int a=-1;a<=1;a++)for(int b=-1;b<=1;b++)if(!world.getBlockState(at.offset(a,-1,b)).isCollisionShapeFullBlock(world,at.offset(a,-1,b)))floorSafe=false;if(!floorSafe)continue;
                    var type=s.role==3?ChudPeople.GUARD.get():s.role==0?ChudPeople.RESIDENT.get():ChudPeople.ARTISAN.get();var mob=type.create(world.getLevel());if(mob==null)continue;
                    UUID id=UUID.nameUUIDFromBytes((root+":"+s.key).getBytes(StandardCharsets.UTF_8));if(world instanceof net.minecraft.server.level.ServerLevel server&&server.getEntity(id)!=null){done=true;continue;}
                    mob.getPersistentData().putString("ChudCity",root);mob.setUUID(id);mob.setup(s.role,at,s.rare);mob.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,rotation.ordinal()*90,0);if(!world.noCollision(mob))continue;
                    if(s.role==3){mob.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_SWORD));mob.setDropChance(EquipmentSlot.MAINHAND,0);}done=world.addFreshEntity(mob);
                }else if(s.content.equals("chud_bloomery")||s.content.equals("refining_hearth")){
                    var block=s.content.equals("chud_bloomery")?Industry.BLOOMERY.get():Industry.HEARTH.get();done=world.setBlock(at,block.defaultBlockState().setValue(MachineBlock.FACING,facing),2);
                }else{
                    boolean adjacentChest=false;for(var d:Direction.Plane.HORIZONTAL)if(world.getBlockState(at.relative(d)).is(Blocks.CHEST))adjacentChest=true;if(adjacentChest)continue;
                    if(world.setBlock(at,Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,facing),2)&&world.getBlockEntity(at) instanceof ChestBlockEntity chest){chest.setLootTable(ResourceKey.create(Registries.LOOT_TABLE,Industry.id("chests/"+s.content)),seed^at.asLong()^s.content.hashCode());chest.getPersistentData().putString("ChudContent",root+":"+s.key);chest.setChanged();done=true;}
                }
            }
            if(!done)com.mojang.logging.LogUtils.getLogger().debug("Chud optional content skipped {} {}",root,s.key);
        }
    }
    private ChudContents(){}
}
