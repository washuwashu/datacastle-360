# -*- coding: utf-8 -*-
"""
Created on Sat Feb  4 22:13:55 2017

@author: zhanghuijfls
"""

import lightgbm as lgb
import pandas as pd
from sklearn.model_selection import StratifiedKFold
import numpy as np
from sklearn.metrics import roc_curve,roc_auc_score

def ks_score(true, preds):
    fpr, tpr, thre=roc_curve(true, preds, pos_label=1)
    return abs(fpr-tpr).max()

def ks_lgb(preds, train_data):
    true = train_data.get_label()
    fpr, tpr, thre=roc_curve(true, preds, pos_label=1)
    return 'ks', abs(fpr-tpr).max(), True
    
train = pd.read_csv('D:/SLaughter_code/python_feature/train.csv')
train = train.fillna(-999)

test = pd.read_csv('D:/SLaughter_code/python_feature/test.csv')
test = test.fillna(-999)

test_id = test.id
test_x = test.drop('id',1)

train_x = train.drop(['id','target'],1)
train_y = train['target']

train_x = np.array(train_x)
train_y = np.array(train_y)
kf = StratifiedKFold(n_splits=5,shuffle = True, random_state=0)

result = pd.DataFrame(columns=['1','2','3','4','5'])
i = str(1)
score = 0
score_list = []
auc_score = 0
feature_score = pd.DataFrame(columns=['feature','score'])
feature_score.feature = train.columns[2:]
feature_score.score = 0
for x_index, y_index in kf.split(train_x, train_y):
    x_train, x_val = train_x[x_index], train_x[y_index]
    y_train, y_val = train_y[x_index], train_y[y_index]
    lgb_train = lgb.Dataset(x_train, y_train)
    lgb_eval = lgb.Dataset(x_val, y_val, reference=lgb_train)
    params = {
    'task': 'train',
    'boosting_type': 'gbdt',
    'objective': 'binary',
#    'metric': 'auc',
    'num_leaves': 16,
    'learning_rate': 0.1,
    'feature_fraction': 0.8,
    'bagging_fraction': 0.8,
    'bagging_freq': 2,
    'bagging_seed':10,
    'lambda_l1':20,
    'lambda_l2':150,
    'verbose': 0,
    'is_unbalance':True
    }
    model = lgb.train(params,lgb_train,num_boost_round=1500,valid_sets=[lgb_train,lgb_eval],
                    feval=ks_lgb,verbose_eval=100,early_stopping_rounds=300)
    pred_eval = model.predict(x_val,num_iteration=model.best_iteration)
    a = ks_score(y_val, pred_eval)
    auc = roc_auc_score(y_val, pred_eval)
    print(a, auc)
    score +=a
    auc_score +=auc
    score_list.append(a)
    pred = model.predict(test_x,num_iteration=model.best_iteration)
    result[i] = pred
    i = str(int(i)+1) 
result['end1'] = (result['1']+result['2']+result['3']+result['4']+result['5'])/5
result['end2'] = (4*result['1']+result['2']+5*result['3']+2*result['4']+3*result['5'])/15
print(score_list)
print(score/5, auc_score/5)

result1 = pd.DataFrame(columns = ['userid','probability'])
result1.userid = test_id
result1.probability = result['end1']
result1.to_csv('D:/SLaughter_code/result/result_0.458.csv',index=None)

result1 = pd.DataFrame(columns = ['userid','probability'])
result1.userid = test_id
result1.probability = result['end2']
result1.to_csv('D:/SLaughter_code/result/result_0.4585.csv',index=None)      
   
    
    
    
    
    
    
    
    
    
    
    
    
