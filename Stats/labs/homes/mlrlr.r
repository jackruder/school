################
# author: jack ruder
# date: oct 06, 2022
# multiple linear regression lab report iii
###############################
datadir <- "~/School/Stats/data"
filename <- "kc_house_data.csv"
sep <- "/" # posix

datafile <- paste(datadir, filename, sep=sep)
df <- read.csv(datafile)

#convert yyyymmdd to years since 2014
parsedate <- function(x) {
	yr <- strtoi(substr(x, 1,4), base=10L)
	month <- strtoi(substr(x, 5,6), base=10L)
	d <- strtoi(substr(x, 7,8), base=10L)
	return (yr - 2014 + (month / 12.0) + (d/365.0))
}

notzero <- function(x){ # 0 if 0, 1 if not zero
	return (ifelse(x==0,0,1))
}

inczero <- function(x, e) { # add a tiny value of e to only zero values
	return (ifelse(x==0,e,x))
}

df$date <- parsedate(df$date) ## make date numeric
names(df)[names(df)=="date"] <- "daten"
df$hasView <- notzero(df$view) # create binary indicator of whether or not a view is present
df$isRenovated <- notzero(df$yr_renovated)
df$age <- df$daten + 2014 - df$yr_built
df$ageRenovated <- df$daten + 2014 - df$yr_renovated

logdf <- sapply(df, log) # log all data
colnames(logdf) <- paste0(colnames(df), "_log")

ihs <- function(x) log(x + sqrt(1 + x^2))
ihsdf <- sapply(df, ihs) # ihs all data
colnames(ihsdf) <- paste0(colnames(df), "_ihs")

df <- cbind(df,logdf,ihsdf) # combine into one dataframe



plot.lm <- function(x, y, xlab, ylab) {
	par(mfrow=c(2,2))
	plot(x, y, xlab=xlab, ylab=ylab)
	model <- lm(y~x)
	abline(model)
	summary(model)

	#diagnostic plots
	plot(model$fitted, model$resid, xlab="fitted values", ylab="residuals")
	hist(model$resid, xlab="residuals", main="residual distribution")
	# slight left skew in the residuals
	qqnorm(model$reqid)
	qqline(model$resid)
}

plotdensity  <- function(col, data, colname) {
	pname <-paste("density plot of",colname,sep=" ")
mod2 <- lm(price_log ~ bedrooms_ihs * bathrooms_ihs * sqft_living_log * sqft_lot_log * (floors_log + sqft_above + sqft_basement + (condition*grade) + (waterfront * hasView * view) + (daten * age * isRenovated * ageRenovated) + (lat + long) + (sqft_living15_log * sqft_lot15_log)), na.action=na.exclude, data=df)
	lattice::densityplot(~col, data=data, main=pname, xlab=colname)
}

res_x <- 1080
res_y <- 1080

mod <- lm(price_log ~ bedrooms_ihs * bathrooms_ihs * sqft_living_log * sqft_lot_log * (floors_log + sqft_above + sqft_basement + (condition*grade) + (waterfront * hasView * view) + (daten * age * isRenovated * ageRenovated) + (lat + long) + (sqft_living15_log * sqft_lot15_log)), na.action=na.exclude, data=df)
par(mfrow=c(1,3))
hist(mod$resid,main="Model Residuals", xlab="Residual")
plot(mod$resid~mod$fitted,main="Model Residuals against Fitted Values", xlab="Model Fitted Values", ylab="Model Residuals")
qqnorm(mod$resid)
qqline(mod$resid)

