#!/usr/bin/env python3
"""Slavic Myths III/IV: diagnostic geometry, not an exported Java voxel model.
Mirrors selected formulas from commit 59f470678012aa83fa19d0d49d24a7bd4999da26.
Blue/green/tan overlays are PROPOSED zones; validate in the actual Java palette.
Run: python technical_reference.py [output_directory]
Requires: matplotlib, numpy.
"""
import json, math, sys
from pathlib import Path
import numpy as np
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d.art3d import Poly3DCollection

SHA = "59f470678012aa83fa19d0d49d24a7bd4999da26"
OUT = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
OUT.mkdir(parents=True, exist_ok=True)

def config(level, extra_length=0, extra_width=0, decks=1, bridge=False, cabin=False):
    width = (15 if level == 3 else 21) + 2*extra_width
    raw_length = (33 if level == 3 else 51) + 4*extra_length
    minimum = math.ceil(width * 3.6)
    if minimum % 2 == 0:
        minimum += 1
    length = max(raw_length, minimum)
    return dict(level=level, width=width, length=length, end=length//2,
                decks=decks, bridge=bridge, cabin=cabin)

def beam(c, z):
    end = c["end"]
    if abs(z) > end:
        return -1
    half = c["width"]//2
    if c["level"] == 4:
        if z >= end - 12:
            t = max(0, (end-z)/12)
            return max(2, round(2+(half-2)*math.sin(t*math.pi/2)))
        if z <= -end + 8:
            t = max(0, (end+z)/8)
            return max(3, round(3+(half-3)*math.sin(t*math.pi/2)))
    else:
        if end-z < min(8,end):
            t = max(0,(end-z)/min(8,end))
            return max(1,round(1+(half-1)*t))
        if end+z < min(8,end):
            t = max(0,(end+z)/min(8,end))
            return max(3,round(3+(half-3)*math.sin(t*math.pi/2)))
    return half

def box(ax,x,y,z,dx,dy,dz,color,alpha=.75):
    p=np.array([[x,y,z],[x+dx,y,z],[x+dx,y+dy,z],[x,y+dy,z],
                [x,y,z+dz],[x+dx,y,z+dz],[x+dx,y+dy,z+dz],[x,y+dy,z+dz]])
    p=p[:,[0,2,1]]  # plot axes X, Z, Y (source cells are X, Y, Z)
    faces=[[p[i] for i in ids] for ids in
           ([0,1,2,3],[4,5,6,7],[0,1,5,4],[1,2,6,5],[2,3,7,6],[3,0,4,7])]
    ax.add_collection3d(Poly3DCollection(faces, facecolors=color, alpha=alpha,
                                        linewidths=.12, edgecolors="#555555"))

