################
# Author: Jack Ruder
# date: Oct 03, 2022
# Warm-Up: Comparing two regression lines
###################

attach(iris)

lattice::xyplot(Petal.Length~Sepal.Length, data=iris, groups=Species, xlab="Sepal Length", ylab="Petal Length", type=c("p", "r"), auto.key=TRUE)

# PETAL = B0 + B1*SEPAL + I(versicolor) (B2 + B3*SEPAL) + I(virginica) (B4 + B5*SEPAL)
m <- lm(iris$Petal.Length~iris$Sepal.Length + iris$Species + iris$Sepal.Length:iris$Species)
summary(m)
#PETAL = 0.803 + 0.132 * SEPAL + I(versicolor) (-0.618 + 0.555 * SEPAL) + I(virginica) (-0.193 + 0.618 * SEPAL)
# Looking at the p values, the conditions are quite good, since there is a very high statistical significance in the slopes. For Sepal Lengths close to 0, the predictions would be bad given a lack of statistical significance in the intercepts.
