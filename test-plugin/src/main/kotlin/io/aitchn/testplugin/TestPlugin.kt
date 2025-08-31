package io.aitchn.testplugin

import io.aitchn.dcnucleus.api.DCNucleusPlugin

class TestPlugin: DCNucleusPlugin() {

    override fun onEnable() {
        logger.info("Test plugin enabled. $dataFolder")
    }
}