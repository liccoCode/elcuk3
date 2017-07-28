# 项目相关介绍

## 配置文件
项目所使用到外部环境, 例如 DB, Redis, ES 请遵守 [The Twelve-Factor App](http://12factor.net/zh_cn/)
所有, application 中的 db, redis 都抽取到外部, 邮件的发送也应该抽取出来.

## 开发环境的搭建
1. 下载最新的 master 分支的代码
2. 使用 play deps --sync 下载整个项目的依赖
3. 使用 docker-compose up (自动调用项目目录下 docker-compose.yml), 启动中间件
4. 将准备好的 elcuk2-wyatt.sql 文件导入 msyql 中
5. 检查自己的环境变量 (详细见下)
7. play run 运行启动


## 环境变量
下面是一个环境变量的例子, 这些信息如果在开发环境, 那么直接设置进入 ~/.bashrc 或者 ~/.profile 文件,
如果是产品环境, 使用 supervisor 的配置文件模板改造为 environment 的参数
```bash
DB_HOST="rdsb2vz83t044o7d2tq2.mysql.rds.aliyuncs.com"
DB_NAME='elcuk2'
DB_USER='root'
DB_PASS='crater10lake'
REDIS_HOST="127.0.0.1"
ROCKEND_HOST="http://127.0.0.1:4567"
ROOT_URL="http://127.0.0.1:9000"
KOD_HOST="http://127.0.0.1:8080"
ES_INDEX="elcuk2"
ES_HOST="http://----:9200"
```

## 产品环境部署
1. 下载最新的 ea/elcuk2 master 分支代码
2. 使用 play deps --sync 初始化完整的项目依赖
3. 确定好环境变量, 并且将变量初始化到 erp.conf 模板中的 environment 参数
4. 通过 `supervisorctl start erp` 完成项目启动
