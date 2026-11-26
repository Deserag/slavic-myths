// Deterministic geometry and native pixel artwork; no external packages.
function generateAssets(){
const ids={desert_sword:"sword",desert_axe:"axe",desert_mace:"mace",desert_halberd:"halberd",desert_scythe:"scythe",scorpion_double_sting:"scorpion",stinger_dagger:"dagger",sand_sling:"sling",desert_glass_blade:"glass",dune_mace:"dune",buried_hammer:"hammer",hala_solar_staff:"sun",colony_scepter:"colony",four_winds_fists:"fists",lamya_weapon:"lamya",quicksand_whip:"whip"};
const out={},add=(a,b)=>a.map((x,i)=>x+b[i]),sub=(a,b)=>a.map((x,i)=>x-b[i]),mul=(a,s)=>a.map(x=>x*s),cross=(a,b)=>[a[1]*b[2]-a[2]*b[1],a[2]*b[0]-a[0]*b[2],a[0]*b[1]-a[1]*b[0]],norm=a=>mul(a,1/Math.hypot(...a));
function mesh(id,k,stage=0){
let V=[],F=[],U=[];function face(p,c){let n=V.length+1,u=U.length+1;V.push(...p);U.push([(c+.5)/20,.5]);F.push("f "+p.map((_,i)=>`${n+i}/${u}`).join(" "));}
function tube(p,r,cols,n=8,aspect=1){let rs=p.map((q,i)=>{let axis=norm(sub(p[Math.min(i+1,p.length-1)],p[Math.max(i-1,0)])),u=norm(cross(axis,Math.abs(axis[2])>.85?[0,1,0]:[0,0,1])),v=cross(axis,u);return Array.from({length:n},(_,j)=>{let a=(j+.5)*2*Math.PI/n;return add(q,add(mul(u,r[i]*Math.cos(a)),mul(v,r[i]*aspect*Math.sin(a))))})});for(let i=0;i<rs.length-1;i++)for(let j=0;j<n;j++){let l=(j+1)%n;face([rs[i][j],rs[i][l],rs[i+1][l],rs[i+1][j]],cols[(j+i)%cols.length]);}for(let j=0;j<n;j++){let l=(j+1)%n;face([p[0],rs[0][l],rs[0][j]],cols[0]);face([p.at(-1),rs.at(-1)[j],rs.at(-1)[l]],cols[0]);}}
function shaft(a,b,r=.68){tube([[0,a,0],[0,b,0]],[r,r],[1,2,1,0]);for(let y=a+.6;y<b-.3;y+=1.8)tube([[0,y,0],[0,y+.25,0]],[r*1.13,r*1.13],[2,3,2,1]);for(let y of [a,b-.42])tube([[0,y,0],[0,y+.42,0]],[r*1.35,r*1.35],[3,4,5,2]);}
function gem(x,y,z,r,c=17){tube([[x,y-r,z],[x,y,z],[x,y+r,z]],[.04,r,.04],[c,Math.min(c+1,19),Math.min(c+2,19),Math.max(0,c-1)],6,.85);}
function guard(y,w=3){tube([[-w,y+.5,0],[-w*.65,y,0],[0,y-.15,0],[w*.65,y,0],[w,y+.5,0]],[.27,.55,.7,.55,.27],[3,4,5,2],6);for(let z of [-.65,.65])gem(0,y,z,.55);}
function sections(rs,cs){for(let i=0;i<rs.length-1;i++)for(let j=0;j<6;j++){let l=(j+1)%6;face([rs[i][j],rs[i+1][j],rs[i+1][l],rs[i][l]],cs[j]);}for(let j=1;j<5;j++){face([rs[0][0],rs[0][j],rs[0][j+1]],cs[j]);face([rs.at(-1)[0],rs.at(-1)[j+1],rs.at(-1)[j]],cs[j]);}}
function blade(rows,glass=false){sections(rows.map(([x,y,w,d])=>[[x-w,y,0],[x-w*.36,y,-d],[x+w*.36,y,-d],[x+w,y,0],[x+w*.36,y,d],[x-w*.36,y,d]]),glass?[13,12,11,13,12,10]:[9,8,7,9,8,6]);}
function edge(rows,fire=false){sections(rows.map(([x,a,b,d])=>[[x,a,0],[x,a+(b-a)*.22,-d],[x,b-(b-a)*.18,-d],[x,b,0],[x,b-(b-a)*.18,d],[x,a+(b-a)*.22,d]]),fire?[19,17,16,18,17,3]:[9,8,7,9,8,6]);}
if(["sword","glass","dagger","scorpion"].includes(k)){shaft(-3,3,.64);guard(3,k==="dagger"?2.2:2.8);if(k==="scorpion")blade([[0,3,1.15,.65],[-.9,6,1.25,.82],[-1.1,9,1.05,.67],[.2,12,.85,.48],[2.5,14,.06,.04]]);else{let e=k==="dagger"?9:14;blade([[0,3,.95,.55],[0,4,1.55,.8],[0,e-3,1.25,.65],[0,e,.03,.03]],k==="glass");}}
else if(k==="axe"||k==="halberd"){let y=k==="axe"?8:15;shaft(-4,y+1,.76);edge([[-5,y-5,y+3,.14],[-4.7,y-4.5,y+4,.65],[-3,y-3,y+3,.95],[-1.4,y-1.8,y+1.9,1.1],[.65,y-1.4,y+1.5,.8]]);tube([[0,y-1.8,0],[0,y+1.8,0]],[1.2,1.2],[3,4,5,2]);if(k==="halberd"){blade([[0,y+1,.85,.65],[0,y+4,1.25,.65],[0,y+7,.04,.04]]);edge([[0,y-1,y+1,.8],[2.8,y-.6,y+.6,.45],[4,y,y+.05,.03]]);}}
else if(k==="scythe"||k==="lamya"){shaft(-5,17,.83);tube([[0,15,0],[0,18,0]],[1.15,1.15],[3,4,5,2]);edge([[-11,12,12.05,.025],[-9,15,17,.45],[-6,17,19,.8],[-3,17,19.7,1],[0,16,19,1.05],[3,15,17.8,.75],[5,14,14.05,.03]],k==="lamya");if(k==="lamya"){for(let z of [-1.1,1.1])gem(0,17,z,1.1);edge([[.5,13,16,.8],[3.8,12.5,15,.65],[6,10,10.05,.03]],true);}}
else if(k==="mace"||k==="dune"){shaft(-4,7,.72);tube([[0,5.7,0],[0,6.5,0],[0,9.2,0],[0,10.2,0]],[1.45,2.65,2.65,1.45],k==="dune"?[14,15,16,7]:[6,7,8,9]);for(let j=0;j<8;j++){let a=j*Math.PI/4,d=[Math.cos(a),0,Math.sin(a)];tube([add([0,8,0],mul(d,2.1)),add([0,8,0],mul(d,3.1)),add([0,8,0],mul(d,3.6))],[.85,.65,.06],[7,8,9,6],5);}if(k==="dune")for(let z of [-2.1,2.1])gem(0,8,z,1.15);}
else if(k==="hammer"){shaft(-5,12,.95);tube([[-5.8,12,0],[-5,12,0],[-4.3,12,0],[4.3,12,0],[5,12,0],[5.8,12,0]],[2.7,3.2,2.8,2.8,3.2,2.7],[6,7,8,9,14,15,3,4],8,1.12);for(let z of [-2.9,2.9])gem(0,12,z,1.25);}
else if(k==="sun"||k==="colony"){shaft(-5,14,.8);if(k==="sun"){for(let j=0;j<8;j++){let a=j*Math.PI/4,d=[Math.cos(a),Math.sin(a),0];tube([add([0,17,0],mul(d,2.3)),add([0,17,0],mul(d,4.3))],[.6,.06],[3,4,5,2],5);let b=(j+1)*Math.PI/4;tube([[3*Math.cos(a),17+3*Math.sin(a),0],[3*Math.cos(b),17+3*Math.sin(b),0]],[.57,.57],[3,4,5,2],6);}gem(0,17,0,2);}else{gem(0,17,0,2.2);for(let side of [-1,1])for(let j=0;j<3;j++){let y=14+j*2;tube([[side*.75,y,0],[side*3,y+.7,side*.5],[side*4,y+2,0],[side*3.5,y+2.6,-side*.6]],[.75,.57,.42,.06],[14,15,3,0],6);}tube([[0,18,0],[0,21,0]],[.75,.05],[3,4,5,2],6);}}
else if(k==="fists"){for(let j=0;j<8;j++){let a=j*Math.PI/4,b=(j+1)*Math.PI/4,p=(r,y,t)=>[r*Math.cos(t),y,r*Math.sin(t)];face([p(2.4,-3,a),p(2.4,-3,b),p(2.4,2,b),p(2.4,2,a)],j%2?3:14);face([p(1.4,-3,b),p(1.4,-3,a),p(1.4,2,a),p(1.4,2,b)],0);for(let y of [-3,2])face([p(1.4,y,a),p(1.4,y,b),p(2.4,y,b),p(2.4,y,a)],4);}for(let j=0;j<4;j++){let x=-2.1+j*1.4;tube([[x,1.1,0],[x,3.2,0],[x,4.1,-.18]],[.74,.85,.55],[14,15,3,2,7,8,9,6],6,1.2);}tube([[-2,0,.2],[-3,1.1,.4],[-2.8,2.2,.6]],[.8,.8,.5],[3,4,5,2],6);gem(0,-.6,-2.45,1.1);}
else if(k==="sling"){shaft(-3,1,.58);for(let s of [-1,1])tube([[0,1,0],[s*2,4,s*.6],[s*3.5,7,s*.7],[s*3,9,0]],[.55,.42,.35,.25],[1,2,3,0],6);tube([[-3,9,0],[-1.5,11,-1],[1.5,11,-1],[3,9,0]],[.28,.8,.8,.28],[0,1,2,3],6,.5);gem(0,11,-.8,1,7);}
else if(k==="whip"){shaft(-3,2,.7);let p=[[0,2,0]];for(let i=0;i<=28;i++){let a=i*.21,r=4.9-i*.12;p.push([r*Math.sin(a),7.2-r*Math.cos(a),1.5*Math.sin(a*.8)]);}tube(p,p.map((_,i)=>Math.max(.14,.62-i*.015)),[1,2,3,0,4],6);gem(...p.at(-1),.45);}
else if(k==="bow"){let draw=[0,2.4,4.8,7.5][stage],tip=-9+stage*.85;shaft(-2,2,.85);for(let s of [-1,1]){let p=[[0,s*1.5,0],[-.8,s*5,0],[-2.2,s*9,0],[-5.1,s*12,0],[tip,s*(15-stage*.25),0],[tip-.5,s*(15.6-stage*.25),0]];tube(p,[.95,1.05,.9,.73,.48,.15],[1,2,3,0,6,7,8,9],8,.85);tube([p.at(-1),[tip-draw,0,0]],[.105,.105],[8,9,8,7],6);}if(stage){tube([[tip-draw,0,0],[5,0,0]],[.2,.2],[1,2,3,0],6);tube([[5,0,0],[7,0,0]],[.55,.035],[7,8,9,6],6);}}
return "# Slavic Myths desert V3: independent faceted geometry, grip at 8/16\nmtllib desert.mtl\no "+id+"\n"+V.map(p=>"v "+p.map(x=>((x+8)/16).toFixed(6)).join(" ")).join("\n")+"\n"+U.map(p=>"vt "+p.map(x=>x.toFixed(6)).join(" ")).join("\n")+"\nusemtl desert\n"+F.join("\n")+"\n";
}
for(let [id,k] of Object.entries(ids))out[id+".obj"]=mesh(id,k);for(let i=0;i<4;i++){let id="mirage_bow"+(i?"_pulling_"+(i-1):"");out[id+".obj"]=mesh(id,"bow",i);}
const P={o:[48,29,24,255],b:[106,60,34,255],s:[191,122,49,255],g:[247,183,68,255],l:[255,238,173,255],d:[109,102,78,255],m:[214,200,152,255],h:[255,248,217,255],t:[28,77,82,255],c:[57,169,161,255],a:[153,239,218,255],r:[165,48,27,255],f:[241,99,31,255],i:[100,185,238,255],w:[218,250,255,255]};
const atlas=new Uint8Array(128*64*4),tiles=[];
for(let i=0;i<29;i++){
let a=new Array(256).fill(null),px=(x,y,c)=>{x=Math.round(x);y=Math.round(y);if(x>=0&&x<16&&y>=0&&y<16)a[y*16+x]=c};
function poly(p,c){for(let y=0;y<16;y++)for(let x=0;x<16;x++){let yes=false;for(let j=0,k=p.length-1;j<p.length;k=j++){let [u,v]=p[j],[q,r]=p[k];if((v>y+.01)!=(r>y+.01)&&x+.01<(q-u)*(y+.01-v)/(r-v)+u)yes=!yes;}if(yes)px(x,y,c);}}
function line(p,c,w=1){for(let j=1;j<p.length;j++){let [x,y]=p[j-1],[u,v]=p[j],n=Math.max(1,Math.ceil(Math.hypot(u-x,v-y)*2));for(let k=0;k<=n;k++)for(let dy=0;dy<w;dy++)for(let dx=0;dx<w;dx++)px(x+(u-x)*k/n+dx-(w-1)/2,y+(v-y)*k/n+dy-(w-1)/2,c);}}
function rect(x,y,w,h,c){for(let j=y;j<y+h;j++)for(let k=x;k<x+w;k++)px(k,j,c)}
function circle(x,y,r,c){for(let j=0;j<16;j++)for(let k=0;k<16;k++)if((k-x)**2+(j-y)**2<=r*r)px(k,j,c);}
function slash(x,y,c="m"){poly([[x-2,y+2],[x+5,y-7],[x+7,y-8],[x+7,y-4],[x+1,y+4]],c);line([[x,y+1],[x+6,y-6]],"h");line([[x-2,y+3],[x+2,y+6]],"g",2);line([[x,y+5],[x-2,y+7]],"b",2);}
function arrow(x,y,xx,yy,c="g"){line([[x,y],[xx,yy]],c,2);let dx=xx-x,dy=yy-y,l=Math.hypot(dx,dy),u=dx/l,v=dy/l;poly([[xx+u,yy+v],[xx-u*4-v*2,yy-v*4+u*2],[xx-u*4+v*2,yy-v*4-u*2]],"h");}
function sun(x,y,r=3){for(let j=0;j<8;j++){let t=j*Math.PI/4;line([[x+Math.cos(t)*(r+1),y+Math.sin(t)*(r+1)],[x+Math.cos(t)*(r+3),y+Math.sin(t)*(r+3)]],"g");}circle(x,y,r,"f");circle(x-.6,y-.6,r-1,"g");px(x-1,y-1,"l");}
function ant(x,y,s=1){line([[x-3*s,y-2*s],[x,y],[x+3*s,y-2*s]],"b");line([[x-3*s,y+2*s],[x,y],[x+3*s,y+2*s]],"b");line([[x-3*s,y+4*s],[x,y+2*s],[x+3*s,y+4*s]],"s");circle(x,y+2*s,1.6*s,"b");circle(x,y,1.1*s,"s");circle(x,y-2*s,1.1*s,"g");px(x-1,y-2,"l");line([[x-1,y-3],[x-2,y-4]],"g");line([[x+1,y-3],[x+2,y-4]],"g");}
function fist(x,y){poly([[x-3,y+1],[x-3,y-2],[x-1,y-4],[x+4,y-4],[x+5,y-2],[x+4,y+3],[x+1,y+4],[x-2,y+3]],"b");rect(x-1,y-3,5,3,"m");line([[x-1,y-3],[x+3,y-3]],"h");rect(x-2,y,6,3,"g");line([[x-2,y+2],[x+3,y+2]],"s");for(let j=0;j<3;j++)px(x+j*2-1,y-1,"d");}
function wave(y=10,c="g"){poly([[1,y+3],[1,y+1],[4,y-3],[7,y],[11,y-4],[14,y-1],[14,y+3]],"b");line([[1,y+1],[4,y-3],[7,y],[11,y-4],[14,y-1]],c,2);line([[3,y-1],[4,y-3],[5,y-2]],"l");}
function heal(area,ice){let c=ice?"i":"f",hi=ice?"w":"l";if(area){for(let j=0;j<16;j++){let t=j*Math.PI/8;px(8+6*Math.cos(t),8+6*Math.sin(t),ice?"c":"g");}}else{poly([[3,7],[3,4],[5,2],[8,3],[11,2],[13,4],[13,7],[8,13]],c);line([[4,5],[5,3],[7,4]],hi);}rect(6,4,4,8,c);rect(4,6,8,4,c);rect(7,5,2,6,hi);rect(5,7,6,2,hi);}
switch(i){
case 0:slash(3,8);slash(7,8);line([[2,3],[4,2]],"g");break;
case 1:slash(6,8);line([[1,11],[5,7]],"s");line([[1,6],[5,4]],"g");break;
case 2:circle(10,5,3,"d");circle(9,4,1,"h");line([[2,13],[2,9],[6,5],[10,4]],"b",2);line([[1,10],[5,6],[8,5]],"g");px(3,3,"l");break;
case 3:slash(6,8,"c");line([[1,12],[5,10]],"a",2);line([[1,7],[4,6]],"c");break;
case 4:sun(4,11,2);poly([[5,8],[13,1],[15,3],[7,11]],"f");line([[6,9],[14,2]],"l",2);break;
case 5:sun(8,8,4);px(2,2,"l");px(13,12,"l");break;
case 6:wave(9);line([[2,14],[12,14]],"s");break;
case 7:line([[9,3],[6,10]],"b",2);poly([[5,1],[12,1],[14,4],[12,6],[5,5]],"m");line([[5,1],[11,1]],"h");wave(12);break;
case 8:arrow(2,13,3,3);arrow(7,14,8,1);arrow(12,13,13,3);break;
case 9:arrow(2,13,13,2,"c");line([[8,2],[8,5]],"d",2);line([[12,8],[15,8]],"d",2);px(9,6,"l");break;
case 10:line([[2,13],[2,8],[5,4],[10,3],[13,5],[12,8],[8,9]],"b",3);line([[2,11],[3,7],[6,4],[10,4],[12,6],[10,8]],"g");arrow(13,12,6,11,"c");break;
case 11:line([[1,7],[3,3],[9,2],[13,5],[14,9],[11,13],[5,13],[3,11]],"s",3);line([[1,6],[4,3],[9,3],[13,6],[13,9],[10,12],[5,12]],"l");arrow(6,12,2,10);break;
case 12:circle(8,8,6,"t");circle(8,8,4,"o");ant(8,7);px(2,4,"a");px(13,10,"a");break;
case 13:ant(4,5,.75);ant(11,4,.75);ant(8,11,.75);break;
case 14:ant(8,10,.85);arrow(8,6,8,1,"c");px(2,5,"a");px(13,5,"a");break;
case 15:fist(9,7);line([[1,5],[5,5]],"a",2);line([[1,9],[4,9]],"c");break;
case 16:for(let [x,y] of [[4,4],[11,4],[4,11],[11,11]]){rect(x-2,y-2,4,4,"b");rect(x-2,y-2,3,2,"g");px(x-2,y-2,"h");px(x,y,"s");}break;
case 17:fist(7,5);wave(12,"c");break;
case 18:poly([[1,14],[2,8],[5,10],[7,2],[10,7],[12,4],[15,14]],"r");poly([[3,13],[4,9],[6,11],[8,4],[11,10],[13,7],[13,14]],"f");poly([[6,14],[8,8],[11,14]],"l");break;
case 19:sun(8,3,2);poly([[6,6],[10,6],[13,14],[3,14]],"f");poly([[7,6],[9,6],[10,14],[6,14]],"l");break;
case 20:line([[1,10],[3,5],[7,3],[12,4],[14,7],[13,10],[10,12],[5,12],[3,9],[5,7],[9,6],[11,8],[9,10],[7,9]],"b",3);line([[1,9],[3,5],[7,3],[12,4],[14,7],[13,9],[10,11],[5,11],[4,9],[6,7],[9,7],[10,8]],"g",2);px(4,4,"l");px(12,5,"l");break;
case 21:line([[8,2],[8,9]],"b",2);rect(3,2,10,4,"d");line([[4,2],[11,2]],"h",2);line([[4,4],[11,4]],"s");wave(12);break;
case 22:circle(8,10,6,"b");circle(8,10,4,"s");circle(8,10,2,"o");line([[2,10],[5,8],[10,8],[13,10]],"g");arrow(8,1,8,8);break;
case 23:for(let [x,h] of [[2,5],[6,11],[11,8]]){poly([[x,14],[x,14-h],[x+2,13-h],[x+3,14-h],[x+3,14]],"s");line([[x,14-h],[x+2,13-h],[x+2,14]],"l");}break;
case 24:heal(false,true);break;case 25:heal(true,true);break;case 26:heal(false,false);break;case 27:heal(true,false);break;
case 28:line([[4,2],[8,3],[11,6],[11,9],[8,12],[4,14]],"b",3);line([[4,2],[8,4],[10,7],[8,12],[4,14]],"g",2);line([[4,2],[2,8],[4,14]],"h");arrow(3,8,14,8);break;
}
let border=a.slice();for(let y=0;y<16;y++)for(let x=0;x<16;x++){if(a[y*16+x])continue;for(let [dx,dy] of [[1,0],[-1,0],[0,1],[0,-1]])if(x+dx>=0&&x+dx<16&&y+dy>=0&&y+dy<16&&a[(y+dy)*16+x+dx]){border[y*16+x]="o";break;}}
tiles.push(border.map(c=>c||".").join(""));for(let y=0;y<16;y++)for(let x=0;x<16;x++){let c=border[y*16+x];if(c)atlas.set(P[c],((Math.floor(i/8)*16+y)*128+i%8*16+x)*4);}
}
if(new Set(tiles).size!==29)throw Error("duplicate icons");
const be=n=>[(n>>>24)&255,(n>>>16)&255,(n>>>8)&255,n&255],crc=arr=>{let c=0xffffffff;for(let x of arr){c^=x;for(let j=0;j<8;j++)c=(c>>>1)^((c&1)?0xedb88320:0);}return (c^0xffffffff)>>>0;},chunk=(name,data)=>{let t=[...name].map(x=>x.charCodeAt(0)),b=t.concat(Array.from(data));return be(data.length).concat(b,be(crc(b)))};
let raw=[];for(let y=0;y<64;y++)raw.push(0,...atlas.slice(y*512,(y+1)*512));let z=[0x78,0x01],s1=1,s2=0;for(let i=0;i<raw.length;i+=65535){let p=raw.slice(i,i+65535),n=p.length;z.push(i+n===raw.length?1:0,n&255,n>>8,~n&255,(~n>>8)&255,...p);}for(let x of raw){s1=(s1+x)%65521;s2=(s2+s1)%65521;}z.push(...be((s2<<16)|s1));
return {out,png:[137,80,78,71,13,10,26,10].concat(chunk("IHDR",be(128).concat(be(64),[8,6,0,0,0])),chunk("IDAT",z),chunk("IEND",[])),ids:Object.keys(ids),tiles};
}
if(require.main===module){const fs=require('fs'),path=require('path'),g=generateAssets(),a=path.join(__dirname,'../src/main/resources/assets/slavicmyths/');for(const [n,v] of Object.entries(g.out))fs.writeFileSync(path.join(a,'models/item/desert_mesh',n),v);fs.writeFileSync(path.join(a,'textures/gui/weapon_skills/desert_atlas.png'),Buffer.from(g.png));}
module.exports=generateAssets;
