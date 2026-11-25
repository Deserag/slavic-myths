package org.slavicmyths.combat;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slavicmyths.rpg.PathData;

/** Server-owned, per-player and per-action deadlines; the item tag is a HUD mirror only. */
@EventBusSubscriber(modid="slavicmyths")
public final class IndependentWeaponCooldowns {
    public static final String NEVEYA="neveya_scythe", LOMEYA="lomeya_hammer",
        SPEAR="nine_mor_spear", AXE="nine_mor_axe", MYTAR="mytar_shackled_sickle", POLOZ="poloz_whip", HARPOON="storm_harpoon", SHADOW_KNIFE="shadow_throwing_knife";
    public static final String HEALING_STAFF="healing_staff";
    public static String key(String weapon,boolean alternate){
        return "SlavicSkillReady_"+weapon+(alternate?"_shift":"_normal");
    }
    public static boolean ready(ServerPlayer player,String weapon,boolean alternate){
        return PathData.data(player).getLong(key(weapon,alternate))<=player.level().getGameTime();
    }
    public static void start(ServerPlayer player,ItemStack stack,String weapon,boolean alternate,int ticks){
        PathData.data(player).putLong(key(weapon,alternate),player.level().getGameTime()+ticks);
        sync(player,stack,weapon);
    }
    public static long remaining(CompoundTag tag,String weapon,boolean alternate,long now){
        return Math.max(0L,tag.getLong(key(weapon,alternate))-now);
    }
    private static void sync(ServerPlayer player,ItemStack stack,String weapon){
        CompoundTag data=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        CompoundTag saved=PathData.data(player);
        boolean changed=false;
        for(boolean alternate:new boolean[]{false,true}){
            String key=key(weapon,alternate);
            long deadline=saved.getLong(key);
            if(data.getLong(key)!=deadline){data.putLong(key,deadline);changed=true;}
        }
        // Tether cooldown starts on successful impact, not on the throw.
        if(HARPOON.equals(weapon)){
            String key="MarineHarpoonReady";
            long deadline=player.getPersistentData().getLong(key);
            if(data.getLong(key)!=deadline){data.putLong(key,deadline);changed=true;}
            String hitsKey="MarineHarpoonHits";
            int hits=player.getPersistentData().getInt(hitsKey);
            if(data.getInt(hitsKey)!=hits){data.putInt(hitsKey,hits);changed=true;}
        }
        if(changed)stack.set(DataComponents.CUSTOM_DATA,CustomData.of(data));
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player))return;
        syncHeld(player);
    }
    public static void syncHeld(ServerPlayer player){
        ItemStack stack=player.getMainHandItem();
        var id=BuiltInRegistries.ITEM.getKey(stack.getItem());
        if(!id.getNamespace().equals("slavicmyths"))return;
        String weapon=switch(id.getPath()){
            case NEVEYA,LOMEYA,SPEAR,AXE,MYTAR,POLOZ,HARPOON,SHADOW_KNIFE -> id.getPath();
            case "lomeya_hammer_offhand" -> LOMEYA;
            case "ice_healing_staff", "fire_healing_staff" -> HEALING_STAFF;
            default -> null;
        };
        if(weapon!=null)sync(player,stack,weapon);
    }
    private IndependentWeaponCooldowns(){}
}
