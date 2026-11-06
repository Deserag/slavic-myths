package org.slavicmyths.verifyclient.mixin;
import java.util.function.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.lwjgl.glfw.GLFW;
/** Test-only hardware GL window: never shown or focused on the owner's desktop. */
@Mixin(com.mojang.blaze3d.platform.Window.class)
public abstract class HiddenPosadWindow {
 @Redirect(method="<init>",at=@At(value="INVOKE",target="Lnet/neoforged/fml/loading/ImmediateWindowHandler;setupMinecraftWindow(Ljava/util/function/IntSupplier;Ljava/util/function/IntSupplier;Ljava/util/function/Supplier;Ljava/util/function/LongSupplier;)J"))
 private long hidden(IntSupplier width,IntSupplier height,Supplier<String> title,LongSupplier monitor){
  GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE,GLFW.GLFW_FALSE);GLFW.glfwWindowHint(GLFW.GLFW_FOCUSED,GLFW.GLFW_FALSE);GLFW.glfwWindowHint(GLFW.GLFW_FOCUS_ON_SHOW,GLFW.GLFW_FALSE);
  return GLFW.glfwCreateWindow(width.getAsInt(),height.getAsInt(),title.get()+" | isolated Posad QA",0,0);
 }
}
