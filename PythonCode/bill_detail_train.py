# -*- coding: utf-8 -*-
"""
Created on Mon Dec 19 11:06:11 2016

@author: zhanghuijfls
"""

import pandas as pd

'''
bill特征命名方式，x1-x10顺序命名了 x1:上期账单金额，x2:上期还款金额，x3:信用卡额度，
x4:本期账单余额，x5：本期账单最低还款额， x6：本期账单金额，x7：调整金额，
x8：循环利息，x9：可用余额，x10：预借现金额度
'''
print('--------------loan-data------------------')
#加入bill，loan_time数据
train = pd.read_table('D:/SLaughter_code/RawData/bill_detail_Train.txt',sep=',',header=-1)
train.columns = ['id','date','bank_id','x1','x2','x3','x4','x5','spend_num',
                 'x6','x7','x8','x9','x10','state']
train = train.drop_duplicates()
train['date'] =((train['date']-5800000000)/86400).astype(int)

loan_time_train = pd.read_csv('D:/SLaughter_code/RawData/loan_time_train.txt',sep=',',header=-1)
loan_time_train.columns = ['id','loan_time']
loan_time_train['loan_time'] = ((loan_time_train['loan_time']-5800000000)/86400).astype(int)

train = pd.merge(train, loan_time_train, how='left', on='id')

train['date_loan'] = train['loan_time']-train['date']

train_id = pd.DataFrame(columns=['id'])
train_id.id = train['id']

print('---------------build feature-----------------')
#id_size， 每个id记录个数
result = train.groupby('id').size().reset_index()
result.columns = ['id','id_size']

#bill_beforeloan_time，放款前最后的时间戳
def bill_before(df):
    df = df[df>0]
    if len(df)==0:
        return 0
    else:
        return min(df)
train_before_time = train.groupby('id')['date_loan'].apply(bill_before).reset_index()
train_before_time.columns=['id','bill_beforeloan_time']
result = pd.merge(result, train_before_time, on='id')

#duplicate_num，同一天重复的个数
dup = train.groupby('id').apply(lambda x:len(x)-len(x.drop_duplicates())).reset_index()
dup.columns = ['id','duplicate_num']
result = pd.merge(result, dup, on='id')

def date_mean(df):
    return (df.max()-df.min())/(len(df)-1)
#id_data_size，每个id不同时间的个数，   id_date_interval  每个id信用卡记录频率
id_date = train.groupby(['id','date']).size().reset_index()
id_date.columns = ['id','date','id_date']
id_date_size = id_date.groupby('id').size().reset_index()
id_date_size.columns = ['id','id_date_size']
id_date_interval = train.groupby('id')['date'].apply(date_mean).reset_index()
id_date_interval.columns = ['id','id_date_interval']
result = pd.merge(result, id_date_size, on='id')
result = pd.merge(result, id_date_interval, on='id')



#x2_x1_0, x2-x1小于0的个数，  x2_x1_1，  x2-x1大于等于0的个数，  
def divide(df):
    if df>=0:
        return 1
    else:
        return 0
train['x2-x1'] = train['x2']-train['x1']
train['x2-x1'] = train['x2-x1'].apply(divide)
x2_x1_dummy = pd.get_dummies(train['x2-x1'],prefix='x2_x1')
train_id = train_id.join(x2_x1_dummy)
x2_x1_sum = train_id.groupby('id')[['x2_x1_0','x2_x1_1']].sum().reset_index()
result = pd.merge(result, x2_x1_sum, on='id')

#id_spend
id_spend = train.groupby('id')['spend_num'].agg(['sum','max','mean']).reset_index()
id_spend.columns = ['id','id_spend_sum','id_spend_max','id_spend_mean']
result = pd.merge(result, id_spend, on='id')

#date_0_num， date为0的个数
def date_0_num(df):
    if len(df[df==0])>0:
        return df[df==0].count()
    else:
        return 0
train_date_0 = train.groupby('id')['date'].apply(date_0_num).reset_index()
train_date_0.columns = ['id','date_0_num']
result = pd.merge(result, train_date_0, on='id')

