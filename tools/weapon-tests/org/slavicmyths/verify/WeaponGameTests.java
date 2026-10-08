package org.slavicmyths.verify;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.combat.*;
import org.slavicmyths.item.ItemState;
import org.slavicmyths.rpg.Runes;
import org.slavicmyths.armorer.*;

@GameTestHolder("slavicmyths_weapons") @PrefixGameTestTemplate(false)
public final class WeaponGameTests {
    private static final class Connection extends net.minecraft.network.Connection {
        private final io.netty.channel.embedded.EmbeddedChannel channel=new io.netty.channel.embedded.EmbeddedChannel();
        Connection(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);}
        @Override public io.netty.channel.Channel channel(){return channel;}
    }
    private static final class Listener extends net.minecraft.server.network.ServerGamePacketListenerImpl {
        Listener(ServerPlayer p){super(p.getServer(),new Connection(),p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));}
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet) { }
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener) { }
    }
    private static ServerPlayer player(GameTestHelper t,String name){var w=t.getLevel();var p=new ServerPlayer(w.getServer(),w,new com.mojang.authlib.GameProfile(UUID.randomUUID(),name),ClientInformation.createDefault());p.connection=new Listener(p);p.setNoGravity(true);p.setGameMode(GameType.SURVIVAL);p.moveTo(t.absoluteVec(new Vec3(3,2,3)));p.setYRot(0);p.setXRot(0);return p;}
    private static ItemStack marked(GameTestHelper t,Item item) {
        ItemStack s=new ItemStack(item);ItemState.runes(s,1,List.of("heat"));s.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("KeptWeapon"));s.setDamageValue(5);
        s.enchant(t.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(item instanceof BowItem?Enchantments.POWER:Enchantments.SHARPNESS),2);return s;
    }
    private static List<WeaponProjectile> projectiles(GameTestHelper t,ServerPlayer p){return t.getLevel().getEntitiesOfClass(WeaponProjectile.class,p.getBoundingBox().inflate(6));}
    @GameTest(template="empty") public static void catalogAndCapacity(GameTestHelper t) {
        t.assertTrue(WeaponCatalog.ITEMS.size()==53,"Wrong new item count");
        for(var entry:WeaponCatalog.ITEMS.entrySet()) {
            var item=entry.getValue().get();int category=org.slavicmyths.registry.ModItemGroup.category(item);
            t.assertTrue(category>=0&&org.slavicmyths.registry.ModItemGroup.items(category).contains(item),"Missing creative item "+entry.getKey());
            t.assertTrue(!entry.getKey().startsWith("wood"),"Wooden weapon introduced");
        }
        for(var m:WeaponCatalog.MATERIALS)for(String kind:List.of("spear","sulitsa","flail","throwing_knife")) {
            ItemStack s=new ItemStack(WeaponCatalog.item(WeaponCatalog.weaponId(m.id(),kind)));
            t.assertTrue(Runes.capacity(s)==m.sockets()&&!Runes.category(s).isEmpty(),"Capacity/category "+m.id()+" "+kind);
        }
        for(Item item:List.of(Items.STONE_SWORD,Items.IRON_SWORD,Items.GOLDEN_AXE))t.assertTrue(Runes.capacity(new ItemStack(item))==1,"Common vanilla tier capacity");
        for(Item item:List.of(Items.DIAMOND_SWORD,Items.NETHERITE_AXE))t.assertTrue(Runes.capacity(new ItemStack(item))==2,"Advanced vanilla tier capacity");
        for(Item item:List.of(Items.BOW,Items.CROSSBOW,Items.TRIDENT,Items.MACE))t.assertTrue(Runes.max(new ItemStack(item))>0,"Unsupported vanilla weapon");
        t.assertTrue(Runes.category(new ItemStack(org.slavicmyths.registry.ModItems.NIGHTINGALE_DAGGER.get())).equals("dagger")&&Runes.category(new ItemStack(org.slavicmyths.registry.ModItems.POOL_SPEAR.get())).equals("spear"),"Existing weapon category mismatch");
        ItemStack grandfathered=new ItemStack(Items.DIAMOND_SWORD);ItemState.runes(grandfathered,3,List.of("heat","wind","life"));
        t.assertTrue(Runes.capacity(grandfathered)==2&&Runes.max(grandfathered)==3&&Runes.list(grandfathered).size()==3,"Existing rune data erased");
        var audit=new com.google.gson.JsonArray();
        for(Item item:net.minecraft.core.registries.BuiltInRegistries.ITEM) {
            ItemStack s=new ItemStack(item);String category=Runes.category(s);var id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
            if(!category.isEmpty()&&(id.getNamespace().equals("slavicmyths")||id.getNamespace().equals("minecraft"))) {
                var row=new com.google.gson.JsonObject();row.addProperty("id",id.toString());row.addProperty("category",category);row.addProperty("capacity",Runes.capacity(s));row.addProperty("durability",s.getMaxDamage());
                if(item instanceof TieredItem tiered)row.addProperty("material_damage_bonus",tiered.getTier().getAttackDamageBonus());audit.add(row);
            }
        }
        try{java.nio.file.Files.writeString(java.nio.file.Path.of("weapon-audit-1321.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(audit));}catch(Exception error){throw new RuntimeException(error);}t.succeed();
    }
    @GameTest(template="empty") public static void benchRecipesAndUpgrade(GameTestHelper t) {
        int count=0;var p=player(t,"WeaponBench");t.setBlock(new BlockPos(3,2,3),org.slavicmyths.registry.ModBlocks.ARMORER_TABLE.get());
        for(var holder:t.getLevel().getRecipeManager().getAllRecipesFor(ArmorerRecipe.TYPE.get()))if(holder.id().getPath().startsWith("weapon_")) {
            var recipe=holder.value();var input=new ArrayList<ItemStack>(Collections.nCopies(16,ItemStack.EMPTY));
            for(int n=0;n<recipe.getIngredients().size();n++)input.set(n,recipe.getIngredients().get(n).getItems()[0].copy());
            var grid=new ArmorerInput(4,4,input);t.assertTrue(recipe.matches(grid,t.getLevel()),"Recipe mismatch "+holder.id());
            ItemStack output=recipe.assemble(grid,t.getLevel().registryAccess());t.assertTrue(!output.isEmpty(),"Empty recipe "+holder.id());
            if(recipe.copyComponents) {
                input.set(0,marked(t,input.getFirst().getItem()));grid=new ArmorerInput(4,4,input);output=recipe.assemble(grid,t.getLevel().registryAccess());
                t.assertTrue(ItemState.runes(output).equals(ItemState.runes(input.getFirst()))&&output.get(DataComponents.ENCHANTMENTS).equals(input.getFirst().get(DataComponents.ENCHANTMENTS))&&output.getHoverName().equals(input.getFirst().getHoverName()),"Upgrade dropped components");
            }
            var menu=new ArmorerMenu(1,p.getInventory(),t.absolutePos(new BlockPos(3,2,3)));
            for(int n=0;n<16;n++)menu.input.setItem(n,input.get(n).copy());
            ItemStack taken=menu.quickMoveStack(p,0);t.assertTrue(ItemStack.matches(taken,output),"Native bench output mismatch "+holder.id());
            t.assertTrue(menu.input.getItems().stream().allMatch(ItemStack::isEmpty),"Bench duplicated inputs "+holder.id());p.getInventory().clearContent();count++;
        }
        t.assertTrue(count==36,"Bench recipe count "+count);System.out.println("WEAPON_BENCH_RECIPES_PASS count="+count);t.succeed();
    }
    @GameTest(template="empty") public static void partsAndCosts(GameTestHelper t) {
        int count=0;
        for(var holder:t.getLevel().getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING))if(holder.id().getPath().startsWith("weapon_part_")) {
            var recipe=(ShapedRecipe)holder.value();var stacks=new ArrayList<ItemStack>();
            for(var ingredient:recipe.getIngredients())stacks.add(ingredient.isEmpty()?ItemStack.EMPTY:ingredient.getItems()[0].copy());
            var grid=CraftingInput.of(recipe.getWidth(),recipe.getHeight(),stacks);
            t.assertTrue(recipe.matches(grid,t.getLevel())&&!recipe.assemble(grid,t.getLevel().registryAccess()).isEmpty(),"Part recipe failed "+holder.id());
            if(holder.id().getPath().endsWith("flail_ball"))t.assertTrue(stacks.stream().filter(s->!s.isEmpty()).count()==5&&recipe.getWidth()==3&&recipe.getHeight()==3,"Ball not five-material cross");
            if(holder.id().getPath().endsWith("flail_chain"))t.assertTrue(stacks.size()==3&&stacks.stream().allMatch(s->s.is(Items.CHAIN)),"Chain cost not 3 chains");
            if(holder.id().getPath().endsWith("iron_blank"))t.assertTrue(stacks.size()==3&&stacks.stream().allMatch(s->s.is(Items.IRON_NUGGET)),"Blank not 3 nuggets");
            count++;
        }
        t.assertTrue(count==23,"Part recipe count "+count);System.out.println("WEAPON_PART_RECIPES_PASS count="+count);t.succeed();
    }
    @GameTest(template="empty") public static void nativeReach(GameTestHelper t) {
        var p=player(t,"WeaponReach");double ordinary=p.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(WeaponCatalog.item("diamond_spear")));p.doTick();
        t.assertTrue(Math.abs(p.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)-ordinary-1.25)<.001,"Spear native range missing");
        for(int n=0;n<5;n++)p.doTick();t.assertTrue(p.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)==ordinary+1.25,"Range stacked");
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(WeaponCatalog.item("iron_sulitsa")));p.doTick();t.assertTrue(p.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)==ordinary+.5,"Sulitsa range wrong");
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_SWORD));p.doTick();t.assertTrue(p.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)==ordinary,"Range leaked after unequip");t.succeed();
    }
    @GameTest(template="empty") public static void nativeThrowsAndComponents(GameTestHelper t) {
        int count=0;var p=player(t,"WeaponThrow");
        for(var m:WeaponCatalog.MATERIALS)for(String kind:List.of("sulitsa","throwing_knife")) {
            var item=(ThrowingWeaponItem)WeaponCatalog.item(m.id()+"_"+kind);ItemStack input=marked(t,item);p.setItemInHand(InteractionHand.MAIN_HAND,input);
            item.use(t.getLevel(),p,InteractionHand.MAIN_HAND);if(!item.knife)item.releaseUsing(input,t.getLevel(),p,72000-20);
            var shots=projectiles(t,p);t.assertTrue(input.isEmpty()&&shots.size()==1,"Throw did not transfer one item "+m.id()+kind);
            var projectile=shots.getFirst();ItemStack recovered=projectile.getPickupItemStackOrigin();
            t.assertTrue(recovered.getDamageValue()==6&&ItemState.runes(recovered).runes().equals(List.of("heat"))&&recovered.get(DataComponents.ENCHANTMENTS)!=null&&recovered.getHoverName().getString().equals("KeptWeapon"),"Throw lost data/durability");
            CompoundTag saved=new CompoundTag();projectile.save(saved);var loaded=org.slavicmyths.registry.ModEntities.WEAPON_PROJECTILE.get().create(t.getLevel());loaded.load(saved);
            t.assertTrue(ItemStack.matches(recovered,loaded.getPickupItemStackOrigin()),"Projectile native save/load lost stack");
            projectile.discard();p.stopUsingItem();p.getCooldowns().removeCooldown(item);count++;
        }
        System.out.println("WEAPON_THROW_MATERIALS_PASS count="+count);t.succeed();
    }
    @GameTest(template="empty") public static void nativeKnifeCollisionAndLoss(GameTestHelper t) {
        var p=player(t,"KnifeNative");var cow=EntityType.COW.create(t.getLevel());cow.setNoAi(true);cow.setNoGravity(true);cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(10000);cow.setHealth(10000);cow.moveTo(p.getX(),p.getY(),p.getZ()+2);t.getLevel().addFreshEntity(cow);
        ItemStack weapon=marked(t,WeaponCatalog.item("iron_throwing_knife"));int kept=0;var seeds=new java.util.SplittableRandom(87123L);
        for(int n=0;n<512;n++) {
            cow.invulnerableTime=0;cow.setHealth(cow.getMaxHealth());float before=cow.getHealth();
            var shot=new WeaponProjectile(t.getLevel(),p,weapon,true,false);shot.getRandom().setSeed(seeds.nextLong());shot.setPos(p.getX(),p.getY()+.7,p.getZ());shot.setDeltaMovement(0,0,3);t.getLevel().addFreshEntity(shot);shot.tick();
            t.assertTrue(cow.getHealth()<before&&shot.isRemoved(),"Native knife collision failed "+n);
            var drops=t.getLevel().getEntitiesOfClass(ItemEntity.class,cow.getBoundingBox().inflate(3));t.assertTrue(drops.size()<=1,"Knife duplicate");
            for(var drop:drops){t.assertTrue(ItemStack.matches(drop.getItem(),weapon),"Recoverable knife data changed");t.assertTrue(drop.distanceTo(cow)<3,"Knife not near target");drop.discard();kept++;}
        }
        t.assertTrue(kept>=350&&kept<=418,"Knife loss outside broad 25% tolerance: "+kept+"/512");t.assertTrue(cow.isOnFire(),"Thrown heat rune ignored after item transfer");cow.discard();System.out.println("WEAPON_NATIVE_KNIFE_LOSS_PASS lost="+(512-kept)+" sample=512");t.succeed();
    }
    @GameTest(template="empty") public static void sulitsaCollisionPickupAndNoDuplicate(GameTestHelper t) {
        var p=player(t,"SulitsaNative");var cow=EntityType.COW.create(t.getLevel());cow.setNoAi(true);cow.moveTo(p.getX(),p.getY(),p.getZ()+2);t.getLevel().addFreshEntity(cow);
        ItemStack original=marked(t,WeaponCatalog.item("silver_sulitsa"));var shot=new WeaponProjectile(t.getLevel(),p,original,false,false);shot.setPos(p.getX(),p.getY()+.7,p.getZ());shot.setDeltaMovement(0,0,3);t.getLevel().addFreshEntity(shot);shot.tick();
        t.assertTrue(cow.getHealth()<cow.getMaxHealth()&&!shot.isRemoved(),"Sulitsa failed creature hit");shot.setNoPhysics(true);shot.shakeTime=0;
        shot.playerTouch(p);shot.playerTouch(p);long found=p.getInventory().items.stream().filter(s->s.is(original.getItem())).mapToLong(ItemStack::getCount).sum();
        t.assertTrue(shot.isRemoved()&&found==1,"Repeated native pickup duped sulitsa");var kept=p.getInventory().items.stream().filter(s->s.is(original.getItem())).findFirst().orElseThrow();t.assertTrue(ItemStack.matches(original,kept),"Pickup data changed");cow.discard();t.succeed();
    }
    @GameTest(template="empty") public static void nativeBowsAmmoAndBalance(GameTestHelper t) {
        var p=player(t,"BowNative");double[] damage=new double[3],speed=new double[3];int index=0;
        for(Item item:List.of(WeaponCatalog.item("short_bow"),Items.BOW,WeaponCatalog.item("heavy_bow"))) {
            ItemStack bow=new ItemStack(item);p.setItemInHand(InteractionHand.MAIN_HAND,bow);p.getInventory().add(new ItemStack(Items.ARROW,8));p.startUsingItem(InteractionHand.MAIN_HAND);
            int ticks=item instanceof FieldBowItem custom?custom.drawTicks():20;
            item.releaseUsing(bow,t.getLevel(),p,72000-ticks);
            var arrows=t.getLevel().getEntitiesOfClass(AbstractArrow.class,p.getBoundingBox().inflate(5));t.assertTrue(arrows.size()==1&&bow.getDamageValue()==1,"Native bow shot/durability failed");
            var arrow=arrows.getFirst();damage[index]=arrow.getBaseDamage()*arrow.getDeltaMovement().length();speed[index]=arrow.getDeltaMovement().length();
            t.assertTrue(p.getInventory().countItem(Items.ARROW)==7&&arrow.isCritArrow(),"Ammo consumed incorrectly");arrow.discard();p.getInventory().clearContent();p.stopUsingItem();index++;
        }
        t.assertTrue(damage[0]<damage[1]&&damage[2]>damage[1]&&speed[0]<speed[1]&&speed[2]>speed[1],"Bow roles not distinct");t.succeed();
    }
    @GameTest(template="empty") public static void flailArmorAndShield(GameTestHelper t) {
        var p=player(t,"FlailNative");var victim=player(t,"FlailShield");victim.moveTo(p.getX(),p.getY(),p.getZ()+2);victim.setYRot(180);
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(WeaponCatalog.item("flail")));t.assertTrue(p.canDisableShield(),"Native shield disable unsupported");
        for(int n=0;n<61;n++)victim.tick();victim.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.SHIELD));victim.startUsingItem(InteractionHand.OFF_HAND);for(int n=0;n<6;n++)victim.doTick();victim.invulnerableTime=0;
        boolean pvp=t.getLevel().getServer().isPvpAllowed();try{t.getLevel().getServer().setPvpAllowed(true);t.assertTrue(victim.isBlocking(),"Fixture shield not raised");victim.hurt(p.damageSources().playerAttack(p),8);t.assertTrue(victim.getCooldowns().isOnCooldown(Items.SHIELD),"Native shield not disabled");}finally{t.getLevel().getServer().setPvpAllowed(pvp);}
        var cow=EntityType.COW.create(t.getLevel());cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);cow.getAttribute(Attributes.ARMOR).setBaseValue(20);cow.setHealth(100);cow.moveTo(p.getX(),p.getY(),p.getZ()+2);t.getLevel().addFreshEntity(cow);cow.hurt(p.damageSources().playerAttack(p),8);
        t.assertTrue(cow.getHealth()<100&&cow.getHealth()>92,"Flail fully bypassed armor");cow.discard();t.succeed();
    }
    @GameTest(template="empty") public static void nativeRepairAndItemLifecycle(GameTestHelper t) {
        var p=player(t,"WeaponRepair");ItemStack input=marked(t,WeaponCatalog.item("diamond_sulitsa"));input.setDamageValue(100);
        var menu=new net.minecraft.world.inventory.AnvilMenu(1,p.getInventory(),net.minecraft.world.inventory.ContainerLevelAccess.create(t.getLevel(),p.blockPosition()));
        menu.getSlot(0).set(input.copy());menu.getSlot(1).set(new ItemStack(Items.DIAMOND));menu.createResult();ItemStack repaired=menu.getSlot(2).getItem();
        t.assertTrue(!repaired.isEmpty()&&repaired.getDamageValue()<100&&ItemState.runes(repaired).equals(ItemState.runes(input))&&repaired.get(DataComponents.ENCHANTMENTS).equals(input.get(DataComponents.ENCHANTMENTS)),"Native anvil lost runes/enchantments");
        var crafting=t.getLevel().getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING).stream().filter(h->h.id().toString().equals("minecraft:repair_item")).findFirst().orElseThrow().value();t.assertTrue(crafting instanceof org.slavicmyths.rpg.RuneRepair,"Native repair recipe not replaced safely");ItemStack other=new ItemStack(input.getItem());other.setDamageValue(120);
        var grid=CraftingInput.of(2,1,List.of(input,other));t.assertTrue(crafting.matches(grid,t.getLevel())&&ItemState.runes(crafting.assemble(grid,t.getLevel().registryAccess())).equals(ItemState.runes(input)),"Craft repair lost rune state");
        ItemState.runes(other,1,List.of("wind"));t.assertTrue(!crafting.matches(grid,t.getLevel()),"Conflicting runes silently consumed");
        var grind=new net.minecraft.world.inventory.GrindstoneMenu(1,p.getInventory());grind.getSlot(0).set(input.copy());ItemStack ground=grind.getSlot(2).getItem();t.assertTrue(!ground.isEmpty()&&ItemState.runes(ground).equals(ItemState.runes(input)),"Native grindstone erased runes");
        grind.getSlot(1).set(other.copy());t.assertTrue(grind.getSlot(2).getItem().isEmpty(),"Grindstone consumed conflicting runes");
        var drop=new ItemEntity(t.getLevel(),p.getX(),p.getY(),p.getZ(),input.copy());drop.setNoPickUpDelay();t.getLevel().addFreshEntity(drop);drop.playerTouch(p);if(!drop.isRemoved())drop.playerTouch(p);
        t.assertTrue(p.getInventory().countItem(input.getItem())==1,"Native dropped item duplicated");var recovered=p.getInventory().items.stream().filter(s->s.is(input.getItem())).findFirst().orElseThrow();t.assertTrue(ItemStack.matches(input,recovered),"Drop/pickup data lost");t.succeed();
    }
    @GameTest(template="empty") public static void nativePlayerStorageRestart(GameTestHelper t) {
        String phase=System.getProperty("slavicmyths.weaponRestartPhase","none");var w=t.getLevel();var p=new ServerPlayer(w.getServer(),w,new com.mojang.authlib.GameProfile(UUID.fromString("b5f61518-df64-47de-821e-aaa001321321"),"WeaponRestart"),ClientInformation.createDefault());p.connection=new Listener(p);var expected=java.nio.file.Path.of("weapon-restart-1321.nbt");
        try {
            if(!phase.equals("verify")) {
                var stacks=new ListTag();for(var m:WeaponCatalog.MATERIALS)for(String kind:List.of("spear","sulitsa","flail","throwing_knife")) {
                    ItemStack s=marked(t,WeaponCatalog.item(WeaponCatalog.weaponId(m.id(),kind)));p.getInventory().add(s.copy());stacks.add(s.save(w.registryAccess()));
                }
                for(String id:List.of("short_bow","heavy_bow")){ItemStack s=marked(t,WeaponCatalog.item(id));p.getInventory().add(s.copy());stacks.add(s.save(w.registryAccess()));}
                CompoundTag data=new CompoundTag();data.put("Weapons",stacks);NbtIo.writeCompressed(data,expected);w.getServer().getPlayerList().remove(p);System.out.println("WEAPON_NATIVE_STORAGE_WRITE_PASS items=30");
            }
            if(!phase.equals("write")) {
                t.assertTrue(w.getServer().getPlayerList().load(p).isPresent(),"Player .dat absent");var data=NbtIo.readCompressed(expected,NbtAccounter.unlimitedHeap()).getList("Weapons",10);
                for(var tag:data){ItemStack s=ItemStack.parse(w.registryAccess(),tag).orElseThrow();t.assertTrue(p.getInventory().items.stream().anyMatch(item->ItemStack.matches(item,s)),"Native restart lost "+s.getItem());}
                t.assertTrue(data.size()==30,"Incomplete persisted weapon catalog");System.out.println("WEAPON_NATIVE_STORAGE_"+(phase.equals("verify")?"NEW_PROCESS":"SAME_PROCESS")+"_PASS items=30");
            }
            t.succeed();
        }catch(Exception error){throw new RuntimeException(error);}
    }
    @GameTest(template="empty") public static void nativeBlockRecoveryAndCreative(GameTestHelper t) {
        var p=player(t,"KnifeBlock");t.setBlock(new BlockPos(3,2,5),Blocks.STONE);
        ItemStack stack=marked(t,WeaponCatalog.item("gold_throwing_knife"));
        for(boolean creative:List.of(false,true)) {
            var projectile=new WeaponProjectile(t.getLevel(),p,stack,true,creative);projectile.setPos(p.getX(),p.getY()+.7,p.getZ());projectile.setDeltaMovement(0,0,3);t.getLevel().addFreshEntity(projectile);projectile.tick();
            t.assertTrue(projectile.isRemoved(),"Knife did not resolve block");var drops=t.getLevel().getEntitiesOfClass(ItemEntity.class,p.getBoundingBox().inflate(5));
            t.assertTrue(drops.size()==(creative?0:1),"Block recovery loss/creative duplication");for(var drop:drops){t.assertTrue(ItemStack.matches(drop.getItem(),stack),"Block recovered wrong data");drop.discard();}
        }
        var item=(ThrowingWeaponItem)WeaponCatalog.item("iron_sulitsa");ItemStack weapon=new ItemStack(item);p.setItemInHand(InteractionHand.MAIN_HAND,weapon);item.use(t.getLevel(),p,InteractionHand.MAIN_HAND);item.releaseUsing(weapon,t.getLevel(),p,71995);
        t.assertTrue(!weapon.isEmpty()&&projectiles(t,p).isEmpty(),"Uncharged sulitsa consumed item");p.stopUsingItem();
        p.setGameMode(GameType.CREATIVE);ItemStack knife=new ItemStack(WeaponCatalog.item("iron_throwing_knife"));p.setItemInHand(InteractionHand.MAIN_HAND,knife);knife.getItem().use(t.getLevel(),p,InteractionHand.MAIN_HAND);
        t.assertTrue(knife.getCount()==1&&knife.getDamageValue()==0,"Creative throw consumed source");for(var shot:projectiles(t,p))shot.discard();t.succeed();
    }
    @GameTest(template="empty") public static void nativeDeathDropAndRespawn(GameTestHelper t) {
        var p=player(t,"WeaponDeath");ItemStack weapon=marked(t,WeaponCatalog.item("perunite_sulitsa"));p.getInventory().add(weapon.copy());p.setRespawnPosition(t.getLevel().dimension(),t.absolutePos(new BlockPos(3,2,3)),0,true,false);
        p.kill();t.assertTrue(!p.isAlive(),"Native death failed");var drops=t.getLevel().getEntitiesOfClass(ItemEntity.class,p.getBoundingBox().inflate(5),e->e.getItem().is(weapon.getItem()));t.assertTrue(drops.size()==1,"Native death lost/duplicated weapon");
        var next=t.getLevel().getServer().getPlayerList().respawn(p,false,Entity.RemovalReason.KILLED);next.connection.player=next;
        try {
            var drop=drops.getFirst();drop.setNoPickUpDelay();drop.playerTouch(next);t.assertTrue(next.getInventory().items.stream().anyMatch(s->ItemStack.matches(s,weapon)),"Death/respawn pickup lost components");
        }finally{t.getLevel().getServer().getPlayerList().remove(next);}
        t.succeed();
    }
    @GameTest(template="empty") public static void oldAndNewRecipeWire(GameTestHelper t) {
        int checked=0;
        for(var holder:t.getLevel().getRecipeManager().getAllRecipesFor(ArmorerRecipe.TYPE.get())) {
            var recipe=holder.value();var buffer=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),t.getLevel().registryAccess());
            try {
                var serializer=(ArmorerRecipe.Serializer)recipe.getSerializer();serializer.streamCodec().encode(buffer,recipe);var decoded=serializer.streamCodec().decode(buffer);
                t.assertTrue(decoded.width==recipe.width&&decoded.height==recipe.height&&decoded.shapeless==recipe.shapeless&&decoded.copyComponents==recipe.copyComponents&&ItemStack.matches(decoded.getResultItem(t.getLevel().registryAccess()),recipe.getResultItem(t.getLevel().registryAccess())),"Recipe wire changed "+holder.id());
                t.assertTrue(buffer.readableBytes()==0,"Unread recipe bytes");checked++;
            }finally{buffer.release();}
        }
        t.assertTrue(checked>33,"Historical bench recipes absent");System.out.println("WEAPON_RECIPE_WIRE_PASS count="+checked);t.succeed();
    }
    @GameTest(template="empty") public static void loadedOwnerGuardAndFlightBound(GameTestHelper t) {
        var p=player(t,"KnifeOwnerGone");ItemStack weapon=marked(t,WeaponCatalog.item("iron_throwing_knife"));
        var source=new WeaponProjectile(t.getLevel(),p,weapon,true,false);CompoundTag tag=new CompoundTag();source.save(tag);
        var loaded=org.slavicmyths.registry.ModEntities.WEAPON_PROJECTILE.get().create(t.getLevel());loaded.load(tag);loaded.setPos(p.getX(),p.getY()+.7,p.getZ());loaded.setDeltaMovement(0,0,3);
        var cow=EntityType.COW.create(t.getLevel());cow.setNoAi(true);cow.moveTo(p.getX(),p.getY(),p.getZ()+2);t.getLevel().addFreshEntity(cow);t.getLevel().addFreshEntity(loaded);
        t.assertTrue(loaded.getOwner()==null,"Fixture owner unexpectedly present after load");loaded.tick();t.assertTrue(cow.getHealth()==cow.getMaxHealth(),"Ownerless loaded shot bypassed PvP/team rules");
        loaded.setNoGravity(true);loaded.setDeltaMovement(Vec3.ZERO);for(int n=0;n<25&&!loaded.isRemoved();n++)loaded.tick();
        t.assertTrue(loaded.isRemoved(),"Flight lifetime unbounded");var drops=t.getLevel().getEntitiesOfClass(ItemEntity.class,loaded.getBoundingBox().inflate(3));t.assertTrue(drops.size()==1&&ItemStack.matches(drops.getFirst().getItem(),weapon),"Timed recovery lost/duplicated stack");drops.getFirst().discard();cow.discard();t.succeed();
    }
    @GameTest(template="empty") public static void nativeDimensionItemState(GameTestHelper t) {
        var p=player(t,"WeaponDimension");var expected=new ArrayList<ItemStack>();
        for(var m:WeaponCatalog.MATERIALS)for(String kind:List.of("spear","sulitsa","flail","throwing_knife"))expected.add(marked(t,WeaponCatalog.item(WeaponCatalog.weaponId(m.id(),kind))));
        for(String id:List.of("short_bow","heavy_bow"))expected.add(marked(t,WeaponCatalog.item(id)));
        for(ItemStack s:expected)p.getInventory().add(s.copy());
        var origin=p.position();var world=t.getLevel();world.addNewPlayer(p);
        try {
            var nether=world.getServer().getLevel(net.minecraft.world.level.Level.NETHER);
            t.assertTrue(p.changeDimension(new net.minecraft.world.level.portal.DimensionTransition(nether,new Vec3(0,80,0),Vec3.ZERO,0,0,net.minecraft.world.level.portal.DimensionTransition.DO_NOTHING))==p&&p.serverLevel()==nether,"Native dimension travel failed");
            t.assertTrue(p.changeDimension(new net.minecraft.world.level.portal.DimensionTransition(world,origin,Vec3.ZERO,0,0,net.minecraft.world.level.portal.DimensionTransition.DO_NOTHING))==p&&p.serverLevel()==world,"Native return travel failed");
            for(ItemStack s:expected)t.assertTrue(p.getInventory().items.stream().anyMatch(item->ItemStack.matches(item,s)),"Dimension changed weapon state");
            System.out.println("WEAPON_NATIVE_DIMENSION_PASS transitions=2 items=30");
        }finally{world.getServer().getPlayerList().remove(p);}
        t.succeed();
    }
}
