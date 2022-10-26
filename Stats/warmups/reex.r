df <- read.csv("../data/woodsTrees.csv")

# produce anova diagnostic plot
m <- tapply(df$Diameter, df$Trt, mean)
s <- tapply(df$Diameter, df$Trt, sd)
plot(s,m)

# plot shows slope of about one, should be reexpressed with a log transform
(logM = aov(log(df$Diameter)~df$Trt))
(m = aov(df$Diameter~df$Trt))

par(mfrow=c(1,2))
qqnorm(logM$resid)
qqnorm(m$resid)

library(agricolae)
(out <- LSD.test(logM))


TukeyHSD(logM)
