mkdir bigjar

unzip -o lib/commons-io-2.4.jar -d bigjar
unzip -o lib/dropbox-core-sdk-1.7.7.jar -d bigjar
unzip -o lib/jackson-core-2.2.4.jar -d bigjar
unzip -o lib/commons-lang3-3.3.2.jar -d bigjar	
unzip -o lib/gson-2.3.1.jar -d bigjar
cp -Rv out/production/dropboxhackathon/* bigjar

rm manifest-backend-bigjar.jar
cd bigjar
jar cf ../manifest-backend-bigjar.jar .
cd ..
