package com.atpexgo.tiebasign.util;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;

public class Test {

    public static void main(String[] args) throws FileNotFoundException, ScriptException, NoSuchMethodException {
        ClassLoader classLoader = Test.class.getClassLoader();
        URL resource = classLoader.getResource("static/baidupassEncrypt.js");
        String path = resource.getPath();
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName( "js" );
        FileReader reader = new FileReader(path);
        engine.eval(reader);

        Invocable invoke = (Invocable)engine;

        String xxx = (String) invoke.invokeFunction("test","Atpex24901124","b91248c16a");
//        String xxx = (String) invoke.invokeFunction("test1");
        System.out.println(xxx);
    }
}
