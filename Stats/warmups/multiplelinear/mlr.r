#######
# author: Jack Ruder
# date: Sep 30, 2022
# Warm-up: multiple linear regression 
######
# load, attach and view the data
df <- read.csv("./snowPetrels.csv")
head(df)

# produce initial scatter plot
plot(df$wing ~ df$tarsus)

#construct initial model
iModel <- lm(df$wing ~ df$tarsus)
abline(iModel)
# we see a clear outlier in the plot, we should examine and probably remove it

# determine quartile range
q <- quantile(df$tarsus, probs=c(0.25,0.75), na.rm=TRUE)
iqr <- IQR(df$tarsus, na.rm=TRUE)

# remove outliers
rem <- subset(df, (tarsus > (q[1] - 1.5*iqr)) & (tarsus < (q[2] + 1.5*iqr)))

