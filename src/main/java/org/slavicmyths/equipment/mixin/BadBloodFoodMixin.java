package org.slavicmyths.equipment.mixin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(Player.class)
public abstract class BadBloodFoodMixin {
 @Redirect(method="eat",at=@At(value="INVOKE",target="Lnet/minecraft/world/food/FoodData;eat(Lnet/minecraft/world/food/FoodProperties;)V"))
 private void slavicmyths$food(FoodData food,FoodProperties properties){org.slavicmyths.volkolak.BadBlood.food((Player)(Object)this,food,properties);}
}
