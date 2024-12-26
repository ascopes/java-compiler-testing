# Contribution guidelines and info

Hi! Thanks for taking an interest in this project! If you would like to contribute, then help is
always welcome however big or small!

## Finding something to contribute

If you are new and would like to pick something up, check the issues tab to see if anything is of 
interest. If you want further clarification, feel free to drop a comment.

> [!TIP]
> You could alternatively add a message on the discussions
> tab!

Contributions do not necessarily need to be code changes either. You can also contribute by:

- Testing this project out for yourself and discussing any feedback on the discussions page.
- Fixing and improving documentation.
- Contributing new Wiki pages.
- Raising bugs in the issue tab.

## Getting help

Please make a discussion in the discussions tab rather than opening a new issue if you need help
with using this library.

## Contributing changes to fix issues

If you are contributing to an issue, drop a message on that issue so that I can assign it to you! 
**If you would be able to keep me up to date with any progress on that issue, that would be 
fantastic**, since it enables me to track how much work is left to do and to be able to provide any
help if needed.

Everyone is entitled to their own life, but if I haven't heard anything back for a while, 
then I may unassign issues at my own discretion so that it can be worked on. If there is any
reason you may not be able to do something for a while, then just let me know.

This is just so I can keep track of what is being worked on without changes being proposed
but never actually finished.

## Branching

When you are ready, make a fork of this repo. When you  work on your fork, make sure you create
a new branch to work off of rather than committing to your master/main branch. This will make it
easier for you to update your copy with any new changes to this project, and avoid filling pull
requests with lots of messy merge commits unnecessarily!

## Building this project

> [!IMPORTANT]
> This project uses Apache Maven as the build system, and
> requires JDK 17 or newer. 

To build this project and run the tests, you can run the following in your terminal:

```bash
# Linux, Mac OS, Git Bash users
./mvnw clean package verify

# People using CMD on Windows
.\mvnw.cmd clean package verify
```

If you use Windows, you should download the JDK from somewhere such as 
https://adoptium.net/en-GB/temurin/.

On Linux and macOS, I tend to use a tool called [SDKMan](https://sdkman.io/)
to download and install Java tooling. I personally use Amazon Corretto, but you can use any JDK 
implementation you like, such as Temurin, Graal, Liberica, SAP, etc. All should work
the same for the most part.

## Licensing

All changes that you make will be applied under the Apache license, as documented in this
repository. You can find out more about what this means at [TLDRLegal](https://tldrlegal.com/license/apache-license-2.0-(apache-2.0))!

Each file that you create must have a special header comment at the top that mentions this license.

> [!TIP]
> If you need to add this header to your files, you can run
> `./mvnw license:format` to do it for you automatically!

## Code style

This project uses a modified version of the Google Code Style guide for Java. The main things to 
remember are:

- 2-space indentation rather than tabs
- Public classes and methods need a JavaDoc
- Line length is limited to 100 lines

Good things to remember:

- Keep lines of code simple. It is fine to use multiple lines of code to declare something if it
- makes it easier to read.
- Keep naming clear and simple!

A tool called CheckStyle will attempt to enforce these rules
for you.

> [!TIP]
> You can run `./mvnw checkstyle:check` to verify your code matches these rules.

## Commits

Please try to keep commits atomic and clear. Each commit should ideally leave the project in a
working state. Each commit should also have a clear title and explanation as to what the commit
changes, and why.

## Deployment process

Deployments to Maven Central are performed from GitHub Actions, and are triggered when we feel 
that it is an appropriate time to release a change.

Unless otherwise specified, deployments will occur from the `main` branch, so any PRs should
target that branch where possible.

Changelogs are automatically generated from the pull request history between the head of the 
`main` branch and the last tag that was added. This is why it is important to have descriptive
pull requests where appropriate.

Upon successfully deploying to Maven Central, a release will be added to the [releases](../../releases)
page on the repository. A changelog will automatically get attached and any contributors will be
`@mentioned` in those changelogs.
