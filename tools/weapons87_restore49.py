"""Reproducible P49 restoration of eight resources rolled back by older generators."""
from pathlib import Path
import hashlib,json,shutil
R=Path(__file__).resolve().parents[1]
def apply():
    source=R/'tools/weapon-art-original/p49-restored'
    manifest=json.loads((source/'manifest.json').read_text(encoding='utf-8'))
    # P49 manifest contains archived pre-approval SHA256 receipts. The exact
    # originals were independently verified against committed P52 source
    # 1192e229. Pin their immutable Git blobs, then replay newer #217/#225
    # overlays later in the canonical generator; never silently accept edits.
    original_blobs={
        'assets/slavicmyths/models/item/ancient_carolingian_sword_held.json':'50ca0d9b950c1539d8bd6681aae2abd80772a66f',
        'assets/slavicmyths/models/item/restored_carolingian_sword_held.json':'b378e18878cfafa259a152e77cbd2e4aab27d2a4',
        'assets/slavicmyths/models/item/poloz_whip.json':'9567908999c04144408f230f5d339c42cad36b91',
        'assets/slavicmyths/models/item/poloz_whip_pose_0.json':'dc8b377378db0e9dbaf301f487971be9f3a18d85',
        'assets/slavicmyths/models/item/poloz_whip_pose_1.json':'41022ac07f079cd28089198d71b3aa7a0d213341',
        'assets/slavicmyths/models/item/poloz_whip_pose_2.json':'d8c7a45835921d597b8e0c90ecc7884991143fe7',
        'assets/slavicmyths/models/item/poloz_whip_pose_3.json':'41478dd6f0b52a2a8bb64513a7e9a88d3f1e48fe',
        'assets/slavicmyths/models/item/poloz_whip_pose_4.json':'1ef7d4d4cc7df86db907d2f5d222f7d4eeb64808',
    }
    assert set(original_blobs)==set(manifest['files']), 'archived P49 resource set changed'
    for relative,expected in manifest['files'].items():
        original=source/relative
        data=original.read_bytes()
        git_blob=hashlib.sha1(b'blob '+str(len(data)).encode()+b'\x00'+data).hexdigest()
        assert git_blob==original_blobs[relative],('wrong approved P49 original',relative)
        target=R/'src/main/resources'/relative
        target.parent.mkdir(parents=True,exist_ok=True)
        shutil.copyfile(original,target)
if __name__=='__main__':apply()
