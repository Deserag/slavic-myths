# Исходные свидетельства

Автоматические выдержки; наличие строки не доказывает достижимость ветки или итоговый урон. Единицы времени — тики, расстояния нужно проверять на squared comparisons.

## src/main/java/org/slavicmyths/artifact/ArtifactEvents.java

```java
21:  public static boolean eligible(Mob e){return e.canChangeDimensions(e.level(),e.level())&&!summoned(e)&&e.getMaxHealth()<100&&!e.getTags().contains("slavic_music_immune")&&(!net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getNamespace().equals("slavicmyths")||e instanceof WildlifeEntity);}
24:   for(Mob mob:p.level().getEntitiesOfClass(Mob.class,p.getBoundingBox().inflate(12),e->e.isAlive()&&e.distanceToSqr(p)<=144)){
35:  @SubscribeEvent public static void attack(LivingIncomingDamageEvent event){Entity attacker=event.getSource().getEntity();if(attacker instanceof LivingEntity&&(calm((LivingEntity)attacker)||(summoned((LivingEntity)attacker)&&!enemy((LivingEntity)attacker,event.getEntity()))))event.setCanceled(true);}
```

## src/main/java/org/slavicmyths/bandit/BanditEntity.java

```java
2: import net.minecraft.world.entity.SpawnGroupData;
8: import net.minecraft.world.entity.ai.attributes.Attribute;
12: import net.minecraft.world.entity.ai.attributes.*;
26: import net.minecraft.world.damagesource.DamageSource;
49:     private long commandUntil,shieldUntil;private int commandCooldown=100;
51:     public static AttributeSupplier.Builder attributes(int role){return createMonsterAttributes().add(Attributes.MAX_HEALTH,role==ATAMAN?150:role==HEAVY?50:role==SENIOR?44:role==ARCHER?36:30).add(Attributes.MOVEMENT_SPEED,role==HEAVY?.23:role==ATAMAN?.27:.28).add(Attributes.ATTACK_DAMAGE,2).add(Attributes.FOLLOW_RANGE,24).add(Attributes.KNOCKBACK_RESISTANCE,role==HEAVY?.35:role==ATAMAN?.3:0);}
66:     @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor world,DifficultyInstance difficulty,MobSpawnType reason,SpawnGroupData data){
67:         SpawnGroupData result=super.finalizeSpawn(world,difficulty,reason,data);entityData.set(FACE,random.nextInt(4));
76:     @Override public boolean hurt(DamageSource source,float amount){
80:         return super.hurt(source,amount);
84:     @Override protected SoundEvent getHurtSound(DamageSource s){return ModSounds.BANDIT_HURT.get();}
89:     @Override public void die(DamageSource source){
93:                 for(BanditEntity b:level().getEntitiesOfClass(BanditEntity.class,getBoundingBox().inflate(24),e->Objects.equals(e.camp,camp)))b.commandUntil=0;}
97:         private int timer,cooldown,repath,shots;private boolean heavy;
101:         public void start(){timer=0;cooldown=10;}
105:             double distance=distanceToSqr(target);boolean sight=hasLineOfSight(target);if(cooldown>0)cooldown--;if(repath>0)repath--;
106:             if(role==ATAMAN&&--commandCooldown<=0){commandCooldown=180;playSound(ModSounds.BANDIT_COMMAND.get(),.8F,1);
107:                 for(BanditEntity b:level().getEntitiesOfClass(BanditEntity.class,getBoundingBox().inflate(20),e->e!=BanditEntity.this&&Objects.equals(camp,e.camp)&&(campTotal!=StrongholdRecords.TOTAL||e.zone==zone))){b.commandUntil=level().getGameTime()+160;if(b.getTarget()==null)b.setTarget(target);}if(timer==0)action(6);}
111:                 if(action()==4){if(timer==0){stopUsingItem();action(3);cooldown=8;}return;}
116:                         if(heavy)getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(power);
117:                         try {hit=doHurtTarget(target);} finally {if(heavy)getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(power);}
121:                     playSound(ModSounds.BANDIT_ATTACK.get(),.65F,heavy?.8F:1);action(3);cooldown=heavy?30:getMainHandItem().getItem() instanceof SwordItem?15:25;
124:             if(cooldown>0){getNavigation().stop();return;}
135:             getNavigation().stop();if(cooldown>0)return;if(!isUsingItem()){startUsingItem(InteractionHand.MAIN_HAND);action(5);shots=0;}
136:             if(++shots>=24){Arrow arrow=new Arrow(level(),BanditEntity.this,new ItemStack(Items.ARROW),getMainHandItem());double x=target.getX()-getX(),z=target.getZ()-getZ();arrow.setBaseDamage(2);arrow.shoot(x,target.getY(.5)-arrow.getY()+Math.sqrt(x*x+z*z)*.18,z,1.6F,6);level().addFreshEntity(arrow);playSound(SoundEvents.SKELETON_SHOOT,.65F,1);stopUsingItem();action(3);cooldown=35;shots=0;}
```

## src/main/java/org/slavicmyths/bandit/CampPiece.java

```java
63:         if(!marker.startsWith("spawn:"))return;world.setBlock(pos,Blocks.AIR.defaultBlockState(),2);
69:         mob.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,180,0);mob.finalizeSpawn(world,world.getCurrentDifficultyAt(pos),MobSpawnType.STRUCTURE,null);
71:         if(world.addFreshEntity(mob)){processed|=1<<local;world.getLevel().getServer().execute(()->records.spawned(camp,slot,total));}
```

## src/main/java/org/slavicmyths/bandit/LargeCampPiece.java

```java
20: import net.minecraft.world.entity.ai.attributes.Attributes;
29: import net.minecraft.world.damagesource.DamageSource;
68:   if(parts[0].equals("nightingale")){w.setBlock(pos,Blocks.AIR.defaultBlockState(),2);if(!bossProcessed&&!records.nightingaleProcessed(camp)){NightingaleEntity boss=ModEntities.NIGHTINGALE.get().create(w.getLevel());boss.camp=camp;boss.home=pos;boss.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,180,0);boss.setUUID(UUID.nameUUIDFromBytes((camp+":nightingale").getBytes(StandardCharsets.UTF_8)));if(w.addFreshEntity(boss)){bossProcessed=true;w.getLevel().getServer().execute(()->records.nightingaleSpawned(camp));}}return;}
72:   if(!parts[0].equals("spawn"))return;int slot=Integer.parseInt(parts[2]);w.setBlock(pos,Blocks.AIR.defaultBlockState(),2);if((processed&(1L<<slot))!=0||records.processed(camp,slot))return;
74:   if(role.equals("horse")){if(r.nextInt(4)==0){processed|=1L<<slot;w.getLevel().getServer().execute(()->records.spawned(camp,slot));return;}Horse horse=EntityType.HORSE.create(w.getLevel());horse.finalizeSpawn(w,w.getCurrentDifficultyAt(pos),MobSpawnType.STRUCTURE,null);horse.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.23+r.nextDouble()*.09);horse.setPersistenceRequired();entity=horse;}
75:   else if(role.equals("prisoner")){int choice=r.nextInt(3);if(choice==0){processed|=1L<<slot;w.getLevel().getServer().execute(()->records.spawned(camp,slot));return;}Mob prisoner=(choice==1?EntityType.VILLAGER:EntityType.WANDERING_TRADER).create(w.getLevel());prisoner.finalizeSpawn(w,w.getCurrentDifficultyAt(pos),MobSpawnType.STRUCTURE,null);prisoner.setPersistenceRequired();entity=prisoner;}
78:    BanditEntity mob=type.create(w.getLevel());mob.finalizeSpawn(w,w.getCurrentDifficultyAt(pos),MobSpawnType.STRUCTURE,null);mob.camp=camp;mob.campMember=slot;mob.campTotal=StrongholdRecords.TOTAL;mob.zone=Integer.parseInt(parts[3]);mob.duty(Integer.parseInt(parts[4]));mob.home=pos;mob.restrictTo(pos,36);mob.setPersistenceRequired();
79:    if(role.startsWith("ataman")){boolean gate=role.equals("ataman_gate");mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(150);mob.setHealth(mob.getMaxHealth());mob.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(gate?.25:.31);mob.setItemSlot(EquipmentSlot.HEAD,new ItemStack(gate?Items.IRON_HELMET:Items.CHAINMAIL_HELMET));if(!gate){mob.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_SWORD));mob.setItemSlot(EquipmentSlot.CHEST,new ItemStack(ModItems.GAMBESON.get()));mob.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);}}
82:   entity.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,180,0);entity.setUUID(UUID.nameUUIDFromBytes((camp+":"+slot).getBytes(StandardCharsets.UTF_8)));if(w.addFreshEntity(entity)){processed|=1L<<slot;w.getLevel().getServer().execute(()->records.spawned(camp,slot));}
```

## src/main/java/org/slavicmyths/bandit/NightingaleEntity.java

```java
4: import net.minecraft.world.entity.ai.attributes.Attribute;
9: import net.minecraft.world.entity.ai.attributes.*;
20: import net.minecraft.world.damagesource.DamageSource;
38:     public enum Phase { PHASE_ONE, TRANSFORMING, PHASE_TWO }
47:     private final Map<Integer,Integer> attackCooldowns=new HashMap<>();
48:     private record Threat(float damage,int time) {}
62:     private boolean engaged,lootReleased;private DamageSource fatal;
64:     public NightingaleEntity(EntityType<? extends NightingaleEntity> type,Level world){super(type,world);fatal=damageSources().generic();setPersistenceRequired();xpReward=50;getNavigation().setCanFloat(true);}
66:     public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,250).add(Attributes.MOVEMENT_SPEED,.265).add(Attributes.ATTACK_DAMAGE,10).add(Attributes.FOLLOW_RANGE,32).add(Attributes.KNOCKBACK_RESISTANCE,.65);}
80:     private double threatScore(LivingEntity e){Threat t=threats.get(e.getUUID());return (e instanceof Player?300:8)+(t==null?0:t.damage()*2*Math.max(0,1-(tickCount-t.time())/200.0))+Math.max(0,12-distanceTo(e));}
85:         for(LivingEntity e:level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(32),e->valid(e)&&(BossTargets.automatic(e)||threats.containsKey(e.getUUID()))))
89:     private void startAttack(int a,int duration,int cooldown){begin(a,duration);if(a==DASH)playSound(ModSounds.boss("nightingale_phase2_dash"),1,1);actionTick=0;leapImpactReleased=false;dashImpactReleased=false;actionOrigin=position();leapDistance=getTarget()==null?0:Math.min(8,distanceTo(getTarget()));dashDistance=getTarget()==null?0:Math.min(10,distanceTo(getTarget())+1.5);attackCooldowns.put(a,cooldown);}
97:         attackCooldowns.replaceAll((a,t)->Math.max(0,t-1));selectTarget();transformIfNeeded();
99:         getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.265*(second?1.2:1));getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(second?12:10);
104:             if(getTarget()!=null)for(Player p:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(36),this::valid)){
121:         if(!engaged){engaged=true;entityData.set(DRAWN,true);playSound(ModSounds.boss("nightingale_phase1_aggro"),1,1);if(camp!=null)for(BanditEntity b:level().getEntitiesOfClass(BanditEntity.class,getBoundingBox().inflate(22),b->camp.equals(b.camp)))b.engage(target);}
144:     private boolean ready(int action){return attackCooldowns.getOrDefault(action,0)==0;}
165:         for(LivingEntity victim:w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(12),this::valid)){
168:                 victim.hurt(damageSources().mobAttack(this),14);
174:     private boolean melee(LivingEntity target,float damage,double range){
176:             if(action()!=QUICK&&hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS))damage=Math.max(0,damage-4*(getEffect(net.minecraft.world.effect.MobEffects.WEAKNESS).getAmplifier()+1));
177:             boolean hit=target.hurt(damageSources().mobAttack(this),damage);if(hit){target.knockback(.35,getX()-target.getX(),getZ()-target.getZ());target.hurtMarked=true;}return hit;
180:     private void wave(float damage,double range,boolean radial){
181:         WindAttack.release(this,aim,range,damage,2.2F,radial,false);for(int radius=1;radius<=range;radius+=2)WindAttack.particles((ServerLevel)level(),position().add(0,1,0),aim,radius,radial);
185:         if(a==QUICK&&t==10){melee(target,(float)getAttributeValue(Attributes.ATTACK_DAMAGE),2.2);playSound(ModSounds.boss(second?"nightingale_phase2_slash":"nightingale_phase1_slash"),.9F,second?1.3F:1);}
201:                     for(LivingEntity victim:level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(2.2),this::valid))if(distanceTo(victim)<=2.2&&Math.abs(getY()-victim.getY())<1.8&&hasLineOfSight(victim))victim.hurt(damageSources().mobAttack(this),16);
215:                 for(LivingEntity e:level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(2),e->valid(e)&&(BossTargets.automatic(e)||threats.containsKey(e.getUUID()))))if(distanceTo(e)<=2&&hasLineOfSight(e))e.hurt(damageSources().mobAttack(this),12);
234:     @Override public boolean hurt(DamageSource source,float amount){
236:         boolean hit=super.hurt(source,amount);
239:                 Threat old=threats.get(attacker.getUUID());threats.put(attacker.getUUID(),new Threat(Math.min(100,amount+(old==null?0:old.damage())),tickCount));
249:     @Override public boolean causeFallDamage(float distance,float multiplier,net.minecraft.world.damagesource.DamageSource source){return phase()==Phase.PHASE_TWO?false:super.causeFallDamage(Math.max(0,distance-7),multiplier,source);}
253:     @Override protected SoundEvent getHurtSound(DamageSource s){return ModSounds.boss(phase()==Phase.PHASE_TWO?"nightingale_phase2_hurt":"nightingale_phase1_hurt");}
258:     @Override public void die(DamageSource source){
268:     @Override protected void dropAllDeathLoot(ServerLevel world,DamageSource source){fatal=source;} // deferred until the collapse
273:     @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(camp!=null)n.putUUID("NightingaleCamp",camp);if(home!=null)n.putLong("NightingaleHome",home.asLong());n.putString("NightingalePhase",phase().name());CompoundTag cds=new CompoundTag();attackCooldowns.forEach((a,t)->cds.putInt(Integer.toString(a),t));n.put("NightingaleCooldowns",cds);n.putBoolean("NightingaleLoot",lootReleased);n.putInt("CollapseTicks",deathTicks);ListTag list=new ListTag();for(UUID id:participants)list.add(StringTag.valueOf(id.toString()));n.put("Witnesses",list);}
274:     @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);camp=n.hasUUID("NightingaleCamp")?n.getUUID("NightingaleCamp"):null;home=n.contains("NightingaleHome")?BlockPos.of(n.getLong("NightingaleHome")):null;lootReleased=n.getBoolean("NightingaleLoot");deathTicks=n.getInt("CollapseTicks");for(Tag v:n.getList("Witnesses",8))try{participants.add(UUID.fromString(v.getAsString()));}catch(IllegalArgumentException ignored){}entityData.set(PHASE,getHealth()<=getMaxHealth()*.5F||n.getString("NightingalePhase").equals("PHASE_TWO")||n.getString("NightingalePhase").equals("TRANSFORMING")?2:0);attackCooldowns.clear();CompoundTag cds=n.getCompound("NightingaleCooldowns");for(String key:cds.getAllKeys())try{attackCooldowns.put(Integer.parseInt(key),Math.max(0,Math.min(440,cds.getInt(key))));}catch(NumberFormatException ignored){}begin(getHealth()<=0?DYING:IDLE,0);if(getHealth()<=0)entityData.set(START,(int)level().getGameTime()-deathTicks);recovery=40;}
```

## src/main/java/org/slavicmyths/bandit/WindImpulse.java

```java
12:     public static final TagKey<EntityType<?>> IMMUNE = TagKey.create(Registries.ENTITY_TYPE,
13:         ResourceLocation.fromNamespaceAndPath("slavicmyths", "wind_immune"));
15:         if (victim.getType().is(IMMUNE) || strength <= 0) return;
```

## src/main/java/org/slavicmyths/bayun/BayunContent.java

```java
7: import net.minecraft.world.entity.ai.attributes.*;
18:     public static final DeferredHolder<Item,Item> EGG=ModItems.ITEMS.register("kot_bayun_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(ENTITY,0x302e36,0xeaa23f,new Item.Properties()));
26:     @SubscribeEvent public static void attributes(EntityAttributeCreationEvent e){e.put(ENTITY.get(),PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH,100).add(Attributes.MOVEMENT_SPEED,.20).add(Attributes.FOLLOW_RANGE,16).add(Attributes.KNOCKBACK_RESISTANCE,1).build());}
```

## src/main/java/org/slavicmyths/bayun/BayunEntity.java

```java
13: import net.minecraft.world.damagesource.*;
27:     private enum Phase { GRACE,WARNING,SONG,CHARMED,POUNCING,SAFE,COOLDOWN }
33:     public BayunEntity(EntityType<? extends BayunEntity> type,Level w){super(type,w);setPersistenceRequired();setInvulnerable(true);setCanPickUpLoot(false);getNavigation().setCanFloat(false);}
46:     private void abort(UUID id,Session s){ServerPlayer p=player(id);if(s.phase==Phase.CHARMED||s.phase==Phase.POUNCING){if(p!=null){BayunNetwork.charm(p,getId(),false);tell(p,"released");}}s.phase=Phase.COOLDOWN;s.due=now()+300;s.absent=now();if(id.equals(victim)){victim=null;getNavigation().stop();setDeltaMovement(Vec3.ZERO);recoveryUntil=now()+120;begin("recover");}}
50:             if(!eligible(p)){if(s.phase!=Phase.COOLDOWN)abort(e.getKey(),s);if(now()-s.absent>600){it.remove();}continue;}
51:             if(s.phase==Phase.COOLDOWN){if(now()>=s.due&&distanceToSqr(p)<12*12){s.phase=Phase.GRACE;s.due=now()+160;tell(p,"notice");}continue;}
66:     private void notice(){if(sessions.size()>=32)return;for(ServerPlayer p:level().getEntitiesOfClass(ServerPlayer.class,getBoundingBox().inflate(12),this::eligible)){if(distanceToSqr(p)>144||sessions.containsKey(p.getUUID()))continue;boolean safe=safeMeeting(p);sessions.put(p.getUUID(),new Session(safe?Phase.SAFE:Phase.GRACE,now()+160));if(!safe){tell(p,"notice");cue("mew.soft",.35F);}TaleService.subject(p,"entity/slavicmyths:kot_bayun",2,false);org.slavicmyths.progression.Knowledge.award(p,"meet_bayun");if(clip().startsWith("sleep"))begin("wake_up");if(sessions.size()>=32)break;}}
72:         if(age>=17&&age<=21&&distanceToSqr(p)<=1.8*1.8&&getBoundingBox().inflate(.18).intersects(p.getBoundingBox())&&hasLineOfSight(p)){
73:             var between=getBoundingBox().minmax(p.getBoundingBox()).inflate(.05);boolean intercepted=!level().getEntitiesOfClass(LivingEntity.class,between,e->e!=this&&e!=p&&e.isAlive()).isEmpty();
74:             if(!intercepted){ResourceKey<DamageType> key=ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,BayunContent.id("bayun_claws"));p.hurt(new DamageSource(level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(key),this),Math.max(1000,p.getMaxHealth()+p.getAbsorptionAmount()+100));abort(p.getUUID(),s);}
78:     private void ambient(){if(now()<clipEnd&&!BayunAnimations.loop(clip()))return;if(clip().equals("sleep_curl_in")){begin("sleep_curl_loop");return;}if(clip().equals("wake_up")){begin("stretch_front");return;}if(!getNavigation().isDone()){if(!clip().equals("walk"))begin("walk");return;}if(clip().equals("walk"))begin("idle_sit");if(now()<nextAmbient)return;nextAmbient=now()+600+random.nextInt(1201);int roll=random.nextInt(level().isNight()?107:100);if(roll>=100){begin("sleep_curl_in");return;}if(roll<28)begin("idle_sit");else if(roll<48)begin("loaf_rest");else if(roll<72)begin("sleep_curl_in");else if(roll<82)begin("groom_paw");else if(roll<89)begin("stretch_front");else{BlockPos target=home.offset(random.nextInt(9)-4,0,random.nextInt(9)-4);if(level().hasChunkAt(target)&&level().getFluidState(target).isEmpty()&&level().noCollision(this,new AABB(target).inflate(.1))&&level().getBlockState(target.below()).isSolid()){getNavigation().moveTo(target.getX()+.5,target.getY(),target.getZ()+.5,.8);begin("walk");}else begin("turn_attention");}}
89:     @Override public boolean hurt(DamageSource s,float amount){return false;}
90:     @Override public boolean isInvulnerableTo(DamageSource s){return true;}
103:     @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);home=n.contains("Home")?BlockPos.of(n.getLong("Home")):blockPosition();sessions.clear();victim=null;setInvulnerable(true);recoveryUntil=now()+Math.clamp(n.getLong("Recovery"),0,120);nextAmbient=now()+Math.clamp(n.getLong("AmbientRemaining"),0,1800);begin(n.getString("Clip").startsWith("sleep")?"sleep_curl_loop":"idle_sit");}
```

## src/main/java/org/slavicmyths/block/KitchenTableBlock.java

```java
18:  public enum Part implements StringRepresentable{LEFT,RIGHT;public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}}
```

## src/main/java/org/slavicmyths/bogatyr/BogatyrContent.java

```java
7: import net.minecraft.world.entity.ai.attributes.Attributes;
17:     public static final DeferredHolder<EntityType<?>,EntityType<IdolishcheEntity>> IDOL=ModEntities.ENTITIES.register("idolishche_poganoe",()->EntityType.Builder.of(IdolishcheEntity::new,MobCategory.MONSTER).sized(4.2F,8.8F).clientTrackingRange(12).updateInterval(1).fireImmune().build("slavicmyths:idolishche_poganoe"));
26:         e.put(ILYA.get(),net.minecraft.world.entity.Mob.createMobAttributes().add(Attributes.MAX_HEALTH,280).add(Attributes.MOVEMENT_SPEED,.28).add(Attributes.ATTACK_DAMAGE,12).add(Attributes.FOLLOW_RANGE,48).add(Attributes.KNOCKBACK_RESISTANCE,.5).build());
27:         e.put(IDOL.get(),net.minecraft.world.entity.monster.Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,900).add(Attributes.MOVEMENT_SPEED,.24).add(Attributes.ATTACK_DAMAGE,20).add(Attributes.FOLLOW_RANGE,72).add(Attributes.KNOCKBACK_RESISTANCE,1).add(Attributes.STEP_HEIGHT,1).build());
```

## src/main/java/org/slavicmyths/bogatyr/BogatyrEvents.java

```java
5: import net.minecraft.world.damagesource.DamageTypes;
13:     @SubscribeEvent public static void incoming(LivingIncomingDamageEvent e){if(!(e.getEntity() instanceof ServerPlayer p)||e.getAmount()<=0)return;if(e.getSource().getEntity() instanceof IdolishcheEntity)p.getPersistentData().putFloat("IdolHpBefore",p.getHealth());var h=BogatyrLedger.hero(p);if(h!=null&&h.canIntercept(p,e.getSource())){h.interceptUsed=true;float amount=e.getAmount();e.setCanceled(true);h.hurt(e.getSource(),amount);}}
14:     @SubscribeEvent public static void pre(LivingDamageEvent.Pre e){if(e.getEntity().level().isClientSide)return;if(e.getEntity() instanceof IlyaEntity h)e.setNewDamage(e.getNewDamage()*h.mitigation(e.getSource(),e.getNewDamage()));if(e.getSource().is(DamageTypes.FALL)){var n=e.getEntity().getPersistentData();if(n.getLong("IdolThrowUntil")>e.getEntity().level().getGameTime()){e.setNewDamage(Math.min(4,e.getNewDamage()));n.remove("IdolThrowUntil");}}
15:         if(e.getEntity() instanceof IdolishcheEntity b&&e.getSource().getEntity() instanceof ServerPlayer p){var n=p.getPersistentData();if(n.hasUUID("IdolComboBoss")&&n.getUUID("IdolComboBoss").equals(b.getUUID())&&n.getLong("IdolComboUntil")>=b.level().getGameTime()){e.setNewDamage(e.getNewDamage()*1.2F);n.remove("IdolComboBoss");n.remove("IdolComboUntil");}}}
16:     @SubscribeEvent public static void post(LivingDamageEvent.Post e){if(e.getSource().getEntity() instanceof IdolishcheEntity source)source.outgoingHit(e.getEntity(),e.getNewDamage());if(e.getEntity() instanceof IdolishcheEntity b)b.registeredHit(e.getSource(),e.getOriginalDamage(),e.getNewDamage());if(e.getSource().getEntity() instanceof IdolishcheEntity b&&e.getEntity() instanceof ServerPlayer p){b.contribution(p);b.lastDamageReport=String.format(java.util.Locale.ROOT,"[Boss171] target=%s hit=%s armor=%d toughness=%.1f hpBefore=%.3f hpAfter=%.3f preArmorDamage=%.3f postArmorDamage=%.3f postEffectsDamage=%.3f affectedByShield=%s statusEffects=%s",p.getUUID(),e.getSource().getMsgId(),p.getArmorValue(),p.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS),p.getPersistentData().getFloat("IdolHpBefore"),p.getHealth(),e.getOriginalDamage(),e.getOriginalDamage()-e.getReduction(net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction.ARMOR),e.getNewDamage(),p.isBlocking(),p.getActiveEffects());org.slf4j.LoggerFactory.getLogger("Boss171").info(b.lastDamageReport);}if(e.getEntity() instanceof IlyaEntity h&&h.ready("pain")&&e.getNewDamage()>0){h.cooldown("pain",16);h.cue(h.getRandom().nextBoolean()?"pain_nonverbal_01":"pain_nonverbal_02");}}
20:     @SubscribeEvent public static void landing(LivingFallEvent e){if(!(e.getEntity().level() instanceof ServerLevel w))return;var n=e.getEntity().getPersistentData();if(n.getLong("IdolThrowUntil")<w.getGameTime()||!n.hasUUID("IdolThrowBoss"))return;var id=n.getUUID("IdolThrowBoss");n.remove("IdolThrowUntil");n.remove("IdolThrowBoss");e.setDamageMultiplier(0);if(w.getEntity(id) instanceof IdolishcheEntity b)e.getEntity().hurt(IdolishcheEntity.source(b,"grasp"),10);}
```

## src/main/java/org/slavicmyths/bogatyr/BogatyrRules.java

```java
5:     public enum State {
15:     public static boolean rage(float hp){return hp<=315;}
16:     public static boolean retreat(float hp){return hp<=42;}
17:     public static boolean unyielding(float hp){return hp<=84;}
```

## src/main/java/org/slavicmyths/bogatyr/CombatEntity.java

```java
14:     public final Map<String,Long> cooldowns=new HashMap<>();
23:     public boolean ready(String key){return level().getGameTime()>=cooldowns.getOrDefault(key,0L);}
24:     public void cooldown(String key,int ticks){cooldowns.put(key,level().getGameTime()+ticks);}
32:     @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putString("CombatState",state().name());n.putInt("CombatDuration",duration());n.putInt("CombatElapsed",elapsed());n.putInt("CombatRemaining",Math.max(0,duration()-elapsed()));var cds=new CompoundTag();cooldowns.forEach((k,v)->cds.putLong(k,v));n.put("CombatCooldowns",cds);}
33:     @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);var cds=n.getCompound("CombatCooldowns");for(String k:cds.getAllKeys())cooldowns.put(k,cds.getLong(k));begin(State.IDLE,0);}
```

## src/main/java/org/slavicmyths/bogatyr/IdolishcheEntity.java

```java
11: import net.minecraft.tags.DamageTypeTags;
13: import net.minecraft.world.damagesource.*;
33:     private long startTick,lastPlayerHit,lastDanger,tauntUntil,tauntImmune,comboUntil,lastValidTick,introStart;
38:     private record Pressure(long tick,UUID target,float damage){}
41:     public void outgoingHit(LivingEntity target,float damage){long now=level().getGameTime();while(!pressure.isEmpty()&&now-pressure.peekFirst().tick>300)pressure.removeFirst();pressure.addLast(new Pressure(now,target.getUUID(),damage));}
42:     private boolean staticTank(){float total=0,hero=0;long now=level().getGameTime();for(var e:pressure)if(now-e.tick<=300){total+=e.damage;if(level() instanceof ServerLevel w&&w.getEntity(e.target) instanceof IlyaEntity)hero+=e.damage;}return total>=40&&hero/total>.7F;}
47:     public String lastDamageReport="No measured player hit";
65:     public void start(ServerPlayer p,boolean debug){this.debug=debug;initiator=p.getUUID();origin=position();active=true;entityData.set(ACTIVE,true);entityData.set(ORIGIN,BlockPos.containing(origin));startTick=level().getGameTime();lastValidTick=startTick;lastPlayerHit=startTick;encounter=UUID.randomUUID();if(!debug)for(var h:level().getEntitiesOfClass(IlyaEntity.class,getBoundingBox().inflate(40),h->h.isAlive()&&h.owner!=null)){h.bind(this);alliesAtStart.add(h.owner);}cue("roar_01");}
68:     private List<LivingEntity> targets(double r){return level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(r),this::valid);}
69:     public void taunt(IlyaEntity h,boolean crisis){long now=level().getGameTime();if(now<tauntImmune&&!crisis)return;taunted=h.getUUID();tauntUntil=now+(now<tauntImmune?60:120);tauntImmune=tauntUntil+260;setTarget(h);}
71:     public static DamageSource source(LivingEntity owner,String attack){var type=ResourceKey.create(Registries.DAMAGE_TYPE,BogatyrContent.id("idolishche_"+attack));return new DamageSource(owner.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type),owner);}
76:         if(tickCount%10==0){boolean any=false;for(var p:w.getEntitiesOfClass(ServerPlayer.class,new AABB(origin,origin).inflate(72),p->p.isAlive()&&!p.isCreative()&&!p.isSpectator())){double d=p.position().multiply(1,0,1).distanceToSqr(origin.multiply(1,0,1));if(d<=26*26){any=true;bar.addPlayer(p);}else{bar.removePlayer(p);if(BogatyrConfig.HINTS.get())p.displayClientMessage(Component.translatable("gui.slavicmyths.171.leave_arena"),true);}}if(debug&&org.slavicmyths.bogatyr.HeroThreats.debugOpponent(this,26)!=null)any=true;if(any)lastValidTick=w.getGameTime();if(w.getGameTime()-lastValidTick>=100){reset();return;}
77:             if(!intro){for(var h:w.getEntitiesOfClass(IlyaEntity.class,getBoundingBox().inflate(24),h->encounter.equals(h.encounter))){intro=true;introStart=w.getGameTime();for(var p:bar.getPlayers()){p.sendSystemMessage(Component.translatable("dialogue.slavicmyths.171.ilya"));}h.cue("taunt_nonverbal_01");break;}}
80:         for(var id:new ArrayList<>(poisonAt.keySet()))if(w.getGameTime()>=poisonAt.get(id)){if(w.getEntity(id) instanceof LivingEntity e&&valid(e))e.hurt(w.damageSources().indirectMagic(this,this),1);int left=poisonTicks.getOrDefault(id,0)-1;if(left<=0){poisonAt.remove(id);poisonTicks.remove(id);}else{poisonTicks.put(id,left);poisonAt.put(id,w.getGameTime()+40);}}
81:         if(!rage()&&BogatyrRules.rage(getHealth())&&victim==null){entityData.set(RAGE,true);getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).setBaseValue(.24*1.18);begin(State.RAGE_TRANSITION,65);}
94:         if(getHealth()<765&&feeds<2&&food!=null&&ready("feed")){minion=food.getUUID();cooldown("feed",800);begin(State.FEED_GRAB,24);return;}
95:         if(feeds<2&&age>=240&&ready("summon")&&ownMinions(w).size()<5&&(food==null||hunger==2)){cooldown("summon",840);begin(State.FEED_SUMMON,50);return;}
96:         if(distanceToSqr(t)>=81&&distanceToSqr(t)<=529&&ready("charge")){cooldown("charge",340);begin(State.CHARGE_WINDUP,28);return;}
97:         if(age>=80&&distanceToSqr(t)<225&&ready("stomp")){cooldown("stomp",300);begin(State.STOMP_TELEGRAPH,32);return;}
98:         if(edge(t)<6&&ready("grab")&&w.getGameTime()>=grabImmunity.getOrDefault(t.getUUID(),0L)){cooldown("grab",360);begin(State.GRAB_START,25);return;}
99:         if(edge(t)<11&&ready("breath")){cooldown("breath",420);begin(State.BREATH_WINDUP,25);return;}
104:         case FIST_SLAM->{if(t==8){cue("punch_impact_01");for(var e:targets(5))if(cone(e,4.8,95,heading())&&e.getY()<getY()+3&&hasLineOfSight(e))damage(e,"fist",20, .8);dust(25);lastDanger=w.getGameTime();}}
105:         case STOMP_PULSE->{if(t==4||t==10||t==16){int ring=(t-4)/6;cue(switch(ring){case 0->"stomp_near_01";case 1->"stomp_mid_01";default->"stomp_outer_01";});for(var e:targets(16)){double r=e.position().multiply(1,0,1).distanceTo(originContact().multiply(1,0,1));int zone=r<=5?0:r<=10?1:r<=15?2:-1;if(zone!=ring||hit.contains(e.getUUID())||!groundVisible(e))continue;hit.add(e.getUUID());float raw=BogatyrRules.stompRaw(ring);if(e.getY()>getY()+.55&&!e.onGround())raw*=.45F;damage(e,"stomp",raw,.65);}lastDanger=w.getGameTime();dust(40);}}
106:         case GRAB_HOLD->{if(t==8&&victim==null){var target=getTarget();if(valid(target)&&cone(target,6,62,heading())&&hasLineOfSight(target)){victim=target.getUUID();entityData.set(VICTIM,target.getId());damage(target,"grasp",18,0);cue("grasp_01");}}}
111:         case BREATH_ACTIVE->{if(t==1)cue("poison_breath_01");if(t%10==0){for(var e:targets(13))if(cone(e,11,75,locked)&&hasLineOfSight(e)&&e.getY()<getY()+4&&!hit.contains(e.getUUID())){hit.add(e.getUUID());damage(e,"breath",20,0);e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,60));e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,80));e.addEffect(new MobEffectInstance(MobEffects.CONFUSION,50));poisonAt.put(e.getUUID(),w.getGameTime()+40);poisonTicks.put(e.getUUID(),2);}w.sendParticles(net.minecraft.core.particles.ColorParticleOption.create(net.minecraft.core.particles.ParticleTypes.ENTITY_EFFECT,0xFF879F43),getX()+locked.x*4,getY()+2,getZ()+locked.z*4,14,2,1,2,.01);}lastDanger=w.getGameTime();}
112:         case CHARGE_RUNNING->{if(t==1)cue("rush_charge_01");setYRot(lockedYaw());yBodyRot=getYRot();Vec3 next=position().add(locked.scale(.31));if(!w.hasChunkAt(BlockPos.containing(next))||!w.getWorldBorder().isWithinBounds(BlockPos.containing(next))){begin(State.SHIELD_RECOVER,15);return;}setDeltaMovement(locked.scale(.31).add(0,getDeltaMovement().y,0));for(var e:targets(.8))if(!hit.contains(e.getUUID())&&getBoundingBox().inflate(.4).intersects(e.getBoundingBox())){hit.add(e.getUUID());if(e instanceof IlyaEntity h&&h.shield()&&h.ready("contest")&&h.heading().dot(locked)<-.42){h.cooldown("contest",280);h.hurt(source(this,"charge"),32);setDeltaMovement(Vec3.ZERO);contestHero=h.getUUID();begin(State.CONTEST,24);return;}damage(e,"charge",22,1.1);lastDanger=w.getGameTime();}if(horizontalCollision&&thickWall(w)){setDeltaMovement(Vec3.ZERO);begin(State.STUNNED,50);}dust(14);}
119:         case FIST_SLAM->{cooldown("fist",rage()?61:72);begin(State.SHIELD_RECOVER,16);}
135:     public void struggle(ServerPlayer p){if(grabbed(p)&&ready("struggle")&&(state()==State.GRAB_HOLD&&elapsed()>=8||state()==State.GRAB_THROW&&elapsed()<12)){cooldown("struggle",3);interruptMeter+=5;if(interruptMeter>=30){release(true);begin(State.STUNNED,16);}}}
137:     private void damage(LivingEntity e,String attack,float raw,double knock){if(rage()&&!attack.equals("breath"))raw*=1.1F;if(e.hurt(source(this,attack),raw)&&knock>0)e.knockback(knock,getX()-e.getX(),getZ()-e.getZ());}
139:     private void summon(ServerLevel w){int remaining=Math.min(5-ownMinions(w).size(),rage()?4:2+random.nextInt(2));for(int i=0;i<12&&remaining>0;i++){double a=i*Math.PI/6;var at=BogatyrLedger.safe(w,position().add(Math.cos(a)*6,0,Math.sin(a)*6),.6,1.95,3);if(at==null||at.distanceToSqr(origin)>26*26||!w.getEntitiesOfClass(ServerPlayer.class,new AABB(at,at).inflate(3)).isEmpty())continue;var z=EntityType.ZOMBIE.create(w);if(z==null)continue;z.moveTo(at.x,at.y,at.z,0,0);z.setPersistenceRequired();z.getPersistentData().putUUID("IdolOwner",getUUID());z.getPersistentData().putUUID("IdolEncounter",encounter);z.getPersistentData().putBoolean("feedEligible",true);z.setCanPickUpLoot(false);if(getTarget()!=null)z.setTarget(getTarget());if(w.addFreshEntity(z)){minions.add(z.getUUID());remaining--;}}}
145:     public void registeredHit(DamageSource s,float raw,float actual){if(actual<=0)return;if(s.getEntity() instanceof ServerPlayer p){contribution(p);playerHits.put(p.getUUID(),level().getGameTime());lastPlayerHit=level().getGameTime();if(state()==State.FEED_CHEW&&elapsed()>=12&&elapsed()<=38){interruptMeter+=raw;if(interruptMeter>=32||heroInterrupt>=40&&interruptMeter>=8)interruptFeed();}if(victim!=null&&handHit){interruptMeter+=raw;if(interruptMeter>=25){release(true);begin(State.STUNNED,16);}}if(state()==State.STUNNED&&duration()==50&&ready("combo")){var h=level().getEntitiesOfClass(IlyaEntity.class,getBoundingBox().inflate(4),h1->h1.isAlive()&&!h1.recovering()&&h1.edge(this)<=3.3&&hasLineOfSight(h1)&&encounter.equals(h1.encounter)).stream().findFirst().orElse(null);if(h!=null){cooldown("combo",700);comboUntil=level().getGameTime()+24;h.jointStrikeBoss=getUUID();h.jointStrikeUntil=comboUntil;h.setTarget(this);h.face(this);h.begin(State.MACE_WINDUP,31);h.cooldown("mace",42);p.getPersistentData().putLong("IdolComboUntil",comboUntil);p.getPersistentData().putUUID("IdolComboBoss",getUUID());}}}if(ready("hurt_sound")){cooldown("hurt_sound",32);cue(random.nextBoolean()?"hurt_01":"hurt_02");}}
147:     private void clean(ServerLevel w){release(true);for(var id:minions)if(w.getEntity(id) instanceof Zombie z)z.discard();minions.clear();for(var h:w.getEntitiesOfClass(IlyaEntity.class,getBoundingBox().inflate(80),h->encounter.equals(h.encounter)))h.endEncounter();bar.removeAllPlayers();poisonAt.clear();poisonTicks.clear();}
148:     public void reset(){if(level() instanceof ServerLevel w){clean(w);active=false;entityData.set(ACTIVE,false);feeds=0;hunger=0;contributions.clear();playerHits.clear();pressure.clear();alliesAtStart.clear();intro=false;setTarget(null);entityData.set(RAGE,false);setHealth(900);getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).setBaseValue(.24);cooldowns.clear();begin(State.IDLE,0);}}
149:     @Override public void die(DamageSource s){if(level() instanceof ServerLevel w){if(!debug&&!recorded&&!s.is(DamageTypeTags.BYPASSES_INVULNERABILITY)){recorded=true;for(var e:contributions.entrySet()){var p=w.getServer().getPlayerList().getPlayer(e.getKey());if(p!=null&&!p.isCreative()&&!p.isSpectator()&&p.level()==w&&p.position().distanceToSqr(origin)<=72*72&&w.getGameTime()-e.getValue()<=900)BogatyrLedger.get(w).victory(p,this,alliesAtStart.contains(p.getUUID()));}}clean(w);active=false;entityData.set(ACTIVE,false);}super.die(s);begin(State.DEATH,110);cue("death_01");}
150:     @Override protected void dropAllDeathLoot(ServerLevel w,DamageSource s){}
```

## src/main/java/org/slavicmyths/bogatyr/IlyaEntity.java

```java
28:     public void kalinGuard(org.slavicmyths.kalin.KalinEntity b){face(b);shieldHits=0;interceptUsed=false;cooldown("shield",280);begin(State.SHIELD_RAISE,12);}
41:             for(var shot:w.getEntitiesOfClass(net.minecraft.world.entity.projectile.Projectile.class,getBoundingBox().inflate(14),shot->shot.getOwner()==null||!isAlliedTo(shot.getOwner()))){var velocity=shot.getDeltaMovement();if(velocity.lengthSqr()<.01||p.getBoundingBox().inflate(.4).clip(shot.position(),shot.position().add(velocity.scale(18))).isEmpty())continue;var source=shot.getOwner();if(source instanceof LivingEntity enemy)face(enemy);cooldown("shield",280);shieldHits=0;begin(State.SHIELD_RAISE,12);return;}
44:             if((b.state()==State.FIST_WINDUP||b.state()==State.CHARGE_WINDUP)&&edge(b)<5&&ready("shield")){shieldHits=0;cooldown("shield",280);begin(State.SHIELD_RAISE,12);return;}
45:             if(p!=null&&b.getTarget()==p&&b.state()==State.FIST_WINDUP&&distanceToSqr(p)<25&&ready("intercept")&&elapsed()<18){cooldown("intercept",340);interceptUsed=false;begin(State.PROTECTOR_DASH,14);getNavigation().moveTo(p.getX()+b.heading().x*1.5,p.getY(),p.getZ()+b.heading().z*1.5,.37/.28);return;}
47:             if(b.ownerActive(owner)&&ready("shockwave")&&edge(b)<4.5&&(b.state()==State.CHARGE_WINDUP||w.getEntitiesOfClass(Monster.class,getBoundingBox().inflate(4.5),m->m.isAlive()&&!isAlliedTo(m)).size()>2)){cooldown("shockwave",500);begin(State.SHOCKWAVE_CAST,46);return;}
48:             if(b.ownerActive(owner)&&edge(b)<=3.3&&ready("mace")){cooldown("mace",42);begin(State.MACE_WINDUP,31);return;}
52:             if(enemy!=null){setTarget(enemy);face(enemy);if(p.getHealth()<p.getMaxHealth()*.6&&ready("taunt")&&distanceToSqr(enemy)<=144&&hasLineOfSight(enemy)){begin(State.TAUNT_CAST,28);return;}if(ready("shockwave")&&w.getEntitiesOfClass(Monster.class,getBoundingBox().inflate(4.5),m->m.isAlive()&&!isAlliedTo(m)&&distanceToSqr(m)<=20.25).size()>2){cooldown("shockwave",500);begin(State.SHOCKWAVE_CAST,46);return;}if(edge(enemy)<3.3&&ready("mace")){cooldown("mace",42);begin(State.MACE_WINDUP,31);}else org.slavicmyths.poloz.PolozAim.navigate(this,enemy,1);}
58:         case TAUNT_CAST->{if(t==15){taunt(w,b,false);cooldown("taunt",400);}}
62:         case MACE_WINDUP->{if(t==13)cue("mace_swing_01");if(t==19){var target=getTarget();if(target!=null&&target.isAlive()&&cone(target,3.3,120,heading())&&hasLineOfSight(target)){if(target instanceof org.slavicmyths.poloz.GreatPoloz poloz)org.slavicmyths.poloz.PolozAim.strike(this,poloz,12);else target.hurt(damageSources().mobAttack(this),target instanceof IdolishcheEntity&&target.getUUID().equals(jointStrikeBoss)&&w.getGameTime()<=jointStrikeUntil?15:target instanceof IdolishcheEntity?10:12);if(target instanceof IdolishcheEntity giant)giant.heroMace(this);else target.knockback(.55,getX()-target.getX(),getZ()-target.getZ());cue("mace_impact_01");jointStrikeBoss=null;}}}
63:         case SHOCKWAVE_CAST->{if(t==29){for(var enemy:w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(4.5),e->e.isAlive()&&e!=this&&!isAlliedTo(e)&&(e instanceof Monster||e instanceof IdolishcheEntity||org.slavicmyths.kalin.KalinParty.allowed(this,e))))if(distanceToSqr(enemy)<20.25&&hasLineOfSight(enemy)){enemy.hurt(damageSources().mobAttack(this),enemy instanceof IdolishcheEntity?5:8);if(enemy instanceof IdolishcheEntity giant&&giant.state()==State.CHARGE_WINDUP)giant.begin(State.STUNNED,10);else if(!(enemy instanceof IdolishcheEntity))enemy.knockback(.45,getX()-enemy.getX(),getZ()-enemy.getZ());}dust(25);cue("mace_impact_01");}}
67:     private void taunt(ServerLevel w,IdolishcheEntity b,boolean crisis){var k=org.slavicmyths.kalin.KalinParty.commander(this);if(k!=null&&distanceToSqr(k)<=144&&hasLineOfSight(k)){k.getPersistentData().putUUID("BogatyrTauntHero",getUUID());k.getPersistentData().putLong("BogatyrTauntUntil",w.getGameTime()+120);k.setTarget(this);}cue("taunt_nonverbal_01");if(b!=null&&distanceToSqr(b)<=144&&hasLineOfSight(b))b.taunt(this,crisis);int count=0;for(var m:w.getEntitiesOfClass(Monster.class,getBoundingBox().inflate(12),m->m.isAlive()&&!isAlliedTo(m)&&distanceToSqr(m)<=144&&hasLineOfSight(m))){if(count++>=5)break;m.setTarget(this);m.getPersistentData().putLong("BogatyrTauntUntil",w.getGameTime()+120);m.getPersistentData().putUUID("BogatyrTauntHero",getUUID());}dust(14);}
68:     public float mitigation(net.minecraft.world.damagesource.DamageSource source,float before){if(source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)||source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)||source.is(net.minecraft.world.damagesource.DamageTypes.MAGIC)||source.is(net.minecraft.world.damagesource.DamageTypes.INDIRECT_MAGIC)||source.is(net.minecraft.tags.DamageTypeTags.IS_DROWNING)||source.is(net.minecraft.tags.DamageTypeTags.IS_FREEZING)||source.is(net.minecraft.world.damagesource.DamageTypes.STARVE)||source.getMsgId().contains("breath"))return 1;float scale=.75F;if(level().getGameTime()<unyieldingUntil)scale*=.75F;var at=source.getSourcePosition();if(shield()&&at!=null&&at.subtract(position()).multiply(1,0,1).normalize().dot(heading())>=Math.cos(Math.toRadians(65))){boolean massive=source.getMsgId().contains("stomp")||source.getMsgId().contains("grasp");scale*=massive?.8F:source.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)?.15F:.4F;if(ready("block_sound")){cue(random.nextBoolean()?"shield_block_01":"shield_block_02");cooldown("block_sound",6);}if(before>=6&&++shieldHits>=3)begin(State.SHIELD_RECOVER,24);}return scale;}
69:     public boolean canIntercept(ServerPlayer p,net.minecraft.world.damagesource.DamageSource source){if(!shield()||interceptUsed||!p.getUUID().equals(owner)||distanceToSqr(p)>6.25||source.getSourcePosition()==null)return false;var toward=source.getSourcePosition().subtract(position()).multiply(1,0,1).normalize();var behind=p.position().subtract(position()).multiply(1,0,1).normalize();return toward.dot(heading())>=Math.cos(Math.toRadians(65))&&behind.dot(heading())<=-Math.cos(Math.toRadians(22.5))&&!source.getMsgId().contains("stomp")&&!source.getMsgId().contains("breath")&&!source.getMsgId().contains("grasp");}
80:     @Override public void die(net.minecraft.world.damagesource.DamageSource source){if(level() instanceof ServerLevel w&&owner!=null){var d=BogatyrLedger.get(w);if(getUUID().equals(d.heroes.get(owner))){d.heroes.remove(owner);d.setDirty();}}super.die(source);}
81:     @Override protected void dropAllDeathLoot(ServerLevel w,net.minecraft.world.damagesource.DamageSource s){}
```

## src/main/java/org/slavicmyths/brewing/Brewing.java

```java
19:  // No copyOnDeath: respawn starts sober. Milk never writes this attachment.
```

## src/main/java/org/slavicmyths/chud/AbandonedPlan.java

```java
11:     public enum Kind { ATTACHED_SMALL, INDEPENDENT_MEDIUM, INDEPENDENT_GREAT }
12:     public enum Role { QUARTER, GALLERY, SHAFT, LABYRINTH, SEALED_GATE, SECONDARY }
13:     public enum Module { GUARD_POST, TOOL_ROOM, ORE_SORTING, OLD_INFIRMARY, WORKER_REST, VENT_SHAFT,
```

## src/main/java/org/slavicmyths/combat/CombatEquipment.java

```java
3: import net.minecraft.tags.DamageTypeTags;
6: import net.minecraft.world.entity.ai.attributes.*;
11: import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
19:    return super.getDefaultAttributeModifiers().withModifierAdded(Attributes.MOVEMENT_SPEED,
23:  @SubscribeEvent public static void hurt(LivingIncomingDamageEvent event) {
24:   if(event.getEntity().level().isClientSide || event.getSource().is(DamageTypeTags.IS_PROJECTILE)
```

## src/main/java/org/slavicmyths/combat/CombatTargeting.java

```java
14:     public static final TagKey<EntityType<?>> HOSTILE=tag("village_hostiles"),PROTECTED=tag("combat_protected"),CONTROL_IMMUNE=tag("control_immune");
16:         if(owner==null||target==null||owner==target||owner.isSpectator()||target instanceof net.minecraft.world.entity.decoration.ArmorStand||!owner.isAlive()||!target.isAlive()||target.isRemoved()||target.isSpectator()||target.isInvulnerable()||target.getType().is(PROTECTED)||owner.isAlliedTo(target)||target.isAlliedTo(owner))return false;
24:     public static boolean canControl(LivingEntity owner,LivingEntity target){return canAttack(owner,target)&&!target.getType().is(CONTROL_IMMUNE);}
32:     public static boolean controlResistant(LivingEntity target){return org.slavicmyths.rpg.classes.ClassControl.resistant(target)||target.getType().is(org.slavicmyths.rpg.RareRuneRuntime.IMMUNE);}
```

## src/main/java/org/slavicmyths/combat/CutEffect.java

```java
13: import net.minecraft.world.damagesource.DamageTypes;
16: import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
23:     public static final TagKey<EntityType<?>> IMMUNE=TagKey.create(Registries.ENTITY_TYPE,ResourceLocation.fromNamespaceAndPath("slavicmyths","bleed_immune"));
27:         return target.isAlive()&&!target.getType().is(IMMUNE)&&target!=attacker&&
30:     @SubscribeEvent public static void hit(LivingDamageEvent.Post e){
31:         if(e.getNewDamage()<=0||!e.getSource().is(DamageTypes.PLAYER_ATTACK)||!(e.getSource().getDirectEntity() instanceof ServerPlayer attacker)
41:         if(!target.addEffect(new MobEffectInstance(ModEffects.CUT,100,stacks-1,false,false,true),attacker))return;
49:         if(target.getType().is(IMMUNE))return false;
58:         if(pulse<=0){pulse=20;target.hurt(owner==null?MythDamageSources.unattributed("cut",target):MythDamageSources.periodic("cut",owner),.25F*Math.min(5,amplifier+1));}
```

## src/main/java/org/slavicmyths/combat/MobMaceCombat.java

```java
3: import net.minecraft.tags.DamageTypeTags;
7: import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
9: /** NPC weapon knockback retained independently of the removed HP overlay. */
12:     @SubscribeEvent public static void damage(LivingDamageEvent.Pre event) {
14:         if (victim.level().isClientSide || event.getNewDamage() <= 0 || event.getSource().is(DamageTypeTags.IS_PROJECTILE)) return;
```

## src/main/java/org/slavicmyths/combat/RareCombat.java

```java
6: import net.minecraft.world.damagesource.DamageTypes;
18:     public static final int SWING_WINDOW=80, SECOND_WINDOW=25, SECOND_COOLDOWN=160;
21:     private static boolean melee(net.minecraft.world.damagesource.DamageSource s){return s.is(DamageTypes.PLAYER_ATTACK)&&s.getDirectEntity() instanceof ServerPlayer;}
23:     @SubscribeEvent(priority=EventPriority.LOWEST) public static void attack(AttackEntityEvent e){
31:             PathData.data(p).putLong("CD_rare_second",now(p)+SECOND_COOLDOWN);
32:             // Native Player.attack still owns damage, enchantments, exhaustion and durability.
33:             p.attackStrengthTicker=1000;target.invulnerableTime=0;
36:     @SubscribeEvent(priority=EventPriority.HIGH) public static void incoming(LivingIncomingDamageEvent e){
49:         if(!e.getOriginalBlock()||!melee(e.getDamageSource()))return;
50:         var p=(ServerPlayer)e.getDamageSource().getDirectEntity();if(!p.getPersistentData().getBoolean("RareSwingBoost"))return;
51:         if(e.getEntity() instanceof Player defender){defender.getCooldowns().addCooldown(defender.getUseItem().getItem(),60);defender.stopUsingItem();}
54:         e.setBlockedDamage(e.getOriginalBlockedDamage()*.5F);
56:     @SubscribeEvent public static void hit(LivingDamageEvent.Post e){
57:         if(e.getNewDamage()<=0||!melee(e.getSource()))return;
59:         if(paired(p)&&d.getFloat("RareAttackCharge")>=.9F){RareBleedEffect.apply(p,target);p.swing(InteractionHand.OFF_HAND,true);p.getMainHandItem().hurtAndBreak(1,p,EquipmentSlot.MAINHAND);}
82:     @SubscribeEvent public static void respawn(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent e){clear(e.getEntity());}
```

## src/main/java/org/slavicmyths/combat/WeaponProjectile.java

```java
20:     private boolean dealtDamage,knife,resolved,playerThrown;
37:     @Override protected EntityHitResult findHitEntity(Vec3 start,Vec3 end){return dealtDamage||returning?null:super.findHitEntity(start,end);}
44:         if(level().isClientSide||dealtDamage||resolved)return;
47:         float damage=weapon.getItem() instanceof ThrowingWeaponItem item?item.thrownDamage():0;
48:         var source=damageSources().trident(this,owner==null?this:owner);
49:         if(level() instanceof ServerLevel server)damage=EnchantmentHelper.modifyDamage(server,weapon,target,source,damage);
50:         dealtDamage=true;
52:         if(target.hurt(source,damage)&&level() instanceof ServerLevel server)EnchantmentHelper.doPostAttackEffectsWithItemSource(server,target,source,weapon);
64:         resolved=true; // Set before spawning; persisted resolution cannot mint another item.
66:             var dropped=spawnAtLocation(getPickupItem(),.1F);
72:     private void beginReturn(){returning=true;inGround=false;dealtDamage=true;entityData.set(RETURNING,true);setNoPhysics(true);setNoGravity(true);}
77:         if(delta.lengthSqr()<1.5){if(pickup==Pickup.ALLOWED&&owner instanceof Player p){ItemStack carried=getPickupItem().copy();p.getInventory().add(carried);if(!carried.isEmpty()){var drop=p.spawnAtLocation(carried);if(drop!=null)drop.setNoPickUpDelay();}}resolved=true;discard();return;}
87:     @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putBoolean("Knife",knife);tag.putBoolean("DealtDamage",dealtDamage);tag.putBoolean("Resolved",resolved);tag.putBoolean("PlayerThrown",playerThrown);tag.putInt("FlightTicks",flightTicks);tag.putBoolean("Returning",returning);tag.putInt("ReturnTicks",returnTicks);}
88:     @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);knife=tag.getBoolean("Knife");dealtDamage=tag.getBoolean("DealtDamage");resolved=tag.getBoolean("Resolved");playerThrown=tag.getBoolean("PlayerThrown");flightTicks=tag.getInt("FlightTicks");entityData.set(DISPLAY,getPickupItemStackOrigin().copy());returnTicks=tag.getInt("ReturnTicks");if(tag.getBoolean("Returning"))beginReturn();}
```

## src/main/java/org/slavicmyths/depth/ElderVodyanoy.java

```java
3: import net.minecraft.world.entity.ai.attributes.Attribute;
6: import net.minecraft.world.entity.ai.attributes.*;
15: import net.minecraft.world.damagesource.DamageSource;
29:  public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,150).add(Attributes.MOVEMENT_SPEED,.19).add(Attributes.ATTACK_DAMAGE,8).add(Attributes.KNOCKBACK_RESISTANCE,.8).add(Attributes.FOLLOW_RANGE,24);}
38:   if(waveTicks>0){float radius=(30-waveTicks)*.35F;waveTicks--;if(tickCount%3==0){ServerLevel w=(ServerLevel)level();for(int i=0;i<12;i++){double a=i*Math.PI/6;w.sendParticles(ParticleTypes.SPLASH,getX()+Math.cos(a)*radius,getY()+.35,getZ()+Math.sin(a)*radius,1,0,.05,0,.02);}}for(Player p:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(radius+1,2,radius+1),this::validPlayer)){double distance=Math.sqrt(distanceToSqr(p));if(Math.abs(distance-radius)<1.2&&hasLineOfSight(p)&&waveHit.add(p.getUUID())){p.hurt(damageSources().mobAttack(this),3);p.knockback(.9F,getX()-p.getX(),getZ()-p.getZ());p.hurtMarked=true;}}}
41:  private void resolve(int action){if(action==3&&isInWater()){dashTicks=12;return;}if(action==6){waveTicks=30;waveHit.clear();return;}if(action==5){for(Player p:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(10),this::validPlayer)){if(!hasLineOfSight(p)||(!p.isInWater()&&!level().getFluidState(p.blockPosition().below()).is(net.minecraft.tags.FluidTags.WATER)))continue;double strength=org.slavicmyths.item.FolkEquipmentEffects.wears(p,org.slavicmyths.registry.ModItems.DEPTH_AMULET.get())?.25:.4;p.setDeltaMovement(p.getDeltaMovement().add(position().subtract(p.position()).normalize().scale(strength)));p.hurtMarked=true;}return;}hitArea(action==4?6:4,action==1?9:action==2?5:3,action==1?.3F:.75F,true);}
42:  private void hitArea(float radius,float damage,float knock,boolean front){Vec3 facing=new Vec3(-Math.sin(Math.toRadians(getYRot())),0,Math.cos(Math.toRadians(getYRot())));for(Player p:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(radius,2,radius),this::validPlayer)){if(distanceToSqr(p)>radius*radius||!hasLineOfSight(p)||(front&&p.position().subtract(position()).normalize().dot(facing)<.1))continue;p.hurt(damageSources().mobAttack(this),damage);p.knockback(knock,getX()-p.getX(),getZ()-p.getZ());p.hurtMarked=true;}}
44:  public boolean hurt(DamageSource s,float amount){return super.hurt(s,s.is(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD)?amount:Math.min(20,amount));}
45:  protected SoundEvent getAmbientSound(){return ModSounds.ELDER_IDLE.get();}protected SoundEvent getHurtSound(DamageSource s){return ModSounds.ELDER_HURT.get();}protected SoundEvent getDeathSound(){return ModSounds.ELDER_DEATH.get();}
47:  public void die(DamageSource s){if(!level().isClientSide&&!resolved){resolved=true;bar.removeAllPlayers();waveTicks=dashTicks=windup=0;if(home!=null&&level().getBlockEntity(home) instanceof PoolStoneTile)((PoolStoneTile)level().getBlockEntity(home)).finish(getUUID());for(Player p:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(24)))org.slavicmyths.progression.Knowledge.award(p,"defeat_depth_master");particles(ParticleTypes.SPLASH,12);}super.die(s);}
```

## src/main/java/org/slavicmyths/depth/ThrownNet.java

```java
18:   if(!org.slavicmyths.combat.CombatTargeting.canControl(owner,e)||e.getTags().contains("slavic_music_immune")){immune(e);return;}
20:   if(!e.canChangeDimensions(e.level(),e.level())||e.getMaxHealth()>=100){immune(e);return;}
21:   e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,e.isInWater()?100:60,e.isInWater()?4:3));
23:  private void immune(LivingEntity e){if(getOwner() instanceof net.minecraft.server.level.ServerPlayer p)p.displayClientMessage(net.minecraft.network.chat.Component.translatable("combat.slavicmyths.net_immune"),true);((net.minecraft.server.level.ServerLevel)level()).sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,getX(),getY(),getZ(),3,.1,.1,.1,0);}
```

## src/main/java/org/slavicmyths/earth/EarthActor.java

```java
19:     private static final EntityDataAccessor<Float> RADIUS=SynchedEntityData.defineId(EarthActor.class,EntityDataSerializers.FLOAT),DX=SynchedEntityData.defineId(EarthActor.class,EntityDataSerializers.FLOAT),DZ=SynchedEntityData.defineId(EarthActor.class,EntityDataSerializers.FLOAT);
23:     @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(KIND,0);b.define(LIFE,80);b.define(OWNER,-1);b.define(VARIANT,0);b.define(RADIUS,1F);b.define(DX,0F);b.define(DZ,1F);b.define(START,0L);}
25:     public int kind(){return entityData.get(KIND);}public int life(){return entityData.get(LIFE);}public int ownerId(){return entityData.get(OWNER);}public int variant(){return entityData.get(VARIANT);}public float radius(){return entityData.get(RADIUS);}public Vec3 direction(){return new Vec3(entityData.get(DX),0,entityData.get(DZ));}public UUID owner(){return owner;}
26:     private static EarthActor create(ServerLevel w,EarthSpirit boss,int kind,Vec3 at,int life,double radius){var a=EarthContent.ACTOR.get().create(w);if(a==null)throw new IllegalStateException("earth actor factory");a.entityData.set(START,w.getGameTime());a.entityData.set(KIND,kind);a.entityData.set(LIFE,life);a.entityData.set(RADIUS,(float)radius);a.entityData.set(VARIANT,w.random.nextInt(3));a.setPos(at);a.origin=at;if(boss!=null){a.owner=boss.getUUID();a.arena=boss.arenaId();a.entityData.set(OWNER,boss.getId());}return a;}
27:     public static void marker(ServerLevel w,EarthSpirit b,Vec3 at,double radius,int ticks){w.addFreshEntity(create(w,b,WARNING,at,ticks,radius));}
28:     public static void wave(ServerLevel w,EarthSpirit b,double radius,int ticks,boolean tremor){w.addFreshEntity(create(w,b,tremor?TREMOR:COUNTER,b.position(),ticks+28,radius));}
34:     /** Cosmetic only: no ItemEntity, block placement, loot, owner damage or pickup. */
51:     private boolean damage(ServerLevel w,EarthSpirit b,Player p,float raw,double force,Vec3 center){boolean landed=hit.add(p.getUUID())&&p.hurt(b.attackSource(this),raw);if(landed&&force>0){var v=p.position().subtract(center).multiply(1,0,1).normalize();p.knockback(force,-v.x,-v.z);p.hurtMarked=true;}return landed;}
52:     private void waveTick(ServerLevel w,EarthSpirit b){if(b==null)return;int travel=kind()==TREMOR?16:9;if(tickCount>travel)return;double previous=1.5+(radius()-1.5)*(tickCount-1)/travel,current=1.5+(radius()-1.5)*tickCount/travel;
53:         for(var p:w.getEntitiesOfClass(Player.class,new AABB(origin,origin).inflate(radius()+1,2,radius()+1),b::valid)){double d=p.position().subtract(origin).horizontalDistance();double feet=p.getY()-origin.y;double half=p.getBbWidth()/2;if(d+half<previous-.55||d-half>current+.55||feet<-.4||feet>(kind()==TREMOR?.9:1.5)||hit.contains(p.getUUID()))continue;if(sight(w,origin.add(0,.3,0),p.position().add(0,.3,0),this)){boolean landed=damage(w,b,p,EarthRules.WAVE_RAW,kind()==TREMOR?.35:1.5,origin);if(landed&&kind()==TREMOR){p.addEffect(new net.minecraft.world.effect.MobEffectInstance(EarthContent.IMBALANCE,13,0,false,false,true));}}}
55:     private void spikeTick(ServerLevel w,EarthSpirit b){if(b==null)return;for(int i=0;i<3;i++)if(tickCount==1+i*2){double start=1+i*4,end=start+4;for(var p:w.getEntitiesOfClass(Player.class,new AABB(origin,origin.add(axis.scale(13))).inflate(2,1.4,2),b::valid)){Vec3 v=p.position().subtract(origin);double along=v.dot(axis),side=Math.abs(v.x*axis.z-v.z*axis.x);if(along>=start&&along<=end&&side<=1+p.getBbWidth()/2&&Math.abs(v.y)<1.2&&sight(w,origin.add(0,.3,0),p.position().add(0,.3,0),this))damage(w,b,p,EarthRules.SPIKE_RAW,.25,origin);}}}
56:     private boolean solid(ServerLevel w,Vec3 sample,double r){var box=new AABB(sample,sample).inflate(r);for(var at:BlockPos.betweenClosed(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ))){if(!w.hasChunkAt(at))return true;var shape=w.getBlockState(at).getCollisionShape(w,at);for(var part:shape.toAabbs())if(part.move(at).intersects(box))return true;}return false;}
59:             var candidates=w.getEntitiesOfClass(Player.class,new AABB(center,center).inflate(length/2+r,2+r,length/2+r),b::valid);
60:             for(var p:candidates){Vec3 lo=center.subtract(axis.scale(length/2)),hi=center.add(axis.scale(length/2));if(p.getBoundingBox().inflate(r).contains(lo)||p.getBoundingBox().inflate(r).clip(lo,hi).isPresent()){long until=p.getPersistentData().getLong("EarthVolleyHitUntil");if(kind()!=STONE||w.getGameTime()>=until){damage(w,b,p,kind()==PILLAR?EarthRules.PILLAR_RAW:EarthRules.STONE_RAW,kind()==PILLAR?.9:.35,center);if(kind()==STONE)p.getPersistentData().putLong("EarthVolleyHitUntil",w.getGameTime()+8);}impact(w,b,center);return;}}
64:     private void impact(ServerLevel w,EarthSpirit b,Vec3 at){EarthArena.cue(w,BlockPos.containing(at),kind()==PILLAR?"pillar_impact":"volley_impact",kind()==PILLAR?1:.6F);if(kind()==PILLAR)for(var p:w.getEntitiesOfClass(Player.class,new AABB(at,at).inflate(3.5,1.5,3.5),b::valid))if(!hit.contains(p.getUUID())&&p.position().distanceToSqr(at)<3.5*3.5&&sight(w,at.add(0,.1,0),p.position().add(0,.6,0),this))damage(w,b,p,EarthRules.SPLASH_RAW,.6,at);
```

## src/main/java/org/slavicmyths/earth/EarthCombatEvents.java

```java
4: import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
7:     @SubscribeEvent public static void pre(LivingDamageEvent.Pre e){if(e.getEntity() instanceof EarthSpirit b)e.setNewDamage(e.getNewDamage()*b.armorMultiplier(e.getSource()));}
8:     @SubscribeEvent public static void post(LivingDamageEvent.Post e){if(e.getEntity() instanceof EarthSpirit b)b.registeredHit(e.getSource(),e.getNewDamage());if(e.getSource().getEntity() instanceof EarthSpirit b&&e.getEntity() instanceof net.minecraft.server.level.ServerPlayer player){var resistance=player.getEffect(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE);var text=String.format(java.util.Locale.ROOT,"source=%s raw=%.3f actualHP=%.3f armor=%d toughness=%.1f difficulty=%s resistance=%d armorReduction=%.3f enchantmentReduction=%.3f absorption=%.3f",e.getSource().getMsgId(),e.getOriginalDamage(),e.getNewDamage(),player.getArmorValue(),player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS),player.level().getDifficulty(),resistance==null?0:resistance.getAmplifier()+1,e.getReduction(net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction.ARMOR),e.getReduction(net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction.ENCHANTMENTS),e.getReduction(net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction.ABSORPTION));b.lastDamageReport=text;}}
```

## src/main/java/org/slavicmyths/earth/EarthContent.java

```java
7: import net.minecraft.world.entity.ai.attributes.Attributes;
26:     public static final DeferredHolder<net.minecraft.world.effect.MobEffect,net.minecraft.world.effect.MobEffect> IMBALANCE=ModEffects.EFFECTS.register("earth_imbalance",()->new net.minecraft.world.effect.MobEffect(net.minecraft.world.effect.MobEffectCategory.HARMFUL,0xa0784d){}.addAttributeModifier(Attributes.MOVEMENT_SPEED,id("earth_imbalance_speed"),-.5,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
34:         ModItems.ITEMS.register("earth_servant_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(SERVANT,0x5b4736,0xa66a39,new Item.Properties()));
41:         e.put(BOSS.get(),net.minecraft.world.entity.monster.Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,EarthRules.HEALTH).add(Attributes.MOVEMENT_SPEED,EarthRules.SPEED).add(Attributes.ATTACK_DAMAGE,EarthRules.MELEE_RAW).add(Attributes.FOLLOW_RANGE,38).add(Attributes.KNOCKBACK_RESISTANCE,1).add(Attributes.STEP_HEIGHT,1).build());
42:         e.put(SERVANT.get(),net.minecraft.world.entity.monster.Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,EarthRules.SERVANT_HEALTH).add(Attributes.MOVEMENT_SPEED,EarthRules.SERVANT_SPEED).add(Attributes.ATTACK_DAMAGE,EarthRules.SERVANT_RAW).add(Attributes.FOLLOW_RANGE,18).build());
```

## src/main/java/org/slavicmyths/earth/EarthRules.java

```java
3: /** Encounter-local balance. HP are half-hearts; time is server ticks at 20 TPS.
4:  * Raw damage is calibrated against diamond (20 armour, 8 toughness); servants retain iron calibration. No armour bypass. */
6:     public static double HEALTH=800, HEIGHT=6.5, SERVANT_HEALTH=18, SPEED=.18, SERVANT_SPEED=.24;
9:     public static double PHASE_II=.70, PHASE_III=.30, RUNE_HP=2, RUNE_DAMAGE=.025, RUNE_KB=.15;
11:     public enum Action {
15:         public final int duration;public int cooldown;
16:         Action(int duration,int cooldown){this.duration=duration;this.cooldown=cooldown;}
23:     public static int phase(double hp,double max){return hp<=max*PHASE_III?3:hp<=max*PHASE_II?2:1;}
25:     public static double runeDamage(int n){return 1-Math.clamp(n,0,4)*RUNE_DAMAGE;}
```

## src/main/java/org/slavicmyths/earth/EarthRune.java

```java
5: import net.minecraft.world.entity.ai.attributes.*;
7: import net.minecraft.tags.DamageTypeTags;
17:     public static List<Component> tooltip(){return List.of(Component.translatable("earth.slavicmyths.rune_tier"),Component.translatable("earth.slavicmyths.rune_hp"),Component.translatable("earth.slavicmyths.rune_damage"),Component.translatable("earth.slavicmyths.rune_kb"),Component.translatable("earth.slavicmyths.armor_only"));}
19:     public static void refresh(LivingEntity e){if(e.level().isClientSide)return;int n=count(e);apply(e,Attributes.MAX_HEALTH,"earth_rune_health",n*EarthRules.RUNE_HP);apply(e,Attributes.KNOCKBACK_RESISTANCE,"earth_rune_knockback",n*EarthRules.RUNE_KB);if(e.getHealth()>e.getMaxHealth())e.setHealth(e.getMaxHealth());}
24:     @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){refresh(e.getEntity());}
26:     @SubscribeEvent public static void damage(LivingDamageEvent.Pre e){if(e.getEntity().level().isClientSide||e.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)||e.getNewDamage()<=0)return;e.setNewDamage((float)(e.getNewDamage()*EarthRules.runeDamage(count(e.getEntity()))));}
```

## src/main/java/org/slavicmyths/earth/EarthServant.java

```java
17:     public EarthServant(EntityType<? extends Monster> t,Level w){super(t,w);setPersistenceRequired();xpReward=0;getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(EarthRules.SERVANT_HEALTH);setHealth((float)EarthRules.SERVANT_HEALTH);}
27:         if(state()==0){if(age(0)<12)return;entityData.set(STATE,1);}if(tickCount%10==0)setTarget(w.getEntitiesOfClass(Player.class,new AABB(home).inflate(19,5,19),this::valid).stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null));
28:         attackIn=Math.max(0,attackIn-1);if(state()==2){getNavigation().stop();if(age(0)>=9&&age(0)<10&&getTarget() instanceof Player p&&valid(p)&&distanceToSqr(p)<2.6*2.6&&hasLineOfSight(p)){p.hurt(damageSources().mobAttack(this),EarthRules.SERVANT_RAW);EarthArena.cue(w,blockPosition(),"servant_hit",.4F);}if(age(0)>=21)entityData.set(STATE,1);}else if(attackIn==0&&getTarget()!=null&&distanceToSqr(getTarget())<2.6*2.6&&hasLineOfSight(getTarget())){entityData.set(STATE,2);entityData.set(START,w.getGameTime());attackIn=32;}
31:     @Override public void die(net.minecraft.world.damagesource.DamageSource s){if(owner!=null&&level() instanceof ServerLevel w&&w.getEntity(owner) instanceof EarthSpirit b)b.servantDied(getUUID());super.die(s);}
39:     @Override protected net.minecraft.sounds.SoundEvent getDeathSound(){return EarthContent.sound("servant_death");}@Override protected net.minecraft.sounds.SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource s){return EarthContent.sound("servant_hit");}
```

## src/main/java/org/slavicmyths/earth/EarthSpirit.java

```java
10: import net.minecraft.world.entity.ai.attributes.Attributes;
17: import net.minecraft.world.damagesource.*;
20: import net.minecraft.tags.DamageTypeTags;
24: /** One local attack timeline on the logical server. Rendering never causes hits or spawns. */
26:     public String lastDamageReport="No registered hit against a player yet";
32:     private UUID arena;private BlockPos home;private final EnumMap<Action,Integer> cooldowns=new EnumMap<>(Action.class);
34:     private Vec3 aim=Vec3.ZERO;private final Map<UUID,Integer> pressure=new HashMap<>(),meleeLast=new HashMap<>();private final List<UUID> servants=new ArrayList<>();private List<BlockPos> spawnPoints=List.of();private boolean legitimate;private final Set<UUID> participants=new HashSet<>();
35:     public EarthSpirit(EntityType<? extends Monster> t,Level w){super(t,w);setPersistenceRequired();xpReward=80;getAttribute(Attributes.MAX_HEALTH).setBaseValue(EarthRules.HEALTH);setHealth((float)EarthRules.HEALTH);getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(EarthRules.SPEED);}
38:     public net.minecraft.world.damagesource.DamageSource attackSource(Entity direct){var key=net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,EarthContent.id(direct instanceof EarthActor actor&&(actor.kind()==EarthActor.STONE||actor.kind()==EarthActor.PILLAR)?"earth_projectile":"earth_strike"));return new net.minecraft.world.damagesource.DamageSource(level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(key),direct,this);}
48:     @Override public boolean canBeAffected(MobEffectInstance e){return e.getEffect().value()!=ModEffects.CUT.get()&&e.getEffect().value()!=ModEffects.RARE_BLEED.get()&&e.getEffect().value()!=ModEffects.WOLF_CUT.get()&&super.canBeAffected(e);}
49:     @Override public boolean hurt(DamageSource s,float amount){if(action()==Action.AWAKEN&&!s.is(DamageTypeTags.BYPASSES_INVULNERABILITY)||s.is(DamageTypes.FALL)||s.is(DamageTypes.IN_WALL)||s.getEntity() instanceof EarthServant||s.getEntity()==this)return false;boolean result=super.hurt(s,amount);return result;}
50:     public void registeredHit(DamageSource source,float actual){if(actual<=0)return;if(source.getEntity() instanceof Player p&&valid(p)){participants.add(p.getUUID());setTarget(p);getLookControl().setLookAt(p,35,25);if(action()==Action.APPROACH)begin(Action.RECOVER,8);}if(source.is(DamageTypes.PLAYER_ATTACK)&&source.getDirectEntity() instanceof Player p&&valid(p))meleeLast.put(p.getUUID(),tickCount);}
51:     public float armorMultiplier(DamageSource source){return armorIn>0?(source.is(DamageTypeTags.IS_PROJECTILE)||source.getDirectEntity()!=source.getEntity()?.75F:.90F):1;}
52:     private boolean ready(Action a){return cooldowns.getOrDefault(a,0)==0;}
53:     private void begin(Action a,int ticks){entityData.set(STATE,a.ordinal());entityData.set(START,level().getGameTime());elapsed=0;duration=ticks;if(a==Action.MELEE)cooldowns.put(a,a.cooldown);attackId++;getNavigation().stop();aim=valid(getTarget())?getTarget().position():position().add(getLookAngle().scale(8));}
54:     private void finish(){Action a=action();if(a!=Action.MELEE&&a!=Action.PHASE&&a!=Action.AWAKEN&&a!=Action.APPROACH)cooldowns.put(a,(int)(a.cooldown*(phase()==3&&(a==Action.VOLLEY||a==Action.TREMOR)?.9:1)));if(a!=Action.MELEE&&a!=Action.RECOVER&&a!=Action.PHASE)lastHeavy=a;entityData.set(PENDING,0);entityData.set(PILLAR,-1);begin(Action.RECOVER,a==Action.MELEE?6:EarthRules.HEAVY_GAP);}
58:         if(tickCount%10==0){var choices=w.getEntitiesOfClass(Player.class,new AABB(home).inflate(19,8,19),this::valid);if(!valid(getTarget()))setTarget(choices.stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null));if(choices.isEmpty())absent+=10;else absent=0;}
61:         cooldowns.replaceAll((a,n)->Math.max(0,n-1));if(armorIn>0){armorIn--;entityData.set(ARMOR,armorIn);if(armorIn==14)cue("pillar_rip");}if(action()!=Action.AWAKEN)summonIn=Math.max(0,summonIn-1);
73:         if(summonIn==0){servants.removeIf(id->{var entity=w.getEntity(id);return entity!=null&&!entity.isAlive();});if(servants.size()<=3){spawnPoints=planSummons(w);if(EarthRules.canSummon(servants.size(),spawnPoints.size())){begin(Action.SUMMON,44);cue("summon_warning");for(var at:spawnPoints)EarthActor.marker(w,this,at.getCenter().add(0,-.45,0),1,44);return;}}summonIn=EarthRules.INTERVAL;}
87:         case MELEE->{if(elapsed==12){cue("melee_impact");Vec3 forward=aim.subtract(position()).multiply(1,0,1).normalize();for(var p:w.getEntitiesOfClass(Player.class,getBoundingBox().inflate(4.4,0,4.4),this::valid)){Vec3 v=p.position().subtract(position()).multiply(1,0,1).normalize();if(surfaceDistance(p)<=4.4&&v.dot(forward)>=Math.cos(Math.toRadians(50))&&hasLineOfSight(p))p.hurt(attackSource(this),EarthRules.MELEE_RAW);}}}
88:         case SUMMON->{if(elapsed==24){var wave=new ArrayList<EarthServant>();boolean safe=spawnPoints.size()==3;for(var at:spawnPoints)if(!safeSpawn(w,at))safe=false;if(safe)for(var at:spawnPoints){var s=EarthContent.SERVANT.get().create(w);if(s==null){safe=false;break;}s.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,0,0);s.bind(getUUID(),arena,home());if(!w.addFreshEntity(s)){safe=false;break;}wave.add(s);}if(!safe)wave.forEach(Entity::discard);else{wave.forEach(s->servants.add(s.getUUID()));EarthArena.cue(w,spawnPoints.getFirst(),"servant_emerge",.55F);}}if(elapsed==44)summonIn=EarthRules.INTERVAL;}
91:         case PHASE->{if(elapsed==22)cue("roar_2");if(elapsed==1&&phase()==3)getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(EarthRules.SPEED*1.08);}
96:     private List<BlockPos> planSummons(ServerLevel w){var points=new ArrayList<BlockPos>();for(int i=0;i<12&&points.size()<3;i++){double a=i*Math.PI*2/12+random.nextDouble()*.2;var p=BlockPos.containing(getX()+Math.cos(a)*8,getY(),getZ()+Math.sin(a)*8);if(safeSpawn(w,p)&&points.stream().allMatch(q->q.distSqr(p)>=9))points.add(p);}return List.copyOf(points);}
97:     private boolean safeSpawn(ServerLevel w,BlockPos p){if(!inside(p.getCenter())||!w.hasChunkAt(p)||!w.getBlockState(p.below()).isCollisionShapeFullBlock(w,p.below()))return false;var box=new AABB(p.getX()+.05,p.getY(),p.getZ()+.05,p.getX()+.95,p.getY()+1.5,p.getZ()+.95);return w.noCollision(box)&&w.getEntitiesOfClass(Player.class,box.inflate(3),Player::isAlive).isEmpty()&&w.getEntitiesOfClass(EarthServant.class,box.inflate(1)).isEmpty();}
98:     private void cancelActors(){if(level() instanceof ServerLevel w)for(var actor:w.getEntitiesOfClass(EarthActor.class,getBoundingBox().inflate(40),a->getUUID().equals(a.owner())))actor.discard();entityData.set(PENDING,0);entityData.set(PILLAR,-1);}
99:     @Override public void die(DamageSource source){if(source.getEntity() instanceof Player p&&valid(p))participants.add(p.getUUID());legitimate=!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)&&!participants.isEmpty()&&!(source.getEntity() instanceof Player p&&(p.isCreative()||p.isSpectator()));if(!legitimate)xpReward=0;bar.removeAllPlayers();cancelActors();armorIn=0;begin(Action.DEATH,EarthRules.DEATH);if(level() instanceof ServerLevel w){var e=entry();if(e!=null)EarthArena.defeat(w,e,true);for(var id:servants)if(w.getEntity(id) instanceof EarthServant s)s.collapse();}if(legitimate&&level() instanceof ServerLevel w)for(var id:participants){var p=w.getServer().getPlayerList().getPlayer(id);if(p!=null)org.slavicmyths.progression.Knowledge.award(p,"earth_heart");}super.die(source);cue("death_fall");}
102:     @Override protected void dropCustomDeathLoot(ServerLevel w,DamageSource s,boolean hit){super.dropCustomDeathLoot(w,s,hit);if(arena==null)spawnAtLocation(EarthContent.RUNE.get());}
104:     @Override protected net.minecraft.sounds.SoundEvent getDeathSound(){return null;}@Override protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource s){return null;}
105:     @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(arena!=null)n.putUUID("EarthArena",arena);n.putLong("EarthHome",home().asLong());n.putInt("EarthPhase",phase());n.putInt("EarthSummonIn",summonIn);n.putString("EarthAction",action().name());n.putInt("EarthElapsed",elapsed);n.putInt("EarthAttackId",attackId);n.putString("EarthLastHeavy",lastHeavy.name());n.putInt("EarthAbsent",absent);var c=new CompoundTag();cooldowns.forEach((a,v)->c.putInt(a.name(),v));n.put("EarthCooldowns",c);var list=new ListTag();for(var id:servants)list.add(NbtUtils.createUUID(id));n.put("EarthServants",list);var members=new ListTag();for(var id:participants)members.add(NbtUtils.createUUID(id));n.put("EarthParticipants",members);}
106:     @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);if(n.hasUUID("EarthArena"))arena=n.getUUID("EarthArena");home=BlockPos.of(n.getLong("EarthHome"));restrictTo(home,19);entityData.set(PHASE,Math.clamp(n.getInt("EarthPhase"),1,3));summonIn=Math.clamp(n.getInt("EarthSummonIn"),40,EarthRules.INTERVAL);attackId=n.getInt("EarthAttackId");absent=n.getInt("EarthAbsent");for(var a:Action.values())cooldowns.put(a,Math.max(0,n.getCompound("EarthCooldowns").getInt(a.name())));for(var t:n.getList("EarthServants",Tag.TAG_INT_ARRAY))servants.add(NbtUtils.loadUUID(t));try{lastHeavy=Action.valueOf(n.getString("EarthLastHeavy"));}catch(IllegalArgumentException ignored){}for(var t:n.getList("EarthParticipants",Tag.TAG_INT_ARRAY))participants.add(NbtUtils.loadUUID(t));if(phase()==3)getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(EarthRules.SPEED*1.08);var e=entry();if(e!=null){servants.removeAll(e.retired);e.retired.clear();EarthArena.get((ServerLevel)level()).setDirty();}if(e!=null&&!e.gateClosed)e.gateStart=0;String saved=n.getString("EarthAction");if(saved.equals("AWAKEN")){begin(Action.AWAKEN,88);elapsed=Math.clamp(n.getInt("EarthElapsed"),0,87);entityData.set(START,level().getGameTime()-elapsed);}else if(saved.equals("DORMANT"))begin(Action.DORMANT,0);else begin(Action.RECOVER,40);}
```

## src/main/java/org/slavicmyths/entity/BannikEntity.java

```java
2: import net.minecraft.world.entity.ai.attributes.Attribute;
5: import net.minecraft.world.entity.ai.attributes.*;
14: import net.minecraft.world.damagesource.DamageSource;
19:     public enum Behavior { DORMANT, WARNING, HOSTILE, STEAM_PREP, STEAM_BURST, SLAP_COMBO, HEAT_ENRAGE, RECOVERY }
23:     public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,80).add(Attributes.MOVEMENT_SPEED,.26).add(Attributes.ATTACK_DAMAGE,7).add(Attributes.ARMOR,5).add(Attributes.KNOCKBACK_RESISTANCE,.7).add(Attributes.FOLLOW_RANGE,20);}
55:             if(steam){behavior=Behavior.STEAM_BURST;particles(ParticleTypes.CLOUD,45);for(Player player:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(5),p->attackable(p,5)&&p.position().subtract(position()).multiply(1,0,1).normalize().dot(getLookAngle().multiply(1,0,1).normalize())>.35)){player.hurt(damageSources().mobAttack(this),4);player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,40));}}
```

## src/main/java/org/slavicmyths/entity/DomovoyEntity.java

```java
6: import net.minecraft.world.entity.ai.attributes.Attribute;
11: import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
12: import net.minecraft.world.entity.ai.attributes.Attributes;
17: import net.minecraft.world.damagesource.DamageSource;
50:         return createMobAttributes().add(Attributes.MAX_HEALTH, 12).add(Attributes.MOVEMENT_SPEED, 0.25)
51:                 .add(Attributes.FOLLOW_RANGE, 12).add(Attributes.ATTACK_DAMAGE, 2);
101:                 player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 100, 0));
108:     @Override public boolean hurt(DamageSource damage, float amount) {
109:         boolean hit = super.hurt(damage, amount);
110:         if (hit && !level().isClientSide && damage.getEntity() instanceof Player) {
111:             Player player = (Player) damage.getEntity();
149:     @Override public int getMaxSpawnClusterSize() { return 1; }
153:     @Override protected SoundEvent getHurtSound(DamageSource damage) { return ModSounds.DOMOVOY_HURT.get(); }
```

## src/main/java/org/slavicmyths/entity/HotStoneEntity.java

```java
5: import net.minecraft.world.damagesource.DamageSource;
18:     @Override protected void onHitEntity(EntityHitResult hit){super.onHitEntity(hit);if(!level().isClientSide && hit.getEntity().hurt(damageSources().thrown(this,getOwner()),4))hit.getEntity().igniteForSeconds(2);}
```

## src/main/java/org/slavicmyths/entity/IgoshaEntity.java

```java
3: import net.minecraft.world.entity.ai.attributes.Attribute;
5: import net.minecraft.world.entity.ai.attributes.*;
14: import net.minecraft.world.damagesource.DamageSource;
24:     public enum Behavior { WATCH, FEINT_APPROACH, VANISH, NUISANCE_HIT, FLEE, COOLDOWN }
28:     public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,30).add(Attributes.MOVEMENT_SPEED,.32).add(Attributes.ATTACK_DAMAGE,4).add(Attributes.FOLLOW_RANGE,24);}
44:         if(attack.phase()==AttackTimeline.Phase.COOLDOWN)behavior=Behavior.COOLDOWN;
54:         if(depart>0){if(level().getGameTime()>=depart){if(random.nextInt(3)==0)spawnAtLocation(ModItems.OLD_BUTTON.get());particles(ParticleTypes.POOF,10);discard();}return;}
61:             p.addEffect(new MobEffectInstance(random.nextBoolean()?MobEffects.WEAKNESS:MobEffects.BLINDNESS,45));voice("power",.35F);
```

## src/main/java/org/slavicmyths/entity/KikimoraEntity.java

```java
2: import net.minecraft.world.entity.ai.attributes.Attribute;
4: import net.minecraft.world.entity.ai.attributes.*;
11:     public enum Behavior { IDLE, STALK, HIDE, PEEK, AMBUSH, SCRATCH_COMBO, FLEE_REPOSITION, COOLDOWN }
15:     public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,36).add(Attributes.MOVEMENT_SPEED,.28).add(Attributes.ATTACK_DAMAGE,5).add(Attributes.ARMOR,2).add(Attributes.FOLLOW_RANGE,28);}
31:             if(attack.elapsed()<=10&&!firstStrike&&strikeTarget(6,2.3,.15F)){firstStrike=true;getTarget().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,30));}
35:         else if(attack.phase()==AttackTimeline.Phase.COOLDOWN){behavior=Behavior.COOLDOWN;state(1);}
```

## src/main/java/org/slavicmyths/entity/LandSpiritEntity.java

```java
18: import net.minecraft.world.damagesource.DamageSource;
46:     @Override public SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level,net.minecraft.world.DifficultyInstance difficulty,MobSpawnType reason,SpawnGroupData data){SpawnGroupData result=super.finalizeSpawn(level,difficulty,reason,data);mor.finalizedSpawn();return result;}
73:     protected Player nearby(double radius) {
74:         Player p=level().getNearestPlayer(this,radius);
93:     protected boolean strikeTarget(float damage,double range,float knockback){var target=getTarget();if(!attackable(target,range))return false;
95:         boolean hit=target.hurt(damageSources().mobAttack(this),damage);if(hit&&knockback>0)target.knockback(knockback,getX()-target.getX(),getZ()-target.getZ());return hit;}
103:     @Override protected SoundEvent getHurtSound(DamageSource source){return morState()!=0?org.slavicmyths.mor.MorAudio.hurt(mor.kind()):sound("hurt");}
106:     @Override public int getMaxSpawnClusterSize(){return 1;}
107:     @Override protected boolean shouldDespawnInPeaceful(){return true;}
109:     @Override public boolean hurt(DamageSource source,float amount) {
110:         boolean hit=super.hurt(source,amount);
```

## src/main/java/org/slavicmyths/entity/LeshyEntity.java

```java
8: import net.minecraft.world.damagesource.DamageSource;
12: import net.minecraft.world.entity.ai.attributes.*;
18:  public enum Behavior { WATCH, WARN, VANISH, REPOSITION, ROOT_SNARE, WOODLAND_STRIKE, FALSE_PRESENCE, CHASE, DISENGAGE }
25:  public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,100).add(Attributes.ARMOR,8).add(Attributes.MOVEMENT_SPEED,.30).add(Attributes.FOLLOW_RANGE,36).add(Attributes.ATTACK_DAMAGE,8).add(Attributes.KNOCKBACK_RESISTANCE,.25);}
28:  @Override public boolean hurt(DamageSource source,float amount){
29:   if(morState()==0&&vanished>0&&!source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY))return false;
30:   boolean hit=super.hurt(source,amount);if(hit&&source.getEntity() instanceof Player player)org.slavicmyths.progression.Knowledge.award(player,"meet_leshy");return hit;
54:    if(roots&&attackable(getTarget(),20)){getTarget().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40));behavior=Behavior.FALSE_PRESENCE;
61:    for(int i=0;i<8;i++){double angle=random.nextDouble()*Math.PI*2;int radius=8+random.nextInt(9);var p=blockPosition().offset((int)(Math.cos(angle)*radius),0,(int)(Math.sin(angle)*radius));
```

## src/main/java/org/slavicmyths/entity/OvinnikEntity.java

```java
2: import net.minecraft.world.entity.ai.attributes.Attribute;
9:  public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder attributes(){return org.slavicmyths.hunt.HuntMob.attributes(true);}
```

## src/main/java/org/slavicmyths/entity/PolevikEntity.java

```java
2: import net.minecraft.world.entity.ai.attributes.Attribute;
5: import net.minecraft.world.entity.ai.attributes.*;
20:     public enum Behavior { FIELD_WANDER, INSPECT, WARNING, DUST_DASH, GRASS_SNARE, RECOVERY }
25:     public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,44).add(Attributes.MOVEMENT_SPEED,.30).add(Attributes.ATTACK_DAMAGE,6).add(Attributes.ARMOR,3).add(Attributes.FOLLOW_RANGE,24);}
39:         if(opened){direction=getTarget().position().subtract(position()).multiply(1,0,1).normalize();if(snare&&attackable(getTarget(),7)){getTarget().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40));particles(ParticleTypes.HAPPY_VILLAGER,12);}}
47:             for(ItemEntity drop:level().getEntitiesOfClass(ItemEntity.class,getBoundingBox().inflate(6),i->i.isAlive() && i.getOwner() instanceof Player && (i.getItem().getItem()==Items.BREAD || i.getItem().getItem()==Items.WHEAT || i.getItem().getItem()==ModItems.HONEY_BREAD.get()))){
54:                 if(score>=3){p.addEffect(new MobEffectInstance(MobEffects.LUCK,1200));if(random.nextInt(12)==0)spawnAtLocation(ModItems.FIELD_BUNDLE.get());}
```

## src/main/java/org/slavicmyths/entity/PoludnitsaEntity.java

```java
2: import net.minecraft.world.entity.ai.attributes.Attribute;
4: import net.minecraft.world.entity.ai.attributes.*;
10: import net.minecraft.world.damagesource.DamageSource;
15:     public enum Behavior { DISTANT_WATCH, APPROACH_THRESHOLD, TRANSFORM, CHASE, SWEEP, HEAT_PRESSURE, RECOVERY, CALM }
21:     public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,60).add(Attributes.MOVEMENT_SPEED,.28).add(Attributes.ATTACK_DAMAGE,7).add(Attributes.ARMOR,4).add(Attributes.FOLLOW_RANGE,32);}
37:             if(heat){particles(ParticleTypes.CLOUD,20);for(Player player:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(6),p->attackable(p,6)))player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,60));}
38:             else{for(Player player:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(3),p->attackable(p,3)&&p.position().subtract(position()).multiply(1,0,1).normalize().dot(getLookAngle().multiply(1,0,1).normalize())>.25))player.hurt(damageSources().mobAttack(this),7);}
```

## src/main/java/org/slavicmyths/entity/VolkolakEntity.java

```java
2: import net.minecraft.world.entity.ai.attributes.Attribute;
8:  public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder attributes(){return org.slavicmyths.hunt.HuntMob.attributes(false);}
```

## src/main/java/org/slavicmyths/entity/WildlifeEntity.java

```java
3: import net.minecraft.world.entity.SpawnGroupData;
9: import net.minecraft.world.entity.ai.attributes.Attribute;
12: import net.minecraft.world.entity.ai.attributes.*;
21: import net.minecraft.world.damagesource.DamageSource;
29:  public enum Kind { BEAR,CUB,WOLF,BOAR,STAG,DOE }
38:  private static final class HerdGroup implements SpawnGroupData {
45:  private int actionTicks,actionLength,recovery,alertTicks,fleeTicks,leapCooldown;
52:   super(type,world);this.kind=kind;installGoals();motherBar.setVisible(false);getAttribute(Attributes.STEP_HEIGHT).setBaseValue(kind==Kind.BEAR?.9F:.6F);
55:  @Override public SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor world,net.minecraft.world.DifficultyInstance difficulty,MobSpawnType reason,SpawnGroupData data){
56:   SpawnGroupData result=super.finalizeSpawn(world,difficulty,reason,data);
58:   if(kind==Kind.BEAR&&(reason==MobSpawnType.NATURAL||reason==MobSpawnType.CHUNK_GENERATION)){
63:    if(kind==Kind.DOE&&(reason==MobSpawnType.NATURAL||reason==MobSpawnType.CHUNK_GENERATION)){
75:  public void fawn(UUID mother){parentId=mother;entityData.set(FAWN,true);getAttribute(Attributes.MAX_HEALTH).setBaseValue(12);getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(0);setHealth(12);refreshDimensions();}
86:     double angle=attempt*Math.PI/8;double radius=2.2+i*.8;child.moveTo(getX()+Math.cos(angle)*radius,getY(),getZ()+Math.sin(angle)*radius,getYRot(),0);
96:   if(kind==Kind.BEAR){entityData.set(MOTHER,true);getAttribute(Attributes.MAX_HEALTH).setBaseValue(120);getAttribute(Attributes.ARMOR).setBaseValue(6);getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(8);setHealth(120);}
113:   return createMobAttributes().add(Attributes.MAX_HEALTH,k==Kind.BEAR?50:k==Kind.CUB?12:k==Kind.WOLF?30:k==Kind.BOAR?36:k==Kind.STAG?26:22)
114:    .add(Attributes.MOVEMENT_SPEED,k==Kind.BEAR?.25:k==Kind.CUB?.29:k==Kind.WOLF?.32:k==Kind.BOAR?.28:.34)
115:    .add(Attributes.ARMOR,k==Kind.BEAR?3:k==Kind.WOLF?1:k==Kind.BOAR?2:0)
116:    .add(Attributes.ATTACK_DAMAGE,k==Kind.BEAR?7:k==Kind.WOLF?5:k==Kind.BOAR?4:k==Kind.STAG?4:0).add(Attributes.FOLLOW_RANGE,k==Kind.BOAR?20:24);
121:  @Override public boolean hurt(DamageSource source,float amount){
122:   boolean hit=super.hurt(source,amount);
131:     if(kind==Kind.WOLF||kind==Kind.CUB)for(WildlifeEntity ally:level().getEntitiesOfClass(WildlifeEntity.class,getBoundingBox().inflate(14),a->a!=this&&!a.getPersistentData().getBoolean("GreyPackAlly")&&!a.getPersistentData().getBoolean("summonedByGreyWolf")&&(kind==Kind.CUB?a.kind==Kind.BEAR:a.kind==Kind.WOLF))){
136:     if(kind==Kind.STAG||kind==Kind.DOE)for(var ally:level().getEntitiesOfClass(WildlifeEntity.class,getBoundingBox().inflate(14),a->a!=this&&(a.kind==Kind.STAG||a.kind==Kind.DOE))){ally.danger=attacker;ally.fleeTicks=160;ally.alertTicks=5+ally.getId()%10;}
151:    var herd=level().getEntitiesOfClass(WildlifeEntity.class,getBoundingBox().inflate(24),a->a!=this&&herdId.equals(a.herdId)&&!a.isFawn()&&a.isAlive());
158:    LivingEntity prey=level().getEntitiesOfClass(net.minecraft.world.entity.animal.Sheep.class,getBoundingBox().inflate(16),a->a.isAlive()).stream().findFirst().orElse(null);
165:    for(var ally:level().getEntitiesOfClass(WildlifeEntity.class,getBoundingBox().inflate(12),a->a!=this&&(a.kind==Kind.STAG||a.kind==Kind.DOE))){ally.danger=player;ally.herdEscape=herdEscape;ally.alertTicks=5+ally.getId()%12;ally.fleeTicks=140;}
170:    var cubs=isMother()?level().getEntitiesOfClass(WildlifeEntity.class,getBoundingBox().inflate(14),a->a.kind==Kind.CUB&&getUUID().equals(a.parentId)):List.<WildlifeEntity>of();
195:   if(recovery>0)recovery--;if(leapCooldown>0)leapCooldown--;
201:    var pack=level().getEntitiesOfClass(WildlifeEntity.class,getBoundingBox().inflate(16),a->a.kind==Kind.WOLF&&a.getTarget()==getTarget()&&a.isAlive()&&a.getPersistentData().getBoolean("summonedByGreyWolf")==getPersistentData().getBoolean("summonedByGreyWolf")&&a.getPersistentData().getBoolean("GreyPackAlly")==getPersistentData().getBoolean("GreyPackAlly")&&(!getPersistentData().getBoolean("GreyPackAlly")||Objects.equals(a.getPersistentData().getUUID("GreyPackOwner"),getPersistentData().getUUID("GreyPackOwner"))));
213:  private void strike(LivingEntity target,float multiplier,float knock){
216:   int blockedBefore=target instanceof net.minecraft.server.level.ServerPlayer?((net.minecraft.server.level.ServerPlayer)target).getStats().getValue(net.minecraft.stats.Stats.CUSTOM.get(net.minecraft.stats.Stats.DAMAGE_BLOCKED_BY_SHIELD)):0;
217:   float damage=(float)getAttributeValue(Attributes.ATTACK_DAMAGE)*multiplier;
218:   if(kind==Kind.BOAR&&motion()==CHARGE)damage=7;
219:   if(isMother())damage=motion()==CHARGE?10:rage?9:8;
220:   target.hurt(damageSources().mobAttack(this),damage);
222:   boolean blocking=target instanceof net.minecraft.server.level.ServerPlayer&&((net.minecraft.server.level.ServerPlayer)target).getStats().getValue(net.minecraft.stats.Stats.CUSTOM.get(net.minecraft.stats.Stats.DAMAGE_BLOCKED_BY_SHIELD))>blockedBefore;
226:    p.getCooldowns().addCooldown(p.getUseItem().getItem(),30);p.stopUsingItem();voice("impact",.75F);
255:     if((kind==Kind.STAG||kind==Kind.DOE)&&onGround()&&!isInWater()&&leapCooldown==0&&distanceToSqr(danger)<64){
256:      setDeltaMovement(getDeltaMovement().add(0,.48,0));leapCooldown=70;
267:      if(valid(target)&&distanceToSqr(target)<3.5){strike(target,1.35F,1);action(STUN,20);return;}
276:      if(motion()==ATTACK&&actionTicks==actionLength/2){strike(target,kind==Kind.BEAR&&attackStyle()==1?1.35F:1,kind==Kind.STAG?.6F:.15F);voice("attack",.55F);}
293:     var cub=level().getEntitiesOfClass(WildlifeEntity.class,getBoundingBox().inflate(14),a->a.kind==Kind.CUB&&getUUID().equals(a.parentId)).stream().min(Comparator.comparingDouble(a->a.distanceToSqr(target))).orElse(null);
319:  @Override protected SoundEvent getHurtSound(DamageSource source){return sound("hurt");}
325:  @Override public int getMaxSpawnClusterSize(){return kind==Kind.WOLF?4:kind==Kind.STAG||kind==Kind.DOE?5:kind==Kind.CUB?1:3;}
```

## src/main/java/org/slavicmyths/equipment/ClearSightNetwork.java

```java
48:         if(!UniqueEquipmentRules.ready(now,d.getLong("SightReady"))){p.displayClientMessage(net.minecraft.network.chat.Component.translatable("unique.slavicmyths.sight_cooldown",(d.getLong("SightReady")-now+19)/20),true);return;}
49:         d.putLong("SightReady",now+UniqueEquipmentRules.SIGHT_COOLDOWN);
50:         var targets=p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(UniqueEquipmentRules.SIGHT_RADIUS),e->eligible(e,p)&&e.distanceToSqr(p)<=UniqueEquipmentRules.SIGHT_RADIUS*UniqueEquipmentRules.SIGHT_RADIUS)
```

## src/main/java/org/slavicmyths/equipment/UniqueEquipmentEvents.java

```java
4: import net.minecraft.world.entity.ai.attributes.*;
27:     @SubscribeEvent public static void respawn(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent event){cleanup(event.getEntity());}
30:         var a=e.getAttribute(Attributes.KNOCKBACK_RESISTANCE);if(a==null)return;
33:     /** Invoked after health changes as well as equipment changes; no health polling. */
35:         // LivingEntity constructor initializes health before Player constructs its inventory.
44:         if(wears(event.getEntity(),EquipmentSlot.FEET,UniqueEquipment.WIND_BOOTS.get()))event.setDamageMultiplier(event.getDamageMultiplier()*(float)UniqueEquipmentRules.FALL);
51:     @SubscribeEvent public static void damage(LivingDamageEvent.Pre event) {
53:         if(wears(e,EquipmentSlot.CHEST,UniqueEquipment.BOGATYR_CHESTPLATE.get()) && UniqueEquipmentRules.stance(e.getHealth(),e.getMaxHealth()) && !event.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY))event.setNewDamage(event.getNewDamage()*(float)(1-UniqueEquipmentRules.STANCE_REDUCTION));
56:         if(!(event.getEntity() instanceof Player p) || p.level().isClientSide || event.isCanceled() || !wears(p,EquipmentSlot.LEGS,UniqueEquipment.INDOMITABILITY_LEGGINGS.get()) || event.getStrength()*(1-p.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE))<UniqueEquipmentRules.STRONG_KB)return;
58:         d.putLong("KbReady",now+UniqueEquipmentRules.KB_COOLDOWN);p.getPersistentData().putLong("UniqueKbUntil",now+UniqueEquipmentRules.KB_DURATION);
69:         try { e.addEffect(new MobEffectInstance(incoming.getEffect(),UniqueEquipmentRules.controlDuration(incoming.getDuration()),incoming.getAmplifier(),incoming.isAmbient(),incoming.isVisible(),incoming.showIcon()),event.getEffectSource()); }
75:         var source=event.getDamageSource();var attacker=source.getEntity();
76:         boolean attack=source.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)
77:             || source.is(net.minecraft.world.damagesource.DamageTypes.MOB_ATTACK)
78:             || source.is(net.minecraft.world.damagesource.DamageTypes.MOB_ATTACK_NO_AGGRO)
79:             || source.is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK)
80:             || source.is(net.minecraft.world.damagesource.DamageTypes.STING);
82:         // NeoForge has already subtracted blocked damage: the event's clamped getter is now zero.
83:         float amount=UniqueEquipmentRules.reflected(event.getBlocked(),event.getDamageContainer().getBlockedDamage(),valid,"ward_reflection".equals(event.getDamageSource().getMsgId()),defender.getRandom().nextFloat());
84:         if(amount>0)attacker.hurt(org.slavicmyths.combat.MythDamageSources.periodic("ward_reflection",defender),amount);
```

## src/main/java/org/slavicmyths/equipment/UniqueEquipmentRules.java

```java
30:     public static final int KB_DURATION = 100, KB_COOLDOWN = 300;
31:     public static final int SIGHT_RADIUS = 24, SIGHT_DURATION = 100, SIGHT_COOLDOWN = 800;
33:     public static boolean stance(float health, float max) { return health > 0 && max > 0 && health <= max * LOW_HEALTH; }
36:     public static float reflected(boolean blocked, float damage, boolean validOwner, boolean recursion, float roll) {
37:         return blocked && damage > 0 && validOwner && !recursion && roll >= 0 && roll < REFLECT_CHANCE ? damage * REFLECT_MULTIPLIER : 0;
39:     /** Preserve fraction worn, rounded against free repair. Custom max-damage still wins. */
40:     public static int upgradedDamage(int damage, int oldMax, int newMax) {
41:         if(damage <= 0 || oldMax <= 0 || newMax <= 0) return Math.max(0,damage);
42:         return Math.min(newMax, (int)Math.ceil((double)damage * newMax / oldMax));
```

## src/main/java/org/slavicmyths/finist/BannerEntity.java

```java
2: import java.util.UUID;import net.minecraft.nbt.CompoundTag;import net.minecraft.server.level.ServerLevel;import net.minecraft.world.damagesource.DamageSource;import net.minecraft.world.entity.*;import net.minecraft.world.level.Level;
3: /** Stationary, damageable temporary prop; never writes terrain. */
9:  @Override public void die(DamageSource s){cue(anchor()?"event.encounter.curse_anchor_die":"event.encounter.black_banner_die");if(level() instanceof ServerLevel w&&boss!=null&&w.getEntity(boss) instanceof KartausEntity k)k.propGone(this,true);super.die(s);discard();}
```

## src/main/java/org/slavicmyths/finist/FalconActor.java

```java
13:  protected final Map<String,Long> cooldowns=new HashMap<>();
21:  public boolean ready(String k){return level().getGameTime()>=cooldowns.getOrDefault(k,0L);}public void cooldown(String k,int t){cooldowns.put(k,level().getGameTime()+t);}
29:  @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);var c=new CompoundTag();cooldowns.forEach(c::putLong);n.put("FalconCooldowns",c);n.putString("FalconAction",action().name());}
30:  @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);cooldowns.clear();var c=n.getCompound("FalconCooldowns");for(var k:c.getAllKeys())cooldowns.put(k,c.getLong(k));begin(FalconAction.IDLE);}
```

## src/main/java/org/slavicmyths/finist/FinistContent.java

```java
6: import net.minecraft.world.entity.ai.attributes.Attributes;
22:  ModItems.ITEMS.register("kartaus_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(KARTAUS,0x242127,0xC65B31,new net.minecraft.world.item.Item.Properties()));}
25:  e.put(FINIST.get(),Mob.createMobAttributes().add(Attributes.MAX_HEALTH,170).add(Attributes.ARMOR,12).add(Attributes.MOVEMENT_SPEED,.28).add(Attributes.FOLLOW_RANGE,24).add(Attributes.KNOCKBACK_RESISTANCE,.12).build());
26:  e.put(KARTAUS.get(),net.minecraft.world.entity.monster.Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,650).add(Attributes.ARMOR,10).add(Attributes.ARMOR_TOUGHNESS,2).add(Attributes.MOVEMENT_SPEED,.21).add(Attributes.FOLLOW_RANGE,32).add(Attributes.KNOCKBACK_RESISTANCE,.7).build());
27:  e.put(BANNER.get(),Mob.createMobAttributes().add(Attributes.MAX_HEALTH,45).add(Attributes.MOVEMENT_SPEED,0).add(Attributes.KNOCKBACK_RESISTANCE,1).build());
28:  e.put(ANCHOR.get(),Mob.createMobAttributes().add(Attributes.MAX_HEALTH,12).add(Attributes.MOVEMENT_SPEED,0).add(Attributes.KNOCKBACK_RESISTANCE,1).build());}
```

## src/main/java/org/slavicmyths/finist/FinistEffects.java

```java
9:  @SubscribeEvent(priority=EventPriority.LOWEST) public static void multiplier(LivingIncomingDamageEvent e){if(e.getAmount()>0&&e.getSource().getDirectEntity() instanceof FinistArrow a&&a.getPersistentData().getBoolean("FinistEnhanced")&&!a.getPersistentData().getBoolean("FinistApplied")){a.getPersistentData().putBoolean("FinistApplied",true);e.setAmount(e.getAmount()*1.5F);}}
10:  @SubscribeEvent public static void landed(LivingDamageEvent.Post e){if(e.getNewDamage()<=0||!(e.getSource().getEntity() instanceof ServerPlayer p)||!(e.getSource().getDirectEntity() instanceof AbstractArrow a)||!a.getPersistentData().getBoolean("FinistPair"))return;var weapon=a.getWeaponItem();if(weapon==null||!weapon.is(FinistItems.BOW.get()))return;FinistMarks.hit(p,e.getEntity(),a.getPersistentData().getBoolean("FinistManual"));if(!(a instanceof FinistArrow)||!a.getPersistentData().getBoolean("FinistEnhanced")||!GreyPackRing.enemy(p,e.getEntity()))return;
11:   var target=e.getEntity();boolean inverted=target.isInvertedHealAndHarm()||target.getType().is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","finist_inverted_heal_harm")));boolean immune=target.getType().is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","finist_immune_to_effect")));
12:   // Never apply a beneficial effect. The inverted policy uses a hostile instant-health-type source.
13:   if(!immune){if(inverted){int window=target.invulnerableTime;target.invulnerableTime=0;try{target.hurt(p.damageSources().indirectMagic(p,p),2);}finally{target.invulnerableTime=Math.max(window,target.invulnerableTime);}}else target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,60,0),p);}
```

## src/main/java/org/slavicmyths/finist/FinistEntity.java

```java
2: import java.util.*;import net.minecraft.nbt.CompoundTag;import net.minecraft.network.syncher.*;import net.minecraft.server.level.*;import net.minecraft.world.entity.*;import net.minecraft.world.entity.monster.Monster;import net.minecraft.world.damagesource.DamageSource;import net.minecraft.world.effect.*;import net.minecraft.world.level.Level;import net.minecraft.world.phys.*;import net.minecraft.core.BlockPos;import org.slavicmyths.skim.HeroCalls;
26:  public boolean evade(DamageSource s){if(action()==FalconAction.MORPH&&age()>=4&&age()<12&&s.getDirectEntity() instanceof org.slavicmyths.kalin.KalinEntity&&s.is(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,org.slavicmyths.kalin.KalinContent.id("kalin_sabre"))))return true;return action()==FalconAction.MORPH&&age()>=4&&age()<12&&s.getDirectEntity() instanceof KartausEntity&&!(s.is(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,FinistContent.id("kartaus_eye")))||s.is(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,FinistContent.id("kartaus_chains"))));}
27:  public boolean guard(DamageSource s,ServerPlayer p){if(cursed()||recovering()||morphed()||!ready("guard")||!Objects.equals(owner,p.getUUID())||distanceTo(p)>12)return false;var direct=s.getDirectEntity();if(direct instanceof org.slavicmyths.kalin.KalinEntity k&&org.slavicmyths.kalin.KalinParty.allowed(this,k)&&s.is(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,org.slavicmyths.kalin.KalinContent.id("kalin_sabre")))&&hasLineOfSight(k)){cooldown("guard",240);bird(3,k);cue("entity.falcon.intercept");return true;}if(direct instanceof KartausEntity k&&s.is(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,FinistContent.id("kartaus_melee")))&&hasLineOfSight(k)){cooldown("guard",240);bird(3,k);cue("entity.falcon.intercept");return true;}if(!(direct instanceof net.minecraft.world.entity.projectile.Projectile pr)||!(pr.getOwner() instanceof Monster)||!hasLineOfSight(pr))return false;cooldown("guard",240);pr.discard();bird(3,null);cue("entity.falcon.intercept");return true;}
31:  private boolean leap(LivingEntity t,boolean morph){if(!(level() instanceof ServerLevel w))return false;var d=t.position().subtract(position()).multiply(1,0,1);if(d.lengthSqr()<.1)return false;double travel=Math.min(6,d.length()+1);var at=KartausEncounter.ground(w,position().add(d.normalize().scale(travel)),.76,2.25);if(at==null)return false;for(int n=1;n<=10;n++){double f=n/10D;var q=position().lerp(at,f).add(0,Math.sin(f*Math.PI)*2.6,0);if(!w.noCollision(getBoundingBox().move(q.subtract(position()))))return false;}leapFrom=position();leapTo=at;committed=t;begin(morph?FalconAction.MORPH:FalconAction.DASH);cue(morph?"entity.finist.transform":"entity.finist.wing_dash");cooldown(morph?"morph":"dash",morph?600:180);if(morph){var b=boss();morphPhase=b==null?0:b.phase;bird(5,null);}return true;}
32:  private void leapTick(){boolean morph=action()==FalconAction.MORPH;if(morph&&frame(12))entityData.set(MORPH,true);int from=morph?12:4,to=morph?32:21;if(age()>=from&&age()<=to&&leapFrom!=null&&leapTo!=null){double f=(age()-from)/(double)(to-from);var q=leapFrom.lerp(leapTo,f).add(0,Math.sin(f*Math.PI)*2.6,0);var delta=q.subtract(position());if(level().noCollision(getBoundingBox().move(delta)))move(MoverType.SELF,delta);fallDistance=0;setDeltaMovement(Vec3.ZERO);if(frame(to-1)&&allowed(committed)&&edge(committed)<2.7&&hasLineOfSight(committed))committed.hurt(damageSources().mobAttack(this),morph?7:8);}
34:  private void controller(ServerPlayer p){if(cursed())return;if(bird()==1){if(tickCount%20==0){var list=level().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(20,8,20),this::allowed).stream().filter(this::hasLineOfSight).sorted(Comparator.comparingDouble(this::distanceToSqr)).limit(3).toList();EntityDataAccessor<Integer>[] ids=new EntityDataAccessor[]{WATCH1,WATCH2,WATCH3};for(int n=0;n<3;n++)entityData.set(ids[n],n<list.size()?list.get(n).getId():0);}if(level().getGameTime()-birdStarted>=100){bird(0,null);entityData.set(WATCH1,0);entityData.set(WATCH2,0);entityData.set(WATCH3,0);cooldown("watch",120);}}
35:  else if(bird()==2){if(birdAge(0)==16&&level().getEntity(entityData.get(BIRD_TARGET)) instanceof LivingEntity t&&allowed(t)&&hasLineOfSight(t)&&distanceTo(t)<=20){t.hurt(damageSources().mobAttack(this),t instanceof KartausEntity||t instanceof org.slavicmyths.kalin.KalinEntity?2:3);if(t instanceof Monster m)m.getPersistentData().putLong("FinistDistractedUntil",level().getGameTime()+35);cue("entity.falcon.dive_impact");}if(birdAge(0)>=26)bird(0,null);}else if(bird()==3&&birdAge(0)>=25)bird(0,null);
43:  if(action()!=FalconAction.IDLE){if(action()==FalconAction.DASH||action()==FalconAction.MORPH)leapTick();if(action()==FalconAction.SWORD&&frame(8)&&allowed(committed)&&cone(committed,2.7,95)&&hasLineOfSight(committed))committed.hurt(damageSources().mobAttack(this),5);if(action()==FalconAction.MARK&&frame(12)&&allowed(committed)&&distanceTo(committed)<=20&&hasLineOfSight(committed)&&!cursed()){marked=committed.getUUID();markUntil=w.getGameTime()+120;entityData.set(MARK,committed.getId());}if(action()==FalconAction.INSPIRE&&frame(15)&&distanceTo(p)<=12){p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,100,0));for(String id:List.of("ilya_muromets","dobrynya_nikitich","alyosha_popovich")){var ally=HeroCalls.loaded(p,id);if(ally instanceof LivingEntity a&&a.level()==w&&distanceTo(a)<=12)a.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,100,0));}}
45:  if(t!=null){face(t);if(!cursed()&&bird()==0&&ready("watch")){bird(1,t);cue("entity.falcon.scout_call");}if(!cursed()&&ready("mark")&&marked==null&&hasLineOfSight(t)){committed=t;begin(FalconAction.MARK);cooldown("mark",240);cue("entity.finist.mark_cast");return;}
46:  if(ready("inspire")&&(p.getHealth()<p.getMaxHealth()*.6||(t instanceof KartausEntity||t instanceof org.slavicmyths.kalin.KalinEntity))){begin(FalconAction.INSPIRE);cooldown("inspire",400);cue("entity.finist.inspire");return;}
47:  if(!cursed()&&bird()==0&&ready("birdDive")&&distanceTo(t)>=8&&distanceTo(t)<=17&&hasLineOfSight(t)){bird(2,t);cooldown("birdDive",140);cue("entity.falcon.dive_start");}if(distanceTo(t)>=3&&distanceTo(t)<=9&&ready("dash")&&!(t instanceof BannerEntity)&&leap(t,false))return;
48:  if(edge(t)<=2.7&&ready("sword")&&hasLineOfSight(t)){committed=t;begin(FalconAction.SWORD);cooldown("sword",30);cue("entity.finist.sword_swing_1");}else if(tickCount%10==0)getNavigation().moveTo(t,1);}
51:  @Override protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource s){return FinistContent.sound("entity.finist.hurt_"+(1+random.nextInt(3)));}
53:  @Override public void die(DamageSource s){if(level() instanceof ServerLevel w&&owner!=null){HeroCalls.map(w,"finist").remove(owner,getUUID());HeroCalls.dirty(w);if(encounter!=null){var e=KartausEncounter.get(w).entries.get(encounter);if(e!=null)KartausEncounter.get(w).finish(w,e,false);}}super.die(s);}
```

## src/main/java/org/slavicmyths/finist/FinistEvents.java

```java
7:  @SubscribeEvent public static void incoming(LivingIncomingDamageEvent e){if(e.getEntity() instanceof FinistEntity f&&f.evade(e.getSource())){e.setCanceled(true);return;}if(e.getEntity() instanceof ServerPlayer p&&p.level() instanceof ServerLevel){var h=HeroCalls.loaded(p,"finist");if(h instanceof FinistEntity f&&f.level()==p.level()&&f.guard(e.getSource(),p)){e.setCanceled(true);return;}}
9:  @SubscribeEvent public static void damage(LivingDamageEvent.Pre e){float d=e.getNewDamage();if(e.getEntity() instanceof KartausEntity k)d*=1-k.mitigation();if(e.getSource().getDirectEntity() instanceof ServerPlayer p&&e.getSource().getEntity()==p){var h=HeroCalls.loaded(p,"finist");if(h instanceof FinistEntity f&&f.level()==p.level()&&f.boosts(p,e.getEntity()))d*=1.15F;}
10:  if(e.getEntity() instanceof FinistEntity f&&!f.lethalUsed&&d>=f.getHealth()&&!e.getSource().is(net.minecraft.world.damagesource.DamageTypes.GENERIC_KILL)){f.lethalUsed=true;d=Math.max(0,f.getHealth()-1);f.retreat();}e.setNewDamage(d);}
11:  @SubscribeEvent public static void after(LivingDamageEvent.Post e){if(e.getEntity() instanceof KartausEntity k&&e.getNewDamage()>0)k.ownerDamage(e.getSource());}
```

## src/main/java/org/slavicmyths/finist/KartausEntity.java

```java
2: import java.util.*;import net.minecraft.core.*;import net.minecraft.core.registries.Registries;import net.minecraft.nbt.CompoundTag;import net.minecraft.network.chat.Component;import net.minecraft.network.syncher.*;import net.minecraft.resources.ResourceKey;import net.minecraft.server.level.*;import net.minecraft.world.*;import net.minecraft.world.damagesource.DamageSource;import net.minecraft.world.entity.*;import net.minecraft.world.entity.monster.*;import net.minecraft.world.item.*;import net.minecraft.world.effect.*;import net.minecraft.world.level.*;import net.minecraft.world.phys.*;
5:  public UUID owner,encounter;public Vec3 center;public int phase=1;public boolean raged;private boolean dispelled;public final Set<UUID> banners=new HashSet<>(),guards=new HashSet<>();private final java.util.List<Vec3> guardPlan=new ArrayList<>();private UUID anchor;private long curseImmune;private LivingEntity committed;private Vec3 waveOrigin,waveDir;private boolean waveStopped;private final Set<UUID> waveHits=new HashSet<>();private final Map<UUID,Chain> chains=new HashMap<>();private int lastSkill,repeated,stuck;private long lastMove;private Vec3 lastPosition;
22:  public DamageSource source(String name){return new DamageSource(level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE,FinistContent.id("kartaus_"+name))),this);}
24:  public void strike(LivingEntity t,String name,float net){if(t==null||!t.isAlive()||!hasLineOfSight(t))return;float damage=raw(net);if(phase==3&&(name.equals("wave")||name.equals("eye")))damage=raw(net*1.25F);t.hurt(source(name),damage);}
26:  public void releaseCurse(boolean rescued){var a=anchor();if(a!=null)a.discard();anchor=null;var f=hero();if(f!=null)f.clearCurse();curseImmune=level().getGameTime()+180;if(isAlive()){begin(FalconAction.STAGGER,rescued?50:20);cue("entity.falcon.chain_break");}}
27:  public void propGone(BannerEntity a,boolean destroyed){if(a.anchor()){releaseCurse(destroyed);return;}banners.remove(a.getUUID());entityData.set(COUNT,banners.size());if(banners.isEmpty()){cooldown("banner",160);begin(FalconAction.STAGGER,60);cue("entity.kartaus.barrier_break");}}
30:  public boolean bindFalcon(){var f=hero();if(!(level() instanceof ServerLevel w)||f==null||f.morphed()||f.recovering()||anchor()!=null||level().getGameTime()<curseImmune||phase<2)return false;var at=propSpot(true);if(at==null)return false;var a=FinistContent.ANCHOR.get().create(w);a.boss=getUUID();a.encounter=encounter;a.expiry=w.getGameTime()+240;a.moveTo(at.x,at.y,at.z,0,0);if(!w.addFreshEntity(a))return false;anchor=a.getUUID();f.curse(a);cue("entity.falcon.chained");return true;}
35:  private void cast(FalconAction a,String key,int cooldown){if(a.ordinal()==lastSkill)repeated++;else repeated=1;lastSkill=a.ordinal();committed=getTarget();if(committed!=null)face(committed);begin(a,phase==3?Math.max(a.ticks,a==FalconAction.GUARDS?50:a==FalconAction.CHAINS?47:0):a.ticks);cooldown(key,(int)Math.ceil(cooldown*(phase==3?.85:1)));
38:  private void choose(){var t=getTarget();if(t==null)return;if(ready("banner")&&banners.size()<(phase==1?1:2)){cast(FalconAction.BANNER,"banner",260);return;}if(ready("guards")&&guards.size()<(phase==1?2:3)&&distanceTo(t)>5){cast(FalconAction.GUARDS,"guards",240);return;}if(phase>=2&&ready("curse")&&hero()!=null&&!hero().recovering()&&!hero().morphed()&&anchor()==null&&level().getGameTime()>=curseImmune){cast(FalconAction.CURSE,"curse",360);return;}
41:  private void wave(){if(waveStopped||age()<19||age()>32||waveOrigin==null)return;int n=age()-18;var last=waveOrigin.add(waveDir.scale(n-1)).add(0,.8,0);var next=waveOrigin.add(waveDir.scale(n)).add(0,.8,0);var hit=level().clip(new ClipContext(last,next,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));if(hit.getType()!=HitResult.Type.MISS){waveStopped=true;return;}for(var t:victims(18)){var d=t.position().subtract(waveOrigin);double along=d.dot(waveDir);double side=Math.abs(d.x*waveDir.z-d.z*waveDir.x);if(along>=n-1.5&&along<=n+.6&&side<=1.5&&Math.abs(d.y)<2&&waveHits.add(t.getUUID()))strike(t,"wave",6.5F);}}
44:  case SWORD->{if(frame(13))for(var t:victims(4))if(cone(t,3.3,100)){strike(t,"melee",6);var d=t.position().subtract(position()).multiply(1,0,1).normalize();t.push(d.x*.55,.15,d.z*.55);}}
45:  case EYE->{if(frame(21)&&committed!=null)entityData.set(AIM,committed.blockPosition());if(frame(42)){var at=Vec3.atBottomCenterOf(aim());for(var t:victims(20))if(t.position().distanceToSqr(at)<2.5*2.5)strike(t,"eye",7);cue("entity.kartaus.black_eye_impact");if(phase==3&&hero()!=null&&!dispelled){dispelled=true;hero().dispel();}}}
46:  case CHAINS->{if(frame(22)&&committed!=null&&committed.position().distanceToSqr(Vec3.atBottomCenterOf(aim()))<2.5*2.5&&hasLineOfSight(committed)){strike(committed,"chains",2);committed.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,50,1));chains.put(committed.getUUID(),new Chain(committed.position(),level().getGameTime()+20,3));}}
49:  private void chainTicks(ServerLevel w){for(var item:new ArrayList<>(chains.entrySet())){var c=item.getValue();var t=w.getEntity(item.getKey());if(!(t instanceof LivingEntity l)||!l.isAlive()||l.position().distanceToSqr(c.at)>36){chains.remove(item.getKey());continue;}if(w.getGameTime()>=c.until){strike(l,"chains",1);if(c.pulses<=1)chains.remove(item.getKey());else chains.put(item.getKey(),new Chain(c.at,c.until+20,c.pulses-1));}}}
59:  public void ownerDamage(DamageSource s){if(level() instanceof ServerLevel w&&encounter!=null&&s.getEntity() instanceof ServerPlayer p&&p.getUUID().equals(owner)&&!p.isCreative()&&!p.isSpectator()){var e=KartausEncounter.get(w).entries.get(encounter);if(e!=null){e.ownerHit=true;KartausEncounter.get(w).setDirty();}}}
60:  @Override protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource s){return FinistContent.sound("entity.kartaus.hurt_"+(1+random.nextInt(3)));}
62:  @Override public void die(DamageSource s){ownerDamage(s);if(level() instanceof ServerLevel w&&encounter!=null){var e=KartausEncounter.get(w).entries.get(encounter);var p=owner==null?null:w.getServer().getPlayerList().getPlayer(owner);boolean eligible=e!=null&&e.ownerHit&&p!=null&&p.isAlive()&&!p.isCreative()&&!p.isSpectator()&&p.serverLevel()==w&&p.position().distanceToSqr(center)<36*36&&!s.is(net.minecraft.world.damagesource.DamageTypes.GENERIC_KILL);if(e!=null)KartausEncounter.get(w).finish(w,e,eligible);}cleanup();begin(FalconAction.DEATH);cue("entity.kartaus.death");super.die(s);}
```

## src/main/java/org/slavicmyths/firebird/FirebirdContent.java

```java
7: import net.minecraft.world.entity.ai.attributes.*;
17:     public static final DeferredHolder<EntityType<?>,EntityType<FirebirdEntity>> ENTITY=ModEntities.ENTITIES.register("firebird",()->EntityType.Builder.of(FirebirdEntity::new,MobCategory.CREATURE).sized(1.35F,2.6F).fireImmune().clientTrackingRange(16).updateInterval(2).build("slavicmyths:firebird"));
20:     public static final DeferredHolder<Item,Item> EGG=ModItems.ITEMS.register("firebird_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(ENTITY,0xa92b18,0xffd55b,new Item.Properties()));
27:     @SubscribeEvent public static void attributes(EntityAttributeCreationEvent e){e.put(ENTITY.get(),PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH,1024).add(Attributes.MOVEMENT_SPEED,.18).add(Attributes.FOLLOW_RANGE,40).add(Attributes.KNOCKBACK_RESISTANCE,.6).build());}
```

## src/main/java/org/slavicmyths/firebird/FirebirdEntity.java

```java
11: import net.minecraft.world.damagesource.*;
22:     private Vec3 waypoint;private LivingEntity aggressor;private int cooldown,ambient=400,call=1400,hurtCue,flightTicks;private BlockPos rewardSite;
26:     @Override public double getAttributeValue(net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute){return attribute.equals(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)?3000:super.getAttributeValue(attribute);}
34:     @Override public boolean causeFallDamage(float f,float m,DamageSource s){return false;}
36:     @Override public void tick(){super.tick();if(level().isClientSide){if(isAlive()&&tickCount%10==0)level().addParticle(net.minecraft.core.particles.ParticleTypes.GLOW,getX(),getY()+2.4,getZ(),0,.008,0);if(isAlive()&&tickCount%7==0)level().addParticle(net.minecraft.core.particles.ParticleTypes.GLOW,getX(),getY()+1.3,getZ()+1.5,0,-.008,0);return;}if(!isAlive())return;var w=(ServerLevel)level();var d=FirebirdEncounter.get(w);entityData.set(AGE,age()+1);if(cooldown>0)cooldown--;if(hurtCue>0)hurtCue--;
44:         if(state()==6){if(age()==8){cue("retaliation",.8F);if(aggressor!=null&&aggressor.isAlive()&&distanceToSqr(aggressor)<=9&&hasLineOfSight(aggressor)){var key=ResourceKey.create(Registries.DAMAGE_TYPE,FirebirdContent.id("firebird_retaliation"));aggressor.hurt(new DamageSource(w.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key),this),20);}}if(age()>=17){state(2);cooldown=60;waypoint=position().add(0,14,10);}return;}
57:         else {if(waypoint==null||position().distanceToSqr(waypoint)<9||age()%160==0){double angle=tickCount*.009;double radius=35+random.nextDouble()*20;var p=FirebirdEncounter.surface(w,anchor.getX()+(int)(Math.cos(angle)*radius),anchor.getZ()+(int)(Math.sin(angle)*radius));if(p!=null)waypoint=Vec3.atBottomCenterOf(p).add(0,12+random.nextInt(15),0);else waypoint=Vec3.atBottomCenterOf(anchor).add(0,16,0);}navigate(w,.18);if(flightTicks>800&&tickCount%100==0){var p=FirebirdEncounter.surface(w,getBlockX(),getBlockZ());if(p!=null){anchor=p;state(0);}}else if(flightTicks%320==0){state(9);cue("glide",.23F);}}
62:         var existing=w.getEntitiesOfClass(ItemEntity.class,getBoundingBox().inflate(48),i->{var n=i.getPersistentData();return n.hasUUID("FirebirdVisit")&&n.getUUID("FirebirdVisit").equals(visit)&&n.getInt("FirebirdSlot")==current;});
68:     @Override public boolean hurt(DamageSource s,float amount){boolean result=super.hurt(s,amount);if(result&&!level().isClientSide){if(hurtCue==0){cue("hurt",.55F);hurtCue=12;}if(s.getEntity() instanceof LivingEntity a&&a.isAlive()&&!(a instanceof net.minecraft.world.entity.player.Player p&&(p.isCreative()||p.isSpectator()))&&cooldown==0&&state()!=5&&state()!=6&&state()!=7){aggressor=a;state(5);cue("warning",.85F);}}return result;}
69:     @Override public void die(DamageSource s){if(!level().isClientSide){cue("death",.85F);var d=FirebirdEncounter.get((ServerLevel)level());if(d.owns(this))d.close((ServerLevel)level());}super.die(s);}
```

## src/main/java/org/slavicmyths/flight/FlyingVessel.java

```java
23: import net.minecraft.world.damagesource.DamageSource;
65:  @Override public boolean hurt(DamageSource src,float amount){if(level().isClientSide||!isAlive()||isVehicle()||viewers>0||!(src.getEntity() instanceof Player))return false;Player p=(Player)src.getEntity();if(!owns(p)||!p.isCrouching()||p.distanceToSqr(this)>16)return false;
71:  @Override public boolean causeFallDamage(float d,float multiplier,DamageSource source){return false;}
```

## src/main/java/org/slavicmyths/garden/Gardens.java

```java
52:         ITEMS.put("apple_sign",ModItems.ITEMS.register("apple_sign",()->new SignItem(new Item.Properties().stacksTo(16),block("apple_sign"),block("apple_wall_sign"))));
53:         ITEMS.put("apple_hanging_sign",ModItems.ITEMS.register("apple_hanging_sign",()->new HangingSignItem(block("apple_hanging_sign"),block("apple_wall_hanging_sign"),new Item.Properties().stacksTo(16))));
```

## src/main/java/org/slavicmyths/gorynych/BreathCloud.java

```java
16:  private UUID owner;private int life=GorynychRules.CLOUD_LIFE;private boolean collectable=true;private double radius=3;
19:  public void configure(LivingEntity e,int type,Vec3 p,int uses){owner=e.getUUID();entityData.set(TYPE,type);entityData.set(USES,uses);setPos(p);collectable=e instanceof Gorynych;life=collectable?240:80;radius=collectable?3:2;}
24:   if(tickCount%4==0)for(int i=0;i<uses()*3;i++){double a=i*Math.PI*2/Math.max(1,uses()*3)+tickCount*.04;w.sendParticles(particle(type()),getX()+Math.cos(a)*radius,getY()+(type()==2?.6:.2),getZ()+Math.sin(a)*radius,1,.1,.08,.1,0);}
25:   if(tickCount%20==0)for(var target:w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(0,.3,0),t->t!=living&&t.isAlive()&&t.position().distanceToSqr(position())<radius*radius&&allowed(living,t))){if(w.clip(new net.minecraft.world.level.ClipContext(position().add(0,.6,0),target.getEyePosition(),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,this)).getType()!=HitResult.Type.MISS)continue;target.hurt(org.slavicmyths.combat.MythDamageSources.periodic(type()==0?"gorynych_mor":type()==1?"gorynych_fire":"gorynych_impact",living),type()==2?2:3);if(type()==0)target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS,80));if(type()==1)target.igniteForSeconds(2);if(type()==2){Vec3 d=target.position().subtract(position()).normalize().scale(.4);target.setDeltaMovement(target.getDeltaMovement().add(d.x,.18,d.z));target.hurtMarked=true;}if(living instanceof Gorynych g){UUID player=org.slavicmyths.karachun.NightOwners.owner(target);if(player!=null)GorynychLedger.get(w).participant(g.instance,player);}}
```

## src/main/java/org/slavicmyths/gorynych/BreathShot.java

```java
15:  @Override public void tick(){super.tick();if(level().isClientSide)return;var w=(ServerLevel)level();if(!(getOwner() instanceof Gorynych boss)||!boss.fighting()||tickCount>90||!w.hasChunkAt(blockPosition())){discard();return;}var from=position();var to=from.add(getDeltaMovement());var block=level().clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));var entity=ProjectileUtil.getEntityHitResult(level(),this,from,to,getBoundingBox().expandTowards(getDeltaMovement()).inflate(.6),e->e instanceof LivingEntity l&&boss.candidate(l));HitResult hit=entity!=null&&(block.getType()==HitResult.Type.MISS||from.distanceToSqr(entity.getLocation())<from.distanceToSqr(block.getLocation()))?entity:block;if(hit.getType()!=HitResult.Type.MISS){var at=hit.getLocation();for(var t:w.getEntitiesOfClass(LivingEntity.class,new AABB(at.subtract(2,2,2),at.add(2,2,2)),boss::candidate))if(boss.hasLineOfSight(t)){t.hurt(org.slavicmyths.combat.MythDamageSources.caused(type()==0?"gorynych_mor":"gorynych_fire",boss),type()==0?6:8);if(type()==1)t.igniteForSeconds(3);}boss.cloud(type(),at,3);boss.sound(type()==0?"mor_cloud":"head_fireball");discard();return;}setPos(to);w.sendParticles(BreathCloud.particle(type()),getX(),getY(),getZ(),5,.12,.12,.12,0);}
```

## src/main/java/org/slavicmyths/gorynych/Gorynych.java

```java
9: import net.minecraft.world.entity.ai.attributes.*;
13: import net.minecraft.world.damagesource.*;
18: /** One server health owner, three native multipart heads and bounded attack channels. */
22:  private static final EntityDataAccessor<Float> HP=SynchedEntityData.defineId(Gorynych.class,EntityDataSerializers.FLOAT);
27:  private final Set<UUID> effects=new HashSet<>();private final List<Vec3> marks=new ArrayList<>();private float pendingDamage;private boolean damagePass;private int bodyAge,bodyRest=80,patrolAt,airAge;private Vec3 bodyAim=Vec3.ZERO;private List<LivingEntity> nearby=List.of();private int nearbyAt=-100;private ServerPlayer grabbed;private int grabAge,grabHits;
29:  public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,1024).add(Attributes.MOVEMENT_SPEED,.23).add(Attributes.FOLLOW_RANGE,24).add(Attributes.ARMOR,12).add(Attributes.ARMOR_TOUGHNESS,6).add(Attributes.KNOCKBACK_RESISTANCE,1);}
30:  @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(STATE,0);b.define(AGE,0);b.define(CLEAN,0);b.define(BODY,0);b.define(WINGS,false);b.define(FLY,false);b.define(DUCK,false);b.define(HP,GorynychRules.HP);b.define(HEADS,new CompoundTag());}
31:  @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(2,new Goal(){ {setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}@Override public boolean canUse(){return (state()==GorynychRules.GROUND||state()==GorynychRules.AIR&&!flying())&&getTarget()!=null;}@Override public void tick(){var t=getTarget();getLookControl().setLookAt(t,8,8);if(tickCount%20==0&&distanceToSqr(t)>90&&GorynychPlaces.loaded((ServerLevel)level(),getBoundingBox().inflate(40)))getNavigation().moveTo(t,.85);}});}
32:  public int state(){return entityData.get(STATE);}public int age(){return entityData.get(AGE);}public int cleanMask(){return entityData.get(CLEAN);}public boolean wings(){return entityData.get(WINGS);}public boolean flying(){return entityData.get(FLY);}public boolean ducking(){return entityData.get(DUCK);}public float logicalHealth(){return entityData.get(HP);}public int bodyAction(){return entityData.get(BODY);}public CompoundTag heads(){return entityData.get(HEADS);}
35:  public void configure(UUID id,CompoundTag record){instance=id;layout=record.copy();node=record.getInt("Node");entityData.set(HP,Math.clamp(record.getFloat("HP"),GorynychRules.FLOOR,GorynychRules.HP));entityData.set(CLEAN,record.getInt("Clean")&7);entityData.set(WINGS,record.getBoolean("Wings"));state(record.getInt("State"));if(state()==GorynychRules.TRANSFORM)entityData.set(AGE,record.getInt("Age"));if(state()==GorynychRules.CLEANSING&&cleanMask()==7)entityData.set(AGE,record.getInt("Age"));super.setHealth(1024*logicalHealth()/GorynychRules.HP);}
37:  @Override public void move(MoverType type,Vec3 delta){if(instance!=null&&level() instanceof ServerLevel w){var bounds=GorynychPlaces.bounds(layout);double r=getBbWidth()/2;delta=new Vec3(Math.clamp(getX()+delta.x,bounds.minX+r,bounds.maxX-r)-getX(),Math.clamp(getY()+delta.y,bounds.minY,bounds.maxY-getBbHeight())-getY(),Math.clamp(getZ()+delta.z,bounds.minZ+r,bounds.maxZ-r)-getZ());if(!GorynychPlaces.loaded(w,getBoundingBox().expandTowards(delta).inflate(1)))delta=Vec3.ZERO;}super.move(type,delta);}
40:  private List<LivingEntity> targets(){if(tickCount-nearbyAt>=10){nearby=level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(48,30,48),this::candidate);nearbyAt=tickCount;}return nearby.stream().filter(this::candidate).toList();}
42:  private boolean hit(LivingEntity t,float amount,int type){if(!fighting()||!candidate(t)||!hasLineOfSight(t))return false;boolean ok=t.hurt(org.slavicmyths.combat.MythDamageSources.caused(type==0?"gorynych_mor":type==1?"gorynych_fire":"gorynych_impact",this),amount);if(ok){credit(t);if(type==1)t.igniteForSeconds(3);if(type==0)t.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS,100));}return ok;}
48:   bar.setProgress(logicalHealth()/GorynychRules.HP);bar.setName(GorynychContent.text("health",Math.round(logicalHealth()),3600));
51:   if(state()==GorynychRules.EXHAUSTED||state()==GorynychRules.CLEANSING){releaseGrab();setDeltaMovement(Vec3.ZERO);bar.removeAllPlayers();if(state()==GorynychRules.CLEANSING&&cleanMask()==7){if(tickCount%4==0)particle(2,position().add(0,2,0),18);if(age()>=GorynychRules.CLEANSE_TICKS){state(GorynychRules.PURIFIED);entityData.set(HP,GorynychRules.HP);super.setHealth(1024);entityData.set(WINGS,true);sound("purified_roar");persist();w.getServer().overworld().getDataStorage().save();}}return;}
54:  private void patrol(){if(tickCount<patrolAt)return;patrolAt=tickCount+100;int next=switch(node){case 0->getRandom().nextBoolean()?1:3;case 1->getRandom().nextBoolean()?0:2;case 2->1;case 3->getRandom().nextBoolean()?0:4;case 4->3;case 5->6;case 6->5;case 7->8;default->7;};var p=GorynychPlaces.node(layout,next);if(GorynychPlaces.safe((ServerLevel)level(),this,p)&&GorynychPlaces.loaded((ServerLevel)level(),getBoundingBox().inflate(40))&&getNavigation().moveTo(p.x,p.y,p.z,.7))node=next;}
82:  @Override public boolean hurt(DamageSource source,float amount){if(level().isClientSide||!Float.isFinite(amount)||amount<=0||state()>=GorynychRules.EXHAUSTED||state()==GorynychRules.TRANSFORM)return false;if(source.getEntity() instanceof Gorynych)return false;if(!layout.getBoolean("Standalone")&&source.getEntity() instanceof LivingEntity attacker&&!candidate(attacker))return false;if(damagePass){boolean nested=super.hurt(source,amount);if(nested)credit(source.getEntity());return nested;}if(state()==GorynychRules.PATROL)state(GorynychRules.GROUND);pendingDamage=0;damagePass=true;super.setHealth(1024);boolean hit=super.hurt(source,amount);damagePass=false;entityData.set(HP,GorynychRules.remaining(logicalHealth(),pendingDamage));super.setHealth(1024*logicalHealth()/GorynychRules.HP);if(hit&&pendingDamage>0){credit(source.getEntity());resistGrab();}if(logicalHealth()<=GorynychRules.FLOOR)exhaust();else if(logicalHealth()<=GorynychRules.TRANSFORM_HP&&!wings()){stopAttacks();state(GorynychRules.TRANSFORM);sound("transform_start");}persist();return hit;}
83:  @Override public void setHealth(float value){if(damagePass){pendingDamage+=Math.max(0,getHealth()-value);super.setHealth(Math.max(1,value));}else super.setHealth(Math.max(1,value));}
84:  @Override public void die(DamageSource source){if(!level().isClientSide&&state()<GorynychRules.EXHAUSTED)exhaust();}
86:  private void exhaust(){stopAttacks();entityData.set(HP,GorynychRules.FLOOR);state(GorynychRules.EXHAUSTED);surface();entityData.set(FLY,false);setNoGravity(false);sound("exhausted");persist();}
93:  @Override public void checkDespawn(){}
97:  private void persist(){if(instance==null||!(level() instanceof ServerLevel w))return;var d=GorynychLedger.get(w);var n=d.row(instance);n.putUUID("Boss",getUUID());n.putLong("LastPosition",blockPosition().asLong());n.putFloat("HP",logicalHealth());n.putInt("State",state());n.putInt("Age",age());n.putInt("Clean",cleanMask());n.putBoolean("Wings",wings());n.putInt("Node",node);d.setDirty();effects.removeIf(id->w.getEntity(id)==null);}
99:  @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);instance=n.hasUUID("GorynychInstance")?n.getUUID("GorynychInstance"):null;layout=n.getCompound("GorynychLayout");node=n.getInt("GorynychNode");entityData.set(HP,n.contains("GorynychHP")?Math.clamp(n.getFloat("GorynychHP"),180,3600):3600);entityData.set(CLEAN,n.getInt("GorynychClean")&7);entityData.set(WINGS,n.getBoolean("GorynychWings"));state(n.getInt("GorynychState"));entityData.set(AGE,n.getInt("GorynychAge"));if(instance!=null&&level() instanceof ServerLevel w){var row=GorynychLedger.get(w).row(instance);if(row.contains("Origin"))configure(instance,row);}super.setHealth(1024*logicalHealth()/3600);stopAttacks();}
```

## src/main/java/org/slavicmyths/gorynych/GorynychContent.java

```java
11:  public static final DeferredHolder<EntityType<?>,EntityType<Gorynych>> BOSS=ModEntities.ENTITIES.register("zmey_gorynych",()->EntityType.Builder.of(Gorynych::new,MobCategory.MONSTER).sized(4.8f,5.5f).eyeHeight(4.8f).fireImmune().clientTrackingRange(20).updateInterval(2).build("slavicmyths:zmey_gorynych"));
25:   ITEMS.put("zmey_gorynych_spawn_egg",ModItems.ITEMS.register("zmey_gorynych_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(BOSS,0x202126,0xA52FDB,new Item.Properties())));
```

## src/main/java/org/slavicmyths/gorynych/GorynychEquipment.java

```java
10: import net.minecraft.tags.DamageTypeTags;
19:  @SubscribeEvent public static void damage(LivingDamageEvent.Pre e){if(e.getEntity().level().isClientSide)return;var target=e.getEntity();if(worn(target,EquipmentSlot.CHEST)&&(e.getSource().is(DamageTypeTags.IS_FIRE)||e.getSource().is(DamageTypeTags.IS_EXPLOSION)))e.setNewDamage(e.getNewDamage()*.85f);if(worn(target,EquipmentSlot.FEET)&&e.getSource().is(net.minecraft.world.damagesource.DamageTypes.FALL))e.setNewDamage(e.getNewDamage()*.65f);}
22:  @SubscribeEvent public static void player(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post e){if(!(e.getEntity() instanceof ServerPlayer p)||p.tickCount%20!=0||!p.isAlive())return;var n=GorynychLedger.part(org.slavicmyths.rpg.PathData.data(p),"GorynychEquipment");long now=p.serverLevel().getGameTime();if(p.getHealth()<p.getMaxHealth()*.3&&GorynychContent.ARMOR.keySet().stream().allMatch(s->worn(p,s))&&now>=n.getLong("StandReady")){n.putLong("StandReady",now+3600);p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,100,0));GorynychContent.sound(p.level(),p.blockPosition(),"armor_fullset_proc");}}
23:  @SubscribeEvent public static void hit(LivingDamageEvent.Post e){if(e.getEntity().level().isClientSide||e.getNewDamage()<=0||SECONDARY.get()||e.getSource().getDirectEntity()==null)return;var from=e.getSource().getEntity();if(!(from instanceof LivingEntity owner)||!BreathCloud.allowed(owner,e.getEntity()))return;ItemStack weapon=owner.getMainHandItem();boolean ranged=e.getSource().getDirectEntity() instanceof Projectile;if(e.getSource().getDirectEntity() instanceof AbstractArrow arrow&&arrow.getWeaponItem()!=null)weapon=arrow.getWeaponItem();var n=GorynychLedger.part(owner instanceof ServerPlayer p?org.slavicmyths.rpg.PathData.data(p):owner.getPersistentData(),"GorynychRuneCounters");for(String rune:GorynychContent.RUNES){if(!supports(weapon,rune)||!org.slavicmyths.rpg.Runes.has(weapon,rune)||(rune.equals("storm_strike")?!ranged:ranged&&!org.slavicmyths.rpg.Runes.category(weapon).equals("staff")))continue;int threshold=rune.equals("mor_breath")?5:4;int count=n.getInt(rune)+1;n.putInt(rune,count%threshold);if(count%threshold==0)burst(owner,e.getEntity().position(),rune.equals("mor_breath")?0:rune.equals("dragon_flame")?1:2,rune.equals("mor_breath")?2:3,e.getEntity());}}
25:  public static void burst(LivingEntity owner,Vec3 at,int type,double radius){burst(owner,at,type,radius,null);}
26:  private static void burst(LivingEntity owner,Vec3 at,int type,double radius,LivingEntity excluded){if(!(owner.level() instanceof ServerLevel w))return;SECONDARY.set(true);try{if(type==0){var cloud=GorynychContent.CLOUD.get().create(w);if(cloud!=null){cloud.configure(owner,0,at,3);w.addFreshEntity(cloud);}return;}for(var target:w.getEntitiesOfClass(LivingEntity.class,new AABB(at.subtract(radius,radius,radius),at.add(radius,radius,radius)),t->t!=excluded&&BreathCloud.allowed(owner,t)&&t.position().distanceToSqr(at)<=radius*radius)){if(w.clip(new net.minecraft.world.level.ClipContext(at.add(0,.5,0),target.getEyePosition(),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,owner)).getType()!=HitResult.Type.MISS)continue;if(type==1){target.hurt(org.slavicmyths.combat.MythDamageSources.periodic("gorynych_fire",owner),5);target.igniteForSeconds(2);}else{var delta=target.position().subtract(at).normalize().scale(.65);target.setDeltaMovement(target.getDeltaMovement().add(delta.x,.2,delta.z));target.hurtMarked=true;if(target instanceof Mob mob&&!(mob instanceof Gorynych)){mob.getNavigation().stop();mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,20,2));}}}w.sendParticles(BreathCloud.particle(type),at.x,at.y+.3,at.z,36,radius,.4,radius,.04);}finally{SECONDARY.set(false);}}
30:  @SubscribeEvent public static void attack(net.neoforged.neoforge.event.entity.player.AttackEntityEvent e){if(e.getEntity() instanceof ServerPlayer p)escape(p);}
31:  private static void escape(ServerPlayer p){for(var boss:p.serverLevel().getEntitiesOfClass(Gorynych.class,p.getBoundingBox().inflate(16),b->b.grabbed(p)))boss.resistGrab();}
```

## src/main/java/org/slavicmyths/gorynych/GorynychRules.java

```java
4:  public static final float HP=3600,TRANSFORM_HP=1800,FLOOR=180;
7:  public static float remaining(float hp,float damage){return Math.max(FLOOR,hp-Math.max(0,damage));}
```

## src/main/java/org/slavicmyths/guardians/FireGuardian.java

```java
11:  @Override protected boolean hit(LivingEntity target,float damage){return super.hit(target,damage,state()==GuardianRules.MELEE?damageSources().mobAttack(this):GuardianContent.fireDamage(this));}
16:   if(s==GuardianRules.PULSE){if(a<GuardianRules.PULSE_WINDUP&&a%4==0){ring(position(),3,ParticleTypes.SMOKE);ring(position(),6,ParticleTypes.FLAME);ring(position(),10,ParticleTypes.SMOKE);}if(a==GuardianRules.PULSE_WINDUP){sound("fire_pulse_burst");for(var t:targets())hit(t,GuardianRules.pulse(distanceTo(t)));ring(position(),6,ParticleTypes.FLAME);}if(a>=GuardianRules.PULSE_WINDUP+18){pulseReady=now+(lastEmbers()?224:GuardianRules.PULSE_COOLDOWN);state(GuardianRules.COMBAT);}return;}
17:   if(s==GuardianRules.SLAM){if(a%4==0)cracks(a>=GuardianRules.SLAM_WINDUP);if(a==GuardianRules.SLAM_WINDUP){sound("fire_guardian_ground_impact");for(var t:targets())if(inCrack(t.position())&&t.getY()<getY()+.65&&hit(t,GuardianRules.SLAM_DAMAGE))t.knockback(.25,getX()-t.getX(),getZ()-t.getZ());}if(a>=GuardianRules.SLAM_WINDUP+18){slamReady=now+GuardianRules.SLAM_COOLDOWN;state(GuardianRules.COMBAT);}return;}
18:   if(s==GuardianRules.LOCKDOWN){int warning=GuardianRules.lockWarning(finalLock),length=warning+GuardianRules.LOCK_ACTIVE,wave=Math.min(2,(a-1)/length),local=(a-1)%length;boolean active=local>=warning;if(local==0&&wave>0)sound("fire_lockdown_warning");if(local==warning)sound("fire_lockdown_ignite");
20:    if(active&&local%15==warning%15)for(var t:targets())if(dangerous(t.position(),wave))hit(t,GuardianRules.ZONE_DAMAGE,GuardianContent.fireDamage(this));
21:    if(a>=length*GuardianRules.LOCK_WAVES){lockReady=now+GuardianRules.LOCK_COOLDOWN;sound("fire_shield_break");state(GuardianRules.EXHAUSTED);}return;}
```

## src/main/java/org/slavicmyths/guardians/GuardianContent.java

```java
17:  public static final DeferredHolder<Item,Item> FIRE_EGG=ModItems.ITEMS.register("extinguished_flame_guardian_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(FIRE,0x332723,0xF39534,new Item.Properties())),WATER_EGG=ModItems.ITEMS.register("defiled_spring_keeper_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(WATER,0x244E51,0x92D6C3,new Item.Properties()));
24:  static {for(String s:List.of("fire_guardian_idle","fire_guardian_awaken","fire_guardian_step","fire_guardian_hurt","fire_guardian_swing","fire_pulse_charge","fire_pulse_burst","fire_lockdown_warning","fire_lockdown_ignite","fire_shield_break","fire_guardian_ground_impact","fire_guardian_purify","spring_keeper_idle","spring_keeper_emerge","spring_keeper_step","spring_keeper_hurt","spring_whip_windup","spring_whip_impact","spring_whirlpool_warning","spring_whirlpool_loop","spring_prison_form","spring_prison_break","spring_flood_rise","spring_heal_begin","spring_heal_interrupt","spring_valve_switch","spring_keeper_purify","amulet_fragment_claim"))SOUNDS.put(s,ModSounds.SOUNDS.register("guardians."+s,()->SoundEvent.createFixedRangeEvent(id("guardians."+s),24)));}
26:  public static net.minecraft.world.damagesource.DamageSource fireDamage(GuardianEntity e){var key=net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,id("guardian_fire"));return new net.minecraft.world.damagesource.DamageSource(e.level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(key),e,e);}
```

## src/main/java/org/slavicmyths/guardians/GuardianEntity.java

```java
8: import net.minecraft.world.entity.ai.attributes.*;
11: import net.minecraft.world.damagesource.*;
25:  public static AttributeSupplier.Builder attributes(boolean water){return createMobAttributes().add(Attributes.MAX_HEALTH,GuardianRules.maximum(water)).add(Attributes.ARMOR,water?GuardianRules.WATER_ARMOR:GuardianRules.FIRE_ARMOR).add(Attributes.MOVEMENT_SPEED,water?GuardianRules.WATER_SPEED:GuardianRules.FIRE_SPEED).add(Attributes.ATTACK_DAMAGE,water?GuardianRules.WATER_MELEE:GuardianRules.FIRE_MELEE).add(Attributes.FOLLOW_RANGE,24).add(Attributes.KNOCKBACK_RESISTANCE,.8);}
31:  @Override public void move(MoverType type,Vec3 delta){if(instance!=null&&!level().isClientSide){var b=arena();double radius=getBbWidth()/2+.05;double x=Math.clamp(getX()+delta.x,b.minX+radius,b.maxX-radius),z=Math.clamp(getZ()+delta.z,b.minZ+radius,b.maxZ-radius);delta=new Vec3(x-getX(),delta.y,z-getZ());}super.move(type,delta);}
38:  public void configure(UUID id,CompoundTag n){instance=id;layout=n.copy();syncLayout();super.setHealth(n.contains("Health")?Math.max(GuardianRules.threshold(water),n.getFloat("Health")):getMaxHealth());if(n.getString("Stage").equals("CLEARED")){super.setHealth(getMaxHealth());state(GuardianRules.CLEANSED);}else if(n.getString("Stage").equals("PURIFICATION"))state(GuardianRules.PURIFY);else state(GuardianRules.IDLE);}
41:  protected boolean hit(LivingEntity t,float damage){return hit(t,damage,damageSources().mobAttack(this));}
42:  protected boolean hit(LivingEntity t,float damage,DamageSource source){if(!fighting()||!candidate(t)||!hasLineOfSight(t)||damage<=0)return false;boolean hit=t.hurt(source,damage);if(hit)credit(t);return hit;}
46:  protected void ring(Vec3 center,double radius,net.minecraft.core.particles.ParticleOptions kind){for(int i=0;i<16;i++){double a=i*Math.PI/8;var p=center.add(Math.cos(a)*radius,.1,Math.sin(a)*radius);if(arena().contains(p))particle(kind,p,1);}}
49:   if(!arena().contains(position())){var back=Vec3.atBottomCenterOf(BlockPos.of(layout.getLong("SpawnAnchor")));if(w.hasChunkAt(BlockPos.containing(back))){setPos(back);getNavigation().stop();}}
50:   entityData.set(AGE,age()+1);if(tickCount%20==0){n.putUUID("Boss",getUUID());n.putLong("LastPosition",blockPosition().asLong());n.putFloat("Health",getHealth());d.setDirty();GuardianWorld.install(w,instance,n);var ts=targets();boolean any=ts.stream().anyMatch(t->t instanceof ServerPlayer);absent=any?0:absent+20;for(var old:new ArrayList<>(bar.getPlayers()))if(!candidate(old)||state()>=GuardianRules.PURIFY||state()==GuardianRules.IDLE)bar.removePlayer(old);if(state()>GuardianRules.IDLE&&state()<GuardianRules.PURIFY)for(var t:ts)if(t instanceof ServerPlayer p){bar.addPlayer(p);d.participate(instance,p.getUUID());}
61:  /** Spawn eggs and /summon get a real local encounter rather than disappearing. */
70:  private void complete(){state(GuardianRules.CLEANSED);scene=false;entityData.set(SCENE,false);setHealth(getMaxHealth());var w=(ServerLevel)level();var d=GuardianLedger.get(w);d.stage(instance,"CLEARED");var n=d.row(instance);n.putInt("Valves",3);n.putFloat("Health",getHealth());GuardianWorld.install(w,instance,n);w.getServer().overworld().getDataStorage().save();for(var t:targets())if(t instanceof ServerPlayer p){org.slavicmyths.tales.TaleService.promote(p,"slavicmyths:"+(water?"cleansed_spring_keeper":"cleansed_flame_guardian"),4,true);p.sendSystemMessage(GuardianContent.text(water?"water_cleansed":"fire_cleansed"));}clearHazards();}
71:  @Override public boolean hurt(DamageSource source,float amount){if(level().isClientSide||instance==null)return false;if(source.is(DamageTypes.GENERIC_KILL)){clearHazards();bar.removeAllPlayers();discard();return true;}if(!fighting()||state()==GuardianRules.LOCKDOWN||source.getEntity() instanceof GuardianEntity||source.getEntity() instanceof WaterEcho)return false;if(source.getEntity() instanceof LivingEntity attacker&&!candidate(attacker))return false;credit(source.getEntity());return super.hurt(source,amount);}
72:  @Override public void setHealth(float health){if(instance!=null&&state()<GuardianRules.PURIFY&&health<=GuardianRules.threshold(water)&&!level().isClientSide){beginPurification();return;}super.setHealth(health);}
75:  @Override protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source){return GuardianContent.SOUNDS.get(water?"spring_keeper_hurt":"fire_guardian_hurt").get();}
```

## src/main/java/org/slavicmyths/guardians/GuardianRules.java

```java
5:  public static final int AWAKEN_TICKS=60,MELEE_WINDUP=11,MELEE_RECOVERY=9,PULSE_WINDUP=30,PULSE_COOLDOWN=280,SLAM_WINDUP=20,SLAM_COOLDOWN=180,LOCK_WARNING=24,LOCK_ACTIVE=40,LOCK_WAVES=3,LOCK_COOLDOWN=500,EXHAUST=40,WHIP_WINDUP=16,WHIP_GAP=10,WHIP_COOLDOWN=140,WHIRL_WARNING=24,WHIRL_ACTIVE=80,WHIRL_COOLDOWN=280,PRISON_WARNING=20,PRISON_MAX=80,PRISON_COOLDOWN=400,VALVE_USE=10,HEAL_WARNING=40,HEAL_TICK=20,HEAL_BLOCK=400,FLOOD_WARNING=24,FLOOD_ACTIVE=140,PURIFY_SCENE=60,ECHO_LIFE=300;
6:  public static final float FIRE_HP=500,WATER_HP=450,FIRE_ARMOR=7,WATER_ARMOR=4,FIRE_MELEE=5,WATER_MELEE=3,SLAM_DAMAGE=4,ZONE_DAMAGE=2,WHIP_DAMAGE=3,WHIRL_DAMAGE=2,HEAL_AMOUNT=5,ECHO_HP=25,ECHO_DAMAGE=2;
7:  public static final double FIRE_SPEED=.21,WATER_SPEED=.26,WHIRL_RADIUS=2.5,WHIRL_PULL=.065,PRISON_DRAG=.65,FLOOD_DRAG=.82;
8:  public static final int HEAL_ACTIVE=200,HEAL_COOLDOWN=200,SEEK_LIMIT=100,FLOOD_COOLDOWN=340,FLOOD_STEP=50,ABSENT_RESET=200;
```

## src/main/java/org/slavicmyths/guardians/SpringKeeper.java

```java
24:   if(s==GuardianRules.SEEK){var center=Vec3.atBottomCenterOf(net.minecraft.core.BlockPos.of(layout.getLong("SpawnAnchor")));getNavigation().moveTo(center.x,center.y,center.z,1);if(distanceToSqr(center)<4){state(GuardianRules.HEAL);sound("spring_heal_begin");}else if(a>GuardianRules.SEEK_LIMIT){healReady=now+GuardianRules.HEAL_COOLDOWN;state(GuardianRules.COMBAT);}return;}
25:   if(s==GuardianRules.HEAL){if(a%4==0)for(int role=0;role<2;role++){var from=Vec3.atCenterOf(GuardianWorld.valveAt(layout,role));for(int i=0;i<6;i++){var p=from.lerp(position().add(0,1.5,0),i/6.);particle(ParticleTypes.GLOW,p,(valves()&(1<<role))==0?2:1);}}if(a>=GuardianRules.HEAL_WARNING&&a%GuardianRules.HEAL_TICK==0&&GuardianRules.healAllowed(valves(),now,n.getLong("ValveBlockedUntil"))&&w.getBlockState(GuardianWorld.healingSource(n)).is(org.slavicmyths.sanctuary.SanctuaryContent.block("pure_spring_heart"))&&distanceToSqr(Vec3.atBottomCenterOf(net.minecraft.core.BlockPos.of(layout.getLong("SpawnAnchor"))))<=4)heal(GuardianRules.HEAL_AMOUNT);if(a>=GuardianRules.HEAL_WARNING+GuardianRules.HEAL_ACTIVE){healReady=now+GuardianRules.HEAL_COOLDOWN;state(GuardianRules.COMBAT);}return;}
27:   if(s==GuardianRules.WHIP){if(getTarget()==null){whipReady=now+GuardianRules.WHIP_COOLDOWN;state(GuardianRules.COMBAT);return;}if(a==GuardianRules.WHIP_WINDUP||a==GuardianRules.WHIP_WINDUP+GuardianRules.WHIP_GAP){boolean second=a>GuardianRules.WHIP_WINDUP;sound("spring_whip_impact");for(var t:targets())if(arc(t,second)){hit(t,GuardianRules.WHIP_DAMAGE);t.knockback(.12,getX()-t.getX(),getZ()-t.getZ());}}if(a>=GuardianRules.WHIP_WINDUP+GuardianRules.WHIP_GAP+12){whipReady=now+(desperation()?110:GuardianRules.WHIP_COOLDOWN);state(GuardianRules.COMBAT);}return;}
28:   if(s==GuardianRules.WHIRL){if(a%4==0)ring(whirl,GuardianRules.WHIRL_RADIUS,a<=GuardianRules.WHIRL_WARNING?ParticleTypes.GLOW:ParticleTypes.BUBBLE);if(a==GuardianRules.WHIRL_WARNING)sound("spring_whirlpool_loop");if(a>GuardianRules.WHIRL_WARNING)for(var t:targets())if(t.position().distanceToSqr(whirl)<=6.25&&hasLineOfSight(t)){var pull=whirl.subtract(t.position()).multiply(1,0,1).normalize().scale(GuardianRules.WHIRL_PULL);t.setDeltaMovement(t.getDeltaMovement().add(pull));t.hurtMarked=true;if((a-GuardianRules.WHIRL_WARNING)%20==0)hit(t,GuardianRules.WHIRL_DAMAGE);}if(a>=GuardianRules.WHIRL_WARNING+GuardianRules.WHIRL_ACTIVE){whirlReady=now+GuardianRules.WHIRL_COOLDOWN;state(GuardianRules.COMBAT);}return;}
29:   if(s==GuardianRules.PRISON){var t=prisonTarget==null?null:w.getEntity(prisonTarget);if(t instanceof LivingEntity l&&candidate(l)&&a<GuardianRules.PRISON_WARNING&&a%4==0)ring(l.position(),1,ParticleTypes.GLOW);if(a==GuardianRules.PRISON_WARNING&&t instanceof ServerPlayer p&&candidate(p)&&safePrison(p)){prison=new WaterPrison(GuardianContent.PRISON.get(),w);prison.configure(this,p);w.addFreshEntity(prison);lastPrison=p.getUUID();}if(a>=GuardianRules.PRISON_WARNING+10){prisonReady=now+GuardianRules.PRISON_COOLDOWN;state(GuardianRules.COMBAT);}return;}
30:   if(s==GuardianRules.FLOOD){if(a%4==0){var regions=floodRegions();for(int region=0;region<regions.length;region++){var b=regions[region];int local=a-GuardianRules.floodDelay(region);double height=local<GuardianRules.FLOOD_WARNING?.05:Math.min(.8,(local-GuardianRules.FLOOD_WARNING)/25.);height=Math.min(height,Math.max(.05,(GuardianRules.FLOOD_WARNING+GuardianRules.FLOOD_ACTIVE-a)/30.));for(double x=b.minX+.5;x<b.maxX;x+=2)for(double z=b.minZ+.5;z<b.maxZ;z+=2)particle(local<GuardianRules.FLOOD_WARNING?ParticleTypes.GLOW:ParticleTypes.SPLASH,new Vec3(x,b.minY+height,z),1);}}if(a>GuardianRules.FLOOD_WARNING)for(var t:targets())if(floodDanger(t.getX(),t.getZ())&&floodRegion(t.position())>=0&&a>=GuardianRules.FLOOD_WARNING+GuardianRules.floodDelay(floodRegion(t.position()))){t.setDeltaMovement(t.getDeltaMovement().multiply(GuardianRules.FLOOD_DRAG,1,GuardianRules.FLOOD_DRAG));t.hurtMarked=true;}if(a>=GuardianRules.FLOOD_WARNING+GuardianRules.FLOOD_ACTIVE){floodReady=now+GuardianRules.FLOOD_COOLDOWN;state(GuardianRules.COMBAT);}return;}
34:  private int floodRegion(Vec3 p){var areas=floodRegions();for(int i=0;i<areas.length;i++)if(areas[i].inflate(0,5,0).contains(p))return i;return -1;}
36:  private boolean safePrison(LivingEntity t){return arena().contains(t.position().add(-1,0,-1))&&arena().contains(t.position().add(1,2.7,1))&&level().noCollision(t,t.getBoundingBox().inflate(.6,.1,.6));}
38:   for(int role=0;role<2;role++){String key="PendingValve"+role;var action=n.getCompound(key);if(!action.hasUUID("Player")||now<action.getLong("At"))continue;n.remove(key);var p=w.getServer().getPlayerList().getPlayer(action.getUUID("Player"));var at=GuardianWorld.valveAt(n,role);if(p==null||p.level()!=w||!p.isAlive()||p.isSpectator()||p.distanceToSqr(Vec3.atCenterOf(at))>25||!w.mayInteract(p,at)||p.server.isUnderSpawnProtection(w,at,p))continue;int mask=n.getInt("Valves")|(1<<role);n.putInt("Valves",mask);d.setDirty();credit(p);GuardianContent.sound(w,at,"spring_valve_switch");if(mask==3){n.putLong("ValveBlockedUntil",now+GuardianRules.HEAL_BLOCK);healReady=now+GuardianRules.HEAL_BLOCK;state(GuardianRules.INTERRUPTED);sound("spring_heal_interrupt");}GuardianWorld.install(w,instance,n);}
```

## src/main/java/org/slavicmyths/guardians/WaterEcho.java

```java
4: import net.minecraft.world.entity.ai.attributes.*;
12:  public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,GuardianRules.ECHO_HP).add(Attributes.MOVEMENT_SPEED,.27).add(Attributes.ATTACK_DAMAGE,GuardianRules.ECHO_DAMAGE).add(Attributes.FOLLOW_RANGE,16);}
15:  @Override public void tick(){super.tick();if(!(level() instanceof ServerLevel w))return;var master=keeper==null?null:w.getEntity(keeper);if(!(master instanceof SpringKeeper b)||!b.fighting()||--life<=0||!b.arena().contains(position())){discard();return;}if(tickCount%10==0){var t=b.getTarget();if(t!=null&&b.candidate(t)){getLookControl().setLookAt(t);getNavigation().moveTo(t,1);if(distanceToSqr(t)<36&&hasLineOfSight(t)&&tickCount%40==0){w.sendParticles(net.minecraft.core.particles.ParticleTypes.SPLASH,t.getX(),t.getY()+1,t.getZ(),6,.2,.3,.2,.02);t.hurt(damageSources().mobAttack(this),GuardianRules.ECHO_DAMAGE);}}}}
```

## src/main/java/org/slavicmyths/guardians/WaterPrison.java

```java
10: import net.minecraft.world.damagesource.DamageSource;
16:  public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder attributes(){return createMobAttributes().add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH,3).add(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED,0);}
24:  @Override public boolean hurt(DamageSource s,float amount){if(s.getEntity() instanceof LivingEntity e&&org.slavicmyths.karachun.NightOwners.owner(e)!=null)return action(e);return false;}
```

## src/main/java/org/slavicmyths/hunt/BossEffects.java

```java
3: import net.minecraft.world.entity.ai.attributes.AttributeInstance;
6: import net.minecraft.world.entity.ai.attributes.*;
14: import net.minecraft.world.damagesource.DamageSource;
33:  public static void clearBelt(LivingEntity p){AttributeInstance a=p.getAttribute(Attributes.KNOCKBACK_RESISTANCE);if(a!=null)a.removeModifier(NAPOR);p.getPersistentData().remove("NaporTicks");p.getPersistentData().remove("NaporUntil");}
37:  @SubscribeEvent public static void melee(LivingIncomingDamageEvent e){if(e.getEntity().level().isClientSide)return;if(e.getSource().getEntity() instanceof Player&&e.getSource().getEntity()==e.getSource().getDirectEntity()){Player p=(Player)e.getSource().getEntity();if(napor(p)){e.getEntity().getPersistentData().putLong("NaporHitTick",p.level().getGameTime());e.getEntity().getPersistentData().putUUID("NaporAttacker",p.getUUID());}}}
38:  @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post e){if(e.getEntity().level().isClientSide)return;Player p=e.getEntity();net.minecraft.nbt.CompoundTag n=p.getPersistentData();boolean equipped=FolkEquipmentEffects.wears(p,ModItems.POYAS_TUGARINA.get());if(!p.isAlive()||!equipped||p.isSwimming()||p.isInWater()||p.isFallFlying()||p.getAbilities().flying){clearBelt(p);return;}long now=p.level().getGameTime();int ticks=p.isSprinting()?n.getInt("NaporTicks")+1:0;n.putInt("NaporTicks",Math.min(40,ticks));if(ticks>=40)n.putLong("NaporUntil",now+20);AttributeInstance a=p.getAttribute(Attributes.KNOCKBACK_RESISTANCE);boolean active=n.getLong("NaporUntil")>now;if(active&&a.getModifier(NAPOR)==null){a.addTransientModifier(new AttributeModifier(NAPOR,.2,AttributeModifier.Operation.ADD_VALUE));p.level().playSound(null,p.blockPosition(),SoundEvents.IRON_GOLEM_STEP,SoundSource.PLAYERS,.6F,.7F);((ServerLevel)p.level()).sendParticles(ParticleTypes.CLOUD,p.getX(),p.getY()+.1,p.getZ(),4,.2,.04,.2,.02);FolkAccessoryItem.award(p,"full_speed_ahead");}if(!active)a.removeModifier(NAPOR);}
40:   e.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);n.putBoolean("EyeProcGuard",true);try{p.addEffect(new MobEffectInstance(effect,Math.max(1,incoming.getDuration()/2),incoming.getAmplifier(),incoming.isAmbient(),incoming.isVisible(),incoming.showIcon()));}finally{n.remove("EyeProcGuard");}n.putLong("EyeProcReady",now+600);p.level().playSound(null,p.blockPosition(),SoundEvents.ENDER_EYE_DEATH,SoundSource.PLAYERS,.35F,.65F);((ServerLevel)p.level()).sendParticles(ParticleTypes.ENCHANT,p.getX(),p.getY()+1,p.getZ(),3,.2,.3,.2,.01);
```

## src/main/java/org/slavicmyths/hunt/BossKind.java

```java
2: public enum BossKind {
4:  public final String id;public final double hp,speed,armor,toughness,resistance,range;public final int xp;
5:  BossKind(String id,double hp,double speed,double armor,double tough,double resistance,double range,int xp){this.id=id;this.hp=hp;this.speed=speed;this.armor=armor;toughness=tough;this.resistance=resistance;this.range=range;this.xp=xp;}
```

## src/main/java/org/slavicmyths/hunt/BossRules.java

```java
4:  public static final long RESPAWN=7L*24000;
5:  public enum Move {
7:   public final int windup,recovery,cooldown;Move(int w,int r,int c){windup=w;recovery=r;cooldown=c;}
10:  public static int cooldown(BossKind k,Move m,int phase){if(m==Move.SPIN&&phase==3)return 150;double factor=k==BossKind.LIKHO&&phase==3?.85:k==BossKind.TUGARIN&&phase>=2?.9:1;return (int)Math.ceil(m.cooldown*factor);}
```

## src/main/java/org/slavicmyths/hunt/BossStone.java

```java
9: import net.minecraft.world.damagesource.DamageSource;
21:  @Override protected void onHitEntity(EntityHitResult hit){super.onHitEntity(hit);if(!level().isClientSide&&getOwner() instanceof LivingEntity&&hit.getEntity() instanceof LivingEntity){LivingEntity victim=(LivingEntity)hit.getEntity();victim.hurt(damageSources().mobProjectile(this,(LivingEntity)getOwner()),7);Vec3 d=victim.position().subtract(position()).normalize();victim.knockback(.5F,-d.x,-d.z);}}
```

## src/main/java/org/slavicmyths/hunt/ElementHuntMob.java

```java
5: import net.minecraft.world.entity.ai.attributes.Attribute;
8: import net.minecraft.world.entity.ai.attributes.*;
21: import net.minecraft.world.damagesource.DamageSource;
48:  public static AttributeSupplier.Builder attributes(boolean fire){AttributeSupplier.Builder a=createMobAttributes().add(Attributes.MAX_HEALTH,fire?155:160).add(Attributes.ATTACK_DAMAGE,7).add(Attributes.MOVEMENT_SPEED,fire?.24:.29).add(Attributes.ARMOR,fire?5:4).add(Attributes.KNOCKBACK_RESISTANCE,fire?.35:.75).add(Attributes.FOLLOW_RANGE,fire?40:38);return fire?a.add(Attributes.FLYING_SPEED,.34):a;}
52:  @Override public boolean causeFallDamage(float distance,float multiplier,net.minecraft.world.damagesource.DamageSource source){return false;}
56:  @Override protected SoundEvent getHurtSound(DamageSource d){return serpent()?SoundEvents.BLAZE_HURT:SoundEvents.PHANTOM_HURT;}
64:  private List<Player> players(double radius){return level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(radius),p->valid(p)&&distanceTo(p)<=radius&&getSensing().hasLineOfSight(p));}
65:  private void ring(Vec3 p,double radius,boolean fire,int count){ServerLevel w=(ServerLevel)level();for(int i=0;i<count;i++){double a=i*Math.PI*2/count;w.sendParticles(fire?ParticleTypes.FLAME:ParticleTypes.CLOUD,p.x+Math.cos(a)*radius,p.y+.15,p.z+Math.sin(a)*radius,1,0,0,0,.01);}}
66:  private float fireDamage(float d){return ElementRules.fireDamage(d,wetRain());}
67:  private void fireHit(Player p,float d,int burn,float push){if(p.hurt(org.slavicmyths.combat.MythDamageSources.caused("fire_serpent",this),fireDamage(d))){int duration=dampFire(p,ElementRules.ignite(burn,wetRain()));p.setRemainingFireTicks(Math.max(p.getRemainingFireTicks(),duration));}Vec3 v=p.position().subtract(position()).multiply(1,0,1).normalize();p.knockback(push,-v.x,-v.z);}
68:  private void begin(State s,LivingEntity t){state(s);ready.put(s,level().getGameTime()+(s==State.MELEE_SNAP&&!serpent()?20:ElementRules.cooldown(s,wetRain())));aim=t.position().add(t.getDeltaMovement().scale(8)).subtract(position()).normalize();destination=t.position();hit=false;dashHits.clear();travelled=0;alignWait=0;aligned=false;
76:   if(serpent()&&isInWater()){if(tickCount%20==0)hurt(damageSources().drown(),4);if(move()!=State.WATER_STUN){state(State.WATER_STUN);waterUntil=50;ready.put(State.ALIGN_DASH,level().getGameTime()+130);cue(SoundEvents.FIRE_EXTINGUISH);}}
77:   if(tickCount%5==0){long now=level().getGameTime();traces.removeIf(t->t.until<=now);hits.entrySet().removeIf(e->e.getValue()+40<now);for(Trace tr:traces){((ServerLevel)level()).sendParticles(ParticleTypes.FLAME,tr.p.x,tr.p.y,tr.p.z,1,.2,.1,.2,.01);for(Player p:level().getEntitiesOfClass(Player.class,new AABB(tr.p.x-.7,tr.p.y-.6,tr.p.z-.7,tr.p.x+.7,tr.p.y+.7,tr.p.z+.7),this::valid))if(now>=hits.getOrDefault(p.getUUID(),0L)&&clearLine(tr.p,p.getEyePosition(1))){p.hurt(org.slavicmyths.combat.MythDamageSources.caused("serpent_trail",this),fireDamage(2));hits.put(p.getUUID(),now+20);}}}
81:    if(d<1.5&&level().getGameTime()>=hits.getOrDefault(p.getUUID(),0L)){p.hurt(damageSources().mobAttack(this),6);p.setDeltaMovement(p.position().subtract(position()).multiply(1,0,1).normalize().scale(.65).add(0,.5,0));p.hurtMarked=true;hits.put(p.getUUID(),level().getGameTime()+30);}}
82:   if(ticks%2==0)for(Projectile e:level().getEntitiesOfClass(Projectile.class,getBoundingBox().inflate(5),e->e.getOwner()!=this&&(e instanceof AbstractArrow||e instanceof Snowball||e instanceof ThrownEgg)&&distanceTo(e)<5))if(!deflected.contains(e.getUUID())&&clearLine(position().add(0,1,0),e.position())){Vec3 radial=e.position().subtract(position()).multiply(1,0,1).normalize(),v=e.getDeltaMovement();if(v.dot(radial)>=0)continue;Vec3 side=new Vec3(-radial.z,v.y*.25,radial.x).normalize().scale(Math.max(.35,v.length()*.85));e.setDeltaMovement(side);e.hasImpulse=true;deflected.add(e.getUUID());}
98:    if(s==State.FIRE_DASH){fly(position().add(aim.scale(2)),.34*1.8);if(ticks%3==0&&traces.size()<14)traces.add(new Trace(position(),level().getGameTime()+40));for(Player p:players(2.2))if(!dashHits.contains(p.getUUID())&&getBoundingBox().inflate(.4).intersects(p.getBoundingBox())){fireHit(p,8,60,.7F);dashHits.add(p.getUUID());}if(ticks>=18||horizontalCollision)recover(18);return;}
102:    if(s==State.MELEE_SNAP){navigation.stop();if(ticks==(serpent()?7:8)&&d<=(serpent()?2.4:2.5)&&sight)t.hurt(damageSources().mobAttack(ElementHuntMob.this),7);if(ticks>=12)recover(6);return;}
105:    if(s==State.GUST_CHARGE){navigation.stop();if(ticks%4==0)particles(ParticleTypes.CLOUD,3);if(ticks>=14){state(State.GUST_RELEASE);cue(SoundEvents.PLAYER_ATTACK_SWEEP);for(Player p:players(8)){Vec3 v=p.position().subtract(position()).multiply(1,0,1).normalize();if(aim.multiply(1,0,1).normalize().dot(v)<.843)continue;boolean shield=p.isBlocking()&&p.getLookAngle().dot(position().subtract(p.position()).normalize())>.4;p.hurt(damageSources().mobAttack(ElementHuntMob.this),4);p.setDeltaMovement(p.getDeltaMovement().add(v.scale(shield?1.25*.55:1.25)).add(0,.15,0));p.hurtMarked=true;}for(int i=1;i<=8;i++)ring(position().add(aim.scale(i)),.35,false,3);}return;}
109:     if(!hit&&d<2.1&&sight){t.hurt(damageSources().mobAttack(ElementHuntMob.this),6);t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,60));t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40));t.knockback(.3F,-aim.z,aim.x);hit=true;}if(travelled>=7||ticks>=14)recover(16);return;}
120:  @Override public void die(DamageSource d){
129:  @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);CompoundTag cds=n.getCompound("ElementReady");for(State s:State.values())ready.put(s,cds.getLong(s.name()));copiesUntil=n.getLong("CopiesUntil");state(serpent()?State.ORBIT:State.HUNT);getAttribute(Attributes.MAX_HEALTH).setBaseValue(serpent()?155:160);getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(7);getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(serpent()?.24:.29);getAttribute(Attributes.ARMOR).setBaseValue(serpent()?5:4);getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(0);getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(serpent()?.35:.75);getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(serpent()?40:38);if(morClone()){getAttribute(Attributes.MAX_HEALTH).setBaseValue(30);setHealth(Math.min(getHealth(),30));}}
```

## src/main/java/org/slavicmyths/hunt/ElementRules.java

```java
4:  public enum State {APPROACH,ORBIT,ALIGN_DASH,FIRE_DASH,ALIGN_DIVE,STAR_DIVE,DECOY_CAST,MELEE_SNAP,WATER_STUN,RECOVERY,RETURN_TO_ANCHOR,HUNT,STRAFE,VORTEX_CHARGE,VORTEX_ACTIVE,GUST_CHARGE,GUST_RELEASE,BROKEN_PATH_CHARGE,BROKEN_PATH_DASH}
5:  public static int cooldown(State s,boolean rain){switch(s){case ALIGN_DASH:return rain?120:100;case ALIGN_DIVE:return 150;case DECOY_CAST:return 220;case MELEE_SNAP:return 22;case VORTEX_CHARGE:return 160;case GUST_CHARGE:return 110;case BROKEN_PATH_CHARGE:return 120;default:return 20;}}
7:  public static float fireDamage(float base,boolean rain){return base*(rain?.8F:1);}
8:  public static int ignite(int base,boolean rain){return rain?base*3/4:base;}
11:  public static double pushScale(double hp,boolean hunt){return hp>=250?.15:hunt||hp>=100?.35:1;}
```

## src/main/java/org/slavicmyths/hunt/EmberClump.java

```java
9: import net.minecraft.world.damagesource.DamageSource;
21:  @Override protected void onHitEntity(EntityHitResult hit){super.onHitEntity(hit);if(!level().isClientSide&&getOwner() instanceof HuntMob&&hit.getEntity() instanceof net.minecraft.world.entity.player.Player)hit.getEntity().hurt(org.slavicmyths.combat.MythDamageSources.ember(this,(HuntMob)getOwner()),damage((HuntMob)getOwner()));}
22:  @Override protected void onHit(HitResult hit){super.onHit(hit);if(level().isClientSide)return;Entity owner=getOwner();if(owner instanceof HuntMob m&&org.slavicmyths.mor.HigherMor.higher(m)){((net.minecraft.server.level.ServerLevel)level()).sendParticles(org.slavicmyths.mor.MorContent.SPARK.get(),getX(),getY(),getZ(),18,.6,.4,.6,.04);((net.minecraft.server.level.ServerLevel)level()).sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,getX(),getY(),getZ(),10,.5,.3,.5,.03);playSound(SoundEvents.GENERIC_EXPLODE.value(),.45F,.7F);}if(owner instanceof HuntMob)for(net.minecraft.world.entity.player.Player p:level().getEntitiesOfClass(net.minecraft.world.entity.player.Player.class,getBoundingBox().inflate(1.5)))if(!(hit instanceof EntityHitResult&&((EntityHitResult)hit).getEntity()==p)&&p.distanceTo(this)<=1.5&&((HuntMob)owner).clearLine(position(),p.position().add(0,1,0)))p.hurt(org.slavicmyths.combat.MythDamageSources.ember(this,(HuntMob)owner),damage((HuntMob)owner));discard();}
23:  private float damage(HuntMob owner){return owner instanceof ElementHuntMob?ElementRules.fireDamage(4,owner.wetRain()):owner.wetRain()?4.25F:5;}
```

## src/main/java/org/slavicmyths/hunt/HuntEffects.java

```java
2: import net.minecraft.world.entity.ai.attributes.AttributeInstance;
4: import net.minecraft.world.entity.ai.attributes.*;
11: import net.minecraft.world.damagesource.DamageSource;
22:  public static void removeBelt(net.minecraft.world.entity.LivingEntity e){AttributeInstance a=e.getAttribute(Attributes.MOVEMENT_SPEED);if(a!=null)a.removeModifier(BELT);}
24:   int fire=p.getRemainingFireTicks(),previous=p.getPersistentData().getInt("HuntLastFire");if(fire>previous+1&&FolkEquipmentEffects.wears(p,ModItems.ZOLNY_OBEREG.get())){fire=(int)Math.ceil(fire*.6);p.setRemainingFireTicks(fire);}p.getPersistentData().putInt("HuntLastFire",fire);
25:   if(p.tickCount%20==0){AttributeInstance a=p.getAttribute(Attributes.MOVEMENT_SPEED);double value=FolkEquipmentEffects.wears(p,ModItems.VOLCHIY_POYAS.get())?HuntRules.belt(!p.level().isDay(),p.level().getMoonPhase()==0):0;AttributeModifier old=a.getModifier(BELT);if(old!=null&&(value==0||Math.abs(old.amount()-value)>.00001))a.removeModifier(BELT);if(value>0&&a.getModifier(BELT)==null)a.addTransientModifier(new AttributeModifier(BELT,value,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));}
27:  @SubscribeEvent public static void fire(LivingIncomingDamageEvent event){if(event.getEntity().level().isClientSide||!event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FIRE)||!FolkEquipmentEffects.wears(event.getEntity(),ModItems.ZOLNY_OBEREG.get()))return;event.setAmount(event.getAmount()*.8F);net.minecraft.world.entity.LivingEntity e=event.getEntity();long now=e.level().getGameTime();if(now>=e.getPersistentData().getLong("HuntAshFeedback")){e.getPersistentData().putLong("HuntAshFeedback",now+20);((ServerLevel)e.level()).sendParticles(ParticleTypes.ASH,e.getX(),e.getY()+.5,e.getZ(),2,.15,.2,.15,.01);e.playSound(SoundEvents.FIRE_AMBIENT,.15F,.6F);}}
28:  @SubscribeEvent(priority=EventPriority.LOWEST) public static void silver(LivingDamageEvent.Pre event){if(!event.getEntity().level().isClientSide&&event.getEntity() instanceof org.slavicmyths.entity.VolkolakEntity&&org.slavicmyths.item.SilverCombat.silverStrike(event.getSource()))event.setNewDamage(event.getNewDamage()*1.25F);}
```

## src/main/java/org/slavicmyths/hunt/HuntMob.java

```java
6: import net.minecraft.world.entity.ai.attributes.Attribute;
10: import net.minecraft.world.entity.ai.attributes.*;
22: import net.minecraft.world.damagesource.DamageSource;
52:  public static AttributeSupplier.Builder attributes(boolean o){return createMobAttributes().add(Attributes.MAX_HEALTH,o?175:165).add(Attributes.ATTACK_DAMAGE,o?10:9).add(Attributes.MOVEMENT_SPEED,o?.245:.335).add(Attributes.ARMOR,o?10:7).add(Attributes.ARMOR_TOUGHNESS,o?2:0).add(Attributes.KNOCKBACK_RESISTANCE,o?.55:.25).add(Attributes.FOLLOW_RANGE,o?32:36);}
63:  @Override public boolean fireImmune(){return oven()||super.fireImmune();}
64:  @Override public void checkDespawn(){if(!level().isClientSide&&level().getDifficulty()==Difficulty.PEACEFUL&&owner!=null)HuntRecords.get((ServerLevel)level()).finish(owner,getUUID(),HuntRecords.Status.FAILED);super.checkDespawn();}
68:  @Override protected SoundEvent getHurtSound(DamageSource source){return oven()?super.getHurtSound(source):SoundEvents.WOLF_HURT;}
71:  @Override public boolean hurt(DamageSource source,float amount){if(oven()&&source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE))return false;return super.hurt(source,amount);}
77:   if(oven()&&isInWater()&&tickCount%20==0)hurt(damageSources().drown(),2);
79:    for(Trail t:trail){((ServerLevel)level()).sendParticles(ParticleTypes.SMOKE,t.pos.x,t.pos.y+.1,t.pos.z,1,.1,0,.1,.01);for(Player p:level().getEntitiesOfClass(Player.class,new AABB(t.pos.x-.6,t.pos.y-.2,t.pos.z-.6,t.pos.x+.6,t.pos.y+.5,t.pos.z+.6)))if(!p.isCreative()&&!p.isSpectator()&&level().getGameTime()>=trailHits.getOrDefault(p.getUUID(),0L)&&clearLine(t.pos.add(0,.2,0),p.position().add(0,.2,0))){p.hurt(org.slavicmyths.combat.MythDamageSources.caused("ovinnik_trail",this),wetRain()?1.7F:2);trailHits.put(p.getUUID(),level().getGameTime()+20);}}
83:  private void begin(Move move,LivingEntity target){entityData.set(ACTION,move.ordinal());entityData.set(START,(int)level().getGameTime());actionTick=0;alignWait=0;dashHit=false;navigation.stop();aim=target.position().subtract(position()).multiply(1,0,1).normalize();timers.put(move,HuntRules.cooldown(move.cooldown,rage()&&(oven()||move==Move.POUNCE)));if(move==Move.DODGE){Vec3 side=new Vec3(-aim.z,0,aim.x).scale(random.nextBoolean()?2.5:-2.5);sidePoint=position().add(side);}
87:  private boolean strike(LivingEntity target,float damage,double reach,double cone,float knockback){if(!target.isAlive()||distanceTo(target)>reach||!getSensing().hasLineOfSight(target)||aim.dot(target.position().subtract(position()).multiply(1,0,1).normalize())<cone)return false;
88:   // Six-tick combo spacing needs its own follow-up hits to pass vanilla's ten-tick damage window.
89:   if(action()==Move.COMBO&&actionTick>Move.COMBO.windup&&target.getLastHurtByMob()==this)target.invulnerableTime=Math.min(target.invulnerableTime,10);
90:   boolean hit=target.hurt(damageSources().mobAttack(this),damage);if(knockback>0){Vec3 d=target.position().subtract(position()).normalize();target.knockback(knockback,-d.x,-d.z);}return hit;}
96:   case MELEE:case CLAW:strike(target,oven()?10:9,oven()?2.7:2.4,-.1,0);basicWait=(oven()?(rage()?18:22):(rage()?16:18))-m.windup-1;break;
97:   case SLAM:particles(ParticleTypes.SMOKE,18);cue(SoundEvents.GENERIC_EXPLODE.value());for(Player p:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(4)))if(distanceTo(p)<=4&&getSensing().hasLineOfSight(p)){boolean shield=p.isBlocking()&&p.getLookAngle().dot(position().subtract(p.position()).normalize())>.4;if(!shield)p.hurt(damageSources().mobAttack(this),8);else p.hurt(org.slavicmyths.combat.MythDamageSources.slam(this),3);Vec3 d=p.position().subtract(position()).normalize();p.knockback(shield?.35F:.7F,-d.x,-d.z);}break;
98:   case ASH:particles(ParticleTypes.SMOKE,18);for(Player p:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(6)))if(distanceTo(p)<=6&&getSensing().hasLineOfSight(p)&&aim.dot(p.position().subtract(position()).multiply(1,0,1).normalize())>=.819){p.hurt(org.slavicmyths.combat.MythDamageSources.unattributed("ovinnik_ash",this),4);p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,80));}break;
101:   case COMBO:strike(target,4,2.7,-.1,0);break;
102:   case HOWL:for(Player p:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(12)))if(distanceTo(p)<=12&&getSensing().hasLineOfSight(p)&&!p.isCreative()&&!p.isSpectator()){p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,80));prey.put(p.getUUID(),level().getGameTime()+120);}break;
114:    getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(HuntRules.nightSpeed(oven(),rage(),!level().isDay(),level().getMoonPhase()==0,prey.containsKey(t.getUUID())));
116:     if(m==Move.POUNCE&&actionTick>=m.windup&&actionTick<m.windup+10&&!dashHit&&distanceTo(t)<2.6&&getSensing().hasLineOfSight(t)){strike(t,8,2.6,.2,.5F);dashHit=true;}
118:     if(m==Move.COMBO&&(actionTick==m.windup+6||actionTick==m.windup+12))strike(t,actionTick==m.windup+12?7:4,2.7,-.1,actionTick==m.windup+12?.7F:0);
127:  @Override public void die(DamageSource source){boolean before=dead;super.die(source);if(!level().isClientSide){bar.setVisible(false);bar.removeAllPlayers();}if(!before&&dead&&!level().isClientSide){cue(oven()?SoundEvents.WOOD_BREAK:SoundEvents.GENERIC_BIG_FALL);if(oven())cue(SoundEvents.FIRE_EXTINGUISH);if(owner!=null)HuntRecords.get((ServerLevel)level()).finish(owner,getUUID(),HuntRecords.Status.CLEARED);if(miniBoss()&&!(this instanceof ElementHuntMob)&&source.getEntity() instanceof Player&&(oven()||org.slavicmyths.item.SilverCombat.silverStrike(source)))org.slavicmyths.item.FolkAccessoryItem.award((LivingEntity)source.getEntity(),oven()?"extinguish_the_barn":"silver_remedy");}}
129:  @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);owner=n.hasUUID("HuntOwner")?n.getUUID("HuntOwner"):null;entityData.set(RAGE,n.getBoolean("HuntRage")||n.getBoolean("Enraged"));CompoundTag cd=n.getCompound("HuntTimers");for(Move m:Move.values())timers.put(m,Math.max(0,Math.min(m.cooldown,cd.getInt(m.name()))));entityData.set(ACTION,0);
131:   // Saved 0.8.x attributes must not silently retain the old 140-HP Ovinnik profile.
132:   getAttribute(Attributes.MAX_HEALTH).setBaseValue(oven()?175:165);getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(oven()?10:9);getAttribute(Attributes.ARMOR).setBaseValue(oven()?10:7);getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(oven()?2:0);getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(oven()?.55:.25);getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(oven()?32:36);getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(HuntRules.nightSpeed(oven(),rage(),!level().isDay(),level().getMoonPhase()==0,false));
```

## src/main/java/org/slavicmyths/hunt/HuntRules.java

```java
6:  public static int cooldown(int base,boolean rage){return rage?(int)Math.ceil(base*.85):base;}
7:  public enum Move{
10:   public final int windup,recovery,cooldown;Move(int w,int r,int c){windup=w;recovery=r;cooldown=c;}
```

## src/main/java/org/slavicmyths/hunt/SerpentProjection.java

```java
2: import net.minecraft.world.entity.ai.attributes.Attribute;
5: import net.minecraft.world.entity.ai.attributes.*;
8: import net.minecraft.world.damagesource.DamageSource;
16:  public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,1).add(Attributes.MOVEMENT_SPEED,0);}
18:  @Override public boolean causeFallDamage(float d,float m,net.minecraft.world.damagesource.DamageSource source){return false;}
20:  @Override public boolean hurt(DamageSource d,float amount){if(level().isClientSide)return false;if(d.getEntity()!=null&&d.getEntity()!=this){burst();discard();return true;}return false;}
```

## src/main/java/org/slavicmyths/hunt/WorldBoss.java

```java
5: import net.minecraft.world.entity.ai.attributes.Attribute;
9: import net.minecraft.world.entity.ai.attributes.*;
21: import net.minecraft.world.damagesource.DamageSource;
40:  public enum TugarinState {PHASE_ONE,TRANSFORMING,PHASE_TWO}
42:  void tugarinState(TugarinState state){entityData.set(TUGARIN_STATE,state.ordinal());entityData.set(PHASE,state==TugarinState.PHASE_TWO?2:1);if(!level().isClientSide){if(state==TugarinState.PHASE_TWO)addEffect(new MobEffectInstance(org.slavicmyths.registry.ModEffects.SERPENT_GUARD,-1,0,false,true,true));else removeEffect(org.slavicmyths.registry.ModEffects.SERPENT_GUARD);}}
46:  private final EnumMap<Move,Integer> cooldowns=new EnumMap<>(Move.class);
58:  public static AttributeSupplier.Builder attributes(BossKind k){return createMobAttributes().add(Attributes.MAX_HEALTH,k.hp).add(Attributes.ATTACK_DAMAGE,k==BossKind.LIKHO?11:12).add(Attributes.MOVEMENT_SPEED,k.speed).add(Attributes.ARMOR,k.armor).add(Attributes.ARMOR_TOUGHNESS,k.toughness).add(Attributes.KNOCKBACK_RESISTANCE,k.resistance).add(Attributes.FOLLOW_RANGE,k.range);}
64:   if(tickCount%20==0){bossBar.setName(kind()==BossKind.TUGARIN&&tugarinState()!=TugarinState.PHASE_ONE?Component.translatable(tugarinState()==TugarinState.TRANSFORMING?"boss.slavicmyths.tugarin.transforming":"boss.slavicmyths.tugarin.true_form"):getDisplayName());bossBar.setColor(kind()==BossKind.LIKHO?BossEvent.BossBarColor.PURPLE:BossEvent.BossBarColor.RED);List<ServerPlayer> nearby=level().getEntitiesOfClass(ServerPlayer.class,getBoundingBox().inflate(48),p->p.isAlive()&&!p.isSpectator()&&distanceToSqr(p)<=48*48);for(ServerPlayer p:new ArrayList<>(bossBar.getPlayers()))if(!nearby.contains(p))bossBar.removePlayer(p);for(ServerPlayer p:nearby)bossBar.addPlayer(p);}
68:  @Override public void checkDespawn(){if(!level().isClientSide&&level().getDifficulty()==Difficulty.PEACEFUL){if(!encounter.isEmpty()){HuntRecords d=HuntRecords.get((ServerLevel)level());HuntRecords.Boss b=d.bosses.get(encounter);if(b!=null&&getUUID().equals(b.target)){b.active=false;b.ready=level().getGameTime()+1200;d.setDirty();}}discard();}}
69:  private boolean ready(Move m){return cooldowns.getOrDefault(m,0)==0;}
70:  private void begin(Move m,LivingEntity target){entityData.set(MOVE,m.ordinal());entityData.set(START,(int)level().getGameTime());time=0;align=0;hits.clear();zones.clear();navigation.stop();heading=target.position().subtract(position()).multiply(1,0,1).normalize();pivot=position();cooldowns.put(m,BossRules.cooldown(kind(),m,phase()));
75:  private boolean strike(LivingEntity p,float damage,double radius,double cone,float kb){if(!p.isAlive()||p instanceof Player&&(((Player)p).isCreative()||p.isSpectator())||distanceTo(p)>radius||!getSensing().hasLineOfSight(p)||heading.dot(p.position().subtract(position()).multiply(1,0,1).normalize())<cone)return false;boolean shield=p.isBlocking()&&p.getLookAngle().dot(position().subtract(p.position()).normalize())>.35;boolean hit=p.hurt(damageSources().mobAttack(this),damage);Vec3 d=p.position().subtract(position()).multiply(1,0,1).normalize();if(kb>0)p.knockback(shield?kb*.35F:kb,-d.x,-d.z);return hit;}
76:  private List<Player> players(double radius){return level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(radius),p->p.isAlive()&&!p.isCreative()&&!p.isSpectator());}
77:  private void area(float damage,double r,double cone,float kb){for(Player p:players(r))strike(p,damage,r,cone,kb);}
78:  private void fate(LivingEntity p){p.addEffect(new MobEffectInstance(BossEffects.ILL_FATE,160));}
80:  private void releaseHeld(boolean toss){LivingEntity p=heldEntity();held=null;holdTicks=0;if(p==null)return;p.fallDistance=0;p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,60,0,false,false));if(toss&&p.isAlive()){p.hurt(damageSources().mobAttack(this),6);p.setDeltaMovement(heading.scale(.72).add(0,.35,0));p.hurtMarked=true;}}
81:  private boolean spinSafe(){return onGround()&&!isInWater()&&level().noCollision(getBoundingBox().inflate(.7,0,.7).expandTowards(0,.4,0));}
85:   case PUNCH:strike(t,12,2.8,.5,.8F);break;
86:   case GRAB:if(strike(t,4,3.5,Math.cos(Math.toRadians(30)),0)){held=t.getUUID();holdTicks=0;fate(t);entityData.set(MOVE,Move.HOLD.ordinal());entityData.set(START,(int)level().getGameTime());time=0;}break;
87:   case GAZE:for(Player p:players(10))if(distanceTo(p)<=10&&getSensing().hasLineOfSight(p)&&heading.dot(p.position().subtract(position()).multiply(1,0,1).normalize())>=Math.cos(Math.toRadians(25))&&p.getLookAngle().dot(getEyePosition(1).subtract(p.getEyePosition(1)).normalize())>=.75){p.hurt(damageSources().mobAttack(this),2);fate(p);p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,80));}break;
88:   case GROUND:for(Vec3 z:zones)if(clearLine(getEyePosition(1),z.add(0,.3,0)))for(Player p:players(14))if(p.position().distanceToSqr(z)<=1.75*1.75&&clearLine(z.add(0,.3,0),p.position().add(0,.3,0))&&hits.add(p.getUUID())){p.hurt(damageSources().mobAttack(this),6);Vec3 d=p.position().subtract(z).normalize();p.knockback(.6F,-d.x,-d.z);p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,50));}break;
99:   public void tick(){if(kind()==BossKind.TUGARIN){tugarin().tick();return;}for(Move m:Move.values())cooldowns.computeIfPresent(m,(k,v)->Math.max(0,v-1));int next=BossRules.phase(kind(),getHealth()/getMaxHealth());if(next>phase()){entityData.set(PHASE,next);recover(18);entityData.set(MOVE,Move.TRANSITION.ordinal());playSound(SoundEvents.RAVAGER_ROAR,1,.55F);}
100:    getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(kind().speed*(kind()==BossKind.LIKHO?(phase()==3?1.12:phase()==2?1.07:1):(phase()>=2?1.06:1))*(move()==Move.CHASE?1.25:move()==Move.RECOVERY&&wait>=30?.7:1));
112:    if(m==Move.CHARGE&&time>12){setDeltaMovement(heading.scale(1.7).add(0,getDeltaMovement().y,0));hasImpulse=true;for(Player p:players(2.4))if(!hits.contains(p.getUUID())&&strike(p,10,2.4,.2,1.2F))hits.add(p.getUUID());if(horizontalCollision){dust();recover(30);return;}if(time>=34){recover(20);return;}}
114:    if(m==Move.SPIN){setDeltaMovement(0,getDeltaMovement().y,0);if(time>=22&&time<28)for(Player p:players(phase()==3?4.5:4))if(!hits.contains(p.getUUID())&&strike(p,phase()==3?10:9,phase()==3?4.5:4,-1,phase()==3?1.9F:1.7F)){hits.add(p.getUUID());if(!p.isBlocking())p.setDeltaMovement(p.getDeltaMovement().add(0,.25,0));}if(time>=36){recover(phase()==3?24:28);return;}return;}
115:    if(m==Move.BREATH&&time>22&&time<=46){if(time%2==0)((ServerLevel)level()).sendParticles(ParticleTypes.FLAME,getX()+heading.x*2,getEyeY()-.3,getZ()+heading.z*2,7,1,.35,1,.04);if(time==24||time==34||time==44)for(Player p:players(7))if(strike(p,4,7,Math.cos(Math.toRadians(30)),0))p.igniteForSeconds(3);if(time==46)recover(18);return;}
119:  @Override public void die(DamageSource source){boolean was=dead;super.die(source);if(!was&&dead&&!level().isClientSide){releaseHeld(false);bossBar.removeAllPlayers();if(!encounter.isEmpty())HuntRecords.get((ServerLevel)level()).finishBoss(encounter,getUUID(),level().getGameTime());if(source.getEntity() instanceof Player)org.slavicmyths.item.FolkAccessoryItem.award((Player)source.getEntity(),kind()==BossKind.LIKHO?"name_your_misfortune":"steppe_champion");}}
120:  @Override public boolean hurt(DamageSource source,float amount){if(kind()!=BossKind.TUGARIN)return super.hurt(source,amount);if(tugarinState()==TugarinState.TRANSFORMING)amount*=.15F;else if(tugarinState()==TugarinState.PHASE_TWO)amount*=.5F;boolean hit=super.hurt(source,amount);if(hit&&source.getEntity() instanceof LivingEntity attacker)tugarin().threat(attacker,amount);if(hit&&isAlive())tugarin().startTransformationIfNeeded();return hit;}
122:  @Override protected SoundEvent getAmbientSound(){return kind()==BossKind.LIKHO?(morState()!=0?null:SoundEvents.WITCH_AMBIENT):tugarinState()==TugarinState.PHASE_ONE?org.slavicmyths.registry.ModSounds.boss("tugarin_phase1_idle"):org.slavicmyths.registry.ModSounds.boss("tugarin_phase2_idle");}@Override protected SoundEvent getHurtSound(DamageSource s){return kind()==BossKind.LIKHO?(morState()!=0?org.slavicmyths.mor.MorAudio.hurt(5):SoundEvents.WITCH_HURT):tugarinState()==TugarinState.PHASE_ONE?org.slavicmyths.registry.ModSounds.boss("tugarin_phase1_hurt"):org.slavicmyths.registry.ModSounds.boss("tugarin_phase2_hurt");}@Override protected SoundEvent getDeathSound(){return kind()==BossKind.LIKHO?(morState()!=0?org.slavicmyths.mor.MorAudio.death(5):SoundEvents.WITCH_DEATH):tugarinState()==TugarinState.PHASE_ONE?org.slavicmyths.registry.ModSounds.boss("tugarin_phase1_death"):org.slavicmyths.registry.ModSounds.boss("tugarin_phase2_death");}@Override protected void playStepSound(BlockPos p,net.minecraft.world.level.block.state.BlockState s){playSound(kind()==BossKind.LIKHO?SoundEvents.BONE_BLOCK_STEP:org.slavicmyths.registry.ModSounds.boss("tugarin.phase1.step"),.5F,.6F);}
123:  @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(kind()==BossKind.TUGARIN)tugarin().save(n);n.putString("BossEncounter",encounter);if(bossOwner!=null)n.putUUID("BossOwner",bossOwner);n.putInt("BossPhase",phase());CompoundTag cd=new CompoundTag();cooldowns.forEach((m,v)->cd.putInt(m.name(),v));n.put("BossCooldowns",cd);}
124:  @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);owner=null;encounter=n.getString("BossEncounter");bossOwner=n.hasUUID("BossOwner")?n.getUUID("BossOwner"):null;entityData.set(PHASE,Math.max(1,Math.min(3,n.getInt("BossPhase"))));entityData.set(MOVE,Move.RECOVERY.ordinal());wait=20;CompoundTag cd=n.getCompound("BossCooldowns");for(Move m:Move.values())cooldowns.put(m,Math.max(0,Math.min(m.cooldown,cd.getInt(m.name()))));BossKind k=kind();getAttribute(Attributes.MAX_HEALTH).setBaseValue(k.hp);getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(k==BossKind.LIKHO?11:12);getAttribute(Attributes.ARMOR).setBaseValue(k.armor);getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(k.toughness);getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(k.resistance);getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(k.range);getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(k.speed);if(k==BossKind.TUGARIN)tugarin().load(n);}
```

## src/main/java/org/slavicmyths/husbandry/Husbandry.java

```java
7: import net.minecraft.world.entity.ai.attributes.Attributes;
45:         item("goose_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(GOOSE,0xd8d7cd,0xe99031,new Item.Properties()));
46:         item("duck_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(DUCK,0x735038,0xe58b2d,new Item.Properties()));
47:         item("domestic_goat_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(GOAT,0xe9ddc5,0x8c8070,new Item.Properties()));
51:         var types=java.util.List.of(GOOSE,DUCK,GOAT);double[] hp={12,10,18},speed={.22,.20,.23};
52:         for(int i=0;i<3;i++)e.put(types.get(i).get(),Animal.createMobAttributes().add(Attributes.MAX_HEALTH,hp[i]).add(Attributes.MOVEMENT_SPEED,speed[i]).add(Attributes.FOLLOW_RANGE,16).add(Attributes.ATTACK_DAMAGE,i==0?1:0).add(Attributes.ATTACK_KNOCKBACK,0).build());
54:     private static void placements(net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent e){
55:         for(var type:java.util.List.of(GOOSE,DUCK,GOAT))e.register(type.get(),SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
56:             (t,l,r,p,random)->l.getLevel().dimension()==net.minecraft.world.level.Level.OVERWORLD && l.getBlockState(p.below()).is(net.minecraft.tags.BlockTags.DIRT) && l.getMaxLocalRawBrightness(p)>8 && Mob.checkMobSpawnRules(t,l,r,p,random),net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
```

## src/main/java/org/slavicmyths/husbandry/YardAnimal.java

```java
9: import net.minecraft.world.damagesource.DamageSource;
22:     private int ambientCooldown;
27:         super(type,level);this.kind=kind;eggTimer=newEggTimer();ambientCooldown=newAmbientCooldown();
55:         if(!level().isClientSide&&--ambientCooldown<=0){
56:             ambientCooldown=newAmbientCooldown();
57:             boolean quiet=kind==0&&level().getEntitiesOfClass(YardAnimal.class,getBoundingBox().inflate(10),a->a!=this&&a.kind==0&&distanceToSqr(a)<=100&&level().getGameTime()-a.lastAmbient<160).size()>0;
62:             for(YardAnimal baby:level().getEntitiesOfClass(YardAnimal.class,getBoundingBox().inflate(6),a->a.kind==0&&a.isBaby()&&distanceToSqr(a)<=36)){
78:     @Override public boolean hurt(DamageSource source,float damage){
79:         boolean result=super.hurt(source,damage);
82:                 for(YardAnimal adult:level().getEntitiesOfClass(YardAnimal.class,getBoundingBox().inflate(6),a->a.kind==0&&!a.isBaby()&&distanceToSqr(a)<=36))adult.warn(p);
89:     private int newAmbientCooldown(){return kind==0?1200+random.nextInt(1201):kind==1?600+random.nextInt(601):800+random.nextInt(801);}
92:     @Override protected SoundEvent getHurtSound(DamageSource d){return (kind==0?Husbandry.GOOSE_HURT:kind==1?Husbandry.DUCK_HURT:Husbandry.GOAT_HURT).get();}
```

## src/main/java/org/slavicmyths/industry/ChudPeople.java

```java
14:     private static void egg(String name,DeferredHolder<EntityType<?>,EntityType<ChudPerson>> type,int color){ModItems.ITEMS.register(name+"_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(type,color,0xb2a188,new Item.Properties()));}
```

## src/main/java/org/slavicmyths/industry/ChudPerson.java

```java
10: import net.minecraft.world.entity.ai.attributes.*;
31:     public ChudPerson(EntityType<? extends ChudPerson> type,Level level){super(type,level);setPersistenceRequired();getAttribute(Attributes.STEP_HEIGHT).setBaseValue(1);setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.WATER,-1);setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.LAVA,-1);setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.DAMAGE_FIRE,-1);if(type==ChudPeople.GUARD.get())entityData.set(ROLE,3);else if(type==ChudPeople.ARTISAN.get())entityData.set(ROLE,1);}
32:     public static AttributeSupplier.Builder attributes(double hp){return createMobAttributes().add(Attributes.MAX_HEALTH,hp).add(Attributes.MOVEMENT_SPEED,hp==44?.29:hp==20?.26:.24).add(Attributes.FOLLOW_RANGE,hp==44?16:20).add(Attributes.ATTACK_DAMAGE,5).add(Attributes.ARMOR,2);}
41:         goalSelector.addGoal(2,new MeleeAttackGoal(this,1.1,true){public boolean canUse(){return role()==3&&super.canUse();}protected void checkAndPerformAttack(LivingEntity t){if(canPerformAttack(t)&&guardVictim==null){resetAttackCooldown();guardVictim=t;guardHitAt=level().getGameTime()+7;activity(3);getNavigation().stop();}}});
52:         if(role()==3||!isAlive()||isBaby()||p.getItemInHand(hand).getItem() instanceof SpawnEggItem)return super.mobInteract(p,hand);
71:             if(random.nextBoolean())offer(ModItems.SILVER_INGOT.get(),3,null,0,Industry.FLUX.get(),2,8);
82:         if(role()==3&&guardVictim!=null&&now>=guardHitAt){if(guardVictim.isAlive()&&distanceToSqr(guardVictim)<6.25&&hasLineOfSight(guardVictim)){swing(InteractionHand.MAIN_HAND);guardVictim.hurt(damageSources().mobAttack(this),5.5F);}guardVictim=null;}
94:     public boolean hurt(net.minecraft.world.damagesource.DamageSource source,float amount){boolean hit=super.hurt(source,amount);if(hit&&!level().isClientSide&&source.getEntity() instanceof LivingEntity enemy){for(var n:level().getEntitiesOfClass(ChudPerson.class,getBoundingBox().inflate(24))){if(enemy instanceof Player p){var aggression=n.getPersistentData().getCompound("ChudAggressors");long now=level().getGameTime();for(String key:new ArrayList<>(aggression.getAllKeys()))if(aggression.getLong(key)<=now)aggression.remove(key);aggression.putLong(p.getUUID().toString(),now+1200);n.getPersistentData().put("ChudAggressors",aggression);n.getPersistentData().putLong("ChudAggressorUntil",now+1200);}if(n.role()==3&&n.hasLineOfSight(enemy)&&!(enemy instanceof ChudPerson))n.setTarget(enemy);}}return hit;}
98:     public void die(net.minecraft.world.damagesource.DamageSource source){if(!level().isClientSide&&getTradingPlayer()!=null){getTradingPlayer().closeContainer();setTradingPlayer(null);}super.die(source);}
```

## src/main/java/org/slavicmyths/industry/IndustryAccessories.java

```java
8: import net.minecraft.world.entity.ai.attributes.*;
35:     @SubscribeEvent public static void fall(LivingDamageEvent.Pre e){if(!e.getEntity().level().isClientSide&&e.getSource().is(net.minecraft.world.damagesource.DamageTypes.FALL)&&wearing(e.getEntity(),AMULET.get()))e.setNewDamage(e.getNewDamage()*.75F);}
55:             if(current==null||!current.isInfiniteDuration()&&current.getDuration()<500){OUR_EFFECT.set(true);try{e.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,520,0,true,false,false));}finally{OUR_EFFECT.set(false);}}
56:         }else if(active){var restore=external(e);d.remove("IndustryGogglesNV");d.remove("IndustryExternalNV");d.remove("IndustryNVAt");OUR_EFFECT.set(true);try{e.removeEffect(MobEffects.NIGHT_VISION);if(restore!=null)e.addEffect(restore);}finally{OUR_EFFECT.set(false);}}
65:         private void refresh(LivingEntity e,boolean equipped){if(this==GOGGLES.get())goggles(e,equipped);if(this==BELT.get()){modifier(e,Attributes.ARMOR,"underground_guard_armor",equipped?1:0);modifier(e,Attributes.KNOCKBACK_RESISTANCE,"underground_guard_knockback",equipped?.10:0);}}
```

## src/main/java/org/slavicmyths/item/FolkEquipmentEffects.java

```java
2: import net.minecraft.world.entity.ai.attributes.AttributeInstance;
6: import net.minecraft.world.entity.ai.attributes.*;
12: import net.minecraft.world.damagesource.DamageSource;
26:   CompoundTag data=p.getPersistentData();AttributeInstance speed=p.getAttribute(Attributes.MOVEMENT_SPEED);
37:  @SubscribeEvent public static void hurt(LivingIncomingDamageEvent event){
39:   if(event.getSource().is(net.minecraft.world.damagesource.DamageTypes.LIGHTNING_BOLT)&&wears(victim,ModItems.PERUN_RING.get()))event.setAmount(event.getAmount()*.65F);
47:    if(event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)&&victim instanceof org.slavicmyths.entity.WildlifeEntity&&wears(attacker,ModItems.HUNTER_BELT.get()))event.setAmount(event.getAmount()*1.1F);
53:   LivingEntity e=event.getEntity();if(e.level().isClientSide||event.getSource().is(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD))return;
56:   charm.shrink(1);event.setCanceled(true);e.setHealth(1);e.invulnerableTime=40;e.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,60,1));
64:    // Ordinary attributed damage: armor, resistance and boss-specific hurt rules remain in force.
65:    target.hurt(org.slavicmyths.combat.MythDamageSources.caused("slavic_retribution",e),50);
```

## src/main/java/org/slavicmyths/item/SilverCombat.java

```java
6: import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
13: public final class SilverCombat {
14:     public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> SILVER=net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,ResourceLocation.fromNamespaceAndPath(SlavicMyths.MOD_ID,"silver_weapons"));
15:     public static boolean silverStrike(net.minecraft.world.damagesource.DamageSource source){return source.getEntity() instanceof net.minecraft.world.entity.LivingEntity&&source.getDirectEntity()==source.getEntity()&&((net.minecraft.world.entity.LivingEntity)source.getEntity()).getMainHandItem().is(SILVER);}
17:             net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE,ResourceLocation.fromNamespaceAndPath(SlavicMyths.MOD_ID, "silver_vulnerable"));
18:     @SubscribeEvent public static void onHurt(LivingIncomingDamageEvent event) {
26:         if (silverStrike(event.getSource())) {
30:     private SilverCombat() { }
```

## src/main/java/org/slavicmyths/item/StormProtection.java

```java
5: import net.minecraft.world.damagesource.DamageSource;
8: import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
18:     @SubscribeEvent public static void onHurt(LivingIncomingDamageEvent event) {
21:         if (player.getOffhandItem().getItem() == ModItems.AMBER_CHARM.get() && event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
24:         if (player.getOffhandItem().getItem() == ModItems.TRAVELER_CHARM.get() && event.getSource().is(net.minecraft.world.damagesource.DamageTypes.FALL)) {
31:         if (!event.getSource().is(net.minecraft.world.damagesource.DamageTypes.LIGHTNING_BOLT)) return;
```

## src/main/java/org/slavicmyths/item/ThunderSpearCombat.java

```java
8: import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
14: /** Add the discharge to the accepted melee hit, so hurt invulnerability cannot swallow it. */
17:     @SubscribeEvent public static void onHurt(LivingIncomingDamageEvent event) {
23:                 || player.getCooldowns().isOnCooldown(ModItems.THUNDER_SPEAR.get())) return;
25:         player.getCooldowns().addCooldown(ModItems.THUNDER_SPEAR.get(), 120);
```

## src/main/java/org/slavicmyths/kalin/KalinContent.java

```java
6: import net.minecraft.world.entity.ai.attributes.Attributes;
19:  public static final List<String> CUES=List.of("banner_flap","battle_seal_use","camp_fire_loop_short","encounter_start_fx","horse_gallop_loop","horse_rear","horse_snort","horse_step_01","horse_step_02","horse_stomp","kalin_command_shout_fx","kalin_kneel_surrender","kalin_parry","kalin_rage_burst","kalin_sabre_heavy","kalin_sabre_light","kalin_spin_attack","kalin_step_heavy_01","kalin_step_heavy_02","kalin_war_horn","soldier_spawn_common","soldier_spawn_elite","ui_select_two_heroes");
23:  ModItems.ITEMS.register("kalin_tsar_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(KALIN,0x222326,0x9B2833,new Item.Properties()));
24:  ModItems.ITEMS.register("kalin_war_horse_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(HORSE,0x302326,0xB59852,new Item.Properties()));}
27:  e.put(KALIN.get(),Mob.createMobAttributes().add(Attributes.MAX_HEALTH,1024).add(Attributes.ARMOR,16).add(Attributes.ARMOR_TOUGHNESS,4).add(Attributes.MOVEMENT_SPEED,.24).add(Attributes.KNOCKBACK_RESISTANCE,.8).add(Attributes.FOLLOW_RANGE,40).build());
28:  e.put(HORSE.get(),Mob.createMobAttributes().add(Attributes.MAX_HEALTH,300).add(Attributes.ARMOR,16).add(Attributes.MOVEMENT_SPEED,.32).add(Attributes.KNOCKBACK_RESISTANCE,1).build());
29:  e.put(COMMON.get(),net.minecraft.world.entity.monster.Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,28).add(Attributes.ARMOR,4).add(Attributes.ATTACK_DAMAGE,5).add(Attributes.MOVEMENT_SPEED,.25).add(Attributes.FOLLOW_RANGE,40).build());
30:  e.put(ELITE.get(),net.minecraft.world.entity.monster.Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,65).add(Attributes.ARMOR,12).add(Attributes.ATTACK_DAMAGE,9).add(Attributes.MOVEMENT_SPEED,.27).add(Attributes.FOLLOW_RANGE,40).build());}
```

## src/main/java/org/slavicmyths/kalin/KalinEntity.java

```java
2: import java.util.*;import net.minecraft.nbt.CompoundTag;import net.minecraft.network.syncher.*;import net.minecraft.server.level.*;import net.minecraft.server.level.ServerBossEvent;import net.minecraft.world.BossEvent;import net.minecraft.world.damagesource.DamageSource;import net.minecraft.world.entity.*;import net.minecraft.world.level.Level;import net.minecraft.world.phys.Vec3;import org.slavicmyths.finist.*;import org.slavicmyths.skim.HeroCalls;
8:  /** Vanilla MAX_HEALTH attribute caps at1024; this fixed boss pool stays1800 without changing global attributes. */
22:  public DamageSource source(String kind){var key=net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,KalinContent.id("kalin_"+kind));return new DamageSource(level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(key),this);}
23:  public void strike(LivingEntity e,float diamond){if(enemy(e)&&hasLineOfSight(e)){e.hurt(source(action()==FalconAction.DASH?"charge":action()==FalconAction.WAVE?"spin":action()==FalconAction.BANNER?"stomp":"sabre"),diamondRaw(diamond));e.knockback(.65,getX()-e.getX(),getZ()-e.getZ());}}
25:  int[][] points={{-26,-8},{26,-8},{-26,8},{26,8},{-8,26},{8,26}};for(int n=0;n<elites+commons&&soldiers.size()<14;n++){boolean elite=n<elites;for(int attempt=0;attempt<12;attempt++){int index=elite?4+attempt%2:(n+attempt)%4;var q=points[index];var proposed=origin().add(q[0]+(attempt/6)*2,0,q[1]+n%3*2);var at=KartausEncounter.ground(w,proposed,.85,2.3);if(at==null||targets().stream().anyMatch(t->t.position().distanceToSqr(at)<25)||w.getEntitiesOfClass(KalinSoldier.class,new net.minecraft.world.phys.AABB(at.add(-1,0,-1),at.add(1,3,1))).size()>0)continue;var s=(elite?KalinContent.ELITE:KalinContent.COMMON).get().create(w);s.commander=getUUID();s.moveTo(at);s.setTarget(getTarget());if(w.addFreshEntity(s)){soldiers.add(s.getUUID());s.playSound(KalinContent.sound(elite?"soldier_spawn_elite":"soldier_spawn_common"),.6F,1);}break;}}
27:  private void cast(FalconAction a,String key,int cd){committed=getTarget();if(committed!=null)face(committed);begin(a);burst=false;if(mounted())((WarHorse)getVehicle()).begin(a);cooldown(key,cd);hits.clear();step=0;if(mounted()){var h=(WarHorse)getVehicle();h.setYRot(getYRot());h.yBodyRot=getYRot();}if(a==FalconAction.DASH){charge=heading();sound(mounted()?"horse_gallop_loop":"kalin_sabre_light");}if(a==FalconAction.BANNER)sound("horse_rear");if(a==FalconAction.SWORD)sound("kalin_sabre_heavy");if(a==FalconAction.WAVE)sound("kalin_spin_attack");if(a==FalconAction.GUARDS){sound("kalin_war_horn");sound("kalin_command_shout_fx");}}
31:  int phase=getHealth()<360?4:getHealth()<720?3:getHealth()<=1170?2:1;if(phase!=phase()){entityData.set(PHASE,phase);if(phase>=3){getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);sound("kalin_rage_burst");}}
34:  if(action()!=FalconAction.IDLE){getNavigation().stop();int t=age();if((action()==FalconAction.SWORD&&frame(6))||(action()==FalconAction.INTERCEPT&&parried&&frame(8)))swing(net.minecraft.world.InteractionHand.MAIN_HAND);if(action()==FalconAction.SWORD&&frame(12)&&enemy(committed)&&cone(committed,mounted()?4.5:3.6,100))strike(committed,7);
35:  if(action()==FalconAction.WAVE&&(frame(15)||frame(26)||frame(37))){for(var target:targets())if(distanceToSqr(target)<(mounted()?36:25)&&hasLineOfSight(target)){strike(target,3); }{var mover=mounted()?getVehicle():this;var d=heading().scale(.12);if(mover.position().add(d).distanceToSqr(origin())<28*28&&w.noCollision(mover,mover.getBoundingBox().move(d)))mover.move(MoverType.SELF,d);if(mounted())setPos(mover.position().add(0,1.4,0));}}
36:  if(action()==FalconAction.INTERCEPT&&parried&&frame(14)&&enemy(committed)&&cone(committed,4,120))strike(committed,5);
38:  if(action()==FalconAction.BANNER&&frame(25)){sound("horse_stomp");for(var target:targets())if(distanceToSqr(target)<16)strike(target,4);}
39:  if(action()==FalconAction.DASH&&t>=(burst?6:8)&&t<=(burst?12:21)&&charge!=null){var mover=mounted()?getVehicle():this;double speed=mounted()?(burst?.6:.9):.55;var d=charge.scale(speed);var next=mover.position().add(d);if(next.distanceToSqr(origin())>28*28||!w.hasChunkAt(net.minecraft.core.BlockPos.containing(next))||!org.slavicmyths.sivka.SivkaWhistle.safeFloor(w,net.minecraft.core.BlockPos.containing(next).below())||!w.noCollision(mover,mover.getBoundingBox().move(d))){begin(FalconAction.STAGGER,45);sound("horse_rear");return;}mover.move(MoverType.SELF,d);mover.setDeltaMovement(Vec3.ZERO);if(mounted()){setPos(mover.position().add(0,1.4,0));mover.setYRot(getYRot());((WarHorse)mover).yBodyRot=getYRot();}for(var target:targets())if(mover.getBoundingBox().inflate(.7).intersects(target.getBoundingBox())&&hits.add(target.getUUID()))strike(target,mounted()?6:5);}
40:  if(t>=duration()){begin(FalconAction.IDLE);if(mounted())((WarHorse)getVehicle()).begin(FalconAction.IDLE);cooldown("recovery",phase>=3?12:20);}return;}
47:  @Override public boolean hurt(DamageSource s,float d){if(surrendered())return false;if(s.getEntity() instanceof KalinSoldier||s.getEntity() instanceof WarHorse)return false;if(action()==FalconAction.INTERCEPT&&age()>=2&&age()<=8&&!parried&&s.getDirectEntity() instanceof LivingEntity attacker&&enemy(attacker)&&(KalinParty.owner(attacker)!=null||attacker.getMainHandItem().is(net.minecraft.tags.ItemTags.SWORDS)||attacker.getMainHandItem().is(net.minecraft.tags.ItemTags.AXES))&&cone(attacker,4,120)&&!s.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)&&!s.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)){parried=true;committed=attacker;sound("kalin_parry");return false;}return super.hurt(s,d);}
48:  public void damageReceived(DamageSource s){if(s.getEntity() instanceof ServerPlayer p&&owner!=null&&owner.equals(p.getUUID())&&!p.isCreative())playerHit=true;else if(s.getEntity() instanceof Mob m&&enemy(m))heroPressure++;}
49:  public void surrender(DamageSource source){if(surrendered())return;setHealth(1);entityData.set(SURRENDER,true);finishAt=level().getGameTime()+100;begin(FalconAction.DEATH,100);setTarget(null);sound("kalin_kneel_surrender");cleanup();if(level() instanceof ServerLevel w){var e=entry();if(e!=null)KalinEncounter.get(w).finish(w,e,playerHit&&!source.is(net.minecraft.world.damagesource.DamageTypes.GENERIC_KILL));}}
54:  @Override protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource s){return net.minecraft.sounds.SoundEvents.IRON_GOLEM_HURT;}
55:  @Override protected void dropAllDeathLoot(ServerLevel w,DamageSource s){}@Override protected void dropExperience(Entity e){}
```

## src/main/java/org/slavicmyths/kalin/KalinEvents.java

```java
7:  @SubscribeEvent public static void incoming(LivingIncomingDamageEvent e){var attacker=e.getSource().getEntity();var victim=e.getEntity();var a=attacker==null?null:KalinParty.owner(attacker);var b=KalinParty.owner(victim);if(a!=null&&a.equals(b)&&victim.level() instanceof ServerLevel world&&KalinEncounter.get(world).owner(a)!=null){e.setCanceled(true);return;}if(attacker instanceof KalinEntity k&&(!k.valid()||k.surrendered()||!k.enemy(victim)))e.setCanceled(true);if(attacker instanceof KalinSoldier s&&s.level() instanceof ServerLevel w){var boss=s.commander==null?null:w.getEntity(s.commander);if(!(boss instanceof KalinEntity k)||!k.valid()||k.surrendered()||!k.soldiers.contains(s.getUUID())||!k.enemy(victim))e.setCanceled(true);}}
8:  @SubscribeEvent public static void damage(LivingDamageEvent.Pre e){if(e.getEntity() instanceof KalinEntity k&&e.getNewDamage()>0){k.damageReceived(e.getSource());if(e.getNewDamage()>=k.getHealth()){e.setNewDamage(0);k.surrender(e.getSource());}}}
```

## src/main/java/org/slavicmyths/kalin/KalinSoldier.java

```java
12:  @Override protected void dropAllDeathLoot(ServerLevel w,net.minecraft.world.damagesource.DamageSource s){}@Override protected void dropExperience(Entity e){}
```

## src/main/java/org/slavicmyths/kalin/WarHorse.java

```java
10:  @Override public boolean hurt(net.minecraft.world.damagesource.DamageSource s,float d){if(commander!=null&&level() instanceof ServerLevel w&&w.getEntity(commander) instanceof KalinEntity k)return k.hurt(s,d);return super.hurt(s,d);}
```

## src/main/java/org/slavicmyths/karachun/Karachun.java

```java
8: import net.minecraft.world.entity.ai.attributes.*;
10: import net.minecraft.world.damagesource.*;
19:  public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,1024).add(Attributes.MOVEMENT_SPEED,.2).add(Attributes.ARMOR,10).add(Attributes.ATTACK_DAMAGE,12).add(Attributes.FOLLOW_RANGE,40).add(Attributes.KNOCKBACK_RESISTANCE,.9);}
22:  /** Like the existing Poloz, own health ceiling avoids vanilla's 1024 attribute clamp. */
24:  public void configureHealth(float health){entityData.set(MAX,health);setHealth(health);}
27:  private List<LivingEntity> targets(){return level().getEntitiesOfClass(LivingEntity.class,new AABB(anchor,anchor).inflate(NightConfig.value("arena")),this::candidate);}
28:  public void credit(UUID uuid,float damage,boolean attacked){if(uuid==null||session==null||!(level() instanceof ServerLevel w))return;var d=NightLedger.get(w);var s=d.session(session);var parts=s.getCompound("Participants");var n=parts.getCompound(uuid.toString());n.putDouble("Damage",n.getDouble("Damage")+damage);if(attacked)n.putBoolean("Attacked",true);parts.put(uuid.toString(),n);s.put("Participants",parts);d.setDirty();}
34:   if(tickCount%4==0){var ts=targets();boolean present=false;var d=NightLedger.get(w);var parts=d.session(session).getCompound("Participants");for(var t:ts){affected.add(t.getUUID());if(t instanceof ServerPlayer p){present=true;bar.addPlayer(p);var row=parts.getCompound(p.getUUID().toString());row.putInt("Active",row.getInt("Active")+4);parts.put(p.getUUID().toString(),row);}double distance=Math.sqrt(FrostWorld.bboxDistance(position().add(0,1,0),t.getBoundingBox()));double radius=NightConfig.value("aura"),rate=distance<4?NightConfig.value("nearGain"):distance<7?NightConfig.value("midGain"):distance<=radius?NightConfig.value("farGain"):0;if(rate>0&&hasLineOfSight(t))NightStatus.add(t,session,rate/5*(t instanceof ServerPlayer?1:.5)*(nightTicks>0?1.4:1),nightTicks>0?.6:phase()==3?.2:0,nightTicks>0);else if(nightTicks>0)NightStatus.add(t,session,0,.6,true);}
38:   birds.removeIf(id->!(w.getEntity(id) instanceof NightRaven r)||!r.isAlive());long now=w.getGameTime();if(birds.size()<4&&now>=ready[0]){ravens();ready[0]=now+NightConfig.ticks("resummonCooldown");}
40:   if(battleTicks>120&&marker==null&&now>=ready[9]){marker=t.position();markerAge=0;action(9);ready[9]=now+NightConfig.ticks("hourCooldown");}
41:   else if(battleTicks>80&&nightTicks==0&&now>=ready[7]){action(7);ready[7]=now+NightConfig.ticks("nightCooldown");}
42:   else if(battleTicks>180&&now>=ready[8]){action(8);ready[8]=now+NightConfig.ticks("fearCooldown");}
43:   else if(now>=ready[4]&&distanceToSqr(t)>16){action(4);ready[4]=now+NightConfig.ticks("hungerCooldown");}
44:   else if(distanceToSqr(t)<81&&now>=ready[5]){action(5);ready[5]=now+NightConfig.ticks("frostCooldown");}
45:   else if(distanceToSqr(t)<25&&now>=ready[6]){action(6);ready[6]=now+NightConfig.ticks("deathCooldown");}
46:   else if(distanceToSqr(t)<36&&now>=ready[1]){action(1+combo++%3);ready[1]=now+NightConfig.ticks("sweepCooldown")/(phase()==3?2:1);}
57:  public boolean hit(LivingEntity t,float damage,int waning,int fear){if(!candidate(t))return false;boolean success=t.hurt(damageSources().mobAttack(this),damage);if(success){affected.add(t.getUUID());NightStatus.add(t,session,waning*(t instanceof ServerPlayer?1:.5),fear,false);credit(NightOwners.owner(t),0,true);}return success;}
60:  private void cleanup(){nightTicks=0;marker=null;if(level() instanceof ServerLevel w){for(UUID id:birds){var r=w.getEntity(id);if(r!=null)r.discard();}birds.clear();for(UUID id:affected){var t=w.getEntity(id);if(t instanceof LivingEntity l){NightStatus.clear(l,session);NightEffects.clear(l,session);}}affected.clear();for(var e:w.getEntitiesOfClass(NightBlade.class,getBoundingBox().inflate(NightConfig.value("arena")+10),b->Objects.equals(b.session,session)))e.discard();}}
61:  private void ring(Vec3 at,double radius,int count){var w=(ServerLevel)level();for(int i=0;i<count;i++){double a=i*Math.PI*2/count;w.sendParticles(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(.3f,.18f,.48f),.8f),at.x+Math.cos(a)*radius,at.y+.1,at.z+Math.sin(a)*radius,1,0,.05,0,0);}}
63:  @Override public boolean hurt(DamageSource s,float n){if(s.is(DamageTypes.GENERIC_KILL)&&level() instanceof ServerLevel w){if(session!=null){NightLedger.get(w).session(session).putBoolean("Cancelled",true);NightLedger.get(w).setDirty();}discard();return true;}if(!fighting()||s.getEntity() instanceof Karachun||s.getEntity() instanceof NightRaven)return false;return super.hurt(s,n);}
64:  @Override public void setHealth(float hp){if(session!=null&&phase()<4&&hp<=NightRules.floor(getMaxHealth()))hp=NightRules.floor(getMaxHealth());super.setHealth(hp);}
65:  @Override public void die(DamageSource s){if(!level().isClientSide)yieldTrial();}
66:  @Override protected void dropAllDeathLoot(ServerLevel w,DamageSource s){}
```

## src/main/java/org/slavicmyths/karachun/NightBlade.java

```java
18:  @Override protected void onHitEntity(EntityHitResult h){if(level().isClientSide)return;if(h.getEntity() instanceof LivingEntity t){if(getOwner() instanceof Karachun b&&b.candidate(t)){NightEffects.add(t,session,net.minecraft.world.effect.MobEffects.HUNGER,240,3);NightStatus.add(t,session,0,6,false);b.credit(NightOwners.owner(t),0,true);}else if(getOwner() instanceof ServerPlayer p&&cast!=null&&NightCombat.claim(p,cast,t.getUUID()))NightCombat.damage(p,t,weapon,1.1f);}discard();}
```

## src/main/java/org/slavicmyths/karachun/NightContent.java

```java
16:  static {for(String s:List.of("ambient_low_wind","enter_trial","voice_whisper","scythe_windup","scythe_hit","dark_frost","long_night_cast","touch_hunger","touch_frost","touch_death","last_hour_mark","last_hour_impact","fear_phantom_spawn","fear_phantom_dispel","yield","raven_call","raven_flap","raven_dive","raven_hit","life_eater","last_breath","black_harvest","wing_activate","waning_stage_up"))SOUNDS.put(s,ModSounds.SOUNDS.register("karachun."+s,()->SoundEvent.createFixedRangeEvent(id("karachun."+s),32)));}
```

## src/main/java/org/slavicmyths/karachun/NightEvents.java

```java
11:  @SubscribeEvent(priority=EventPriority.LOWEST) public static void clamp(LivingDamageEvent.Pre e){if(e.getEntity() instanceof Karachun b&&b.fighting())e.setNewDamage(NightRules.clamp(b.getHealth(),b.getMaxHealth(),e.getNewDamage()));}
12:  @SubscribeEvent public static void friendly(LivingIncomingDamageEvent e){if(e.getSource().getEntity() instanceof NightRaven r&&!r.allowed(e.getEntity()))e.setCanceled(true);}
13:  @SubscribeEvent public static void damage(LivingDamageEvent.Post e){if(e.getNewDamage()<=0||e.getEntity().level().isClientSide)return;var from=e.getSource().getEntity();if(from==null)return;UUID owner=NightOwners.owner(from);if(e.getEntity() instanceof Karachun b&&b.fighting()&&owner!=null){b.credit(owner,e.getNewDamage(),false);if(from instanceof LivingEntity l)NightStatus.reduceFear(l,b.session);}if(from instanceof LivingEntity l&&NightCombat.valid(e.getEntity(),l)&&org.slavicmyths.rpg.Runes.has(l.getMainHandItem(),"waning")){var t=e.getEntity();var n=t.getPersistentData();long now=t.level().getGameTime();if(now>=n.getLong("Night192RuneReady")){n.putLong("Night192RuneReady",now+20);if(l.getRandom().nextDouble()<NightConfig.value("runeChance"))NightStatus.add(t,l.getUUID(),NightConfig.value("runeGain"),0,false);}}}
14:  @SubscribeEvent public static void attack(AttackEntityEvent e){if(e.getEntity() instanceof ServerPlayer p&&p.getMainHandItem().is(NightContent.SCYTHE.get())){e.setCanceled(true);NightCombat.action(p,0);}}
23:  public static void leave(ServerPlayer p){for(String key:new ArrayList<>(p.getPersistentData().getCompound("Night192Effects").getAllKeys()))try{NightEffects.clear(p,UUID.fromString(key));}catch(IllegalArgumentException ignored){}NightStatus.clearAll(p);p.getPersistentData().remove(NightCombat.KEY);p.getPersistentData().remove("Night192Casts");for(var r:p.serverLevel().getEntitiesOfClass(NightRaven.class,p.getBoundingBox().inflate(64),r->p.getUUID().equals(r.owner)))r.discard();}
```

## src/main/java/org/slavicmyths/karachun/NightRaven.java

```java
7: import net.minecraft.world.entity.ai.attributes.*;
16:  public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,20).add(Attributes.MOVEMENT_SPEED,.28).add(Attributes.FLYING_SPEED,.7).add(Attributes.FOLLOW_RANGE,32).add(Attributes.ATTACK_DAMAGE,3);}
21:  public void configure(Karachun boss,int index){owner=boss.getUUID();session=boss.session;slot=index;getAttribute(Attributes.MAX_HEALTH).setBaseValue(NightConfig.value("ravenHealth"));setHealth((float)NightConfig.value("ravenHealth"));nextDive=level().getGameTime()+60+index*25;}
26:  @Override public void tick(){super.tick();if(level().isClientSide)return;var w=(ServerLevel)level();var master=master();if(master==null||!master.isAlive()||master instanceof ServerPlayer p&&(p.isSpectator()||p.hasDisconnected())||master instanceof Karachun b&&!b.fighting()&&b.phase()!=0||allied()&&--life<=0||distanceToSqr(master)>64*64){dispel();return;}age++;long now=w.getGameTime();if(tickCount%20==0){var t=getTarget();if(t==null||!allowed(t)||distanceToSqr(t)>1024){LivingEntity wanted=master.getLastHurtMob();if(!allowedNull(wanted))wanted=master.getLastHurtByMob();if(!allowedNull(wanted))wanted=w.getEntitiesOfClass(LivingEntity.class,master.getBoundingBox().inflate(28),this::allowed).stream().min(Comparator.comparingDouble(master::distanceToSqr)).orElse(null);setTarget(wanted);}}
29:   else if(state()==2){if(target==null||!allowed(target)){state(3);return;}Vec3 delta=divePoint.subtract(position());Vec3 velocity=delta.normalize().scale(.8);var collision=w.clip(new net.minecraft.world.level.ClipContext(position(),position().add(velocity),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,this));if(collision.getType()!=HitResult.Type.MISS||horizontalCollision||verticalCollision){state(3);return;}setDeltaMovement(velocity);getLookControl().setLookAt(target);if(!hit&&getBoundingBox().expandTowards(velocity).inflate(.25).intersects(target.getBoundingBox())&&hasLineOfSight(target)){hit=true;boolean damaged=target.hurt(damageSources().mobAttack(this),(float)NightConfig.value("ravenDamage"));if(damaged){NightContent.sound(this,"raven_hit");if(master instanceof Karachun b){b.credit(FrostWorld.owner(target),0,true);NightStatus.add(target,b.session,0,8,false);}}state(3);}else if(age>=25||delta.length()<.6)state(3);}
30:   else {move(master.position().add(Math.cos(slot*2)*4,5,Math.sin(slot*2)*4),1);if(age>=30){nextDive=now+NightConfig.ticks("ravenCooldown");state(0);}}
35:  @Override protected void dropAllDeathLoot(ServerLevel w,net.minecraft.world.damagesource.DamageSource s){}
36:  @Override public boolean causeFallDamage(float d,float m,net.minecraft.world.damagesource.DamageSource s){return false;}
37:  @Override public boolean hurt(net.minecraft.world.damagesource.DamageSource s,float damage){var master=master();if(master==null)return false;if(s.getEntity() instanceof LivingEntity l&&(l==master||l instanceof NightRaven||allied()&&!FrostWorld.hostile(l,master)))return false;return super.hurt(s,damage);}
```

## src/main/java/org/slavicmyths/karachun/NightRules.java

```java
4:  public static float clamp(float hp,float max,float hit){return Math.max(0,Math.min(hit,hp-floor(max)));}
5:  public static int phase(float hp,float max){return hp<=max*.25?3:hp<=max*.7?2:1;}
8:  public static boolean eligible(double damage,int active,boolean attacked){return damage>=1||attacked&&active>=300;}
```

## src/main/java/org/slavicmyths/kurgan/BurialAccessories.java

```java
4: import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
14:  @SubscribeEvent public static void hurt(LivingIncomingDamageEvent e){LivingEntity victim=e.getEntity();if(victim.level().isClientSide||e.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY))return;
16:   if(FolkEquipmentEffects.wears(victim,ModItems.GRIVNA.get())&&!e.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR))e.setAmount(e.getAmount()*.96F);
```

## src/main/java/org/slavicmyths/kurgan/BurialCoffinBlock.java

```java
20: import net.minecraft.world.damagesource.DamageSource;
```

## src/main/java/org/slavicmyths/kurgan/KurganBolt.java

```java
7: import net.minecraft.world.damagesource.DamageSource;
22:     @Override protected void onHitEntity(EntityHitResult hit){super.onHitEntity(hit);if(!level().isClientSide){Entity owner=getOwner();if(owner instanceof LivingEntity&&!(hit.getEntity() instanceof KurganCreature))hit.getEntity().hurt(damageSources().mobProjectile(this,(LivingEntity)owner),6);discard();}}
```

## src/main/java/org/slavicmyths/kurgan/KurganCreature.java

```java
6: import net.minecraft.world.entity.ai.attributes.Attribute;
12: import net.minecraft.world.entity.ai.attributes.*;
26: import net.minecraft.world.damagesource.DamageSource;
49:     private final EnumMap<Move,Integer> cooldowns=new EnumMap<>(Move.class);
54:     public static AttributeSupplier.Builder attributes(Kind k){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,k.hp).add(Attributes.ATTACK_DAMAGE,k.damage).add(Attributes.MOVEMENT_SPEED,k.speed).add(Attributes.FOLLOW_RANGE,k.range).add(Attributes.ARMOR,k.armor).add(Attributes.KNOCKBACK_RESISTANCE,k.resistance);}
64:     @Override protected SoundEvent getHurtSound(DamageSource s){return sound("hurt");}
73:     @Override public boolean hurt(DamageSource source,float damage){
75:         if(!level().isClientSide&&guarding()&&frontal&&!source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)&&!source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR)){
76:             damage*=kind==Kind.DRUZHINNIK?.4F:.3F;blocks++;if(blocks>=3){blocks=0;riposte=true;}
78:         }return super.hurt(source,damage);
80:     private boolean ready(Move m){return cooldowns.getOrDefault(m,0)<=0;}
81:     private void begin(Move move,LivingEntity target){entityData.set(ACTION,move.ordinal());entityData.set(START,(int)level().getGameTime());actionTick=0;alignWait=0;dashHit=false;entityData.set(GUARD,false);navigation.stop();aim=target.position().subtract(position()).multiply(1,0,1).normalize();sealTarget=target.blockPosition();cooldowns.put(move,move.cooldown);
104:     private boolean strike(LivingEntity target,float damage,double reach,double cone,boolean bash){if(!target.isAlive()||distanceTo(target)>reach||!getSensing().hasLineOfSight(target))return false;Vec3 direction=target.position().subtract(position()).multiply(1,0,1).normalize();if(aim.dot(direction)<cone)return false;
105:         if(bash&&target instanceof Player&&target.isBlocking()){((Player)target).getCooldowns().addCooldown(Items.SHIELD,60);target.stopUsingItem();}
106:         boolean hit=target.hurt(damageSources().mobAttack(this),damage);if(bash)target.knockback(.9F, -aim.x,-aim.z);return hit;
108:     private void release(LivingEntity target){Move m=action();float damage=(float)kind.damage;basicWait=basicInterval(kind,phase());
111:             case CLAW:case SWORD:case AXE:case COMBO:strike(target,damage,kind.boss()?3.3:2.7,-.1,false);break;
112:             case TOUCH:if(strike(target,damage,2.5,-.2,false))target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40,0));break;
113:             case BITE:if(strike(target,damage+2,2.3,.4,false)){heal(4);target.getPersistentData().putLong("SlavicUpyrBiteUntil",level().getGameTime()+100);}break;
119:             case SWEEP:for(Player p:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(3.5)))strike(p,damage+2,3.5,-.35,true);break;
120:             case HEAVY:if(strike(target,damage+4,3.8,.65,true))cue("heavy_strike");break;
121:             case GRAB:if(strike(target,8,3,.5,true)){heal(12);target.setDeltaMovement(position().subtract(target.position()).normalize().scale(.6).add(0,.1,0));target.hurtMarked=true;}break;
132:     private void areaSeal(){if(sealTarget==null)return;Vec3 center=new Vec3(sealTarget.getX()+.5,sealTarget.getY()+.2,sealTarget.getZ()+.5);for(Player p:level().getEntitiesOfClass(Player.class,new AABB(sealTarget).inflate(2.5,2,2.5)))if(p.isAlive()&&p.position().distanceTo(center)<3&&clearLine(center.add(0,.5,0),p.position().add(0,1,0))&&p.hurt(damageSources().mobAttack(this),8))p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,50,2));}
139:         @Override public void tick(){LivingEntity target=getTarget();if(target==null)return;for(Move m:new ArrayList<>(cooldowns.keySet()))cooldowns.put(m,Math.max(0,cooldowns.get(m)-1));basicWait=Math.max(0,basicWait-1);
140:             int nextPhase=Math.max(phase(),KurganFighter.phase(kind,getHealth()/getMaxHealth()));if(nextPhase!=phase()){entityData.set(PHASE,nextPhase);entityData.set(PHASE_START,(int)level().getGameTime());getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(kind.speed+(kind==Kind.VOEVODA?.02:nextPhase*.025));cue("phase_shift");}
149:                 if((move==Move.LEAP||move==Move.BASH||move==Move.CHARGE||move==Move.RUSH||move==Move.RIPOSTE)&&actionTick>=move.windup&&actionTick<move.windup+6){dash();if(!dashHit&&distanceTo(target)<2.7&&getSensing().hasLineOfSight(target)){strike(target,(float)(move==Move.BASH||move==Move.CHARGE?kind.damage*.6:kind.damage),2.7,.45,true);dashHit=true;}}
151:                 if(move==Move.COMBO&&(actionTick==move.windup+spacing||actionTick==move.windup+spacing*2))strike(target,(float)kind.damage,3.3,.2,false);
152:                 if(actionTick>=move.windup+move.recovery-(move==Move.COMBO?phase()*4:0)){entityData.set(ACTION,0);getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(kind.speed+(kind==Kind.VOEVODA&&phase()>0?.02:kind==Kind.PRINCE?phase()*.025:0));}
171:     @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(kurgan!=null)n.putUUID("Kurgan",kurgan);if(home!=null)n.putLong("KurganHome",home.asLong());n.putInt("KurganRoom",room);n.putBoolean("LimitedSummon",summoned);n.putInt("CombatPhase",phase());CompoundTag cd=new CompoundTag();for(Map.Entry<Move,Integer> e:cooldowns.entrySet())cd.putInt(e.getKey().name(),e.getValue());n.put("KurganCooldowns",cd);}
172:     @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);kurgan=n.hasUUID("Kurgan")?n.getUUID("Kurgan"):null;home=n.contains("KurganHome")?BlockPos.of(n.getLong("KurganHome")):null;room=n.contains("KurganRoom")?n.getInt("KurganRoom"):-1;summoned=n.getBoolean("LimitedSummon");entityData.set(PHASE,Math.max(0,Math.min(2,n.getInt("CombatPhase"))));CompoundTag cd=n.getCompound("KurganCooldowns");for(Move m:Move.values())cooldowns.put(m,Math.max(0,Math.min(m.cooldown,cd.getInt(m.name()))));}
173:     @Override public void die(DamageSource source){boolean wasDead=dead;super.die(source);if(!wasDead&&dead&&!level().isClientSide)KurganEncounters.died(this);}
```

## src/main/java/org/slavicmyths/kurgan/KurganEncounters.java

```java
11: import net.minecraft.world.damagesource.DamageSource;
```

## src/main/java/org/slavicmyths/kurgan/KurganFighter.java

```java
5:     public enum Kind {
12:         public final String id;public final double hp,damage,speed,range,resistance,armor;public final float width,height;
13:         Kind(String id,double hp,double damage,double speed,double range,double resistance,double armor,float width,float height){this.id=id;this.hp=hp;this.damage=damage;this.speed=speed;this.range=range;this.resistance=resistance;this.armor=armor;this.width=width;this.height=height;}
16:     public enum Move {
22:         public final int windup,recovery,cooldown;
23:         Move(int windup,int recovery,int cooldown){this.windup=windup;this.recovery=recovery;this.cooldown=cooldown;}
```

## src/main/java/org/slavicmyths/kuzya/KuzyaContent.java

```java
7: import net.minecraft.world.entity.ai.attributes.Attributes;
16:     public static final DeferredHolder<EntityType<?>,EntityType<KuzyaEntity>> ENTITY=ModEntities.ENTITIES.register("kuzya",()->EntityType.Builder.of(KuzyaEntity::new,MobCategory.MISC).sized(.52F,.70F).fireImmune().clientTrackingRange(8).updateInterval(1).build("slavicmyths:kuzya"));
21:     @SubscribeEvent public static void attributes(EntityAttributeCreationEvent e){e.put(ENTITY.get(),Mob.createMobAttributes().add(Attributes.MAX_HEALTH,20).add(Attributes.MOVEMENT_SPEED,.12).add(Attributes.KNOCKBACK_RESISTANCE,1).add(Attributes.STEP_HEIGHT,.6).build());}
```

## src/main/java/org/slavicmyths/kuzya/KuzyaEntity.java

```java
10: import net.minecraft.world.damagesource.*;
36:     public KuzyaEntity(EntityType<? extends KuzyaEntity> type,net.minecraft.world.level.Level level){super(type,level);setPersistenceRequired();setInvulnerable(true);setCanPickUpLoot(false);getNavigation().setCanFloat(false);moveControl=new net.minecraft.world.entity.ai.control.MoveControl(this){@Override public void tick(){if(!route.isEmpty()&&(purpose.equals("stove")&&waypoint>0||purpose.equals("down"))){Vec3 target=route.get(waypoint);if(position().distanceToSqr(target)<2.6)setWantedPosition(target.x,target.y,target.z,2.6);}super.tick();}};}
58:         if(!homeBounds().inflate(.1).contains(position())){if(now>=nextPath){nextPath=now+80;Vec3 floor=anchor(.5,4,.5);if(safe(floor))getNavigation().moveTo(floor.x,floor.y,floor.z,.8);}return;}
86:     private void speak(String category){long now=level().getGameTime();boolean response=clip().equals("respond_right_click");if(now<voiceReady||!response&&now<houseVoiceReady)return;int[] choices=KuzyaAssets.voices(category);if(choices.length==0)return;int v=choices[random.nextInt(choices.length)];entityData.set(VOICE,v);entityData.set(VOICE_START,now);voiceReady=now+Math.max(240,KuzyaAssets.VOICE_TICKS[v]+20);houseVoiceReady=now+(category.endsWith("_large")?1400+random.nextInt(801):category.endsWith("_medium")?900+random.nextInt(601):900);sound(KuzyaAssets.VOICES[v],.7F);for(Player listener:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(14),p->distanceToSqr(p)<=196))listener.displayClientMessage(net.minecraft.network.chat.Component.translatable("subtitles.slavicmyths.kuzya."+KuzyaAssets.VOICES[v]),true);}
90:     @Override public boolean hurt(DamageSource source,float amount){return false;}
91:     @Override public boolean isInvulnerableTo(DamageSource source){return true;}
104:     @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);renovated=!n.contains("Renovated")||n.getBoolean("Renovated");houseVoiceReady=level().getGameTime()+Math.clamp(n.getLong("HouseVoiceRemaining"),0,2400);home=n.contains("HomeOrigin")?BlockPos.of(n.getLong("HomeOrigin")):null;homeHutId=n.hasUUID("HomeHutId")?n.getUUID("HomeHutId"):null;voiceReady=level().getGameTime()+Math.clamp(n.getLong("VoiceRemaining"),0,2400);guests.clear();for(Tag raw:n.getList("Guests",10)){CompoundTag t=(CompoundTag)raw;if(!t.hasUUID("Player")||guests.size()>=64)continue;Guest g=new Guest();g.ticks=Math.clamp(t.getInt("Ticks"),0,24000);g.away=Math.clamp(t.getInt("Away"),0,1200);g.complaintReady=level().getGameTime()+Math.clamp(t.getLong("ComplaintRemaining"),0,2400);guests.put(t.getUUID("Player"),g);}restoreScene(n);reloadPending=true;setInvulnerable(true);}
```

## src/main/java/org/slavicmyths/marinka/DobrynyaEntity.java

```java
10: import net.minecraft.world.damagesource.DamageSource;
33:   if(bull()){if(b!=null&&ready("bull")&&tickCount%4==0){face(b);charge=heading();cooldown("bull",160+random.nextInt(101));begin(WitchAction.BULL_CHARGE);cue("aurochs_charge_01");}return;}
36:   if(p!=null&&p.level()==w&&distanceToSqr(p)<36&&ready("ward")){for(var shot:w.getEntitiesOfClass(WitchBolt.class,getBoundingBox().inflate(12),s->!s.reflected&&s.reflectable&&s.activeBoss()!=null)){if(p.getBoundingBox().inflate(.5).clip(shot.position(),shot.position().add(shot.getDeltaMovement().scale(18))).isPresent()){face(shot.activeBoss());cooldown("ward",240);begin(WitchAction.WARD);cue("shield_ward_01");return;}}}
37:   if(target!=null){face(target);if(b!=null&&b.exposed()&&ready("unravel")&&edge(b)<=3.2){cooldown("unravel",340);begin(WitchAction.UNRAVEL);return;}if(ready("parry")&&edge(target)<3.8&&target.swinging&&random.nextFloat()<.6F){parryUsed=false;cooldown("parry",160);begin(WitchAction.PARRY);return;}
38:    if(ready("sidestep")&&b!=null&&b.action()==WitchAction.FIRE&&b.age()>4&&b.age()<12){Vec3 side=new Vec3(heading().z,0,-heading().x).scale(random.nextBoolean()?.9:-.9);if(w.noCollision(this,getBoundingBox().move(side))){move(MoverType.SELF,side);cooldown("sidestep",100);begin(WitchAction.SIDESTEP);return;}}
39:    if(ready("sweep")&&w.getEntitiesOfClass(Monster.class,getBoundingBox().inflate(3.25),m->m.isAlive()&&!isAlliedTo(m)).size()>=2){swept.clear();cooldown("sweep",280);begin(WitchAction.SWEEP);return;}
40:    boolean ownerActive=b==null||b.ownerActive(owner);if(ownerActive&&edge(target)<=2.6&&ready("combo")){cooldown("combo",120);begin(WitchAction.COMBO);return;}if(ownerActive&&edge(target)<=3.6&&ready("thrust")){cooldown("thrust",110);begin(WitchAction.THRUST);return;}if(tickCount%8==0)org.slavicmyths.poloz.PolozAim.navigate(this,target,1);
42:   if(getDeltaMovement().horizontalDistanceSqr()>.0004&&tickCount%12==0)cue("armor_step_01");if(tickCount%300==0&&ready("ambient")){cooldown("ambient",240+random.nextInt(141));cue("breath_idle_01");}
44:  private void strike(float damage,double reach,double angle){var target=getTarget();if(target!=null&&target.isAlive()&&cone(target,reach,angle)&&hasLineOfSight(target)){target.invulnerableTime=0;if(target instanceof org.slavicmyths.poloz.GreatPoloz poloz)org.slavicmyths.poloz.PolozAim.strike(this,poloz,damage);else target.hurt(damageSources().mobAttack(this),damage);if(action()==WitchAction.THRUST)target.knockback(target instanceof MarinkaEntity?.08:.45,getX()-target.getX(),getZ()-target.getZ());cue("spear_impact_01");}}
46:  case COMBO->{if((t==10||t==20)&&getTarget()!=null){face(getTarget());Vec3 advance=getTarget().position().subtract(position()).multiply(1,0,1).normalize().scale(.6);if(edge(getTarget())>2.1&&w.noCollision(this,getBoundingBox().move(advance)))move(MoverType.SELF,advance);}if(t==6)strike(9,2.6,95);if(t==13)strike(10,2.6,95);if(t==23)strike(15,2.6,95);if(t==3||t==10||t==20)cue("spear_swing_01");}
47:  case THRUST->{if(t==4)cue("spear_swing_01");if(t==8)strike(21,3.6,55);}
48:  case PARRY->{if(t==10&&parryUsed){strike(13,3.6,95);if(getTarget() instanceof MarinkaEntity m)m.begin(WitchAction.EXHAUSTED,7);else if(getTarget() instanceof net.minecraft.world.entity.Mob mob){mob.getNavigation().stop();mob.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,12,5));}}}
49:  case SWEEP->{if(t==4)cue("spear_sweep_01");if(t==9||t==15){int count=0;for(var e:w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(3.25),e->e instanceof Monster||e instanceof MarinkaEntity||e instanceof TurMinion)){if(count>=5)break;if(!isAlliedTo(e)&&cone(e,3.25,210)&&hasLineOfSight(e)&&swept.add(e.getUUID())){e.hurt(damageSources().mobAttack(this),15);if(!(e instanceof MarinkaEntity))e.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,22));count++;}}}}
50:  case WARD->{if(t>=6&&t<=13&&ready("reflected")){for(var shot:w.getEntitiesOfClass(WitchBolt.class,getBoundingBox().inflate(2.2),s->s.reflectable&&!s.reflected)){var toward=shot.position().subtract(position()).multiply(1,0,1).normalize();if(toward.dot(heading())>=Math.cos(Math.toRadians(55))&&shot.reflect(this)){cooldown("reflected",24);cue("magic_deflect_01");bark(4);break;}}}}
54:  case BULL_CHARGE->{if(t>=9&&t<=20&&charge!=null){if(b!=null&&yard()!=null&&yard().radius(position().add(charge.scale(.5)))<=23&&w.noCollision(this,getBoundingBox().move(charge.scale(.5))))move(MoverType.SELF,charge.scale(.5));if(t==14&&b!=null&&edge(b)<=3&&hasLineOfSight(b))b.hurt(damageSources().mobAttack(this),12);}}
61:  public float mitigation(DamageSource s){if(s.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY))return 1;boolean magic=s.getMsgId().startsWith("marinka_")||s.is(net.minecraft.world.damagesource.DamageTypes.MAGIC)||s.is(net.minecraft.world.damagesource.DamageTypes.INDIRECT_MAGIC);var at=s.getSourcePosition();boolean front=at!=null&&at.subtract(position()).multiply(1,0,1).normalize().dot(heading())>=Math.cos(Math.toRadians(55));if(!bull()&&!magic&&action()==WitchAction.PARRY&&age()>=4&&age()<=8&&!parryUsed&&front){parryUsed=true;cue("spear_parry_01");bark(3);return .75F*.3F;}if(!bull()&&magic&&action()==WitchAction.WARD&&age()>=1&&age()<=16&&front)return .6F;return magic?1:.75F;}
62:  @Override public boolean addEffect(net.minecraft.world.effect.MobEffectInstance effect,Entity source){var type=effect.getEffect();if(type==net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN||type==net.minecraft.world.effect.MobEffects.WEAKNESS||type==net.minecraft.world.effect.MobEffects.CONFUSION){String key="resolve_"+effect.getDescriptionId();if(!ready(key))return false;cooldown(key,240);effect=new net.minecraft.world.effect.MobEffectInstance(type,Math.max(1,Math.round(effect.getDuration()*.65F)),effect.getAmplifier(),effect.isAmbient(),effect.isVisible(),effect.showIcon());}return super.addEffect(effect,source);}
64:  @Override public boolean hurt(DamageSource s,float amount){if(s.getEntity()!=null&&isAlliedTo(s.getEntity()))return false;boolean result=super.hurt(s,amount);if(result){lastHurt=level().getGameTime();if(ready("hurt")){cooldown("hurt",28);cue("grunt_hurt_01");}}return result;}
65:  @Override public void die(DamageSource s){if(arena!=null){incapacitated=true;begin(WitchAction.INCAPACITATED);}super.die(s);if(owner!=null&&level() instanceof ServerLevel w){var d=WitchYard.get(w.getServer().overworld());d.heroes.remove(owner,getUUID());d.setDirty();}}
66:  @Override protected void dropAllDeathLoot(ServerLevel w,DamageSource s){}@Override protected void dropExperience(Entity cause){}
```

## src/main/java/org/slavicmyths/marinka/HexAnchor.java

```java
8: import net.minecraft.world.damagesource.DamageSource;
18:  @Override public boolean hurt(DamageSource s,float n){if(!(s.getEntity() instanceof net.minecraft.server.level.ServerPlayer p)||p.isSpectator()||n<=0)return false;if(invulnerableTime>0)return false;boolean result=super.hurt(s,n);if(result){entityData.set(HITS,hits()+1);if(level() instanceof ServerLevel w&&w.getEntity(boss) instanceof MarinkaEntity m)m.contribution(p);if(hits()>=3&&isAlive())release();}return result;}
20:  @Override public void die(DamageSource s){release();}
21:  @Override protected void dropAllDeathLoot(ServerLevel w,DamageSource s){}@Override protected void dropExperience(Entity cause){}
```

## src/main/java/org/slavicmyths/marinka/MarinkaContent.java

```java
6: import net.minecraft.world.entity.ai.attributes.Attributes;
26:  e.put(DOBRYNYA.get(),Mob.createMobAttributes().add(Attributes.MAX_HEALTH,230).add(Attributes.MOVEMENT_SPEED,.27).add(Attributes.ATTACK_DAMAGE,9).add(Attributes.FOLLOW_RANGE,40).add(Attributes.KNOCKBACK_RESISTANCE,.35).build());
27:  e.put(MARINKA.get(),net.minecraft.world.entity.monster.Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,600).add(Attributes.MOVEMENT_SPEED,.30).add(Attributes.ATTACK_DAMAGE,5).add(Attributes.FOLLOW_RANGE,40).add(Attributes.KNOCKBACK_RESISTANCE,.65).build());
28:  e.put(ANCHOR.get(),Mob.createMobAttributes().add(Attributes.MAX_HEALTH,80).add(Attributes.MOVEMENT_SPEED,0).build());
29:  e.put(TUR.get(),net.minecraft.world.entity.monster.Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,35).add(Attributes.MOVEMENT_SPEED,.25).add(Attributes.ATTACK_DAMAGE,5).build());}
```

## src/main/java/org/slavicmyths/marinka/MarinkaEntity.java

```java
9: import net.minecraft.world.damagesource.*;
30:  public void contribution(ServerPlayer p){var e=yard();if(e!=null&&p.isAlive()&&!p.isSpectator()&&!p.isCreative()&&p.level()==level()&&e.radius(p.position())<=43){contribution.put(p.getUUID(),level().getGameTime());e.participants.add(p.getUUID());}}
32:  public void breakWard(){if(falseBody())return;entityData.set(VEIL,false);veilEnd=0;brokenEnd=level().getGameTime()+160;exposedEnd=0;cooldown("veil",440);cooldown("restore_seal",260);begin(WitchAction.CRACK);cue("barrier_break_01");message("barrier_broken");}
41:   if(start==0){start=w.getGameTime();cooldown("veil",80);cooldown("shadow",280+random.nextInt(201));cooldown("magpie",540);lastValid=start;}
42:   if(falseBody()){var r=root();if(r==null||w.getGameTime()>=shadowEnd){dissolve();return;}phase=r.phase;}else{var p=w.getServer().getPlayerList().getPlayer(owner);if(p==null||!p.isAlive()||p.level()!=w){if(arena==null){cleanup();return;}WitchYard.fail(w,e);return;}if(e.radius(p.position())<=29)lastValid=w.getGameTime();if(w.getGameTime()-lastValid>240){if(arena==null){cleanup();return;}WitchYard.fail(w,e);return;}if(getHealth()<=390&&phase<2){phase=2;curseDelay=w.getGameTime()+200;}if(getHealth()<=180&&phase<3){phase=3;if(!rageUsed){rageUsed=true;entityData.set(VEIL,false);begin(WitchAction.RAGE);cue("witch_rage_01");message("boss_rage");}}
43:    if(w.getGameTime()>=veilEnd)entityData.set(VEIL,false);if(shadowEnd>0&&w.getGameTime()>=shadowEnd){clearClones();shadowEnd=0;cooldown("shadow",700);}bar.setProgress(getHealth()/600);for(var p1:w.getPlayers(p1->e.radius(p1.position())<=43))bar.addPlayer(p1);for(var p1:new ArrayList<>(bar.getPlayers()))if(e.radius(p1.position())>43)bar.removePlayer(p1);
57:   if(tickCount%8==0){double r=Math.sqrt(distanceToSqr(target));if(r<7){var away=position().subtract(target.position()).multiply(1,0,1).normalize();var at=position().add(away.scale(3));if(e.radius(at)<20)getNavigation().moveTo(at.x,at.y,at.z,1);}else if(r>13&&e.radius(target.position())<25)getNavigation().moveTo(target,1);}
58:   if(getDeltaMovement().horizontalDistanceSqr()>.0004&&tickCount%14==0)cue("cloth_step_01");if(tickCount%300==0&&ready("idle")){cooldown("idle",240+random.nextInt(121));cue("witch_whisper_01");}
62:  private void cast(WitchAction a,String key,int cd){cooldown(key,phase==3&&(a==WitchAction.FIRE||a==WitchAction.TRACE||a==WitchAction.LURE)?Math.round(cd*.85F):cd);begin(a);}
65:  case FIRE->{if(frame(5))cue("fire_charge_01");if(frame(11)&&p!=null){if(locked==null)locked=p.position().add(0,1,0).subtract(position().add(0,1.3,0)).normalize();boolean major=burst();for(int i=-1;i<=1;i++){if(i==0&&!major)continue;var bolt=MarinkaContent.BOLT.get().create(w);bolt.boss=getUUID();bolt.encounter=encounter;bolt.arena=arena;bolt.raw=i==0?17.5F:10;bolt.armingTicks=phase==3?11:9;bolt.reflectable=i==0;bolt.setOwner(this);bolt.moveTo(getX(),getY()+1.3,getZ());bolt.launch(locked.yRot((float)Math.toRadians(i*18)).scale(.65));w.addFreshEntity(bolt);}cue("fire_release_01");cooldown("heavy_safe",24);offensive++;}}
67:  case LURE->{if(t>=22&&t<38&&p!=null&&hasLineOfSight(p)&&distanceToSqr(p)<169){if(frame(22)){p.hurt(source(this,"lure"),7.32F);controlSafe=w.getGameTime()+25;cooldown("heavy_safe",25);cue("witch_whisper_01");offensive++;}Vec3 move=position().subtract(p.position()).multiply(1,0,1).normalize().scale(.15);if(w.noCollision(p,p.getBoundingBox().move(move))){p.setDeltaMovement(move);p.hurtMarked=true;}}}
68:  case SHADOW->{if(frame(1)){cue("shadow_dissolve_01");message("clone_warning");}if(frame(13)&&p!=null)spawnClones(w,p);}
70:  case BLINK->{if(frame(1))cue("witch_blink_01");if(frame(10)&&destination!=null&&validLanding(destination)){teleportTo(destination.x,destination.y,destination.z);cooldown("heavy_safe",12);}}
72:  case BIRD_FLIGHT->{if(destination!=null){double f=Math.clamp(t/26.0,0,1);Vec3 delta=destination.subtract(position());var next=position().add(delta.scale(1.0/Math.max(1,26-t))).add(0,t<13?.08:-.08,0);if(e.radius(next)<=21&&w.hasChunkAt(BlockPos.containing(next))&&w.noCollision(this,getBoundingBox().move(next.subtract(position()))))move(MoverType.SELF,next.subtract(position()));}if(t%10==0)cue("magpie_wings_01");}
75:  case HERD->{if(frame(14)){int count=w.getEntitiesOfClass(TurMinion.class,new AABB(e.center).inflate(32),a->getUUID().equals(a.boss)).size();for(int i=count;i<2;i++){var at=landing(p,8,11);if(at==null)continue;var tur=MarinkaContent.TUR.get().create(w);tur.boss=getUUID();tur.arena=arena;tur.owner=owner;tur.expires=w.getGameTime()+440;tur.moveTo(at.x,at.y,at.z);w.addFreshEntity(tur);}cue("enchanted_herd_01");}}
79:  private void rune(ServerLevel w,WitchYard.Entry e,Vec3 pos,int kind,int wait){var floor=BlockPos.containing(pos).below();if(!w.hasChunkAt(floor)||!w.getBlockState(floor).isCollisionShapeFullBlock(w,floor)||e.radius(pos)>25)return;var r=MarinkaContent.RUNE.get().create(w);r.boss=getUUID();r.arena=arena;r.kind(kind);r.waitTicks=wait;r.moveTo(floor.getX()+.5,floor.getY()+1.015,floor.getZ()+.5);w.addFreshEntity(r);}
82:  public boolean validLanding(Vec3 at){var e=yard();var w=level();var pos=BlockPos.containing(at);return e!=null&&e.radius(at)<=21&&w.hasChunkAt(pos)&&w.hasChunkAt(pos.above(3))&&w.getWorldBorder().isWithinBounds(pos)&&w.getFluidState(pos).isEmpty()&&w.getBlockState(pos.below()).isCollisionShapeFullBlock(w,pos.below())&&w.noCollision(new AABB(at.x-.34,at.y,at.z-.34,at.x+.34,at.y+1.96,at.z+.34));}
83:  private void spawnClones(ServerLevel w,ServerPlayer p){if(falseBody()||shadowEnd>0)return;var places=new ArrayList<Vec3>();double base=random.nextDouble()*Math.PI*2;for(int i=0;i<3;i++){var at=org.slavicmyths.bogatyr.BogatyrLedger.safe(w,p.position().add(Math.cos(base+i*2*Math.PI/3)*11,0,Math.sin(base+i*2*Math.PI/3)*11),.68,1.96,3);if(at==null||!validLanding(at)||at.distanceToSqr(p.position())<64||at.distanceToSqr(p.position())>196)return;places.add(at);}Collections.shuffle(places, new Random(random.nextLong()));shadowEnd=w.getGameTime()+360;Vec3 real=places.get(0);teleportTo(real.x,real.y,real.z);for(int i=1;i<3;i++){var c=MarinkaContent.MARINKA.get().create(w);c.parent=getUUID();c.owner=owner;c.arena=arena;c.encounter=encounter;c.debug=debug;c.active=true;c.shadowEnd=shadowEnd;c.start=start;c.phase=phase;c.cooldown("fire",20+random.nextInt(21));c.moveTo(places.get(i).x,places.get(i).y,places.get(i).z);w.addFreshEntity(c);}entityData.set(VEIL,false);begin(WitchAction.IDLE);cooldown("fire",20+random.nextInt(21));for(var at:places)w.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF,at.x,at.y+1,at.z,14,.4,.7,.4,.02);}
84:  private void clearClones(){if(level() instanceof ServerLevel w)for(var c:w.getEntitiesOfClass(MarinkaEntity.class,new AABB(BlockPos.containing(center())).inflate(40),m->getUUID().equals(m.parent)))c.dissolve();}
87:  @Override public boolean hurt(DamageSource s,float amount){if(action()==WitchAction.CLONE_BREAK||action()==WitchAction.DEFEAT)return false;if(action()==WitchAction.INTRO)return false;if(s.getEntity() instanceof TurMinion)return false;if(bird()&&action()==WitchAction.BIRD_FLIGHT){begin(WitchAction.BIRD_RETURN);}boolean hit=super.hurt(s,amount);if(hit&&ready("hurt")){cooldown("hurt",22);cue("witch_hurt_01");}return hit;}
88:  public void registeredHit(DamageSource s,float damage){if(damage<=0)return;if(s.getEntity() instanceof ServerPlayer p){var root=root();if(root!=null)root.contribution(p);}if(falseBody()){falseHp-=damage;if(falseHp<=0)dissolve();}}
90:  public void cleanup(){if(!(level() instanceof ServerLevel w))return;active=false;bar.removeAllPlayers();clearClones();var e=yard();if(e!=null){for(var a:w.getEntities((Entity)null,new AABB(e.center).inflate(43),a->a instanceof WitchBolt bolt&&getUUID().equals(bolt.boss)||a instanceof WitchRune rune&&getUUID().equals(rune.boss)||a instanceof TurMinion tur&&getUUID().equals(tur.boss)||a instanceof HexAnchor anchor&&getUUID().equals(anchor.boss)))a.discard();}if(isAlive())discard();}
91:  @Override public void die(DamageSource s){if(falseBody()){dissolve();return;}super.die(s);active=false;bar.removeAllPlayers();clearClones();var e=yard();if(arena!=null&&e!=null&&level() instanceof ServerLevel w){if(!debug&&!s.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY))WitchYard.get(w).victory(w,e,this);else WitchYard.fail(w,e);}cleanup();begin(WitchAction.DEFEAT);cue("witch_defeat_01");}
93:  @Override protected void dropAllDeathLoot(ServerLevel w,DamageSource s){}@Override protected void dropExperience(Entity cause){}
95:  public static DamageSource source(MarinkaEntity actor,String kind){var w=actor.level();var key=net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,MarinkaContent.id("marinka_"+kind));return new DamageSource(w.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(key),actor);}
```

## src/main/java/org/slavicmyths/marinka/MarinkaEvents.java

```java
6: /** Armor is still the engine's responsibility. No final damage floors or client claims. */
9:  @SubscribeEvent public static void incoming(LivingIncomingDamageEvent e){if(e.getEntity() instanceof ServerPlayer p&&e.getSource().getEntity() instanceof MarinkaEntity)p.getPersistentData().putFloat("WitchHpBefore",p.getHealth());}
10:  @SubscribeEvent public static void pre(LivingDamageEvent.Pre e){if(e.getEntity().level().isClientSide)return;if(e.getEntity() instanceof DobrynyaEntity h)e.setNewDamage(e.getNewDamage()*h.mitigation(e.getSource()));if(e.getEntity() instanceof MarinkaEntity m)e.setNewDamage(e.getNewDamage()*m.mitigation());if(e.getSource().getEntity() instanceof MarinkaEntity m&&m.falseBody())e.setNewDamage(e.getNewDamage()*.45F);}
11:  @SubscribeEvent public static void post(LivingDamageEvent.Post e){if(e.getEntity() instanceof MarinkaEntity m)m.registeredHit(e.getSource(),e.getNewDamage());if(e.getEntity() instanceof ServerPlayer p&&e.getSource().getEntity() instanceof MarinkaEntity m){var r=m.root();if(r!=null)r.contribution(p);if(m.debug)org.slf4j.LoggerFactory.getLogger("Boss172").info("[Boss172] target={} hit={} armor={} toughness={} hpBefore={} hpAfter={} raw={} final={} effects={}",p.getUUID(),e.getSource().getMsgId(),p.getArmorValue(),p.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS),p.getPersistentData().getFloat("WitchHpBefore"),p.getHealth(),e.getOriginalDamage(),e.getNewDamage(),p.getActiveEffects());}}
```

## src/main/java/org/slavicmyths/marinka/TurMinion.java

```java
7: import net.minecraft.world.damagesource.DamageSource;
8: /** Local herd, 35HP, 22-second lifetime, no ecological spawning or loot. */
12:  @Override public void tick(){super.tick();if(!(level() instanceof ServerLevel w))return;if(boss==null||!(w.getEntity(boss) instanceof MarinkaEntity m)||!m.active||w.getGameTime()>=expires){discard();return;}var p=w.getServer().getPlayerList().getPlayer(owner);if(p==null||m.yard()==null||m.yard().radius(position())>27){discard();return;}if(action()==WitchAction.BULL_CHARGE){if(age()==14&&edge(p)<1.7&&hasLineOfSight(p))p.hurt(damageSources().mobAttack(this),5);if(age()>=28)begin(WitchAction.IDLE);return;}if(tickCount%4==0){face(p);setTarget(p);if(edge(p)<1.7&&ready("attack")){cooldown("attack",30);begin(WitchAction.BULL_CHARGE);}else if(tickCount%8==0)getNavigation().moveTo(p,1);}}
13:  @Override protected void dropAllDeathLoot(ServerLevel w,DamageSource s){}@Override protected void dropExperience(Entity cause){}
```

## src/main/java/org/slavicmyths/marinka/WitchActor.java

```java
9: /** Bounded entity-local scheduler; no world polling and no client damage. */
14:     protected final Map<String,Long> cooldowns=new HashMap<>();
18:     @Override public boolean hurt(net.minecraft.world.damagesource.DamageSource source,float amount){boolean hit=super.hurt(source,amount);if(hit&&!level().isClientSide)entityData.set(FLINCH,amount>=12?2F:1F);return hit;}
23:     public boolean ready(String key){return level().getGameTime()>=cooldowns.getOrDefault(key,0L);}public void cooldown(String key,int ticks){cooldowns.put(key,level().getGameTime()+ticks);}
31:     public void bark(int line){if(!(level() instanceof ServerLevel w))return;WitchActor speaker=this;UUID owner=null;String who="dobrynya";if(this instanceof DobrynyaEntity h)owner=h.owner;else if(this instanceof MarinkaEntity m){var root=m.root();if(root==null)return;speaker=root;owner=m.owner;who="marinka";}if(owner==null||!speaker.ready("voice_line"))return;var p=w.getServer().getPlayerList().getPlayer(owner);if(p==null||p.level()!=w||distanceToSqr(p)>1024)return;speaker.cooldown("voice_line",140);p.sendSystemMessage(net.minecraft.network.chat.Component.translatable("dialogue.slavicmyths.172."+who+"_"+line));}
34:     @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putString("WitchAction",action().name());n.putInt("WitchElapsed",age());n.putInt("WitchLength",length());var c=new CompoundTag();cooldowns.forEach(c::putLong);n.put("WitchCooldowns",c);n.putInt("WitchSequence",sequence());}
35:     @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);var c=n.getCompound("WitchCooldowns");for(String k:c.getAllKeys())cooldowns.put(k,c.getLong(k));entityData.set(SEQUENCE,n.getInt("WitchSequence"));begin(WitchAction.IDLE);}
```

## src/main/java/org/slavicmyths/marinka/WitchBolt.java

```java
18:  @Override public void tick(){super.tick();if(!(level() instanceof ServerLevel w))return;var b=activeBoss();if(b==null||tickCount>80){discard();return;}if(tickCount<armingTicks&&!reflected)return;Vec3 from=position(),to=from.add(launch);if(!w.hasChunkAt(net.minecraft.core.BlockPos.containing(to))){discard();return;}var wall=w.clip(new net.minecraft.world.level.ClipContext(from,to,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,this));double first=wall.getType()==HitResult.Type.MISS?2:from.distanceToSqr(wall.getLocation());LivingEntity hit=null;for(var e:w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().expandTowards(launch).inflate(.25),e->e.isAlive()&&e!=getOwner()&&!(e instanceof HexAnchor)&&!(e instanceof TurMinion)&&(!(e instanceof MarinkaEntity)||reflected))){var contact=e.getBoundingBox().inflate(.15).clip(from,to);if(contact.isPresent()){double d=from.distanceToSqr(contact.get());if(d<first){first=d;hit=e;}}}
19:   if(hit!=null){if(reflected&&hit instanceof MarinkaEntity m){hit.hurt(hSource(),13);if(getOwner() instanceof DobrynyaEntity h)m.deflected(h);}else hit.hurt(MarinkaEntity.source(b,"fire"),raw);discard();return;}
21:  private net.minecraft.world.damagesource.DamageSource hSource(){return getOwner() instanceof LivingEntity h?h.damageSources().mobAttack(h):damageSources().magic();}
```

## src/main/java/org/slavicmyths/marinka/WitchRune.java

```java
17:   for(var target:w.getEntitiesOfClass(LivingEntity.class,new AABB(getX()-1.25,getY()-.3,getZ()-1.25,getX()+1.25,getY()+2.3,getZ()+1.25),e->e instanceof net.minecraft.world.entity.player.Player||e instanceof DobrynyaEntity)){if(!b.line(position().add(0,.4,0),target.position().add(0,.4,0)))continue;if(kind()!=2&&!hit.add(target.getUUID()))continue;if(kind()==2){var n=target.getPersistentData();if(n.getLong("WitchEmberLast")+20>w.getGameTime())continue;n.putLong("WitchEmberLast",w.getGameTime());target.hurt(target.damageSources().magic(),1);}else{target.hurt(MarinkaEntity.source(b,kind()==1?"rage":"trace"),kind()==1?17.5F:11.2F);if(kind()==0){target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS,80));target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,40));}}}}
```

## src/main/java/org/slavicmyths/military/GreyWolfStoryEvents.java

```java
9:  public static ServerPlayer owner(net.minecraft.world.damagesource.DamageSource s,ServerLevel w){if(s.getEntity() instanceof ServerPlayer p)return p;if(s.getEntity() instanceof org.slavicmyths.entity.WildlifeEntity wolf)return wolf.ringOwner();return null;}
10:  @SubscribeEvent public static void damaged(LivingDamageEvent.Post e){if(!(e.getEntity().level() instanceof ServerLevel w)||e.getNewDamage()<=0)return;var victim=e.getEntity();var p=owner(e.getSource(),w);if(p!=null){if(victim instanceof org.slavicmyths.kurgan.KurganCreature k&&k.kurgan!=null){var records=org.slavicmyths.kurgan.BurialRecords.get(w);var site=records.instances.get(k.kurgan);if(site!=null&&GreyWolfStory.mainBoss(site,k)&&site.bossContributors.add(p.getUUID()))records.setDirty();}var d=victim.getPersistentData();d.putUUID("WolfStoryKiller",p.getUUID());d.putLong("WolfStoryDamageAt",w.getGameTime());GreyPackRing.attacked(p,victim);}if(victim instanceof ServerPlayer player&&e.getSource().getEntity() instanceof LivingEntity attacker)GreyPackRing.attacked(player,attacker);}
11:  @SubscribeEvent(priority=EventPriority.LOWEST) public static void death(LivingDeathEvent e){if(!(e.getEntity().level() instanceof ServerLevel w))return;var victim=e.getEntity();var d=victim.getPersistentData();if(d.getBoolean("WolfStoryDeathCredited"))return;var p=owner(e.getSource(),w);if(p==null&&d.hasUUID("WolfStoryKiller")&&w.getGameTime()-d.getLong("WolfStoryDamageAt")<=200)p=(ServerPlayer)w.getPlayerByUUID(d.getUUID("WolfStoryKiller"));if(p!=null){d.putBoolean("WolfStoryDeathCredited",true);GreyWolfStory.killed(p,victim);}}
13:  @SubscribeEvent public static void logout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p){org.slavicmyths.wolf.WolfStoryNetwork.clear(p);GreyPackRing.data(p).remove("SpawnAt");}}
```

## src/main/java/org/slavicmyths/military/Military.java

```java
14:  public static final int PATROL_RADIUS=32, RESPONSE_RADIUS=48, LOST_TABLE_GRACE=1200, ACTIVE_LIMIT=3, RAID_SEARCH=224;
15:  public static final long RAID_COOLDOWN=48000;
```

## src/main/java/org/slavicmyths/military/MilitaryEvents.java

```java
18:  @SubscribeEvent public static void damage(LivingIncomingDamageEvent e){if(!(e.getEntity().level() instanceof ServerLevel l))return;var victim=e.getEntity();if(e.getSource().getEntity() instanceof Villager source&&Military.guard(source)&&!GuardBehavior.hostile(source,victim)){e.setCanceled(true);return;}if(victim instanceof Villager v&&Military.guard(v)&&v.getPersistentData().getLong("GuardBlockUntil")>=l.getGameTime()&&e.getSource().getSourcePosition()!=null){var from=e.getSource().getSourcePosition().subtract(v.position()).normalize();if(v.getLookAngle().dot(from)>.25)e.setAmount(e.getAmount()*.45F);}
19:   if(victim instanceof Villager&&e.getSource().getEntity() instanceof LivingEntity attacker)for(Villager guard:l.getEntitiesOfClass(Villager.class,victim.getBoundingBox().inflate(48),Military::guard)){if(!(attacker instanceof net.minecraft.world.entity.player.Player p)||( !p.isCreative()&&!p.isSpectator())){guard.getPersistentData().putUUID("GuardAggressor",attacker.getUUID());guard.getPersistentData().putLong("GuardAggressorUntil",l.getGameTime()+1200);}guard.setLastHurtByMob(attacker);}
21:  @SubscribeEvent public static void entity(EntityTickEvent.Post e){if(e.getEntity() instanceof BanditEntity b&&b.level() instanceof ServerLevel l){long now=l.getGameTime();if(Math.floorMod(now+b.getId(),400)==0)BanditRaids.get(l).discover(l,b);if(b.getPersistentData().getBoolean("MilitaryRaider")&&now%40==0&&b.getTarget()==null){var target=l.getEntitiesOfClass(LivingEntity.class,b.getBoundingBox().inflate(24),t->t instanceof Villager||t instanceof IronGolem).stream().min(java.util.Comparator.comparingDouble(b::distanceToSqr)).orElse(null);if(target!=null)b.engage(target);else b.getNavigation().moveTo(b.home.getX()+.5,b.home.getY(),b.home.getZ()+.5,1);}}}
```

## src/main/java/org/slavicmyths/mor/HigherRules.java

```java
5:  public static boolean phase2(float hp,float max){return max>0&&hp<=max*.5F;}
```

## src/main/java/org/slavicmyths/mor/MorBolt.java

```java
15:     private float damage=3;
19:     public void configureRoot(Mob owner){setOwner(owner);entityData.set(ROOT,true);damage=0;setNoGravity(true);}
21:     public void configure(Mob owner,boolean thread,float damage){setOwner(owner);entityData.set(THREAD,thread);this.damage=damage;}
24:     @Override protected void onHitEntity(EntityHitResult hit){if(!level().isClientSide&&hit.getEntity() instanceof LivingEntity e&&MorController.valid(e)&&getOwner() instanceof LivingEntity owner){if(e.getLastHurtByMob()==owner)e.invulnerableTime=Math.min(e.invulnerableTime,10);if(e.hurt(damageSources().mobProjectile(this,owner),damage)&&thread())MorController.hold(e,20);}}
27:     @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putBoolean("Thread",thread());n.putBoolean("Root",root());n.putFloat("Damage",damage);n.putInt("Age",tickCount);}
28:     @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);entityData.set(THREAD,n.getBoolean("Thread"));entityData.set(ROOT,n.getBoolean("Root"));damage=n.getFloat("Damage");tickCount=n.getInt("Age");}
```

## src/main/java/org/slavicmyths/mor/MorFlaskProjectile.java

```java
18:     @Override protected void onHit(HitResult hit){super.onHit(hit);if(level() instanceof ServerLevel server){Vec3 p=hit.getLocation();for(var mob:level().getEntitiesOfClass(LandSpiritEntity.class,new AABB(p,p).inflate(3),e->MorRules.flaskRange(e.getBoundingBox().getCenter().distanceToSqr(p))))mob.mor.apply(false);playSound(SoundEvents.SPLASH_POTION_BREAK,1,1);server.sendParticles(MorContent.SPARK.get(),p.x,p.y,p.z,12,.6,.4,.6,0);server.sendParticles(MorContent.ASH.get(),p.x,p.y,p.z,8,.5,.3,.5,.01);discard();}}
```

## src/main/java/org/slavicmyths/mor/MorRules.java

```java
8:     public static boolean clonesNeeded(boolean clone,boolean summoned,float hp,float max){return !clone&&!summoned&&max>0&&hp<=max*.5F;}
11:     public static boolean natural(boolean overworld,long dayTime,double spawnDistanceSquared,boolean allowedReason,int roll) {
12:         return overworld && dayTime/24000>=7 && spawnDistanceSquared>160*160 && allowedReason && roll<4;
14:     public static float health(float current,float oldMax,float newMax) {
20:     public static int cooldown(int skill){return switch(skill){case 1,8->240;case 2->360;case 3->400;case 4->220;case 5->260;case 6->320;case 7->180;default->0;};}
```

## src/main/java/org/slavicmyths/mor/MorZone.java

```java
17:  public int style(){return entityData.get(STYLE);}public boolean cloud(){return style()>=2;}public double radius(){return cloud()?5:3;}
20:  @Override public void tick(){super.tick();if(level().isClientSide){if(tickCount%4==0)for(int i=0;i<(cloud()?10:3);i++){double x=getX()+(random.nextDouble()*2-1)*radius(),z=getZ()+(random.nextDouble()*2-1)*radius();level().addParticle(style()%2==0?MorContent.ASH.get():net.minecraft.core.particles.ParticleTypes.SMOKE,x,getY()+random.nextDouble()*(cloud()?2:.25),z,0,.02,0);level().addParticle(MorContent.SPARK.get(),x,getY()+.1,z,0,.015,0);}return;}if(++age> HigherRules.life(cloud())){discard();return;}if(tickCount%10!=0)return;
22:   Set<UUID> present=new HashSet<>();for(Player p:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(radius(),cloud()?3:1,radius()),p->MorController.valid(p)&&p.distanceToSqr(getX(),p.getY(),getZ())<=radius()*radius()&&Math.abs(p.getY()-getY())<(cloud()?3:1.2)&& (source==null||!source.isAlliedTo(p)))){if(source instanceof LandSpiritEntity s&&!s.clearLineForMor(position().add(0,.3,0),p.position().add(0,.3,0)))continue;present.add(p.getUUID());int time=exposure.merge(p.getUUID(),10,Integer::sum);if(HigherRules.harms(cloud(),time)){p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,30,0));if(style()==3)p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,30,0));if(time%20==0)p.hurt(source instanceof LivingEntity l?damageSources().mobAttack(l):damageSources().magic(),cloud()?2:1);}}
```

## src/main/java/org/slavicmyths/morozko/FrostContent.java

```java
6: import net.minecraft.world.entity.ai.attributes.Attributes;
19:  static {for(String name:List.of("ambient","step","staff_hit","ice_bolt","blizzard_start","blizzard_loop","spike_telegraph","spike_break","breath","wave","clone_spawn","clone_break","frost_damage","yield","mirror_summon","mirror_break","staff_shoot","staff_stream","staff_icicles","staff_wave","trial_call"))SOUNDS.put(name,ModSounds.SOUNDS.register("morozko."+name,()->SoundEvent.createFixedRangeEvent(id("morozko."+name),name.contains("blizzard")||name.equals("yield")?32:24)));}
20:  public static void init(net.neoforged.bus.api.IEventBus bus){bus.addListener(FrostNetwork::register);bus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent e)->{e.put(BOSS.get(),Morozko.attributes().build());e.put(DOUBLE.get(),Morozko.attributes().add(Attributes.MAX_HEALTH,FrostRules.DOUBLE_HP).build());e.put(MIRROR_ENTITY.get(),net.minecraft.world.entity.PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH,FrostRules.MIRROR_HP).add(Attributes.MOVEMENT_SPEED,.3).add(Attributes.ATTACK_DAMAGE,1).build());});}
```

## src/main/java/org/slavicmyths/morozko/FrostEvents.java

```java
12:  @SubscribeEvent public static void mirrorFriendly(LivingIncomingDamageEvent e){if(e.getSource().getEntity() instanceof PlayerDouble c&&!c.allowedTarget(e.getEntity()))e.setCanceled(true);}
13:  @SubscribeEvent(priority=EventPriority.LOWEST) public static void clamp(LivingDamageEvent.Pre e){if(e.getEntity() instanceof Morozko b&&!(b instanceof IceDouble)&&b.fighting())e.setNewDamage(FrostRules.clampedDamage(b.getHealth(),b.getMaxHealth(),e.getNewDamage()));}
14:  @SubscribeEvent public static void damage(LivingDamageEvent.Post e){if(e.getNewDamage()<=0||e.getEntity().level().isClientSide)return;var from=e.getSource().getEntity();var target=e.getEntity();if(target instanceof Morozko boss&&boss.fighting()&&from!=null&&FrostWorld.owner(from)!=null&&from.position().distanceToSqr(boss.anchor)<=1600)boss.credit(FrostWorld.owner(from),e.getNewDamage());if(from instanceof PlayerDouble c&&e.getSource().getDirectEntity() instanceof net.minecraft.world.entity.projectile.AbstractArrow)proc(c,target);if(from instanceof ServerPlayer p&&e.getSource().getDirectEntity()==p&&!e.getSource().getMsgId().equals("morozko_stream")){proc(p,target);var clone=FrostSkills.copy(p);if(clone!=null){var n=p.getPersistentData();int seq=n.getInt("FrostAttackSequence")+1;n.putInt("FrostAttackSequence",seq);clone.mirrorHit(target,seq,false);}}}
15:  public static void proc(LivingEntity e,LivingEntity t){if(!org.slavicmyths.rpg.Runes.has(e.getMainHandItem(),"frost")||!FrostWorld.hostile(t,e))return;var n=e.getPersistentData();long now=e.level().getGameTime();if(now<n.getLong("FrostRuneReady"))return;n.putLong("FrostRuneReady",now+12);if(e.getRandom().nextFloat()<.25f){t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,80));t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,80));if(t.getPersistentData().contains(FrostCold.KEY))FrostCold.add(t,10);}}
18:  @SubscribeEvent public static void attack(AttackEntityEvent e){if(e.getEntity() instanceof ServerPlayer p){if(p.getMainHandItem().is(FrostContent.STAFF.get())){e.setCanceled(true);FrostSkills.action(p,0);}else{p.getPersistentData().putFloat("FrostAttackScale",p.getAttackStrengthScale(.5f));p.getPersistentData().putBoolean("FrostCritical",p.fallDistance>0&&!p.onGround()&&!p.onClimbable()&&!p.isInWater()&&!p.isPassenger()&&!p.isSprinting()&&!p.hasEffect(MobEffects.BLINDNESS));}}}
19:  @SubscribeEvent public static void projectile(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent e){if(!(e.getEntity() instanceof net.minecraft.world.entity.projectile.AbstractArrow arrow)||!(arrow.getOwner() instanceof ServerPlayer p))return;var c=FrostSkills.copy(p);if(c==null)return;var from=p.getEyePosition();var to=from.add(p.getLookAngle().scale(24));var target=p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().expandTowards(p.getLookAngle().scale(24)).inflate(1),t->c.allowedTarget(t)&&t.getBoundingBox().inflate(.3).clip(from,to).isPresent()&&p.hasLineOfSight(t)).stream().min(java.util.Comparator.comparingDouble(t->FrostWorld.bboxDistance(from,t.getBoundingBox()))).orElse(null);if(target!=null){int seq=p.getPersistentData().getInt("FrostAttackSequence")+1;p.getPersistentData().putInt("FrostAttackSequence",seq);c.ranged(arrow);c.mirrorHit(target,seq,true);}}
```

## src/main/java/org/slavicmyths/morozko/FrostHazard.java

```java
11:  private static final EntityDataAccessor<Float> RADIUS=SynchedEntityData.defineId(FrostHazard.class,EntityDataSerializers.FLOAT);
12:  private UUID owner;public UUID session;private float damage;private int cold;private final Set<UUID> hit=new HashSet<>();private double groundY;
14:  @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(KIND,0);b.define(DELAY,18);b.define(AGE,0);b.define(RADIUS,7f);}
15:  public int kind(){return entityData.get(KIND);}public int delay(){return entityData.get(DELAY);}public int age(){return entityData.get(AGE);}public float radius(){return entityData.get(RADIUS);}
16:  private static FrostHazard create(LivingEntity from,Vec3 p,int kind,int delay,float radius,float damage,int cold,UUID session){var h=FrostContent.HAZARD.get().create(from.level());if(h==null)return null;h.owner=from.getUUID();h.session=session;h.damage=damage;h.cold=cold;h.entityData.set(KIND,kind);h.entityData.set(DELAY,delay);h.entityData.set(RADIUS,radius);h.setPos(p);h.groundY=p.y;from.level().addFreshEntity(h);return h;}
17:  public static void spike(LivingEntity from,Vec3 p,int delay,float damage,int cold,UUID session){create(from,p,0,delay,1,damage,cold,session);}
18:  public static void wave(LivingEntity from,float radius,float damage,int cold,UUID session){create(from,from.position(),2,0,radius,damage,cold,session);}
22:  if(a==0)FrostContent.sound(this,kind()==1?"staff_icicles":kind()==2?"wave":"spike_break");AABB box=getBoundingBox();if(kind()==1){var old=position();var next=old.add(0,-.8,0);var clip=w.clip(new net.minecraft.world.level.ClipContext(old,next,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,this));if(clip.getType()!=HitResult.Type.MISS){FrostVisuals.burst(w,clip.getLocation(),12);discard();return;}setPos(next);box=getBoundingBox().minmax(new AABB(old,next).inflate(.3));}else if(kind()==0){if(!FrostRules.spikeActive(a)){if(a>40)discard();return;}box=new AABB(getX()-.5,getY(),getZ()-.5,getX()+.5,getY()+2.5,getZ()+.5);}else{double r=2+(radius()-2)*Math.min(1,a/18.0);if(a>18){discard();return;}if(tickCount%3==0)FrostVisuals.ring(w,position(),r,.15,24);box=getBoundingBox().inflate(r,1,r);}
23:  for(var t:w.getEntitiesOfClass(LivingEntity.class,box,e->(caster instanceof Morozko b?b.candidate(e):FrostWorld.hostile(e,caster))&&!hit.contains(e.getUUID())&&caster.hasLineOfSight(e))){if(kind()==2){double r=2+(radius()-2)*Math.min(1,a/18.0);double dist=Math.sqrt(FrostWorld.bboxDistance(position(),t.getBoundingBox()));if(!t.onGround()||dist<r-.8||dist>r+.8)continue;}hit.add(t.getUUID());if(t.hurt(FrostDamage.projectile(kind()==1?"morozko_icicle_damage":"morozko_frost",this,caster),damage)){FrostCold.add(t,cold);if(caster instanceof Morozko b)b.attacked(t);if(kind()==1){t.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS,60));t.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,60));}if(kind()==2)t.knockback(.7,getX()-t.getX(),getZ()-t.getZ());}if(kind()==1){FrostVisuals.burst(w,position(),12);discard();break;}}
```

## src/main/java/org/slavicmyths/morozko/FrostRules.java

```java
3: /** Pure policy: HP, timers and admission shared by the server and offline tests. */
10:  public static float clampedDamage(float hp,float max,float incoming){return Math.max(0,Math.min(incoming,hp-floor(max)));}
12:  public static int phase(float hp,float max){return hp<=floor(max)?4:hp<max*.3f?3:hp<max*.6f?2:1;}
15:  public static boolean eligible(double damage,int activeTicks,boolean attacked){return damage>=1||activeTicks>=300&&attacked;}
```

## src/main/java/org/slavicmyths/morozko/FrostShard.java

```java
8:  public UUID session;private float damage;private int cold;
12:  public static void launch(LivingEntity owner,Vec3 target,float damage,int cold,UUID session){var e=FrostContent.SHARD.get().create(owner.level());if(e==null)return;e.setOwner(owner);e.session=session;e.damage=damage;e.cold=cold;if(owner instanceof Morozko){double yaw=Math.toRadians(owner.getYRot());var side=new Vec3(Math.cos(yaw)*.875,0,Math.sin(yaw)*.875);e.setPos(owner.position().add(side).add(0,2.17,0).add(owner.getLookAngle().multiply(1,0,1).normalize().scale(1)));}else e.setPos(owner.getEyePosition().add(owner.getLookAngle().scale(.8)));var d=target.subtract(e.position()).normalize();e.shoot(d.x,d.y,d.z,1,0);owner.level().addFreshEntity(e);}
14:  @Override protected void onHitEntity(EntityHitResult h){if(level().isClientSide||!(h.getEntity() instanceof LivingEntity t)||!(getOwner() instanceof LivingEntity from))return;if(t.hurt(FrostDamage.projectile("morozko_frost",this,from),damage)){FrostCold.add(t,cold);if(from instanceof Morozko b)b.attacked(t);if(from instanceof net.minecraft.server.level.ServerPlayer p)FrostSkills.remember(p,t);}}
```

## src/main/java/org/slavicmyths/morozko/IceDouble.java

```java
11:  @Override public void credit(java.util.UUID p,float damage){var b=boss();if(b!=null)b.credit(p,damage);}
13:  @Override public boolean hurt(net.minecraft.world.damagesource.DamageSource source,float amount){return fighting()&&super.hurt(source,amount);}
```

## src/main/java/org/slavicmyths/morozko/Morozko.java

```java
10: import net.minecraft.world.entity.ai.attributes.*;
11: import net.minecraft.world.damagesource.DamageSource;
20:  public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,FrostRules.BOSS_HP).add(Attributes.MOVEMENT_SPEED,.22).add(Attributes.ARMOR,8).add(Attributes.ATTACK_DAMAGE,FrostRules.MELEE).add(Attributes.FOLLOW_RANGE,28).add(Attributes.KNOCKBACK_RESISTANCE,.8);}
24:  protected void action(int a){entityData.set(ACTION,a);entityData.set(AGE,0);getNavigation().stop();if(a>0){String cue=switch(a){case 1,2->null;case 3->"blizzard_start";case 4->"spike_telegraph";case 5->"breath";case 6->"wave";case 7->"clone_spawn";case 8->"blizzard_start";default->"yield";};if(cue!=null)FrostContent.sound(this,cue);}}
27:  public void credit(UUID id,float damage){if(id==null||!(level() instanceof ServerLevel w)||session==null)return;var d=FrostLedger.get(w);var s=d.session(session);var n=s.getCompound("Participants");var row=n.getCompound(id.toString());row.putDouble("Damage",row.getDouble("Damage")+damage);n.put(id.toString(),row);s.put("Participants",n);d.setDirty();}
29:  protected List<LivingEntity> targets(){return level().getEntitiesOfClass(LivingEntity.class,new net.minecraft.world.phys.AABB(anchor,anchor).inflate(40),this::candidate);}
67:  protected void hit(LivingEntity e,float damage,int cold){if(!candidate(e))return;if(e.hurt(org.slavicmyths.combat.MythDamageSources.caused("morozko_frost",this),damage)){FrostCold.add(e,cold);attacked(e);}}
69:  protected void cleanup(){if(phase()<4||phase()>=6||isRemoved())bar.removeAllPlayers();if(level() instanceof ServerLevel w){for(var id:children){var e=w.getEntity(id);if(e instanceof IceDouble c)c.dissolve();else if(e!=null)e.discard();}children.clear();for(var e:w.getEntitiesOfClass(Entity.class,getBoundingBox().inflate(80),e->e instanceof FrostShard s&&Objects.equals(s.session,session)||e instanceof FrostHazard h&&Objects.equals(h.session,session)||e instanceof IceDouble c&&Objects.equals(c.session,session))){if(e instanceof IceDouble c)c.dissolve();else e.discard();}for(var e:w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(40),e->FrostWorld.owner(e)!=null))FrostCold.stop(e);}}
70:  @Override public boolean hurt(DamageSource source,float amount){if(!(this instanceof IceDouble)&&source.is(net.minecraft.world.damagesource.DamageTypes.GENERIC_KILL)&&level() instanceof ServerLevel w){if(session!=null){FrostLedger.get(w).session(session).putBoolean("Cancelled",true);FrostLedger.get(w).setDirty();}discard();return true;}if(!(this instanceof IceDouble)&&!fighting()||source.getEntity() instanceof Morozko||source.is(net.minecraft.tags.DamageTypeTags.IS_FREEZING))return false;return super.hurt(source,amount);}
72:  /** Last line of defence after other damage subscribers; vanilla never sees lethal health. */
73:  @Override public void setHealth(float hp){if(!(this instanceof IceDouble)&&session!=null&&phase()>0&&phase()<6)hp=Math.max(hp,FrostRules.floor(getMaxHealth()));super.setHealth(hp);}
75:  @Override public void die(DamageSource s){if(this instanceof IceDouble){FrostContent.sound(this,"clone_break");super.die(s);}else if(level() instanceof ServerLevel)yieldTrial();}
76:  @Override protected void dropAllDeathLoot(ServerLevel w,DamageSource s){}
```

## src/main/java/org/slavicmyths/morozko/PlayerDouble.java

```java
8: import net.minecraft.world.damagesource.DamageSource;
15:  private int life,sequence;private float snapshotDamage;private int meleeInterval=13;private double rangedBase=2,rangedSpeed=2;private boolean rangedCritical;private final java.util.Set<java.util.UUID> arrows=new java.util.HashSet<>();private long lastMelee;private boolean restored;
22:  public void configure(ServerPlayer p){entityData.set(OWNER,Optional.of(p.getUUID()));entityData.set(OWNER_NAME,p.getName().getString());entityData.set(LEFT,p.getMainArm()==net.minecraft.world.entity.HumanoidArm.LEFT);for(var slot:EquipmentSlot.values()){var snapshot=p.getItemBySlot(slot).copy();boolean glint=snapshot.hasFoil();EnchantmentHelper.updateEnchantments(snapshot,m->m.removeIf(h->h.is(net.minecraft.world.item.enchantment.Enchantments.LOOTING)));if(glint&&!snapshot.hasFoil())snapshot.set(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE,true);setItemSlot(slot,snapshot);}snapshotDamage=(float)p.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);meleeInterval=Math.max(1,(int)Math.ceil(20/p.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED)));setHealth(FrostRules.MIRROR_HP);}
25:  public void mirrorHit(LivingEntity target,int attackSequence,boolean ranged){if(attackSequence<=sequence||life<13)return;sequence=attackSequence;var p=player();if(p==null||!target.isAlive()||target==p||!allowedTarget(target))return;getLookControl().setLookAt(target,180,180);setYRot((float)(Math.atan2(target.getZ()-getZ(),target.getX()-getX())*180/Math.PI)-90);swing(net.minecraft.world.InteractionHand.MAIN_HAND,true);if(!hasLineOfSight(target))return;long now=level().getGameTime();if(now-lastMelee<meleeInterval)return;lastMelee=now;if(ranged){if(FrostWorld.bboxDistance(getEyePosition(),target.getBoundingBox())>24*24)return;net.minecraft.world.entity.projectile.AbstractArrow arrow;if(getMainHandItem().getItem() instanceof org.slavicmyths.combat.ThrowingWeaponItem weapon){arrow=new org.slavicmyths.combat.WeaponProjectile(level(),this,getMainHandItem().copy(),weapon.knife,false);}else if(getMainHandItem().getItem() instanceof TridentItem){arrow=new net.minecraft.world.entity.projectile.ThrownTrident(level(),this,getMainHandItem().copy());}else if(getMainHandItem().getItem() instanceof BowItem||getMainHandItem().getItem() instanceof CrossbowItem){arrow=new net.minecraft.world.entity.projectile.Arrow(level(),this,new ItemStack(Items.ARROW),getMainHandItem());}else return;arrow.pickup=net.minecraft.world.entity.projectile.AbstractArrow.Pickup.DISALLOWED;arrow.setBaseDamage(rangedBase);arrow.setCritArrow(rangedCritical);var aim=target.getBoundingBox().getCenter().subtract(arrow.position());arrow.shoot(aim.x,aim.y+aim.horizontalDistance()*.04,aim.z,(float)rangedSpeed,0);level().addFreshEntity(arrow);arrows.add(arrow.getUUID());return;}if(FrostWorld.bboxDistance(getEyePosition(),target.getBoundingBox())>9)return;var source=org.slavicmyths.combat.MythDamageSources.caused("morozko_mirror",this);float scale=Math.clamp(p.getPersistentData().getFloat("FrostAttackScale"),0,1);float damage=EnchantmentHelper.modifyDamage((ServerLevel)level(),getMainHandItem(),target,source,snapshotDamage*(.2f+scale*scale*.8f));if(p.getPersistentData().getBoolean("FrostCritical")&&scale>.9f)damage*=1.5f;if(target.hurt(source,damage)){EnchantmentHelper.doPostAttackEffectsWithItemSource((ServerLevel)level(),target,source,getMainHandItem());FrostEvents.proc(this,target);}}
26:  public void ranged(net.minecraft.world.entity.projectile.AbstractArrow original){rangedBase=original.getBaseDamage();rangedSpeed=Math.max(.1,original.getDeltaMovement().length());rangedCritical=original.isCritArrow();}
29:  @Override public void die(DamageSource s){shatter();}
30:  @Override protected void dropAllDeathLoot(ServerLevel w,DamageSource s){}
```

## src/main/java/org/slavicmyths/navigation/NavigationEvents.java

```java
18: import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
33:     @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){if(e.getEntity() instanceof ServerPlayer p)NavigationNetwork.sync(p,false);}
52:     @SubscribeEvent public static void attacked(LivingIncomingDamageEvent e){
106:             boolean inside=area.distance(p.getX(),p.getZ())<=(wasInside?area.radius()+12:area.radius()-4);
162:                 double y=Math.max(box.minY(),Math.min(p.getY(),box.maxY()));int radius=major?96:path.contains("medium")||path.contains("warrior")?64:48;
163:                 if(Math.pow(p.getX()-x,2)+Math.pow(p.getY()-y,2)+Math.pow(p.getZ()-z,2)>radius*radius)continue;
```

## src/main/java/org/slavicmyths/navigation/SlavicMarker.java

```java
11:     public enum Kind { PERMANENT_DISCOVERY,TEMPORARY_QUEST,TEMPORARY_EVENT,SEARCH_AREA,TRAVEL_POINT,TRACKED_ONLY }
12:     public enum Lifetime { PERMANENT,UNTIL_QUEST_END,UNTIL_EVENT_END,UNTIL_DISCOVERED }
28:             a.putInt("Radius",area.radius());a.putString("State",area.state().name());n.put("Area",a);}
34:         if(n.contains("Area",10)){CompoundTag a=n.getCompound("Area");area=new SearchArea(a.getDouble("CenterX"),a.getDouble("CenterZ"),a.getInt("Radius"),SearchArea.State.valueOf(a.getString("State")));}
```

## src/main/java/org/slavicmyths/nochnitsa/NochnitsaContent.java

```java
7: import net.minecraft.world.entity.ai.attributes.Attributes;
24:     public static final DeferredHolder<Item,Item> EGG=ModItems.ITEMS.register("nochnitsa_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(ENTITY,0x343b50,0xdce5f6,new Item.Properties()));
30:     private static final DeferredHolder<MapCodec<? extends BiomeModifier>,MapCodec<SpawnModifier>> SPAWNS=MODIFIERS.register("nochnitsa_spawns",()->MapCodec.unit(SpawnModifier::new));
34:     public static final class SpawnModifier implements BiomeModifier{
35:         public MapCodec<? extends BiomeModifier> codec(){return SPAWNS.get();}
36:         public void modify(net.minecraft.core.Holder<Biome> biome,Phase phase,ModifiableBiomeInfo.BiomeInfo.Builder builder){if(phase==Phase.ADD&&biome.is(BIOMES)&&NochnitsaConfig.spawnWeight>0)builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER,new net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData(ENTITY.get(),NochnitsaConfig.spawnWeight,1,1));}
38:     @SubscribeEvent public static void attributes(EntityAttributeCreationEvent e){e.put(ENTITY.get(),Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,50).add(Attributes.ARMOR,0).add(Attributes.ATTACK_DAMAGE,6).add(Attributes.MOVEMENT_SPEED,.26).add(Attributes.FOLLOW_RANGE,24).add(Attributes.KNOCKBACK_RESISTANCE,.05).build());}
39:     @SubscribeEvent public static void placements(RegisterSpawnPlacementsEvent e){e.register(ENTITY.get(),SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(type,w,reason,p,r)->{
40:         if(reason==MobSpawnType.SPAWN_EGG||reason==MobSpawnType.COMMAND)return true;
41:         if(w.getLevel().dimension()!=Level.OVERWORLD||!NochnitsaRules.night(w.getLevel().getDayTime())||!w.canSeeSky(p)||!w.getBiome(p).is(BIOMES)||!w.getLevel().getWorldBorder().isWithinBounds(p)||!w.getFluidState(p).isEmpty()||!Monster.checkMonsterSpawnRules(type,w,reason,p,r))return false;
42:         if(!w.getLevel().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBSPAWNING))return false;
43:         return w.getLevel().getEntitiesOfClass(NochnitsaEntity.class,new net.minecraft.world.phys.AABB(p).inflate(48),n->n.isAlive()&&n.distanceToSqr(p.getX()+.5,p.getY(),p.getZ()+.5)<=48*48).isEmpty();
44:     },RegisterSpawnPlacementsEvent.Operation.REPLACE);}
```

## src/main/java/org/slavicmyths/nochnitsa/NochnitsaEntity.java

```java
12: import net.minecraft.world.damagesource.DamageSource;
50:         if(clock.state==State.PURSUE&&target!=null){if(distanceToSqr(target)<=2.15*2.15&&hasLineOfSight(target)&&clock.attackCooldown==0){clock.claw();sync();getNavigation().stop();}else if(clock.mayScream()&&seenTicks>=20&&distanceToSqr(target)>2.5*2.5&&mayScreamAt(target)){charging=target.getUUID();begin(State.WINDUP);debug("scream_start");}}
55:         State before=clock.state;Event event=clock.tick();if(event==Event.INHALE)cue("scream_inhale",.9F);if(event==Event.HIT&&valid(target)&&distanceToSqr(target)<=2.15*2.15&&hasLineOfSight(target))target.hurt(damageSources().mobAttack(this),6);
56:         if(event==Event.SCREAM){if(charging!=null&&target!=null&&charging.equals(target.getUUID())&&mayScreamAt(target)){NochnitsaHorde.release(this,target);cue("scream_release",2);level().broadcastEntityEvent(this,(byte)61);}else{clock.cooldown=NochnitsaRules.RETRY;clock.begin(State.PURSUE);}charging=null;}
60:     @Override public boolean hurt(DamageSource source,float amount){boolean accepted=super.hurt(source,amount);if(accepted&&isAlive()&&!level().isClientSide&&clock.state!=State.DAWN){boolean interrupted=clock.hurt();charging=null;sync();getNavigation().stop();cue(interrupted?"scream_interrupt":"hurt",.7F);if(interrupted)debug("scream_abort damage");}return accepted;}
61:     @Override public void die(DamageSource source){begin(State.DEATH);cue("death",.8F);super.die(source);}
63:     @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putInt("NochnitsaScreamCooldown",clock.cooldown);n.putInt("NochnitsaAttackCooldown",clock.attackCooldown);n.putBoolean("NochnitsaLeft",clock.left);if(debugNight())n.putBoolean("NochnitsaDebugNightOverride",true);}
64:     @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);clock.cooldown=Math.clamp(n.getInt("NochnitsaScreamCooldown"),0,600);clock.attackCooldown=Math.clamp(n.getInt("NochnitsaAttackCooldown"),0,24);clock.left=!n.contains("NochnitsaLeft")||n.getBoolean("NochnitsaLeft");nightOverride=NochnitsaConfig.debug&&n.getBoolean("NochnitsaDebugNightOverride");clock.cooldown=Math.max(clock.cooldown,NochnitsaRules.RETRY);clock.begin(State.WANDER);tracked=charging=null;sync();}
```

## src/main/java/org/slavicmyths/nochnitsa/NochnitsaHorde.java

```java
23:     public static boolean species(Mob m){return m instanceof Monster&&!(m instanceof NochnitsaEntity)&&!(m instanceof org.slavicmyths.underground.RoomBoss)&&!(m instanceof org.slavicmyths.industry.ChudPerson)&&!m.getType().is(OminousAura.IMMUNE)&&m.getMaxHealth()<=50&&(m instanceof Zombie||m instanceof AbstractSkeleton||m instanceof Spider||m instanceof Witch||m instanceof Creeper||m instanceof AbstractIllager||m.getType().is(RECRUITABLE));}
32:         var candidates=w.getEntitiesOfClass(Monster.class,source.getBoundingBox().inflate(32),m->m!=source&&NochnitsaRules.inRadius(m.distanceToSqr(source)));
```

## src/main/java/org/slavicmyths/nochnitsa/NochnitsaRules.java

```java
3: /** Fixed gameplay contract. HP are vanilla half-hearts. Pure clock used by the live entity. */
5:     public static final int WINDUP=30, RELEASE=12, COOLDOWN=600, RETRY=60, ALERT_TTL=240, WRITE_GAP=40, CAP=48;
6:     public enum State { WANDER, SPOT, PURSUE, CLAW_LEFT, CLAW_RIGHT, WINDUP, RELEASE, HURT, DAWN, DEATH, HEAD_SNAP }
7:     public enum Event { NONE, HIT, INHALE, SCREAM, DAWN_REMOVE, DEATH_REMOVE }
9:     public static boolean inRadius(double squared){return squared<=32*32;}
11:         public State state=State.WANDER;public int age,cooldown,attackCooldown;public boolean left=true;
14:         public boolean mayScream(){return state==State.PURSUE&&cooldown==0;}
15:         public void claw(){if(attackCooldown==0){begin(left?State.CLAW_LEFT:State.CLAW_RIGHT);left=!left;attackCooldown=24;}}
16:         public void abort(){if(state==State.WINDUP){cooldown=Math.max(cooldown,RETRY);begin(State.PURSUE);}}
17:         public boolean hurt(){boolean interrupted=state==State.WINDUP;if(interrupted)cooldown=Math.max(cooldown,RETRY);if(state!=State.DAWN&&state!=State.DEATH)begin(State.HURT);return interrupted;}
18:         public Event tick(){cooldown=Math.max(0,cooldown-1);attackCooldown=Math.max(0,attackCooldown-1);age++;
20:                 case WINDUP->{if(age==10)yield Event.INHALE;if(age==WINDUP){cooldown=COOLDOWN;begin(State.RELEASE);yield Event.SCREAM;}yield Event.NONE;}
```

## src/main/java/org/slavicmyths/pesigolovtsy/DogheadArrow.java

```java
10:  public DogheadArrow(Level w,DogheadEntity owner,UUID pack){this(DogheadContent.ARROW.get(),w);setOwner(owner);this.pack=pack;setBaseDamage(2.7);}
```

## src/main/java/org/slavicmyths/pesigolovtsy/DogheadContent.java

```java
8: import net.minecraft.world.entity.ai.attributes.Attributes;
32:  static{for(var role:Role.values()){EGGS.add(ModItems.ITEMS.register("pesigolovets_"+role.id()+"_spawn_egg",()->{var n=new CompoundTag();n.putString("id","slavicmyths:pesigolovets");n.putString("DogheadRole",role.name());n.putFloat("Health",role.hp);return new net.neoforged.neoforge.common.DeferredSpawnEggItem(ENTITY,role==Role.LEADER?0x2b2928:role==Role.SCOUT?0x514b49:0x796358,role==Role.SCOUT?0x3f322b:0x843b30,new Item.Properties().component(DataComponents.ENTITY_DATA,CustomData.of(n)));}));}
40:  private static final DeferredHolder<MapCodec<? extends BiomeModifier>,MapCodec<SpawnModifier>> SPAWNS=MODIFIERS.register("doghead_spawns",()->MapCodec.unit(SpawnModifier::new));
42:  public static final class SpawnModifier implements BiomeModifier{public MapCodec<? extends BiomeModifier> codec(){return SPAWNS.get();}public void modify(net.minecraft.core.Holder<Biome> biome,Phase phase,ModifiableBiomeInfo.BiomeInfo.Builder b){if(phase==Phase.ADD&&biome.is(BIOMES)&&DogheadConfig.weight>0)b.getMobSpawnSettings().addSpawn(MobCategory.MONSTER,new MobSpawnSettings.SpawnerData(ENTITY.get(),DogheadConfig.weight,3,6));}}
43:  @SubscribeEvent public static void attributes(EntityAttributeCreationEvent e){e.put(ENTITY.get(),Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,40).add(Attributes.ARMOR,0).add(Attributes.ATTACK_DAMAGE,6).add(Attributes.MOVEMENT_SPEED,.26).add(Attributes.FOLLOW_RANGE,28).build());}
44:  @SubscribeEvent public static void placements(RegisterSpawnPlacementsEvent e){e.register(ENTITY.get(),SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(type,w,reason,p,r)->{
45:   if(reason==MobSpawnType.SPAWN_EGG||reason==MobSpawnType.COMMAND)return true;
46:   if(w.getLevel().dimension()!=Level.OVERWORLD||!w.getBiome(p).is(BIOMES)||w.getLevel().isCloseToVillage(p,2)||p.distToLowCornerSqr(w.getLevel().getSharedSpawnPos().getX(),p.getY(),w.getLevel().getSharedSpawnPos().getZ())<96*96||!w.getLevel().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBSPAWNING)||!w.getLevel().getWorldBorder().isWithinBounds(p)||!w.getFluidState(p).isEmpty()||p.getY()<w.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,p.getX(),p.getZ())-2)return false;
48:   return Monster.checkMonsterSpawnRules(type,w,reason,p,r);
49:  },RegisterSpawnPlacementsEvent.Operation.REPLACE);}
```

## src/main/java/org/slavicmyths/pesigolovtsy/DogheadEntity.java

```java
10: import net.minecraft.world.entity.ai.attributes.Attributes;
17: import net.minecraft.world.damagesource.DamageSource;
28:  public UUID pack=UUID.randomUUID();public boolean spawnRejected;private boolean founder;private int naturalCheck,ambientIn=300,preyCheck=200,blockedTicks;private long attackAfter,sweepAfter,howlAfter,switchAfter,dodgeAfter,preyAfter,interestUntil,seenAt,replyAt,retreatUntil,approachUntil;private UUID tracked;private Vec3 lastSeen,slot,formationTarget;private float attackYaw,stepPhase;private Vec3 stepPosition;
33:  public void role(Role r,boolean heal){entityData.set(ROLE,r.ordinal());getAttribute(Attributes.MAX_HEALTH).setBaseValue(r.hp);getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(r.speed);getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(r.sight);xpReward=r==Role.SCOUT?6:r.xp;if(heal)setHealth(r.hp);else setHealth(Math.min(getHealth(),r.hp));refreshDimensions();}
45:  private static final class NaturalGroup implements SpawnGroupData{final UUID id=UUID.randomUUID();int count;boolean rejected;Vec3 anchor;final boolean leader;NaturalGroup(boolean leader){this.leader=leader;}}
46:  @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor w,DifficultyInstance difficulty,MobSpawnType reason,SpawnGroupData data){super.finalizeSpawn(w,difficulty,reason,data);if(reason==MobSpawnType.NATURAL||reason==MobSpawnType.CHUNK_GENERATION){NaturalGroup g=data instanceof NaturalGroup n?n:new NaturalGroup(random.nextDouble()<DogheadConfig.leaderChance);if(g.count==0){founder=true;g.anchor=position();g.rejected=!w.getLevel().getEntitiesOfClass(DogheadEntity.class,getBoundingBox().inflate(96),m->m.isAlive()&&m.distanceToSqr(this)<=96*96).isEmpty();}spawnRejected=g.rejected;pack=g.id;role(g.count==0?Role.SCOUT:g.count==3&&g.leader?Role.LEADER:Role.HUNTER,true);if(g.count>0&&!spawnRejected){boolean placed=false;for(int attempt=0;attempt<12;attempt++){double angle=random.nextDouble()*Math.PI*2,radius=5+random.nextDouble()*6;int x=net.minecraft.util.Mth.floor(g.anchor.x+Math.cos(angle)*radius),z=net.minecraft.util.Mth.floor(g.anchor.z+Math.sin(angle)*radius);BlockPos column=new BlockPos(x,blockPosition().getY(),z);if(!w.getLevel().hasChunkAt(column))continue;int y=w.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z);BlockPos candidate=new BlockPos(x,y,z);if(Math.abs(y-g.anchor.y)>3||!w.getLevel().getWorldBorder().isWithinBounds(candidate)||w.getLevel().isCloseToVillage(candidate,2)||!w.getFluidState(candidate).isEmpty()||w.getLevel().getNearestPlayer(x+.5,y,z+.5,24,false)!=null)continue;Vec3 old=position();setPos(x+.5,y,z+.5);if(w.getLevel().noCollision(this,getBoundingBox())&&DogheadContent.clearLargestBody(w.getLevel(),candidate)&&w.getBlockState(candidate.below()).isFaceSturdy(w,candidate.below(),net.minecraft.core.Direction.UP)&&DogheadPacks.members(this).stream().noneMatch(m->m!=this&&m.distanceToSqr(this)<1.44)){placed=true;break;}setPos(old);}if(!placed)spawnRejected=true;}g.count++;if(g.count>6)spawnRejected=true;return g;}role(role(),true);return data;}
47:  @Override public int getMaxSpawnClusterSize(){return 6;}
48:  private boolean clearShot(LivingEntity victim,Vec3 from){if(!hasLineOfSight(victim))return false;Vec3 to=victim.getBoundingBox().getCenter();if(level().clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this)).getType()!=HitResult.Type.MISS)return false;for(var mate:DogheadPacks.members(this))if(mate!=this&&mate.getBoundingBox().inflate(.2).clip(from,to).isPresent())return false;return true;}
49:  private void hit(LivingEntity victim,float damage,double reach,double degrees){if(!valid(victim)||!hasLineOfSight(victim)||distanceToSqr(victim)>reach*reach)return;Vec3 forward=new Vec3(-Math.sin(Math.toRadians(attackYaw)),0,Math.cos(Math.toRadians(attackYaw))),toward=victim.position().subtract(position()).multiply(1,0,1).normalize();if(forward.dot(toward)<Math.cos(Math.toRadians(degrees/2)))return;Vec3 start=position().add(0,role().height*.58,0),end=victim.getBoundingBox().getCenter();if(clock.clip==Clip.THRUST&&!victim.getBoundingBox().inflate(.2).clip(start,start.add(forward.scale(reach)).add(0,-Math.tan(thrustAim())*reach,0)).isPresent())return;if(!clearShot(victim,start))return;if(victim.hurt(damageSources().mobAttack(this),damage)){cue(role()==Role.LEADER?"leader.axe_impact":"shared.claw_gear",.7F);if(clock.clip==Clip.SWEEP)victim.knockback(.25,getX()-victim.getX(),getZ()-victim.getZ());debug("hit target="+victim.getUUID()+" raw="+damage);}}
50:  private void impact(LivingEntity victim){switch(clock.clip){case THRUST->hit(victim,6,2.4,45);case KNIFE->hit(victim,4,1.75,80);case CHOP->{float hp=victim==null?0:victim.getHealth();hit(victim,8,2.1,55);if(victim==null||victim.getHealth()>=hp)missDust();}case SWEEP,CLEAVE->{double reach=clock.clip==Clip.SWEEP?1.8:2;var candidates=level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(reach),this::valid);candidates.sort(Comparator.comparingDouble(this::distanceToSqr));int struck=0;for(var t:candidates){Vec3 delta=t.position().subtract(position()).multiply(1,0,1).normalize(),f=new Vec3(-Math.sin(Math.toRadians(attackYaw)),0,Math.cos(Math.toRadians(attackYaw)));if(distanceToSqr(t)<=reach*reach&&hasLineOfSight(t)&&f.dot(delta)>=Math.cos(Math.toRadians(clock.clip==Clip.SWEEP?55:52.5))){hit(t,clock.clip==Clip.SWEEP?4:6,reach,clock.clip==Clip.SWEEP?110:105);if(++struck>=2)break;}}}default->{}}}
57:   if(victim==null&&clock.clip==Clip.READY&&--preyCheck<=0){preyCheck=200;if(now>=preyAfter&&random.nextDouble()<DogheadConfig.preyChance){var prey=level().getEntitiesOfClass(Animal.class,getBoundingBox().inflate(16),a->valid(a)&&hasLineOfSight(a));prey.sort(Comparator.comparingDouble(this::distanceToSqr));if(!prey.isEmpty()){setTarget(prey.getFirst());victim=getTarget();preyAfter=now+DogheadConfig.huntGap;}}}
60:   if(clock.clip.attack()&&clock.age<clock.clip.marker&&(victim==null||!valid(victim))){begin(Clip.READY);attackAfter=Math.max(attackAfter,now+20);}
61:   if(victim!=null){getLookControl().setLookAt(victim,clock.clip.attack()?10:25,20);if(clock.clip.attack()){setYRot(attackYaw);yBodyRot=attackYaw;}}
70:   if(marker){if(running.attack()){if(running==Clip.BOW)fire(victim);else impact(victim);attackAfter=now+(running==Clip.BOW?44:role()==Role.SCOUT?20:role()==Role.HUNTER?30:50);}else if((running==Clip.HUNTER_BARK||running==Clip.SCOUT_SPOT||running==Clip.HOWL)&&valid(victim)&&hasLineOfSight(victim)){DogheadPacks.alert(this,victim,running==Clip.HOWL);cue(running==Clip.HOWL?"leader.howl_release":running==Clip.SCOUT_SPOT?"scout.spot_bark":"hunter.bark",running==Clip.HOWL?2:1.5F);}}
74:  @Override public boolean hurt(DamageSource source,float amount){float before=getHealth();boolean accepted=super.hurt(source,amount);if(accepted&&!level().isClientSide&&isAlive()){float actual=Math.max(0,before-getHealth());if(DogheadRules.interrupts(clock.clip,clock.age,actual)){attackAfter=Math.max(attackAfter,level().getGameTime()+20);begin(DogheadRules.hurt(role()));}cue(role().id()+".hurt",.7F);}return accepted;}
75:  @Override public void knockback(double force,double x,double z){super.knockback(force,x,z);if(!level().isClientSide&&isAlive()&&clock!=null&&force>=.7){begin(DogheadRules.hurt(role()));attackAfter=Math.max(attackAfter,level().getGameTime()+20);}}
77:  @Override public void die(DamageSource s){if(!level().isClientSide){begin(DogheadRules.death(role()));cue(role().id()+".death",.8F);}super.die(s);}
81:  @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putString("DogheadRole",role().name());n.putInt("DogheadRetreatCooldown",(int)Math.clamp(retreatUntil-level().getGameTime(),0,80));n.putBoolean("DogheadFounder",founder);n.putInt("DogheadNaturalCheck",naturalCheck);n.putUUID("DogheadPack",pack);n.putBoolean("DogheadBow",bow());n.putInt("DogheadAttackNonce",clock.nonce);n.putInt("DogheadSwitchCooldown",(int)Math.clamp(switchAfter-level().getGameTime(),0,20));n.putInt("DogheadDodgeCooldown",(int)Math.clamp(dodgeAfter-level().getGameTime(),0,160));long now=level().getGameTime();n.putInt("DogheadAttackCooldown",(int)Math.clamp(attackAfter-now,0,120));n.putInt("DogheadHowlCooldown",(int)Math.clamp(howlAfter-now,0,240));n.putInt("DogheadSweepCooldown",(int)Math.clamp(sweepAfter-now,0,120));n.putInt("DogheadPreyCooldown",(int)Math.clamp(preyAfter-now,0,24000));}
82:  @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);Role r;try{r=Role.valueOf(n.getString("DogheadRole"));}catch(IllegalArgumentException e){r=Role.HUNTER;}role(r,false);if(n.contains("Health",net.minecraft.nbt.Tag.TAG_ANY_NUMERIC))setHealth(Math.clamp(n.getFloat("Health"),0,r.hp));founder=n.getBoolean("DogheadFounder");naturalCheck=Math.clamp(n.getInt("DogheadNaturalCheck"),0,23);pack=n.hasUUID("DogheadPack")?n.getUUID("DogheadPack"):UUID.randomUUID();entityData.set(BOW,n.getBoolean("DogheadBow"));clock.nonce=Math.max(0,n.getInt("DogheadAttackNonce"));clock.begin(Clip.READY);sync();long now=level().getGameTime();attackAfter=now+Math.clamp(n.getInt("DogheadAttackCooldown"),20,120);howlAfter=now+Math.clamp(n.getInt("DogheadHowlCooldown"),60,240);sweepAfter=now+Math.clamp(n.getInt("DogheadSweepCooldown"),0,120);preyAfter=now+Math.clamp(n.getInt("DogheadPreyCooldown"),0,24000);retreatUntil=now+Math.clamp(n.getInt("DogheadRetreatCooldown"),0,80);switchAfter=now+Math.clamp(n.getInt("DogheadSwitchCooldown"),0,20);dodgeAfter=now+Math.clamp(n.getInt("DogheadDodgeCooldown"),0,160);tracked=null;lastSeen=null;interestUntil=0;}
```

## src/main/java/org/slavicmyths/pesigolovtsy/DogheadRules.java

```java
5:  public enum Role {
7:   public final int hp,sight,xp;public final float speed,width,height;
8:   Role(int hp,float speed,float width,float height,int sight,int xp){this.hp=hp;this.speed=speed;this.width=width;this.height=height;this.sight=sight;this.xp=xp;}
11:  public enum Clip {
17:   public boolean attack(){return this==THRUST||this==SWEEP||this==KNIFE||this==BOW||this==CHOP||this==CLEAVE;}
20:  public static Clip hurt(Role r){return switch(r){case HUNTER->Clip.HUNTER_HURT;case SCOUT->Clip.SCOUT_HURT;case LEADER->Clip.LEADER_HURT;};}
22:  public static boolean interrupts(Clip clip,int tick,float actualDamage){return !clip.death()&&(!(clip==Clip.CHOP)&&!(clip==Clip.CLEAVE)||tick<17&&actualDamage>=4);}
```

## src/main/java/org/slavicmyths/poloz/GreatPoloz.java

```java
12: import net.minecraft.world.entity.ai.attributes.*;
15: import net.minecraft.world.damagesource.DamageSource;
23: /** One health owner with native NeoForge multipart collision, server path and bounded impact queues. */
34:  private final int[] cooldowns=new int[6];private int attackAge,rest,phaseAge,noPlayers,debugTicks;
42:  public static AttributeSupplier.Builder attributes(){return Mob.createMobAttributes().add(Attributes.MAX_HEALTH,3000).add(Attributes.MOVEMENT_SPEED,.23).add(Attributes.KNOCKBACK_RESISTANCE,1).add(Attributes.FOLLOW_RANGE,80).add(Attributes.ATTACK_DAMAGE,12);}
71:  public boolean rewardFissure(ServerPlayer p,float amount){if(!isAlive()||isAlliedTo(p)||nearestDistance(p.position())>36)return false;invulnerableTime=0;boolean hit=super.hurt(p.damageSources().playerAttack(p),amount);if(hit)updatePhase();return hit;}
72:  public boolean hitPart(PolozPart part,DamageSource source,float amount){return acceptHit(part.segment,part.scar,source,amount,part.position());}
73:  @Override public boolean hurt(DamageSource source,float amount){
76:   if(source.getDirectEntity() instanceof LivingEntity attacker){PolozPart closest=null;double best=getBoundingBox().distanceToSqr(attacker.getEyePosition());for(var p:parts){double d=p.getBoundingBox().distanceToSqr(attacker.getEyePosition());if(d<best){closest=p;best=d;}}if(best>(attacker instanceof ServerPlayer?Math.pow(attacker.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ENTITY_INTERACTION_RANGE),2):16))return false;if(closest!=null)return hitPart(closest,source,amount);}
80:  private boolean acceptHit(int segment,boolean scar,DamageSource source,float amount,Vec3 at){if(level().isClientSide||!isAlive()||!Float.isFinite(amount)||amount<=0||source.getEntity() instanceof LivingEntity living&&ally(living))return false;
81:   if(source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)||source.is(net.minecraft.tags.DamageTypeTags.IS_FALL)||source.getSourcePosition()==null)return false;
87:   invulnerableTime=0;boolean result=super.hurt(source,amount*multiplier);if(result){UUID credit=attacker instanceof ServerPlayer p?p.getUUID():attacker instanceof LivingEntity l?org.slavicmyths.kalin.KalinParty.owner(l):null;if(credit!=null){participants.put(credit,now);PolozEvent.get(w).participants.add(credit);PolozEvent.get(w).setDirty();}playSound(PolozContent.sound("poloz_weakspot_hit"),.7f,1);updatePhase();}return result;
92:   for(int i=0;i<cooldowns.length;i++)if(cooldowns[i]>0)cooldowns[i]--;if(rest>0)rest--;
101:   if(action()!=0)attack(w);
106:  private void pickTarget(ServerLevel w){if(deferredTarget!=null){if(w.getEntity(deferredTarget) instanceof LivingEntity old&&old.isAlive())setTarget(old);deferredTarget=null;}var current=getTarget();if(current instanceof org.slavicmyths.quest185.WolfSupport wolf&&wolf.isAlive()&&w.getGameTime()<wolf.getPersistentData().getLong("ThreatUntil"))return;if(current!=null&&current.isAlive()&&tickCount%60!=0)return;setTarget(w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(80),e->e.isAlive()&&!ally(e)&&(!(e instanceof Player p)||!p.isCreative()&&!p.isSpectator())&&(e instanceof Player||org.slavicmyths.bogatyr.HeroThreats.availableHero(e)||e instanceof Mob m&&m.getTarget()==this)).stream().min(Comparator.comparingDouble(e->distanceToSqr(e)-(e instanceof Player?800:0))).orElse(null));}
107:  private int choose(){for(int k=0;k<6;k++){int skill=1+random.nextInt(phase()==1?4:6);if(cooldowns[skill-1]>0)continue;if(skill==5&&(!ringSpace()||getTarget().distanceTo(this)>20))continue;if(skill==6&&minions.size()+(phase()==3?6:4)>8)continue;return skill;}return 0;}
110:  private void begin(int skill){entityData.set(ACTION,skill);entityData.set(CLOCK,(int)level().getGameTime());attackAge=0;attackHits.clear();var target=getTarget();mark=target==null?position():target.position();aim=mark.subtract(position()).multiply(1,0,1).normalize();cooldowns[skill-1]=PolozRules.cooldown(skill,phase());playSound(PolozContent.sound(switch(skill){case 2->"poloz_burrow";case 3->"poloz_mor_rain";case 4->"poloz_tail_slam";case 6->"poloz_minion_call";default->"poloz_surface_roar";}),2,1);}
111:  private void attack(ServerLevel w){int skill=action();attackAge++;int wind=windup(skill);if(attackAge<wind&&tickCount%4==0)w.sendParticles(org.slavicmyths.mor.MorContent.SPARK.get(),getX(),getY()+1,getZ(),12,2,.4,2,.02);
112:   if(skill==1&&attackAge>=30&&attackAge<60)strike(getBoundingBox().inflate(1),12,2.5,false);
113:   if(skill==2&&attackAge>=76&&attackAge<100&&attackAge%4==0){for(int i=0;i<20;i++){double a=i*Math.PI/10;w.sendParticles(org.slavicmyths.mor.MorContent.SPARK.get(),getX()+Math.cos(a)*4,w.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(int)getX(),(int)getZ())+.1,getZ()+Math.sin(a)*4,1,0,0,0,0);}}if(skill==2&&attackAge==100){setPos(getX(),w.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(int)getX(),(int)getZ()),getZ());playSound(PolozContent.sound("poloz_emerge"),3,1);strike(getBoundingBox().inflate(4),isInWater()?12:14,2.5,true);}
116:   if(skill==5&&attackAge>=80&&attackAge<140&&attackAge%20==0){attackHits.clear();for(var victim:w.getEntitiesOfClass(LivingEntity.class,new AABB(mark.add(-7,-1,-7),mark.add(7,4,7)),v->v.isAlive()&&!ally(v)&&v.position().multiply(1,0,1).distanceToSqr(mark.multiply(1,0,1))<=49))if(!(victim instanceof Player p)||!p.isCreative()&&!p.isSpectator()){victim.hurt(damageSources().mobAttack(this),8);Vec3 push=mark.subtract(victim.position()).multiply(1,0,1).normalize().scale(.15);victim.push(push.x,.1,push.z);}}
121:  private void strike(AABB box,float damage,double push,boolean up){for(var e:level().getEntitiesOfClass(LivingEntity.class,box,l->l.isAlive()&&!ally(l)&&(!(l instanceof Player p)||!p.isCreative()&&!p.isSpectator())))if(attackHits.add(e.getUUID())){float mult=phase()==1?1:phase()==2?1.15f:1.25f;if(e.hurt(damageSources().mobAttack(this),damage*mult)){Vec3 dir=e.position().subtract(position()).multiply(1,0,1).normalize().scale(push*.2);e.push(dir.x,up?.9:.2,dir.z);}}}
122:  private void tailSweep(ServerLevel w){Vec3 center=parts[23].position(),forward=parts[22].position().subtract(center).multiply(1,0,1).normalize();float mult=phase()==1?1:phase()==2?1.15f:1.25f;for(var victim:w.getEntitiesOfClass(LivingEntity.class,parts[23].getBoundingBox().inflate(9,2,9),v->v.isAlive()&&!ally(v)&&(!(v instanceof Player p)||!p.isCreative()&&!p.isSpectator()))){Vec3 relative=victim.position().subtract(center).multiply(1,0,1);if(relative.lengthSqr()<=81&&(relative.lengthSqr()<.01||relative.normalize().dot(forward)>=Math.cos(Math.toRadians(70)))&&attackHits.add(victim.getUUID())){victim.hurt(damageSources().mobAttack(this),Math.min(12,11*mult));Vec3 push=relative.normalize().scale(.8);victim.push(push.x,.2,push.z);}}Vec3 wave=parts[19].position();for(var victim:w.getEntitiesOfClass(LivingEntity.class,parts[19].getBoundingBox().inflate(6,1,6),v->v.isAlive()&&!ally(v)&&(!(v instanceof Player p)||!p.isCreative()&&!p.isSpectator())))if(victim.position().multiply(1,0,1).distanceToSqr(wave.multiply(1,0,1))<=36&&attackHits.add(victim.getUUID()))victim.hurt(damageSources().mobAttack(this),5*mult);}
124:  public boolean rainDamage(LivingEntity target,float amount,UUID volley){var n=target.getPersistentData();long now=level().getGameTime();if(!n.hasUUID("PolozRainOwner")||!volley.equals(n.getUUID("PolozRainOwner"))||now>n.getLong("PolozRainUntil")){n.putUUID("PolozRainOwner",volley);n.putLong("PolozRainUntil",now+100);n.putFloat("PolozRainDamage",0);}float allowed=Math.max(0,Math.min(amount,12-n.getFloat("PolozRainDamage")));if(allowed<=0)return false;n.putFloat("PolozRainDamage",n.getFloat("PolozRainDamage")+allowed);return target.hurt(damageSources().mobAttack(this),allowed);}
127:  @Override public void die(DamageSource source){if(!level().isClientSide&&!finished){var w=(ServerLevel)level();for(var e:participants.entrySet()){var p=w.getServer().getPlayerList().getPlayer(e.getKey());if(p!=null&&p.serverLevel()==w&&w.getGameTime()-e.getValue()<=2400&&nearestDistance(p.position())<=256*256){org.slavicmyths.tales.TaleService.promote(p,"slavicmyths:great_poloz",4,true);org.slavicmyths.progression.Knowledge.award(p,"great_poloz_fall");org.slavicmyths.quest185.Quest185Story.data(p).putBoolean("Poloz",true);org.slavicmyths.tales.TaleService.promote(p,"slavicmyths:poloz_rewards",4,true);}}playSound(PolozContent.sound("poloz_death"),3,1);if(w.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)){spawnAtLocation(PolozContent.CORE.get());spawnAtLocation(PolozRewards.WHIP.get());spawnAtLocation(PolozTrophy.ITEM.get());if(random.nextInt(100)<5)spawnAtLocation(PolozRewards.RUNE.get());spawnAtLocation(new net.minecraft.world.item.ItemStack(PolozContent.SCALE.get(),8+random.nextInt(5)));spawnAtLocation(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLD_INGOT,16+random.nextInt(9)));ExperienceOrb.award(w,position(),1500);}cleanup(true);}super.die(source);}
130:  @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);UUID focus=getTarget()!=null?getTarget().getUUID():deferredTarget;if(focus!=null)n.putUUID("PolozTarget",focus);n.putInt("PolozPhase",phase());n.putInt("PolozAction",action());n.putInt("PolozAttackAge",attackAge);n.putInt("PolozRest",rest);n.putIntArray("PolozCooldowns",cooldowns);n.putFloat("PolozHeading",heading);n.putLong("PolozPhaseStart",entityData.get(PHASE_START));n.putBoolean("PolozFinished",finished);var list=new ListTag();for(var at:history){var t=new CompoundTag();t.putDouble("X",at.x);t.putDouble("Y",at.y);t.putDouble("Z",at.z);list.add(t);}n.put("PolozHistory",list);var ms=new ListTag();for(UUID id:minions)ms.add(StringTag.valueOf(id.toString()));n.put("PolozMinions",ms);var ps=new CompoundTag();participants.forEach((id,t)->ps.putLong(id.toString(),t));n.put("PolozParticipants",ps);n.putDouble("AimX",aim.x);n.putDouble("AimZ",aim.z);n.putDouble("MarkX",mark.x);n.putDouble("MarkY",mark.y);n.putDouble("MarkZ",mark.z);}
131:  @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);deferredTarget=n.hasUUID("PolozTarget")?n.getUUID("PolozTarget"):null;entityData.set(PHASE,Math.max(Math.clamp(n.getInt("PolozPhase"),1,3),PolozRules.phase(getHealth())));entityData.set(ACTION,Math.clamp(n.getInt("PolozAction"),0,6));attackAge=Math.clamp(n.getInt("PolozAttackAge"),0,150);entityData.set(CLOCK,(int)level().getGameTime()-attackAge);rest=Math.clamp(n.getInt("PolozRest"),0,1100);int[] cds=n.getIntArray("PolozCooldowns");for(int i=0;i<Math.min(6,cds.length);i++)cooldowns[i]=Math.clamp(cds[i],0,1100);heading=n.getFloat("PolozHeading");entityData.set(PHASE_START,n.getLong("PolozPhaseStart"));finished=n.getBoolean("PolozFinished");history.clear();for(var tag:n.getList("PolozHistory",Tag.TAG_COMPOUND)){if(history.size()>=600)break;var t=(CompoundTag)tag;Vec3 v=new Vec3(t.getDouble("X"),t.getDouble("Y"),t.getDouble("Z"));if(Double.isFinite(v.x)&&Double.isFinite(v.y)&&Double.isFinite(v.z))history.add(v);}minions.clear();for(var tag:n.getList("PolozMinions",Tag.TAG_STRING))if(minions.size()<8)try{minions.add(UUID.fromString(tag.getAsString()));}catch(IllegalArgumentException ignored){}participants.clear();var ps=n.getCompound("PolozParticipants");for(String id:ps.getAllKeys())try{participants.put(UUID.fromString(id),ps.getLong(id));}catch(IllegalArgumentException ignored){}aim=new Vec3(n.getDouble("AimX"),0,n.getDouble("AimZ")).normalize();mark=new Vec3(n.getDouble("MarkX"),n.getDouble("MarkY"),n.getDouble("MarkZ"));updateParts();syncTrack();}
```

## src/main/java/org/slavicmyths/poloz/PolozContent.java

```java
11:  public static final DeferredHolder<EntityType<?>,EntityType<GreatPoloz>> BOSS=ModEntities.ENTITIES.register("great_poloz",()->EntityType.Builder.<GreatPoloz>of(GreatPoloz::new,MobCategory.MONSTER).sized(3.5F,3).fireImmune().clientTrackingRange(16).updateInterval(4).build("slavicmyths:great_poloz"));
```

## src/main/java/org/slavicmyths/poloz/PolozEvents.java

```java
18:  @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.HIGHEST) public static void single(EntityJoinLevelEvent e){if(!(e.getLevel() instanceof ServerLevel w)||!(e.getEntity() instanceof GreatPoloz boss)||!boss.isAlive())return;var d=PolozEvent.get(w);if(w.dimension()!=net.minecraft.world.level.Level.OVERWORLD||d.running()&&!boss.getUUID().equals(d.boss)){e.setCanceled(true);return;}if(!d.running()){d.boss=boss.getUUID();d.event=UUID.randomUUID();d.spawn=boss.blockPosition();d.state=PolozEvent.State.ACTIVE;d.elapsed=180;d.setDirty();}}
29:  @SubscribeEvent public static void damaged(LivingDamageEvent.Post e){if(e.getNewDamage()>0&&e.getEntity() instanceof ServerPlayer p&&p.isUsingItem()&&p.getUseItem().is(PolozContent.HORN.get()))p.stopUsingItem();}
31:   boolean fighter=mob instanceof Enemy||mob instanceof net.minecraft.world.entity.animal.IronGolem||org.slavicmyths.kalin.KalinParty.owner(mob)!=null||mob instanceof TamableAnimal pet&&pet.isTame()&&mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)!=null;
```

## src/main/java/org/slavicmyths/poloz/PolozGlob.java

```java
15:   Vec3 start=position(),end=start.add(getDeltaMovement());if(!w.hasChunkAt(net.minecraft.core.BlockPos.containing(end))){discard();return;}var hit=w.clip(new net.minecraft.world.level.ClipContext(start,end,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,this));var entity=net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(w,this,start,end,getBoundingBox().expandTowards(getDeltaMovement()).inflate(.2),e->e instanceof LivingEntity l&&!boss.isAlliedTo(l),0);
16:   if(entity!=null&&entity.getEntity() instanceof LivingEntity living&&entity.getLocation().distanceToSqr(start)<=hit.getLocation().distanceToSqr(start))boss.rainDamage(living,6,volley);
17:   if(entity!=null||hit.getType()==HitResult.Type.BLOCK){Vec3 at=entity!=null?entity.getLocation():hit.getLocation();boss.impact.patch(net.minecraft.core.BlockPos.containing(at).below());for(var l:w.getEntitiesOfClass(LivingEntity.class,new AABB(at.add(-2,-2,-2),at.add(2,2,2)),l->!boss.isAlliedTo(l))){boss.rainDamage(l,3,volley);l.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,60,0));}discard();return;}setPos(end);setDeltaMovement(getDeltaMovement().add(0,-.025,0));
```

## src/main/java/org/slavicmyths/poloz/PolozRewardEvents.java

```java
6: import net.minecraft.world.entity.ai.attributes.*;
14: import net.minecraft.tags.DamageTypeTags;
15: import net.minecraft.world.damagesource.DamageTypes;
26:  @SubscribeEvent public static void attack(AttackEntityEvent e){if(e.getEntity() instanceof ServerPlayer p&&p.getMainHandItem().is(PolozRewards.WHIP.get())){p.getPersistentData().putLong("PolozPrimaryAt",p.server.overworld().getGameTime());p.getPersistentData().putBoolean("PolozPrimaryCharged",p.getAttackStrengthScale(.5f)>.85f);}}
29:  public static void refresh(LivingEntity e){if(e.level().isClientSide)return;apply(e,Attributes.MAX_HEALTH,"poloz_set_health",PolozRewards.full(e)?2:0);apply(e,Attributes.KNOCKBACK_RESISTANCE,"poloz_set_knockback",PolozRewards.knockback(e));if(e.getHealth()>e.getMaxHealth())e.setHealth(e.getMaxHealth());}
34:  @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){refresh(e.getEntity());}
36:  @SubscribeEvent public static void mitigate(LivingDamageEvent.Pre e){if(!e.getEntity().level().isClientSide&&!e.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY))e.setNewDamage((float)(e.getNewDamage()*(1-PolozRewards.reduction(e.getEntity()))));}
47:  @SubscribeEvent public static void hit(LivingDamageEvent.Post e){if(e.getNewDamage()<=0||!(e.getSource().getEntity() instanceof ServerPlayer p)||e.getSource().getDirectEntity()!=p||!e.getSource().is(DamageTypes.PLAYER_ATTACK)||EFFECT.contains(p.getUUID()))return;
52:   if(s.is(PolozRewards.WHIP.get())&&data.getLong("PolozPrimaryAt")==now&&data.getBoolean("PolozPrimaryCharged")&&now>=data.getLong("PolozSweepAt")){data.putLong("PolozSweepAt",now+20);var look=p.getLookAngle().multiply(1,0,1).normalize();EFFECT.add(p.getUUID());try{int hits=0;for(var t:p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(6,2,6),t->t!=target&&eligible(p,t))){var delta=t.position().subtract(p.position()).multiply(1,0,1);if(delta.lengthSqr()>36||delta.normalize().dot(look)<.25||!sight(p,t))continue;if(hits++>=3)break;t.hurt(p.damageSources().playerAttack(p),13);}}finally{EFFECT.remove(p.getUUID());}p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.PLAYER_ATTACK_SWEEP,SoundSource.PLAYERS,.8f,.65f);}
57:   EFFECT.add(p.getUUID());try{Vec3 impact=target.position();if(target instanceof GreatPoloz boss){double distance=boss.getBoundingBox().distanceToSqr(p.getEyePosition());for(var part:boss.parts){double d=part.getBoundingBox().distanceToSqr(p.getEyePosition());if(d<distance){distance=d;impact=part.position();}}boss.rewardFissure(p,6);}fissure(p,impact);final Vec3 center=impact;int n=target instanceof GreatPoloz?1:0;for(var t:p.serverLevel().getEntitiesOfClass(LivingEntity.class,new AABB(impact,impact).inflate(2),t->eligible(p,t)&&!(t instanceof GreatPoloz)&&t.position().distanceToSqr(center)<=4)){if(n++>=4)break;t.invulnerableTime=0;t.hurt(p.damageSources().playerAttack(p),6);}}finally{EFFECT.remove(p.getUUID());}
60:  public static void spikes(ServerPlayer p){var w=p.serverLevel();var direction=p.getLookAngle().multiply(1,0,1).normalize();if(direction.lengthSqr()<.5)return;Set<UUID> struck=new HashSet<>();EFFECT.add(p.getUUID());try{for(int i=1;i<=3;i++){Vec3 at=p.position().add(direction.scale(i*1.8));var hit=w.clip(new net.minecraft.world.level.ClipContext(p.position().add(0,.5,0),at.add(0,.5,0),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,p));if(hit.getType()!=HitResult.Type.MISS)break;var ground=w.clip(new net.minecraft.world.level.ClipContext(at.add(0,1,0),at.add(0,-2,0),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,p));if(ground.getType()==HitResult.Type.MISS||!w.hasChunkAt(BlockPos.containing(ground.getLocation())))continue;at=ground.getLocation();var spike=PolozContent.SPIKE.get().create(w);if(spike!=null){spike.setPos(at);w.addFreshEntity(spike);}fissure(p,at);for(int y=0;y<8;y++)w.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,Blocks.DEEPSLATE.defaultBlockState()),at.x,at.y+y*.18,at.z,5,.12,.05,.12,.015);for(var t:w.getEntitiesOfClass(LivingEntity.class,new AABB(at.x-.8,at.y,at.z-.8,at.x+.8,at.y+2,at.z+.8),t->eligible(p,t)))if(struck.add(t.getUUID()))t.hurt(p.damageSources().playerAttack(p),10);}}finally{EFFECT.remove(p.getUUID());}}
```

## src/main/java/org/slavicmyths/poloz/PolozRules.java

```java
4:  public static int phase(float hp){return hp<=900?3:hp<=2100?2:1;}
8:  public static int cooldown(int skill,int phase){return switch(skill){case 1->new int[]{440,360,300}[phase-1];case 2->new int[]{600,520,440}[phase-1];case 3->new int[]{560,460,380}[phase-1];case 4->new int[]{360,320,280}[phase-1];case 5->1100;default->phase==3?700:900;};}
```

## src/main/java/org/slavicmyths/poloz/PolozTrophy.java

```java
35:  public static final class Tile extends BlockEntity {public Tile(BlockPos p,BlockState s){super(TILE.get(),p,s);}public net.minecraft.world.phys.AABB getRenderBoundingBox(){return new net.minecraft.world.phys.AABB(worldPosition).inflate(5);}}
```

## src/main/java/org/slavicmyths/quest185/Quest185Content.java

```java
20:  public static void init(net.neoforged.bus.api.IEventBus bus){CleansedLand.IMMUNITY.getId();Quest185Origins.TYPES.register(bus);PARTICLES.register(bus);bus.addListener(Quest185Network::register);bus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent e)->e.put(WOLF.get(),org.slavicmyths.wolf.GreyWolf.attributes().add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH,50).add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,8).build()));}
```

## src/main/java/org/slavicmyths/quest185/Quest185Events.java

```java
16:   if(victim instanceof Enemy&&!mor(victim)&&net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()).getNamespace().equals("minecraft")&&Quest185Story.status(p,8)==Quest185State.Status.ACTIVE&&p.serverLevel().getEntitiesOfClass(org.slavicmyths.wolf.GreyWolf.class,p.getBoundingBox().inflate(12),w->!(w instanceof WolfSupport)&&w.distanceToSqr(victim)<=225).size()>0){var q=Quest185State.quest(n,8);q.putInt("TrainingKills",Math.min(3,q.getInt("TrainingKills")+1));}
22:  @SubscribeEvent public static void mitigation(LivingDamageEvent.Pre e){if(e.getEntity().hasEffect(Quest185Content.WARD_EFFECT)&&e.getSource().getMsgId().equals("mor_block"))e.setNewDamage(e.getNewDamage()*.5f);}
23:  @SubscribeEvent public static void fall(LivingFallEvent e){if(e.getEntity().getPersistentData().getInt("Quest185FallGrace")>0)e.setDamageMultiplier(0);}
25:   if(Quest185Story.status(p,6)==Quest185State.Status.ACTIVE||!n.getBoolean("SeenMor")){var found=p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(16),Quest185Events::mor);if(!found.isEmpty()){n.putBoolean("SeenMor",true);n.putBoolean("TraceMob",true);if(!n.contains("FirstMorEncounterAt"))n.putLong("FirstMorEncounterAt",Quest185Story.now(p));org.slavicmyths.tales.TaleService.promote(p,"slavicmyths:mor_first_signs",2,false);}}
28:   if(p.hasEffect(Quest185Content.TRACKER_EFFECT)){var list=new ListTag();int count=0;for(var mob:p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(64),m->(mor(m)||m instanceof org.slavicmyths.poloz.GreatPoloz)&&p.distanceToSqr(m)<=4096)){if(count++>=128)break;var row=new CompoundTag();row.putInt("Entity",mob.getId());list.add(row);}var out=new CompoundTag();out.putString("Kind","tracker");out.put("Targets",list);Quest185Network.send(p,out);}
31:  @SubscribeEvent public static void damaged(LivingDamageEvent.Post e){if(e.getNewDamage()>0&&e.getEntity() instanceof ServerPlayer p)Quest185Sessions.close(p);}
```

## src/main/java/org/slavicmyths/quest185/WolfSupport.java

```java
22:   if(now<n.getLong("WolfReady")){p.displayClientMessage(Quest185Story.text("cooldown",(n.getLong("WolfReady")-now+19)/20),true);return false;}var at=Quest185Sessions.safe(p,p.blockPosition(),3,6,2.2f,3.5f);if(at==null){p.displayClientMessage(Quest185Story.text("no_safe_ground"),true);return false;}var wolf=Quest185Content.WOLF.get().create(p.serverLevel());if(wolf==null)return false;wolf.owner=p.getUUID();wolf.expiry=now+2400;wolf.setHealth(50);wolf.moveTo(at);wolf.setPersistenceRequired();if(!p.serverLevel().addFreshEntity(wolf))return false;n.putUUID("Support",wolf.getUUID());n.putLong("SupportExpires",wolf.expiry);n.putLong("WolfReady",now+6000);Quest185Content.sound(p,"wolf_call");Quest185Content.sound(p,"wolf_arrive");p.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD,at.x,at.y+1,at.z,16,.6,.6,.6,.01);return true;
27:  @Override public boolean hurt(net.minecraft.world.damagesource.DamageSource s,float amount){if(s.getEntity() instanceof LivingEntity l&&friendly(l))return false;if(getHealth()-amount<=5){retreat();return false;}return super.hurt(s,amount);}
29:  @Override public void die(net.minecraft.world.damagesource.DamageSource s){retreat();setHealth(5);}
34:    if(now>=leapAt&&boss.nearestDistance(position())<=64){leapAt=now+800;Entity target=boss;double d=boss.getBoundingBox().distanceToSqr(getEyePosition());for(var part:boss.parts){if(PolozRules.multiplier(boss.phase(),part.segment,part.scar)<=0)continue;double distance=part.getBoundingBox().distanceToSqr(getEyePosition());if(distance<d){d=distance;target=part;}}if(d<=64&&hasLineOfSight(target)){var delta=target.position().subtract(position()).multiply(1,0,1).normalize();setDeltaMovement(delta.scale(.55).add(0,.3,0));supportMove(WolfTimeline.Move.POUNCE);if(d<=16){target.hurt(damageSources().mobAttack(this),8);boss.setTarget(this);getPersistentData().putLong("ThreatUntil",now+80);}}}
35:    if(now>=howlAt){howlAt=now+1200;supportMove(WolfTimeline.Move.HOWL_SUMMON);playSound(Quest185Content.SOUNDS.get("wolf_howl").get(),.8f,1);for(var ally:w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(12),l->friendly(l)&&l.isAlive()&&distanceToSqr(l)<=144))ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,300,0));}
37:   }else{var enemies=w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(8),l->l.isAlive()&&!friendly(l)&&(l instanceof Enemy||Quest185Events.mor(l)));setTarget(enemies.stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null));}
41:  @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);owner=n.hasUUID("SupportOwner")?n.getUUID("SupportOwner"):null;expiry=n.getLong("Expiry");sniffAt=n.getLong("Sniff");leapAt=n.getLong("Leap");howlAt=n.getLong("Howl");setHealth(Math.clamp(n.getFloat("Health"),5,50));}
```

## src/main/java/org/slavicmyths/registry/ModEntities.java

```java
31:     public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.entity.OvinnikEntity>> OVINNIK = ENTITIES.register("ovinnik", () -> EntityType.Builder.of(org.slavicmyths.entity.OvinnikEntity::new, MobCategory.MONSTER).sized(1.15F,2.45F).clientTrackingRange(12).fireImmune().build("slavicmyths:ovinnik"));
61:         event.put(PIKE.get(),org.slavicmyths.water.RiverFish.createAttributes().add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH,8).add(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED,.7).build());
62:         event.put(CARP.get(),org.slavicmyths.water.RiverFish.createAttributes().add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH,6).add(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED,.5).build());
63:         event.put(CRAYFISH.get(),org.slavicmyths.water.RiverFish.createAttributes().add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH,4).add(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED,.25).build());
114:     public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.hunt.ElementHuntMob>> FIRE_SERPENT=ENTITIES.register("fire_serpent",()->EntityType.Builder.of(org.slavicmyths.hunt.ElementHuntMob::new,MobCategory.MONSTER).sized(1.25F,1.55F).clientTrackingRange(12).fireImmune().build("slavicmyths:fire_serpent"));
116:     public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.hunt.SerpentProjection>> SERPENT_PROJECTION=ENTITIES.register("serpent_projection",()->EntityType.Builder.of(org.slavicmyths.hunt.SerpentProjection::new,MobCategory.MISC).sized(1.25F,1.55F).clientTrackingRange(12).updateInterval(2).fireImmune().build("slavicmyths:serpent_projection"));
```

## src/main/java/org/slavicmyths/registry/ModItems.java

```java
23:     public static final DeferredHolder<Item,Item> CURSED_VOLKOLAK_SPAWN_EGG=egg("cursed_volkolak_spawn_egg",ModEntities.CURSED_VOLKOLAK,0x28272B,0x64574D);
60:     public static final DeferredHolder<Item, Item> DOMOVOY_SPAWN_EGG = ITEMS.register("domovoy_spawn_egg",
61:             () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(ModEntities.DOMOVOY, 0x77563D, 0xD2C0A2, properties()));
62:     public static final DeferredHolder<Item, Item> LESHY_SPAWN_EGG = ITEMS.register("leshy_spawn_egg",
63:             () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(ModEntities.LESHY, 0x343F2B, 0xC5DC76, properties()));
74:     public static final DeferredHolder<Item, Item> ANCIENT_SIGN = ITEMS.register("ancient_sign", () -> new org.slavicmyths.item.AncientSignItem(properties().rarity(Rarity.RARE)));
77:     public static final DeferredHolder<Item, Item> SILVER_ORE = ITEMS.register("silver_ore", () -> new BlockItem(ModBlocks.SILVER_ORE.get(), properties()));
78:     public static final DeferredHolder<Item, Item> SILVER_INGOT = ITEMS.register("silver_ingot", () -> new Item(properties()));
79:     public static final DeferredHolder<Item, Item> SILVER_NUGGET = ITEMS.register("silver_nugget", () -> new Item(properties()));
89:     public static final DeferredHolder<Item, Item> SILVER_SWORD = ITEMS.register("silver_sword", () -> new SwordItem(ModGear.SILVER, properties().attributes(SwordItem.createAttributes(ModGear.SILVER, 3, -2.4F))));
90:     public static final DeferredHolder<Item, Item> SILVER_DAGGER = ITEMS.register("silver_dagger", () -> new SwordItem(ModGear.SILVER, properties().attributes(SwordItem.createAttributes(ModGear.SILVER, 1, -1.5F))));
92:     public static final DeferredHolder<Item, Item> RITUAL_KNIFE = ITEMS.register("ritual_knife", () -> new SwordItem(ModGear.SILVER, properties().attributes(SwordItem.createAttributes(ModGear.SILVER, 1, -1.7F))));
119:     public static final DeferredHolder<Item, Item> SILVER_SPEAR = ITEMS.register("silver_spear", () -> new org.slavicmyths.item.SpearItem(ModGear.SILVER, properties()));
136:     public static final DeferredHolder<Item, Item> SILVER_FITTING = ITEMS.register("silver_fitting", () -> new Item(properties()));
146:     public static final DeferredHolder<Item, Item> NOON_SICKLE = ITEMS.register("noon_sickle", () -> new SwordItem(ModGear.SILVER, properties().rarity(Rarity.UNCOMMON).attributes(SwordItem.createAttributes(ModGear.SILVER, 3, -2.5F))));
147:     public static final DeferredHolder<Item, Item> KIKIMORA_SPAWN_EGG = ITEMS.register("kikimora_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(ModEntities.KIKIMORA, 0x494137, 0xbca981, properties()));
148:     public static final DeferredHolder<Item, Item> POLUDNITSA_SPAWN_EGG = ITEMS.register("poludnitsa_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(ModEntities.POLUDNITSA, 0x494137, 0xbca981, properties()));
149:     public static final DeferredHolder<Item, Item> POLEVIK_SPAWN_EGG = ITEMS.register("polevik_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(ModEntities.POLEVIK, 0x494137, 0xbca981, properties()));
150:     public static final DeferredHolder<Item, Item> BANNIK_SPAWN_EGG = ITEMS.register("bannik_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(ModEntities.BANNIK, 0x494137, 0xbca981, properties()));
151:     public static final DeferredHolder<Item, Item> IGOSHA_SPAWN_EGG = ITEMS.register("igosha_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(ModEntities.IGOSHA, 0x494137, 0xbca981, properties()));
152:     public static final DeferredHolder<Item, Item> OVINNIK_SPAWN_EGG = ITEMS.register("ovinnik_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(ModEntities.OVINNIK, 0x2C2722, 0xE34A18, properties()));
189:     public static final DeferredHolder<Item, Item> BROWN_BEAR_SPAWN_EGG=ITEMS.register("brown_bear_spawn_egg",()->new org.slavicmyths.item.WildlifeFamilyEgg(true,0x4b3427,0xb28a62,properties()));
190:     public static final DeferredHolder<Item, Item> BEAR_CUB_SPAWN_EGG=egg("bear_cub_spawn_egg",ModEntities.BEAR_CUB,0x6b4a35,0xcda578);
191:     public static final DeferredHolder<Item, Item> GREY_WOLF_SPAWN_EGG=egg("grey_wolf_spawn_egg",ModEntities.GREY_WOLF,0x34353a,0xd99a37);
192:     public static final DeferredHolder<Item, Item> FOREST_WOLF_SPAWN_EGG=egg("forest_wolf_spawn_egg",ModEntities.FOREST_WOLF,0x292b2e,0xb2a98f);
193:     public static final DeferredHolder<Item, Item> BOAR_SPAWN_EGG=egg("boar_spawn_egg",ModEntities.BOAR,0x493a32,0xd5c09c);
194:     public static final DeferredHolder<Item, Item> STAG_SPAWN_EGG=ITEMS.register("stag_spawn_egg",()->new org.slavicmyths.item.WildlifeFamilyEgg(false,0x76533b,0xd1ad7b,properties()));
195:     public static final DeferredHolder<Item, Item> DOE_SPAWN_EGG=egg("doe_spawn_egg",ModEntities.DOE,0x9a7653,0xe0c29c);
198:     private static DeferredHolder<Item, Item> egg(String id,java.util.function.Supplier<? extends net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Mob>> type,int primary,int secondary){return ITEMS.register(id,()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(type,primary,secondary,properties()));}
219:     public static final DeferredHolder<Item, Item> PIKE_SPAWN_EGG=egg("pike_spawn_egg",ModEntities.PIKE,0x53623b,0xbaad78),CARP_SPAWN_EGG=egg("carp_spawn_egg",ModEntities.CARP,0x9b753a,0xd0ae64),CRAYFISH_SPAWN_EGG=egg("crayfish_spawn_egg",ModEntities.CRAYFISH,0x414333,0x777750);
224:     public static final DeferredHolder<Item,Item> BOLOTNIK_SPAWN_EGG=egg("bolotnik_spawn_egg",ModEntities.BOLOTNIK,0x343529,0x657144);
225:     public static final DeferredHolder<Item, Item> VODYANOY_SPAWN_EGG=egg("vodyanoy_spawn_egg",ModEntities.VODYANOY,0x59695d,0x8f9364),RUSALKA_SPAWN_EGG=egg("rusalka_spawn_egg",ModEntities.RUSALKA,0xc3d1c7,0x3e5749);
227:     public static final DeferredHolder<Item, Item> ANCIENT_WATER_SIGN=ITEMS.register("ancient_water_sign",()->new org.slavicmyths.depth.WaterSignItem(properties().rarity(Rarity.UNCOMMON)));
232:     public static final DeferredHolder<Item, Item> ELDER_VODYANOY_SPAWN_EGG=egg("elder_vodyanoy_spawn_egg",ModEntities.ELDER_VODYANOY,0x3f5149,0x889273);
237:     public static final DeferredHolder<Item, Item> SILVER_MACE=ITEMS.register("silver_mace",()->new org.slavicmyths.combat.MaceItem(ModGear.SILVER,7,1F,.3F,.4F,properties()));
241:     public static final DeferredHolder<Item, Item> BANDIT_FIGHTER_EGG=egg("bandit_fighter_spawn_egg",ModEntities.BANDIT_FIGHTER,0x574434,0xb39978);
242:     public static final DeferredHolder<Item, Item> BANDIT_ARCHER_EGG=egg("bandit_archer_spawn_egg",ModEntities.BANDIT_ARCHER,0x485343,0xb39978);
243:     public static final DeferredHolder<Item, Item> BANDIT_HEAVY_EGG=egg("bandit_heavy_spawn_egg",ModEntities.BANDIT_HEAVY,0x474a49,0xb39978);
244:     public static final DeferredHolder<Item, Item> BANDIT_SENIOR_EGG=egg("bandit_senior_spawn_egg",ModEntities.BANDIT_SENIOR,0x66503b,0xb39978);
245:     public static final DeferredHolder<Item, Item> ATAMAN_EGG=egg("ataman_spawn_egg",ModEntities.ATAMAN,0x653d36,0xa5a199);
246:     public static final DeferredHolder<Item, Item> NIGHTINGALE_SPAWN_EGG=egg("nightingale_spawn_egg",ModEntities.NIGHTINGALE,0x243c28,0x9d2539);
268:     public static final DeferredHolder<Item, Item> UPYR_SPAWN_EGG=egg("upyr_spawn_egg",ModEntities.UPYR,0xE4D8CE,0x8D1717);
269:     public static final DeferredHolder<Item, Item> NAV_SPAWN_EGG=egg("nav_spawn_egg",ModEntities.NAV,0xCDE7FF,0x5FA7FF);
270:     public static final DeferredHolder<Item, Item> DRUZHINNIK_SPAWN_EGG=egg("kurgan_druzhinnik_spawn_egg",ModEntities.DRUZHINNIK,0x7B7A80,0xB22E2E);
271:     public static final DeferredHolder<Item, Item> VOEVODA_SPAWN_EGG=egg("kurgan_voevoda_spawn_egg",ModEntities.VOEVODA,0x8A2327,0xD6A451);
272:     public static final DeferredHolder<Item, Item> VOLKHV_SPAWN_EGG=egg("buried_volkhv_spawn_egg",ModEntities.VOLKHV,0x2F2D38,0xB39A63);
273:     public static final DeferredHolder<Item, Item> PRINCE_SPAWN_EGG=egg("unresting_prince_spawn_egg",ModEntities.PRINCE,0x9F1E28,0xD9B14E);
301:     public static final DeferredHolder<Item, Item> FIRE_SERPENT_SPAWN_EGG=egg("fire_serpent_spawn_egg",ModEntities.FIRE_SERPENT,0x29191A,0xF04B16);
302:     public static final DeferredHolder<Item, Item> PODVEY_SPAWN_EGG=egg("podvey_spawn_egg",ModEntities.PODVEY,0x56626F,0xC5E4F1);
303:     public static final DeferredHolder<Item, Item> VOLKOLAK_SPAWN_EGG=egg("volkolak_spawn_egg",ModEntities.VOLKOLAK,0x3D3A38,0x9A1F27);
315:     public static final DeferredHolder<Item, Item> LIKHO_ONE_EYED_SPAWN_EGG=egg("likho_one_eyed_spawn_egg",ModEntities.LIKHO_ONE_EYED,0x1E1B1D,0xC7A77A);
316:     public static final DeferredHolder<Item, Item> TUGARIN_ZMEY_SPAWN_EGG=egg("tugarin_zmey_spawn_egg",ModEntities.TUGARIN_ZMEY,0x8F3D2F,0xC99B45);
326:  public static final DeferredHolder<Item, Item> BABA_YAGA_SPAWN_EGG=ITEMS.register("baba_yaga_spawn_egg",()->new org.slavicmyths.yaga.YagaSpawnEgg(properties()));
```

## src/main/java/org/slavicmyths/rewards/RewardEvents.java

```java
11: import net.minecraft.tags.DamageTypeTags;
13: import net.minecraft.world.entity.ai.attributes.*;
17: import net.minecraft.world.damagesource.*;
33:  public static void cooldown(ServerPlayer p,String key,int ticks){state(p).putLong(key,now(p)+ticks);}
38:  public static boolean melee(DamageSource s,ServerPlayer p){return s.getEntity()==p&&s.getDirectEntity()==p&&s.is(DamageTypes.PLAYER_ATTACK);}
39:  public static void fx(ServerPlayer p,String sound){var s=RewardContent.SOUNDS.get(sound);if(s!=null)p.serverLevel().playSound(null,p.blockPosition(),s.get(),net.minecraft.sounds.SoundSource.PLAYERS,.65F,1);int color=sound.contains("dispelling")||sound.contains("suppression")?0xB173E8:sound.contains("stomp")?0xD6B47B:sound.contains("marinka")?0x981E43:sound.contains("kartaus")?0x901921:sound.contains("pursuit")?0xF5B82E:sound.contains("finist")?0xE8C274:0xCF9E44;var dust=new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(((color>>16)&255)/255F,((color>>8)&255)/255F,(color&255)/255F),.6F);boolean ring=sound.contains("stomp")||sound.contains("ilya")||sound.contains("battle_line");if(ring){double radius=sound.contains("stomp")?3.5:3;for(int i=0;i<20;i++){double a=i*Math.PI/10;p.serverLevel().sendParticles(dust,p.getX()+Math.cos(a)*radius,p.getY()+.2,p.getZ()+Math.sin(a)*radius,1,0,0,0,0);}}else p.serverLevel().sendParticles(dust,p.getX(),p.getY()+1,p.getZ(),12,.4,.3,.4,.01);if(sound.contains("marinka"))p.serverLevel().sendParticles(ParticleTypes.SMOKE,p.getX(),p.getY()+.6,p.getZ(),10,.3,.4,.3,.02);}
48:  /** Pure mitigation estimate; never damages armor or invokes mutable damage-container methods. */
49:  public static float potential(ServerPlayer p,DamageSource source,float raw){float amount=raw;if(!source.is(DamageTypeTags.BYPASSES_ARMOR))amount=CombatRules.getDamageAfterAbsorb(p,amount,source,p.getArmorValue(),(float)p.getAttributeValue(Attributes.ARMOR_TOUGHNESS));if(!source.is(DamageTypeTags.BYPASSES_EFFECTS)){
50:   var res=p.getEffect(MobEffects.DAMAGE_RESISTANCE);if(res!=null&&!source.is(DamageTypeTags.BYPASSES_RESISTANCE))amount=Math.max(0,amount*(25-(res.getAmplifier()+1)*5)/25F);
51:   if(!source.is(DamageTypeTags.BYPASSES_ENCHANTMENTS))amount=CombatRules.getDamageAfterMagicAbsorb(amount,net.minecraft.world.item.enchantment.EnchantmentHelper.getDamageProtection(p.serverLevel(),p,source));
52:  }amount=Math.max(0,amount-p.getAbsorptionAmount());if(amount>=6&&source.getEntity() instanceof LivingEntity attacker&&hostile(attacker,p)&&!source.is(DamageTypeTags.BYPASSES_ARMOR)&&!source.is(DamageTypeTags.IS_FIRE)&&!source.is(DamageTypeTags.IS_EXPLOSION)){if(wears(p,"trophy_idol_chest"))amount*=.9F;if(RewardRunes.active(p,"battle_line")&&ready(p,"LineReady")&&allies(p)>=2)amount*=.85F;}return amount;}
54:  public static void blocked(LivingEntity defender,LivingShieldBlockEvent event){if(!(defender instanceof ServerPlayer p)||!event.getBlocked()||event.getDamageContainer().getBlockedDamage()<=0)return;var source=event.getDamageSource();if(!(source.getEntity() instanceof LivingEntity attacker)||!hostile(attacker,p)||source.getMsgId().equals("reward_reflection"))return;
57:    float damage=potential(p,source,event.getDamageContainer().getBlockedDamage())*REFLECTION;if(damage<=0)return;cooldown(p,"ShieldReady",700);
58:    for(var mob:p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(3),e->hostile(e,p)&&ordinary(e)&&e.distanceToSqr(p)<=9))mob.knockback(.65,p.getX()-mob.getX(),p.getZ()-mob.getZ());
59:    attacker.hurt(org.slavicmyths.combat.MythDamageSources.periodic("reward_reflection",p),damage);fx(p,"gift_ilya_block_reflect");
62:  @SubscribeEvent(priority=EventPriority.LOWEST) public static void damage(LivingDamageEvent.Pre e){var source=e.getSource();var target=e.getEntity();if(target.level().isClientSide||e.getNewDamage()<=0)return;
63:   if(source.getEntity() instanceof LivingEntity from&&from.getPersistentData().contains("RewardSuppressed")){var n=from.getPersistentData();long clock=((ServerLevel)target.level()).getServer().overworld().getGameTime();if(clock<n.getLong("RewardSuppressed")){e.setNewDamage(e.getNewDamage()*.85F);}n.remove("RewardSuppressed");}
66:    if(wears(p,"gift_dobrynya_ring")&&now(p)<n.getLong("CounterUntil")&&ready(p,"CounterReady")){multiplier*=COUNTER;n.remove("CounterUntil");cooldown(p,"CounterReady",240);fx(p,"gift_dobrynya_counter");}
69:    e.setNewDamage(e.getNewDamage()*multiplier);
71:   if(source.getEntity() instanceof ServerPlayer archer&&source.is(DamageTypeTags.IS_PROJECTILE)&&source.getDirectEntity()!=null&&hostile(target,archer)&&wears(archer,"trophy_skim_necklace")&&target.getHealth()<target.getMaxHealth()*.3F){e.setNewDamage(e.getNewDamage()*1.1F);fx(archer,"trophy_skim_execute");}
73:    boolean physical=!source.is(DamageTypeTags.BYPASSES_ARMOR)&&!source.is(DamageTypeTags.IS_FIRE)&&!source.is(DamageTypeTags.IS_EXPLOSION);
74:    if(physical&&e.getNewDamage()>=6){if(wears(p,"trophy_idol_chest")){e.setNewDamage(e.getNewDamage()*.9F);fx(p,"trophy_idol_heavy_guard");}if(RewardRunes.active(p,"battle_line")&&ready(p,"LineReady")&&allies(p)>=2){e.setNewDamage(e.getNewDamage()*.85F);cooldown(p,"LineReady",600);fx(p,"rune_battle_line_proc");}}
77:  public static int allies(ServerPlayer p){return p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(10),e->e!=p&&e.isAlive()&&e.distanceToSqr(p)<=100&&((e instanceof ServerPlayer other&&!other.isSpectator()&&p.isAlliedTo(other))||(p.getUUID().equals(owner(e))&&e.getHealth()>e.getMaxHealth()*.15F&&!e.getPersistentData().getBoolean("KalinRetreated")))).size();}
80:  @SubscribeEvent public static void after(LivingDamageEvent.Post e){if(e.getEntity() instanceof ServerPlayer p&&e.getNewDamage()>0&&wears(p,"trophy_marinka_cloak")&&ready(p,"BlinkReady")&&p.isAlive()){var to=safeSide(p);if(to!=null){p.teleportTo(p.serverLevel(),to.x,to.y,to.z,p.getYRot(),p.getXRot());cooldown(p,"BlinkReady",900);fx(p,"trophy_marinka_blink");}}
81:   if(e.getSource().getEntity() instanceof ServerPlayer p&&melee(e.getSource(),p)&&e.getNewDamage()>0&&enemy(e.getEntity(),p))sweep(p,e.getEntity(),e.getNewDamage());
82:   if(e.getSource().getEntity() instanceof ServerPlayer p&&melee(e.getSource(),p)&&e.getNewDamage()>0&&hostile(e.getEntity(),p)&&RewardRunes.active(p,"suppression")){var n=e.getEntity().getPersistentData();if(now(p)>=n.getLong("RewardSuppressionReady")){n.putLong("RewardSuppressionReady",now(p)+500);n.putLong("RewardSuppressed",now(p)+80);if(e.getEntity() instanceof org.slavicmyths.finist.KartausEntity a&&a.getType().is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE,ResourceLocation.fromNamespaceAndPath("slavicmyths","reward_interruptible")))&&a.action()==org.slavicmyths.finist.FalconAction.EYE&&a.age()<24)a.begin(org.slavicmyths.finist.FalconAction.STAGGER,8);fx(p,"rune_suppression_proc");}}
84:  @SubscribeEvent public static void fall(LivingFallEvent e){if(e.getEntity() instanceof ServerPlayer p&&e.getDistance()>=2&&p.onGround()&&!p.isFallFlying()&&!p.hasEffect(MobEffects.LEVITATION)&&RewardRunes.active(p,"stomp")&&ready(p,"StompReady")){cooldown(p,"StompReady",400);for(var enemy:p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(3.5),t->hostile(t,p)&&ordinary(t)&&t.distanceToSqr(p)<=12.25))enemy.knockback(.65,p.getX()-enemy.getX(),p.getZ()-enemy.getZ());fx(p,"rune_stomp_proc");}}
86:  @SubscribeEvent public static void equipment(LivingEquipmentChangeEvent e){if(!(e.getEntity() instanceof ServerPlayer p))return;var id=ResourceLocation.fromNamespaceAndPath("slavicmyths","idol_chest_slow176");var attr=p.getAttribute(Attributes.MOVEMENT_SPEED);if(attr!=null){attr.removeModifier(id);if(wears(p,"trophy_idol_chest"))attr.addTransientModifier(new AttributeModifier(id,-.05,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));}}
88:   var targets=p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(18),t->hostile(t,p)&&t.distanceToSqr(p)<=324&&p.hasLineOfSight(t)).stream().limit(30).map(t->new org.slavicmyths.equipment.ClearSightNetwork.Target(t.getId(),t.getUUID())).toList();
90:  }if(n.getBoolean("MantleBuff")&&!wears(p,"trophy_kartaus_mantle")){var effect=p.getEffect(MobEffects.DAMAGE_RESISTANCE);if(effect!=null&&effect.getAmplifier()==0&&effect.getDuration()<=100)p.removeEffect(MobEffects.DAMAGE_RESISTANCE);n.remove("MantleBuff");}if(p.tickCount%20==0&&RewardRunes.active(p,"dispelling")&&ready(p,"DispelReady"))for(var effect:p.getActiveEffects())if(magic(effect)){p.removeEffect(effect.getEffect());cooldown(p,"DispelReady",900);fx(p,"rune_dispelling_proc");break;}}
93:  public static void telegraphs(ServerPlayer p){for(var enemy:p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(18),t->hostile(t,p)&&p.hasLineOfSight(t)&&heavyWindup(t))){if(wears(p,"gift_finist_amulet")&&ready(p,"WarningReady")){cooldown(p,"WarningReady",20);p.displayClientMessage(Component.translatable("reward176.warning",Math.round(enemy.getX()-p.getX()),Math.round(enemy.getZ()-p.getZ())),true);fx(p,"gift_finist_warning");}if(wears(p,"gift_alyosha_belt")&&ready(p,"DodgeReady")&&enemy.distanceToSqr(p)<=16&&enemy instanceof Mob mob&&mob.getTarget()==p){var to=safeSide(p);if(to!=null){p.teleportTo(p.serverLevel(),to.x,to.y,to.z,p.getYRot(),p.getXRot());cooldown(p,"DodgeReady",800);fx(p,"gift_alyosha_dodge");}break;}}}
94:  @SubscribeEvent public static void attack(AttackEntityEvent e){if(e.getEntity() instanceof ServerPlayer p&&p.getMainHandItem().is(RewardContent.item("trophy_kalin_sabre"))&&p.getAttackStrengthScale(.5F)>.9F&&p.onGround()&&!p.isSprinting()){state(p).putUUID("SweepTarget",e.getTarget().getUUID());state(p).putLong("SweepIntent",now(p)+1);}}
96:  @SubscribeEvent(priority=EventPriority.LOWEST) public static void death(LivingDeathEvent e){if(!(e.getSource().getEntity() instanceof ServerPlayer p)||e.isCanceled()||!enemy(e.getEntity(),p))return;var mob=e.getEntity();if(ownedSummon(mob)&&wears(p,"trophy_kartaus_mantle")&&ready(p,"MantleReady")){p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,100,0));state(p).putBoolean("MantleBuff",true);cooldown(p,"MantleReady",400);fx(p,"trophy_kartaus_resistance");}if(ordinary(mob)&&!ownedSummon(mob)&&melee(e.getSource(),p)&&p.getMainHandItem().is(RewardContent.item("trophy_kalin_sabre"))){var n=state(p);var old=java.util.Arrays.stream(n.getLongArray("MomentumKills")).filter(at->now(p)-at<=600).toArray();int count=old.length;var kills=new long[Math.min(3,count+1)];int kept=Math.min(2,count);System.arraycopy(old,count-kept,kills,0,kept);kills[kills.length-1]=now(p);n.putLongArray("MomentumKills",kills);n.putInt("Momentum",kills.length);n.putLong("MomentumAt",now(p));fx(p,count>=2?"trophy_kalin_momentum_3":"trophy_kalin_momentum_1");}}
97:  public static void sweep(ServerPlayer p,LivingEntity target,float damage){var n=state(p);if(java.util.Arrays.stream(n.getLongArray("MomentumKills")).filter(at->now(p)-at<=600).count()<3||now(p)-n.getLong("MomentumAt")>600||now(p)>n.getLong("SweepIntent")||!n.hasUUID("SweepTarget")||!n.getUUID("SweepTarget").equals(target.getUUID())||!p.getMainHandItem().is(RewardContent.item("trophy_kalin_sabre")))return;n.remove("SweepIntent");n.putInt("Momentum",0);n.remove("MomentumKills");var heading=p.getLookAngle().multiply(1,0,1).normalize();int hits=0;for(var enemy:p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(4.5),t->t!=target&&hostile(t,p)&&t.distanceToSqr(p)<=20.25&&p.hasLineOfSight(t))){if(enemy.position().subtract(p.position()).multiply(1,0,1).normalize().dot(heading)<Math.cos(Math.toRadians(47.5)))continue;enemy.hurt(org.slavicmyths.combat.MythDamageSources.periodic("reward_sweep",p),damage*.6F);if(++hits==3)break;}fx(p,"trophy_kalin_sweep");}
98:  @SubscribeEvent(priority=EventPriority.LOWEST) public static void applied(MobEffectEvent.Applicable e){if(!(e.getEntity() instanceof ServerPlayer p)||e.getResult()==MobEffectEvent.Applicable.Result.DO_NOT_APPLY||!RewardRunes.active(p,"dispelling")||!magic(e.getEffectInstance())||state(p).getBoolean("DispelGuard"))return;var original=e.getEffectInstance();if(original.isInfiniteDuration()||original.getDuration()<1)return;e.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);state(p).putBoolean("DispelGuard",true);try{p.addEffect(new MobEffectInstance(original.getEffect(),Math.max(1,(int)Math.ceil(original.getDuration()*.75)),original.getAmplifier(),original.isAmbient(),original.isVisible(),original.showIcon()),e.getEffectSource());}finally{state(p).remove("DispelGuard");}}
```

## src/main/java/org/slavicmyths/rpg/classes/ClassControl.java

```java
19:         e.addEffect(new MobEffectInstance(soft?MobEffects.MOVEMENT_SLOWDOWN:ModEffects.CLASS_ROOT,duration,soft?0:1));
20:         if(sleep){e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,duration,0));if(!soft&&e instanceof Mob m)m.setTarget(null);}
24:         public RootEffect(){super(MobEffectCategory.HARMFUL,0x8EAF87);addAttributeModifier(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED,ClassState.id("class_root_speed"),-.98,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);}
```

## src/main/java/org/slavicmyths/rpg/classes/ClassDefinitions.java

```java
7:     public enum Kind { ACTIVE, PASSIVE, TRIGGER, EVOLUTION }
9:                         int[] cooldown,int[] duration,double[] range,double[] power) {
11:         public int cooldown(int rank){return cooldown[Math.max(0,Math.min(2,rank-1))];}
```

## src/main/java/org/slavicmyths/rpg/classes/ClassEvents.java

```java
4: import net.minecraft.world.entity.ai.attributes.*;
14:     private record PendingBlock(net.neoforged.neoforge.common.damagesource.DamageContainer damage,long tick,net.minecraft.resources.ResourceLocation dimension){}
15:     // At most one native damage reference per player, consumed on the next player event.
16:     // NeoForge 21.1.255 clamps event.getBlockedDamage() to the already reduced newDamage;
20:         if(!(e.getEntity() instanceof ServerPlayer p)||!e.getBlocked()||!e.getOriginalBlock()||e.getBlockedDamage()<=0||!p.isBlocking()||!p.isAlive()||p.invulnerableTime>10||p.isInvulnerableTo(e.getDamageSource())||ClassState.rank(p,"riposte")==0||ClassState.remaining(p,"riposte")>0)return;
21:         BLOCKS.putIfAbsent(p.getUUID(),new PendingBlock(e.getDamageContainer(),ClassState.now(p),p.level().dimension().location()));
26:     @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){if(e.getEntity() instanceof ServerPlayer p)restore(p);}
27:     private static void restore(ServerPlayer p){p.getAttribute(Attributes.ATTACK_SPEED).removeModifier(ClassState.FLURRY);for(String key:java.util.Set.copyOf(p.getPersistentData().getAllKeys()))if(key.startsWith("Class"))p.getPersistentData().remove(key);ClassState.restore(p);ClassState.sync(p);}
29:         var block=BLOCKS.remove(p.getUUID());if(block!=null&&p.isAlive()&&now-block.tick()<=2&&block.dimension().equals(p.level().dimension().location())&&block.damage().getBlockedDamage()>0)confirmedBlock(p);
30:         if(d.contains("ClassFlurryUntil")&&d.getLong("ClassFlurryUntil")<=now){p.getAttribute(Attributes.ATTACK_SPEED).removeModifier(ClassState.FLURRY);d.remove("ClassFlurryUntil");}
33:         int sense=ClassState.passive(p,"spirit_sense");if(sense>0&&p.tickCount%40==0){double range=ClassDefinitions.SKILLS.get("spirit_sense").range(sense);var nearby=p.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,p.getBoundingBox().inflate(range),v->v.getType().is(ClassControl.MYTHICAL)&&p.hasLineOfSight(v)&&p.distanceToSqr(v)<=range*range);if(!nearby.isEmpty()){double distance=nearby.stream().mapToDouble(p::distanceToSqr).min().orElse(range*range);p.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,p.getX(),p.getY()+1,p.getZ(),distance<16?5:2,.3,.3,.3,.01);}}
35:     @SubscribeEvent public static void damage(LivingIncomingDamageEvent e){
48:         int passive=ClassState.passive(p,"steady_aim");if(passive>0&&a.isCritArrow())a.setBaseDamage(a.getBaseDamage()*(1+ClassDefinitions.SKILLS.get("steady_aim").power(passive)));
55:     @SubscribeEvent public static void fall(LivingFallEvent e){if(e.getEntity() instanceof ServerPlayer p&&p.getPersistentData().getBoolean("ClassAirborne")&&p.getPersistentData().getLong("ClassAirUntil")>ClassState.now(p)){e.setDamageMultiplier(0);land(p);}}
56:     private static void land(ServerPlayer p){var d=p.getPersistentData();int rank=ClassState.rank(p,"aerial_strike");var s=ClassDefinitions.SKILLS.get("aerial_strike");if(rank>0&&!d.getBoolean("ClassAirHit")){int hits=0;for(var target:p.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,p.getBoundingBox().inflate(s.range(rank)),v->org.slavicmyths.rpg.Abilities.canHit(p,v)&&p.distanceToSqr(v)<=s.range(rank)*s.range(rank))){if(hits++>=6)break;target.hurt(p.damageSources().playerAttack(p),(float)(2*(1+ClassBalance.power(ClassState.data(p),"aerial_strike",rank))));}}p.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,p.getX(),p.getY()+.1,p.getZ(),12,1,.1,1,.02);d.remove("ClassAirUntil");d.remove("ClassAirborne");d.remove("ClassAirHit");}
57:     public static void opportunity(ServerPlayer p){int rank=ClassState.rank(p,"opportunity");if(rank==0||ClassState.remaining(p,"opportunity")>0)return;var s=ClassDefinitions.SKILLS.get("opportunity");p.getPersistentData().putLong("ClassOpportunityUntil",ClassState.now(p)+s.duration(rank));ClassState.cooldown(p,"opportunity",s.cooldown(rank));}
58:     @SubscribeEvent public static void afterDamage(LivingDamageEvent.Post e){if(e.getNewDamage()<=0)return;
59:         if(e.getEntity() instanceof ServerPlayer p&&!p.getPersistentData().getBoolean("ClassLowHealthLatched")&&p.isAlive()&&p.getHealth()<p.getMaxHealth()*.3&&p.getHealth()+e.getNewDamage()>=p.getMaxHealth()*.3){p.getPersistentData().putBoolean("ClassLowHealthLatched",true);for(String id:java.util.List.of("second_wind","last_rite")){int rank=ClassState.rank(p,id);if(rank>0&&ClassState.remaining(p,id)==0){var s=ClassDefinitions.SKILLS.get(id);float before=p.getAbsorptionAmount();double power=ClassBalance.power(ClassState.data(p),id,rank);p.addEffect(new MobEffectInstance(id.equals("second_wind")?MobEffects.DAMAGE_RESISTANCE:MobEffects.ABSORPTION,ClassBalance.duration(ClassState.data(p),id,rank),id.equals("last_rite")?Math.max(0,(int)Math.ceil(power/4)-1):0));if(id.equals("last_rite"))p.setAbsorptionAmount(Math.max(before,(float)power));ClassState.cooldown(p,id,s.cooldown(rank));ClassState.sync(p);}}}
60:         if(e.getSource().getEntity() instanceof ServerPlayer p&&e.getSource().getDirectEntity() instanceof AbstractArrow&&org.slavicmyths.rpg.Abilities.canAffect(p,e.getEntity())){p.getPersistentData().putUUID("ClassLastTarget",e.getEntity().getUUID());p.getPersistentData().putString("ClassLastTargetDimension",p.level().dimension().location().toString());p.getPersistentData().putLong("ClassLastTargetTick",ClassState.now(p));int rank=ClassState.rank(p,"hunters_fervor");if(rank>0&&ClassState.remaining(p,"hunters_fervor")==0){var s=ClassDefinitions.SKILLS.get("hunters_fervor");p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,s.duration(rank),0));ClassState.cooldown(p,"hunters_fervor",s.cooldown(rank));ClassState.sync(p);}}
62:     public static void confirmedBlock(ServerPlayer p){int rank=ClassState.rank(p,"riposte");if(rank==0||ClassState.remaining(p,"riposte")>0)return;var s=ClassDefinitions.SKILLS.get("riposte");p.getPersistentData().putLong("ClassRiposteUntil",ClassState.now(p)+s.duration(rank));ClassState.cooldown(p,"riposte",s.cooldown(rank));ClassState.sync(p);}
```

## src/main/java/org/slavicmyths/rpg/RareRuneRules.java

```java
9:     public static int cooldown(String id){return switch(id){case "floating_weapon"->700;case "flight"->900;case "rare_protection"->1200;case "death"->400;case "sacrifice"->800;default->0;};}
11:     public static int tier(double health,double max){if(max<=0||health<=0)return 0;double f=health/max;return f<=.15?3:f<=.30?2:f<=.50?1:0;}
12:     public static double damage(int tier){return Math.clamp(tier,0,3)*.10;}
15:     public static boolean canPay(double hp,double price){return Double.isFinite(hp)&&hp>price&&price>0;}
17:     public static boolean execute(double targetHp,double maxHealth,double playerHp,boolean immune){return !immune&&Double.isFinite(targetHp)&&targetHp>0&&targetHp<=executeThreshold(maxHealth)&&canPay(playerHp,12);}
```

## src/main/java/org/slavicmyths/rpg/RareRuneRuntime.java

```java
8: import net.minecraft.world.entity.ai.attributes.*;
9: import net.minecraft.world.damagesource.*;
18: import org.slavicmyths.combat.MythDamageSources;
24:     public static final TagKey<EntityType<?>> IMMUNE=TagKey.create(Registries.ENTITY_TYPE,id("rune_death_immune"));
25:     private static final ResourceLocation DAMAGE=id("rare_damage"),SPEED=id("rare_speed");
31:     private static void cooldown(Player p,String id){data(p).putLong("CD_"+id,now(p)+RareRuneRules.cooldown(id));}
33:     public static double meleeMultiplier(Player p){return 1+RareRuneRules.damage(tier(p))+(temp(p).getLong("RuneSacrificeUntil")>now(p)&&RareRunes.equipped(p,"sacrifice")?.25:0);}
36:         modifier(p,Attributes.ATTACK_SPEED,SPEED,RareRuneRules.speed(tier)+(sacrifice?.15:0));
37:         // Damage is applied only to direct melee in incoming(), not to projectiles or empty hands.
38:         modifier(p,Attributes.ATTACK_DAMAGE,DAMAGE,0);
45:         String id=active.get(selected);if(!ready(p,id)){p.displayClientMessage(RareRunes.text("cooldown",(data(p).getLong("CD_"+id)-now(p)+19)/20),true);return;}
52:         cooldown(p,id);refresh(p);
54:     /** Exact health debit, explicit marker, no hostile hurt event: armor, absorption and procs cannot pay the price. */
57:     @SubscribeEvent(priority=EventPriority.HIGHEST) public static void incoming(LivingIncomingDamageEvent e){if(e.getEntity().level().isClientSide||e.getAmount()<=0)return;DamageSource s=e.getSource();
58:         if(e.getEntity() instanceof ServerPlayer p){if(s.is(DamageTypeTags.IS_FALL)&&temp(p).getLong("RuneFallGrace")>now(p)){e.setCanceled(true);return;}
64:         if(s.is(DamageTypes.PLAYER_ATTACK)&&s.getDirectEntity() instanceof ServerPlayer p&&RuneRework.melee(p.getMainHandItem()))e.setAmount((float)(e.getAmount()*meleeMultiplier(p)));
66:     public static boolean direct(DamageSource s){if(s.is(DamageTypeTags.BYPASSES_INVULNERABILITY)||s.is(DamageTypes.THORNS)||s.getEntity()==null||s.getDirectEntity()==null||s.getMsgId().equals("rune_execute")||s.getMsgId().equals("rune_cost"))return false;return s.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile||s.is(DamageTypeTags.IS_PROJECTILE)||s.getDirectEntity() instanceof RuneVisual||s.getDirectEntity() instanceof LivingEntity&&!s.is(DamageTypeTags.IS_FIRE)&&!s.is(DamageTypeTags.IS_FALL);}
67:     @SubscribeEvent(priority=EventPriority.LOWEST) public static void defense(LivingDamageEvent.Pre e){if(e.getEntity() instanceof ServerPlayer p&&e.getNewDamage()>0&&!e.getSource().getMsgId().equals("rune_cost"))e.setNewDamage((float)(e.getNewDamage()*RareRuneRules.defenseMultiplier(tier(p))));}
68:     @SubscribeEvent public static void hit(LivingDamageEvent.Post e){if(e.getEntity().level().isClientSide||e.getNewDamage()<=0)return;
70:         if(!e.getSource().is(DamageTypes.PLAYER_ATTACK)||!(e.getSource().getDirectEntity() instanceof ServerPlayer p))return;data(p).putLong("HitUntil",now(p)+8);if(tier(p)>0)RpgNetwork.sync(p);
71:         var target=e.getEntity();if(!RareRunes.equipped(p,"death")||!RuneRework.melee(p.getMainHandItem())||!ready(p,"death")||!target.isAlive()||!org.slavicmyths.combat.CombatTargeting.canAttack(p,target)||!p.hasLineOfSight(target)||!RareRuneRules.execute(target.getHealth(),target.getMaxHealth(),p.getHealth(),target.getType().is(IMMUNE)))return;
72:         var source=MythDamageSources.periodic("rune_execute",p);if(target.isInvulnerableTo(source))return;
73:         if(target.hurt(source,Math.max(100,target.getMaxHealth()*4))&&!target.isAlive()){pay(p,12);cooldown(p,"death");p.serverLevel().sendParticles(ParticleTypes.SOUL,target.getX(),target.getY()+.7,target.getZ(),4,.2,.3,.2,.01);refresh(p);}
87:     private static void endProtection(ServerPlayer p){if(temp(p).getInt("RuneShields")<=0&&temp(p).getLong("RuneShieldUntil")==0)return;temp(p).remove("RuneShields");temp(p).remove("RuneShieldUntil");cooldown(p,"rare_protection");var v=p.level().getEntity(temp(p).getInt("RuneShieldVisual"));if(v instanceof RuneVisual visual&&visual.ownedBy(p))visual.dissolve();temp(p).remove("RuneShieldVisual");}
93:     @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){if(e.getEntity() instanceof ServerPlayer p)cleanup(p);}
```

## src/main/java/org/slavicmyths/rpg/RpgEvents.java

```java
23:  @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){if(e.getEntity() instanceof ServerPlayer)RpgNetwork.sync((ServerPlayer)e.getEntity());}
26:  // Capture target shield blocking, then confirm the actual blocked-damage statistic after hurt().
27:  @SubscribeEvent(priority=EventPriority.LOWEST) public static void attack(LivingShieldBlockEvent e){
28:   if(!(e.getEntity() instanceof ServerPlayer)||!e.getBlocked()||e.getBlockedDamage()<=0)return;ServerPlayer p=(ServerPlayer)e.getEntity();
30:   if(e.getDamageSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR)||p.isInvulnerableTo(e.getDamageSource())||e.getDamageSource().getSourcePosition()==null)return;
31:   if(e.getDamageSource().getDirectEntity() instanceof AbstractArrow&&((AbstractArrow)e.getDamageSource().getDirectEntity()).getPierceLevel()>0)return;
32:   net.minecraft.world.phys.Vec3 direction=p.position().subtract(e.getDamageSource().getSourcePosition()).normalize();
34:   CompoundTag pending=new CompoundTag();pending.putInt("Before",p.getStats().getValue(Stats.CUSTOM.get(Stats.DAMAGE_BLOCKED_BY_SHIELD)));pending.put("Shield",p.getUseItem().saveOptional(p.registryAccess()));pending.putInt("Attacker",e.getDamageSource().getEntity()==null?-1:e.getDamageSource().getEntity().getId());p.getPersistentData().put("SlavicPendingBlock",pending);
41:   if(p.getStats().getValue(Stats.CUSTOM.get(Stats.DAMAGE_BLOCKED_BY_SHIELD))<=n.getInt("Before"))return;Entity a=p.level().getEntity(n.getInt("Attacker"));RuneEffects.block(p,a instanceof LivingEntity?(LivingEntity)a:null,org.slavicmyths.item.ItemState.snapshot(p.registryAccess(),n.getCompound("Shield")));
47:   double damage=arrow.getBaseDamage(),speed=1;
48:   if(arrow.isCritArrow()&&PathData.has(p,"aim"))damage*=1.1;
49:   if(arrow.isCritArrow()&&PathData.has(p,"precision")&&PathData.ready(p,"precision",400))damage*=1.2;
50:   if(Runes.has(bow,"midday")&&arrow.isCritArrow()&&RuneEffects.day(p))damage*=RuneDefinition.n("midday","bow_multiplier");
51:   if(Runes.has(bow,"shadow")&&RuneEffects.dark(p))damage*=RuneDefinition.n("shadow","bow_multiplier");
53:   speed=1+Math.min(RuneBalance.PROJECTILE_CAP,speed-1+RuneRuntime.projectileBonus(bow));arrow.setBaseDamage(damage);arrow.setDeltaMovement(arrow.getDeltaMovement().scale(speed));
56:  @SubscribeEvent public static void hurt(LivingIncomingDamageEvent e){
74:     PathData.data(p).remove("Sweep");int n=0;for(LivingEntity x:p.level().getEntitiesOfClass(LivingEntity.class,target.getBoundingBox().inflate(1.5),x->x!=target&&Abilities.canHit(p,x)&&p.distanceToSqr(x)<16)){x.hurt(p.damageSources().indirectMagic(p,p),2);if(++n>=3)break;}
79:  @SubscribeEvent public static void death(LivingDeathEvent e){if(e.getEntity().level().isClientSide||!(e.getSource().getEntity() instanceof Player))return;Player p=(Player)e.getSource().getEntity();if(e.getSource().getDirectEntity()==p&&!Runes.armor(p.getMainHandItem())&&Runes.has(p.getMainHandItem(),"life")&&PathData.ready(p,"rune_life",RuneDefinition.i("life","kill_cooldown")))p.heal(RuneDefinition.i("life","kill_healing"));}
80:  @SubscribeEvent public static void food(LivingEntityUseItemEvent.Finish e){if(!(e.getEntity() instanceof Player)||e.getEntity().level().isClientSide)return;Player p=(Player)e.getEntity();Item item=e.getItem().getItem();if(item!=ModItems.BERRY_MORS.get()&&item!=ModItems.FOREST_MIX.get())return;if(PathData.has(p,"herbs"))p.addEffect(new MobEffectInstance(MobEffects.REGENERATION,60,0));if(item==ModItems.BERRY_MORS.get()&&PathData.has(p,"healing_brew")&&PathData.ready(p,"brew",200))p.heal(2);}
```

## src/main/java/org/slavicmyths/rpg/RuneEffects.java

```java
8: import net.minecraft.world.damagesource.DamageSource;
10: import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
16:  public static boolean legacyFireReady(Player p){return proc(p,"heat",RuneDefinition.i("heat","hit_cooldown"));}
17:  public static void hit(Player p,LivingEntity target,ItemStack item,LivingIncomingDamageEvent e,boolean arrow,boolean first){
19:   if(Runes.has(item,"thunder")&&(!type.equals("dagger")||first)&&proc(p,"thunder",RuneDefinition.i("thunder","hit_cooldown"))){
20:    net.minecraft.nbt.CompoundTag shock=new net.minecraft.nbt.CompoundTag();shock.putInt("Target",target.getId());shock.putUUID("UUID",target.getUUID());shock.putString("Dimension",p.level().dimension().location().toString());shock.putFloat("Damage",type.equals("heavy")?RuneDefinition.i("thunder","heavy_damage"):type.equals("dagger")?RuneDefinition.i("thunder","dagger_damage"):RuneDefinition.f("thunder","hit_damage"));p.getPersistentData().put("SlavicShock",shock);Abilities.particles(p,target);
21:    if(type.equals("sword"))for(LivingEntity next:p.level().getEntitiesOfClass(LivingEntity.class,target.getBoundingBox().inflate(RuneDefinition.i("thunder","chain_radius")),x->x!=target&&Abilities.canHit(p,x)&&x instanceof Enemy)){next.hurt(p.damageSources().indirectMagic(p,p),RuneDefinition.i("thunder","chain_damage"));Abilities.particles(p,next);break;}
23:   if(!RuneRework.melee(item)&&RuneRework.legacyCopies(item,"heat")>0&&proc(p,"heat",RuneDefinition.i("heat","hit_cooldown")))target.igniteForSeconds(RuneDefinition.i("heat","fire_seconds"));
24:   if(!arrow&&Runes.has(item,"forest")&&forest(p)&&target instanceof Enemy)e.setAmount(e.getAmount()+RuneDefinition.f("forest","melee_damage"));
25:   if(!arrow&&Runes.has(item,"midday")&&day(p))e.setAmount(e.getAmount()+RuneDefinition.f("midday","melee_damage"));
27:    if(type.equals("dagger")&&first)e.setAmount(e.getAmount()+RuneDefinition.i("shadow","dagger_damage"));
28:    else if(type.equals("sword")&&proc(p,"shadow",RuneDefinition.i("shadow","speed_cooldown")))p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,RuneDefinition.i("shadow","speed_duration"),RuneDefinition.i("shadow","speed_level")));
30:   if(Runes.has(item,"protection")&&proc(p,"protection",RuneDefinition.i("protection","hit_cooldown")))p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,RuneDefinition.i("protection","hit_duration"),RuneDefinition.i("protection","hit_level")));
31:   if(RuneRework.legacyCopies(item,"wind")>0&&proc(p,"wind",RuneDefinition.i("wind","hit_cooldown")))target.knockback(type.equals("spear")?RuneDefinition.f("wind","spear_force"):RuneDefinition.f("wind","weapon_force"),p.getX()-target.getX(),p.getZ()-target.getZ());
39:   LivingEntity target=(LivingEntity)entity;int previous=target.invulnerableTime;
42:   target.invulnerableTime=0;
43:   try{target.hurt(p.damageSources().indirectMagic(p,p),Math.min(2,Math.max(0,n.getFloat("Damage"))));}
44:   finally{target.invulnerableTime=Math.max(previous,target.invulnerableTime);}
48:   if(Runes.has(shield,"protection")&&proc(p,"protection",RuneDefinition.i("protection","shield_cooldown")))p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,RuneDefinition.i("protection","shield_duration"),RuneDefinition.i("protection","shield_level")));
49:   if(Runes.has(shield,"forest")&&forest(p)&&proc(p,"forest",RuneDefinition.i("forest","shield_cooldown")))p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,RuneDefinition.i("forest","shield_duration"),RuneDefinition.i("forest","shield_level")));
50:   if(Runes.has(shield,"midday")&&day(p)&&proc(p,"midday",RuneDefinition.i("midday","shield_cooldown")))p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,RuneDefinition.i("midday","shield_duration"),RuneDefinition.i("midday","shield_level")));
51:   if(Runes.has(shield,"life")&&p.getHealth()<p.getMaxHealth()*RuneDefinition.n("life","shield_health_threshold")&&proc(p,"life",RuneDefinition.i("life","shield_cooldown")))p.addEffect(new MobEffectInstance(MobEffects.REGENERATION,RuneDefinition.i("life","shield_regeneration"),RuneDefinition.i("life","shield_level")));
53:   if(Runes.has(shield,"thunder")&&proc(p,"thunder",RuneDefinition.i("thunder","shield_cooldown"))){attacker.hurt(p.damageSources().indirectMagic(p,p),RuneDefinition.f("thunder","shield_damage"));Abilities.particles(p,attacker);}
54:   if(RuneRework.legacyCopies(shield,"heat")>0&&proc(p,"heat",RuneDefinition.i("heat","shield_cooldown")))attacker.igniteForSeconds(RuneDefinition.i("heat","shield_fire_seconds"));
55:   if(RuneRework.legacyCopies(shield,"wind")>0&&proc(p,"wind",RuneDefinition.i("wind","shield_cooldown")))attacker.knockback(RuneDefinition.f("wind","shield_force"),p.getX()-attacker.getX(),p.getZ()-attacker.getZ());
58:   if(RuneRework.legacyCopies(staff,"heat")>0&&proc(p,"heat",RuneDefinition.i("heat","staff_cooldown")))target.igniteForSeconds(RuneDefinition.i("heat","staff_fire_seconds"));
59:   if(RuneRework.legacyCopies(staff,"wind")>0&&proc(p,"wind",RuneDefinition.i("wind","staff_cooldown")))target.knockback(RuneDefinition.f("wind","staff_force"),p.getX()-target.getX(),p.getZ()-target.getZ());
60:   if(Runes.has(staff,"forest")&&proc(p,"forest",RuneDefinition.i("forest","staff_cooldown")))p.addEffect(new MobEffectInstance(MobEffects.REGENERATION,RuneDefinition.i("forest","staff_regeneration"),RuneDefinition.i("forest","staff_level")));
61:   if(Runes.has(staff,"life")&&proc(p,"life",RuneDefinition.i("life","staff_cooldown")))p.heal(RuneDefinition.i("life","staff_healing"));
62:   if(Runes.has(staff,"protection")&&proc(p,"protection",RuneDefinition.i("protection","staff_cooldown")))p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,RuneDefinition.i("protection","staff_duration"),RuneDefinition.i("protection","staff_level")));
```

## src/main/java/org/slavicmyths/rpg/RuneRuntime.java

```java
7: import net.minecraft.world.entity.ai.attributes.*;
10: import net.minecraft.tags.DamageTypeTags;
11: import net.minecraft.world.damagesource.DamageTypes;
23:     private static final ResourceLocation DAMAGE=id("damage"),ATTACK=id("attack"),MOVE=id("movement"),TOUGH=id("toughness"),KB=id("knockback"),JUMP=id("jump");
31:         modifier(e,Attributes.ATTACK_DAMAGE,DAMAGE,Math.min(RuneBalance.DAMAGE_CAP,RuneBalance.STRENGTH*RuneBalance.effective(strength)+RuneBalance.CRUSHING_DAMAGE*RuneBalance.effective(crush)),AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
32:         double attack=RuneBalance.attackFactor(speed,crush)-1;var a=e.getAttribute(Attributes.ATTACK_SPEED);if(a!=null){a.removeModifier(ATTACK);attack=RuneBalance.safeAttackFactor(a.getValue(),speed,crush)-1;}
33:         modifier(e,Attributes.ATTACK_SPEED,ATTACK,attack,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
34:         modifier(e,Attributes.MOVEMENT_SPEED,MOVE,RuneBalance.movement(bootSpeed,wind,fort),AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
35:         modifier(e,Attributes.ARMOR_TOUGHNESS,TOUGH,RuneBalance.toughness(res,fort),AttributeModifier.Operation.ADD_VALUE);
36:         modifier(e,Attributes.KNOCKBACK_RESISTANCE,KB,RuneBalance.bonus(RuneBalance.FORT_KB,fort,RuneBalance.KB_CAP),AttributeModifier.Operation.ADD_VALUE);
37:         modifier(e,Attributes.JUMP_STRENGTH,JUMP,RuneBalance.bonus(RuneBalance.WIND_JUMP,wind,RuneBalance.JUMP_CAP),AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
42:     @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){refresh(e.getEntity());}
44:     @SubscribeEvent public static void fireDefense(LivingDamageEvent.Pre e){if(e.getEntity().level().isClientSide||!e.getSource().is(DamageTypeTags.IS_FIRE)||e.getNewDamage()<=0)return;
45:         double reduction=RuneBalance.bonus(RuneBalance.FIRE_REDUCTION,armorCount(e.getEntity(),"heat"),RuneBalance.FIRE_CAP);e.setNewDamage((float)(e.getNewDamage()*(1-reduction)));
47:     @SubscribeEvent public static void hit(LivingDamageEvent.Post e){if(e.getNewDamage()<=0||!e.getSource().is(DamageTypes.PLAYER_ATTACK)||!(e.getSource().getDirectEntity() instanceof ServerPlayer p))return;
55:             if(target.getRemainingFireTicks()<fireSeconds*20)target.igniteForSeconds(fireSeconds);
57:         if(blood>0&&!target.getType().is(CutEffect.IMMUNE)&&p.getRandom().nextDouble()<RuneBalance.bonus(RuneBalance.BLOOD_CHANCE,blood,RuneBalance.PROC_CAP)){
58:             RareBleedEffect.applyRune(p,target);if(target.level() instanceof ServerLevel w)w.sendParticles(new DustParticleOptions(new org.joml.Vector3f(.4F,.06F,.07F),1),target.getX(),target.getEyeY(),target.getZ(),3,.1,.1,.1,0);
62:     @SubscribeEvent public static void shield(LivingShieldBlockEvent e){if(e.getEntity().level().isClientSide||!e.getBlocked()||e.getBlockedDamage()<=0)return;ItemStack s=e.getEntity().getUseItem();if(!(s.getItem() instanceof ShieldItem))return;
63:         double reduction=Math.min(RuneBalance.SHIELD_CAP,RuneBalance.SHIELD_EFFICIENCY*(RuneBalance.effective(count(s,"resilience"))+RuneBalance.effective(count(s,"fortitude"))));e.setShieldDamage((float)(e.shieldDamage()*(1-reduction)));
```

## src/main/java/org/slavicmyths/rpg/RuneVisual.java

```java
9: import net.minecraft.world.entity.ai.attributes.Attributes;
16: import org.slavicmyths.combat.MythDamageSources;
23:     private UUID owner;private long expires;private int lifetime,attacks,dissolving;public int animationAge;private float damage;private LivingEntity target;private Vec3 start=Vec3.ZERO,aim=Vec3.ZERO;
27:     public static RuneVisual weapon(ServerPlayer p){var v=new RuneVisual(TYPE.get(),p.level());v.owner=p.getUUID();v.expires=RareRuneRuntime.now(p)+120;v.entityData.set(OWNER,p.getId());v.entityData.set(ITEM,p.getMainHandItem().copyWithCount(1));v.damage=(float)(p.getAttributeValue(Attributes.ATTACK_DAMAGE)*RareRuneRuntime.meleeMultiplier(p)*.70);v.setPos(p.position().add(shoulder(p)));return v;}
37:     private LivingEntity target(ServerPlayer p){var last=p.getLastHurtMob();if(valid(p,last))return last;return p.level().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(12),t->org.slavicmyths.combat.CombatTargeting.isHostileCombatTarget(p,t)&&valid(p,t)).stream().min(java.util.Comparator.comparingDouble(p::distanceToSqr)).orElse(null);}
48:         else if(cycle<12){entityData.set(PHASE,2);setPos(start.lerp(aim,(cycle-5)/6.0));if(cycle==11&&target!=null){attacks++;if(valid(p,target)&&position().distanceToSqr(target.position().add(0,target.getBbHeight()*.6,0))<4){target.hurt(MythDamageSources.echo(this,p),damage);((ServerLevel)level()).sendParticles(ParticleTypes.ENCHANT,getX(),getY(),getZ(),3,.1,.1,.1,0);}}}
```

## src/main/java/org/slavicmyths/sanctuary/FlameVial.java

```java
12:  @Override protected void onHit(HitResult hit){super.onHit(hit);if(!(level() instanceof ServerLevel w))return;setPos(hit.getLocation().add(hit instanceof BlockHitResult block?net.minecraft.world.phys.Vec3.atLowerCornerOf(block.getDirection().getNormal()).scale(.1):Vec3.ZERO));if(getOwner() instanceof LivingEntity owner&&owner.isAlive())for(var t:w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(2.5),t->t instanceof Mob&&(org.slavicmyths.morozko.FrostWorld.hostile(t,owner)||org.slavicmyths.bogatyr.HeroThreats.blighted(t))&&!(t instanceof org.slavicmyths.karachun.NightRaven r&&r.allied())&&org.slavicmyths.morozko.FrostWorld.bboxDistance(position(),t.getBoundingBox())<=6.25&&w.clip(new net.minecraft.world.level.ClipContext(position(),t.getBoundingBox().getCenter(),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,this)).getType()==HitResult.Type.MISS)){boolean mor=t instanceof org.slavicmyths.entity.LandSpiritEntity m&&m.morState()>0;float damage=(float)(SanctuaryConfig.FLAME_DAMAGE.get()*(mor?1.25:1));if(t.hurt(damageSources().thrown(this,owner),damage))t.igniteForSeconds(3);}w.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,getX(),getY(),getZ(),24,.8,.25,.8,.04);SanctuaryContent.sound(w,blockPosition(),"vial_hit");discard();}
```

## src/main/java/org/slavicmyths/sanctuary/SanctuaryEvents.java

```java
8:  @SubscribeEvent public static void damage(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Pre e){if(e.getEntity().hasEffect(SanctuaryContent.GUARD)&&(e.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FIRE)||e.getSource().getMsgId().equals("mor_block")))e.setNewDamage((float)(e.getNewDamage()*(1-SanctuaryConfig.GUARD.get())));}
```

## src/main/java/org/slavicmyths/serpenthill/HillRules.java

```java
4:  public static final float AURA_DAMAGE=3;
```

## src/main/java/org/slavicmyths/sivka/SivkaContent.java

```java
7: import net.minecraft.world.entity.ai.attributes.Attributes;
20:     public static final DeferredHolder<Item,Item> EGG=ModItems.ITEMS.register("sivka_burka_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(ENTITY,0xf4e9d8,0xdf8b2a,new Item.Properties()));
27:     @SubscribeEvent public static void attributes(EntityAttributeCreationEvent e){e.put(ENTITY.get(),net.minecraft.world.entity.PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH,80).add(Attributes.MOVEMENT_SPEED,.24).add(Attributes.FOLLOW_RANGE,24).add(Attributes.KNOCKBACK_RESISTANCE,.3).add(Attributes.STEP_HEIGHT,1).build());}
28:     @SubscribeEvent public static void placements(RegisterSpawnPlacementsEvent e){e.register(ENTITY.get(),SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(type,w,reason,p,r)->{
29:         if(reason!=MobSpawnType.SPAWN_EGG&&r.nextFloat()>=.002F)return false;
35:     },RegisterSpawnPlacementsEvent.Operation.REPLACE);}
```

## src/main/java/org/slavicmyths/sivka/SivkaEntity.java

```java
12: import net.minecraft.world.damagesource.DamageSource;
30:     public SivkaEntity(EntityType<? extends SivkaEntity> type,Level w){super(type,w);setPersistenceRequired();setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.WATER,-1);setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.DANGER_FIRE,-1);setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.DAMAGE_FIRE,-1);}
55:         if(flying()&&level() instanceof ServerLevel){rider.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,80,0,false,true));}
56:         for(int radius=2;radius<=4;radius++)for(int i=0;i<8;i++){double a=i*Math.PI/4;var p=position().add(Math.sin(a)*radius,0,Math.cos(a)*radius);var q=BlockPos.containing(p);if(level().hasChunkAt(q)&&SivkaWhistle.safeFloor(level(),q.below())&&level().noCollision(rider,rider.getBoundingBox().move(p.subtract(rider.position()))))return p;}
61:     private boolean headClear(double dy){var box=getBoundingBox().inflate(.6,0,.85).expandTowards(0,dy,0);return level().noCollision(this,box)&&level().hasChunkAt(BlockPos.containing(box.maxX,box.maxY,box.maxZ))&&level().hasChunkAt(BlockPos.containing(box.minX,box.minY,box.minZ));}
87:         if((vx!=0||vz!=0)&&!level().noCollision(this,getBoundingBox().inflate(.35,0,.65).move(vx,Math.max(0,vy),vz))){vx=vz=0;if(flying())vy=Math.min(vy,0);}
96:             if(tickCount%20==0){b.health=getHealth();b.last=blockPosition();d.setDirty();}
117:     @Override public boolean hurt(DamageSource source,float amount){if(!level().isClientSide){scared=400;react(8,10);cue("whinny_alarm",.8F);
118:         if(owner!=null&&amount>=getHealth()&&!source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)&&level() instanceof ServerLevel w){var d=SivkaBonds.get(w);var b=d.bonds.get(owner);if(b!=null){b.recovery=b.play+6000;b.health=80;b.entity=null;b.generation++;d.setDirty();ejectPassengers();spark(8);cue("depart",.5F);discard();return true;}}
119:     }return super.hurt(source,amount);}
120:     @Override public boolean causeFallDamage(float distance,float multiplier,DamageSource source){return flying()||mode()==3?false:super.causeFallDamage(distance,multiplier,source);}
122:     @Override public void die(DamageSource source){super.die(source);if(owner!=null&&level() instanceof ServerLevel w){var d=SivkaBonds.get(w);var b=d.bonds.get(owner);if(b!=null&&getUUID().equals(b.entity)){b.entity=null;b.recovery=b.play+6000;b.health=80;d.setDirty();}}}
```

## src/main/java/org/slavicmyths/sivka/SivkaRules.java

```java
6:     public static final float WIDTH=1.65F, HEIGHT=2.85F, HEALTH=80;
```

## src/main/java/org/slavicmyths/skim/AlyoshaArrow.java

```java
7: /** Ordinary non-explosive bow shot, bounded to18b, never damages peaceful or player allies. */
11:  public AlyoshaArrow(Level w,AlyoshaEntity owner){this(SkimContent.ARROW.get(),w);setOwner(owner);setPos(owner.getX(),owner.getEyeY()-.1,owner.getZ());launch=position();setBaseDamage(.8);pickup=Pickup.DISALLOWED;}
```

## src/main/java/org/slavicmyths/skim/AlyoshaEntity.java

```java
10: import net.minecraft.world.damagesource.DamageSource;
23:  public boolean evade(DamageSource s){if(action()==SkimAction.DODGE&&age()>=4&&age()<=7&&s.getDirectEntity() instanceof org.slavicmyths.kalin.KalinEntity&&s.is(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,org.slavicmyths.kalin.KalinContent.id("kalin_sabre"))))return true;return action()==SkimAction.DODGE&&age()>=4&&age()<=7&&s.getDirectEntity() instanceof SkimBeastEntity b&&(b.action()==SkimAction.CLAWS||b.action()==SkimAction.FLANK||b.action()==SkimAction.GRIP);}
29:   var kb=org.slavicmyths.kalin.KalinParty.commander(this);if(kb!=null&&tickCount%5==0&&ready("dodge")&&action()==SkimAction.IDLE&&kb.action()==org.slavicmyths.finist.FalconAction.SWORD&&kb.age()<10&&edge(kb)<6&&planDodge(w)){cooldown("dodge",100);begin(SkimAction.DODGE);cue("alyosha.dodge_swish_1");}
30:   if(b!=null){totalCombat++;if(action()==SkimAction.BOW)bowTicks++;if(tickCount%5==0&&b.heavy()&&b.getTarget()==this&&action()!=SkimAction.DODGE&&ready("dodge")&&edge(b)>=2&&edge(b)<=9&&((b.action()==SkimAction.LEAP&&b.age()>=12&&b.age()<=20)||(b.action()!=SkimAction.LEAP&&b.age()<15))&&planDodge(w)){cooldown("dodge",36);dodgedBoss=b.getUUID();begin(SkimAction.DODGE);cue("alyosha.dodge_swish_"+(random.nextBoolean()?1:2));}}step(w,b);if(duration()>0&&age()>=duration()){if(action()==SkimAction.DODGE&&b!=null&&dodgedBoss!=null&&ready("counter")&&edge(b)<3&&b.landing().distSqr(blockPosition())>9){cooldown("counter",100);begin(SkimAction.COUNTER);b.addThreat(this,12);}else begin(SkimAction.IDLE);}if(action()!=SkimAction.IDLE)return;
33:   if(b!=null&&b.held()==p&&distanceToSqr(b)<64&&ready("rescue")){if(edge(b)>2.5){getNavigation().moveTo(b,1.3);return;}cooldown("rescue",100);begin(SkimAction.SABER);return;}
35:   if(ready("taunt")&&edge(target)<14&&hasLineOfSight(target)){cooldown("taunt",240);begin(SkimAction.TAUNT);return;}
36:   if(edge(target)<=2.5&&ready("saber")){cooldown("saber",76);begin(SkimAction.SABER);return;}if(edge(target)>=2&&edge(target)<=4.5&&ready("feint")){cooldown("feint",160);begin(SkimAction.FEINT);cue("alyosha.feint");return;}
37:   if(edge(target)>=6&&edge(target)<=18&&ready("bow")&&hasLineOfSight(target)&&(b==null||bowTicks+35<Math.max(240,totalCombat)*.15)){cooldown("bow",200);begin(SkimAction.BOW);cue("alyosha.bow_draw");return;}
42:  private void strike(float raw,SkimBeastEntity b){var t=getTarget();if(t!=null&&t.isAlive()&&cone(t,2.5,100)&&hasLineOfSight(t)){t.invulnerableTime=0;if(t instanceof org.slavicmyths.poloz.GreatPoloz poloz?org.slavicmyths.poloz.PolozAim.strike(this,poloz,raw):t.hurt(damageSources().mobAttack(this),raw)){cue("alyosha.saber_hit_1");if(level().getGameTime()-lastHit>120)accurate=0;lastHit=level().getGameTime();if(++accurate>=3&&ready("daring")){cooldown("daring",400);daringUntil=level().getGameTime()+80;accurate=0;}}}if(b!=null&&b.held()!=null&&b.held()!=this&&cone(b,2.5,100)&&hasLineOfSight(b)&&(frame(6)||frame(11)))b.rescue(8);}
44:  case TAUNT->{if(t==11){var k=org.slavicmyths.kalin.KalinParty.commander(this);if(k!=null&&distanceToSqr(k)<196&&hasLineOfSight(k)){k.getPersistentData().putUUID("AlyoshaTaunt",getUUID());k.getPersistentData().putLong("AlyoshaTauntUntil",w.getGameTime()+90);k.setTarget(this);}if(b!=null)b.taunt(this);int count=0;for(var m:w.getEntitiesOfClass(Monster.class,getBoundingBox().inflate(14),e->e.isAlive()&&!isAlliedTo(e)&&hasLineOfSight(e)&&(e.getTarget()==this||e.getTarget()==w.getServer().getPlayerList().getPlayer(owner)))){if(count++>=6)break;m.getPersistentData().putUUID("AlyoshaTaunt",getUUID());m.getPersistentData().putLong("AlyoshaTauntUntil",w.getGameTime()+90);m.setTarget(this);}cue("alyosha.taunt_"+(random.nextBoolean()?1:2));}}
46:  case SABER->{if(frame(3)||frame(9)||frame(14)||frame(18))cue("alyosha.saber_swing_"+(random.nextBoolean()?1:2));if(frame(6)||frame(11))strike(2,b);if(frame(16))strike(2.4F,b);if(frame(19))strike(2.6F,b);}
47:  case FEINT->{if(t==14){if(b==null||b.action()==SkimAction.RECOVER||b.action()==SkimAction.EXHAUSTED){strike(4,b);if(b!=null)b.fatigue(12);}}}
48:  case COUNTER->{if(frame(8)){strike(4,b);if(b!=null)b.fatigue(12);cue("alyosha.counter");}}
53:  @Override public void die(DamageSource s){if(owner!=null&&level() instanceof ServerLevel w){HeroCalls.get(w).alyosha.remove(owner,getUUID());HeroCalls.get(w).setDirty();}super.die(s);}
```

## src/main/java/org/slavicmyths/skim/SkimActor.java

```java
13:  protected final Map<String,Long> cooldowns=new HashMap<>();
21:  public boolean ready(String k){return level().getGameTime()>=cooldowns.getOrDefault(k,0L);}public void cooldown(String k,int t){cooldowns.put(k,level().getGameTime()+t);}
30:  @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);var c=new CompoundTag();cooldowns.forEach(c::putLong);n.put("SkimCooldowns",c);n.putString("SkimAction",action().name());}
31:  @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);cooldowns.clear();var c=n.getCompound("SkimCooldowns");for(var k:c.getAllKeys())cooldowns.put(k,c.getLong(k));begin(SkimAction.IDLE);}
```

## src/main/java/org/slavicmyths/skim/SkimBeastEntity.java

```java
10: import net.minecraft.world.damagesource.*;
15: /** Finite attacks, encounter-local threat, locked leap and resumable fatigue. No passive contact damage. */
35:  public void fatigue(int value){if(ready("allyFatigue")){stamina=Math.max(0,stamina-value);cooldown("allyFatigue",100);}}
38:  private List<LivingEntity> candidates(ServerLevel w){return w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(36),this::valid);}
52:   if(phase>=2&&d<2.5&&ready("grip")&&now>=majorAt){attack(SkimAction.GRIP,"grip",260);return;}
53:   if(phase>=2&&ready("whistle")&&now>=majorAt&&d<18&&hasLineOfSight(t)){attack(SkimAction.WHISTLE,"whistle",210);return;}
54:   if(phase>=2&&ready("roar")&&now>=majorAt&&d<7){attack(SkimAction.ROAR,"roar",190);return;}
55:   if(ready("leap")&&now>=majorAt&&d>=4&&d<=14){attack(SkimAction.LEAP,"leap",150);return;}
56:   if(phase>=2&&ready("flank")&&now>=majorAt&&d>=4&&d<=9){attack(SkimAction.FLANK,"flank",165);flank=heading().scale(.34).add(new Vec3(heading().z,0,-heading().x).scale(random.nextBoolean()?.28:-.28));return;}
57:   if(d<=3.3&&ready("claws")){attack(SkimAction.CLAWS,"claws",84);return;}getNavigation().moveTo(t,phase==3?1.32:1.18);
58:   if(tickCount%14==0&&getDeltaMovement().horizontalDistanceSqr()>.001)cue("skim.step_"+(tickCount%28==0?1:2));if(ready("ambient")){cooldown("ambient",180+random.nextInt(141));cue("skim.ambient_"+(random.nextBoolean()?1:2));}
60:  public void attack(SkimAction a,String k,int cd){cooldown(k,(int)Math.ceil(cd*(phase==3?.87:1)));if(a==SkimAction.ROAR)entityData.set(REAR,level().noCollision(this,new AABB(getX()-1.4,getY()+2.95,getZ()-2.6,getX()+1.4,getY()+4.7,getZ()+2.6)));if(a!=SkimAction.CLAWS)majorAt=level().getGameTime()+(phase==1?140:20)+a.ticks;begin(a);}
61:  public DamageSource damage(String k){return new DamageSource(level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(net.minecraft.resources.ResourceKey.create(Registries.DAMAGE_TYPE,SkimContent.id("skim_"+k))),this);}
63:  private void hit(LivingEntity e,String kind,double net){if(!valid(e)||!hasLineOfSight(e))return;e.invulnerableTime=0;e.hurt(damage(kind),raw(net));}
64:  private int strike(ServerLevel w,double reach,double angle,String k,double net){int hits=0;for(var e:w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(reach),this::valid))if(cone(e,reach,angle)&&hasLineOfSight(e)){hit(e,k,net);hits++;}if(hits>0)cue("skim.claw_hit");return hits;}
66:   case CLAWS->{if(t==5)cue("skim.claw_windup");if(t==11||t==19)strike(w,3.3,125,"claw",2.5);if(t==30){int n=strike(w,3.8,125,"claw",6.1);if(n==0)miss();}}
69:     if(rock){release();begin(SkimAction.STAGGER);cooldown("stagger",80);cue("skim.stone_collision");return;}move(MoverType.SELF,delta);setDeltaMovement(Vec3.ZERO);fallDistance=0;
71:   case WHISTLE->{if(t==9)cue("skim.whistle_charge");if(t==22){for(var e:w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(18),this::valid))if(cone(e,18,75)&&hasLineOfSight(e)){hit(e,"whistle",1);e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,60));e.addEffect(new MobEffectInstance(MobEffects.CONFUSION,30));}cue("skim.whistle_release");}}
72:   case ROAR->{if(t==26)cue("skim.roar");if(t==28){for(var e:w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(7),this::valid))if(hasLineOfSight(e)&&edge(e)<=7){if(cone(e,4,115))hit(e,"roar",6.1);else hit(e,"roar",3);e.knockback(.45,getX()-e.getX(),getZ()-e.getZ());}}}
73:   case FLANK->{if(t>=11&&t<=22&&flank!=null){if(w.noCollision(this,getBoundingBox().move(flank)))move(MoverType.SELF,flank);if(valid(target))face(target);}if(t==24&&strike(w,3.8,90,"flank",6.1)==0)miss();}
83:  public void rescue(float amount){if(held()==null)return;rescue+=amount;if(rescue>=16){var e=held();release();if(e!=null){e.setDeltaMovement(heading().scale(.3).add(0,.1,0));e.hurtMarked=true;}begin(SkimAction.RECOVER);cooldown("stagger",80);}}
84:  public void registeredHit(DamageSource s,float raw,float actual){if(actual<=0)return;if(s.getEntity() instanceof ServerPlayer p&&!p.isCreative()&&!p.isSpectator()&&valid(p)){contributors.add(p.getUUID());addThreat(p,s.getDirectEntity()==p?8:5);}else if(s.getEntity() instanceof AlyoshaEntity a)addThreat(a,s.getDirectEntity()==a?6:4);var direct=s.getDirectEntity();if(held()!=null&&direct!=null&&direct.getY()>=getY()&&direct.position().distanceToSqr(position().add(heading().scale(2.5)).add(0,1.4,0))<16)rescue(raw);}
85:  @Override public boolean hurt(DamageSource s,float amount){boolean hit=super.hurt(s,amount);if(hit&&ready("hurt")){cooldown("hurt",14);cue("skim.hurt_"+(random.nextBoolean()?1:2));}return hit;
87:  @Override public void die(DamageSource s){if(!recorded&&level() instanceof ServerLevel w){recorded=true;release();if(s.getEntity() instanceof ServerPlayer hitter&&!hitter.isCreative()&&!hitter.isSpectator()&&valid(hitter))contributors.add(hitter.getUUID());if(active()&&!s.is(net.minecraft.world.damagesource.DamageTypes.GENERIC_KILL)&&((s.getEntity() instanceof ServerPlayer p&&!p.isCreative())||(s.getEntity() instanceof AlyoshaEntity a&&owner!=null&&owner.equals(a.owner))))SkimValley.win(w,valley(),this);else if(active())SkimValley.abort(w,valley());bar.removeAllPlayers();begin(SkimAction.DEATH);cue("skim.death");}super.die(s);}
```

## src/main/java/org/slavicmyths/skim/SkimContent.java

```java
6: import net.minecraft.world.entity.ai.attributes.Attributes;
22:  ModItems.ITEMS.register("skim_beast_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(SKIM,0x3B2A25,0x9A3430,new net.minecraft.world.item.Item.Properties()));
23:  ModItems.ITEMS.register("idolishche_poganoe_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(org.slavicmyths.bogatyr.BogatyrContent.IDOL,0x59413B,0xC1A48A,new net.minecraft.world.item.Item.Properties()));
24:  ModItems.ITEMS.register("marinka_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(org.slavicmyths.marinka.MarinkaContent.MARINKA,0x2F222C,0x9B3351,new net.minecraft.world.item.Item.Properties()));
28:  e.put(ALYOSHA.get(),Mob.createMobAttributes().add(Attributes.MAX_HEALTH,175).add(Attributes.ARMOR,6).add(Attributes.MOVEMENT_SPEED,.34).add(Attributes.FOLLOW_RANGE,28).add(Attributes.KNOCKBACK_RESISTANCE,.12).build());
29:  e.put(SKIM.get(),net.minecraft.world.entity.monster.Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,750).add(Attributes.ARMOR,8).add(Attributes.ARMOR_TOUGHNESS,2).add(Attributes.MOVEMENT_SPEED,.28).add(Attributes.FOLLOW_RANGE,36).add(Attributes.KNOCKBACK_RESISTANCE,.85).build());
```

## src/main/java/org/slavicmyths/skim/SkimEvents.java

```java
11:  @SubscribeEvent public static void incoming(LivingIncomingDamageEvent e){if(e.getEntity().level().isClientSide)return;if(e.getEntity() instanceof AlyoshaEntity a&&a.evade(e.getSource()))e.setCanceled(true);}
12:  @SubscribeEvent public static void post(LivingDamageEvent.Post e){if(e.getEntity() instanceof SkimBeastEntity b)b.registeredHit(e.getSource(),e.getOriginalDamage(),e.getNewDamage());}
13:  @SubscribeEvent public static void pre(LivingDamageEvent.Pre e){if(e.getEntity().level().isClientSide)return;if(e.getEntity() instanceof SkimBeastEntity b&&b.action()==SkimAction.EXHAUSTED)e.setNewDamage(e.getNewDamage()*1.25F);if(e.getEntity() instanceof AlyoshaEntity a&&e.getNewDamage()>0){a.incomingHit();if(e.getNewDamage()>=a.getHealth()&&!a.intercepted&&!e.getSource().is(net.minecraft.world.damagesource.DamageTypes.GENERIC_KILL)){a.intercepted=true;e.setNewDamage(Math.max(0,a.getHealth()-1));a.retreat();}}}
```

## src/main/java/org/slavicmyths/storage/Household.java

```java
10:  public enum Kind {SACK("storage_sack",5,"sack_items"),BASKET("basket",4,"basket_items"),LARGE("large_basket",9,"large_basket_items"),CRATE("produce_crate",9,"crate_items"),CHEST("household_chest",27,""),BARREL("wooden_barrel",9,"barrel_items"),WALL("wall_shelf",3,""),SHELF("storage_shelf",6,""),DRY("drying_rack",4,""),HAY("haystack",0,"");
```

## src/main/java/org/slavicmyths/swamp/BolotnikEntity.java

```java
3: import net.minecraft.world.entity.ai.attributes.*;
12:  public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,48).add(Attributes.MOVEMENT_SPEED,.20).add(Attributes.ATTACK_DAMAGE,7).add(Attributes.ARMOR,4).add(Attributes.KNOCKBACK_RESISTANCE,.4).add(Attributes.FOLLOW_RANGE,24);}
14:  @Override public boolean doHurtTarget(Entity entity){boolean hit=super.doHurtTarget(entity);if(hit&&entity instanceof LivingEntity living&&level().getGameTime()>=nextPower){living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,60,0));nextPower=level().getGameTime()+120;}return hit;}
16:  @Override protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source){return SoundEvents.MUD_HIT;}
```

## src/main/java/org/slavicmyths/textile/Carcass.java

```java
13:     public enum Kind {COW,PIG,SHEEP,RABBIT,GOOSE,DUCK,DOMESTIC_GOAT}
26:     @Override public boolean hurt(net.minecraft.world.damagesource.DamageSource d,float amount){return false;}
36:     private void drop(Item item,int min,int max){int n=min+random.nextInt(max-min+1);if(n>0)spawnAtLocation(new ItemStack(item,n));}
```

## src/main/java/org/slavicmyths/textile/Textiles.java

```java
6: import net.minecraft.world.entity.ai.attributes.*;
19:     public static final DeferredHolder<MobEffect,MobEffect> RESTED=ModEffects.EFFECTS.register("well_rested",()->new MobEffect(MobEffectCategory.BENEFICIAL,0xe3d5b3){}.addAttributeModifier(Attributes.MOVEMENT_SPEED,id("well_rested_speed"),.05,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL).addAttributeModifier(Attributes.BLOCK_BREAK_SPEED,id("well_rested_mining"),.10,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
```

## src/main/java/org/slavicmyths/underground/EncounterMob.java

```java
7: import net.minecraft.world.entity.ai.attributes.*;
19:  public enum Action {IDLE,ALERT,MELEE,MINER_HEAVY,LUNGE,BURROW,THROW,SPIKES,COIN_WAVE,TREMOR,HEAVY,CHARGE,RAGE,RECOVER}
23:  protected int elapsed,duration;protected Vec3 aim=Vec3.ZERO;protected final Map<Action,Integer> cooldowns=new EnumMap<>(Action.class);protected BlockPos home;protected String encounterKey="";
24:  public EncounterMob(EntityType<? extends Monster> t,Level l){super(t,l);setPersistenceRequired();getAttribute(Attributes.STEP_HEIGHT).setBaseValue(1);}
25:  public static AttributeSupplier.Builder attributes(double hp,double speed,double damage,double range,double kb){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,hp).add(Attributes.MOVEMENT_SPEED,speed).add(Attributes.ATTACK_DAMAGE,damage).add(Attributes.FOLLOW_RANGE,range).add(Attributes.KNOCKBACK_RESISTANCE,kb);}
34:  protected boolean ready(Action a){return cooldowns.getOrDefault(a,0)<=0;}
36:  protected void finish(Action a,int cd,int recovery){cooldowns.put(a,cd);begin(Action.RECOVER,recovery);}
37:  public void aiStep(){super.aiStep();if(level().isClientSide||!isAlive())return;if(home==null)bind(blockPosition(),encounterKey);cooldowns.replaceAll((a,n)->Math.max(0,n-1));if(!validTarget(getTarget())){setTarget(null);if(action()!=Action.IDLE&&action()!=Action.RECOVER)cancel();}
42:  protected void melee(float damage,double reach){var t=getTarget();if(validTarget(t)&&distanceToSqr(t)<=reach*reach&&hasLineOfSight(t)&&Math.abs(t.getY()-getY())<1.8)t.hurt(damageSources().mobAttack(this),damage);}
43:  protected List<LivingEntity> victims(Vec3 center,double radius){return level().getEntitiesOfClass(LivingEntity.class,new AABB(center,center).inflate(radius,1.4,radius),t->validTarget(t)&&(t instanceof Player||t instanceof ChudPerson)&&t.distanceToSqr(center)<=radius*radius&&Math.abs(t.getY()-center.y)<1.4&&hasLineOfSight(t)&&!isAlliedTo(t));}
44:  protected void ring(double radius,int count){if(level() instanceof ServerLevel w)for(int i=0;i<count;i++){double a=i*Math.PI*2/count;w.sendParticles(ParticleTypes.POOF,getX()+Math.cos(a)*radius,getY()+.12,getZ()+Math.sin(a)*radius,1,0,0,0,.01);}}
47:  public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(home!=null)n.putLong("EncounterHome",home.asLong());n.putString("EncounterKey",encounterKey);n.putBoolean("EncounterRage",raged());var c=new CompoundTag();cooldowns.forEach((a,v)->c.putInt(a.name(),v));n.put("EncounterCooldowns",c);n.putString("EncounterAction",action().name());}
48:  public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);if(n.contains("EncounterHome"))bind(BlockPos.of(n.getLong("EncounterHome")),n.getString("EncounterKey"));entityData.set(RAGED,n.getBoolean("EncounterRage"));for(Action a:Action.values())cooldowns.put(a,Math.max(0,n.getCompound("EncounterCooldowns").getInt(a.name())));if(!n.getString("EncounterAction").equals("IDLE")){for(Action a:Action.values())cooldowns.merge(a,200,Math::max);}noPhysics=false;setInvisible(false);begin(Action.RECOVER,20);}
49:  public void die(net.minecraft.world.damagesource.DamageSource d){cancel();if(level() instanceof ServerLevel w&&!encounterKey.isEmpty())EncounterPopulation.data(w).defeat(encounterKey);super.die(d);}
```

## src/main/java/org/slavicmyths/underground/EncounterPopulation.java

```java
34:    EntityType<? extends EncounterMob> type=s.kind.equals("kladovik")?UndergroundMobs.KLADOVIK.get():s.kind.equals("large_podzemnik")?UndergroundMobs.LARGE.get():UndergroundMobs.PODZEMNIK.get();var mob=type.create(w);if(mob==null)continue;UUID id=UUID.nameUUIDFromBytes(s.key.getBytes(StandardCharsets.UTF_8));if(w.getEntity(id)!=null){consumed.put(s.key,"SPAWNED");pending.remove(s.key);setDirty();continue;}
37:    candidates:for(int radius=0;radius<=4&&!placed;radius++)for(int dx=-radius;dx<=radius&&!placed;dx++)for(int dz=-radius;dz<=radius&&!placed;dz++){if(Math.max(Math.abs(dx),Math.abs(dz))!=radius)continue;if(candidate++<first)continue;if(testedCandidates++>=4){exhausted=false;break candidates;}cursors.put(s.key,candidate);BlockPos at=s.at.offset(dx,0,dz);boolean safe=true;for(int a=-clear;a<=clear&&safe;a++)for(int b=-clear;b<=clear&&safe;b++){BlockPos p=at.offset(a,0,b);if(!s.room.isInside(p)||!w.getBlockState(p.below()).isCollisionShapeFullBlock(w,p.below())){safe=false;break;}for(int y=0;y<(s.kind.equals("large_podzemnik")?4:2);y++)if((s.kind.equals("kladovik")?Math.abs(a)<=1&&Math.abs(b)<=1:true)&&(!w.getBlockState(p.above(y)).getCollisionShape(w,p.above(y)).isEmpty()||!w.getFluidState(p.above(y)).isEmpty())){safe=false;break;}}if(!safe)continue;mob.setUUID(id);mob.bind(at,s.key);if(mob instanceof Podzemnik p)p.variant(s.variant);mob.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,0,0);if(!w.noCollision(mob)||!w.getEntitiesOfClass(LivingEntity.class,mob.getBoundingBox().inflate(1.5)).isEmpty())continue;placed=w.addFreshEntity(mob);}
38:    if(!placed&&!exhausted){pending.remove(s.key);pending.put(s.key,s);setDirty();continue;}cursors.remove(s.key);if(mob instanceof RoomBoss&&!placed)com.mojang.logging.LogUtils.getLogger().info("Underground boss skipped: {} no safe arena in {}",s.key,s.room);consumed.put(s.key,placed?"SPAWNED":"SKIPPED_NO_SAFE_FLOOR");pending.remove(s.key);setDirty();
```

## src/main/java/org/slavicmyths/underground/EncounterRules.java

```java
4:  public static final float HEAVY_DAMAGE=32,STONE_DAMAGE=5.5F;
```

## src/main/java/org/slavicmyths/underground/Kladovik.java

```java
10: import net.minecraft.world.entity.ai.attributes.Attributes;
16:   super.aiStep();if(!level().isClientSide){if(getTarget()==null||!hasLineOfSight(getTarget()))lost++;else lost=0;if(lost>=300){setTarget(null);if(home!=null)getNavigation().moveTo(home.getX()+.5,home.getY(),home.getZ()+.5,.8);}if(getHealth()<=100&&!raged()){rage();getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.3);sound("rage",1);}}
27:   case BURROW->{if(elapsed==1)sound("burrow",1);if(elapsed<20&&elapsed%5==0)dust(position(),2);if(elapsed==20)noPhysics=true;if(elapsed>=20&&elapsed<60){if(safeEmerge){Vec3 p=start.lerp(emerge,Math.min(1,(elapsed-20)/30.0));setPos(p.x,p.y,p.z);}if(elapsed%5==0)dust(position(),2);}if(elapsed>=60&&elapsed<75&&elapsed%5==0)dust(position(),4);if(elapsed==75){noPhysics=false;sound("impact",1);}if(elapsed==80&&safeEmerge){for(var t:victims(position(),1.6)){if(t.hurt(damageSources().mobAttack(this),9))t.push(0,.28,0);}dust(position(),8);}if(elapsed==duration){noPhysics=false;finish(Action.BURROW,raged()?200:270,20);}}
28:   case SPIKES->{if(elapsed>=8&&elapsed<26&&elapsed%6==0)for(var p:spikePoints)dust(p.getCenter().add(0,-.5,0),2);if(elapsed==26){Set<UUID> hit=new HashSet<>();for(var p:spikePoints){var fx=UndergroundMobs.SPIKE.get().create(level());if(fx!=null){fx.owner=getUUID();fx.moveTo(p.getX()+.5,p.getY(),p.getZ()+.5,0,0);level().addFreshEntity(fx);}for(var t:victims(new Vec3(p.getX()+.5,p.getY(),p.getZ()+.5),.85))if(hit.add(t.getUUID())){t.hurt(damageSources().mobAttack(this),6);t.push(0,.2,0);t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,20,0));}}sound("impact",1);}if(elapsed==duration)finish(Action.SPIKES,250,16);}
29:   case COIN_WAVE->{if(elapsed==1){sound("coins",.8F);ring(4,20);}if(elapsed==20){for(var t:victims(position(),4)){t.hurt(damageSources().mobAttack(this),1);Vec3 v=t.position().subtract(position()).normalize();t.push(v.x*.8,.15,v.z*.8);t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40,0));}dust(position(),24);}if(elapsed==duration)finish(Action.COIN_WAVE,raged()?240:320,16);}
34:  public boolean isInvulnerableTo(net.minecraft.world.damagesource.DamageSource d){return action()==Action.BURROW&&age(0)>=20&&age(0)<75&&!d.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)||super.isInvulnerableTo(d);}
36:  public void die(net.minecraft.world.damagesource.DamageSource d){if(!level().isClientSide){for(var s:level().getEntitiesOfClass(ThrownStone.class,getBoundingBox().inflate(40),s->s.getOwner()==this))s.discard();for(var s:level().getEntitiesOfClass(StoneSpike.class,getBoundingBox().inflate(40),s->getUUID().equals(s.owner)))s.discard();}super.die(d);}
```

## src/main/java/org/slavicmyths/underground/LargePodzemnik.java

```java
8: import net.minecraft.world.entity.ai.attributes.Attributes;
14:  public void aiStep(){super.aiStep();if(!level().isClientSide&&isAlive()&&getHealth()<=90&&!raged()&&action()==Action.IDLE){rage();getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.242);begin(Action.RAGE,30);sound("rage",.8F);}}
20:   case HEAVY->{int impact=raged()?32:38;if(elapsed==impact-12){if(validTarget(getTarget()))aim=getTarget().position();heading=new Vec3(aim.x-getX(),0,aim.z-getZ()).normalize();}if(elapsed==impact){sound("impact",.65F);dust(position().add(heading.scale(2)),40);for(var t:victims(position(),3.1)){Vec3 d=t.position().subtract(position()).normalize();if(EncounterRules.inCone(d.dot(heading),distanceTo(t),OminousAura.knocked(t)))t.hurt(damageSources().mobAttack(this),EncounterRules.HEAVY_DAMAGE);}}if(elapsed==duration)finish(Action.HEAVY,raged()?250:320,1);}
21:   case TREMOR->{if(elapsed==1){ring(2,12);ring(4,12);ring(6,12);}if(elapsed==24){sound("impact",.8F);dust(position(),36);for(var t:victims(position(),6))if(t.onGround()&&!t.isPassenger()&&Math.abs(t.getY()-getY())<.6&&!(t instanceof Player p&&(p.isCreative()||p.isSpectator()))){t.hurt(damageSources().mobAttack(this),3);t.push(0,.18,0);if(t instanceof Player p)OminousAura.knock(p,this);}}if(elapsed==duration)finish(Action.TREMOR,350,18);}
22:   case CHARGE->{if(elapsed==18){heading=aim.subtract(position());heading=new Vec3(heading.x,0,heading.z).normalize();if(!clearCharge(heading)){finish(Action.CHARGE,280,30);return;}sound("charge",.8F);}if(elapsed>=19&&elapsed<=36){if(horizontalCollision||!safe(BlockPos.containing(position().add(heading.scale(.45))))){setDeltaMovement(0,getDeltaMovement().y,0);sound("impact",.7F);finish(Action.CHARGE,280,30);return;}setDeltaMovement(heading.x*.42,getDeltaMovement().y,heading.z*.42);for(var t:victims(position().add(heading.scale(.4)),1.7))if(chargeHits.add(t.getUUID())){t.hurt(damageSources().mobAttack(this),8);t.push(heading.x*.8,.2,heading.z*.8);}if(elapsed%5==0)dust(position(),3);}if(elapsed==duration){setDeltaMovement(0,getDeltaMovement().y,0);finish(Action.CHARGE,280,18);}}
```

## src/main/java/org/slavicmyths/underground/OminousAura.java

```java
18:  public static final TagKey<EntityType<?>> IMMUNE=TagKey.create(Registries.ENTITY_TYPE,UndergroundMobs.id("ominous_aura_immune"));
20:  public static boolean ignores(Mob mob,Player p){if(!(mob instanceof net.minecraft.world.entity.monster.Enemy)||mob instanceof RoomBoss||mob instanceof org.slavicmyths.industry.ChudPerson||mob.getType().is(IMMUNE)||mob.getMaxHealth()>50||active(p)<2)return false;var prov=mob.getPersistentData().getCompound("OminousProvocation");String id=p.getUUID().toString();boolean provoked=prov.contains(id)&&(mob.level().getGameTime()-prov.getLong(id)<=1800||mob.getTarget()==p);return EncounterRules.protectedByRunes(mob.getMaxHealth(),active(p),false,provoked);}
22:  @SubscribeEvent public static void provoke(LivingDamageEvent.Post e){if(e.getNewDamage()<=0||!(e.getEntity() instanceof Mob m)||m.level().isClientSide)return;Entity source=e.getSource().getEntity();Player p=source instanceof Player a?a:source instanceof TamableAnimal t&&t.getOwner() instanceof Player a?a:null;if(p==null)return;var root=m.getPersistentData();var c=root.getCompound("OminousProvocation");long now=m.level().getGameTime();for(String id:new ArrayList<>(c.getAllKeys()))if(now-c.getLong(id)>1800)c.remove(id);c.putLong(p.getUUID().toString(),now);root.put("OminousProvocation",c);}
23:  public static float exhaustion(Player p,float amount){if(amount<=0||p.level().isClientSide||p.isCreative()||p.isSpectator()||!p.isAlive())return amount;return p.level().getEntitiesOfClass(LargePodzemnik.class,p.getBoundingBox().inflate(12),b->b.auraActive()&&b.distanceToSqr(p)<=144).isEmpty()?amount:EncounterRules.exhaustion(amount,true);}
25:  public static void knock(Player p,LargePodzemnik owner){long now=p.level().getGameTime();var n=p.getPersistentData();if(p.isPassenger()||p.isCreative()||p.isSpectator()||n.getLong("UndergroundControlImmune")>now)return;n.putLong("UndergroundControlImmune",now+52);n.putUUID("UndergroundControlOwner",owner.getUUID());n.putString("UndergroundControlDimension",p.level().dimension().location().toString());p.addEffect(new MobEffectInstance(UndergroundMobs.KNOCKDOWN,20,0,false,false,true));if(p.getForcedPose()==null){p.setForcedPose(Pose.SWIMMING);n.putBoolean("UndergroundForcedPose",true);}}
27:  @SubscribeEvent public static void attack(AttackEntityEvent e){if(knocked(e.getEntity()))e.setCanceled(true);}
30:  @SubscribeEvent public static void directAttack(LivingIncomingDamageEvent e){if(e.getSource().getEntity() instanceof Player p&&e.getSource().getDirectEntity()==p&&knocked(p))e.setCanceled(true);}
33:  @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){clear(e.getEntity());}
```

## src/main/java/org/slavicmyths/underground/Podzemnik.java

```java
7: import net.minecraft.world.entity.ai.attributes.Attributes;
22:  public void variant(int v){entityData.set(VARIANT,v==1?1:0);getAttribute(Attributes.MAX_HEALTH).setBaseValue(v==1?40:28);getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(v==1?.24:.25);getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(v==1?6.5:5);setHealth(getMaxHealth());setItemSlot(EquipmentSlot.MAINHAND,v==1?new ItemStack(Items.IRON_PICKAXE):ItemStack.EMPTY);setDropChance(EquipmentSlot.MAINHAND,0);refreshDimensions();}
24:  public SpawnGroupData finalizeSpawn(ServerLevelAccessor l,DifficultyInstance d,MobSpawnType type,SpawnGroupData group){var result=super.finalizeSpawn(l,d,type,group);variant(random.nextFloat()<.3?1:0);return result;}
26:  protected net.minecraft.sounds.SoundEvent getAmbientSound(){return UndergroundSounds.sound("podzemnik","idle");}protected net.minecraft.sounds.SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource d){return UndergroundSounds.sound("podzemnik","hurt");}protected net.minecraft.sounds.SoundEvent getDeathSound(){return UndergroundSounds.sound("podzemnik","death");}
28:  protected void step(){switch(action()){case MELEE->{if(elapsed==6)melee(variant()==1?6.5F:5,2.4);if(elapsed==duration)finish(Action.MELEE,1,1);}case MINER_HEAVY->{if(elapsed==20){var t=getTarget();melee(10,2.6);if(validTarget(t)&&distanceTo(t)<2.6&&hasLineOfSight(t))t.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN,50,0));dust(position(),5);}if(elapsed==duration)finish(Action.MINER_HEAVY,160,12);}case LUNGE->{if(elapsed==7&&validTarget(getTarget())){var v=getTarget().position().subtract(position()).normalize();if(safe(net.minecraft.core.BlockPos.containing(position().add(v.scale(1.5)))))setDeltaMovement(v.scale(.35));}if(elapsed==12)melee(5,2.4);if(elapsed==duration)finish(Action.LUNGE,200,8);}default->{}}}
31:  public void readAdditionalSaveData(CompoundTag n){float health=n.contains("Health",99)?n.getFloat("Health"):Float.NaN;super.readAdditionalSaveData(n);variant(n.getInt("Variant"));if(!Float.isNaN(health))setHealth(Math.min(health,getMaxHealth()));}
```

## src/main/java/org/slavicmyths/underground/RoomBoss.java

```java
15:  public void die(net.minecraft.world.damagesource.DamageSource d){bar.removeAllPlayers();super.die(d);}
18:  protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource d){return UndergroundSounds.sound(this instanceof Kladovik?"kladovik":"large_podzemnik","hurt");}
```

## src/main/java/org/slavicmyths/underground/ThrownStone.java

```java
13:  protected void onHitEntity(EntityHitResult hit){super.onHitEntity(hit);hit.getEntity().hurt(damageSources().thrown(this,getOwner()),EncounterRules.STONE_DAMAGE);}
```

## src/main/java/org/slavicmyths/underground/UndergroundMobs.java

```java
7: import net.minecraft.world.entity.ai.attributes.*;
21:  public static final DeferredHolder<MobEffect,MobEffect> KNOCKDOWN=EFFECTS.register("underground_knockdown",()->new MobEffect(MobEffectCategory.HARMFUL,0x615d57){}.addAttributeModifier(Attributes.MOVEMENT_SPEED,id("knockdown"),-.95,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
24:  private static void egg(String n,DeferredHolder<EntityType<?>,? extends EntityType<? extends Mob>> type,int a,int b){ModItems.ITEMS.register(n+"_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(type,a,b,new Item.Properties()));}
```

## src/main/java/org/slavicmyths/village/OutfitRules.java

```java
7:     public enum Climate { COLD, TEMPERATE, WARM }
```

## src/main/java/org/slavicmyths/volkolak/CursedVolkolak.java

```java
11: import net.minecraft.world.damagesource.DamageSource;
13: import net.minecraft.world.entity.ai.attributes.*;
24:  public enum Move {IDLE(0,0),ALERT(16,0),RISE(16,0),DROP(12,0),CLAW_L(18,18),CLAW_R(20,18),DOUBLE(30,52),TRIPLE(40,85),BITE(21,25),POUNCE(36,90),FLANK(28,55),GRAB(60,260),STAGGER(20,0),ROAR(26,0),SNIFF(18,0),LISTEN(24,0),SNARL(25,0),HOWL_SHORT(42,0),ESCAPE(131,0);
25:   public final int length,cooldown;Move(int length,int cooldown){this.length=length;this.cooldown=cooldown;}}
28:  private final EnumMap<Move,Integer> cooldowns=new EnumMap<>(Move.class);
33:  public static AttributeSupplier.Builder attributes(){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,400).add(Attributes.ATTACK_DAMAGE,15).add(Attributes.MOVEMENT_SPEED,.42).add(Attributes.ARMOR,7).add(Attributes.FOLLOW_RANGE,32).add(Attributes.KNOCKBACK_RESISTANCE,.6).add(Attributes.STEP_HEIGHT,1);}
41:  private void stance(boolean value){if(value&&!canStand())return;entityData.set(UPRIGHT,value);getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.42*(value?.85:1)*(phase()==3?1.12:1));}
47:  @Override protected SoundEvent getHurtSound(DamageSource source){return null;}
53:  @Override public void die(DamageSource source){release();super.die(source);}
56:  private void begin(Move m,LivingEntity target){release();navigation.stop();entityData.set(MOVE,m.ordinal());entityData.set(START,(int)level().getGameTime());lockedTarget=target==null?null:target.getUUID();connected=false;hitMask=0;heldTicks=0;getPersistentData().remove("VolkolakLandCue");if(target!=null)aim=target.position().subtract(position()).multiply(1,0,1).normalize();cooldowns.put(m,m.cooldown);if(m==Move.CLAW_L||m==Move.CLAW_R){cooldowns.put(Move.CLAW_L,18);cooldowns.put(Move.CLAW_R,18);}if(m==Move.FLANK)flank=position().add(new Vec3(-aim.z,0,aim.x).scale(random.nextBoolean()?3.5:-3.5));if(m==Move.GRAB)cue("grab_prepare",.85F);if(m==Move.ALERT)cue("alert",.7F);if(m==Move.ESCAPE)cue("fake_fall",.8F);if(m!=Move.RISE&&m!=Move.DROP&&m!=Move.ALERT&&m!=Move.ROAR&&m!=Move.STAGGER)cycles++;}
59:  private boolean shield(LivingEntity target){return target.isDamageSourceBlocked(damageSources().mobAttack(this));}
60:  private boolean strike(LivingEntity target,float damage,double reach,double degrees,boolean bleed){if(!valid(target)||distanceTo(target)>reach||!hasLineOfSight(target))return false;Vec3 d=target.position().subtract(position()).multiply(1,0,1).normalize();if(aim.dot(d)<Math.cos(Math.toRadians(degrees*.5)))return false;float before=target.getHealth()+target.getAbsorptionAmount();boolean result=target.hurt(damageSources().mobAttack(this),Math.max(0,damage+(float)getAttributeValue(Attributes.ATTACK_DAMAGE)-15+(phase()==3?2:0)));boolean actual=result&&target.getHealth()+target.getAbsorptionAmount()<before;if(actual&&bleed&&target instanceof ServerPlayer p)org.slavicmyths.wolf.WolfBleeding.apply(this,p);return actual;}
86:  private boolean ready(Move m){return cooldowns.getOrDefault(m,0)==0;}
93:   if(shield(t)){t.hurt(damageSources().mobAttack(this),16);begin(Move.STAGGER,null);return;}
100:  @Override public void tick(){super.tick();if(level().isClientSide)return;if(home==null)home=blockPosition();cooldowns.replaceAll((m,t)->Math.max(0,t-1));recovery=Math.max(0,recovery-1);if(move()!=Move.GRAB&&!getPassengers().isEmpty())release();
108:  @Override public boolean hurt(DamageSource source,float amount){if(move()==Move.ESCAPE)return false;boolean result=super.hurt(source,amount);if(result&&source.getEntity() instanceof LivingEntity attacker&&valid(attacker))setTarget(attacker);if(result&&level() instanceof ServerLevel w&&w.getGameTime()>=nextHurt){cue("hurt",.65F);nextHurt=w.getGameTime()+12;}return result;}
127:   else if(m==Move.CLAW_L||m==Move.CLAW_R){if(tick==3)cue("snarl",.55F);int hit=m==Move.CLAW_L?10:12;if(tick==hit-3)cue("claw_windup",.65F);if(tick>=hit&&(hitMask&1)==0){hitMask|=1;cue("claw_swing",.7F);strike(t,15,3.1,90,true);}}
128:   else if(m==Move.DOUBLE||m==Move.TRIPLE){if(tick==7||tick==17||m==Move.TRIPLE&&tick==27)cue("claw_windup",.65F);for(int hit=0;hit<(m==Move.TRIPLE?3:2);hit++)if(tick>=10+hit*10&&(hitMask&(1<<hit))==0){hitMask|=1<<hit;cue("claw_swing",.7F);strike(t,hit==2?18:13,3.1,hit==2?100:90,true);}}
129:   else if(m==Move.BITE&&tick>=13&&(hitMask&1)==0){hitMask|=1;cue("bite",.7F);strike(t,17,3.05,80,true);}
132:    if(tick>14&&tick<=32){if(position().subtract(launchOrigin).multiply(1,0,1).lengthSqr()>=launchRange*launchRange||horizontalCollision)setDeltaMovement(0,getDeltaMovement().y,0);if(tick>=16&&!connected)connected=strike(t,20,2.8,90,true);}
135:   else if(m==Move.FLANK){if(tick==7)navigation.moveTo(flank.x,flank.y,flank.z,1.3);if(tick>7&&tick<23){turn(t);if(tick%5==0)navigation.moveTo(flank.x,flank.y,flank.z,1.3);}if(tick==24){aim=t.position().subtract(position()).multiply(1,0,1).normalize();cue("bite",.7F);strike(t,16,2.4,55,true);}}
136:   else if(m==Move.GRAB){if(tick==14)launch(.6,.12);if(tick>=22&&tick<=25&&!connected)capture(t);if(tick==28&&(hitMask&1)==0&&heldEntity()==t.getId()&&t.getVehicle()==this){hitMask|=1;cue("grab_bite",.85F);if(strike(t,24,2.4,100,true)){BadBlood.apply(t,this);cue("bad_blood_apply",.45F);}}if(tick==45)release(true);if(tick==49)cue("grab_release",.6F);if(tick==26&&!connected){finish();recovery=18;return;}}
139:  @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);if(home!=null)tag.putLong("VolkolakHome",home.asLong());if(encounter!=null)tag.putUUID("VolkolakEncounter",encounter);tag.putBoolean("VolkolakEscaped",escapeCommitted);tag.putInt("VolkolakPhase",phase());var cd=new CompoundTag();cooldowns.forEach((m,t)->cd.putInt(m.name(),t));tag.put("VolkolakCooldowns",cd);}
140:  @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);home=tag.contains("VolkolakHome")?BlockPos.of(tag.getLong("VolkolakHome")):null;encounter=tag.hasUUID("VolkolakEncounter")?tag.getUUID("VolkolakEncounter"):null;escapeCommitted=tag.getBoolean("VolkolakEscaped");entityData.set(PHASE,Math.clamp(tag.getInt("VolkolakPhase"),1,3));for(Move m:Move.values())cooldowns.put(m,Math.clamp(tag.getCompound("VolkolakCooldowns").getInt(m.name()),0,m.cooldown));finish();stance(false);if(escapeCommitted)discard();}
```

## src/main/java/org/slavicmyths/volkolak/VolkolakEvents.java

```java
12:  @SubscribeEvent(priority=EventPriority.LOWEST) public static void lethal(LivingDamageEvent.Pre e){if(!(e.getEntity() instanceof CursedVolkolak boss)||!(boss.level() instanceof ServerLevel w)||boss.encounter==null)return;var r=VolkolakRecords.get(w).encounters.get(boss.encounter);if(r==null||!boss.getUUID().equals(r.boss))return;if(boss.move()==CursedVolkolak.Move.ESCAPE){e.setNewDamage(0);return;}if(e.getNewDamage()>0&&e.getNewDamage()>=boss.getHealth()){e.setNewDamage(Math.max(0,boss.getHealth()-1));VolkolakStory.lethal(boss);}}
13:  @SubscribeEvent public static void damaged(LivingDamageEvent.Post e){if(!(e.getEntity() instanceof CursedVolkolak boss)||!(boss.level() instanceof ServerLevel w)||e.getNewDamage()<=0||boss.encounter==null)return;var p=GreyWolfStoryEvents.owner(e.getSource(),w);var data=VolkolakRecords.get(w);var record=data.encounters.get(boss.encounter);if(p!=null&&record!=null&&boss.getUUID().equals(record.boss)&&p.distanceToSqr(boss)<=48*48){record.participants.put(p.getUUID(),w.getGameTime());data.setDirty();}}
```

## src/main/java/org/slavicmyths/water/RiverFish.java

```java
10: import net.minecraft.world.damagesource.DamageSource;
21:  protected SoundEvent getHurtSound(DamageSource s){return SoundEvents.COD_HURT;}
26:   if(kind==0&&tickCount%120==0){java.util.List<AbstractFish> prey=level().getEntitiesOfClass(AbstractFish.class,getBoundingBox().inflate(5),f->f!=this&&f.getBbWidth()<.6F);if(!prey.isEmpty())getNavigation().moveTo(prey.get(0),1.25);}
```

## src/main/java/org/slavicmyths/water/RusalkaEntity.java

```java
2: import net.minecraft.world.entity.ai.attributes.Attribute;
4: import net.minecraft.world.entity.ai.attributes.*;
6: import net.minecraft.world.damagesource.DamageSource;
12:  public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,28).add(Attributes.MOVEMENT_SPEED,.23).add(Attributes.ATTACK_DAMAGE,3).add(Attributes.FOLLOW_RANGE,20);}
15:   for(Player listener:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(20),q->q.isAlive()&&!q.isCreative()&&!q.isSpectator()&&distanceToSqr(q)<=400)){RusalkaSong.expose(listener,this);if(listener.getPersistentData().getFloat("SlavicWaterCharm")>=50)lured=true;if(distanceToSqr(listener)<6.25){provoke(listener);silentUntil=level().getGameTime()+80;return;}}
18:  public boolean hurt(DamageSource source,float amount){boolean hit=super.hurt(source,amount);if(hit&&!level().isClientSide){silentUntil=level().getGameTime()+120;watching=0;state(3);}return hit;}
```

## src/main/java/org/slavicmyths/water/VodyanoyEntity.java

```java
2: import net.minecraft.world.entity.ai.attributes.Attribute;
4: import net.minecraft.world.entity.ai.attributes.*;
13: import net.minecraft.world.damagesource.DamageSource;
20:  public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,44).add(Attributes.MOVEMENT_SPEED,.21).add(Attributes.ATTACK_DAMAGE,5).add(Attributes.FOLLOW_RANGE,24);}
24:  protected InteractionResult mobInteract(Player p,InteractionHand h){if(level().isClientSide)return InteractionResult.SUCCESS;Knowledge.award(p,"meet_vodyanoy");ItemStack s=p.getItemInHand(h);Item i=s.getItem();if(i!=Items.BREAD&&i!=ModItems.KARAVAI.get()&&i!=ModItems.PEARL_FRAGMENT.get())return InteractionResult.PASS;String key=p.getStringUUID();if(!relations.contains(key)&&relations.getAllKeys().size()>=64)return InteractionResult.FAIL;CompoundTag n=relations.getCompound(key);long now=level().getGameTime();if(now<n.getLong("GiftReady"))return InteractionResult.FAIL;n.putInt("Rep",Math.min(100,n.getInt("Rep")+10));n.putLong("GiftReady",now+1200);relations.put(key,n);if(!p.isCreative())s.shrink(1);friend=p.getUUID();friendUntil=now+1200;if(getTarget()==p)setTarget(null);state(0);if(n.getInt("Rep")>=40&&!n.getBoolean("DepthClue")){n.putBoolean("DepthClue",true);relations.put(key,n);spawnAtLocation(ModItems.ANCIENT_WATER_SIGN.get());Knowledge.award(p,"depth_clue");}if(n.getInt("Rep")>=20){p.addEffect(new MobEffectInstance(MobEffects.LUCK,1800,0));if(random.nextInt(4)==0)spawnAtLocation(ModItems.PEARL_FRAGMENT.get());}voice("ambient",.5F);return InteractionResult.CONSUME;}
25:  public boolean hurt(DamageSource s,float amount){boolean hit=super.hurt(s,amount);if(hit&&!level().isClientSide&&s.getEntity() instanceof Player)pressure((Player)s.getEntity(),20);return hit;}
```

## src/main/java/org/slavicmyths/water/WaterSpawns.java

```java
2: import net.minecraft.world.level.biome.MobSpawnSettings;
3: import net.minecraft.world.entity.SpawnPlacements;
11: public final class WaterSpawns {
12:  public static void placements(net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent event){
13:   event.register(ModEntities.BOLOTNIK.get(),net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(t,w,s,p,r)->w.getBiome(p).is(Biomes.SWAMP)&&w.getDifficulty()!=net.minecraft.world.Difficulty.PEACEFUL&&!w.getLevel().isDay()&&net.minecraft.world.entity.Mob.checkMobSpawnRules(t,w,s,p,r)&&r.nextInt(4)==0,net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
14:   event.register(ModEntities.VODYANOY.get(),net.minecraft.world.entity.SpawnPlacementTypes.IN_WATER,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(t,w,s,p,r)->w.getLevel().dimension()==net.minecraft.world.level.Level.OVERWORLD&&w.getFluidState(p).is(net.minecraft.tags.FluidTags.WATER)&&w.getFluidState(p.above()).is(net.minecraft.tags.FluidTags.WATER)&&r.nextInt(8)==0,net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
15:   event.register(ModEntities.RUSALKA.get(),net.minecraft.world.entity.SpawnPlacementTypes.IN_WATER,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(t,w,s,p,r)->w.getLevel().dimension()==net.minecraft.world.level.Level.OVERWORLD&&!w.getLevel().isDay()&&w.getFluidState(p).is(net.minecraft.tags.FluidTags.WATER)&&r.nextInt(10)==0,net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
16:   event.register(ModEntities.PIKE.get(),net.minecraft.world.entity.SpawnPlacementTypes.IN_WATER,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,WaterSpawns::riverAnimal,net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
17:   event.register(ModEntities.CARP.get(),net.minecraft.world.entity.SpawnPlacementTypes.IN_WATER,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,WaterSpawns::riverAnimal,net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
18:   event.register(ModEntities.CRAYFISH.get(),net.minecraft.world.entity.SpawnPlacementTypes.IN_WATER,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,WaterSpawns::riverAnimal,net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
20:  public static boolean riverAnimal(EntityType<? extends RiverFish> type,net.minecraft.world.level.ServerLevelAccessor world,MobSpawnType reason,net.minecraft.core.BlockPos pos,net.minecraft.util.RandomSource random){
```

## src/main/java/org/slavicmyths/water/WaterSpirit.java

```java
13:  @Override public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor world,net.minecraft.world.DifficultyInstance difficulty,net.minecraft.world.entity.MobSpawnType reason,net.minecraft.world.entity.SpawnGroupData data){
14:   var result=super.finalizeSpawn(world,difficulty,reason,data);
16:    double hp=this instanceof RusalkaEntity?36:this instanceof VodyanoyEntity?72:getMaxHealth();
17:    getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(hp);setHealth((float)hp);
```

## src/main/java/org/slavicmyths/winters/WintersEvents.java

```java
13: import net.minecraft.world.entity.ai.attributes.*;
38:   if(!t.isAlive()||t==p||t instanceof Player||t.isAlliedTo(p)||p.isAlliedTo(t)||t.isInvulnerable())return false;
51:   var kb=p.getAttribute(Attributes.KNOCKBACK_RESISTANCE);boolean defensive=has&&!dark(p);
61:   if(time<ready){message(p,"cooldown",(ready-time+19)/20);return;}
62:   n.putLong("Ready",time+WintersRules.COOLDOWN);
63:   var item=WintersRecipe.tag(stack);item.putLong("TwoWintersReady",time+WintersRules.COOLDOWN);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(item));
67:     for(var t:p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(5),t->enemy(t,p)&&t.distanceToSqr(p)<=25&&p.hasLineOfSight(t))){
68:      boolean b=boss(t);if(t.hurt(p.damageSources().playerAttack(p),b?3:6)&&!b){t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,100,0),p);t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,80,0),p);}
79:   // Animation only, no target search or damage.
89:   if(!p.isAlive()||p.level().dimension()!=Level.NETHER||present(p)||p.isCreative()||p.isSpectator()||p.getAbilities().invulnerable){n.remove("AuraStart");n.remove("AuraStage");n.remove("AuraHit");p.removeEffect(WintersContent.AURA);return;}
93:   if(stage>0)p.addEffect(new MobEffectInstance(WintersContent.AURA,40,stage==1?0:1,false,false,true));
95:   int interval=WintersRules.interval(stage);if(interval>0&&time-n.getLong("AuraHit")>=interval){n.putLong("AuraHit",time);p.hurt(p.damageSources().magic(),stage==2?1:2);}
97:  @SubscribeEvent(priority=EventPriority.LOWEST) public static void damage(LivingDamageEvent.Pre e){
98:   if(e.getEntity().level().isClientSide||e.getNewDamage()<=0)return;
100:    e.setNewDamage(e.getNewDamage()*1.15f);
101:   if(e.getEntity() instanceof ServerPlayer p&&present(p)&&!dark(p)&&!e.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)){
102:    if(e.getSource().getDirectEntity()==e.getSource().getEntity()&&e.getSource().getEntity() instanceof LivingEntity attacker&&attacker.distanceToSqr(p)<=9&&enemy(attacker,p)&&p.getRandom().nextFloat()<.2f)attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,60,0),p);
103:    float damage=e.getNewDamage();if(e.getSource().is(DamageTypeTags.IS_FIRE))damage*=.6f;
104:    var n=data(p);if(p.level().dimension()==Level.NETHER&&now(p)<n.getLong("ShieldUntil")&&n.getFloat("Shield")>0){float reserve=n.getFloat("Shield");n.putFloat("Shield",WintersRules.shieldLeft(damage,reserve));damage=WintersRules.shieldDamage(damage,reserve);if(n.getFloat("Shield")==0){n.putFloat("Shield",reserve);clearShield(p,true);}}
105:    e.setNewDamage(damage);
108:  @SubscribeEvent public static void hit(LivingDamageEvent.Post e){
109:   if(e.getEntity().level().isClientSide||e.getNewDamage()<=0||WAVE.get())return;
110:   if(e.getSource().getDirectEntity()!=null&&e.getSource().getEntity() instanceof ServerPlayer p&&present(p)&&dark(p)&&enemy(e.getEntity(),p)&&!boss(e.getEntity())&&p.getRandom().nextFloat()<.15f)e.getEntity().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,60,0),p);
```

## src/main/java/org/slavicmyths/winters/WintersRules.java

```java
4:  public static final int COOLDOWN=2400,SHIELD_TICKS=160;
7:  public static float shieldDamage(float damage,float reserve){return Math.max(0,damage-Math.max(0,reserve));}
8:  public static float shieldLeft(float damage,float reserve){return Math.max(0,reserve-Math.max(0,damage));}
```

## src/main/java/org/slavicmyths/wolf/GreyWolf.java

```java
10: import net.minecraft.world.entity.ai.attributes.*;
13: import net.minecraft.world.damagesource.DamageSource;
29:  public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,500).add(Attributes.MOVEMENT_SPEED,.28).add(Attributes.ATTACK_DAMAGE,13).add(Attributes.FOLLOW_RANGE,40).add(Attributes.KNOCKBACK_RESISTANCE,.85).add(Attributes.STEP_HEIGHT,1);}
64:  public void debugHealth(float health){if(debug&&active())setHealth(Math.max(50,Math.min(500,health)));}
68:    packSpots=WolfEncounter.packSpots((ServerLevel)level(),blockPosition(),32);if(packSpots.size()<2)return false;state(State.TRIAL_SUMMONING);howlReady=now+HOWL_COOLDOWN;
92:  @Override public boolean causeFallDamage(float distance,float multiplier,DamageSource source){return false;}
105:  public void strike(ServerPlayer p,Move action){
111:   float damage=switch(action){case BITE->13;case DOUBLE_CLAW->11;case POUNCE->17;case FLANK_DASH->15;default->0;};if(damage<=0)return;
112:   float before=p.getHealth()+p.getAbsorptionAmount();boolean landed=p.hurt(damageSources().mobAttack(this),damage);
113:   if(landed&&p.getHealth()+p.getAbsorptionAmount()<before){WolfBleeding.apply(this,p);if(action==Move.POUNCE)p.knockback(.65,getX()-p.getX(),getZ()-p.getZ());}
119:  @Override public boolean hurt(DamageSource source,float amount){
120:   if(!narrativeActor())return super.hurt(source,amount);
121:   if(source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL)||source.getEntity()==this)return false;
123:   return super.hurt(source,amount);
125:  @Override protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source){return active()?WolfSounds.get("hurt"):null;}
128:  @Override public float getPickRadius(){return 1.1F;}
139:   if(busy()&&tickCount%20==0){bar.setProgress(Math.max(0,(getHealth()-50)/450));for(var observer:w.getEntitiesOfClass(ServerPlayer.class,getBoundingBox().inflate(32)))bar.addPlayer(observer);for(var observer:List.copyOf(bar.getPlayers()))if(observer.level()!=level()||distanceToSqr(observer)>40*40)bar.removePlayer(observer);}
151:     if(a==Move.HOWL_SUMMON){setDeltaMovement(0,getDeltaMovement().y,0);if(t==HOWL_SOUND)voice("summon_howl",3);if(t==PACK_SPAWN)summon();}
159:      if(WolfTimeline.hit(a,t))strike(p,a);
```

## src/main/java/org/slavicmyths/wolf/WolfEvents.java

```java
6:  @SubscribeEvent(priority=EventPriority.LOWEST) public static void damage(LivingDamageEvent.Pre e){if(e.getEntity() instanceof GreyWolf wolf&&wolf.narrativeActor()){if(!wolf.active()){e.setNewDamage(0);return;}if(e.getNewDamage()>0&&wolf.getHealth()-e.getNewDamage()<=50){e.setNewDamage(0);wolf.finish();}}}
11:   var target=e.getEntity();w.getServer().tell(new net.minecraft.server.TickTask(w.getServer().getTickCount()+1,()->{if(!target.isAlive()||org.slavicmyths.combat.RareBleedEffect.current(target)!=null)return;var d=target.getPersistentData();long now=org.slavicmyths.combat.RareCombat.now(target);d.remove("RareBleedWolfUntil");d.remove("RareBleedWolfStacks");d.remove("RareBleedWolfActor");
12:    int n=d.getLong("RareBleedNativeUntil")>now?d.getInt("RareBleedNative"):0,r=d.getLong("RareBleedRuneUntil")>now?d.getInt("RareBleedRune"):0,stacks=org.slavicmyths.rpg.RuneBalance.bleedStacks(n,r);int remaining=(int)(Math.max(d.getLong("RareBleedNativeUntil"),d.getLong("RareBleedRuneUntil"))-now);
13:    if(stacks>0&&remaining>0)target.addEffect(new net.minecraft.world.effect.MobEffectInstance(org.slavicmyths.registry.ModEffects.RARE_BLEED,remaining,stacks-1,false,true,true));
```

## src/main/java/org/slavicmyths/wood/Woodlands.java

```java
50:             ModItems.ITEMS.register(n+"_sign",()->new SignItem(props().stacksTo(16),get("sign"),get("wall_sign")));
```

## src/main/java/org/slavicmyths/world/WildlifeSpawns.java

```java
2: import net.minecraft.world.level.biome.MobSpawnSettings;
3: import net.minecraft.world.entity.SpawnPlacements;
11: public final class WildlifeSpawns {
12:  public static void placements(net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent event){
14:    event.register(type,net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,WildlifeSpawns::canSpawn,net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
16:  public static boolean canSpawn(EntityType<? extends Mob> type,net.minecraft.world.level.ServerLevelAccessor world,MobSpawnType reason,net.minecraft.core.BlockPos pos,net.minecraft.util.RandomSource random){
26:    &&Mob.checkMobSpawnRules(type,world,reason,pos,random);
28:  private WildlifeSpawns(){}
```

## src/main/java/org/slavicmyths/yaga/BabaYaga.java

```java
3: import net.minecraft.world.entity.SpawnGroupData;
8: import net.minecraft.world.entity.ai.attributes.Attribute;
12: import net.minecraft.world.entity.ai.attributes.*;
23: import net.minecraft.world.damagesource.DamageSource;
37:  public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,40).add(Attributes.MOVEMENT_SPEED,.23).add(Attributes.KNOCKBACK_RESISTANCE,1).add(Attributes.FOLLOW_RANGE,12);}
42:  @Override public boolean removeWhenFarAway(double distance){return false;}@Override public boolean canChangeDimensions(net.minecraft.world.level.Level from,net.minecraft.world.level.Level to){return false;}@Override public boolean causeFallDamage(float d,float mult,net.minecraft.world.damagesource.DamageSource source){return false;}@Override public void checkDespawn(){}
52:  @Override public boolean hurt(DamageSource source,float amount){if(!level().isClientSide&&source.getEntity() instanceof ServerPlayer&&canonical()){ServerPlayer p=(ServerPlayer)source.getEntity();YagaData d=YagaData.get((ServerLevel)level());boolean expelled=d.progress(p.getUUID()).attack(level().getGameTime(),amount>=10);d.setDirty();gesture(3);p.displayClientMessage(Component.translatable(expelled?"yaga.warning.expelled":"yaga.warning.first"),false);playSound(expelled?org.slavicmyths.registry.ModSounds.YAGA_WARN.get():org.slavicmyths.registry.ModSounds.YAGA_HURT.get(),.8F,.55F);if(expelled){Vec3 away=p.position().subtract(position()).multiply(1,0,1).normalize();p.knockback(1.6F,-away.x,-away.z);p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,100));}if(p.containerMenu instanceof YagaMenu)p.closeContainer();}return false;}
53:  @Override public void die(DamageSource source){setHealth(40);}
56:  @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor w,DifficultyInstance diff,MobSpawnType reason,SpawnGroupData data){SpawnGroupData result=super.finalizeSpawn(w,diff,reason,data);if(reason==MobSpawnType.SPAWN_EGG){eggSummoned=true;home=blockPosition();}return result;}
58:  @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);eggSummoned=n.getBoolean("YagaEggSummoned");home=n.contains("YagaHome")?BlockPos.of(n.getLong("YagaHome")):null;setPersistenceRequired();getAttribute(Attributes.MAX_HEALTH).setBaseValue(40);getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.23);setHealth(40);}
```

## src/main/java/org/slavicmyths/yaga/YagaUtilityItem.java

```java
12: import net.minecraft.world.damagesource.DamageSource;
24:  public enum Kind{THREAD,CLEANSE,SALVE,SIGHT,RESIST,VIGOR,RESTORE,HERBS}
28:  @Override public InteractionResultHolder<ItemStack> use(Level w,Player player,InteractionHand hand){ItemStack s=player.getItemInHand(hand);if(kind==Kind.HERBS)return super.use(w,player,hand);if(kind!=Kind.THREAD)return start(w,player,hand);if(player.getCooldowns().isOnCooldown(this))return InteractionResultHolder.fail(s);if(!w.isClientSide&&player instanceof ServerPlayer){ServerPlayer p=(ServerPlayer)player;ServerLevel overworld=p.server.getLevel(Level.OVERWORLD);if(!org.slavicmyths.navigation.NavigationManager.thread(p))return InteractionResultHolder.fail(s);p.getCooldowns().addCooldown(this,80);YagaServices.award(p,"follow_the_thread");w.playSound(null,p.blockPosition(),SoundEvents.WOOL_HIT,SoundSource.PLAYERS,.6F,1.1F);}return InteractionResultHolder.success(s);}
30:  @Override public ItemStack finishUsingItem(ItemStack s,Level w,LivingEntity e){if(!w.isClientSide){switch(kind){case CLEANSE:for(net.minecraft.core.Holder<MobEffect> bad:java.util.List.of(MobEffects.POISON,MobEffects.WITHER,MobEffects.BLINDNESS,MobEffects.MOVEMENT_SLOWDOWN,MobEffects.WEAKNESS,MobEffects.HUNGER,MobEffects.CONFUSION))e.removeEffect(bad);break;case SALVE:if(e instanceof Player)FlightRecovery.apply((Player)e);break;case SIGHT:e.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,1200));break;case RESIST:e.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,600));break;case VIGOR:e.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED,1200));break;case RESTORE:e.addEffect(new MobEffectInstance(MobEffects.REGENERATION,200));break;default:break;}}
```
