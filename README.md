# Pollo For Android - Cross-platform Open Source Clicker Client

Pollo is one of the latest apps by [Cornell AppDev](http://cornellappdev.com), a project team at Cornell University. Pollo seeks to replace the use of iClickers on a web and mobile platform.

## Installation
Clone the project with `git clone https://github.com/cuappdev/clicker-ios.git`

After cloning the project, open it with Android Studio and let Gradle take care of dependencies.

## Configuration
Create a `values/secrets.xml` file in the project directory with the following template:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="web_client_id">yourappid.apps.googleusercontent.com</string>
    <string name="deployed_backend">http://example.server.com</string>
</resources>
```
Similarly, create a `keys.properties` file in the project directory with the following template for Gradle to process.

```json
web_client_id="yourappid.apps.googleusercontent.com"
BACKEND_URI="http://example.server.com"
```

Replace `http://example.server.com` under `deployed_backend` with the host of your backend server (clone ours [here](https://github.com/cuappdev/pollo-backend.git)!).


Also insert your `google-services.json` configuration file into the `app\` folder for Google Login purposes.

---

### External Services

 * [Google Sign In](https://developers.google.com/identity/sign-in/android): Used to seamlessly sign in users.
 * [Google Firebase](https://firebase.google.com/docs/android/setup): Used for analytics and Google services.
 * [OkHttp](hhttps://square.github.io/okhttp/): Used to manage network connections with the backend.
 * [Socket.io](https://github.com/socketio/socket.io): Used to manage sockets.

Check out [Issues](https://github.com/cuappdev/pollo-android/issues) to see what we are working on!

# Contributions

We're proud to be an open-source development team. If you want to help contribute or improve Pollo, feel free to submit any issues or pull requests. You can also contact us at [team@cornellappdev.com](mailto:team@cornellappdev.com).

# Made by Cornell App Development

Cornell AppDev is an engineering project team at Cornell University dedicated to designing and developing mobile applications. We were founded in 2014 and have since released apps for Cornell and beyond, like [Eatery](https://itunes.apple.com/us/app/eatery-cornell-dining/id1089672962?mt=8). Our goal is to produce apps that benefit the Cornell community and the local Ithaca area as well as promote open-source development with the community. We have a diverse team of software engineers and product designers that collaborate to create apps from an idea to a reality. Cornell AppDev also aims to foster innovation and learning through training courses, campus initiatives, and collaborative research and development. For more information, visit our [website](http://www.cornellappdev.com) and follow us on [Instagram](https://www.instagram.com/cornellappdev/).

[Back to top ^](#)
