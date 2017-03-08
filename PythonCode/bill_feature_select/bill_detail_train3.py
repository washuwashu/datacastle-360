# -*- coding: utf-8 -*-
"""
Created on Sun Dec 11 15:48:09 2016

@author: zhanghuijfls
"""

import pandas as pd
import xgboost as xgb
import random


train = pd.read_csv('D:/SLaughter_code/PythonCode/bill_feature_select/bill_feature_dup.csv')

train = train.fillna(-1)
train_x = train.drop(['id','target'],1)
train_y = train['target']

dtrain = xgb.DMatrix(train_x, label=train_y)


score = []
def pipeline(iteration,random_seed,gamma,max_depth,lambd,subsample,colsample_bytree,min_child_weight):
    params={
    	'booster':'gbtree',
    	'objective': 'binary:logistic',
    	'scale_pos_weight': float(len(train_y)-sum(train_y))/float(sum(train_y)),
        'eval_metric': 'auc',
    	'gamma':gamma,
    	'max_depth':max_depth,
    	'lambda':lambd,
        'subsample':subsample,
        'colsample_bytree':colsample_bytree,
        'min_child_weight':min_child_weight, 
        'eta': 0.4,
    	'seed':random_seed
        }
    if max_depth==4:
        nround = 800
    elif max_depth==5:
        nround = 700
    else:
        nround = 600
    watchlist  = [(dtrain,'train')]
    model = xgb.train(params,dtrain,num_boost_round=nround,evals=watchlist)
        
    feature_score = model.get_score(importance_type='gain')
    feature_score = sorted(feature_score.items(), key=lambda x:x[1],reverse=True)
    fs = []
    for (key,value) in feature_score:
        fs.append("{0},{1}\n".format(key,value))
    
    with open('D:/SLaughter_code/PythonCode/bill_feature_select/result_score/feature_score_{0}.csv'.format(iteration),'w') as f:
        f.writelines("feature,score\n")
        f.writelines(fs)
    
if __name__ == "__main__":
    random_seed = list(range(1000,2000,10))
    gamma = [i/1000.0 for i in range(100,200,1)]
    max_depth = [4,5,6]
    lambd = list(range(200,400,2))
    subsample = [i/1000.0 for i in range(600,700,2)]
    colsample_bytree = [i/1000.0 for i in range(450,650,2)]
    min_child_weight = [i/1000.0 for i in range(200,300,2)]
    random.shuffle(random_seed)
    random.shuffle(gamma)
    random.shuffle(max_depth)
    random.shuffle(lambd)
    random.shuffle(subsample)
    random.shuffle(colsample_bytree)
    random.shuffle(min_child_weight)
    
for i in range(30):
        pipeline(i,random_seed[i],gamma[i],max_depth[i%3],lambd[i],
                 subsample[i],colsample_bytree[i],min_child_weight[i])  
    

'''计算分数代码'''
#files = os.listdir('result_score')
#
#fs = {}
#for f in files:
#    t = pd.read_csv('result_score/'+f)
#    feature = t.feature
#    t.index = t.feature
#    t = t.drop(['feature'],axis=1)
#    d = t.to_dict()['score']
#    for key in feature:
#        if key in fs:
#            fs[key] += d[key]
#        else:
#            fs[key] = d[key] 
#            
#fs = sorted(fs.items(), key=lambda x:x[1],reverse=True)
#
#t = []
#for (key,value) in fs:
#    t.append("{0},{1}\n".format(key,value))
#
#with open('D:\SLaughter_code\PythonCode\bill_feature_select/bill_feature_score_gain.csv','w') as f:
#    f.writelines("feature,score\n")
#    f.writelines(t)
