package io.aitchn.dcnucleus.server

import kotlinx.serialization.Serializable

@Serializable
data class PluginDescriptor(
    val name: String,
    val version: String = "1.0.0",
    val entrypoint: String
)
