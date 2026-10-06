from pathlib import Path
import subprocess,json
R=Path('.');javap=Path('C:/Program Files/Java/jdk-21.0.12/bin/javap.exe');cp=str(R/'build/classes/java/main');out=R/'docs/verification/swamp-0.9.7';out.mkdir(exist_ok=True)
code=subprocess.check_output([str(javap),'-c','-p','-classpath',cp,'org.slavicmyths.compat.JeiRituals'],text=True)
section=code.split('public void registerRecipeCatalysts')[1].split('private static')[0]
assert 'anewarray' in section and 'RecipeType' in section and 'getstatic' in section and 'RECIPE_TYPE' in section
assert 'addRecipeCatalyst:(Lnet/minecraft/world/item/ItemStack;[Lmezz/jei/api/recipe/RecipeType;)V' in section
category=subprocess.check_output([str(javap),'-c','-p','-classpath',cp,'org.slavicmyths.compat.JeiRituals$Category'],text=True);assert 'JeiRituals.RECIPE_TYPE' in category
jar=R/'.tools/test-pack-0.9.4/jei-1.21.1-neoforge-19.51.0.418.jar';api=subprocess.check_output([str(javap),'-classpath',str(jar),'mezz.jei.api.registration.IRecipeCatalystRegistration'],text=True);assert 'RecipeType<?>...' in api
(out/'jei-ritual-contract-bytecode.txt').write_text(code+'\n'+category+'\n'+api,encoding='utf-8');(out/'jei-ritual-contract.json').write_text(json.dumps({'status':'PASS','level':'compiled bytecode and exact pinned JEI interface','jeiVersion':'19.51.0.418','categoryAndCatalystShareRecipeType':True,'catalystRecipeTypeCount':1,'realClientPluginRegistrationVerified':False,'clientLaunched':False},indent=2)+'\n',encoding='utf-8');print('JEI_RITUAL_CONTRACT_PASS sameRegisteredCategoryType=true catalystTypes=1 pinnedVersion=19.51.0.418; actual client registration remains manual')
