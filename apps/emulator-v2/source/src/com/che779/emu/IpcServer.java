package com.che779.emu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

/** Socket server localhost: moi dong la 1 lenh JSON don gian, tra ve 1 dong JSON. Chay nen, khong block UI. */
final class IpcServer {
    private static final int MAX_CLIENTS = 16;
    private static final int MAX_LINE_CHARS = 65536;
    private static final int CLIENT_TIMEOUT_MS = 10000;
    private final Semaphore clients = new Semaphore(MAX_CLIENTS);
    private final int basePort;
    private ServerSocket server;

    IpcServer(int basePort) { this.basePort = basePort; }

    /** Bind 127.0.0.1; neu cong ban thi thu cong ke tiep (toi da 200 cong). @return cong that su. */
    int start() throws IOException {
        IOException last = null;
        for (int p = basePort; p < basePort + 200; p++) {
            try {
                server = new ServerSocket(p, 16, InetAddress.getByName("127.0.0.1"));
                break;
            } catch (IOException e) { last = e; }
        }
        if (server == null) throw last != null ? last : new IOException("no port");
        Thread t = new Thread(new Runnable() { public void run() { acceptLoop(); } }, "che779-ipc");
        t.setDaemon(true);
        t.start();
        return server.getLocalPort();
    }

    private void acceptLoop() {
        while (!server.isClosed()) {
            try {
                final Socket s = server.accept();
                if (!clients.tryAcquire()) { try { s.close(); } catch (IOException ignore) {} continue; }
                Thread c = new Thread(new Runnable() { public void run() {
                    try { serve(s); } finally { clients.release(); }
                } }, "che779-ipc-client");
                c.setDaemon(true);
                c.start();
            } catch (IOException e) { if (server.isClosed()) return; }
        }
    }

    private void serve(Socket s) {
        try {
            s.setSoTimeout(CLIENT_TIMEOUT_MS);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
            PrintWriter out = new PrintWriter(new java.io.OutputStreamWriter(s.getOutputStream(), "UTF-8"), true);
            String line;
            while ((line = readBoundedLine(in)) != null) {
                line = line.trim();
                if (line.length() == 0) continue;
                out.println(handle(line));
                if (out.checkError()) break;
            }
        } catch (IOException ignore) {
        } finally {
            try { s.close(); } catch (IOException ignore) {}
        }
    }

    private static String readBoundedLine(BufferedReader in) throws IOException {
        StringBuilder b = new StringBuilder(); int ch;
        while ((ch = in.read()) != -1) {
            if (ch == '\n') return b.toString();
            if (ch != '\r') { if (b.length() >= MAX_LINE_CHARS) throw new IOException("IPC line too long"); b.append((char) ch); }
        }
        return b.length() == 0 ? null : b.toString();
    }

    // Bộ phân tích JSON phẳng nghiêm ngặt: nhận chuỗi, số, boolean và null.
    static Map<String, String> parse(String json) {
        if (json == null) throw new IllegalArgumentException("null JSON");
        int[] p = {0}; Map<String, String> m = new HashMap<String, String>(); skipWs(json,p); expect(json,p,'{'); skipWs(json,p);
        if (peek(json,p)=='}') { p[0]++; skipWs(json,p); if(p[0]!=json.length()) throw new IllegalArgumentException("trailing data"); return m; }
        while (true) {
            skipWs(json,p); String key=readString(json,p); skipWs(json,p); expect(json,p,':'); skipWs(json,p);
            String value;
            if (peek(json,p)=='"') value=readString(json,p);
            else { int b=p[0]; while(p[0]<json.length() && ",}".indexOf(json.charAt(p[0]))<0 && !Character.isWhitespace(json.charAt(p[0]))) p[0]++;
                if(b==p[0]) throw new IllegalArgumentException("missing value"); value=json.substring(b,p[0]);
                if (!("true".equals(value)||"false".equals(value)||"null".equals(value)||value.matches("-?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?"))) throw new IllegalArgumentException("invalid value");
                if("null".equals(value)) value=null;
            }
            if(m.containsKey(key)) throw new IllegalArgumentException("duplicate key"); m.put(key,value); skipWs(json,p);
            char c=peek(json,p); if(c==','){p[0]++;continue;} if(c=='}'){p[0]++;break;} throw new IllegalArgumentException("expected delimiter");
        }
        skipWs(json,p); if(p[0]!=json.length()) throw new IllegalArgumentException("trailing data"); return m;
    }
    private static void skipWs(String s,int[] p){while(p[0]<s.length()&&Character.isWhitespace(s.charAt(p[0])))p[0]++;}
    private static char peek(String s,int[] p){return p[0]<s.length()?s.charAt(p[0]):'\0';}
    private static void expect(String s,int[] p,char c){if(peek(s,p)!=c)throw new IllegalArgumentException("expected "+c);p[0]++;}
    private static String readString(String s,int[] p){
        expect(s,p,'"'); StringBuilder b=new StringBuilder();
        while(p[0]<s.length()){char c=s.charAt(p[0]++); if(c=='"')return b.toString(); if(c<0x20)throw new IllegalArgumentException("control char");
            if(c!='\\'){b.append(c);continue;} if(p[0]>=s.length())throw new IllegalArgumentException("unfinished escape"); char e=s.charAt(p[0]++);
            if(e=='"'||e=='\\'||e=='/')b.append(e); else if(e=='b')b.append('\b'); else if(e=='f')b.append('\f'); else if(e=='n')b.append('\n'); else if(e=='r')b.append('\r'); else if(e=='t')b.append('\t');
            else if(e=='u'){if(p[0]+4>s.length())throw new IllegalArgumentException("short unicode"); try{b.append((char)Integer.parseInt(s.substring(p[0],p[0]+4),16));}catch(NumberFormatException ex){throw new IllegalArgumentException("bad unicode");}p[0]+=4;} else throw new IllegalArgumentException("bad escape");
        } throw new IllegalArgumentException("unterminated string");
    }

