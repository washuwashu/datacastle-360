AucAddTwoClassSummary<-function(data, lev = NULL, model = NULL)
{
  lvls <- levels(data$obs)
  if (length(lvls) > 2) 
    stop(paste("Your outcome has", length(lvls), "levels. The twoClassSummary() function isn't appropriate."))
 # requireNamespaceQuietStop("ModelMetrics")
  library(ModelMetrics)
  if (!all(levels(data[, "pred"]) == lvls)) 
    stop("levels of observed and predicted data do not match")
  data$y = as.numeric(data$obs == lvls[2])
  rocAUC <- auc(ifelse(data$obs == lev[2], 0, 
                                     1), data[, lvls[1]])
  
  data <- data.frame(label =ifelse(data$obs == lev[2], 0, 
                                   1),prob = round(data[, lvls[1]],3))
  num0 <- 0
  num1 <- 0
  label0Count <- nrow(subset(data,label==0))
  label1Count <- nrow(subset(data,label==1))
  err <- 0
  for (i in sort(unique(data$prob)))
  {
    num0 <- num0 + nrow(subset(data,prob==i&label==0))
    num1 <- num1 + nrow(subset(data,prob==i&label==1))
    x_0 <- num0 / label0Count
    x_1 <- num1 / label1Count         
    err <- max(abs(x_0 - x_1),err)
  }
  
  out <- c(rocAUC,err)
  names(out) <- c("ROC","KS")
  out
  
}