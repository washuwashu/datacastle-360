# -*- coding: utf-8 -*-
"""
Created on Tue Feb 21 20:22:12 2017

@author: zhanghuijfls
"""

import pandas as pd
import time

def dummy(data, columns):
    for i in columns:
        data_dummy = pd.get_dummies(data[i], prefix=i)
        data = data.join(data_dummy)
        data = data.drop(i,1)
    return data
#提取use_info特征
use_info_train = pd.read_table('D:/SLaughter_code/RawData/user_info_train.txt',sep=',',header=-1)
use_info_train.columns = ['id','sex','zhiye','edu','marry','hukou']
use_info_train = dummy(use_info_train,['sex','zhiye','edu','marry','hukou'])

use_info_test = pd.read_table('D:/SLaughter_code/RawData/user_info_test.txt',sep=',',header=-1)
use_info_test.columns = ['id','sex','zhiye','edu','marry','hukou']
use_info_test = dummy(use_info_test,['sex','zhiye','edu','marry','hukou'])

#载入bank，browse，bill表特征数据
bank_train = pd.read_csv('D:/SLaughter_code/python_feature/bank_train.csv')
bank_test = pd.read_csv('D:/SLaughter_code/python_feature/bank_test.csv')

a=[0]
a.extend([i for i in range(49,57)])
a.extend([i for i in range(18,48)])
browse_train1 = pd.read_csv('D:/SLaughter_code/java_feature/onlineTrain.csv').iloc[:,a]
browse_train1 = browse_train1.rename(columns={'userID':'id'})
browse_train2 = pd.read_csv('D:/SLaughter_code/python_feature/browse_feature_train.csv')
browse_train = pd.DataFrame(columns=['id'])
browse_train.id = browse_train2.id
browse_train = pd.merge(browse_train, browse_train1, how='left', on='id')
browse_train = pd.merge(browse_train, browse_train2, how='left', on='id')

browse_test1 = pd.read_csv('D:/SLaughter_code/java_feature/onlineTest.csv').iloc[:,a]
browse_test1 = browse_test1.rename(columns={'userID':'id'})
browse_test2 = pd.read_csv('D:/SLaughter_code/python_feature/browse_feature_test.csv')
browse_test = pd.DataFrame(columns=['id'])
browse_test.id = browse_test2.id
browse_test = pd.merge(browse_test, browse_test1, how='left', on='id')
browse_test = pd.merge(browse_test, browse_test2, how='left', on='id')


bill_feature_score = pd.read_csv('D:/SLaughter_code/python_feature/bill_feature_score_gain.csv')
feature = bill_feature_score['feature'].tolist()[:50]
bill_train = pd.read_csv('D:/SLaughter_code/python_feature/train.csv')
bill_train = bill_train[bill_train.hasBill==1]
bill_train = bill_train[['id']+feature]

bill_test = pd.read_csv('D:/SLaughter_code/python_feature/test.csv')
bill_test = bill_test[bill_test.hasBill==1]
bill_test = bill_test[['id']+feature]

#载入放款时间特征
def day(a):
    x = time.localtime(a)
    y = time.strftime('%d',x)
    return y
    
loan_time_train = pd.read_csv('D:/SLaughter_code/RawData/loan_time_train.txt',sep=',',header=-1)
loan_time_train.columns = ['id','loan_time']
loan_time_train['day'] = loan_time_train['loan_time'].apply(day).astype(int)
loan_time_train['loan_time2'] = ((loan_time_train['loan_time']-5800000000)/86400).astype(int)

train = pd.read_table('D:/SLaughter_code/RawData/overdue_train.txt',sep=',',header=-1)
train.columns = ['id','target']


loan_time_test = pd.read_csv('D:/SLaughter_code/RawData/loan_time_test.txt',sep=',',header=-1)
loan_time_test.columns = ['id','loan_time']
loan_time_test['day'] = loan_time_test['loan_time'].apply(day).astype(int)
loan_time_test['loan_time2'] = ((loan_time_test['loan_time']-5800000000)/86400).astype(int)

test = pd.DataFrame(columns=['id'])
test.id = loan_time_test.id

#构造hasbank，hasbill，hasbrowse,hotCode特征
hasBank = pd.DataFrame(columns=['id'])
hasBank.id = bank_train.id
hasBank['hasBank']=1
hasBrowse = pd.DataFrame(columns=['id'])
hasBrowse.id = browse_train.id
hasBrowse['hasBrowse']=1
hasBill = pd.DataFrame(columns=['id'])
hasBill.id = bill_train.id
hasBill['hasBill']=1

train = pd.merge(train, hasBank, how='left', on='id')
train = pd.merge(train, hasBrowse, how='left', on='id')
train = pd.merge(train, hasBill, how='left', on='id')
train = train.fillna(0)

hotCode = []
hasBank = train.hasBank.tolist()
hasBrowse = train.hasBrowse.tolist()
hasBill = train.hasBill.tolist()
for i in range(len(train)):
    b = str(int(hasBank[i])) + str(int(hasBrowse[i]))+ str(int(hasBill[i]))
    hotCode.append(int(b))
train['hotCode'] = hotCode

hasBank = pd.DataFrame(columns=['id'])
hasBank.id = bank_test.id
hasBank['hasBank']=1
hasBrowse = pd.DataFrame(columns=['id'])
hasBrowse.id = browse_test.id
hasBrowse['hasBrowse']=1
hasBill = pd.DataFrame(columns=['id'])
hasBill.id = bill_test.id
hasBill['hasBill']=1

test = pd.merge(test, hasBank, how='left', on='id')
test = pd.merge(test, hasBrowse, how='left', on='id')
test = pd.merge(test, hasBill, how='left', on='id')
test = test.fillna(0)

hotCode = []
hasBank = test.hasBank.tolist()
hasBrowse = test.hasBrowse.tolist()
hasBill = test.hasBill.tolist()
for i in range(len(test)):
    b = str(int(hasBank[i])) + str(int(hasBrowse[i]))+ str(int(hasBill[i]))
    hotCode.append(int(b))
test['hotCode'] = hotCode

#合并use_info特征
train = pd.merge(train, use_info_train, how='left', on='id')
test = pd.merge(test, use_info_test, how='left', on='id')

#合并loan_time特征
train = pd.merge(train, loan_time_train, how='left', on='id')
test = pd.merge(test, loan_time_test, how='left', on='id')

#合并bill 特征
train = pd.merge(train, bill_train, how='left', on='id')
test = pd.merge(test, bill_test, how='left', on='id')

#合并browse 特征
train = pd.merge(train, browse_train, how='left', on='id')
test = pd.merge(test, browse_test, how='left', on='id')

#合并bank特征
train = pd.merge(train, bank_train, how='left', on='id')
test = pd.merge(test, bank_test, how='left', on='id')

train.to_csv('D:/SLaughter_code/python_feature/train_all_feature.csv',index=None)
test.to_csv('D:/SLaughter_code/python_feature/test_all_feature.csv',index=None)

    













