"""#165/#170: remove five fully hidden coincident west faces, keep shape/UV/display."""
from pathlib import Path
import json
R=Path(__file__).resolve().parents[1]
def apply():
    path=R/'src/main/resources/assets/slavicmyths/models/item/spear.json'
    data=json.loads(path.read_text(encoding='utf-8'))
    for index in (6,9,12,15,18):
        base=data['elements'][index];rim=data['elements'][index+1]
        assert base['from'][0]==rim['from'][0]
        assert all(rim['from'][axis]<=base['from'][axis] and rim['to'][axis]>=base['to'][axis] for axis in (1,2))
        base['faces'].pop('west',None)
    path.write_text(json.dumps(data,indent=2)+'\n',encoding='utf-8')
if __name__=='__main__':apply()
