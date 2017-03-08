# -*- coding: utf-8 -*-
"""
Created on Mon Dec 19 11:06:11 2016

@author: zhanghuijfls
"""

import pandas as pd

'''
同train
'''
test = pd.read_table('D:/SLaughter_code/RawData/bill_detail_test.txt',sep=',',header=-1)
test.columns = ['id','date','bank_id','x1','x2','x3','x4','x5','spend_num',
                 'x6','x7','x8','x9','x10','state']
test = test.drop_duplicates()
test['date'] =((test['date']-5800000000)/86400).astype(int)

loan_time_test = pd.read_csv('D:/SLaughter_code/RawData/loan_time_test.txt',sep=',',header=-1)
loan_time_test.columns = ['id','loan_time']
loan_time_test['loan_time'] = ((loan_time_test['loan_time']-5800000000)/86400).astype(int)

test = pd.merge(test, loan_time_test, how='left', on='id')

test['date_loan'] = test['loan_time']-test['date']

test_id = pd.DataFrame(columns=['id'])
test_id.id = test['id']

#id_size
result = test.groupby('id').size().reset_index()
result.columns = ['id','id_size']

#duplicate_num
dup = test.groupby('id').apply(lambda x:len(x)-len(x.drop_duplicates())).reset_index()
dup.columns = ['id','duplicate_num']
result = pd.merge(result, dup, on='id')


#id_data_size
id_date = test.groupby(['id','date']).size().reset_index()
id_date.columns = ['id','date','id_date']
id_date_size = id_date.groupby('id').size().reset_index()
id_date_size.columns = ['id','id_date_size']
result = pd.merge(result, id_date_size, on='id')

#x2_x1_0, x2_x1_1
def divide(df):
    if df>=0:
        return 1
    else:
        return 0
test['x2-x1'] = test['x2']-test['x1']
test['x2-x1'] = test['x2-x1'].apply(divide)
x2_x1_dummy = pd.get_dummies(test['x2-x1'],prefix='x2_x1')
test_id = test_id.join(x2_x1_dummy)
x2_x1_sum = test_id.groupby('id')[['x2_x1_0','x2_x1_1']].sum().reset_index()
result = pd.merge(result, x2_x1_sum, on='id')

#id_spend
id_spend = test.groupby('id')['spend_num'].sum().reset_index()
id_spend.columns = ['id','id_spend']
result = pd.merge(result, id_spend, on='id')

#date_0_num
def d(df):
    if len(df[df==0])>0:
        return df[df==0].count()
    else:
        return 0
test_date_0 = test.groupby('id')['date'].apply(d).reset_index()
test_date_0.columns = ['id','date_0_num']
result = pd.merge(result, test_date_0, on='id')

#bank_num
def bank(df):
    return len(df.unique())
test_bank_num = test.groupby('id')['bank_id'].apply(bank).reset_index()
test_bank_num.columns = ['id','bank_num']
result = pd.merge(result, test_bank_num, on='id')

#date_maxmin1,date_maxmin2
def f1(df):
    if len(df[df>0])>0:
        a = df[df>0].min()
    else:
        a=0
    return df.max()-a
test2 = test.groupby('id')['date'].apply(f1).reset_index()
test2.columns = ['id','date_maxmin1']
test_date1 = test.groupby('id')['date'].apply(lambda x:x.max()-x.min()).reset_index()
test_date1.columns = ['id','date_maxmin2']
result = pd.merge(result, test_date1, on='id')
result = pd.merge(result, test2, on='id')

#date_loan1, date_loan2, date_loan3
def f3(df):
    return len(df[df<0])
def f4(df):
    return len(df[df==0])
def f5(df):
    return len(df[df>0])
test_date_loan1 = test.groupby('id')['date_loan'].apply(f3).reset_index()
test_date_loan1.columns = ['id','date_loan_2']
test_date_loan2 = test.groupby('id')['date_loan'].apply(f4).reset_index()
test_date_loan2.columns = ['id','date_loan_0']
test_date_loan3 = test.groupby('id')['date_loan'].apply(f5).reset_index()
test_date_loan3.columns = ['id','date_loan_1']
result = pd.merge(result, test_date_loan1, how='left', on='id')
result = pd.merge(result, test_date_loan2, how='left', on='id')
result = pd.merge(result, test_date_loan3, how='left', on='id')

#x1_x2_max
test['x1_x2'] = test['x1']-test['x2']
test_x1x2 = test.groupby(['id','bank_id'])['x1_x2'].mean().reset_index()
test_x1x2.columns = ['id','bank_id','x1_x2_mean']
test_x1x2_max = test_x1x2.groupby('id')['x1_x2_mean'].agg(['max','min','mean','std']).reset_index()
test_x1x2_max.columns = ['id','x1_x2_max','x1_x2_min','x1_x2_mean','x1_x2_std'] 
result = pd.merge(result, test_x1x2_max, how='left', on='id')


