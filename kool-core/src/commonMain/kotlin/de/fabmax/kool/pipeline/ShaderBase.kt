package de.fabmax.kool.pipeline

import de.fabmax.kool.math.*
import de.fabmax.kool.modules.ksl.blocks.ColorBlockConfig
import de.fabmax.kool.modules.ksl.blocks.PropertyBlockConfig
import de.fabmax.kool.util.Color
import de.fabmax.kool.util.MutableColor
import de.fabmax.kool.util.UniqueId
import kotlin.reflect.KProperty

/**
 * Base class for all regular and compute shaders. Provides methods to easily connect to shader uniforms.
 */
abstract class ShaderBase(val name: String) {

    val uniforms = mutableMapOf<String, Uniform<*>>()

    val texSamplers1d = mutableMapOf<String, TextureSampler1d>()
    val texSamplers2d = mutableMapOf<String, TextureSampler2d>()
    val texSamplers3d = mutableMapOf<String, TextureSampler3d>()
    val texSamplersCube = mutableMapOf<String, TextureSamplerCube>()

    val storage1d = mutableMapOf<String, Storage1d>()
    val storage2d = mutableMapOf<String, Storage2d>()
    val storage3d = mutableMapOf<String, Storage3d>()

    private val connectUniformListeners = mutableMapOf<String, ConnectUniformListener>()

    protected fun pipelineCreated(pipeline: PipelineBase) {
        pipeline.bindGroupLayouts.forEach { group ->
            group.items.forEach { binding ->
                when (binding) {
                    is UniformBuffer -> binding.uniforms.forEach { uniforms[it.name] = it }
                    is TextureSampler1d -> texSamplers1d[binding.name] = binding
                    is TextureSampler2d -> texSamplers2d[binding.name] = binding
                    is TextureSampler3d -> texSamplers3d[binding.name] = binding
                    is TextureSamplerCube -> texSamplersCube[binding.name] = binding
                    is Storage1d -> storage1d[binding.name] = binding
                    is Storage2d -> storage2d[binding.name] = binding
                    is Storage3d -> storage3d[binding.name] = binding
                }
            }
        }
        connectUniformListeners.values.forEach { it.connect() }
    }

    protected interface ConnectUniformListener {
        val isConnected: Boolean?
        fun connect()
    }

    fun uniform1f(uniformName: String, defaultVal: Float? = null): UniformInput1f =
        connectUniformListeners.getOrPut(uniformName) { UniformInput1f(uniformName, defaultVal ?: 0f) } as UniformInput1f
    fun uniform2f(uniformName: String, defaultVal: Vec2f? = null): UniformInput2f =
        connectUniformListeners.getOrPut(uniformName) { UniformInput2f(uniformName, defaultVal ?: Vec2f.ZERO) } as UniformInput2f
    fun uniform3f(uniformName: String, defaultVal: Vec3f? = null): UniformInput3f =
        connectUniformListeners.getOrPut(uniformName) { UniformInput3f(uniformName, defaultVal ?: Vec3f.ZERO) } as UniformInput3f
    fun uniform4f(uniformName: String, defaultVal: Vec4f? = null): UniformInput4f =
        connectUniformListeners.getOrPut(uniformName) { UniformInput4f(uniformName, defaultVal ?: Vec4f.ZERO) } as UniformInput4f
    fun uniformColor(uniformName: String, defaultVal: Color? = null): UniformInputColor =
        connectUniformListeners.getOrPut(uniformName) { UniformInputColor(uniformName, defaultVal ?: Color.BLACK) } as UniformInputColor
    fun uniformQuat(uniformName: String, defaultVal: QuatF? = null): UniformInputQuat =
        connectUniformListeners.getOrPut(uniformName) { UniformInputQuat(uniformName, defaultVal ?: QuatF.IDENTITY) } as UniformInputQuat

