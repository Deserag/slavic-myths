"""Canonical Advancements2 entry point, preserving the historical CLI name.

The old 129-card regrouping is superseded by the approved PR31 presentation.
No old recipe, discovery, world or player data is regenerated here.
"""
from advancements2_resources import apply

if __name__ == '__main__':
    apply()
    print('Advancements2:55 visible,85 data-only,77 technical definitions retained')
