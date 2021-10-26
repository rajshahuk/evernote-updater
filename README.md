# Evernote Updater

## Why have I got this?

I use Evernote a lot. One of the workflows that I have is to scan all my paper documentation using a Doxie scanner,
PDFing it and then uploading to Evernote. I do this in batches and use the following convention to name my files:

```
yyyyMMdd-title for evernote post.pdf
```

for example

```
20160629-Some paper bank statement.pdf
```

When Evernote creates the note it uses the creation date as the date it was added to Evernote. Now that's not great,
what you actually want is the date that the paper thing was created. Luckily I already have this in the filename so what
this small bit of Java does is to take the `yyyyMMdd` at the beginning of the filename and change the date using the
Evernote API.

## Building the code

It's Java so just do:

```
mvn install
```

## Running the code

This is a really hackie piece of code, I'm tidy it up but just run the main method in the `App` class.

## Some things to note

You need to get your Evernote API key from your account. You can do this by logging into Evernote on the web and getting
it from there. You need to copy the `.sample_auth` file over to `.auth` and add your key to this file.

Get an auth token for production by visiting this link:

https://www.evernote.com/api/DeveloperToken.action
