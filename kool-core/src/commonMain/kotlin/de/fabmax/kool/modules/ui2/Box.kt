package de.fabmax.kool.modules.ui2

inline fun UiScope.Column(
    width: Dimension = FitContent,
    height: Dimension = FitContent,
    block: UiScope.() -> Unit
) = Box(ColumnLayout, width, height, block)

inline fun UiScope.Row(
    width: Dimension = FitContent,
    height: Dimension = FitContent,
    block: UiScope.() -> Unit
) = Box(RowLayout, width, height, block)

inline fun UiScope.ReverseColumn(
    width: Dimension = FitContent,
    height: Dimension = FitContent,
    block: UiScope.() -> Unit
) = Box(ReverseColumnLayout, width, height, block)

inline fun UiScope.ReverseRow(
    width: Dimension = FitContent,
    height: Dimension = FitContent,
    block: UiScope.() -> Unit
) = Box(ReverseRowLayout, width, height, block)

inline fun UiScope.Box(
    width: Dimension = FitContent,
    height: Dimension = FitContent,
    block: UiScope.() -> Unit
) = Box(CellLayout, width, height, block)

inline fun UiScope.Box(
    layout: Layout,
    width: Dimension = FitContent,
    height: Dimension = FitContent,
    block: UiScope.() -> Unit
): UiScope {
    val box = uiNode.createChild(BoxNode::class, BoxNode.factory)
    box.modifier
        .width(width)
        .height(height)
        .layout(layout)
    box.block()
    return box
}

open class BoxNode(parent: UiNode?, surface: UiSurface) : UiNode(parent, surface), UiScope {
    override val modifier = UiModifier(surface)

    companion object {
        val factory: (UiNode, UiSurface) -> BoxNode = { parent, surface -> BoxNode(parent, surface) }
    }
}