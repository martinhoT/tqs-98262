Feature: Book categories
  Allow books to be allocated in specific categories, and to filter books by it.

  Scenario Outline: Obtain all books from a given category
    Given a book with the title 'A book', written by 'The final author', published in 2012-07-20, from 'Action' and 'Horror'
    And a book with the title 'Another book', written by 'Yet another author', published in 2021-03-31, from 'Romance' and 'Action'
    And a book with the title 'Yet another book', written by 'Another author', published in 2012-07-12, from 'Educational'
    And a book with the title 'The final book', written by 'An author', published in 2000-12-25
    When the customer searches for books of <category>
    Then <number_of_books> books should have been found

    Examples:
      | category  | number_of_books |
      | Action    | 2               |
      | Romance   | 1               |
      | Anything  | 4               |