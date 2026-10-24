<div align="center">

# 智慧游客管理云平台

**Scenic Platform · 前后端分离的景区智慧旅游平台**

游客在线购票 · 智能路线规划 · 景点导览 &nbsp;|&nbsp; 管理端实时监控 · 数据分析 · 票务与景点管理

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-6DB33F?logo=springboot&logoColor=white)
![MyBatis](https://img.shields.io/badge/MyBatis-3.0.5-red)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-6%2B-DC382D?logo=redis&logoColor=white)
![Vue](https://img.shields.io/badge/Vue-3.3-4FC08D?logo=vuedotjs&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-4-646CFF?logo=vite&logoColor=white)
![Element Plus](https://img.shields.io/badge/Element%20Plus-2.3-409EFF?logo=element&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue)

</div>

---

## 项目简介

一个面向景区的「游客服务 + 后台运营」一体化平台，采用前后端分离架构：

- **游客端**：浏览景区信息与公告、在线购票下单、按偏好智能生成游览路线、景点导览与服务设施查询、订单与个人中心。
- **管理端**：今日客流与景点实时分布监控、客流趋势与热力图、营收/票务/游客画像多维分析并支持报表导出、景点与订单的增删改查、景区信息维护与管理员改密。

前后端通过 RESTful API + JWT 令牌通信，前端开发环境由 Vite 代理转发请求，生产环境可直接由 Nginx 托管静态资源并反向代理后端。

## 技术栈

| 层次 | 技术选型 |
| --- | --- |
| 后端框架 | Spring Boot 3.3.4、Java 17 |
| 持久层 | MyBatis 3.0.5、PageHelper 1.4.7 分页插件 |
| 数据库 / 缓存 | MySQL 8、Redis（Lettuce 连接池） |
| 认证鉴权 | JWT（jjwt 0.9.1）+ 拦截器，游客 / 管理员双令牌体系 |
| 工程增强 | Lombok、Hutool、Spring AOP、Validation 参数校验 |
| 前端框架 | Vue 3.3（组合式 API）、Vue Router 4、Pinia |
| UI / 可视化 | Element Plus 2.3、ECharts 5 图表、百度地图 JavaScript API GL（BMapGL） |
| 构建 / 请求 | Maven、Vite 4、Axios（统一封装与拦截器） |

## 功能模块

### 游客端

| 页面 | 路由 | 主要能力 |
| --- | --- | --- |
| 首页 | `/` | 景区信息、公告通知、推荐景点 |
| 在线购票 | `/ticket` | 选择票种（单次票 / 套票）、填写游客信息、提交订单 |
| 路线规划 | `/route-plan` | 按游览偏好与时间智能生成路线，可保存为我的路线 |
| 景点导览 | `/guide` | 景点列表与详情、服务设施、地图定位 |
| 我的订单 | `/orders` | 订单查询、取消订单、门票核销 |
| 我的路线 | `/routes` | 已保存路线的查看与删除 |
| 个人中心 | `/profile` | 资料修改、修改密码 |
| 登录 / 注册 | `/login`、`/register` | 游客账号注册与登录 |

### 管理端

| 页面 | 路由 | 主要能力 |
| --- | --- | --- |
| 管理员登录 | `/admin` | 管理端独立登录入口 |
| 实时监控 | `/admin/dashboard` | 今日客流概览、各景点实时人数、客流趋势、热力图 |
| 数据分析 | `/admin/analysis` | 营收统计、票务分析、游客画像、报表导出 |
| 景点管理 | `/admin/spots` | 景点分页查询、新增、编辑、删除 |
| 订单管理 | `/admin/orders` | 订单分页与条件筛选、订单详情、状态流转 |
| 景区设置 | `/admin/setting` | 景区信息维护、管理员修改密码 |

## 项目结构

```
scenic-platform/
├── backend/                          # Spring Boot 后端服务
│   ├── src/main/java/com/scenic/
│   │   ├── aspect/                   # 服务层慢调用监控切面
│   │   ├── common/                   # 统一响应体 Result、分页 PageResult、状态码
│   │   ├── config/                   # 跨域、Web、Redis 缓存、JWT 配置
│   │   ├── controller/
│   │   │   ├── admin/                # 管理端接口
│   │   │   └── visitor/              # 游客端接口
│   │   ├── dto/                      # 请求参数对象
│   │   ├── entity/                   # 数据库实体
│   │   ├── exception/                # 业务异常与全局异常处理
│   │   ├── interceptor/              # JWT 登录拦截器
│   │   ├── mapper/                   # MyBatis Mapper 接口
│   │   ├── service/ + service/impl/  # 业务逻辑
│   │   └── utils/                    # JWT 工具类、BCrypt 口令工具
│   ├── src/main/resources/
│   │   ├── com.scenic.mapper/        # MyBatis Mapper XML
│   │   └── application.yml           # 应用配置
│   ├── mvnw / mvnw.cmd               # Maven Wrapper
│   └── pom.xml
├── frontend/                         # Vue 3 前端工程
│   ├── src/
│   │   ├── api/                      # Axios 封装与接口定义
│   │   ├── components/               # 页头、页脚、管理端侧边栏等公共组件
│   │   ├── router/                   # 路由表与登录守卫
│   │   ├── styles/                   # 全局与表格样式
│   │   └── views/
│   │       ├── admin/                # 管理端页面
│   │       └── visitor/              # 游客端页面
│   ├── index.html
│   ├── vite.config.js                # 别名、开发端口与 /api 代理
│   └── package.json
├── sql/
│   └── schema.sql                    # 数据库建表脚本（9 张表）
└── README.md
```

## 接口设计

### 统一响应体

所有接口返回统一结构，前端在 Axios 响应拦截器中统一解包：

```json
{ "code": 200, "message": "操作成功", "data": {} }
```

| 字段 | 说明 |
| --- | --- |
| `code` | 状态码，`200` 成功，`400` 参数错误，`401` 未登录/令牌失效，`403` 无权限，`500` 服务端异常 |
| `message` | 提示信息，失败时可直接展示给用户 |
| `data` | 业务数据，分页场景为 `{ list, total, pageNum, pageSize }` |

### 认证方式

- 游客登录 `POST /api/visitor/login`、管理员登录 `POST /api/admin/login` 成功后返回 JWT 令牌。
- 后续请求在请求头携带 `Authorization: Bearer <token>`；前端按请求地址前缀自动选择游客令牌或管理员令牌。
- 后端由 `JwtInterceptor` 校验令牌并解析出用户身份，令牌失效返回 `401`，前端自动清理本地令牌并跳转登录页。

### 接口一览

**游客端**

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/visitor/login` | 游客登录 |
| POST | `/api/visitor/register` | 游客注册 |
| POST | `/api/visitor/logout` | 退出登录 |
| GET / PUT | `/api/visitor/user/info` | 查询 / 修改个人资料 |
| PUT | `/api/visitor/user/password` | 修改密码 |
| GET | `/api/visitor/home/info` | 首页景区信息 |
| GET | `/api/visitor/home/announcements` | 公告列表 |
| GET | `/api/visitor/spot/list` | 景点列表 |
| GET | `/api/visitor/spot/{id}` | 景点详情 |
| GET | `/api/visitor/spot/facilities` | 服务设施 |
| GET | `/api/visitor/ticket/types` | 票种与价格 |
| POST | `/api/visitor/ticket/create` | 创建订单 |
| GET | `/api/visitor/ticket/list` | 我的订单列表 |
| PUT | `/api/visitor/ticket/cancel/{id}` | 取消订单 |
| PUT | `/api/visitor/ticket/use/{id}` | 门票核销 |
| POST | `/api/visitor/route/generate` | 智能生成游览路线 |
| POST | `/api/visitor/route/save` | 保存路线 |
| GET | `/api/visitor/route/list` | 我的路线 |
| DELETE | `/api/visitor/route/{id}` | 删除路线 |

**管理端**

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/admin/login` | 管理员登录 |
| GET | `/api/admin/monitor/today` | 今日客流概览 |
| GET | `/api/admin/monitor/spots` | 各景点实时人数 |
| GET | `/api/admin/monitor/trend` | 客流趋势（`type=daily/monthly`） |
| GET | `/api/admin/monitor/heatmap` | 热力图数据 |
| GET | `/api/admin/analysis/revenue` | 营收分析 |
| GET | `/api/admin/analysis/ticket` | 票务分析 |
| GET | `/api/admin/analysis/visitor` | 游客画像 |
| GET | `/api/admin/analysis/export` | 报表导出 |
| GET | `/api/admin/order/list` | 订单分页查询 |
| GET | `/api/admin/order/{id}` | 订单详情 |
| PUT | `/api/admin/order/{id}/status` | 更新订单状态 |
| GET | `/api/admin/spot/list` | 景点分页查询 |
| POST | `/api/admin/spot/add` | 新增景点 |
| PUT | `/api/admin/spot/update` | 修改景点 |
| DELETE | `/api/admin/spot/{id}` | 删除景点 |
| GET / PUT | `/api/admin/setting/scenic` | 查询 / 修改景区信息 |
| PUT | `/api/admin/setting/password` | 修改管理员密码 |

## 快速开始

### 1. 环境要求

| 组件 | 版本 |
| --- | --- |
| JDK | 17+ |
| Maven | 3.8+（或直接使用项目自带的 Maven Wrapper） |
| MySQL | 8.0+ |
| Redis | 6.0+ |
| Node.js | 16+ |

### 2. 初始化数据库

```bash
mysql -uroot -p < sql/schema.sql
```

脚本会自动创建并切换到 `scenic_platform` 库，并建立以下 9 张表：

| 表名 | 说明 |
| --- | --- |
| `admin_user` | 管理员表 |
| `visitor` | 游客表 |
| `scenic_info` | 景区信息表 |
| `scenic_spot` | 景点表 |
| `ticket_order` | 门票订单表 |
| `ticket_visitor` | 购票人信息表 |
| `entry_record` | 入园记录表 |
| `visitor_route` | 游览路线表 |
| `route_spot` | 路线景点关联表 |

### 3. 启动后端

```bash
cd backend
./mvnw spring-boot:run          # Windows 使用 mvnw.cmd spring-boot:run
```

服务默认监听 `http://localhost:8080`。也可先打包再运行：

```bash
./mvnw clean package -DskipTests
java -jar target/scenic-platform-backend-1.0.0.jar
```

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

### 5. 访问

| 入口 | 地址 |
| --- | --- |
| 游客端 | http://localhost:5173 |
| 管理端 | http://localhost:5173/admin |
| 后端接口 | http://localhost:8080/api |

> 管理员账号存放于 `admin_user` 表；游客账号可在登录页自助注册。

## 配置说明

后端配置位于 `backend/src/main/resources/application.yml`，关键项如下：

| 配置项 | 说明 | 默认值 |
| --- | --- | --- |
| `server.port` | 服务端口 | `8080` |
| `spring.datasource.url` | 数据库连接 | `jdbc:mysql://localhost:3306/scenic_platform` |
| `spring.datasource.username` / `password` | 数据库账号 | 由环境变量 `DB_USERNAME` / `DB_PASSWORD` 覆盖 |
| `spring.data.redis.host` / `port` | Redis 地址 | `localhost:6379` |
| `jwt.secret` | JWT 签名密钥 | 由环境变量 `JWT_SECRET` 覆盖 |
| `jwt.expiration` / `jwt.admin-expiration` | 游客 / 管理员令牌有效期 | `7200000`（2 小时） |
| `app.mock-enabled` | 是否启用模拟数据（开发环境） | `true` |
| `app.single-ticket-price` / `app.combo-ticket-price` | 单次票 / 套票价格 | `60` / `168` |
| `app.max-order-quantity` | 单笔订单最大票数 | `10` |

数据库、Redis、JWT 密钥均支持通过环境变量覆盖，部署时无需改动源码：

```bash
export DB_HOST=127.0.0.1 DB_USERNAME=scenic DB_PASSWORD=your-password
export REDIS_HOST=127.0.0.1 JWT_SECRET=your-own-long-secret
```

前端开发环境的接口代理在 `frontend/vite.config.js` 中配置，默认将 `/api` 转发到 `http://localhost:8080`。

## 打包部署

```bash
# 1. 打包前端静态资源
cd frontend && npm run build            # 产物输出到 frontend/dist

# 2. 打包后端可执行 JAR
cd ../backend && ./mvnw clean package -DskipTests
```

Nginx 参考配置：

```nginx
server {
    listen 80;
    root /var/www/scenic-platform/dist;   # 指向 frontend/dist

    location / {
        try_files $uri $uri/ /index.html; # 适配前端 history 路由
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 说明

- 项目为个人学习与实战练习作品，后端 `app.mock-enabled` 开启时会使用模拟数据，便于在无真实客流数据时演示监控与分析页面。
- 仓库内 `application.yml` 中的数据库口令为本地开发默认值，请在生产环境中通过环境变量覆盖。
- 前端 `node_modules`、构建产物与 IDE 配置均已通过 `.gitignore` 排除。

## License

本项目基于 [MIT License](https://opensource.org/licenses/MIT) 开源，可自由用于学习与二次开发。