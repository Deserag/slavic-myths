import org.slavicmyths.client.runeglow.RuneWaveState;
import org.slavicmyths.client.runeglow.RuneGlowConsumer;
import org.slavicmyths.client.runeglow.RuneGlowStyle;
import org.joml.Matrix4f;

/** Deterministic real transition/consumer checks; no world, client or server. */
public final class RuneWave105Checks {
    static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    public static void main(String[] args) {
        RuneWaveState loaded = new RuneWaveState();
        check(loaded.observe(true,10,true)<0,"no false load pulse");
        check(loaded.observe(true,11,true)<0,"no idle repetition");
        check(loaded.observe(false,12,true)<0,"incomplete set");
        check(loaded.observe(true,13,true)==0,"only completion starts pulse");
        for(int i=0;i<4;i++) check(loaded.observe(true,18,true)==.25F,"slots share one start time");
        check(loaded.observe(true,17.4,true)==.25F,"inventory/world sub-tick variation must not cancel");
        check(loaded.observe(true,23,true)==.5F,"halfway");
        check(loaded.observe(true,28,true)==.75F,"three quarters");
        check(loaded.observe(true,33,true)<0,"finite duration");
        check(loaded.observe(true,34,true)<0,"never loops");
        loaded.observe(false,35,true); loaded.observe(true,36,true);
        check(loaded.observe(false,37,true)<0,"removal cancels immediately");
        check(loaded.observe(true,38,true)==0,"reequip starts exactly once");
        check(loaded.observe(true,39,false)<0,"death cancels");
        check(loaded.observe(true,40,true)<0,"respawn full baseline");
        loaded.observe(false,41,true); loaded.observe(true,42,true);
        check(loaded.observe(true,1,true)<0,"clock reset cancels");
        loaded.observe(false,2,true);
        check(loaded.observe(true,100,true)<0,"offscreen long gap is baseline");
        check(loaded.observe(true,Double.NaN,true)<0,"invalid clock reset");
        RuneWaveState observer = new RuneWaveState(); observer.observe(false,100,true);
        check(observer.observe(true,101,true)==0,"independent entity transition");
        check(loaded.observe(true,101,true)<0,"entities do not share state");
        // Different root render matrices restore the same humanoid height.
        var a=new RuneGlow104Checks.Capture(); var b=new RuneGlow104Checks.Capture();
        new RuneGlowConsumer(a,RuneGlowStyle.of(1),new Matrix4f(),.5F)
                .addVertex(0,.5F,0).setColor(0,0,0,0).setUv(0,0);
        new RuneGlowConsumer(b,RuneGlowStyle.of(3),new Matrix4f().translation(0,-10,0),.5F)
                .addVertex(0,10.5F,0).setColor(255,255,255,255).setUv(1,1);
        check(a.r==128 && a.g==128 && a.b==255,"encoded phase/root height");
        check(a.g==b.g && a.r==b.r && a.a==64 && b.a==140,"root height and per-slot rune strength");
        var idle=new RuneGlow104Checks.Capture();
        new RuneGlowConsumer(idle,RuneGlowStyle.of(2),new Matrix4f(),-1)
                .addVertex(0,0,0).setColor(0,0,0,0).setUv(0,0);
        check(idle.r==0x55 && idle.g==0xC9 && idle.b==0x78 && idle.a==100,"accepted104 idle colors intact");
        System.out.println("RUNE105_TRANSITION_CONSUMER_PASS: load/completion/cancel/finish/death/reset/observers/batching/root-space; no game launched");
    }
}
