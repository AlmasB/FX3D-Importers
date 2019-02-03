package com.almasb.fx3di.obj

import com.almasb.fx3di.Importer
import javafx.scene.Group
import javafx.scene.paint.Color
import javafx.scene.paint.PhongMaterial
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
        private val lineParsers = linkedMapOf<(String) -> Boolean, (List<String>, ObjData) -> Unit>()

        init {
            lineParsers[ { it.startsWith("g") }  ] = ::parseGroup
            lineParsers[ { it.startsWith("vt") }  ] = ::parseVertexTextures
            lineParsers[ { it.startsWith("vn") }  ] = ::parseVertexNormals
            lineParsers[ { it.startsWith("v ") }  ] = ::parseVertices
            lineParsers[ { it.startsWith("f") }  ] = ::parseFaces
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
            tokens.forEachIndexed { i, token ->

                // dealing with a quad, so before parsing 4th face vertex
                // add 1st (0) and 3rd (2) face vertices
                if (i == 3) {
                    parseFaceVertex(tokens[0], data)
                    parseFaceVertex(tokens[2], data)
                }

                parseFaceVertex(token, data)
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
                    data.currentGroup.faces += faceVertex[0].toInt() - 1
                    data.currentGroup.faces += 0
                    data.currentGroup.faces += 0
                }

                // f v1/vt1
                2 -> {
                    data.currentGroup.faces += faceVertex[0].toInt() - 1
                    data.currentGroup.faces += 0
                    data.currentGroup.faces += faceVertex[1].toInt() - 1
                }

                // f v1//vn1
                // f v1/vt1/vn1
                3 -> {
                    data.currentGroup.faces += faceVertex[0].toInt() - 1
                    data.currentGroup.faces += faceVertex[2].toInt() - 1
                    data.currentGroup.faces += (faceVertex[1].toIntOrNull() ?: 1) - 1
                }
            }
        }

        private fun List<String>.toFloats2(): List<Float> {
            return this.take(2).map { it.toFloat() }
        }

        private fun List<String>.toFloats3(): List<Float> {
            return this.take(3).map { it.toFloat() }
        }
    }

    override fun load(url: URL): Group {
        try {
            val data = loadObjData(url)
            val modelRoot = Group()

            data.groups.forEach {
                println("Group: ${it.name}")

                val mesh = TriangleMesh(VertexFormat.POINT_NORMAL_TEXCOORD)

                mesh.points.addAll(*data.vertices.map { it * 1 }.toFloatArray())

                // if there are no vertex textures, just add 2 values
                if (data.vertexTextures.isEmpty()) {
                    mesh.texCoords.addAll(*FloatArray(2) { _ -> 0.0f})
                } else {
                    mesh.texCoords.addAll(*data.vertexTextures.toFloatArray())
                }

                mesh.normals.addAll(*data.vertexNormals.toFloatArray())
                mesh.faces.addAll(*it.faces.toIntArray())

                val view = MeshView(mesh)
                //view.cullFace = CullFace.NONE
                view.material = PhongMaterial(Color.BLUE)

                modelRoot.children += view
            }

            return modelRoot
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Load failed for URL: $url Error: $e")
        }
    }

    private fun loadObjData(url: URL): ObjData {
        val data = ObjData()

        url.openStream().bufferedReader().useLines {
            it.forEach { line ->

                for ((condition, action) in lineParsers) {
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