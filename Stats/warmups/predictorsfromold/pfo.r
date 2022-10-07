data(co2)

plot(co2)

df <- read.csv("../../data/maunaLoa.csv")
m <- lm(co2~monthsSince1959, data=df)
summary(m)
# model has a very low p varue for both parameters, indicating high statistical significance. 96.95 percent of the variation is explained by the model

m2 <- lm(co2~monthsSince1959 + I(sin(monthsSince1959 / 12 * 2 * pi)), data=df)
summary(m2)
# again, strong statistical significance in this model. We see though that the model has a higher R^2, indicating that 98.67 percent of the variation in CO2 is explained, 1.7 percent higher than without the nonlinear term. We interperate the coefficient on the sin function as its amplitude.

