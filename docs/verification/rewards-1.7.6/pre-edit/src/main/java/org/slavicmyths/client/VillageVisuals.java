package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.*;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.village.*;

/** Extends the vanilla renderers: the vanilla face, animation, item/armor layers and shadow remain. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class VillageVisuals {
    private static final String[] NAMES={"none","nitwit","farmer","fisherman","shepherd","librarian","cartographer","leatherworker","butcher","mason","fletcher","toolsmith","weaponsmith","armorer","cleric","miller","brewer","weaver","herder","hunter","cook","druzhinnik"};
    private static final Map<VillagerProfession,Style> STYLES=new IdentityHashMap<>();
    private static final ResourceLocation[][] BASE=new ResourceLocation[3][2];
    private static final ResourceLocation[] LEVEL=new ResourceLocation[5],BADGE=new ResourceLocation[5];
    private static final ResourceLocation DAMAGE=texture("villager/zombie/damage"),MATERIALS=texture("villager/accessory/materials"),MERCHANT=texture("merchant/travel_coat");
    private static final ModelPart MERCHANT_PARTS=VillageOutfitGeometry.parts("merchant",0,OutfitRules.Climate.TEMPERATE);
    private record Style(ResourceLocation[] cloth,ModelPart[][] parts) {}
    static {
        for(var climate:OutfitRules.Climate.values())for(int v=0;v<2;v++)BASE[climate.ordinal()][v]=texture("villager/climate/"+climate.name().toLowerCase(Locale.ROOT)+"_"+(v==0?"a":"b"));
        String[] badge={"stone","iron","gold","emerald","diamond"};
        for(int i=0;i<5;i++){if(i>0)LEVEL[i]=texture("villager/level/"+(i+1));BADGE[i]=ResourceLocation.withDefaultNamespace("textures/entity/villager/profession_level/"+badge[i]+".png");}
    }
    private static ResourceLocation texture(String name){return VillageRoles.id("textures/entity/"+name+".png");}
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){
        e.registerEntityRenderer(EntityType.VILLAGER,FolkVillagerRenderer::new);
        e.registerEntityRenderer(EntityType.ZOMBIE_VILLAGER,FolkZombieRenderer::new);
    }
    private static boolean longCoat(LivingEntity entity,VillagerData data){
        return entity.isBaby() || (Math.max(0,entity.getData(VillageRoles.OUTFIT))&1)==1
            || OutfitRules.climate(BuiltInRegistries.VILLAGER_TYPE.getKey(data.getType()).getPath())==OutfitRules.Climate.COLD;
    }
    /** Only controls the existing robe part; the vanilla renderer does all remaining work. */
    private static final class FolkVillagerRenderer extends VillagerRenderer {
        FolkVillagerRenderer(EntityRendererProvider.Context ctx){super(ctx);}
        @Override public void render(Villager entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
            getModel().root().getChild("arms").visible=!org.slavicmyths.military.Military.guard(entity);
            getModel().root().getChild("body").getChild("jacket").visible=longCoat(entity,entity.getVillagerData());
            super.render(entity,yaw,partial,pose,buffers,light);
        }
    }
    private static final class FolkZombieRenderer extends ZombieVillagerRenderer {
        FolkZombieRenderer(EntityRendererProvider.Context ctx){super(ctx);model=new ZombieVillagerModel<>(VillageOutfitGeometry.zombieBody());}
        @Override public void render(ZombieVillager entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
            getModel().body.getChild("jacket").visible=longCoat(entity,entity.getVillagerData());
            super.render(entity,yaw,partial,pose,buffers,light);
        }
    }
    @SubscribeEvent public static void layers(EntityRenderersEvent.AddLayers e){
        STYLES.clear();
        for(String name:NAMES){
            ResourceLocation key=name.equals("miller")||name.equals("brewer")||name.equals("weaver")||name.equals("herder")||name.equals("hunter")||name.equals("cook")||name.equals("druzhinnik")?VillageRoles.id(name):ResourceLocation.withDefaultNamespace(name);
            if(!BuiltInRegistries.VILLAGER_PROFESSION.containsKey(key))continue;
            ModelPart[][] shapes=new ModelPart[3][2];
            for(var climate:OutfitRules.Climate.values())for(int v=0;v<2;v++)shapes[climate.ordinal()][v]=VillageOutfitGeometry.parts(name,v,climate);
            STYLES.put(BuiltInRegistries.VILLAGER_PROFESSION.get(key),new Style(new ResourceLocation[]{texture("villager/profession/"+name+"_a"),texture("villager/profession/"+name+"_b")},shapes));
        }
        VillagerRenderer living=e.getRenderer(EntityType.VILLAGER);
        if(living!=null){living.layers.removeIf(x->x instanceof VillagerProfessionLayer || x instanceof OutfitLayer);living.addLayer(new OutfitLayer<Villager,VillagerModel<Villager>>(living,false));living.addLayer(new GuardEquipmentLayer(living,e.getContext()));}
        ZombieVillagerRenderer zombie=e.getRenderer(EntityType.ZOMBIE_VILLAGER);
        if(zombie!=null){zombie.layers.removeIf(x->x instanceof VillagerProfessionLayer || x instanceof OutfitLayer);zombie.addLayer(new OutfitLayer<ZombieVillager,ZombieVillagerModel<ZombieVillager>>(zombie,true));}
        WanderingTraderRenderer merchant=e.getRenderer(EntityType.WANDERING_TRADER);
        if(merchant!=null){merchant.layers.removeIf(x->x instanceof MerchantLayer);merchant.addLayer(new MerchantLayer(merchant));}
    }
    private static final class OutfitLayer<T extends LivingEntity & VillagerDataHolder,M extends EntityModel<T> & VillagerHeadModel> extends RenderLayer<T,M> {
        private final VillagerProfessionLayer<T,M> babies;
        OutfitLayer(RenderLayerParent<T,M> parent,boolean zombie){super(parent);babies=new VillagerProfessionLayer<>(parent,net.minecraft.client.Minecraft.getInstance().getResourceManager(),zombie?"zombie_villager":"villager");}
        @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,T entity,float walk,float speed,float partial,float age,float yaw,float pitch){
            if(entity.isBaby()){babies.render(pose,buffers,light,entity,walk,speed,partial,age,yaw,pitch);return;}
            if(entity.isInvisible())return;
            var data=entity.getVillagerData();Style style=STYLES.get(data.getProfession());
            if(style==null)style=STYLES.get(VillagerProfession.NONE);
            int v=Math.max(0,entity.getData(VillageRoles.OUTFIT))&1;
            var climate=OutfitRules.climate(BuiltInRegistries.VILLAGER_TYPE.getKey(data.getType()).getPath());
            M model=getParentModel();model.hatVisible(false);
            boolean dead=entity instanceof ZombieVillager;
            int color=dead?0xff969b82:-1;
            renderColoredCutoutModel(model,BASE[climate.ordinal()][v],pose,buffers,light,entity,color);
            renderColoredCutoutModel(model,style.cloth()[v],pose,buffers,light,entity,color);
            int level=Math.max(1,Math.min(5,data.getLevel()));
            boolean worker=data.getProfession()!=VillagerProfession.NONE && data.getProfession()!=VillagerProfession.NITWIT;
            if(worker){if(level>1)renderColoredCutoutModel(model,LEVEL[level-1],pose,buffers,light,entity,color);renderColoredCutoutModel(model,BADGE[level-1],pose,buffers,light,entity,-1);}
            if(dead)renderColoredCutoutModel(model,DAMAGE,pose,buffers,light,entity,-1);
            model.hatVisible(true);
            ModelPart head=model instanceof VillagerModel<?> vm?vm.getHead():((ZombieVillagerModel<?>)model).head;
            ModelPart body=model instanceof VillagerModel<?> vm?vm.root().getChild("body"):((ZombieVillagerModel<?>)model).body;
            if(!data.getProfession().name().equals("slavicmyths:druzhinnik"))renderParts(style.parts()[climate.ordinal()][v],head,body,pose,buffers,light,color,worker?level:1,entity);
        }
    }
    private static final class MerchantLayer extends RenderLayer<WanderingTrader,VillagerModel<WanderingTrader>> {
        MerchantLayer(RenderLayerParent<WanderingTrader,VillagerModel<WanderingTrader>> p){super(p);}
        @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,WanderingTrader entity,float a,float b,float c,float d,float f,float g){
            if(entity.isInvisible())return;
            var model=getParentModel();model.hatVisible(false);renderColoredCutoutModel(model,MERCHANT,pose,buffers,light,entity,-1);model.hatVisible(true);
            renderParts(MERCHANT_PARTS,model.getHead(),model.root().getChild("body"),pose,buffers,light,-1,5,entity);
        }
    }
    private static void renderParts(ModelPart parts,ModelPart head,ModelPart body,PoseStack pose,MultiBufferSource buffers,int light,int color,int level,LivingEntity entity){
        var vertices=buffers.getBuffer(RenderType.entityCutoutNoCull(MATERIALS));int overlay=LivingEntityRenderer.getOverlayCoords(entity,0);
        if(entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty()){pose.pushPose();head.translateAndRotate(pose);parts.getChild("head").render(pose,vertices,light,overlay,color);pose.popPose();}
        pose.pushPose();body.translateAndRotate(pose);
        parts.getChild("body").render(pose,vertices,light,overlay,color);
        for(int i=2;i<=level;i++)parts.getChild("level_"+i).render(pose,vertices,light,overlay,color);
        pose.popPose();
    }
    private VillageVisuals(){}
}
