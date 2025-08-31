package io.aitchn.dcnucleus.api.system

interface SystemCommand {
    val name: String
    val description: String
    val usage: String
    val aliases: List<String> get() = emptyList()

    fun execute(args: Array<String>): Boolean
    fun tabComplete(args: Array<String>): List<String> = emptyList()
}