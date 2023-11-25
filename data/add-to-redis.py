import os
import json
import redis

def add_json_elements_to_redis(folder_path):
    r = redis.StrictRedis(host='localhost', port=6379, db=0)

    for root, dirs, files in os.walk(folder_path):
        for file in files:
            if file.endswith('.json'):
                file_path = os.path.join(root, file)
                folder_name = os.path.basename(root)

                with open(file_path, 'r') as f:
                    data = json.load(f)

                print("处理"+folder_name)

                key = f"json:{folder_name}"
                for item in data:
                    r.sadd(key, json.dumps(item))

# 指定文件夹路径，将该文件夹下的每个json数组元素添加到Redis的set中
folder_path = './'
add_json_elements_to_redis(folder_path)