#bank_num  
def bank(df):
    return len(df.unique())
train_bank_num = train.groupby('id')['bank_id'].apply(bank).reset_index()
train_bank_num.columns = ['id','bank_num']
result = pd.merge(result, train_bank_num, on='id')

'''
将bank按照个数排序，取前14的bank，后面bank当做一类，进行one hot编码，计算个数
'''
def bank_divide(df):
    if df==1 or df==12 or df>20:
        return 100
    else:
        return df
train['bank_id'] = train['bank_id'].apply(bank_divide)
bank_dummy = pd.get_dummies(train['bank_id'],prefix='bank_id')
bank_data = pd.DataFrame(columns = ['id'])
bank_data.id = train.id
bank_data = bank_data.join(bank_dummy)
train_bank_id = bank_data.groupby('id').sum().reset_index()
result = pd.merge(result, train_bank_id, on='id')

#date_maxmin1,不包含时间为0的时间跨度， date_maxmin2，包含时间为0的时间跨度
def date_maxmin1(df):
    if len(df[df>0])>0:
        a = df[df>0].min()
    else:
        a=0
    return df.max()-a
train2 = train.groupby('id')['date'].apply(date_maxmin1).reset_index()
train2.columns = ['id','date_maxmin1']
train_date1 = train.groupby('id')['date'].apply(lambda x:x.max()-x.min()).reset_index()
train_date1.columns = ['id','date_maxmin2']
result = pd.merge(result, train_date1, on='id')
result = pd.merge(result, train2, on='id')

#date_loan1放款前个数, date_loan2 放款后个数, date_loan0放款当天个数
def f3(df):
    return len(df[df<0])
def f4(df):
    return len(df[df==0])
def f5(df):
    return len(df[df>0])
train_date_loan1 = train.groupby('id')['date_loan'].apply(f3).reset_index()
train_date_loan1.columns = ['id','date_loan_2']
train_date_loan2 = train.groupby('id')['date_loan'].apply(f4).reset_index()
train_date_loan2.columns = ['id','date_loan_0']
train_date_loan3 = train.groupby('id')['date_loan'].apply(f5).reset_index()
train_date_loan3.columns = ['id','date_loan_1']
result = pd.merge(result, train_date_loan1, how='left', on='id')
result = pd.merge(result, train_date_loan2, how='left', on='id')
result = pd.merge(result, train_date_loan3, how='left', on='id')

#将x1-x2当做一个特征，提取统计特征
train['x1_x2'] = train['x1']-train['x2']
train_x1x2 = train.groupby(['id','bank_id'])['x1_x2'].mean().reset_index()
train_x1x2.columns = ['id','bank_id','x1_x2_mean']
train_x1x2_max = train_x1x2.groupby('id')['x1_x2_mean'].agg(['max','min','mean','std']).reset_index()
train_x1x2_max.columns = ['id','x1_x2_max','x1_x2_min','x1_x2_mean','x1_x2_std'] 
result = pd.merge(result, train_x1x2_max, how='left', on='id')


print('-----------------1-----------------')
def divide2(df):
    if df<0:
        return 2
    elif df==0:
        return 0
    else:
        return 1

#x1_mean,x1_max
train_x1_mean = train.groupby('id')['x1'].mean().reset_index()
train_x1_mean.columns = ['id','x1_mean']
train_x1_max = train.groupby('id')['x1'].max().reset_index()
train_x1_max.columns = ['id','x1_max']
result = pd.merge(result, train_x1_mean, on='id')
result = pd.merge(result, train_x1_max, on='id')

#x2_mean,x2_max
train_x2_mean = train.groupby('id')['x2'].mean().reset_index()
train_x2_mean.columns = ['id','x2_mean']
train_x2_max = train.groupby('id')['x2'].max().reset_index()
train_x2_max.columns = ['id','x2_max']
result = pd.merge(result, train_x2_mean, on='id')
result = pd.merge(result, train_x2_max, on='id')

