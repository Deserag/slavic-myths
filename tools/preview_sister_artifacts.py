#!/usr/bin/env python3
"""Render five actual item-model cuboid sets, both empty and three awakened sockets."""
import json
from pathlib import Path
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import Patch

ROOT=Path(__file__).resolve().parents[1]
NAMES=["sisters_ring","sisters_necklace","sisters_belt","sisters_crown","sisters_brooch"]
COLORS={"metal":"#8b929e","trim":"#c0bac1","socket":"#20212c"}
POWER_COLORS=["#9c65db","#ef8134","#4a337f"]
OUT=ROOT/"build/sisters-artifact-25-previews"
OUT.mkdir(parents=True,exist_ok=True)
fig=plt.figure(figsize=(16,12))
for index,name in enumerate(NAMES):
    source=ROOT/f"src/main/resources/assets/slavicmyths/models/item/{name}.json"
    data=json.loads(source.read_text(encoding="utf-8"))
    for mode in range(2):
        ax=fig.add_subplot(5,2,index*2+mode+1,projection="3d")
        sockets=0
        for el in data["elements"]:
            x,y,z=el["from"];u,v,w=el["to"]
            tex=el["faces"]["north"]["texture"][1:]
            if tex=="socket":
                color=POWER_COLORS[sockets] if mode else COLORS["socket"]
                sockets+=1
            else:color=COLORS[tex]
            ax.bar3d(x,z,y,u-x,w-z,v-y,color=color,shade=True,edgecolor="#111119",linewidth=.15)
        assert sockets==3,(name,sockets)
        ax.set(xlim=(0,16),ylim=(0,16),zlim=(0,16))
        ax.view_init(elev=20,azim=-62)
        ax.set_box_aspect((1,1,1))
        ax.set_xlabel("X",fontsize=6);ax.set_ylabel("Z",fontsize=6);ax.set_zlabel("Y",fontsize=6)
        ax.tick_params(labelsize=5)
        ax.set_title(name.replace("sisters_","").capitalize()+(" — 3 blessings" if mode else " — empty"),fontsize=10)
fig.suptitle("Slavic Myths | Artifacts of the Six Sisters | Actual JSON geometry",fontsize=16)
fig.tight_layout(rect=(0,0,1,.97))
out=OUT/"all-five-models.png";fig.savefig(out,dpi=130);plt.close(fig)
print(out)
