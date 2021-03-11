# ImageAiClassification

一个简单的图像识别demo

[![](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![](https://img.shields.io/badge/version-0.0.1-yellow.svg)](https://bintray.com/beta/#/paul623/maven/wdsyncer?tab=overview)  [![](https://img.shields.io/badge/dynamic/json?labelColor=11ab60&color=282c34&label=%E9%85%B7%E5%AE%89%20Coolapk&suffix=%20fans&query=%24.data.totalSubs&url=https%3A%2F%2Fapi.spencerwoo.com%2Fsubstats%2F%3Fsource%3Dcoolapk%26queryKey%3D1258736&logo=data:image/svg+xml;base64,PHN2ZyBjbGFzcz0iaWNvbiIgdmlld0JveD0iMCAwIDEwMjQgMTAyNCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB3aWR0aD0iNjQiIGhlaWdodD0iNjQiPjxkZWZzPjxzdHlsZS8+PC9kZWZzPjxwYXRoIGQ9Ik0xMjcuODkzIDQyNi42NjdjMjkuOTItNjYuOTg3IDk0LjUwNy0xMTYuNjk0IDE2Ni40LTEzMC4zNDcgNTUuNzg3LTkuNiAxMTIuOTYgNS4wNjcgMTYxLjkyIDMxLjk0N0M0OTcuNzYgMzQ5LjQ0IDUzNC40IDM3OC44OCA1NjcuOTQ3IDQxMS4wNGMtMTYuMTYgMTguNC0zOS4wOTQgMjguODUzLTU3LjQ5NCA0NC43NDctNDYuMTMzLTM4Ljg4LTk2LjY0LTc3LjcwNy0xNTcuOTczLTg3LjA5NC03OC45MzMtMTMuMTczLTE1OC41NiA0OS4yMjctMTcwLjUwNyAxMjcuMTQ3LTguNjkzIDQ1LjkyIDEwLjEzNCA5NC42NjcgNDUuMTc0IDEyNC45MDcgMzkuNjggMzQuOTg2IDk3LjIyNiA0NC41ODYgMTQ3LjYyNiAzMS4yNTMgNTcuNi0xMy45MiAxMDEuOTc0LTU3LjA2NyAxMzYuODU0LTEwMi43NzMgNTQuMDgtNzIuMTA3IDk5LjItMTUwLjQgMTQ3Ljg0LTIyNi4xMzQgMTMuOTItMTkuMTQ2IDQ3LjQxMy0xNy4yMjYgNTguNzIgMy44NCA2My42MjYgMTA5LjAxNCAxMjYuMDggMjE4LjcyIDE4OS42IDMyNy43ODcgNy41NzMgMTUuMDkzIDQuNDI2IDM1Ljc4Ny05LjYgNDYuMTMzLTEzLjA2NyAxMC42MTQtMzMuMzM0IDEwLjI0LTQ2LjEzNC0uNjkzYTk3MDY2LjU1OCA5NzA2Ni41NTggMCAwMS0yMjYuMTg2LTE2Mi43MmMxOC44OC0xNS4wNCAzOC40LTI5LjMzMyA1Ny45NzMtNDMuNDY3IDIzLjczMyAxMi45MDcgNDMuNzg3IDMzLjE3NCA2OS42IDQxLjY1NC0yMC4zNzMtMzkuNTc0LTQzLjYyNy03Ny43MDctNjYuMzQ3LTExNS45NDctNDIuNjY2IDU5LjE0Ny03Ny4wNjYgMTI0LjIxMy0xMjMuMTQ2IDE4MS4wNjdDNTE2IDY2My40NjcgNDQ4LjggNzE2Ljk2IDM2OC42NCA3MjguNDhjLTM4Ljg4IDMuNDEzLTc5LjMwNyA0LjIxMy0xMTYuMzczLTkuOTczLTUzLjQ5NC0xOS4xNDctMTAwLjMyLTU4LjcyLTEyNC41ODctMTEwLjU2LTI4LjIxMy01Ni4xMDctMjYuNzczLTEyNS4wMTQuMjEzLTE4MS4yOHoiIGZpbGw9IiNmZmYiLz48L3N2Zz4=&longCache=true)](http://www.coolapk.com/u/1258736) [![](https://img.shields.io/badge/dynamic/json?color=282c34&labelColor=0084ff&label=%E7%9F%A5%E4%B9%8E%E5%85%B3%E6%B3%A8&query=%24.data.totalSubs&url=https%3A%2F%2Fapi.spencerwoo.com%2Fsubstats%2F%3Fsource%3Dzhihu%26queryKey%3Dzhu-bao-luo-29&longCache=true)](https://www.zhihu.com/people/zhu-bao-luo-29)

## 碎碎念

这个demo是为毕设中卷积神经网络智能相册做的准备

目前自己搭建的卷积神经网络识别效果不太好而且体积巨大

偶然间发现了TensorFlow Lite Model Maker这个神器，只需要几行代码就可以使用EfficientNet这个神经网络来训练。

当然本demo的模型也来自于该网络训练出的结果

## TFlite模型训练代码

先导入依赖包

```python
pip install -q tflite-model-maker
```

```python
import os

import numpy as np

import tensorflow as tf
assert tf.__version__.startswith('2')

from tflite_model_maker import configs
from tflite_model_maker import ExportFormat
from tflite_model_maker import image_classifier
from tflite_model_maker import ImageClassifierDataLoader
from tflite_model_maker import model_spec

import matplotlib.pyplot as plt

image_path='E://PycharmProjects//LearingImageData//DataSets//train'
model_dir='E://PycharmProjects//MLTest//tflite_model'

data = ImageClassifierDataLoader.from_folder(image_path)

train_data, rest_data = data.split(0.8)
validation_data, test_data = rest_data.split(0.5)

model = image_classifier.create(train_data, validation_data=validation_data)

model.summary()

loss,accuracy = model.evaluate(test_data)

model.export(export_dir=model_dir)

```

这里有几个坑注意一下，官网是提供了quant的方法但是目前跑起来会报错，所以就直接导出了，后期如果成功我再贴出代码。

### 可能会报的错：

1.oserror，SavedModel不存在，这个问题直接去缓存文件夹把整个都删除然后再运行一遍就好了，貌似是因为垃圾清理把模型文件删掉了（火绒出来挨打）

2.socker error 这个是网络不好，训练模型的代码应该有一部分是从服务器拉下来的，所以如果报这个错尝试换网或者使用魔法上网吧

## 功能

本项目主要用于测试模型识别准确性的一个初期demo

其实没有啥核心代码，主要是针对安卓Q及以上的图片做了一个特殊适配，毕竟现在隐私权限太敏感了，普通应用根本拿不到手机相册的图片

识别这块准确率不高，主要是三类：others，powerpoint，writing。

目前PowerPoint效果最好，apk大小大约为34MB

## 性能

目前来说经测试

骁龙855plus 小米9PRO 安卓11 1000张照片 45s左右

## 关于

@Paul623

Powered By 巴塞罗那的余晖

博客：https://www.cnblogs.com/robotpaul/

## License

```
Copyright 2020 Paul623. https://github.com/paul623

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
 limitations under the License.
```