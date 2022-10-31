#############################
# Authors: Jack Ruder and Tatum Bunnett
# Anova Lab
# Explores Forbes Top 2000 companies in 2017
# Runs and analyzes a one-way anova test
##############################
DATAFILE  <- "../../data/ForbesTop2000-2017.csv"
#DATAFILE <- "~/Desktop/ForbesTop2000-2017.csv"
df <- read.csv(DATAFILE)

unique(df$Sector)
df[df==""] <- "Unknown" # preprocess a bit for clarity

library(ggplot2)
library(hrbrthemes)
library(viridis)
library(dplyr)

png("densitiesog.png", width=8, height=8, units="in", res=150)
ggplot(data=df, aes(x=Sales, group=Sector, fill=Sector)) +
	geom_density(adjust=1.5, alpha=.4) +
    	theme_ipsum()
dev.off()
# a log transformation will be necessary, the data is heavily right skewed for all groups.

# plot the transformed data
png("densities.png", width=8, height=8, units="in", res=150)
ggplot(data=df, aes(x=log(Sales), group=Sector, fill=Sector)) +
	geom_density(adjust=1.5, alpha=.4) +
	theme_ipsum() + 
	facet_wrap(~Sector) + 
	theme( legend.position="none",
	      panel.spacing = unit(0.3, "lines"),
	      axis.ticks.x=element_blank()
	)
dev.off()

# make a violin plot of untransformed data 
png("violin.png", width=16, height=8, units="in", res=350)
ggplot(df, aes(x=Sector, y=Sales, fill=Sector)) + 
	geom_violin() + 
	geom_boxplot(width=0.25, color='grey', alpha=0.4) + 
	theme_ipsum()
dev.off()

# Financials, "" might have lower means, since their peaks are slightlry shifted to the left of the other distributions.  But the rest don't show any huge differences.


# Figure out what transformation we should use
gMeans <- tapply(df$Sales, df$Sector, mean)
gSd <- tapply(df$Sales, df$Sector, sd)
lm(log(gSd)~log(gMeans)) # slope 1.01

png("transformCheck.png", width=8, height=8, units="in", res=150)
plot(log(gSd)~log(gMeans), xlab='Log Group Means', ylab = 'Log Group Standard Deviation') # slope of 1, we should in fact use log
dev.off()

# run an anova 
(an <- aov(log(Sales)~Sector, data=df))
anova(an) # extremely significant, F=27.868 <2.2e^{-16}

png("grandConditionsCheck.png", width=8, height=8, units="in", res=150)
par(mfrow=c(1,2))
plot(an,1) # pretty much perfect here, indicates homogenity of variance
plot(an,2) # tails are subpar, especially for higher values.
dev.off()

# we see normality in the residuals.

TukeyHSD(an)

# get group effects and residuals
grand <- mean(log(df$Sales))
df <- df %>% 
    group_by(Sector) %>%
	mutate(
		  logMeanSalesSector = mean(log(Sales)),
		  logEffect = logMeanSalesSector-grand,
		  logResid = log(Sales) - grand - logEffect,
		  
		  MeanSalesSector=exp(logMeanSalesSector),
		  Effect=exp(logEffect),
		  Resid=exp(logResid)
	)

library(knitr)
# print the group effects
(gEffects <- tapply(df$Effect, df$Sector, mean))

#kable(data.frame(gEffects), "latex")

## now check the residuals
png("residualsGroupNormality.png", width=8, height=8, units="in", res=150)
ggplot(df, aes(sample=logResid)) + 
	stat_qq() + 
	stat_qq_line() + 
	facet_wrap(~Sector) + 
	theme_ipsum()
dev.off()

png("residualsDensity.png", width=8, height=8, units="in", res=150)
ggplot(df, aes(x=logResid, group=Sector, fill=Sector)) + 
	geom_density(adjust=1.5, alpha=.4) +
	facet_wrap(~Sector) + 
	theme_ipsum()
dev.off()
