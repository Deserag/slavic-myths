package org.slavicmyths.client;

/** Authored geometry shared by the native model builders and offline manifest checks. */
interface BossGeometrySink {
    void add(String name, String parent, float x, float y, float z, float[] box,
             int material, float rx, float ry, float rz);
}
