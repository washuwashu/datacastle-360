# datacastle-360
datacastle 金融预测大赛  线上第六

##文件目录

```
-RawData                 #赛题原始数据
-java_feature            #java提取特征数据
-python_feature          #python提取特征数据
-JavaCode                #java提取特征代码
-PythonCode              #包含python提取特征，特征选择，模型构建代码
-Rcode                   #R的xgboost模型
-result                  #存储结果
```
##代码说明

```
-JavaCode
          -infoClass.java             #数据结构定义
          -bankRecord2.java           #银行流水特征
          -billRecord.java            #信用卡账单特征
          -browseRecord.java          #浏览记录特征
          -longestOverdue.java        #信用卡改进特征
          -Data_clean.java            #重复记录合并与去重
          -FeatureGet.java            #特征汇总

-PythonCode
          -bank_train.py,bank_test.py         #银行流水特征
          -browse_train.py,browse_test.py     #浏览记录特征
          -bill_train.py,bill_test.py         #信用卡特征
          -feature_combine.py                 #所有特征记录合并
          -lgb_5fold.py                       #lightgbm 模型
          -bill_feature_select                #信用卡记录特征选择
```

