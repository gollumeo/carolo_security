# Safe City

## A project for the 2023 Citizens of Wallonia Hackathon

### Introduction

The goal of this hackathon is to create a project that will help the citizens of Wallonia. The project must be
innovative and must be able to be implemented in the region.

### The project

The project is a web application using Azure Computer Vision API to detect unsafe situations in the city.
The application will be able to detect if a person is in danger and will send an alert to the related services.

**Head over [there](https://github.com/gollumeo/carolo_security/tree/java)**

### Development Team

- [Pierre Mauriello](https://github.com/gollumeo)
- [Arno Volts](https://github.com/voltsn)
- [Jonathan Manes](https://github.com/manesjonathan)

### How to use

#### Prerequisites

- Java 17
- Gradle
- Azure Computer Vision API key
- AWS S3 bucket
- AWS IAM user with access to the S3 bucket
- AWS IAM user access key and secret key
- A video file

#### Installation

1. Clone the repository
2. Create a file named `application.properties` in the `src/main/resources` folder
3. Add the following properties to the file:

```properties
azure.sub.key=YOUR_AZURE_COMPUTER_VISION_API_KEY
azure.endpoint=YOUR_AZURE_COMPUTER_VISION_API_ENDPOINT
aws.secret.ket=YOUR_AWS_SECRET_KEY
aws.access.key=YOUR_AWS_ACCESS_KEY
```

4. Create a video directory at root of the project
5. Add your video file to the video directory
6. Run the application with `gradle bootRun`
7. Go to `http://localhost:8080` in your browser
8. Click on the `launch` button
9. Wait for the application to process the video
