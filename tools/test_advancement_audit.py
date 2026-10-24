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
                    lambda n: n.pop(verifier.read(verifier.RES/'data/slavicmyths/advancement/ancient_find.json')['display']['title']['translate']), 'missing')

    def test_missing_icon(self):
        self.reject('src/main/resources/data/slavicmyths/advancement/ancient_find.json',
                    lambda n: n['display']['icon'].update(id='slavicmyths:does_not_exist'), 'icon')

    def test_bad_frame(self):
        self.reject('src/main/resources/data/slavicmyths/advancement/ancient_find.json',
                    lambda n: n['display'].update(frame='invalid'), 'slavicmyths:ancient_find')

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


    def test_rewards_contract(self):
        self.reject('src/main/resources/data/slavicmyths/advancement/shrine.json',
                    lambda n: n.update(rewards={'experience':999}), 'save contract')

    def test_requirements_contract(self):
        self.reject('src/main/resources/data/slavicmyths/advancement/ancient_find.json',
                    lambda n: n.update(requirements=[[key] for key in n['criteria']]), 'save contract')

    def test_no_legacy_display(self):
        self.reject('src/main/resources/data/slavicmyths/advancement/shrine.json',
                    lambda n: n.update(display={'hidden':True}), 'display allowlist')

    def test_no_extra_new_rewards(self):
        self.reject('src/main/resources/data/slavicmyths/advancement/defeat_gorynych.json',
                    lambda n: n.update(rewards={'experience':1}), 'new contract')

    def test_new_criterion_name(self):
        self.reject('src/main/resources/data/slavicmyths/advancement/class_iv.json',
                    lambda n: n.update(criteria={'other':{'trigger':'minecraft:impossible'}}), 'new contract')

    def test_parent_allowlist(self):
        self.reject('src/main/resources/data/slavicmyths/advancement/defeat_gorynych.json',
                    lambda n: n.update(parent='slavicmyths:defeat_leshy'), 'parent allowlist')

    def test_no_missing_visible_card(self):
        self.reject('src/main/resources/data/slavicmyths/advancement/ancient_find.json',
                    lambda n: n.pop('display'), 'display allowlist')

    def test_no_changed_toast(self):
        self.reject('src/main/resources/data/slavicmyths/advancement/first_neighbors.json',
                    lambda n: n['display'].update(show_toast=False), 'display allowlist')

    def test_book_branch_translation(self):
        self.reject('src/main/resources/assets/slavicmyths/lang/ru_ru.json',
                    lambda n: n.update({'tales.group.peaceful':'Old branch'}), 'book navigation localization')


if __name__ == '__main__':
    unittest.main()
