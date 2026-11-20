package org.slavicmyths.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.slavicmyths.marine.ZhelvaEntity;
import org.slavicmyths.marine.ZhelvaGeometryProfile;

/**
 * Actual baked voxel geometry, not a concept image or scaled vanilla turtle.
 * Ellipsoidal thick domed carapace, discrete uneven scutes, belly/plastron,
 * stone scars, shell algae, four separate moving flippers and hinged jaws.
 * Geometry coordinates are authored in Minecraft's 16-units-per-block space.
 */
public final class ZhelvaModel extends EntityModel<ZhelvaEntity> {
    private static final int SKIN = 0xFF536F5D, DARK_SKIN = 0xFF304B43,
        NECK = 0xFF6E8570, PLASTRON = 0xFFB5AB83, SEAM = 0xFF675B45,
        RIM = 0xFF3F5144, SHELL = 0xFF657157, PALE_SHELL = 0xFF87927A,
        SCUTE = 0xFF9D9C70, CRACK = 0xFF303B35, STONE = 0xFF697475,
        LIGHT_STONE = 0xFF9EA79A, KELP = 0xFF336A54, WEED = 0xFF5F9863,
        BARNACLE = 0xFFD8D3BA, EYE = 0xFFEDC75F, PUPIL = 0xFF131D21,
        BEAK = 0xFFB5A58A, MOUTH = 0xFF493E3A, TEETH = 0xFFE5D7B7;
    private final FolkModelGeometry geometry = new FolkModelGeometry();
    private final Group root, shell, head, jaw, tail, leftFront, rightFront, leftBack, rightBack;
    private int cuboids;

    private final class Group {
        final ModelPart pivot = geometry.part(128, 128);
        final Map<Integer, ModelPart> colors = new LinkedHashMap<>();
        final List<Group> children = new ArrayList<>();
        final float x, y, z;
        Group(Group parent, float x, float y, float z) {
            this.x = x; this.y = y; this.z = z;
            if (parent == null) pivot.setPos(x, y, z);
            else {
                pivot.setPos(x - parent.x, y - parent.y, z - parent.z);
                parent.children.add(this);
            }
        }
        void cube(int tint, float x, float y, float z, float w, float h, float d) {
            if (!Float.isFinite(w) || !Float.isFinite(h) || !Float.isFinite(d) || w <= 0 || h <= 0 || d <= 0)
                throw new IllegalArgumentException("Invalid Zhelva cuboid: "+w+" x "+h+" x "+d);
            ModelPart colored = colors.computeIfAbsent(tint, k -> geometry.part(128, 128, 0, 0));
            geometry.box(colored, x - this.x, y - this.y, z - this.z, w, h, d);
            cuboids++;
        }
        void draw(PoseStack pose, VertexConsumer out, int light, int overlay) {
            pose.pushPose();
            pivot.translateAndRotate(pose);
            for (var entry : colors.entrySet())
                entry.getValue().render(pose, out, light, overlay, entry.getKey());
            for (Group child : children) child.draw(pose, out, light, overlay);
            pose.popPose();
        }
    }
    public ZhelvaModel() {
        root = new Group(null, 0, 0, 0);
        shell = new Group(root, 0, -24, 0);
        head = new Group(root, 0, -28, -42);
        jaw = new Group(head, 0, -18, -59);
        tail = new Group(root, 0, -20, 43);
        leftFront = new Group(root, -27, -24, -27);
        rightFront = new Group(root, 27, -24, -27);
        leftBack = new Group(root, -26, -23, 26);
        rightBack = new Group(root, 26, -23, 26);
        buildShell();
        buildNeckAndHead();
        buildFlipper(leftFront, -1, true);
        buildFlipper(rightFront, 1, true);
        buildFlipper(leftBack, -1, false);
        buildFlipper(rightBack, 1, false);
        buildTail();
        if (cuboids > 1280) throw new IllegalStateException("Zhelva geometry exceeded cuboid budget");
    }
    public int cuboidCount() { return cuboids; }

