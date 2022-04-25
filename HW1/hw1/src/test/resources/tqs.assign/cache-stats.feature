Feature: Check API cache stats

  Scenario: Obtain cache stats
    Given I am in the Home page
    When I click 'Submit' under the cache section
    Then I should receive cache stats