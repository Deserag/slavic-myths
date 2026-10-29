package org.slavicmyths.hellvisualqa;

import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import com.google.gson.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.slavicmyths.navya.*;
import org.slavicmyths.blackcinder.*;
import org.slavicmyths.losthalls.*;
import org.slavicmyths.burningislands.*;
import org.slavicmyths.trophy.*;

/** Actual framebuffer only. Artificial creative fixtures are never labelled survival/AI/owner acceptance. */
@EventBusSubscriber(modid="slavicmyths_hellvisualqa",value=Dist.CLIENT)
public final class HellVisualCapture {
    static final Path OUT=Path.of(System.getProperty("hellvisual.output"));
    record View(String name,double x,double y,double z,double tx,double ty,double tz,
                String item,String mode,String armor,boolean crouch) {}
    static final List<View> VIEWS=new ArrayList<>();
    static final Map<String,Mob> ACTORS=new LinkedHashMap<>();
    static final JsonArray FRAMES=new JsonArray();
    static boolean opened,requested,finished;static volatile boolean ready,positioned;
    static int scene=-1,age;static String loadedSha;
    static ItemStack item(String id){return new ItemStack(BuiltInRegistries.ITEM.get(id.contains(":")?ResourceLocation.parse(id):ResourceLocation.fromNamespaceAndPath("slavicmyths",id)));}
    static void views(String name,double x,double z,double height,double distance){
        double y=81+height*.5;
        for(var d:new double[][]{{0,1},{.7,.7},{1,0},{0,-1}}){
            String angle=new String[]{"front","quarter","side","back"}[VIEWS.size()%4];
            VIEWS.add(new View(name+"-"+angle,x+d[0]*distance,y-1.62,z+d[1]*distance,x,y,z,"","spectator","",false));
        }
    }
    static void spawn(ServerLevel w,Mob mob,int index){spawn(w,mob,index,BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).getPath());}
    static void spawn(ServerLevel w,Mob mob,int index,String label){
        double x=index*16;
        for(int bx=(int)x-6;bx<=x+6;bx++)for(int bz=-6;bz<=6;bz++)w.setBlock(new BlockPos(bx,80,bz),Blocks.SMOOTH_STONE.defaultBlockState(),2);
        mob.moveTo(x,81,0,0,0);mob.setNoAi(true);mob.setPersistenceRequired();mob.setInvulnerable(true);mob.setYBodyRot(0);mob.setYHeadRot(0);w.addFreshEntity(mob);
        ACTORS.put(label,mob);views(label,x,0,mob.getBbHeight(),Math.max(4,mob.getBbHeight()*1.6));
    }
    static void gear(String name,String held,String mode,String armor,boolean crouch){
        VIEWS.add(new View(name,0,81,120,0,82,110,held,mode,armor,crouch));
    }
    static void setup(ServerPlayer p){
        var w=p.serverLevel();w.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,w.getServer());
        w.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,w.getServer());w.setDayTime(6000);
        int i=0;
        for(var k:NavyaContent.Kind.values())spawn(w,NavyaContent.MOBS.get(k).get().create(w),i++);
        for(var k:CinderContent.Kind.values())spawn(w,CinderContent.TYPES.get(k).get().create(w),i++);
        for(var k:HallsContent.Kind.values())for(int variant=0;variant<(k==HallsContent.Kind.SHEPTUN?4:1);variant++){
            var mob=HallsContent.TYPES.get(k).get().create(w);var tag=new net.minecraft.nbt.CompoundTag();
            mob.addAdditionalSaveData(tag);tag.putInt("HallsVariant",variant);mob.readAdditionalSaveData(tag);
            spawn(w,mob,i++,HallsContent.id(k)+(k==HallsContent.Kind.SHEPTUN?"-variant-"+variant:""));
        }
        spawn(w,PostenMob.TYPE.get().create(w),i++);
        for(var k:IslandsContent.Kind.values())spawn(w,IslandsContent.TYPES.get(k).get().create(w),i++);
        for(int x=-12;x<=12;x++)for(int z=94;z<=140;z++)w.setBlock(new BlockPos(x,80,z),Blocks.SMOOTH_STONE.defaultBlockState(),2);
        var kinds=List.of(TrophyHeads.Kind.GROVE_GUARDIAN,TrophyHeads.Kind.ZHARENITSA,TrophyHeads.Kind.POSTEN,TrophyHeads.Kind.MAGMA_LORD);
        for(int n=0;n<kinds.size();n++){
            double x=-6+n*4;w.setBlock(new BlockPos((int)x,81,100),TrophyHeads.BLOCKS.get(kinds.get(n)).get().defaultBlockState(),3);
            views(kinds.get(n).id,x+.5,100.5,1,2.7);
        }
        for(String armor:List.of("king","zhara")){
            gear(armor+"-front","","front",armor,false);gear(armor+"-back","","back",armor,false);
            gear(armor+"-crouch-back","","back",armor,true);gear(armor+"-inventory","","inventory",armor,false);
        }
        for(String id:List.of("short_bow","heavy_bow","infernal_bow")){
            gear(id+"-first",id,"first","king",false);gear(id+"-draw",id,"draw","king",false);
            gear(id+"-third",id,"front","king",false);gear(id+"-inventory",id,"inventory","king",false);
        }
        for(String id:List.of("chud_master_pickaxe","chud_multitool_axe","chud_multitool_pickaxe","chud_multitool_shovel")){
            gear(id+"-first",id,"first","",false);gear(id+"-inventory",id,"inventory","",false);
        }
        gear("king-run-back","","run","king",false);gear("king-jump-back","","jump","king",false);
        gear("king-chest-only-back","","back","chest_only",false);gear("king-elytra-replacement","","back","elytra",false);
        for(String id:List.of("short_bow","heavy_bow","infernal_bow"))gear(id+"-draw-left",id,"draw_left","king",false);
        for(String id:List.of("short_bow","heavy_bow","infernal_bow")){
            gear(id+"-draw-third",id,"draw_third","king",false);gear(id+"-draw-third-left",id,"draw_third_left","king",false);
        }
        gear("king-glint-front","","front","king_glint",false);gear("king-glint-back","","back","king_glint",false);
        gear("king-trim-back","","back","king_trim",false);
        gear("vanilla-bow-first","minecraft:bow","first","king",false);gear("vanilla-bow-draw","minecraft:bow","draw","king",false);
        gear("vanilla-bow-third","minecraft:bow","front","king",false);gear("vanilla-bow-inventory","minecraft:bow","inventory","king",false);
        gear("vanilla-bow-draw-left","minecraft:bow","draw_left","king",false);
        for(String id:List.of("navya_ostroga","ashen_greatsword","shadow_throwing_knife","volcanic_heart_flail")){
            gear(id+"-first",id,"first","king",false);gear(id+"-third",id,"front","king",false);gear(id+"-inventory",id,"inventory","king",false);
            if(!id.equals("shadow_throwing_knife"))gear(id+"-charge",id,"charge","king",true);
        }
        gear("actual-heads-tab","","heads_tab","",false);gear("actual-tools-tab","","tools_tab","",false);gear("actual-armor-tab","","armor_tab","",false);
        for(String id:List.of("bone_crawler","coal_crawler")){
            var mob=ACTORS.get(id);double x=mob.getX(),y=81+mob.getBbHeight()*.5;
            VIEWS.add(new View(id+"-walk",x+4.5,y-1.62,5,x+.75,y,0,"","mobwalk","",false));
        }
        if(Boolean.getBoolean("hellvisual.gearOnly"))VIEWS.removeIf(v->v.mode.equals("spectator"));
        String scope=System.getProperty("hellvisual.scope","all");
        if(!Set.of("all","gear","motion","regressions").contains(scope))throw new IllegalArgumentException("Unknown isolated visual scope: "+scope);
        if(scope.equals("motion"))VIEWS.removeIf(v->!v.mode.equals("mobwalk"));
        else if(scope.equals("gear"))VIEWS.removeIf(v->v.mode.equals("spectator")||v.mode.equals("mobwalk"));
        else if(scope.equals("regressions"))VIEWS.removeIf(v->v.mode.equals("spectator"));
        ready=true;
    }
    static void position(ServerPlayer p,View v){
        p.stopUsingItem();p.setShiftKeyDown(v.crouch);p.setGameMode(v.mode.equals("spectator")||v.mode.equals("mobwalk")?GameType.SPECTATOR:GameType.CREATIVE);
        for(var actor:ACTORS.values())if(!actor.isNoAi()){actor.getNavigation().stop();actor.setNoAi(true);}
        if(v.mode.equals("mobwalk")){
            var actor=ACTORS.get(v.name.substring(0,v.name.length()-5));
            // A NoAi actor placed exactly on the slab has never integrated a landing.
            // GroundPathNavigation refuses canUpdatePath while onGround is still false.
            // These temporary tickets belong only to the disposable visual fixture.
            var chunk=actor.chunkPosition();
            for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)p.serverLevel().setChunkForced(chunk.x+dx,chunk.z+dz,true);
            if(!p.serverLevel().getBlockState(actor.blockPosition().below()).is(Blocks.SMOOTH_STONE))throw new AssertionError("Crawler fixture floor missing");
            actor.setOnGround(true);actor.setNoAi(false);actor.setTarget(null);
            if(!actor.getNavigation().moveTo(actor.getX()+4,actor.getY(),actor.getZ(),.7))throw new AssertionError("Actual crawler navigation refused fixture route: "+v.name);
        }
        p.getInventory().clearContent();p.getInventory().selected=0;
        p.setItemInHand(InteractionHand.MAIN_HAND,v.item.isEmpty()?ItemStack.EMPTY:item(v.item));
        p.getInventory().setItem(9,new ItemStack(Items.ARROW,64));
        String[] ids=Set.of("king","king_glint","king_trim","chest_only","elytra").contains(v.armor)?new String[]{"infernal_crown_helmet","infernal_king_cuirass","infernal_king_leggings","infernal_king_boots"}:
                v.armor.equals("zhara")?new String[]{"zhara_helmet","zhara_chestplate","zhara_leggings","zhara_boots"}:null;
        var slots=new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
        for(int n=0;n<4;n++)p.setItemSlot(slots[n],ids==null?ItemStack.EMPTY:item(ids[n]));
        if(v.armor.equals("king_glint"))for(var slot:slots)p.getItemBySlot(slot).enchant(p.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING),1);
        if(v.armor.equals("king_trim"))for(var slot:slots)p.getItemBySlot(slot).set(net.minecraft.core.component.DataComponents.TRIM,new net.minecraft.world.item.armortrim.ArmorTrim(
                p.registryAccess().lookupOrThrow(Registries.TRIM_MATERIAL).getOrThrow(net.minecraft.world.item.armortrim.TrimMaterials.GOLD),
                p.registryAccess().lookupOrThrow(Registries.TRIM_PATTERN).getOrThrow(net.minecraft.world.item.armortrim.TrimPatterns.SENTRY)));
        if(v.armor.equals("chest_only"))for(var slot:List.of(EquipmentSlot.HEAD,EquipmentSlot.LEGS,EquipmentSlot.FEET))p.setItemSlot(slot,ItemStack.EMPTY);
        if(v.armor.equals("elytra"))p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.ELYTRA));
        p.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        double dx=v.tx-v.x,dz=v.tz-v.z,dy=v.ty-(v.y+p.getEyeHeight());
        p.teleportTo(p.serverLevel(),v.x,v.y,v.z,(float)Math.toDegrees(Math.atan2(-dx,dz)),(float)-Math.toDegrees(Math.atan2(dy,Math.hypot(dx,dz))));
        p.getInventory().setChanged();p.inventoryMenu.broadcastChanges();positioned=true;
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event)throws Exception {
        if(finished)return;
        if(!"true".equals(System.getenv("GITHUB_ACTIONS"))||!"github-hosted".equals(System.getenv("RUNNER_ENVIRONMENT")))throw new IllegalStateException("Isolated hosted runner only");
        var mc=Minecraft.getInstance();
        if(mc.screen!=null&&mc.screen.getClass().getSimpleName().equals("AccessibilityOnboardingScreen")){mc.setScreen(new TitleScreen());return;}
        if(!opened&&mc.screen instanceof TitleScreen){
            opened=true;Files.createDirectories(OUT);
            var jar=net.neoforged.fml.ModList.get().getModFileById("slavicmyths").getFile().getFilePath();
            if(!jar.toString().endsWith(".jar"))throw new AssertionError("Production mod must load from actual packaged JAR: "+jar);
            loadedSha=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(jar)));
            mc.options.pauseOnLostFocus=false;mc.options.renderDistance().set(5);mc.options.simulationDistance().set(5);
            mc.options.enableVsync().set(false);mc.options.framerateLimit().set(60);mc.options.guiScale().set(2);mc.options.fov().set(55);
            mc.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;
            mc.createWorldOpenFlows().createFreshLevel("HellVisual-"+System.currentTimeMillis(),new LevelSettings("Isolated artificial Hell visual fixtures",GameType.CREATIVE,false,Difficulty.NORMAL,true,new GameRules(),WorldDataConfiguration.DEFAULT),new WorldOptions(3492026L,true,false),r->r.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),mc.screen);return;
        }
        if(mc.level==null||mc.player==null||mc.getSingleplayerServer()==null)return;
        if(!requested){requested=true;mc.getSingleplayerServer().execute(()->setup(mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst()));return;}
        if(!ready)return;
        if(!mc.player.getSkin().model().toString().equals(System.getProperty("hellvisual.expectedModel")))throw new AssertionError("Unexpected actual skin model: "+mc.player.getSkin().model());
        if(scene<0||age>=65){
            scene++;age=0;positioned=false;mc.options.keyUse.setDown(false);mc.options.keyShift.setDown(false);mc.options.keyUp.setDown(false);mc.options.keySprint.setDown(false);mc.options.keyJump.setDown(false);mc.player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);mc.setScreen(null);
            if(scene==VIEWS.size()){
                finished=true;var receipt=new JsonObject();receipt.addProperty("jar_sha256",loadedSha);receipt.addProperty("seed",3492026);
                receipt.addProperty("scope","Actual packaged-mod framebuffer; artificial creative poses, not survival/AI/audio/owner acceptance");receipt.add("frames",FRAMES);
                Files.writeString(OUT.resolve("finished.json"),new GsonBuilder().setPrettyPrinting().create().toJson(receipt));mc.stop();return;
            }
            var view=VIEWS.get(scene);mc.options.hideGui=!(view.mode.equals("inventory")||view.mode.startsWith("first")||view.mode.startsWith("draw")||view.mode.equals("charge")||view.mode.endsWith("_tab"));
            mc.player.setMainArm(view.mode.endsWith("_left")?HumanoidArm.LEFT:HumanoidArm.RIGHT);
            mc.options.setCameraType(view.mode.equals("front")||view.mode.startsWith("draw_third")?CameraType.THIRD_PERSON_FRONT:Set.of("back","run","jump").contains(view.mode)?CameraType.THIRD_PERSON_BACK:CameraType.FIRST_PERSON);
            mc.getSingleplayerServer().execute(()->position(mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst(),view));return;
        }
        if(!positioned)return;age++;
        var view=VIEWS.get(scene);mc.options.keyShift.setDown(view.crouch);
        if(view.mode.startsWith("draw")||view.mode.equals("charge"))mc.options.keyUse.setDown(true);
        if(view.mode.equals("run")){mc.options.keyUp.setDown(true);mc.options.keySprint.setDown(true);}
        if(view.mode.equals("jump"))mc.options.keyJump.setDown(true);
        if((view.mode.equals("inventory")||view.mode.endsWith("_tab"))&&age==25)mc.setScreen(new InventoryScreen(mc.player));
        if(view.mode.endsWith("_tab")&&age==35){
            if(!(mc.screen instanceof net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen creative))throw new AssertionError("Actual creative screen not open");
            int category=view.mode.equals("heads_tab")?11:view.mode.equals("tools_tab")?4:5;
            var tab=org.slavicmyths.registry.ModItemGroup.THEMATIC.get(category).get();
            var field=net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.class.getDeclaredField("pages");field.setAccessible(true);
            @SuppressWarnings("unchecked") var pages=(List<net.neoforged.neoforge.client.gui.CreativeTabsScreenPage>)field.get(creative);
            var page=pages.stream().filter(pg->pg.getVisibleTabs().contains(tab)).findFirst().orElseThrow();creative.setCurrentPage(page);
            var select=net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.class.getDeclaredMethod("selectTab",CreativeModeTab.class);select.setAccessible(true);select.invoke(creative,tab);
            if(category==11&&tab.getDisplayItems().size()!=11)throw new AssertionError("Actual heads tab must contain11 heads: "+tab.getDisplayItems().size());
            if(category==4)for(String id:List.of("chud_mining_lantern","chud_master_pickaxe","chud_multitool_axe","chud_multitool_pickaxe","chud_multitool_shovel","chud_mining_lift"))
                if(tab.getDisplayItems().stream().noneMatch(s->s.is(item(id).getItem())))throw new AssertionError("Actual tools tab missing "+id);
            System.out.println("HELL_VISUAL_ACTUAL_TAB "+view.mode+" items="+tab.getDisplayItems().size()+" title="+tab.getDisplayName().getString());
        }
        if(age==55||(Set.of("run","jump","charge","mobwalk").contains(view.mode)&&Set.of(15,25,35,45).contains(age))){
            if(view.mode.startsWith("draw")&&age==55&&(!mc.player.isUsingItem()||mc.player.getTicksUsingItem()<24))throw new AssertionError("Actual bow draw input did not remain active: "+view.name);
            String file=String.format("%03d-%s%s.png",scene,view.name,age==55?"":"-t"+age);
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUT.resolve(file));}
            var frame=new JsonObject();frame.addProperty("file",file);frame.addProperty("skin_model",mc.player.getSkin().model().toString());
            frame.addProperty("server_tick",mc.level.getGameTime());frame.addProperty("mode",view.mode);FRAMES.add(frame);
            frame.addProperty("x",mc.player.getX());frame.addProperty("y",mc.player.getY());frame.addProperty("z",mc.player.getZ());
            frame.addProperty("age",age);frame.addProperty("using_item",mc.player.isUsingItem());
            frame.addProperty("held_item",BuiltInRegistries.ITEM.getKey(mc.player.getMainHandItem().getItem()).toString());
            if(view.mode.equals("mobwalk")){
                var actor=ACTORS.get(view.name.substring(0,view.name.length()-5));var visible=mc.level.getEntity(actor.getId());
                if(!(visible instanceof LivingEntity living))throw new AssertionError("Actual moving crawler not tracked by client");
                frame.addProperty("actor_x",living.getX());frame.addProperty("actor_y",living.getY());frame.addProperty("actor_z",living.getZ());
                frame.addProperty("actor_walk_speed",living.walkAnimation.speed(1));
            }
            System.out.println("HELL_VISUAL_FRAME "+file+" jar="+loadedSha);
        }
    }
}
