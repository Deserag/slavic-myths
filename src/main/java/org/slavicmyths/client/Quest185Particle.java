package org.slavicmyths.client;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
/** Tiny authored pixel sparks, with short lifetime and no collision/world effect. */
public final class Quest185Particle extends TextureSheetParticle {
 private Quest185Particle(ClientLevel level,double x,double y,double z,double dx,double dy,double dz,SpriteSet sprites,boolean inspection){super(level,x,y,z,dx,dy,dz);quadSize=inspection?.075f:.045f;lifetime=12;friction=.94f;hasPhysics=false;pickSprite(sprites);setColor(inspection?.85f:1,inspection?.25f:.9f,inspection?.28f:.7f);}
 @Override public ParticleRenderType getRenderType(){return ParticleRenderType.PARTICLE_SHEET_LIT;}
 @Override public void tick(){super.tick();alpha=1-age/(float)lifetime;}
 public static final class Provider implements ParticleProvider<SimpleParticleType>{private final SpriteSet sprites;private final boolean inspection;public Provider(SpriteSet s,boolean i){sprites=s;inspection=i;}@Override public Particle createParticle(SimpleParticleType type,ClientLevel level,double x,double y,double z,double dx,double dy,double dz){return new Quest185Particle(level,x,y,z,dx,dy,dz,sprites,inspection);}}
}
