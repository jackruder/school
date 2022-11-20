##########
# Author: Jack Ruder
# 2022-11-08
# Warm-Up: Fitting and Assesing Two-Way ANOVA
###############################

library(palmerpenguins)
data(package='palmerpenguins')
head(penguins)
library(tidyr)
penguins <- penguins %>% drop_na(body_mass_g,species,sex)
attach(penguins)

#a
tapply(body_mass_g, list(species,sex), length)


#b. 
par(mfrow=c(1,2))
interaction.plot(species,as.factor(sex),body_mass_g) # here, interaction present between Adelie and chinstrap
interaction.plot(sex,as.factor(species),body_mass_g) # same interaction madee clear here, with different (not by much) slopes
library(ggplot2)
library(hrbrthemes)
ggplot(penguins, aes(x=interaction(species,sex),y=body_mass_g,color=species))+
	geom_violin(alpha=0.2) +
	geom_point() + 
	geom_boxplot(alpha=0, size=0.1) + 
	theme_ipsum()
ggplot(penguins, aes(x=interaction(sex, species),y=body_mass_g, color=sex))+
	geom_violin(alpha=0.2) +
	geom_point() + 
	geom_boxplot(alpha=0, size=0.3) + 
	theme_ipsum()
# jump here is larger between sexes for Gentoo

#d.
(an <- aov(body_mass_g~sex*species))
anova(an)
par(mfrow=c(1,3))
plot(an,1)
plot(an,2)
cellSD <- as.vector(tapply(body_mass_g, list(species,sex), sd))
cellM <- as.vector(tapply(body_mass_g, list(species,sex), mean))
plot(log(cellSD)~log(cellM))
abline((a <- lm(log(cellSD)~log(cellM))))
summary(a) # slope 0.13
## looks good, no transformations needed.
