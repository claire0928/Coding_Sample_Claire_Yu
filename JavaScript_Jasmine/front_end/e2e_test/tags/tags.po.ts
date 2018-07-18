import { browser, by, element } from 'protractor';

export class Tags {
  navigateTo() {
    return browser.get('/tag');
  }

  // get the app title
  getTitle() {
    return browser.getTitle();
  }

  // add a new tag
  getNewTag() {
    return element(by.id("new-tag"));
  }

  // get all the tags
  getAllTags() {
    return element.all(by.css(".tag .tag-list-wrap .tag-item"));
  }

}
