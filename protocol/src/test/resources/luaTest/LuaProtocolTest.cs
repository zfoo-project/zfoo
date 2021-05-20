using System.IO;
using System.Text;
using NUnit.Framework;
using XLua;

namespace Test.Editor.LuaTest
{
    public class LuaProtocolTest
    {
        public static readonly string TEST_PATH = "Assets/Test/Editor/LuaTest/";

        [Test]
        public void ComplexObjectTest()
        {
            // 获取复杂对象的字节流
            var complexObjectBytes = File.ReadAllBytes("D:\\zfoo\\protocol\\src\\test\\resources\\ComplexObject.bytes");

            var luaEnv = new LuaEnv();
            var luaDebugBuilder = new StringBuilder();
            // Rider的断点调试
            // luaDebugBuilder.Append("package.cpath = package.cpath .. ';C:/Users/jm/AppData/Roaming/JetBrains/Rider2020.1/plugins/intellij-emmylua/classes/debugger/emmy/windows/x64/?.dll'").Append(FileUtils.LS);
            // luaDebugBuilder.Append("local dbg = require('emmy_core')").Append(FileUtils.LS);
            // luaDebugBuilder.Append("dbg.tcpListen('localhost', 9966)").Append(FileUtils.LS);
            // luaDebugBuilder.Append("dbg.waitIDE()").Append(FileUtils.LS);

            luaEnv.DoString(luaDebugBuilder.ToString());

            luaEnv.AddLoader(CustomLoader);

            var luaProtocolTestStr = File.ReadAllText(TEST_PATH + "LuaProtocolTest.lua");
            luaEnv.DoString(luaProtocolTestStr, "LuaProtocolTest");

            LuaFunction byteBufferTestFunction = luaEnv.Global.Get<LuaFunction>("byteBufferTest");
            byteBufferTestFunction.Call();

            LuaFunction complexObjectTestFuction = luaEnv.Global.Get<LuaFunction>("complexObjectTest");
            complexObjectTestFuction.Call(complexObjectBytes);
        }

        public static byte[] CustomLoader(ref string filepath)
        {
            filepath = filepath.Replace(".", "/") + ".lua";

            return File.ReadAllBytes(TEST_PATH + filepath);
        }
    }
}