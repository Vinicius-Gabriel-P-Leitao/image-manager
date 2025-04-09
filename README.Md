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
## Descrição de arquivos

- [hello-view.fxml](./src/main/resources/me/image/manager/hello-view.fxml): Arquivo de configuração visual define os elementos, ids, classes e tamanhos, importa biblioteca de icones.
- [hello-view.css](./src/main/resources/me/image/manager/css/hello-view.css): Arquivo necessário para criar estilização de elementos dentro do .fxml.

## Descrição de classes

- [module-info.java](./src/main/java/module-info.java): Arquivo para otimizar modulação do projeto, definindo dependências externas.
- [Main](./src/main/java/me/image/manager/Main.java): Classe principal usada para gerencias os controller da aplicação JAVAFX.
- [HelloApplication](./src/main/java/me/image/manager/HelloApplication.java): Classe padrão do JAVAFX para inicializar o projeto e definir a janela principal  do projeto e instanciar o .fxml.
- [HelloController](./src/main/java/me/image/manager/HelloController.java): Classe de controle de eventos padrão do JAVAFX classe contem todos eventos do front-end, buscando via fx:id="": 
  - Método usado para sempre que um usuário passar o mouse em cima de um componente ele ficar com o status de pointer
  ```java
  onPointerMouse();
  ```

  - Método responsável por abrir a função de buscar diretório, necessário um sistema de gerenciamento de diretórios padrão no sistema, assim que o diretório é selecionado o TextField se torna um ComboBox com o caminho do diretório e os arquivos dentro, para ser selecionado e movimentado
  ```java
  onOpenDirectoryChooser();
  ```
  
  - Método responsável por iniciar a task() de copiar os arquivos foram inseridos pelo usuário via TextField ou combobox necessário ter arquivo de origem e de destino bem definidos, caso o diretório de destino não exista ele cria.
  ```java
  onCopyFiles();
  ```
  
  - Método responsável por iniciar task() de renomear arquivos, recebe o caminho de origem, nome + label para definir um nome com metadados da imagem.
  ```java
  onRenameFiles();
  ```
  
  - Método responsável por iniciar task() de converter arquivos, recebe o caminho de origem e o tipo a ser convertido, os arquivos com tipagem antiga são movidos para uma pasta dentro do diretório de origem.
  ```java
  onConvertFiles();
  ```

















```
```
