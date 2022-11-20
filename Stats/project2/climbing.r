################
## Quick and Dirty R script
## to explore potential questions that might be asked of the climbing survey
#####
#### Author: Jack Ruder


FILE <- "./climbing.csv" ## lock and load
df <- read.csv(FILE) ## fire

label <- c('time.response','boulder.flash', 'boulder.redpoint', 'lead.onsight', 'lead.flash', 'lead.redpoint', 'height', 'weight', 'age', 'sex', 'years.climbing', 'pull.max', 'fingers.max2', 'fingers.max1', 'days.inside', 'days.outside', 'days.hangboard', 'days.collagen', 'days.protein', 'healthy', 'alcohol', 'thc', 'home.gym.grades', 'away.gym.grades', 'home.crag.grades', 'away.crag.grades', 'finger.injury.6', 'finger.injury.24')

names(df) <- label ### relabel for now, additional questions will require renaming this


# parse data


## boulder grades to int
stripV <- function(x) substring(x, 2)
df$boulder.redpoint <- strtoi(stripV(df$boulder.redpoint))
df$boulder.flash <- strtoi(stripV(df$boulder.flash))

## yds grades to int
ydsToInt <- function(x) {
	if (x == "") {
		return (NA)
	}	
	relevant <- substring(x,3)
	hasChar  <- nchar(relevant) == 3
	num <- strtoi(substring(relevant,1,2))
	if( num <= 9) {
		return(num)
	} else {
		charVal <- 0
		if (hasChar) {
			char <- substring(relevant,3)
			if (char == 'a') {
				charVal <- 0
			} else if (char == 'b') {
				charVal <- 1
			} else if (char == 'c') {
				charVal <- 2
			} else if (char == 'd') {
				charVal <- 3
			}
		}
		return(10 + (num-10) * 4 + charVal)
	}
}


df$lead.onsight <- sapply(df$lead.onsight, ydsToInt)
df$lead.flash <- sapply(df$lead.flash, ydsToInt)
df$lead.redpoint <- sapply(df$lead.redpoint, ydsToInt)


## 1 - 5 soft/stiff scale to soft, not soft 
softStiff  <- function(x) { 
	if(is.na(x)) {
		return(NA)
	}
	if (x < 3) {
		return("soft")
	} else if (x > 3) {
		return("stiff")
	} else {
		return("neutral")
	}

}

softNotSoft  <- function(x) { 
	if(is.na(x)) {

		return(NA)
	}
	if (x < 3) {
		return("soft")
	} else {
		return("not soft")
	}

}

df$home.gym.grades.cat <- sapply(df$home.gym.grades, softStiff)
df$away.gym.grades.cat <- sapply(df$away.gym.grades, softStiff)
df$home.crag.grades.cat <- sapply(df$home.crag.grades, softStiff)
df$away.crag.grades.cat <- sapply(df$away.crag.grades, softStiff)
df$home.gym.grades.bin <- sapply(df$home.gym.grades, softNotSoft)
df$away.gym.grades.bin <- sapply(df$away.gym.grades, softNotSoft)
df$home.crag.grades.bin <- sapply(df$home.crag.grades, softNotSoft)
df$away.crag.grades.bin <- sapply(df$away.crag.grades, softNotSoft)

## climbs outside
climbsOutside <- function(x) {
	if (is.na(x)) {
		return(NA)
	}
	if (x >= 1) { ## spikes over 0,1,4, likely not at all, once a month, once a week
		return("yes")
	} else {
		return("no")
	}
}
df$climbs.outside <- sapply(df$days.outside, climbsOutside)

blockGrades <- function(x) {
	if (is.na(x)) {
		return(NA)
	}
	if (x <= 3) {
		return("Beginner")
	} else if (x >= 4 & x<= 6) {
		return("Intermediate") 
	} else if (x >= 7 & x <= 9) {
		return("Advanced")
	} else {
		return("Expert")
	}
}
df$ability <- sapply(df$boulder.redpoint, blockGrades)

################ ANALYSIS ########################
## do climbers who believe grades are soft climb harder?
## to carry out a two-way analysis, block how they percieve their home gyms versus other gyms
################################################################
library(ggplot2)
library(hrbrthemes)
library(dplyr)

df1 <- df %>% 
	filter_at(vars(home.gym.grades.cat,away.gym.grades.bin,boulder.redpoint), all_vars(!is.na(.)))
detach(df1)
attach(df1)


tapply(boulder.redpoint, list(home.gym.grades.cat, away.gym.grades.bin), length)

par(mfrow=c(1,2))
interaction.plot(home.gym.grades.cat,away.gym.grades.bin, boulder.redpoint)
interaction.plot(away.gym.grades.bin,home.gym.grades.cat, boulder.redpoint) # need an interaction term
ggplot(df1, aes(x=interaction(home.gym.grades.cat, away.gym.grades.bin),y=boulder.redpoint,color=home.gym.grades.cat))+Infrequently

	geom_violin(alpha=0.2) +
	geom_point() + 
	geom_boxplot(alpha=0.1, size=0.2) + 
	theme_ipsum()

ggplot(df1, aes(x=interaction(away.gym.grades.bin, home.gym.grades.cat),y=boulder.redpoint,color=away.gym.grades.bin))+
	geom_violin(alpha=0.2) +
	geom_point() + 
	geom_boxplot(alpha=0.1, size=0.2) + 
	theme_ipsum()

(an <- aov(boulder.redpoint~home.gym.grades.cat*away.gym.grades.bin))
anova(an) # no stated significance for each individual term.
TukeyHSD(an)# no stated significance between group means
detach(df1)
#####################y


