################################
# Author: Jack Ruder 
# Sep 22, 2022
# Warmup: Partitioning Variability
################################


data(iris)
setosa <- subset(iris, Species='setosa')
m1 <- lm(setosa$Petal.Length~setosa$Sepal.Length)
a <- anova(m1)
# sum sq errors is 352.87
# sum sq residuals is 111.46

#c
MSM <- (a$Sum)[1]
MSE <- (a$Sum)[2] / (length(setosa$Sepal.Length)-2)

#d.
fStat <- MSM/MSE
#468.5502, the same as given by anova()

#e. Previous warmup had p value of 0.06, the p value here is much smaller, because we
#account for variance
