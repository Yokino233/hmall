const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 3000;
const ROOT = path.resolve(__dirname, '..', 'hmall-frontend');
const GATEWAY = { host: 'localhost', port: 8080 };

const MIME = {
  '.html': 'text/html;charset=utf-8',
  '.css': 'text/css;charset=utf-8',
  '.js': 'application/javascript;charset=utf-8',
  '.json': 'application/json',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.gif': 'image/gif',
  '.ico': 'image/x-icon',
  '.svg': 'image/svg+xml',
  '.ttf': 'font/ttf',
  '.woff': 'font/woff',
  '.woff2': 'font/woff2',
};

http.createServer((req, res) => {
  // API 请求 → 代理到 Gateway
  if (req.url.startsWith('/api')) {
    const apiPath = req.url.replace('/api', '');
    const options = {
      hostname: GATEWAY.host,
      port: GATEWAY.port,
      path: apiPath,
      method: req.method,
      headers: { ...req.headers, host: GATEWAY.host },
    };
    const proxyReq = http.request(options, (proxyRes) => {
      // CORS 头
      res.setHeader('Access-Control-Allow-Origin', '*');
      res.writeHead(proxyRes.statusCode, proxyRes.headers);
      proxyRes.pipe(res);
    });
    proxyReq.on('error', () => {
      res.writeHead(502);
      res.end('Gateway unavailable');
    });
    req.pipe(proxyReq);
    return;
  }

  // 静态文件
  let filePath = path.join(ROOT, req.url === '/' ? 'index.html' : req.url);
  const ext = path.extname(filePath).toLowerCase();

  fs.readFile(filePath, (err, data) => {
    if (err) {
      filePath = path.join(ROOT, 'hmall-portal', req.url === '/' ? 'index.html' : req.url);
      fs.readFile(filePath, (err2, data2) => {
        if (err2) {
          res.writeHead(404, { 'Content-Type': 'text/plain' });
          res.end('Not found');
          return;
        }
        res.writeHead(200, { 'Content-Type': MIME[ext] || 'application/octet-stream' });
        res.end(data2);
      });
      return;
    }
    res.writeHead(200, { 'Content-Type': MIME[ext] || 'application/octet-stream' });
    res.end(data);
  });
}).listen(PORT, () => {
  console.log(`✅ 前端服务已启动: http://localhost:${PORT}`);
  console.log(`   API 代理 → http://localhost:${GATEWAY.port}`);
});
