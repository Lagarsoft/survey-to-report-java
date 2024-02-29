# Survey-to-report #

### Google Sheets API Integration

This application demonstrates how to integrate with the Google Sheets API using Java. It fetches data from a specified Google Sheet and processes it.

### OpenAI ChatGPT 3.5-Turbo Integration

Additionally, it showcases integration with the OpenAI ChatGPT 3.5-Turbo API. By providing prompts, it generates coherent text based on the input.

### Initial Configuration

Ensure you have the necessary credentials for accessing the Google Sheets API.
Set up your environment variables, including SPREADSHEETID for the Google Sheet ID and OPENAI_API_KEY for the OpenAI API key.

To run the application, you need to click on "run" in the IDE (for example, IntelliJ) or use the command in the terminal ./gradlew run

Inside src/main/resources, you need to create a file called credentials.json. Similarly, we leave an example file named credentials.json.example.

You can also download the credentials.json file by following these steps:
1. In the Google Cloud console, go to menu > APIs & Services > Credentials.
2. Click Create Credentials > OAuth client ID.
3. Click Application type > Desktop app.
4. In the Name field, type a name for the credential. This name is only shown in the Google Cloud console.
5. Click Create. The OAuth client created screen appears, showing your new Client ID and Client secret.
6. Click OK. The newly created credential appears under OAuth 2.0 Client IDs.
7. Save the downloaded JSON file as credentials.json, and move the file to your working directory.

To create this documentation, I relied on: https://github.com/TheoKanning/openai-java, which is the community-created library accepted by OpenAI. I also used OpenAI's documentation: https://platform.openai.com/docs/introduction
