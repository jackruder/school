#######
# author: Jack Ruder
# date: 2022-09-15
# Warm-up: outliers and influential points
# Apply log and box-cox transformations to model Wing length against leg length
######
library(MASS)
# load, attach and view the data
df <- read.csv("./snowPetrels.csv")
head(df)

# produce initial scatter plot
plot(df$wing ~ df$tarsus)

#construct initial model
iModel <- lm(df$wing ~ df$tarsus)
abline(iModel)
# we see a clear outlier in the plot, we should examine and probably remove it

# determine quartile range
q <- quantile(df$tarsus, probs=c(0.25,0.75), na.rm=TRUE)
iqr <- IQR(df$tarsus, na.rm=TRUE)

# remove outliers
rem <- subset(df, (tarsus > (q[1] - 1.5*iqr)) & (tarsus < (q[2] + 1.5*iqr)))

# plot the resulting data
x <- rem$tarsus
y <- rem$wing
plot(y~x)
#We observe a distinctive concave up behavior. Thus, we can try a log transform.

#Apply the transformation, and fit the model.
plot(log(y)~x)
logmodel <- lm(log(y) ~ x)
abline(logmodel)

## Evaluate the model
hist(logmodel$resid)
#The histogram looks good, and normal.

plot(logmodel$fit~logmodel$resid)
#This view of the residuals shows two clumps of data, 
#there is not a random distribution as we would want/expect

qqnorm(logmodel$resid)
qqline(logmodel$resid)
# the tails here very clearly diverge at the extremes. 
# The fit is not fantastic here, we can try a box-cox transformation

# Optimize lambda for box-cox
boxiecoxie <- boxcox(y~x)
l <- boxiecoxie$x[which.max(boxiecoxie$y)]

#apply the transformation

plot(((y^l - 1) / l) ~ x)
bcmodel <- lm(((y^l - 1) / l) ~ x)
abline(bcmodel)
hist(bcmodel$resid)
# residuals here look normal too
plot(bcmodel$fit~bcmodel$resid)
# this plot looks significantly better with a more even distribution and less clumping
qqnorm(bcmodel$resid)
qqline(bcmodel$resid)
# still the tails leave the line a bit with this transformation, but it looks overall better than the log transform.

