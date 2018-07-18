import { browser, by, element } from 'protractor';

export class About {
  navigateTo() {
    return browser.get('/about');
  }
  
  // get the app title
  getTitle() {
    return browser.getTitle();
  }

  // get the app name
  getAppName() {
    //return element(by.css('.header h1')).getText();
    return element(by.id('app-name')).getText();
  }
  
  // get info
  getAppInfo() {
    return element(by.id('app-info')).getText();
  }
}
