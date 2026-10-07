"""One-time source integration; refuses duplicate insertion and preserves existing RC2 edits."""
from pathlib import Path

ROOT=Path('src/main/java/org/slavicmyths')
def insert(file, marker, content):
    p=ROOT/file; s=p.read_text(encoding='utf-8');
    if content.strip() not in s:
        assert marker in s, (file,marker)
        p.write_text(s.replace(marker,content+marker,1),encoding='utf-8')

crops=['rye','barley','oat','turnip','cabbage','pea','flax']
produce=['rye_grain','barley_grain','oat_grain','turnip','cabbage','pea_pod','flax_stalk']
blocks=''; items=''
for c in crops:
    rate={'cabbage':'.8F','flax':'.9F'}.get(c,'1F')
    blocks+=f'    public static final DeferredHolder<Block, Block> {c.upper()}_CROP = BLOCKS.register("{c}_crop", () -> new org.slavicmyths.farming.FarmingCrop(plantProperties().randomTicks().noOcclusion(), () -> ModItems.{c.upper()}_SEEDS.get(), {rate}));\n'
    items+=f'    public static final DeferredHolder<Item, Item> {c.upper()}_SEEDS = ITEMS.register("{c}_seeds", () -> new ItemNameBlockItem(ModBlocks.{c.upper()}_CROP.get(), properties()));\n'
for c in produce:
    food='.food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(2).saturationModifier('+('.4F' if c=='turnip' else '.3F')+').build())' if c in ['turnip','cabbage','pea_pod'] else ''
    items+=f'    public static final DeferredHolder<Item, Item> {c.upper()} = ITEMS.register("{c}", () -> new Item(properties(){food}));\n'
for id,cl in [('sickle','SickleItem'),('field_hoe','FieldHoeItem'),('watering_can','WateringCanItem'),('organic_fertilizer','FertilizerItem')]:
    items+=f'    public static final DeferredHolder<Item, Item> {id.upper()} = ITEMS.register("{id}", () -> new org.slavicmyths.farming.{cl}(properties()));\n'
insert('registry/ModItems.java','    public static final DeferredHolder<Item, Item> BIRCH_BARK_SCROLL',items+'\n')
insert('registry/ModBlocks.java','    // Wild plants:',blocks+'\n')
insert('registry/ModBlocks.java','import org.slavicmyths.SlavicMyths;', 'import org.slavicmyths.registry.ModItems;\n')
insert('SlavicMyths.java','        ModLoot.SERIALIZERS.register(bus);','        org.slavicmyths.farming.Farming.FUNCTIONS.register(bus);\n')
insert('registry/ModLoot.java','    private ModLoot()', '    static { SERIALIZERS.register("grass_farming_seeds", () -> org.slavicmyths.farming.GrassSeedsModifier.CODEC); }\n')
insert('item/ItemState.java','    public record RuneState', '    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> WATER_CHARGES = component("watering_can_water", Codec.intRange(0,8));\n')
insert('client/ClientSetup.java','            ItemBlockRenderTypes.setRenderLayer(ModBlocks.FLAX.get()', '            org.slavicmyths.farming.Farming.crops().forEach(crop -> ItemBlockRenderTypes.setRenderLayer(crop, RenderType.cutout()));\n')
p=Path('gradle.properties');s=p.read_text();s=s.replace('mod_version=0.9.10-rc2','mod_version=1.1.0');p.write_text(s)
