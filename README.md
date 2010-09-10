# Programmable Pacman

## Intorduction

The idea of this project is to introduce kids to programming in a playful
and fun way.

The Pacman in this game is not controlled by user-input but by a "behavior"
program that has to be written by the player. Such behaviors are written in
a simple and intuitive (and, for the moment, French :) DSL. New behaviors
can be compiled and loaded at run-time to gradually make Pacman smarter.

The game has two modes:

 - Simple mode: Pacman has to escape the monsters for 30 seconds to win
 - Advanced mode: Pacman can eat points, eat superpoints (cherries)
   and in super-mode eat monsters. The goal is to eat all points and / or
   to gain a maximum number of points.


The game has been very successfully tested during the [Open Doors 2010 at
EPFL][1]. A lot of kids (starting from the age of 8) spent a surprisingly
long time trying to improve Pacmans intelligence, and they had a lot of
fun. Some [impressions][2].

[1]: http://objectifsciences.epfl.ch/
[2]: http://picasaweb.google.com/lukas.rytz/EpflPortesOuvertes#



### How to run

 - install [sbt](http://code.google.com/p/simple-build-tool/)
 - the first time, run "sbt update"
 - run "sbt run"



### Documentation

The DSL is described and explained in the file doc/documentation.pdf
(currently only in French, contributions welcome).



### Contribute

There is a lot of space for contributions! For bugfixes, see the list
of open [tickets](http://github.com/soundrabbit/pacman/issues)

For new features, see the list below.



## Ideas for improvements


### clean up MVC

iir, some parts of the GUI which are currently part of the View could be
seperate from the MVC.


### translate DSL to english


### enrich the language

1. filters like "versUneCerise" or "versUnMonstre" only return one direction.
when one writes "versUnMonstre telQue loinDesCerises", and the two are not
the same, then she gets no direction (pacman stops).

    if the filters would instead return a sequence of directions (the first
being the best), we could allow multiple filters.

2. there should be a new filter allowing to avoid things, such as
"pasDeMonstreSurChemin", "pasDeCeriseSurChemin"

3. we should give access to more data in the model
    - distance to all monsters, cherries, points (not only to the closest)
    - number of lives
    - time remaining as a hunter
    - number of cherries
    - number monsters alive
    - length of tunnel (i.e distance to next intersection)

4. we should allow arbitrary direction filters, for example:
bouge telQue (distanceVersCerise < 3) // restricts to directions where distance
to cherry is less than 3

    This is (almost) equivalent to
si (distanceVersCerise < 3) (bouge vers versUneCerise) sinon (reste)



### time

in simple mode, the time to survive (30s) does not depend on the animation
speed (Settings.sleepTime). so for slower animation it gets easier. it should
rather be a certain number of big ticks.



### error reporting

error reporting is often not helpful, for instance if you miss-spell an
operaotr such as "telQue", the receiver will be highlighted, not the
operator. also, always an entire line is marked as wrong (positions are
just mapped to lines..)

Also if we had automatic line-wrapping and indentation, error correction of
parenthesis would become a lot easier (this was an issue during objective
science, with people spending lots of time counting parenthesis, and not enjoying
it greatly).

also: have the code become "green" based on running a presentation compiler instead
instead of simple regexes.



### relax on spelling

for the kids, the pacman language should forgive more errors like
 - not using camelCase
 - have more aliases ("bouge vers" = "aller vers") // Gilles is not sure about more aliases: I found the vers/telQue alias to be confusing already
 - maybe even allow space-separated directions ("vers un monstre" =
   "versUnMonstre")



### active debugging

by viktor kuncak: For learning to program, one useful addition might
be to have a replay debugger, where one could see which conditions
evaluated to 'true' and which actions were executed by the pacman. If
all randomness was given by a pseudo-random generator, a (bounded)
replay should not be too hard and should be reasonably efficient, since
there is a discrete notion of time. To see which part was executed,
it seems sufficient to add to DSL a comment combinator, of type

    comment : String => T => T

such that (comment "Foo" action) behaves just as 'action' but stores
"Foo" on a stack that is used to explain which actions got triggered.


Also, we could have a mode that displays some of the info being used 
internally to determine directions, for example an arrow through the path
to the closest monster or cherry, or highlighting cells that are not 
"enSéuritéendant(x)".



### fairness

the number of cherries, and maybe also their location, should  be fixed,
otherwise one needs to be just very lucky to get a high score.



### doc

there should be a documentation which explains more step-by-step, so that
people can actually start solving the thing by themselves, without somebody
explaining them ("bogue", then "bouge vers direction", then "siMonstresLoin",
then, "siChasseur", then "telQue enSecuritePendant", ...)



### multi-player

by viktor kuncak: some sort of multiplayer version where two (or more) can
code "against" each other could create a potentially infinite amount of
enthusiasm.



### multi-player 2

There should be an internet high-score system, so that people can send in their
high score and display it on the web.



### imporved text editor

automatic indention and highlighting matching paranthesis would help.
also, undo with "Control-Z" does not work.



### speed adjustment

a slider allowing to adjust the game speed (simple to implement,
Settings.sleepTime). For small delayTimes, we should reduce the frame
rate (re-compute the frames only in every second iteration)



### memory buttons

a set of 3 or 5 memory locations where users can store their code and re-load
it later (just in memory is fine)



### packaging

 - use proguard to create a single .jar
 - applet



## Solutions

### make a lot of points

    siChasse (
      si (distanceVersCerise > 1) (
        bouge telQue enSecuritePendant(3)
        telQue versUneCerise
        sinon
        bouge telQue enSecuritePendant(3)
        telQue versUnPoint
        sinon
        bouge telQue loinDesMonstres
      ) sinon (
        si (distanceVersMonstre < 4) (
          bouge telQue versUneCerise
        ) sinon reste
      )
    ) sinon (
      si ((distanceVersCerise < 5 &&
            distanceVersMonstre > 8) ||
           distanceVersMonstre > 12 ||
           distanceVersCerise < 2) (
        bouge telQue versUneCerise
        sinon
        bouge telQue versUnMonstre
      ) sinon (
        bouge telQue versUnMonstre
      )
    )

## Credits

This program has been written by Gilles Dubochet, Etienne Kneuss
and Lukas Rytz.