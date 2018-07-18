# SmartNote

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 1.4.2.


## Introduction

SmartNote is a notes app based on Angualr 4, MongoDB and Node.js developed by Group Augo.


## Features
- Account management: sign in, sign out.
- Basic notes: Create, Retrieve, Update, Delete
- URL notes: generate note from URL automatically.
- Markdown notes: create a markdown note with its title, tag and content.
- Archive note: view all archived notes.
- Classification: classify note by tags.
- View note: preview url note and markdown note.
- Edit note: modify markdown note.
- Delete note.
- Tag notes: create tags and classify notes by tag.
- Search notes: search note by keyword or tag.


## Development 

1. Before run our application, you need to install mongodb.
- Open the Terminal app and type `brew update`.
- After updating Homebrew `brew install mongodb`
- After downloading Mongo, create the “db” directory. This is where the Mongo data files will live. You can create the directory in the default location by running `mkdir -p /data/db`
- Make sure that the /data/db directory has the right permissions by running

``sudo chown -R `id -un` /data/db``

2. Run `mongod` in the terminal to start the the Mongo daemon, this step starts the Mongo serever.

3. Before starting the application, you need to install some dependencies. Please reference the next section for details.
Run `npm run start` to start the application. Navigate to `http://localhost:4300/`. The app will automatically reload if you change any of the source files.

4. For Server part, go to folder server by `cd server/`.
Run `npm install` and `npm run dev`.

5. Exit mongo service:
- To exit the Mongo shell run `quit()`
- to quit the Mongo daemon, hit `ctrl-c`.

6. Backend Testing:
- To install Mocha and Chai using 
`npm install -g mocha`.
`npm install chai`.
`npm install supertest --save-dev`.
And to run backend test in the folder /smartnote using 
`mocha test.js `.


## Dependencies
For markdown directives, you need to install some libraries.

Install SimpleDEM and Highlight for markdown note features.

1. navigate to app root directory(./smart-note), run `npm install simplemde --save` and `npm install angular-highlightjs --save` to install dependencies for markdown functionality.

2. navigate to ./server, run `npm install nodemon --save`


## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `-prod` flag for a production build.


## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).


## Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via [Protractor](http://www.protractortest.org/).
Before running the tests make sure you are serving the app via `ng serve`.