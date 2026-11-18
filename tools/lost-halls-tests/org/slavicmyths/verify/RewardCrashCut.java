package org.slavicmyths.verify;
import java.nio.file.*;import java.nio.*;import java.nio.channels.*;import java.util.*;import org.slavicmyths.losthalls.BossRewardAuthority;
/** Child JVM: deliberately halts at file persistence boundaries. Never starts Minecraft. */
public final class RewardCrashCut {
 static final UUID A=new UUID(0,1),B=new UUID(0,2);
 static void save(Path p,String body)throws Exception{var temp=p.resolveSibling(p.getFileName()+".tmp");try(var out=FileChannel.open(temp,StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.WRITE)){var bytes=ByteBuffer.wrap(body.getBytes(java.nio.charset.StandardCharsets.UTF_8));while(bytes.hasRemaining())out.write(bytes);out.force(true);}Files.move(temp,p,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}
 public static void main(String[] args)throws Exception{var dir=Path.of(args[0]);int cut=Integer.parseInt(args[1]);var a=new BossRewardAuthority(dir.resolve("authority.json"));a.pin(new BossRewardAuthority.Entry("arena","magma",A,4,0));if(cut==1)Runtime.getRuntime().halt(31);save(dir.resolve("world.json"),"defeated:A");if(cut==2)Runtime.getRuntime().halt(31);save(dir.resolve("playerA.json"),"inventory=one_batch;receipt=arena");if(cut==3)Runtime.getRuntime().halt(31);save(dir.resolve("world.json"),"alive:rolled_back");Runtime.getRuntime().halt(31);}
}