def axes(ax,c,title,top=29):
    ax.set_title(title,fontsize=11)
    ax.set_xlabel("X / port (-) to starboard (+)")
    ax.set_ylabel("Z / stern (-) to bow (+)")
    ax.set_zlabel("Y / height")
    ax.set_xlim(-c["width"]//2-3,c["width"]//2+3)
    ax.set_ylim(-c["end"]-4,c["end"]+22)
    ax.set_zlim(0,top)
    ax.set_box_aspect((c["width"]+6,c["length"]+18,top*1.4))
    ax.grid(True,alpha=.5)
    ax.view_init(elev=20,azim=-65)

def longitudinal(c,path):
    fig=plt.figure(figsize=(15,9))
    ax=fig.add_subplot(projection="3d")
    e=c["end"]; rear=-e+(4 if c["level"]==4 else 2)
    bottoms=[1,6,11] if c["level"]==4 else [1,5,10]
    for z in range(-e,e+1,2):
        b=beam(c,z)
        for floor in bottoms:
            # Thin cutaway bands, NOT a full 3D reconstruction of the Java hull
            box(ax,-.5,floor-.12,z-.5,1,.24,1,"#a87b4b",.65)
        for x in (-b,b):
            for y in (2,5,8,11):
                box(ax,x-.25,y-.5,z-.5,.5,1,1,"#887055",.16)
    if c["level"]==4:
        for z in range(rear,rear+16):
            box(ax,-.5,15.8,z-.48,1,.2,.96,"#805832")
        if c["decks"]>=2 or c["bridge"]:
            for z in range(rear,rear+10):
                box(ax,-.5,20.8,z-.48,1,.2,.96,"#805832")
        if c["decks"]>=3:
            for z in range(rear+1,rear+5):
                box(ax,-.5,25.8,z-.48,1,.2,.96,"#805832")
        for z in range(e+1,e+19):
            t=(z-e)/18
            underside=5+round(8*t*t*(3-2*t))
            box(ax,-.5,underside,z-.5,1,15-underside,1,"#756347",.6)
    else:
        upper=20 if c["decks"]>=3 else 15 if (c["decks"]>=2 or c["bridge"] or c["cabin"]) else 10
        if upper>=15:
            for z in range(rear,rear+(16 if upper==20 else 10)):
                box(ax,-.5,14.8,z-.48,1,.2,.96,"#805832")
        if upper==20:
            for z in range(rear,rear+17):
                box(ax,-.5,19.8,z-.48,1,.2,.96,"#805832")
    axes(ax,c,f"LEVEL {c['level']} | longitudinal design cut, W={c['width']} L={c['length']}")
    fig.savefig(OUT/path,dpi=130,bbox_inches="tight")
    plt.close(fig)

def gunport(c,z):
    e=c["end"]
    if c["level"]==4:
        return (z+e)%5==0 and z > -e+7 and z < e-9
    return abs(z)<=e-5 and abs(z)>=3 and (z+e)%4==0

def stair_cells(c):
    e=c["end"]
    if c["level"]==4:
        start=-e+34
        # Two connected interior flights from floors 1,6,11.
        return {(x,z) for x in (-3,-2)
                for z in list(range(start-6,start+1))+
                         list(range(start-12,start-5))}
    return set()  # III flight positions resolved separately by Java at integration

def hybrid_top(c):
    assert c["level"]==4
    fig=plt.figure(figsize=(15,9))
    ax=fig.add_subplot(projection="3d")
    stairs=stair_cells(c)
    e=c["end"]
    count={"corridor":0,"cargo_candidates":0,"artillery_reserve":0,"stair":0}
    for z in range(-e,e+1):
        b=beam(c,z)
        if b<5:
            continue
        ports=[p for p in range(z-1,z+2) if gunport(c,p)]
        for x in range(-b+2,b-1):
            if (x,z) in stairs:
                color="#be5b55";count["stair"]+=1
            elif abs(x)<=1:
                color="#3a9a7a";count["corridor"]+=1
            elif ports and abs(x)>=b-3:
                color="#d1ab55";count["artillery_reserve"]+=1
            elif 2<=abs(x)<=b-4:
                color="#6485ba";count["cargo_candidates"]+=1
            else:
                continue
            box(ax,x-.48,6.18,z-.48,.96,.13,.96,color,.85)
    axes(ax,c,"IV Y=6: PROPOSED corridor (green), cargo (blue), gun reserve (ochre), stairs (red)",15)
    ax.view_init(elev=68,azim=-90)
    fig.savefig(OUT/"iv_hybrid_top.png",dpi=150,bbox_inches="tight")
    plt.close(fig)
    return count

def transverse(c):
    fig=plt.figure(figsize=(11,8))
    ax=fig.add_subplot(projection="3d")
    z=0
    b=beam(c,z)
    for x in range(-b,b+1):
        box(ax,x-.45,5.87,z-.5,.9,.24,1,"#7b5335",.82)
        box(ax,x-.45,10.9,z-.5,.9,.2,1,"#7b5335",.67)
    for x in (-b,-b+1,b-1,b):
        for y in range(7,11):
            box(ax,x-.42,y-.42,z-.5,.84,.84,1,"#8e7554",.72)
    for x in (-1,0,1):
        box(ax,x-.42,6.3,z-.5,.84,.15,1,"#3a9a7a",.95)
    for x in (-b+4,-b+5,b-5,b-4):
        box(ax,x-.42,6.5,z-.5,.84,.18,1,"#6485ba",.8)
    for x in (-b+2,-b+3,b-3,b-2):
        box(ax,x-.42,6.8,z-.5,.84,.18,1,"#d1ab55",.8)
    ax.set_title("IV central transverse cut: future artillery / cargo zones / corridor")
    ax.set_xlabel("X");ax.set_ylabel("Z");ax.set_zlabel("Y")
    ax.set_xlim(-b-2,b+2);ax.set_ylim(-2,2);ax.set_zlim(5,12)
    ax.set_box_aspect((2*b+4,6,9));ax.grid(True);ax.view_init(elev=19,azim=-79)
    fig.savefig(OUT/"iv_hybrid_section.png",dpi=150,bbox_inches="tight")
    plt.close(fig)

if __name__=="__main__":
    third=config(3)
    fourth=config(4)
    longitudinal(third,"iii_longitudinal.png")
    longitudinal(fourth,"iv_longitudinal.png")
    transverse(fourth)
    candidate_counts=hybrid_top(fourth)
    manifest={
        "source_commit":SHA,
        "provenance":"geometry-derived design visualization, NOT Java generated voxel output",
        "coordinate_system":{"X":"transverse", "Y":"vertical", "Z":"+bow,-stern"},
        "existing":{"III":{"base":third,"max":config(3,3,2),
                            "known_floors":[1,5,10],"raised_if_enabled":[15,20]},
                    "IV":{"base":fourth,"max":config(4,3,2),
                          "known_floors":[1,6,11,16],
                          "captain_deck_when_decks_at_least_2_or_bridge":21,
                          "canopy_when_decks_at_least_3":26}},
        "proposal":{"hybrid_deck":"IV y=6 floor, y=7..10 accessible interior",
                    "corridor":"x=-1,0,+1 when halfBeam >=5; continuity must be validated",
                    "artillery_reserve":"both sides, around y=8 shutter markers",
                    "cargo":"candidate cells only; real permission via ShipModularBlockHull.movingCargoCell",
                    "baseline_candidate_counts":candidate_counts,
                    "note":"overlays are NOT exact Java palette, collision clearance or cargo authorizations"},
        "reference_files":["iii_longitudinal.png","iv_longitudinal.png",
                           "iv_hybrid_section.png","iv_hybrid_top.png"]
    }
    (OUT/"diagnostic_manifest.json").write_text(json.dumps(manifest,indent=2,ensure_ascii=False),encoding="utf-8")
    print("Created technical PNGs and manifest in",OUT)
