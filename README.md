# FileGlob
Java File Glob snobol-ish matcher and regex converter with quoting.

File Glob pattern matcher. Does not use regex internally, instead it compiles the pattern into a list of anchors.

It's nice java 8 has java.nio.file.PathMatcher, but what about if you want to be compatible with older versions of java?

Patterns are composed of * and ?. Use a ? to match exactly one arbitrary character. The regex equivalent is ..

Use a * to match any number of characters, including none. The regex equivalent is .*.

Use a \ to escape ? * or backslash itself. however, a backslash followed by any other character will be exactly that,
a backslash and the other character.