mod2 <- lm(price_log ~
bedrooms_ihs                                                                +
bathrooms_ihs                                                               +
sqft_living_log                                                             +
sqft_lot_log                                                                +
floors_log                                                                  +
sqft_above                                                                  +
sqft_basement                                                               +
condition                                                                   +
grade                                                                       +
waterfront                                                                  +
hasView                                                                     +
view                                                                        +
daten                                                                       +
age                                                                         +
isRenovated                                                                 +
ageRenovated                                                                +
lat                                                                         +
long                                                                        +
sqft_living15_log                                                           +
sqft_lot15_log                                                              +
bedrooms_ihs:bathrooms_ihs                                                  +
bathrooms_ihs:sqft_living_log                                               +
bedrooms_ihs:sqft_lot_log                                                   +
condition:grade                                                             +
daten:age                                                                   +
daten:isRenovated                                                           +
age:isRenovated                                                             +
sqft_living15_log                                                           +
sqft_lot15_log                                                              +
bedrooms_ihs:floors_log                                                     +
bedrooms_ihs:sqft_above                                                     +
bedrooms_ihs:sqft_basement                                                  +
bedrooms_ihs:hasView                                                        +
bedrooms_ihs:age                                                            +
bedrooms_ihs:long                                                           +
bedrooms_ihs:sqft_living15_log                                              +
bathrooms_ihs:floors_log                                                    +
bathrooms_ihs:condition                                                     +
bathrooms_ihs:hasView                                                       +
bathrooms_ihs:age                                                           +
bathrooms_ihs:isRenovated                                                   +
bathrooms_ihs:lat                                                           +
bathrooms_ihs:long                                                          +
bathrooms_ihs:sqft_living15_log                                             +
bathrooms_ihs:sqft_lot15_log                                                +
sqft_living_log:sqft_basement                                               +
sqft_living_log:grade                                                       +
sqft_living_log:isRenovated                                                 +
sqft_living_log:lat                                                         +
sqft_living_log:long                                                        +
sqft_lot_log:floors_log                                                     +
sqft_lot_log:sqft_above                                                     +
sqft_lot_log:sqft_basement                                                  +
sqft_lot_log:condition                                                      +
sqft_lot_log:grade                                                          +
sqft_lot_log:waterfront                                                     +
sqft_lot_log:hasView                                                        +
sqft_lot_log:age                                                            +
sqft_lot_log:isRenovated                                                    +
sqft_lot_log:lat                                                            +
sqft_lot_log:long                                                           +
sqft_lot_log:sqft_lot15_log                                                 +
bedrooms_ihs:sqft_living_log:sqft_lot_log                                   +
bathrooms_ihs:sqft_living_log:sqft_lot_log                                  +
daten:age:ageRenovated                                                      +
daten:isRenovated:ageRenovated                                              +
bedrooms_ihs:condition:grade                                                +
bedrooms_ihs:daten:age                                                      +
bedrooms_ihs:sqft_living15_log:sqft_lot15_log                               +
bathrooms_ihs:daten:isRenovated                                             +
bathrooms_ihs:sqft_living15_log:sqft_lot15_log                              +
bedrooms_ihs:bathrooms_ihs:condition                                        +
bedrooms_ihs:bathrooms_ihs:age                                              +
bedrooms_ihs:bathrooms_ihs:lat                                              +
bedrooms_ihs:bathrooms_ihs:long                                             +
bedrooms_ihs:sqft_living_log:floors_log                                     +
bedrooms_ihs:sqft_living_log:sqft_above                                     +
bedrooms_ihs:sqft_living_log:condition                                      +
bedrooms_ihs:sqft_living_log:long                                           +
bathrooms_ihs:sqft_living_log:floors_log                                    +
bathrooms_ihs:sqft_living_log:sqft_basement                                 +
bathrooms_ihs:sqft_living_log:grade                                         +
bathrooms_ihs:sqft_living_log:view                                          +
bathrooms_ihs:sqft_living_log:daten                                         +
bathrooms_ihs:sqft_living_log:age                                           +
bathrooms_ihs:sqft_living_log:ageRenovated                                  +
bathrooms_ihs:sqft_living_log:long                                          +
bathrooms_ihs:sqft_living_log:sqft_living15_log                             +
bathrooms_ihs:sqft_living_log:sqft_lot15_log                                +
sqft_lot_log:condition:grade                                                +
sqft_lot_log:daten:isRenovated                                              +
sqft_lot_log:daten:ageRenovated                                             +
sqft_lot_log:age:ageRenovated                                               +
sqft_lot_log:sqft_living15_log:sqft_lot15_log                               +
bedrooms_ihs:sqft_lot_log:floors_log                                        +
bedrooms_ihs:sqft_lot_log:sqft_above                                        +
bedrooms_ihs:sqft_lot_log:condition                                         +
bedrooms_ihs:sqft_lot_log:waterfront                                        +
bedrooms_ihs:sqft_lot_log:age                                               +
bathrooms_ihs:sqft_lot_log:sqft_basement                                    +
bathrooms_ihs:sqft_lot_log:grade                                            +
bathrooms_ihs:sqft_lot_log:view                                             +
bathrooms_ihs:sqft_lot_log:age                                              +
bathrooms_ihs:sqft_lot_log:isRenovated                                      +
bathrooms_ihs:sqft_lot_log:lat                                              +
bathrooms_ihs:sqft_lot_log:long                                             +
sqft_living_log:sqft_lot_log:floors_log                                     +
sqft_living_log:sqft_lot_log:sqft_basement                                  +
sqft_living_log:sqft_lot_log:condition                                      +
sqft_living_log:sqft_lot_log:isRenovated                                    +
sqft_living_log:sqft_lot_log:lat                                            +
sqft_living_log:sqft_lot_log:sqft_lot15_log                                 +
bedrooms_ihs:bathrooms_ihs:sqft_living_log:sqft_lot_log                     +
daten:age:isRenovated:ageRenovated                                          +
bedrooms_ihs:bathrooms_ihs:condition:grade                                  +
bedrooms_ihs:bathrooms_ihs:daten:age                                        +
bedrooms_ihs:bathrooms_ihs:age:isRenovated                                  +
sqft_living_log:daten:age:isRenovated                                       +
bedrooms_ihs:sqft_living_log:condition:grade                                +
bedrooms_ihs:sqft_living_log:waterfront:view                                +
bathrooms_ihs:sqft_living_log:condition:grade                               +
bathrooms_ihs:sqft_living_log:age:isRenovated                               +
bathrooms_ihs:sqft_living_log:daten:ageRenovated                            +
bathrooms_ihs:sqft_living_log:sqft_living15_log:sqft_lot15_log              +
bedrooms_ihs:bathrooms_ihs:sqft_living_log:sqft_above                       +
bedrooms_ihs:bathrooms_ihs:sqft_living_log:waterfront                       +
bedrooms_ihs:bathrooms_ihs:sqft_living_log:long                             +
bedrooms_ihs:bathrooms_ihs:sqft_living_log:sqft_living15_log                +
bedrooms_ihs:bathrooms_ihs:sqft_living_log:sqft_lot15_log                   +
bedrooms_ihs:sqft_lot_log:sqft_living15_log:sqft_lot15_log                  +
bathrooms_ihs:sqft_lot_log:condition:grade                                  +
bathrooms_ihs:sqft_lot_log:age:isRenovated                                  +
bedrooms_ihs:bathrooms_ihs:sqft_lot_log:floors_log                          +
bedrooms_ihs:bathrooms_ihs:sqft_lot_log:daten                               +
bedrooms_ihs:bathrooms_ihs:sqft_lot_log:ageRenovated                        +
bedrooms_ihs:bathrooms_ihs:sqft_lot_log:long                                +
bedrooms_ihs:bathrooms_ihs:sqft_lot_log:sqft_lot15_log                      +
sqft_living_log:sqft_lot_log:age:isRenovated                                +
sqft_living_log:sqft_lot_log:sqft_living15_log:sqft_lot15_log               +
bedrooms_ihs:sqft_living_log:sqft_lot_log:floors_log                        +
bedrooms_ihs:sqft_living_log:sqft_lot_log:sqft_above                        +
bedrooms_ihs:sqft_living_log:sqft_lot_log:age                               +
bathrooms_ihs:sqft_living_log:sqft_lot_log:floors_log                       +
bathrooms_ihs:sqft_living_log:sqft_lot_log:grade                            +
bathrooms_ihs:sqft_living_log:sqft_lot_log:lat                              +
bathrooms_ihs:sqft_living_log:sqft_lot_log:long                             +
bedrooms_ihs:bathrooms_ihs:sqft_living_log:daten:ageRenovated               +
bedrooms_ihs:bathrooms_ihs:sqft_lot_log:daten:age                           +
bedrooms_ihs:bathrooms_ihs:sqft_lot_log:daten:isRenovated                   +
bedrooms_ihs:bathrooms_ihs:sqft_lot_log:sqft_living15_log:sqft_lot15_log    +
bedrooms_ihs:sqft_living_log:sqft_lot_log:age:ageRenovated                  +
bathrooms_ihs:sqft_living_log:sqft_lot_log:sqft_living15_log:sqft_lot15_log +
bedrooms_ihs:bathrooms_ihs:sqft_living_log:sqft_lot_log:floors_log          +
bedrooms_ihs:bathrooms_ihs:sqft_living_log:sqft_lot_log:age                 +
bedrooms_ihs:bathrooms_ihs:sqft_living_log:sqft_lot_log:ageRenovated        +
bedrooms_ihs:bathrooms_ihs:sqft_living_log:sqft_lot_log:daten:age           +
bedrooms_ihs:bathrooms_ihs:sqft_lot_log:daten:age:isRenovated:ageRenovated  +
bedrooms_ihs:bathrooms_ihs:sqft_living_log:sqft_lot_log:age:isRenovated:age, data=df, na.action=na.exclude)