    private void buildShell() {
        // Bottom-to-top cross-sections preserve a deep rounded animal body on every side.
        // Each cell is an actual contiguous column, capped by individual overlapping scutes.
        for (float z = -47; z <= 47; z += 6F) {
            for (float x = -33; x <= 33; x += 5.2F) {
                float elliptical = x*x/(35F*35F) + z*z/(49F*49F);
                if (elliptical > 1F) continue;
                float dome = (float) Math.sqrt(Math.max(0, 1 - elliptical));
                float top = -32.8F - 19F*dome;
                float belly = -13.5F + 5.4F*dome;
                int row = (int)((z+47)/6F);
                int col = (int)((x+33)/5.2F);
                int mottled = Math.floorMod(row*13+col*7, 9);
                int plateColor = mottled <= 1 ? PALE_SHELL : mottled == 6 ? SCUTE : SHELL;
                shell.cube(DARK_SKIN, x-2.75F, top+3.5F, z-3.1F,
                    5.65F, Math.max(2F, belly-top-3F), 6.25F);
                shell.cube(PLASTRON, x-2.65F, belly-3.1F, z-2.95F, 5.45F, 4.5F, 5.95F);
                if (elliptical > .18F) {
                    shell.cube(plateColor, x-2.78F, top-1.3F, z-3.08F, 5.75F, 4.8F, 6.3F);
                    if ((row+col)%5 == 0)
                        shell.cube(SCUTE, x-2.3F, top-2.1F, z-2.5F, 4.6F, 1.6F, 4.75F);
                } else {
                    shell.cube(plateColor, x-2.78F, top-1F, z-3.08F, 5.75F, 4.5F, 6.3F);
                }
                // Thin dark scute boundaries, not a disconnected checkerboard.
                if ((row + col * 3) % 4 == 0 && elliptical < .84F)
                    shell.cube(CRACK, x-1.9F, top-2.15F, z-2.3F, .8F, 1.15F, 3.65F);
                if (elliptical > .72F) {
                    shell.cube(RIM, x-2.9F, -32.8F, z-3.05F, 5.9F, 7.4F, 6.2F);
                    shell.cube(SEAM, x-2.6F, -24.9F, z-3.02F, 5.2F, 1.2F, 6.1F);
                }
                if ((col*11 + row*17) % 19 == 0 && elliptical < .83F) {
                    shell.cube(STONE, x-1.6F, top-4.0F, z-.6F, 3.4F, 3.1F, 2.8F);
                    shell.cube(LIGHT_STONE, x-.8F, top-4.8F, z, 1.7F, 1.5F, 1.7F);
                }
                if ((col*7 + row*5) % 22 == 0 && elliptical > .4F && elliptical < .94F)
                    for (int k=0; k<3; k++)
                        shell.cube(k==0?KELP:WEED, x-.65F+k*.5F,
                            top-5.5F-k*3.2F, z+k*1.2F, 1.3F, 4.4F, 1.65F);
                if ((row*13 + col*29)%31==0 && elliptical < .95F) {
                    shell.cube(BARNACLE, x-2.3F, top-3.0F, z+.4F, 2.9F, 1.9F, 3.2F);
                    shell.cube(SEAM, x-1.5F, top-3.6F, z+1F, 1.3F, .7F, 1.4F);
                }
            }
        }
        // Archaeic concentric ridges and radiating cracks, following real shell curvature.
        for (int i=0; i<24; i++) {
            float angle = i*(float)Math.PI/12F;
            float r = i%2==0 ? .54F : .70F;
            float x = 33F*r*Mth.cos(angle);
            float z = 47F*r*Mth.sin(angle);
            float y = -33F - 19F*(float)Math.sqrt(1-r*r);
            shell.cube(i%3==0?SCUTE:SEAM, x-1.4F, y-2.5F, z-1.3F,
                2.8F, 1.35F, 2.6F);
        }
        // Underside/plastron division visible from below.
        for (int z=-35; z<=35; z+=14)
            shell.cube(SEAM, -26F, -9.5F, z, 52F, 1.2F, 1.2F);
        shell.cube(SEAM,-1.0F,-10F,-40F,2F,1.3F,80F);
    }

