################
# Author: Jack Ruder
# Date: Oct 06, 2022
# Multiple Linear Regression Lab: Punting
###############################

DATADIR <- "~/School/Stats/data"
FILENAME <- "punting.txt"
SEP <- "/" # posix

DATAFILE <- paste(DATADIR, FILENAME, sep=SEP)
df <- read.table(DATAFILE,header=TRUE)
#attach(df)

plot.lm <- function(x, y, xlab, ylab) {
	par(mfrow=c(2,2))
	plot(x, y, xlab=xlab, ylab=ylab)
	model <- lm(y~x)
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

#RES_X <- 1080
#RES_Y <- 1080
#
#colname <- "Left Leg Strength"
#x <- L_Strength
#fname <- gsub(" ", "_", colname)
#png(paste(fname,"png",sep="."), width=RES_X, height=RES_Y)
#plotdensity(x, df, colname)
#dev.off()
#
#colname <- "Right Leg Strength"
#x <- R_Strength
#fname <- gsub(" ", "_", colname)
#png(paste(fname,"png",sep="."), width=RES_X, height=RES_Y)
#plotdensity(x, df, colname)
#dev.off()
#
#colname <- "Overall Leg Strength"
#x <- O_Strength
#fname <- gsub(" ", "_", colname)
#png(paste(fname,"png",sep="."), width=RES_X, height=RES_Y)
#plotdensity(x, df, colname)
#dev.off()
#
#colname <- "Left Leg Flexibility"
#x <- L_Flexibility
#fname <- gsub(" ", "_", colname)
#png(paste(fname,"png",sep="."), width=RES_X, height=RES_Y)
#plotdensity(x, df, colname)
#dev.off()
#
#colname <- "Right Leg Flexibility"
#x <- R_Flexibility
#fname <- gsub(" ", "_", colname)
#png(paste(fname,"png",sep="."), width=RES_X, height=RES_Y)
#plotdensity(x, df, colname)
#dev.off()
#
#colname <- "Distance"
#x <- Distance
#fname <- gsub(" ", "_", colname)
#png(paste(fname,"png",sep="."), width=RES_X, height=RES_Y)
#plotdensity(x, df, colname)
#dev.off()
#
#colname <- "Hang Time"
#x <- Hang
#fname <- gsub(" ", "_", colname)
#png(paste(fname,"png",sep="."), width=RES_X, height=RES_Y)
#plotdensity(x, df, colname)
#dev.off()

cor(df) # L_Flexibility we can leave out off the bat. Both O_Strength (0.74) and L_Strength (0.79) correlate 
#with distance, but not much with eachother (0.52). R_Strength and L_Strength are highly correlated (0.89), we should pick just one to include.

#observe that all of the variables appear to be fairly normally distributed, no obvious tronsformations are necessary
m1 <- lm(Distance~L_Strength + R_Strength + O_Strength + L_Flexibility, data=df)
m2 <- lm(Distance~L_Strength + R_Strength + O_Strength + R_Flexibility, data=df)
m <- lm(Distance~L_Strength + R_Strength + O_Strength, data=df)
summary(m)
anova(m)
anova(m,m1) # we see that marginal gains are made including L_Flexibility
anova(m,m2) # better, but still too small of a gain to include without overfitting data

m3 <- lm(Distance~L_Strength + O_Strength, data=df)
summary(m3)
anova(m3)

#is R_Strength better?
m4 <- lm(Distance~R_Strength + O_Strength, data=df)
summary(m4)
anova(m4) # actually, yes.
hist(m4$resid, breaks=8)
qqnorm(m4$resid)
qqline(m4$resid)
plot(m4$resid~m4$fitted)
#However, we should consider that some players are left footed, others are right footed. We might try inferring this from the data.

#before we continue, can we include another term?
m5 <- lm(Distance~R_Strength + O_Strength + R_Flexibility, data=df)
summary(m5) # R^2 rises, but adjusted R_squared is unchanged. Stick to 2 terms

# now, guess leg preference based on strength
rfooted <- function(x) {
	if (x['L_Strength'] < x['R_Strength']) {
		return(1)
	} else {
		return(0)
	}
}
df <- cbind(df, right_footed=apply(df,1,rfooted))

m6 <- lm(Distance~I(right_footed * R_Strength) + I((1 - right_footed) * L_Strength) + O_Strength, data=df)
summary(m6)
anova(m6)
hist(m6$resid, breaks=8) # appears to be more normal.
qqnorm(m6$resid) # same deal
qqline(m6$resid)
plot(m6$resid~m6$fitted) 

anova(m4,m6)

avgKicker <- mean(df$Distance)
sdDist <- sd(df$Distance)
star <- avgKicker + 1.5 * sdDist  # top 7% play in varsity, this is roughly 1.5standard deviations assuming normal distribution


#define average to be 0 +- 0.25 sd
avgKickers <- subset(df,Distance > (avgKicker - 0.25 * sdDist) & Distance < (avgKicker + 0.25 * sdDist))
starKickers <- subset(df, Distance>star)

sapply(data.frame(predict(m6, newdata=avgKickers, interval="prediction")), mean)
sapply(data.frame(predict(m6, newdata=starKickers, interval="prediction")), mean)