mod3 <- lm(price_log~
bedrooms_ihs                                                                +
bathrooms_ihs                                                               +
sqft_living_log                                                             +
sqft_lot_log                                                                +
floors_log                                                                  +
sqft_above                                                                  +
sqft_basement                                                               +
condition                                                                   +
grade                                                                       +
waterfront                                                                  +
hasView                                                                     +
view                                                                        +
daten                                                                       +
age                                                                         +
isRenovated                                                                 +
ageRenovated                                                                +
lat                                                                         +
long                                                                        +
sqft_living15_log                                                           +
sqft_lot15_log                                                              +
bedrooms_ihs:bathrooms_ihs                                                  +
bathrooms_ihs:sqft_living_log                                               +
bedrooms_ihs:sqft_lot_log                                                   +
condition:grade                                                             +
daten:age                                                                   +
daten:isRenovated                                                           +
age:isRenovated                                                             +
bedrooms_ihs:floors_log                                                     +
bedrooms_ihs:sqft_above                                                     +
bedrooms_ihs:sqft_basement                                                  +
bedrooms_ihs:age                                                            +
bedrooms_ihs:long                                                           +
bedrooms_ihs:sqft_living15_log                                              +
bathrooms_ihs:floors_log                                                    +
bathrooms_ihs:condition                                                     +
bathrooms_ihs:hasView                                                       +
bathrooms_ihs:age                                                           +
bathrooms_ihs:isRenovated                                                   +
bathrooms_ihs:lat                                                           +
bathrooms_ihs:long                                                          +
bathrooms_ihs:sqft_living15_log                                             +
bathrooms_ihs:sqft_lot15_log                                                +
sqft_living_log:long                                                        +
sqft_lot_log:floors_log                                                     +
sqft_lot_log:sqft_above                                                     +
sqft_lot_log:sqft_basement                                                  +
sqft_lot_log:condition                                                      +
sqft_lot_log:grade                                                          +
sqft_lot_log:waterfront                                                     +
sqft_lot_log:hasView                                                        +
sqft_lot_log:age                                                            +
sqft_lot_log:isRenovated                                                    +
sqft_lot_log:lat                                                            +
sqft_lot_log:long                                                           +
sqft_lot_log:sqft_lot15_log                                                 +
daten:isRenovated:ageRenovated                                              +
bedrooms_ihs:condition:grade                                                +
bathrooms_ihs:daten:isRenovated                                             +
bedrooms_ihs:bathrooms_ihs:condition                                        +
bedrooms_ihs:bathrooms_ihs:age                                              +
bedrooms_ihs:bathrooms_ihs:lat                                              +
bedrooms_ihs:bathrooms_ihs:long                                             +
bedrooms_ihs:sqft_living_log:long                                           +
bathrooms_ihs:sqft_living_log:long                                          +
bathrooms_ihs:sqft_living_log:sqft_lot15_log                                +
bedrooms_ihs:sqft_lot_log:floors_log                                        +
bedrooms_ihs:sqft_lot_log:condition                                         +
bedrooms_ihs:sqft_lot_log:waterfront                                        +
bedrooms_ihs:sqft_lot_log:age                                               +
bathrooms_ihs:sqft_lot_log:grade                                            +
bathrooms_ihs:sqft_lot_log:age                                              +
bathrooms_ihs:sqft_lot_log:isRenovated                                      +
sqft_living_log:sqft_lot_log:floors_log                                     +
sqft_living_log:sqft_lot_log:sqft_basement                                  +
sqft_living_log:sqft_lot_log:condition                                      +
sqft_living_log:sqft_lot_log:isRenovated                                    +
sqft_living_log:sqft_lot_log:lat                                            +
sqft_living_log:sqft_lot_log:sqft_lot15_log                                 +
bathrooms_ihs:sqft_lot_log:age:isRenovated                                  +
bedrooms_ihs:bathrooms_ihs:sqft_lot_log:sqft_lot15_log                      +
bedrooms_ihs:sqft_living_log:sqft_lot_log:sqft_above                        +
bedrooms_ihs:sqft_living_log:sqft_lot_log:age                               +
bathrooms_ihs:sqft_living_log:sqft_lot_log:lat                              +
bathrooms_ihs:sqft_living_log:sqft_lot_log:sqft_living15_log:sqft_lot15_log +
bedrooms_ihs:bathrooms_ihs:sqft_living_log:sqft_lot_log:age                 +
bedrooms_ihs:bathrooms_ihs:sqft_lot_log:daten:age:isRenovated:ageRenovated, data=df, na.action=na.exclude)

