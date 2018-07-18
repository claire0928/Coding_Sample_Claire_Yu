import { browser, by, element } from 'protractor';

export class Addnote {
  navigateTo() {
    return browser.get('/addNote');
  }

  // get the app title
  getTitle() {
    return browser.getTitle();
  }
  getNoteTitle() {
    return element(by.id('note-title'));
  }

  getSaveButton() {
    return element(by.id('save-button'));
  }

  getTag() {
    //return element(by.id('tag-dropdown'));
    return element(by.cssContainingText('li', 'DemoTag'));
  }
  getTagList() {
    return element(by.css('my-dropdown-menu'));
  }
}
