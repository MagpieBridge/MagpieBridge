
<p align="center">
<img src="https://github.com/MagpieBridge/MagpieBridge/blob/develop/doc/logshort.png" width="400">
</p> 

## What is MagpieBridge? [![Build Status](https://travis-ci.com/MagpieBridge/MagpieBridge.svg?branch=develop)](https://travis-ci.com/MagpieBridge/MagpieBridge)[![Gitter](https://badges.gitter.im/MagpieBridgeHelp/community.svg)](https://gitter.im/MagpieBridgeHelp/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

MapgieBridge is a framework for integrating Static Analyses into IDEs and Editors with the [Language Server Protocol](https://microsoft.github.io/language-server-protocol/specification). It is a part of the [FutureSoot](http://sable.github.io/soot/future-soot/) project. MagpieBridge offers a default implementation of a language server---`MagpieServer` which runs analysis; a default TextDocumentService--`MagpieTextDocumentService` which handles requests like didOpen, didChange, didSave; a default WorkspaceService--`MagpieWorkspaceService` which handles different kinds of code actions and many other useful APIs for static analysis. For more information, please read our paper:
[MagpieBridge: A General Approach to Integrating Static Analyses into IDEs and Editors](https://drops.dagstuhl.de/opus/volltexte/2019/10813/pdf/LIPIcs-ECOOP-2019-21.pdf) (ECOOP 2019).
A [Poster](https://linghuiluo.github.io/ECOOP19MagpieBridgePoster.pdf) is also available. 

<img src="https://github.com/MagpieBridge/MagpieBridge/blob/master/doc/goal.PNG"  width="800">

## Cite the research paper
For scientific usage, please **cite the paper** [[BibTex](https://drops.dagstuhl.de/opus/volltexte/2019/10813/)].

## Why is it called MagpieBridge?
In a Chinese legend, a human and a fairy fall in love, but this love angers the gods, who separate them
on opposite sides of the Milky Way. However, on the seventh day of the seventh lunar month each year,
thousands of magpies form a bridge, called 鹊桥 in Chinese and Queqiao in pinyin, allowing the lovers
to meet. We use MagpieBridge as a metaphor for a system that connects arbitrary static analysis to arbitrary IDE.

## Integrated Static Analyses using MagpieBridge
- [MagpieBridge-based CogniCrypt](https://github.com/MagpieBridge/CryptoLSPDemo)
- [MagpieBridge-based FlowDroid](https://github.com/MagpieBridge/FlowDroidLSPDemo)

## How to use MapgieBridge?
- [Check the tutorials to create your first project with MagpieBridge](https://github.com/MagpieBridge/MagpieBridge/wiki) :star2:**UPDATED**:star2: 

## Use MagpieBridge in your Maven project
You can either 
1. use the release by adding this to your `pom.xml` directly (see [github package](https://github.com/MagpieBridge/MagpieBridge/packages/62902?version=0.0.6)) 
````
<dependency>
  <groupId>magpiebridge</groupId>
  <artifactId>magpiebridge</artifactId>
  <version>0.0.6</version>
</dependency>

````

2. or build MagpieBridge by yourself 
    -  check out the develop branch with `git clone -b develop https://github.com/MagpieBridge/MagpieBridge.git`
    -  run `mvn install` in the project root directory to build the tool and run all tests. To skip tests, run `mvn install -DskipTests`.

## Build MagpieBridge in IDE
1. Simply import the project as maven project in your IDE, Maven should take care of all required dependences. For Eclipse: 
```
Eclipse> File> Import > Maven > Existing Maven Projects > Enter the path to your local repository > Finish
```

## Contributors:
<a href="https://github.com/MagpieBridge/MagpieBridge/graphs/contributors">
    <img src="https://github.com/MagpieBridge/MagpieBridge/blob/master/doc/contributor.png"/>
</a>

## Get Involved
- Pull requests are welcome!
- Submit github issues for any feature enhancements, bugs or documentation problems
- Please format the code with `mvn com.coveo:fmt-maven-plugin:format` before `git push`
## Contact 
&#x2709; linghui[at]outlook.de
