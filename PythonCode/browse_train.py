# -*- coding: utf-8 -*-
"""
Created on Sat Jan 14 14:21:32 2017

@author: zhanghuijfls
"""

import pandas as pd

#载入browse数据
train = pd.read_csv('D:/SLaughter_code/RawData/browse_history_train.txt',sep=',',header=None)
train.columns = ['id','date','browse','browse_type']
train['date'] = (train['date']/86400).astype(int)


loan_time = pd.read_csv('D:/SLaughter_code/RawData/loan_time_train.txt',sep=',',header=None)
loan_time.columns=['id','loan_time']
loan_time['loan_time'] = (loan_time['loan_time']/86400).astype(int)

train = pd.merge(train, loan_time, how='left', on='id')
train['loan'] = train['date']-train['loan_time']

print('------------------id--------------------')

#id browse记录个数
train2 = train.groupby('id').size().reset_index()
train2.columns = ['id','browse_size']

print('-------------------------date----------------')
#date统计特征
#bdate_loan_beforenear 放款前最近的时间戳
#bdate_loan_afternear  放款后最近的时间戳

def cal_unique(df):
    return len(df.unique())
def date_loan_beforenear(df):
    if len(df[df<0])==0:
        return 0
    else:
        return abs(df[df<0].max())
def date_loan_afternear(df):
    if len(df[df>0])==0:
        return 0
    else:
        return df[df>0].min()

date_max = train.groupby('id')['date'].max().reset_index()
date_max.columns = ['id','bdate_max']
date_min = train.groupby('id')['date'].min().reset_index()
date_min.columns = ['id','bdate_min']
date_unique = train.groupby('id')['date'].apply(cal_unique).reset_index()
date_unique.columns=['id','bdate_unique']
train_date = train.groupby(['id','date']).size().reset_index()
train_date.columns = ['id','date','date_size']
train_date = train_date.groupby('id')['date_size'].agg(['max','mean','min']).reset_index()
train_date.columns = ['id','bdate_maxsize','bdate_meansize','bdate_minsize']

date_loan_beforenear = train.groupby('id')['loan'].apply(date_loan_beforenear).reset_index()
date_loan_beforenear.columns = ['id','bdate_loan_beforenear']
date_loan_afternear = train.groupby('id')['loan'].apply(date_loan_afternear).reset_index()
date_loan_afternear.columns = ['id','bdate_loan_afternear']
       
train2 = pd.merge(train2, date_max, on='id')
train2 = pd.merge(train2, date_min, on='id')
train2 = pd.merge(train2, date_unique, on='id')
train2 = pd.merge(train2, train_date, on='id')
train2 = pd.merge(train2, date_loan_beforenear, on='id')
train2 = pd.merge(train2, date_loan_afternear, on='id')

train2['bdate_minmax'] = train2['bdate_max']-train2['bdate_min']
train2['bdate_max_loan'] = train2['bdate_max']-loan_time['loan_time']

#date_loan1, date_loan2, date_loan0， 放款前，放款后，放款当天个数
def f3(df):
    return len(df[df<0])
def f4(df):
    return len(df[df==0])
def f5(df):
    return len(df[df>0])
train_date_loan1 = train.groupby('id')['loan'].apply(f3).reset_index()
train_date_loan1.columns = ['id','bdate_loan_2']
train_date_loan2 = train.groupby('id')['loan'].apply(f4).reset_index()
train_date_loan2.columns = ['id','bdate_loan_0']
train_date_loan3 = train.groupby('id')['loan'].apply(f5).reset_index()
train_date_loan3.columns = ['id','bdate_loan_1']
train2 = pd.merge(train2, train_date_loan1, how='left', on='id')
train2 = pd.merge(train2, train_date_loan2, how='left', on='id')
train2 = pd.merge(train2, train_date_loan3, how='left', on='id')


#放款前一个月的记录个数
def d1(df):
    a = df[(df.date>=df.loan2)&(df.date<=df.loan_time)]
    if len(a)>0:
        return len(a)
    else:
        return 0
        
train['loan2'] = train['loan_time']-31
train_before = train.groupby('id').apply(d1).reset_index()
train_before.columns = ['id','b_31_num']
train2 = pd.merge(train2, train_before, how='left', on='id')


print('---------------------------type------------------------')
'''
将browse_type one hot 编码后计算个数和频率
'''
def type(a):
    d = train[train.browse_type==a]
    train_type = d.groupby('id').size().reset_index()
    train_type.columns = ['id','type_'+str(a)+'_size']
    return train_type

for i in train['browse_type'].unique():
    a = type(i)
    train2 = pd.merge(train2, a, how='left',on='id')
    train2['type_'+str(i)+'_freq'] = train2['type_'+str(i)+'_size']/train2['browse_size']
train2 = train2.fillna(0)


print('---------------------------browse feature-------------------')
#browse 统计特征
browse_num = train.groupby('id')['browse'].unique().apply(lambda x:len(x)).reset_index()
browse_num.columns = ['id','browse_num']
browse_size = train.groupby(['id','browse']).size().reset_index()
browse_size.columns = ['id','browse','browse_size']
browse_maxsize = browse_size.groupby('id')['browse_size'].agg(['max','min','mean']).reset_index()
browse_maxsize.columns = ['id','browse_maxsize','browse_minsize','browse_meansize']


train2 = pd.merge(train2, browse_num, on='id')
train2 = pd.merge(train2, browse_maxsize, on='id')


print('---------------------browse top 15----------------------')
'''
取browse个数排名前15的类别，将其余归为一类，计算每个id的个数和频率
'''
def browse_15(df):
    if df not in [118,173,45,38,50,139,164,82,120,190,101,110,189,44,167]:
        return 999
    else:
        return df
train['browse'] = train['browse'].apply(browse_15)

def browse(a):
    d = train[train.browse==a]
    data = d.groupby('id').size().reset_index()
    data.columns = ['id','browse_'+str(a)+'_size']
    return data

for i in [118,173,45,38,50,139,164,82,120,190,101,110,189,44,167,999]:
    b = browse(i)
    train2 = pd.merge(train2, b, how='left', on='id')
    train2['browse_'+str(i)+'_freq'] = train2['browse_'+str(i)+'_size']/train2['browse_size']

train2 = train2.fillna(0)

print('--------------------------top15------------------------')
'''
取浏览行为数据对应浏览行为编号个数排名前15的类别，计算top15的总个数，总频率
'''
train2['top_15_freq'] = train2['browse_118_freq']
train2['top_15_num'] = train2['browse_118_size']
for a in [173,45,38,50,139,164,82,120,190,101,110,189,44,167]:
    train2['top_15_freq'] +=train2['browse_'+str(a)+'_freq']
    train2['top_15_num'] +=train2['browse_'+str(a)+'_size']


train2.to_csv('D:/SLaughter_code/python_feature/browse_feature_train.csv',index=None)

   
        

        
        