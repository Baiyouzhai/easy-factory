package com.byz.factory.core;

import javax.script.Compilable;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScriptExecutor {

    public static final ScriptExecutor Instance = new ScriptExecutor();

    public static Object Declare(String script) {
        return Instance.declare(script);
    }

    public static Object Execute(String script, Object... args) {
        return Instance.execute(script, args);
    }

//    protected ScheduledExecutorService executor;
    protected ScriptEngine engine;

    public ScriptExecutor() {
//        executor = Executors.newScheduledThreadPool(10);
        engine = new ScriptEngineManager().getEngineByName("nashorn");
    }

    public ScriptEngine getEngine() {
//        return new ScriptEngineManager().getEngineByName("nashorn");
        return engine;
    }

    public Object declare(String script) {
        try {
            return ((Compilable) getEngine()).compile(script).eval();
//            return executor.schedule(() -> ((Compilable) getEngine()).compile(script).eval(), 0, TimeUnit.SECONDS).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object execute(String name, Object... args) {
        try {
            return ((Invocable) getEngine()).invokeFunction(name, args);
//            return executor.schedule(() -> ((Invocable) getEngine()).invokeFunction(name, args), 0, TimeUnit.SECONDS).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
