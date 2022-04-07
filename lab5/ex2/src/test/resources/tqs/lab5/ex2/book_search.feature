Feature: Standard book search
  To allow a customer to find his favourite books quickly, the library must offer multiple ways to search for a book.

  Background:
    Given a book with the title 'One good book', written by 'Anonymous', published in 14 March 2013
    And another book with the title 'Some other book', written by 'Tim Tomson', published in 23 August 2014
    And another book with the title 'How to cook a dino', written by 'Fred Flintstone', published in 01 January 2012

  Scenario: Search books by publication year
    When the customer searches for books published between 2013 and 2014
    Then 2 books should have been found
    And Book 1 should have the title 'Some other book'
    And Book 2 should have the title 'One good book'

  Scenario: Search books by title
    When the customer searches for books with the title 'cook'
    Then 1 book should have been found
    And that book should have the title 'How to cook a dino'

  Scenario: Search books by author
    Given a book with the title 'How to hunt a dino', written by 'Fred Flintstone', published in 28 February 2012
    When the customer searches for books written by 'Fred Flintstone'
    Then 2 books should have been found
    And Book 1 should have the title 'How to cook a dino'
    And Book 2 should have the title 'How to hunt a dino'



Feature: Book categories
  Allow books to be allocated in specific categories, and to filter books by it.

  Scenario Outline: Obtain all books from a given category
    Given a book with the title 'A book', written by 'The final author', published in 20 July 2012, from 'Action' and 'Horror'
    And a book with the title 'Another book', written by 'Yet another author', published in 31 March 2021, from 'Romance' and 'Action'
    And a book with the title 'Yet another book', written by 'Another author', published in 12 July 2012, from 'Educational'
    And a book with the title 'The final book', written by 'An author', published in 25 December 2000
    When the customer searches for books of <category>
    Then <number_of_books> books should have been found

    Examples:
      | category  | number_of_books |
      | Action    | 2               |
      | Romance   | 1               |
      | Anything  | 4               |



Feature: Book bookings
  Allow books to be booked, permitting searches of unbooked books.

  Scenario: Obtain all unbooked books
    Given the books catalogue is initialized with the following books
      | title             | author    | published           | booked  |
      | Wow               | Man       | 02 April 2009       | false   |
      | No way            | Person    | 17 June 1986        | true    |
      | Cucumber          | Ruby      | 17 June 2021        | false   |
      | Creativity 0      | Human     | 09 September 2009   | false   |
      | TQS guide         | UA        | 08 March 2022       | true    |
    When the customer searches for unbooked books
    Then 3 books should have been found
    And all books should not be booked
