# Author: Jack Ruder
# Date: Sep 12, 2022
#
# Warmup Questions from Introduction to Simple Linear regression
#
#
####################


# 1.  load the dataset of virginica flowers
virginicaData <- droplevels(subset(iris, Species == "virginica"))

# 2  View the relationship betweel sepal length dependent on petal length.
attach(virginicaData)
plot(Sepal.Length, Petal.Length, xlab = "Sepal Length", ylab = "Petal Length", main = "Sepal vs Petal Length")

# 3. Data seems approximately linear, with a positive slope of about 1.
# The data has a uniform spread without too much width. There are no strong deviations, although there appears to be one outlier at about (6.25,6).

# 4. Given the linearity of the above plots, we fit a linear model and view the summary
irisModel <- lm(Sepal.Length ~ Petal.Length, data = virginicaData)
summary(irisModel)
# a. ==>  SepalLength = 1.060 + 0.996 * Petal.Length

#
# b. We examine the residuals to assess effectiveness
#
hist(irisModel$resid)
# The residuals here appear to follow a gamma distribution more than a normal distribution, so potentially the data is non-linear, although the residuals are not too
# far off from a normal distribution

plot(irisModel$fitted.values, irisModel$resid)
# Here, there is no clear pattern, although it is obvious that there are more data points around the median x values than the extremes.

qqnorm(irisModel$resid)
qqline(irisModel$resid)
# the Q-Q plots seem to be linear, along the expected line.

# Overall this model seems to be a very good fit. The residuals
# are a bit skewed, but the other plots seem to support the linearity of the data.
