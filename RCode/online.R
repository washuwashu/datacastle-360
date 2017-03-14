library(xgboost)
setwd("D:\\SLaughter_code\\Rcode")
df_train<-read.csv("../python_feature/train_all_feature.csv")
df_train<-cbind(df_train[,-2],df_train[,2])
colnames(df_train)[c(1,ncol(df_train))]<-c("id","target")
df_test<-read.csv("../python_feature/test_all_feature.csv")

#���ȱʧֵ
for(i in 2:ncol(df_test))
{
  len<-nrow(df_test)
  min_test<-min(df_test[,i],na.rm = T)
  Max_test<-max(df_test[,i],na.rm = T)
  df_test[,i][is.na(df_test[,i])]<-min_test-(Max_test-min_test)/len;
  df_train[,i][is.na(df_train[,i])]<-min_test-(Max_test-min_test)/len;
}


labels_train <- df_train['target']
sample_weigtht <- (length(labels_train$target)-sum(labels_train))/sum(labels_train) # 样本权重
df_train <- df_train[-grep('target', colnames(df_train))]


dtrain <- xgb.DMatrix(data.matrix(df_train[,-1]),missing = NA, label = labels_train$target)

watch_list <- list(train = dtrain)


nround <- 1600
param <- list(
  objective = 'binary:logistic',
  eta = 0.04,
  gamma = 0.12,
  lambda = 200,
  scale_pos_weight  = sample_weigtht,
  max_depth = 5, 
  subsample = 0.7,
  colsample_bytree = 0.8,
  seed = 100,
  eval_metric = 'auc',
  nthread = 8,
  min_child_weight = 2.5,
  missing = NA
)
bst <- xgb.train(params = param,dtrain,nrounds = nround,watchlist = watch_list)

y_pred <- predict(bst,data.matrix(df_test[,-1]),missing = NA)

write.csv(y_pred,"../result/onlinePredict4.csv")
