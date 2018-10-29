# Future Messenger
 **An free, open source, ad-free app for Android that allows you to schedule SMS messages ahead of time! Just schedule a message and it'll be sent automatically at the requested time!**
 ## Version History & Downloads 
 | Version        | Developers           | Release Date  | Notes | Download | Android Version Requirements |
| ------------- |:-------------:| :-----:| :-----| :-----:| :-----: |
| 1      | Samarth Dhruva, Billy Do, and Arthur Storey | August 14, 2016 | Created as part of the Summer 2016 CS371m Mobile Computing class at UT Austin, taught by Michael Scott. | [Version 1 APK Download](https://github.com/samarthd/FutureMessengerApp/blob/master/FutureMessenger-V1.apk)| Android 4.2+, AKA API 17 |
| 2      | Samarth Dhruva |   October 28, 2018 | A full rewrite from scratch, with UI and functional improvements. | [Version 2 APK Download ](https://github.com/samarthd/FutureMessengerApp/blob/master/FutureMessenger-V2.apk) | Android 5.1+, AKA API 21 |

## Why is it open source, free, and also ad-free? What's the catch?
No catch. This app was really just supposed to be a good learning experience for me, and I don't feel right about ruining the user experience with ads or charging people money for an app that has such a niche use case. It's open source because I personally enjoy open source software and I've got nothing to hide from my users! I want to have their trust when they use it!


## Why isn't this on the official Google Play Store?
Great question. The answer brings up a sore spot for me, but I'm happy to divulge the history of this app.

- Originally, the app was on the Play Store at [this link](https://play.google.com/store/apps/details?id=cs371m.hermes.futuremessenger), which is now broken, for about a year and a half. We even had a [nice web advertisement for the app](http://futuremessengerapp.wixsite.com/main), which is still up as of October 28, 2018.
Then came the massive sweeping Play Store policy changes in February 2018 due to the [Facebook Cambridge Analytica scandal](https://www.cnbc.com/2018/04/10/facebook-cambridge-analytica-a-timeline-of-the-data-hijacking-scandal.html), and because I was too busy with school to draft up an official privacy policy for the app to comply with the 
new requirements, it was removed from the store. 

    - Note: **The app never did (and still doesn't) collect any information, and stays *entirely offline and local to your device*** - trust me, I'm a privacy nut and would be as skeptical as you. The Play Store policy changes required me to specifically update the app's Play Store listing with an official *statement* about how I handle user data. This, like I said, I was too busy to do, and thus it was removed. There was never any dispute over the actual behavior of the app. And if you don't believe me, you don't have to! Just look at the code yourself and verify my claims! It's open source, I've got nothing to hide. 😊

- On July 6, 2018, I started working on a full rewrite of the app (Version 2), as part of a recommitment to Android development after I got out of school. My plan was actually to finish version 2 and then put it back on the Play Store with full compliance to the new policies! I had gotten about two months into the development of it, when [news broke about unsecured Google+ APIs potentially leaking user data](https://www.theverge.com/2018/10/8/17951890/google-plus-shut-down-security-api-change-gmail-android).
    - As part of the effort to manage the PR nightmare, Google launched [Project Strobe](https://www.blog.google/technology/safety-security/project-strobe/), a set of measures to regain user trust, including shutting down Google+ and some policy updates for Android developers! 
    - Here's a quote from Google: 
        - >Finding 4: When users grant SMS, Contacts and Phone permissions to Android apps, they do so with certain use cases in mind.   
        - >Action 4: We are limiting apps’ ability to receive Call Log and SMS permissions on Android devices, and are no longer making contact interaction data available via the Android Contacts API.
        - >Some Android apps ask for permission to access a user’s phone (including call logs) and SMS data. Going forward, Google Play will limit which apps are allowed to ask for these permissions.  ***Only an app that you’ve selected as your default app for making calls or text messages will be able to make these requests.*** 

    - Another more detailed look at permission requirements is found [here](https://play.google.com/about/privacy-security-deception/permissions/).
        - >Activity: Your app manifest requests the SMS permission group (e.g. READ_SMS, SEND_SMS, WRITE_SMS, RECEIVE_SMS, RECEIVE_WAP_PUSH, RECEIVE_MMS)

        - >Requirement: It must be actively registered as the default SMS or Assistant handler on the device. 

    - This meant that my app was now in violation of Google Play's policies for its core functionality! Future Messenger cannot (and was never intended to) function as a complete SMS app, it's just a scheduling/sending utility! It needs the `SEND_SMS` permission in order to automatically send messages! I was super defeated when I heard the news, but I decided that I wouldn't let my hard work completely go to waste. I finished up the app and put the `.apk` (linked above) on GitHub so any lone wanderers (like yourself) could still download and use it.

- At the time this is being written, the permissions requirements are purely in the policy of the Play Store, and there is no hard code-based enforcement of permissions in Android. ***This means that you can still download the app from the links above, and it'll work for you.***
It has been tested to work properly on all versions of Android from 5.1 (Lollipop) to 9.0 (Pie). Future versions of Android may hard-enforce the aforementioned permissions requirements and thus the app may not work on them. **Until then, though, enjoy the app, and feel free to hit me up if you have any comments/feedback! 👍**

## Screenshots
![Main activity](https://github.com/samarthd/FutureMessengerApp/blob/master/screenshots/main_activity.jpg)
![Message drafting](https://github.com/samarthd/FutureMessengerApp/blob/master/screenshots/message_draft.jpg)
![Sent notification](https://github.com/samarthd/FutureMessengerApp/blob/master/screenshots/sent_notification.jpg)
