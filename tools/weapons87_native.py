"""Generate offline factories from actual registrations; never mutate production source."""
from pathlib import Path
import re,json
R=Path(__file__).resolve().parents[1]
out=R/'tools/weapons87-offline';out.mkdir(exist_ok=True)
ids=[l.split('\t')[0] for l in (R/'src/main/resources/assets/slavicmyths/creative_catalog.tsv').read_text().splitlines() if '\tweapons\t' in l]
source=(R/'src/main/java/org/slavicmyths/registry/ModItems.java').read_text()
rare=(R/'src/main/java/org/slavicmyths/combat/RareWeapons.java').read_text()
def expression(text,id):
    m=re.search(r'register\("'+re.escape(id)+r'",\s*\(\)\s*->\s*',text)
    if not m:return None
    start=m.end();depth=0;string=False
    for i in range(start,len(text)):
        c=text[i]
        if c=='"' and text[i-1]!='\\':string=not string
        if string:continue
        if c=='(':depth+=1
        if c==')':
            if depth==0:return text[start:i]
            depth-=1
    raise ValueError(id)
cases=[];provenance=[]
for id in ids:
    expr=expression(source,id) or expression(rare,id)
    origin='ModItems.java' if expression(source,id) else 'RareWeapons.java'
    if not expr:
        fixed={'likho_staff':'new org.slavicmyths.combat.LikhoStaffItem()','morozko_staff':'new org.slavicmyths.morozko.FrostStaff()','poloz_whip':'new org.slavicmyths.poloz.PolozWhip()','karachun_scythe':'new org.slavicmyths.karachun.NightScythe()','serpent_crossbow':'new org.slavicmyths.gorynych.SerpentCrossbow(false)','three_breath_crossbow':'new org.slavicmyths.gorynych.SerpentCrossbow(true)','finist_bow':'new org.slavicmyths.finist.FinistBow(new Item.Properties().durability(640).rarity(Rarity.RARE))','trophy_kalin_sabre':'new SwordItem(Tiers.DIAMOND,new Item.Properties().attributes(SwordItem.createAttributes(Tiers.DIAMOND,5,-2.7F)).rarity(Rarity.RARE))','short_bow':'new org.slavicmyths.combat.FieldBowItem(false,new Item.Properties().durability(300))','heavy_bow':'new org.slavicmyths.combat.FieldBowItem(true,new Item.Properties().durability(500))'}
        expr=fixed.get(id);origin='dedicated registration/WeaponCatalog.java'
    if not expr:
        material,kind=id.split('_',1)
        tier={'silver':'ModGear.SILVER','perunite':'ModGear.PERUNITE','gold':'Tiers.GOLD'}.get(material,'Tiers.'+material.upper())
        expr={'spear':f'new org.slavicmyths.item.SpearItem({tier},new Item.Properties())','flail':f'new org.slavicmyths.combat.FlailItem({tier},new Item.Properties())','sulitsa':f'new org.slavicmyths.combat.ThrowingWeaponItem({tier},false,new Item.Properties())','throwing_knife':f'new org.slavicmyths.combat.ThrowingWeaponItem({tier},true,new Item.Properties())'}[kind]
        origin='WeaponCatalog.java material loop'
    expr=expr.replace('properties()','new Item.Properties()').replace('new MaceItem(','new org.slavicmyths.combat.MaceItem(')
    cases.append(f'case "{id}" -> {expr};')
    provenance.append({'id':id,'origin':origin,'factory':expr})
