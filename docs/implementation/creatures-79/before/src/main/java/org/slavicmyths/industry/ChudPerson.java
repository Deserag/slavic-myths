package org.slavicmyths.industry;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.*;
import net.minecraft.world.level.Level;
import org.slavicmyths.registry.ModItems;

/** Three original NPC silhouettes with a miner trade profile on the artisan; native MerchantMenu owns transactions. */
public final class ChudPerson extends AbstractVillager {
    private static final EntityDataAccessor<Integer> ROLE=SynchedEntityData.defineId(ChudPerson.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ACT=SynchedEntityData.defineId(ChudPerson.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> ACT_START=SynchedEntityData.defineId(ChudPerson.class,EntityDataSerializers.LONG);
    private long guardHitAt;private LivingEntity guardVictim;
    public int activity(){return entityData.get(ACT);}public float activityAge(float p){return level().getGameTime()-entityData.get(ACT_START)+p;}
    private void activity(int a){if(activity()!=a){entityData.set(ACT,a);entityData.set(ACT_START,level().getGameTime());}}
    private boolean alarm(Player p){var n=getPersistentData();return n.getCompound("ChudAggressors").getLong(p.getUUID().toString())>level().getGameTime();}
    private BlockPos home;private boolean rareStock;private long stockDay=-1;private int stockBits;
    public ChudPerson(EntityType<? extends ChudPerson> type,Level level){super(type,level);setPersistenceRequired();getAttribute(Attributes.STEP_HEIGHT).setBaseValue(1);setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.WATER,-1);setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.LAVA,-1);setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.DAMAGE_FIRE,-1);if(type==ChudPeople.GUARD.get()){entityData.set(ROLE,3);guardEquipment();}else if(type==ChudPeople.ARTISAN.get())entityData.set(ROLE,1);}
    public static AttributeSupplier.Builder attributes(double hp){return createMobAttributes().add(Attributes.MAX_HEALTH,hp).add(Attributes.MOVEMENT_SPEED,hp==44?.29:hp==20?.26:.24).add(Attributes.FOLLOW_RANGE,hp==44?16:20).add(Attributes.ATTACK_DAMAGE,hp==44?2:5).add(Attributes.ARMOR,2);}
    protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(ROLE,0);b.define(ACT,0);b.define(ACT_START,0L);}
    public int role(){return entityData.get(ROLE);}
    public int getMaxFallDistance(){return 2;}
    public boolean isAlliedTo(Entity e){return e instanceof ChudPerson||super.isAlliedTo(e);}
    private void guardEquipment(){if(role()!=3)return;if(getMainHandItem().isEmpty())setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_AXE));setDropChance(EquipmentSlot.MAINHAND,0);}
    public void setup(int role,BlockPos home,boolean rare){entityData.set(ROLE,role);this.home=home.immutable();restrictTo(home,10);rareStock=rare;offers=null;guardEquipment();if(role==1||role==2){setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(role==2?Items.IRON_PICKAXE:Items.IRON_AXE));setDropChance(EquipmentSlot.MAINHAND,0);}}
    public Component getDisplayName(){return hasCustomName()?super.getDisplayName():Component.translatable("industry.slavicmyths.role."+role());}
    protected void registerGoals(){
        goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(1,new TradeWithPlayerGoal(this));goalSelector.addGoal(1,new LookAtTradingPlayerGoal(this));
        goalSelector.addGoal(2,new MeleeAttackGoal(this,1.1,true){public boolean canUse(){return role()==3&&super.canUse();}protected void checkAndPerformAttack(LivingEntity t){if(canPerformAttack(t)&&guardVictim==null){resetAttackCooldown();guardVictim=t;guardHitAt=level().getGameTime()+7;activity(3);getNavigation().stop();}}});
        goalSelector.addGoal(2,new AvoidEntityGoal<>(this,Monster.class,8,.8,1.15){public boolean canUse(){return role()!=3&&super.canUse();}});
        goalSelector.addGoal(2,new AvoidEntityGoal<Player>(this,Player.class,12,.9,1.15,t->t instanceof Player p&&alarm(p)){public boolean canUse(){return role()!=3&&super.canUse();}});
        goalSelector.addGoal(3,new MoveTowardsRestrictionGoal(this,.8));
        goalSelector.addGoal(3,new Goal(){ {setFlags(EnumSet.of(Flag.MOVE));}public boolean canUse(){return role()!=3&&home!=null&&!isTrading()&&(Math.floorMod(level().getDayTime(),24000)>=11000&&Math.floorMod(level().getDayTime(),24000)<23000||getLastHurtByMob()!=null)&&distanceToSqr(home.getCenter())>4;}public void start(){getNavigation().moveTo(home.getX()+.5,home.getY(),home.getZ()+.5,.85);}public boolean canContinueToUse(){return canUse()&&!getNavigation().isDone();}});
        goalSelector.addGoal(4,new Goal(){ {setFlags(EnumSet.of(Flag.MOVE));}private BlockPos destination;public boolean canUse(){if(home==null||isTrading()||level().getGameTime()%80!=0||activity()==1)return false;long time=Math.floorMod(level().getDayTime(),24000);if(role()!=3&&time>=11000&&time<23000)return false;for(int i=0;i<6;i++){BlockPos p=home.offset(getRandom().nextInt(13)-6,0,getRandom().nextInt(13)-6);boolean safe=true;for(int a=-1;a<=1;a++)for(int b=-1;b<=1;b++)if(!level().getBlockState(p.offset(a,-1,b)).isCollisionShapeFullBlock(level(),p.offset(a,-1,b)))safe=false;if(safe&&level().getBlockState(p).isAir()&&level().getBlockState(p.above()).isAir()){var path=getNavigation().createPath(p,0);if(path!=null&&path.canReach()){destination=p;return true;}}}return false;}public void start(){getNavigation().moveTo(destination.getX()+.5,destination.getY(),destination.getZ()+.5,.65);}public boolean canContinueToUse(){return !getNavigation().isDone()&&!isTrading()&&getTarget()==null;}});goalSelector.addGoal(5,new LookAtPlayerGoal(this,Player.class,6));goalSelector.addGoal(6,new RandomLookAroundGoal(this));
        targetSelector.addGoal(1,new HurtByTargetGoal(this){public boolean canUse(){return role()==3&&super.canUse();}});
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Monster.class,10,true,false,m->!(m instanceof net.minecraft.world.entity.monster.warden.Warden)&&hasLineOfSight(m)&&(home==null||m.distanceToSqr(home.getCenter())<=256)){public boolean canUse(){return role()==3&&super.canUse();}});
    }
    public net.minecraft.world.entity.AgeableMob getBreedOffspring(ServerLevel l,net.minecraft.world.entity.AgeableMob other){return null;}
    protected InteractionResult mobInteract(Player p,InteractionHand hand){
        if(role()==3||!isAlive()||isBaby()||p.getItemInHand(hand).getItem() instanceof SpawnEggItem)return super.mobInteract(p,hand);
        if(level().isClientSide)return InteractionResult.SUCCESS;
        if(getTradingPlayer()!=null&&getTradingPlayer()!=p)return InteractionResult.FAIL;
        setTradingPlayer(p);getNavigation().stop();getLookControl().setLookAt(p,30,30);openTradingScreen(p,getDisplayName(),1);return InteractionResult.CONSUME;
    }
    public boolean showProgressBar(){return false;}
    public boolean canRestock(){return true;}
    protected void rewardTradeXp(MerchantOffer offer){}
    protected void updateTrades(){
        if(role()==3)return;var random=new Random(getUUID().getLeastSignificantBits());
        if(role()==0){offer(Items.COPPER_INGOT,3,null,0,Items.BREAD,4,12);offer(Items.IRON_INGOT,4,null,0,Items.LEATHER,3,8);
            if(random.nextBoolean())offer(Items.GOLD_INGOT,2,null,0,Items.HONEY_BOTTLE,1,6);else offer(Items.COPPER_INGOT,6,null,0,Items.TORCH,8,12);
            if(random.nextBoolean())offer(ModItems.ANCIENT_COIN.get(),1,null,0,ModItems.KARAVAI.get(),2,8);
            if(rareStock||Math.floorMod(getUUID().getLeastSignificantBits(),3)==0){
                offer(Items.NETHERITE_INGOT,1,Items.DIAMOND,12,IndustryAccessories.GOGGLES.get(),1,1);
                switch(random.nextInt(3)){case 0->offer(Industry.COPPER.get(),8,Items.DIAMOND,1,IndustryAccessories.RING.get(),1,2);case 1->offer(Industry.IRON.get(),8,Items.DIAMOND,1,IndustryAccessories.BELT.get(),1,2);case 2->offer(Industry.IRON.get(),6,Items.GOLD_INGOT,4,IndustryAccessories.AMULET.get(),1,2);}
            }
        }else if(role()==1){
            offer(Items.IRON_INGOT,8,null,0,Industry.FLUX.get(),2,12);offer(Items.COPPER_INGOT,16,null,0,Industry.FLUX.get(),2,12);
            if(random.nextBoolean())offer(ModItems.SILVER_INGOT.get(),3,null,0,Industry.FLUX.get(),2,8);
            if(random.nextBoolean())offer(Items.IRON_INGOT,8,Items.DIAMOND,1,Industry.IRON.get(),1,8);else offer(Items.COPPER_INGOT,12,Items.DIAMOND,1,Industry.COPPER.get(),1,8);
        }else{
            offer(Items.IRON_INGOT,6,null,0,Industry.FLUX.get(),2,16);offer(Items.COPPER_INGOT,12,null,0,Industry.FLUX.get(),2,16);
            offer(Items.IRON_INGOT,6,Items.AMETHYST_SHARD,2,MiningRunes.ITEMS.get("mining_1").get(),1,4);
            offer(Items.DIAMOND,6,Industry.COPPER.get(),12,MiningRunes.ITEMS.get("mining_2").get(),1,2);
            offer(Items.DIAMOND,8,Items.NETHERITE_SCRAP,2,MiningRunes.ITEMS.get("fortune_1").get(),1,2);
        }
    }
    private void offer(Item a,int n,Item b,int m,Item out,int count,int uses){offers.add(new MerchantOffer(new ItemCost(a,n),b==null?Optional.empty():Optional.of(new ItemCost(b,m)),new ItemStack(out,count),uses,1,0));}
    public void aiStep(){super.aiStep();if(level().isClientSide)return;long now=level().getGameTime();
        if(role()==3&&guardVictim!=null&&now>=guardHitAt){if(org.slavicmyths.combat.CombatTargeting.canAttack(this,guardVictim)&&distanceToSqr(guardVictim)<6.25&&hasLineOfSight(guardVictim)){swing(InteractionHand.MAIN_HAND);doHurtTarget(guardVictim);}guardVictim=null;}
        if(activity()==3&&activityAge(0)>=15)activity(0);
        if(getTarget() instanceof Player player&&!alarm(player)&&distanceToSqr(player)>6.25)setTarget(null);
        if(getTarget()!=null&&home!=null&&getTarget().distanceToSqr(home.getCenter())>576)setTarget(null);
        if(role()!=3&&tickCount%20==0){boolean fear=getLastHurtByMob()!=null&&tickCount-getLastHurtByMobTimestamp()<200||getPersistentData().getLong("ChudAggressorUntil")>now;
            if(fear){if(activity()!=2)playSound(org.slavicmyths.underground.UndergroundSounds.sound("chud","panic"),.35F,1);activity(2);}else if(role()>0&&home!=null&&!isTrading()&&distanceToSqr(home.getCenter())<9&&Math.floorMod(level().getDayTime(),24000)<11000&&now%600<100){activity(1);getNavigation().stop();if(now%20==0)playSound(org.slavicmyths.underground.UndergroundSounds.sound("chud","work"),.25F,1.1F);}else activity(0);}
        if(tickCount%40!=0)return;
        if(getTradingPlayer()!=null&&(!getTradingPlayer().isAlive()||getTradingPlayer().level()!=level()||distanceToSqr(getTradingPlayer())>64)) {getTradingPlayer().closeContainer();setTradingPlayer(null);}
        if(home==null||role()==3||isTrading()||distanceToSqr(home.getCenter())>100)return;
        long day=level().getDayTime()/24000;if(day>stockDay){stockDay=day;stockBits=0;}int period=IndustryRules.restockPeriod(level().getDayTime());
        if(period!=0&&(stockBits&period)==0){getOffers().forEach(MerchantOffer::resetUses);stockBits|=period;}
    }
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source,float amount){boolean hit=super.hurt(source,amount);if(hit&&!level().isClientSide&&source.getEntity() instanceof LivingEntity enemy){for(var n:level().getEntitiesOfClass(ChudPerson.class,getBoundingBox().inflate(24))){if(enemy instanceof Player p){var aggression=n.getPersistentData().getCompound("ChudAggressors");long now=level().getGameTime();for(String key:new ArrayList<>(aggression.getAllKeys()))if(aggression.getLong(key)<=now)aggression.remove(key);aggression.putLong(p.getUUID().toString(),now+1200);n.getPersistentData().put("ChudAggressors",aggression);n.getPersistentData().putLong("ChudAggressorUntil",now+1200);}if(n.role()==3&&n.hasLineOfSight(enemy)&&!(enemy instanceof ChudPerson))n.setTarget(enemy);}}return hit;}
    public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putInt("ChudRole",role());n.putBoolean("RareStock",rareStock);n.putLong("StockDay",stockDay);n.putInt("StockBits",stockBits);if(home!=null)n.putLong("Home",home.asLong());}
    public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);if(n.contains("ChudRole"))entityData.set(ROLE,Math.clamp(n.getInt("ChudRole"),0,3));rareStock=n.getBoolean("RareStock");stockDay=n.getLong("StockDay");stockBits=n.getInt("StockBits");if(n.contains("Home")){home=BlockPos.of(n.getLong("Home"));restrictTo(home,10);}guardEquipment();}
    public void remove(RemovalReason reason){if(!level().isClientSide&&getTradingPlayer()!=null){getTradingPlayer().closeContainer();setTradingPlayer(null);}super.remove(reason);}
    public void die(net.minecraft.world.damagesource.DamageSource source){if(!level().isClientSide&&getTradingPlayer()!=null){getTradingPlayer().closeContainer();setTradingPlayer(null);}super.die(source);}
}
