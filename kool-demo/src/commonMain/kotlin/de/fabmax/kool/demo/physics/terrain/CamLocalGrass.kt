package de.fabmax.kool.demo.physics.terrain

import de.fabmax.kool.math.Mat4f
import de.fabmax.kool.math.Random
import de.fabmax.kool.math.Vec3f
import de.fabmax.kool.math.clamp
import de.fabmax.kool.math.spatial.*
import de.fabmax.kool.pipeline.Attribute
import de.fabmax.kool.pipeline.Texture2d
import de.fabmax.kool.pipeline.ibl.EnvironmentMaps
import de.fabmax.kool.scene.Camera
import de.fabmax.kool.scene.Mesh
import de.fabmax.kool.scene.MeshInstanceList
import de.fabmax.kool.scene.geometry.IndexedVertexList
import de.fabmax.kool.util.PerfTimer
import de.fabmax.kool.util.ShadowMap
import de.fabmax.kool.util.logD
import kotlin.math.sqrt

class CamLocalGrass(val terrain: Terrain, val trees: Trees) {

    private val grassInstances = MeshInstanceList(listOf(Attribute.INSTANCE_MODEL_MAT, GrassShader.DISTANCE_SCALE))
    private val grassPositions = mutableListOf<GrassInstance>()
    private val instanceTree: KdTree<GrassInstance>
    private val instanceTrav = GrassTraverser()

    val grassQuads: Mesh

    init {
        grassQuads = Mesh(IndexedVertexList(Attribute.POSITIONS, Attribute.NORMALS, Attribute.TEXTURE_COORDS, TreeShader.WIND_SENSITIVITY)).apply {
            generate {
                with(Grass) {
                    val pos = Vec3f(-0.5f, 0f, 0f)
                    val step = Vec3f(0.333f, 0f, 0f)
                    val midOffset = Vec3f(0f, 0f, 0.333f)
                    val topOffset = Vec3f(0.17f, 1f, -0.13f)
                    grassSprite(pos, step, topOffset, midOffset)
                }
                geometry.generateNormals()
            }
            isFrustumChecked = false
            isCastingShadow = false
            instances = grassInstances
        }

        val pt = PerfTimer()
        val rand = Random(1337)
        for (i in 0..750_000) {
            val x = rand.randomF(-250f, 250f)
            val z = rand.randomF(-250f, 250f)

            val weights = terrain.getSplatWeightsAt(x, z)
            if (weights.y > 0.5f && rand.randomF() < weights.y) {
                val y = terrain.getTerrainHeightAt(x, z)
                val p = rand.randomF(0.25f, 1f)
                val s = rand.randomF(0.75f, 1.25f) * weights.y
                grassPositions += GrassInstance(Vec3f(x, y, z), p, s).apply {
                    transform.rotate(rand.randomF(0f, 360f), Vec3f.Y_AXIS).scale(s)
                }
            }
        }

        instanceTree = KdTree(grassPositions, Vec3fAdapter)
        logD { "Generated ${grassPositions.size} grass patches in ${pt.takeMs()} ms" }
    }

    fun setupShader(grassColor: Texture2d, ibl: EnvironmentMaps, shadowMap: ShadowMap) {
        val grassShader = GrassShader(grassColor, ibl, shadowMap, trees.windDensity, true)
        grassQuads.shader = grassShader

        grassQuads.onUpdate += { ev ->
            if (grassQuads.isVisible) {
                grassShader.windOffset = trees.windOffset
                grassShader.windStrength = trees.windStrength
                grassShader.windScale = 1f / trees.windScale

                val cam = ev.camera
                val radius = 50f
                instanceTrav.setup(cam.globalPos, radius, cam).traverse(instanceTree)

                grassInstances.clear()
                grassInstances.addInstances(instanceTrav.result.size) { buf ->
                    for (grass in instanceTrav.result) {
                        val distScale = ((grass.distance(cam.globalPos) / radius - 0.1f).clamp(0f, 1f) / (grass.p - 0.1f)).clamp(0f, 1f)
                        buf.put(grass.transform.matrix)
                        buf.put(distScale * grass.s)
                    }
                }
            }
        }
    }

    class GrassInstance(pos: Vec3f, val p: Float, val s: Float) : Vec3f(pos) {
        val transform = Mat4f().translate(pos)
    }

    class GrassTraverser : InRadiusTraverser<GrassInstance>() {
        lateinit var camera: Camera

        init {
            pointDistance = object : PointDistance<GrassInstance> {
                override fun nodeSqrDistanceToPoint(node: SpatialTree<GrassInstance>.Node, point: Vec3f): Float {
                    val dSqr = super.nodeSqrDistanceToPoint(node, point)
                    if (dSqr < radiusSqr) {
                        val radius = node.bounds.size.length() * 0.5f
                        if (!camera.isInFrustum(node.bounds.center, radius)) {
                            return Float.MAX_VALUE
                        }
                    }
                    return dSqr
                }

                override fun itemSqrDistanceToPoint(tree: SpatialTree<GrassInstance>, item: GrassInstance, point: Vec3f): Float {
                    val dSqr = super.itemSqrDistanceToPoint(tree, item, point)
                    if (dSqr < radiusSqr && !camera.isInFrustum(item, 1.5f)) {
                        return Float.MAX_VALUE
                    }
                    val relDist = sqrt(dSqr) / radius
                    if (item.p < relDist) {
                        return Float.MAX_VALUE
                    }
                    return dSqr
                }
            }
        }

        fun setup(center: Vec3f, radius: Float, camera: Camera): InRadiusTraverser<GrassInstance> {
            this.camera = camera
            return super.setup(center, radius)
        }
    }
}