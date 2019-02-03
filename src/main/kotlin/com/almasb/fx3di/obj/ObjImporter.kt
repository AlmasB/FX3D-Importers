package com.almasb.fx3di.obj

import com.almasb.fx3di.Importer
import javafx.scene.AmbientLight
import javafx.scene.Group
import javafx.scene.paint.Color
import javafx.scene.paint.PhongMaterial
import javafx.scene.shape.CullFace
import javafx.scene.shape.MeshView
import javafx.scene.shape.TriangleMesh
import javafx.scene.shape.VertexFormat
import java.lang.Exception
import java.net.URL

/**
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class ObjImporter : Importer {

    companion object {
        private val objParsers = linkedMapOf<(String) -> Boolean, (List<String>, ObjData) -> Unit>()
        private val mtlParsers = linkedMapOf<(String) -> Boolean, (List<String>, MtlData) -> Unit>()

        init {
            objParsers[ { it.startsWith("g") }  ] = ::parseGroup
            objParsers[ { it.startsWith("vt") }  ] = ::parseVertexTextures
            objParsers[ { it.startsWith("vn") }  ] = ::parseVertexNormals
            objParsers[ { it.startsWith("v ") }  ] = ::parseVertices
            objParsers[ { it.startsWith("f") }  ] = ::parseFaces
            objParsers[ { it.startsWith("mtllib") }  ] = ::parseMaterialLib
            objParsers[ { it.startsWith("usemtl") }  ] = ::parseUseMaterial

            mtlParsers[ { it.startsWith("newmtl") }  ] = ::parseNewMaterial
            mtlParsers[ { it.startsWith("Ka") }  ] = ::parseColorAmbient
            mtlParsers[ { it.startsWith("Kd") }  ] = ::parseColorDiffuse
            mtlParsers[ { it.startsWith("Ks") }  ] = ::parseColorSpecular
            mtlParsers[ { it.startsWith("Ns") }  ] = ::parseSpecularPower
        }

        private fun parseGroup(tokens: List<String>, data: ObjData) {
            val groupName = if (tokens.isEmpty()) "default" else tokens[0]

            data.groups += ObjGroup(groupName)
        }

        private fun parseVertexTextures(tokens: List<String>, data: ObjData) {
            data.vertexTextures += tokens.toFloats2()
        }

        private fun parseVertexNormals(tokens: List<String>, data: ObjData) {
            data.vertexNormals += tokens.toFloats3()
        }

        private fun parseVertices(tokens: List<String>, data: ObjData) {
            // for -Y
            // .mapIndexed { index, fl -> if (index == 1) -fl else fl }
            data.vertices += tokens.toFloats3()
        }

        private fun parseFaces(tokens: List<String>, data: ObjData) {
            if (tokens.size > 3) {
                for (i in 2 until tokens.size) {
                    parseFaceVertex(tokens[0], data)
                    parseFaceVertex(tokens[i-1], data)
                    parseFaceVertex(tokens[i], data)
                }
            } else {
                tokens.forEach { token ->
                    parseFaceVertex(token, data)
                }
            }
        }

        /**
         * Each token is of form v1/(vt1)/(vn1).
         * Case v1
         * Case v1/vt1
         * Case v1//n1
         * Case v1/vt1/vn1
         */
        private fun parseFaceVertex(token: String, data: ObjData) {
            val faceVertex = token.split("/")

            // JavaFX format is vertices, normals and tex
            when (faceVertex.size) {
                // f v1
                1 -> {
                    data.currentGroup.currentSubGroup.faces += faceVertex[0].toInt() - 1
                    data.currentGroup.currentSubGroup.faces += 0
                    data.currentGroup.currentSubGroup.faces += 0
                }

                // f v1/vt1
                2 -> {
                    data.currentGroup.currentSubGroup.faces += faceVertex[0].toInt() - 1
                    data.currentGroup.currentSubGroup.faces += 0
                    data.currentGroup.currentSubGroup.faces += faceVertex[1].toInt() - 1
                }

                // f v1//vn1
                // f v1/vt1/vn1
                3 -> {
                    data.currentGroup.currentSubGroup.faces += faceVertex[0].toInt() - 1
                    data.currentGroup.currentSubGroup.faces += faceVertex[2].toInt() - 1
                    data.currentGroup.currentSubGroup.faces += (faceVertex[1].toIntOrNull() ?: 1) - 1
                }
            }
        }

        private fun parseMaterialLib(tokens: List<String>, data: ObjData) {
            val fileName = tokens[0]
            val mtlURL = URL(data.url.toExternalForm().substringBeforeLast('/') + '/' + fileName)

            val mtlData = loadMtlData(mtlURL)

            data.materials += mtlData.materials
            data.ambientColors += mtlData.ambientColors
        }

        private fun parseUseMaterial(tokens: List<String>, data: ObjData) {
            data.currentGroup.subGroups += SubGroup()

            data.currentGroup.currentSubGroup.material = data.materials[tokens[0]]
                    ?: throw RuntimeException("Material with name ${tokens[0]} not found")

            data.currentGroup.currentSubGroup.ambientColor = data.ambientColors[data.currentGroup.currentSubGroup.material]
        }

        private fun List<String>.toFloats2(): List<Float> {
            return this.take(2).map { it.toFloat() }
        }

        private fun List<String>.toFloats3(): List<Float> {
            return this.take(3).map { it.toFloat() }
        }

        private fun List<String>.toColor(): Color {
            val rgb = this.toFloats3().map { if (it > 1.0) 1.0 else it.toDouble() }
            return Color.color(rgb[0], rgb[1], rgb[2])
        }

        private fun parseNewMaterial(tokens: List<String>, data: MtlData) {
            data.currentMaterial = PhongMaterial()
            data.materials[tokens[0]] = data.currentMaterial
        }

        private fun parseColorAmbient(tokens: List<String>, data: MtlData) {
            data.ambientColors[data.currentMaterial] = tokens.toColor()
        }

        private fun parseColorDiffuse(tokens: List<String>, data: MtlData) {
            data.currentMaterial.diffuseColor = tokens.toColor()
        }

        private fun parseColorSpecular(tokens: List<String>, data: MtlData) {
            data.currentMaterial.specularColor = tokens.toColor()
        }

        private fun parseSpecularPower(tokens: List<String>, data: MtlData) {
            data.currentMaterial.specularPower = tokens[0].toDouble()
        }

        // TODO: refactor loadXXXData below
        private fun loadObjData(url: URL): ObjData {
            val data = ObjData(url)

            url.openStream().bufferedReader().useLines {
                it.forEach { line ->

                    for ((condition, action) in objParsers) {
                        if (condition.invoke(line) ) {
                            // drop identifier
                            val tokens = line.split(" +".toRegex()).drop(1)

                            action.invoke(tokens, data)
                            break
                        }
                    }
                }
            }

            return data
        }

        private fun loadMtlData(url: URL): MtlData {
            val data = MtlData()

            url.openStream().bufferedReader().useLines {
                it.forEach { line ->

                    for ((condition, action) in mtlParsers) {
                        if (condition.invoke(line) ) {
                            // drop identifier
                            val tokens = line.split(" +".toRegex()).drop(1)

                            action.invoke(tokens, data)
                            break
                        }
                    }
                }
            }

            return data
        }
    }

    override fun load(url: URL): Group {
        try {
            val data = loadObjData(url)
            val modelRoot = Group()

            data.groups.forEach {
                println("Group: ${it.name}")

                val groupRoot = Group()
                groupRoot.properties["name"] = it.name

                it.subGroups.forEach {

                    // TODO: ?
                    if (!it.faces.isEmpty()) {

                        val subGroupRoot = Group()

                        val mesh = TriangleMesh(VertexFormat.POINT_NORMAL_TEXCOORD)

                        mesh.points.addAll(*data.vertices.map { it * 0.05f }.toFloatArray())

                        // if there are no vertex textures, just add 2 values
                        if (data.vertexTextures.isEmpty()) {
                            mesh.texCoords.addAll(*FloatArray(2) { _ -> 0.0f })
                        } else {
                            mesh.texCoords.addAll(*data.vertexTextures.toFloatArray())
                        }

                        // if there are no vertex normals, just add 3 values
                        if (data.vertexNormals.isEmpty()) {
                            mesh.normals.addAll(*FloatArray(3) { _ -> 0.0f })
                        } else {
                            mesh.normals.addAll(*data.vertexNormals.toFloatArray())
                        }

                        mesh.faces.addAll(*it.faces.toIntArray())

                        val view = MeshView(mesh)
                        view.material = it.material
                        view.cullFace = CullFace.NONE

                        subGroupRoot.children.addAll(view)

                        it.ambientColor?.let {
                            val light = AmbientLight()
                            light.color = it

                            subGroupRoot.children.addAll(light)
                        }

                        groupRoot.children += view
                    }
                }

                modelRoot.children += groupRoot
            }

            return modelRoot
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Load failed for URL: $url Error: $e")
        }
    }
}