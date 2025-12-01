Feature: Get User

  Background:
    * header x-api-key = apiKey

  @GetUser
  Scenario: Get User
    Given url createUSerUrl = api + GetUser
    When method GET
    Then status 200
    And print response
    * assert response.data.length > 5
    And match each response.data[*].email == '#regex [a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}'
