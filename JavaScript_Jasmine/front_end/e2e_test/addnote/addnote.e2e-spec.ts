import { Addnote } from './addnote.po';
import { by, browser, protractor } from 'protractor';

// sleep for demonstration 
function sleep() {
    browser.driver.sleep(3000); 
  }
  
var selectDropdownbyNum = function ( element, optionNum ) {
    if (optionNum){
        var options = element.findElements(by.css('my-dropdown-menu'))   
        .then(function(options){
            options[optionNum].click();
        });
    }
};

describe('Addnote page', () => {
  let page: Addnote;

  beforeEach(() => {
    page = new Addnote();
  });
  // display app title
  it('should get the title', () => {
    page.navigateTo();
    sleep();    
    expect(page.getTitle()).toEqual('SmartNote');
  });

  it('should not save incomplete note', ()=> {
    let saveButton = page.getSaveButton();
    saveButton.click();      
    browser.driver.sleep(2000); 
    let notePage = browser.getCurrentUrl();
    expect(browser.getCurrentUrl()).toEqual(notePage);
  });

  it('should create a new note', ()=>{
    let noteTitle = page.getNoteTitle();
    
    noteTitle.sendKeys('This is a note for demo3');

    expect(noteTitle.getAttribute('value')).toEqual('This is a note for demo3');     
    noteTitle.sendKeys(protractor.Key.ENTER);    
    sleep(); 
    noteTitle.clear();
    noteTitle.sendKeys('This is another note for demo3');
    sleep();     
  });

});