def dd1(df):
    return df.max()-df.min()
def dd2(df):
    a = df.unique().tolist()
    result = 0
    if len(a)>1:
        for i in range(len(a)-1):
            if a[i+1]>a[i]:
                result +=1
        return result
    else:
        return result
'''
drop_num特征表示额度上升的个数，将额度进行去重，若额度上升结果加1
'''
print('-----------------------x3----------------------')
#x3_mean,drop_num_sum,x3_maxmin,x3_maxmin_sum,x3_num, x3_0,x3_max
train_x3_mean = train.groupby('id')['x3'].mean().reset_index()
train_x3_mean.columns = ['id','x3_mean']
train_x3_max = train.groupby('id')['x3'].max().reset_index()
train_x3_max.columns = ['id','x3_max']
result = pd.merge(result, train_x3_mean, on='id')
result = pd.merge(result, train_x3_max, on='id')

train_x3_drop = train.groupby(['id','bank_id'])['x3'].apply(dd2).reset_index()
train_x3_drop.columns = ['id','bank_id','drop_num']
train_x3_drop1 = train_x3_drop.groupby('id')['drop_num'].sum().reset_index()
train_x3_drop1.columns = ['id','drop_num_sum']
result = pd.merge(result, train_x3_drop1, on='id')

train_x3 = train.groupby('id')['x3'].apply(dd1).reset_index()
train_x3.columns = ['id','x3_maxmin']
result = pd.merge(result, train_x3, on='id')

train_x3_minmax = train.groupby(['id','bank_id'])['x3'].apply(dd1).reset_index()
train_x3_minmax.columns = ['id','bank_id','x3_maxmin_1']
train_x3_minmax2 = train_x3_minmax.groupby('id')['x3_maxmin_1'].sum().reset_index()
train_x3_minmax2.columns = ['id','x3_maxmin_sum']
train_x3_minmax3 = train_x3_minmax.groupby('id')['x3_maxmin_1'].mean().reset_index()
train_x3_minmax3.columns = ['id','x3_maxmin_mean']
train_x3_minmax4 = train_x3_minmax.groupby('id')['x3_maxmin_1'].max().reset_index()
train_x3_minmax4.columns = ['id','x3_maxmin_max']
result = pd.merge(result, train_x3_minmax2, on='id')
result = pd.merge(result, train_x3_minmax3, on='id')
result = pd.merge(result, train_x3_minmax4, on='id')

def d1(df):
    return len(df[df==0])
x3_num = train.groupby('id')['x3'].unique().apply(len).reset_index()
x3_num.columns = ['id','x3_num']
x3_unique_mean = train.groupby('id')['x3'].apply(lambda x:x.unique().mean()).reset_index()
x3_unique_mean.columns = ['id','x3_unique_mean']
result = pd.merge(result, x3_num, on='id')
result = pd.merge(result, x3_unique_mean, on='id')

#取x3大于0，小于0，等于0的个数
train_x3 = pd.DataFrame(columns=['id','x3'])
train_x3.id = train['id']
train_x3.x3 = train['x3']
train_x3['x3'] = train_x3['x3'].apply(divide2)
x3_dummy = pd.get_dummies(train_x3['x3'],prefix='x3')
train_x3 = train_x3.join(x3_dummy)
train_x3 = train_x3.drop('x3',1)
x3 = train_x3.groupby('id').sum().reset_index()
result = pd.merge(result, x3, on='id')

#x4_mean,x4_max,x4_0,x4_2,x4_1,x4_min

#取x4大于0，小于0，等于0的个数
train_x4 = pd.DataFrame(columns=['id','x4'])
train_x4.id = train['id']
train_x4.x4 = train['x4']
train_x4['x4'] = train_x4['x4'].apply(divide2)
x4_dummy = pd.get_dummies(train_x4['x4'],prefix='x4')
train_x4 = train_x4.join(x4_dummy)
train_x4 = train_x4.drop('x4',1)
x4 = train_x4.groupby('id').sum().reset_index()
result = pd.merge(result, x4, on='id')

