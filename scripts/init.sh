cd ..
#mvn clean compile test assembly:single
cp target/*.jar ../neo4j-community-2.2.2/plugins
cp conf/neo4j-server.properties ../neo4j-community-2.2.2/conf
cd ../neo4j-community-2.2.2/plugins
mv neo4location-*.jar neo4location.jar
cd ../bin/
./neo4j start
cd ~/gatling-2.1.6/
rm -r results/*
echo "6\n\n\n" | sh bin/gatling.sh
