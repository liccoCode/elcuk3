# 项目相关介绍

## 配置文件
项目所使用到外部环境, 例如 DB, Redis, ES 请遵守 [The Twelve-Factor App](http://12factor.net/zh_cn/)
所有, application 中的 db, redis 都抽取到外部, 邮件的发送也应该抽取出来.

## 开发环境的搭建
1. 下载最新的 master 分支的代码
2. 使用 play deps 下载整个项目的依赖
3. 使用 cd core/lib; wget http://77g8qz.com1.z0.glb.clouddn.com/mws-1.0.jar  解决单独的 mws-1.0 的依赖.
4. 使用 docker-compose up (自动调用项目目录下 docker-compose.yml), 启动中间件
5. 将准备好的 elcuk2-wyatt.sql 文件导入 msyql 中
6. 检查自己的环境变量中的 DB_HOST, DB_NAME, DB_PASS, REDIS_HOST 环境变量参数
6. play run 运行启动