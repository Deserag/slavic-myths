package org.slavicmyths.navya;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
/** Append to an already loaded preset; never create bootstrap registry references. */
public interface NavyaPresetAccess { void slavicmyths$append(Holder<Biome> biome); }
