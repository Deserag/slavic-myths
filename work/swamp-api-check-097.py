import zipfile
z=zipfile.ZipFile('build/moddev/artifacts/neoforge-21.1.255-sources.jar')
for file in ['world/level/chunk/LevelChunkSection','world/level/levelgen/feature/FeaturePlaceContext','world/level/chunk/PalettedContainer']:
 s=z.read('net/minecraft/'+file+'.java').decode();print(file);print('\n'.join(l.strip() for l in s.splitlines() if 'public '+file.split('/')[-1]+'(' in l or 'getStates(' in l or 'SECTION_BIOMES' in l))
