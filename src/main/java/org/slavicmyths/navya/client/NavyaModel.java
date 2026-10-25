package org.slavicmyths.navya.client;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import org.slavicmyths.navya.*;
public final class NavyaModel extends HierarchicalModel<NavyaMob> {
    public final NavyaMesh.Rig rig;
    public NavyaModel(NavyaContent.Kind kind){rig=NavyaMesh.create(kind);}
    @Override public ModelPart root(){return rig.root();}
    @Override public void setupAnim(NavyaMob mob,float walk,float amount,float age,float yaw,float pitch){
        float partial=age-mob.tickCount;
        NavyaPose.pose(rig,mob.kind,mob.action(),mob.actionTime(partial),age,walk,amount,yaw,pitch,mob.hurtTime,mob.deathTime);
    }
}
