bears <- read.csv("bears.csv")
attach(bears)

#1.
plot(WEIGHT~LENGTH)
#a  This does not appear linear, we shoud apply a log transformation.
#b. 
lattice::densityplot(LENGTH)
# Doesn't seem skewed

lattice::densityplot(WEIGHT)
#this is definitely skewed right 

plot(log(WEIGHT) ~ LENGTH, data=bears)
#definitely looks linear, lets try a fit
lm1 <- lm(log(WEIGHT) ~ LENGTH, data=bears)
# now lets evaluate
plot(lm1$fit, lm1$resid)
# no observable pattern, a good sign

hist(lm1$resid, breaks=(10)
# these residuals seem to be lacking on the low end with standard binning, adding more bins things look a bit more normal 

qqnorm(lm1$resid)
qqline(lm1$resid)
#these plots indicate that the fit on the log transform is quite good.



#2.
plot(LENGTH~AGE)
#a  This does not appear linear, we shoud apply a log transformation. (again)
#b. 
lattice::densityplot(LENGTH)
# Doesn't seem skewed (again)

lattice::densityplot(AGE)
#this is definitely skewed right (again)

plot(LENGTH ~ log(AGE), data=bears)
#definitely looks linear, lets try a fit
lm2 <- lm(LENGTH ~ log(AGE), data=bears)
# now lets evaluate
plot(lm2$fit, lm2$resid)
# no observable pattern, a good sign

hist(lm2$resid, breaks=(10))
#Here, the residuals aren't quite following a normal distribution, there are some spikes in the middle but it doesn't look great

qqnorm(lm2$resid)
qqline(lm2$resid)
#we can se here that the tails diverge a bit, so the linear fit on the log transform is not great.



plot(LENGTH ~ log(log(AGE)), data=bears)
lm3 <- lm(LENGTH ~ log(log(AGE)), data=bears)
plot(lm3$fit, lm3$resid)
hist(lm3$resid, breaks=(10))
#these look a bit better

qqnorm(lm3$resid)
qqline(lm3$resid)
#here, the tails still exist but the fit on the extremes is much better. Most likely single linear regression is the wrong choice in fitting this model due to the extreme measures of obtaining a good fit. 