mod4 <- lm(price_log~ 
bedrooms_ihs                                                              +
bathrooms_ihs                                                             +
sqft_living_log                                                           +
sqft_lot_log                                                              +
floors_log                                                                +
sqft_above                                                                +
sqft_basement                                                             +
condition                                                                 +
grade                                                                     +
waterfront                                                                +
hasView                                                                   +
view                                                                      +
daten                                                                     +
age                                                                       +
isRenovated                                                               +
ageRenovated                                                              +
lat                                                                       +
long                                                                      +
sqft_living15_log                                                         +
sqft_lot15_log                                                            +
bedrooms_ihs:bathrooms_ihs                                                +
bathrooms_ihs:sqft_living_log                                             +
bedrooms_ihs:sqft_lot_log                                                 +
condition:grade                                                           +
daten:age                                                                 +
daten:isRenovated                                                         +
age:isRenovated                                                           +
bedrooms_ihs:floors_log                                                   +
bedrooms_ihs:sqft_above                                                   +
bedrooms_ihs:sqft_basement                                                +
bedrooms_ihs:age                                                          +
bedrooms_ihs:long                                                         +
bedrooms_ihs:sqft_living15_log                                            +
bathrooms_ihs:floors_log                                                  +
bathrooms_ihs:condition                                                   +
bathrooms_ihs:hasView                                                     +
bathrooms_ihs:age                                                         +
bathrooms_ihs:isRenovated                                                 +
bathrooms_ihs:lat                                                         +
bathrooms_ihs:long                                                        +
bathrooms_ihs:sqft_living15_log                                           +
bathrooms_ihs:sqft_lot15_log                                              +
sqft_living_log:long                                                      +
sqft_lot_log:floors_log                                                   +
sqft_lot_log:sqft_above                                                   +
sqft_lot_log:sqft_basement                                                +
sqft_lot_log:condition                                                    +
sqft_lot_log:grade                                                        +
sqft_lot_log:waterfront                                                   +
sqft_lot_log:hasView                                                      +
sqft_lot_log:age                                                          +
sqft_lot_log:isRenovated                                                  +
sqft_lot_log:lat                                                          +
sqft_lot_log:long                                                         +
sqft_lot_log:sqft_lot15_log                                               +
daten:isRenovated:ageRenovated                                            +
bedrooms_ihs:condition:grade                                              +
bathrooms_ihs:daten:isRenovated                                           +
bedrooms_ihs:bathrooms_ihs:condition                                      +
bedrooms_ihs:bathrooms_ihs:age                                            +
bedrooms_ihs:bathrooms_ihs:lat                                            +
bedrooms_ihs:bathrooms_ihs:long                                           +
bedrooms_ihs:sqft_living_log:long                                         +
bathrooms_ihs:sqft_living_log:long                                        +
bathrooms_ihs:sqft_living_log:sqft_lot15_log                              +
bedrooms_ihs:sqft_lot_log:floors_log                                      +
bedrooms_ihs:sqft_lot_log:condition                                       +
bedrooms_ihs:sqft_lot_log:waterfront                                      +
bedrooms_ihs:sqft_lot_log:age                                             +
bathrooms_ihs:sqft_lot_log:grade                                          +
bathrooms_ihs:sqft_lot_log:age:isRenovated                                +
bathrooms_ihs:sqft_living_log:sqft_lot_log:lat                            +
bedrooms_ihs:bathrooms_ihs:sqft_living_log:sqft_lot_log:age, data=df, na.action=na.exclude)
bedrooms_ihs                                                                                                                                                 ─╯





