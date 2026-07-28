# 黑马商城 — SpringCloud 微服务全栈项目

## 项目结构

```
hmall/
├── hm-gateway/       网关 (8080)   — JWT认证 + 限流 + traceId
├── item-service/     商品 (8081)   — ES搜索 + 库存扣减
├── cart-service/     购物车 (8082)  — Feign + MQ清空
├── user-service/     用户 (8083)   — BCrypt + JWT签发
├── trade-service/    订单 (8084)   — Seata分布式事务 + 支付超时
├── pay-service/      支付 (8085)   — 余额支付
├── hm-common/        公共模块      — JWT工具 / 异常处理 / 自动配置
├── hm-api/           Feign接口     — 服务间调用定义
├── docker-compose.yml               — 基础设施一键启动
└── pom.xml                          — 父工程
```

## 快速启动

### 1. VM 启动基础设施

```bash
# 登录 VM
ssh hd@192.168.137.128
cd ~

# 一键启动所有中间件（6个服务）
docker compose up -d

# 启动 Seata TC（如未在 docker-compose 中启动）
bash ~/seata-server/seata/bin/seata-server.sh -p 8091 -h 192.168.137.128
```

基础设施清单：

| 服务 | 端口 | 访问 |
|------|------|------|
| MySQL | 3306 | `root/123` |
| Nacos | 8848 | `nacos/nacos` |
| RabbitMQ | 15672 | `guest/guest` |
| Sentinel | 8858 | `sentinel/sentinel` |
| Elasticsearch | 9200 | — |
| Seata TC | 8091 | — |

### 2. 启动微服务

**方式一：IDEA** — 依次运行各模块的 `*Application.java`

**方式二：Maven 命令行**

```bash
# Windows 需配置 JDK17 --add-opens（已配置在父 pom 中）
mvn spring-boot:run -pl item-service
```

启动顺序不依赖（MQ 队列由各服务自行声明）。

### 3. 验证

```bash
# 健康检查
curl http://localhost:8081/actuator/health

# ES 商品搜索
curl "http://localhost:8081/items/search?key=华为&pageNo=1&pageSize=3"

# 带过滤 + 排序
curl "http://localhost:8081/items/search?key=手机&brand=华为&minPrice=1000&sortBy=sold&isAsc=false"

# 通过网关（需要 JWT token）
# 1. 登录获取 token
curl -X POST http://localhost:8080/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"Jack","password":"123"}'
# 2. 带 token 访问
curl http://localhost:8080/items/search?key=华为 \
  -H "Authorization: Bearer <token>"
```

## 核心功能

| 功能 | 说明 |
|------|------|
| ES 商品搜索 | IK中文分词，支持分类/品牌/价格过滤，按销量/价格/评论数排序 |
| traceId 链路追踪 | Gateway 生成 UUID，Feign 透传，日志输出 `[traceId]` |
| 订单状态机 | 未支付→已支付→已发货→已完成，仅未支付可取消 |
| 支付超时取消 | MQ TTL 30分钟，到期未支付自动取消 |
| 分布式事务 | Seata AT模式（下单扣库存），file 模式注册 |
| 限流熔断 | Gateway Sentinel 限流（搜索100/s，下单50/s，登录20/s） |
| MQ 可靠性 | 发送确认 + 手动ACK + 幂等表 + 死信队列 + 重试 |
| 健康检查 | Actuator 暴露 db/discovery/disk/es/rabbit 健康状态 |

## 技术栈

- **Spring Boot** 2.7.12 / **Spring Cloud** 2021.0.3 / **Alibaba** 2021.0.5.0
- **MySQL 8.0** / **MyBatis-Plus** 3.4.3
- **Nacos** 服务发现 + 配置管理
- **Sentinel** 限流熔断（服端 + Gateway）
- **Seata** 1.6.1 分布式事务
- **RabbitMQ** 3.12 异步消息（延迟队列 = TTL + DLQ）
- **Elasticsearch** 7.17 + IK 分词器
- **Knife4j** Swagger 接口文档
