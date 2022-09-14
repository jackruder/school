irisData <- iris
head(iris)

grapeData <- read.csv(file.choose(), header=TRUE)
grapeData <- read.csv("~/Downloads/grapes.csv", header=TRUE)
head(grapeData)

rainData <- read.table("http://www.statsci.org/data/oz/sydrain.txt", header=TRUE)
head(rainData)

#steps described in warmup are unecessary, read.table knows how to handle whitespace delimiters, no whitespace separated enteries means this is fine
riverData <- read.table("http://www.statsci.org/data/oz/nzrivers.txt", header=TRUE)
head(riverData)

attach(iris)
meanSepalLength <- tapply(Sepal.Length, Species, mean)
meanSepalStdDev <- tapply(Sepal.Length, Species, sd)
tbl <- round(cbind(meanSepalLength, meanSepalStdDev),2)
tbl
