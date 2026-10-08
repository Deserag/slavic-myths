package org.slavicmyths.verify;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.village.*;

@GameTestHolder("slavicmyths_village") @PrefixGameTestTemplate(false)
public final class VillageGameTests {
    @GameTest(template="empty",timeoutTicks=100)
    public static void registrations(GameTestHelper t){
        Set<net.minecraft.world.level.block.state.BlockState> all=new HashSet<>();
        for(var e:VillageRoles.ROLES.entrySet()){
            var poi=VillageRoles.JOBS.get(e.getKey());var holder=poi.get();
            t.assertTrue(poi.getDelegate().is(PoiTypeTags.ACQUIRABLE_JOB_SITE),"Missing job tag "+e.getKey());
            t.assertTrue(holder.maxTickets()==1,"POI sharing "+e.getKey());
            var states=VillageRoles.WORKSTATIONS.get(e.getKey()).get().getStateDefinition().getPossibleStates().stream().filter(VillageRoles::isWorkstationMaster).toList();
            t.assertTrue(holder.matchingStates().size()==states.size()&&holder.matchingStates().containsAll(states),"Missing block state "+e.getKey());
            for(var state:states){t.assertTrue(all.add(state),"Duplicate POI state");var mapped=net.minecraft.world.entity.ai.village.poi.PoiTypes.forState(state).orElseThrow();t.assertTrue(mapped.is(poi.getKey()),"Native POI mapping");t.assertTrue(e.getValue().get().heldJobSite().test(mapped)&&e.getValue().get().acquirableJobSite().test(mapped),"Profession predicate");}
            if(e.getKey().equals("hunter"))for(var state:VillageRoles.WORKSTATIONS.get("hunter").get().getStateDefinition().getPossibleStates())if(state.getValue(org.slavicmyths.storage.StorageBlock.PART)!=0)t.assertTrue(net.minecraft.world.entity.ai.village.poi.PoiTypes.forState(state).isEmpty(),"Drying rack secondary part became another workstation");
        }
        long vanilla=BuiltInRegistries.VILLAGER_PROFESSION.keySet().stream().filter(k->k.getNamespace().equals("minecraft")).count();
        t.assertTrue(vanilla==15,"Vanilla IDs changed");t.assertTrue(VillageRoles.ROLES.size()==7,"Role count");
        System.out.println("VILLAGE_REGISTRATIONS_PASS roles=7 vanillaIds=15 mappedStates="+all.size());t.succeed();
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void poolsRestockAndSerialization(GameTestHelper t){
        int samples=0;Set<String> different=new HashSet<>();
        for(var role:VillageRoles.ROLES.values().stream().filter(v->!v.getId().getPath().equals("druzhinnik")).toList())for(int level=1;level<=5;level++)for(int seed=0;seed<12;seed++){
            Villager v=EntityType.VILLAGER.create(t.getLevel());v.getRandom().setSeed(seed*0x9e3779b97f4a7c15L);v.setVillagerData(new VillagerData(VillagerType.PLAINS,role.get(),level));
            v.setData(VillageRoles.OUTFIT,OutfitRules.variant(v.getUUID()));
            t.assertTrue(v.getOffers().size()==2,"Vanilla two-offer selection "+role.getId()+"/"+level);
            String initial=assortment(v);different.add(role.getId()+"/"+level+"/"+initial);
            v.getOffers().getFirst().increaseUses();v.restock();t.assertTrue(assortment(v).equals(initial)&&v.getOffers().getFirst().getUses()==0,"Restock rerolled");
            CompoundTag saved=v.saveWithoutId(new CompoundTag());Villager loaded=EntityType.VILLAGER.create(t.getLevel());loaded.load(saved);
            t.assertTrue(assortment(loaded).equals(initial)&&loaded.getVillagerData().getProfession()==v.getVillagerData().getProfession()&&loaded.getVillagerData().getType()==v.getVillagerData().getType()&&loaded.getVillagerData().getLevel()==level,"NBT offers/profession changed");
            t.assertTrue(loaded.getData(VillageRoles.OUTFIT).equals(v.getData(VillageRoles.OUTFIT)),"Visual seed lost");samples++;
        }
        t.assertTrue(different.size()>90,"Pools are not varying");
        System.out.println("VILLAGE_POOLS_PASS samples="+samples+" variants="+different.size()+" offers=2 restock=true NBT=true");t.succeed();
    }
    private static String assortment(Villager v){return v.getOffers().stream().map(o->BuiltInRegistries.ITEM.getKey(o.getBaseCostA().getItem())+"/"+o.getBaseCostA().getCount()+"->"+BuiltInRegistries.ITEM.getKey(o.getResult().getItem())+"/"+o.getResult().getCount()).toList().toString();}
    @GameTest(template="empty",timeoutTicks=100)
    public static void vanillaProfessionTrades(GameTestHelper t){
        int count=0;
        for(var key:BuiltInRegistries.VILLAGER_PROFESSION.keySet()){
            if(!key.getNamespace().equals("minecraft")||key.getPath().equals("none")||key.getPath().equals("nitwit"))continue;
            var profession=BuiltInRegistries.VILLAGER_PROFESSION.get(key);
            for(int level=1;level<=5;level++){
                var v=EntityType.VILLAGER.create(t.getLevel());v.setVillagerData(new VillagerData(VillagerType.PLAINS,profession,level));
                t.assertTrue(!v.getOffers().isEmpty(),"Vanilla trades missing "+key+"/"+level);
                String original=assortment(v);v.restock();t.assertTrue(assortment(v).equals(original),"Vanilla restock altered");
            }
            count++;
        }
        t.assertTrue(count==13,"Vanilla profession count");System.out.println("VILLAGE_VANILLA_TRADES_PASS professions=13 levels=65 restock=true");t.succeed();
    }
    private static Villager spawn(GameTestHelper t,int x,int z,VillagerProfession profession,boolean baby){
        Villager v=EntityType.VILLAGER.create(t.getLevel());v.setVillagerData(new VillagerData(VillagerType.PLAINS,profession,1));if(baby)v.setAge(-24000);
        var p=t.absolutePos(new BlockPos(x,2,z));v.moveTo(p.getX()+.5,p.getY(),p.getZ()+.5,0,0);v.setPersistenceRequired();t.getLevel().addFreshEntity(v);return v;
    }
    private static void floor(GameTestHelper t){
        for(int x=0;x<12;x++)for(int z=0;z<12;z++)t.getLevel().setBlock(t.absolutePos(new BlockPos(x,1,z)),Blocks.STONE.defaultBlockState(),3);
        for(int x=0;x<12;x++)for(int z=0;z<12;z++)if(x==0||z==0||x==11||z==11)for(int y=2;y<5;y++)t.getLevel().setBlock(t.absolutePos(new BlockPos(x,y,z)),Blocks.GLASS.defaultBlockState(),3);
    }
    private static void placeWorkstation(GameTestHelper t,BlockPos at,String role){
        var block=VillageRoles.WORKSTATIONS.get(role).get();var state=block.defaultBlockState();var pos=t.absolutePos(at);
        t.getLevel().setBlock(pos,state,3);block.setPlacedBy(t.getLevel(),pos,state,null,net.minecraft.world.item.ItemStack.EMPTY);
    }
    @GameTest(template="empty",batch="economic_jobs",timeoutTicks=1800)
    public static void naturalWorkstations(GameTestHelper t){
        floor(t);t.getLevel().getPoiManager().getInRange(h->h.is(VillageRoles.JOBS.get("druzhinnik").getKey()),t.absolutePos(BlockPos.ZERO),64,net.minecraft.world.entity.ai.village.poi.PoiManager.Occupancy.ANY).map(x->x.getPos()).toList().forEach(x->t.getLevel().destroyBlock(x,false));t.getLevel().setDayTime(3000);List<Villager> villagers=new ArrayList<>();List<VillagerProfession> jobs=new ArrayList<>();
        int i=0;for(var role:VillageRoles.ROLES.entrySet().stream().filter(v->!v.getKey().equals("druzhinnik")).toList()){
            int x=2+(i%3)*3,z=2+(i/3)*5;
            placeWorkstation(t,new BlockPos(x,2,z+1),role.getKey());
            villagers.add(spawn(t,x,z,VillagerProfession.NONE,false));jobs.add(role.getValue().get());i++;
        }
        // All six jobs must be occupied exactly once; villagers may legitimately choose a neighbouring job.
        t.runAfterDelay(590,()->System.out.println("JOBS_DIAGNOSTIC "+villagers.stream().map(v->v.getVillagerData().getProfession().name()+" site="+v.getBrain().getMemory(MemoryModuleType.JOB_SITE)+" failure="+VillageWork.state(v).failure).toList()));
        t.succeedWhen(()->{
            Set<VillagerProfession> found=new HashSet<>();Set<GlobalPos> sites=new HashSet<>();
            for(var v:villagers){t.assertTrue(jobs.contains(v.getVillagerData().getProfession()),"Workstation not acquired");found.add(v.getVillagerData().getProfession());t.assertTrue(sites.add(v.getBrain().getMemory(MemoryModuleType.JOB_SITE).orElseThrow(()->new GameTestAssertException("Missing claimed POI"))),"Shared POI");}
            t.assertTrue(found.size()==6,"Missing profession");System.out.println("VILLAGE_NATIVE_POI_PASS adultJobs=6 distinctOwners=6");
        });
    }
    @GameTest(template="empty",timeoutTicks=260)
    public static void childrenAndNitwits(GameTestHelper t){
        floor(t);List<Villager> villagers=new ArrayList<>();int i=0;
        for(var role:VillageRoles.ROLES.keySet()){
            int x=2+(i%3)*3,z=2+(i/3)*5;
            placeWorkstation(t,new BlockPos(x,2,z+1),role);
            villagers.add(spawn(t,x,z,VillagerProfession.NONE,true));villagers.add(spawn(t,x,z,VillagerProfession.NITWIT,false));i++;
        }
        t.runAfterDelay(220,()->{for(var v:villagers){t.assertTrue(v.getVillagerData().getProfession()==(v.isBaby()?VillagerProfession.NONE:VillagerProfession.NITWIT),"Ineligible villager acquired job");t.assertTrue(v.getBrain().getMemory(MemoryModuleType.JOB_SITE).isEmpty(),"Ineligible claimed POI");}System.out.println("VILLAGE_NEGATIVE_POI_PASS children=6 nitwits=6");t.succeed();});
    }
    @GameTest(template="empty",timeoutTicks=300)
    public static void vanillaProgression(GameTestHelper t){
        floor(t);List<Villager> villagers=new ArrayList<>();int i=0;
        for(var role:VillageRoles.ROLES.values().stream().filter(v->!v.getId().getPath().equals("druzhinnik")).toList()){Villager v=spawn(t,2+i%3*3,2+i/3*4,role.get(),false);villagers.add(v);i++;}
        // The same notifyTrade path and timed XP progression used by MerchantResultSlot.
        advance(t,villagers,1);
    }
    private static void advance(GameTestHelper t,List<Villager> villagers,int expected){
        for(var v:villagers){t.assertTrue(v.getVillagerData().getLevel()==expected,"Level progression failed "+expected);var offers=v.getOffers();t.assertTrue(offers.size()==expected*2,"Offers on level up");if(expected<5){var o=offers.getLast();int threshold=VillagerData.getMaxXpPerLevel(expected);int safety=0;while(v.getVillagerXp()<threshold&&safety++<200){if(o.isOutOfStock())v.restock();v.notifyTrade(o);}}}
        if(expected==5){System.out.println("VILLAGE_LEVEL_PASS professions=6 noviceToMaster=true offersPerMaster=10");t.succeed();}
        else t.runAfterDelay(48,()->advance(t,villagers,expected+1));
    }
    @GameTest(template="empty",timeoutTicks=200)
    public static void infectionCure(GameTestHelper t){
        floor(t);t.getLevel().getServer().setDifficulty(net.minecraft.world.Difficulty.HARD,true);
        Villager v=spawn(t,4,4,VillageRoles.ROLES.get("miller").get(),false);v.setVillagerData(v.getVillagerData().setLevel(3));v.setVillagerXp(80);
        String offers=assortment(v);int variant=v.getData(VillageRoles.OUTFIT);UUID original=v.getUUID();
        Zombie killer=EntityType.ZOMBIE.create(t.getLevel());killer.killedEntity(t.getLevel(),v);
        t.runAfterDelay(2,()->{
            var pos=t.absolutePos(new BlockPos(4,2,4));var zombies=t.getLevel().getEntitiesOfClass(ZombieVillager.class,new net.minecraft.world.phys.AABB(pos).inflate(3));t.assertTrue(zombies.size()==1,"Infection failed");var z=zombies.getFirst();
            t.assertTrue(z.getVillagerData().getProfession()==VillageRoles.ROLES.get("miller").get()&&z.getVillagerData().getLevel()==3,"Zombie profession/level lost");t.assertTrue(z.getData(VillageRoles.OUTFIT)==variant,"Infection outfit changed");
            // Load a short vanilla conversion countdown; do not call conversion implementation or fabricate outcome.
            var tag=z.saveWithoutId(new CompoundTag());tag.putInt("ConversionTime",1);z.load(tag);
            t.runAfterDelay(10,()->{
                var cured=t.getLevel().getEntitiesOfClass(Villager.class,new net.minecraft.world.phys.AABB(pos).inflate(4));t.assertTrue(cured.size()==1,"Cure failed");var c=cured.getFirst();
                t.assertTrue(c.getVillagerData().getLevel()==3&&c.getVillagerXp()==80&&c.getVillagerData().getProfession()==VillageRoles.ROLES.get("miller").get(),"Cure profession/XP lost");
                t.assertTrue(assortment(c).equals(offers)&&c.getData(VillageRoles.OUTFIT)==variant,"Cure rerolled offers/outfit");
                System.out.println("VILLAGE_CONVERSION_PASS infection=true cure=true sameOffers=true sameOutfit=true uuidChanged="+!c.getUUID().equals(original));t.succeed();
            });
        });
    }
}
