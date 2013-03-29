ragel
=====

Maven plug-in for [*Ragel*](http://www.complang.org/ragel/) and [*RagelX*](http://github.com/morandat/ragelx)

Currently this simple maven plugin take care of generating files with `.rl` and `.rlx` extensions. It does not recompile untouched files.

Supported phases:

  * compile
  * clean

Maven Coordinates

    <build>
      <plugins>
        <plugin>
          <groupId>fr.labri.ragel</groupId>
          <artifactId>ragel</artifactId>
          <!-- A ''stable'' 0.1 version is also avialable -->
          <version>0.2-SNAPSHOT</version>
        </plugin>
      </plugins>
    </build>

You can also ask to use the RagelX feature by adding an explicit dependency on it.

    <build>
      <plugins>
        <plugin>
          <groupId>fr.labri.ragel</groupId>
          <artifactId>ragel</artifactId>
          <version>0.2-SNAPSHOT</version>

          <dependencies>
            <dependency>
              <groupId>fr.labri.ragel</groupId>
              <artifactId>ragelx</artifactId>
              <version>0.2-SNAPSHOT</version>
            </dependency>
          </dependencies>
        </plugin>
      </plugins>
    </build>
