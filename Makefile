all: setup
	scalac -cp classes -d classes/ -make:transitive -deprecation -dependencyfile classes/.scala_dependencies `find src/main/scala/ -name "*.scala"`

setup:
	@ test -d classes || mkdir classes

run: all
	@ scala -cp classes scala.epfl.pacman.Main
