package org.slavicmyths.textile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import org.slavicmyths.husbandry.YardAnimal;

@EventBusSubscriber(modid="slavicmyths")
public final class TextileEvents {
    @SubscribeEvent public static void drops(LivingDropsEvent e){
        if(!(e.getEntity().level() instanceof ServerLevel level))return;
        if(e.getEntity() instanceof Player p){BeltData.death(p,e.getDrops());return;}
        if(!(e.getEntity() instanceof Animal a)||a.isBaby()||!level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT))return;
        // Environment damage may retain vanilla player kill credit. An unrelated mob kill is excluded.
        if(!(e.getSource().getEntity() instanceof Player)&&(!e.isRecentlyHit()||!(a.getKillCredit() instanceof Player)||e.getSource().getEntity() instanceof net.minecraft.world.entity.LivingEntity))return;
        Carcass.Kind kind=a instanceof YardAnimal y?switch(y.kind){case 0->Carcass.Kind.GOOSE;case 1->Carcass.Kind.DUCK;default->Carcass.Kind.DOMESTIC_GOAT;}:a instanceof Cow?Carcass.Kind.COW:a instanceof Pig?Carcass.Kind.PIG:a instanceof Sheep?Carcass.Kind.SHEEP:a instanceof Rabbit?Carcass.Kind.RABBIT:null;
        if(kind==null)return;
        Carcass carcass=Textiles.CARCASS.get().create(level);if(carcass==null)return;carcass.setup(kind,a.isOnFire());carcass.moveTo(a.getX(),a.getY(),a.getZ(),a.getYRot(),0);
        if(level.addFreshEntity(carcass))e.getDrops().removeIf(drop->standardLoot(kind,drop.getItem())); // XP remains on its separate vanilla path.
    }
    private static boolean standardLoot(Carcass.Kind kind,net.minecraft.world.item.ItemStack s){
        return switch(kind){
            case COW->s.is(Items.BEEF)||s.is(Items.COOKED_BEEF)||s.is(Items.LEATHER);
            case PIG->s.is(Items.PORKCHOP)||s.is(Items.COOKED_PORKCHOP);
            case SHEEP->s.is(Items.MUTTON)||s.is(Items.COOKED_MUTTON)||s.is(net.minecraft.tags.ItemTags.WOOL);
            case RABBIT->s.is(Items.RABBIT)||s.is(Items.COOKED_RABBIT)||s.is(Items.RABBIT_HIDE)||s.is(Items.RABBIT_FOOT);
            case GOOSE->s.is(org.slavicmyths.husbandry.Husbandry.RAW_GOOSE.get())||s.is(org.slavicmyths.husbandry.Husbandry.COOKED_GOOSE.get())||s.is(Items.FEATHER);
            case DUCK->s.is(org.slavicmyths.husbandry.Husbandry.RAW_DUCK.get())||s.is(org.slavicmyths.husbandry.Husbandry.COOKED_DUCK.get())||s.is(Items.FEATHER);
            case DOMESTIC_GOAT->s.is(org.slavicmyths.husbandry.Husbandry.RAW_GOAT.get())||s.is(org.slavicmyths.husbandry.Husbandry.COOKED_GOAT.get());
        };
    }
    @SubscribeEvent public static void sleep(PlayerWakeUpEvent e){
        Player p=e.getEntity();
        // ServerLevel completed deep sleep calls stopSleepInBed(false,false).
        // Manual/interrupted wakes use different flags; no saved player sleep flags or player scans.
        if(p.level() instanceof ServerLevel && !e.wakeImmediately() && !e.updateLevel() && p.isSleepingLongEnough()
            && p.getSleepingPos().filter(pos->p.level().getBlockState(pos).is(Textiles.BLOCKS.get("linen_bed").get())).isPresent())p.addEffect(new MobEffectInstance(Textiles.RESTED,12000,0));
    }
    private TextileEvents(){}
}