java='''package org.slavicmyths.verify;
import java.nio.file.*;import java.util.*;import com.google.gson.*;
import net.minecraft.world.item.*;import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ai.attributes.*;import org.slavicmyths.item.ModGear;
public final class Weapons87Native {
static Item factory(String id){return switch(id){FACTORIES default -> throw new IllegalArgumentException(id);};}
public static void main(String[] args)throws Exception{
net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());
net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
// Isolated offline fixture only: no loader exists to open the item registration phase.
var itemRegistry=(net.minecraft.core.MappedRegistry<Item>)net.minecraft.core.registries.BuiltInRegistries.ITEM;itemRegistry.unfreeze();
var intrusive=net.minecraft.core.MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");intrusive.setAccessible(true);intrusive.set(itemRegistry,new IdentityHashMap<>());
var items=new LinkedHashMap<String,Item>();
for(String line:Files.readAllLines(Path.of("src/main/resources/assets/slavicmyths/creative_catalog.tsv"))){
 if(!line.contains("\\tweapons\\t"))continue;String id=line.split("\\t")[0];Item item=factory(id);
 net.minecraft.core.Registry.register(itemRegistry,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths",id),item);items.put(id,item);
}
boolean after=Arrays.asList(args).contains("--after");
var beforeComponents=new LinkedHashMap<String,net.minecraft.core.component.DataComponentMap>();items.forEach((id,item)->beforeComponents.put(id,item.components()));
if(after){
 var phase=net.neoforged.neoforge.internal.RegistrationEvents.class.getDeclaredField("canModifyComponents");phase.setAccessible(true);phase.setBoolean(null,true);
 try{org.slavicmyths.combat.WeaponDefaults.modify(new net.neoforged.neoforge.event.ModifyDefaultComponentsEvent());}finally{phase.setBoolean(null,false);}
 for(var entry:items.entrySet()){
  String id=entry.getKey();var old=beforeComponents.get(id);var current=entry.getValue().components();
  for(var component:old.keySet())if(component!=DataComponents.MAX_DAMAGE&&!Objects.equals(old.get(component),current.get(component)))throw new AssertionError("Unapproved component changed: "+id+component);
  int expected=org.slavicmyths.combat.WeaponDefaults.DURABILITY.getOrDefault(id,Optional.ofNullable(old.get(DataComponents.MAX_DAMAGE)).orElse(0));
  if(Optional.ofNullable(current.get(DataComponents.MAX_DAMAGE)).orElse(0)!=expected)throw new AssertionError("Wrong approved durability "+id);
 }
}
var rows=new JsonArray();
for(String line:Files.readAllLines(Path.of("src/main/resources/assets/slavicmyths/creative_catalog.tsv"))){
 if(!line.contains("\\tweapons\\t"))continue;String id=line.split("\\t")[0];Item item=items.get(id);
 var row=new JsonObject();row.addProperty("id",id);row.addProperty("class",item.getClass().getName());
 var attrs=item.components().get(DataComponents.ATTRIBUTE_MODIFIERS);double damage=1,speed=4,reach=3;
 var raw=new JsonArray();if(attrs!=null)for(var e:attrs.modifiers()){
 var a=new JsonObject();a.addProperty("attribute",e.attribute().getRegisteredName());a.addProperty("amount",e.modifier().amount());a.addProperty("operation",e.modifier().operation().toString());a.addProperty("slot",e.slot().toString());raw.add(a);
 if(e.modifier().operation()==AttributeModifier.Operation.ADD_VALUE){if(e.attribute().equals(Attributes.ATTACK_DAMAGE))damage+=e.modifier().amount();if(e.attribute().equals(Attributes.ATTACK_SPEED))speed+=e.modifier().amount();if(e.attribute().equals(Attributes.ENTITY_INTERACTION_RANGE))reach+=e.modifier().amount();}}
 row.add("modifiers",raw);row.addProperty("attack_damage_total",damage);row.addProperty("attack_speed_total",speed);row.addProperty("reach_total",reach);row.addProperty("unarmored_full_charge_melee_dps_ceiling",damage*speed);
 row.addProperty("max_damage",Optional.ofNullable(item.components().get(DataComponents.MAX_DAMAGE)).orElse(0));
 row.addProperty("measurement","native production constructors/components; no entities/world/client; not measured combat DPS");rows.add(row);
}
if(rows.size()!=68)throw new AssertionError(rows.size());
Files.writeString(Path.of("docs/implementation/weapons87/"+(after?"native-attributes-after.json":"native-attributes.json")),new GsonBuilder().setPrettyPrinting().create().toJson(rows));
System.out.println("WEAPONS87_NATIVE: 68 production constructor attributes exported; no game launches");
}}
'''.replace('FACTORIES','\n'.join(cases))
(out/'Weapons87Native.java').write_text(java,encoding='utf-8')
(R/'docs/implementation/weapons87/native-factory-provenance.json').write_text(json.dumps(provenance,indent=2),encoding='utf-8')
print('Generated 68 offline factories, source provenance recorded')
