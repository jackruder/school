students <- read.csv("http://mathcs.pugetsound.edu/~jrprice/uploads/students.csv")
View(students)
head(students)

# 1. No, because n/a entries exist
nrow(students)
#2. 112
nrow(subset(students, Height >= 72))
#3. 22
nrow(subset(students, Height >= 72 & Gender=="Female"))
#4. None


#5. An interesting question would be the relationship between gpa and different habits (shower, sleep, etc.,)
