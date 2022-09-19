######
# author: Jake Price
# date: 9/14
# Fit a Linear Model to the relationship between sepal length and petal length
# for the setosa species
# Plots:
# 	Sepal Length ~ Petal Length
# 	- Scatter w/ line of best fit
# 	- Residual plot vs model
# 	- Histogram of residuals
# 	- QQ plot
#######


#load and attach setosa dataset 
setosa = subset(iris, Species == "setosa")
attach(setosa)

#display initial data, show line of best fit through scatter plot
lattice::xyplot(Sepal.Length ~ Petal.Length, type = c("p","r"))

#fit a model, view summary
sModel = lm(Sepal.Length~Petal.Length)
summary(sModel)

#scatter the residuals against the fitted values
plot(sModel$fitted.values,sModel$residuals)
# No observable pattern here, other than that the data is centered around a fitted value of 5.0

#view the residuals
hist(sModel$residuals)
# Residuals are skew right, suggesting a bad fit

#make a QQ plot
qqnorm(sModel$residuals)
qqline(sModel$residuals)
#Show a line too, we see a divergence from the line 
# suggesting a bad fit.


