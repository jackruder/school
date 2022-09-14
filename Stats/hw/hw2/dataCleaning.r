########################
# author: Jack Ruder
# date: Sep 12, 2022
#
#  Data cleaning HW for Stats 260
# ########################
require(ggplot2)
require(hrbrthemes)
library(ggplot2)
library(hrbrthemes)

# Parse the data from csv format, preprocess in pythno
df <- read.csv("child_anthro_1977.csv")
head(df)

#1. Get number of variables
ncol(df)
# -> 123

#2. Get number of rows
nrow(df)
# -> 3901


#3.
numNa  <- sapply(df, function(col) sum(length(which(is.na(col)))))
numNa['Head_Length'] # -> 2600


#4.
range(subset(df, Age_in_Years > 0)$Age_in_Years)
# -> 2001, 20054
# The youngest in the study is 2.0 years, according to pg.446 in the pdf
# So, we divide by 1000
range(subset(df, Age_in_Years > 0)$Age_in_Years/1000)
# ->  2.001, 20.054
# It seems like the units are in Years/1000 since this is a plausible range
#
# Add the scaled age in years
df$Age <- df$Age_in_Years/1000
# We can check too, there is an Age in Months Column, where months are entered as integers
mean(df$Age/df$Age_in_Months, na.rm=TRUE) - 1/12
# => 0.0004
# This is next to zero, so the scaling is appropriate.


#5.
head(df$Weight)
plot(df$Weight ~ df$Age)
# Based on the plot, it seems like the values are small, and increasing with age.
range(subset(df, Weight>0)$Weight)
# The top end value is 1123, this should be 112.3 kg, bottom is 10.1 kg, both logical.
# Thus units here are in KG/10. These max/mins correspond too with values seen on pg. 454 (min 10.2kg for 2-3) and 461 of the pdf (max 112.3 for 17.5-19).


#6.
# We will look at the post-puberty weights, males should be heavier on average.
old <- subset(df, Age>17)
tapply(old$Weight, old$Gender, mean, na.rm=TRUE)
# => 1, 720.7292
# => 2, 553.8828
# The implication here is that Gender 1 is Male, and 2 is Female.
# Also, on pdf page 41 there is an indication that 1 is male and 2 is female.

#7/8/9
tapply(df$Handedness, df$Handedness,length)
numNa['Handedness'] # -> 0
hand0  <- subset(df, Handedness==0)
head(hand0)
hand5  <- subset(df, Handedness==5)
head(hand5)
# Here, there are 5 values shown in the table. 309 enteries are neither 1 nor 2.In the pdf there is Right Handed, Left Handed, and Both as options. Since 1 has by far the most entries, and it is listed first on the form, it is a logical choice for Right handed. 2 has the next most, so it is probably Left handed. 3 makes sense to be Both following the rationale for 1 and 2. 0 and 5 are the strange values, and lack any description in the pdf. On page 41, we learn that the data was entered by humans using words, and transcribed in software to a number. 0 Likely then means N/A of sorts(missing values), since none of the values in the Handedness column are N/A. Looking at the single entry for 5, it is hard to see any pattern for why there exists a unique value. Potentially 5 represents a disability of sorts, although there is no comment code for the entry.

#10. From pdf page 75, stature records the height (floor to top of head).
df$Stature
# Units in the tables in the study are in cm. The units in the table then should be cm/10. 

#11
# The questionnaire on pg. 28 of the pdf describes the birth order as the order in which children were born, with 1 being the oldest, 2 being the second oldest, and so on.
tapply(df$Birth_Order, df$Birth_Order,length)
# The values that are most questionable are 0, 20 and 90. 
# In consistency with handedness the most likely scenario is that 0 is for a blank. Being the 20th born while unlikely seems plausible, so discounting that value is smart. Again, 90 must be a computer assigned code. Potentially this value represents that the field was filled out as unknown by the subject, but not left blank.

#12 
male <- subset(df, Gender==1)
female <- subset(df, Gender==2)

ggplot() +
	geom_smooth(aes(x=Age, y=Stature),data=male,method="gam", color='blue') +  
	geom_smooth(aes(x=Age, y=Stature),data=female,method="gam", color='pink') + 
	geom_point(aes(x=Age, y=Stature),data=male,color='blue') +  
	geom_point(aes(x=Age, y=Stature),data=female,color='pink') + 
	ggtitle("Plot of Stature vs Age for Males and Females") +
	ylab("Stature in cm/10") +
	xlab("Age in Years") +
	theme_ipsum()
ggsave("q12.png")
# now just the lines
ggplot() + 
	geom_smooth(aes(x=Age, y=Stature),data=male,method="gam", color='blue') +  
	geom_smooth(aes(x=Age, y=Stature),data=female,method="gam", color='pink') + 
	theme_ipsum()

#13.
hist(df$Upper_Arm_Circumference)
# We see that the distribution is skewed right.
hist(log(df$Upper_Arm_Circumference))
# After the log transformation, the hisogram shows a normal distribution of upper arm circumferences