    fun uniform1i(uniformName: String, defaultVal: Int? = null): UniformInput1i =
        connectUniformListeners.getOrPut(uniformName) { UniformInput1i(uniformName, defaultVal ?: 0) } as UniformInput1i
    fun uniform2i(uniformName: String, defaultVal: Vec2i? = null): UniformInput2i =
        connectUniformListeners.getOrPut(uniformName) { UniformInput2i(uniformName, defaultVal ?: Vec2i.ZERO) } as UniformInput2i
    fun uniform3i(uniformName: String, defaultVal: Vec3i? = null): UniformInput3i =
        connectUniformListeners.getOrPut(uniformName) { UniformInput3i(uniformName, defaultVal ?: Vec3i.ZERO) } as UniformInput3i
    fun uniform4i(uniformName: String, defaultVal: Vec4i? = null): UniformInput4i =
        connectUniformListeners.getOrPut(uniformName) { UniformInput4i(uniformName, defaultVal ?: Vec4i.ZERO) } as UniformInput4i

    fun uniformMat3f(uniformName: String, defaultVal: Mat3f? = null): UniformInputMat3f =
        connectUniformListeners.getOrPut(uniformName) { UniformInputMat3f(uniformName, defaultVal) } as UniformInputMat3f
    fun uniformMat4f(uniformName: String, defaultVal: Mat4f? = null): UniformInputMat4f =
        connectUniformListeners.getOrPut(uniformName) { UniformInputMat4f(uniformName, defaultVal) } as UniformInputMat4f

    fun uniform1fv(uniformName: String, arraySize: Int): UniformInput1fv =
        connectUniformListeners.getOrPut(uniformName) { UniformInput1fv(uniformName, arraySize) } as UniformInput1fv
    fun uniform2fv(uniformName: String, arraySize: Int): UniformInput2fv =
        connectUniformListeners.getOrPut(uniformName) { UniformInput2fv(uniformName, arraySize) } as UniformInput2fv
    fun uniform3fv(uniformName: String, arraySize: Int): UniformInput3fv =
        connectUniformListeners.getOrPut(uniformName) { UniformInput3fv(uniformName, arraySize) } as UniformInput3fv
    fun uniform4fv(uniformName: String, arraySize: Int): UniformInput4fv =
        connectUniformListeners.getOrPut(uniformName) { UniformInput4fv(uniformName, arraySize) } as UniformInput4fv

    fun uniform1iv(uniformName: String, arraySize: Int): UniformInput1iv =
        connectUniformListeners.getOrPut(uniformName) { UniformInput1iv(uniformName, arraySize) } as UniformInput1iv
    fun uniform2iv(uniformName: String, arraySize: Int): UniformInput2iv =
        connectUniformListeners.getOrPut(uniformName) { UniformInput2iv(uniformName, arraySize) } as UniformInput2iv
    fun uniform3iv(uniformName: String, arraySize: Int): UniformInput3iv =
        connectUniformListeners.getOrPut(uniformName) { UniformInput3iv(uniformName, arraySize) } as UniformInput3iv
    fun uniform4iv(uniformName: String, arraySize: Int): UniformInput4iv =
        connectUniformListeners.getOrPut(uniformName) { UniformInput4iv(uniformName, arraySize) } as UniformInput4iv

    fun uniformMat3fv(uniformName: String, arraySize: Int): UniformInputMat3fv =
        connectUniformListeners.getOrPut(uniformName) { UniformInputMat3fv(uniformName, arraySize) } as UniformInputMat3fv
    fun uniformMat4fv(uniformName: String, arraySize: Int): UniformInputMat4fv =
        connectUniformListeners.getOrPut(uniformName) { UniformInputMat4fv(uniformName, arraySize) } as UniformInputMat4fv

    fun texture1d(uniformName: String, defaultVal: Texture1d? = null): UniformInputTexture1d =
        connectUniformListeners.getOrPut(uniformName) { UniformInputTexture1d(uniformName, defaultVal) } as UniformInputTexture1d
    fun texture2d(uniformName: String, defaultVal: Texture2d? = null): UniformInputTexture2d =
        connectUniformListeners.getOrPut(uniformName) { UniformInputTexture2d(uniformName, defaultVal) } as UniformInputTexture2d
    fun texture3d(uniformName: String, defaultVal: Texture3d? = null): UniformInputTexture3d =
        connectUniformListeners.getOrPut(uniformName) { UniformInputTexture3d(uniformName, defaultVal) } as UniformInputTexture3d
    fun textureCube(uniformName: String, defaultVal: TextureCube? = null): UniformInputTextureCube =
        connectUniformListeners.getOrPut(uniformName) { UniformInputTextureCube(uniformName, defaultVal) } as UniformInputTextureCube

