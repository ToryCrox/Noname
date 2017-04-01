#!/usr/bin/env python3
# -*- coding: utf-8 -*-

'''
Created on 2016年4月16日

@author: Tory
'''
import os
import base64
import sys

READ_SIZE = 1024 * 10
CHARESET = 'utf-8'




def base64_file(file,output_file,is_encode = True,delete_org = False):
    global encode_file_list, decode_file_list
    with open(file,'rb') as fr ,open(output_file,'wb') as fw:
        fbb = fr.read()
        bse_fbb = base64.b64encode(fbb) if is_encode else base64.b64decode(fbb)
        fw.write(bse_fbb)
        f_set = (file,output_file)
        encode_file_list.append(f_set) if is_encode else decode_file_list.append(f_set)
        print('base64文件:"'+file+'"到"'+output_file+'"')
    if delete_org:
        os.remove(file)
        print('删除文件:file:'+file)


def cript_file(file,is_encode = True,delete_org = False):
    ext_dict = encode_ext_dict if is_encode else decode_ext_dict
    folder = os.path.dirname(file)
    basename = os.path.basename(file)
    split = os.path.splitext(basename)
    if len(split) <= 1 or split[1] not in ext_dict.keys():
        return
    name = split[0]
    ext = split[1]
    out_ext = ext_dict[ext]
    out_file = os.path.join(folder,name + out_ext)
    print('process:'+file)
    base64_file(file,out_file,is_encode,delete_org)


def collect_files(root):
    global all_files
    list_files = [os.path.join(root,file) for file in os.listdir(root)]
    list_files_f= [file for file in list_files if os.path.isfile(file)]
    all_files.extend(list_files_f)
    list_files_dir= [file for file in list_files if os.path.isdir(file)]
    list(map(collect_files,list_files_dir))

def collect_files1(root):
    global all_files
    for dirpath, dirnames, filenames in os.walk(root):
        file_paths = [os.path.join(dirpath,file) for file in filenames]
        all_files.extend(file_paths)

encode_file_list = []
decode_file_list = []

encode_ext_dict = {'.java':".avaj",'.xml':'.lmx','.doc':'.cod','.docx':'.xcod'}
decode_ext_dict = {value:key for key,value in encode_ext_dict.items()}

is_encode = True
is_delete = True
#root = r'E:\workspace\git\T_InCallUI\doc\XUI_InCallUI'
root_path_list = []



'''
参数：
-d 解密
-b 不删除
'''
for i in range(len(sys.argv)):
    arg = sys.argv[i]
    if arg.startswith('-'):
        for j in range(len(arg)):
            if arg[j] == 'd': #这里写参数
                is_encode = False
            elif arg[j] == 'b':
                is_delete = False
    elif i > 0:
        root_path_list.append(arg.strip())

if len(root_path_list) == 0:
    input_path = input("请输入需要%s的文件夹或文件夹:" % (('加密' if is_encode else '解密')))
    root_path_list.append(input_path.strip())

if len(root_path_list) == 0:
    print('无有效的文件或文件夹输入')

print("root_path_list:"+str(root_path_list))
all_files = []
#collect_files(root)
for path in root_path_list:
    if os.path.isfile(path):
        cript_file(file,is_encode,is_delete)
    elif os.path.isdir(path):
        collect_files1(path)
        list(map(lambda file: cript_file(file,is_encode,is_delete),all_files))
        all_files.clear()
        print(path)
        print('    encode_file_list:'+str(len(encode_file_list))+';') if is_encode else print('decode_file_list:'+str(len(decode_file_list))+';')

    else:
        print('%s不是有效的文件和文件夹' % path)


