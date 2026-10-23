package org.slavicmyths.verify;

import java.util.*;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.InteractionHand;

/** Actual server attack/hurt paths, not a constructor DPS calculation.
 * Each target is isolated and discarded; this is primary-hit coverage only.
 */
@GameTestHolder("slavicmyths_weapons") @PrefixGameTestTemplate(false)
public final class Weapon191PrimaryTests {
    @GameTest(template="empty",timeoutTicks=1200)
    public static void allMeleeFamiliesAgainstThreeArmorLevels(GameTestHelper t) {
        var rows=new com.google.gson.JsonArray(); int weapons=0;
        for(String line:resourceCatalog().split("\n")) {
            String[] fields=line.strip().split("\t");
            if(fields.length!=3||!fields[1].equals("weapons")||!Set.of("knives","swords","maces","axes","flails","spears","polearms").contains(fields[2]))continue;
            String id=fields[0]; var p=Issue217GameTests.player(t,"W191_"+weapons);
            if(id.equals("karachun_scythe"))continue; // Delayed server swing has its own actual-time tests below.
            var stack=Issue225GameTests.item(id);p.setItemInHand(InteractionHand.MAIN_HAND,stack);
            p.setOnGround(true);p.fallDistance=0;p.setSprinting(false);
            for(int armor:new int[]{0,8,20}) {
                // Offline player connection is intentionally unregistered; manually
                // advance its native cooldown rather than forging a charged flag.
                for(int tick=0;tick<60;tick++)p.doTick();
                var target=EntityType.SLIME.create(t.getLevel());target.setSize(4,true);target.setNoAi(true);target.setNoGravity(true);
                target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);target.setHealth(500);
                target.getAttribute(Attributes.ARMOR).setBaseValue(armor);target.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(0);
                target.moveTo(p.getX(),p.getY(),p.getZ()+2);t.getLevel().addFreshEntity(target);
                float charge=p.getAttackStrengthScale(.5F);t.assertTrue(charge>.99F,"Not fully charged: "+id);
                float before=target.getHealth();p.attack(target);float damage=before-target.getHealth();
                t.assertTrue(damage>0&&Float.isFinite(damage),"Actual primary failed: "+id+" armor="+armor);
                var row=new com.google.gson.JsonObject();row.addProperty("id",id);row.addProperty("armor",armor);row.addProperty("toughness",0);row.addProperty("charge",charge);row.addProperty("actual_immediate_damage",damage);row.addProperty("measurement","native ServerPlayer.attack on actual Slime; no crit/runes/enchant; not sustained DPS, projectile or ability coverage");rows.add(row);target.discard();
            }
            weapons++;
        }
        t.assertTrue(weapons==42,"Unexpected immediate melee family count "+weapons);
        try {
            var out=java.nio.file.Path.of(System.getProperty("slavicmyths.primary191.output","primary191.json"));java.nio.file.Files.createDirectories(out.toAbsolutePath().getParent());
            java.nio.file.Files.writeString(out,new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(rows));
        } catch(java.io.IOException ex){throw new RuntimeException(ex);}
        System.out.println("WEAPON191_PRIMARY_NATIVE_PASS weapons="+weapons+" armor_cases="+rows.size()+"; no sustained DPS claim");t.succeed();
    }
    @GameTest(template="empty") public static void scytheUnarmored(GameTestHelper t){scythe(t,0);}
    @GameTest(template="empty") public static void scytheArmor8(GameTestHelper t){scythe(t,8);}
    @GameTest(template="empty") public static void scytheArmor20(GameTestHelper t){scythe(t,20);}
    private static void scythe(GameTestHelper t,int armor){
        var p=Issue217GameTests.player(t,"Delayed191");p.setItemInHand(InteractionHand.MAIN_HAND,Issue225GameTests.item("karachun_scythe"));for(int i=0;i<60;i++)p.doTick();
        var target=EntityType.SLIME.create(t.getLevel());target.setSize(4,true);target.setNoAi(true);target.setNoGravity(true);target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);target.setHealth(500);target.getAttribute(Attributes.ARMOR).setBaseValue(armor);target.moveTo(p.getX(),p.getY(),p.getZ()+2);t.getLevel().addFreshEntity(target);
        p.attack(target);var state=p.getPersistentData().getCompound(org.slavicmyths.karachun.NightCombat.KEY);
        t.assertTrue(!state.isEmpty()&&target.getHealth()==500,"Scythe must start a delayed native attack, not deal immediate damage");int windup=state.getInt("Windup");
        t.runAfterDelay(windup,()->{
            // This unregistered fixture player has no world tick wrapper; invoke
            // the production Post-tick handler at the real scheduled world time.
            org.slavicmyths.karachun.NightCombat.tick(p);float after=target.getHealth();t.assertTrue(after<500,"Native delayed scythe missed armor="+armor);
            org.slavicmyths.karachun.NightCombat.tick(p);t.assertTrue(target.getHealth()==after,"Scythe applied twice on same contact tick");
            System.out.println("WEAPON191_SCYTHE_DELAYED_PASS armor="+armor+" actual_damage="+(500-after)+" windup="+windup);org.slavicmyths.karachun.NightCombat.cancel(p);target.discard();t.succeed();
        });
    }
    private static String resourceCatalog(){
        try(var in=Weapon191PrimaryTests.class.getResourceAsStream("/assets/slavicmyths/creative_catalog.tsv")) {
            if(in==null)throw new IllegalStateException("catalog missing");return new String(in.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);
        } catch(java.io.IOException ex){throw new RuntimeException(ex);}
    }
}
