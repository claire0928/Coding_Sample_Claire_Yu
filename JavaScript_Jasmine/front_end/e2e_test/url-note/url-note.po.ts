import { browser, by, element } from 'protractor';

export class URLNotes {
  navigateTo() {
    return browser.get('/addLinkNote');
  }

  // get the app title
  getTitle() {
    return browser.getTitle();
  }
  
  getNoteTitle() {
    return element(by.id('urlnote-title'));
  }
}