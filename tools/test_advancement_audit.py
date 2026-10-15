"""Mutation tests: the auditor must reject broken inputs, not merely count files."""
import copy
import unittest
from unittest.mock import patch
import verify_advancement_tree as verifier


class AuditTests(unittest.TestCase):
    def reject(self, relative, change, message):
        original = verifier.read
        def changed(path):
            value = original(path)
            if path.relative_to(verifier.ROOT).as_posix() == relative:
                value = copy.deepcopy(value)
                change(value)
            return value
        with patch.object(verifier, 'read', changed), self.assertRaisesRegex(AssertionError, message):
            verifier.audit()

    def test_missing_parent(self):
        self.reject('src/main/resources/data/slavicmyths/advancement/shrine.json',
                    lambda n: n.update(parent='slavicmyths:does_not_exist'), 'missing parent')

    def test_cycle(self):
        self.reject('src/main/resources/data/slavicmyths/advancement/shrine.json',
                    lambda n: n.update(parent='slavicmyths:shrine'), 'cycle')

    def test_criteria_contract(self):
        self.reject('src/main/resources/data/slavicmyths/advancement/shrine.json',
                    lambda n: n.update(criteria={'renamed': {'trigger': 'minecraft:impossible'}}), 'save contract')

    def test_missing_translation(self):
        self.reject('src/main/resources/assets/slavicmyths/lang/ru_ru.json',
                    lambda n: n.pop('advancements.slavicmyths.shrine.title'), 'missing')

    def test_missing_icon(self):
        self.reject('src/main/resources/data/slavicmyths/advancement/shrine.json',
                    lambda n: n['display']['icon'].update(id='slavicmyths:does_not_exist'), 'icon')

    def test_bad_frame(self):
        self.reject('src/main/resources/data/slavicmyths/advancement/shrine.json',
                    lambda n: n['display'].update(frame='invalid'), 'slavicmyths:shrine')

    def test_dangling_book_reference(self):
        self.reject('src/main/resources/data/slavicmyths/tales/leshy.json',
                    lambda n: n.update(advancements=['slavicmyths:does_not_exist']), 'dangling advancement')

    def test_dangling_book_link(self):
        self.reject('src/main/resources/data/slavicmyths/tales/leshy.json',
                    lambda n: n.update(links=['slavicmyths:does_not_exist']), 'dangling tale')

    def test_missing_dispatch(self):
        original = verifier.Path.read_text
        def removed(path, *args, **kwargs):
            text = original(path, *args, **kwargs)
            if path.name in ('KitchenMenu.java', 'KitchenTile.java'):
                text = text.replace('Knowledge.award(p,"kitchen_meal")', 'Knowledge.knows(p,"kitchen_meal")')
            return text
        with patch.object(verifier.Path, 'read_text', removed), self.assertRaisesRegex(AssertionError, 'no server award dispatch'):
            verifier.audit()

    def test_helper_criterion_mismatch(self):
        original = verifier.Path.read_text
        def broken(path, *args, **kwargs):
            text = original(path, *args, **kwargs)
            if path.name == 'GuardianLedger.java':
                text = text.replace('org.slavicmyths.progression.Knowledge.award(p,spring?',
                                    'org.slavicmyths.item.FolkAccessoryItem.award(p,spring?')
            return text
        with patch.object(verifier.Path, 'read_text', broken), self.assertRaisesRegex(AssertionError, 'helper criterion mismatch'):
            verifier.audit()


if __name__ == '__main__':
    unittest.main()
