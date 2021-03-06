## a) Identify a couple of examples on the use of AssertJ expressive methods chaining.
A-75; B-113(; D-57; E-52; E-66).

## b) Identify an example in which you mock the behavior of the repository (and avoid involving a database).
B, mocking is done in the `setUp()` method.
 
## c) What is the difference between standard @Mock and @MockBean?
@Mock is a Mockito framework annotation that indicates that the annotated class should be mocked, and its main purpose
is to be used directly within the test methods or be manually injected as a dependency to other classes (probably the
test class).
@MockBean is an extension of the Mockito framework functionality in the Spring Framework, which not only annotates a
class to be mocked, but also takes care of automatic dependency injection for classes that require that mocked class
within the Spring application context, essentially substituting the usual Bean used everywhere with its mocked
counterpart.

## d) What is the role of the file “application-integrationtest.properties”? In which conditions will it be used?
The file `application-integrationtest.properties` is used for Integration Tests, in this case for those where a database
is used. It won't use the `application.properties` file since that's suited for the normal execution environment, and
this file allows configuration that may be more suited for the test environment.
The file `application-integrationtest.properties` is only used if specified. When, for example, configuring a database for
an Integration Test (for example, a MySQL database, and not an in-memory database like H2), then a file like this can be
specified that contains the necessary configurations.

## e) The sample project demonstrates three test strategies to assess an API (C, D and E) developed with SpringBoot. Which are the main/key differences?
The C and D examples use the MockMvc to communicate with the MVC application, while the E example uses a RestTemplate.
C and D examples use direct communication, while E uses a RestTemplate which also simulates processes like
serialization used in an actual communication with a REST API. The MockMvc strips those processes away and focuses only
on invoking the controller's endpoints.
The C and D tests have different scopes: the C example mocks the EmployeeService and is concerned with testing the API
without the persistence layer, since the Service layer is simply mocked (through the MockBean annotation), while D
performs actions in that API but tests those actions by directly manipulating the persistence layer. In this manner, the
D example is an Integration Test, since its scope is of the entire API (Controller, Service and Repository), while the C
example only tests the Controller side, with the Service being mocked, being therefore an Unit Test.
