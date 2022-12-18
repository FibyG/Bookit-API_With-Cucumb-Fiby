package com.bookit.step_definitions;

import com.bookit.pages.SelfPage;
import com.bookit.utilities.BookitUtils;
import com.bookit.utilities.ConfigurationReader;
import com.bookit.utilities.DB_Util;
import com.bookit.utilities.Environment;
import io.cucumber.java.en.*;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.Assert;

import java.util.Map;

import static io.restassured.RestAssured.*;

public class ApiStepDefs {

    String token;
    Response response;
    String globalEmail;

    @Given("I logged Bookit api as a {string}")
    public void i_logged_bookit_api_as_a(String role) {
        token = BookitUtils.generateTokenByRole(role);
        System.out.println("token = " + token);
        Map<String, String> credentials = BookitUtils.returnCredentials(role);
        globalEmail = credentials.get("email");


    }
    @When("I sent get request to {string} endpoint")
    public void i_sent_get_request_to_endpoint(String endpoint) {
        response = given().accept(ContentType.JSON)
                .header("Authorization", token)
                .when().get(Environment.BASE_URL + endpoint);

    }

    @Then("status code should be {int}")
    public void status_code_should_be(int expectedStatusCode) {

        System.out.println("response.statusCode() = " + response.statusCode());
        Assert.assertEquals(expectedStatusCode,response.statusCode());


    }
    @Then("content type is {string}")
    public void content_type_is(String expectedContentType) {
        System.out.println("response.contentType() = " + response.contentType());
        Assert.assertEquals(expectedContentType,response.contentType());

    }

    @Then("role is {string}")
    public void role_is(String expectedRole) {

        System.out.println("response.path(\"role\") = " + response.path("role"));
        System.out.println("response.jsonPath().getString(\"role\") = " + response.jsonPath().getString("role"));

        Assert.assertEquals(expectedRole,response.path("role"));

    }

    @Then("the information about current user from api and database should match")
    public void the_information_about_current_user_from_api_and_database_should_match(){

        // GET DATA FROM API
        JsonPath jsonPath = response.jsonPath();
        /*
        {
            "id": 11312,
            "firstName": "Lissie",
            "lastName": "Finnis",
            "role": "student-team-leader"
}
         */
        // lastname.. actual from API
        String actualLastName = jsonPath.getString("lastName");
        // firstname
        String actualFirstName = jsonPath.getString("firstName");
        // role
        String actualRole = jsonPath.getString("role");

        System.out.println("actualFirstName = " + actualFirstName);
        System.out.println("actualRole = " + actualRole);
        // GET DATA FROM DB

        String query="select firstname,lastname,role from users where email='"+globalEmail+"'";

        DB_Util.runQuery(query);

        Map<String, String> dbMap = DB_Util.getRowMap(1);

     System.out.println(dbMap);

        String expectedFirstName = dbMap.get("firstname"); // expected from DB
        String expectedLastName = dbMap.get("lastname"); // expected from DB
        String expectedRole = dbMap.get("role"); // expected from DB
        System.out.println(expectedLastName);
        System.out.println(expectedFirstName);
        System.out.println(expectedRole);

        //ASSERTIONS.. expected from DB, actual from API

        Assert.assertEquals(expectedFirstName,actualFirstName);
        Assert.assertEquals(expectedLastName,actualLastName);
        Assert.assertEquals(expectedRole,actualRole);

    }


    @Then("UI,API and Database user information must be match")
    public void ui_API_And_Database_User_Information_Must_Be_Match() {


        // GET DATA FROM API
        JsonPath jsonPath = response.jsonPath();
        /*
        {
            "id": 11312,
            "firstName": "Lissie",
            "lastName": "Finnis",
            "role": "student-team-leader"
}
         */
        // lastname.. actual from API
        String actualLastNameAPI = jsonPath.getString("lastName");
        // firstname
        String actualFirstNameAPI = jsonPath.getString("firstName");
        // role
        String actualRoleAPI = jsonPath.getString("role");

        System.out.println("actualFirstNameAPI = " + actualFirstNameAPI);
        System.out.println("actualRoleAPI = " + actualRoleAPI);
        // GET DATA FROM DB

        String query="select firstname,lastname,role from users where email='"+globalEmail+"'";

        DB_Util.runQuery(query);

        Map<String, String> dbMap = DB_Util.getRowMap(1);

        System.out.println(dbMap);

        String expectedFirstNameDB = dbMap.get("firstname"); // expected from DB
        String expectedLastNameDB = dbMap.get("lastname"); // expected from DB
        String expectedRoleDB = dbMap.get("role"); // expected from DB
        System.out.println(expectedLastNameDB);
        System.out.println(expectedFirstNameDB);
        System.out.println(expectedRoleDB);

        //ASSERTIONS API vs DB.. expected from DB, actual from API

        Assert.assertEquals(expectedFirstNameDB,actualFirstNameAPI);
        Assert.assertEquals(expectedLastNameDB,actualLastNameAPI);
        Assert.assertEquals(expectedRoleDB,actualRoleAPI);

        // Get data from UI
        // There is no only firstname and lastname.It is stored as fullname
        SelfPage selfPage= new SelfPage();
        String actualNameFromUI = selfPage.name.getText();
        String actualRoleFromUI = selfPage.role.getText();

        // UI vs DB assertion
        String expectedNameDB = expectedFirstNameDB+" "+expectedLastNameDB;
        Assert.assertEquals(expectedNameDB,actualNameFromUI);
        Assert.assertEquals(expectedRoleDB,actualRoleFromUI);

        // UI vs API assertion
        String actualNameAPI = actualFirstNameAPI+" "+actualLastNameAPI;
        Assert.assertEquals(actualNameAPI,actualNameFromUI);
        Assert.assertEquals(actualRoleAPI,actualRoleFromUI);


    }
}