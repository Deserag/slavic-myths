package org.slavicmyths.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

/** Tiny square sprite with twelve loaded client ticks, rather than a long-lived cloud. */
public final class MorParticle extends TextureSheetParticle {
    private MorParticle(ClientLevel level,double x,double y,double z,double vx,double vy,double vz,SpriteSet sprites,boolean ash){super(level,x,y,z,vx,vy,vz);lifetime=12;quadSize=.04F;friction=.86F;gravity=.025F;xd=vx;yd=vy;zd=vz;setColor(ash?.12F:.88F,ash?.10F:.12F,ash?.13F:.18F);pickSprite(sprites);}
    @Override public ParticleRenderType getRenderType(){return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;}
    @Override public void tick(){super.tick();alpha=Math.max(0,1-age/12F);}
    public static ParticleProvider<SimpleParticleType> provider(SpriteSet sprites,boolean ash){return (type,level,x,y,z,vx,vy,vz)->new MorParticle(level,x,y,z,vx,vy,vz,sprites,ash);}
}
