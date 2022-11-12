#################################
library(knitr)
library(ggplot2)
library(hrbrthemes)
library(viridis)
library(svglite)
library(dplyr)

FILE <- "~/School/Stats/data/babes2.csv"
babes <- read.csv(FILE)

str(babes)
head(babes)
attach(babes)

(cellCt <- tapply(HatchBmass, list(egg.trmt, inc.envmt), length)) # not balanced
#kable(data.frame(cellCt), "latex")

png("int.png",width=8,height=6, units='in', res=150)
par(mfrow=c(1,2))
interaction.plot(inc.envmt,as.factor(egg.trmt),HatchBmass)
interaction.plot(egg.trmt,as.factor(inc.envmt),HatchBmass)
dev.off()
#mostly parallel, but not perfect

png("bp1.png",width=8,height=6, units='in', res=150)
ggplot(babes, aes(x=interaction(inc.envmt,egg.trmt),y=HatchBmass,color=inc.envmt))+
	geom_violin(alpha=0.2) +
	geom_point() + 
	geom_boxplot(alpha=0.1, size=0.2) + 
	theme_ipsum()
dev.off()
png("bp2.png",width=8,height=6, units='in', res=150)
ggplot(babes, aes(x=interaction(egg.trmt, inc.envmt),y=HatchBmass, color=egg.trmt))+
	geom_violin(alpha=0.2) +
	geom_point() + 
	geom_boxplot(alpha=0.1, size=0.2) + 
	theme_ipsum()e
dev.off()



###########################
# non-transformed analysis
###########################
(grand <- mean(HatchBmass))
(trmtEffect <- tapply(HatchBmass, egg.trmt, mean) - grand)
(envmtEffect <- tapply(HatchBmass, inc.envmt, mean) - grand)
(an <- aov(HatchBmass~egg.trmt + inc.envmt))
babes <- babes %>%
	group_by(egg.trmt) %>%
		mutate(
		      trmtEffect = mean(HatchBmass) - grand
		)

babes <- babes %>%
	group_by(inc.envmt) %>%
		mutate(
		      envmtEffect = mean(HatchBmass) - grand,
		      res = HatchBmass - trmtEffect - envmtEffect
		)
anova(an)

png("inference.png",width=8,height=6, units='in', res=150)
par(mfrow=c(2,2))
plot(an,1)
plot(an,2)
comp <- babes$trmtEffect * babes$envmtEffect / grand
plot(an$resid~comp, main="Tukey Non-Additivity Plot")
abline(lm(an$resid~comp))

cellSD <- as.vector(tapply(HatchBmass, list(inc.envmt,egg.trmt), sd))
cellM <- as.vector(tapply(HatchBmass, list(inc.envmt,egg.trmt), mean))
plot(log(cellSD)~log(cellM))
abline((a <- lm(log(cellSD)~log(cellM))))
dev.off()
summary(a) # slope 1.162, looks good
##################
sdErr <- anova(an)[3,3]
(trmtEffectSize <- TukeyHSD(an)$egg.trmt[1] / sdErr)
(envrEffectSize <- TukeyHSD(an)$inc.envmt[1] / sdErr)
