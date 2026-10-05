from pathlib import Path
import re

def args_at(s,start):
 i=start;depth=1;quote=False;escape=False;args=[];mark=start
 while depth:
  c=s[i]
  if quote:
   if escape:escape=False
   elif c=='\\':escape=True
   elif c=='"':quote=False
  elif c=='"':quote=True
  elif c in '([{':depth+=1
  elif c in ')]}':depth-=1
  elif c==',' and depth==1:args.append(s[mark:i]);mark=i+1
  i+=1
 args.append(s[mark:i-1]);return args,i
for p in Path('src/main/java/org/slavicmyths/client').glob('*Screen.java'):
 s=p.read_text(encoding='utf-8')
 if 'extends AbstractContainerScreen' not in s and 'extends Screen' not in s:continue
 s=s.replace('import com.mojang.blaze3d.vertex.PoseStack;','import net.minecraft.client.gui.GuiGraphics;').replace('PoseStack ','GuiGraphics ').replace('net.minecraft.client.gui.widget.button.Button','net.minecraft.client.gui.components.Button').replace('net.minecraft.inventory.container.Slot','net.minecraft.world.inventory.Slot').replace('net.minecraft.client.gui.screen.inventory.InventoryScreen','net.minecraft.client.gui.screens.inventory.InventoryScreen').replace('TranslationTextComponent','MutableComponent')
 s=s.replace('buttons.clear();children.clear();','clearWidgets();').replace('buttons.remove(b);children.remove(b);','removeWidget(b);').replace('addButton(','addRenderableWidget(').replace('b.x','b.getX()').replace('b.y','b.getY()')
 s=s.replace('@Override public void tick(){super.tick();','@Override protected void containerTick(){super.containerTick();')
 # Preserve every button's authored bounds and action, using the target builder.
 pos=0
 while m:=re.search(r'new Button\(',s[pos:]):
  begin=pos+m.start();args,end=args_at(s,pos+m.end());assert len(args)==6,(p,args)
  x,y,w,h,label,action=args;replacement='Button.builder('+label+','+action+').bounds('+','.join([x,y,w,h])+').build()';s=s[:begin]+replacement+s[end:];pos=begin+len(replacement)
 # Font calls now belong to GuiGraphics; direct draws preserve no-shadow behavior.
 pos=0
 while m:=re.search(r'font\.draw\(',s[pos:]):
  begin=pos+m.start();args,end=args_at(s,pos+m.end());assert len(args)==5,(p,args)
  graphics,*rest=args;replacement=graphics+'.drawString(font,'+','.join(rest)+',false)';s=s[:begin]+replacement+s[end:];pos=begin+len(replacement)
 s=re.sub(r'(?<![\w.])fill\((\w+),',r'\1.fill(',s)
 s=re.sub(r'(?<![\w.])drawCenteredString\((\w+),',r'\1.drawCenteredString(',s)
 s=re.sub(r'(?<![\w.])drawString\((\w+),',r'\1.drawString(',s)
 s=re.sub(r'(?<![\w.])renderComponentTooltip\((\w+),',r'\1.renderComponentTooltip(font,',s)
 # Four-argument text/item tooltip methods moved; three-argument container tooltip stays.
 pos=0
 while m:=re.search(r'(?<![\w.])renderTooltip\(',s[pos:]):
  begin=pos+m.start();args,end=args_at(s,pos+m.end())
  if len(args)==4:
   graphics,*rest=args;replacement=graphics+'.renderTooltip(font,'+','.join(rest)+')';s=s[:begin]+replacement+s[end:];pos=begin+len(replacement)
  else:pos=end
 if 'extends AbstractContainerScreen' in s:s=re.sub(r'renderBackground\(\w+\);','',s)
 else:s=s.replace('renderBackground(pose);','renderBackground(pose,mouseX,mouseY,partial);')
 if p.name=='YagaScreen.java':
  s=s.replace('private void icon(ItemStack s,int x,int y){itemRenderer.renderAndDecorateItem(s,leftPos+x,topPos+y);itemRenderer.renderGuiItemDecorations(font,s,leftPos+x,topPos+y);}','private void icon(GuiGraphics m,ItemStack s,int x,int y){m.renderItem(s,x,y);m.renderItemDecorations(font,s,x,y);}')
  s=re.sub(r'(?<!void )\bicon\((?!GuiGraphics)',r'icon(m,',s)
  s=s.replace('minecraft.getTextureManager().bind(BG);blit(m,','m.blit(BG,')
  s=s.replace('InventoryScreen.renderEntityInInventory(leftPos+54,topPos+104,43,leftPos+54-mx,topPos+72-my,(BabaYaga)e)','preview(m,mx,my,(BabaYaga)e)')
  s=s.replace(' @Override public boolean mouseClicked', ''' private void preview(GuiGraphics graphics,int mouseX,int mouseY,BabaYaga entity){
  float horizontal=(float)Math.atan((leftPos+54-mouseX)/40F),vertical=(float)Math.atan((topPos+72-mouseY)/40F);
  var pitch=new org.joml.Quaternionf().rotateX(vertical*20*(float)Math.PI/180);
  var rotation=new org.joml.Quaternionf().rotateZ((float)Math.PI).mul(pitch);
  float body=entity.yBodyRot,yaw=entity.getYRot(),oldPitch=entity.getXRot(),head=entity.yHeadRot,oldHead=entity.yHeadRotO;
  try{entity.yBodyRot=180+horizontal*20;entity.setYRot(180+horizontal*40);entity.setXRot(-vertical*20);entity.yHeadRot=entity.yHeadRotO=entity.getYRot();
   InventoryScreen.renderEntityInInventory(graphics,leftPos+54,topPos+104,43,new org.joml.Vector3f(),rotation,pitch,entity);
  }finally{entity.yBodyRot=body;entity.setYRot(yaw);entity.setXRot(oldPitch);entity.yHeadRot=head;entity.yHeadRotO=oldHead;}
 }
 @Override public boolean mouseClicked''')
 p.write_text(s,encoding='utf-8')
print('Migrated eight screens to GuiGraphics, original button geometry/actions retained; visual QA still manual.')
