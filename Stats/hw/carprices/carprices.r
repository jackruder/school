###########################################
# Author: jack Ruder
# Stats 260 HW car prices
#
#
##########################################


DATADIR = "."
FNAME = "mazdas.txt"
SEP = "/"
FILE = paste(DATADIR,FNAME,sep=SEP)

library(ggplot2)
plot.lm <- function(x, y, xlab, ylab) {
	par(mfrow=c(2,2))
	plot(x, y, xlab=xlab, ylab=ylab)
	model <- lm(y~x)
	abline(model)
	summary(model)

	#diagnostic plots
	plot(model$fitted, model$resid, xlab="Fitted Values", ylab="Residuals")
	hist(model$resid, xlab="Residuals")
	# Slight left skew in the residuals
	qqnorm(model$resid)
	qqline(model$resid)
}


# load data and add age column
df <- read.table(FILE, header=TRUE)
df$Age = 91 - df$Year
df$PriceL = log(df$Price)

#plot data 
png("PvA.png")
am <- lm(df$Price~df$Age)
plot(df$Price~df$Age)
abline(am)
dev.off()
summary(am)
# data is nonlinear as expected


# by default, include linear term. 
# Likely useful to indicate increasing base prices over time
# log(PRICE) = B_0 + B_1 * AGE
m1 <- lm(PriceL~Age, data=df)
summary(m1)
png("logPvA.png")
plot.lm(df$Age, log(df$Price), xlab="Age", ylab="Log Price")
dev.off()

confint(m1)
#upper bound is -0.14, lower -0.179


# looking pretty normal in the residuals, the qqplot tails are a bit off, though there are only a total of 4 datapoints. All in all, this is very good.


#R^2 of 0.7941. This is a pretty good fit, considering how many datapoints have multiple Y values for the same x value.

#f. A t-test yields a p-value of <2e-16, nearly zero. This means Age is an extremely good predictor for 

#i.
sapply(data.frame(), exp)
sapply(data.frame(predict(m1, data.frame(Age=c(91-85)), int="prediction")), exp)
predict(m1, data.frame(Age=c(91-85)), int="confidence")