    static String unescape(String s) {
        if (s == null) return null;
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char n = s.charAt(++i);
                if (n == 'n') b.append('\n');
                else if (n == 't') b.append('\t');
                else if (n == 'u' && i + 4 < s.length()) {
                    try { b.append((char) Integer.parseInt(s.substring(i + 1, i + 5), 16)); i += 4; }
                    catch (NumberFormatException e) { b.append('u'); }
                } else b.append(n);
            } else b.append(c);
        }
        return b.toString();
    }

    private static String jsonEscape(String v) { StringBuilder b=new StringBuilder(); for(int i=0;i<v.length();i++){char c=v.charAt(i); if(c=='"'||c=='\\')b.append('\\').append(c); else if(c=='\n')b.append("\\n"); else if(c=='\r')b.append("\\r"); else if(c=='\t')b.append("\\t"); else if(c<0x20)b.append('?'); else b.append(c);} return b.toString(); }

    static String handle(String json) {
        Map<String, String> m;
        try { m = parse(json); } catch (IllegalArgumentException ex) { return "{\"ok\":false,\"error\":\"invalid json\"}"; }
        String a = m.get("action");
        if (a == null) return "{\"ok\":false,\"error\":\"missing action\"}";
        a = a.toLowerCase();
        boolean ok;
        if (a.equals("keypress")) {
            int hold = 60;
            try { if (m.get("hold") != null) hold = Integer.parseInt(m.get("hold")); } catch (NumberFormatException ignore) {}
            ok = Che779Main.sendKey(m.get("key"), true, true, hold);
        } else if (a.equals("keydown")) {
            ok = Che779Main.sendKey(m.get("key"), true, false, 0);
        } else if (a.equals("keyup")) {
            ok = Che779Main.sendKey(m.get("key"), false, true, 0);
        } else if (a.equals("text")) {
            String v = m.get("text") != null ? m.get("text") : m.get("value");
            ok = Che779Main.typeText(v);
        } else if (a.equals("clean_ram")) {
            Che779Main.runCleanAsync(); ok = true;
        } else if (a.equals("sleep")) {
            ok = Che779Main.setSleep(m.get("state"));
        } else if (a.equals("hide")) {
            ok = Che779Main.setVisible(false);
        } else if (a.equals("show")) {
            ok = Che779Main.setVisible(true);
        } else if (a.equals("mouse")) {
            double fx = 0, fy = 0;
            try { fx = Double.parseDouble(m.get("fx")); fy = Double.parseDouble(m.get("fy")); } catch (Exception e) { return "{\"ok\":false,\"error\":\"bad coords\"}"; }
            ok = Che779Main.mouse(m.get("type"), fx, fy);
        } else if (a.equals("proxy")) {
            String mode = m.get("mode");
            if ("none".equals(mode)) {
                org.microemu.app.ProxyManager.setOverride(null);
            } else {
                org.microemu.app.ProxyManager.Proxy px = org.microemu.app.ProxyManager.parseSpec(
                        m.get("host") + "|" + m.get("port") + "|" + (m.get("user") == null ? "" : m.get("user"))
                        + "|" + (m.get("pass") == null ? "" : m.get("pass")) + "|" + (m.get("type") == null ? "AUTO" : m.get("type")));
                if (px == null) return "{\"ok\":false,\"error\":\"bad proxy\"}";
                org.microemu.app.ProxyManager.setOverride(px);
            }
            org.microemu.app.Main.statusMessage("Proxy: " + org.microemu.app.ProxyManager.describeActive());
            ok = true;
        } else if (a.equals("proxy_get")) {
            return "{\"ok\":true,\"proxy\":\"" + jsonEscape(org.microemu.app.ProxyManager.describeActive()) + "\"}";
        } else if (a.equals("add_tab")) {
            ok = org.microemu.app.V2Main.ipcAddTab(m.get("path"));
        } else if (a.equals("open")) {
            int tab = 1;
            try { tab = Integer.parseInt(m.get("tab")); } catch (Exception ignore) {}
            ok = org.microemu.app.V2Main.ipcOpen(tab, m.get("path"));
        } else if (a.equals("slots")) {
            return "{\"ok\":true,\"slots\":" + org.microemu.app.V2Main.ipcSlots() + "}";
        } else if (a.equals("ping")) {
            ok = true;
        } else if (a.equals("status")) {
            return "{\"ok\":true,\"pid\":" + Che779Main.pid() + ",\"port\":" + Che779Main.port
                    + ",\"sleeping\":" + Che779Main.isSleeping()
                    + ",\"version\":\"" + org.microemu.app.Che779.VERSION + "\"}";
        } else if (a.equals("screenshot")) {
            ok = Che779Main.screenshot(m.get("path"));
        } else if (a.equals("version")) {
            return "{\"ok\":true,\"version\":\"" + org.microemu.app.Che779.VERSION + "\"}";
        } else if (a.equals("quit") || a.equals("close")) {
            new Thread(new Runnable() { public void run() {
                try { Thread.sleep(150); } catch (InterruptedException ignore) {}
                System.exit(0);
            }}).start();
            ok = true;
        } else {
            return "{\"ok\":false,\"error\":\"unknown action\"}";
        }
        return ok ? "{\"ok\":true}" : "{\"ok\":false,\"error\":\"failed\"}";
    }
}