############################################
# Try with crag gradesInfrequently
#############################################
df1 <- df %>% 
	filter_at(vars(home.crag.grades.cat,away.crag.grades.cat,boulder.redpoint), all_vars(!is.na(.)))
attach(df1)


tapply(boulder.redpoint, list(home.crag.grades.cat, away.crag.grades.cat), length)

par(mfrow=c(1,2))
interaction.plot(home.crag.grades.cat,away.crag.grades.cat, boulder.redpoint)
interaction.plot(away.crag.grades.cat,home.crag.grades.cat, boulder.redpoint) # no pronounced interaction
ggplot(df1, aes(x=interaction(home.crag.grades.cat, away.crag.grades.cat),y=boulder.redpoint,color=home.crag.grades.cat))+
	geom_violin(alpha=0.2) +
	geom_point() + 
	geom_boxplot(alpha=0.1, size=0.2) + 
	theme_ipsum()

ggplot(df1, aes(x=interaction(away.crag.grades.cat, home.crag.grades.cat),y=boulder.redpoint,color=away.crag.grades.cat))+
	geom_violin(alpha=0.2) +
	geom_point() + y
	geom_boxplot(alpha=0.1, size=0.2) + 
	theme_ipsum()

(an <- aov(boulder.redpoint~home.crag.grades.cat+away.crag.grades.cat))
anova(an) # no stated significance for each individual term, but p=0.057 and p=0.072 are very close!

TukeyHSD(an)# no stated significance between group means, however for away crags, stiff-soft has a diff of -2.516, adjusted p value of 0.065
detach(df1)
################################################




#############################################################################
##### Do Male and Female climbers percieve gym grades differently?
##### Need to block by ability
##### not enough responses in Other
#############################################################################
toobig <- function(x){return(x > 84)}
isOther <- function(x){return(x=='Other')}
df1 <- df %>% 
	filter_at(vars(sex,ability,height,home.gym.grades,away.gym.grades), all_vars(!is.na(.))) %>%
	filter_at(vars(height), all_vars(!toobig(.))) %>% # just remove > 7 ft
	filter_at(vars(sex), all_vars(!isOther(.)))
	
attach(df1)
tapply(home.gym.grades, list(sex,ability), length) ## cannot say much for beginners or for experts due to number of responses
par(mfrow=c(1,2))
interaction.plot(sex,ability,home.gym.grades)
interaction.plot(ability,sex,home.gym.grades)# definitely should consider an interaction term, note that the slope is misleading due to a single female expert

ggplot(df1, aes(x=interaction(sex, ability),y=home.gym.grades,color=sex))+
	geom_violin(alpha=0.2) +
	geom_point() + 
	geom_boxplot(alpha=0.1, size=0.2) + 
	theme_ipsum()

(an <- aov(home.gym.grades~sex*ability))
anova(an) # p value of 0.059 Male-Female
TukeyHSD(an) # no significant differences. Ability is not a good blocking variable!

(an <- aov(away.gym.grades~sex*ability))
anova(an) # nothing significant, 0.11 pval Male-Female
TukeyHSD(an)

(an <- aov(home.crag.grades~sex*ability))
anova(an) # p value of 0.06 for abilitiy here, interesting!
TukeyHSD(an) # no siginficant differences, 0.07 Intermediate-Expert
	filter_at(vars(height), all_vars(!toobig(.))) %>% # just remove > 7 ft


(an <- aov(away.crag.grades~sex*ability))
anova(an) #p val 0.11 Male-Female
TukeyHSD(an) # nothing interesting

detach(df1)
################################################################## 
######## Same question, do Male and Female climbers percieve indoor grades differently?
######## what if we block using whether or not they climb outside >1 a month?
######################################################################
df1 <- df %>% 
	filter_at(vars(sex,climbs.outside,home.gym.grades,away.gym.grades), all_vars(!is.na(.))) %>%
	filter_at(vars(sex), all_vars(!isOther(.)))
attach(df1)
tapply(home.gym.grades, list(sex,climbs.outside), length) ## cannot say much for beginners or for experts due to number of responses
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
(oneway <- aov(home.gym.grades~sex)) 
anova(oneway) # F=3.6, climbs.outside is not a good blocking variable.

TukeyHSD(an) # 
################################################
################################################################## 
######## Do climbers of different ability levels percieve outdoor grades differently?
######## block with ability, and block using whether or not they climb outside >1 a month?
######################################################################
df1 <- df %>% 
	filter_at(vars(ability,climbs.outside,home.crag.grades,away.crag.grades), all_vars(!is.na(.)))
attach(df1)
tapply(home.crag.grades, list(ability,climbs.outside), length) ## cannot say much for beginners or for experts due to number of responses
par(mfrow=c(1,2))
interaction.plot(climbs.outside,ability,home.crag.grades)
interaction.plot(ability,climbs.outside,home.crag.grades) # again, interaction term is appropriate, hugely!

ggplot(df1, aes(x=interaction(climbs.outside, ability),y=home.gym.grades,color=ability))+
	geom_violin(alpha=0.2) +
	geom_point() + 
	geom_boxplot(alpha=0.1, size=0.2) + 
	theme_ipsum()

(an <- aov(home.crag.grades~ability*climbs.outside))
anova(an) # no individually significant differences, though F values are all better than 1
TukeyHSD(an) #  Intermediate-Expert adjusted p val of 0.14, Infrequently-Frequently adjusted p-val of 0.134
################################################
