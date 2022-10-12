################
# Author: Jack Ruder
# Date: Oct 06, 2022
# Multiple Linear Regression Lab report III
###############################
DATADIR <- "~/School/Stats/data"
FILENAME <- "kc_house_data.csv"
SEP <- "/" # posix

DATAFILE <- paste(DATADIR, FILENAME, sep=SEP)
df <- read.csv(DATAFILE)
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
