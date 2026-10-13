package org.slavicmyths.village;

import java.util.*;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.*;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.entity.animal.*;
import org.slavicmyths.husbandry.*;
import net.minecraft.tags.FluidTags;

/** Native WORK behavior; MoveToTargetSink owns all movement. No replacement Brain or tick event. */
public final class VillageWork extends Behavior<Villager> {
    private BlockPos anchor,target,approach,lastReachable;private String task;private Animal animal;private long began,fishingSince;private WalkTarget ownedWalk;
    public VillageWork(){super(ImmutableMap.of(MemoryModuleType.JOB_SITE,MemoryStatus.VALUE_PRESENT,MemoryModuleType.WALK_TARGET,MemoryStatus.VALUE_ABSENT),WorkLimits.TRAVEL_TIMEOUT+120);}
    public static WorkState state(Villager v){return v.getData(VillageRoles.WORK_STATE);}
    public static boolean eligible(ServerLevel level,Villager v){return v.isAlive()&&!v.isBaby()&&!v.isTrading()&&!v.isSleeping()&&v.getVillagerData().getProfession()!=VillagerProfession.NONE&&v.getVillagerData().getProfession()!=VillagerProfession.NITWIT
        &&v.getBrain().isActive(Activity.WORK)&&!v.getBrain().isActive(Activity.PANIC)&&!v.getBrain().isActive(Activity.RAID)&&!v.getBrain().isActive(Activity.PRE_RAID)&&!v.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_HOSTILE);}
    private static boolean grief(ServerLevel level,Villager v){return net.neoforged.neoforge.event.EventHooks.canEntityGrief(level,v);}
    private static boolean close(Villager v,BlockPos p){if(v.distanceToSqr(p.getCenter())>WorkLimits.INTERACTION_DISTANCE_SQUARED)return false;
        var hit=v.level().clip(new net.minecraft.world.level.ClipContext(v.getEyePosition(),p.getCenter(),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,v));
        return hit.getType()==net.minecraft.world.phys.HitResult.Type.MISS||hit.getBlockPos().equals(p);
    }
    private boolean reachable(Villager v,BlockPos p){state(v).pathRequests++;var path=v.getNavigation().createPath(p,v.level().getBlockState(p).getBlock() instanceof CropBlock?0:1);boolean valid=path!=null&&path.canReach()&&path.getEndNode()!=null;if(valid){lastReachable=p;approach=path.getEndNode().asBlockPos();}return valid;}
    private boolean validJob(ServerLevel level,Villager v){if(!level.hasChunkAt(anchor))return false;var poi=level.getPoiManager().getType(anchor);return poi.isPresent()&&v.getVillagerData().getProfession().heldJobSite().test(poi.get());}
    @Override protected boolean checkExtraStartConditions(ServerLevel level,Villager v){
        WorkState s=state(v);long now=level.getGameTime();if(org.slavicmyths.military.Military.guard(v))return false;if(!eligible(level,v)){s.failure="OUTSIDE_WORK_TIME";return false;}if(now<s.nextAttempt)return false;
        var wanted=v.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);if(wanted.isPresent()&&wanted.get().distanceToSqr(v)<16&&v.wantsToPickUp(wanted.get().getItem()))return false;
        s.nextAttempt=now+WorkLimits.ACTION_COOLDOWN+Math.floorMod(v.getUUID().hashCode(),40);animal=null;var job=v.getBrain().getMemory(MemoryModuleType.JOB_SITE).orElse(null);
        if(job==null||!job.dimension().equals(level.dimension())){s.failure="NO_WORKSTATION";return false;}anchor=job.pos();if(!validJob(level,v)){s.failure="NO_WORKSTATION";return false;}
        s.workstationReachable=close(v,anchor)||reachable(v,anchor);if(!s.workstationReachable){s.failure="WORKSTATION_UNREACHABLE";return false;}
        if(s.storage!=null&&(s.storage.distSqr(anchor)>WorkLimits.STORAGE_RADIUS*WorkLimits.STORAGE_RADIUS||!level.hasChunkAt(s.storage)||!(level.getBlockEntity(s.storage) instanceof SettlementChest)))s.storage=null;
        // Deposit real surplus first. Farmer keeps a planting reserve in every seed stack.
        boolean cargo=false;for(int i=0;i<v.getInventory().getContainerSize();i++)if(depositCount(v,i)>0){cargo=true;break;}
        if(cargo){if(chooseStorage(level,v,true))return choose(s.storage,"DEPOSIT",v);if(s.failure.equals("STORAGE_FULL"))return false;}
        String role=WorkPolicy.role(v);
        s.required="tag="+WorkPolicy.tag("inputs",role).location();s.available=v.getInventory().getItems().toString();
        var tile=level.getBlockEntity(anchor);s.pending=tile instanceof org.slavicmyths.kitchen.KitchenTile k?k.pending+" potServings="+k.servings:tile instanceof org.slavicmyths.brewing.BrewTile b?b.pending+" progress="+b.progress+"/"+b.total:tile instanceof org.slavicmyths.textile.TextileStation t?t.npcOutput().toString():"none";
        if(role.equals("farmer")){
            if(!grief(level,v)){s.failure="MOB_GRIEFING_DISABLED";return false;}
            BlockPos crop=findBlock(level,v,WorkLimits.FIELD_RADIUS,(p,b)->b.getBlock() instanceof CropBlock c&&c.isMaxAge(b));
            if(crop!=null&&v.distanceToSqr(crop.getCenter())>=1)return choose(crop,"FARM_APPROACH",v);
            // Native HarvestFarmland does harvest/replant/pickup and food sharing remains untouched.
            s.task="NATIVE_FARMING";return false;
        }
        if(role.equals("fisherman")&&now>=s.nextFishing&&findItem(v,Items.FISHING_ROD)>=0){
            BlockPos shore=findBlock(level,v,WorkLimits.ANIMAL_RADIUS,(p,b)->fishingSpot(level,p));
            if(shore!=null)return choose(shore,"FISH",v);s.failure="NO_VALID_TARGET";
        }
        if(role.equals("shepherd")&&findItem(v,Items.SHEARS)>=0&&grief(level,v)){
            s.searches++;for(Sheep sheep:level.getEntitiesOfClass(Sheep.class,new net.minecraft.world.phys.AABB(anchor).inflate(WorkLimits.ANIMAL_RADIUS),Sheep::readyForShearing))if(reachable(v,sheep.blockPosition())){animal=sheep;return choose(sheep.blockPosition(),"SHEAR",v);}
        }
        if(role.equals("herder")&&grief(level,v)){
            s.searches++;var animals=level.getEntitiesOfClass(Animal.class,new net.minecraft.world.phys.AABB(anchor).inflate(WorkLimits.ANIMAL_RADIUS),a->a instanceof YardAnimal||a instanceof Cow||a instanceof Pig||a instanceof Chicken);
            for(Animal a:animals)if((a instanceof YardAnimal y&&y.eggReady()||(a.isBaby()||a.getHealth()<a.getMaxHealth())&&v.getInventory().getItems().stream().anyMatch(a::isFood))&&reachable(v,a.blockPosition())){animal=a;return choose(a.blockPosition(),"TEND",v);}
            // No breeding is initiated: native feeder care avoids an unbounded population entirely.
        }
        if(role.equals("hunter")&&grief(level,v)&&v.getInventory().getItems().stream().noneMatch(x->WorkPolicy.input(v,x))){
            BlockPos berry=findBlock(level,v,WorkLimits.AUXILIARY_RADIUS,(p,b)->b.getBlock() instanceof org.slavicmyths.garden.PerennialBush&&b.getValue(org.slavicmyths.garden.PerennialBush.PHASE)==4&&b.getValue(org.slavicmyths.garden.PerennialBush.HALF)==net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER);
            if(berry!=null)return choose(berry,"GATHER",v);
        }
        if(needsInput(v)&&chooseStorage(level,v,false))return choose(s.storage,"EXTRACT",v);
        if(role.equals("weaver")){
            s.textileStations.removeIf(p->!level.hasChunkAt(p)||p.distSqr(anchor)>WorkLimits.AUXILIARY_RADIUS*WorkLimits.AUXILIARY_RADIUS||!(level.getBlockEntity(p) instanceof org.slavicmyths.textile.TextileStation));
            BlockTest usable=(p,b)->level.getBlockEntity(p) instanceof org.slavicmyths.textile.TextileStation t&&(t.hasFlaxToBreak()||!t.npcOutput().isEmpty()||v.getInventory().getItems().stream().anyMatch(t::accepts));
            for(BlockPos p:s.textileStations)if(usable.test(p,level.getBlockState(p))&&reachable(v,p))return choose(p,"WORKSTATION",v);
            BlockPos station=findBlock(level,v,WorkLimits.AUXILIARY_RADIUS,usable);
            if(station!=null){if(s.textileStations.size()<8)s.textileStations.add(station);return choose(station,"WORKSTATION",v);}
        }
        if(role.equals("fisherman")||role.equals("shepherd"))return false;
        return choose(anchor,"WORKSTATION",v);
    }
    private static boolean needsInput(Villager v){String r=WorkPolicy.role(v);if(r.equals("fisherman"))return findItem(v,Items.FISHING_ROD)<0;if(r.equals("shepherd"))return findItem(v,Items.SHEARS)<0;
        return state(v).failure.equals("NO_INPUT")||v.getInventory().getItems().stream().noneMatch(x->WorkPolicy.input(v,x));}
    private static int findItem(Villager v,Item item){for(int i=0;i<v.getInventory().getContainerSize();i++)if(v.getInventory().getItem(i).is(item))return i;return -1;}
    private boolean choose(BlockPos p,String action,Villager v){target=p;task=action;if(!p.equals(lastReachable)&&!close(v,p)&&!reachable(v,p))return false;state(v).target=p;state(v).task=action;return true;}
    private boolean chooseStorage(ServerLevel level,Villager v,boolean deposit){WorkState s=state(v);long now=level.getGameTime();
        if(s.storage!=null&&suitable(level,v,s.storage,deposit)){s.storageReachable=reachable(v,s.storage);if(s.storageReachable)return true;s.storage=null;}
        if(now<s.nextSearch)return false;s.nextSearch=now+WorkLimits.SEARCH_COOLDOWN;s.searches++;
        var candidates=level.getPoiManager().getInRange(h->h.is(SettlementStorage.POI.getKey()),anchor,WorkLimits.STORAGE_RADIUS,PoiManager.Occupancy.ANY).map(r->r.getPos()).filter(level::hasChunkAt).sorted(Comparator.comparingDouble(p->p.distSqr(v.blockPosition()))).limit(8).toList();
        boolean suitable=false;BlockPos full=null;for(BlockPos p:candidates){if(suitable(level,v,p,deposit)){suitable=true;if(reachable(v,p)){s.storage=p;s.storageReachable=true;return true;}}else if(deposit&&full==null&&level.getBlockEntity(p) instanceof SettlementChest chest&&chest.isFull()&&reachable(v,p))full=p;}
        s.storage=full;s.storageReachable=full!=null;s.failure=candidates.isEmpty()?"NO_STORAGE":full!=null?"STORAGE_FULL":suitable?"STORAGE_UNREACHABLE":deposit?"STORAGE_FULL":"NO_INPUT";return false;
    }
    private static int depositCount(Villager v,int slot){ItemStack stack=v.getInventory().getItem(slot);if(!WorkPolicy.deposit(v,stack))return 0;String role=WorkPolicy.role(v);if(role.equals("weaver")&&(stack.is(org.slavicmyths.textile.Textiles.item("flax_fiber"))||stack.is(org.slavicmyths.textile.Textiles.item("linen_thread"))))return 0;return role.equals("farmer")?Math.max(0,stack.getCount()-8):stack.getCount();}
    private static boolean suitable(ServerLevel level,Villager v,BlockPos p,boolean deposit){if(!level.hasChunkAt(p)||!(level.getBlockEntity(p) instanceof SettlementChest chest))return false;
        if(deposit){for(int i=0;i<v.getInventory().getContainerSize();i++)if(depositCount(v,i)>0&&chest.hasSpace(WorkPolicy.clean(v.getInventory().getItem(i).copyWithCount(depositCount(v,i)))))return true;}
        else for(int i=0;i<chest.getContainerSize();i++){ItemStack stack=chest.getItem(i);int wanted=WorkPolicy.wanted(v,stack);if(wanted>0&&InventoryTransactions.canInsert(v.getInventory(),stack.copyWithCount(Math.min(wanted,stack.getCount()))))return true;}return false;
    }
    private interface BlockTest {boolean test(BlockPos p,net.minecraft.world.level.block.state.BlockState s);}
    /** Bounded slice of anchored field/shore; 768 loaded positions per cooled attempt, staggered by UUID. */
    private BlockPos findBlock(ServerLevel level,Villager v,int radius,BlockTest predicate){WorkState s=state(v);s.searches++;int side=radius*2+1,total=side*side*3;
        for(BlockPos p:BlockPos.betweenClosed(v.blockPosition().offset(-3,-1,-3),v.blockPosition().offset(3,1,3)))if(p.distSqr(anchor)<=radius*radius&&level.hasChunkAt(p)&&predicate.test(p,level.getBlockState(p))&&reachable(v,p))return p.immutable();
        if(s.scanCursor==0)s.scanCursor=Math.floorMod(v.getUUID().hashCode(),total);
        for(int n=0;n<768;n++){int i=s.scanCursor++%total;BlockPos p=anchor.offset(i%side-radius,i/(side*side)-1,i/side%side-radius);
            if(level.hasChunkAt(p)&&predicate.test(p,level.getBlockState(p))&&reachable(v,p))return p;}return null;
    }
    private static boolean fishingSpot(ServerLevel level,BlockPos p){if(!level.getBlockState(p).isAir()||!level.getBlockState(p.below()).isSolid())return false;for(Direction d:Direction.Plane.HORIZONTAL){BlockPos q=p.relative(d);if(level.hasChunkAt(q)&&(level.getFluidState(q).is(FluidTags.WATER)||level.getFluidState(q.below()).is(FluidTags.WATER)))return true;}return false;}
    @Override protected void start(ServerLevel level,Villager v,long now){began=now;fishingSince=-1;ownedWalk=null;if(!close(v,target)||task.equals("FARM_APPROACH")){ownedWalk=new WalkTarget(new BlockPosTracker(approach==null?target:approach),.5F,0);v.getBrain().setMemory(MemoryModuleType.WALK_TARGET,ownedWalk);}v.getBrain().setMemory(MemoryModuleType.LOOK_TARGET,new BlockPosTracker(target));}
    @Override protected boolean canStillUse(ServerLevel level,Villager v,long now){return eligible(level,v)&&target!=null&&level.hasChunkAt(target)&&validJob(level,v)&&now-began<WorkLimits.TRAVEL_TIMEOUT+(task.equals("FISH")?120:0)&&(!task.equals("FISH")||fishingSpot(level,target));}
    @Override protected void tick(ServerLevel level,Villager v,long now){if(!canStillUse(level,v,now)){doStop(level,v,now);return;}
        if(animal!=null){if(!animal.isAlive()||!v.hasLineOfSight(animal)||v.distanceToSqr(animal)>WorkLimits.INTERACTION_DISTANCE_SQUARED)return;}else if(task.equals("FARM_APPROACH")?v.distanceToSqr(target.getCenter())>=1:!close(v,target))return;
        if(task.equals("FARM_APPROACH")){doStop(level,v,now);return;}
        if(task.equals("FISH")){if(fishingSince<0){fishingSince=now;v.playSound(net.minecraft.sounds.SoundEvents.FISHING_BOBBER_SPLASH,.5F,1);}if(now-fishingSince<100)return;}
        WorkState s=state(v);String result="NONE";
        if(task.equals("DEPOSIT")||task.equals("EXTRACT")){
            // Type, loaded chunk, proximity and real contents rechecked at the point of transaction.
            if(!(level.getBlockEntity(target) instanceof SettlementChest chest)){s.storage=null;result="NO_STORAGE";}
            else if(task.equals("DEPOSIT")){result="STORAGE_FULL";for(int i=0;i<v.getInventory().getContainerSize();i++){int n=depositCount(v,i);if(n==0)continue;ItemStack clean=WorkPolicy.clean(v.getInventory().getItem(i).copyWithCount(n));var copy=InventoryTransactions.snapshot(chest);if(InventoryTransactions.insert(copy,clean,chest.getMaxStackSize())){v.getInventory().removeItem(i,n);InventoryTransactions.commit(chest,copy);result="DEPOSITED";break;}}}
            else {result="NO_INPUT";int taken=0;for(int i=0;i<chest.getContainerSize()&&taken<4;i++){ItemStack stack=chest.getItem(i);int wanted=WorkPolicy.wanted(v,stack);if(wanted>0&&InventoryTransactions.transfer(chest,i,v.getInventory(),Math.min(wanted,stack.getCount()))){taken++;result="EXTRACTED";}}}
        }else if(task.equals("FISH")){
            int rod=findItem(v,Items.FISHING_ROD);if(rod<0)result="NO_INPUT";else {ItemStack catchStack=WorkPolicy.harvest(new ItemStack(org.slavicmyths.water.WaterFishing.fish(level,target,v.getRandom())));
                if(InventoryTransactions.canInsert(v.getInventory(),catchStack)){v.getInventory().addItem(catchStack);damage(v,rod);s.nextFishing=now+6000;result="CAUGHT";}else result="PERSONAL_INVENTORY_FULL";}
        }else if(task.equals("SHEAR")&&animal instanceof Sheep sheep){
            int shears=findItem(v,Items.SHEARS);if(shears<0)result="NO_INPUT";else if(!grief(level,v))result="MOB_GRIEFING_DISABLED";else if(sheep.readyForShearing()){
                // Vanilla shearing event/loot with actual wool item entities, then native inventory capacity.
                Set<java.util.UUID> before=new HashSet<>();for(var item:level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,sheep.getBoundingBox().inflate(2)))before.add(item.getUUID());
                sheep.shear(net.minecraft.sounds.SoundSource.NEUTRAL);damage(v,shears);
                for(var item:level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,sheep.getBoundingBox().inflate(2)))if(!before.contains(item.getUUID())&&item.getItem().is(net.minecraft.tags.ItemTags.WOOL))item.setItem(WorkPolicy.harvest(item.getItem()));result="SHEARED";
            }
        }else if(task.equals("TEND")&&animal!=null){
            if(!grief(level,v))result="MOB_GRIEFING_DISABLED";else if(animal instanceof YardAnimal y&&y.eggReady()&&InventoryTransactions.canInsert(v.getInventory(),y.egg())){v.getInventory().addItem(y.egg());y.resetEgg();result="COLLECTED";}
            else {result="NO_INPUT";for(int i=0;i<v.getInventory().getContainerSize();i++){ItemStack food=v.getInventory().getItem(i);if(animal.isFood(food)&&(animal.isBaby()||animal.getHealth()<animal.getMaxHealth())){animal.heal(2);if(animal.isBaby())animal.ageUp(20,true);v.getInventory().removeItem(i,1);if(animal instanceof YardAnimal y)y.eatAnimation();result="FED";break;}}}
        }else if(task.equals("GATHER")){
            var block=level.getBlockState(target);if(!grief(level,v))result="MOB_GRIEFING_DISABLED";
            else if(block.getBlock() instanceof org.slavicmyths.garden.PerennialBush bush&&block.getValue(org.slavicmyths.garden.PerennialBush.PHASE)==4){
                int min=bush.kind<3?2:1,max=bush.kind==1?5:bush.kind<3?4:3;ItemStack berries=new ItemStack(org.slavicmyths.garden.Gardens.berry(bush.kind),min+v.getRandom().nextInt(max-min+1));
                if(!InventoryTransactions.canInsert(v.getInventory(),berries))result="PERSONAL_INVENTORY_FULL";else if(bush.growTo(level,target,1)){v.getInventory().addItem(berries);result="GATHERED";}
            }else result="NO_VALID_TARGET";
        }else if(task.equals("WORKSTATION"))result=grief(level,v)?VillageProduction.process(level,v,level.getBlockEntity(target)):"MOB_GRIEFING_DISABLED";
        s.actions++;s.failure=result;s.available=v.getInventory().getItems().toString();if(!result.startsWith("NO_")&&!result.endsWith("FULL")&&!result.equals("PROCESSING")){v.playWorkSound();v.swing(InteractionHand.MAIN_HAND);}doStop(level,v,now);
    }
    private static void damage(Villager v,int slot){v.getInventory().getItem(slot).hurtAndBreak(1,(ServerLevel)v.level(),v,item->v.playSound(net.minecraft.sounds.SoundEvents.ITEM_BREAK,.6F,1));v.getInventory().setChanged();}
    @Override protected void stop(ServerLevel level,Villager v,long now){
        // Do not erase safety/trading WALK_TARGET installed by a higher-priority native activity.
        var walk=v.getBrain().getMemory(MemoryModuleType.WALK_TARGET);if(walk.isPresent()&&walk.get()==ownedWalk){v.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);v.getNavigation().stop();}
        target=null;animal=null;state(v).target=null;if(!eligible(level,v))state(v).failure="OUTSIDE_WORK_TIME";
    }
}
