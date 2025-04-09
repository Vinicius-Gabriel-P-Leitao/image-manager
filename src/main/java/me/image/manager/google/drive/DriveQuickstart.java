package me.image.manager.google.drive;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class DriveQuickstart {
    private static final String APPLICATION_NAME = "image-manager";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);

    private static final String TOKENS_DIRECTORY_PATH = "resource/drive/token";

    /**
     * Obtém as credenciais de acesso para a API do Google Drive usando OAuth 2.0.
     *
     * <p>Este método realiza a autenticação com a API do Google Drive através do fluxo OAuth 2.0,
     * carregando as credenciais do cliente a partir do arquivo JSON e gerenciando o token de acesso.</p>
     *
     * <p><b>Fluxo de operação:</b></p>
     * <ol>
     *   <li>Carrega as credenciais do cliente (client ID e secret) do arquivo credentials.json</li>
     *   <li>Configura o fluxo de autorização com escopos e tipo de acesso offline</li>
     *   <li>Inicia um servidor local para receber o código de autorização (porta 8888)</li>
     *   <li>Realiza a troca do código por um token de acesso</li>
     * </ol>
     *
     * @param HTTP_TRANSPORT O transporte HTTP configurado para conexões com a API Google
     * @return Credential Objeto de credenciais autorizadas para acesso à API
     * @throws IOException           Se ocorrer algum erro durante:
     *                               <ul>
     *                                 <li>Leitura do arquivo credentials.json</li>
     *                                 <li>Comunicação com o servidor de autenticação do Google</li>
     *                                 <li>Operações com o DataStore de tokens</li>
     *                               </ul>
     * @throws FileNotFoundException Se o arquivo credentials.json não for encontrado no classpath
     * @see com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
     * @see com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
     * @since 0.0.2
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        InputStream in = DriveQuickstart.class.getResourceAsStream("/me/image/manager/drive/credentials.json");
        if (in == null)
            throw new FileNotFoundException("Arquivo de credenciais não encontrado!");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH))).setAccessType("offline").build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Cria e retorna uma instância do serviço do Google Drive configurada e autenticada.
     *
     * <p>Este método é o ponto de entrada principal para interação com a API do Google Drive,
     * realizando toda a configuração necessária:</p>
     *
     * <ol>
     *   <li>Cria um transporte HTTP confiável usando {@link GoogleNetHttpTransport}</li>
     *   <li>Obtém as credenciais de autenticação através do método {@link #getCredentials(NetHttpTransport)}</li>
     *   <li>Constrói o serviço do Drive com as configurações apropriadas</li>
     * </ol>
     *
     * @return Instância configurada do serviço do Google Drive
     * @throws IOException Se ocorrer erro durante:
     *                    <ul>
     *                      <li>Criação do transporte HTTP</li>
     *                      <li>Autenticação com a API</li>
     *                      <li>Carregamento das credenciais</li>
     *                    </ul>
     * @throws GeneralSecurityException Se ocorrer erro na configuração de segurança do transporte HTTP
     *
     * @see com.google.api.services.drive.Drive
     * @see #getCredentials(NetHttpTransport)
     *
     * @since 0.0.2
     */
    public static Drive getDriveService() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT)).setApplicationName(APPLICATION_NAME).build();
    }
}
