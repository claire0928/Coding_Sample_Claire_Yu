import { style } from '@angular/animations';
import { Router } from '@angular/router';
import { MsgService } from './../../services/msg/msg.service';
import { NoteService } from './../../services/note/note.service';
import { Component, OnInit, ViewEncapsulation, ElementRef, Input } from '@angular/core';
import marked from 'marked';
import highlight from 'highlight.js';

@Component({
  selector: 'app-view-note',
  templateUrl: './view-note.component.html',
  styleUrls: ['./view-note.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class ViewNoteComponent implements OnInit {
  isShowEdit = false;
  noteInfo = {
    content: '',
    date: String,
    sourceLink: '',
    tag: Array,
    title: String,
    __v: Number,
    _id: String
  };
  isShowDelect = false;
  catalog = [];

  constructor(
    private noteService: NoteService,
    private msgService: MsgService,
    private router: Router
  ) {  }

  ngOnInit() {
    this.noteInfo = JSON.parse(localStorage.getItem('noteItemInfo'));
    this.noteInfo.sourceLink === '' ? this.isShowEdit = true : this.isShowEdit = false;
    this.noteInfo.content =  marked(this.noteInfo.content, {
      renderer: new marked.Renderer(),
      gfm: true,
      pedantic: false,
      sanitize: false,
      tables: true,
      breaks: true,
      smartLists: true,
      smartypants: true,
      highlight: function (code) {
        return highlight.highlightAuto(code).value;
      }
    });

    const contentDom = this.parseDom(this.noteInfo.content); // convert html string to DOM
    // extract note info to generate index info
    Array.prototype.slice.call(contentDom.querySelectorAll('h1,h2,h3,h4,h5,h6')).forEach((item, index) => {
      item.id = item.localName + '-' + index;
      let active;
      index === 0 ? active = true : active = false;
      this.catalog.push({
        tagName: item.localName,
        text: item.innerText,
        href: '#' + item.localName + '-' + index,
        el: item,
        isActive: active
      });
    });
    const previewDom  = <HTMLElement>document.querySelector('.preview');
    const catalogDom = <HTMLElement>document.querySelector('.catalog-wrap');
    previewDom.appendChild(contentDom);
    if (this.catalog.length === 0) {
      return;
    }
    previewDom.addEventListener('scroll', this.throttle(() => {
      this.catalog.forEach((item, index) => {
        if (index !== this.catalog.length - 1) {
          if ((previewDom.scrollTop + 125) > item.el.offsetTop && (previewDom.scrollTop + 125) < this.catalog[index + 1].el.offsetTop) {
            this.catalog.forEach(j => j.isActive = false);
            item.isActive = true;
          }
        }else {
          if ((previewDom.scrollTop + 125) > item.el.offsetTop) {
            this.catalog.forEach(j => j.isActive = false);
            item.isActive = true;
          }
        }
      });
      const activeDom = <HTMLElement>catalogDom.querySelector('.active');
      catalogDom.scrollTop = activeDom.offsetTop - window.innerHeight / 2;
    }, 200));

  }

  delect() {
    this.isShowDelect = true;
  }

  cancel() {
    this.isShowDelect = false;
  }

  confirmDelete() {
    const id = this.noteInfo._id;
    this.noteService._deleteNote(id).subscribe((res) => {
      if (res['data'].ok === 1) {
        this.isShowDelect = false;
        this.msgService.info('Sucessfully Deleted!');
        this.noteService._updateAllNote();
        this.router.navigate(['/classification']);
      }
    });
  }

  editNote() {
    this.router.navigate(['/editNote']);
  }

  catalogNavigation(data) {
    const previewDom  = <HTMLElement>document.querySelector('.preview');
    const activeNode = <HTMLElement>previewDom.querySelector(`${data.href}`);
    previewDom.scrollTop = activeNode.offsetTop - 120;
  }

  private parseDom(arg) {
    const objl = document.createElement('div');
    objl.innerHTML = arg;
    return objl;
  }

  private throttle(fn, interval = 300) {
    let canRun = true;
    return function () {
      if (!canRun) {
        return;
      }
      canRun = false;
      setTimeout(function() {
        fn.apply(this, arguments);
        canRun = true;
      }, interval);
    };
  }
}
