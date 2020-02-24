all: 
	javac DNSlookup.java
	jar cvfe DNSlookup.jar DNSlookup *.class

clean:
	rm -f *.class
	rm -f DNSlookup.jar
