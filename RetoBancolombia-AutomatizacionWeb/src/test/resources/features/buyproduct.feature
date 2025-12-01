Feature: Buy products
  This feature is designed to automate and validate the product purchasing process on the https://www.saucedemo.com/ platform.

  @BuyProduct
  Scenario Outline: Buy product
    Given the user opens the browser
      | <id> | <archive> | <sheet> |
    When the user log in
    And selects the products to purchase and completes the checkout
    Then they should verify added products
      | <id> | <archive> | <sheet> |
    And the price total
      | <id> | <archive> | <sheet> |
    And that the message is displayed
      | <id> | <archive> | <sheet> |
    Examples:
      | id | archive              | sheet |
      | 1  | DataBuyProducts.xlsx | Data  |