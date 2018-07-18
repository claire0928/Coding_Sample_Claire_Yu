import { About } from './about.po';
import { browser } from 'protractor';

// sleep for demonstration 
function sleep() {
    browser.driver.sleep(2000); 
}
  
describe('About page', () => {
  let page: About;

  beforeEach(() => {
    page = new About();
  });

  // display app title
  it('should get the title', () => {
    page.navigateTo();
    sleep();    
    expect(page.getTitle()).toEqual('SmartNote');
  });
});
