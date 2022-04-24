package de.fabmax.kool.demo.physics.terrain

import de.fabmax.kool.KoolContext
import de.fabmax.kool.math.Mat3f
import de.fabmax.kool.math.Mat4f
import de.fabmax.kool.math.Vec3d
import de.fabmax.kool.math.Vec3f
import de.fabmax.kool.math.spatial.BoundingBox
import de.fabmax.kool.physics.*
import de.fabmax.kool.physics.geometry.BoxGeometry
import de.fabmax.kool.physics.geometry.HeightField
import de.fabmax.kool.physics.geometry.HeightFieldGeometry
import de.fabmax.kool.physics.geometry.PlaneGeometry
import de.fabmax.kool.scene.Scene
import de.fabmax.kool.util.HeightMap

class PhysicsObjects(mainScene: Scene, heightMap: HeightMap, ctx: KoolContext) {

    val world: PhysicsWorld

    val terrain: RigidStatic
    val ground: RigidStatic
    val playerController: PlayerController
    val chainBridge: ChainBridge
    val boxes = mutableListOf<RigidDynamic>()
    private val boxInitPoses = mutableListOf<Mat4f>()

    init {
        // create a new physics world
        // CCD is recommended when using height fields, to avoid objects tunneling through the ground
        world = PhysicsWorld(mainScene, isContinuousCollisionDetection = true)

        // use constant time step for more stable bridge behavior
        world.simStepper = ConstantPhysicsStepper()

        // create a static actor from provided height field, which will serve as terrain / ground
        val heightField = HeightField(heightMap, 1f, 1f)
        val hfGeom = HeightFieldGeometry(heightField)
        val hfBounds = hfGeom.getBounds(BoundingBox())
        terrain = RigidStatic()
        terrain.attachShape(Shape(hfGeom, Physics.defaultMaterial))
        terrain.position = Vec3f(hfBounds.size.x * -0.5f, 0f, hfBounds.size.z * -0.5f)
        world.addActor(terrain)

        // put another infinitely large ground plane below terrain to catch stuff which falls of the edge of the world
        ground = RigidStatic()
        ground.attachShape(Shape(PlaneGeometry(), Physics.defaultMaterial))
        ground.position = Vec3f(0f, -5f, 0f)
        ground.setRotation(Mat3f().rotate(90f, Vec3f.Z_AXIS))
        world.addActor(ground)

        // create a chain bridge, the player can walk across
        chainBridge = ChainBridge(world)

        // spawn a few dynamic boxes, the player can interact with
        spawnBoxes()

        // spawn player
        playerController = PlayerController(this, ctx).apply {
            // set spawn position
            controller.position = Vec3d(-146.5, 47.8, -89.0)
        }

        world.onPhysicsUpdate += { timeStep ->
            playerController.onPhysicsUpdate(timeStep)
        }
    }

    private fun spawnBoxes() {
        val n = 15
        val boxSize = 2f
        for (x in -n..n) {
            for (z in -n..n) {
                val shape = BoxGeometry(Vec3f(boxSize))
                val body = RigidDynamic(100f)
                body.attachShape(Shape(shape, Physics.defaultMaterial))
                body.position = Vec3f(x * 5.5f, 100f, z * 5.5f)
                world.addActor(body)
                boxes += body

                boxInitPoses += Mat4f().set(body.transform)
            }
        }
    }

    fun respawnBoxes() {
        boxes.forEachIndexed { i, body ->
            body.setTransform(boxInitPoses[i])
            body.linearVelocity = Vec3f.ZERO
            body.angularVelocity = Vec3f.ZERO
        }
    }

    fun release(ctx: KoolContext) {
        playerController.release(ctx)
        world.release()
    }
}