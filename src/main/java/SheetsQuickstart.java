import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.service.OpenAiService;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SheetsQuickstart {
    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES =
            Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = SheetsQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Prints sample spreadsheet:
     * https://docs.google.com/spreadsheets/d/1bmpxwptVtLRek9vFp7KaamJeBDQTvxB1VNGxe7jwmHM/edit
     */

    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = System.getenv("SPREADSHEETID");
        final String range = "Form Responses 1!A1:Z2";
        Sheets service =
                new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                        .setApplicationName(APPLICATION_NAME)
                        .build();

        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        List<List<Object>> values = response.getValues();

        List<Object> questionRow = values.get(0);
        List<Object> answerRow = values.get(1);

        List<String> questions = new ArrayList<>();
        List<String> answers = new ArrayList<>();

        for (Object value : questionRow) {
            if (value != null && !value.toString().isEmpty()) {
                questions.add(value.toString());
            }
        }
        for (Object value : answerRow) {
            if (value != null && !value.toString().isEmpty()) {
                answers.add(value.toString());
            }
        }

        StringBuilder textBuilder = new StringBuilder();
        for (int i = 0; i < Math.min(questions.size(), answers.size()); i++) {
            textBuilder.append(questions.get(i)).append(" ").append(answers.get(i));
        }
        String text = textBuilder.toString();

        String prompt = "Given the following questions and answers between angle brackets given by an architect who performed the inspection of a building, draft a Building Envelope Assesment Report with an introduction, body and conclusion. Questions and answers:\n<" + text + ">";

        generateTextUsingLLM(prompt);

    }
    private static String generateTextUsingLLM(String prompt) {
        try {
            String token = System.getenv("OPENAI_API_KEY");
            OpenAiService openAiService = new OpenAiService(token,  Duration.ofSeconds(60));

            List<ChatMessage> chatMessageList = new ArrayList<>();
            ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), prompt);
            chatMessageList.add(chatMessage);
            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                    .model("gpt-3.5-turbo")
                    .messages(chatMessageList)
                    .n(1)
                    .build();

            if (!openAiService.createChatCompletion(chatCompletionRequest).getChoices().isEmpty()) {
                String result = openAiService
                        .createChatCompletion(chatCompletionRequest).getChoices()
                        .get(0).getMessage().getContent();
                System.out.println(result);
                return result;
            } else {
                System.err.println("No completion choices received.");
            }
        } catch(Exception exception) {
            System.err.println("Error occurred: " + exception.getMessage());
            throw new RuntimeException(exception);
        }
        return null;
    }
}