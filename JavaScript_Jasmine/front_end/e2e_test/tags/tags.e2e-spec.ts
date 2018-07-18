import { Tags } from './tags.po';
import { browser, protractor } from 'protractor';

// sleep for demonstration 
function sleep() {
    browser.driver.sleep(2000); 
}

describe('Tag Page', () => {
  let page: Tags;

  beforeEach(() => {
    page = new Tags();
  });

  // display app title
  it('should get the title', () => {
    page.navigateTo();
    sleep();
    expect(page.getTitle()).toEqual('SmartNote');
  });

  it('should not add an empty tag', ()=> {
    var newTag = page.getNewTag()
    newTag.sendKeys('');
    newTag.sendKeys(protractor.Key.ENTER);        
    expect(newTag.getAttribute('value')).not.toEqual('DemoTag');  
    browser.driver.sleep(3000); 
  });
  
  it('should be able to add the new tag to the tag list', () => {
    var newTag = page.getNewTag()
    newTag.sendKeys('DemoTag');
    expect(newTag.getAttribute('value')).toEqual('DemoTag'); 
    sleep();        
    newTag.sendKeys(protractor.Key.ENTER);    
  });
  
});
