# Contribution guidelines and info

Hi! Thanks for taking an interest in this project!
If you would like to contribute, then help is always
welcome however big or small!

## Finding something to contribute

If you are new and would like to pick something up,
check the issues tab to see if anything is of interest.
If you want further clarification, feel free to drop a
comment.

You could alternatively add a message on the discussions
tab!

Contributions do not necessarily need to be code changes
either. You can also contribute by:

- Testing this project out for yourself and discussing any
  feedback on the discussions page.
- Fixing and improving documentation.
- Contributing new Wiki pages.
- Raising bugs in the issue tab.

## Raising issues

If raising an issue, please make sure that you include
some details such as:

- The problem in a short description.
- What do you expect to happen?
- What actually happens?
- How did you reproduce this (if applicable)?
- What JDK you are using
- What OS you are using

If these are not relevant to you, then they can be
ignored.

## Getting help

Please make a discussion in the discussions tab rather
than opening a new issue if you need help with something!

## Contributing changes to fix issues

If you are contributing to an issue, drop a message on
that issue so that I can assign it to you! If you would be
able to keep me up to date with any progress on that issue,
that would be fantastic, since it enables me to track
how much work is left to do and to be able to provide any
help if needed.

## Branching

When you are ready, make a fork of this repo. When you
work on your fork, make sure you create a new branch to
work off of rather than committing to your master/main
branch. This will make it easier for you to update your
copy with any new changes to this project, and avoid
filling pull requests with lots of messy merge commits
unnecessarily!

## Building this project

This project uses Apache Maven as the build system, and
requires JDK 11 or newer. 

To build this project and run the tests, you can run the
following in your terminal:

```bash
# Linux, Mac OS, Git Bash users
./mvnw clean package verify

# People using CMD on Windows
.\mvnw.cmd clean package verify
```

If you use Windows, you should download the JDK from
somewhere such as https://adoptium.net/en-GB/temurin/.

On Linux and MacOS, I tend to use a tool called
[SDKMan](https://sdkman.io/) to download and install
Java tooling. I personally use Amazon Corretto 17, but
you can use any JDK implementation you like, such as
Temurin, Graal, Liberica, SAP, etc. All should work
the same for the most part.

## Licensing

All changes that you make will be applied under the
Apache license, as documented in this repository.
You can find out more about what this means at
[TLDRLegal](https://tldrlegal.com/license/apache-license-2.0-(apache-2.0))!

Each file that you create must have a special header
comment at the top that mentions this license. If you
need to add this header to your files, you can run
`./mvnw license:format` to do it for you automatically!

## Code style

This project uses a modified version of the Google
Code Style guide for Java. The main things to remember
are:

- 2-space indentation rather than tabs
- Public classes and methods need a JavaDoc
- Line length is limited to 100 lines

Good things to remember:

- Keep lines of code simple. It is fine to use multiple
  lines of code to declare something if it makes it easier
  to read.
- Keep naming clear and simple!

A tool called CheckStyle will attempt to enforce these rules
for you. You can run `./mvnw checkstyle:check` to verify
your code matches these rules.

## Commits

Please try to keep commits atomic and clear. Each commit
should ideally leave the project in a working state. Each
commit should also have a clear title and explaination as
to what the commit changes, and why.
