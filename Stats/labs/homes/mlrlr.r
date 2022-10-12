################
# Author: Jack Ruder
# Date: Oct 06, 2022
# Multiple Linear Regression Pre-Lab
###############################
#Warmup answers
#
# Variable		Description
#Distance		Distance travelled in feet
#Hang		Time in air in seconds
#R_Strength		Right leg strength in pounds
#L_Strength		Left leg strength in pounds
#R_Flexibility		Right leg flexibility in degrees
#L_Flexibility		Left leg flexibility in degrees
#O_Strength		Overall leg strength in pounds
#
# Obviously, distance and left/right strength (with an indicator term) are going to be the most useful.
# One of these models would be helpful in identifying weaknesses in someone's performance, to identify skill vs. strength, or to weed out certain players based on their metrics.
################

DATADIR <- "~/School/Stats/data"
FILENAME <- "punting.txt"
SEP <- "/" # posix

DATAFILE <- paste(DATADIR, FILENAME, sep=SEP)
df <- read.table(DATAFILE,header=TRUE)
#attach(df)

library(ggplot2)
ggplot(df, aes(x=R_Strength, y=O_Strength)) +
	geom_point(size=3) 




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

RES_X <- 1080
RES_Y <- 1080

colname <- "Left Leg Strength"
x <- L_Strength
fname <- gsub(" ", "_", colname)
png(paste(fname,"png",sep="."), width=RES_X, height=RES_Y)
plotdensity(x, df, colname)
dev.off()

colname <- "Right Leg Strength"
x <- R_Strength
fname <- gsub(" ", "_", colname)
png(paste(fname,"png",sep="."), width=RES_X, height=RES_Y)
plotdensity(x, df, colname)
dev.off()

colname <- "Overall Leg Strength"
x <- O_Strength
fname <- gsub(" ", "_", colname)
png(paste(fname,"png",sep="."), width=RES_X, height=RES_Y)
plotdensity(x, df, colname)
dev.off()

colname <- "Left Leg Flexibility"
x <- L_Flexibility
fname <- gsub(" ", "_", colname)
png(paste(fname,"png",sep="."), width=RES_X, height=RES_Y)
plotdensity(x, df, colname)
dev.off()

colname <- "Right Leg Flexibility"
x <- R_Flexibility
fname <- gsub(" ", "_", colname)
png(paste(fname,"png",sep="."), width=RES_X, height=RES_Y)
plotdensity(x, df, colname)
dev.off()

colname <- "Distance"
x <- Distance
fname <- gsub(" ", "_", colname)
png(paste(fname,"png",sep="."), width=RES_X, height=RES_Y)
plotdensity(x, df, colname)
dev.off()

colname <- "Hang Time"
x <- Hang
fname <- gsub(" ", "_", colname)
png(paste(fname,"png",sep="."), width=RES_X, height=RES_Y)
plotdensity(x, df, colname)
dev.off()


#observe that all of the variables appear to be fairly normally distributed, no obvious tronsformations are necessary

m <- lm(Distance~L_Strength + R_Strength + R_Flexibility +  O_Strength, data=df)
# adjusted r squared of 0.943, not a terrible overfit
summary(m)
anova(m)
