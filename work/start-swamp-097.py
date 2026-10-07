from pathlib import Path
import shutil,zipfile
R=Path('.');b=R/'.tools/port-backups';b.mkdir(exist_ok=True);shutil.copy2(R/'build/libs/slavicmyths-0.9.6.jar',b/'slavicmyths-0.9.6-final.jar')
with zipfile.ZipFile(b/'slavicmyths-0.9.6-pre-0.9.7.zip','w',zipfile.ZIP_DEFLATED) as z:
 for folder in ['src','docs','packaging']:
  for p in (R/folder).rglob('*'):
   if p.is_file():z.write(p,p.as_posix())
 for name in ['README.md','gradle.properties','build.gradle']:z.write(R/name,name)
p=R/'src/main/java/org/slavicmyths/compat/xaero/XaeroNavigationBridge.java';s=p.read_text(encoding='utf-8').replace('var manager=current.getWaypointsManager();var next=manager.getCurrentWorld();','var manager=current.getWaypointsManager();if(manager==null){clear();return false;}var next=manager.getCurrentWorld();');old='''    @Override public void clear(){if(set!=null)for(var point:owned.values())set.remove(point);
        if(session!=null)session.getWaypointsManager().updateWaypoints();
        owned.clear();set=null;world=null;session=null;publishedState=null;}''';new='''    @Override public void clear(){
        var previousSession=session;var previousSet=set;var points=List.copyOf(owned.values());
        // Reset our state before touching optional integration during session teardown.
        owned.clear();set=null;world=null;session=null;publishedState=null;
        try {
            if(previousSet!=null)for(var point:points)previousSet.remove(point);
            if(previousSession!=null&&XaeroMinimapSession.getCurrentSession()==previousSession){
                var manager=previousSession.getWaypointsManager();
                if(manager!=null)manager.updateWaypoints();
            }
        } catch(RuntimeException|LinkageError unavailable){
            com.mojang.logging.LogUtils.getLogger().debug("Xaero session closed during navigation cleanup",unavailable);
        }
    }''';assert old in s;s=s.replace(old,new);p.write_text(s,encoding='utf-8')
p=R/'src/main/java/org/slavicmyths/navigation/client/NavigationClient.java';s=p.read_text(encoding='utf-8');old='bridge.clear();bridge=MapIntegrationBridge.NONE;state=new NavigationState();initialized=false;mapGuidance=false;retry=0;';new='var previous=bridge;bridge=MapIntegrationBridge.NONE;state=new NavigationState();initialized=false;mapGuidance=false;retry=0;try{previous.clear();}catch(RuntimeException|LinkageError unavailable){com.mojang.logging.LogUtils.getLogger().debug("Optional map cleanup unavailable",unavailable);}';assert old in s;s=s.replace(old,new);p.write_text(s,encoding='utf-8')
p=R/'src/main/java/org/slavicmyths/compat/JeiRituals.java';s=p.read_text(encoding='utf-8').replace('registration.addRecipeCatalyst(new ItemStack(ModItems.ALTAR.get()));','registration.addRecipeCatalyst(new ItemStack(ModItems.ALTAR.get()),RECIPE_TYPE);');p.write_text(s,encoding='utf-8')
jar=zipfile.ZipFile(R/'build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar');states=__import__('json').loads(jar.read('assets/minecraft/blockstates/oak_door.json'));J=__import__('json');out=R/'src/main/resources/assets/slavicmyths';models={v['model'].split('/')[-1] for v in states['variants'].values()}
for v in states['variants'].values():v['model']=v['model'].replace('minecraft:block/oak_door','slavicmyths:block/linden_door')
(out/'blockstates/linden_door.json').write_text(J.dumps(states,indent=2)+'\n',encoding='utf-8')
for name in models:
 model=J.loads(jar.read('assets/minecraft/models/block/'+name+'.json'));model['textures']={k:v.replace('minecraft:block/oak_door','slavicmyths:block/linden_door') for k,v in model['textures'].items()};(out/'models/block'/name.replace('oak_','linden_')).with_suffix('.json').write_text(J.dumps(model,indent=2)+'\n',encoding='utf-8')
# Preserve old model IDs as aliases for external/resource-pack references.
for old,new in [('bottom','bottom_left'),('bottom_hinge','bottom_right'),('top','top_left'),('top_hinge','top_right')]:
 (out/'models/block'/('linden_door_'+old+'.json')).write_text(J.dumps({'parent':'slavicmyths:block/linden_door_'+new},indent=2)+'\n',encoding='utf-8')
print('Backup and three runtime fixes written; native door states',len(states['variants']))
