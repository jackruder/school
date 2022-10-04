##########################
# Author: Jack Ruder
# Date: September 18, 2022 
# Polar Pre Lab
###########################
library(MASS)
# Data location constants
DATA_DIR <- "~/School/Stats/data" # can change to . for local
#DATA_DIR <- "." # can change to . for local
iceCorefile <- "ice_core.csv"
isoTfile <- "isotope_and_T.csv"
fsSep <- "/" # posix

#load data
iceCore <- read.csv(paste(DATA_DIR,iceCorefile, sep=fsSep))
isoT <- read.csv(paste(DATA_DIR, isoTfile, sep=fsSep))



#Question 1.
plot(isoT$delta18O, isoT$temperature)
# The plot exhibits some approximately linear behavior (positive relationship), although there is a potential slight bend upwards and an outlier.


# There is a clear outlier, although it is hard to determine that it is a bad datapoint since it is within the range of data. 

subset(isoT, X==82) # the outlier, at y=-5.4 degrees.
# in the temperature_corrected column, there is an entry of -55.4,rEV5TX5kRSjA﻿
# it appears that the user forgot to type a 5.

# We confirm this,
subset(isoT, X > 78 & X < 86)$temperature
# we see that indeed -55.4 is in line with all of the surrounding X values, so this corrected mistake appears to be appropriate

#for readibility, set temp column to temperature_corrected
isoT$temperature <- isoT$temperature_corrected


### Diagnostic plotting function #########
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



######### fit data as is############
x <- isoT$delta18O
y <- isoT$temperature
slr <- lm(temperature~delta18O, data=isoT)
# scatter plot with fitted line
plot.lm(x,y,"delta18O", "temperature") 
x11() #keep plot open

#diagnostic plots

# We see that the data here is centered around 0, however there appears to be 
# a pattern in the residuals for lower values, with increased spread

# Slight left skew in the residual histogram

#QQ plot here has long tails on the edges. This data should need a transformation

par(mfrow=c(1,2))
hist(y, breaks=20, main="Histogram of Temperature Values", xlab="Temperature")
hist(x, breaks=20, main="Histogram of delta18O Values", xlab="delta18O")
# it seems that there are higher frequencies of datapoints at the extremes, we can try some transformations, possibly a square root transform

hist(sqrt(-y), breaks=20) # we see extremes here still at the edge, but this looks mostly uniform.
hist(log(-y), breaks=20) # a log transform looks worse.
hist(1/y, breaks=20) # now we have consistent concave up, maybe now a transform?
hist(log(-1/y), breaks=20) # this does not seem any better than sqrt(-y), and given the added complexity of the transformation it seems not worth it.



hist(sqrt(-x), breaks=20) # this data is still bimodal.
hist(log(-x), breaks=20) # a log transform looks worse, again.
hist(1/x, breaks=20) # now we have consistent concave up, maybe now a transform?
hist(log(-1/x), breaks=20) # this seems worse again.
# it seems no transformation of x is fully satisfactory. We will try square roots since the best transformations came from those

######### try square root transforms data as is############
plot.lm(-sqrt(-x), -sqrt(-y), xlab="sqrt(-delta18O)", ylab="sqrt(-ylab)")

#diagnostic plots
# We see that the data here is centered around 0, however there appears to be 
# a pattern in the residuals for lower values, with increased spread. The residuals look less normal and more skewed.
############### not too helpful. clearly, this plot is no better ###############




##### The bimodal nature of the data is responsible for the difficulty in transformation. Last idea to obtain a normal distribution is boxcox, although it will obfuscate meaning.####

px <- -x
py <- -y
# box cox transform
bc <- boxcox(py ~ px)
lambda <- (bc$x)[which.max(bc$y)]
#fit new linear regression model using the Box-Cox transformation
ty <- ((py^lambda-1)/lambda)
plot(ty ~ px)
bcmodel <- lm(ty ~ px)
abline(bcmodel)

#evaluate fit
plot(bcmodel$fitted, bcmodel$resid)
# Here the data is scattered much better in the plot, although there is some clumping around the fitted values of 100.
hist(bcmodel$resid)
# 
# Slight left skew in the residuals
qqnorm(bcmodel$resid)
qqline(bcmodel$resid)
# This is the best fit, but we have a loss of understanding here.

iceCore$predictedTemp <- predict(slr, data.frame(delta18O=iceCore$d18O))
plot(iceCore$Age_decimal, iceCore$predictedTemp, xlab="Age", ylab="Predicted Temperature")