    private void buildNeckAndHead() {
        // Muscular tapered neck overlaps the carapace's leading rim continuously.
        for (float z=-59F; z<=-31F; z+=4.2F) {
            float t=(z+59F)/28F;
            float radius=12.5F+3.5F*t;
            // Sample the center of each neck cell. The ellipse edge has zero
            // thickness and must never be baked into an empty ModelPart cube.
            for(float x=-radius+2.2F; x<radius; x+=4.4F) {
                float thickness=ZhelvaGeometryProfile.neckThickness(x,radius);
                if(thickness<=0F) continue;
                head.cube(z<-48?SKIN:NECK,x-2.2F,-28F-thickness,z,
                    4.55F,thickness*1.9F,4.6F);
            }
        }
        // A broad intelligent turtle skull, narrow hooked beak at the front.
        for (float z=-82F; z<=-53F; z+=4.5F) {
            float t=(z+82F)/29F;
            float rx=10F+7.5F*t;
            for (float x=-rx; x<=rx; x+=4.1F) {
                float q=Math.max(0,1-x*x/(rx*rx));
                float ry=7F+7F*(float)Math.sqrt(q);
                head.cube((Math.round(x+z)&3)==0?NECK:SKIN,
                    x-2.2F,-33F-ry*.65F,z,4.45F,ry*1.27F,4.8F);
            }
        }
        head.cube(DARK_SKIN, -12.5F,-36.5F,-83F,25F,6.5F,4F);
        // Hooked upper beak forms a central descending wedge, not a flat snout.
        head.cube(BEAK,-8.0F,-29.8F,-87.5F,16F,5.8F,8.3F);
        head.cube(BEAK,-4.6F,-26.8F,-89.0F,9.2F,7.7F,4.0F);
        head.cube(SEAM,-6.8F,-24.3F,-84.2F,13.6F,1.25F,8F);
        // Jaw hinge rotates separately and opens in the precise bite windup.
        jaw.cube(MOUTH,-12F,-21.2F,-80F,24F,3.3F,19.2F);
        jaw.cube(BEAK,-9.6F,-17.9F,-83.8F,19.2F,5F,23F);
        jaw.cube(PLASTRON,-7.7F,-14.1F,-86F,15.4F,3.2F,20F);
        for(int x:new int[]{-10,-5,5,10}) {
            jaw.cube(TEETH,x-1F,-21.5F,-78F,2.2F,3.3F,2.8F);
            head.cube(TEETH,x-1F,-22.5F,-79F,2.2F,3.2F,2.8F);
        }
        // Recessed amber irises, scleral ring and pupils on both sides.
        for(int s:new int[]{-1,1}) {
            head.cube(DARK_SKIN,s*15F-4F,-43F,-66F,8F,8F,10F);
            head.cube(BARNACLE,s*16.5F-3.25F,-41.7F,-65F,6.5F,6.2F,7.1F);
            head.cube(EYE,s*17.5F-2.7F,-41F,-63.6F,5.4F,5.0F,5F);
            head.cube(PUPIL,s*18.5F-1.8F,-40F,-62.8F,3.6F,3.35F,3.3F);
            head.cube(LIGHT_STONE,s*18.5F-1F,-40.8F,-63F,1.5F,1.4F,1.5F);
            // Bone-like cheek scutes and deeply engraved lower-gill creases.
            for(int j=0;j<3;j++)
                head.cube(j==0?STONE:PALE_SHELL,s*(14.4F+j*.4F)-2F,
                    -31F+j*3.7F,-57F+j*3F,4.1F,2.4F,6F);
        }
    }

