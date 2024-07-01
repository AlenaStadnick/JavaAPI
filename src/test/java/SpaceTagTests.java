import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class SpaceTagTests {

    private static final String BASE_URL = "https://api.clickup.com/api/v2";
    private String apiKey;
    private String spaceId;
    private String taskId;
    private String tagName;

    @BeforeClass
    public void setup() {
        loadConfig();
        RestAssured.baseURI = BASE_URL;
    }

    private void loadConfig() {
        Properties properties = new Properties();
        try {
            FileInputStream configFile = new FileInputStream("src/test/resources/config.properties");
            properties.load(configFile);
            apiKey = properties.getProperty("apiKey");
            spaceId = properties.getProperty("spaceId");
            taskId = properties.getProperty("task_id");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetSpaceTags() {
        try {
            Response response =
                    given()
                            .header("Authorization", apiKey)
                            .header("Content-Type", "application/json")
                            .pathParam("space_id", spaceId)
                            .log().all()
                            .when()
                            .get("/space/{space_id}/tag")
                            .then()
                            .log().all()
                            .statusCode(200)
                            .extract().response();

            response.then().body("tags", not(empty()));
        } catch (AssertionError e) {
            System.err.println("Test for GET /space/{space_id}/tag failed: " + e.getMessage());
            throw e;
        }
    }

    @Test(dependsOnMethods = "testGetSpaceTags")
    public void testCreateSpaceTag() {
        String tagColor = "#000000";
        String requestBody = "{\n" +
                "    \"tag\": {\n" +
                "      \"name\": \"" + tagName + "\",\n" +
                "      \"tag_fg\": \"" + tagColor + "\",\n" +
                "      \"tag_bg\": \"" + tagColor + "\"\n" +
                "    }\n" +
                "}";

        try {
            Response response =
                    given()
                            .header("Authorization", apiKey)
                            .header("Content-Type", "application/json")
                            .pathParam("space_id", spaceId)
                            .body(requestBody)
                            .log().all()
                            .when()
                            .post("/space/{space_id}/tag")
                            .then()
                            .log().all()
                            .statusCode(200)
                            .extract().response();

            tagName = "MyTagTest";

        } catch (AssertionError e) {
            System.err.println("Test for POST /space/{space_id}/tag failed: " + e.getMessage());
            throw e;
        }
    }

    @Test(dependsOnMethods = "testCreateSpaceTag")
    public void testEditSpaceTag() {
        String editRequestBody = "{ \"tag\": { \"name\": \"UpdatedMyTagTest\", \"tag_fg\": \"#FFFFFF\", \"tag_bg\": \"#FFFFFF\" } }";

        try {
            given()
                    .header("Authorization", apiKey)
                    .header("Content-Type", "application/json")
                    .pathParam("space_id", spaceId)
                    .pathParam("tag_name", tagName)
                    .body(editRequestBody)
                    .log().all()
                    .when()
                    .put("/space/{space_id}/tag/{tag_name}")
                    .then()
                    .log().all()
                    .statusCode(200);
        } catch (AssertionError e) {
            System.err.println("Test for PUT /space/{space_id}/tag/{tag_name} failed: " + e.getMessage());
            throw e;
        }
    }

    //@Test(dependsOnMethods = "testEditSpaceTag")


    @Test(dependsOnMethods = "testEditSpaceTag")
    public void testDeleteSpaceTag() {
        String tagToDelete = "UpdatedMyTagTest";

        try {
            given()
                    .header("Authorization", apiKey)
                    .header("Content-Type", "application/json")
                    .pathParam("space_id", spaceId)
                    .pathParam("tag_name", tagToDelete)
                    .log().all()
                    .when()
                    .delete("/space/{space_id}/tag/{tag_name}")
                    .then()
                    .log().all()
                    .statusCode(200);
        } catch (AssertionError e) {
            System.err.println("Test for DELETE /space/{space_id}/tag/{tag_name} failed: " + e.getMessage());
            throw e;
        }
    }


    @Test(dependsOnMethods = "testDeleteSpaceTag")
    public void testVerifyDeletedSpaceTag() {
        String tagToDelete = "UpdatedMyTagTest";

        try {
            Response response =
                    given()
                            .header("Authorization", apiKey)
                            .header("Content-Type", "application/json")
                            .pathParam("space_id", spaceId)
                            .log().all()
                            .when()
                            .get("/space/{space_id}/tag")
                            .then()
                            .log().all()
                            .statusCode(200)
                            .extract().response();

            response.then()
                    .body("tags.name", not(hasItem(tagToDelete)));

        } catch (AssertionError e) {
            System.err.println("Verification after DELETE /space/{space_id}/tag/{tag_name} failed: " + e.getMessage());
            throw e;
        }
    }
    @Test
    public void testAddTagToTask() {
        String tagName = "Tag2";
        String requestBody = "{}";

        try {
            Response response =
                    given()
                            .header("Authorization", apiKey)
                            .header("Content-Type", "application/json")
                            .pathParam("task_id", taskId)
                            .pathParam("tag_name", tagName)
                            .body(requestBody)
                            .log().all()
                            .when()
                            .post("/task/{task_id}/tag/{tag_name}")
                            .then()
                            .log().all()
                            .statusCode(200)
                            .extract().response();

        } catch (AssertionError e) {
            System.err.println("Test for POST /task/{task_id}/tag/{tag_name} failed: " + e.getMessage());
            throw e;
        }
    }
    @Test(dependsOnMethods = "testAddTagToTask")
    public void testDeleteTagFromTask() {
        String tagName = "Tag2";

        try {
            given()
                    .header("Authorization", apiKey)
                    .header("Content-Type", "application/json")
                    .pathParam("task_id", taskId)
                    .pathParam("tag_name", tagName)
                    .log().all()  // Логування запиту
                    .when()
                    .delete("/task/{task_id}/tag/{tag_name}")
                    .then()
                    .log().all()  // Логування відповіді
                    .statusCode(200);  // Перевірка, що статус відповіді 200
        } catch (AssertionError e) {
            System.err.println("Test for DELETE /task/{task_id}/tag/{tag_name} failed: " + e.getMessage());
            throw e;  // Повторно кидаємо виняток, щоб TestNG міг зафіксувати невдалий тест
        }
    }

}
