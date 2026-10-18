"""Four original cuboid silhouettes; generated Java and atlases share one manifest.

Weapons: Tugarin uses two 24-unit crescent cleavers, 8-unit wide steel blades,
bronze crossguards and dark wood grips. Nightingale uses two 11-unit hooked
steel knives, 3-unit blades, leather grips. They have distinct proportions.
"""
from pathlib import Path
import json,math,sys,io
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
MODELS = []
PALETTE = [(190,135,100),(104,27,40),(42,26,22),(25,18,18),
           (121,134,139),(205,214,209),(128,94,44),(27,53,34),
           (36,30,35),(95,39,43),(228,121,37),(44,112,117)]

def model(name):
    parts=[]
    def add(n,p='',xyz=(0,0,0),box=None,mat=0,rot=(0,0,0)):
        parts.append(dict(name=n,parent=p,pivot=list(xyz),box=box,material=mat,rotation=list(rot)))
    def box(n,p,x,y,z,w,h,d,mat=0,rot=(0,0,0)):
        add(n,p,box=[x,y,z,w,h,d],mat=mat,rot=rot)
    add('root',xyz=(0,24,0));add('body','root',xyz=(0,-12 if name.startswith('tugarin') else -16,0))
    dragon=name=='tugarin_phase2';fat=name=='tugarin_phase1';comic=name=='nightingale_phase2'
    if fat:
        box('chest','body',-8.5,-17,-4.5,17,16,9)
        add('belly','body')
        for band in range(7):
            factor=math.sqrt(1-((band-3)/4.1)**2)
            width=20*factor;depth=13*factor
            box('bellyBand'+str(band),'belly',-width/2,-12+band*2,-3.5-depth/2,width,2.15,depth)

        box('belt','body',-10.1,0,-10.1,20.2,2.2,13.2,2)
        box('buckle','body',-2,0,-10.5,4,2.4,.8,6)
        for sign,side in [(1,'L'),(-1,'R')]:
            box('openVest'+side,'body',-9 if sign<0 else 5.5,-17,-5,3.5,12,10,1)
        add('head','body',xyz=(0,-19,0));box('face','head',-5,-8,-5,10,10,10)
        box('hairCap','head',-5.2,-8.4,-3,10.4,2,8.4,3)
        box('nose','head',-1.7,-3,-6.8,3.4,3,2)
        box('jaw','head',-4,-.5,-5.3,8,2,7)
    elif dragon:
        box('scaledTorso','body',-9,-18,-5,18,19,11,8)
        box('belly','body',-7.5,-13,-6,15,14,2,9)
        box('neck','body',-5,-23,-3,10,9,8,8)
        add('head','body',xyz=(0,-23,-2));box('skull','head',-6,-6,-4,12,10,12,8)
        box('snout','head',-5,-2,-13,10,5,10,9)
        add('jaw','head',xyz=(0,3,-3));box('lowerJaw','jaw',-4.5,0,-10,9,2.5,13,8)
        box('mouthGlow','head',-3.7,2,-12.8,7.4,1.8,.4,10)
        for sign,side in [(1,'L'),(-1,'R')]:
            box('hornBase'+side,'head',sign*4-1,-10,1,2,6,3,6,rot=(.3,0,sign*.25))
            box('hornTip'+side,'head',sign*5-0.7,-13,2,1.4,5,2,5,rot=(.4,0,sign*.3))
            for i in range(3):box('fang'+side+str(i),'head',sign*3.5-.4,2,-11+i*2,.8,2.4,.8,5)
            add('wing'+side,'body',xyz=(sign*7,-16,4))
            box('wingUpper'+side,'wing'+side,0 if sign>0 else -14,-1,0,14,2,2,8,rot=(0,0,-sign*.18))
            add('wingLowerJoint'+side,'wing'+side,xyz=(sign*13,-2,0))
            box('wingLower'+side,'wingLowerJoint'+side,0 if sign>0 else -18,0,0,18,1.6,1.6,8,rot=(0,0,sign*.22))
            for i in range(5):
                x=i*6 if sign>0 else -(i+1)*6
                box('membrane'+side+str(i),'wing'+side,x,1+i*.5,.6,6,16-i*2,.45,9)
                box('wingRib'+side+str(i),'wing'+side,x,0,.2,.8,17-i*2,1,8)
        parent='body'
        for i,(w,d) in enumerate([(8,11),(6,11),(4,10),(2.5,9)]):
            add('tail'+str(i),parent,xyz=(0,9 if i==0 else 0,7 if i==0 else 9),rot=(.08 if i==0 else 0,.45,0));box('tailScale'+str(i),'tail'+str(i),-w/2,-w/2,0,w,w,d,8)
            box('tailSpine'+str(i),'tail'+str(i),-.6,-w/2-2,3,1.2,3,2,6);parent='tail'+str(i)
    else:
        width=8 if comic else 11;depth=4.5 if comic else 6
        box('torso','body',-width/2,-13 if comic else -14,-depth/2,width,13 if comic else 14,depth,1 if comic else 7)
        box('belt','body',-width/2-.3,-1,-depth/2-.3,width+.6,2,depth+.6,2)
        box('buckle','body',-1.2,-.8,-depth/2-.8,2.4,1.6,.7,6)
        add('head','body',xyz=(0,-15,0));box('face','head',-4.4,-7,-4,8.8,8.5,8)
        box('nose','head',-.9,-2.8,-5.8,1.8,2.7,2)
        box('jaw','head',-3.5,0,-4.2,7,1.7,6)
        box('hairCap','head',-4.5,-7.4,-3,9,2,7.3,3)
        if comic:
            add('topknot','head',xyz=(0,-7,1),rot=(0,0,.3));box('topknotStem','topknot',-.9,-8,-1,1.8,8,2,3)
            box('topknotTip','topknot',-.5,-10,-1,4,2,2,3,rot=(0,0,-.5))
        else:
            box('beard','jaw',-3.6,.5,-4.5,7.2,3.5,2,3)
            for sign,side in [(1,'L'),(-1,'R')]:
                box('shoulderMantle'+side,'body',0 if sign>0 else -8,-14,-3.8,8,3,8,7)
            add('cloakSideL','body',xyz=(6.5,-12,-1),rot=(0,0,-.1));box('cloakSideClothL','cloakSideL',0,0,-2,2.5,21,2,7)
            add('cloakSideR','body',xyz=(-6.5,-12,-1),rot=(0,0,.1));box('cloakSideClothR','cloakSideR',-2.5,0,-2,2.5,18,2,7)
            for i in range(7):
                add('cloakStrip'+str(i),'body',xyz=(-7+i*2,-12,3),rot=(.08,0,(i-3)*.025))
                box('cloakCloth'+str(i),'cloakStrip'+str(i),-1,0,0,2.2,18+(i%3)*2,.8,7)
            for sign in [-1,1]:box('pouch'+str(sign),'body',sign*5-1,-1,-4,2.6,4,2.5,2)
    for sign,side in [(1,'L'),(-1,'R')]:
        if not dragon:
            add('moustache'+side,'head',xyz=(sign*1.3,-.5,-5.6),rot=(0,0,-sign*.16))
            box('whisker'+side,'moustache'+side,0 if sign>0 else -(8 if comic else 5),0,0,8 if comic else 5,1.2,1,3)
            box('sideHair'+side,'head',sign*(5 if fat else 4)-.8,-5,2,1.6,9 if fat else 6,3,3)
        box('eye'+side,'head',sign*2.5-.6,-4,-5.15 if fat else -4.15,1.2,.8,.4,5)
        box('pupil'+side,'head',sign*2.5-.2,-4,-5.6 if fat else -4.6,.4,.8,.2,3)
        shoulder=7.5 if fat else 9 if dragon else 5 if comic else 7
        aw=6.5 if fat else 5.5 if dragon else 3.5 if comic else 4.5
        length=8 if fat or dragon else 9 if comic else 7.5
        add('arm'+side,'body',xyz=(sign*shoulder,-15 if fat or dragon else -12,0),rot=(0,0,-sign*.1))
        box('upperArm'+side,'arm'+side,-aw/2,-1,-aw/2,aw,length,aw,8 if dragon else 11 if comic else 0)
        add('fore'+side,'arm'+side,xyz=(0,length-1,0));box('forearm'+side,'fore'+side,-aw/2,0,-aw/2,aw,length,aw,8 if dragon else 11 if comic else 0)
        box('cuff'+side,'fore'+side,-aw/2-.2,length-3,-aw/2-.2,aw+.4,2.5,aw+.4,4)
        if dragon:
            for i in range(3):box('claw'+side+str(i),'fore'+side,-2+i*1.5,length-1,-3,.6,3,1.5,5)
        else:
            add('blade'+side,'fore'+side,xyz=(0,length-1,-2),rot=(-.45,0,sign*.3))
            box('grip'+side,'blade'+side,-.7,-1,-.7,1.4,4,1.4,2)
            box('guard'+side,'blade'+side,-3,2,-1,6,1,2,6)
            bl=15 if fat else 11;bw=5 if fat else 3
            for i,curve in enumerate([0,2,3,2,-.2] if fat else [0,1,2,1.5,0]):
                width=[5,6,5,3,1.5][i] if fat else [3,3.8,3,2,1][i]
                center=sign*curve;left=center-width/2
                box('bladeCurve'+side+str(i),'blade'+side,left,3+i*bl/5,-.6,width,bl/5+.2,1.2,4)
                box('bladeEdge'+side+str(i),'blade'+side,left if sign<0 else left+width-.4,3+i*bl/5,-.75,.4,bl/5+.2,1.5,5)
        legwidth=7.5 if fat else 6 if dragon else 3.5 if comic else 4.5
        leglength=12 if fat or dragon else 16
        add('leg'+side,'root',xyz=(sign*(4.5 if fat else 4 if dragon else 2.6),-leglength,0))
        box('pants'+side,'leg'+side,-legwidth/2,0,-legwidth/2,legwidth,leglength-3,legwidth,8 if dragon else 1 if fat or comic else 2)
        box('boot'+side,'leg'+side,-legwidth/2-.2,leglength-5,-legwidth/2-1,legwidth+.4,5,legwidth+2,8 if dragon else 2)
    # Physical geometry, not renderer/entity scaling. Feet stay at model Y=24.
    by={p['name']:p for p in parts}
    if name.startswith('nightingale'):
        # #57: stocky forest robber versus lean, ribbed, quick whistle form.
        upper=6.5 if not comic else 7.2;lower=3.0;sole=1.5 if not comic else 1.6
        waist=upper+lower+sole
        add('pelvis','root',xyz=(0,-waist,0))
        by['body']['parent']='pelvis';by['body']['pivot']=[0,0,0]
        by['torso']['box']=[-4.1,-4.5,-2.1,8.2,4.5,4.2] if not comic else [-3.2,-3.6,-1.9,6.4,3.6,3.8]
        box('ribcage','body',-4.9 if not comic else -4.2,-10.5 if not comic else -10,-2.7 if not comic else -2.5,9.8 if not comic else 8.4,6.4 if not comic else 6.8,5.4 if not comic else 5,7 if not comic else 1)
        box('neckWrap','body',-2,-11,-1.8,4,2.4,3.6,7 if not comic else 11)
        by['belt']['box']=[-4.5 if not comic else -3.6,-1,-2.5,9 if not comic else 7.2,1.8,5]
        by['buckle']['box']=[-1,-.8,-2.9,2,1.3,.7]
        by['head']['pivot']=[0,-10 if not comic else -9.2,0]
        by['face']['box']=[-3.7,-6.3,-3.6,7.4,7.5,7.2] if not comic else [-3.4,-6,-3.4,6.8,7.2,6.8]
        by['nose']['box']=[-.8,-3.1,-4.7,1.6,2.5,1.6]
        by['jaw']['box']=[-3.1,0,-3.5,6.2,1.3,5.7]
        by['hairCap']['box']=[-3.85 if not comic else -3.55,-6.6 if not comic else -6.3,-2.8,7.7 if not comic else 7.1,1.7,6.5]
        if not comic:by['beard']['box']=[-3.2,.6,-3.7,6.4,2.5,1.6]
        box('lipRim','head',-.8,.1,-4.2,1.6,1.1,.35,0)
        box('whistleLips','head',-.35,.4,-4.45,.7,.45,.12,3)
        for sign,side in [(1,'L'),(-1,'R')]:
            by['eye'+side]['box']=[sign*1.9-.55,-3.7,-3.85 if not comic else -3.65,1.1,.8,.4]
            by['pupil'+side]['box']=[sign*1.9-.2,-3.7,-4.05 if not comic else -3.85,.4,.8,.22]
            box('brow'+side,'head',sign*1.9-1,-4.4,-4.05,2.1,.6,.6,3,rot=(0,0,-sign*(.14 if not comic else .28)))
            by['sideHair'+side]['box']=[sign*3.5-.55,-4.5,1.6,1.1,6 if not comic else 5,2.4]
            by['moustache'+side]['pivot']=[sign*1.1,-.3,-4.05]
            whisker=3.6 if not comic else 4.7
            by['whisker'+side]['box']=[0 if sign>0 else -whisker,0,0,whisker,.9,.8]
            by['arm'+side]['pivot']=[sign*(4.85 if not comic else 5),-9.1 if not comic else -9.4,0]
            by['arm'+side]['rotation']=[0,0,sign*.04]
            aw=3.6 if not comic else 3.0;u=5.5 if not comic else 5.8;fore=5 if not comic else 5.5
            by['upperArm'+side]['box']=[-aw/2,-.6,-aw/2,aw,u+.4,aw]
            by['fore'+side]['pivot']=[0,u,0]
            by['forearm'+side]['box']=[-aw/2,0,-aw/2,aw,fore,aw]
            by['cuff'+side]['box']=[-aw/2-.15,fore-1.8,-aw/2-.15,aw+.3,1.6,aw+.3]
            add('wrist'+side,'fore'+side,xyz=(0,fore-.2,-1.3))
            box('palm'+side,'wrist'+side,-1.6,-.8,-1.7,3.2,3.4,3.4,2)
            box('thumb'+side,'wrist'+side,sign*1.4-.45,0,-2,.9,1.6,1.4,0 if not comic else 11)
            by['blade'+side]['parent']='wrist'+side;by['blade'+side]['pivot']=[0,0,0];by['blade'+side]['rotation']=[-1,0,sign*.15]
            by['grip'+side]['box']=[-.6,-1,-.6,1.2,3.2,1.2]
            by['guard'+side]['box']=[-1.6,2,-.9,3.2,.8,1.8]
            # Short hooked steel knife: 8-unit blade, narrow guard, leather grip.
            for i in range(5):
                by['bladeCurve'+side+str(i)]['box'][1]=2.8+i*1.6;by['bladeCurve'+side+str(i)]['box'][4]=1.8
                by['bladeEdge'+side+str(i)]['box'][1]=2.8+i*1.6;by['bladeEdge'+side+str(i)]['box'][4]=1.8
            by['leg'+side]['parent']='pelvis';by['leg'+side]['pivot']=[sign*(2.5 if not comic else 2.2),0,0]
            by['pants'+side]['box']=[-1.9 if not comic else -1.65,0,-1.9 if not comic else -1.65,3.8 if not comic else 3.3,upper,3.8 if not comic else 3.3]
            add('knee'+side,'leg'+side,xyz=(0,upper,0))
            box('shin'+side,'knee'+side,-1.8 if not comic else -1.5,0,-1.8 if not comic else -1.5,3.6 if not comic else 3,lower,3.6 if not comic else 3,2 if not comic else 1)
            add('ankle'+side,'knee'+side,xyz=(0,lower,0))
            by['boot'+side]['parent']='ankle'+side;by['boot'+side]['box']=[-2.1 if not comic else -1.85,-2,-3 if not comic else -2.8,4.2 if not comic else 3.7,2+sole,5 if not comic else 4.7]
        if not comic:
            for sign,side in [(1,'L'),(-1,'R')]:
                by['shoulderMantle'+side]['box']=[0 if sign>0 else -6.8,-10,-3.2,6.8,2.5,7.4]
                by['cloakSide'+side]['pivot']=[sign*5.4,-9,-1];by['cloakSideCloth'+side]['box']=[0 if sign>0 else -1.4,0,-2,1.4,13,2]
            discard={n for i in range(3,7) for n in ['cloakStrip'+str(i),'cloakCloth'+str(i)]}
            parts[:]=[p for p in parts if p['name'] not in discard]
            for i in range(3):
                by['cloakStrip'+str(i)]['pivot']=[-4+i*4,-9,3]
                by['cloakCloth'+str(i)]['box']=[-2.2,0,0,4.4,13+i%2,.9]
        else:
            for sign,side in [(1,'L'),(-1,'R')]:
                add('coatTail'+side,'body',xyz=(sign*2.7,-1,2),rot=(.08,0,sign*.05))
                box('coatCloth'+side,'coatTail'+side,-1.6,0,0,3.2,8 if sign>0 else 10,.8,11)
        parts[:]=[p for p in parts if p['name'] not in ('topknotStem','topknotTip')]
        if not comic:add('topknot','head',xyz=(0,-6.3,1),rot=(.12,0,.12))
        else:by['topknot']['pivot']=[0,-5.8,1];by['topknot']['rotation']=[.16,0,-.18]
        box('topknotStem','topknot',-1,-1.6,-1,2,2,2,3)
        box('topknotTip','topknot',-1.3,-1.9,.3,2.6,2.2,3.3,3,rot=(-.15,0,-.1))
    if fat:
        # Broad shoulder girdle and deep, round belly above short planted legs.
        by['body']['pivot'][1]=-24
        by['chest']['box']=[-24,-32,-10,48,30,20]
        for band in range(7):
            factor=math.sqrt(1-((band-3)/4.5)**2);w=54*factor;d=30*factor
            by['bellyBand'+str(band)]['box']=[-w/2,-25+band*4,-5-d/2,w,4.2,d]
        by['belt']['box']=[-22,1,-18,44,3,27]
        by['buckle']['box']=[-4,1,-19,8,3.2,1.2]
        by['head']['pivot'][1]=-40
        box('neckMass','body',-10,-42,-7,20,15,14)
        for p in parts:
            if p['parent'] in ('head','moustacheL','moustacheR'):
                p['pivot']=[v*1.65 for v in p['pivot']]
                if p['box']:p['box']=[v*1.65 for v in p['box']]
        for sign,side in [(1,'L'),(-1,'R')]:
            box('brow'+side,'head',sign*4.1-3,-7.8,-8.8,6,1.5,1.4,3,rot=(0,0,-sign*.18))
            box('ear'+side,'head',sign*8-1,-6,-2,2,5,3)
        box('snarlMouth','head',-5.8,.2,-8.9,11.6,3.6,.9,3)
        for tooth in range(6):box('snarlTooth'+str(tooth),'head',-5.2+tooth*1.75,.35,-9.5,1.4,1.6,.5,5)
        for sign,side in [(1,'L'),(-1,'R')]:
            by['openVest'+side]['box']=[-25 if sign<0 else 16,-32,-11,9,24,23]
            by['arm'+side]['pivot']=[sign*25,-28,0]
            by['upperArm'+side]['box']=[-8,-2,-8,16,16,16]
            by['fore'+side]['pivot']=[0,14,0]
            by['forearm'+side]['box']=[-7,0,-7,14,15,14]
            by['cuff'+side]['box']=[-7.3,10,-7.3,14.6,4,14.6]
            by['blade'+side]['pivot']=[0,14,-3]
            for p in parts:
                if p['parent']=='blade'+side and p['box']:
                    p['box']=[v*(1.6 if j<2 or j in (3,4) else 1.3) for j,v in enumerate(p['box'])]
            by['leg'+side]['pivot']=[sign*10,-24,0]
            by['pants'+side]['box']=[-8,0,-7,16,19,14]
            by['boot'+side]['box']=[-8.4,15,-10,16.8,9,19]
    if fat:
        # #56: authored hip/knee/ankle/wrist pivots, preserving weapon silhouettes.
        add('pelvis','root',xyz=(0,-24,0))
        by['body']['parent']='pelvis';by['body']['pivot']=[0,0,0]
        by['head']['pivot'][1]=-39.5
        for sign,side in [(1,'L'),(-1,'R')]:
            by['leg'+side]['parent']='pelvis';by['leg'+side]['pivot']=[sign*10,0,0]
            by['pants'+side]['box'][4]=13
            add('knee'+side,'leg'+side,xyz=(0,13,0))
            box('shinShell'+side,'knee'+side,-7.6,0,-6.8,15.2,7,13.6,1)
            add('ankle'+side,'knee'+side,xyz=(0,7,0))
            by['boot'+side]['parent']='ankle'+side;by['boot'+side]['box'][1]=-5
            by['arm'+side]['pivot'][0]=sign*22.4
            by['arm'+side]['rotation'][2]=sign*.04
            by['upperArm'+side]['box']=[-5.5,-2,-5.5,11,16,11]
            by['forearm'+side]['box']=[-5,0,-5,10,15,10]
            by['cuff'+side]['box']=[-5.3,10,-5.3,10.6,4,10.6]
            add('wrist'+side,'fore'+side,xyz=(0,14,-3))
            box('gauntlet'+side,'wrist'+side,-4.5,-2,-3.5,9,6,7,4)
            by['blade'+side]['parent']='wrist'+side;by['blade'+side]['pivot']=[0,0,0]
    if dragon:
        # Serpentine trunk, rising articulated neck, rear haunches and swept wings.
        by['body']['pivot']=[0,-25,8]
        by['scaledTorso']['box']=[-14,-14,-24,28,25,43]
        by['belly']['box']=[-11,-10,-25,22,20,3]
        by['neck']['box']=None
        by['neck']['pivot']=[0,-13,-20]
        box('neckLower','neck',-9,-12,-8,18,18,16,8,rot=(.22,0,0))
        add('neckUpper','neck',xyz=(0,-13,-5),rot=(.18,0,0))
        box('neckScales','neckUpper',-7,-14,-7,14,18,14,8)
        box('neckPlate','neckUpper',-6,-13,-8,12,16,2,9)
        by['head']['parent']='neckUpper';by['head']['pivot']=[0,-14,-3]
        by['skull']['box']=[-10,-8,-7,20,14,19]
        by['snout']['box']=[-7,-3,-24,14,7,18]
        by['jaw']['pivot']=[0,4,-6];by['lowerJaw']['box']=[-6.5,0,-18,13,4,21]
        by['mouthGlow']['box']=[-6,3,-24,12,3,.5]
        for p in parts:
            if p['name'].startswith(('horn','fang')):
                p['box']=[v*1.7 for v in p['box']]
        for sign,side in [(1,'L'),(-1,'R')]:
            by['hornBase'+side]['box'][1]=-8.5;by['hornBase'+side]['box'][4]=5.5
            by['hornTip'+side]['box'][1]=-11;by['hornTip'+side]['box'][4]=4
            by['hornBase'+side]['rotation'][0]=-.6;by['hornTip'+side]['rotation'][0]=-.8
            by['eye'+side]['box']=[sign*7-1,-4,-7.5,2,1.4,.5]
            by['pupil'+side]['box']=[sign*7-.3,-4,-8,.6,1.4,.3]
            box('serpentBrow'+side,'head',sign*7-2,-6,-8,4,2,3,8,rot=(0,0,-sign*.2))
            box('nostril'+side,'head',sign*4-1,-2,-24.3,2,1.2,.5,3)
            for tooth in range(4):
                box('longFang'+side+str(tooth),'head',sign*5-.6,3,-23+tooth*4,1.2,4,1.3,5)
            by['wing'+side]['pivot']=[sign*12,-12,5]
            for p in parts:
                if p['parent'] in ('wing'+side,'wingLowerJoint'+side):
                    p['pivot']=[v*1.65 for v in p['pivot']]
                    if p['box']:p['box']=[v*1.65 for v in p['box']]
            by['arm'+side]['pivot']=[sign*13,-7,-12]
            by['arm'+side]['rotation']=[-.3,0,-sign*.3]
            by['upperArm'+side]['box']=[-4,-1,-4,8,11,8]
            by['fore'+side]['pivot']=[0,10,0]
            by['forearm'+side]['box']=[-3.5,0,-3.5,7,14,7]
            by['cuff'+side]['box']=[-4,10,-4,8,4,8];by['cuff'+side]['material']=8
            for i in range(3):by['claw'+side+str(i)]['box']=[-3+i*2.5,13,-6,1.2,5,3]
            by['leg'+side]['pivot']=[sign*10,-19,17]
            by['pants'+side]['box']=[-6,0,-5,12,12,12]
            by['boot'+side]['box']=[-5,10,-10,10,9,15]
            box('hindClaw'+side,'leg'+side,-5,17,-14,10,2,5,5)
        for i in range(4):
            by['tail'+str(i)]['pivot']=[0,-2 if i==0 else 0,22 if i==0 else 16]
            by['tail'+str(i)]['rotation']=[.08,.28,0]
            w=[13,10,7,4][i];by['tailScale'+str(i)]['box']=[-w/2,-w/2,0,w,w,19]
            by['tailSpine'+str(i)]['box']=[-1,-w/2-3,5,2,5,4]
        for i in range(5):box('backSpine'+str(i),'body',-1.5,-19,-16+i*7,3,7,5,6,rot=(.25,0,0))
    ordered=[];pending=list(parts);known=set()
    while pending:
        ready=[p for p in pending if not p['parent'] or p['parent'] in known]
        if not ready:raise ValueError('Unresolved model hierarchy: '+name)
        for p in ready:ordered.append(p);known.add(p['name']);pending.remove(p)
    return dict(name=name,parts=ordered)

