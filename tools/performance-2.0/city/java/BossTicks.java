package org.slavicmyths.perf2;

import java.io.*;
import java.nio.file.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/** Follow-up observer only: no entity/world cache, target assignment, damage or phase mutation. */
final class BossTicks {
    private static PrintWriter writer;
    static void record(String scene,int age,LivingEntity boss,ServerPlayer player) {
        if(boss==null)return;
        int state,action,h0=0,h1=0,h2=0;
        boolean fighting,target=false;
        float health=boss.getHealth(),logical=health;
        if(boss instanceof org.slavicmyths.gorynych.Gorynych b) {
            state=b.state();action=b.bodyAction();logical=b.logicalHealth();fighting=b.fighting();
            h0=b.heads().getInt("Action0");h1=b.heads().getInt("Action1");h2=b.heads().getInt("Action2");target=b.getTarget()==player;
        } else if(boss instanceof org.slavicmyths.karachun.Karachun b) {
            state=b.phase();action=b.action();fighting=b.fighting();target=b.getTarget()==player;
        } else if(boss instanceof org.slavicmyths.morozko.Morozko b) {
            state=b.phase();action=b.action();fighting=b.fighting();target=b.getTarget()==player;
        } else return;
        try {
            if(writer==null) {
                writer=new PrintWriter(Files.newBufferedWriter(Path.of(System.getProperty("perf.output")).resolve("boss-ticks.csv")));
                writer.println("scene,age,epoch_ms,alive,hp,logical_hp,state,action,head0,head1,head2,fighting,target_player");
            }
            writer.println(scene+","+age+","+System.currentTimeMillis()+","+boss.isAlive()+","+health+","+logical+","+state+","+action+","+h0+","+h1+","+h2+","+fighting+","+target);
            if(age%20==0)writer.flush();
        } catch(IOException failure){throw new RuntimeException(failure);}
    }
}
