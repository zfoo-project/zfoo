using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Text;
using UnityEngine;
using XLua;
using zfoocs;

public class Main : MonoBehaviour
{
    
    public static readonly string TEST_PATH = "Assets/";
    
    private void Start()
    {
        var luaEnv = new LuaEnv();
        luaEnv.DoString("CS.UnityEngine.Debug.Log('hello world')");
        
        var luaDebugBuilder = new StringBuilder();
        // Rider的断点调试
        // luaDebugBuilder.Append("package.cpath = package.cpath .. ';C:/Users/Administrator/AppData/Roaming/JetBrains/Rider2022.3/plugins/EmmyLua/debugger/emmy/windows/x64/?.dll'").Append(System.Environment.NewLine);
        // luaDebugBuilder.Append("local dbg = require('emmy_core')").Append(System.Environment.NewLine);
        // luaDebugBuilder.Append("dbg.tcpConnect('localhost', 9966)").Append(System.Environment.NewLine);

        luaEnv.DoString(luaDebugBuilder.ToString());

        luaEnv.AddLoader(CustomLoader);

        var luaProtocolTestStr = File.ReadAllText( "Assets/main.lua");
        luaEnv.DoString(luaProtocolTestStr, "main");

        LuaFunction byteBufferTestFunction = luaEnv.Global.Get<LuaFunction>("byteBufferTest");
        byteBufferTestFunction.Call();

        
        // 获取复杂对象的字节流
        var complexObjectBytes = File.ReadAllBytes("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\complexObject.bytes");
        LuaFunction complexObjectTestFuction = luaEnv.Global.Get<LuaFunction>("complexObjectTest");
        complexObjectTestFuction.Call(complexObjectBytes);
        
        // 获取普通对象的字节流
        // var normalObjectBytes = File.ReadAllBytes("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-no-compatible.bytes");
        // var normalObjectBytes = File.ReadAllBytes("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-compatible.bytes");
        // var normalObjectBytes = File.ReadAllBytes("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-inner-compatible.bytes");
        // var normalObjectBytes = File.ReadAllBytes("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-inner-compatible.bytes");
        var normalObjectBytes = File.ReadAllBytes("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-inner-inner-compatible.bytes");
        LuaFunction normalObjectTestFuction = luaEnv.Global.Get<LuaFunction>("normalObjectTest");
        normalObjectTestFuction.Call(normalObjectBytes);
    }


    public static byte[] CustomLoader(ref string filepath)
    {
        filepath = filepath.Replace(".", "/") + ".lua";

        return File.ReadAllBytes(TEST_PATH + filepath);
    }
}