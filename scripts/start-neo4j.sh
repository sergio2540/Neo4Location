cd ..
cp target/*.jar ../neo4j-community-2.2.2/plugins
cp -ar conf/neo4j-server.properties ../neo4j-community-2.2.2/conf
cp -ar libs/spatial ../neo4j-community-2.2.2/plugins/
cd ../neo4j-community-2.2.2/plugins
mv neo4location-*.jar neo4location.jar
cd ../bin/
./neo4j start
