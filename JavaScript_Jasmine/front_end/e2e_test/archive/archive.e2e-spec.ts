import { Archive } from './archive.po';
import { browser } from 'protractor';

// sleep for demonstration 
function sleep() {
    browser.driver.sleep(2000); 
}

describe('Archive Page', () => {
  let page: Archive;

  beforeEach(() => {
    page = new Archive();
  });

  // display app title
  it('should get the title', () => {
    page.navigateTo();
    sleep();
    expect(page.getTitle()).toEqual('SmartNote');
  });


});
