##################
# Author: Jack Ruder
# Date: Oct 31, 2022
# Warm-Up: Main Effects Models
####################

DATAFILE <- "~/Downloads/singerHeightsData.csv"
df <- read.csv(DATAFILE)

#a. The response here is the height of the individual. 
#The blocking variable is the part they sing, with 4 levels.
#The explanatory variable is which choir, with 2 levels.
# It appears that each part has a subdivision of 2 more categories, and there is a variable accounting the number of singers for each part in each choir.

#b. Results from this study cannot describe any causation, however we might use the results to infer about height vs part sung, though this will not directly result from an anova. We will be able to understand the differences between these two choirs. 

#c.

tapply(df$height, list(df$choir, df$part), mean)
interaction.plot(df$part, df$choir, df$height)

#d.
(an <- aov(height ~ choir + part, data=df)) 

(grand <- mean(df$height))
TukeyHSD(an) # no significant difference between sopranos and altos, tenor and bass are barely the same.
# Reed Chorus is shorter


#e.

par(mfrow=c(1,2))
plot(an,1) 
plot(an,2)

# residuals appear normally distributed, and randomly dispersed while centered around 0. There is no indication that part and choir should be dependent upon eachother, so all-in-all this appears to be pretty good.