    private void buildFlipper(Group fin, int sign, boolean front) {
        float originX=sign*(front?27F:26F);
        float originZ=front?-27F:26F;
        // Broad tapered paddle with thickness in all three axes, and no air gap at root.
        for(int i=0;i<10;i++) {
            float t=i/9F;
            float x=originX+sign*i*4.35F;
            float z=originZ+(front?-1:1)*i*2.1F;
            float width=8.0F*(1-.56F*t);
            float thickness=8F*(1-.62F*t);
            float depth=13.5F*(1-.33F*t);
            fin.cube(i%3==0?NECK:SKIN,x-width*.5F,-26F+2F*t-thickness*.45F,
                z-depth*.5F,width,thickness,depth);
            if(i%2==0)
                fin.cube(PALE_SHELL,x-width*.42F,-29.7F+2F*t,
                    z-depth*.38F,width*.84F,1.7F,depth*.75F);
        }
        // Three tapering terminal claws and joint scars.
        for(int j=0;j<3;j++) {
            float x=originX+sign*43F+sign*j*1.2F;
            float z=originZ+(front?-18F:18F)+(j-1)*3.6F;
            fin.cube(BEAK,x-2F,-24F,z-2.2F,4F,4F,5.7F);
            fin.cube(TEETH,x-1.1F,-25F,z-3.5F,2.1F,2.4F,4.2F);
        }
    }
    private void buildTail() {
        for(int i=0;i<11;i++) {
            float t=i/10F;
            float z=43F+i*3.4F;
            float width=Math.max(1F,9F*(1-t));
            float height=Math.max(1F,8F*(1-t));
            float sway=3F*Mth.sin(t*Mth.PI*.85F);
            tail.cube(i%3==0?NECK:SKIN, sway-width*.5F,-22F-height*.5F,z,
                width,height,3.8F);
            if(i%2==0 && i<9)
                tail.cube(PALE_SHELL,sway-width*.3F,-23F-height*.6F,z,
                    width*.6F,1.7F,3.2F);
        }
        tail.cube(BEAK,2.1F,-24.5F,79F,2.5F,2.6F,4F);
    }
    @Override public void setupAnim(ZhelvaEntity mob, float walk, float speed,
                                    float age, float yaw, float pitch) {
        int state=mob.action();
        float tick=mob.actionTicks();
        float cycle=Mth.sin(age*.085F);
        float heavy=Mth.sin(Mth.PI*Mth.clamp(tick/50F,0F,1F));
        root.pivot.xRot=0;
        root.pivot.zRot=0;
        shell.pivot.xRot=0;
        head.pivot.yRot=0;
        head.pivot.xRot=0;
        leftFront.pivot.zRot=-.11F*cycle;
        rightFront.pivot.zRot=.11F*cycle;
        leftBack.pivot.zRot=.08F*cycle;
        rightBack.pivot.zRot=-.08F*cycle;
        tail.pivot.yRot=.13F*Mth.sin(age*.08F);
        jaw.pivot.xRot=.03F;
        if(state==ZhelvaEntity.BITE) {
            jaw.pivot.xRot=.75F*Mth.sin(Mth.PI*Mth.clamp(tick/32F,0,1));
            head.pivot.xRot=-.23F*heavy;
        } else if(state==ZhelvaEntity.SWEEP_LEFT) {
            leftFront.pivot.yRot=-.78F*heavy;
            rightFront.pivot.yRot=.12F*heavy;
        } else if(state==ZhelvaEntity.SWEEP_RIGHT) {
            rightFront.pivot.yRot=.78F*heavy;
            leftFront.pivot.yRot=-.12F*heavy;
        }
        if(state!=ZhelvaEntity.SWEEP_LEFT && state!=ZhelvaEntity.SWEEP_RIGHT) {
            leftFront.pivot.yRot=0;
            rightFront.pivot.yRot=0;
        }
        if(state==ZhelvaEntity.GUARD) {
            head.pivot.zRot=.0F;
            head.pivot.xRot=.35F;
            shell.pivot.xRot=-.035F;
            leftFront.pivot.zRot=-.42F;
            rightFront.pivot.zRot=.42F;
        } else head.pivot.zRot=0;
        if(state==ZhelvaEntity.PRESSURE_RING || state==ZhelvaEntity.WHIRLPOOL) {
            shell.pivot.xRot=.12F*heavy;
            head.pivot.xRot=-.18F*heavy;
            leftFront.pivot.zRot=-.30F*heavy;
            rightFront.pivot.zRot=.30F*heavy;
        }
        if(state==ZhelvaEntity.CHARGE) {
            head.pivot.xRot=-.14F;
            tail.pivot.yRot=.20F*Mth.sin(age*.20F);
            leftFront.pivot.zRot=-.23F;
            rightFront.pivot.zRot=.23F;
        }
        if(state==ZhelvaEntity.SHELL_SLAM) {
            root.pivot.xRot=.13F*heavy;
            shell.pivot.xRot=-.18F*heavy;
            head.pivot.xRot=-.15F*heavy;
        }
        if(state==ZhelvaEntity.PRESSURE_LANCE || state==ZhelvaEntity.SILT)
            head.pivot.xRot=-.16F*heavy;
        if(mob.deathTime>0) {
            float fall=Mth.clamp(mob.deathTime/20F,0,1);
            root.pivot.zRot=.12F*fall;
            head.pivot.xRot=.28F*fall;
            jaw.pivot.xRot=.24F*fall;
        }
    }
    @Override public void renderToBuffer(PoseStack pose, VertexConsumer consumer,
                                         int light, int overlay, int color) {
        root.draw(pose,consumer,light,overlay);
    }
    /** The physical trophy shows exactly the same independently animated head cuboids. */
    public void renderTrophyHead(PoseStack pose, VertexConsumer consumer,
                                 int light, int overlay, float age) {
        jaw.pivot.xRot=.08F+.09F*(.5F+.5F*Mth.sin(age*.08F));
        head.pivot.xRot=0;
        head.pivot.yRot=0;
        head.draw(pose,consumer,light,overlay);
    }
}
