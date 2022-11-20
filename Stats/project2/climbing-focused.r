#######
## Author: Jack Ruder
## Dataset collected from google forms survey
## R script explores a singled out question from a larger exploration of data
################
library(ggplot2)
library(hrbrthemes)
library(dplyr)
################ ANALYSIS ########################
######## Do Male and Female climbers percieve gym grades differently?
######## Block using whether or not they climb outside >1 a week?
######################################################################

### load, no preprocessing required. 
FILE <- "./climbing.csv"
df <- read.csv(FILE)

#relabel data
label <- c('time.response','boulder.flash', 'boulder.redpoint', 'lead.onsight', 'lead.flash', 'lead.redpoint', 'height', 'weight', 'age', 'sex', 'years.climbing', 'pull.max', 'fingers.max2', 'fingers.max1', 'days.inside', 'days.outside', 'days.hangboard', 'days.collagen', 'days.protein', 'healthy', 'alcohol', 'thc', 'home.gym.grades', 'away.gym.grades', 'home.crag.grades', 'away.crag.grades', 'finger.injury.6', 'finger.injury.24')

names(df) <- label

## climbs outside binary predictor
climbsOutside <- function(x) {
	if (is.na(x)) {
		return(NA)
	}
	if (x >= 4) { ## once a week
		return("frequently")
	} else {
		return("infrequently")
	}
}
df$climbs.outside <- sapply(df$days.outside, climbsOutside) 

isOther <- function(x){return(x=='Other')} ## too few entries for other to use, remove them
df1 <- df %>% 
	filter_at(vars(sex,climbs.outside,home.gym.grades,away.gym.grades), all_vars(!is.na(.))) %>%
	filter_at(vars(sex), all_vars(!isOther(.)))
attach(df1)
library(knitr)

### explore data
(n <- tapply(home.gym.grades, list(sex,climbs.outside), length))##  disproportionately infrequent females, otherwise balaneced

par(mfrow=c(1,2))
interaction.plot(climbs.outside,sex,home.gym.grades)
interaction.plot(sex,climbs.outside,home.gym.grades)# definitely should consider an interaction term, different slopes 

ggplot(df1, aes(x=interaction(climbs.outside, sex),y=home.gym.grades,color=sex))+
	geom_violin(alpha=0.2) +
	geom_point() + 
	geom_boxplot(alpha=0.1, size=0.2) + 
	theme_ipsum()
# means appear very close, expect negligible differences

## f test
(an <- aov(home.gym.grades~sex*climbs.outside))
anova(an) # sex is significant, F=3.94 p=0.048. Not clear that climbs.outside is at all relevant
TukeyHSD(an)#

## fit the model
means <- tapply(home.gym.grades, list(sex,climbs.outside), mean) ## cannot say much for beginners or for experts due to number of responses

(grand = mean(home.gym.grades))

(sexEffect = tapply(home.gym.grades, 
                       sex, 
                       mean) - grand)

(outdoorEffect = tapply(home.gym.grades, 
                       climbs.outside, 
                       mean) - grand)

(interaction = (means - rbind(outdoorEffect,outdoorEffect)
                - cbind(sexEffect,sexEffect) - grand))
library('dplyr')
df1 <- df1 %>% ## add fitted parameters to dataframe
	group_by(sex) %>%
		mutate(
		      sexEffect = mean(home.gym.grades) - grand
		)

df1 <- df1 %>%
	group_by(climbs.outside) %>%
		mutate(
		      outdoorEffect = mean(home.gym.grades) - grand
		)
df1 <- df1 %>%
	group_by(climbs.outside, sex) %>%
		mutate(
		      interact = mean(home.gym.grades) - outdoorEffect - sexEffect,
		      res = home.gym.grades - grand - outdoorEffect - sexEffect - interact
		)

# produce inference plot
par(mfrow=c(2,2))
plot(an,1)
plot(an,2)
comp <- df1$sexEffect * df1$outdoorEffect / grand
tkpl <- an$resid~comp
plot(tkpl, main="Tukey Non-Additivity Plot")
abline(lm(tkpl))
cellSD <- as.vector(tapply(home.gym.grades, list(sex,climbs.outside), sd))
cellM <- as.vector(tapply(home.gym.grades, list(sex,climbs.outside), mean))
plot(log(cellSD)~log(cellM)) ## oof, -1 slope. transform to reciprocal
abline((a <- lm(log(cellSD)~log(cellM))))
summary(a)

an2 <- aov(1/home.gym.grades ~ sex*climbs.outside) ## dang it, it was better.

anova(an2) # much more significant,
TukeyHSD(an2) # we see more significant differences in group means


meansI <- tapply(1/home.gym.grades, list(sex,climbs.outside), mean) ## cannot say much for beginners or for experts due to number of responses


# fit the transformed model
(grandI = mean(1/home.gym.grades))
(sexEffectI = tapply(1/home.gym.grades, 
                       sex, 
                       mean) - grandI)
(outdoorEffectI = tapply(1/home.gym.grades, 
                       climbs.outside, 
                       mean) - grandI)
(interaction = (meansI - rbind(outdoorEffectI,outdoorEffectI)
                - cbind(sexEffectI,sexEffectI) - grandI))
library('dplyr')
df1 <- df1 %>% ## add transformed fitted parameters to model
	group_by(sex) %>%
		mutate(
		      sexEffectI = mean(1/home.gym.grades) - grandI
		)

df1 <- df1 %>%
	group_by(climbs.outside) %>%
		mutate(
		      outdoorEffectI = mean(1/home.gym.grades) - grandI
		)
df1 <- df1 %>%
	group_by(climbs.outside, sex) %>%
		mutate(
		      interactI = mean(1/home.gym.grades) - outdoorEffectI - sexEffectI,
		      resI = 1/home.gym.grades - grandI - outdoorEffectI - sexEffectI - interactI
		)
par(mfrow=c(2,2))
plot(an2,1)
plot(an2,2)
compI <- df1$sexEffectI * df1$outdoorEffectI / grandI
tkpl <- an2$resid~compI
plot(tkpl, main="Tukey Non-Additivity Plot")
abline(lm(tkpl))

cellSDI <- as.vector(tapply(1/home.gym.grades, list(sex,climbs.outside), sd))
cellMI <- as.vector(tapply(1/home.gym.grades, list(sex,climbs.outside), mean))
plot(log(cellSDI)~log(cellMI)) ## oof, -1 slope. transform to reciprocal
abline((a2 <- lm(log(cellSDI)~log(cellMI))))
summary(a2)
