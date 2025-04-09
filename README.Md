# Como rodar o projeto:

- Tenha o JAVA baixado e com a JAVA_HOME bem configurados, na versão descrita no `.tools-versions` (Necessário para acesso a memoria  do JAVAFX).
- Execute para compilar o projeto e gerar o .jar
```shell
./mvnw clean package
```
- Execute o arquivo .jar
``` java
java -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -jar ./target/manager.jar
```