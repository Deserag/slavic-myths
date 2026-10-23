package org.slavicmyths.verify;

import java.util.List;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.slavicmyths.rpg.Runes;

/** Opt-in actual PlayerRenderer capture. Does not control the user's desktop.
 * Only the pre-existing disposable VisualQA world; never a personal save.
 */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class Issue105WaveProbe {
    static boolean opened; static int tick, scene=-1; static WaveScreen screen;
    static final EquipmentSlot[] SLOTS={EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
    static ItemStack equipment(int actor,int slot) {
        String part=List.of("helmet","chestplate","leggings","boots").get(slot);
        ItemStack stack=actor%2==0 ? new ItemStack(List.of(Items.IRON_HELMET,Items.IRON_CHESTPLATE,Items.IRON_LEGGINGS,Items.IRON_BOOTS).get(slot))
                : Issue217Probe.item("perunite_"+part);
        Runes.write(stack,3,slot==1?List.of("protection","life","wind"):slot==2?List.of("protection","life"):List.of("protection"));
        return stack;
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event)throws Exception {
        if(!Boolean.getBoolean("slavicmyths.issue105.wave"))return;
        var mc=Minecraft.getInstance();
        if(!opened&&mc.screen instanceof TitleScreen){opened=true;mc.options.pauseOnLostFocus=false;mc.options.guiScale().set(2);mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).set(.2D);mc.createWorldOpenFlows().openWorld("VisualQA",()->{});}
        if(mc.player==null||mc.level==null)return;
        if(screen==null){scene=0;screen=new WaveScreen(scene);mc.setScreen(screen);}
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event)throws Exception {
        if(!Boolean.getBoolean("slavicmyths.issue105.wave")||screen==null)return;
        var mc=Minecraft.getInstance(); int age=screen.age;
        if(List.of(5,15,21,25,30,35,40,50,60).contains(age)){
            Issue217Probe.capture(mc,"wave105-view"+scene+"-tick"+age);
            System.out.println("ISSUE105_RENDERED_PHASE age="+age+" phase="+org.slavicmyths.client.runeglow.ArmorRuneWave.phase(screen.actors[0],1));
        }
        tick++; int next=tick/65;
        if(next>=3){System.out.println("ISSUE105_NATIVE_CAPTURE_DONE: 27 exact-frame captures, source-controlled actor clock; no sword pilot");screen=null;mc.stop();return;}
        if(next!=scene){scene=next;screen=new WaveScreen(scene);mc.setScreen(screen);}
        screen.age=tick%65;
    }
    static final class WaveScreen extends Screen {
        final net.minecraft.client.player.RemotePlayer[] actors=new Issue217Probe.Skins().actors;
        final int view; int age;
        WaveScreen(int view){super(Component.literal("Native armor rune wave"));this.view=view;for(int i=0;i<4;i++)for(int s=0;s<4;s++)actors[i].setItemSlot(SLOTS[s],equipment(i,s));}
        @Override public boolean isPauseScreen(){return false;}
        @Override public void render(GuiGraphics g,int x,int y,float partial){
            g.fill(0,0,width,height,0xff202830);
            g.drawString(font,"ACTUAL PlayerRenderer / wave105 / view="+view+" age="+age+" / mixed rune counts 1,3,2,1",6,6,0xffffff);
            for(int i=0;i<4;i++){
                var actor=actors[i];actor.tickCount=1000+age;
                actor.setItemSlot(EquipmentSlot.FEET,age>=10&&age<20?ItemStack.EMPTY:equipment(i,3));
                float yaw=view==0?0:view==1?180:90;
                int xx=30+i*150;
                g.drawString(font,(i>=2?"Alex":"Steve")+(i%2==0?" iron":" perunite"),xx,32,0xffffff);
                InventoryScreen.renderEntityInInventory(g,xx+60,250,85,new org.joml.Vector3f(0,actor.getBbHeight()/2+.0625F,0),
                    new org.joml.Quaternionf().rotateZ((float)Math.PI).rotateY((float)Math.toRadians(yaw)),new org.joml.Quaternionf(),actor);
            }
        }
    }
}
