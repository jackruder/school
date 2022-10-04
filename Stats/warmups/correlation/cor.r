#1.
data(trees)
m <- lm(Girth~Height, data=trees)
summary(m)
#The fitted model is Girth = 0.256 * height - 6.188
# P value of the slope is 0.00276, this is significant

#2. This model might be useful in understanding how much wood could be cut per length of tree.

#3. Girth is positively correlated with height. The R^2 is 0.2697. This says that about a quarter of the variability is explained by the linear model (so not very good).

conf <- predict(m, data.frame(Height=c(10,15,20)), int="confidence")
pred <- predict(m, data.frame(Height=c(10,15,20)), int="prediction")
# we see that the confidence intervals are about 21.2, 19.6, 18.01 inches wide respectively
# the prediction intervals are about 23.95, 22.55, and 21.18 inches wide respectively.
# We see the wider prediction intervals since there is a large range of values, but we expect with 95% confidence that predicted data should center a bit closer aronud the mean.
