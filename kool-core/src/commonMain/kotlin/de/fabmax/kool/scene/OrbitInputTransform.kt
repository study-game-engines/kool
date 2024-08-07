package de.fabmax.kool.scene

import de.fabmax.kool.KoolContext
import de.fabmax.kool.input.CursorMode
import de.fabmax.kool.input.InputStack
import de.fabmax.kool.input.PointerInput
import de.fabmax.kool.input.PointerState
import de.fabmax.kool.math.*
import de.fabmax.kool.math.spatial.BoundingBoxD
import de.fabmax.kool.pipeline.RenderPass
import de.fabmax.kool.scene.animation.ExponentialDecayDouble
import de.fabmax.kool.util.Time
import kotlin.math.abs

/**
 * A special kind of transform group which translates mouse input into a orbit transform. This is mainly useful
 * for camera manipulation.
 *
 * @author fabmax
 */

fun orbitCamera(view: RenderPass.View, name: String? = null, block: OrbitInputTransform.() -> Unit): OrbitInputTransform {
    val orbitCam = OrbitInputTransform(name)
    orbitCam.addNode(view.camera)
    orbitCam.block()
    view.drawNode.addNode(orbitCam)

    InputStack.defaultInputHandler.pointerListeners += orbitCam
    orbitCam.onRelease {
        InputStack.defaultInputHandler.pointerListeners -= orbitCam
    }
    return orbitCam
}

fun Scene.orbitCamera(name: String? = null, block: OrbitInputTransform.() -> Unit): OrbitInputTransform {
    return orbitCamera(mainRenderPass.screenView, name, block)
}

fun Scene.defaultOrbitCamera(yaw: Float = 20f, pitch: Float = -30f): OrbitInputTransform {
    return orbitCamera {
        setMouseRotation(yaw, pitch)
    }
}

open class OrbitInputTransform(name: String? = null) : Node(name), InputStack.PointerListener {
    var leftDragMethod = DragMethod.ROTATE
    var middleDragMethod = DragMethod.NONE
    var rightDragMethod = DragMethod.PAN
    var zoomMethod = ZoomMethod.ZOOM_CENTER
    var isConsumingPtr = true

    var verticalAxis = Vec3d.Y_AXIS
    var horizontalAxis = Vec3d.X_AXIS
    var minHorizontalRot = -90.0
    var maxHorizontalRot = 90.0

    val translation = MutableVec3d()
    var verticalRotation = 0.0
    var horizontalRotation = 0.0
    var zoom = 10.0
        set(value) {
            field = value.clamp(minZoom, maxZoom)
        }

    var isKeepingStandardTransform = false
    var isInfiniteDragCursor = false

    var invertRotX = false
    var invertRotY = false

    var minZoom = 1.0
    var maxZoom = 100.0
    var translationBounds: BoundingBoxD? = null

    var panMethod: PanBase = CameraOrthogonalPan()

    val vertRotAnimator = ExponentialDecayDouble(0.0)
    val horiRotAnimator = ExponentialDecayDouble(0.0)
    val zoomAnimator = ExponentialDecayDouble(zoom)

    private var prevButtonMask = 0
    private var dragMethod = DragMethod.NONE
    private var prevDragMethod = DragMethod.NONE
    private var dragStart = false
    private val deltaPos = MutableVec2d()
    private var deltaScroll = 0.0

    private val ptrPos = MutableVec2d()
    private val panPlane = PlaneD()
    private val pointerHitStart = MutableVec3d()
    private val pointerHit = MutableVec3d()
    private val tmpVec1 = MutableVec3d()
    private val tmpVec2 = MutableVec3d()

    private val mouseTransform = MutableMat4d()
    private val mouseTransformInv = MutableMat4d()

    private val matrixTransform: MatrixTransformD
        get() = transform as MatrixTransformD

    var panSmoothness: Double = 0.5
    var zoomRotationDecay: Double = 0.0
        set(value) {
            field = value
            vertRotAnimator.decay = value
            horiRotAnimator.decay = value
            zoomAnimator.decay = value
        }

    init {
        transform = MatrixTransformD()
        zoomRotationDecay = 16.0
        panPlane.p.set(Vec3f.ZERO)
        panPlane.n.set(Vec3f.Y_AXIS)

        onUpdate += { (view, ctx) ->
            doCamTransform(view, ctx)
        }
    }

