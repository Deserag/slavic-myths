package org.slavicmyths.savanna;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.registry.ModEntities;
import org.slavicmyths.registry.ModItems;
@GameTestHolder("slavicmyths_accessories278")
@PrefixGameTestTemplate(false)
public final class DivGameTests {
    @GameTest(template="empty")
    public static void divRealHealthAndEgg(GameTestHelper h){
        var w=h.getLevel();var d=ModEntities.DIV.get().create(w);var restored=ModEntities.DIV.get().create(w);
        if(d==null||restored==null)throw new IllegalStateException("Div registry missing");
        d.setHealth(1600);h.assertTrue(d.getHealth()==1600&&d.getMaxHealth()==1600&&d.getAttributeValue(Attributes.MAX_HEALTH)==1600,"Div HP capped");
        h.assertTrue(d.getAttributeValue(Attributes.ARMOR)==16&&d.getAttributeValue(Attributes.ARMOR_TOUGHNESS)==6,"Div armor mismatch");
        h.assertTrue(ModItems.DIV_SPAWN_EGG.get() instanceof SpawnEggItem,"Missing Div spawn egg");
        d.setHealth(1234);CompoundTag n=new CompoundTag();d.saveWithoutId(n);restored.load(n);h.assertTrue(restored.getHealth()==1234&&restored.getMaxHealth()==1600,"Div HP save/load mismatch");
        h.assertTrue(Div.phaseFor(1121)==1&&Div.phaseFor(1120)==2&&Div.phaseFor(561)==2&&Div.phaseFor(560)==3,"Div phase thresholds mismatch");d.discard();restored.discard();h.succeed();
    }
    @GameTest(template="empty")
    public static void divCurseStacksAndCleanup(GameTestHelper h){
        var w=h.getLevel();var d=ModEntities.DIV.get().create(w);var x=EntityType.ZOMBIE.create(w);if(d==null||x==null)throw new IllegalStateException("Entity creation failed");
        double speed=x.getAttributeValue(Attributes.MOVEMENT_SPEED);for(int i=1;i<=4;i++)h.assertTrue(DivCurse.add(x,d.getUUID())==i,"Wrong curse stack");
        h.assertTrue(DivCurse.add(x,d.getUUID())==4&&DivCurse.active(x).length==4,"Curse cap failed");
        h.assertTrue(Math.abs(x.getAttributeValue(Attributes.MOVEMENT_SPEED)-speed*.9)<.001,"Curse slow incorrect");
        x.getPersistentData().putLongArray("DivCurseMarks",new long[]{w.getGameTime()-1,w.getGameTime()+20,w.getGameTime()+40,w.getGameTime()+60});
        h.assertTrue(DivCurse.active(x).length==3,"Independent curse expiry failed");DivCurse.clear(x);
        h.assertTrue(DivCurse.active(x).length==0&&Math.abs(x.getAttributeValue(Attributes.MOVEMENT_SPEED)-speed)<.001,"Curse cleanup failed");d.discard();x.discard();h.succeed();
    }
}
