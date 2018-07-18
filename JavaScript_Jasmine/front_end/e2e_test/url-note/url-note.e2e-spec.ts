import { URLNotes } from './url-note.po';
import { browser, protractor} from 'protractor';

// sleep for demonstration 
function sleep() {
    browser.driver.sleep(10000); 
}

describe('URL note Page', () => {
  let page: URLNotes;

  beforeEach(() => {
    page = new URLNotes();
  });

  // display app title
  it('should get the title', () => {
    page.navigateTo();
    // sleep();
    expect(page.getTitle()).toEqual('SmartNote');
  });

  it('should not enter an empty URL', ()=> {
    let noteTitle = page.getNoteTitle();
    noteTitle.sendKeys('');
    noteTitle.sendKeys(protractor.Key.ENTER);        
    expect(noteTitle.getAttribute('value')).not.toEqual('some url');  
  });
  
  it('should create a URL note', ()=>{
    let noteTitle = page.getNoteTitle();
    noteTitle.sendKeys('https://www.cise.ufl.edu/~adobra/cen5035');
    noteTitle.sendKeys(protractor.Key.ENTER);    
    sleep();    
    expect(noteTitle.getAttribute('value')).toEqual('https://www.cise.ufl.edu/~adobra/cen5035'); 
  });


});
