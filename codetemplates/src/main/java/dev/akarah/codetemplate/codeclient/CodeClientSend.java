package dev.akarah.codetemplate.codeclient;

import dev.akarah.codetemplate.template.GzippedCodeTemplateData;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class CodeClientSend {
    List<GzippedCodeTemplateData> codeTemplateDatas = new ArrayList<>();

    public static CodeClientSend of() {
        return new CodeClientSend();
    }

    public CodeClientSend push(GzippedCodeTemplateData data) {
        this.codeTemplateDatas.add(data);
        return this;
    }

    public void finish() {
        Thread.ofPlatform().start(() -> {
            var wh = new WebsocketHandler(URI.create("ws://localhost:31375"), this);
            try {
                var r = wh.connectBlocking();
                if(!r) {
                    throw new RuntimeException("nope, cc api did NOT connect");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        try {
            Thread.sleep(10000000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static class WebsocketHandler extends WebSocketClient {
        CodeClientSend sender;

        public WebsocketHandler(URI serverUri) {
            super(serverUri);
        }

        public WebsocketHandler(URI serverUri, CodeClientSend sender) {
            super(serverUri);
            this.sender = sender;
        }

        @Override
        public void onOpen(ServerHandshake serverHandshake) {
            System.out.println("-> OPEN");
            this.send("scopes default inventory movement read_plot write_code clear_plot");
            System.out.println("Go in-game and do /auth!");
        }

        @Override
        public void send(String text) {
            super.send(text);
            System.out.println("<- " + text);
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onMessage(String s) {
            System.out.println("-> " + s);
            if(s.equals("auth")) {
                this.send("mode code");
                this.send("clear");

                this.send("place");
                for(var entry : this.sender.codeTemplateDatas) {
                    this.send("place " + entry.code());
                }
                this.send("place go");
            }
            if(s.equals("place done")) {
                System.exit(0);
            }
        }

        @Override
        public void onClose(int i, String s, boolean b) {
            System.out.println("-> close (" + i + " | '" + s + "' | " + b + ")");
        }

        @Override
        public void onError(Exception e) {
            System.out.println(e.toString());
        }
    }



}
