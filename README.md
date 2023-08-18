# DiminishingColoursBot

Webtoon notifications via Discord! (So they actually work!)  
so this is a bot i made because i was fed up with Webtoon subscription email **never working**. so this instead requests the rss of subscribed webtoons and sends messages to channels as notifications instead  
**it checks every 5 minutes**  
most of the interacting with the bot will not be done using the GUI that this launches with, instead, mostly using Slash Commands. for a bot you're hosting on your own, the bot is structured in a way that provides customisability per channel, and can handle any reasonable number of channels with any reasonable number of Webtoons, suggesting it could also just be hosted by me, but oh well.

the first time you launch the .jar you will be prompted for your bot's token, then it saves it for future use.

`/webtoon` and its subcommands deal with the subscribing and unsubscribing of webtoons  
`/notify` deals with what changes in the subscribed webtoons a channel will care about  
`/message` and its subcommands deal with the messages that get sent when change a channel cares about happens


### [Download](https://github.com/Canary-Prism/DiminishingColoursBot/releases/)

I'm assuming you know how to use GitHub. If not then here:

### Download Steps

1. Click above link
2. Find latest release
3. Find "Assets" Section
4. Click the "WebToonLinkx.y.z.jar" file

### Notice

This program uses Java, you should download and install Java Development Kit (JDK) if you haven't already to run this

#### [Here's a handy link](https://www.oracle.com/java/technologies/downloads/)

(yes ik it should only need jre if it's compiled but idk how i managed to screw up my code so much that you still need jdk but here we are. If you manage to get this working with jre only plz tell me how and you will have earned the right to call me moron 20 times)

## Historical Changelog

### 0.1
* Made a thing, i haven't even tested it yet