    fun texture1dArray(uniformName: String, arraySize: Int): UniformInputTextureArray1d =
        connectUniformListeners.getOrPut(uniformName) { UniformInputTextureArray1d(uniformName, arraySize) } as UniformInputTextureArray1d
    fun texture2dArray(uniformName: String, arraySize: Int): UniformInputTextureArray2d =
        connectUniformListeners.getOrPut(uniformName) { UniformInputTextureArray2d(uniformName, arraySize) } as UniformInputTextureArray2d
    fun texture3dArray(uniformName: String, arraySize: Int): UniformInputTextureArray3d =
        connectUniformListeners.getOrPut(uniformName) { UniformInputTextureArray3d(uniformName, arraySize) } as UniformInputTextureArray3d
    fun textureCubeArray(uniformName: String, arraySize: Int): UniformInputTextureArrayCube =
        connectUniformListeners.getOrPut(uniformName) { UniformInputTextureArrayCube(uniformName, arraySize) } as UniformInputTextureArrayCube

    fun storage1d(uniformName: String, defaultVal: StorageTexture1d? = null): UniformInputStorage1d =
        connectUniformListeners.getOrPut(uniformName) { UniformInputStorage1d(uniformName, defaultVal) } as UniformInputStorage1d
    fun storage2d(uniformName: String, defaultVal: StorageTexture2d? = null): UniformInputStorage2d =
        connectUniformListeners.getOrPut(uniformName) { UniformInputStorage2d(uniformName, defaultVal) } as UniformInputStorage2d
    fun storage3d(uniformName: String, defaultVal: StorageTexture3d? = null): UniformInputStorage3d =
        connectUniformListeners.getOrPut(uniformName) { UniformInputStorage3d(uniformName, defaultVal) } as UniformInputStorage3d

    fun colorUniform(cfg: ColorBlockConfig): UniformInputColor =
        uniformColor(cfg.primaryUniform?.uniformName ?: UniqueId.nextId("_"), cfg.primaryUniform?.defaultColor)
    fun colorTexture(cfg: ColorBlockConfig): UniformInputTexture2d =
        texture2d(cfg.primaryTexture?.textureName ?: UniqueId.nextId("_"), cfg.primaryTexture?.defaultTexture)

    fun propertyUniform(cfg: PropertyBlockConfig): UniformInput1f =
        uniform1f(cfg.primaryUniform?.uniformName ?: UniqueId.nextId("_"), cfg.primaryUniform?.defaultValue)
    fun propertyTexture(cfg: PropertyBlockConfig): UniformInputTexture2d =
        texture2d(cfg.primaryTexture?.textureName ?: UniqueId.nextId("_"), cfg.primaryTexture?.defaultTexture)