    fun setMouseRotation(yaw: Float, pitch: Float) = setMouseRotation(yaw.toDouble(), pitch.toDouble())

    fun setMouseRotation(yaw: Double, pitch: Double) {
        vertRotAnimator.set(yaw)
        horiRotAnimator.set(pitch)
        verticalRotation = yaw
        horizontalRotation = pitch
    }

    fun setMouseTranslation(x: Float, y: Float, z: Float) = setMouseTranslation(x.toDouble(), y.toDouble(), z.toDouble())

    fun setMouseTranslation(x: Double, y: Double, z: Double) {
        translation.set(x, y, z)
    }

    fun setZoom(newZoom: Float) = setZoom(newZoom.toDouble())

    fun setZoom(newZoom: Double, min: Double = minZoom, max: Double = maxZoom) {
        zoom = newZoom
        zoomAnimator.set(zoom)
        minZoom = min
        maxZoom = max
    }

    fun updateTransform() {
        translationBounds?.clampToBounds(translation)

        if (isKeepingStandardTransform) {
            mouseTransform.invert(mouseTransformInv)
            matrixTransform.mul(mouseTransformInv)
        }

        val z = zoomAnimator.actual
        val vr = vertRotAnimator.actual
        val hr = horiRotAnimator.actual
        mouseTransform.setIdentity()
        mouseTransform.translate(translation.x, translation.y, translation.z)
        mouseTransform.scale(z)
        mouseTransform.rotate(vr.deg, verticalAxis)
        mouseTransform.rotate(hr.deg, horizontalAxis)

        if (isKeepingStandardTransform) {
            matrixTransform.mul(mouseTransform)
        } else {
            matrixTransform.setMatrix(mouseTransform)
        }
    }

    private fun doCamTransform(view: RenderPass.View, ctx: KoolContext) {
        if (dragMethod == DragMethod.PAN && panMethod.computePanPoint(pointerHit, view, ptrPos, ctx)) {
            if (dragStart) {
                dragStart = false
                pointerHitStart.set(pointerHit)

                // stop any ongoing smooth motion, as we start a new one
                stopSmoothMotion()

            } else {
                val s = (1 - panSmoothness).clamp(0.1, 1.0)
                tmpVec1.set(pointerHitStart).subtract(pointerHit).mul(s)
                parent?.toLocalCoords(tmpVec1, 0.0)

                // limit panning speed
                val tLen = tmpVec1.length()
                if (tLen > view.camera.globalRange * 0.5f) {
                    tmpVec1.mul(view.camera.globalRange * 0.5f / tLen)
                }

                translation.add(tmpVec1)
            }
        } else {
            pointerHit.set(view.camera.globalLookAt)
        }

        if (!deltaScroll.isFuzzyZero()) {
            zoom *= 1f - deltaScroll / 10f
            deltaScroll = 0.0
        }

        if (dragMethod == DragMethod.ROTATE) {
            verticalRotation -= deltaPos.x / 3 * if (invertRotX) { -1f } else { 1f }
            horizontalRotation -= deltaPos.y / 3 * if (invertRotY) { -1f } else { 1f }
            horizontalRotation = horizontalRotation.clamp(minHorizontalRot, maxHorizontalRot)
            deltaPos.set(Vec2d.ZERO)
        }

        vertRotAnimator.desired = verticalRotation
        horiRotAnimator.desired = horizontalRotation
        zoomAnimator.desired = zoom

        val oldZ = zoomAnimator.actual
        val z = zoomAnimator.animate(Time.deltaT)
        if (!isFuzzyEqual(oldZ, z)
                && zoomMethod == ZoomMethod.ZOOM_TRANSLATE
                && panMethod.computePanPoint(pointerHit, view, ptrPos, ctx)) {
            computeZoomTranslationPerspective(view.camera, oldZ, z)
        }

        vertRotAnimator.animate(Time.deltaT)
        horiRotAnimator.animate(Time.deltaT)
        updateTransform()
    }

    /**
     * Computes the required camera translation so that the camera zooms to the point under the pointer (only works
     * with perspective cameras)
     */
    protected open fun computeZoomTranslationPerspective(camera: Camera, oldZoom: Double, newZoom: Double) {
        // tmpVec1 = zoomed pos on pointer ray
        val s = newZoom / oldZoom
        camera.dataD.globalPos.subtract(pointerHit, tmpVec1).mul(s).add(pointerHit)
        // tmpVec2 = zoomed pos on view center ray
        camera.dataD.globalPos.subtract(camera.dataD.globalLookAt, tmpVec2).mul(s)
            .add(camera.globalLookAt)
        tmpVec1.subtract(tmpVec2)
        parent?.toLocalCoords(tmpVec1, 0.0)
        translation.add(tmpVec1)
    }

