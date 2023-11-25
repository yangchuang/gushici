## 随机返回一首诗词
[古诗词API](https://github.com/xenv/gushici) （[古诗词](GUISHICI.md)）修改版。
<font style="color:red">**修改后的版本只支持json格式返回**</font>

```json
{
    "welcome": "欢迎使用古诗词·一言",
    "api-document": "下面为本API可用的所有类型，支持json格式输出",
    "help": "具体安装方法请见GUISHICI.md",
    "list": [
        {
          "全部": "http://localhost:8080/all"
        },
        {
            "唐诗300首": "http://localhost:8080/tangshi300"
        },
        {
            "宋词300首": "http://localhost:8080/songci300"
        },
        {
            "千家诗": "http://localhost:8080/qianjiashi"
        },
        {
            "南唐": "http://localhost:8080/nantang"
        },
        {
            "纳兰性德": "http://localhost:8080/nalanxingde"
        },
        {
            "花间集": "http://localhost:8080/huajianji"
        },
        {
            "古诗十九首": "http://localhost:8080/gushi19"
        }
    ]
}
```

### 安装 redis
```shell
docker pull redis:latest
docker images
  #-p 6379:6379：映射容器服务的 6379 端口到宿主机的 6379 端口。外部可以直接通过宿主机ip:6379 访问到 Redis 的服务。数据目录挂载到服务器/data/redis 目录下
  # docker run -itd --name redis -p 6379:6379 redis
docker run -d --name redis-container -v /data/redis:/data --restart=always -p 6379:6379 redis
docker ps
  #通过 redis-cli 连接测试使用 redis 服务
docker exec -it redis /bin/bash
redis-cli
  # 重启容器
docker start [container id]
```

### 服务器后台运行代码 
带日志输出  
`nohup java -jar yiyan-1.4.1-fat.jar -conf conf.json > output.log 2>&1 &`

不要日志输出  
`nohup java -jar yiyan-1.4.1-fat.jar -conf conf.json &`



### 数据集
新的数据集集使用了 [花间集](https://github.com/chinese-poetry/huajianji) 中的一部分，并做了一些修改。每首诗词添加了sentence字段，字段值为诗词中的比较好的诗句，跟原[古诗词API](https://github.com/xenv/gushici)返回的content字段含义一致。sentence是先用python脚本将诗词的首句赋值的，然后再人工重新检查修改，所以不一定准确（比如花间集没时间检查了，sentence都是用了首句）。

## 感谢
- 数据源 [花间集](https://github.com/chinese-poetry/huajianji)
- [古诗词API](https://github.com/xenv/gushici)