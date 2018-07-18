import { browser, by, element } from 'protractor';

export class Archive {
  navigateTo() {
    return browser.get('/archive');
  }

  // get the app title
  getTitle() {
    return browser.getTitle();
  }

}
