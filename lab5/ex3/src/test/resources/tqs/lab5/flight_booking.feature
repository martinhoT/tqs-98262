Feature: Flight booking
  Book flights from a source and destination, with filling of details and purchasing procedure.

  Scenario: Successfully book a flight
    Given I am in the BlazeDemo Home page
    When I find flights from 'San Diego' to 'Cairo'
    * I choose the 4th reservation
    * I purchase with the following details
      | name            | address         | city      | state   | zipcode | card type         | credit card number  |
      | Bruh momento    | Address example | Portugal  | Europe  | 12345   | American Express  | you wish            |
    Then I should be in the Confirm page
