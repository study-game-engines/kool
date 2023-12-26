package de.fabmax.kool.physics.geometry

import de.fabmax.kool.physics.PhysicsImpl
import de.fabmax.kool.physics.createPxHeightFieldDesc
import de.fabmax.kool.physics.createPxHeightFieldSample
import de.fabmax.kool.physics.createPxMeshGeometryFlags
import de.fabmax.kool.util.HeightMap
import org.lwjgl.system.MemoryStack
import physx.PxTopLevelFunctions
import physx.geometry.PxHeightField
import physx.geometry.PxHeightFieldFormatEnum
import physx.geometry.PxHeightFieldGeometry
import physx.geometry.PxHeightFieldSample
import physx.support.Vector_PxHeightFieldSample
import kotlin.math.max
import kotlin.math.roundToInt

actual fun HeightField(heightMap: HeightMap, rowScale: Float, columnScale: Float): HeightField {
    return HeightFieldImpl(heightMap, rowScale, columnScale)
}

val HeightField.pxHeightField: PxHeightField get() = (this as HeightFieldImpl).pxHeightField

class HeightFieldImpl(
    override val heightMap: HeightMap,
    override val rowScale: Float,
    override val columnScale: Float
) : HeightField() {

    val pxHeightField: PxHeightField
    override val heightScale: Float

    override var releaseWithGeometry = true
    internal var refCnt = 0

    init {
        val maxAbsHeight = max(-heightMap.minHeight, heightMap.maxHeight)
        heightScale = maxAbsHeight / 32767f
        val revHeightToI16 = if (heightScale > 0) 1f / heightScale else 0f

        PhysicsImpl.checkIsLoaded()
        MemoryStack.stackPush().use { mem ->
            val rows = heightMap.width
            val cols = heightMap.height
            val sample = mem.createPxHeightFieldSample()
            val samples = Vector_PxHeightFieldSample()
            for (row in 0..rows) {
                for (col in (cols-1) downTo 0) {
                    sample.height = (heightMap.getHeight(row, col) * revHeightToI16).roundToInt().toShort()
                    if (row % 2 != col % 2) {
                        sample.clearTessFlag()
                    } else {
                        sample.setTessFlag()
                    }
                    samples.push_back(sample)
                }
            }

            val desc = mem.createPxHeightFieldDesc()
            desc.format = PxHeightFieldFormatEnum.eS16_TM
            desc.nbRows = rows
            desc.nbColumns = cols
            desc.samples.data = samples.data()
            desc.samples.stride = PxHeightFieldSample.SIZEOF

            pxHeightField = PxTopLevelFunctions.CreateHeightField(desc)
        }
    }

    /**
     * Only use this if [releaseWithGeometry] is false. Releases the underlying PhysX mesh.
     */
    override fun release() {
        super.release()
        pxHeightField.release()
    }
}

class HeightFieldGeometryImpl(override val heightField: HeightField) : CollisionGeometryImpl(), HeightFieldGeometry {
    override val holder: PxHeightFieldGeometry

    init {
        PhysicsImpl.checkIsLoaded()
        MemoryStack.stackPush().use { mem ->
            val flags = mem.createPxMeshGeometryFlags(0)
            holder = PxHeightFieldGeometry(heightField.pxHeightField, flags, heightField.heightScale, heightField.rowScale, heightField.columnScale)
        }

        if (heightField.releaseWithGeometry) {
            heightField as HeightFieldImpl
            if (heightField.refCnt > 0) {
                // PxHeightField starts with a ref count of 1, only increment it if this is not the first
                // geometry which uses it
                heightField.pxHeightField.acquireReference()
            }
            heightField.refCnt++
        }
    }
}