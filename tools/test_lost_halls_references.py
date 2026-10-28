"""Tests of the SOURCE validator, not evidence that approved art is available."""
import hashlib
import io
from pathlib import Path
import tempfile
import unittest
import zipfile
from PIL import Image
from check_lost_halls_references import inspect


class SourceGateTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        stream = io.BytesIO()
        Image.new('RGB', (2, 3), 'blue').save(stream, format='PNG')
        self.png = stream.getvalue()
        self.expected = {'fixture': (hashlib.sha256(self.png).hexdigest(), (2, 3))}

    def test_missing_original_blocks(self):
        self.assertEqual(inspect([self.root], self.expected)['status'], 'BLOCKED_REFERENCE')

    def test_named_impostor_does_not_pass(self):
        (self.root / 'blocks.png').write_bytes(b'not an approved PNG')
        self.assertEqual(inspect([self.root], self.expected)['status'], 'BLOCKED_REFERENCE')

    def test_exact_bytes_pass_even_with_different_name(self):
        (self.root / 'renamed.png').write_bytes(self.png)
        self.assertEqual(inspect([self.root], self.expected)['status'], 'PASS')

    def test_zip_is_read_without_extraction(self):
        path = self.root / 'refs.zip'
        with zipfile.ZipFile(path, 'w') as archive:
            archive.writestr('../outside.png', self.png)
        self.assertEqual(inspect([path], self.expected)['status'], 'PASS')
        self.assertEqual(list(self.root.iterdir()), [path])

    def test_wrong_dimensions_fail_despite_matching_hash(self):
        (self.root / 'source.png').write_bytes(self.png)
        expected = {'fixture': (self.expected['fixture'][0], (3, 2))}
        self.assertEqual(inspect([self.root], expected)['status'], 'BLOCKED_REFERENCE')

    def test_bad_zip_reported(self):
        (self.root / 'bad.zip').write_bytes(b'not ZIP')
        report = inspect([self.root], self.expected)
        self.assertTrue(report['scan_errors'])
        self.assertEqual(report['status'], 'BLOCKED_REFERENCE')

    def test_all_references_required(self):
        (self.root / 'source.png').write_bytes(self.png)
        expected = dict(self.expected, missing=('0' * 64, (2, 3)))
        self.assertEqual(inspect([self.root], expected)['status'], 'BLOCKED_REFERENCE')

    def test_corrupt_image_with_matching_hash_rejected(self):
        bad = self.png[:40]
        (self.root / 'source.png').write_bytes(bad)
        expected = {'fixture': (hashlib.sha256(bad).hexdigest(), (2, 3))}
        self.assertEqual(inspect([self.root], expected)['status'], 'BLOCKED_REFERENCE')


if __name__ == '__main__':
    unittest.main()
