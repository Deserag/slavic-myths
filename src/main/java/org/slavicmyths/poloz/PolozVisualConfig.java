package org.slavicmyths.poloz;
import net.neoforged.neoforge.common.ModConfigSpec;
public final class PolozVisualConfig {
 public static final ModConfigSpec SPEC;public static final ModConfigSpec.BooleanValue SHAKE;
 static{var b=new ModConfigSpec.Builder();SHAKE=b.define("polozCameraShake",true);SPEC=b.build();}
}
