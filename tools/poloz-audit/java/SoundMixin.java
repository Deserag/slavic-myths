package org.slavicmyths.polozaudit.mixin;
import org.slavicmyths.polozaudit.PressureObserver;
import net.minecraft.client.sounds.*;import net.minecraft.client.resources.sounds.SoundInstance;import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(value=SoundEngine.class,remap=false)
public abstract class SoundMixin {
 @Shadow private java.util.Map<SoundInstance,ChannelAccess.ChannelHandle> instanceToChannel;
 @Unique private java.util.Set<SoundInstance> auditPrevious;
 @Inject(method="tickNonPaused",at=@At("HEAD"))private void tickBegin(CallbackInfo ci){auditPrevious=java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());auditPrevious.addAll(instanceToChannel.keySet());}
 @Inject(method="tickNonPaused",at=@At("RETURN"))private void tickEnd(CallbackInfo ci){for(var s:auditPrevious)if(!instanceToChannel.containsKey(s))PressureObserver.soundEnd(s);auditPrevious=null;}
 @Inject(method="play",at=@At("HEAD"))private void play(SoundInstance s,CallbackInfo ci){PressureObserver.sound(s);}
 @Inject(method="play",at=@At("RETURN"))private void played(SoundInstance s,CallbackInfo ci){PressureObserver.currentSound.remove();}
 @Inject(method="stop(Lnet/minecraft/client/resources/sounds/SoundInstance;)V",at=@At("HEAD"))private void stop(SoundInstance s,CallbackInfo ci){PressureObserver.soundStop(s);}
}
