##############################
# Author: jack ruder
# Date: Oct 21, 2022
# Warmup: Effect Sizes
###############################

#a. This is an expreiment. This should allow the inference of causation
#b.

df <- read.csv("~/School/Stats/data/woodsTrees.csv")
plot(x, y, col=factor(Trt), data=df)
library(ggplot2)
ggplot(df, aes(x=Trt, y=Diameter, fill=Trt)) + 
	geom_violin() + 
	geom_boxplot(width=0.25, color='grey', alpha=0.4)

#c
(m <- aov(Diameter~Trt, data=df))
anova(m)
# F statistic is 3.42, p value is 0.041. This says that there is a probable effect about the treatments.

#d.

confint(m)
#e.
TukeyHSD(m)


#f. the group effect of P is 0.11/sqrt(0.052774) = 
# the group effect of N is 0.075/sqrt(0.052774) = 
