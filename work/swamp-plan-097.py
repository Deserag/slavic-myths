from pathlib import Path
p=Path('src/main/java/org/slavicmyths/swamp/SwampStructure.java');s=p.read_text(encoding='utf-8');a=s.index('            switch(id)');b=s.index('            // Validate',a);s=s[:a]+'''            switch(id) {
                case "swamp_hut":plan.add(new Plan("v097_abandoned_"+variant,0,0,false));break;
                case "fishing_camp":plan.add(new Plan("v097_fisher_"+variant,0,0,false));break;
                case "bog_causeway":plan.add(new Plan("v097_boardwalk_"+variant,0,0,false));break;
                case "swamp_remnants":plan.add(new Plan("v097_landing_"+variant,0,0,false));break;
                case "flooded_shrine":plan.add(new Plan("v097_shrine_"+variant,0,0,false));break;
                case "abandoned_settlement":plan.add(new Plan("v097_cluster_"+variant,0,0,false));break;
                case "swamp_watchtower":plan.add(new Plan("v097_watchtower_"+variant,0,0,false));break;
                case "underwater_ruins":plan.add(new Plan("ruin_"+variant,0,0,true));break;
                default:return;
            }
'''+s[b:];s=s.replace('if(min<45||max>78||max-min>4||maxSurface-minSurface>4)','if(min<45||max>78||max-min>4||maxSurface-minSurface>4||maxSurface-min>7||maxSurface+size.getY()>=context.heightAccessor().getMaxBuildHeight())');s=s.replace('"swamp_remnants");','"swamp_remnants","swamp_watchtower");');p.write_text(s,encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/swamp/SwampStructures.java');s=p.read_text(encoding='utf-8').replace('"swamp_remnants"};','"swamp_remnants","swamp_watchtower"};');p.write_text(s,encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/swamp/SwampCommands.java');s=p.read_text(encoding='utf-8').replace('Arrays.copyOf(SwampStructures.IDS,6)','SwampStructures.IDS');p.write_text(s,encoding='utf-8')
p=Path('gradle.properties');s=p.read_text(encoding='utf-8').replace('mod_version=0.9.6','mod_version=0.9.7');p.write_text(s,encoding='utf-8')
