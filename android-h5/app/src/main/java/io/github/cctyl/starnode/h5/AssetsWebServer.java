package io.github.cctyl.starnode.h5;

// AssetsWebServer.java
import android.content.Context;
import fi.iki.elonen.NanoHTTPD;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class AssetsWebServer extends NanoHTTPD {
    private Context context;

    // 构造函数，传入上下文和端口
    public AssetsWebServer(Context context, int port) {
        super(port);
        this.context = context.getApplicationContext();
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Map<String, String> headers = session.getHeaders();

        // 1. 默认请求映射到 index.html
        if ("/".equals(uri)) {
            uri = "/index.html";
        }

        // 2. 构建 assets 中的文件路径
        // 假设你把 Vue 打包的整个 dist 文件夹放入了 `assets/www/` 下
        String assetPath = "www" + uri; // 例如：请求 `/img/logo.png` -> `assets/www/img/logo.png`

        try {
            // 3. 打开 assets 中的文件流
            InputStream inputStream = context.getAssets().open(assetPath);
            // 4. 根据文件后缀确定 MIME 类型
            String mimeType = getMimeType(uri);

            // 5. 创建并返回响应
            // 使用 `newFixedLengthResponse` 并传入流和长度
            Response response = newFixedLengthResponse(Response.Status.OK, mimeType, inputStream, inputStream.available());
            // 解决可能的 CORS 问题（如果 Vue 代码需要）
            response.addHeader("Access-Control-Allow-Origin", "*");
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            // 文件未找到，返回 404
            if (uri.endsWith(".html") || uri.endsWith("/")) {
                // 对于单页应用 (Vue Router的history模式)，所有未知路径都应返回 index.html
                try {
                    InputStream fallbackStream = context.getAssets().open("www/index.html");
                    return newFixedLengthResponse(Response.Status.OK, "text/html", fallbackStream, fallbackStream.available());
                } catch (IOException ex) {
                    return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "404 Not Found - " + uri);
                }
            }
            return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "404 Not Found - " + uri);
        }
    }

    // 简单的 MIME 类型映射
    private String getMimeType(String uri) {
        if (uri.endsWith(".html")) return "text/html";
        if (uri.endsWith(".js")) return "application/javascript";
        if (uri.endsWith(".css")) return "text/css";
        if (uri.endsWith(".png")) return "image/png";
        if (uri.endsWith(".jpg") || uri.endsWith(".jpeg")) return "image/jpeg";
        if (uri.endsWith(".gif")) return "image/gif";
        if (uri.endsWith(".svg")) return "image/svg+xml";
        if (uri.endsWith(".json")) return "application/json";
        if (uri.endsWith(".ico")) return "image/x-icon";
        // 默认类型
        return "application/octet-stream";
    }
}