all: setup
	scalac -cp classes -d classes/ -make:transitive -deprecation -dependencyfile classes/.scala_dependencies `find src/main/scala/ -name "*.scala"`

setup:
	@ test -d classes || mkdir classes


onlyrun:
	@ scala -cp classes epfl.pacman.Main 2>&1 | tee last.log

run: all onlyrun
