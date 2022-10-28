########
# author: Jack Ruder
# date: Sep 21, 2022
# 
# Stats260 warmup: Inference for Regression slope
########


# A. mine was 0.02, guess I'm a skeptic.

#B. load data, construct lm
data(iris)
setosa <- subset(iris, Species=="setosa")
m <- lm(setosa$Petal.Length~setosa$Sepal.Length)

summary(m)
#a. p value of 0.06

#b.
confint(m, 1, level=0.95)
#=> 2.5%: 0.112, 97.5%: 1.494

