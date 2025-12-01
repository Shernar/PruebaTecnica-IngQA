Feature: Update User

  Background:
    * url UpdateUSerUrl = api + PutUser
    * header x-api-key = apiKey
    * json requestUpdateUser = read('classpath:/assets/requests/update-user.json')

  @UpdateUser
  Scenario: Update User
    * def timeBefore = new Date().toISOString()
    * print timeBefore
    * replace requestUpdateUser.name = "Santiago Hernández Rojo"
    * replace requestUpdateUser.job = "Automation QA"
    * json requestUpdateUser = requestUpdateUser
    * def expectedResponse = read('classpath:/assets/responses/update-user-success.json')
    * print requestUpdateUser
    Given request requestUpdateUser
    When method PUT
    Then status 200
    And print response
    And match response == expectedResponse
    And match response.updatedAt == '#string'
    * assert response.updatedAt > timeBefore