# 黑马商城 · hmall（Spring Cloud Alibaba 微服务电商）

一套可完整跑通的微服务电商后端 + 静态前端：网关鉴权、ES 商品搜索、购物车、下单、余额支付，用 Nacos / Sentinel / Seata / RabbitMQ / Elasticsearch 串起分布式场景。

## 架构

```
                        ┌──────────────┐
  浏览器 (hmall-frontend)│  hm-gateway  │ :8080  JWT 鉴权 / 路由 / traceId / Sentinel 限流
                        └──────┬───────┘
        ┌──────────┬───────────┼───────────┬───────────┐
   item-service cart-service user-service trade-service pay-service
     :8081        :8082         :8083        :8084        :8085
     商品/ES搜索   购物车        用户/登录     订单/状态机    余额支付
        └──────────┴───────────┴───────────┴───────────┘
                 Nacos（注册+配置） · Sentinel（限流熔断） · Seata（AT 分布式事务）
                 MySQL 8 · Redis · RabbitMQ（延迟/死信） · Elasticsearch 7.17 + IK
```

## 模块

```
hmall/
├── hm-gateway/       网关 (8080)   — JWT 认证 + 路由 + traceId + 限流
├── item-service/     商品 (8081)   — ES 搜索 + 库存扣减
├── cart-service/     购物车 (8082) — Feign 调用 + MQ 清空
├── user-service/     用户 (8083)   — BCrypt + JWT 签发
├── trade-service/    订单 (8084)   — Seata 分布式事务 + 支付超时
├── pay-service/      支付 (8085)   — 余额支付
├── hm-common/        公共模块      — JWT 工具 / 异常处理 / 自动配置
├── hm-api/           Feign 接口定义
├── hmall-frontend/   静态前端（HTML/CSS/JS，无需构建）
├── docker-compose.yml             — 基础设施一键启动
├── start-all.bat / frontend.bat   — Windows 一键启动
└── pom.xml                        — 父工程
```

## 技术栈

- **Spring Boot** 2.7.12 / **Spring Cloud** 2021.0.3 / **Spring Cloud Alibaba** 2021.0.5.0
- **Nacos** 服务发现 + 配置中心（`bootstrap.yml` 拉取共享配置）
- **Sentinel** 限流熔断（服务端 + Gateway 网关层）
- **Seata** 1.6.1 分布式事务（AT 模式，下单扣库存）
- **RabbitMQ** 3.12（延迟队列 = TTL + 死信，发送确认 + 手动 ACK + 幂等表）
- **Elasticsearch** 7.17 + IK 中文分词（商品搜索）
- **MySQL 8.0** + **MyBatis-Plus** 3.4.3、**Redis**、**Knife4j** 接口文档

## 核心实现

| 能力 | 做法 |
|:----|:----|
| ES 商品搜索 | IK 分词，支持分类/品牌/价格过滤，按销量/价格/评论数排序 |
| traceId 链路追踪 | Gateway 生成 UUID → Feign 拦截器透传 → 日志统一输出 `[traceId]` |
| 订单状态机 | 未支付 → 已支付 → 已发货 → 已完成，仅未支付可取消 |
| 支付超时取消 | MQ TTL 30 分钟，到期未支付自动取消并回滚库存 |
| 分布式事务 | Seata AT 模式（下单扣库存跨 trade/item 服务） |
| 限流熔断 | Gateway Sentinel 规则：搜索 100/s、下单 50/s、登录 20/s |
| MQ 可靠性 | 发送确认 + 手动 ACK + 幂等表 + 死信队列 + 重试 |
| 健康检查 | Actuator 暴露 db / discovery / disk / es / rabbit 状态 |
| 一键启动 | `docker-compose.yml` 起中间件，`start-all.bat` 起 6 个服务 + 前端 |

## 快速启动

### 1. 基础设施（Docker Compose）

```bash
# 在装有 Docker 的 Linux VM 上
docker compose up -d
```

| 服务 | 端口 | 备注 |
|:----|:----|:----|
| MySQL | 3306 | 库名 `hmall`，账号密码自行在 compose 里设置 |
| Nacos | 8848 | 默认 `nacos/nacos` |
| RabbitMQ | 15672 | 默认 `guest/guest` |
| Sentinel Dashboard | 8858 | 默认 `sentinel/sentinel` |
| Elasticsearch | 9200 | 7.17 + IK 分词 |
| Seata TC | 8091 | file 模式注册 |

> 各服务 `application.yaml` 中的 `host` 需要改成你 VM 的 IP（仓库里是占位值）。

### 2. JWT 密钥（必做，仓库不含 keystore）

`hm-gateway` 与 `user-service` 用 RSA 私钥签发/校验 JWT，keystore **不入库**，首次运行需自己生成：

```bash
keytool -genkeypair -alias hmall -keyalg RSA -keysize 2048 -validity 3650 \
  -storetype PKCS12 -keystore hmall.jks \
  -storepass hmall123 -keypass hmall123 \
  -dname "CN=hmall, OU=dev, O=hmall, L=Hangzhou, ST=Zhejiang, C=CN"
```

把生成的 `hmall.jks` 放到 `hm-gateway/src/main/resources/` 与 `user-service/src/main/resources/`（其余服务不读它）。
口令与 `application.yaml` 中的 `hm.jwt.password`、`hm.jwt.alias` 保持一致；正式环境请换成随机口令并放环境变量。

### 3. 启动微服务

- **IDEA**：`.idea/runConfigurations` 里已配好 6 个服务的启动项，逐个运行即可。
- **命令行**：`mvn spring-boot:run -pl item-service`（Windows 下 JDK 17 的 `--add-opens` 已写在父 pom）。

### 4. 验证

```bash
curl http://localhost:8081/actuator/health
curl "http://localhost:8081/items/search?key=手机&pageNo=1&pageSize=3"

# 登录拿 token（演示账号）
curl -X POST http://localhost:8080/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"Jack","password":"123"}'

curl http://localhost:8080/items/search?key=手机 -H "Authorization: Bearer <token>"
```

接口文档：各服务 `/doc.html`。

## 安全说明

- 仓库**不再包含**任何 keystore / 私钥；`.gitignore` 已加固（`*.jks`、`*.p12`、`*.pfx`、`*.pem`、`*.key`、`application-local.yaml`）。
- 项目曾误提交过一份 JWT 私钥 keystore：已轮换密钥并重写全部 49 个历史提交，历史中不再存在该文件（`git rev-list --all --objects` 校验为 0）。
- 配置里的数据库口令、VM IP 均为示例值，部署时请替换。

## 已知限制

- 需要一台装好 Docker 的 VM 提供 6 个中间件，无法单机一键起（这是架构的固有成本）。
- 没有自动化测试与 CI，验证主要靠手工 curl + 前端点击。
- 前端是静态 HTML/CSS/JS，未做工程化（无打包、无路由），只求能跑通。
