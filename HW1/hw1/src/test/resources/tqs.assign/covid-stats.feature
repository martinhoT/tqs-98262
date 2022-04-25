Feature: Check Covid-19 stats

  Scenario: Obtain world data
    Given I am in the Home page
    When I check to obtain world data
    And I choose the date 2021-01-01
    And I click 'Submit' under the covid section
    Then I should receive covid stats from the 'world'
    And the date 'at' field should be 2021-01-01
    And no other date field should appear


  Scenario: Obtain country data
    Given I am in the Home page
    When I uncheck to obtain world data
    * I choose stats after 2021-12-12
    * I choose the country 'Portugal'
    * I click 'Submit' under the covid section
    Then I should receive covid stats from 'Portugal'
    And the date 'after' field should be 2021-12-12
    And no other date field should appear