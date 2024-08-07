package de.fabmax.kool.editor.components

import de.fabmax.kool.editor.api.GameEntity
import de.fabmax.kool.editor.data.ComponentData
import de.fabmax.kool.editor.data.ComponentInfo
import de.fabmax.kool.editor.data.TransformComponentData
import de.fabmax.kool.math.*
import de.fabmax.kool.physics.PhysicsWorld
import de.fabmax.kool.scene.TrsTransformF
import de.fabmax.kool.scene.set
import de.fabmax.kool.util.logW

abstract class PhysicsComponent<T: ComponentData>(
    gameEntity: GameEntity,
    componentInfo: ComponentInfo<T>
) :
    GameEntityDataComponent<T>(gameEntity, componentInfo),
    TransformComponent.ListenerComponent
{

    val physicsWorldComponent: PhysicsWorldComponent?
        get() = gameEntity.scene.sceneEntity.getComponent<PhysicsWorldComponent>()
    val physicsWorld: PhysicsWorld?
        get() = physicsWorldComponent?.physicsWorld

    abstract val globalActorTransform: TrsTransformF?
    val localActorTransform = TrsTransformF()

    private val tmpMat4 = MutableMat4d()
    protected val scale = MutableVec3d(Vec3d.ONES)

    protected var warnOnNonUniformScale = true

    override suspend fun applyComponent() {
        super.applyComponent()
        localActorTransform.set(gameEntity.transform.transform)
        gameEntity.transform.transform = localActorTransform
        gameEntity.drawNode.updateModelMat()
    }

    override fun onStart() {
        super.onStart()
        setPhysicsTransformFromDrawNode()
    }

    override fun onPhysicsUpdate(timeStep: Float) {
        val globalTrs = globalActorTransform ?: return

        gameEntity.parent!!.drawNode.invModelMatD.mul(globalTrs.matrixD, tmpMat4)
        tmpMat4.scale(scale)
        localActorTransform.setMatrix(tmpMat4)
    }

    suspend fun getOrCreatePhysicsWorldComponent(): PhysicsWorldComponent {
        val sceneEntity = gameEntity.scene.sceneEntity
        val physicsWorldComponent = sceneEntity.getOrPutComponentLifecycleAware<PhysicsWorldComponent> {
            PhysicsWorldComponent(sceneEntity)
        }
        return physicsWorldComponent
    }

    override fun onTransformChanged(component: TransformComponent, transformData: TransformComponentData) {
        setPhysicsTransformFromDrawNode()
    }

    fun setPhysicsTransformFromDrawNode() {
        val t = MutableVec3d()
        val r = MutableQuatD()
        val s = MutableVec3d()
        gameEntity.drawNode.modelMatD.decompose(t, r, s)

        if (warnOnNonUniformScale && !s.isFuzzyEqual(Vec3d.ONES, eps = 1e-3)) {
            logW { "${gameEntity.name} / ${this::class.simpleName}: transform contains a scaling component $s, which may lead to unexpected behavior." }
        }
        applyPose(t, r)
    }

    protected abstract fun applyPose(position: Vec3d, rotation: QuatD)
}