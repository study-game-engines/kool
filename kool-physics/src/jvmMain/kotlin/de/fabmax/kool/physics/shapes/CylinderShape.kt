package de.fabmax.kool.physics.shapes

import de.fabmax.kool.math.MutableVec4f
import de.fabmax.kool.physics.BtCylinderShape
import de.fabmax.kool.util.BoundingBox
import javax.vecmath.Vector3f

actual class CylinderShape actual constructor(height: Float, radius: Float) : CommonCylinderShape(height, radius), CollisionShape {

    override val btShape: BtCylinderShape

    init {
        val halfExtents = Vector3f(radius, height / 2, radius)
        btShape = BtCylinderShape(halfExtents)
    }

    private val boundsHelper = ShapeBoundsHelper(btShape)
    override fun getAabb(result: BoundingBox) = boundsHelper.getAabb(result)
    override fun getBoundingSphere(result: MutableVec4f) = boundsHelper.getBoundingSphere(result)

}