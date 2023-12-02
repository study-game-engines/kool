package de.fabmax.kool.modules.ksl

import de.fabmax.kool.KoolSystem
import de.fabmax.kool.modules.ksl.lang.KslProgram
import de.fabmax.kool.pipeline.ComputePipeline
import de.fabmax.kool.pipeline.ComputeRenderPass
import de.fabmax.kool.pipeline.ComputeShader
import de.fabmax.kool.util.logW

fun KslComputeShader(
    name: String,
    block: KslProgram.() -> Unit
): KslComputeShader {
    val shader = KslComputeShader(name)
    shader.program.apply {
        block()
    }
    return shader
}

class KslComputeShader(name: String) : ComputeShader(name) {

    val program = KslProgram(name)

    override fun onPipelineSetup(builder: ComputePipeline.Builder, computePass: ComputeRenderPass) {
        val computeStage = program.computeStage
        checkNotNull(computeStage) {
            "KslProgram computeStage is missing (a valid KslComputeShader needs a computeStage)"
        }
        if (program.vertexStage != null || program.fragmentStage != null) {
            logW { "KslProgram has a vertex or fragment stage defined, although it is used as a compute shader. Vertex and fragment stages are ignored." }
        }

        // prepare shader model for generating source code, also updates program dependencies (e.g. which
        // uniform is used by which shader stage)
        program.prepareGenerate()

        builder.name = program.name
        builder.workGroupSize.set(computeStage.workGroupSize)
        builder.bindGroupLayouts += program.setupBindGroupLayout(this)
        builder.shaderCodeGenerator = { KoolSystem.requireContext().backend.generateKslComputeShader(this, it) }
    }

    override fun onComputePipelineCreated(pipeline: ComputePipeline, computePass: ComputeRenderPass) {
        super.onComputePipelineCreated(pipeline, computePass)

        pipeline.onUpdate += {
            for (i in program.shaderListeners.indices) {
                program.shaderListeners[i].onComputeUpdate(it)
            }
        }
        program.shaderListeners.forEach { it.onShaderCreated(this, pipeline) }
    }

}