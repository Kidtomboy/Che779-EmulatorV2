package com.che779.emu;

import java.util.Map;

/** Dependency-free regression tests for the IPC JSON parser. */
public final class IpcServerParserTest {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private static void rejects(String json) {
        try { IpcServer.parse(json); throw new AssertionError("Expected rejection: " + json); }
        catch (IllegalArgumentException expected) { }
    }
    public static void main(String[] args) {
        Map<String,String> m = IpcServer.parse("{\"action\":\"open\",\"path\":\"C:\\\\Games\\\\a\\\"b.jar\",\"tab\":2}");
        check("open".equals(m.get("action")), "action parsed");
        check("C:\\Games\\a\"b.jar".equals(m.get("path")), "escaped path parsed");
        check("2".equals(m.get("tab")), "number parsed as string");
        check(IpcServer.parse("{\"x\":null}").containsKey("x"), "null accepted");
        rejects("{\"action\":\"ping\"} trailing");
        rejects("{\"action\":\"ping\",\"action\":\"quit\"}");
        rejects("{\"action\":oops}");
        rejects("{\"action\":\"bad\\q\"}");
        rejects("[]");
        System.out.println("IPC parser tests: PASS");
    }
}
