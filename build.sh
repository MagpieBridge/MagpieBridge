
function install {
 jarFile=$1
 pomFile=`dirname $jarFile`/`basename $jarFile .jar`.pom
 cmd="mvn install:install-file -DpomFile=$pomFile -Dfile=$jarFile -Dpackaging=jar -DlocalRepositoryPath=repository"

 sourcesFile=`dirname $jarFile`/`basename $jarFile .jar`-sources.jar
 if [ -e $sourcesFile ]; then
   cmd="$cmd -Dsources=$sourcesFile"
 fi

 $cmd
}

function install_tree {
    for f in `find $1 -name '*-SNAPSHOT.jar'`; do
	install $f
    done
}

for dir in "$@"; do
  install_tree $dir
done