    private fun stopSmoothMotion() {
        vertRotAnimator.set(vertRotAnimator.actual)
        horiRotAnimator.set(horiRotAnimator.actual)
        zoomAnimator.set(zoomAnimator.actual)

        verticalRotation = vertRotAnimator.actual
        horizontalRotation = horiRotAnimator.actual
        zoom = zoomAnimator.actual
    }

    override fun handlePointer(pointerState: PointerState, ctx: KoolContext) {
        if (!isVisible) {
            return
        }

        val dragPtr = pointerState.primaryPointer
        if (dragPtr.isConsumed()) {
            deltaPos.set(Vec2d.ZERO)
            deltaScroll = 0.0
            return
        }

        if (isInfiniteDragCursor) {
            if (dragPtr.isDrag && dragMethod != DragMethod.NONE && PointerInput.cursorMode != CursorMode.LOCKED) {
                PointerInput.cursorMode = CursorMode.LOCKED
            } else if (prevDragMethod != DragMethod.NONE && dragMethod == DragMethod.NONE) {
                PointerInput.cursorMode = CursorMode.NORMAL
            }
        }

        prevDragMethod = dragMethod
        if (dragPtr.buttonEventMask != 0 || dragPtr.buttonMask != prevButtonMask) {
            dragMethod = when {
                dragPtr.isLeftButtonDown -> leftDragMethod
                dragPtr.isRightButtonDown -> rightDragMethod
                dragPtr.isMiddleButtonDown -> middleDragMethod
                else -> DragMethod.NONE
            }
            dragStart = dragMethod != DragMethod.NONE
        }

        prevButtonMask = dragPtr.buttonMask
        ptrPos.set(dragPtr.x, dragPtr.y)
        deltaPos.set(dragPtr.deltaX, dragPtr.deltaY)
        deltaScroll = dragPtr.deltaScroll
        if (isConsumingPtr) {
            dragPtr.consume()
        }
    }

    private fun MutableVec3d.add(v: Vec3f) {
        x += v.x
        y += v.y
        z += v.z
    }

    enum class DragMethod {
        NONE,
        ROTATE,
        PAN
    }

    enum class ZoomMethod {
        ZOOM_CENTER,
        ZOOM_TRANSLATE
    }
}

abstract class PanBase {
    abstract fun computePanPoint(result: MutableVec3d, view: RenderPass.View, ptrPos: Vec2d, ctx: KoolContext): Boolean
}

class CameraOrthogonalPan : PanBase() {
    val panPlane = PlaneD()
    private val pointerRay = RayD()

    override fun computePanPoint(result: MutableVec3d, view: RenderPass.View, ptrPos: Vec2d, ctx: KoolContext): Boolean {
        panPlane.p.set(view.camera.globalLookAt)
        panPlane.n.set(view.camera.globalLookDir)
        return view.camera.computePickRay(pointerRay, ptrPos.x.toFloat(), ptrPos.y.toFloat(), view.viewport) &&
                panPlane.intersectionPoint(pointerRay, result)
    }
}

class FixedPlanePan(planeNormal: Vec3f) : PanBase() {
    val panPlane = PlaneD()
    private val pointerRay = RayD()

    init {
        panPlane.n.set(planeNormal)
    }

    override fun computePanPoint(result: MutableVec3d, view: RenderPass.View, ptrPos: Vec2d, ctx: KoolContext): Boolean {
        panPlane.p.set(view.camera.globalLookAt)
        if (!view.camera.computePickRay(pointerRay, ptrPos.x.toFloat(), ptrPos.y.toFloat(), view.viewport)) {
            return false
        }
        if (abs(pointerRay.direction dot panPlane.n) < 0.01) {
            return false
        }
        return panPlane.intersectionPoint(pointerRay, result)
    }
}

fun xPlanePan() = FixedPlanePan(Vec3f.X_AXIS)
fun yPlanePan() = FixedPlanePan(Vec3f.Y_AXIS)
fun zPlanePan() = FixedPlanePan(Vec3f.Z_AXIS)
