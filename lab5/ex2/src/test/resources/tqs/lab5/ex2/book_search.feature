Feature: Standard book search
  To allow a customer to find his favourite books quickly, the library must offer multiple ways to search for a book.

  Background:
    Given a book with the title 'One good book', written by 'Anonymous', published in 2013-03-14
    And another book with the title 'Some other book', written by 'Tim Tomson', published in 2014-08-23
    And another book with the title 'How to cook a dino', written by 'Fred Flintstone', published in 2012-01-01

  Scenario: Search books by publication year
    When the customer searches for books published between 2013 and 2014
    Then 2 books should have been found
    And Book 1 should have the title 'Some other book'
    And Book 2 should have the title 'One good book'

  Scenario: Search books by title
    When the customer searches for books with the title 'cook'
    Then 1 book should have been found
    And Book 1 should have the title 'How to cook a dino'

  Scenario: Search books by author
    Given a book with the title 'How to hunt a dino', written by 'Fred Flintstone', published in 2012-02-28
    When the customer searches for books written by 'Fred Flintstone'
    Then 2 books should have been found
    And Book 1 should have the title 'How to hunt a dino'
    And Book 2 should have the title 'How to cook a dino'

  Scenario: Search books by multiple attributes
    When the customer searches for books with the title 'book' written by 'Tim Tomson'
    Then 1 book should have been found
    And Book 1 should have the title 'Some other book'