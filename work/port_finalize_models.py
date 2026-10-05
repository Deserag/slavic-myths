from pathlib import Path
import re,json
receiver=r'[A-Za-z_]\w*(?:\([^()\n]*\)|\[[^\]\n]*\])?(?:\.[A-Za-z_]\w*(?:\([^()\n]*\)|\[[^\]\n]*\])?)*'
changed=[]
for p in Path('src/main/java/org/slavicmyths/client').glob('*Model.java'):
 s=p.read_text(encoding='utf-8')
 if 'extends EntityModel' not in s:continue
 if 'private final FolkModelGeometry geometry=' in s:
  changed.append(str(p));continue
 old=s
 s=s.replace('com.mojang.blaze3d.matrix.MatrixStack','com.mojang.blaze3d.vertex.PoseStack').replace('com.mojang.blaze3d.vertex.IVertexBuilder','com.mojang.blaze3d.vertex.VertexConsumer')
 s=re.sub(r'\bMatrixStack\b','PoseStack',s);s=re.sub(r'\bIVertexBuilder\b','VertexConsumer',s)
 # A separate builder belongs to each real model, with its unchanged authored atlas dimensions.
 s=re.sub(r'(public (?:final )?class [^{]+\{)',r'\1\n private int texWidth=64,texHeight=32;\n private final FolkModelGeometry geometry=new FolkModelGeometry();',s,count=1)
 s=s.replace('new ModelPart(this)', 'geometry.part(texWidth,texHeight)')
 s=re.sub(r'new ModelPart\(this,\s*', 'geometry.part(texWidth,texHeight,',s)
 s=re.sub('('+receiver+r')\.texOffs\(([^()]+)\)\.addBox\(',r'geometry.box(\1,\2,',s)
 s=re.sub('('+receiver+r')\.addBox\(',r'geometry.box(\1,',s)
 s=re.sub('('+receiver+r')\.addChild\(('+receiver+r')\)',r'geometry.attach(\1,\2)',s)
 # The target Model contract carries one packed ARGB color.
 def signature(m):
  arguments=m.group(1).split(',');assert len(arguments)==8,(p,arguments)
  colors=[a.strip().split()[-1] for a in arguments[4:]]
  oldcolor=r',\s*'+r'\s*,\s*'.join(re.escape(c) for c in colors)
  return 'renderToBuffer('+','.join(arguments[:4])+',int color)'
 sig=re.search(r'renderToBuffer\(([^)]+)\)',s)
 if sig:
  colors=[a.strip().split()[-1] for a in sig.group(1).split(',')[4:]]
  color_pattern=r',\s*'+r'\s*,\s*'.join(re.escape(c) for c in colors)
  s=re.sub(color_pattern+r'\*opacity\)',',FolkModelGeometry.withOpacity(color,opacity))',s)
  s=re.sub(color_pattern+r'\)',',color)',s)
  s=re.sub(r'renderToBuffer\(([^)]+)\)',signature,s,count=1)
 if 'new ModelPart(this' in s or '.texOffs(' in s or '.addChild(' in s or '.addBox(' in s:raise RuntimeError('Incomplete geometry transform '+str(p))
 p.write_text(s,encoding='utf-8');changed.append(str(p))
Path('docs/port/finalize-model-migration.json').write_text(json.dumps({'scope':'Geometry construction/render signature only; entity callbacks/renderers still require full compilation and MANUAL visual QA','files':changed},indent=2),encoding='utf-8')
print('Authored model geometry migrated:',len(changed))
