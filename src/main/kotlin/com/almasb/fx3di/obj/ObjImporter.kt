package com.almasb.fx3di.obj

import com.almasb.fx3di.Importer
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
        private val lineParsers = linkedMapOf<(String) -> Boolean, (List<String>, ObjData) -> Unit>()

        init {
            lineParsers[ { it.startsWith("g") }  ] = ::parseGroup
            lineParsers[ { it.startsWith("vn") }  ] = ::parseVertexNormals
            lineParsers[ { it.startsWith("v ") }  ] = ::parseVertices
            lineParsers[ { it.startsWith("f") }  ] = ::parseFaces
        }

        private fun parseGroup(tokens: List<String>, data: ObjData) {
            val groupName = if (tokens.isEmpty()) "default" else tokens[0]

            data.groups += ObjGroup(groupName)
        }

        private fun parseVertexNormals(tokens: List<String>, data: ObjData) {
            data.vertexNormals += tokens.toFloats()
        }

        private fun parseVertices(tokens: List<String>, data: ObjData) {
            // for -Y
            // .mapIndexed { index, fl -> if (index == 1) -fl else fl }
            data.vertices += tokens.toFloats()
        }

        /**
         * Each token is of form v1/(vt1)/(vn1)
         */
        private fun parseFaces(tokens: List<String>, data: ObjData) {
            tokens.forEachIndexed { i, token ->


                // dealing with quads, so add 1,3,4
                if (i == 3) {
                    val innerTokens1 = tokens[0].split("/")
                    val innerTokens3 = tokens[2].split("/")

                    // JavaFX needs vertices, normals and tex
                    data.currentGroup.faces += (innerTokens1[0].toInt() - 1)
                    data.currentGroup.faces += (innerTokens1[2].toInt() - 1)

                    data.currentGroup.faces += 0
                    //data.faces += innerTokens[1].toInt()

                    // JavaFX needs vertices, normals and tex
                    data.currentGroup.faces += (innerTokens3[0].toInt() - 1)
                    data.currentGroup.faces += (innerTokens3[2].toInt() - 1)

                    data.currentGroup.faces += 0
                    //data.faces += innerTokens[1].toInt()
                }


                val innerTokens = token.split("/")

                // TODO: fix if no normals/textures
                if (innerTokens.size == 1) {
                    data.currentGroup.faces += innerTokens[0].toInt() - 1
                    data.currentGroup.faces += 0
                    data.currentGroup.faces += 0

                } else {
                    // JavaFX needs vertices, normals and tex
                    data.currentGroup.faces += (innerTokens[0].toInt() - 1)
                    data.currentGroup.faces += (innerTokens[2].toInt() - 1)

                    data.currentGroup.faces += 0
                    //data.faces += innerTokens[1].toInt()
                }
            }
        }

        private fun List<String>.toFloats(): List<Float> {
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

                println(mesh.points.size())

                // TODO: fix
                mesh.texCoords.addAll(*FloatArray(2) { i -> Math.random().toFloat() })

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