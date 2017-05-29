import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import spark.Request;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.List;

/**
 * Created by Karthik on 5/27/17.
 */
public class CreateProjectService {

    /**
     *
     * @param request
     * @return
     */
    public ResponseMessageWithStatusCode createSingleProject(Request request) {

        if(!isProjectKeysValid(request.body())) {
            return new ResponseMessageWithStatusCode("Invalid key in data", 400);
        }

        Project project = new Gson().fromJson(request.body(), Project.class);

        if (!project.isValid()) {
            return new ResponseMessageWithStatusCode("Data is invalid", 400);
        }

        return createProject(project);
    }

    /**
     *
     * @param request
     * @return
     */
    public ResponseMessageWithStatusCode createMultipleProjects(Request request) {

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(request.body());
        JsonArray jsonArray = element.getAsJsonArray();
        ResponseMessageWithStatusCode responseMessageWithStatusCode = null;

        for (int i = 0; i < jsonArray.size(); ++i) {
            JsonElement jsonElement = jsonArray.get(i);

            if(!isProjectKeysValid(jsonElement.toString()))
                return new ResponseMessageWithStatusCode("Invalid key in data", 400);

            Project project = new Gson().fromJson(jsonElement.toString(), Project.class);

            if (!project.isValid()) {
                 return new ResponseMessageWithStatusCode("Data is invalid", 400);
            }

            responseMessageWithStatusCode = createProject(project);
            if (responseMessageWithStatusCode.getStatusCode() != 200)
                return responseMessageWithStatusCode;
        }

        return responseMessageWithStatusCode;
    }

    /**
     *
     * @param project
     * @return
     */
    private ResponseMessageWithStatusCode createProject(Project project) {

        Gson gson = new Gson();
        BufferedWriter writer = null;
        ResponseMessageWithStatusCode responseMessageWithStatusCode = null;

        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("projects.txt", true), "UTF-8"));
            writer.write(gson.toJson(project));
            writer.newLine();
            responseMessageWithStatusCode = new ResponseMessageWithStatusCode("campaign is successfully created", 200);
        } catch (IOException e) {
            responseMessageWithStatusCode = new ResponseMessageWithStatusCode("Error writing to file: " + e, 500);
        } finally {
            try {
                writer.flush();
                writer.close();
                return responseMessageWithStatusCode;
            } catch (IOException e) {
                return new ResponseMessageWithStatusCode("Error closing file: " + e, 500);
            }
        }
    }

    /**
     *
     * @param request
     * @return
     */
    private boolean isProjectKeysValid(String request) {

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(request);
        JsonObject obj = element.getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> entries = obj.entrySet();

        String[] tempValidKeys = new String[] { "id", "projectName", "creationDate","expiryDate", "enabled",
                "targetCountries", "projectCost", "projectUrl", "targetKeys"};
        Set<String> validKeys = new HashSet<String >(Arrays.asList(tempValidKeys));

        for (Map.Entry<String, JsonElement> entry: entries) {
            if(!validKeys.contains(entry.getKey())) {
                return false;
            }
        }
        return true;
    }
}
