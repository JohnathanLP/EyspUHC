javac -Xlint -cp ../Spigot/Spigot-API/target/spigot-api-1.13.2-R0.1-SNAPSHOT-shaded.jar eyspuhc/*.java

jar cvf EyspUHC.jar eyspuhc/*.class plugin.yml

#rm -r eyspuhc/*.class

rm ../plugins/EyspUHC.jar

cp EyspUHC.jar ../plugins
