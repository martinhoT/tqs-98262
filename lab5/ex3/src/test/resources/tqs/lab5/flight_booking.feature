Feature: Flight booking
  Book flights from a source and destination, with filling of details and purchasing procedure.

  Scenario: Find flights from source to destination
    Given I am on the BlazeDemo home page
    When I choose the source port 'San Diego' from the available options
    * I choose the destination port 'Cairo' from the available options
    * I click 'Find Flights'
    Then I should be in the reservation page
    And I should see the header 'Flights from San Diego to Cairo:'

  Scenario: Choose a reservation
    Given I am on the BlazeDemo reservation page
    When I click on the 4th reservation choice
    Then I should be in the purchase page
    And I the displayed airline should be 'United'

  Scenario: Purchase the reservation
    Given I am on the BlazeDemo purchase page
    When I set the name to 'Bruh momento'
    * I set the address to 'Address example'
    * I set the city to Portugal
    * I set the state to Europe
    * I set the zipcode to 12345
    * I choose the card type 'American Express' from the available options
    * I set the credit card number to 'you wish'
    And I click 'Purchase Flight'
    Then I should be in the confirmation page