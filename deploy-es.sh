#!/bin/bash
# ES 7.17.10 + ik 分词器部署脚本
# 用法: 传到 VM 上执行: bash deploy-es.sh

set -e

ES_VERSION="7.17.10"
ES_CONTAINER="elasticsearch"
ES_PORT=9200

echo "===== 部署 Elasticsearch ${ES_VERSION} ====="

# 1. 拉取镜像
docker pull docker.m.daocloud.io/elasticsearch:${ES_VERSION}

# 2. 停止并删除旧容器
docker rm -f ${ES_CONTAINER} 2>/dev/null || true

# 3. 启动 ES（单节点，关闭安全认证，分配 512m 内存）
docker run -d \
  --name ${ES_CONTAINER} \
  --restart unless-stopped \
  -e "discovery.type=single-node" \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  -e "xpack.security.enabled=false" \
  -p ${ES_PORT}:9200 \
  -p 9300:9300 \
  docker.m.daocloud.io/elasticsearch:${ES_VERSION}

echo "等待 ES 启动..."

# 4. 等待 ES 就绪
for i in $(seq 1 30); do
  if curl -s "http://localhost:${ES_PORT}" > /dev/null 2>&1; then
    echo "ES 已就绪！"
    break
  fi
  echo "等待中... ${i}/30"
  sleep 2
done

# 5. 安装 ik 分词器
echo "安装 ik 分词器..."
docker exec ${ES_CONTAINER} bin/elasticsearch-plugin install -b https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v${ES_VERSION}/elasticsearch-analysis-ik-${ES_VERSION}.zip

# 6. 重启 ES
echo "重启 ES 加载 ik 分词器..."
docker restart ${ES_CONTAINER}

echo "等待 ES 重启..."
sleep 10

# 7. 验证
echo "===== 验证 ====="
curl -s "http://localhost:${ES_PORT}" | python3 -m json.tool 2>/dev/null || curl -s "http://localhost:${ES_PORT}"

echo ""
echo "===== 验证 ik 分词器 ====="
curl -s -H "Content-Type: application/json" -X POST "http://localhost:${ES_PORT}/_analyze" -d '{
  "analyzer": "ik_smart",
  "text": "你好世界"
}' | python3 -m json.tool 2>/dev/null || true

echo ""
echo "===== 部署完成 ====="
echo "ES 地址: http://192.168.137.128:${ES_PORT}"
