package org.slavicmyths.client;

import net.minecraft.client.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
import org.slavicmyths.rpg.classes.*;

@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class ClassClient {
    public static final KeyMapping[] ACTIVE={key("path_ability",71),key("class_slot_2",72),key("class_slot_3",74)};
    public static final KeyMapping OPEN=key("class_menu",75);
    private static long receivedTick;
    private static final long[] previous=new long[3],flash=new long[3];
    private static final String[] previousIds={"","",""};
    private static KeyMapping key(String id,int code){return new KeyMapping("key.slavicmyths."+id,code,"key.categories.slavicmyths");}
    public static CompoundTag data(){return RpgClient.data.getCompound(ClassState.KEY);}
    public static void keys(RegisterKeyMappingsEvent e){for(var key:ACTIVE)e.register(key);e.register(OPEN);}
    public static void synced(){var mc=Minecraft.getInstance();receivedTick=mc.level==null?0:mc.level.getGameTime();if(mc.screen instanceof ClassScreen s)s.refresh();}
    public static long now(){var mc=Minecraft.getInstance();return data().getLong("ServerTick")+(mc.level==null?0:Math.max(0,mc.level.getGameTime()-receivedTick));}
    @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(mc.player==null)return;for(int i=0;i<3;i++)while(ACTIVE[i].consumeClick())if(mc.screen==null)ClassNetwork.send(4,"",i);while(OPEN.consumeClick())if(mc.screen==null)ClassNetwork.send(6,"",0);}
    @SubscribeEvent public static void open(ClassNetwork.OpenEvent e){Minecraft.getInstance().setScreen(new ClassScreen());}
    public static Component label(String id){return Component.translatable("classskill.slavicmyths."+id);}
    public static java.util.List<Component> conflicts(KeyMapping key){var out=new java.util.ArrayList<Component>();if(key.isUnbound())return out;for(var other:Minecraft.getInstance().options.keyMappings)if(other!=key&&key.same(other))out.add(Component.translatable(other.getName()));return out;}
    public static boolean conflict(KeyMapping key){return !conflicts(key).isEmpty();}
    public static net.minecraft.resources.ResourceLocation icon(String id){return ClassState.id("textures/gui/classes/"+id+".png");}
    @SubscribeEvent public static void hud(RenderGuiEvent.Post e){var mc=Minecraft.getInstance();var d=data();if(mc.player==null||mc.options.hideGui||!ClassHudConfig.SHOW.get()||d.getString("Base").isEmpty())return;var g=e.getGuiGraphics();int width=mc.getWindow().getGuiScaledWidth(),height=mc.getWindow().getGuiScaledHeight(),x=width-124,y=height-(width<430?116:62);long tick=now();
        for(int i=0;i<3;i++){int sx=x+i*38;String id=d.getString("Active"+i);var def=ClassDefinitions.SKILLS.get(id);long left=Math.max(0,d.getCompound("Cooldowns").getLong(id)-tick);if(!id.equals(previousIds[i])){previous[i]=left;flash[i]=0;previousIds[i]=id;}if(previous[i]>0&&left==0)flash[i]=tick+8;previous[i]=left;ClassTheme.image(g,"ui/node_"+(flash[i]>tick?"available":"learned"),sx,y,36,36,44,44);if(def!=null)ClassTheme.icon(g,id,sx+2,y+2,32);if(left>0&&def!=null){int rank=d.getCompound("Ranks").getInt(id);int cover=(int)Math.min(32,32.0*left/Math.max(1,ClassBalance.cooldown(d,id,rank)));g.fill(sx+2,y+34-cover,sx+34,y+34,0xB8000000);g.drawCenteredString(mc.font,Component.literal(Long.toString((left+19)/20)),sx+18,y+13,0xFFFFFF);}String key=ACTIVE[i].getTranslatedKeyMessage().getString();if(mc.font.width(key)>36)key=mc.font.plainSubstrByWidth(key,30)+"…";g.drawCenteredString(mc.font,Component.literal(key),sx+18,y+39,conflict(ACTIVE[i])?ClassTheme.ERROR:ClassTheme.TEXT);}
        String passive=d.getString("Passive");if(ClassDefinitions.SKILLS.containsKey(passive)){ClassTheme.image(g,"ui/node_learned",x+92,y-25,22,22,44,44);ClassTheme.icon(g,passive,x+95,y-22,16);}
    }
    private ClassClient(){}
}