print('-----------------1-----------------')
def divide2(df):
    if df<0:
        return 2
    elif df==0:
        return 0
    else:
        return 1

#x1_mean,x1_max
test_x1_mean = test.groupby('id')['x1'].mean().reset_index()
test_x1_mean.columns = ['id','x1_mean']
test_x1_max = test.groupby('id')['x1'].max().reset_index()
test_x1_max.columns = ['id','x1_max']
result = pd.merge(result, test_x1_mean, on='id')
result = pd.merge(result, test_x1_max, on='id')

#x2_mean,x2_max
test_x2_mean = test.groupby('id')['x2'].mean().reset_index()
test_x2_mean.columns = ['id','x2_mean']
test_x2_max = test.groupby('id')['x2'].max().reset_index()
test_x2_max.columns = ['id','x2_max']
result = pd.merge(result, test_x2_mean, on='id')
result = pd.merge(result, test_x2_max, on='id')

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
print('-----------------------x3----------------------')
#x3_mean,drop_num_sum,x3_maxmin,x3_maxmin_sum,x3_num, x3_0
test_x3_mean = test.groupby('id')['x3'].mean().reset_index()
test_x3_mean.columns = ['id','x3_mean']
result = pd.merge(result, test_x3_mean, on='id')

test_x3_drop = test.groupby(['id','bank_id'])['x3'].apply(dd2).reset_index()
test_x3_drop.columns = ['id','bank_id','drop_num']
test_x3_drop1 = test_x3_drop.groupby('id')['drop_num'].sum().reset_index()
test_x3_drop1.columns = ['id','drop_num_sum']
result = pd.merge(result, test_x3_drop1, on='id')

test_x3 = test.groupby('id')['x3'].apply(dd1).reset_index()
test_x3.columns = ['id','x3_maxmin']
result = pd.merge(result, test_x3, on='id')

test_x3_minmax = test.groupby(['id','bank_id'])['x3'].apply(dd1).reset_index()
test_x3_minmax.columns = ['id','bank_id','x3_maxmin_1']
test_x3_minmax2 = test_x3_minmax.groupby('id')['x3_maxmin_1'].sum().reset_index()
test_x3_minmax2.columns = ['id','x3_maxmin_sum']
result = pd.merge(result, test_x3_minmax2, on='id')

def d1(df):
    return len(df[df==0])
x3_num = test.groupby('id')['x3'].unique().apply(len).reset_index()
x3_num.columns = ['id','x3_num']
result = pd.merge(result, x3_num, on='id')

test_x3 = pd.DataFrame(columns=['id','x3'])
test_x3.id = test['id']
test_x3.x3 = test['x3']
test_x3['x3'] = test_x3['x3'].apply(divide2)
x3_dummy = pd.get_dummies(test_x3['x3'],prefix='x3')
test_x3 = test_x3.join(x3_dummy)
test_x3 = test_x3.drop('x3',1)
x3 = test_x3.groupby('id').sum().reset_index()
result = pd.merge(result, x3, on='id')

#x4_mean,x4_max,x4_0,x4_2
test_x4 = pd.DataFrame(columns=['id','x4'])
test_x4.id = test['id']
test_x4.x4 = test['x4']
test_x4['x4'] = test_x4['x4'].apply(divide2)
x4_dummy = pd.get_dummies(test_x4['x4'],prefix='x4')
test_x4 = test_x4.join(x4_dummy)
test_x4 = test_x4.drop('x4',1)
x4 = test_x4.groupby('id').sum().reset_index()
result = pd.merge(result, x4, on='id')

test_x4_mean = test.groupby('id')['x4'].mean().reset_index()
test_x4_mean.columns = ['id','x4_mean']
test_x4_max = test.groupby('id')['x4'].max().reset_index()
test_x4_max.columns = ['id','x4_max']
result = pd.merge(result, test_x4_mean, on='id')
result = pd.merge(result, test_x4_max, on='id')

#x5_mean,x5_max
test_x5_mean = test.groupby('id')['x5'].mean().reset_index()
test_x5_mean.columns = ['id','x5_mean']
test_x5_max = test.groupby('id')['x5'].max().reset_index()
test_x5_max.columns = ['id','x5_max']
result = pd.merge(result, test_x5_mean, on='id')
result = pd.merge(result, test_x5_max, on='id')

