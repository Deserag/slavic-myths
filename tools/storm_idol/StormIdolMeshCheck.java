package org.slavicmyths.client;
public final class StormIdolMeshCheck {
 public static void main(String[] args){
  if(StormIdolMesh.HAIR.size()!=21)throw new AssertionError("21 independent hair strands expected");
  if(StormIdolMesh.SURFACE_CUBES<2000||StormIdolMesh.SURFACE_CUBES>4500)throw new AssertionError("unexpected surface cube count: "+StormIdolMesh.SURFACE_CUBES);
  for(int i=0;i<4;i++){final int bone=StormIdolMesh.FIST+i;if(StormIdolMesh.FACES.stream().noneMatch(f->f.bone()==bone))throw new AssertionError("missing fist "+i);}
  for(var f:StormIdolMesh.FACES)for(float v:f.vertices())if(!Float.isFinite(v))throw new AssertionError("invalid vertex");
  System.out.println("Storm Idol: "+StormIdolMesh.SURFACE_CUBES+" surface voxels, "+StormIdolMesh.FACES.size()+" faces, "+StormIdolMesh.HAIR.size()+" hair strands");
 }
}
