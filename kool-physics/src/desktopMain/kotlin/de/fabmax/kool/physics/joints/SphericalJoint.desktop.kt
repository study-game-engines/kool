package de.fabmax.kool.physics.joints

import de.fabmax.kool.math.Mat4f
import de.fabmax.kool.physics.PhysicsImpl
import de.fabmax.kool.physics.RigidActor
import de.fabmax.kool.physics.createPxTransform
import de.fabmax.kool.physics.toPxTransform
import org.lwjgl.system.MemoryStack
import physx.PxTopLevelFunctions
import physx.extensions.PxJointLimitCone
import physx.extensions.PxSphericalJoint
import physx.extensions.PxSphericalJointFlagEnum
import physx.extensions.PxSpring

actual fun SphericalJoint(bodyA: RigidActor, bodyB: RigidActor, frameA: Mat4f, frameB: Mat4f): SphericalJoint {
    return SphericalJointImpl(bodyA, bodyB, frameA, frameB)
}

class SphericalJointImpl(
    override val bodyA: RigidActor,
    override val bodyB: RigidActor,
    frameA: Mat4f,
    frameB: Mat4f
) : JointImpl(frameA, frameB), SphericalJoint {

    override val joint: PxSphericalJoint

    init {
        MemoryStack.stackPush().use { mem ->
            val frmA = frameA.toPxTransform(mem.createPxTransform())
            val frmB = frameB.toPxTransform(mem.createPxTransform())
            joint = PxTopLevelFunctions.SphericalJointCreate(PhysicsImpl.physics, bodyA.holder, frmA, bodyB.holder, frmB)
        }
    }

    override fun setSoftLimitCone(yLimitAngle: Float, zLimitAngle: Float, stiffness: Float, damping: Float) {
        joint.setLimitCone(PxJointLimitCone(yLimitAngle, zLimitAngle, PxSpring(stiffness, damping)))
        joint.setSphericalJointFlag(PxSphericalJointFlagEnum.eLIMIT_ENABLED, true)
    }

    override fun removeLimitCone() {
        joint.setSphericalJointFlag(PxSphericalJointFlagEnum.eLIMIT_ENABLED, false)
    }
}