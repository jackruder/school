DATAFILE  <- "../data/ForbesTop2000-2017.csv"
df <- read.csv(DATAFILE)

library(ggplot2)
library(hrbrthemes)
library(viridis)
ggplot(data=df, aes(x=Sales, group=Sector, fill=Sector)) +
	geom_density(adjust=1.5, alpha=.4) +
    	theme_ipsum()
# a log transformation will be necessary, the data is heavily right skewed for all groups.

ggplot(data=df, aes(x=log(Sales), group=Sector, fill=Sector)) +
	geom_density(adjust=1.5, alpha=.4) +
	theme_ipsum() + 
	facet_wrap(~Sector) + 
	theme( legend.position="none",
	      panel.spacing = unit(0.1, "lines"),
	      axis.ticks.x=element_blank()
	)
# Financials, "" might have lower means, since their peaks are slightlry shifted to the left of the other distributions.  But the rest don't show any huge differences.