train_x4_mean = train.groupby('id')['x4'].mean().reset_index()
train_x4_mean.columns = ['id','x4_mean']
train_x4_max = train.groupby('id')['x4'].max().reset_index()
train_x4_max.columns = ['id','x4_max']
train_x4_min = train.groupby('id')['x4'].min().reset_index()
train_x4_min.columns = ['id','x4_min']
result = pd.merge(result, train_x4_mean, on='id')
result = pd.merge(result, train_x4_max, on='id')
result = pd.merge(result, train_x4_min, on='id')

#x5_mean,x5_max
train_x5_mean = train.groupby('id')['x5'].mean().reset_index()
train_x5_mean.columns = ['id','x5_mean']
train_x5_max = train.groupby('id')['x5'].max().reset_index()
train_x5_max.columns = ['id','x5_max']
result = pd.merge(result, train_x5_mean, on='id')
result = pd.merge(result, train_x5_max, on='id')


#x6_mean,x6_max
train_x6_mean = train.groupby('id')['x6'].agg(['mean','max']).reset_index()
train_x6_mean.columns = ['id','x6_mean','x6_max']
result = pd.merge(result, train_x6_mean, on='id')

#x7,x7_mean,x7_max

#x7大于0，等于0，小于0的个数
train_x7 = pd.DataFrame(columns=['id','x7'])
train_x7.id = train['id']
train_x7.x7 = train['x7']
train_x7['x7'] = train_x7['x7'].apply(divide2)
x7_dummy = pd.get_dummies(train_x7['x7'],prefix='x7')
train_x7 = train_x7.join(x7_dummy)
train_x7 = train_x7.drop('x7',1)
x7 = train_x7.groupby('id').sum().reset_index()
result = pd.merge(result, x7, on='id')

train_x7_mean = train.groupby('id')['x7'].agg(['mean','max']).reset_index()
train_x7_mean.columns = ['id','x7_mean','x7_max']
result = pd.merge(result, train_x7_mean, on='id')

#x8_mean, x8_max
#x8大于0，等于0，小于0的个数
train_x8 = pd.DataFrame(columns=['id','x8'])
train_x8.id = train['id']
train_x8.x8 = train['x8']
train_x8['x8'] = train_x8['x8'].apply(divide2)
x8_dummy = pd.get_dummies(train_x8['x8'],prefix='x8')
train_x8 = train_x8.join(x8_dummy)
train_x8 = train_x8.drop('x8',1)
x8 = train_x8.groupby('id').sum().reset_index()
result = pd.merge(result, x8, on='id')

train_x8_mean = train.groupby('id')['x8'].agg(['mean','max']).reset_index()
train_x8_mean.columns = ['id','x8_mean','x8_max']
result = pd.merge(result, train_x8_mean, on='id')


#x9_mean,x9_max
#x8大于0，等于0，小于0的个数
train_x9 = pd.DataFrame(columns=['id','x9'])
train_x9.id = train['id']
train_x9.x9 = train['x9']
train_x9['x9'] = train_x9['x9'].apply(divide2)
x9_dummy = pd.get_dummies(train_x9['x9'],prefix='x9')
train_x9 = train_x9.join(x9_dummy)
train_x9 = train_x9.drop('x9',1)
x9 = train_x9.groupby('id').sum().reset_index()
result = pd.merge(result, x9, on='id')

train_x9_mean = train.groupby('id')['x9'].agg(['mean','max']).reset_index()
train_x9_mean.columns = ['id','x9_mean','x9_max']
result = pd.merge(result, train_x9_mean, on='id')

#x10_mean, x10_max
train_x10_mean = train.groupby('id')['x10'].agg(['mean','max']).reset_index()
train_x10_mean.columns = ['id','x10_mean','x10_max']
result = pd.merge(result, train_x10_mean, on='id')