    inner class UniformInput1f(val uniformName: String, defaultVal: Float) : ConnectUniformListener {
        private var uniform: Uniform1f? = null
        private var buffer = defaultVal
        override val isConnected: Boolean = uniform != null
        override fun connect() { uniform = (uniforms[uniformName] as? Uniform1f)?.apply { value = buffer } }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Float = uniform?.value ?: buffer
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
            uniform?.let { it.value = value } ?: run { buffer = value }
        }
    }

    inner class UniformInput2f(val uniformName: String, defaultVal: Vec2f) : ConnectUniformListener {
        private var uniform: Uniform2f? = null
        private val buffer = MutableVec2f(defaultVal)
        override val isConnected: Boolean = uniform != null
        override fun connect() { uniform = (uniforms[uniformName] as? Uniform2f)?.apply { value.set(buffer) } }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Vec2f = uniform?.value ?: buffer
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Vec2f) = (uniform?.value ?: buffer).set(value)
    }

    inner class UniformInput3f(val uniformName: String, defaultVal: Vec3f) : ConnectUniformListener {
        private var uniform: Uniform3f? = null
        private val buffer = MutableVec3f(defaultVal)
        override val isConnected: Boolean = uniform != null
        override fun connect() { uniform = (uniforms[uniformName] as? Uniform3f)?.apply { value.set(buffer) } }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Vec3f = uniform?.value ?: buffer
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Vec3f) = (uniform?.value ?: buffer).set(value)
    }

    inner class UniformInput4f(val uniformName: String, defaultVal: Vec4f) : ConnectUniformListener {
        var uniform: Uniform4f? = null
        val buffer = MutableVec4f(defaultVal)
        override val isConnected: Boolean = uniform != null
        override fun connect() { uniform = (uniforms[uniformName] as? Uniform4f)?.apply { value.set(buffer) } }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Vec4f = uniform?.value ?: buffer
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Vec4f) = (uniform?.value ?: buffer).set(value)
    }

    inner class UniformInputColor(val uniformName: String, defaultVal: Color) : ConnectUniformListener {
        var uniform: Uniform4f? = null
        val buffer = MutableColor(defaultVal)
        override val isConnected: Boolean = uniform != null
        override fun connect() { uniform = (uniforms[uniformName] as? Uniform4f)?.apply { value.set(buffer.r, buffer.g, buffer.b, buffer.a) } }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Color {
            uniform?.value?.let { buffer.set(it.x, it.y, it.z, it.w) }
            return buffer
        }
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Color) {
            val u = uniform?.value
            if (u != null) {
                u.set(value.r, value.g, value.b, value.a)
            } else {
                buffer.set(value)
            }
        }
    }

    inner class UniformInputQuat(val uniformName: String, defaultVal: QuatF) : ConnectUniformListener {
        var uniform: Uniform4f? = null
        val buffer = MutableQuatF(defaultVal)
        override val isConnected: Boolean = uniform != null
        override fun connect() { uniform = (uniforms[uniformName] as? Uniform4f)?.apply { value.set(buffer.x, buffer.y, buffer.z, buffer.w) } }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): QuatF {
            uniform?.value?.let { buffer.set(it.x, it.y, it.z, it.w) }
            return buffer
        }
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: QuatF) {
            val u = uniform?.value
            if (u != null) {
                u.set(value.x, value.y, value.z, value.w)
            } else {
                buffer.set(value)
            }
        }
    }

    inner class UniformInput1i(val uniformName: String, defaultVal: Int) : ConnectUniformListener {
        private var uniform: Uniform1i? = null
        private var buffer = defaultVal
        override val isConnected: Boolean = uniform != null
        override fun connect() { uniform = (uniforms[uniformName] as? Uniform1i)?.apply { value = buffer } }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Int = uniform?.value ?: buffer
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
            uniform?.let { it.value = value } ?: run { buffer = value }
        }
    }

    inner class UniformInput2i(val uniformName: String, defaultVal: Vec2i) : ConnectUniformListener {
        private var uniform: Uniform2i? = null
        private val buffer = MutableVec2i(defaultVal)
        override val isConnected: Boolean = uniform != null
        override fun connect() { uniform = (uniforms[uniformName] as? Uniform2i)?.apply { value.set(buffer) } }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Vec2i = uniform?.value ?: buffer
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Vec2i) = (uniform?.value ?: buffer).set(value)
    }

    inner class UniformInput3i(val uniformName: String, defaultVal: Vec3i) : ConnectUniformListener {
        private var uniform: Uniform3i? = null
        private val buffer = MutableVec3i(defaultVal)
        override val isConnected: Boolean = uniform != null
        override fun connect() { uniform = (uniforms[uniformName] as? Uniform3i)?.apply { value.set(buffer) } }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Vec3i = uniform?.value ?: buffer
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Vec3i) = (uniform?.value ?: buffer).set(value)
    }

    inner class UniformInput4i(val uniformName: String, defaultVal: Vec4i) : ConnectUniformListener {
        private var uniform: Uniform4i? = null
        private val buffer = MutableVec4i(defaultVal)
        override val isConnected: Boolean = uniform != null
        override fun connect() { uniform = (uniforms[uniformName] as? Uniform4i)?.apply { value.set(buffer) } }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Vec4i = uniform?.value ?: buffer
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Vec4i) = (uniform?.value ?: buffer).set(value)
    }

    inner class UniformInputMat3f(val uniformName: String, defaultVal: Mat3f?) : ConnectUniformListener {
        var uniform: UniformMat3f? = null
        private val buffer = MutableMat3f().apply { defaultVal?.let { set(it) } }
        override val isConnected: Boolean = uniform != null
        override fun connect() { uniform = (uniforms[uniformName] as? UniformMat3f)?.apply { value.set(buffer) } }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): MutableMat3f = uniform?.value ?: buffer
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: MutableMat3f) = (uniform?.value ?: buffer).set(value)
    }

    inner class UniformInputMat4f(val uniformName: String, defaultVal: Mat4f?) : ConnectUniformListener {
        var uniform: UniformMat4f? = null
        private val buffer = MutableMat4f().apply { defaultVal?.let { set(it) } }
        override val isConnected: Boolean = uniform != null
        override fun connect() { uniform = (uniforms[uniformName] as? UniformMat4f)?.apply { value.set(buffer) } }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): MutableMat4f = uniform?.value ?: buffer
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: MutableMat4f) = (uniform?.value ?: buffer).set(value)
    }

    inner class UniformInput1fv(val uniformName: String, val arraySize: Int) : ConnectUniformListener {
        private var uniform: Uniform1fv? = null
        private val buffer = FloatArray(arraySize)
        override val isConnected: Boolean = uniform != null
        override fun connect() {
            uniform = (uniforms[uniformName] as? Uniform1fv)?.apply {
                check(size == arraySize) { "Mismatching uniform array size: $size != $arraySize" }
                value = buffer
            }
        }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): FloatArray = uniform?.value ?: buffer
    }

    inner class UniformInput2fv(val uniformName: String, val arraySize: Int) : ConnectUniformListener {
        private var uniform: Uniform2fv? = null
        private val buffer = Array(arraySize) { MutableVec2f(Vec2f.ZERO) }
        override val isConnected: Boolean = uniform != null
        override fun connect() {
            uniform = (uniforms[uniformName] as? Uniform2fv)?.apply {
                check(size == arraySize) { "Mismatching uniform array size: $size != $arraySize" }
                value = buffer
            }
        }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Array<MutableVec2f> = uniform?.value ?: buffer
    }

    inner class UniformInput3fv(val uniformName: String, val arraySize: Int) : ConnectUniformListener {
        private var uniform: Uniform3fv? = null
        private val buffer = Array(arraySize) { MutableVec3f(Vec3f.ZERO) }
        override val isConnected: Boolean = uniform != null
        override fun connect() {
            uniform = (uniforms[uniformName] as? Uniform3fv)?.apply {
                check(size == arraySize) { "Mismatching uniform array size: $size != $arraySize" }
                value = buffer
            }
        }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Array<MutableVec3f> = uniform?.value ?: buffer
    }

    inner class UniformInput4fv(val uniformName: String, val arraySize: Int) : ConnectUniformListener {
        private var uniform: Uniform4fv? = null
        private val buffer = Array(arraySize) { MutableVec4f(Vec4f.ZERO) }
        override val isConnected: Boolean = uniform != null
        override fun connect() {
            uniform = (uniforms[uniformName] as? Uniform4fv)?.apply {
                check(size == arraySize) { "Mismatching uniform array size: $size != $arraySize" }
                value = buffer
            }
        }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Array<MutableVec4f> = uniform?.value ?: buffer
    }

    inner class UniformInput1iv(val uniformName: String, val arraySize: Int) : ConnectUniformListener {
        private var uniform: Uniform1iv? = null
        private val buffer = IntArray(arraySize)
        override val isConnected: Boolean = uniform != null
        override fun connect() {
            uniform = (uniforms[uniformName] as? Uniform1iv)?.apply {
                check(size == arraySize) { "Mismatching uniform array size: $size != $arraySize" }
                value = buffer
            }
        }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): IntArray = uniform?.value ?: buffer
    }

    inner class UniformInput2iv(val uniformName: String, val arraySize: Int) : ConnectUniformListener {
        private var uniform: Uniform2iv? = null
        private val buffer = Array(arraySize) { MutableVec2i(Vec2i.ZERO) }
        override val isConnected: Boolean = uniform != null
        override fun connect() {
            uniform = (uniforms[uniformName] as? Uniform2iv)?.apply {
                check(size == arraySize) { "Mismatching uniform array size: $size != $arraySize" }
                value = buffer
            }
        }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Array<MutableVec2i> = uniform?.value ?: buffer
    }

    inner class UniformInput3iv(val uniformName: String, val arraySize: Int) : ConnectUniformListener {
        private var uniform: Uniform3iv? = null
        private val buffer = Array(arraySize) { MutableVec3i(Vec3i.ZERO) }
        override val isConnected: Boolean = uniform != null
        override fun connect() {
            uniform = (uniforms[uniformName] as? Uniform3iv)?.apply {
                check(size == arraySize) { "Mismatching uniform array size: $size != $arraySize" }
                value = buffer
            }
        }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Array<MutableVec3i> = uniform?.value ?: buffer
    }

    inner class UniformInput4iv(val uniformName: String, val arraySize: Int) : ConnectUniformListener {
        private var uniform: Uniform4iv? = null
        private val buffer = Array(arraySize) { MutableVec4i(Vec4i.ZERO) }
        override val isConnected: Boolean = uniform != null
        override fun connect() {
            uniform = (uniforms[uniformName] as? Uniform4iv)?.apply {
                check(size == arraySize) { "Mismatching uniform array size: $size != $arraySize" }
                value = buffer
            }
        }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Array<MutableVec4i> = uniform?.value ?: buffer
    }

    inner class UniformInputMat3fv(val uniformName: String, val arraySize: Int) : ConnectUniformListener {
        var uniform: UniformMat3fv? = null
        private val buffer = Array(arraySize) { MutableMat3f() }
        override val isConnected: Boolean = uniform != null
        override fun connect() {
            uniform = (uniforms[uniformName] as? UniformMat3fv)?.apply {
                check(size == arraySize) { "Mismatching uniform array size: $size != $arraySize" }
                buffer.forEachIndexed { i, m -> value[i] = m }
            }
        }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Array<MutableMat3f> = uniform?.value ?: buffer
    }

    inner class UniformInputMat4fv(val uniformName: String, val arraySize: Int) : ConnectUniformListener {
        var uniform: UniformMat4fv? = null
        private val buffer = Array(arraySize) { MutableMat4f() }
        override val isConnected: Boolean = uniform != null
        override fun connect() {
            uniform = (uniforms[uniformName] as? UniformMat4fv)?.apply {
                check(size == arraySize) { "Mismatching uniform array size: $size != $arraySize" }
                buffer.forEachIndexed { i, m -> value[i] = m }
            }
        }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Array<MutableMat4f> = uniform?.value ?: buffer
    }

    inner class UniformInputTexture1d(val uniformName: String, defaultVal: Texture1d?) : ConnectUniformListener {
        var uniform: TextureSampler1d? = null
        private var buffer: Texture1d? = defaultVal
        override val isConnected: Boolean = uniform != null
        override fun connect() { uniform = texSamplers1d[uniformName]?.apply { texture = buffer } }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Texture1d? = uniform?.texture ?: buffer
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Texture1d?) {
            uniform?.let { it.texture = value } ?: run { buffer = value }
        }
    }

    inner class UniformInputTexture2d(val uniformName: String, defaultVal: Texture2d?) : ConnectUniformListener {
        var uniform: TextureSampler2d? = null
        private var buffer: Texture2d? = defaultVal
        override val isConnected: Boolean = uniform != null
        override fun connect() { uniform = texSamplers2d[uniformName]?.apply { texture = buffer } }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Texture2d? = uniform?.texture ?: buffer
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Texture2d?) {
            uniform?.let { it.texture = value } ?: run { buffer = value }
        }
    }

    inner class UniformInputTexture3d(val uniformName: String, defaultVal: Texture3d?) : ConnectUniformListener {
        var uniform: TextureSampler3d? = null
        private var buffer: Texture3d? = defaultVal
        override val isConnected: Boolean = uniform != null
        override fun connect() { uniform = texSamplers3d[uniformName]?.apply { texture = buffer } }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Texture3d? = uniform?.texture ?: buffer
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Texture3d?) {
            uniform?.let { it.texture = value } ?: run { buffer = value }
        }
    }

    inner class UniformInputTextureCube(val uniformName: String, defaultVal: TextureCube?) : ConnectUniformListener {
        var uniform: TextureSamplerCube? = null
        private var buffer: TextureCube? = defaultVal
        override val isConnected: Boolean = uniform != null
        override fun connect() { uniform = texSamplersCube[uniformName]?.apply { texture = buffer } }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): TextureCube? = uniform?.texture ?: buffer
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: TextureCube?) {
            uniform?.let { it.texture = value } ?: run { buffer = value }
        }
    }

    inner class UniformInputTextureArray1d(val uniformName: String, val arrSize: Int) : ConnectUniformListener {
        var uniform: TextureSampler1d? = null
        private val buffer = Array<Texture1d?>(arrSize) { null }
        override val isConnected: Boolean = uniform != null
        override fun connect() {
            uniform = texSamplers1d[uniformName]?.apply {
                check(arraySize == arrSize) { "Mismatching texture array size: $arraySize != $arrSize" }
                for (i in textures.indices) { textures[i] = buffer[i] }
            }
        }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Array<Texture1d?> = uniform?.textures ?: buffer
    }

    inner class UniformInputTextureArray2d(val uniformName: String, val arrSize: Int) : ConnectUniformListener {
        var uniform: TextureSampler2d? = null
        private val buffer = Array<Texture2d?>(arrSize) { null }
        override val isConnected: Boolean = uniform != null
        override fun connect() {
            uniform = texSamplers2d[uniformName]?.apply {
                check(arraySize == arrSize) { "Mismatching texture array size: $arraySize != $arrSize" }
                for (i in textures.indices) { textures[i] = buffer[i] }
            }
        }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Array<Texture2d?> = uniform?.textures ?: buffer
    }

    inner class UniformInputTextureArray3d(val uniformName: String, val arrSize: Int) : ConnectUniformListener {
        var uniform: TextureSampler3d? = null
        private val buffer = Array<Texture3d?>(arrSize) { null }
        override val isConnected: Boolean = uniform != null
        override fun connect() {
            uniform = texSamplers3d[uniformName]?.apply {
                check(arraySize == arrSize) { "Mismatching texture array size: $arraySize != $arrSize" }
                for (i in textures.indices) { textures[i] = buffer[i] }
            }
        }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Array<Texture3d?> = uniform?.textures ?: buffer
    }

    inner class UniformInputTextureArrayCube(val uniformName: String, val arrSize: Int) : ConnectUniformListener {
        var uniform: TextureSamplerCube? = null
        private val buffer = Array<TextureCube?>(arrSize) { null }
        override val isConnected: Boolean = uniform != null
        override fun connect() {
            uniform = texSamplersCube[uniformName]?.apply {
                check(arraySize == arrSize) { "Mismatching texture array size: $arraySize != $arrSize" }
                for (i in textures.indices) { textures[i] = buffer[i] }
            }
        }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Array<TextureCube?> = uniform?.textures ?: buffer
    }

    inner class UniformInputStorage1d(val uniformName: String, defaultVal: StorageTexture1d?) : ConnectUniformListener {
        var uniform: Storage1d? = null
        private var buffer: StorageTexture1d? = defaultVal
        override val isConnected: Boolean = uniform != null
        override fun connect() { uniform = storage1d[uniformName]?.apply { storageTex = buffer } }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): StorageTexture1d? = uniform?.storageTex ?: buffer
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: StorageTexture1d?) {
            uniform?.let { it.storageTex = value } ?: run { buffer = value }
        }
    }

    inner class UniformInputStorage2d(val uniformName: String, defaultVal: StorageTexture2d?) : ConnectUniformListener {
        var uniform: Storage2d? = null
        private var buffer: StorageTexture2d? = defaultVal
        override val isConnected: Boolean = uniform != null
        override fun connect() { uniform = storage2d[uniformName]?.apply { storageTex = buffer } }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): StorageTexture2d? = uniform?.storageTex ?: buffer
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: StorageTexture2d?) {
            uniform?.let { it.storageTex = value } ?: run { buffer = value }
        }
    }

    inner class UniformInputStorage3d(val uniformName: String, defaultVal: StorageTexture3d?) : ConnectUniformListener {
        var uniform: Storage3d? = null
        private var buffer: StorageTexture3d? = defaultVal
        override val isConnected: Boolean = uniform != null
        override fun connect() { uniform = storage3d[uniformName]?.apply { storageTex = buffer } }
        operator fun getValue(thisRef: Any?, property: KProperty<*>): StorageTexture3d? = uniform?.storageTex ?: buffer
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: StorageTexture3d?) {
            uniform?.let { it.storageTex = value } ?: run { buffer = value }
        }
    }
}