mod5 <- lm(price_log~
bathrooms_ihs                                               +
sqft_living_log                                             +
sqft_lot_log                                                +
isRenovated                                                 +
bathrooms_ihs:sqft_living_log                               +
bathrooms_ihs:sqft_living_log:long                          +
bathrooms_ihs:long                                          +
floors_log                                                  +
condition                                                   +
grade                                                       +
waterfront                                                  +
hasView                                                     +
lat                                                         +
long                                                        +
sqft_living15_log                                           +
sqft_lot15_log                                              +
bedrooms_ihs:bathrooms_ihs                                  +
daten:isRenovated                                           +
bedrooms_ihs:floors_log                                     +
bedrooms_ihs:long                                           +
bathrooms_ihs:isRenovated                                   +
bathrooms_ihs:lat                                           +
bathrooms_ihs:long                                          +
bathrooms_ihs:sqft_living15_log                             +
sqft_lot_log:lat                                            +
bathrooms_ihs:daten:isRenovated                             +
bedrooms_ihs:bathrooms_ihs:lat                              +
bedrooms_ihs:bathrooms_ihs:long                             +
bathrooms_ihs:sqft_lo7t15_log                                +
sqft_living_log:long                                        +
bedrooms_ihs:sqft_living15_log                              +
bathrooms_ihs:sqft_living_log:long,data=df, na.action=na.exclude)

