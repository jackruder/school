#1. 
virginicaData <- droplevels(subset(iris,Species == "virginica"))
#2.
attach(virginicaData)
plot(Sepal.Length,Petal.Length,xlab="Sepal Length", ylab="Petal Length", main="Sepal vs Petal Length")
# #3. Data seems approximately linear, with a positive slope of about 1.
#The data has a uniform spread without too much width. There are no strong deviations, although there appears to be one outlier at about (6.25,6).

#4
irisModel <- lm(Sepal.Length~Petal.Length, data = virginicaData)
summary(irisModel)
# a.  SepalLength = 1.060 + 0.996 * Petal.Length

#b.
hist(irisModel$resid)
# The residuals here appear to follow a gamma distribution more than a normal distribution, so potentially the data is non-linear, although the residuals are not too 
# far off from a normal distribution
plot(irisModel$fitted.values, irisModel$resid)
#Here, there is no clear pattern, although it is obvious that there are more data points around the median x values than the extremes.
qqnorm(irisModel$resid)
qqline(irisModel$resid)
#the Q-Q plots seem to be linear, along the expected line. 
#Overall this model seems to be a very good fit. The residuals
#are a bit skewed, but the other plots seem to support the linearity of the data.

