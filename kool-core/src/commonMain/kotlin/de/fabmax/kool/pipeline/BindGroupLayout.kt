package de.fabmax.kool.pipeline

import de.fabmax.kool.util.LongHash

class BindGroupLayout(val group: Int, val scope: BindGroupScope, val bindings: List<BindingLayout>) {

    val hash: Long = LongHash().let {
        it += scope
        bindings.forEachIndexed { i, binding ->
            binding.bindingIndex = i
            it += binding.hash
        }
        it.hash
    }

    fun createData(): BindGroupData = BindGroupData(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as BindGroupLayout
        return hash == other.hash
    }

    override fun hashCode(): Int = hash.hashCode()

    class Builder(val group: Int, val scope: BindGroupScope) {
        val ubos = mutableListOf<UniformBufferLayout>()
        val textures = mutableListOf<TextureLayout>()
        val storage = mutableListOf<StorageBufferLayout>()

        fun create(): BindGroupLayout {
            return BindGroupLayout(group, scope, ubos + textures + storage)
        }
    }
}

data class BindGroupLayouts(val viewScope: BindGroupLayout, val pipelineScope: BindGroupLayout, val meshScope: BindGroupLayout) {
    val asList = listOf(viewScope, pipelineScope, meshScope)

    init {
        check(
            viewScope.scope == BindGroupScope.VIEW &&
            pipelineScope.scope == BindGroupScope.PIPELINE &&
            meshScope.scope == BindGroupScope.MESH
        )
    }

    operator fun get(scope: BindGroupScope): BindGroupLayout {
        return when(scope) {
            BindGroupScope.VIEW -> viewScope
            BindGroupScope.PIPELINE -> pipelineScope
            BindGroupScope.MESH -> meshScope
        }
    }
}

enum class BindGroupScope {
    VIEW,
    PIPELINE,
    MESH
}

sealed class BindingLayout(
    val name: String,
    val stages: Set<ShaderStage>,
    val type: BindingType
) {
    abstract val hash: Long

    /**
     * Index of this binding within it's parent [BindGroupLayout]. Only valid after the [BindGroupLayout] is created.
     */
    var bindingIndex: Int = -1
        internal set

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as BindingLayout
        return hash == other.hash
    }

    override fun hashCode(): Int = hash.hashCode()
}

class UniformBufferLayout(
    name: String,
    val uniforms: List<Uniform>,
    stages: Set<ShaderStage>,
) :
    BindingLayout(name, stages, BindingType.UNIFORM_BUFFER)
{
    val layout: Std140BufferLayout = Std140BufferLayout(uniforms)

    override val hash: Long = LongHash().let {
        it += name
        it += type
        stages.forEach { s -> it += s }
        uniforms.forEach { u ->
            it += u.name
            it += u.type
            it += u.arraySize
        }
        it.hash
    }

    fun hasUniform(name: String) = uniforms.any { it.name == name }
}

sealed class TextureLayout(
    name: String,
    stages: Set<ShaderStage>,
    type: BindingType,
    val sampleType: TextureSampleType
) : BindingLayout(name, stages, type) {

    override val hash: Long = LongHash().let {
        it += name
        it += type
        stages.forEach { s -> it += s }
        it.hash
    }
}

class Texture1dLayout(
    name: String,
    stages: Set<ShaderStage>,
    sampleType: TextureSampleType = TextureSampleType.FLOAT,
) : TextureLayout(name, stages, BindingType.TEXTURE_1D, sampleType)

class Texture2dLayout(
    name: String,
    stages: Set<ShaderStage>,
    sampleType: TextureSampleType = TextureSampleType.FLOAT,
) : TextureLayout(name, stages, BindingType.TEXTURE_2D, sampleType)

class Texture3dLayout(
    name: String,
    stages: Set<ShaderStage>,
    sampleType: TextureSampleType = TextureSampleType.FLOAT,
) : TextureLayout(name, stages, BindingType.TEXTURE_3D, sampleType)

class TextureCubeLayout(
    name: String,
    stages: Set<ShaderStage>,
    sampleType: TextureSampleType = TextureSampleType.FLOAT,
) : TextureLayout(name, stages, BindingType.TEXTURE_CUBE, sampleType)

sealed class StorageBufferLayout(
    name: String,
    val format: GpuType,
    val accessType: StorageAccessType,
    stages: Set<ShaderStage>,
    type: BindingType,
) : BindingLayout(name, stages, type) {

    override val hash: Long = LongHash().let {
        it += name
        it += accessType
        it += format
        it += type
        stages.forEach { s -> it += s }
        it.hash
    }
}

class StorageBuffer1dLayout(
    name: String,
    format: GpuType,
    val sizeX: Int?,
    accessType: StorageAccessType,
    stages: Set<ShaderStage>,
) : StorageBufferLayout(name, format, accessType, stages, BindingType.STORAGE_BUFFER_1D)

class StorageBuffer2dLayout(
    name: String,
    format: GpuType,
    val sizeX: Int,
    val sizeY: Int?,
    accessType: StorageAccessType,
    stages: Set<ShaderStage>,
) : StorageBufferLayout(name, format, accessType, stages, BindingType.STORAGE_BUFFER_2D)

class StorageBuffer3dLayout(
    name: String,
    format: GpuType,
    val sizeX: Int,
    val sizeY: Int,
    val sizeZ: Int?,
    accessType: StorageAccessType,
    stages: Set<ShaderStage>,
) : StorageBufferLayout(name, format, accessType, stages, BindingType.STORAGE_BUFFER_3D)

enum class BindingType {
    TEXTURE_1D,
    TEXTURE_2D,
    TEXTURE_3D,
    TEXTURE_CUBE,
    UNIFORM_BUFFER,
    STORAGE_BUFFER_1D,
    STORAGE_BUFFER_2D,
    STORAGE_BUFFER_3D
}

enum class StorageAccessType {
    READ_ONLY,
    WRITE_ONLY,
    READ_WRITE
}

enum class TextureSampleType {
    FLOAT,
    UNFILTERABLE_FLOAT,
    DEPTH
}
