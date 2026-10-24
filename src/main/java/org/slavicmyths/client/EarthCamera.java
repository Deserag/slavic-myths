package org.slavicmyths.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.slavicmyths.earth.*;

@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class EarthCamera {
    private static final EntityTypeTest<Entity, EarthActor> ACTOR_TYPE = EntityTypeTest.forClass(EarthActor.class);
    private static final EntityTypeTest<Entity, EarthSpirit> BOSS_TYPE = EntityTypeTest.forClass(EarthSpirit.class);
    // Render-thread scratch only; clear even on failure to avoid retaining a disconnected world.
    private static final List<EarthActor> actors = new ArrayList<>();
    private static final List<EarthSpirit> bosses = new ArrayList<>();

    @SubscribeEvent public static void camera(ViewportEvent.ComputeCameraAngles e) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || !EarthVisualConfig.SHAKE.get()) return;
        double amount = 0, time = mc.level.getGameTime() + e.getPartialTick();
        var bounds = mc.player.getBoundingBox().inflate(16);
        try {
            mc.level.getEntities(ACTOR_TYPE, bounds, EntitySelector.NO_SPECTATORS, actors);
            for (int i = 0; i < actors.size(); i++) {
                var actor = actors.get(i);
                if (actor.kind() == EarthActor.TREMOR && actor.age(0) < 9)
                    amount = Math.max(amount, .4 * (1 - actor.age(0) / 9.0));
                else if (actor.kind() == EarthActor.DEBRIS && actor.age(0) < 6)
                    amount = Math.max(amount, .24 * (1 - actor.age(0) / 6.0));
            }
            mc.level.getEntities(BOSS_TYPE, bounds, EntitySelector.NO_SPECTATORS, bosses);
            for (int i = 0; i < bosses.size(); i++) {
                var boss = bosses.get(i);
                float age = boss.age(0);
                float start = boss.action() == EarthRules.Action.AWAKEN ? 80 : 22;
                if ((boss.action() == EarthRules.Action.PHASE || boss.action() == EarthRules.Action.AWAKEN)
                        && age >= start && age < start + 8)
                    amount = Math.max(amount, .1 * (1 - (age - start) / 8));
            }
        } finally {
            actors.clear();
            bosses.clear();
        }
        var balance = mc.player.getEffect(EarthContent.IMBALANCE);
        if (balance != null)
            e.setRoll(e.getRoll() + (float) (Math.sin((13 - balance.getDuration() + e.getPartialTick()) * Math.PI / 13) * 2));
        if (amount > 0) {
            e.setPitch(e.getPitch() + (float) (Math.sin(time * 2.7) * amount));
            e.setRoll(e.getRoll() + (float) (Math.sin(time * 3.1) * amount * .4));
        }
    }
    private EarthCamera() {}
}
