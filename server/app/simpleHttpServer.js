const http = require('http');
const url = require('url');


const HTTP_PORT = 8080;
const HTTP_TOKEN = 'abcdef';
const HTTP_PATH = `/api/data`;

function simpleHttpServer( handler) {
   let httpServer = http.createServer((req, res) => {
        // 只处理 POST 请求
        if (req.method === 'POST') {
            // 解析 URL 和查询参数
            const parsedUrl = url.parse(req.url, true);
            const pathname = parsedUrl.pathname;
            const query = parsedUrl.query;
            const endpointName = query.endpointName; // 从查询参数中获取 endpointName

            console.log(`HTTP:${endpointName}`)
            // 添加路径判断，例如只处理 /api/data 路径
            if (pathname === HTTP_PATH) {
                // 验证请求头 token
                const authHeader = req.headers['token'];
                if (authHeader === HTTP_TOKEN) {
                    // 检查是否提供了 endpointName 参数
                    if (!endpointName) {
                        res.writeHead(200);
                        res.end('success');
                        return;
                    }

                    let body = '';

                    // 收集请求体数据
                    req.on('data', chunk => {
                        body += chunk.toString();
                    });

                    // 数据接收完成后处理
                    req.on('end', () => {
                        try {

                            handler(endpointName, body);

                            // 发送成功响应（可选）
                            res.writeHead(200, {'Content-Type': 'application/json'});
                            res.end(JSON.stringify({status: 'success'}));
                        } catch (error) {
                            console.error('JSON parsing error:', error);
                            // 不返回任何内容
                            res.writeHead(200);
                            res.end();
                        }
                    });
                } else {
                    // 不满足 token 要求，不返回任何内容
                    res.writeHead(200); // No Content
                    res.end();
                }
            } else {
                // 路径不匹配，返回 404
                res.writeHead(200);
                res.end();
            }
        } else {
            // 非 POST 请求，返回 405 Method Not Allowed
            res.writeHead(200);
            res.end();
        }
    });

    // 启动 HTTP 服务器监听 8080 端口
    httpServer.listen(HTTP_PORT, () => {
        console.log('HTTP server listening on port 8080');
    });

}


function start(){

}
module.exports = {
    simpleHttpServer
}