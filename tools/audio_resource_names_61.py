"""Explicit Minecraft resource names for the licensed recorded replacements."""
import copy
PREFIXES=('voice/','kurgan/recorded/','foley/','boss/tugarin/foley/','recorded61/')
def qualified(events):
 result=copy.deepcopy(events)
 for definition in result.values():
  for i,entry in enumerate(definition.get('sounds',[])):
   name=entry if isinstance(entry,str) else entry['name']
   if ':' not in name and name.startswith(PREFIXES):
    if isinstance(entry,str):definition['sounds'][i]='slavicmyths:'+name
    else:entry['name']='slavicmyths:'+name
 return result
