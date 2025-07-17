# 项目介绍

个人学习项目，[原项目地址](https://github.com/qifan777/dive-into-spring-ai) ；

基于SpringAI实现RAG/Agent大模型应用平台

## 运行环境

- Java 17
- Node.js 18+
- MySQL 8
- DashScope API KEY（或者其他）
- Redis-Stack

  redis基础上拓展向量查询功能

    ```shell
    docker run -d --name redis-stack --restart=always -v redis-data:/data -p 6379:6379 -p 8001:8001 -e REDIS_ARGS="--requirepass 123456" redis/redis-stack:latest
    ```

## 运行步骤

### 1. clone代码

### 2. 修改配置文件

修改application.yml中的API-KEY, MySQL, Redis-Stack, Neo4j配置
### 3. 运行项目

后端运行

1. 运行ServerApplication.java
2. target/generated-sources/annotations右键mark directory as/generated source root

前端运行，在front-end目录下

- npm install
- npm run api （先运行后端）
- npm run dev

