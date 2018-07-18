import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SearchComponent } from './search.component';
import { FilterNoteContentPipe } from './../classification/classification.component';
import { NoteService } from '../../services/note/note.service';
import { HttpClientModule, HttpClient } from '@angular/common/http';
import { RouterTestingModule } from '@angular/router/testing';

import { FormsModule } from '@angular/forms';

describe('SearchComponent', () => {
  let component: SearchComponent;
  let fixture: ComponentFixture<SearchComponent>;

  const searchname = 'SmartNote';
  const note = [{title: 'SmartNote is cool', date:'2017-08-29 22:53:30', content: 'Welcome to SmartNote, you will love it!'}, {title: 'Software Engineering', date:'2017-11-05 09:00:38',
  contenet: 'This is an awesome class, keep on coding!'}];

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        HttpClientModule,
        RouterTestingModule,
      ],
      declarations: [
        SearchComponent,
        FilterNoteContentPipe ],
      providers: [
        NoteService
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    component.searchName = searchname;
    component.resultNoteList = note;
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should have keywords for title and tag to be searched', () => {
    expect(component.searchName).toBeTruthy();
  });

});
