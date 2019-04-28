
# Development Testing

## Test Strategies



### Job Intent Services

We will use three different JobIntentServices, WMSNCIPService, AuthService and IMService. All of these services make api requests to either our server, or three different OCLC APIs. Collectively, they are responsible for obtaining all the data required by the application to function. Therefore, we must ensure that these services are tested rigorously, to ensure the app is able remains functioning. Although we are not able to mock these external APIs, we are able to create Request objects and then ensure that the requests made have the correct format and adhere to the correct set of rules outlines by the API provider (either ourselves or OCLC).

If the service is making a request to the server we would outline how data will be sent with the request and in what format, as well as, HTTP methods and any headers that are required. Once we have agreed the protcol required for communicating to the server we could then create a set of tests that would ensure any request we made adhered to this pre-set protocol, by doing this it would allow us to ensure the API Request created in the service would be as expected when it reached our server. We carried out a similar process when creating requests that would be made to the OCLC WMS API, inspecting their documentation, we were able to create some tests to ensure that any request made would be in the correct format with the correct data. By following this approach we were able to ensure that the API requests were of the correct specification for the API.

#### Challenges

There were a few challenges the we faced when testing the Job Intent Services. Firstly, it is not possible to test an intent service directly, this point is made clear on the Android documentation


### NFC Scanning

We initialled attempted to contact Bilbiotheka for assistance with reading the tags, however after turning a blank, we had to workout how the data on the tags was encoded. This had many challenges, one of the key challenges was testing that any method for decoding the data on the tag had to be tested thoroughly. After working out how the data was encoded, we created tests with the example data we had accumulated from the manual decoding. These tests coud then be used as a basis for ensuring that our implementation of any decoding algorithm was correct.

#### Challenges
blah





### Server
- Main job was to take request from phone and wrap with extra permissions. Majority of testing will take place on the application, only need to check it executes requests correctly. If request is misformed its the applications fault.
- Unit Tests to test key components.
- Manual testing of the api before implementing in java.

#### Challenges
blah

### UI Testing
Help?

We will need to create tests for the UI to ensure that the app looks as we expect it to do, this will involve creating unit tests and also doing some manual testing. The unit tests will ensure that making a change to the UI does not affect another part without us knowing. We would also want to do standard user testing to ensure that the UI is easy to use and meets the requirements for accessibility, which are important to our client.

#### Challenges
blah




## Testing Frameworks



We will use JUnit to write out tests, these will then be run on Circle CI after each commit. This will ensure that all our components constantly checked after each addition.



These three components will have a similar testing strategy. Since they are all external services we will have to create mock services for each service, this will allow us to predict the data being *received* by our system, since it will be hard coded into the mock service. This allows us to ensure that the data is parsed correctly by our system, we can do this by creating unit tests to ensure that the data output of the parser matches what we expect when given the data in the mock service.

We could also attempt to ensure that the requests made by our service are the same as what we are expecting them to be when *requesting a users loan history* for example.

## Challenges with testing

- We are using JobIntentServices [google says this is impossible] => need to split out as much logic into separate classes to allow us to test the logic. Will have to manually test that the services
- cannot simulate an nfc tag on the emulator, requires physical phone and book
- cannot be sure weve tested all nfc tag types
- Do not have a sandbox api, only live


There will be several issues when testing, this is because a lot of our key functions rely on connections to external services. This makes it hard to test as we do not know the response of these external services until a request is made making it hard to check the output of a function is correct. 

To overcome this issue we will have to create an interface for each of these external services, then create two concrete classes which implement the interface:
- one will be a *live* class that will query the APIs, this will be used in the production app.
- one will be a *mock* class that will return dummy data as an api response. This will allow us to predict the outputs of the functions, therefore allowing us to check that they function correctly in the specific cases. Both classes will have identical functions to be accessed in the same way.