package io.aitchn.testplugin

import io.aitchn.dcnucleus.api.Plugin

class TestPlugin: Plugin() {

    override fun onEnable() {
        logger.info("Test plugin enabled. $name")
    }
}