package net.stoppedwumml.modpacks.scripting;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.HashMap;
import java.util.Map;

public class ScriptManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ScriptEngineManager manager = new ScriptEngineManager();
    private static final Map<String, Invocable> LOCAL_SCRIPTS = new HashMap<>();
    private static Invocable globalScript = null;

    public static void loadScript(String id, String content) {
        Invocable inv = compile(content);
        if (inv != null) LOCAL_SCRIPTS.put(id, inv);
    }

    public static void loadGlobalScript(String content) {
        globalScript = compile(content);
        LOGGER.info("Global script loaded.");
    }

    private static Invocable compile(String content) {
        try {
            ScriptEngine engine = manager.getEngineByName("nashorn");
            engine.eval(content);
            return (Invocable) engine;
        } catch (Exception e) {
            LOGGER.error("Script Error: {}", e.getMessage());
            return null;
        }
    }

    public static void runFunction(String id, String func, ScriptHelper ctx) {
        execute(LOCAL_SCRIPTS.get(id), func, ctx);
    }

    public static void runGlobalFunction(String func, ScriptHelper ctx) {
        execute(globalScript, func, ctx);
    }

    private static void execute(Invocable inv, String func, ScriptHelper ctx) {
        if (inv != null) {
            try { inv.invokeFunction(func, ctx); }
            catch (NoSuchMethodException ignored) {}
            catch (Exception e) { LOGGER.error("Execution error in {}: {}", func, e.getMessage()); }
        }
    }
}