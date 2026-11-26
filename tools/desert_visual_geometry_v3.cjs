// Deterministic geometry and native pixel artwork; no external packages.
function generateAssets(){
const ids={desert_sword:"sword",desert_axe:"axe",desert_mace:"mace",desert_halberd:"halberd",desert_scythe:"scythe",scorpion_double_sting:"scorpion",stinger_dagger:"dagger",sand_sling:"sling",desert_glass_blade:"glass",dune_mace:"dune",buried_hammer:"hammer",hala_solar_staff:"sun",colony_scepter:"colony",four_winds_fists:"fists",lamya_weapon:"lamya",quicksand_whip:"whip"};
const out={},add=(a,b)=>a.map((x,i)=>x+b[i]),sub=(a,b)=>a.map((x,i)=>x-b[i]),mul=(a,s)=>a.map(x=>x*s),cross=(a,b)=>[a[1]*b[2]-a[2]*b[1],a[2]*b[0]-a[0]*b[2],a[0]*b[1]-a[1]*b[0]],norm=a=>mul(a,1/Math.hypot(...a));
function mesh(id,k,stage=0){
 const cells=new Map(),V=[],U=[],F=[],key=(x,y,z)=>`${x},${y},${z}`;
 const grid=n=>Math.sign(n)*Math.round(Math.abs(n)*2);
 function box(x0,y0,z0,x1,y1,z1,c){for(let x=grid(x0);x<grid(x1);x++)for(let y=grid(y0);y<grid(y1);y++)for(let z=grid(z0);z<grid(z1);z++)cells.set(key(x,y,z),c);}
 function face(p,c){let n=V.length+1,u=U.length+1;V.push(...p);U.push([(c+.5)/20,.5]);F.push("f "+p.map((_,i)=>`${n+i}/${u}`).join(" "));}
 function shaft(a,b,r=.75){box(-r,a,-r,r,b,r,1);box(-r,a,-r,-r+.5,b,r,2);for(let y=a+.5;y<b-.5;y+=2)box(-r-.25,y,-r-.25,r+.25,y+.5,r+.25,3);for(let y of [a,b-.5])box(-r-.5,y,-r-.5,r+.5,y+.5,r+.5,4);box(-1,-1.5,-1,1,1.5,1,0);for(let y of [-1.5,0,1])box(-1,y,-1,1,y+.5,1,2);}
 function jewel(x,y,z,r=1){box(x-r,y-r,z-.5,x+r,y+r,z+.5,16);box(x-r+.5,y-r+.5,z-.75,x+r-.5,y+r-.5,z+.75,17);box(x-.5,y,z-.75,x,y+.5,z+.75,19);}
 function guard(y,w=2.5){box(-w,y-.5,-1,w,y+.5,1,3);for(let s of [-1,1])box(s*w-(s>0?1:0),y,-1,s*w+(s<0?1:0),y+1.5,1,4);box(-1,y-.75,-1.5,1,y+.75,1.5,4);}
 function blade(a,b,glass=false,curve=false){for(let y=a;y<b;y+=.5){let t=(y-a)/(b-a),x=curve?Math.round((t<.55?-2*t:-1.1+7*(t-.55))*2)/2:0,w=t>.78?Math.max(.5,Math.round((1-t)*12)/2):t<.13?1:1.5;box(x-w,y,-.5,x+w,y+.5,.5,glass?11:7);box(x-w,y,-.5,x-w+.5,y+.5,.5,glass?13:9);box(x+w-.5,y,-.5,x+w,y+.5,.5,glass?12:8);if(w>1&&t<.9){box(x-.5,y,-1,x+.5,y+.5,1,glass?12:8);box(x-.5,y,-1,x,y+.5,-.5,glass?13:9);}}}
 function axe(y,wide=false){box(-1.5,y-1.5,-1.5,1.5,y+1.5,1.5,3);box(-1,y-2,-1,1,y+2,1,4);for(let i=0;i<8;i++){let x=-1.5-i*.5,h=1.5+Math.floor(i/2)*.5+(wide?.5:0),d=i<3?1:i<6?.75:.5;box(x-.5,y-h,-d,x,y+h+.5,d,7);box(x-.5,y+h,-d,x,y+h+.5,d,9);if(i>=6)box(x-.5,y-h,-.5,x,y+h+.5,.5,8);}for(let z of [-1.5,1.5])jewel(0,y,z,.75);}
 function crescent(y,fire=false){let strips=[[-10,-5,-3],[-9,-4,-1.5],[-8,-2.5,0],[-7,-1.5,1],[-6,-.5,1.5],[-5,0,2],[-4,.5,2.5],[-3,.5,2.5],[-2,0,2],[-1,-1,1.5],[0,-1.5,1],[1,-1.5,.5],[2,-2,-.5]];for(let [x,a,b] of strips){let d=x<-8?.5:x<-5?.75:1;box(x,y+a,-d,x+1,y+b,d,fire?16:7);box(x,y+b-.5,-d,x+1,y+b,d,fire?19:9);box(x,y+a,-d,x+1,y+a+.5,d,fire?17:8);}}
 function mace(y,heavy){box(-2.5,y-2,-2.5,2.5,y+2,2.5,heavy?14:7);box(-2,y-2.5,-2,2,y+2.5,2,heavy?15:8);box(-1.5,y-3,-1.5,1.5,y+3,1.5,3);for(let s of [-1,1])for(let t of [-1,1]){box(s*2.5-(s<0?1:0),y+t*1.25-.5,-.75,s*2.5+(s>0?1:0),y+t*1.25+.5,.75,8);box(-.75,y+t*1.25-.5,s*2.5-(s<0?1:0),.75,y+t*1.25+.5,s*2.5+(s>0?1:0),8);}if(heavy)for(let z of [-2.5,2.5])jewel(0,y,z,1.5);}
 // Straight, narrow rectangular cuboids for taut bowstring and arrow.
 function bar(a,b,width,depth,c){let d=b.map((v,i)=>v-a[i]),l=Math.hypot(...d),q=d.map(x=>x/l),u=Math.abs(q[2])<.9?[q[1],-q[0],0]:[1,0,0],ul=Math.hypot(...u);u=u.map(x=>x/ul);let v=[q[1]*u[2]-q[2]*u[1],q[2]*u[0]-q[0]*u[2],q[0]*u[1]-q[1]*u[0]],ring=p=>[[-1,-1],[1,-1],[1,1],[-1,1]].map(([s,t])=>p.map((x,i)=>x+s*u[i]*width/2+t*v[i]*depth/2)),r=ring(a),s=ring(b);face([...r].reverse(),c);face(s,c);for(let i=0;i<4;i++){let j=(i+1)%4;face([r[i],r[j],s[j],s[i]],c);}}
 if(["sword","glass","dagger","scorpion"].includes(k)){shaft(-3,2.5);guard(2.5,k==="dagger"?2:2.5);blade(3,k==="dagger"?9:k==="scorpion"?13:14,k==="glass",k==="scorpion");}
 else if(k==="axe"||k==="halberd"){let y=k==="axe"?8:14;shaft(k==="axe"?-3.5:-7,y+1);axe(y,k==="halberd");if(k==="halberd"){blade(y+1,y+7);box(1,y-.5,-.75,3,y+.5,.75,8);box(3,y,-.5,4,y+.5,.5,9);}}
 else if(k==="scythe"||k==="lamya"){shaft(-7,15);box(-1.5,13,-1.5,1.5,16,1.5,3);crescent(15,k==="lamya");if(k==="lamya"){for(let z of [-1.5,1.5])jewel(0,15,z,1.5);for(let i=0;i<5;i++)box(1+i,13-i*.5,-.5,2+i,14-i*.5,.5,17);}}
 else if(k==="mace"||k==="dune"){shaft(-4,7);mace(8,k==="dune");}
 else if(k==="hammer"){shaft(-6,12);box(-5,9.5,-2.5,5,14.5,2.5,14);box(-6,9,-3,-4.5,15,3,7);box(4.5,9,-3,6,15,3,7);for(let x of [-4.5,3.5])box(x,9,-3,x+1,15,3,3);box(-3.5,14,-2.5,3.5,15,2.5,8);for(let z of [-2.5,2.5]){box(-2.5,10,z-.5,2.5,14,z+.5,1);box(-2,10.5,z-.75,2,13.5,z+.75,3);jewel(0,12,z+(z>0?.5:-.5),1.25);}}
 else if(k==="sun"||k==="colony"){shaft(-5,13);if(k==="sun"){box(-2,13,-1,2,19,1,3);box(-3,14,-1,3,18,1,4);box(-1,12,-.75,1,14,.75,3);box(-1,19,-.75,1,21,.75,4);box(-5,15,-.75,-3,17,.75,4);box(3,15,-.75,5,17,.75,4);for(let x of [-1,1])for(let y of [-1,1])box(x*3-1,16+y*3-.5,-.75,x*3+1,16+y*3+.5,.75,4);for(let z of [-1,1])jewel(0,16,z,1.5);}else{box(-1.5,13,-1.5,1.5,19,1.5,14);for(let z of [-1.5,1.5])jewel(0,16,z,1.5);for(let s of [-1,1])for(let j=0;j<3;j++){let y=13+j*2;box(s<0?-3.5:1,y,-1,s<0?-1:3.5,y+1,1,15);box(s<0?-4.5:3,y+.5,-.75,s<0?-3:4.5,y+2,.75,3);}box(-.5,19,-.5,.5,21,.5,4);}}
 else if(k==="fists"){box(-2.5,-3,-2.5,-1.5,2,2.5,14);box(1.5,-3,-2.5,2.5,2,2.5,14);box(-1.5,-3,-2.5,1.5,2,-1.5,3);box(-1.5,-3,1.5,1.5,2,2.5,3);for(let x of [-2,-1,0,1]){box(x,1.5,-2.5,x+1,4,0,7);box(x,3,-2.5,x+1,4,-1,9);}box(-3,0,-1,-2,2,1,3);jewel(0,-.5,-2.5);}
 else if(k==="sling"){shaft(-2.5,1,.5);for(let s of [-1,1])for(let y=1;y<11;y+=.5){let t=(y-1)/10,x=s*Math.round((.5+3*Math.sin(Math.PI*t))*2)/2;box(x-.5,y,-.5,x+.5,y+.5,.5,2);}box(-3.5,6.5,-.5,3.5,7.5,.5,1);box(-2,6,-1,2,8,1,1);box(-1.5,6.5,-1.5,1.5,8,1.5,7);box(-1,7,-1.5,0,8,-.5,9);}
 else if(k==="whip"){shaft(-3,2);for(let i=0;i<45;i++){let a=i*.16,r=4.5-i*.065,x=Math.round(r*Math.sin(a)*2)/2,y=Math.round((7-r*Math.cos(a))*2)/2,z=Math.round(Math.sin(a)*2)/2;box(x-.5,y-.5,z-.5,x+.5,y+.5,z+.5,i%5===0?4:2);}box(-.5,2,-.5,.5,3,.5,3);}
 else if(k==="bow"){
  // +X is rear/string side; -X is the direction of fire. Grip fixed at origin.
  let rear=[5,6,7,8][stage],draw=[0,1,3,5][stage],tipY=[12,11.5,11,10.5][stage];
  box(-.75,-2,-1,.75,2,1,1);for(let y of [-1.5,-.5,.5,1.5])box(-1,y,-1.25,1,y+.5,1.25,3);
  for(let s of [-1,1]){let points=[[0,1.5],[.5,4],[1.5,7],[3,9],[rear-1,tipY-1],[rear,tipY]];
   for(let i=0;i<points.length-1;i++){let [x,y]=points[i],[xx,yy]=points[i+1],n=Math.max(1,Math.ceil(Math.abs(yy-y)*4));for(let j=0;j<=n;j++){let t=j/n,px=Math.round((x+(xx-x)*t)*2)/2,py=Math.round((y+(yy-y)*t)*2)/2*s,w=i<2?1:i<4?.75:.5,d=i<2?1:i<4?.75:.5;box(px-w,py-.5,-d,px+w,py+.5,d,i===2?3:7);box(px-w,py-.5,-d,px-w+.5,py+.5,d,i===2?5:9);}}
   bar([rear,s*tipY,0],stage?[rear+draw,1.75,1.25]:[rear,0,0],.15,.15,9);
  }
  if(stage){const nock=rear+draw,head=nock-18;bar([nock,1.75,1.25],[head,1.75,1.25],.28,.28,2);box(head-1,1.25,1,head,2.25,1.5,8);box(head-1.5,1,.75,head-.5,2.5,1.75,7);box(nock-1,1,1,nock-.5,2.5,1.5,9);}
 }
 // Union of solid cuboids, only exposed faces; greedy merge adjacent coplanar faces.
 if(k!=="bow"){
 const remaining=new Set(cells.keys()),components=[];
 while(remaining.size){let first=remaining.values().next().value,queue=[first];remaining.delete(first);
  for(let i=0;i<queue.length;i++){let p=queue[i].split(",").map(Number);for(let a=0;a<3;a++)for(let d of [-1,1]){let q=p.slice();q[a]+=d;let v=key(...q);if(remaining.delete(v))queue.push(v);}}
  components.push(queue.length);
 }
 if(components.length!==1)throw Error(id+" disconnected volumes: "+components.join(","));
 }
 let planes=new Map();
 for(let [s,c] of cells){let p=s.split(",").map(Number);for(let d=0;d<6;d++){let axis=Math.floor(d/2),sign=d%2?1:-1,q=p.slice();q[axis]+=sign;if(cells.has(key(...q)))continue;let u=(axis+1)%3,v=(axis+2)%3,plane=p[axis]+(sign>0?1:0),shade=d===3?Math.min(c+1,19):c,g=[d,plane,shade].join(",");if(!planes.has(g))planes.set(g,new Set());planes.get(g).add(`${p[u]},${p[v]}`);}}
 for(let [g,ps] of planes){let [d,plane,c]=g.split(",").map(Number),axis=Math.floor(d/2),u=(axis+1)%3,v=(axis+2)%3;while(ps.size){let [a,b]=ps.values().next().value.split(",").map(Number),w=1,h=1;while(ps.has(`${a+w},${b}`))w++;outer:while(true){for(let i=0;i<w;i++)if(!ps.has(`${a+i},${b+h}`))break outer;h++;}for(let i=0;i<w;i++)for(let j=0;j<h;j++)ps.delete(`${a+i},${b+j}`);let point=(x,y)=>{let p=[0,0,0];p[axis]=plane/2;p[u]=x/2;p[v]=y/2;return p;},quad=[point(a,b),point(a+w,b),point(a+w,b+h),point(a,b+h)];face(d%2?quad:quad.reverse(),c);}}
 return "# Slavic Myths desert V4: cuboid volumes, grip at 8/16\nmtllib desert.mtl\no "+id+"\n"+V.map(p=>"v "+p.map(x=>((x+8)/16).toFixed(6)).join(" ")).join("\n")+"\n"+U.map(p=>"vt "+p.map(x=>x.toFixed(6)).join(" ")).join("\n")+"\nusemtl desert\n"+F.join("\n")+"\n";
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
