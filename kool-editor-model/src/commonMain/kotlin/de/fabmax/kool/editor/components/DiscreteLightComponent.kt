package de.fabmax.kool.editor.components

import de.fabmax.kool.editor.api.GameEntity
import de.fabmax.kool.editor.data.ColorData
import de.fabmax.kool.editor.data.ComponentInfo
import de.fabmax.kool.editor.data.DiscreteLightComponentData
import de.fabmax.kool.editor.data.LightTypeData
import de.fabmax.kool.scene.Light
import de.fabmax.kool.scene.Node
import de.fabmax.kool.scene.Scene
import de.fabmax.kool.util.Color

class DiscreteLightComponent(
    gameEntity: GameEntity,
    componentInfo: ComponentInfo<DiscreteLightComponentData> = ComponentInfo(
        DiscreteLightComponentData(LightTypeData.Directional(ColorData(Color.WHITE), 3f))
    )
) :
    GameEntityDataComponent<DiscreteLightComponentData>(gameEntity, componentInfo),
    DrawNodeComponent
{
    override var drawNode: Light = data.light.createLight()
        private set

    override suspend fun applyComponent() {
        super.applyComponent()
        updateLight(data.light, true)
    }

    override fun destroyComponent() {
        gameEntity.replaceDrawNode(Node(gameEntity.name))
        val scene = sceneEntity.drawNode as Scene
        scene.lighting.removeLight(drawNode)
        super.destroyComponent()
    }

    override fun onDataChanged(oldData: DiscreteLightComponentData, newData: DiscreteLightComponentData) {
        updateLight(newData.light, forceReplaceNode = false)
    }

    private fun updateLight(lightData: LightTypeData, forceReplaceNode: Boolean) {
        val updateLight = lightData.updateOrCreateLight(drawNode)

        if (forceReplaceNode || updateLight != drawNode) {
            val scene = sceneEntity.drawNode as Scene
            val lighting = scene.lighting
            lighting.removeLight(drawNode)

            drawNode = updateLight
            gameEntity.replaceDrawNode(drawNode)
            lighting.addLight(drawNode)
        }

        gameEntity.getComponent<ShadowMapComponent>()?.updateLight(drawNode)
    }
}