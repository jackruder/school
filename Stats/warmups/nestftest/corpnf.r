################
# Author: Jack Ruder
# Date: Oct 06, 2022
# Warm-Up: Correlated Predictors and Nested F-tests
###############################
DATADIR <- "~/School/Stats/data"
FILENAME <- "sleep.txt"
SEP <- "/" # posix

DATAFILE <- paste(DATADIR, FILENAME, sep=SEP)
df <- read.table(DATAFILE,header=TRUE)
#attach(df)

plot.lm <- function(x, y, xlab, ylab) {
	par(mfrow=c(2,2))
	plot(x, y, xlab=xlab, ylab=ylab)
	model <- lm(y~x, na.action=na.omit)
	abline(model)
	summary(model)

	#diagnostic plots
	plot(model$fitted, model$resid, xlab="Fitted Values", ylab="Residuals")
	hist(model$resid, xlab="Residuals", main="Residual Distribution")
	# Slight left skew in the residuals
	qqnorm(model$resid)
	qqline(model$resid)
}

plotdensity  <- function(col, data, colName) {
	pname <-paste("Density Plot of",colName,sep=" ")
	lattice::densityplot(~col, data=data, main=pname, xlab=colName)
}

ihs <- function (z) { log (z + sqrt(1 + z^2))}

plotdensity(df$BodyWt, df, "Body Weight") # large range, but likely elephant weight is not an outlier. 
plotdensity(df$BrainWt, df, "Brain Weight") # This is 100x in scale, right skewed
plotdensity(df$NonDreaming, df, "Non Dreaming") # normal
plotdensity(df$Dreaming, df, "Dreaming") # some outliers but not too crazy, these will cary high leverage however, we will view this as a right skew, although we do have 0s
plotdensity(df$TotalSleep, df, "Total Sleep") # normal
plotdensity(df$LifeSpan, df, "Life Span") # again, right skew
plotdensity(df$Gestation, df, "Gestation") # right skew
plotdensity(df$Predation, df, "Predation") # normal
plotdensity(df$Exposure, df, "Exposure") # more bimodal than normal, but OK
plotdensity(df$Danger, df, "Danger") # normal

df$logBodyWt <- log(df$BodyWt)
df$logBrainWt <- log(df$BrainWt)
df$ihsDreaming <- ihs(df$Dreaming)
df$logLifeSpan <- log(df$LifeSpan)
df$logGestation <- log(df$Gestation)

plotdensity(df$logBodyWt, df, "Log Body Weight") # normal, but sharp peak
plotdensity(df$logBrainWt, df, "Log Brain Weight") # normal
plotdensity(df$logDreaming, df, "Log Dreaming") # normal
plotdensity(df$logLifeSpan, df, "Log Life Span") # normal
plotdensity(df$logGestation, df, "Log Gestation") # normal


# most of the data needed a log transformation, this is mostly expected though since some animals are extremely large vs extremely small.

plot.lm(df$logBodyWt, df$logLifeSpan, xlab="Log Body Weight", ylab="Log Lifespan") # linear, include
plot.lm(df$NonDreaming, df$logLifeSpan, xlab="Nondreaming sleep", ylab="Log Lifespan") # weakly linear
plot.lm(df$ihsDreaming, df$logLifeSpan, xlab="IHS Dreaming sleep", ylab="Log Lifespan") # weakly linear
plot.lm(df$TotalSleep, df$logLifeSpan, xlab="Total sleep", ylab="Log Lifespan") # weakly linear again
plot.lm(df$Gestation, df$logLifeSpan, xlab="Gestation", ylab="Log Lifespan") # concave down, 
plot.lm(sqrt(df$Gestation), df$logLifeSpan, xlab="Gestation", ylab="Log Lifespan") # concave down, 
plot.lm(log(df$Gestation), df$logLifeSpan, xlab="Gestation", ylab="Log Lifespan") # this looks good


# limited data, stick probably to no more than 3 terms

library(car)
ncol(df)
mod <- lm(logLifeSpan~logBodyWt + NonDreaming + ihsDreaming + TotalSleep + log(Gestation), data=df)
vif(mod)

m <- lm(logLifeSpan~logBodyWt + NonDreaming + log(Gestation), data=df)
summary(m)
m2 <- lm(logLifeSpan~logBodyWt + ihsDreaming + log(Gestation), data=df)
summary(m2)
m3 <- lm(logLifeSpan~logBodyWt + TotalSleep + log(Gestation), data=df) # total sleep appears best here
summary(m3)

m4 <- lm(logLifeSpan~logBodyWt + log(Gestation), data=df) # sleep had a tiny improvement, not worth including
summary(m4)

m5 <- lm(logLifeSpan~logBodyWt*log(Gestation), data=df) # interaction term is not significant
summary(m5)

# go the other way, see if this is a big difference
m6 <- lm(logLifeSpan~(logBodyWt + log(Gestation) + TotalSleep)^2, data=df)
summary(m6)
anova(m6)
# 4 more terms, only +10% variance explained. Bad choice.


# due to marginal gains above just including Gestation and body weight, we will stick with the simple 2-predictor model. We saw high VIF for most variables, so this should be acceptable.
model <- m4
vif(m4)

#2.
# The full model contains every term of the reduced model, so all of the error captured in the reduced model is captured in the full model. The full model has some additional terms that must capture some degree of error, meaning its sum of squares will be lower.

#b.It cannot be. The numerator, for the above reason, must have a negative sign. In the denominator, n-k is negative (k > n by definition of full/reduced), so the signs will cancel and give a positive F.

#c. F is close to zero when either the full and reduced models are nearly identical. In this case we accept the null hypothesis because we have seen that the exclusion of the additional terms makes a small difference in the error.


