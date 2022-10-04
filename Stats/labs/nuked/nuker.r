################
# Author: Jack Ruder
# Date: Sep 27, 2022
# Simple Linear Rgeression Pre-Lab
###############################

DATADIR <- "~/School/Stats/data"
FILENAME <- "hanford.txt"
SEP <- "/" # posix

DATAFILE <- paste(DATADIR, FILENAME, sep=SEP)
dat <- read.table(DATAFILE,header=TRUE)

library(ggplot2)
ggplot(dat, aes(x=Exposure, y=Mortality, label=County)) +
	geom_point(size=3) +
	geom_text(hjust=-0.1, vjust=-0.1)
# based on the plot, I would not assume the linear model to have a very strong fit. There are few points, and multiple points with similar y values for different x values.

#2. Clearly, there is a correlation of higher cancer risks with higher indices of exposure. This data would help show what levels of exposure are possible given an acceptable (if any) risk of cancer

#3.  
# I believe they should, their lives are affected negatively and unfairly, thus they should be compensated as medical issues/impacted health are irreversible in most cases. Again, we see that cancer has a positive correlation with exposure. With a t-test and R^2 value, the argument can be mad that this correlation is significant, which establishes a causation.


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


hist(dat$Exposure, breaks=6)
hist(dat$Mortality, breaks=6)
# netiher x nor y appear normally distributed, however there are only 9 datapoints. Based on the scatter plot, we might want transform one of these variables. Exposure seems like the more likely candidate for a transform given the more obvious right skew.

x0 = dat$Exposure
y0 = dat$Mortality
plot.lm(x0,y0,xlab="Exposure", ylab="Mortality")
m1 = lm(y0~x0)
# p value 0.000332, t=6.5, R^2 0.85,

x11()
x1 = log(dat$Exposure)
plot.lm(x1,y0,xlab="Log Exposure", ylab="Mortality")
m2 = lm(y0~x1)
# p value 0.000306, t=6.6, R^2 0.86 

x11()
y1 = log(dat$Mortality)
plot.lm(x1,y1,xlab="Log Exposure", ylab="Log Mortality")
m3 = lm(y1~x1)
# p value 0.000325, t=6.5, R^2 0.86

x11()
x2 = sqrt(dat$Exposure)
plot.lm(x2,y0,xlab="Sqrt Exposure", ylab="Mortality")
m4 = lm(x2~y0)
summary(m4)
# p value 0.0001697 t = 7.25, R^2 0.88, F 52.58

sqExp <- sqrt(dat$Exposure)
Mortality <- dat$Mortality
m = lm(Mortality~sqrt(Exposure), data=dat)
confint(m) # high 56.38, low 28.65

LOW <- 2
HIGH <- 8

low = subset(dat, Exposure <= 2)$Exposure
med = subset(dat, Exposure > 2 & Exposure < 8)$Exposure
high = subset(dat, Exposure >= 8)$Exposure
plow <- data.frame(predict(m, data.frame(Exposure=low), int="prediction"))
pmed <- data.frame(predict(m, data.frame(Exposure=med), int="prediction"))
phigh <- data.frame(predict(m, data.frame(Exposure=high), int="prediction"))
clow <- data.frame(predict(m, data.frame(Exposure=low), int="confidence"))
cmed <- data.frame(predict(m, data.frame(Exposure=med), int="confidence"))
chigh <- data.frame(predict(m, data.frame(Exposure=high), int="confidence"))
mean(plow$lwr)




