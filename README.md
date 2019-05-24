
<p align="center">
    <img src="https://github.com/MagpieBridge/MagpieBridge/blob/develop/doc/logshort.png" width="400">
</p>

# MapgieBridge [![Build Status](https://travis-ci.com/MagpieBridge/MagpieBridge.svg?branch=master)](https://travis-ci.com/MagpieBridge/MagpieBridge)


## What is MagpieBridge?
MapgieBridge is a framework for integrating Static Analyses into IDEs and Editors with the Language Server Protocol. 
<p align="center">
<img src="https://github.com/MagpieBridge/MagpieBridge/blob/master/doc/goal.PNG"  width="800">
</p>
MagpieBridge is a part of the [FutureSoot](http://sable.github.io/soot/future-soot/) project. 

## Integrated Static Analyses using MagpieBridge
- [MagpieBridge-based CogniCrypt](https://github.com/MagpieBridge/CryptoLSPDemo)
- [MagpieBridge-based FlowDroid](https://github.com/MagpieBridge/FlowDroidLSPDemo)

## How to use MapgieBridge for your own analysis?
- Check if your analysis is built on top the analysis frameworks MagpieBridge supports (WALA, Soot)
- [Check this tutorial to create your first project with MagpieBridge](https://github.com/MagpieBridge/MagpieBridge/wiki/Create-your-first-project-with-MagpieBridge)

## Build MagpieBridge with Maven
1. check out the develop branch with `git clone -b develop git@github.com:MagpieBridge/MagpieBridge.git`
2. run `mvn install` in the project root directory to build the tool and run all tests. To skip tests, run `mvn install -DskipTests`.

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

## Contact 
&#x2709; linghui[at]outlook.de
