package org.slavicmyths.husbandry;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid="slavicmyths")
public final class YardFeeding {
    public static String kind(Animal a){
        if(a instanceof YardAnimal y)return new String[]{"goose","duck","domestic_goat"}[y.kind];
        if(a instanceof Cow)return "cow";if(a instanceof Sheep)return "sheep";if(a instanceof Pig)return "pig";if(a instanceof Chicken)return "chicken";if(a instanceof Rabbit)return "rabbit";return null;
    }
    public static boolean accepts(Animal a,ItemStack s){String k=kind(a);return k!=null&&(s.is(Husbandry.feed(k))||a.isFood(s));}
    @SubscribeEvent public static void join(EntityJoinLevelEvent e){
        if(e.getLevel().isClientSide||!(e.getEntity() instanceof Animal a)||kind(a)==null)return;
        if(a.goalSelector.getAvailableGoals().stream().noneMatch(g->g.getGoal() instanceof FeederGoal))a.goalSelector.addGoal(4,new FeederGoal(a));
        if(!(a instanceof YardAnimal)&&a.goalSelector.getAvailableGoals().stream().noneMatch(g->g.getGoal() instanceof AddedTempt))a.goalSelector.addGoal(3,new AddedTempt(a,kind(a)));
    }
    private static final class AddedTempt extends TemptGoal {AddedTempt(Animal a,String k){super(a,1.1,Ingredient.of(Husbandry.feed(k)),false);}}
    @SubscribeEvent public static void interact(PlayerInteractEvent.EntityInteract e){
        if(!(e.getTarget() instanceof Animal a)||!accepts(a,e.getItemStack()))return;
        Player p=e.getEntity();if(p.isSpectator())return;
        boolean baby=a.isBaby(),love=!baby&&a.getAge()==0&&a.canFallInLove(),hurt=a.getHealth()<a.getMaxHealth();
        if(!baby&&!love&&!hurt)return;
        if(!a.level().isClientSide){
            ItemStack food=e.getItemStack().copyWithCount(1);
            if(baby)a.ageUp(net.minecraft.world.entity.AgeableMob.getSpeedUpSecondsWhenFeeding(-a.getAge()),true);
            else if(love)a.setInLove(p);
            a.heal(2);e.getItemStack().consume(1,p);eaten(a,food);
        }
        e.setCanceled(true);e.setCancellationResult(InteractionResult.sidedSuccess(a.level().isClientSide));
    }
    public static void eaten(Animal a,ItemStack food){
        a.playSound(SoundEvents.GENERIC_EAT,.6F,1F);
        if(a instanceof YardAnimal y)y.eatAnimation();
        if(a.level() instanceof ServerLevel l)l.sendParticles(new ItemParticleOption(ParticleTypes.ITEM,food),a.getX(),a.getY()+a.getBbHeight()*.6,a.getZ(),5,.15,.10,.15,.02);
    }
    private YardFeeding(){}
}
