package org.slavicmyths.cubecross.mixin;
import com.mojang.blaze3d.vertex.*;import org.slavicmyths.cubeaudit.*;import org.slavicmyths.client.GuardianClient;import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(value=GuardianClient.class,remap=false)
public abstract class CubeSwitch {
 @Inject(method="cube",at=@At("HEAD"),cancellable=true)private static void route(PoseStack p,VertexConsumer out,double x,double y,double z,double w,double h,double d,int color,CallbackInfo ci){if(!CrossControl.enabled||CrossControl.mode==2)return;if(CrossControl.mode==0)ReferenceCube.cube(p,out,x,y,z,w,h,d,color);else HelperCube.cube(p,out,x,y,z,w,h,d,color);ci.cancel();}
}
