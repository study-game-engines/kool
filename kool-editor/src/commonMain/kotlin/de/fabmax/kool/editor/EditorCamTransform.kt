package de.fabmax.kool.editor

import de.fabmax.kool.KoolContext
import de.fabmax.kool.editor.api.GameEntity
import de.fabmax.kool.input.InputStack
import de.fabmax.kool.input.PointerState
import de.fabmax.kool.math.MutableVec3d
import de.fabmax.kool.math.Vec3d
import de.fabmax.kool.math.expDecay
import de.fabmax.kool.math.spatial.BoundingBoxF
import de.fabmax.kool.math.toVec3d
import de.fabmax.kool.scene.OrbitInputTransform
import kotlin.math.max

class EditorCamTransform(val editor: KoolEditor) : OrbitInputTransform("Editor cam transform") {

    private var panTarget: Vec3d? = null

    init {
        minZoom = 1.0
        maxZoom = 10000.0
        zoom = 50.0
        zoomRotationDecay = 20.0
        setMouseRotation(20f, -30f)
        InputStack.defaultInputHandler.pointerListeners += this

        middleDragMethod = DragMethod.ROTATE
        isInfiniteDragCursor = true

        onUpdate {
            panTarget?.let { animatePan(it) }
        }
    }

    private fun animatePan(target: Vec3d) {
        val pos = transform.getTranslationD(MutableVec3d())
        val diff = MutableVec3d(target).subtract(pos)
        if (diff.length() < 0.001) {
            // target reached
            setMouseTranslation(target.x, target.y, target.z)
            panTarget = null

        } else {
            pos.expDecay(target, zoomRotationDecay)
            setMouseTranslation(pos.x, pos.y, pos.z)
        }
    }

    fun focusSelectedObject() = focusObjects(editor.selectionOverlay.getSelectedSceneNodes())

    fun focusObject(gameEntity: GameEntity) = focusObjects(listOf(gameEntity))

    fun focusObjects(gameEntities: List<GameEntity>) {
        val bounds = BoundingBoxF()
        gameEntities.forEach { nodeModel ->
            val c = nodeModel.drawNode.globalCenter
            val r = max(1f, nodeModel.drawNode.globalRadius)
            bounds.add(c, r)
        }

        if (bounds.isNotEmpty) {
            panTarget = bounds.center.toVec3d()
            zoom = bounds.size.length().toDouble() * 0.7
        }
    }

    override fun handlePointer(pointerState: PointerState, ctx: KoolContext) {
        if (pointerState.primaryPointer.isAnyButtonDown) {
            // stop any ongoing animated pan
            panTarget = null
        }
        super.handlePointer(pointerState, ctx)
    }
}