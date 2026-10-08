package org.slavicmyths.rpg;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Persistent blank substrate; deliberately independent of installed rune effects. */
public record RuneBase(int tier, String material) {
    public RuneBase {
        if (!valid(tier, material)) throw new IllegalArgumentException("Invalid blank rune base");
    }
    public static boolean valid(int tier, String material) {
        return switch (tier) {
            case 1 -> material.equals("iron") || material.equals("gold");
            case 2 -> material.equals("diamond") || material.equals("perunite");
            case 3 -> material.equals("perunite");
            default -> false;
        };
    }
    private record Stored(int tier, String material) { }
    public static final Codec<RuneBase> CODEC = RecordCodecBuilder.<Stored>create(i -> i.group(
        Codec.intRange(1,3).fieldOf("tier").forGetter(Stored::tier),
        Codec.STRING.fieldOf("material").forGetter(Stored::material)
    ).apply(i, Stored::new)).comapFlatMap(s -> valid(s.tier,s.material)
        ? DataResult.success(new RuneBase(s.tier,s.material))
        : DataResult.error(() -> "Invalid tier/material pair"), b -> new Stored(b.tier,b.material));
}
