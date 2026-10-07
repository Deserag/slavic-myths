import zipfile
z=zipfile.ZipFile('build/moddev/artifacts/neoforge-21.1.255-sources.jar')
for path,token in [('world/level/levelgen/structure/Structure','public StructureStart generate'),('world/level/levelgen/flat/FlatLevelGeneratorSettings','public FlatLevelGeneratorSettings(')]:
 s=z.read('net/minecraft/'+path+'.java').decode();start=s.index(token);print(s[start:start+1450])
