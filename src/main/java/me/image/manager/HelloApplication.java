package me.image.manager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

/**
 * Classe principal que inicia a aplicação JavaFX.
 * Esta classe estende a classe abstrata {@link javafx.application.Application} e
 * implementa o método start() para configurar a interface gráfica da aplicação.
 */
public class HelloApplication extends Application {

    /**
     * Método principal que inicia a aplicação JavaFX.
     *
     * @param args Argumentos de linha de comando passados para a aplicação.
     *             Não são utilizados nesta implementação.
     */
    public static void main(String[] args) {
        launch();
    }

    /**
     * Método principal de inicialização da aplicação JavaFX.
     * Carrega a interface gráfica a partir de um arquivo FXML, configura a cena principal
     * e exibe o palco (stage) da aplicação.
     *
     * @param stage O palco primário (janela principal) da aplicação, fornecido pelo sistema JavaFX.
     * @throws Exception Se ocorrer algum erro durante o carregamento do arquivo FXML
     *                   ou na inicialização da interface gráfica.
     */
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
        TabPane pane = loader.<TabPane>load();

        Scene scene = new Scene(pane, 500, 300);

        stage.setTitle("Image manager");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }
}