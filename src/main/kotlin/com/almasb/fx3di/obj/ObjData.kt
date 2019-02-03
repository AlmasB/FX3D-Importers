package com.almasb.fx3di.obj

/**
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
internal class ObjData {
    val groups = arrayListOf<ObjGroup>()
    val vertices = arrayListOf<Float>()
    val vertexNormals = arrayListOf<Float>()

    val currentGroup
        get() = groups.last()
}

internal class ObjGroup(val name: String) {

    val faces = arrayListOf<Int>()
}