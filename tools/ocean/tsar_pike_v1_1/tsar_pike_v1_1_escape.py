"""Technical add-on to Tsar Pike V1: release from a jaw hold by damaging the pike.
Uses the ORIGINAL articulated V1 procedural voxel mesh without changing any fish cuboid.
Only poses, a separate 3D target dummy and strike-arm are added. No game AI/Java code.

Run:
  python tsar_pike_v1_1_escape.py --base ../tsar_pike_v1/tsar_pike_v1.py
"""
import argparse
import importlib.util
import os
import sys
import math
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d.art3d import Poly3DCollection
from PIL import Image, ImageDraw, ImageFont

ap = argparse.ArgumentParser()
ap.add_argument('--base',default=os.path.abspath(os.path.join(os.path.dirname(__file__),'..','tsar_pike_v1','tsar_pike_v1.py')))
a = ap.parse_args()
spec = importlib.util.spec_from_file_location('tsar_pike_procedural_base',a.base)
m = importlib.util.module_from_spec(spec)
sys.modules[spec.name] = m
spec.loader.exec_module(m)

OUT=os.path.dirname(__file__)
PALETTE={'body':'#438caa','head':'#e1a57b','hand':'#d7a17d','belt':'#273f4a','dark':'#304c5b','weapon':'#ded7b1'}
# Model pivot for the dummy shoulder inside the snout cavity.
SHOULDER=(-46,-7,-17)

def boxes_mesh(boxes):
    fs=[];cs=[]
    for x,y,z,sx,sy,sz,col in boxes:
        c=np.array((x,y,z),float)
        s=np.array((sx,sy,sz),float)
        for q in m.quad_corners:
            fs.append(c+q*s);cs.append(PALETTE[col])
    return np.asarray(fs),np.asarray(cs,dtype=object)

TORSO,CT=boxes_mesh([
 (-46,0,-8,8,8,9,'head'),
 (-46,0,-17.4,7,8,12,'body'),
 (-46,0,-24,7,8,2.4,'belt'),
 (-48,-1.6,-29,3.2,3.5,9,'dark'),(-44,1.6,-29,3.2,3.5,9,'dark'),
 (-45,7,-20,3,3,10,'body'),(-45,7,-26,3,3,3,'hand')
])
STRIKING_ARM,CA=boxes_mesh([
 (-51.0,-7,-17,10,3.4,3.2,'body'),
 (-56.5,-7,-17,3.5,4.2,4.2,'hand'),
 # A blunt forearm guard, not a weapon texture or boss body part
 (-49,-7,-17,2.4,4.3,4.2,'belt')
])

# Hit times correspond to three full melee strikes. Visible damage is a suggested
# in-game *post-mitigation* budget, not damage applied by this offline demo.
HITS=(0.23,0.44,0.65)
HIT_VALUE=(5,4,5)
BREAK_THRESHOLD=14

def ease(x):
    x=float(np.clip(x,0,1)); return x*x*(3-2*x)

def pulse(t,at,width=0.075):
    return max(0,1-abs(t-at)/width)

def state(t):
    strike=max(pulse(t,h) for h in HITS)
    strikes_done=sum(t>=h for h in HITS)
    effective=sum(d for h,d in zip(HITS,HIT_VALUE) if t>=h)
    progress=ease((t-.71)/.20)
    # Jaw hinge and head motion are the same as in the V1 rig.
    s=m.motion('shake',t)
    s['jaw']=.48+.26*progress
    s['headpitch']=.07*math.sin(8*math.pi*t)*(1-progress)
    s['turn']=.10*math.sin(9*math.pi*t)*(1-progress)
    s['wave']=.06*math.sin(2*math.pi*t)
    s['tail']=.10*math.sin(2*math.pi*t-.7)
    s['prey']=False
    # Target does not snap into the fish's center. It leaves toward the open snout.
    exit_offset=-38*progress
    arm_angle=.90*ease(strike)
    return s, effective, strikes_done, strike, exit_offset, arm_angle, progress

def make_frame(t):
    s,damage,hits,raise_arm,exit_offset,arm_angle,releasing=state(t)
    fig=plt.figure(figsize=(8.5,5.4),dpi=72)
    ax=fig.add_subplot(111,projection='3d',computed_zorder=False)
    m.setup_ax(ax,view='mouth',detail=True)
    # Draw all original base-fish faces from immutable V1 cubic geometry.
    fs=[];cs=[]
    for part in m.ALL_PARTS:
        f,c=m.build_mesh(part)
        if len(f):
            fs.append(m.transform_mesh(part,f,s));cs.append(c)
    # Pose dummy in the ACTUAL between-jaw cavity; animate a real solid arm.
    # Both arm and target keep their relative world location when the jaw shakes.
    pivot=(-46,-7,-17)
    target1=TORSO.copy();target2=m.rot_y(STRIKING_ARM,arm_angle,pivot)
    # Pre-release shaking is tiny, pinned to mouth position rather than model centre.
    wobble=.06*math.sin(8*math.pi*t)*(1-releasing)
    target1=m.rot_z(target1,wobble,(-46,0,-17))
    target2=m.rot_z(target2,wobble,(-46,0,-17))
    target1[...,0]+=exit_offset+s['lunge']
    target2[...,0]+=exit_offset+s['lunge']
    fs.extend([target1,target2]);cs.extend([CT,CA])
    ax.add_collection3d(Poly3DCollection(np.concatenate(fs),facecolors=np.concatenate(cs),
        edgecolors='#263126',linewidths=.075,alpha=1,zsort='average',rasterized=True))
    ax.set_title('TSAR PIKE V1.1  |  MELEE ESCAPE FROM JAWS',fontsize=11,weight='bold',pad=10)
    # Rich model overlay gives exact strike count without substituting drawn geometry.
    if t<.71:
        label=f'HELD: {hits}/3 actual strikes  |  accumulated effective damage {damage}/{BREAK_THRESHOLD} HP'
    else:
        label=f'ESCAPE: threshold {BREAK_THRESHOLD} HP reached — jaws open, victim moves clear'
    fig.text(.5,.075,label,ha='center',va='center',fontsize=10,color='#28372d',weight='bold')
    fig.canvas.draw()
    frame=Image.frombuffer('RGBA',fig.canvas.get_width_height(),fig.canvas.buffer_rgba()).convert('RGB')
    plt.close(fig)
    return frame

if __name__=='__main__':
    N=18
    frames=[]
    for i in range(N):
        t=i/(N-1)
        frames.append(make_frame(t))
        print('FRAME',i+1,'/',N,flush=True)
    gif=os.path.join(OUT,'15_escape_by_hits.gif')
    frames[0].save(gif,save_all=True,append_images=frames[1:],duration=240,loop=0,optimize=True)
    # Four authentic rendered stages (each is one frame from the exact 3D animation).
    stages=[(2,'01 / Gripped'),(7,'02 / Counter-hits'),(12,'03 / Jaw opens'),(16,'04 / Escape')]
    thumb_w=620;thumb_h=397
    canvas=Image.new('RGB',(thumb_w*2,thumb_h*2+50),'#f6f8f5')
    d=ImageDraw.Draw(canvas)
    for j,(idx,label) in enumerate(stages):
        im=frames[idx].resize((thumb_w,thumb_h))
        x=(j%2)*thumb_w;y=(j//2)*thumb_h+30
        canvas.paste(im,(x,y)); d.text((x+14,y-21),label,fill='#2d4537')
    canvas.save(os.path.join(OUT,'15_escape_4_keyframes.png'))
    print('GIF:',gif,'bytes',os.path.getsize(gif),flush=True)
