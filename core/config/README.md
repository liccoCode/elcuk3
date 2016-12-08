## 自动部署的介绍
整个 eluck2 项目的自动部署分为如下几个部分, 每个部分确认 OK, 项目则可以使用 cap <xxx> deploy 来完成无人值守的自动部署.

### 项目使用的工具
* capistrano , 用于自动部署的脚本工具
* supervisor , 用于在服务器上进行进程管理的工具


### supervisor 部分
如果需要部署多个不同的 elcuk2 的项目实例, 会有很多的变量是需要改变的, 那么这些信息被全部抽取到了环境变量中,
同时, 为了让这些信息能够在项目部署的环境上固化下来, 使用了 supervisor 工具, 解决如下几个问题:
1. 项目启动的各项配置参数固化. 转而使用一条 supervisorctl restart/start/stop erp 进行启停
2. 控制项目的自动宕机的问题. 如果项目异常挂掉,  supervisor 会自动重启.

每个部署实例都拥有一个自己的 erp.conf 文件存放在部署服务器的 `/etc/supervisor/conf.d` 目录下, 这个文件在
`conf/erp.conf` 中有一个模板, 其中标记了 `--change_me---` 的则为需要替换的部分.

环境变量列别:

* ROOT_URL: 当前项目在互联网上访问的根路径
* JVM_MEM: 在 application.conf 中的 jvm.memory 的变量, 控制 JVM 的内存使用.
* EXCHANGERATE_TOKEN: 使用了 EXCHANGERATE 的汇率变化服务, 需要的 Token. ex: nnsYEb2gv6r69qYT5849g9N9X4J63M0r
* DB_USER: 数据库名称. ex: elcuk2
* DB_NAME: 登陆数据库用户名. ex: user
* DB_PASS: 登陆数据库密码. ex: password
* DB_HOST: 登陆数据库的 url 地址. ex: rdsb2vz83t04123d2tq2.mysql.rds.aliyuncs.com
* DB_POOL_MIN: 数据库连接池最小数量. ex: 10
* DB_POOL_MAX: 数据库连接池最大数量. ex: 30
* REDIS_HOST: 项目的 Cache 所使用的 REDIS 连接. ex: 127.0.0.1
* ROCKEND_HOST: 后端任务项目的 ROCKEND_HOST 地址, 主要用于提交后端任务. ex: http://10.117.234.4:4567
* ES_INDEX: 系统使用 ES 时, 在 ES 中的 index 命令(类似 mysql 的数据库名称). ex: elcuk2
* ES_HOST: 系统使用的 ES 的 Host 地址. ex: http://10.117.239.66:9200
* KOD_HOST: 系统后端附件项目使用的 HOST 地址. ex: http://47.88.6.96:8080

这些环境变量会被使用在如下的地方:
1. conf/application.conf 文件中, Play 会自行处理, 可以在配置文件中使用 `${ENV}` 来使用.
2. 项目的启动应用时, 例如 ES_HOST, REDIS_HOST 等等, 都是项目运行时需要的, 会在项目启动时被检查.

### capistrano 部分
这个部分负责从部署服务器开始到完整部署项目上线的整个过程中需要做的事情, 并且使用代码的方式固化下来而不再需要人手工介入.

因为当前项目没有 js 预先编译, bundle 处理等等, 所以需要编写的代码较少, 但这里可以负责:

1. 项目的代码更新
2. 项目的目录结构控制
3. 项目的前进/历史版本回滚
4. 前端代码预编译
5. 动态配置文件调整
6. 项目依赖库更新
7. ... 等等

自动部署的代码可以拆分成为一个一个独立的 task, 可将这些代码存放在 `config/lib/capistrano/tasks` 目录下, 以 `.rake` 结尾的 ruby 代码.
这些 task 可以在 `config/deploy.rb` 文件中注册到 capistrano 为我们提供的部署的不同环节的 hook 上([完整的 hook](http://capistranorb.com/documentation/getting-started/flow/))



