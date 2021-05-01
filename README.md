# Design Pattern Doc

----
## What is Design Pattern Doc?

 The Design Pattern Doc is a plugin that helps understanding software in terms of its design patterns, by reducing
 the mutual feedback loop between programming and documenting.

 It contains the following features:
  1. Highlights and suggests documentation for source-code objects that play a role in a detected pattern instance
     (Design patterns are detected with the help of the DP-CORE tool, which we do not own).
  2. Displays pattern hints at the end of the lines where documented pattern participants are found (Object → PatternName:Role).
  3. UML pattern instance representation by hovering on top of documented pattern participants. The UML is generated
     by plantuml tool, which we do not own.
  4. Manual creation and edition of pattern instance's documentation.
  5. Definition of other design patterns, not initially supported by the plugin. These definitions will not be used in
     the automatic detection of design patterns by the DP-CORE.

Note: Graphviz should be installed in the OS to take full advantage of the UML class diagrams

----
## Usage

For plugin developers:

 1. Open Intellij → File → New → Project From Version Control → your branch
 2. Use gradle to run the plugin:
 `./gradlew run`
 3. Or use it to build the Intellij plugin package: `./gradlew buildPlugin`
 

For developers:

* The plugin should be available soon in the 
[Intellij marketplace](https://plugins.jetbrains.com/plugin/14102-design-pattern-doc). For the time being, you can 
install the [release that we make available](https://github.com/lemosf-ppb/Design-Pattern-DocUserStudy/releases) in this 
GitHub repository.