'''
将特征进行交叉，提取交叉特征，例如提取x1,x4之间相减后大于0，等于0，小于0的个数，
共提取了x1和x4, x3和x4, x5和x6, x4和x6, x1和x6
'''
####
train3 = train.groupby('id').size().reset_index()
train3.columns = ['id','size']
train3 = train3.drop('size',1)

def a1(df):
    if df>0:
        return 1
    elif df==0:
        return 0
    else:
        return 2

def tran(x1,x2,x1_x2,train3):
    train4 = pd.DataFrame(columns = ['id'])
    train4.id = train.id
    train[x1_x2] = train[x1]-train[x2]
    train[x1_x2] = train[x1_x2].apply(a1)
    dummy = pd.get_dummies(train[x1_x2],prefix=x1_x2)
    train4 = train4.join(dummy)
    train_x1_x2 = train4.groupby('id').sum().reset_index()
    train3 = pd.merge(train3, train_x1_x2, on='id')
    return train3

a=tran('x1','x4','x1_x4',train3)
b=tran('x3','x4','x3_x4',train3)
c=tran('x5','x6','x5_x6',train3)
d=tran('x4','x6','x4_x6',train3)
e=tran('x1','x6','x1_x6',train3)

result = pd.merge(result, a, on='id')
result = pd.merge(result, b, on='id')
result = pd.merge(result, c, on='id')
result = pd.merge(result, d, on='id')
result = pd.merge(result, e, on='id')

'''
提取交叉特征，命名中m代表大于0，e代表等于0，l代表小于0，
例如x3mx10e代表每个id  x3大于0同时x10等于0的个数，
共提取x3mx10e, x1mx2e, x1ex3m, x2ex3m, x8ex10m, x6mx8e, x8ex9e, x4mx6e
'''
    
aa = train.groupby('id').apply(lambda x:len(x[(x['x3']>0)&(x['x10']==0)])).reset_index()
aa.columns = ['id','x3mx10e']
bb = train.groupby('id').apply(lambda x:len(x[(x['x1']>0)&(x['x2']==0)])).reset_index()
bb.columns = ['id','x1mx2e']
cc = train.groupby('id').apply(lambda x:len(x[(x['x1']==0)&(x['x3']>0)])).reset_index()
cc.columns = ['id','x1ex3m']
dd = train.groupby('id').apply(lambda x:len(x[(x['x2']==0)&(x['x3']>0)])).reset_index()
dd.columns = ['id','x2ex3m']
ee = train.groupby('id').apply(lambda x:len(x[(x['x8']==0)&(x['x10']>0)])).reset_index()
ee.columns = ['id','x8ex10m']
ff = train.groupby('id').apply(lambda x:len(x[(x['x6']>0)&(x['x8']==0)])).reset_index()
ff.columns = ['id','x6mx8e']
gg = train.groupby('id').apply(lambda x:len(x[(x['x8']==0)&(x['x9']==0)])).reset_index()
gg.columns = ['id','x8ex9e']
hh = train.groupby('id').apply(lambda x:len(x[(x['x4']>0)&(x['x6']==0)])).reset_index()
hh.columns = ['id','x4mx6e']
result = pd.merge(result, aa, on='id')
result = pd.merge(result, bb, on='id')
result = pd.merge(result, cc, on='id')
result = pd.merge(result, dd, on='id')
result = pd.merge(result, ee, on='id')
result = pd.merge(result, ff, on='id')
result = pd.merge(result, gg, on='id')
result = pd.merge(result, hh, on='id')

#输出bill特征结果

result.to_csv('D:/SLaughter_code/python_feature/train_bill_dup.csv',index=None)




import matplotlib.pyplot as plt
import matplotlib

zhfont1 = matplotlib.font_manager.FontProperties(fname='C:\Windows\Fonts\simhei.ttf')


plt.title('信用卡放款后个数对比表',fontproperties=zhfont1)
plt.ylabel('放款后个数',fontproperties=zhfont1)
plt.style.use('fivethirtyeight')
plt.boxplot((train1.date_loan_2.tolist(),train2.date_loan_2.tolist()),labels=('target=1','target=0'))
            


plt.legend(prop=zhfont1)
































