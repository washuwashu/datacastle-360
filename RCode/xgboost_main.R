

setwd("D:\\SLaughter_code\\Rcode")
Btrain<-read.csv("../python_feature/train_all_feature.csv")
Btrain<-cbind(Btrain[,-2],Btrain[,2])
colnames(Btrain)[c(1,ncol(Btrain))]<-c("id","label")
Btest<-read.csv("../python_feature/test_all_feature.csv")

 

source("AucAddTwoClassSummary.R")
source("trainXgbModel.R")
library(xgboost)
library(caret)


for(i in 2:ncol(Btest))
{
  len<-nrow(Btest)
  min_test<-min(Btest[,i],na.rm = T)
  Max_test<-max(Btest[,i],na.rm = T)
  Btest[,i][is.na(Btest[,i])]<-min_test-(Max_test-min_test)/len;
  Btrain[,i][is.na(Btrain[,i])]<-min_test-(Max_test-min_test)/len;
}


nrounds<-1600     
eta<-0.04       
gamma<-0.12
max_depth<-5         
subsample<-0.7
colsample_bytree<-0.8
min_child_weight<-2.5

models<-trainXgbModel(Btrain,nrounds,eta,gamma,max_depth,subsample,colsample_bytree,min_child_weight)

y_pred<-predict(models,Btest[,-1],type = "prob")$p
result<-cbind(Btest[,1],y_pred)
colnames(result)<-c("userid","probability")

write.csv(result,"../result/xgboostResult.csv",row.names = F)



