#trainXgbModel

trainXgbModel<-function(df_train,nrounds,eta,gamma,max_depth,subsample,colsample_bytree,min_child_weight)
{
  #df_train 的第一列为uid,最后一列为label
library(caret)
trainData<-df_train
#计算权重
weight_n<-nrow(trainData[trainData$label==1,])/nrow(trainData[trainData$label==0,])
weight_p<-1-weight_n
weight<-trainData$label*(weight_p-weight_n)+weight_n

trainData$label<-factor(trainData$label,levels = c(0,1),labels = c("n","p"))
fitControl <- trainControl(method = "repeatedcv",
                           number = 5,
                           repeats = 1,
                           ## Estimate class probabilities
                           classProbs = TRUE,
                           ## Evaluate performance using 
                           ## the following function
                           summaryFunction = AucAddTwoClassSummary)
tunGrid<-expand.grid(   nrounds=nrounds,#3:4
                        eta =eta,# 0.05,#0.04, #0.05,
                        gamma =gamma,# 0.1,
                        max_depth =max_depth,# 4, 
                        subsample =subsample,# 0.7,
                        colsample_bytree =colsample_bytree,# 0.8,
                        min_child_weight =min_child_weight # 2.5
                      
)
#param<-list(
  #   objective = 'binary:logistic',
   #  scale_pos_weight  = sample_weigtht,nthread = 8, lambda = 200
  #)
xgbModel<-train(label~.,data =trainData[,-1],method="xgbTree", 
                objective = 'binary:logistic',
                trControl=fitControl,
                tuneGrid=tunGrid,
                #scale_pos_weight  = sample_weigtht,
                scale_pos_weight  =weight,
                nthread = 8,
                lambda = 200,
                eval_metric="auc",
                #early_stopping_rounds=100,
               # na.action=na.pass,
               # param
               # print.every.n = 1,
                missing = NA,
                metric="ROC"
                )
return(xgbModel)
}



  