# jWormhole Mailer Plugin

This is a plugin, that facilitates email testing, for
[jWormhole client](https://github.com/vvasabi/jwormhole-client). By using this plugin, it is
possible to test emails created on localhost without having to upload assets to web servers first.


## Requirements

* jWormhole client & server
* SMTP server


## Installation

* Install jWormhole client jar by running `mvn install` in jWormhole client project.
* Create plugin jar by running `mvn package` in this project’s folder.
* Create `$HOME/.jwormhole/plugins` directory if it does not already exist.
* Move `target/jwormhole-mailer-mailer-1.0-SNAPSHOT-all-deps.jar` to `$HOME/.jwormhole/plugins`.
* Update `$HOME/.jwormhole/client.properties` (see the next section).


## Configuration

This plugin uses the same configuration file as jWormhole client. Add the following lines to
`$HOME/.jwormhole/client.properties`, uncomment lines and edit as necessary.

```
jwormhole.client.console.commandHandlers = com.bradchen.jwormhole.mailer.MailCommandHandler

# SMTP server login details
#jwormhole.mailer.server =
#jwormhole.mailer.port = 25
#jwormhole.mailer.username =
#jwormhole.mailer.password =
#jwormhole.mailer.smtps = false
#jwormhole.mailer.startTls = false

# Mailer settings
#jwormhole.mailer.fromName = jWormhole
#jwormhole.mailer.fromEmail = jWormhole@localhost
```


## Usage
Run jWormhole client as usual (see [jWormhole client](https://github.com/vvasabi/jwormhole-client)
for more details), and issue the following command to send out an email:

```
mail "subject line" http://localhost/path/to/email.html recipient@example.com
```

To resend an email, issue `rs`.


## License

```
  Copyright 2014 Brad Chen

  Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
