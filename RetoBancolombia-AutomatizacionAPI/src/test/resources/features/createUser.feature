Feature: Create User

  Background:
    * url createUSerUrl = api + PostUser
    * header x-api-key = apiKey
    * json requestCreateUser = read('classpath:/assets/requests/create-user.json')

  @CreateUser
  Scenario: Create User
    * replace requestCreateUser.name = "Santiago Hernández Rojo"
    * replace requestCreateUser.job = "Automation QA"
    * json requestCreateUser = requestCreateUser
    * def expectedResponse = read('classpath:/assets/responses/create-user-success.json')
    * print requestCreateUser
    Given request requestCreateUser
    When method POST
    Then status 201
    And print response
    And match response == expectedResponse
    And match response.id == '#string'
    And match response.id != ''
    And match response.createdAt == '#string'
    And match response.createdAt == '#regex \\d{4}-\\d{2}-\\d{2}T.*Z'