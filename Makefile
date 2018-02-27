all: build 

build:
	javac ./*.java

run:
	java MyBot

clean:
	rm -rf *.class
