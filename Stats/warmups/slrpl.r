################
# Author: Jack Ruder
# Date: Sep 27, 2022
# Simple Linear Rgeression Pre-Lab
###############################

DATADIR <- "~/School/Stats/data"
FILENAME <- "hanford.txt"
SEP <- "/" # posix

DATAFILE <- paste(DATADIR, FILENAME, sep=SEP)
dat <- read.table(DATAFILE,header=TRUE)

library(ggplot2)
ggplot(dat, aes(x=Exposure, y=Mortality, label=County)) +
	geom_point(size=3) +
	geom_text(hjust=-0.1, vjust=-0.1)
# based on the plot, I would not assume the linear model to have a very strong fit. There are few points, and multiple points with similar y values for different x values.

#2. Clearly, there is a correlation of higher cancer risks with higher indices of exposure. This data would help show what levels of exposure are possible given an acceptable (if any) risk of cancer

#3.  
# I believe they should, their lives are affected negatively and unfairly, thus they should be compensated as medical issues/impacted health are irreversible in most cases. Again, we see that cancer has a positive correlation with exposure. With a t-test and R^2 value, the argument can be mad that this correlation is significant, which establishes a causation.
