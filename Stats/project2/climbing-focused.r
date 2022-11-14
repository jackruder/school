################
library(ggplot2)
library(hrbrthemes)
library(dplyr)
################ ANALYSIS ########################
######## Do Male and Female climbers percieve gym grades differently?
######## Block using whether or not they climb outside >1 a week?
######################################################################


FILE <- "./climbing.csv"
df <- read.csv(FILE)

label <- c('time.response','boulder.flash', 'boulder.redpoint', 'lead.onsight', 'lead.flash', 'lead.redpoint', 'height', 'weight', 'age', 'sex', 'years.climbing', 'pull.max', 'fingers.max2', 'fingers.max1', 'days.inside', 'days.outside', 'days.hangboard', 'days.collagen', 'days.protein', 'healthy', 'alcohol', 'thc', 'home.gym.grades', 'away.gym.grades', 'home.crag.grades', 'away.crag.grades', 'finger.injury.6', 'finger.injury.24')

names(df) <- label

## climbs outside
climbsOutside <- function(x) {
	if (is.na(x)) {
		return(NA)
	}
	if (x >= 4) {
		return("frequently")
	} else {
		return("infrequently")
	}
}
df$climbs.outside <- sapply(df$days.outside, climbsOutside)

isOther <- function(x){return(x=='Other')}
df1 <- df %>% 
	filter_at(vars(sex,climbs.outside,home.gym.grades,away.gym.grades), all_vars(!is.na(.))) %>%
	filter_at(vars(sex), all_vars(!isOther(.)))
attach(df1)
tapply(home.gym.grades, list(sex,climbs.outside), length) ## cannot say much for beginners or for expert due to number of responses
par(mfrow=c(1,2))
interaction.plot(climbs.outside,sex,home.gym.grades)
interaction.plot(sex,climbs.outside,home.gym.grades)# definitely should consider an interaction term, different slopes 

ggplot(df1, aes(x=interaction(climbs.outside, sex),y=home.gym.grades,color=sex))+
	geom_violin(alpha=0.2) +
	geom_point() + 
	geom_boxplot(alpha=0.1, size=0.2) + 
	theme_ipsum()
# means appear very close, expect negligible differences

(an <- aov(home.gym.grades~sex*climbs.outside))
anova(an) # sex is significant, F=3.94 p=0.048. Not clear that climbs.outside is at all relevant
TukeyHSD(an)#


means <- tapply(home.gym.grades, list(sex,climbs.outside), length) ## cannot say much for beginners or for experts due to number of responses

(grand = mean(home.gym.grades))

(sexEffect = tapply(home.gym.grades, 
                       sex, 
                       mean) - grand)

(outdoorEffect = tapply(home.gym.grades, 
                       climbs.outside, 
                       mean) - grand)

(interaction = (means - rbind(outdoorEffect,outdoorEffect)
                - cbind(sexEffect,sexEffect) - grand))

