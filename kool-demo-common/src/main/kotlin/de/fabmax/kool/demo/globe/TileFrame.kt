package de.fabmax.kool.demo.globe

import de.fabmax.kool.math.Mat4d
import de.fabmax.kool.scene.Group
import de.fabmax.kool.scene.doubleprec.TransformGroupDp

class TileFrame(val tileName: TileName, private val globe: Globe) : TransformGroupDp() {

    val transformToLocal: Mat4d get() = invTransform
    val transformToGlobal: Mat4d get() = transform

    val zoomGroups = mutableListOf<Group>()
    var tileCount = 0
        private set

    init {
        rotate(tileName.lonCenter, 0.0, 1.0, 0.0)
        rotate(90.0 - tileName.latCenter, 1.0, 0.0, 0.0)
        translate(0.0, globe.radius, 0.0)
        checkInverse()

        for (i in tileName.zoom..globe.maxZoomLvl) {
            val grp = Group()
            zoomGroups += grp
            +grp
        }
    }

    fun addTile(tile: TileMesh) {
        getZoomGroup(tile.tileName.zoom) += tile
        tileCount++
    }


    fun removeTile(tile: TileMesh) {
        getZoomGroup(tile.tileName.zoom) -= tile
        tileCount--
    }

    private fun getZoomGroup(level: Int) = zoomGroups[level - tileName.zoom]

}