#x6_mean
test_x6_mean = test.groupby('id')['x6'].mean().reset_index()
test_x6_mean.columns = ['id','x6_mean']
result = pd.merge(result, test_x6_mean, on='id')

#x7
test_x7 = pd.DataFrame(columns=['id','x7'])
test_x7.id = test['id']
test_x7.x7 = test['x7']
test_x7['x7'] = test_x7['x7'].apply(divide2)
x7_dummy = pd.get_dummies(test_x7['x7'],prefix='x7')
test_x7 = test_x7.join(x7_dummy)
test_x7 = test_x7.drop('x7',1)
x7 = test_x7.groupby('id').sum().reset_index()
result = pd.merge(result, x7, on='id')

#x8
test_x8 = pd.DataFrame(columns=['id','x8'])
test_x8.id = test['id']
test_x8.x8 = test['x8']
test_x8['x8'] = test_x8['x8'].apply(divide2)
x8_dummy = pd.get_dummies(test_x8['x8'],prefix='x8')
test_x8 = test_x8.join(x8_dummy)
test_x8 = test_x8.drop('x8',1)
x8 = test_x8.groupby('id').sum().reset_index()
result = pd.merge(result, x8, on='id')

#x9
test_x9 = pd.DataFrame(columns=['id','x9'])
test_x9.id = test['id']
test_x9.x9 = test['x9']
test_x9['x9'] = test_x9['x9'].apply(divide2)
x9_dummy = pd.get_dummies(test_x9['x9'],prefix='x9')
test_x9 = test_x9.join(x9_dummy)
test_x9 = test_x9.drop('x9',1)
x9 = test_x9.groupby('id').sum().reset_index()
result = pd.merge(result, x9, on='id')

####
test3 = test.groupby('id').size().reset_index()
test3.columns = ['id','size']
test3 = test3.drop('size',1)

def a1(df):
    if df>0:
        return 1
    elif df==0:
        return 0
    else:
        return 2

def tran(x1,x2,x1_x2,test3):
    test4 = pd.DataFrame(columns = ['id'])
    test4.id = test.id
    test[x1_x2] = test[x1]-test[x2]
    test[x1_x2] = test[x1_x2].apply(a1)
    dummy = pd.get_dummies(test[x1_x2],prefix=x1_x2)
    test4 = test4.join(dummy)
    test_x1_x2 = test4.groupby('id').sum().reset_index()
    test3 = pd.merge(test3, test_x1_x2, on='id')
    return test3

a=tran('x1','x4','x1_x4',test3)
b=tran('x3','x4','x3_x4',test3)
c=tran('x5','x6','x5_x6',test3)

result = pd.merge(result, a, on='id')
result = pd.merge(result, b, on='id')
result = pd.merge(result, c, on='id')


aa = test.groupby('id').apply(lambda x:len(x[(x['x3']>0)&(x['x10']==0)])).reset_index()
aa.columns = ['id','x3mx10e']
bb = test.groupby('id').apply(lambda x:len(x[(x['x1']>0)&(x['x2']==0)])).reset_index()
bb.columns = ['id','x1mx2e']
cc = test.groupby('id').apply(lambda x:len(x[(x['x1']==0)&(x['x3']>0)])).reset_index()
cc.columns = ['id','x1ex3m']
dd = test.groupby('id').apply(lambda x:len(x[(x['x2']==0)&(x['x3']>0)])).reset_index()
dd.columns = ['id','x2ex3m']
ee = test.groupby('id').apply(lambda x:len(x[(x['x8']==0)&(x['x10']>0)])).reset_index()
ee.columns = ['id','x8ex10m']
ff = test.groupby('id').apply(lambda x:len(x[(x['x6']>0)&(x['x8']==0)])).reset_index()
ff.columns = ['id','x6mx8e']
gg = test.groupby('id').apply(lambda x:len(x[(x['x8']==0)&(x['x9']==0)])).reset_index()
gg.columns = ['id','x8ex9e']
result = pd.merge(result, aa, on='id')
result = pd.merge(result, bb, on='id')
result = pd.merge(result, cc, on='id')
result = pd.merge(result, dd, on='id')
result = pd.merge(result, ee, on='id')
result = pd.merge(result, ff, on='id')
result = pd.merge(result, gg, on='id')

test_result = pd.read_table('D:/SLaughter_code/RawData/usersID_test.txt',sep=',',header=-1)
test_result.columns = ['id']
test_result = pd.merge(test_result, result, how='left', on='id')
test_result = pd.merge(test_result, loan_time_test, how='left', on='id')


test_result.to_csv('D:/SLaughter_code/python_feature/test_bill_dup.csv',index=None)


















































