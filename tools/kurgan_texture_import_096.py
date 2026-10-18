"""Compatibility entry point: restore canonical V2, never historical 256px/V1 art.

Missing/invalid canonical input is an error. Archived original sources are never
used as an automatic fallback and remain untouched in art/kurgan-0.9.6.
"""
from install_kurgan_16 import install

if __name__ == '__main__':
    install()
