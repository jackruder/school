###################################################
# Author: Jack Ruder
# Oct 25, 2022
# Project1: Predict gas milage using multiple linear regression, using the mtcars dataset
###################################################

# load data
data(mtcars)
knitr::kable(mtcars,"latex")

#    A data frame with 32 observations on 11 (numeric) variables.

#      [, 1]  mpg   Miles/(US) gallon                        
#      [, 2]  cyl   Number of cylinders                      
#      [, 3]  disp  Displacement (cu.in.)                    
#      [, 4]  hp    Gross horsepower                         
#      [, 5]  drat  Rear axle ratio                          
#      [, 6]  wt    Weight (1000 lbs)                        
#      [, 7]  qsec  1/4 mile time                            
#      [, 8]  vs    Engine (0 = V-shaped, 1 = straight)      
#      [, 9]  am    Transmission (0 = automatic, 1 = manual) 
#      [,10]  gear  Number of forward gears                  
#      [,11]  carb  Number of carburetors                    


library(GGally)
# inspect distributions and correlations
png("corrplot.png", width=8, height=8, units='in', res=150)
ggpairs(mtcars, title="mtcars Dataset Distribution and Correlation") + theme_bw()
dev.off()
# variables to log transform: mpg, disp, hp, drat,carb
# vars to closely inspect (possible outliers?): qsec wt, ,
# ordering of vars from most to least correlated: wt, cyl, disp, hp, drat, vs, am, carb, gear, qsec

#apply transformations
library('gridExtra')
gMpg <- lattice::densityplot(~log(mtcars$mpg)) # appears more normal
gDisp <- lattice::densityplot(~log(mtcars$disp)) # appears more normal
gHp <- lattice::densityplot(~log(mtcars$hp)) # appears more normal
gDrat <- lattice::densityplot(~log(mtcars$drat)) # slightly better, good enough that it should be transformed
gCarb <- lattice::densityplot(~log(mtcars$carb)) # more normal



gQsec <- lattice::densityplot(~log(mtcars$qsec)) # transform not too useful, there's just an outlier.
lattice::densityplot(~mtcars$wt) # looks like there is just a gap in the data from 4-5, there is clumping towards 4. We should try a transformation
gWt <- lattice::densityplot(~log(mtcars$wt)) # much better. Still a grouping at the edge but appears more normal.

#png("transformedDensities.png", width=8, height=8, units='in', res=150)
#grid.arrange(gMpg, gDisp, gHp, gDrat, gCarb, gQsec, gWt, ncol=4)
#dev.off()

# Thus, we will transform mpg, disp, hp, drat,carb and wt
library(car)
modall <- lm(log(mpg)~log(wt) + cyl + log(disp) + log(hp) + log(drat) + vs + am, data=mtcars)
summary(modall)
vif(modall) # looks like log(disp) is highly correlated with something else. 
#We will drop log(disp) for sure then.

# let's remove log(disp), and compare with the full model
mod2 <- lm(log(mpg)~log(wt) + cyl + log(hp) + log(drat) + vs + am, data=mtcars)
summary(mod2) # drop in multiple R-squared of 0.001. 
anova(mod2, modall) # nested F test gives F=0.75, Pr(>F) = 0.396. We will drop log(disp) for sure then.

#check VIF again
vif(mod2) # worst offender is cyl. repeat the above process.

mod3 <- lm(log(mpg)~log(wt) + log(hp) + log(drat) + vs + am, data=mtcars)
summary(mod3) # drop in multiple R-squared of 0.0016. 
anova(mod3, mod2) # nested F test gives F=0.34, Pr(>F) = 0.566. We should drop cyl from the model.

# check VIF
vif(mod3) # we should be happy with a VIF below 10 for all predictors.
anova(mod3) # it seems,log(drat), vs, am,contribute to a minimal portion of the error. We will drop them as terms.

mod5 <- lm(log(mpg)~log(wt) + log(hp), data=mtcars)
summary(mod5) # gave small multiple R squared decrease, but an increase in the adjusted r squared.
anova(mod5,mod3) # nested F test shows this is good, p=0.9608=

#lets compare to the original model,
anova(mod5, modall) # p = 0.9893, accept the null hypothesis that modall is not better than mod5
vif(mod5) # minimal collinearity
summary(mod5) # extremely significant, 3.14 e^-14
anova(mod5)
 
library(ggplot2)
ggplot(mtcars, aes(y=log(mpg),x=log(wt),color=log(hp))) + geom_point(size=3) + stat_smooth(method="lm")

# linear model diagnostic plots
png('diag.png', width=9, height=3, units='in', res=150)
par(mfrow=c(1,3))
plot(mod5$resid~mod5$fitted)
hist(mod5$resid)
qqnorm(mod5$resid)
qqline(mod5$resid)
dev.off()
# quite normal, though there are still a few unaccounted extreme outliers.

 
#
confint(mod5)

# compact is less than 2500
compact <- subset(mtcars, wt<2.5)
# midsize is between 2500 and 4000
midsize <- subset(mtcars, wt >= 2.5 & wt <= 4)
#large is above 4000
large <- subset(mtcars, wt > 4)


# prediction intervals
exp(sapply(data.frame(predict(mod5, newdata=compact, int='prediction')),mean))
exp(sapply(data.frame(predict(mod5, newdata=midsize, int='prediction')),mean))
exp(sapply(data.frame(predict(mod5, newdata=large, int='prediction')),mean))

# confidence intervals
exp(sapply(data.frame(predict(mod5, newdata=compact, int='confidence')),mean))
exp(sapply(data.frame(predict(mod5, newdata=midsize, int='confidence')),mean))
exp(sapply(data.frame(predict(mod5, newdata=large, int='confidence')),mean))

