Feature: Book bookings
  Allow books to be booked, permitting searches of unbooked books.

  Scenario: Obtain all unbooked books
    Given the books catalogue is initialized with the following books
      | title             | author    | published   | booked  |
      | Wow               | Man       | 2009-04-02  | false   |
      | No way            | Person    | 1986-06-17  | true    |
      | Cucumber          | Ruby      | 2021-06-17  | false   |
      | Creativity 0      | Human     | 2009-09-09  | false   |
      | TQS guide         | UA        | 2022-03-08  | true    |
    When the customer searches for unbooked books
    Then 3 books should have been found
    And all books should not be booked