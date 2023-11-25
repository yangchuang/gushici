import os
import json

def add_sentence_node(file_path):
    print('processing:' + file_path)
    with open(file_path, 'r') as file:
        data = json.load(file)

    for item in data:
        if 'paragraphs' in item and len(item['paragraphs']) > 0:
            item['sentence'] = item['paragraphs'][0]

    with open(file_path, 'w') as file:
        json.dump(data, file, ensure_ascii=False, indent=2)

def process_json_files(folder_path):
    for root, dirs, files in os.walk(folder_path):
        for file in files:
            if file.endswith('.json'):
                file_path = os.path.join(root, file)
                add_sentence_node(file_path)

# 指定文件夹路径，将该文件夹及其子文件夹中的所有json文件添加sentence节点
folder_path = './nalanxingde'
process_json_files(folder_path)