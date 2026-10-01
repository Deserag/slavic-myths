package org.slavicmyths.client;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.IMob;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slavicmyths.bandit.BanditEntity;
import org.slavicmyths.combat.CombatHudConfig;
@Mod.EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class CombatHud extends AbstractGui {
    private static int targetId=-1;private static long expires;private static Object world;
    public static void target(int id){Minecraft mc=Minecraft.getInstance();world=mc.level;if(mc.level!=null){targetId=id;expires=mc.level.getGameTime()+100;}}
    @SubscribeEvent public static void render(RenderGameOverlayEvent.Post e){
        Minecraft mc=Minecraft.getInstance();if(e.getType()!=RenderGameOverlayEvent.ElementType.ALL||!CombatHudConfig.ENABLED.get()||mc.player==null||mc.level==null||mc.options.hideGui||mc.options.renderDebug)return;
        if(world!=mc.level){targetId=-1;world=mc.level;}
        if(mc.hitResult instanceof EntityRayTraceResult){Entity pointed=((EntityRayTraceResult)mc.hitResult).getEntity();if(pointed instanceof IMob&&pointed instanceof LivingEntity)target(pointed.getId());}
        Entity entity=mc.level.getEntity(targetId);
        if(!(entity instanceof LivingEntity)||!entity.isAlive()||mc.level.getGameTime()>expires||mc.player.distanceToSqr(entity)>48*48)return;
        LivingEntity target=(LivingEntity)entity;boolean boss=target instanceof BanditEntity&&((BanditEntity)target).role==BanditEntity.ATAMAN;
        int width=boss?182:154,height=boss?49:40,x=Math.min(CombatHudConfig.X.get(),Math.max(0,e.getWindow().getGuiScaledWidth()-width)),y=Math.min(CombatHudConfig.Y.get(),Math.max(0,e.getWindow().getGuiScaledHeight()-height));
        MatrixStack m=e.getMatrixStack();fill(m,x,y,x+width,y+height,0xef211a14);fill(m,x+2,y+2,x+width-2,y+height-2,0xef4c3828);
        for(int dx:new int[]{0,width-7})for(int dy:new int[]{0,height-7})fill(m,x+dx,y+dy,x+dx+7,y+dy+7,0xff999487);
        // A small shield/type emblem avoids unstable offscreen entity rendering.
        fill(m,x+9,y+10,x+25,y+26,0xff797567);fill(m,x+13,y+26,x+21,y+30,0xff797567);fill(m,x+15,y+12,x+19,y+26,boss?0xffaa7050:0xff483d31);
        mc.font.draw(m,mc.font.substrByWidth(target.getDisplayName(),width-42).getString(),x+33,y+5,0xffecddbd);
        int bar=width-43;fill(m,x+33,y+19,x+33+bar,y+25,0xff211711);fill(m,x+33,y+19,x+33+(int)(bar*Math.max(0,Math.min(1,target.getHealth()/target.getMaxHealth()))),y+25,boss?0xffa9643e:0xff9e4736);
        mc.font.draw(m,(int)Math.ceil(target.getHealth())+" / "+(int)target.getMaxHealth(),x+33,y+28,0xffdbcbae);
    }
}
