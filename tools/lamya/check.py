import gzip,struct,sys
from pathlib import Path
import numpy as np
p=Path(sys.argv[1])
with gzip.open(p/'mesh.bin.gz','rb') as f:
    magic,n,q,a,k=struct.unpack('>5i',f.read(20))
    assert magic==0x4c414d36 and n>1000 and q>30000 and a==18 and k==9
    idx=np.frombuffer(f.read(q*16),dtype='>i4');assert idx.min()>=0 and idx.max()<n
    assert len(f.read(q*4))==q*4
    for i in range(a):
        for j in range(k):
            v=np.frombuffer(f.read(n*6),dtype='>i2');assert len(v)==n*3
    assert not f.read(1)
with gzip.open(p/'bounds.bin.gz','rb') as f:
    magic,n,a,k=struct.unpack('>4i',f.read(16));assert (magic,n,a,k)==(0x4c414236,20,18,9)
    names=[f.read(struct.unpack('>H',f.read(2))[0]).decode() for _ in range(n)]
    v=np.frombuffer(f.read(),dtype='>f4').reshape(a,k,n*6+9)
    assert np.isfinite(v).all()
    assert np.all(v[:,:,:n*6].reshape(a,k,n,6)[:,:,:,:3]<=v[:,:,:n*6].reshape(a,k,n,6)[:,:,:,3:])
    assert v[1,4,names.index('tail9')*6+2]<-5
print('Lamya V6 mesh and collision frame checks passed')
