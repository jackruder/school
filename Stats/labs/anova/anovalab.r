DATAFILE  <- "../../data/ForbesTop2000-2017.csv"
df <- read.csv(DATAFILE)

unique(df$Sector)
df[df==""] <- "Unknown"

library(ggplot2)
library(hrbrthemes)
library(viridis)

#png("densitiesog.png", width=8, height=8, units="in", res=150)
ggplot(data=df, aes(x=Sales, group=Sector, fill=Sector)) +
	geom_density(adjust=1.5, alpha=.4) +
    	theme_ipsum()
#dev.off()
# a log transformation will be necessary, the data is heavily right skewed for all groups.

# plot the transformed data
#png("densities.png", width=8, height=8, units="in", res=150)
ggplot(data=df, aes(x=log(Sales), group=Sector, fill=Sector)) +
	geom_density(adjust=1.5, alpha=.4) +
	theme_ipsum() + 
	facet_wrap(~Sector) + 
	theme( legend.position="none",
	      panel.spacing = unit(0.3, "lines"),
	      axis.ticks.x=element_blank()
	)
#dev.off()

png("violin.png", width=16, height=8, units="in", res=350)
ggplot(df, aes(x=Sector, y=Sales, fill=Sector)) + 
	geom_violin() + 
	geom_boxplot(width=0.25, color='grey', alpha=0.4) + 
	theme_ipsum()
dev.off()

# Financials, "" might have lower means, since their peaks are slightlry shifted to the left of the other distributions.  But the rest don't show any huge differences.

gMeans <- tapply(df$Sales, df$Sector, mean)
gSd <- tapply(df$Sales, df$Sector, sd)
plot(gSd~gMeans) # slope of 1, we should in fact use log

(an <- aov(log(Sales)~Sector, data=df))
anova(an) # extremely significant, <2.2e^{-16}

plot(an,1) # pretty much perfect here, indicates homogenity of variance
plot(an,2) # tails are subpar, especially for higher values.

# we see normality in the residuals.

TukeyHSD(an)

