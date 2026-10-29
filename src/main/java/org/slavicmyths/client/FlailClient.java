package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.List;

/** Only explicitly registered flail models participate. No world polling, packets or server-side chain. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class FlailClient {
    static final List<String> IDS=List.of("stone_flail","gold_flail","flail","silver_flail","diamond_flail","zhara_flail","perunite_flail","netherite_flail","ataman_flail","volcanic_heart_flail");
    private record Binding(LivingEntity owner, ItemStack stack, ItemDisplayContext context) {}
    // applyTransform and BEWLR run synchronously on the render thread. Consumed before nested piece draws.
    private static final ThreadLocal<Binding> BINDING=new ThreadLocal<>();
    static ResourceLocation id(String name){return ResourceLocation.fromNamespaceAndPath("slavicmyths",name);}
    static ModelResourceLocation model(String name){return ModelResourceLocation.standalone(id("item/"+name));}
    @SubscribeEvent public static void models(ModelEvent.RegisterAdditional event){
        for(String name:IDS)for(String suffix:List.of("_held","_icon","_handle87","_ball87"))event.register(model(name+suffix));
        event.register(model("flail_link87"));
    }
    @SubscribeEvent public static void bake(ModelEvent.ModifyBakingResult event){
        BINDING.remove();
        for(String name:IDS){var key=ModelResourceLocation.inventory(id(name));var original=event.getModels().get(key);if(original!=null)event.getModels().put(key,new BoundModel(original,null,null));}
    }
    private static final class BoundModel extends BakedModelWrapper<BakedModel> {
        private final LivingEntity owner; private final ItemStack stack;
        BoundModel(BakedModel original,LivingEntity owner,ItemStack stack){super(original);this.owner=owner;this.stack=stack;}
        @Override public ItemOverrides getOverrides(){return new ItemOverrides(){
            @Override public BakedModel resolve(BakedModel base,ItemStack item,ClientLevel level,LivingEntity entity,int seed){
                BakedModel resolved=originalModel.getOverrides().resolve(originalModel,item,level,entity,seed);
                return new BoundModel(resolved==null?originalModel:resolved,entity,item);
            }
        };}
        @Override public java.util.List<BakedModel> getRenderPasses(ItemStack stack,boolean fabulous){return java.util.List.of(this);}
        @Override public BakedModel applyTransform(ItemDisplayContext context,PoseStack pose,boolean left){
            originalModel.applyTransform(context,pose,left);
            // A resource pack may replace builtin/entity with an ordinary baked model.
            // Such a draw never enters BEWLR, so it must not retain an entity in this thread.
            if(originalModel.isCustomRenderer())BINDING.set(new Binding(owner,stack,context));
            else BINDING.remove();
            return this;
        }
    }
    @SubscribeEvent public static void extensions(RegisterClientExtensionsEvent event){
        event.registerItem(new IClientItemExtensions(){
            private Renderer renderer;
            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer(){if(renderer==null)renderer=new Renderer();return renderer;}
            @Override public HumanoidModel.ArmPose getArmPose(LivingEntity owner,InteractionHand hand,ItemStack stack){
                // UseAnim.SPEAR otherwise flips the complete hanging chain over the shoulder.
                return owner.isUsingItem()&&owner.getUsedItemHand()==hand&&stack.getItem() instanceof org.slavicmyths.combat.FlailItem?HumanoidModel.ArmPose.ITEM:null;
            }
            @Override public boolean applyForgeHandTransform(PoseStack pose,net.minecraft.client.player.LocalPlayer player,net.minecraft.world.entity.HumanoidArm arm,ItemStack stack,float partial,float equip,float swing){
                // Keep the hanging head inside the view; also skips the unrelated vanilla spear windup.
                firstPersonWrist(pose,arm==net.minecraft.world.entity.HumanoidArm.LEFT,equip,swing);return true;
            }
        },IDS.stream().map(name->BuiltInRegistries.ITEM.get(id(name))).toArray(net.minecraft.world.item.Item[]::new));
    }
    public static void firstPersonWrist(PoseStack pose,boolean left,float equip,float swing){
        float motion=(float)Math.sin(Math.sqrt(Math.clamp(swing,0,1))*Math.PI);
        pose.translate((left?-1:1)*(.42-.08*motion),-.20-equip*.6+.05*motion,-1.15-.10*motion);
    }
    private static final class Renderer extends BlockEntityWithoutLevelRenderer {
        Renderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
        @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
            Binding binding=BINDING.get();BINDING.remove();
            var mc=Minecraft.getInstance();String name=BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
            if(!IDS.contains(name))return;
            boolean hand=context.firstPerson()||context==ItemDisplayContext.THIRD_PERSON_LEFT_HAND||context==ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
            LivingEntity owner=binding!=null&&binding.context==context&&binding.stack!=null&&ItemStack.isSameItemSameComponents(binding.stack,stack)?binding.owner:null;
            pose.pushPose();try{
                pose.translate(.5,.5,.5);
                if(!hand||owner==null){draw(stack,name+(context==ItemDisplayContext.GUI?"_icon":"_held"),pose,buffers,light,overlay);return;}
                float partial=mc.getTimer().getGameTimeDeltaPartialTick(false);
                boolean left=context==ItemDisplayContext.FIRST_PERSON_LEFT_HAND||context==ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
                InteractionHand held=(owner.getMainArm()==net.minecraft.world.entity.HumanoidArm.LEFT)==left?InteractionHand.MAIN_HAND:InteractionHand.OFF_HAND;
                if(!ItemStack.isSameItemSameComponents(owner.getItemInHand(held),stack)){draw(stack,name+"_held",pose,buffers,light,overlay);return;}
                float charge=owner.isUsingItem()&&owner.getUsedItemHand()==held&&stack.getItem() instanceof org.slavicmyths.combat.FlailItem?Math.clamp((owner.getTicksUsingItem()+partial)/18f,0,1):0;
                boolean attacking=owner.swinging&&owner.swingingArm==held;
                FlailMotion.Pose motion=FlailMotion.sample(owner.tickCount+partial,owner.walkAnimation.speed(partial),charge,owner.getAttackAnim(partial),attacking,name.equals("ataman_flail"));
                draw(stack,name+"_handle87",pose,buffers,light,overlay);
                for(int i=0;i<FlailMotion.LINKS;i++){
                    pose.pushPose();place(pose,motion.links()[i],motion.directions()[i]);
                    if((i&1)==0)pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
                    draw(stack,"flail_link87",pose,buffers,light,overlay);pose.popPose();
                }
                pose.pushPose();place(pose,motion.ball(),motion.directions()[FlailMotion.LINKS-1]);draw(stack,name+"_ball87",pose,buffers,light,overlay);pose.popPose();
            }finally{pose.popPose();BINDING.remove();}
        }
        private static void place(PoseStack pose,FlailMotion.Point point,FlailMotion.Point direction){
            pose.translate((point.x()-8)/16,(point.y()-8)/16,(point.z()-8)/16);
            pose.mulPose(new Quaternionf().rotationTo(new Vector3f(0,-1,0),new Vector3f((float)direction.x(),(float)direction.y(),(float)direction.z())));
        }
        private static void draw(ItemStack stack,String name,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
            var mc=Minecraft.getInstance();mc.getItemRenderer().render(stack,ItemDisplayContext.NONE,false,pose,buffers,light,overlay,mc.getModelManager().getModel(model(name)));
        }
    }
    private FlailClient(){}
}
