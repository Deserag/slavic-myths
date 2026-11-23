package org.slavicmyths.client;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/** Uses GUI coordinates: an icon is exactly the vanilla inventory item's 16 GUI pixels. */
@EventBusSubscriber(modid="slavicmyths", value=Dist.CLIENT)
public final class WeaponSkillHud {
    private record Skill(String icon, String key, long remaining) {}
    // Approved 2026-11-23 marine-skill art; transparent 32px PNGs rendered at 16 GUI pixels.
    private static final Map<String, ResourceLocation> MARINE_ICONS = Map.of(
        "abyss_staff_shot", marineTexture("abyss_staff_shot"),
        "abyss_staff_bubble", marineTexture("abyss_staff_bubble"),
        "abyss_staff_vortex", marineTexture("abyss_staff_vortex"),
        "storm_anchor_slam", marineTexture("storm_anchor_slam"),
        "pharaohka_trident_wave", marineTexture("pharaohka_trident_wave"),
        "storm_harpoon_tether", marineTexture("storm_harpoon_tether")
    );
    private static ResourceLocation marineTexture(String name) {
        return ResourceLocation.fromNamespaceAndPath("slavicmyths", "textures/gui/weapon_skills/" + name + ".png");
    }
    @SubscribeEvent public static void draw(RenderGuiEvent.Post event) {
        var mc=Minecraft.getInstance();
        if(mc.player==null||mc.level==null||mc.screen!=null||mc.options.hideGui
                ||mc.player.isSpectator()||mc.getCameraEntity()!=mc.player)return;
        var stack=mc.player.getMainHandItem();
        var id=BuiltInRegistries.ITEM.getKey(stack.getItem());
        if(!id.getNamespace().equals("slavicmyths"))return;
        var tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        long now=mc.level.getGameTime();
        List<Skill> skills=new ArrayList<>();
        switch(id.getPath()) {
            case "abyss_staff" -> {
                skills.add(new Skill("abyss_staff_shot","R",tag.getLong("MarineWeapon_staffShot")-now));
                skills.add(new Skill("abyss_staff_bubble","S+L",tag.getLong("MarineWeapon_bubble")-now));
                skills.add(new Skill("abyss_staff_vortex","S+R",tag.getLong("MarineWeapon_vortex")-now));
            }
            case "storm_anchor" -> skills.add(new Skill("storm_anchor_slam","S+R",tag.getLong("MarineWeapon_slam")-now));
            case "pharaohka_trident" -> skills.add(new Skill("pharaohka_trident_wave","S+R",tag.getLong("MarineWeapon_wave")-now));
            case "storm_harpoon" -> skills.add(new Skill("storm_harpoon_tether","S+R",tag.getLong("MarineHarpoonReady")-now));
            case "morozko_staff" -> {
                skills.add(new Skill("shot","L",FrostClient.skillRemaining("BoltReady")));
                skills.add(new Skill("breath","R",FrostClient.skillRemaining("StreamReady")));
                skills.add(new Skill("ice","S+L",FrostClient.skillRemaining("IcicleReady")));
                skills.add(new Skill("wave","S+R",FrostClient.skillRemaining("WaveReady")));
            }
            case "karachun_scythe" -> {
                skills.add(new Skill("breath","S+L",NightClient.skillRemaining("BreathReady")));
                skills.add(new Skill("harvest","S+R",NightClient.skillRemaining("HarvestReady")));
            }
            case "morovoy_staff" -> {
                skills.add(new Skill("shot","L",tag.getLong("MorStaff25_bolt")-now));
                skills.add(new Skill("mode","R",0));
                skills.add(new Skill("raven","S+L",tag.getLong("MorStaff25_flock")-now));
                skills.add(new Skill("fire","S+R",tag.getLong("MorStaff25_area")-now));
            }
            case "gneteya_great_hammer" -> skills.add(new Skill("slam","S+R",-1));
            case "neveya_scythe" -> {
                skills.add(new Skill("harvest","R",-1));skills.add(new Skill("slash","S+R",-1));
            }
            case "lomeya_hammer", "lomeya_hammer_offhand" -> {
                skills.add(new Skill("slam","R",-1));
                if(mc.player.getOffhandItem().is(org.slavicmyths.sisters.SistersContent.LOMEYA_HAMMER_OFFHAND.get()))skills.add(new Skill("wave","S+R",-1));
            }
            case "nine_mor_spear" -> {
                skills.add(new Skill("ice","R",-1));skills.add(new Skill("vortex","S+R",-1));
            }
            case "nine_mor_axe" -> {
                skills.add(new Skill("wave","R",-1));skills.add(new Skill("harvest","S+R",-1));
            }
            case "navya_ostroga", "ashen_greatsword", "shadow_throwing_knife", "volcanic_heart_flail", "infernal_bow", "empowered_infernal_bow" ->
                skills.add(new Skill(id.getPath().contains("bow")?"fire":id.getPath().contains("flail")?"slam":"slash","S+R",tag.getLong("slavicmyths_skill_"+id.getPath())-now));
            case "storm_staff", "thunder_axe", "thunder_spear", "veles_staff", "likho_staff", "trophy_kalin_sabre", "nightingale_dagger", "pool_spear", "zhelva_shield", "nine_mor_shield", "mytar_shackled_sickle" ->
                skills.add(new Skill(id.getPath().contains("shield")?"bubble":id.getPath().contains("staff")?"shot":"slash","",-1));
            case "poloz_whip" -> {skills.add(new Skill("ice","R",-1));skills.add(new Skill("wave","S+R",-1));}
            case "three_breath_crossbow", "serpent_crossbow" -> {
                int mode=Math.floorMod(tag.getCompound("Gorynych196").getInt("Mode"),3);
                skills.add(new Skill("shot","R",-1));
                if(id.getPath().equals("three_breath_crossbow"))skills.add(new Skill(mode==0?"raven":mode==1?"fire":"wave","S+R",0));
            }
            default -> {
                if(!WeaponTooltips.isWeapon(id.getPath()))return;
                boolean chargedWeapon=stack.getItem() instanceof org.slavicmyths.combat.FlailItem
                    ||stack.getItem() instanceof org.slavicmyths.combat.ThrowingWeaponItem;
                if(!chargedWeapon&&!mc.player.getCooldowns().isOnCooldown(stack.getItem()))return;
                skills.add(new Skill("shot","",-1));
            }
        }
        var g=event.getGuiGraphics();
        // Keep the panel against the left edge, above the hotbar/offhand even on narrow GUI sizes.
        int x=6,y=g.guiHeight()-44;
        for(var skill:skills){
            g.fill(x-1,y-1,x+17,y+17,0xC0181B25);
            icon(g,skill.icon,x,y);
            float shared=skill.remaining<0?mc.player.getCooldowns().getCooldownPercent(stack.getItem(),0):0;
            if(shared>0)g.fill(x,y+16-(int)Math.ceil(16*shared),x+16,y+16,0xB0000000);
            if(skill.remaining>0){
                g.fill(x,y,x+16,y+16,0xA0000000);
                String seconds=Long.toString((skill.remaining+19)/20);
                g.drawCenteredString(mc.font,seconds,x+8,y+4,0xFFFFFF);
            }
            if(id.getPath().equals("storm_harpoon"))for(int i=0;i<4;i++)
                g.fill(x+i*4,y+16,x+i*4+3,y+18,i<tag.getInt("MarineHarpoonHits")?0xFF89E1F2:0xFF454955);
            x+=22;
        }
    }
    private static void icon(GuiGraphics g,String kind,int x,int y){
        ResourceLocation marine = MARINE_ICONS.get(kind);
        if (marine != null) {
            // UV covers the full image; GUI size remains 16x16 and follows Minecraft GUI scale.
            g.blit(marine, x, y, 0.0F, 0.0F, 16, 16, 16, 16);
            return;
        }
        String[] rows=switch(kind){
            case "shot" -> new String[]{"   ##   ","  ####  ","   ##   ","   ##   ","   ##   "," # ## # ","  ####  ","   ##   "};
            case "bubble" -> new String[]{"  ####  "," #    # ","#  #   #","#      #","#      #","#      #"," #    # ","  ####  "};
            case "vortex" -> new String[]{"  ##### "," ##   ##","## ##  #","# #  # #","# # ## #","#  ### #","##    ##"," #####  "};
            case "ice" -> new String[]{"  #  #  "," ### ## "," ### ## "," ### ## ","  #  #  ","  #  #  ","  #     ","        "};
            case "wave" -> new String[]{"    ### ","   #   #","  ##    ","  #     "," ##     ","##   #  ","########"," ###### "};
            case "chain" -> new String[]{" ##     ","#  #    ","# ##    "," ###    ","   ###  ","   ## # ","    #  #","     ## "};
            case "raven" -> new String[]{"#      #","##    ##","###  ###"," ###### ","  ####  ","   ##   ","  ####  "," #    # "};
            case "fire" -> new String[]{"    #   ","   ##   "," # ###  "," #### # ","####### ","### ### "," ## ##  ","  ###   "};
            case "mode" -> new String[]{"   ##   "," ###### ","#  ##  #","      # "," #      ","#  ##  #"," ###### ","   ##   "};
            case "slash" -> new String[]{"      ##","     ## ","    ##  ","   ##   ","  ##    "," ###    ","## ##   ","#   #   "};
            case "harvest" -> new String[]{" #####  ","#     # ","     ## ","    ##  ","   ##   ","  ##    "," ##     ","##      "};
            case "breath" -> new String[]{"##      "," ### ## ","   ##  #","###     ","   #### ","##     #","  ##### ","        "};
            default -> new String[]{"   ##   ","   ##   ","#  ##  #"," # ## # ","  ####  ","   ##   ","########"," # ## # "};
        };
        int color=switch(kind){case "fire","slam"->0xFFFFAE55;case "raven","harvest","breath"->0xFFC298ED;default->0xFF89E1F2;};
        for(int row=0;row<8;row++)for(int col=0;col<8;col++)if(rows[row].charAt(col)=='#')
            g.fill(x+col*2,y+row*2,x+col*2+2,y+row*2+2,color);
    }
    private WeaponSkillHud(){}
}
