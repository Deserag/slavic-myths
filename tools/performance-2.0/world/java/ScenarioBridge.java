package org.slavicmyths.perf2;
import net.minecraft.server.level.*;import java.io.PrintWriter;
public interface ScenarioBridge {
 void setup(String scene,ServerLevel w,ServerPlayer p,PrintWriter out);
 void tick(String scene,int age,ServerLevel w,ServerPlayer p,PrintWriter out);
 void finish(String scene,ServerLevel w,ServerPlayer p,PrintWriter out);
 String status();
}