mod6 <- lm(price_log~
bathrooms_ihs                                               +
sqft_living_log                                             +
sqft_lot_log                                                +
isRenovated                                                 +
bathrooms_ihs:sqft_living_log                               +
floors_log                                                  +
condition                                                   +
grade                                                       +
waterfront                                                  +
hasView                                                     +
lat                                                         +
sqft_living15_log                                           +
sqft_lot15_log                                              +
bedrooms_ihs:bathrooms_ihs                                  +
bedrooms_ihs:floors_log                                     +
bathrooms_ihs:isRenovated                                   +
bathrooms_ihs:sqft_living15_log                             +
bathrooms_ihs:sqft_lot15_log,data=df, na.action=na.exclude)

mod7 <- lm(price_log~ 
	   bathrooms_ihs * 
	   (sqft_living_log +
	    bedrooms_ihs +
	    isRenovated +
	    sqft_living15_log +
	    sqft_lot15_log)+ 
	   bedrooms_ihs * floors_log + 
	   condition + 
	   grade + 
	   waterfront + 
	   hasView + 
	   lat, data=df, na.action=na.exclude)
summary(mod7)

library(car)
vif(mod7)
png("check.png", width=1440, height=420)
par(mfrow=c(1,3))
hist(mod7$resid,main="Model Residuals", xlab="Residual")
plot(mod7$resid~mod7$fitted,main="Model Residuals against Fitted Values", xlab="Model Fitted Values", ylab="Model Residuals")
qqnorm(mod7$resid)
qqline(mod7$resid)
dev.off()

nD <- data.frame(bathrooms_ihs=ihs(c(2)),sqft_living_log=log(c(1450)), bedrooms_ihs=ihs(c(3)), isRenovated=c(0),sqft_living15_log=log(c(1576)),sqft_lot15_log=log(c(9600)),floors_log=log(c(3)),condition=c(3),grade=c(8),waterfront=c(0),hasView=c(1),lat=c(47.5102))
predict(mod7, newdata=nD, int="prediction")
