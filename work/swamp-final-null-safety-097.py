from pathlib import Path
p=Path('src/main/java/org/slavicmyths/compat/xaero/XaeroNavigationBridge.java');s=p.read_text(encoding='utf-8');old='    @Override public boolean publish(NavigationState state,ResourceLocation dimension){';new='''    @Override public boolean publish(NavigationState state,ResourceLocation dimension){
        try{return publishAvailable(state,dimension);}
        catch(RuntimeException|LinkageError unavailable){clear();com.mojang.logging.LogUtils.getLogger().debug("Optional Xaero guidance unavailable",unavailable);return false;}
    }
    private boolean publishAvailable(NavigationState state,ResourceLocation dimension){
        if(state==null||dimension==null){clear();return false;}
''';assert old in s;s=s.replace(old,new);s=s.replace('        set=world.getSets().get(GROUP);Set<UUID> wanted=new HashSet<>();','        set=world.getSets().get(GROUP);if(set==null){clear();return false;}Set<UUID> wanted=new HashSet<>();');p.write_text(s,encoding='utf-8')
p=Path('tools/verify_xaero_lifecycle_097.py');s=p.read_text(encoding='utf-8');s=s.replace('  System.out.println("XAERO_LIFECYCLE_PASS cases="+cases+', '  check(!b.publish(null,ResourceLocation.fromNamespaceAndPath("minecraft","overworld")));cases++;check(!b.publish(new NavigationState(),null));cases++;\n  System.out.println("XAERO_LIFECYCLE_PASS cases="+cases+').replace("'cases':8","'cases':10");p.write_text(s,encoding='utf-8')
# Make the current resource entrypoint select the current version rather than historical port checks.
p=Path('tools/verify_resources.py');s=p.read_text(encoding='utf-8');a="    if 'mod_version=0.9.6' in active";index=s.index(a);s=s[:index]+"    if 'mod_version=0.9.7' in active and '--navigation' not in sys.argv:\n        raise SystemExit(subprocess.call([sys.executable,str(ROOT/'tools/verify_swamp_097.py'),*[a for a in sys.argv[1:] if a!='--swamp-rework']]))\n"+s[index:];p.write_text(s,encoding='utf-8')