def save_png(path,im):
    stream=io.BytesIO();im.save(stream,format='PNG');data=stream.getvalue()
    if path.exists() and path.read_bytes()==data:return
    temporary=path.with_suffix('.png.tmp')
    try:
        temporary.write_bytes(data);temporary.replace(path)
    finally:
        if temporary.exists():temporary.unlink()

def apply(tugarin_only=False,nightingale_only=False):
    models=[model(n) for n in ['tugarin_phase1','tugarin_phase2','nightingale_phase1','nightingale_phase2']]
    target=ROOT/'src/main/resources/assets/slavicmyths/boss_geometry';target.mkdir(exist_ok=True)
    java=['package org.slavicmyths.client;','/** Generated by tools/boss_reimplementation_resources.py; original silhouettes. */','final class BossReworkGeometry {','static void build(BossGeometrySink m,int form){switch(form){']
    def f(v):return str(float(v))+'F'
    for i,m in enumerate(models):
        selected=not(tugarin_only or nightingale_only) or tugarin_only and i<2 or nightingale_only and i>=2
        if selected:(target/(m['name']+'.json')).write_text(json.dumps(m,indent=2)+'\n')
        java.append('case '+str(i)+' -> {')
        for p in m['parts']:
            box='null' if p['box'] is None else 'new float[]{'+','.join(map(f,p['box']))+'}'
            java.append('m.add("'+p['name']+'","'+p['parent']+'",'+','.join(map(f,p['pivot']))+','+box+','+str(p['material'])+','+','.join(map(f,p['rotation']))+');')
        java.append('}')
        if not selected:continue
        im=Image.new('RGB',(1024,1024))
        px=im.load()
        for slot,color in enumerate(PALETTE):
            if i==2:color={7:(43,68,45),2:(61,42,28)}.get(slot,color)
            if i==3:color={0:(173,154,128),1:(99,39,39),11:(48,93,91)}.get(slot,color)
            for y in range(256):
                for x in range(256):
                    delta=((x//3*7+y//3*11+slot*3+i*5)%9)-4
                    if slot in [8,9] and y%6==0:delta-=12
                    if i<2:
                        delta+=((x//5*3+y//4*7+slot)%5-2)*3
                        if slot==1 and (y%12==0 or x%17==0):delta-=10
                        if slot in [4,5,6] and (x+y)%13==0:delta+=12
                    if i>=2:
                        delta+=((x//4*3+y//5*5+slot)%5-2)*3
                        if slot in [1,7,11] and y%13==0:delta-=7
                    px[slot%4*256+x,slot//4*256+y]=tuple(max(0,min(255,c+delta)) for c in color)
        save_png(ROOT/'src/main/resources/assets/slavicmyths/textures/entity'/ (m['name']+'.png'),im)
    java.extend(['default -> throw new IllegalArgumentException("Boss form: "+form);','}}','private BossReworkGeometry(){}','}'])
    (ROOT/'src/main/java/org/slavicmyths/client/BossReworkGeometry.java').write_text('\n'.join(java)+'\n')
    if tugarin_only or nightingale_only:return
    sounds=json.loads((ROOT/'tools/boss_reimplementation_sounds.json').read_text())
    path=ROOT/'src/main/resources/assets/slavicmyths/sounds.json'
    data=json.loads(path.read_text(encoding='utf-8'));data.update({k:{'sounds':[{'name':v,'type':'event'}]} for k,v in sounds.items()});path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    for lang in ['ru_ru','en_us']:
        path=ROOT/f'src/main/resources/assets/slavicmyths/lang/{lang}.json';data=json.loads(path.read_text(encoding='utf-8'))
        data['item.slavicmyths.nightingale_spawn_egg']='\u042f\u0439\u0446\u043e \u043f\u0440\u0438\u0437\u044b\u0432\u0430 \u0421\u043e\u043b\u043e\u0432\u044c\u044f-\u0440\u0430\u0437\u0431\u043e\u0439\u043d\u0438\u043a\u0430' if lang=='ru_ru' else 'Nightingale Spawn Egg'
        data['entity.slavicmyths.nightingale.enraged']='\u0421\u043e\u043b\u043e\u0432\u0435\u0439-\u0440\u0430\u0437\u0431\u043e\u0439\u043d\u0438\u043a \u2014 \u0420\u0430\u0437\u044a\u044f\u0440\u0451\u043d\u043d\u044b\u0439' if lang=='ru_ru' else 'Nightingale - Enraged'
        data['tooltip.slavicmyths.nightingale_dagger']='10 \u0443\u0440\u043e\u043d\u0430. \u0412\u0435\u0442\u0440\u043e\u0432\u043e\u0439 \u0432\u0437\u043c\u0430\u0445: 8 \u0431\u043b\u043e\u043a\u043e\u0432, 7 \u0443\u0440\u043e\u043d\u0430, \u0437\u0430\u0440\u044f\u0434 0,4 \u0441, \u043f\u0435\u0440\u0435\u0437\u0430\u0440\u044f\u0434\u043a\u0430 7 \u0441.' if lang=='ru_ru' else '10 attack damage. Wind slash: 8 blocks, 7 damage, 0.4 s charge, 7 s cooldown.'
        data['tooltip.slavicmyths.nightingale_whistle']='\u0420\u0430\u0434\u0438\u0430\u043b\u044c\u043d\u044b\u0439 \u043f\u043e\u0440\u044b\u0432: 6 \u0431\u043b\u043e\u043a\u043e\u0432, \u043f\u0435\u0440\u0435\u0437\u0430\u0440\u044f\u0434\u043a\u0430 16 \u0441.' if lang=='ru_ru' else 'Radial gust: 6 blocks, 16 s cooldown.'
        path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
if __name__=='__main__':apply('--tugarin-model-only' in sys.argv,'--nightingale-model-only' in sys.argv)
