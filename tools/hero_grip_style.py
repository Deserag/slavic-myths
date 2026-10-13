"""Production grip corrections, matching the approved arm/forward-weapon proposals.
Silhouettes: compact iron mace on 12px wooden haft; continuous curved 11px steel
saber with gold guard; long wood/steel spear; straight 19px steel/gold sword.
All handles sit in the palm, weapon direction follows the forearm joint.
"""
def apply(r):
 changes={'ilya_muromets':(0,0,'arm'),'alyosha_popovich':(2,3,'shoulder'),'dobrynya_nikitich':(.5,2,'arm'),'finist':(.5,.5,'arm')}
 if r.name not in changes:return
 upper,lower,upper_name=changes[r.name]
 def descendants(root):
  return [n for n in r.bones if n==root or ischild(n,root)]
 def ischild(n,root):
  while r.bones[n]['parent']:
   n=r.bones[n]['parent']
   if n==root:return True
  return False
 for side in ['right','left']:
  arm=side+'_'+upper_name;hand=side+'_hand'
  for b in r.bones[arm]['boxes']:
   if b['size'][1]>=4:b['size'][1]+=upper;b['center'][1]-=upper/2
  branch=descendants(hand)
  for n in branch:r.bones[n]['pos'][1]-=upper
  for b in r.boxes:
   if b['bone'] in branch:b['center'][1]-=upper
  # Forearm extends below elbow; small palm/bracelet boxes follow its lower end.
  fore=max(r.bones[hand]['boxes'],key=lambda b:b['size'][1]);fore['size'][1]+=lower;fore['center'][1]-=lower/2
  for b in r.boxes:
   if b['bone'] in branch and b is not fore:b['center'][1]-=lower
  for n in branch:
   if n!=hand:r.bones[n]['pos'][1]-=lower
 weapon={'ilya_muromets':'mace','alyosha_popovich':'saber','dobrynya_nikitich':'spear','finist':'weapon'}[r.name]
 hand=r.bones['right_hand'];x=hand['pos'][0];z=hand['pos'][2];palm=min(b['center'][1]-b['size'][1]/2 for b in hand['boxes'])+1.5
 old=r.bones[weapon];oldpos=old['pos'][:];old['pos']=[x,palm,z]
 def box(cx,cy,cz,w,h,d,mat):r.box(weapon,cx,cy-h/2,cz,w,h,d,mat)
 if weapon=='spear':
  # Grip is fixed; flip the steel end into the same forward direction as other weapons.
  for b in old['boxes']:
   b['center'][0]+=x-oldpos[0];b['center'][1]=palm-(b['center'][1]-oldpos[1]);b['center'][2]+=z-oldpos[2]
 else:
  r.boxes[:]=[b for b in r.boxes if b['bone']!=weapon];old['boxes'].clear()
  if weapon=='mace':
   box(x,palm-4,z,1.6,12,1.6,'wood');box(x,palm-9.8,z,6.8,4.4,6.8,'steel');box(x,palm-7.1,z,5,1.2,5,'steel');box(x,palm-12.6,z,5,1.2,5,'steel');box(x,palm+2,z,2,1,2,'gold')
  elif weapon=='saber':
   box(x,palm,z,1.4,3.5,1.4,'wood');box(x,palm-2.15,z,3.6,.8,1.6,'gold');box(x,palm+2.1,z,1.7,.7,1.7,'gold')
   for i in range(7):box(x+.025*i*i,palm-3.1-i*1.5,z,1.4 if i<5 else .9 if i==5 else .35,1.55,.6,'steel')
  else:
   box(x,palm,z,1.4,4,1.4,'navy');box(x,palm-2.25,z,5.6,.8,1.8,'gold');box(x,palm+2.3,z,1.8,.7,1.8,'gold');box(x,palm-11.95,z,1.7,19,.7,'steel');box(x,palm-22.1,z,.65,1.3,.6,'steel')
