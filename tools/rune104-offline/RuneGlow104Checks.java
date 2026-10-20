import com.mojang.blaze3d.vertex.VertexConsumer;
import org.slavicmyths.client.runeglow.RuneGlowStyle;
import org.slavicmyths.client.runeglow.RuneGlowConsumer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import java.io.InputStream;

/** Plain JVM: no client/world/server, no GL and no registry bootstrapping. */
public final class RuneGlow104Checks {
 static void require(boolean b,String s){if(!b)throw new AssertionError(s);}
 static final class Capture implements VertexConsumer {
  int r,g,b,a,position,uv; float x,y,z,u,v;
  public VertexConsumer addVertex(float x,float y,float z){this.x=x;this.y=y;this.z=z;position++;return this;}
  public VertexConsumer setColor(int r,int g,int b,int a){this.r=r;this.g=g;this.b=b;this.a=a;return this;}
  public VertexConsumer setUv(float u,float v){this.u=u;this.v=v;uv++;return this;}
  public VertexConsumer setUv1(int u,int v){throw new AssertionError("Unexpected light overlay attribute");}
  public VertexConsumer setUv2(int u,int v){throw new AssertionError("Unexpected lightmap attribute");}
  public VertexConsumer setNormal(float x,float y,float z){throw new AssertionError("Unexpected normal attribute");}
 }
 static void target(String name,String method,String desc)throws Exception {
  try(InputStream in=RuneGlow104Checks.class.getClassLoader().getResourceAsStream(name+".class")){
   require(in!=null,"Target class missing "+name);ClassNode c=new ClassNode();new ClassReader(in).accept(c,ClassReader.SKIP_FRAMES);
   require(c.methods.stream().filter(m->m.name.equals(method)&&m.desc.equals(desc)).count()==1,"Target ABI mismatch "+name+method);
  }
 }
 public static void main(String[] args)throws Exception {
  require(RuneGlowStyle.of(-10).count()==0&&RuneGlowStyle.of(0).alpha()==0,"Zero/negative style");
  require(RuneGlowStyle.of(1).alpha()<RuneGlowStyle.of(2).alpha()&&RuneGlowStyle.of(2).alpha()<RuneGlowStyle.of(3).alpha(),"Non-monotonic intensity");
  require(RuneGlowStyle.of(99).equals(RuneGlowStyle.of(3)),"Count clamp");
  // Interleave independent vertices, modelling two identical items with different rune counts in a batch.
  Capture one=new Capture(),three=new Capture();VertexConsumer a=new RuneGlowConsumer(one,RuneGlowStyle.of(1),new org.joml.Matrix4f()),b=new RuneGlowConsumer(three,RuneGlowStyle.of(3),new org.joml.Matrix4f());
  for(int i=0;i<4;i++){
   a.addVertex(i,2,3).setColor(255,0,255,255).setUv(.25f,.75f).setUv1(1,2).setUv2(3,4).setNormal(0,1,0);
   b.addVertex(-i,4,5).setColor(0,0,0,0).setUv(.5f,.125f);
  }
  require(one.r==0x55&&one.g==0xc9&&one.b==0x78&&one.a==36,"Emerald level1 color");
  require(three.a==82&&one.a!=three.a,"Batched stack parameter isolation");
  require(one.position==4&&one.uv==4&&one.x==3&&one.y==2&&one.z==3&&Math.abs(one.u-4.8f)<.00001f&&Math.abs(one.v-1.25f)<.00001f,"Geometry/UV changed");
  Capture world=new Capture(),gui=new Capture();
  new RuneGlowConsumer(world,RuneGlowStyle.of(2),new org.joml.Matrix4f()).addVertex(1,2,3).setColor(1,1,1,1).setUv(0,0);
  new RuneGlowConsumer(gui,RuneGlowStyle.of(2),new org.joml.Matrix4f().scaling(1f/16)).addVertex(16,32,48).setColor(1,1,1,1).setUv(1,1);
  require(Math.abs(world.u-gui.u)<.00001f&&Math.abs(world.v-gui.v)<.00001f,"GUI/world density must be invariant to render scale and atlas UVs");
  target("net/minecraft/client/renderer/entity/ItemRenderer","render","(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V");
  target("net/minecraft/client/renderer/entity/layers/HumanoidArmorLayer","renderArmorPiece","(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V");
  try(InputStream in=RuneGlow104Checks.class.getClassLoader().getResourceAsStream("net/minecraft/client/renderer/MultiBufferSource$BufferSource.class")){
   ClassNode c=new ClassNode();new ClassReader(in).accept(c,ClassReader.SKIP_CODE);
   require(c.fields.stream().anyMatch(field->field.name.equals("fixedBuffers")&&field.desc.equals("Ljava/util/SequencedMap;")),"Buffer accessor target ABI mismatch");
  }
  System.out.println("RUNE104_PLAIN_JVM_PASS: style, color/geometry/UV forwarding, batch isolation, pinned target descriptors. No game/GL launched.");
 }
}
