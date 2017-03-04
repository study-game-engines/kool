package de.fabmax.kool.scene.ui

import de.fabmax.kool.platform.GL
import de.fabmax.kool.platform.RenderContext
import de.fabmax.kool.scene.Group
import de.fabmax.kool.scene.Mesh
import de.fabmax.kool.scene.MeshData
import de.fabmax.kool.shading.BasicShader
import de.fabmax.kool.shading.ColorModel
import de.fabmax.kool.shading.LightModel
import de.fabmax.kool.util.*

/**
 * @author fabmax
 */

open class UiPanel(name: String? = null) : Group(name), UiNode {

    override var layoutSpec = LayoutSpec()
    override val contentBounds = BoundingBox()

    protected var isUpdateNeeded = true

    protected val contentMesh: Mesh
    protected val contentMeshData = MeshData(true, true, true)
    protected val contentMeshBuilder = MeshBuilder(contentMeshData)

    var panelText = ""

    var font = Font.DEFAULT_FONT
        set(value) {
            field = value
            isUpdateNeeded = true
        }

    var backgroundColor = Color.WHITE
        set(value) {
            field = value
            isUpdateNeeded = true
        }

    init {
        contentMeshData.usage = GL.DYNAMIC_DRAW
        contentMesh = Mesh(contentMeshData)
        contentMesh.shader = fontShader(Font.DEFAULT_FONT) {
            lightModel = LightModel.PHONG_LIGHTING
            colorModel = ColorModel.VERTEX_COLOR
        }
        addNode(contentMesh)
    }

    override fun onLayout(bounds: BoundingBox, ctx: RenderContext) {
        if (!contentBounds.isEqual(bounds)) {
            contentBounds.set(bounds)
            isUpdateNeeded = true
        }
    }

    override fun render(ctx: RenderContext) {
        if (isUpdateNeeded) {
            update(ctx)
        }
        super.render(ctx)
    }

    protected open fun update(ctx: RenderContext) {
        isUpdateNeeded = false

        val shader = contentMesh.shader
        if (shader != null && shader is BasicShader) {
            shader.texture = font.texture
        }
        contentMeshData.clear()

        contentMeshBuilder.identity()
        contentMeshBuilder.translate(contentBounds.min)
        drawBackground(contentMeshBuilder, ctx)
    }

    protected open fun drawBackground(builder: MeshBuilder, ctx: RenderContext) {
        builder.run {
            color = backgroundColor
            rect {
                width = this@UiPanel.width
                height = this@UiPanel.height
            }

            color = Color.BLACK
            translate(pcR(50f, width), pcR(50f, height), 0f)
            text(font) {
                text = panelText
                origin.x = -font.textWidth(text) * 0.5f
                origin.y = -font.sizeUnits * 0.35f
            }
        }
    }

    override fun rayTest(test: RayTest) {
        val hitNode = test.hitNode
        super.rayTest(test)
        if (hitNode != test.hitNode) {
            // an element of this panel was hit!
            test.hitNode = this
            test.hitPositionLocal.subtract(bounds.min)
        }
    }
}