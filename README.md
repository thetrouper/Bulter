# Meteor Butler Addon

Easily control multiple instances of meteor client through a single account.

### How to build:  
- Make sure you have [Git](https://git-scm.com/download/win) installed
  - If you're on linux, you can figure it out.
- Clone into the repo and CD into it
  - open a terminal 
  - run `git clone https://github.com/thetrouper/Bulter && cd Butler`
- Build the jar
  - in the cloned directory, run `./gradlew build`
  - If you are on linux, make sure to `chmod +x gradlew` or it wont run
- The built jar should be in `build/libs/meteor-butler-<version>.jar`

### Modules
- Swarm Plus Worker
  - Used to connect to the host and receive commands
  - Verbose option will send what you receive from the server in your chat
  - Currently, connections will appear to fail, but it still works fine. Please do not open an issue unless it throws an error, then actually doesn't work. The errors are not visible to the host.
- Swarm Plus Host
  - Used to host a server that can be connected to by the workers
  - Verbose will display connection handshake packets
### Commands
- .manager
  - View github wiki for usage guide
