########################
# author: Jack Ruder
# date: Sep 12, 2022
#
#  Data cleaning HW for Stats 260
# ########################

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

#5.
head(df$Weight)
plot(df$Weight ~ df$Age)
# Based on the plot, it seems like the values are small, and increasing with age.
range(subset(df, Weight>0)$Weight)
# The top end value is 1123, this should be 112.3 kg, bottom is 10.1 kg, both logical.
# Thus units here are in KG/10. These max/mins correspond too with values seen on pg. 454 (min 10.2kg for 2-3) and 461 of the pdf (max 112.3 for 17.5-19).
