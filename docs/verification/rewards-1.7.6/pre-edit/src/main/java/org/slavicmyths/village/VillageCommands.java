package org.slavicmyths.village;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid="slavicmyths")
public final class VillageCommands {
    @SubscribeEvent public static void register(RegisterCommandsEvent e){
        var debug=Commands.literal("villager").executes(c->inspect(c.getSource(),nearest(c.getSource())))
            .then(Commands.argument("target",EntityArgument.entity()).executes(c->inspect(c.getSource(),EntityArgument.getEntity(c,"target"))));
        var jobs=Commands.literal("workstation");
        for(String role:VillageRoles.ROLES.keySet())jobs.then(Commands.literal(role).executes(c->workstation(c.getSource(),role)));
        var root=e.getDispatcher().register(Commands.literal("slavicmyths")
            .then(Commands.literal("debug").requires(s->s.hasPermission(2)).then(debug)
                .then(Commands.literal("settlement_chest").then(Commands.argument("pos",net.minecraft.commands.arguments.coordinates.BlockPosArgument.blockPos()).executes(c->storage(c.getSource(),net.minecraft.commands.arguments.coordinates.BlockPosArgument.getLoadedBlockPos(c,"pos"))))))
            .then(Commands.literal("test").requires(s->s.hasPermission(2)).then(Commands.literal("villager").then(jobs)
                .then(Commands.literal("level").then(Commands.argument("level",IntegerArgumentType.integer(1,5)).executes(c->level(c.getSource(),IntegerArgumentType.getInteger(c,"level"))))))));
        if(e.getDispatcher().getRoot().getChild("sm")==null)e.getDispatcher().register(Commands.literal("sm").redirect(e.getDispatcher().getRoot().getChild("slavicmyths")));
    }
    private static LivingEntity nearest(CommandSourceStack s){
        var box=new net.minecraft.world.phys.AABB(s.getPosition().add(-12,-6,-12),s.getPosition().add(12,6,12));
        return s.getLevel().getEntitiesOfClass(LivingEntity.class,box,x->x instanceof Villager||x instanceof ZombieVillager).stream()
            .min(java.util.Comparator.comparingDouble(x->x.distanceToSqr(s.getPosition()))).orElse(null);
    }
    private static int inspect(CommandSourceStack s,Entity target){
        if(!(target instanceof LivingEntity living)||!(target instanceof VillagerDataHolder holder)){s.sendFailure(Component.translatable("village.slavicmyths.debug.none"));return 0;}
        var d=holder.getVillagerData();var id=BuiltInRegistries.VILLAGER_PROFESSION.getKey(d.getProfession());
        String type=BuiltInRegistries.VILLAGER_TYPE.getKey(d.getType()).toString();
        s.sendSuccess(()->Component.literal("UUID="+target.getUUID()+" type="+type+" climate="+OutfitRules.climate(type.substring(type.indexOf(':')+1))+" profession="+id+" level="+d.getLevel()+" outfit="+living.getData(VillageRoles.OUTFIT)+" baby="+living.isBaby()+" zombie="+(target instanceof ZombieVillager)),false);
        s.sendSuccess(()->Component.translatable("entity.minecraft.villager."+(id.getNamespace().equals("minecraft")?"":id.getNamespace()+".")+id.getPath()),false);
        if(target instanceof Villager v){
            var work=VillageWork.state(v);var inv=v.getInventory();int used=0;for(int i=0;i<inv.getContainerSize();i++)if(!inv.getItem(i).isEmpty())used++;
            final int slotsUsed=used;
            var chest=work.storage!=null&&s.getLevel().hasChunkAt(work.storage)&&s.getLevel().getBlockEntity(work.storage) instanceof SettlementChest ch?ch:null;
            s.sendSuccess(()->Component.literal("activity="+v.getBrain().getActiveNonCoreActivity()+" reachableWorkstation="+work.workstationReachable+" task="+work.task+" target="+work.target+" cooldown="+Math.max(0,work.nextAttempt-s.getLevel().getGameTime())+" inventory="+slotsUsed+"/"+inv.getContainerSize()+" storage="+work.storage+" distance="+(work.storage==null?"none":Math.sqrt(v.distanceToSqr(work.storage.getCenter())))+" reachableStorage="+work.storageReachable+" storageFill="+(chest==null?"none":chest.fillRatio())+" required="+work.required+" available="+inv.getItems()+" pending="+work.pending+" lastFailure="+work.failure+" searches="+work.searches+" paths="+work.pathRequests+" actions="+work.actions),false);
            var job=v.getBrain().getMemory(MemoryModuleType.JOB_SITE);
            var poi=job.filter(j->j.dimension().equals(s.getLevel().dimension())).flatMap(j->s.getLevel().getPoiManager().getType(j.pos())).map(h->h.unwrapKey().map(k->k.location().toString()).orElse("unregistered")).orElse("none");
            var offers=v.getOffers();
            s.sendSuccess(()->Component.literal("workstation="+job+" POI="+poi+" locked="+(v.getVillagerXp()>0)+" offers="+offers.size()),false);
            for(var offer:offers)s.sendSuccess(()->Component.literal(BuiltInRegistries.ITEM.getKey(offer.getBaseCostA().getItem())+" x"+offer.getBaseCostA().getCount()+" -> "+BuiltInRegistries.ITEM.getKey(offer.getResult().getItem())+" x"+offer.getResult().getCount()+" uses="+offer.getUses()+"/"+offer.getMaxUses()),false);
        }else if(target instanceof ZombieVillager z){
            var saved=z.saveWithoutId(new net.minecraft.nbt.CompoundTag());
            var offers=saved.contains("Offers")?net.minecraft.world.item.trading.MerchantOffers.CODEC.parse(z.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),saved.get("Offers")).getOrThrow():new net.minecraft.world.item.trading.MerchantOffers();
            s.sendSuccess(()->Component.literal("workstation=none (zombie) locked="+(z.getVillagerXp()>0)+" savedOffers="+offers.size()+" converting="+z.isConverting()),false);
            for(var offer:offers)s.sendSuccess(()->Component.literal(BuiltInRegistries.ITEM.getKey(offer.getBaseCostA().getItem())+" -> "+BuiltInRegistries.ITEM.getKey(offer.getResult().getItem())),false);
        }
        return 1;
    }
    // Places a real POI next to a test villager. Acquisition and profession assignment remain vanilla AI.
    private static int workstation(CommandSourceStack s,String role)throws CommandSyntaxException{
        if(!(nearest(s) instanceof Villager v)||v.isBaby()||v.getVillagerData().getProfession()!=VillagerProfession.NONE||v.getVillagerXp()!=0){s.sendFailure(Component.translatable("village.slavicmyths.test.invalid"));return 0;}
        BlockPos p=v.blockPosition().east(2);
        if(!s.getLevel().hasChunkAt(p)||!s.getLevel().getWorldBorder().isWithinBounds(p)||!s.getLevel().getBlockState(p).canBeReplaced()||!s.getLevel().getBlockState(p.below()).isSolid()){s.sendFailure(Component.translatable("village.slavicmyths.test.invalid"));return 0;}
        var block=VillageRoles.WORKSTATIONS.get(role).get();var state=block.defaultBlockState();
        if(block instanceof org.slavicmyths.storage.StorageBlock storage)for(int part:storage.parts()){
            BlockPos q=storage.position(state,p,part);
            if(!s.getLevel().hasChunkAt(q)||!s.getLevel().getWorldBorder().isWithinBounds(q)||q.getY()>=s.getLevel().getMaxBuildHeight()||!s.getLevel().getBlockState(q).canBeReplaced()){s.sendFailure(Component.translatable("village.slavicmyths.test.invalid"));return 0;}
        }
        if(!s.getLevel().setBlock(p,state,3))return 0;
        block.setPlacedBy(s.getLevel(),p,state,v,net.minecraft.world.item.ItemStack.EMPTY);
        return 1;
    }
    private static int level(CommandSourceStack s,int level){
        if(!(nearest(s) instanceof Villager v)||v.isBaby()||v.getVillagerData().getProfession()==VillagerProfession.NONE||v.getVillagerData().getProfession()==VillagerProfession.NITWIT)return 0;
        v.setVillagerData(v.getVillagerData().setLevel(level));return inspect(s,v);
    }
    private static int storage(CommandSourceStack s,BlockPos p){
        if(!(s.getLevel().getBlockEntity(p) instanceof SettlementChest chest)){s.sendFailure(Component.literal("NO_STORAGE"));return 0;}
        s.sendSuccess(()->Component.literal("capacity="+chest.getContainerSize()+" used="+chest.usedSlots()+" free="+chest.freeSlots()+" fill="+String.format(java.util.Locale.ROOT,"%.1f%%",chest.fillRatio()*100)+" full="+chest.isFull()),false);
        for(String role:java.util.List.of("farmer","fisherman","shepherd","miller","brewer","weaver","herder","hunter","cook")){
            java.util.List<String> visible=new java.util.ArrayList<>();for(int i=0;i<chest.getContainerSize();i++){var stack=chest.getItem(i);if(stack.is(WorkPolicy.tag("inputs",role)))visible.add(BuiltInRegistries.ITEM.getKey(stack.getItem())+" x"+stack.getCount());}
            s.sendSuccess(()->Component.literal(role+" inputs="+visible),false);
        }return 1;
    }
    private VillageCommands(){}
}
