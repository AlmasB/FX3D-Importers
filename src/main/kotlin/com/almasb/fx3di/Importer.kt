package com.almasb.fx3di

import javafx.scene.Group
import java.net.URL

/**
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
interface Importer {

    fun load(url: URL): Group
}