package org.sandboxpowered.fabric.loading

import org.graalvm.polyglot.Source
import org.sandboxpowered.fabric.addon.AddonScanner
import org.sandboxpowered.fabric.scripting.JSScriptLoader
import org.sandboxpowered.fabric.util.RegexUtil
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.readText

class SandboxLoader {

    fun load() {
        val jsLoader = JSScriptLoader()
        val addons = AddonScanner.scanDirectory(Path.of("resources"))
        addons.forEach {
            val scripts = arrayListOf<String>()

            if(it.config.contains("scripts")) scripts.addAll(it.config.get<ArrayList<String>>("scripts"))
            if(it.config.contains("server.scripts")) scripts.addAll(it.config.get<ArrayList<String>>("server.scripts"))
            if(it.config.contains("client.scripts")) scripts.addAll(it.config.get<ArrayList<String>>("client.scripts"))

            val basePath = it.path
            scripts.forEach { script ->
                val regex = RegexUtil.convertGlobToRegex(script)
                val scriptPath = basePath.resolve(script)
                if(scriptPath.name.endsWith(".js")) {
                    val source = Source.newBuilder(
                        "js",
                        scriptPath.readText(StandardCharsets.UTF_8),
                        scriptPath.name
                    ).build()

                    jsLoader.loadScriptContext(it.path.name, source)
                }
            }
        }

        jsLoader.sbxJS.emit("onLoad")
    }
}

fun <E> ArrayList<E>.addAll(vararg elements: E) {
    elements.forEach(this::add)
}
